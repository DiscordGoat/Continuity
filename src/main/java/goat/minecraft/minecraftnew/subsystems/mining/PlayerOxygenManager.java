package goat.minecraft.minecraftnew.subsystems.mining;

import goat.minecraft.minecraftnew.MinecraftNew;
import goat.minecraft.minecraftnew.subsystems.pets.PetManager;
import goat.minecraft.minecraftnew.subsystems.utils.XPManager; // Remove this if no longer needed anywhere else
import org.bukkit.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.*;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.*;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.Random;

import static goat.minecraft.minecraftnew.subsystems.enchanting.CustomEnchantmentManager.getEnchantmentLevel;

public class PlayerOxygenManager implements Listener {

    private final MinecraftNew plugin;

    // Persistent oxygen data
    private File oxygenDataFile;
    private FileConfiguration oxygenDataConfig;
    private static PlayerOxygenManager instance;
    private final Map<UUID, Integer> playerOxygenLevels = new HashMap<>();
    private final Map<UUID, Scoreboard> playerScoreboards = new HashMap<>();
    private final Map<UUID, Objective> oxygenObjectives = new HashMap<>();

    // Constants
    private static final int DEFAULT_OXYGEN_SECONDS = 180; // Base oxygen in seconds
    private static final int OXYGEN_UPDATE_INTERVAL = 20;  // Update every second
    private static final int SIDEBAR_UPDATE_INTERVAL = 10; // Update every 0.5 seconds (not strictly needed now)
    private static final int FULL_RECOVERY_TICKS = 24000;  // ~40 minutes
    // Recovery: If initial oxygen is e.g. 180s, and we want 40 minutes (2400s) to fully recover:
    // That’s about 2400/180 ≈ 13.3 seconds per 1 oxygen increment if empty.
    // We'll pick 12 seconds per oxygen increment for a close approximation.
    private static final int RECOVERY_INTERVAL_SECONDS = 6;
    private int recoveryCounter = 0; // Counts seconds for recovery pacing

