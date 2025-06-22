package goat.minecraft.minecraftnew.subsystems.combat.damage.strategies;

import goat.minecraft.minecraftnew.subsystems.beacon.Catalyst;
import goat.minecraft.minecraftnew.subsystems.beacon.CatalystManager;
import goat.minecraft.minecraftnew.subsystems.beacon.CatalystType;
import goat.minecraft.minecraftnew.subsystems.combat.damage.DamageCalculationContext;
import goat.minecraft.minecraftnew.subsystems.combat.damage.DamageCalculationResult;
import goat.minecraft.minecraftnew.subsystems.combat.damage.DamageCalculationStrategy;
import org.bukkit.entity.Player;

import java.util.logging.Logger;

/**
 * Strategy for calculating damage reduction from Fortitude Catalyst effects.
 * Grants damage reduction of 40% base + 5% per tier and knockback immunity to players within range.
 */
public class FortitudeCatalystDamageStrategy implements DamageCalculationStrategy {
    
    private static final Logger logger = Logger.getLogger(FortitudeCatalystDamageStrategy.class.getName());
    
    private static final double BASE_DAMAGE_REDUCTION = 0.40; // 40% base reduction
    private static final double PER_TIER_REDUCTION = 0.05;    // 5% per tier
    
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
        double originalDamage = context.getBaseDamage();
        
        try {
            CatalystManager catalystManager = CatalystManager.getInstance();
            if (catalystManager == null) {
                return DamageCalculationResult.noChange(originalDamage);
            }
            
            // Check if target player is near a Fortitude catalyst
            if (!catalystManager.isNearCatalyst(targetPlayer.getLocation(), CatalystType.FORTITUDE)) {
                return DamageCalculationResult.noChange(originalDamage);
            }
            
            // Find the nearest Fortitude catalyst to get its tier
            Catalyst nearestFortitudeCatalyst = catalystManager.findNearestCatalyst(targetPlayer.getLocation(), CatalystType.FORTITUDE);
            if (nearestFortitudeCatalyst == null) {
                return DamageCalculationResult.noChange(originalDamage);
            }
            
            int catalystTier = catalystManager.getCatalystTier(nearestFortitudeCatalyst);
            
            // Calculate damage reduction: 40% + (tier * 5%)
            double reductionPercentage = BASE_DAMAGE_REDUCTION + (catalystTier * PER_TIER_REDUCTION);
            // Cap reduction at 95% to prevent complete immunity
            reductionPercentage = Math.min(reductionPercentage, 0.95);
            
            double multiplier = 1.0 - reductionPercentage; // Convert reduction to multiplier
            double finalDamage = originalDamage * multiplier;
            
            DamageCalculationResult.DamageModifier modifier = 
                DamageCalculationResult.DamageModifier.multiplicative(
                    "Fortitude Catalyst", 
                    multiplier, 
                    String.format("Tier %d catalyst protection (-%.0f%%)", catalystTier, reductionPercentage * 100)
                );
            
            logger.fine(String.format("Applied Fortitude Catalyst damage reduction: %s (tier %d) -> %.1f%% reduction", 
                       targetPlayer.getName(), catalystTier, reductionPercentage * 100));
            
            return DamageCalculationResult.withModifier(originalDamage, finalDamage, modifier);
            
        } catch (Exception e) {
            logger.warning(String.format("Failed to calculate Fortitude Catalyst damage reduction for player %s: %s", 
                          targetPlayer.getName(), e.getMessage()));
            return DamageCalculationResult.noChange(originalDamage);
        }
    }
    
    @Override
    public boolean isApplicable(DamageCalculationContext context) {
        // Apply to all damage events where the target is a player
        return context.getTarget() instanceof Player;
    }
    
    @Override
    public int getPriority() {
        return 65; // High priority for defensive calculations
    }
    
    @Override
    public String getName() {
        return "Fortitude Catalyst Damage Reduction";
    }
}