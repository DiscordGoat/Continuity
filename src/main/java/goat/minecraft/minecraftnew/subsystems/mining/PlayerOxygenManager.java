package goat.minecraft.minecraftnew.subsystems.mining;

import goat.minecraft.minecraftnew.MinecraftNew;
import goat.minecraft.minecraftnew.other.additionalfunctionality.BlessingUtils;
import goat.minecraft.minecraftnew.subsystems.pets.PetManager;
import goat.minecraft.minecraftnew.other.skilltree.Skill;
import goat.minecraft.minecraftnew.other.skilltree.SkillTreeManager;
import goat.minecraft.minecraftnew.other.skilltree.Talent;
import goat.minecraft.minecraftnew.subsystems.brewing.PotionManager;
import goat.minecraft.minecraftnew.subsystems.brewing.PotionEffectPreferences;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.*;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.*;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.Random;

import static goat.minecraft.minecraftnew.other.enchanting.CustomEnchantmentManager.getEnchantmentLevel;

public class PlayerOxygenManager implements Listener {

    private final MinecraftNew plugin;

    // Persistent oxygen data
    private File oxygenDataFile;
    private FileConfiguration oxygenDataConfig;
    private static PlayerOxygenManager instance;
    private final Map<UUID, Integer> playerOxygenLevels = new HashMap<>();

    // Data structure for tracking player-placed blocks
    private final Map<UUID, Set<Location>> playerPlacedBlocks = new HashMap<>();

    // Define banned block types when oxygen is 0
    private static final Set<Material> BANNED_BLOCKS = new HashSet<>(Arrays.asList(
            Material.STONE,
            Material.DEEPSLATE,
            Material.ANDESITE,
            Material.DIORITE,
            Material.GRANITE,
            Material.COAL_ORE,
            Material.IRON_ORE,
            Material.GOLD_ORE,
            Material.REDSTONE_ORE,
            Material.LAPIS_ORE,
            Material.DIAMOND_ORE,
            Material.EMERALD_ORE,
            Material.NETHER_QUARTZ_ORE,
            Material.NETHER_GOLD_ORE,
            Material.COPPER_ORE
    ));

    // Constants
    private static final int DEFAULT_OXYGEN_SECONDS = 300; // Base oxygen in seconds
    private static final int OXYGEN_UPDATE_INTERVAL = 20;  // Update every second
    private static final int FULL_RECOVERY_TICKS = 24000;  // ~40 minutes
    // Recovery: If initial oxygen is e.g. 180s, and we want 40 minutes (2400s) to fully recover,
    // that’s roughly 12 seconds per oxygen increment when empty.
    private static final int RECOVERY_INTERVAL_SECONDS = 6;
    private int recoveryCounter = 0; // Counts seconds for recovery pacing

    private int getRecoveryIntervalSeconds(Player player) {
        if (PotionManager.isActive("Potion of Oxygen Recovery", player)
                && PotionEffectPreferences.isEnabled(player, "Potion of Oxygen Recovery")) {
            return 2;
        }
        return RECOVERY_INTERVAL_SECONDS;
    }

    public PlayerOxygenManager(MinecraftNew plugin) {
        this.plugin = plugin;

        // Load oxygen data file
        loadOxygenDataFile();
        instance = this;
        // Load saved data into memory
        loadOxygenDataIntoMemory();

        // Register events
        if (Bukkit.getPluginManager().isPluginEnabled(MinecraftNew.getInstance())) {
            plugin.getServer().getPluginManager().registerEvents(this, plugin);
        }
        // Start the oxygen update task
        startOxygenTask();
    }

    public static PlayerOxygenManager getInstance() {
        if (instance == null) {
            throw new IllegalStateException("PlayerOxygenManager is not initialized yet!");
        }
        return instance;
    }

