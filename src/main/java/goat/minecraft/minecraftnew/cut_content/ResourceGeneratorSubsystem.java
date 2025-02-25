package goat.minecraft.minecraftnew.cut_content;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.*;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class ResourceGeneratorSubsystem implements Listener {

    private final JavaPlugin plugin;
    private final Map<Location, Generator> generators = new HashMap<>();
    private final File dataFile;
    private FileConfiguration dataConfig;

    public ResourceGeneratorSubsystem(JavaPlugin plugin) {
        this.plugin = plugin;
        this.dataFile = new File(plugin.getDataFolder(), "generators.yml");
        this.dataConfig = YamlConfiguration.loadConfiguration(dataFile);

        loadGenerators();
        startGenerators();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public void disable() {
        saveGenerators();
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
            ItemStack generatorItem = createGenerator(generator.getResourceType(), generator.getAmount(), generator.getCooldown());
            loc.getWorld().dropItemNaturally(loc, generatorItem);

            // Play break effects
            loc.getWorld().playSound(loc, Sound.BLOCK_STONE_BREAK, 1.0f, 1.0f);
            loc.getWorld().spawnParticle(Particle.BLOCK_CRACK, loc.clone().add(0.5, 0.5, 0.5), 10, Material.SMOOTH_STONE.createBlockData());
        }
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
