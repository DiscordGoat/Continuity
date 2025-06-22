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
 * Strategy for calculating damage bonuses from Power Catalyst effects.
 * Grants damage bonus of 25% base + 5% per tier to players within range of Power catalysts.
 */
public class PowerCatalystDamageStrategy implements DamageCalculationStrategy {
    
    private static final Logger logger = Logger.getLogger(PowerCatalystDamageStrategy.class.getName());
    
    private static final double BASE_DAMAGE_BONUS = 0.25; // 25% base bonus
    private static final double PER_TIER_BONUS = 0.05;    // 5% per tier
    
    @Override
    public DamageCalculationResult calculateDamage(DamageCalculationContext context) {
        if (!isApplicable(context)) {
            return DamageCalculationResult.noChange(context.getBaseDamage());
        }
        
        Player player = context.getAttackerPlayer().get();
        double originalDamage = context.getBaseDamage();
        
        try {
            CatalystManager catalystManager = CatalystManager.getInstance();
            if (catalystManager == null) {
                return DamageCalculationResult.noChange(originalDamage);
            }
            
            // Check if player is near a Power catalyst
            if (!catalystManager.isNearCatalyst(player.getLocation(), CatalystType.POWER)) {
                return DamageCalculationResult.noChange(originalDamage);
            }
            
            // Find the nearest Power catalyst to get its tier
            Catalyst nearestPowerCatalyst = catalystManager.findNearestCatalyst(player.getLocation(), CatalystType.POWER);
            if (nearestPowerCatalyst == null) {
                return DamageCalculationResult.noChange(originalDamage);
            }
            
            int catalystTier = catalystManager.getCatalystTier(nearestPowerCatalyst);
            
            // Calculate damage multiplier: 1 + (25% + (tier * 5%))
            double bonusPercentage = BASE_DAMAGE_BONUS + (catalystTier * PER_TIER_BONUS);
            double multiplier = 1.0 + bonusPercentage;
            double finalDamage = originalDamage * multiplier;
            
            DamageCalculationResult.DamageModifier modifier = 
                DamageCalculationResult.DamageModifier.multiplicative(
                    "Power Catalyst", 
                    multiplier, 
                    String.format("Tier %d catalyst bonus (+%.0f%%)", catalystTier, bonusPercentage * 100)
                );
            
            logger.fine(String.format("Applied Power Catalyst damage bonus: %s (tier %d) -> %.1f%% increase", 
                       player.getName(), catalystTier, bonusPercentage * 100));
            
            return DamageCalculationResult.withModifier(originalDamage, finalDamage, modifier);
            
        } catch (Exception e) {
            logger.warning(String.format("Failed to calculate Power Catalyst damage for player %s: %s", 
                          player.getName(), e.getMessage()));
            return DamageCalculationResult.noChange(originalDamage);
        }
    }
    
    @Override
    public boolean isApplicable(DamageCalculationContext context) {
        // Apply to all player attacks (melee and ranged)
        return context.getAttackerPlayer().isPresent();
    }
    
    @Override
    public int getPriority() {
        return 60; // Medium-high priority, after base skill bonuses but before situational modifiers
    }
    
    @Override
    public String getName() {
        return "Power Catalyst Damage Bonus";
    }
}