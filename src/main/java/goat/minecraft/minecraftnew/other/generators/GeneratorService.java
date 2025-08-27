package goat.minecraft.minecraftnew.other.generators;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Handles activated generators, scheduling their item production and persisting
 * their locations between server restarts.
 */
public class GeneratorService {
    private static GeneratorService instance;

    private final JavaPlugin plugin;
    private final Map<String, ActiveGenerator> activeGenerators = new HashMap<>();
    private final File dataFile;
    private final YamlConfiguration dataConfig;

    private GeneratorService(JavaPlugin plugin) {
        this.plugin = plugin;
        this.dataFile = new File(plugin.getDataFolder(), "generators.yml");
        if (!dataFile.exists()) {
            try { dataFile.createNewFile(); } catch (IOException e) { e.printStackTrace(); }
        }
        this.dataConfig = YamlConfiguration.loadConfiguration(dataFile);
        loadAll();
    }

    public static void init(JavaPlugin plugin) {
        if (instance == null) {
            instance = new GeneratorService(plugin);
        }
    }

    public static GeneratorService getInstance() {
        return instance;
    }

    private void loadAll() {
        GeneratorManager mgr = GeneratorManager.getInstance();
        if (mgr == null) return;

        for (String id : dataConfig.getKeys(false)) {
            String worldName = dataConfig.getString(id + ".world");
            int x = dataConfig.getInt(id + ".x");
            int y = dataConfig.getInt(id + ".y");
            int z = dataConfig.getInt(id + ".z");
            int slot = dataConfig.getInt(id + ".slot");

            World world = Bukkit.getWorld(worldName);
            if (world == null) continue;

            Block block = world.getBlockAt(x, y, z);
            if (!(block.getState() instanceof InventoryHolder holder)) continue;

            Inventory inv = holder.getInventory();
            ItemStack current = inv.getItem(slot);

            int power = dataConfig.getInt(id + ".power", 0);
            int powerLimit = dataConfig.getInt(id + ".power_limit", 100);
            int tier = dataConfig.getInt(id + ".tier", 1);

            // 🔧 Detect and fix broken entries (AIR + tier 0 → force Tier 10)
            if ((current != null || current.getType() == Material.AIR) && tier == 0) {
                mgr.setGenerator(current, 0, 0, 10, true);
                inv.setItem(slot, current);

                // overwrite tier so ActiveGenerator sees the fixed value
                tier = 10;
                Bukkit.getLogger().warning("[Generators] Fixed broken generator " + id + " at "
                        + x + "," + y + "," + z + " → reset to Tier 10.");
            } else {
                // Normal restore path
                if (current == null || !mgr.isGenerator(current)) continue;
                mgr.setGenerator(current, power, powerLimit, tier, true);
            }

            String currentId = mgr.getId(current);
            if (currentId == null || !currentId.equals(id)) continue;

            ActiveGenerator gen = new ActiveGenerator(id, inv, slot, current, tier);
            activeGenerators.put(id, gen);
            gen.start();
        }
    }



