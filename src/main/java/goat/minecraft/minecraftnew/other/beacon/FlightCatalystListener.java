package goat.minecraft.minecraftnew.other.beacon;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Logger;

import goat.minecraft.minecraftnew.subsystems.pets.perks.Flight;

/**
 * Listener for handling Flight Catalyst creative flight abilities.
 * Grants creative flight to players within range of Flight catalysts.
 */
public class FlightCatalystListener implements Listener {
    
    private static final Logger logger = Logger.getLogger(FlightCatalystListener.class.getName());
    
    private final JavaPlugin plugin;
    private final Set<UUID> playersWithCatalystFlight = new HashSet<>();
    private BukkitRunnable flightTask;
    
    public FlightCatalystListener(JavaPlugin plugin) {
        this.plugin = plugin;
        startFlightTask();
    }
    
    private void startFlightTask() {
        flightTask = new BukkitRunnable() {
            @Override
            public void run() {
                updatePlayerFlightStatus();
            }
        };
        flightTask.runTaskTimer(plugin, 0L, 10L); // Check every 0.5 seconds
    }
    
    private void updatePlayerFlightStatus() {
        try {
            CatalystManager catalystManager = CatalystManager.getInstance();
            if (catalystManager == null) {
                return;
            }
            
            for (Player player : plugin.getServer().getOnlinePlayers()) {
                UUID playerId = player.getUniqueId();
                boolean nearFlightCatalyst = catalystManager.isNearCatalyst(player.getLocation(), CatalystType.FLIGHT);
                boolean hadCatalystFlight = playersWithCatalystFlight.contains(playerId);

                // Restore flight time while within range
                if (nearFlightCatalyst) {
                    Flight flight = Flight.getInstance();
                    if (flight != null) {
                        flight.restoreFlightSeconds(player, 1); // 1s every 0.5s => 2s per second
                    }
                }
                
                // Player entered catalyst range
                if (nearFlightCatalyst && !hadCatalystFlight && player.getGameMode() != GameMode.CREATIVE && player.getGameMode() != GameMode.SPECTATOR) {
                    playersWithCatalystFlight.add(playerId);
                    player.setAllowFlight(true);
                    player.setFlying(true);
                    player.sendMessage("§b✈ Flight enabled by Catalyst of Flight!");
                    
                    logger.fine(String.format("Enabled flight for player %s due to Flight Catalyst", player.getName()));
                }
                // Player left catalyst range
                else if (!nearFlightCatalyst && hadCatalystFlight) {
                    playersWithCatalystFlight.remove(playerId);
                    
                    // Only disable flight if they're not in creative/spectator mode
                    if (player.getGameMode() != GameMode.CREATIVE && player.getGameMode() != GameMode.SPECTATOR) {
                        player.setAllowFlight(false);
                        player.setFlying(false);
                        player.sendMessage("§c✈ Flight disabled - left catalyst range.");
                    }
                    
                    logger.fine(String.format("Disabled flight for player %s - left Flight Catalyst range", player.getName()));
                }
            }
            
        } catch (Exception e) {
            logger.warning(String.format("Failed to update flight status: %s", e.getMessage()));
        }
    }
    
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        // Handle immediate flight updates when players move between chunks
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();
        
        try {
            CatalystManager catalystManager = CatalystManager.getInstance();
            if (catalystManager == null) {
                return;
            }
            
            boolean nearFlightCatalyst = catalystManager.isNearCatalyst(player.getLocation(), CatalystType.FLIGHT);
            boolean hadCatalystFlight = playersWithCatalystFlight.contains(playerId);
            
            // Quick response for entering catalyst range
            if (nearFlightCatalyst && !hadCatalystFlight && player.getGameMode() != GameMode.CREATIVE && player.getGameMode() != GameMode.SPECTATOR) {
                playersWithCatalystFlight.add(playerId);
                player.setAllowFlight(true);
                player.setFlying(true);
                player.sendMessage("§b✈ Flight enabled by Catalyst of Flight!");
            }
            
        } catch (Exception e) {
            logger.warning(String.format("Failed to process flight catalyst move event for player %s: %s", 
                          player.getName(), e.getMessage()));
        }
    }
    
    public void cleanup() {
        if (flightTask != null) {
            flightTask.cancel();
        }
        
        // Disable catalyst flight for all players
        for (UUID playerId : playersWithCatalystFlight) {
            Player player = plugin.getServer().getPlayer(playerId);
            if (player != null && player.getGameMode() != GameMode.CREATIVE && player.getGameMode() != GameMode.SPECTATOR) {
                player.setAllowFlight(false);
                player.setFlying(false);
            }
        }
        playersWithCatalystFlight.clear();
    }
}