    public PlayerOxygenManager(MinecraftNew plugin) {
        this.plugin = plugin;

        // Load oxygen data file
        loadOxygenDataFile();
        instance = this;
        // Load saved data into memory
        loadOxygenDataIntoMemory();

        // Register events
        if(Bukkit.getPluginManager().isPluginEnabled(MinecraftNew.getInstance())) {
            plugin.getServer().getPluginManager().registerEvents(this, plugin);
        }
        // Start the oxygen and sidebar update tasks
        startOxygenAndSidebarTasks();
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
     * Starts the repeating task that updates oxygen levels and sidebars.
     */
    private void startOxygenAndSidebarTasks() {
        new BukkitRunnable() {
            @Override
            public void run() {
                recoveryCounter++;
                for (Player player : Bukkit.getOnlinePlayers()) {
                    updatePlayerOxygen(player);
                    updatePlayerSidebar(player);
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

        if (oxygenDepletionRate > 0) {
            // Deplete oxygen every second
            currentOxygen -= oxygenDepletionRate;
            if (currentOxygen < 0) currentOxygen = 0;
            playerOxygenLevels.put(uuid, currentOxygen);

            // Effects and sounds when oxygen is low
            if (currentOxygen == 150) {
                player.playSound(player.getLocation(), Sound.ENTITY_WARDEN_HEARTBEAT, 1F, 1F);
            }
            if(currentOxygen > 0 && player.getGameMode() != GameMode.CREATIVE && player.getGameMode() != GameMode.SPECTATOR){
                player.setGameMode(GameMode.SURVIVAL);
            }

            if (currentOxygen <= 60 && currentOxygen > 15) {
                // Play breathing sfx every 5 seconds
                if (currentOxygen % 5 == 0) {
                    player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_BREATH, 0.5F, 1.0F);
                }
            }


            // If oxygen is depleted, apply damage
            if (currentOxygen == 0 && player.getGameMode() == GameMode.SURVIVAL) {
                player.setGameMode(GameMode.ADVENTURE);
            }


        } else {
            // Oxygen replenishes when not depleting, at a controlled rate
            // We want approx. 40 minutes to fully recover. We'll increment by 1 every RECOVERY_INTERVAL_SECONDS.
            // The loop runs every second, so we check if (recoveryCounter % RECOVERY_INTERVAL_SECONDS == 0)
            if (currentOxygen < initialOxygen) {
                if (recoveryCounter % RECOVERY_INTERVAL_SECONDS == 0) {
                    currentOxygen++;
                    if (currentOxygen > initialOxygen) currentOxygen = initialOxygen;
                    playerOxygenLevels.put(uuid, currentOxygen);
                }
            }
        }
    }

    /**
     * Calculates the player's initial oxygen capacity based on their Mining level and Ventilation.
     * In original code, Mining level was from XPManager, but now we have no direct Mining level source.
     * You may need to re-implement Mining level logic if needed, or set it to a constant.
     * For now, we assume 0 mining level to maintain old behavior if XPManager isn't replaced.
     */
    public int calculateInitialOxygen(Player player) {
        // If you still have a way to get Mining level, implement it here.
        // Otherwise, assume zero for demonstration.
        XPManager xpManager = new XPManager(plugin);
        int miningLevel = xpManager.getPlayerLevel(player, "Mining");
        int ventilationBonus = getTotalVentilationEnchantmentLevel(player) * 30;
        int initialOxygen = DEFAULT_OXYGEN_SECONDS + (miningLevel * 5) + ventilationBonus;
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

        boolean hasBlacklung = activePet != null && activePet.hasPerk(PetManager.PetPerk.BLACKLUNG);
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
     * Sets the player's oxygen level to the specified value, saves it to file, and reloads it from file.
     *
     * @param player The player whose oxygen level is being set.
     * @param oxygenLevel The new oxygen level to set.
     */
    public void setPlayerOxygenLevel(Player player, int oxygenLevel) {
        UUID uuid = player.getUniqueId();

        // Update the oxygen level in memory
        playerOxygenLevels.put(uuid, oxygenLevel);

        // Save the updated oxygen level to file
        oxygenDataConfig.set(uuid.toString(), oxygenLevel);
        saveOxygenData();


        // Notify the player
        player.sendMessage(ChatColor.AQUA + "Your oxygen level has been set to " + ChatColor.WHITE + oxygenLevel + " seconds.");
    }
    /**
     * Updates the player's sidebar with oxygen and temperature information.
     *
     * @param player The player to update.
     */
    public void updatePlayerSidebar(Player player) {
        UUID uuid = player.getUniqueId();
        Scoreboard scoreboard = playerScoreboards.get(uuid);
        if (scoreboard == null) {
            scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
            playerScoreboards.put(uuid, scoreboard);
        }

        Objective objective = oxygenObjectives.get(uuid);
        if (objective == null) {
            objective = scoreboard.registerNewObjective("oxygen", "dummy", ChatColor.GREEN + "Environment");
            objective.setDisplaySlot(DisplaySlot.SIDEBAR);
            oxygenObjectives.put(uuid, objective);
        }

        Integer currentOxygen = playerOxygenLevels.get(uuid);
        if (currentOxygen == null) {
            // If this happens, initialize once. Only do this if absolutely necessary.
            currentOxygen = calculateInitialOxygen(player);
            playerOxygenLevels.put(uuid, currentOxygen);
        }

        String oxygenStr = ChatColor.AQUA + "Oxygen: " + ChatColor.WHITE + currentOxygen + "s";

        int temperature = calculateTemperature(player);
        int saturationLevel = (int) player.getSaturation();
        String temperatureStr = ChatColor.RED + "Temperature: " + ChatColor.WHITE + temperature + "°F";
        String saturation = ChatColor.YELLOW + "Saturation: " + ChatColor.WHITE + saturationLevel;

        if (saturationLevel == 0 && player.getFoodLevel() == 20) {
            player.setFoodLevel(player.getFoodLevel() - 1);
        }

        // Clear existing scores
        for (String entry : scoreboard.getEntries()) {
            scoreboard.resetScores(entry);
        }

        // Set scores
        // Adjust ordering by setting different scores
        objective.getScore(oxygenStr).setScore(2);
        objective.getScore(temperatureStr).setScore(1);
        objective.getScore(saturation).setScore(3);

        // Apply scoreboard to player
        player.setScoreboard(scoreboard);
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
     * Applies darkness effect to the player when oxygen is low.
     *
     * @param player The player to affect.
     */
    public void applyDarknessEffect(Player player) {
        player.addPotionEffect(new PotionEffect(PotionEffectType.DARKNESS, 40, 0, false, false, false));
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        // Check if the player's oxygen level is already loaded
        if (!playerOxygenLevels.containsKey(uuid)) {
            int storedOxygen = oxygenDataConfig.getInt(uuid.toString(), -1);
            if (storedOxygen < 0) {
                // Not found, initialize
                storedOxygen = calculateInitialOxygen(player);
            }
            playerOxygenLevels.put(uuid, storedOxygen);
        }

        updatePlayerSidebar(player);
    }


    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();

        // Save current oxygen level
        if (playerOxygenLevels.containsKey(uuid)) {
            oxygenDataConfig.set(uuid.toString(), playerOxygenLevels.get(uuid));
            saveOxygenData();
        }

        playerOxygenLevels.remove(uuid);
        playerScoreboards.remove(uuid);
        oxygenObjectives.remove(uuid);
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

    /**
     * Optionally call this from onDisable in your main plugin class to ensure data is saved on shutdown.
     */
    public void saveOnShutdown() {
        saveOxygenData();
    }
}
