package goat.minecraft.minecraftnew.subsystems.beacon;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.util.Vector;

import java.util.logging.Logger;

/**
 * Listener for handling Fortitude Catalyst knockback immunity.
 */
public class FortitudeCatalystListener implements Listener {
    
    private static final Logger logger = Logger.getLogger(FortitudeCatalystListener.class.getName());
    
    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        // Check if the damaged entity is a player
        if (!(event.getEntity() instanceof Player)) {
            return;
        }
        
        Player targetPlayer = (Player) event.getEntity();
        
        try {
            CatalystManager catalystManager = CatalystManager.getInstance();
            if (catalystManager == null) {
                return;
            }
            
            // Check if target player is near a Fortitude catalyst
            if (catalystManager.isNearCatalyst(targetPlayer.getLocation(), CatalystType.FORTITUDE)) {
                // Cancel knockback by setting velocity to zero after a short delay
                // This prevents the knockback from being applied
                targetPlayer.getServer().getScheduler().runTask(
                    catalystManager.getPlugin(),
                    () -> {
                        Vector currentVelocity = targetPlayer.getVelocity();
                        // Only reset horizontal knockback, preserve natural falling
                        targetPlayer.setVelocity(new Vector(0, currentVelocity.getY(), 0));
                    }
                );
                
                logger.fine(String.format("Prevented knockback for player %s due to Fortitude Catalyst", 
                           targetPlayer.getName()));
            }
            
        } catch (Exception e) {
            logger.warning(String.format("Failed to process Fortitude Catalyst knockback immunity for player %s: %s", 
                          targetPlayer.getName(), e.getMessage()));
        }
    }
}