    private void saveAll() {
        for (String key : dataConfig.getKeys(false)) {
            dataConfig.set(key, null);
        }
        GeneratorManager mgr = GeneratorManager.getInstance();
        for (ActiveGenerator gen : activeGenerators.values()) {
            Location loc = gen.getLocation();
            dataConfig.set(gen.id + ".world", loc.getWorld().getName());
            dataConfig.set(gen.id + ".x", loc.getBlockX());
            dataConfig.set(gen.id + ".y", loc.getBlockY());
            dataConfig.set(gen.id + ".z", loc.getBlockZ());
            dataConfig.set(gen.id + ".slot", gen.slot);

            // Get the CURRENT item from the inventory slot
            ItemStack current = gen.inventory.getItem(gen.slot);
            if (current != null && mgr.isGenerator(current)) {
                // Save only live data
                dataConfig.set(gen.id + ".power", mgr.getPower(current));
                dataConfig.set(gen.id + ".power_limit", mgr.getPowerLimit(current));
                dataConfig.set(gen.id + ".tier", mgr.getTier(current));
            }
        }
        try {
            dataConfig.save(dataFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    public void shutdown() {
        saveAll();
        for (ActiveGenerator gen : activeGenerators.values()) {
            gen.stop();
        }
        activeGenerators.clear();
    }

    public void activate(ItemStack item, Inventory inventory, int slot) {
        GeneratorManager mgr = GeneratorManager.getInstance();
        if (mgr == null) return;
        String id = mgr.getId(item);
        if (id == null) return;
        if (activeGenerators.containsKey(id)) return;
        int tier = mgr.getTier(item);
        mgr.setGenerator(item, mgr.getPower(item), mgr.getPowerLimit(item), tier, true);
        ActiveGenerator gen = new ActiveGenerator(id, inventory, slot, item, tier);
        activeGenerators.put(id, gen);
        gen.start();
        saveAll();
    }

    public void deactivate(ItemStack item) {
        GeneratorManager mgr = GeneratorManager.getInstance();
        if (mgr == null || item == null) return;
        String id = mgr.getId(item);
        if (id == null) return;
        ActiveGenerator gen = activeGenerators.remove(id);
        if (gen != null) {
            gen.stop();
            saveAll();
        }
        mgr.setGenerator(item, mgr.getPower(item), mgr.getPowerLimit(item), mgr.getTier(item), false);
    }

    public void onMove(ItemStack item) {
        deactivate(item);
    }

    public void forceGeneration() {
        for (ActiveGenerator gen : activeGenerators.values()) {
            long period = computePeriod(gen.tier);
            gen.reschedule(20L, period);
        }
    }

    private long computePeriod(int tier) {
        switch (tier) {
            case 1: return 20 * 60 * 20; // 20m
            case 2: return 20 * 60 * 10; // 10m
            case 3: return 20 * 60 * 5;  // 5m
            case 4: return 20 * 120;     // 2m
            case 5: return 20 * 60;      // 1m
            case 6: return 20 * 30;      // 30s
            case 7: return 20 * 15;      // 15s
            case 8: return 20 * 10;      // 10s
            case 9: return 20 * 7;       // 7s
            case 10: return 20 * 5;      // 5s
            default: return 20 * 60 * 20; // fallback 20m
        }
    }


    private ItemStack generateItem(ItemStack generator) {
        // For now we only have the Rocket Generator type
        return createRandomRocket();
    }

    private ItemStack createRandomRocket() {
        Random rand = new Random();
        ItemStack rocket = new ItemStack(Material.FIREWORK_ROCKET);
        FireworkMeta meta = (FireworkMeta) rocket.getItemMeta();
        meta.setPower(5);
        FireworkEffect.Type[] types = FireworkEffect.Type.values();
        FireworkEffect effect = FireworkEffect.builder()
                .withColor(Color.fromRGB(rand.nextInt(256), rand.nextInt(256), rand.nextInt(256)))
                .with(types[rand.nextInt(types.length)])
                .trail(rand.nextBoolean())
                .flicker(rand.nextBoolean())
                .build();
        meta.addEffect(effect);
        rocket.setItemMeta(meta);
        return rocket;
    }

    private class ActiveGenerator {
        private final String id;
        private final Inventory inventory;
        private final int slot;
        private final ItemStack item;
        private final int tier;
        private BukkitTask task;

        ActiveGenerator(String id, Inventory inv, int slot, ItemStack item, int tier) {
            this.id = id;
            this.inventory = inv;
            this.slot = slot;
            this.item = item;
            this.tier = tier;
        }

        void start() {
            long period = computePeriod(tier);
            Bukkit.getLogger().info("Starting generator " + id + " with period " + period);

            // Just schedule normally right away
            GeneratorChunkLoader.loadChunks(getLocation(), plugin);

            task = schedule(period, period);
        }


        void reschedule(long delay, long period) {
            stop();
            task = schedule(delay, period);
        }

        void stop() {
            if (task != null) {
                task.cancel();
            }
            GeneratorChunkLoader.unloadChunks(getLocation(), plugin);
        }

        private BukkitTask schedule(long delay, long period) {
            return new BukkitRunnable() {
                @Override
                public void run() {
                    if (Bukkit.getOnlinePlayers().isEmpty()) return;
                    GeneratorManager mgr = GeneratorManager.getInstance();
                    ItemStack current = inventory.getItem(slot);
                    if (current == null || mgr == null || !id.equals(mgr.getId(current))) {
                        deactivate(item);
                        return;
                    }
                    ItemStack drop = generateItem(current);
                    inventory.addItem(drop);
                }
            }.runTaskTimer(plugin, delay, period);
        }

        Location getLocation() {
            InventoryHolder holder = inventory.getHolder();
            if (holder instanceof BlockState state) {
                return state.getLocation();
            } else if (holder instanceof Entity entity) {
                return entity.getLocation();
            }
            return plugin.getServer().getWorlds().get(0).getSpawnLocation();
        }
    }
    public class GeneratorChunkLoader {
        private static final int RADIUS = 4;

        public static void loadChunks(Location loc, JavaPlugin plugin) {
            World world = loc.getWorld();
            Chunk origin = loc.getChunk();

            for (int dx = -RADIUS; dx <= RADIUS; dx++) {
                for (int dz = -RADIUS; dz <= RADIUS; dz++) {
                    world.addPluginChunkTicket(origin.getX() + dx, origin.getZ() + dz, plugin);
                }
            }
        }

        public static void unloadChunks(Location loc, JavaPlugin plugin) {
            World world = loc.getWorld();
            Chunk origin = loc.getChunk();

            for (int dx = -RADIUS; dx <= RADIUS; dx++) {
                for (int dz = -RADIUS; dz <= RADIUS; dz++) {
                    world.removePluginChunkTicket(origin.getX() + dx, origin.getZ() + dz, plugin);
                }
            }
        }
    }

}

