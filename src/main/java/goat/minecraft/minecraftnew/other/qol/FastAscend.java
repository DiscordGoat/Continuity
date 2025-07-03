package goat.minecraft.minecraftnew.other.qol;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.util.Vector;

/**
 * Gives players a quick upward boost when they look straight up while flying.
 */
public class FastAscend implements Listener {

    // Triggered every time the player moves.
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();

        // Only apply when the player is currently flying
        if (!player.isFlying()) {
            return;
        }

        // Pitch is negative when looking up. About -90 is straight up.
        if (player.getLocation().getPitch() > -85) {
            return;
        }

        Vector velocity = player.getVelocity();
        // Only boost if the player is already moving upward
        if (velocity.getY() <= 0) {
            return;
        }

        // Increase upward velocity but cap it to avoid excessive speed
        double boostedY = Math.min(velocity.getY() + 0.5, 1.5);
        player.setVelocity(new Vector(velocity.getX(), boostedY, velocity.getZ()));
    }
}
