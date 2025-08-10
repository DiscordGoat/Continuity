package goat.minecraft.minecraftnew.subsystems.cartography;

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
    private static final MapView.Scale TILE_SCALE = MapView.Scale.NORMAL;
    private static final long TELEPORT_SAFE_SCAN = 24;
    private static final NamespacedKey WALL_KEY = NamespacedKey.minecraft("cont_worldmap_wall");
    private static final String SAVE_FILE = "cartography2_walls.yml";

    private static final int EXPAND_WARMUP_TICKS = 10;
    private static final int EXPAND_WARMUP_BUDGET = 800;
    private static final int EXPAND_STEADY_BUDGET = 300;

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

        WorldMapWall wall = new WorldMapWall(plugin, p.getWorld(), centerBlock, face, uFace, vFace, rect.w, rect.h);
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
            WorldMapWall w = WorldMapWall.deserialize(plugin, plugin.getServer(), map);
            if (w == null) continue;
            walls.put(w.id, w);
            w.ensureFramesAndMaps();
            indexFrames(w);
            w.startExpander(false);
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
            case WEST  -> { u = clickedPosition.getZ(); v = cy; }
            case EAST  -> { u = 1 - clickedPosition.getZ(); v = cy; }
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
            case UP   -> BlockFace.WEST;
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


        WorldMapWall(Plugin plugin, World world, Block anchor, BlockFace face, BlockFace uFace, BlockFace vFace, int w, int h) {
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
            this.mapIds = new short[h][w];
            this.caches = new byte[h * w][];
            this.renderers = new MapRenderer[h][w];
            this.scaleSize = 1 << TILE_SCALE.getValue();
            this.tileSpan = 64 * scaleSize;
            this.ux = 1; this.uz = 0; // east
            this.vx = 0; this.vz = 1; // south

            this.tileOriginX = new int[h][w];
            this.tileOriginZ = new int[h][w];

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

                    MapView view = Bukkit.createMap(world);
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
                    caches[tileIndex] = new byte[128 * 128];
                    Arrays.fill(caches[tileIndex], Byte.MIN_VALUE);
                    MapRenderer renderer = new TileRenderer(this, i, j, tileIndex);
                    view.addRenderer(renderer);

                    renderers[j][i] = renderer;
                    views[j][i] = view;
                    mapIds[j][i] = (short) view.getId();

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
                for (byte[] c : caches) Arrays.fill(c, Byte.MIN_VALUE);
                visited.clear();
                bfs.clear();
            }
            if (expanderRunning) return;
            expanderRunning = true;

            int totalW = w * 128, totalH = h * 128;
            int cx = totalW / 2, cz = totalH / 2;
            enqueueIfInside(cx, cz);

            new BukkitRunnable() {
                int warmupTicks = EXPAND_WARMUP_TICKS;
                @Override public void run() {
                    if (bfs.isEmpty()) { expanderRunning = false; cancel(); return; }

                    int budget = (warmupTicks-- > 0) ? EXPAND_WARMUP_BUDGET : EXPAND_STEADY_BUDGET;
                    for (int i = 0; i < budget; i++) {
                        int[] p = bfs.pollFirst();
                        if (p == null) break;
                        int x = p[0], z = p[1];

                        if (!paintFullPixel(x, z)) continue;

                        enqueueIfInside(x + 1, z);
                        enqueueIfInside(x - 1, z);
                        enqueueIfInside(x, z + 1);
                        enqueueIfInside(x, z - 1);
                    }
                }
            }.runTaskTimerAsynchronously(this.plugin, 10L, 1L);
        }

        private void enqueueIfInside(int x, int z) {
            if (x < 0 || z < 0 || x >= w * 128 || z >= h * 128) return;
            int key = z * (w * 128) + x;
            if (visited.get(key)) return;
            visited.set(key);
            bfs.addLast(new int[]{x, z});
        }

        private boolean paintFullPixel(int x, int z) {
            int tileI = x >> 7, tileJ = z >> 7;
            int localPx = x & 127, localPz = z & 127;

            int wx = pixelToWorldX(tileI, tileJ, localPx);
            int wz = pixelToWorldZ(tileI, tileJ, localPz);


            byte color = sampleColor(wx, wz);
            int idx = tileJ * w + tileI;
            int off = localPz * 128 + localPx;

            if (caches[idx][off] == Byte.MIN_VALUE) {
                caches[idx][off] = color;
                return true;
            }
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
        private int worldToPixelX(MapView v, int wx) {
            int s = 1 << v.getScale().getValue();
            return (int) Math.floor((wx - v.getCenterX()) / (double) s) + 64;
        }
        private int worldToPixelZ(MapView v, int wz) {
            int s = 1 << v.getScale().getValue();
            return (int) Math.floor((wz - v.getCenterZ()) / (double) s) + 64;
        }

        private static final int SEA_Y = 63;

        private byte sampleColor(int wx, int wz) {
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
                int G = 120 + d*3, B = 200 + d*2;
                return new int[]{40, clamp(G,70,200), clamp(B,120,230)};
            }
            if (top == Material.LAVA) return new int[]{240,140,30};

            if (top == Material.SNOW || top == Material.SNOW_BLOCK || top == Material.POWDER_SNOW ||
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

            if (top.name().endsWith("_PLANKS") || top.name().endsWith("_LOG") || top.name().endsWith("_WOOD")) {
                return new int[]{166,126,88};
            }

            if (top == Material.FARMLAND || top == Material.WHEAT || top == Material.CARROTS ||
                    top == Material.POTATOES || top == Material.BEETROOTS) {
                return new int[]{110,170,60};
            }

            if (top == Material.DIRT || top == Material.COARSE_DIRT || top == Material.ROOTED_DIRT || top == Material.DIRT_PATH) {
                return new int[]{138,110,80};
            }

            Climate c = climateFor(biome);
            int green = clamp(100 + (int)(c.humidity*70) - slope*2, 70, 190);
            int red   = clamp(55  + (int)(c.humidity*15) - (int)(c.temp*10), 40, 120);
            int blue  = clamp(55  + (int)(c.humidity*10), 40, 120);
            return new int[]{red, green, blue};
        }

        private Climate climateFor(Biome b) {
            return switch (b) {
                case DESERT, BADLANDS, ERODED_BADLANDS, WOODED_BADLANDS -> new Climate(0.95f, 0.05f);
                case SAVANNA, SAVANNA_PLATEAU, WINDSWEPT_SAVANNA -> new Climate(0.9f, 0.3f);
                case JUNGLE, BAMBOO_JUNGLE, SPARSE_JUNGLE -> new Climate(0.95f, 0.95f);
                case SWAMP, MANGROVE_SWAMP -> new Climate(0.8f, 1.0f);
                case FOREST, FLOWER_FOREST, BIRCH_FOREST, OLD_GROWTH_BIRCH_FOREST, DARK_FOREST,
                     TAIGA, OLD_GROWTH_SPRUCE_TAIGA, OLD_GROWTH_PINE_TAIGA -> new Climate(0.6f, 0.75f);
                case MEADOW, PLAINS, SUNFLOWER_PLAINS, CHERRY_GROVE -> new Climate(0.7f, 0.55f);
                case STONY_SHORE, WINDSWEPT_HILLS, WINDSWEPT_FOREST, WINDSWEPT_GRAVELLY_HILLS, STONY_PEAKS -> new Climate(0.5f, 0.35f);
                case GROVE, SNOWY_TAIGA, SNOWY_PLAINS, SNOWY_SLOPES, FROZEN_PEAKS, JAGGED_PEAKS, ICE_SPIKES -> new Climate(0.1f, 0.6f);
                case LUSH_CAVES -> new Climate(0.7f, 0.9f);
                case DRIPSTONE_CAVES, DEEP_DARK -> new Climate(0.5f, 0.5f);
                case BEACH, RIVER, OCEAN, WARM_OCEAN, LUKEWARM_OCEAN, COLD_OCEAN, DEEP_OCEAN, DEEP_LUKEWARM_OCEAN, DEEP_COLD_OCEAN, FROZEN_OCEAN, FROZEN_RIVER, DEEP_FROZEN_OCEAN -> new Climate(0.6f, 0.9f);
                default -> new Climate(0.6f, 0.6f);
            };
        }

        private record Climate(float temp, float humidity) {}

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
            return m;
        }

        @SuppressWarnings("unchecked")
        static WorldMapWall deserialize(Plugin plugin, Server server, Map<String, Object> m) {
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
                return new WorldMapWall(plugin, world, anchor, face, uFace, vFace, w, h);
            } catch (Exception ex) {
                server.getLogger().warning("Failed to load world map wall: " + ex.getMessage());
                return null;
            }
        }
    }

    private static float yawFor(BlockFace f) {
        return switch (f) {
            case NORTH -> 180f;
            case SOUTH -> 0f;
            case EAST  -> -90f;
            case WEST  -> 90f;
            default -> 0f;
        };
    }
    private static float pitchFor(BlockFace f) {
        return switch (f) {
            case UP -> -90f;
            case DOWN -> 90f;
            default -> 0f;
        };
    }

    private record Rect(Block center, BlockFace face, BlockFace uFace, BlockFace vFace, int u0, int v0, int w, int h) {}
    private record Point(int i, int j) {}
}
