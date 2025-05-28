package goat.minecraft.minecraftnew.subsystems.combat.damage;

import org.bukkit.entity.Entity;

/**
 * Strategy interface for different damage calculation approaches.
 * Allows for flexible and extensible damage modification systems.
 */
public interface DamageCalculationStrategy {
    
    /**
     * Calculates the modified damage based on the strategy's rules.
     * 
     * @param context The damage calculation context containing all relevant information
     * @return The damage calculation result with the new damage value and applied modifiers
     */
    DamageCalculationResult calculateDamage(DamageCalculationContext context);
    
    /**
     * Determines if this strategy should be applied to the given damage event.
     * 
     * @param context The damage calculation context
     * @return true if this strategy applies, false otherwise
     */
    boolean isApplicable(DamageCalculationContext context);
    
    /**
     * Gets the priority of this strategy. Higher values execute first.
     * This allows for proper ordering of damage calculations.
     * 
     * @return The priority value (0-100, where 100 is highest priority)
     */
    int getPriority();
    
    /**
     * Gets a human-readable name for this strategy (used for logging and debugging).
     * 
     * @return The strategy name
     */
    String getName();
}