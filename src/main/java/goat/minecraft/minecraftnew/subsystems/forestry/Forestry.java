package goat.minecraft.minecraftnew.subsystems.forestry;

import goat.minecraft.minecraftnew.MinecraftNew;
import goat.minecraft.minecraftnew.subsystems.pets.PetManager;
import goat.minecraft.minecraftnew.utils.devtools.ItemRegistry;
import goat.minecraft.minecraftnew.utils.devtools.XPManager;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class Forestry implements Listener {

    // Singleton instance
    private static Forestry instance;

    private final MinecraftNew plugin;
    private final XPManager xpManager;
    private final Random random = new Random();

    // Notoriety map tracks each player's current forestry notoriety.
    private Map<UUID, Integer> notorietyMap = new HashMap<>();

    // Set of log materials that can grant forestry XP
    private static final Set<Material> LOG_MATERIALS = new HashSet<>(Arrays.asList(
            Material.OAK_LOG, Material.SPRUCE_LOG, Material.BIRCH_LOG,
            Material.JUNGLE_LOG, Material.ACACIA_LOG, Material.DARK_OAK_LOG,
            Material.MANGROVE_LOG, Material.CHERRY_LOG,
            Material.CRIMSON_STEM, Material.WARPED_STEM
    ));

    // Set of nether log materials that grant more XP
    private static final Set<Material> NETHER_LOG_MATERIALS = new HashSet<>(Arrays.asList(
            Material.CRIMSON_STEM, Material.WARPED_STEM
    ));

    /**
     * Private constructor to prevent instantiation from outside.
     * @param plugin The plugin instance.
     */
    private Forestry(MinecraftNew plugin) {
        this.plugin = plugin;
        this.xpManager = new XPManager(plugin);
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    /**
     * Gets the singleton instance of the Forestry class.
     * @param plugin The plugin instance (only needed on first call).
     * @return The singleton Forestry instance.
     */
    public static Forestry getInstance(MinecraftNew plugin) {
        if (instance == null) {
            instance = new Forestry(plugin);
        }
        return instance;
    }

    /**
     * Gets the singleton instance of the Forestry class.
     * @return The singleton Forestry instance.
     * @throws IllegalStateException if getInstance(plugin) hasn't been called first.
     */
    public static Forestry getInstance() {
        if (instance == null) {
            throw new IllegalStateException("Forestry has not been initialized yet. Call getInstance(plugin) first.");
        }
        return instance;
    }

    /**
     * Initializes Forestry functionality, including starting the notoriety decay task.
     * @param plugin The plugin instance.
     */
    public void init(MinecraftNew plugin) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        startNotorietyDecayTask();
    }

    /**
     * Increments a player's notoriety (capped at 700) and saves the new value.
     * If the notoriety crosses a tier threshold, the player is notified.
     * @param player The player whose notoriety is incremented.
     */
    public void incrementNotoriety(Player player) {
        UUID uuid = player.getUniqueId();
        int currentNotoriety = notorietyMap.getOrDefault(uuid, 0);
        int currentTier = getNotorietyTier(currentNotoriety);
        int newNotoriety = Math.min(currentNotoriety + 1, 700);
        notorietyMap.put(uuid, newNotoriety);
        int newTier = getNotorietyTier(newNotoriety);
        if (newTier > currentTier) {
            notifyNotorietyMilestone(player, newTier);
        }
        saveNotoriety(player);
    }

    /**
     * Determines the notoriety tier for a given notoriety value.
     * Tiers are defined as:
     *  Tier 1: 0 - 64
     *  Tier 2: 65 - 192
     *  Tier 3: 193 - 384
     *  Tier 4: 385 - 640
     *  Tier 5: 641 - 700
     * @param notoriety The notoriety value.
     * @return The tier level (1-5).
     */
    private int getNotorietyTier(int notoriety) {
        if (notoriety <= 64) {
            return 1;
        } else if (notoriety <= 192) {
            return 2;
        } else if (notoriety <= 384) {
            return 3;
        } else if (notoriety <= 640) {
            return 4;
        } else {
            return 5;
        }
    }

    /**
     * Notifies the player with a sound and colored message when they cross a notoriety milestone.
     * The message and sound become more concerning as the tier increases.
     * @param player The player to notify.
     * @param tier The new notoriety tier.
     */
    private void notifyNotorietyMilestone(Player player, int tier) {
        String message;
        ChatColor color;
        Sound sound;
        float pitch;
        switch (tier) {
            case 2:
                message = "The forest seems uneasy...";
                color = ChatColor.YELLOW;
                sound = Sound.BLOCK_NOTE_BLOCK_PLING;
                pitch = 1.0f;
                break;
            case 3:
                message = "The forest grows angry!";
                color = ChatColor.GOLD;
                sound = Sound.BLOCK_BELL_USE;
                pitch = 1.0f;
                break;
            case 4:
                message = "The forest is enraged!";
                color = ChatColor.RED;
                sound = Sound.ENTITY_WITHER_SPAWN;
                pitch = 1.0f;
                break;
            case 5:
                message = "The forest is in a murderous fury!";
                color = ChatColor.DARK_RED;
                sound = Sound.ENTITY_WITHER_DEATH;
                pitch = 1.0f;
                break;
            default:
                return;
        }
        player.sendMessage(color + message);
        player.playSound(player.getLocation(), sound, 1.0f, pitch);
    }

    /**
     * Starts the notoriety decay task that runs every 5 seconds.
     * For each online player, it loads the player's notoriety file, decreases the value (if above 0),
     * and saves the updated value.
     */
    private void startNotorietyDecayTask() {
        plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
            for (Player player : plugin.getServer().getOnlinePlayers()) {
                File notorietyFile = new File(plugin.getDataFolder(), "notoriety_" + player.getUniqueId() + ".yml");
                if (notorietyFile.exists()) {
                    YamlConfiguration config = YamlConfiguration.loadConfiguration(notorietyFile);
                    int notoriety = config.getInt("notoriety");
                    if (notoriety > 0) {
                        notoriety--;
                        config.set("notoriety", notoriety);
                        try {
                            config.save(notorietyFile);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        // Update the in-memory value so that external systems can pick it up.
                        notorietyMap.put(player.getUniqueId(), notoriety);
                    }
                }
            }
        }, 0L, 20L * 5);
    }

    /**
     * Saves the player's notoriety to a file.
     * @param player The player whose notoriety is saved.
     */
    public void saveNotoriety(Player player) {
        UUID uuid = player.getUniqueId();
        File notorietyFile = new File(plugin.getDataFolder(), "notoriety_" + uuid + ".yml");
        YamlConfiguration config = YamlConfiguration.loadConfiguration(notorietyFile);
        config.set("notoriety", notorietyMap.getOrDefault(uuid, 0));
        try {
            config.save(notorietyFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Loads the player's notoriety from file.
     * @param player The player whose notoriety is loaded.
     */
    public void loadNotoriety(Player player) {
        UUID uuid = player.getUniqueId();
        File notorietyFile = new File(plugin.getDataFolder(), "notoriety_" + uuid + ".yml");
        if (notorietyFile.exists()) {
            YamlConfiguration config = YamlConfiguration.loadConfiguration(notorietyFile);
            notorietyMap.put(uuid, config.getInt("notoriety"));
        } else {
            notorietyMap.put(uuid, 0);
        }
    }

    /**
     * Returns the current notoriety for a given player.
     * @param player The player.
     * @return The notoriety value.
     */
    public int getNotoriety(Player player) {
        return notorietyMap.getOrDefault(player.getUniqueId(), 0);
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Block block = event.getBlock();
        if (LOG_MATERIALS.contains(block.getType())) {
            // Mark placed logs to prevent XP farming.
            block.setMetadata("placed", new FixedMetadataValue(plugin, true));
        }
    }

    private void grantHaste(Player player, String skill) {
        int level = xpManager.getPlayerLevel(player, skill);
        if (random.nextInt(100) + 1 >= 90) { // 10% chance to grant Haste
            int hasteLevel = 0; // Haste level increases every 33 levels, max level 2
            int duration = 100 + (level * 5); // Duration scales with level (in ticks)
            player.addPotionEffect(new PotionEffect(PotionEffectType.FAST_DIGGING, duration, hasteLevel), true);
            player.playSound(player.getLocation(), Sound.BLOCK_DEEPSLATE_STEP, 1.0f, 1.0f);
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        Player player = event.getPlayer();
        Material blockType = block.getType();
        ForestryPetManager forestryPetManager = MinecraftNew.getInstance().getForestryManager();
        PetManager petManager = PetManager.getInstance(plugin);
        PetManager.Pet activePet = petManager.getActivePet(player);
        int forestryLevel = xpManager.getPlayerLevel(player, "Forestry");

        // Check if it's a log and not player-placed.
        if (LOG_MATERIALS.contains(blockType) && !block.hasMetadata("placed")) {
            // Increment notoriety.
            incrementNotoriety(player);

            // Calculate spirit chance.
            double spiritChance = 0.0;
            int playerForestryLevel = xpManager.getPlayerLevel(player, "Forestry");
            spiritChance += playerForestryLevel * 0.0005; // 0.05% per level, up to 5% base spirit chance.

            // Increment forestry count.
            forestryPetManager.incrementForestryCount(player);

            // Adjust spirit chance for pet perks.
            if (activePet != null && activePet.hasPerk(PetManager.PetPerk.SKEPTICISM)) {
                spiritChance += 0.02;
            }
            if (activePet != null && activePet.hasPerk(PetManager.PetPerk.CHALLENGE)) {
                spiritChance += 0.05;
            }

            // Grant haste effect.
            grantHaste(player, "Forestry");

            // Rare drops.
            if (random.nextInt(1600) + 1 == 1) {
                Objects.requireNonNull(player.getLocation().getWorld())
                        .dropItem(player.getLocation(), ItemRegistry.getSecretsOfInfinity());
                player.sendMessage(ChatColor.YELLOW + "You received Secrets of Infinity!");
            }
            if (random.nextInt(1600) + 1 == 1) {
                Objects.requireNonNull(player.getLocation().getWorld())
                        .dropItem(player.getLocation(), ItemRegistry.getSilkWorm());
                player.sendMessage(ChatColor.YELLOW + "You received Silk Worm!");
            }

            // Award XP based on log type.
            int xpAmount = NETHER_LOG_MATERIALS.contains(blockType) ? 10 : 5;
            xpManager.addXP(player, "Forestry", xpAmount);

            // Process double drop chance.
            processDoubleDropChance(player, block, forestryLevel);
            // Process perfect apple drop chance.
            processPerfectAppleChance(player, block, forestryLevel);

            // (Additional spirit spawning logic could be added here.)
            ForestSpiritManager forestSpiritManager = ForestSpiritManager.getInstance(plugin);
            forestSpiritManager.attemptSpiritSpawn(spiritChance, block.getLocation(), block, player);

        }
    }

    /**
     * Processes the chance for a double log drop.
     * @param player The player who broke the log.
     * @param block The log block that was broken.
     * @param forestryLevel The player's forestry level.
     */
    public void processDoubleDropChance(Player player, Block block, int forestryLevel) {
        if (random.nextInt(100) < forestryLevel) {
            final Location dropLocation = block.getLocation().add(0.5, 0.5, 0.5);
            final Material logType = block.getType();
            new BukkitRunnable() {
                @Override
                public void run() {
                    dropLocation.getWorld().dropItemNaturally(dropLocation, new ItemStack(logType, 1));
                    dropLocation.getWorld().playSound(dropLocation, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.3f, 1.0f);
                    dropLocation.getWorld().spawnParticle(Particle.VILLAGER_HAPPY, dropLocation, 5, 0.3, 0.3, 0.3, 0);
                }
            }.runTaskLater(plugin, 1L);
        }
    }

    /**
     * Processes the chance for a perfect apple drop.
     * @param player The player who broke the log.
     * @param block The log block that was broken.
     * @param forestryLevel The player's forestry level.
     */
    public void processPerfectAppleChance(Player player, Block block, int forestryLevel) {
        double chance = forestryLevel * 0.01;
        if (random.nextDouble() * 100 < chance) {
            final Location dropLocation = block.getLocation().add(0.5, 0.5, 0.5);
            new BukkitRunnable() {
                @Override
                public void run() {
                    ItemStack perfectApple = ItemRegistry.getBrewingApple();
                    dropLocation.getWorld().dropItemNaturally(dropLocation, perfectApple);
                    dropLocation.getWorld().playSound(dropLocation, Sound.ENTITY_WOLF_AMBIENT, 0.5f, 1.0f);
                    dropLocation.getWorld().spawnParticle(Particle.HEART, dropLocation, 5, 0.3, 0.3, 0.3, 0);
                }
            }.runTaskLater(plugin, 1L);
        }
    }
}
