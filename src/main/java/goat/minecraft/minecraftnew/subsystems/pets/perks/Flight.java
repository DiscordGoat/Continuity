package goat.minecraft.minecraftnew.subsystems.pets.perks;

import goat.minecraft.minecraftnew.subsystems.beacon.CatalystManager;
import goat.minecraft.minecraftnew.subsystems.beacon.CatalystType;
import goat.minecraft.minecraftnew.subsystems.pets.PetManager;
import goat.minecraft.minecraftnew.utils.devtools.PlayerMeritManager;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Flight implements Listener {

    private static final double MAX_FLIGHT_DISTANCE_AT_LEVEL_100 = 1.0; // Max flight distance at level 100 in km
    private static final double DRAIN_RATE_PER_SECOND = 0.01; // Drain rate per second in kilometers
    private static final int TICKS_PER_SECOND = 20; // Number of ticks in one second
    private static final double REGEN_RATE_PER_SECOND = 0.01; // Regeneration rate while near a Flight Catalyst
    private final PetManager petManager;
    private final PlayerMeritManager meritManager;
    private final Map<UUID, Double> dailyFlightTracker = new HashMap<>();
    private final Map<UUID, Long> lastFlightReset = new HashMap<>();
    private final Map<UUID, BukkitRunnable> flightTasks = new HashMap<>();
    private final Map<UUID, BukkitRunnable> regenTasks = new HashMap<>();
    private final JavaPlugin plugin;

    public Flight(JavaPlugin plugin) {
        this.petManager = PetManager.getInstance(plugin);
        this.meritManager = PlayerMeritManager.getInstance(plugin);
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();

        // Ensure the player is in survival or adventure mode
        if (player.getGameMode() != GameMode.SURVIVAL && player.getGameMode() != GameMode.ADVENTURE) {
            return;
        }

        // Get the player's active pet
        PetManager.Pet activePet = petManager.getActivePet(player);

        // Reset flight stats if the 3-day period has passed
        resetFlightStatsIfNewDay(player);

        // Calculate the maximum flight distance based on player's level
        int petLevel = activePet != null ? activePet.getLevel() : 0;
        double maxFlightDistanceKm = calculateMaxFlightDistance(player, petLevel);

        // Calculate remaining flight distance
        double flownDistanceKm = dailyFlightTracker.getOrDefault(playerId, 0.0);
        double remainingDistance = maxFlightDistanceKm - flownDistanceKm;

        // Display flight progress
        displayFlightProgress(player, flownDistanceKm, remainingDistance);

        // Check if player is near a Flight Catalyst - this takes priority over everything
        CatalystManager catalystManager = CatalystManager.getInstance();
        if (catalystManager != null && catalystManager.isNearCatalyst(player.getLocation(), CatalystType.FLIGHT)) {
            // Flight Catalyst takes priority - enable flight regardless of pet limitations
            enableFlightFromCatalyst(player);
            startRegenTask(player);
            return;
        } else {
            stopRegenTask(player);
        }

        // Check if the player has exceeded the flight limit
        if (remainingDistance <= 0) {
            disableFlight(player);
            return;
        }

        // Enable flight if the player has the perk and hasn't exceeded the limit
        if (activePet != null && activePet.hasPerk(PetManager.PetPerk.FLIGHT)) {
            enableFlight(player);
        } else {
            disableFlight(player);
        }
    }

    /**
     * Calculates the maximum flight distance based on the player's pet level.
     *
     * @param level The pet level (0-100).
     * @return The maximum flight distance in kilometers.
     */
    private double calculateMaxFlightDistance(Player player, int level) {
        // Flight distance scales linearly with level from 0.0 km to 1.0 km at level 100
        double distance = MAX_FLIGHT_DISTANCE_AT_LEVEL_100 * (level / 100.0);
        if (meritManager.hasPerk(player.getUniqueId(), "Icarus")) {
            distance *= 2;
        }
        return distance;
    }

    private void enableFlightFromCatalyst(Player player) {
        if (!player.getAllowFlight()) {
            player.setAllowFlight(true);
            player.sendMessage(ChatColor.LIGHT_PURPLE + "✈ Flight enabled by Catalyst of Flight!");
        }
        
        // Don't start flight draining task for catalyst flight - it's unlimited
        // Cancel any existing pet flight task since catalyst takes priority
        UUID playerId = player.getUniqueId();
        if (flightTasks.containsKey(playerId)) {
            flightTasks.get(playerId).cancel();
            flightTasks.remove(playerId);
        }
    }

    private void enableFlight(Player player) {
        UUID playerId = player.getUniqueId();

        if (!player.getAllowFlight()) {
            player.setAllowFlight(true);
            player.sendMessage(ChatColor.AQUA + "Your pet's Flight perk is now active! You can fly.");
        }

        // Start a flight draining task if not already active
        if (!flightTasks.containsKey(playerId)) {
            BukkitRunnable flightTask = new BukkitRunnable() {
                @Override
                public void run() {
                    // If the player stops flying, stop the task
                    if (!player.isFlying()) {
                        this.cancel();
                        flightTasks.remove(playerId);
                        return;
                    }

                    // Deduct flight distance
                    double flownDistance = dailyFlightTracker.getOrDefault(playerId, 0.0);
                    flownDistance += DRAIN_RATE_PER_SECOND / TICKS_PER_SECOND;
                    dailyFlightTracker.put(playerId, flownDistance);

                    // Get the pet's level and max distance
                    PetManager.Pet activePet = petManager.getActivePet(player);
                    int petLevel = activePet != null ? activePet.getLevel() : 0;
                    double maxDistance = calculateMaxFlightDistance(player, petLevel);

                    // Check if the player exceeds the limit
                    if (flownDistance >= maxDistance) {
                        disableFlight(player);
                        this.cancel();
                    }
                }
            };
            flightTask.runTaskTimer(plugin, 0, 1); // Run every tick
            flightTasks.put(playerId, flightTask);
        }
    }

    private void resetFlightStatsIfNewDay(Player player) {
        UUID playerId = player.getUniqueId();
        long currentDay = player.getWorld().getFullTime() / 24000; // Minecraft days
        long lastResetDay = lastFlightReset.getOrDefault(playerId, 0L);

        // Reset every 3 Minecraft days instead of daily
        if (currentDay - lastResetDay >= 3) {
            dailyFlightTracker.put(playerId, 0.0);
            lastFlightReset.put(playerId, currentDay);
        }
    }

    private void startRegenTask(Player player) {
        UUID playerId = player.getUniqueId();

        if (regenTasks.containsKey(playerId)) {
            return;
        }

        BukkitRunnable regenTask = new BukkitRunnable() {
            @Override
            public void run() {
                double flown = dailyFlightTracker.getOrDefault(playerId, 0.0);
                if (flown <= 0) {
                    this.cancel();
                    regenTasks.remove(playerId);
                    return;
                }

                flown -= REGEN_RATE_PER_SECOND / TICKS_PER_SECOND;
                if (flown < 0) flown = 0;
                dailyFlightTracker.put(playerId, flown);
            }
        };

        regenTask.runTaskTimer(plugin, 0, 1); // run every tick
        regenTasks.put(playerId, regenTask);
    }

    private void stopRegenTask(Player player) {
        UUID playerId = player.getUniqueId();
        if (regenTasks.containsKey(playerId)) {
            regenTasks.get(playerId).cancel();
            regenTasks.remove(playerId);
        }
    }



    private void disableFlight(Player player) {
        UUID playerId = player.getUniqueId();

        // Check if player is near a Flight Catalyst before disabling flight
        CatalystManager catalystManager = CatalystManager.getInstance();
        if (catalystManager != null && catalystManager.isNearCatalyst(player.getLocation(), CatalystType.FLIGHT)) {
            // Don't disable flight if a Flight Catalyst is active
            return;
        }

        if (player.getAllowFlight()) {
            player.setAllowFlight(false);
            player.setFlying(false);
            player.sendMessage(ChatColor.RED + "You have reached your flight limit!");
        }

        // Cancel the flight task if active
        if (flightTasks.containsKey(playerId)) {
            flightTasks.get(playerId).cancel();
            flightTasks.remove(playerId);
        }

        stopRegenTask(player);
    }

    private void displayFlightProgress(Player player, double flownDistanceKm, double remainingDistance) {
        if(petManager.getActivePet(player) != null && petManager.getActivePet(player).hasPerk(PetManager.PetPerk.FLIGHT)) {
            String progressMessage = ChatColor.AQUA + "Flown: " + ChatColor.GREEN + String.format("%.2f", flownDistanceKm) + " km " +
                    ChatColor.AQUA + "Remaining: " + ChatColor.RED + String.format("%.2f", Math.max(remainingDistance, 0)) + " km";
            sendActionBar(player, progressMessage);
        }
    }

    private void sendActionBar(Player player, String message) {
        player.spigot().sendMessage(net.md_5.bungee.api.ChatMessageType.ACTION_BAR, new net.md_5.bungee.api.chat.TextComponent(message));
    }
}
