package goat.minecraft.minecraftnew.subsystems.cartography;

import goat.minecraft.minecraftnew.MinecraftNew;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.server.MapInitializeEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapPalette;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.NamespacedKey;

import java.util.*;

/**
 * Handles village markers and player-placed X markers on maps.
 */
public class MapMarkerManager implements Listener {

    private final MinecraftNew plugin;
    private final NamespacedKey xKey;

    public MapMarkerManager(MinecraftNew plugin) {
        this.plugin = plugin;
        this.xKey = new NamespacedKey(plugin, "map_x_marks");
    }

    @EventHandler
    public void onMapInit(MapInitializeEvent event) {
        MapView map = event.getMap();
        map.addRenderer(new MarkerRenderer(xKey));
    }

    @EventHandler
    public void onMapClick(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;
        if (!event.getAction().isRightClick()) return;

        ItemStack item = event.getItem();
        if (item == null || item.getType() != Material.FILLED_MAP) return;
        MapMeta meta = (MapMeta) item.getItemMeta();
        if (meta == null) return;
        MapView view = meta.getMapView();
        if (view == null) return;

        // Ensure renderer is present for existing maps
        boolean hasRenderer = false;
        for (MapRenderer r : view.getRenderers()) {
            if (r instanceof MarkerRenderer) { hasRenderer = true; break; }
        }
        if (!hasRenderer) {
            view.addRenderer(new MarkerRenderer(xKey));
        }

        Location loc = null;
        if (event.getClickedBlock() != null) {
            loc = event.getClickedBlock().getLocation();
        } else {
            Block target = event.getPlayer().getTargetBlockExact(100);
            if (target != null) loc = target.getLocation();
        }
        if (loc == null) return;

        int scale = 1 << view.getScale().ordinal();
        int px = (loc.getBlockX() - view.getCenterX()) / scale + 64;
        int pz = (loc.getBlockZ() - view.getCenterZ()) / scale + 64;
        if (px < 0 || px >= 128 || pz < 0 || pz >= 128) return;

        PersistentDataContainer container = view.getPersistentDataContainer();
        String data = container.getOrDefault(xKey, PersistentDataType.STRING, "");
        List<long[]> marks = parse(data);

        boolean removed = false;
        Iterator<long[]> it = marks.iterator();
        while (it.hasNext()) {
            long[] c = it.next();
            int ex = (int) c[0];
            int ez = (int) c[1];
            int epx = (ex - view.getCenterX()) / scale + 64;
            int epz = (ez - view.getCenterZ()) / scale + 64;
            if (Math.abs(epx - px) <= 4 && Math.abs(epz - pz) <= 4) {
                it.remove();
                removed = true;
            }
        }
        if (!removed) {
            marks.add(new long[]{loc.getBlockX(), loc.getBlockZ()});
        }

        container.set(xKey, PersistentDataType.STRING, serialize(marks));
        event.getPlayer().sendMap(view);
    }

    private static List<long[]> parse(String data) {
        List<long[]> list = new ArrayList<>();
        if (data == null || data.isEmpty()) return list;
        for (String part : data.split(";")) {
            if (part.isEmpty()) continue;
            String[] s = part.split(",");
            if (s.length != 2) continue;
            try {
                long x = Long.parseLong(s[0]);
                long z = Long.parseLong(s[1]);
                list.add(new long[]{x, z});
            } catch (NumberFormatException ignored) {}
        }
        return list;
    }

    private static String serialize(List<long[]> marks) {
        StringBuilder sb = new StringBuilder();
        for (long[] c : marks) {
            if (sb.length() > 0) sb.append(';');
            sb.append(c[0]).append(',').append(c[1]);
        }
        return sb.toString();
    }

    private static class MarkerRenderer extends MapRenderer {
        private final NamespacedKey xKey;
        private final Set<Location> villages = new HashSet<>();
        private boolean villagesLoaded = false;
        private static final byte RED = MapPalette.matchColor(255, 0, 0);
        private static final byte WALL = MapPalette.matchColor(150, 75, 0);
        private static final byte ROOF = MapPalette.matchColor(100, 100, 100);

        MarkerRenderer(NamespacedKey xKey) {
            super();
            this.xKey = xKey;
        }

        @Override
        public void render(MapView map, MapCanvas canvas, Player player) {
            if (!villagesLoaded) {
                loadVillages(map);
                villagesLoaded = true;
            }
            drawVillages(map, canvas);
            drawXMarks(map, canvas);
        }

        private void loadVillages(MapView map) {
            World world = map.getWorld();
            if (world == null) return;
            int scale = 1 << map.getScale().ordinal();
            int half = 64 * scale;
            int minX = map.getCenterX() - half;
            int minZ = map.getCenterZ() - half;
            int maxX = map.getCenterX() + half;
            int maxZ = map.getCenterZ() + half;
            int step = 64 * scale;
            for (int x = minX; x <= maxX; x += step) {
                for (int z = minZ; z <= maxZ; z += step) {
                    Location loc = new Location(world, x, world.getHighestBlockYAt(x, z), z);
                    Location found = world.locateNearestStructure(loc, StructureType.VILLAGE, step, false);
                    if (found != null) {
                        int fx = found.getBlockX();
                        int fz = found.getBlockZ();
                        if (fx >= minX && fx <= maxX && fz >= minZ && fz <= maxZ) {
                            boolean unique = true;
                            for (Location v : villages) {
                                if (v.distanceSquared(found) < 256) { unique = false; break; }
                            }
                            if (unique) villages.add(found);
                        }
                    }
                }
            }
        }

        private void drawVillages(MapView map, MapCanvas canvas) {
            int scale = 1 << map.getScale().ordinal();
            for (Location v : villages) {
                int px = (v.getBlockX() - map.getCenterX()) / scale + 64 - 6;
                int pz = (v.getBlockZ() - map.getCenterZ()) / scale + 64 - 6;
                drawHouse(canvas, px, pz);
            }
        }

        private void drawXMarks(MapView map, MapCanvas canvas) {
            PersistentDataContainer container = map.getPersistentDataContainer();
            String data = container.getOrDefault(xKey, PersistentDataType.STRING, "");
            List<long[]> marks = parse(data);
            int scale = 1 << map.getScale().ordinal();
            for (long[] c : marks) {
                int px = ((int) c[0] - map.getCenterX()) / scale + 64 - 6;
                int pz = ((int) c[1] - map.getCenterZ()) / scale + 64 - 6;
                drawX(canvas, px, pz);
            }
        }

        private void drawX(MapCanvas canvas, int x, int z) {
            for (int i = 0; i < 12; i++) {
                canvas.setPixel(x + i, z + i, RED);
                canvas.setPixel(x + 11 - i, z + i, RED);
            }
        }

        private void drawHouse(MapCanvas canvas, int x, int z) {
            for (int dz = 4; dz < 12; dz++) {
                for (int dx = 0; dx < 12; dx++) {
                    canvas.setPixel(x + dx, z + dz, WALL);
                }
            }
            for (int dz = 0; dz < 4; dz++) {
                for (int dx = dz; dx < 12 - dz; dx++) {
                    canvas.setPixel(x + dx, z + dz, ROOF);
                }
            }
        }
    }
}
