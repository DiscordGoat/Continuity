package goat.minecraft.minecraftnew.subsystems.combat.damage.strategies;

import goat.minecraft.minecraftnew.other.beacon.Catalyst;
import goat.minecraft.minecraftnew.other.beacon.CatalystManager;
import goat.minecraft.minecraftnew.other.beacon.CatalystType;
import goat.minecraft.minecraftnew.subsystems.combat.damage.DamageCalculationContext;
import goat.minecraft.minecraftnew.subsystems.combat.damage.DamageCalculationResult;
import goat.minecraft.minecraftnew.subsystems.combat.damage.DamageCalculationStrategy;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.logging.Logger;

/**
 * Strategy for calculating spirit damage reduction from Insanity Catalyst effects.
 * Reduces spirit damage by 50% base + 5% per tier to players within range.
 */
public class InsanityCatalystDamageStrategy implements DamageCalculationStrategy {
    
    private static final Logger logger = Logger.getLogger(InsanityCatalystDamageStrategy.class.getName());
    
    private static final double BASE_SPIRIT_DAMAGE_REDUCTION = 0.50; // 50% base reduction
    private static final double PER_TIER_REDUCTION = 0.05;           // 5% per tier
    
    @Override
    public DamageCalculationResult calculateDamage(DamageCalculationContext context) {
        if (!isApplicable(context)) {
            return DamageCalculationResult.noChange(context.getBaseDamage());
        }
        
        // Check if the target is a player (damage reduction applies to damage taken)
        if (!(context.getTarget() instanceof Player)) {
            return DamageCalculationResult.noChange(context.getBaseDamage());
        }
        
        Player targetPlayer = (Player) context.getTarget();
        Entity attacker = context.getAttacker();
        double originalDamage = context.getBaseDamage();
        
        // Check if the attacker is a spirit-type entity
        if (!isSpiritEntity(attacker)) {
            return DamageCalculationResult.noChange(originalDamage);
        }
        
        try {
            CatalystManager catalystManager = CatalystManager.getInstance();
            if (catalystManager == null) {
                return DamageCalculationResult.noChange(originalDamage);
            }
            
            // Check if target player is near an Insanity catalyst
            if (!catalystManager.isNearCatalyst(targetPlayer.getLocation(), CatalystType.INSANITY)) {
                return DamageCalculationResult.noChange(originalDamage);
            }
            
            // Find the nearest Insanity catalyst to get its tier
            Catalyst nearestInsanityCatalyst = catalystManager.findNearestCatalyst(targetPlayer.getLocation(), CatalystType.INSANITY);
            if (nearestInsanityCatalyst == null) {
                return DamageCalculationResult.noChange(originalDamage);
            }
            
            int catalystTier = catalystManager.getCatalystTier(nearestInsanityCatalyst);
            
            // Calculate spirit damage reduction: 50% + (tier * 5%)
            double reductionPercentage = BASE_SPIRIT_DAMAGE_REDUCTION + (catalystTier * PER_TIER_REDUCTION);
            // Cap reduction at 95% to prevent complete immunity
            reductionPercentage = Math.min(reductionPercentage, 0.95);
            
            double multiplier = 1.0 - reductionPercentage; // Convert reduction to multiplier
            double finalDamage = originalDamage * multiplier;
            
            DamageCalculationResult.DamageModifier modifier = 
                DamageCalculationResult.DamageModifier.multiplicative(
                    "Insanity Catalyst", 
                    multiplier, 
                    String.format("Tier %d spirit protection (-%.0f%%)", catalystTier, reductionPercentage * 100)
                );
            
            logger.fine(String.format("Applied Insanity Catalyst spirit damage reduction: %s (tier %d) -> %.1f%% reduction", 
                       targetPlayer.getName(), catalystTier, reductionPercentage * 100));

            
            return DamageCalculationResult.withModifier(originalDamage, finalDamage, modifier);
            
        } catch (Exception e) {
            logger.warning(String.format("Failed to calculate Insanity Catalyst spirit damage reduction for player %s: %s", 
                          targetPlayer.getName(), e.getMessage()));
            return DamageCalculationResult.noChange(originalDamage);
        }
    }
    
    private boolean isSpiritEntity(Entity entity) {
        if (entity == null) {
            return false;
        }
        
        // Check if the entity is a spirit-type based on name or type
        String entityName = entity.getCustomName();
        if (entityName != null) {
            String lowerName = entityName.toLowerCase();
            return lowerName.contains("spirit") || lowerName.contains("ghost") || lowerName.contains("soul");
        }
        
        // You could also check entity type if spirits have specific entity types
        // For example: entity.getType() == EntityType.VEX (if spirits are vexes)
        
        return false; // Default to false if not identifiable as a spirit
    }
    
    @Override
    public boolean isApplicable(DamageCalculationContext context) {
        // Apply to all damage events where the target is a player and attacker could be a spirit
        return context.getTarget() instanceof Player && context.getAttacker() != null;
    }
    
    @Override
    public int getPriority() {
        return 62; // High priority for defensive calculations, but lower than Fortitude
    }
    
    @Override
    public String getName() {
        return "Insanity Catalyst Spirit Damage Reduction";
    }
}