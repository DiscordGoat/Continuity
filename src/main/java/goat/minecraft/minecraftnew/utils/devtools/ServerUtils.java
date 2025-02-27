package goat.minecraft.minecraftnew.utils.devtools;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ServerUtils {

    // Map to store the last time (in milliseconds) each player moved.
    private static final Map<UUID, Long> lastMovementTimestamps = new ConcurrentHashMap<>();
    // AFK threshold in milliseconds (5 seconds).
    private static final long AFK_THRESHOLD = 5000;

    /**
     * Call this method when a player moves (e.g. in your PlayerMoveEvent listener)
     * to update their last movement timestamp.
     *
     * @param player The player who moved.
     */
    public static void updatePlayerMovement(Player player) {
        lastMovementTimestamps.put(player.getUniqueId(), System.currentTimeMillis());
    }

    /**
     * Checks if the player is AFK.
     * A player is considered AFK if they have not triggered a PlayerMove event in the last 5 seconds.
     *
     * @param player The player to check.
     * @return true if the player is AFK, false otherwise.
     */
    public static boolean isPlayerAFK(Player player) {
        Long lastMovement = lastMovementTimestamps.get(player.getUniqueId());
        if (lastMovement == null) {
            // No record means we haven't received a movement event; assume not AFK.
            return false;
        }
        long timeSinceLastMovement = System.currentTimeMillis() - lastMovement;
        return timeSinceLastMovement >= AFK_THRESHOLD;
    }

    /**
     * Inner class to handle player movement events.
     * Register this listener in your plugin to automatically update players' movement timestamps.
     */
    public static class PlayerMoveListener implements Listener {

        @EventHandler
        public void onPlayerMove(PlayerMoveEvent event) {
            // Update the player's last movement timestamp each time they move.
            Player player = event.getPlayer();
            updatePlayerMovement(player);
        }
    }
}
