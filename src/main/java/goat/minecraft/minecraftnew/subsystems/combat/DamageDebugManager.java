package goat.minecraft.minecraftnew.subsystems.combat;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.bukkit.entity.Player;

/**
 * Tracks players who have enabled detailed damage debug feedback.
 */
public final class DamageDebugManager {

    private static final Set<UUID> enabled = new HashSet<>();

    private DamageDebugManager() {
        // Utility class
    }

    /**
     * Toggles debug feedback for the given player.
     *
     * @return the new enabled state after toggling
     */
    public static boolean toggle(Player player) {
        UUID id = player.getUniqueId();
        if (enabled.contains(id)) {
            enabled.remove(id);
            return false;
        } else {
            enabled.add(id);
            return true;
        }
    }

    /**
     * Checks if debug feedback is enabled for the player.
     */
    public static boolean isEnabled(Player player) {
        return enabled.contains(player.getUniqueId());
    }
}

