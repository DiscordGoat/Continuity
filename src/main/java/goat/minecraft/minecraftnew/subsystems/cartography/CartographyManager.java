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
import org.bukkit.util.Vector;
import org.bukkit.Rotation;

import java.io.File;
import java.io.IOException;
import java.time.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public final class CartographyManager implements Listener {

    private static final int MAX_WALL = 16;
    private static final MapView.Scale TILE_SCALE = MapView.Scale.FAR;
    private static final long TELEPORT_SAFE_SCAN = 24;
    private static final NamespacedKey WALL_KEY = NamespacedKey.minecraft("cont_worldmap_wall");
    private static final String SAVE_FILE = "cartography2_walls.yml";

    // Magic number: Chunks Per Second - adjust this to control map generation speed
    private static final int CPS = 100 ; // Current: 48 chunks per second (2x faster than before)

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

        BlockFace uFace = rightOf(face);
        BlockFace vFace = upOf(face);
        Rect rect = computeSurfaceRect(centerBlock, face, uFace, vFace);
        if (rect.w <= 0 || rect.h <= 0) {
            p.sendMessage(ChatColor.RED + "No flat surface to place a world map here.");
            return;
        }

        e.setCancelled(true);
        if (!p.getGameMode().equals(GameMode.CREATIVE)) {
            inHand.setAmount(inHand.getAmount() - 1);
        }

        WorldMapWall wall = new WorldMapWall(this, plugin, p.getWorld(), centerBlock, face, uFace, vFace, rect.w, rect.h);
        walls.put(wall.id, wall);
        indexFrames(wall);
        saveAll();

        p.sendMessage(ChatColor.GREEN + "Worldmap Placed. Left click with Axe to destroy map. Right click to get the coordinates clicked.");
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

        Vector uv = projectClickUVOnFrame(e.getClickedPosition(), frame.getFacing());
        if (uv == null) return;

        int localPx = clamp((int) Math.floor(uv.getX() * 128.0), 0, 127);
        int localPz = clamp((int) Math.floor((1.0 - uv.getY()) * 128.0), 0, 127);

        Point ij = wall.lookupTileIndex(frame.getUniqueId());
        if (ij == null) return;

        int wx = wall.pixelToWorldX(ij.i, ij.j, localPx);
        int wz = wall.pixelToWorldZ(ij.i, ij.j, localPz);


        e.getPlayer().sendMessage(ChatColor.YELLOW + "Map click at " + ChatColor.AQUA + wx + ", " + wz);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onLeftClickFrame(EntityDamageByEntityEvent e) {
        if (!(e.getEntity() instanceof ItemFrame frame)) return;
        UUID wallId = frameToWall.get(frame.getUniqueId());
        if (wallId == null) return;

        if (!(e.getDamager() instanceof Player p)) {
            removeWholeWall(walls.get(wallId));
            e.setCancelled(true);
            return;
        }

        ItemStack hand = p.getInventory().getItemInMainHand();
        Material hm = hand.getType();

        if (hm != null && (Tag.ITEMS_AXES.isTagged(hm) || hm.name().endsWith("_AXE"))) {
            e.setCancelled(true);
            removeWholeWall(walls.get(wallId));
            p.sendMessage(ChatColor.RED + "Worldmap removed.");
            return;
        }

        if (hm == Material.ENDER_PEARL) {
            e.setCancelled(true);

            Vector uv = rayToFrameUV(p, frame);
            if (uv == null) return;

            int localPx = clamp((int) Math.floor(uv.getX() * 128.0), 0, 127);
            int localPz = clamp((int) Math.floor((1.0 - uv.getY()) * 128.0), 0, 127);

            WorldMapWall wall = walls.get(wallId);
            Point ij = wall.lookupTileIndex(frame.getUniqueId());
            if (ij == null) return;

            int wx = wall.pixelToWorldX(ij.i, ij.j, localPx);
            int wz = wall.pixelToWorldZ(ij.i, ij.j, localPz);

            Location dest = findSafeTeleport(new Location(frame.getWorld(), wx + 0.5, p.getLocation().getY(), wz + 0.5));
            p.teleport(dest);
            p.sendMessage(ChatColor.GREEN + "Teleported to " + ChatColor.AQUA + wx + ", " + wz);
            return;
        }

        e.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onHangingBreak(HangingBreakEvent e) {
        if (!(e.getEntity() instanceof ItemFrame frame)) return;
        UUID wallId = frameToWall.get(frame.getUniqueId());
        if (wallId == null) return;
        removeWholeWall(walls.get(wallId));
        e.setCancelled(true);
    }
    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onHangingBreakByEntity(HangingBreakByEntityEvent e) {
        if (!(e.getEntity() instanceof ItemFrame frame)) return;
        UUID wallId = frameToWall.get(frame.getUniqueId());
        if (wallId == null) return;
        removeWholeWall(walls.get(wallId));
        e.setCancelled(true);
    }

    private void scheduleMidnightRefresh() {
        long delay = ticksUntilNextMidnight();
        long day = 20L * 60L * 60L * 24L;
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (WorldMapWall w : walls.values()) w.startExpander(true);
        }, delay, day);
    }

    /** Forces all known walls to rebuild their pixel caches. */
    private void refreshAll() {
        for (WorldMapWall w : walls.values()) w.startExpander(true);
    }

    private long ticksUntilNextMidnight() {
        ZoneId zone = ZoneId.systemDefault();
        ZonedDateTime now = ZonedDateTime.now(zone);
        ZonedDateTime next = now.plusDays(1).toLocalDate().atStartOfDay(zone);
        return Duration.between(now, next).getSeconds() * 20L;
    }

    private void saveAll() {
        try {
            if (!plugin.getDataFolder().exists()) plugin.getDataFolder().mkdirs();
            FileConfiguration cfg = new YamlConfiguration();
            List<Map<String, Object>> list = new ArrayList<>();
            for (WorldMapWall w : walls.values()) list.add(w.serialize());
            cfg.set("walls", list);
            cfg.save(dataFile);
        } catch (IOException ex) {
            plugin.getLogger().warning("Failed saving Cartography2 walls: " + ex.getMessage());
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

    private void removeWholeWall(WorldMapWall wall) {
        if (wall == null) return;
        for (int j = 0; j < wall.h; j++) {
            for (int i = 0; i < wall.w; i++) {
                UUID fId = wall.frames[j][i];
                if (fId == null) continue;
                Entity ent = Bukkit.getEntity(fId);
                if (ent != null) ent.remove();
                frameToWall.remove(fId);
            }
        }
        walls.remove(wall.id);
        saveAll();
    }

    private Rect computeSurfaceRect(Block center, BlockFace face, BlockFace uFace, BlockFace vFace) {
        int maxU = expand(center, face, uFace, vFace, 1, 0);
        int minU = expand(center, face, uFace, vFace, -1, 0);
        int maxV = expand(center, face, uFace, vFace, 0, 1);
        int minV = expand(center, face, uFace, vFace, 0, -1);

        int spanU = clamp(maxU - minU + 1, 1, MAX_WALL);
        int spanV = clamp(maxV - minV + 1, 1, MAX_WALL);

        int halfU = spanU / 2;
        int halfV = spanV / 2;
        return new Rect(center, face, uFace, vFace, -halfU, +halfV, spanU, spanV); // NOTE: +halfV (top)
    }

    private int expand(Block center, BlockFace face, BlockFace uFace, BlockFace vFace, int duSign, int dvSign) {
        int u = 0, v = 0, count = 0;
        while (Math.abs(u) < MAX_WALL && Math.abs(v) < MAX_WALL) {
            Block b = center.getRelative(uFace, u).getRelative(vFace, v);
            if (!isValidSurfaceCell(b, face)) break;
            count = (duSign != 0) ? u : v;
            u += duSign; v += dvSign;
        }
        return count;
    }

    private static boolean isValidSurfaceCell(Block support, BlockFace face) {
        if (!support.getType().isSolid()) return false;
        Block front = support.getRelative(face);
        return front.getType().isAir();
    }

    private static int clamp(int v, int lo, int hi) { return Math.max(lo, Math.min(hi, v)); }

    private static Vector projectClickUVOnFrame(Vector clickedPosition, BlockFace facing) {
        double u, v;
        double cy = Math.max(0, Math.min(1, clickedPosition.getY()));
        switch (facing) {
            case NORTH -> { u = 1 - clickedPosition.getX(); v = cy; }
            case SOUTH -> { u = clickedPosition.getX(); v = cy; }
            case EAST  -> { u = clickedPosition.getZ(); v = cy; }
            case WEST  -> { u = 1 - clickedPosition.getZ(); v = cy; }
            case UP    -> { u = clickedPosition.getX(); v = 1 - clickedPosition.getZ(); }
            case DOWN  -> { u = clickedPosition.getX(); v = clickedPosition.getZ(); }
            default -> { return null; }
        }
        return new Vector(Math.max(0, Math.min(1, u)), Math.max(0, Math.min(1, v)), 0);
    }

    private static Vector rayToFrameUV(Player p, ItemFrame frame) {
        Location eye = p.getEyeLocation();
        Vector ro = eye.toVector();
        Vector rd = eye.getDirection().normalize();

        Location fl = frame.getLocation();
        Vector p0 = fl.toVector();
        BlockFace facing = frame.getFacing();
        Vector n = facing.getDirection().normalize();

        double denom = rd.dot(n);
        if (Math.abs(denom) < 1e-6) return null;

        double t = p0.clone().subtract(ro).dot(n) / denom;
        if (t <= 0) return null;

        Vector hit = ro.clone().add(rd.multiply(t));

        Axes axes = Axes.forFacing(facing);
        Vector d = hit.clone().subtract(p0);
        double u = d.dot(axes.u) + 0.5;
        double v = d.dot(axes.v) + 0.5;

        if (u < 0 || u > 1 || v < 0 || v > 1) return null;
        return new Vector(u, v, 0);
    }

    private static Location findSafeTeleport(Location approx) {
        World w = approx.getWorld();
        int x = approx.getBlockX(), z = approx.getBlockZ(), baseY = approx.getBlockY();
        for (int dy = 0; dy <= TELEPORT_SAFE_SCAN; dy++) {
            int yDown = baseY - dy;
            if (yDown > w.getMinHeight() + 1 && is2BlockAir(w, x, yDown, z) && w.getBlockAt(x, yDown - 1, z).getType().isSolid())
                return new Location(w, x + 0.5, yDown, z + 0.5, approx.getYaw(), approx.getPitch());
            int yUp = baseY + dy;
            if (yUp < w.getMaxHeight() - 2 && is2BlockAir(w, x, yUp, z) && w.getBlockAt(x, yUp - 1, z).getType().isSolid())
                return new Location(w, x + 0.5, yUp, z + 0.5, approx.getYaw(), approx.getPitch());
        }
        int y = w.getHighestBlockYAt(x, z) + 1;
        return new Location(w, x + 0.5, y, z + 0.5, approx.getYaw(), approx.getPitch());
    }
    private static boolean is2BlockAir(World w, int x, int y, int z) {
        return w.getBlockAt(x, y, z).getType().isAir() && w.getBlockAt(x, y + 1, z).getType().isAir();
    }

    private static BlockFace rightOf(BlockFace f) {
        return switch (f) {
            case NORTH -> BlockFace.WEST;
            case SOUTH -> BlockFace.EAST;
            case EAST  -> BlockFace.NORTH;
            case WEST  -> BlockFace.SOUTH;
            case UP   -> BlockFace.EAST;
            case DOWN -> BlockFace.EAST;
            default -> BlockFace.EAST;
        };
    }
    private static BlockFace upOf(BlockFace f) {
        return switch (f) {
            case NORTH, SOUTH, EAST, WEST -> BlockFace.UP;
            case UP   -> BlockFace.NORTH; // floor
            case DOWN -> BlockFace.NORTH; // ceiling
            default -> BlockFace.UP;
        };
    }

    private static final class Axes {
        final Vector u, v;
        private Axes(Vector u, Vector v) { this.u = u; this.v = v; }
        static Axes forFacing(BlockFace f) {
            return switch (f) {
                case NORTH -> new Axes(new Vector(1,0,0), new Vector(0,1,0));
                case SOUTH -> new Axes(new Vector(-1,0,0), new Vector(0,1,0));
                case EAST  -> new Axes(new Vector(0,0,1), new Vector(0,1,0));
                case WEST  -> new Axes(new Vector(0,0,-1), new Vector(0,1,0));
                case UP    -> new Axes(new Vector(1,0,0), new Vector(0,0,-1));
                case DOWN  -> new Axes(new Vector(-1,0,0), new Vector(0,0,-1));
                default -> new Axes(new Vector(1,0,0), new Vector(0,1,0));
            };
        }
    }

    private static final class WorldMapWall {
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
        final BitSet[] filled;  // Track which pixels have been rendered
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
            this.filled = new BitSet[h * w];  // Initialize filled tracking
            this.renderers = new MapRenderer[h][w];
            this.scaleSize = 1 << TILE_SCALE.getValue();
            this.tileSpan = 128 * scaleSize;
            this.ux = 1; this.uz = 0; // east
            this.vx = 0; this.vz = 1; // south

            this.tileOriginX = new int[h][w];
            this.tileOriginZ = new int[h][w];

            // Ensure cache arrays and filled bitsets exist and are initialised
            int totalTiles = h * w;
            for (int idx = 0; idx < totalTiles; idx++) {
                if (this.caches[idx] == null) {
                    this.caches[idx] = new byte[128 * 128];
                    // No need to fill with sentinel values anymore
                }
                this.filled[idx] = new BitSet(128 * 128);  // All bits start as false (unfilled)
            }

            ensureFramesAndMaps();
        }
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
        }
        int pixelToWorldX(int tileI, int tileJ, int localPx) {
            // Add bounds checking to prevent ArrayIndexOutOfBoundsException
            if (tileJ < 0 || tileJ >= h || tileI < 0 || tileI >= w) {
                return anchor.getX(); // Return anchor position as fallback
            }
            return tileOriginX[tileJ][tileI] + localPx * scaleSize;
        }
        int pixelToWorldZ(int tileI, int tileJ, int localPz) {
            // Add bounds checking to prevent ArrayIndexOutOfBoundsException
            if (tileJ < 0 || tileJ >= h || tileI < 0 || tileI >= w) {
                return anchor.getZ(); // Return anchor position as fallback
            }
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

        private ItemFrame findOrSpawnFrame(Location loc) {
            BlockFace facing = clickedFace;
            for (Entity e : loc.getWorld().getNearbyEntities(loc, 0.25, 0.25, 0.25)) {
                if (e instanceof ItemFrame f && f.getFacing() == facing) {
                    f.setRotation(Rotation.NONE);
                    f.setFixed(true);
                    return f;
                }
            }
            return loc.getWorld().spawn(loc, ItemFrame.class, it -> {
                it.setFacingDirection(facing, true); // attaches to the clicked face of the support block
                it.setRotation(Rotation.NONE);
                it.setFixed(true);
                it.getPersistentDataContainer().set(WALL_KEY, PersistentDataType.STRING, id.toString());
            });
        }

        Point lookupTileIndex(UUID frameId) { return index.get(frameId); }

        void startExpander(boolean clear) {
            if (clear) {
                for (int idx = 0; idx < caches.length; idx++) {
                    filled[idx].clear();  // Clear filled status instead of filling with sentinel
                }
                visited.clear();
                bfs.clear();
            }
            if (expanderRunning) return;
            expanderRunning = true;

            new BukkitRunnable() {
                // Batch-based processing variables
                private final List<String> orderedChunkList = new ArrayList<>();
                private final Set<String> chunksBeingLoaded = new HashSet<>();
                private final Set<String> loadedChunks = new HashSet<>();
                private final Queue<String> readyToProcess = new LinkedList<>();
                private boolean initialized = false;
                private int currentBatchIndex = 0;

                // Debug tracking
                private int totalPixelsProcessed = 0;
                private int failedWorldQueries = 0;
                private int successfulWorldQueries = 0;
                private int chunksProcessed = 0;
                private final Map<String, Integer> errorCounts = new HashMap<>();

                @Override
                public void run() {
                    // Initialize ordered chunk list on first run
                    if (!initialized) {
                        synchronized (this) {
                            // Double-check locking pattern to prevent multiple initialization
                            if (!initialized) {
                                initializeOrderedChunkList();
                                initialized = true;
                                plugin.getLogger().info(String.format("Starting batch-based map rendering. Total chunks: %d, CPS: %d", orderedChunkList.size(), CartographyManager.CPS));
                            }
                        }
                        return;
                    }

                    // Calculate batch size based on CPS (chunks per second)
                    // We run every tick (20 times per second), so batch size = CPS / 20
                    int batchSize = Math.max(1, CartographyManager.CPS / 20);

                    // Phase 1: Load next batch of chunks (if needed)
                    loadNextBatch(batchSize);

                    // Phase 2: Process any chunks that are now loaded
                    processLoadedChunks();

                    // Check if we're done
                    if (chunksProcessed >= orderedChunkList.size()) {
                        expanderRunning = false;
                        cancel();

                        plugin.getLogger().info(String.format(
                                "Map rendering complete! Chunks: %d, Pixels: %d, Success: %d, Failed: %d",
                                chunksProcessed, totalPixelsProcessed, successfulWorldQueries, failedWorldQueries
                        ));
                        if (!errorCounts.isEmpty()) {
                            plugin.getLogger().warning("Final error breakdown: " + errorCounts.toString());
                        }

                        manager.saveAll();
                    }
                }

                // Initialize ordered chunk list (top-left to bottom-right)
                private void initializeOrderedChunkList() {
                    synchronized (this) {
                        int totalW = w * 128, totalH = h * 128;

                        // Find all chunks and sort them by position (top-left to bottom-right)
                        Map<String, int[]> chunkPositions = new HashMap<>();
                        Set<String> requiredChunks = new HashSet<>();

                        for (int z = 0; z < totalH; z++) { // Top to bottom
                            for (int x = 0; x < totalW; x++) { // Left to right
                                int tileI = x >> 7, tileJ = z >> 7;
                                int localPx = x & 127, localPz = z & 127;

                                int wx = pixelToWorldX(tileI, tileJ, localPx);
                                int wz = pixelToWorldZ(tileI, tileJ, localPz);

                                String chunkKey = (wx >> 4) + "," + (wz >> 4);
                                if (!requiredChunks.contains(chunkKey)) {
                                    requiredChunks.add(chunkKey);
                                    chunkPositions.put(chunkKey, new int[]{wx >> 4, wz >> 4});
                                }
                            }
                        }

                        // Sort chunks by position (top-left to bottom-right)
                        orderedChunkList.addAll(requiredChunks);
                        orderedChunkList.sort((a, b) -> {
                            int[] posA = chunkPositions.get(a);
                            int[] posB = chunkPositions.get(b);

                            // Handle null cases to ensure comparator contract is satisfied
                            if (posA == null && posB == null) return 0;
                            if (posA == null) return 1; // null values go to end
                            if (posB == null) return -1; // null values go to end

                            // Sort by Z first (top to bottom), then by X (left to right)
                            if (posA[1] != posB[1]) return Integer.compare(posA[1], posB[1]);
                            return Integer.compare(posA[0], posB[0]);
                        });
                    }
                }

                // Load next batch of chunks on main thread
                private void loadNextBatch(int batchSize) {
                    int loaded = 0;

                    while (loaded < batchSize && currentBatchIndex < orderedChunkList.size()) {
                        String chunkKey = orderedChunkList.get(currentBatchIndex);

                        // Skip if already loaded or being loaded
                        if (loadedChunks.contains(chunkKey) || chunksBeingLoaded.contains(chunkKey)) {
                            currentBatchIndex++;
                            continue;
                        }

                        String[] parts = chunkKey.split(",");
                        int chunkX = Integer.parseInt(parts[0]);
                        int chunkZ = Integer.parseInt(parts[1]);

                        if (world.isChunkLoaded(chunkX, chunkZ)) {
                            // Already loaded
                            loadedChunks.add(chunkKey);
                            readyToProcess.offer(chunkKey);
                        } else {
                            // Need to load - schedule on main thread
                            chunksBeingLoaded.add(chunkKey);

                            Bukkit.getScheduler().runTask(plugin, () -> {
                                try {
                                    world.loadChunk(chunkX, chunkZ);
                                    // Mark as loaded and ready to process
                                    synchronized (this) {
                                        chunksBeingLoaded.remove(chunkKey);
                                        loadedChunks.add(chunkKey);
                                        readyToProcess.offer(chunkKey);
                                    }
                                } catch (Exception e) {
                                    plugin.getLogger().severe(String.format("Failed to load chunk (%d, %d): %s", chunkX, chunkZ, e.getMessage()));
                                    synchronized (this) {
                                        chunksBeingLoaded.remove(chunkKey);
                                    }
                                }
                            });
                        }

                        currentBatchIndex++;
                        loaded++;
                    }
                }

                // Process all chunks that are ready
                private void processLoadedChunks() {
                    while (!readyToProcess.isEmpty()) {
                        String chunkKey = readyToProcess.poll();
                        processLoadedChunk(chunkKey);
                        chunksProcessed++;

                        // Progress update every batch
                        if (chunksProcessed % Math.max(1, CartographyManager.CPS / 10) == 0) {
                            plugin.getLogger().info(String.format(
                                    "Batch rendering progress: %d/%d chunks (%.1f%%), %d pixels, Success: %d, Failed: %d",
                                    chunksProcessed, orderedChunkList.size(),
                                    (chunksProcessed * 100.0 / orderedChunkList.size()),
                                    totalPixelsProcessed, successfulWorldQueries, failedWorldQueries
                            ));
                        }
                    }
                }

                // Process all pixels within a single chunk (chunk is already loaded)
                private void processLoadedChunk(String chunkKey) {
                    String[] parts = chunkKey.split(",");
                    int chunkX = Integer.parseInt(parts[0]);
                    int chunkZ = Integer.parseInt(parts[1]);

                    plugin.getLogger().info(String.format("Processing loaded chunk (%d, %d)", chunkX, chunkZ));

                    // Local cache for this chunk
                    Map<String, Integer> chunkHeightCache = new HashMap<>();
                    Map<String, Material> chunkMaterialCache = new HashMap<>();

                    int totalW = w * 128, totalH = h * 128;
                    int pixelsInChunk = 0;

                    // Process all pixels that fall within this chunk
                    for (int x = 0; x < totalW; x++) {
                        for (int z = 0; z < totalH; z++) {
                            int tileI = x >> 7, tileJ = z >> 7;
                            int localPx = x & 127, localPz = z & 127;

                            int wx = pixelToWorldX(tileI, tileJ, localPx);
                            int wz = pixelToWorldZ(tileI, tileJ, localPz);

                            // Check if this pixel belongs to the current chunk
                            if ((wx >> 4) == chunkX && (wz >> 4) == chunkZ) {
                                try {
                                    paintPixelInChunk(x, z, wx, wz, chunkHeightCache, chunkMaterialCache);
                                    pixelsInChunk++;
                                    totalPixelsProcessed++;
                                } catch (Exception e) {
                                    failedWorldQueries++;
                                    String errorType = e.getClass().getSimpleName();
                                    errorCounts.put(errorType, errorCounts.getOrDefault(errorType, 0) + 1);
                                    plugin.getLogger().warning(String.format(
                                            "Failed to paint pixel at (%d, %d) in chunk (%d, %d): %s",
                                            wx, wz, chunkX, chunkZ, e.getMessage()
                                    ));
                                }
                            }
                        }
                    }

                    plugin.getLogger().info(String.format("Completed chunk (%d, %d) with %d pixels", chunkX, chunkZ, pixelsInChunk));
                }

                // Paint a single pixel within the current chunk context
                private void paintPixelInChunk(int x, int z, int wx, int wz,
                                               Map<String, Integer> chunkHeightCache,
                                               Map<String, Material> chunkMaterialCache) {
                    int tileI = x >> 7, tileJ = z >> 7;
                    int localPx = x & 127, localPz = z & 127;

                    byte color = sampleColorInChunk(wx, wz, chunkHeightCache, chunkMaterialCache);
                    int idx = tileJ * w + tileI;
                    int off = localPz * 128 + localPx;

                    caches[idx][off] = color;
                    filled[idx].set(off);  // Mark pixel as filled
                }

                // Efficient sampling with aggressive caching
                private byte sampleColorInChunk(int wx, int wz, Map<String, Integer> chunkHeightCache, Map<String, Material> chunkMaterialCache) {
                    String key = wx + "," + wz;

                    // Try cache first
                    Integer cachedY = chunkHeightCache.get(key);
                    Material cachedMaterial = chunkMaterialCache.get(key);

                    int y;
                    Material top;

                    if (cachedY != null && cachedMaterial != null) {
                        y = cachedY;
                        top = cachedMaterial;
                    } else {
                        // Only query world if not cached - chunk is already loaded
                        try {
                            y = world.getHighestBlockYAt(wx, wz);
                            Block block = world.getBlockAt(wx, y, wz);
                            top = block.getType();

                            // Cache the results in chunk-local cache
                            chunkHeightCache.put(key, y);
                            chunkMaterialCache.put(key, top);
                            successfulWorldQueries++;

                        } catch (Exception e) {
                            // Detailed error logging for world access failures
                            plugin.getLogger().severe(String.format(
                                    "World query failed at (%d, %d), chunk (%d, %d): %s - %s",
                                    wx, wz, wx >> 4, wz >> 4, e.getClass().getSimpleName(), e.getMessage()
                            ));

                            // Fallback for failed world queries
                            y = 64;
                            top = Material.GRASS_BLOCK;
                            failedWorldQueries++;
                        }
                    }

                    // Calculate depth-based shading by comparing with neighboring heights
                    int depthShading = calculateDepthShadingInChunk(wx, wz, y, chunkHeightCache);

                    int[] c = colorFor(top, y, 0);

                    // Apply depth shading to the color
                    c[0] = Math.max(0, c[0] - depthShading);
                    c[1] = Math.max(0, c[1] - depthShading);
                    c[2] = Math.max(0, c[2] - depthShading);

                    return MapPalette.matchColor(c[0], c[1], c[2]);
                }

                // Calculate depth-based shading using chunk-local cache
                private int calculateDepthShadingInChunk(int wx, int wz, int currentY, Map<String, Integer> chunkHeightCache) {
                    // Sample neighboring heights (4-directional for performance)
                    int[] neighborHeights = new int[4];
                    int[][] directions = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};

                    for (int i = 0; i < 4; i++) {
                        int nx = wx + directions[i][0];
                        int nz = wz + directions[i][1];
                        String neighborKey = nx + "," + nz;

                        Integer cachedHeight = chunkHeightCache.get(neighborKey);
                        if (cachedHeight != null) {
                            neighborHeights[i] = cachedHeight;
                        } else {
                            try {
                                // Check if neighbor is in a different chunk
                                if ((nx >> 4) != (wx >> 4) || (nz >> 4) != (wz >> 4)) {
                                    // Neighbor is in different chunk - check if loaded
                                    if (!world.isChunkLoaded(nx >> 4, nz >> 4)) {
                                        neighborHeights[i] = currentY; // Use current height if chunk not loaded
                                        continue;
                                    }
                                }

                                neighborHeights[i] = world.getHighestBlockYAt(nx, nz);
                                chunkHeightCache.put(neighborKey, neighborHeights[i]);
                                successfulWorldQueries++;
                            } catch (Exception e) {
                                neighborHeights[i] = currentY; // Fallback to current height
                                failedWorldQueries++;
                            }
                        }
                    }

                    // Calculate average height difference
                    int totalDifference = 0;
                    for (int neighborHeight : neighborHeights) {
                        totalDifference += Math.abs(currentY - neighborHeight);
                    }
                    int avgDifference = totalDifference / 4;

                    // Convert height difference to shading intensity
                    // Scale: 0-2 blocks = no shading, 3-10 blocks = light to moderate shading, 10+ blocks = heavy shading
                    int shading = 0;
                    if (avgDifference >= 2) {
                        shading = Math.min(60, (avgDifference - 1) * 6); // Max 60 darkness units
                    }

                    return shading;
                }
            }.runTaskTimerAsynchronously(plugin, 0L, 1L); // Increased speed: 1 tick = ~12 chunks/sec
        }

        boolean needsRefresh() {
            for (BitSet filledSet : filled)
                if (filledSet.cardinality() < 128 * 128) return true;  // Check if any pixels are unfilled
            return false;
        }

        private int[] colorFor(Material top, int y, int slope) {
            // Water - much darker and more blue
            if (top == Material.WATER || top == Material.KELP || top == Material.SEAGRASS) {
                int d = 63 - y;
                d = Math.max(-8, Math.min(15, d));
                int R = Math.max(10, 30 - d);
                int G = Math.max(50, 80 + d*2);
                int B = Math.max(120, 180 + d*4);
                return new int[]{R, G, B};
            }

            // Lava
            if (top == Material.LAVA) return new int[]{255, 80, 0};

            // Brick materials - all bricks except red bricks should be dark gray
            if (top.name().contains("BRICK")) {
                // Keep red bricks their original color
                if (top == Material.RED_NETHER_BRICKS || top == Material.RED_NETHER_BRICK_STAIRS ||
                        top == Material.RED_NETHER_BRICK_SLAB || top == Material.RED_NETHER_BRICK_WALL) {
                    return new int[]{120, 40, 40}; // Dark red for red bricks
                }
                // All other bricks become dark gray
                return new int[]{64, 64, 64}; // Dark gray
            }

            // Snow and ice - much whiter/more cyan
            if (top == Material.SNOW || top == Material.SNOW_BLOCK || top == Material.POWDER_SNOW) {
                int shade = clamp(255 - slope*5, 240, 255);
                return new int[]{shade, shade, shade};
            }
            if (top == Material.ICE || top == Material.PACKED_ICE) {
                int shade = clamp(240 - slope*4, 220, 240);
                return new int[]{shade - 20, shade - 10, shade};
            }
            if (top == Material.BLUE_ICE) {
                return new int[]{180, 220, 255};
            }

            // Sand
            if (top == Material.SAND) {
                return new int[]{240, 220, 140};
            }
            if (top == Material.RED_SAND) {
                return new int[]{220, 140, 80};
            }

            // Stone and rocky materials
            if (y > 63 + 35 || top == Material.STONE || top == Material.TUFF || top == Material.GRAVEL ||
                    top == Material.DIORITE || top == Material.ANDESITE || top == Material.GRANITE || top == Material.CALCITE) {
                int base = 120;
                if (top == Material.DIORITE || top == Material.CALCITE) base = 160;
                else if (top == Material.ANDESITE) base = 100;
                else if (top == Material.GRANITE) base = 140;

                int shade = clamp(base - slope*6, 70, base);
                return new int[]{shade, shade, shade};
            }

            // Wood materials
            if (top.name().endsWith("_PLANKS") || top.name().endsWith("_LOG") || top.name().endsWith("_WOOD")) {
                return new int[]{120, 80, 50};
            }

            // Crops and farmland - bright green
            if (top == Material.FARMLAND) {
                return new int[]{90, 60, 40}; // Brown farmland
            }
            if (top == Material.WHEAT || top == Material.CARROTS || top == Material.POTATOES ||
                    top == Material.BEETROOTS || top == Material.NETHER_WART) {
                return new int[]{80, 200, 60}; // Bright green crops
            }

            // Grass blocks - consistent green without climate influence
            if (top == Material.GRASS_BLOCK) {
                int green = clamp(160 - slope*2, 140, 180);
                int red = clamp(70, 50, 90);
                int blue = clamp(60, 40, 80);
                return new int[]{red, green, blue};
            }

            // Special blocks
            if (top == Material.MUD || top == Material.MUDDY_MANGROVE_ROOTS) {
                return new int[]{80, 60, 45};
            }
            if (top == Material.CLAY) {
                return new int[]{160, 170, 180};
            }
            if (top == Material.MOSS_BLOCK || top == Material.MOSS_CARPET) {
                return new int[]{60, 120, 40};
            }
            if (top == Material.MYCELIUM) {
                return new int[]{120, 100, 130};
            }

            // Dirt variants
            if (top == Material.DIRT || top == Material.COARSE_DIRT || top == Material.ROOTED_DIRT || top == Material.DIRT_PATH) {
                return new int[]{120, 90, 60};
            }

            // Default grass/vegetation color - consistent without climate influence
            int green = clamp(140 - slope*2, 120, 160);
            int red = clamp(60, 40, 80);
            int blue = clamp(50, 30, 70);
            return new int[]{red, green, blue};
        }

        private final class TileRenderer extends MapRenderer {
            final int tileI, tileJ, index;
            TileRenderer(WorldMapWall wall, int tileI, int tileJ, int index) {
                super(true);
                this.tileI = tileI; this.tileJ = tileJ; this.index = index;
            }
            @Override public void render(MapView map, MapCanvas canvas, Player player) {
                byte[] buf = caches[index];
                BitSet filledSet = filled[index];
                for (int y = 0, k = 0; y < 128; y++)
                    for (int x = 0; x < 128; x++, k++)
                        if (filledSet.get(k)) canvas.setPixel(x, y, buf[k]);  // Use BitSet instead of sentinel check

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
        private int worldToPixelX(MapView v, int wx) {
            int s = 1 << v.getScale().getValue();
            return (int) Math.floor((wx - v.getCenterX()) / (double) s) + 64;
        }
        private int worldToPixelZ(MapView v, int wz) {
            int s = 1 << v.getScale().getValue();
            return (int) Math.floor((wz - v.getCenterZ()) / (double) s) + 64;
        }

        private static final int SEA_Y = 63;

        Map<String, Object> serialize() {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", id.toString());
            m.put("world", world.getUID().toString());
            m.put("anchor", List.of(anchor.getX(), anchor.getY(), anchor.getZ()));
            m.put("face", clickedFace.name());
            m.put("uFace", uFace.name());
            m.put("vFace", vFace.name());
            m.put("w", w);
            m.put("h", h);
            List<List<Integer>> ids = new ArrayList<>();
            for (int j = 0; j < h; j++) {
                List<Integer> row = new ArrayList<>();
                for (int i = 0; i < w; i++) row.add((int) mapIds[j][i]);
                ids.add(row);
            }
            m.put("mapIds", ids);

            List<String> data = new ArrayList<>();
            for (byte[] c : caches) data.add(Base64.getEncoder().encodeToString(c));
            m.put("caches", data);
            return m;
        }

        @SuppressWarnings("unchecked")
        static WorldMapWall deserialize(CartographyManager manager, Plugin plugin, Server server, Map<String, Object> m) {
            try {
                World world = server.getWorld(UUID.fromString((String) m.get("world")));
                if (world == null) return null;
                List<Integer> a = (List<Integer>) m.get("anchor");
                Block anchor = world.getBlockAt(a.get(0), a.get(1), a.get(2));
                BlockFace face = BlockFace.valueOf((String) m.get("face"));
                BlockFace uFace = m.containsKey("uFace") ? BlockFace.valueOf((String) m.get("uFace")) : rightOf(face);
                BlockFace vFace = m.containsKey("vFace") ? BlockFace.valueOf((String) m.get("vFace")) : upOf(face);
                int w = (int) m.get("w");
                int h = (int) m.get("h");

                short[][] mapIds = new short[h][w];
                List<List<Integer>> ids = (List<List<Integer>>) m.getOrDefault("mapIds", Collections.emptyList());
                for (int j = 0; j < h && j < ids.size(); j++) {
                    List<Integer> row = ids.get(j);
                    for (int i = 0; i < w && i < row.size(); i++) {
                        mapIds[j][i] = row.get(i).shortValue();
                    }
                }

                byte[][] caches = null;
                List<String> data = (List<String>) m.get("caches");
                if (data != null && data.size() == w * h) {
                    caches = new byte[w * h][];
                    for (int idx = 0; idx < data.size(); idx++) {
                        caches[idx] = Base64.getDecoder().decode(data.get(idx));
                    }
                }

                return new WorldMapWall(manager, plugin, world, anchor, face, uFace, vFace, w, h, mapIds, caches);
            } catch (Exception ex) {
                server.getLogger().warning("Failed to load world map wall: " + ex.getMessage());
                return null;
            }
        }
    }

    private record Rect(Block center, BlockFace face, BlockFace uFace, BlockFace vFace, int u0, int v0, int w, int h) {}
    private record Point(int i, int j) {}
}