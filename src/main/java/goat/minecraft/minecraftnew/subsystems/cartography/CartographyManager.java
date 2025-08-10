// /mnt/data/CartographyManager.java
package goat.minecraft.minecraftnew.subsystems.cartography;

import goat.minecraft.minecraftnew.MinecraftNew;
import org.bukkit.*;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Biome;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapPalette;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;
import org.bukkit.Rotation;

import java.io.*;
import java.time.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public final class CartographyManager implements Listener {

    private static final int MAX_WALL = 16;
    private static final MapView.Scale TILE_SCALE = MapView.Scale.NORMAL;
    private static final long TELEPORT_SAFE_SCAN = 24;
    private static final NamespacedKey WALL_KEY = NamespacedKey.minecraft("cont_worldmap_wall");
    private static final String SAVE_FILE = "cartography2_walls.yml";

    // Expansion behavior & tuning
    // Aggressiveness controls (you can tweak these). With NO_THROTTLE=true we ignore per-tick budgets
    // and fully render the wall in one asynchronous pass.
    private static final boolean NO_THROTTLE = true;          // render in one go (async)
    private static final int EXPAND_SEED_GRID_COLS = 3;       // number of initial BFS seeds horizontally (1=center only)
    private static final int EXPAND_SEED_GRID_ROWS = 3;       // number of initial BFS seeds vertically   (1=center only)
    private static final boolean USE_EIGHT_WAY_EXPANSION = true; // 4-way vs 8-way neighbors (8 spreads faster)
    // Legacy budgets (kept for easy re-enable later). Ignored when NO_THROTTLE=true.
    private static final int EXPAND_WARMUP_TICKS = 0;
    private static final int EXPAND_WARMUP_BUDGET = Integer.MAX_VALUE;
    private static final int EXPAND_STEADY_BUDGET = Integer.MAX_VALUE;

    private final Plugin plugin;
    private final File dataFile;

    private final Map<UUID, WorldMapWall> walls = new ConcurrentHashMap<>();
    private final Map<UUID, UUID> frameToWall = new ConcurrentHashMap<>();

    public CartographyManager(Plugin plugin) {
        this.plugin = plugin;
        this.dataFile = new File(plugin.getDataFolder(), SAVE_FILE);
        Bukkit.getPluginManager().registerEvents(this, plugin);
        loadAll();
        scheduleMidnightRefresh();

        // Admin command to manually force all maps to refresh their pixels
        MinecraftNew.getInstance().getCommand("refreshmaps").setExecutor((sender, command, label, args) -> {
            if (!sender.hasPermission("continuity.admin")) {
                sender.sendMessage(ChatColor.RED + "You do not have permission to do that.");
                return true;
            }
            refreshAll();
            sender.sendMessage(ChatColor.GREEN + "Refreshing all world maps...");
            return true;
        });
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onPlaceWorldMap(PlayerInteractEvent e) {
        if (e.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (e.getHand() != EquipmentSlot.HAND) return;
        if (e.getClickedBlock() == null) return;

        ItemStack inHand = e.getItem();
        if (inHand == null || inHand.getType() != Material.MAP) return;

        BlockFace face = e.getBlockFace();
        Block centerBlock = e.getClickedBlock();
        Player p = e.getPlayer();

        if (face == BlockFace.DOWN) {
            p.sendMessage(ChatColor.RED + "Cannot place a world map on a ceiling.");
            return;
        }

        e.setCancelled(true);

        // figure out u/v faces from clicked face
        BlockFace uFace, vFace;
        switch (face) {
            case UP, DOWN -> { // floor map
                uFace = BlockFace.EAST;
                vFace = BlockFace.SOUTH;
            }
            case NORTH, SOUTH -> { // wall map oriented east-west
                uFace = BlockFace.EAST;
                vFace = (face == BlockFace.NORTH) ? BlockFace.UP : BlockFace.DOWN;
            }
            case EAST, WEST -> { // wall map oriented north-south
                uFace = BlockFace.SOUTH;
                vFace = (face == BlockFace.EAST) ? BlockFace.UP : BlockFace.DOWN;
            }
            default -> { return; }
        }

        int w = Math.min(MAX_WALL, Math.max(1,  e.getPlayer().isSneaking() ? 3 : 1));
        int h = Math.min(MAX_WALL, Math.max(1,  e.getPlayer().isSneaking() ? 3 : 1));

        WorldMapWall wall = new WorldMapWall(this, plugin, centerBlock.getWorld(), centerBlock, face, uFace, vFace, w, h);
        walls.put(wall.id, wall);
        indexFrames(wall);

        p.sendMessage(ChatColor.GRAY + "Placed " + w + "x" + h + " world map. Break a frame to destroy map. Right click to get the coordinates clicked.");
        p.sendMessage(ChatColor.GREEN + "Left click with an ender pearl to teleport to clicked location on the map");

        wall.startExpander(false);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onRightClickFrame(PlayerInteractAtEntityEvent e) {
        if (!(e.getRightClicked() instanceof ItemFrame frame)) return;
        UUID wallId = frameToWall.get(frame.getUniqueId());
        if (wallId == null) return;

        WorldMapWall wall = walls.get(wallId);
        if (wall == null) return;

        Player p = e.getPlayer();
        ItemStack item = p.getInventory().getItemInMainHand();
        // Use Traveler's Brush to expand map from clicked pixel
        if (item != null && item.hasItemMeta() && item.getItemMeta().hasDisplayName()
                && item.getItemMeta().getDisplayName().equals(ChatColor.YELLOW + "Travelers Brush")) {
            e.setCancelled(true);
            WorldMapWall.Point tile = wall.lookupTileIndex(frame.getUniqueId());
            if (tile == null) return;
            Vector pos = e.getClickedPosition();
            if (pos == null) return;
            int localPx, localPz;
            switch (wall.clickedFace) {
                case NORTH, SOUTH -> {
                    localPx = (int) Math.floor(pos.getX() * 128);
                    localPz = (int) Math.floor((1 - pos.getY()) * 128);
                }
                case EAST, WEST -> {
                    localPx = (int) Math.floor(pos.getZ() * 128);
                    localPz = (int) Math.floor((1 - pos.getY()) * 128);
                }
                case UP, DOWN -> {
                    localPx = (int) Math.floor(pos.getX() * 128);
                    localPz = (int) Math.floor(pos.getZ() * 128);
                }
                default -> { return; }
            }
            localPx = Math.max(0, Math.min(127, localPx));
            localPz = Math.max(0, Math.min(127, localPz));
            int globalPx = tile.i * 128 + localPx;
            int globalPz = tile.j * 128 + localPz;
            wall.expandFromPixel(globalPx, globalPz, 5000);
            if (item.getAmount() > 1) item.setAmount(item.getAmount() - 1);
            else p.getInventory().removeItem(item);
            return;
        }

        // Show map coords on right-click with empty hand
        if (item == null || item.getType() == Material.AIR) {
            e.setCancelled(true);
            WorldMapWall.Point tile = wall.lookupTileIndex(frame.getUniqueId());
            if (tile == null) return;
            int px = wall.worldToPixelX(wall.views[tile.j][tile.i], e.getRightClicked().getLocation().getBlockX());
            int pz = wall.worldToPixelZ(wall.views[tile.j][tile.i], e.getRightClicked().getLocation().getBlockZ());
            p.sendMessage(ChatColor.YELLOW + "Map pixel: " + px + ", " + pz);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onLeftClickFrame(EntityDamageByEntityEvent e) {
        if (!(e.getEntity() instanceof ItemFrame frame)) return;
        if (!(e.getDamager() instanceof Player p)) return;
        UUID wallId = frameToWall.get(frame.getUniqueId());
        if (wallId == null) return;

        e.setCancelled(true);

        ItemStack held = p.getInventory().getItemInMainHand();
        if (held != null && held.getType() == Material.ENDER_PEARL) {
            WorldMapWall wall = walls.get(wallId);
            WorldMapWall.Point tile = wall.lookupTileIndex(frame.getUniqueId());
            if (tile == null) return;

            MapView view = wall.views[tile.j][tile.i];
            int wx = view.getCenterX();
            int wz = view.getCenterZ();

            Location target = new Location(frame.getWorld(), wx, frame.getWorld().getHighestBlockYAt(wx, wz) + 1, wz);

            RayTraceResult rtr = frame.getWorld().rayTraceBlocks(target.clone().add(0, TELEPORT_SAFE_SCAN, 0), new Vector(0,-1,0), TELEPORT_SAFE_SCAN);
            if (rtr != null && rtr.getHitBlock() != null) {
                Block b = rtr.getHitBlock().getRelative(BlockFace.UP);
                target.setX(b.getX() + 0.5);
                target.setY(b.getY());
                target.setZ(b.getZ() + 0.5);
            }
            p.teleport(target);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onBreak(HangingBreakEvent e) {
        if (!(e instanceof HangingBreakByEntityEvent)) return;
        if (!(e.getEntity() instanceof ItemFrame frame)) return;
        UUID wallId = frameToWall.get(frame.getUniqueId());
        if (wallId == null) return;

        WorldMapWall wall = walls.remove(wallId);
        if (wall == null) return;

        // cleanup all frames
        for (UUID[][] row : new UUID[][][]{wall.frames}) {
            // noop (placeholder if you keep extra structures)
        }
        // remove all frame->wall index entries
        frameToWall.entrySet().removeIf(kv -> kv.getValue().equals(wallId));
        saveAll();

        e.setCancelled(false);
    }

    private void scheduleMidnightRefresh() {
        long delay = ticksUntilNextMidnight();
        long day = 20L * 60L * 60L * 24L;
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (WorldMapWall w : walls.values()) w.startExpander(false);
        }, delay, day);
    }

    /** Forces all known walls to rebuild their pixel caches. */
    private void refreshAll() {
        for (WorldMapWall w : walls.values()) w.startExpander(false);
    }

    private long ticksUntilNextMidnight() {
        ZoneId zone = ZoneId.systemDefault();
        ZonedDateTime now = ZonedDateTime.now(zone);
        ZonedDateTime next = now.plusDays(1).toLocalDate().atStartOfDay(zone);
        return Duration.between(now, next).getSeconds() * 20L;
    }

    private void saveAll() {
        try {
            FileConfiguration cfg = new YamlConfiguration();
            List<Map<String, Object>> list = new ArrayList<>();
            for (WorldMapWall w : walls.values()) list.add(w.serialize());
            cfg.set("walls", list);
            cfg.save(dataFile);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    private void loadAll() {
        if (!dataFile.exists()) return;
        FileConfiguration cfg = YamlConfiguration.loadConfiguration(dataFile);
        List<Map<String, Object>> list = (List<Map<String, Object>>) cfg.getList("walls", Collections.emptyList());
        for (Map<String, Object> map : list) {
            WorldMapWall w = WorldMapWall.deserialize(this, plugin, plugin.getServer(), map);
            if (w == null) continue;
            walls.put(w.id, w);
            indexFrames(w);
            if (w.needsRefresh()) w.startExpander(false);
        }
    }

    private void indexFrames(WorldMapWall w) {
        for (int j = 0; j < w.h; j++) {
            for (int i = 0; i < w.w; i++) {
                UUID f = w.frames[j][i];
                if (f != null) frameToWall.put(f, w.id);
            }
        }
    }

    // ===== Inner types =====
    static final class WorldMapWall {
        final UUID id = UUID.randomUUID();
        final CartographyManager manager;
        final Plugin plugin;
        final World world;
        final Block anchor;
        final BlockFace clickedFace;
        final BlockFace uFace;
        final BlockFace vFace;
        final int w, h;
        final int ux, uz;  // map X axis (east)
        final int vx, vz;  // map Z axis (south)
        final int[][] tileOriginX, tileOriginZ;  // world X/Z of pixel (0,0) for each tile

        final MapView[][] views;
        final UUID[][] frames;
        final short[][] mapIds;
        final byte[][] caches;
        final MapRenderer[][] renderers;
        final int scaleSize;
        final int tileSpan;

        final Map<UUID, Point> index = new HashMap<>();

        final ArrayDeque<int[]> bfs = new ArrayDeque<>();
        final BitSet visited = new BitSet();
        volatile boolean expanderRunning = false;

        WorldMapWall(CartographyManager manager, Plugin plugin, World world, Block anchor, BlockFace face, BlockFace uFace, BlockFace vFace, int w, int h) {
            this(manager, plugin, world, anchor, face, uFace, vFace, w, h, null, null);
        }

        WorldMapWall(CartographyManager manager, Plugin plugin, World world, Block anchor, BlockFace face, BlockFace uFace,
                     BlockFace vFace, int w, int h, short[][] mapIds, byte[][] caches) {
            this.manager = manager;
            this.plugin = plugin;
            this.world = world;
            this.anchor = anchor;
            this.clickedFace = face;
            this.uFace = uFace;
            this.vFace = vFace;
            this.w = w;
            this.h = h;

            this.views = new MapView[h][w];
            this.frames = new UUID[h][w];
            this.mapIds = (mapIds != null) ? mapIds : new short[h][w];
            this.caches = (caches != null) ? caches : new byte[h * w][];
            this.renderers = new MapRenderer[h][w];
            this.scaleSize = 1 << TILE_SCALE.getValue();
            this.tileSpan = 128 * scaleSize;
            this.ux = 1; this.uz = 0; // east
            this.vx = 0; this.vz = 1; // south

            this.tileOriginX = new int[h][w];
            this.tileOriginZ = new int[h][w];

            // Ensure cache arrays exist and are initialised
            int totalTiles = h * w;
            for (int idx = 0; idx < totalTiles; idx++) {
                if (this.caches[idx] == null) {
                    this.caches[idx] = new byte[128 * 128];
                    Arrays.fill(this.caches[idx], Byte.MIN_VALUE);
                }
            }

            ensureFramesAndMaps();
        }

        World world() { return this.world; }

        private Block topLeftSupport() {
            int uSteps = -(w - 1) / 2;      // left
            int vSteps = +(h - 1) / 2;      // UP  (note the +)
            return anchor.getRelative(uFace, uSteps).getRelative(vFace, vSteps);
        }

        void ensureFramesAndMaps() {
            for (int j = 0; j < h; j++) {
                for (int i = 0; i < w; i++) {
                    Location frameLoc = frameLocationFor(i, j);
                    ItemFrame frame = findOrSpawnFrame(frameLoc);
                    frames[j][i] = frame.getUniqueId();

                    MapView view;
                    short existing = mapIds[j][i];
                    if (existing != 0) {
                        view = Bukkit.getMap(existing);
                        if (view == null) {
                            view = Bukkit.createMap(world);
                            mapIds[j][i] = (short) view.getId();
                        }
                    } else {
                        view = Bukkit.createMap(world);
                        mapIds[j][i] = (short) view.getId();
                    }
                    view.setScale(TILE_SCALE);
                    view.setTrackingPosition(false);
                    int cx = anchor.getX(), cz = anchor.getZ();
                    int halfW = (w - 1) / 2, halfH = (h - 1) / 2;

                    int di = i - halfW;      // left -> right
                    int dj = j - halfH;      // top  -> bottom

                    int centerX = cx + (di * tileSpan * ux) + (dj * tileSpan * vx);
                    int centerZ = cz + (di * tileSpan * uz) + (dj * tileSpan * vz);
                    view.setCenterX(centerX);
                    view.setCenterZ(centerZ);

                    tileOriginX[j][i] = centerX - (tileSpan / 2);
                    tileOriginZ[j][i] = centerZ - (tileSpan / 2);

                    for (MapRenderer r : view.getRenderers()) view.removeRenderer(r);
                    int tileIndex = j * w + i;
                    MapRenderer renderer = new TileRenderer(this, i, j, tileIndex);
                    view.addRenderer(renderer);

                    renderers[j][i] = renderer;
                    views[j][i] = view;

                    ItemStack mapItem = new ItemStack(Material.FILLED_MAP);
                    MapMeta meta = (MapMeta) mapItem.getItemMeta();
                    meta.setMapView(view);
                    mapItem.setItemMeta(meta);
                    frame.setItem(mapItem, false);
                    frame.setRotation(Rotation.NONE);

                    index.put(frame.getUniqueId(), new Point(i, j));
                }
            }

            // fix frame props and tag them
            Bukkit.getScheduler().runTask(plugin, () -> {
                for (int j = 0; j < h; j++) {
                    for (int i = 0; i < w; i++) {
                        UUID fid = frames[j][i];
                        if (fid == null) continue;
                        ItemFrame it = (ItemFrame) Bukkit.getEntity(fid);
                        if (it == null) continue;
                        it.setFacingDirection(clickedFace, true);
                        it.setRotation(Rotation.NONE);
                        it.setFixed(true);
                        it.getPersistentDataContainer().set(WALL_KEY, PersistentDataType.STRING, id.toString());
                    }
                }
            });
        }

        Point lookupTileIndex(UUID frameId) { return index.get(frameId); }

        void startExpander(boolean erasePixels) {
            // Always reset traversal so we repaint every pixel. Only clear pixel data if explicitly asked.
            if (erasePixels) {
                for (byte[] c : caches) Arrays.fill(c, Byte.MIN_VALUE);
            }
            visited.clear();
            bfs.clear();

            if (expanderRunning) return;
            expanderRunning = true;

            int totalW = w * 128, totalH = h * 128;

            // Seed the BFS with a configurable grid so expansion fronts start everywhere.
            // cols/rows of 1 means: seed only at the center.
            int cols = Math.max(1, EXPAND_SEED_GRID_COLS);
            int rows = Math.max(1, EXPAND_SEED_GRID_ROWS);
            if (cols == 1 && rows == 1) {
                enqueueIfInside(totalW / 2, totalH / 2);
            } else {
                for (int r = 0; r < rows; r++) {
                    int z = (rows == 1) ? (totalH / 2) : (int) Math.round((double) r * (totalH - 1) / (rows - 1));
                    for (int c = 0; c < cols; c++) {
                        int x = (cols == 1) ? (totalW / 2) : (int) Math.round((double) c * (totalW - 1) / (cols - 1));
                        enqueueIfInside(x, z);
                    }
                }
            }

            // No throttling: drain the queue in one asynchronous pass.
            if (NO_THROTTLE) {
                Bukkit.getScheduler().runTaskAsynchronously(this.plugin, () -> {
                    processExpansionQueue();
                    expanderRunning = false;
                    manager.saveAll();
                });
            } else {
                // Legacy paced mode (kept for easy re-enable). Budget is effectively unlimited by default.
                new BukkitRunnable() {
                    int warmupTicks = EXPAND_WARMUP_TICKS;
                    @Override public void run() {
                        if (bfs.isEmpty()) { expanderRunning = false; cancel(); manager.saveAll(); return; }
                        int budget = (warmupTicks-- > 0) ? EXPAND_WARMUP_BUDGET : EXPAND_STEADY_BUDGET;
                        for (int i = 0; i < budget; i++) {
                            int[] p = bfs.pollFirst();
                            if (p == null) break;
                            paintAndEnqueueNeighbors(p[0], p[1]);
                        }
                    }
                }.runTaskTimerAsynchronously(this.plugin, 1L, 1L);
            }
        }

        void expandFromPixel(int startX, int startZ, int limit) {
            Bukkit.getScheduler().runTaskAsynchronously(this.plugin, () -> {
                BitSet localVisited = new BitSet();
                ArrayDeque<int[]> q = new ArrayDeque<>();
                int totalW = w * 128, totalH = h * 128;
                q.add(new int[]{startX, startZ});
                int processed = 0;
                while (!q.isEmpty() && processed < limit) {
                    int[] p = q.pollFirst();
                    int x = p[0], z = p[1];
                    if (x < 0 || z < 0 || x >= totalW || z >= totalH) continue;
                    int key = z * totalW + x;
                    if (localVisited.get(key)) continue;
                    localVisited.set(key);
                    paintPixelOverwrite(x, z);
                    processed++;
                    q.add(new int[]{x + 1, z});
                    q.add(new int[]{x - 1, z});
                    q.add(new int[]{x, z + 1});
                    q.add(new int[]{x, z - 1});
                    if (USE_EIGHT_WAY_EXPANSION) {
                        q.add(new int[]{x + 1, z + 1});
                        q.add(new int[]{x + 1, z - 1});
                        q.add(new int[]{x - 1, z + 1});
                        q.add(new int[]{x - 1, z - 1});
                    }
                }
                manager.saveAll();
            });
        }

        private void processExpansionQueue() {
            int[] p;
            while ((p = bfs.pollFirst()) != null) {
                paintAndEnqueueNeighbors(p[0], p[1]);
            }
        }

        private void paintAndEnqueueNeighbors(int x, int z) {
            paintPixelOverwrite(x, z);
            // 4- or 8-way expansion based on tuning flag.
            enqueueIfInside(x + 1, z);
            enqueueIfInside(x - 1, z);
            enqueueIfInside(x, z + 1);
            enqueueIfInside(x, z - 1);
            if (USE_EIGHT_WAY_EXPANSION) {
                enqueueIfInside(x + 1, z + 1);
                enqueueIfInside(x + 1, z - 1);
                enqueueIfInside(x - 1, z + 1);
                enqueueIfInside(x - 1, z - 1);
            }
        }

        private void enqueueIfInside(int x, int z) {
            if (x < 0 || z < 0 || x >= w * 128 || z >= h * 128) return;
            int key = z * (w * 128) + x;
            if (visited.get(key)) return;
            visited.set(key);
            bfs.addLast(new int[]{x, z});
        }

        private void paintPixelOverwrite(int x, int z) {
            int tileI = x >> 7, tileJ = z >> 7;
            int localPx = x & 127, localPz = z & 127;

            int wx = pixelToWorldX(tileI, tileJ, localPx);
            int wz = pixelToWorldZ(tileI, tileJ, localPz);

            byte color = sampleColor(wx, wz);
            int idx = tileJ * w + tileI;
            int off = localPz * 128 + localPx;

            // Always overwrite the pixel; do NOT gate expansion on whether the pixel was previously empty.
            caches[idx][off] = color;
        }

        boolean needsRefresh() {
            for (byte[] c : caches)
                for (byte b : c)
                    if (b == Byte.MIN_VALUE) return true;
            return false;
        }

        private final class TileRenderer extends MapRenderer {
            final int tileI, tileJ, index;
            TileRenderer(WorldMapWall wall, int tileI, int tileJ, int index) {
                super(true);
                this.tileI = tileI; this.tileJ = tileJ; this.index = index;
            }
            @Override public void render(MapView map, MapCanvas canvas, Player player) {
                byte[] buf = caches[index];
                for (int y = 0, k = 0; y < 128; y++)
                    for (int x = 0; x < 128; x++, k++)
                        if (buf[k] != Byte.MIN_VALUE) canvas.setPixel(x, y, buf[k]);

                int px = worldToPixelX(views[tileJ][tileI], anchor.getX());
                int pz = worldToPixelZ(views[tileJ][tileI], anchor.getZ());
                drawStar(canvas, px, pz, MapPalette.RED);
            }
        }

        private void drawStar(MapCanvas c, int px, int pz, byte col) {
            int[][] pts = {{0,0},{1,0},{-1,0},{0,1},{0,-1}};
            for (int[] d : pts) {
                int x = px + d[0], y = pz + d[1];
                if (x>=0 && x<128 && y>=0 && y<128) c.setPixel(x, y, col);
            }
        }

        int worldToPixelX(MapView map, int worldX) {
            return (int) Math.floor((worldX - (map.getCenterX() - (64 * scaleSize))) / (double) scaleSize);
        }
        int worldToPixelZ(MapView map, int worldZ) {
            return (int) Math.floor((worldZ - (map.getCenterZ() - (64 * scaleSize))) / (double) scaleSize);
        }
        int pixelToWorldX(int tileI, int tileJ, int localPx) {
            return tileOriginX[tileJ][tileI] + localPx * scaleSize;
        }
        int pixelToWorldZ(int tileI, int tileJ, int localPz) {
            return tileOriginZ[tileJ][tileI] + localPz * scaleSize;
        }

        Location frameLocationFor(int i, int j) {
            // Place using top-left, moving RIGHT by i and DOWN by j => DOWN = -vFace
            Block support = topLeftSupport()
                    .getRelative(uFace, i)
                    .getRelative(vFace, -j);

            // Spawn at block center; DON'T pre-offset. We rely on setFacingDirection to attach to this block face.
            Location loc = support.getRelative(clickedFace).getLocation().add(0.5, 0.5, 0.5);
            return loc;
        }

        ItemFrame findOrSpawnFrame(Location loc) {
            for (Entity e : loc.getWorld().getNearbyEntities(loc, 0.01, 0.01, 0.01)) {
                if (e instanceof ItemFrame f) return f;
            }
            ItemFrame f = world.spawn(loc, ItemFrame.class);
            f.setFacingDirection(clickedFace, true);
            f.setFixed(true);
            return f;
        }

        static final int SEA_Y = 63;

        byte sampleColor(int wx, int wz) {
            int s = scaleSize;
            int[][] offs = {{0,0},{s/2,0},{0,s/2},{s/2,s/2}};
            int r=0,g=0,b=0,n=0;

            for (int[] o : offs) {
                int x = wx + o[0], z = wz + o[1];
                int y  = world.getHighestBlockYAt(x, z);
                Material top = world.getBlockAt(x, y-1, z).getType();

                int yx = world.getHighestBlockYAt(x+1, z);
                int yz = world.getHighestBlockYAt(x, z+1);
                int slope = Math.min(8, Math.abs(y-yx) + Math.abs(y-yz));

                Biome biome = world.getBiome(x, y-1, z);

                int[] c = colorFor(top, biome, y, slope);
                r += c[0]; g += c[1]; b += c[2]; n++;
            }
            return MapPalette.matchColor(r/n, g/n, b/n);
        }

        private int[] colorFor(Material top, Biome biome, int y, int slope) {
            if (top == Material.WATER || top == Material.KELP || top == Material.SEAGRASS) {
                int d = SEA_Y - y; d = Math.max(-8, Math.min(16, d));
                int G = 120 + d*3, B = 200 + d*4;
                return new int[]{0, clamp(G, 80, 200), clamp(B, 120, 240)};
            }

            if (top == Material.SNOW || top == Material.SNOW_BLOCK ||
                    top == Material.ICE || top == Material.PACKED_ICE || top == Material.BLUE_ICE) {
                int shade = clamp(235 - slope*6, 180, 235);
                return new int[]{shade, shade, shade};
            }

            if (top == Material.SAND || top == Material.RED_SAND) {
                int shade = clamp(220 - slope*3, 160, 220);
                return (top == Material.RED_SAND) ? new int[]{210,120,60} : new int[]{235,215,135};
            }

            if (y > SEA_Y + 28 || top == Material.STONE || top == Material.TUFF || top == Material.GRAVEL ||
                    top == Material.DIORITE || top == Material.ANDESITE || top == Material.GRANITE || top == Material.CALCITE) {
                int shade = clamp(160 - slope*6, 90, 160);
                return new int[]{shade, shade, shade};
            }

            // grass / dirt / leaves bias greener
            int g = clamp(140 - slope*2, 90, 160);
            return new int[]{90, g, 90};
        }

        private int clamp(int v, int lo, int hi) { return Math.max(lo, Math.min(hi, v)); }

        Map<String, Object> serialize() {
            Map<String, Object> m = new HashMap<>();
            m.put("id", id.toString());
            m.put("world", world.getName());
            m.put("anchor", anchor.getLocation().toVector());
            m.put("face", clickedFace.name());
            m.put("uFace", uFace.name());
            m.put("vFace", vFace.name());
            m.put("w", w);
            m.put("h", h);

            List<String> data = new ArrayList<>(w*h);
            for (int j = 0; j < h; j++) {
                for (int i = 0; i < w; i++) {
                    int idx = j*w+i;
                    byte[] buf = caches[idx];
                    data.add(Base64.getEncoder().encodeToString(buf));
                }
            }
            m.put("caches", data);

            List<Integer> ids = new ArrayList<>(w*h);
            for (int j = 0; j < h; j++) for (int i = 0; i < w; i++) ids.add((int) mapIds[j][i]);
            m.put("ids", ids);
            return m;
        }

        @SuppressWarnings("unchecked")
        static WorldMapWall deserialize(CartographyManager manager, Plugin plugin, Server server, Map<String, Object> m) {
            try {
                World world = server.getWorld((String) m.get("world"));
                if (world == null) return null;
                Vector an = (Vector) m.get("anchor");
                Block anchor = world.getBlockAt(an.getBlockX(), an.getBlockY(), an.getBlockZ());
                BlockFace face = BlockFace.valueOf((String) m.get("face"));
                BlockFace uFace = BlockFace.valueOf((String) m.get("uFace"));
                BlockFace vFace = BlockFace.valueOf((String) m.get("vFace"));
                int w = (int) m.get("w");
                int h = (int) m.get("h");

                short[][] mapIds = new short[h][w];
                List<Integer> ids = (List<Integer>) m.get("ids");
                if (ids != null && ids.size() == w*h) {
                    for (int j = 0, k = 0; j < h; j++)
                        for (int i = 0; i < w; i++, k++)
                            mapIds[j][i] = ids.get(k).shortValue();
                }

                byte[][] caches = null;
                List<String> data = (List<String>) m.get("caches");
                if (data != null && data.size() == w * h) {
                    caches = new byte[w * h][];
                    for (int idx = 0; idx < data.size(); idx++) {
                        caches[idx] = Base64.getDecoder().decode(data.get(idx));
                        if (caches[idx].length != 128 * 128) caches[idx] = Arrays.copyOf(caches[idx], 128 * 128);
                    }
                }

                return new WorldMapWall(manager, plugin, world, anchor, face, uFace, vFace, w, h, mapIds, caches);
            } catch (Exception ex) {
                ex.printStackTrace();
                return null;
            }
        }

        static final class Point { final int i, j; Point(int i, int j){this.i=i;this.j=j;} }
    }
}
