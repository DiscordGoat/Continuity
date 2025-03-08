package goat.minecraft.minecraftnew.subsystems.mining;

import goat.minecraft.minecraftnew.MinecraftNew;
import goat.minecraft.minecraftnew.subsystems.pets.PetManager;
import goat.minecraft.minecraftnew.utils.devtools.PlayerDataManager;
import goat.minecraft.minecraftnew.utils.devtools.XPManager; // Remove this if no longer needed anywhere else
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

            if (currentOxygen <= 60 && currentOxygen > 15) {
                // Play breathing sfx every 5 seconds
                if (currentOxygen % 5 == 0) {
                    player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_BREATH, 0.5F, 1.0F);
                }
            }

            // When oxygen is depleted, now restrict block interactions instead of switching game mode
            if (currentOxygen == 0) {
                Bukkit.getLogger().info("Player " + player.getName() + " has 0 oxygen and is now restricted from breaking natural blocks.");
                // Optionally, you can send a one-time message here to the player if desired.
            }
        } else {
            // Handle oxygen recovery
            if (currentOxygen < initialOxygen) {
                if (recoveryCounter % RECOVERY_INTERVAL_SECONDS == 0) {
                    currentOxygen++;
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
        PlayerDataManager playerDataManager = PlayerDataManager.getInstance(plugin);
        XPManager xpManager = new XPManager(plugin);
        int miningLevel = xpManager.getPlayerLevel(player, "Mining");
        int ventilationBonus = getTotalVentilationEnchantmentLevel(player) * 25;
        int deepbreath = 0;
        if(playerDataManager.hasPerk(player.getUniqueId(), "Deep Breath")){
            deepbreath = 100;
        }
        int initialOxygen = DEFAULT_OXYGEN_SECONDS + (miningLevel * 4) + ventilationBonus + deepbreath;
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
        player.sendMessage(ChatColor.AQUA + "Your oxygen level has been increased to " + ChatColor.WHITE + oxygenLevel + " seconds.");
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

    /**
     * Event handler for block placement.
     * When a player with 0 oxygen tries to place a banned block, cancel the event.
     * Also record the block if placed.
     */
    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        int oxygen = playerOxygenLevels.getOrDefault(uuid, DEFAULT_OXYGEN_SECONDS);

        Material blockType = event.getBlock().getType();
        // If player has 0 oxygen and is trying to place a banned block, cancel placement.
        if (oxygen == 0 && BANNED_BLOCKS.contains(blockType)) {
            event.setCancelled(true);
            player.sendMessage(ChatColor.RED + "You cannot place that block when you have no oxygen.");
            return;
        }
        // Record the block as player placed (using block coordinates to avoid precision issues)
        recordPlacedBlock(player, event.getBlock().getLocation());
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        int oxygen = playerOxygenLevels.getOrDefault(uuid, DEFAULT_OXYGEN_SECONDS);
        Block block = event.getBlock();
        Material blockType = block.getType();

        // If the player is completely out of oxygen and is breaking a banned (natural) block
        if (oxygen == 0 && BANNED_BLOCKS.contains(blockType) && !isPlayerPlacedBlock(player, block.getLocation())) {
            // Inflict 4 true damage by directly reducing health (ignores armor)
            double currentHealth = player.getHealth();
            double newHealth = currentHealth - 4.0;
            if (newHealth <= 0) {
                newHealth = 0;
            }
            player.setHealth(newHealth);
            player.sendMessage(ChatColor.RED + "Breaking natural blocks without oxygen hurts you!");
        } else {
            // If the block was player-placed, remove it from our record.
            if (isPlayerPlacedBlock(player, block.getLocation())) {
                removePlacedBlock(player, block.getLocation());
            }
        }
    }



    /**
     * Records a block placed by the player.
     *
     * @param player The player who placed the block.
     * @param loc The location of the placed block.
     */
    private void recordPlacedBlock(Player player, Location loc) {
        UUID uuid = player.getUniqueId();
        Location blockLoc = loc.getBlock().getLocation(); // normalize to block coordinates
        playerPlacedBlocks.computeIfAbsent(uuid, k -> new HashSet<>()).add(blockLoc);
    }

    /**
     * Checks if the block at the given location was placed by the player.
     *
     * @param player The player.
     * @param loc The location of the block.
     * @return True if the block was placed by the player, false otherwise.
     */
    private boolean isPlayerPlacedBlock(Player player, Location loc) {
        UUID uuid = player.getUniqueId();
        Location blockLoc = loc.getBlock().getLocation(); // normalize to block coordinates
        return playerPlacedBlocks.getOrDefault(uuid, Collections.emptySet()).contains(blockLoc);
    }

    /**
     * Removes a block from the record of player placed blocks.
     *
     * @param player The player.
     * @param loc The location of the block.
     */
    private void removePlacedBlock(Player player, Location loc) {
        UUID uuid = player.getUniqueId();
        Location blockLoc = loc.getBlock().getLocation(); // normalize to block coordinates
        Set<Location> locations = playerPlacedBlocks.get(uuid);
        if (locations != null) {
            locations.remove(blockLoc);
        }
    }
    public int getPlayerOxygen(Player player) {
        UUID uuid = player.getUniqueId();
        // Return the current oxygen level, or if not present, calculate the initial value.
        return playerOxygenLevels.getOrDefault(uuid, calculateInitialOxygen(player));
    }
    /**
     * Optionally call this from onDisable in your main plugin class to ensure data is saved on shutdown.
     */
    public void saveOnShutdown() {
        saveOxygenData();
    }
}
