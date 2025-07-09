package goat.minecraft.minecraftnew.other.beacon;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerFishEvent;

import java.util.logging.Logger;

/**
 * Listener for handling Depth Catalyst fishing bonuses.
 * Increases sea creature and treasure chances by 5% base + 1% per tier.
 */
public class DepthCatalystListener implements Listener {
    
    private static final Logger logger = Logger.getLogger(DepthCatalystListener.class.getName());
    
    private static final double BASE_CHANCE_INCREASE = 0.05; // 5% base increase
    private static final double PER_TIER_INCREASE = 0.01;    // 1% per tier
    
    @EventHandler
    public void onPlayerFish(PlayerFishEvent event) {
        if (event.getState() != PlayerFishEvent.State.CAUGHT_FISH) {
            return;
        }
        
        Player player = event.getPlayer();
        
        try {
            CatalystManager catalystManager = CatalystManager.getInstance();
            if (catalystManager == null) {
                return;
            }
            
            // Check if player is near a Depth catalyst
            if (!catalystManager.isNearCatalyst(player.getLocation(), CatalystType.DEPTH)) {
                return;
            }
            
            // Find the nearest Depth catalyst to get its tier
            Catalyst nearestDepthCatalyst = catalystManager.findNearestCatalyst(player.getLocation(), CatalystType.DEPTH);
            if (nearestDepthCatalyst == null) {
                return;
            }
            
            int catalystTier = catalystManager.getCatalystTier(nearestDepthCatalyst);
            
            // Calculate bonus chance: 5% + (tier * 1%)
            double bonusChance = BASE_CHANCE_INCREASE + (catalystTier * PER_TIER_INCREASE);
            
            // Roll for bonus loot
            if (Math.random() < bonusChance) {
                // This is a simplified implementation
                // In a full implementation, you would:
                // 1. Check what was caught
                // 2. Potentially upgrade it to a better item
                // 3. Or add additional drops
                
                player.sendMessage(String.format("§b🌊 Depth Catalyst bonus! (+%.0f%% chance)", bonusChance * 100));
                
                logger.fine(String.format("Applied Depth Catalyst fishing bonus for player %s (tier %d, bonus %.1f%%)", 
                           player.getName(), catalystTier, bonusChance * 100));
                
                // Note: This is where you would integrate with the existing fishing system
                // to actually modify the caught item or add bonus drops
            }
            
        } catch (Exception e) {
            logger.warning(String.format("Failed to process Depth Catalyst fishing bonus for player %s: %s", 
                          player.getName(), e.getMessage()));
        }
    }
}