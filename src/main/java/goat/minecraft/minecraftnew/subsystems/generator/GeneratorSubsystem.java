package goat.minecraft.minecraftnew.subsystems.generator;

import org.bukkit.*;
import goat.minecraft.minecraftnew.utils.devtools.ItemRegistry;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.*;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scheduler.BukkitRunnable;

import goat.minecraft.minecraftnew.subsystems.generator.OreFabricatorGUI;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class GeneratorSubsystem implements Listener {

    private final JavaPlugin plugin;
    private final Map<Location, Generator> generators = new HashMap<>();
    private final Set<String> placedGenerators = new HashSet<>();
    private final OreFabricatorGUI fabricatorGUI;
    private final Map<String, GeneratorTaskSession> activeSessions = new HashMap<>();

    private static final Map<Material, Integer> ORE_POWER_COST = new HashMap<>();
    private static final Map<Material, Integer> ORE_TIME = new HashMap<>();

    static {
        ORE_POWER_COST.put(Material.COPPER_ORE, 1);
        ORE_POWER_COST.put(Material.COAL_ORE, 2);
        ORE_POWER_COST.put(Material.IRON_ORE, 3);
        ORE_POWER_COST.put(Material.GOLD_ORE, 4);
        ORE_POWER_COST.put(Material.REDSTONE_ORE, 5);
        ORE_POWER_COST.put(Material.LAPIS_ORE, 6);
        ORE_POWER_COST.put(Material.DIAMOND_ORE, 8);
        ORE_POWER_COST.put(Material.EMERALD_ORE, 10);

        ORE_TIME.put(Material.COPPER_ORE, 20);
        ORE_TIME.put(Material.COAL_ORE, 25);
        ORE_TIME.put(Material.IRON_ORE, 30);
        ORE_TIME.put(Material.GOLD_ORE, 35);
        ORE_TIME.put(Material.REDSTONE_ORE, 40);
        ORE_TIME.put(Material.LAPIS_ORE, 40);
        ORE_TIME.put(Material.DIAMOND_ORE, 50);
        ORE_TIME.put(Material.EMERALD_ORE, 50);
    }
    private final File generatorBlockFile;
    private YamlConfiguration generatorBlockConfig;
    private final File dataFile;
    private FileConfiguration dataConfig;

    public GeneratorSubsystem(JavaPlugin plugin) {
        this.plugin = plugin;
        this.dataFile = new File(plugin.getDataFolder(), "generators.yml");
        this.dataConfig = YamlConfiguration.loadConfiguration(dataFile);

        this.generatorBlockFile = new File(plugin.getDataFolder(), "generator_blocks.yml");
        if (!generatorBlockFile.exists()) {
            try { generatorBlockFile.createNewFile(); } catch (IOException ignored) {}
        }
        this.generatorBlockConfig = YamlConfiguration.loadConfiguration(generatorBlockFile);

        loadPlacedGenerators();
        loadGenerators();
        startGenerators();
        this.fabricatorGUI = new OreFabricatorGUI(plugin, this);
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public void onDisable() {
        saveGenerators();
        savePlacedGenerators();
        stopGenerators();
    }

    private String formatResourceName(String resourceName) {
        if (resourceName == null || resourceName.isEmpty()) {
            return resourceName;
        }
        return resourceName.substring(0, 1).toUpperCase() + resourceName.substring(1).toLowerCase();
    }

    public ItemStack createGenerator(Material resourceType, int amount, int cooldown) {
        ItemStack generatorItem = new ItemStack(Material.COMMAND_BLOCK);
        ItemMeta meta = generatorItem.getItemMeta();

        String formattedResourceName = formatResourceName(resourceType.name());
        meta.setDisplayName(ChatColor.DARK_PURPLE + formattedResourceName + " Generator");
        meta.setLore(Arrays.asList(
                ChatColor.GRAY + "Amount: " + amount,
                ChatColor.GRAY + "Cooldown: " + cooldown + " ticks"
        ));

        NamespacedKey resourceKey = new NamespacedKey(plugin, "resourceType");
        NamespacedKey amountKey = new NamespacedKey(plugin, "amount");
        NamespacedKey cooldownKey = new NamespacedKey(plugin, "cooldown");
        meta.getPersistentDataContainer().set(resourceKey, PersistentDataType.STRING, resourceType.name());
        meta.getPersistentDataContainer().set(amountKey, PersistentDataType.INTEGER, amount);
        meta.getPersistentDataContainer().set(cooldownKey, PersistentDataType.INTEGER, cooldown);

        generatorItem.setItemMeta(meta);
        return generatorItem;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            ItemStack item = event.getItem();
            if (item != null && item.getType() == Material.COMMAND_BLOCK) {
                ItemMeta meta = item.getItemMeta();
                if (meta != null && meta.getDisplayName().contains("Generator")) {
                    String resourceTypeName = meta.getPersistentDataContainer()
                            .get(new NamespacedKey(plugin, "resourceType"), PersistentDataType.STRING);
                    Integer amount = meta.getPersistentDataContainer()
                            .get(new NamespacedKey(plugin, "amount"), PersistentDataType.INTEGER);
                    Integer cooldown = meta.getPersistentDataContainer()
                            .get(new NamespacedKey(plugin, "cooldown"), PersistentDataType.INTEGER);

                    if (resourceTypeName != null && amount != null && cooldown != null) {
                        Material resourceType = Material.getMaterial(resourceTypeName);
                        if (resourceType != null) {
                            Location loc = event.getClickedBlock().getRelative(event.getBlockFace()).getLocation();
                            loc.getBlock().setType(Material.SMOOTH_STONE_SLAB);

                            Generator generator = new Generator(loc, resourceType, amount, cooldown);
                            generators.put(loc, generator);
                            generator.start();
                            saveGenerators();

                            Player player = event.getPlayer();
                            if (player.getGameMode() != GameMode.CREATIVE) {
                                item.setAmount(item.getAmount() - 1);
                            }

                            event.setCancelled(true);
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    public void onGeneratorUse(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        Block block = event.getClickedBlock();
        if (block == null) return;

        Material type = block.getType();
        if (type != Material.SCULK_SHRIEKER && type != Material.BEDROCK) return;

        Location baseLoc = (type == Material.SCULK_SHRIEKER)
                ? block.getLocation()
                : block.getRelative(BlockFace.DOWN).getLocation();

        if (!placedGenerators.contains(toLocKey(baseLoc))) return;

        event.setCancelled(true);
        Player player = event.getPlayer();
        fabricatorGUI.open(player, baseLoc);
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        ItemStack inHand = event.getItemInHand();
        String genName = ItemRegistry.getGeneratorItem().getItemMeta().getDisplayName();
        if (inHand == null || !inHand.hasItemMeta() || !inHand.getItemMeta().hasDisplayName()) return;
        if (!inHand.getItemMeta().getDisplayName().equals(genName)) return;

        Block placed = event.getBlockPlaced();
        placed.setType(Material.SCULK_SHRIEKER);
        placed.getRelative(BlockFace.UP).setType(Material.BEDROCK);

        String key = toLocKey(placed.getLocation());
        placedGenerators.add(key);
        savePlacedGenerators();
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        Location loc = block.getLocation();

        if (block.getType() == Material.SMOOTH_STONE_SLAB && generators.containsKey(loc)) {
            // Cancel the default drops for the block
            event.setDropItems(false);

            Generator generator = generators.remove(loc);
            generator.stop();
            saveGenerators();

            // Drop the generator item instead of the block
            loc.getWorld().dropItemNaturally(loc, ItemRegistry.getGeneratorItem());

            // Play break effects
            loc.getWorld().playSound(loc, Sound.BLOCK_STONE_BREAK, 1.0f, 1.0f);
            loc.getWorld().spawnParticle(Particle.BLOCK_CRACK, loc.clone().add(0.5, 0.5, 0.5), 10, Material.SMOOTH_STONE.createBlockData());
        }
    }

    /**
     * Begin fabricating the selected ore using provided gems for power.
     */
    void beginFabrication(Player player, Location loc, Material oreType, ItemStack[] gems) {
        int costPerSecond = ORE_POWER_COST.getOrDefault(oreType, 1);
        int totalTime = ORE_TIME.getOrDefault(oreType, 20);

        int totalPower = 0;
        PowerSlot[] slots = new PowerSlot[gems.length];
        NamespacedKey idKey = new NamespacedKey(plugin, "gem_id");
        NamespacedKey powerKey = new NamespacedKey(plugin, "power");
        for (int i = 0; i < gems.length; i++) {
            ItemStack gem = gems[i];
            slots[i] = new PowerSlot();
            if (gem == null || !gem.hasItemMeta()) continue;
            ItemMeta meta = gem.getItemMeta();
            PersistentDataContainer c = meta.getPersistentDataContainer();
            if (c.has(idKey, PersistentDataType.STRING) && c.has(powerKey, PersistentDataType.INTEGER)) {
                slots[i].gemId = c.get(idKey, PersistentDataType.STRING);
                slots[i].power = c.get(powerKey, PersistentDataType.INTEGER);
                totalPower += slots[i].power;
            }
        }

        if (totalPower < costPerSecond) {
            player.sendMessage(ChatColor.RED + "Not enough gem power!");
            return;
        }

        GeneratorTaskSession session = new GeneratorTaskSession(loc, oreType.name(), totalTime);
        session.powerSlots = slots;
        session.spawnArmorStands();
        session.state = GeneratorState.RUNNING;
        activeSessions.put(session.locationKey, session);
        saveSession(session);

        new BukkitRunnable() {
            @Override
            public void run() {
                if (session.state != GeneratorState.RUNNING) { cancel(); return; }
                if (session.timeRemaining <= 0) {
                    session.state = GeneratorState.COMPLETED;
                    session.updateProgressDisplay(100);
                    saveSession(session);
                    cancel();
                    return;
                }

                int consume = costPerSecond;
                for (PowerSlot ps : session.powerSlots) {
                    if (consume <= 0) break;
                    if (ps != null && ps.power > 0) {
                        int used = Math.min(ps.power, consume);
                        ps.power -= used;
                        consume -= used;
                    }
                }

                if (consume > 0) {
                    session.state = GeneratorState.PAUSED;
                    player.sendMessage(ChatColor.RED + "Generator out of power!");
                    saveSession(session);
                    cancel();
                    return;
                }

                session.timeRemaining--;
                int percent = 100 * (session.totalTime - session.timeRemaining) / session.totalTime;
                session.updateProgressDisplay(percent);
                saveSession(session);
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }


    private void loadGenerators() {
        if (!dataFile.exists()) return;

        for (String key : dataConfig.getKeys(false)) {
            ConfigurationSection section = dataConfig.getConfigurationSection(key);
            if (section != null) {
                World world = Bukkit.getWorld(section.getString("world"));
                double x = section.getDouble("x");
                double y = section.getDouble("y");
                double z = section.getDouble("z");
                Material resourceType = Material.getMaterial(section.getString("resourceType"));
                int amount = section.getInt("amount");
                int cooldown = section.getInt("cooldown");

                if (world != null && resourceType != null) {
                    Location loc = new Location(world, x, y, z);
                    Generator generator = new Generator(loc, resourceType, amount, cooldown);
                    generators.put(loc, generator);
                }
            }
        }
    }

    private void saveGenerators() {
        dataConfig = new YamlConfiguration();
        int index = 0;
        for (Generator generator : generators.values()) {
            String key = "generator" + index++;
            ConfigurationSection section = dataConfig.createSection(key);
            Location loc = generator.getLocation();

            section.set("world", loc.getWorld().getName());
            section.set("x", loc.getX());
            section.set("y", loc.getY());
            section.set("z", loc.getZ());
            section.set("resourceType", generator.getResourceType().name());
            section.set("amount", generator.getAmount());
            section.set("cooldown", generator.getCooldown());
        }
        try {
            dataConfig.save(dataFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void startGenerators() {
        generators.values().forEach(Generator::start);
    }

    private void stopGenerators() {
        generators.values().forEach(Generator::stop);
    }

    private void loadPlacedGenerators() {
        generatorBlockConfig = YamlConfiguration.loadConfiguration(generatorBlockFile);
        for (String key : generatorBlockConfig.getKeys(false)) {
            if (generatorBlockConfig.getBoolean(key)) {
                placedGenerators.add(key);
            }
        }
    }

    private void savePlacedGenerators() {
        for (String key : generatorBlockConfig.getKeys(false)) {
            generatorBlockConfig.set(key, null);
        }
        for (String key : placedGenerators) {
            generatorBlockConfig.set(key, true);
        }
        try {
            generatorBlockConfig.save(generatorBlockFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String toLocKey(Location loc) {
        return loc.getWorld().getName() + ":" + loc.getBlockX() + ":" + loc.getBlockY() + ":" + loc.getBlockZ();
    }

    private void saveSession(GeneratorTaskSession session) {
        ConfigurationSection root = dataConfig.getConfigurationSection("sessions");
        if (root == null) root = dataConfig.createSection("sessions");
        ConfigurationSection sec = root.createSection(session.locationKey);
        sec.set("oreType", session.oreType);
        sec.set("timeRemaining", session.timeRemaining);
        List<Map<String, Object>> gems = new ArrayList<>();
        for (PowerSlot ps : session.powerSlots) {
            if (ps.gemId != null) {
                Map<String, Object> m = new HashMap<>();
                m.put("id", ps.gemId);
                m.put("power", ps.power);
                gems.add(m);
            }
        }
        sec.set("gems", gems);
        try {
            dataConfig.save(dataFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Represents a single ore fabrication task running on a generator block.
     * Stores progress data, armor stand displays and power slot information.
     */
    private class GeneratorTaskSession {
        String locationKey;
        String oreType;
        int totalTime;
        int timeRemaining;

        UUID bottomStandUUID;
        UUID middleStandUUID;
        UUID topStandUUID;

        String progressBarText = "";

        GeneratorState state = GeneratorState.PAUSED;

        PowerSlot[] powerSlots = new PowerSlot[9];

        GeneratorTaskSession(Location loc, String oreType, int totalTime) {
            this.locationKey = toLocKey(loc);
            this.oreType = oreType;
            this.totalTime = totalTime;
            this.timeRemaining = totalTime;
            for (int i = 0; i < powerSlots.length; i++) {
                powerSlots[i] = new PowerSlot();
            }
        }

        /** Spawns three stacked armor stands used to display progress text. */
        void spawnArmorStands() {
            String[] parts = locationKey.split(":");
            if (parts.length != 4) return;

            World world = Bukkit.getWorld(parts[0]);
            if (world == null) return;

            int x = Integer.parseInt(parts[1]);
            int y = Integer.parseInt(parts[2]);
            int z = Integer.parseInt(parts[3]);

            Location base = new Location(world, x + 0.5, y, z + 0.5);

            ArmorStand bottom = world.spawn(base, ArmorStand.class);
            configureStand(bottom);
            ArmorStand middle = world.spawn(base.clone().add(0, 1.0, 0), ArmorStand.class);
            configureStand(middle);
            ArmorStand top = world.spawn(base.clone().add(0, 2.0, 0), ArmorStand.class);
            configureStand(top);

            bottomStandUUID = bottom.getUniqueId();
            middleStandUUID = middle.getUniqueId();
            topStandUUID = top.getUniqueId();
        }

        /** Update the text on the progress display stands. */
        void updateProgressDisplay(int percent) {
            this.progressBarText = createProgressBar(percent) + ChatColor.YELLOW + " " + percent + "%";
            updateStandText(topStandUUID, progressBarText);
        }

        private void updateStandText(UUID uuid, String text) {
            if (uuid == null) return;
            Entity e = Bukkit.getEntity(uuid);
            if (e instanceof ArmorStand stand) {
                stand.setCustomName(ChatColor.GREEN + text);
                stand.setCustomNameVisible(true);
            }
        }

        private void configureStand(ArmorStand stand) {
            stand.setInvisible(true);
            stand.setMarker(true);
            stand.setInvulnerable(true);
            stand.setGravity(false);
            stand.setCustomNameVisible(true);
        }

        private String createProgressBar(int percent) {
            int totalBars = 20;
            int filled = percent * totalBars / 100;
            int empty = totalBars - filled;
            StringBuilder bar = new StringBuilder();
            bar.append(ChatColor.DARK_GRAY).append("[");
            bar.append(ChatColor.GREEN);
            for (int i = 0; i < filled; i++) bar.append("|");
            bar.append(ChatColor.GRAY);
            for (int i = 0; i < empty; i++) bar.append("|");
            bar.append(ChatColor.DARK_GRAY).append("]");
            return bar.toString();
        }
    }

    private class PowerSlot {
        String gemId;
        int power;
    }

    private enum GeneratorState {
        PAUSED,
        RUNNING,
        COMPLETED
    }

    private class Generator {
        private final Location location;
        private final Material resourceType;
        private final int amount;
        private final int cooldown;
        private BukkitTask task;

        public Generator(Location location, Material resourceType, int amount, int cooldown) {
            this.location = location;
            this.resourceType = resourceType;
            this.amount = amount;
            this.cooldown = cooldown;
        }

        public void start() {
            task = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
                Location dropLoc = location.clone().add(0.3, 1.0, 0.3);
                ItemStack dropItem = new ItemStack(resourceType, amount);
                location.getWorld().dropItem(dropLoc, dropItem);

                location.getWorld().playSound(location, Sound.BLOCK_METAL_BREAK, SoundCategory.BLOCKS, 1.0f, 0.5f);

                location.getWorld().spawnParticle(Particle.BLOCK_CRACK, location.clone().add(0.0, 1.5, 0.0), 10, Material.SMOOTH_STONE.createBlockData());
            }, 0L, cooldown);
        }

        public void stop() {
            if (task != null && !task.isCancelled()) {
                task.cancel();
            }
        }

        public Location getLocation() {
            return location;
        }

        public Material getResourceType() {
            return resourceType;
        }

        public int getAmount() {
            return amount;
        }

        public int getCooldown() {
            return cooldown;
        }
    }
}
