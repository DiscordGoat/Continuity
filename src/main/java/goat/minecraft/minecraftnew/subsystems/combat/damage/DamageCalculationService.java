package goat.minecraft.minecraftnew.subsystems.combat.damage;

import goat.minecraft.minecraftnew.subsystems.combat.config.CombatConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Central service for coordinating damage calculations across all strategies.
 * Manages strategy execution order and aggregates results.
 */
public class DamageCalculationService {
    
    private static final Logger logger = Logger.getLogger(DamageCalculationService.class.getName());
    
    private final List<DamageCalculationStrategy> strategies;
    private final CombatConfiguration.BuffConfig buffConfig;
    
    public DamageCalculationService(CombatConfiguration.BuffConfig buffConfig) {
        this.strategies = new ArrayList<>();
        this.buffConfig = buffConfig;
    }
    
    /**
     * Registers a damage calculation strategy.
     * Strategies are automatically sorted by priority when added.
     * 
     * @param strategy The strategy to register
     */
    public void registerStrategy(DamageCalculationStrategy strategy) {
        if (strategy == null) {
            throw new IllegalArgumentException("Strategy cannot be null");
        }
        
        strategies.add(strategy);
        strategies.sort(Comparator.comparingInt(DamageCalculationStrategy::getPriority).reversed());
        
        logger.info(String.format("Registered damage strategy: %s (priority: %d)", 
                   strategy.getName(), strategy.getPriority()));
    }
    
    /**
     * Removes a strategy from the service.
     * 
     * @param strategyClass The class of the strategy to remove
     */
    public void unregisterStrategy(Class<? extends DamageCalculationStrategy> strategyClass) {
        strategies.removeIf(strategy -> strategy.getClass().equals(strategyClass));
        logger.info(String.format("Unregistered damage strategy: %s", strategyClass.getSimpleName()));
    }
    
    /**
     * Processes a damage event through all applicable strategies.
     * 
     * @param event The damage event to process
     * @return The aggregated damage calculation result
     */
    public DamageCalculationResult processDamageEvent(EntityDamageByEntityEvent event) {
        if (event == null || event.isCancelled()) {
            throw new IllegalArgumentException("Event cannot be null or cancelled");
        }
        
        try {
            DamageCalculationContext context = createContext(event);
            return calculateDamage(context);
            
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to process damage event", e);
            throw new DamageCalculationException("Damage calculation failed", e);
        }
    }
    
    /**
     * Calculates damage using all applicable strategies.
     * 
     * @param context The damage calculation context
     * @return The final damage calculation result
     */
    public DamageCalculationResult calculateDamage(DamageCalculationContext context) {
        double currentDamage = context.getBaseDamage();
        List<DamageCalculationResult.DamageModifier> allModifiers = new ArrayList<>();
        
        logger.fine(String.format("Processing damage calculation: %.1f base damage", currentDamage));
        
        for (DamageCalculationStrategy strategy : strategies) {
            try {
                if (!strategy.isApplicable(context)) {
                    logger.finest(String.format("Strategy %s not applicable", strategy.getName()));
                    continue;
                }
                
                // Create a new context with the current damage value
                DamageCalculationContext updatedContext = DamageCalculationContext.builder()
                    .event(context.getEvent())
                    .attacker(context.getAttacker())
                    .target(context.getTarget())
                    .baseDamage(currentDamage)
                    .damageType(context.getDamageType())
                    .attackerPlayer(context.getAttackerPlayer().orElse(null))
                    .weapon(context.getWeapon().orElse(null))
                    .isProjectile(context.isProjectile())
                    .projectile(context.getProjectile().orElse(null))
                    .build();
                
                DamageCalculationResult result = strategy.calculateDamage(updatedContext);
                
                if (result.wasModified()) {
                    currentDamage = result.getFinalDamage();
                    allModifiers.addAll(result.getAppliedModifiers());
                    
                    logger.fine(String.format("Strategy %s applied: %.1f -> %.1f", 
                               strategy.getName(), result.getOriginalDamage(), result.getFinalDamage()));
                }
                
            } catch (Exception e) {
                logger.log(Level.WARNING, 
                          String.format("Strategy %s failed, skipping", strategy.getName()), e);
                // Continue with other strategies
            }
        }
        
        DamageCalculationResult finalResult = new DamageCalculationResult(
            context.getBaseDamage(), currentDamage, allModifiers);
        
        logger.fine(String.format("Final damage calculation: %s", finalResult.toString()));
        
        return finalResult;
    }
    
    /**
     * Creates a damage calculation context from a Bukkit damage event.
     */
    private DamageCalculationContext createContext(EntityDamageByEntityEvent event) {
        Entity attacker = event.getDamager();
        Entity target = event.getEntity();
        double baseDamage = event.getDamage();
        
        // Determine damage type and extract player/projectile information
        DamageCalculationContext.DamageType damageType;
        Player attackerPlayer = null;
        boolean isProjectile = false;
        Projectile projectile = null;
        
        if (attacker instanceof Player) {
            damageType = DamageCalculationContext.DamageType.MELEE;
            attackerPlayer = (Player) attacker;
        } else if (attacker instanceof Projectile) {
            projectile = (Projectile) attacker;
            isProjectile = true;
            damageType = DamageCalculationContext.DamageType.PROJECTILE;
            
            if (projectile.getShooter() instanceof Player) {
                attackerPlayer = (Player) projectile.getShooter();
                damageType = DamageCalculationContext.DamageType.RANGED;
            }
        } else {
            damageType = DamageCalculationContext.DamageType.OTHER;
        }
        
        return DamageCalculationContext.builder()
            .event(event)
            .attacker(attacker)
            .target(target)
            .baseDamage(baseDamage)
            .damageType(damageType)
            .attackerPlayer(attackerPlayer)
            .weapon(attackerPlayer != null ? attackerPlayer.getInventory().getItemInMainHand() : null)
            .isProjectile(isProjectile)
            .projectile(projectile)
            .build();
    }
    
    /**
     * Gets the list of registered strategies (read-only).
     * 
     * @return A copy of the strategies list
     */
    public List<DamageCalculationStrategy> getRegisteredStrategies() {
        return new ArrayList<>(strategies);
    }
    
    /**
     * Checks if damage calculation is enabled for a specific buff type.
     */
    public boolean isBuffEnabled(String buffType) {
        switch (buffType.toLowerCase()) {
            case "skill_damage_scaling":
                return buffConfig.isSkillDamageScaling();
            case "potion_interactions":
                return buffConfig.isPotionInteractions();
            case "monster_level_scaling":
                return buffConfig.isMonsterLevelScaling();
            case "projectile_bonuses":
                return buffConfig.isProjectileBonuses();
            default:
                return true;
        }
    }
    
    /**
     * Exception thrown when damage calculation fails
     */
    public static class DamageCalculationException extends RuntimeException {
        public DamageCalculationException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}