    /**
     * Loads the oxygenData.yml file, creating it if necessary.
     */
    private void loadOxygenDataFile() {
        oxygenDataFile = new File(plugin.getDataFolder(), "oxygenData.yml");
        if (!oxygenDataFile.exists()) {
            try {
                plugin.getDataFolder().mkdirs();
                oxygenDataFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        oxygenDataConfig = YamlConfiguration.loadConfiguration(oxygenDataFile);
    }

    /**
     * Saves the current oxygen levels to the oxygenData.yml file.
     */
    private void saveOxygenData() {
        for (Map.Entry<UUID, Integer> entry : playerOxygenLevels.entrySet()) {
            oxygenDataConfig.set(entry.getKey().toString(), entry.getValue());
        }
        try {
            oxygenDataConfig.save(oxygenDataFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Starts the repeating task that updates oxygen levels.
     */
    private void startOxygenTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                recoveryCounter++;
                for (Player player : Bukkit.getOnlinePlayers()) {
                    updatePlayerOxygen(player);
                }
                // Periodically save data to prevent data loss
                if (recoveryCounter % 15 == 0) {
                    saveOxygenData();
                }
            }
        }.runTaskTimer(plugin, 0L, OXYGEN_UPDATE_INTERVAL);
    }

    public static int getTotalVentilationEnchantmentLevel(Player player) {
        int totalLevel = 0;
        for (ItemStack armorPiece : player.getInventory().getArmorContents()) {
            if (armorPiece == null || armorPiece.getType().isAir()) continue;
            int ventilationLevel = getEnchantmentLevel(armorPiece, "Ventilation");
            totalLevel += ventilationLevel;
        }
        return totalLevel;
    }

    public void updatePlayerOxygen(Player player) {
        UUID uuid = player.getUniqueId();
        Location location = player.getLocation();
        World world = location.getWorld();
        if (world == null) return;

        // Initialize oxygen level if not present
        if (!playerOxygenLevels.containsKey(uuid)) {
            int initialOxygen = calculateInitialOxygen(player);
            playerOxygenLevels.put(uuid, initialOxygen);
        }

        int currentOxygen = playerOxygenLevels.get(uuid);
        int initialOxygen = calculateInitialOxygen(player);

        // Determine oxygen depletion rate
        int oxygenDepletionRate = getOxygenDepletionRate(player);

        // Check if the player is underwater
        boolean isUnderwater = player.getEyeLocation().getBlock().getType() == Material.WATER;

        if (oxygenDepletionRate > 0 && !player.isInWater()) {
            // Deplete oxygen every second if not underwater
            currentOxygen -= oxygenDepletionRate;
            if (currentOxygen < 0) currentOxygen = 0;
            playerOxygenLevels.put(uuid, currentOxygen);

            // Effects and sounds when oxygen is low
            if (currentOxygen == 150) {
                player.playSound(player.getLocation(), Sound.ENTITY_WARDEN_HEARTBEAT, 1F, 1F);
            }

            if (currentOxygen <= 60 && currentOxygen > 15) {
                // Trigger the breath sound and darkness effect every 5 oxygen units decremented
                if (currentOxygen % 5 == 0) {
                    player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_BREATH, 0.5F, 1.0F);
                    applyDarknessEffect(player);  // Flash darkness when breathing
                }
            }


            // When oxygen is depleted, restrict block interactions
            if (currentOxygen == 0) {
                Bukkit.getLogger().info("Player " + player.getName() + " has 0 oxygen and is now restricted from breaking natural blocks.");
            }
        } else {
            // Handle oxygen recovery
            if (currentOxygen < initialOxygen) {
                if (recoveryCounter % getRecoveryIntervalSeconds(player) == 0) {
                    currentOxygen++;
                    playerOxygenLevels.put(uuid, currentOxygen);
                }
            }
        }
    }

    /**
     * Calculates the player's initial oxygen capacity based on their Mining level and Ventilation.
     * For now, if Mining level is unavailable, a default is used.
     */
    public int calculateInitialOxygen(Player player) {
        int talentLevel = 0;
        if (SkillTreeManager.getInstance() != null) {
            talentLevel = SkillTreeManager.getInstance()
                    .getTalentLevel(player.getUniqueId(), Skill.MINING, Talent.DEEP_LUNGS);
        }
        int ventilationBonus = getTotalVentilationEnchantmentLevel(player) * 25;
        int dwellerBonus = 0;
        if (BlessingUtils.hasFullSetBonus(player, "Dweller")) {
            dwellerBonus += 500;
        }

        int initialOxygen = DEFAULT_OXYGEN_SECONDS + (talentLevel * 20) + ventilationBonus + dwellerBonus;
        return initialOxygen;
    }

    /**
     * Determines the oxygen depletion rate based on the player's location.
     *
     * @param player The player to check.
     * @return The oxygen depletion rate in oxygen units per second.
     */
    public int getOxygenDepletionRate(Player player) {
        Location location = player.getLocation();
        World world = location.getWorld();
        if (world == null) return 0;

        int y = location.getBlockY();
        PetManager.Pet activePet = PetManager.getInstance(plugin).getActivePet(player);
        boolean hasBlacklung = activePet != null && (activePet.hasPerk(PetManager.PetPerk.BLACKLUNG)
                || activePet.hasUniqueTraitPerk(PetManager.PetPerk.BLACKLUNG));
        Random random = new Random();

        if (world.getEnvironment() == World.Environment.NETHER) {
            if (hasBlacklung) {
                return random.nextBoolean() ? 1 : 0;
            }
            return 1;
        } else if (world.getEnvironment() == World.Environment.NORMAL) {
            if (hasBlacklung) {
                return 1;
            }
            if (y < 44) {
                return 1;
            }
        }
        return 0;
    }

    /**
     * Sets the player's oxygen level to the specified value, saves it, and notifies the player.
     *
     * @param player The player whose oxygen level is being set.
     * @param oxygenLevel The new oxygen level to set.
     */
    public void setPlayerOxygenLevel(Player player, int oxygenLevel) {
        UUID uuid = player.getUniqueId();
        playerOxygenLevels.put(uuid, oxygenLevel);
        oxygenDataConfig.set(uuid.toString(), oxygenLevel);
        saveOxygenData();
    }

    /**
     * Calculates the player's temperature based on their location.
     *
     * @param player The player to calculate for.
     * @return The temperature in degrees Fahrenheit.
     */
    private int calculateTemperature(Player player) {
        Location location = player.getLocation();
        World world = location.getWorld();
        if (world == null) return 72;

        int baseTemperature = 72;
        int y = location.getBlockY();

        if (world.getEnvironment() == World.Environment.NETHER) {
            baseTemperature += 69;
        } else if (y < 0) {
            baseTemperature += Math.abs(y);
        }
        return baseTemperature;
    }

    /**
     * Returns the player's temperature for external use (e.g., scoreboard updates).
     *
     * @param player The player.
     * @return The temperature in degrees Fahrenheit.
     */
    public int getTemperature(Player player) {
        return calculateTemperature(player);
    }

    /**
     * Returns the player's saturation for external use.
     *
     * @param player The player.
     * @return The saturation level as an integer.
     */
    public int getSaturation(Player player) {
        return (int) player.getSaturation();
    }

    /**
     * Applies a darkness effect to the player when oxygen is low.
     *
     * @param player The player to affect.
     */
    public void applyDarknessEffect(Player player) {
        player.addPotionEffect(new PotionEffect(PotionEffectType.DARKNESS, 80, 0, false, false, false));
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        if (!playerOxygenLevels.containsKey(uuid)) {
            int storedOxygen = oxygenDataConfig.getInt(uuid.toString(), -1);
            if (storedOxygen < 0) {
                storedOxygen = calculateInitialOxygen(player);
            }
            playerOxygenLevels.put(uuid, storedOxygen);
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        if (playerOxygenLevels.containsKey(uuid)) {
            oxygenDataConfig.set(uuid.toString(), playerOxygenLevels.get(uuid));
            saveOxygenData();
        }
        playerOxygenLevels.remove(uuid);
        playerPlacedBlocks.remove(uuid);
    }

    private void loadOxygenDataIntoMemory() {
        for (String key : oxygenDataConfig.getKeys(false)) {
            try {
                UUID uuid = UUID.fromString(key);
                int oxygenLevel = oxygenDataConfig.getInt(key, DEFAULT_OXYGEN_SECONDS);
                playerOxygenLevels.put(uuid, oxygenLevel);
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Invalid UUID found in oxygenData.yml: " + key);
            }
        }
    }

    @EventHandler
    public void onPlayerWorldChange(PlayerChangedWorldEvent event) {
        updatePlayerOxygen(event.getPlayer());
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        updatePlayerOxygen(event.getPlayer());
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        int oxygen = playerOxygenLevels.getOrDefault(uuid, DEFAULT_OXYGEN_SECONDS);
        Material blockType = event.getBlock().getType();
        if (oxygen == 0 && BANNED_BLOCKS.contains(blockType)) {
            event.setCancelled(true);
            player.sendMessage(ChatColor.RED + "You cannot place that block when you have no oxygen.");
            return;
        }
        recordPlacedBlock(player, event.getBlock().getLocation());
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        int oxygen = playerOxygenLevels.getOrDefault(uuid, DEFAULT_OXYGEN_SECONDS);
        Block block = event.getBlock();
        Material blockType = block.getType();

        if (oxygen == 0 && BANNED_BLOCKS.contains(blockType) && !isPlayerPlacedBlock(player, block.getLocation())) {
            double currentHealth = player.getHealth();
            double newHealth = currentHealth - 4.0;
            if (newHealth <= 0) newHealth = 0;
            player.setHealth(newHealth);
            player.sendMessage(ChatColor.RED + "Breaking natural blocks without oxygen hurts you!");
        } else {
            if (isPlayerPlacedBlock(player, block.getLocation())) {
                removePlacedBlock(player, block.getLocation());
            }
        }
    }

    private void recordPlacedBlock(Player player, Location loc) {
        UUID uuid = player.getUniqueId();
        Location blockLoc = loc.getBlock().getLocation(); // Normalize to block coordinates
        playerPlacedBlocks.computeIfAbsent(uuid, k -> new HashSet<>()).add(blockLoc);
    }

    private boolean isPlayerPlacedBlock(Player player, Location loc) {
        UUID uuid = player.getUniqueId();
        Location blockLoc = loc.getBlock().getLocation(); // Normalize to block coordinates
        return playerPlacedBlocks.getOrDefault(uuid, Collections.emptySet()).contains(blockLoc);
    }

    private void removePlacedBlock(Player player, Location loc) {
        UUID uuid = player.getUniqueId();
        Location blockLoc = loc.getBlock().getLocation(); // Normalize to block coordinates
        Set<Location> locations = playerPlacedBlocks.get(uuid);
        if (locations != null) {
            locations.remove(blockLoc);
        }
    }

    public int getPlayerOxygen(Player player) {
        UUID uuid = player.getUniqueId();
        return playerOxygenLevels.getOrDefault(uuid, calculateInitialOxygen(player));
    }

    /**
     * Call this from your plugin's onDisable to ensure oxygen data is saved.
     */
    public void saveOnShutdown() {
        saveOxygenData();
    }
}
