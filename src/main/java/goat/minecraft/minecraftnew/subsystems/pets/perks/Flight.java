package goat.minecraft.minecraftnew.subsystems.pets.perks;

import goat.minecraft.minecraftnew.MinecraftNew;
import goat.minecraft.minecraftnew.other.beacon.CatalystManager;
import goat.minecraft.minecraftnew.other.beacon.CatalystType;
import goat.minecraft.minecraftnew.subsystems.pets.PetManager;
import goat.minecraft.minecraftnew.other.skilltree.Skill;
import goat.minecraft.minecraftnew.other.skilltree.SkillTreeManager;
import goat.minecraft.minecraftnew.other.skilltree.Talent;
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

    private static final int MAX_FLIGHT_SECONDS_AT_LEVEL_100 = 60; // Max flight time at level 100 in seconds
    private static final int DRAIN_RATE_PER_SECOND = 1; // Drain rate in seconds
    private static final int TICKS_PER_SECOND = 20; // Number of ticks in one second
    private final PetManager petManager;
    private final Map<UUID, Integer> dailyFlightTracker = new HashMap<>();
    private final Map<UUID, Long> lastFlightReset = new HashMap<>();
    private final Map<UUID, BukkitRunnable> flightTasks = new HashMap<>();
    private final JavaPlugin plugin;

    public Flight(JavaPlugin plugin) {
        this.petManager = PetManager.getInstance(plugin);
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

        // Calculate the maximum flight time based on player's level
        int petLevel = activePet != null ? activePet.getLevel() : 0;
        int flightTalent = 0;
        if (SkillTreeManager.getInstance() != null) {
            flightTalent = SkillTreeManager.getInstance().getTalentLevel(player.getUniqueId(), Skill.TAMING, Talent.FLIGHT);
        }
        int maxFlightSeconds = calculateMaxFlightSeconds(player, petLevel + (flightTalent * 10));

        // Calculate remaining flight time
        int flownSeconds = dailyFlightTracker.getOrDefault(playerId, 0);
        int remainingSeconds = maxFlightSeconds - flownSeconds;

        // Display flight progress
        displayFlightProgress(player, flownSeconds, remainingSeconds);

        CatalystManager catalystManager = CatalystManager.getInstance();
        boolean nearCatalyst = catalystManager != null && catalystManager.isNearCatalyst(player.getLocation(), CatalystType.FLIGHT);
        boolean hasPerk = activePet != null && activePet.hasPerk(PetManager.PetPerk.FLIGHT);

        // Must have either catalyst or perk to fly
        if (!hasPerk && !nearCatalyst) {
            disableFlight(player);
            return;
        }

        // Check if the player has exceeded the flight limit
        if (remainingSeconds <= 0) {
            disableFlight(player);
            return;
        }

        enableFlight(player);
    }

    /**
     * Calculates the maximum flight distance based on the player's pet level.
     *
     * @param level The pet level (0-100).
     * @return The maximum flight distance in kilometers.
     */
    private int calculateMaxFlightSeconds(Player player, int level) {
        // Flight time scales linearly with level from 0s to MAX_FLIGHT_SECONDS_AT_LEVEL_100 at level 100
        int seconds = (int) (MAX_FLIGHT_SECONDS_AT_LEVEL_100 * (level / 100.0));
        int talentLevel = 0;
        if (SkillTreeManager.getInstance() != null) {
            talentLevel = SkillTreeManager.getInstance().getTalentLevel(player.getUniqueId(), Skill.TAMING, Talent.FLIGHT);
        }
        if(PetManager.getInstance(MinecraftNew.getInstance()).getActivePet(player).hasPerk(PetManager.PetPerk.FLIGHT)){
            seconds += talentLevel * 6; // each talent level adds 6 seconds
        }
        return seconds;
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
                private int tickCounter = 0;

                @Override
                public void run() {
                    // If the player stops flying, stop the task
                    if (!player.isFlying()) {
                        this.cancel();
                        flightTasks.remove(playerId);
                        return;
                    }

                    tickCounter++;
                    if (tickCounter >= TICKS_PER_SECOND) {
                        tickCounter = 0;

                        int flown = dailyFlightTracker.getOrDefault(playerId, 0);
                        flown += DRAIN_RATE_PER_SECOND;
                        dailyFlightTracker.put(playerId, flown);

                        // Get the pet's level and max time
                        PetManager.Pet activePet = petManager.getActivePet(player);
                        int petLevel = activePet != null ? activePet.getLevel() : 0;
                        int maxSeconds = calculateMaxFlightSeconds(player, petLevel);

                        // Check if the player exceeds the limit
                        if (flown >= maxSeconds) {
                            disableFlight(player);
                            this.cancel();
                        }
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
            dailyFlightTracker.put(playerId, 0);
            lastFlightReset.put(playerId, currentDay);
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
        if(petManager.getActivePet(player).hasPerk(PetManager.PetPerk.BROOMSTICK)){
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

    }

    private void displayFlightProgress(Player player, int flownSeconds, int remainingSeconds) {
        if(petManager.getActivePet(player) != null && petManager.getActivePet(player).hasPerk(PetManager.PetPerk.FLIGHT)) {
            String progressMessage = ChatColor.AQUA + "Flown: " + ChatColor.GREEN + flownSeconds + "s " +
                    ChatColor.AQUA + "Remaining: " + ChatColor.RED + Math.max(remainingSeconds, 0) + "s";
            sendActionBar(player, progressMessage);
        }
    }

    private void sendActionBar(Player player, String message) {
        player.spigot().sendMessage(net.md_5.bungee.api.ChatMessageType.ACTION_BAR, new net.md_5.bungee.api.chat.TextComponent(message));
    }
}
