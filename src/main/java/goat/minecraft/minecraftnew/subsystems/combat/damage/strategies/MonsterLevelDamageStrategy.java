package goat.minecraft.minecraftnew.subsystems.combat.damage.strategies;

import goat.minecraft.minecraftnew.other.additionalfunctionality.BlessingUtils;
import goat.minecraft.minecraftnew.subsystems.combat.config.CombatConfiguration;
import goat.minecraft.minecraftnew.subsystems.combat.damage.DamageCalculationContext;
import goat.minecraft.minecraftnew.subsystems.combat.damage.DamageCalculationResult;
import goat.minecraft.minecraftnew.subsystems.combat.damage.DamageCalculationStrategy;
import goat.minecraft.minecraftnew.subsystems.combat.utils.EntityLevelExtractor;
import org.bukkit.entity.*;

import java.util.logging.Logger;

/**
 * Strategy for scaling monster damage based on their level.
 * Extracts level information from monster names and applies appropriate scaling.
 */
public class MonsterLevelDamageStrategy implements DamageCalculationStrategy {
    
    private static final Logger logger = Logger.getLogger(MonsterLevelDamageStrategy.class.getName());
    
    private final CombatConfiguration.DamageConfig config;
    private final EntityLevelExtractor levelExtractor;
    
    public MonsterLevelDamageStrategy(CombatConfiguration.DamageConfig config) {
        this.config = config;
        this.levelExtractor = new EntityLevelExtractor();
    }
    
    @Override
    public DamageCalculationResult calculateDamage(DamageCalculationContext context) {
        if (!isApplicable(context)) {
            return DamageCalculationResult.noChange(context.getBaseDamage());
        }
        
        LivingEntity monster = getMonsterAttacker(context);
        if (monster == null) {
            return DamageCalculationResult.noChange(context.getBaseDamage());
        }
        
        double originalDamage = context.getBaseDamage();
        
        try {
            int monsterLevel = levelExtractor.extractLevelFromName(monster);
            
            if (monsterLevel <= 0) {
                logger.fine(String.format("No level found for monster %s, using base damage", 
                           monster.getType()));
                return DamageCalculationResult.noChange(originalDamage);
            }
            
            double multiplier = 1.0 + (monsterLevel * config.getMonsterPerLevel());

            double finalDamage = originalDamage * multiplier;
            
            DamageCalculationResult.DamageModifier modifier = 
                DamageCalculationResult.DamageModifier.multiplicative(
                    "Monster Level", 
                    multiplier, 
                    String.format("Level %d %s", monsterLevel, monster.getType())
                );
            
            logger.fine(String.format("Applied monster level damage: %s (level %d) -> %.1f%% increase", 
                       monster.getType(), monsterLevel, (multiplier - 1.0) * 100));
            
            return DamageCalculationResult.withModifier(originalDamage, finalDamage, modifier);
            
        } catch (Exception e) {
            logger.warning(String.format("Failed to calculate monster level damage for %s: %s", 
                          monster.getType(), e.getMessage()));
            return DamageCalculationResult.noChange(originalDamage);
        }
    }
    
    @Override
    public boolean isApplicable(DamageCalculationContext context) {
        // Must be damage to a player from a monster
        return context.getTarget() instanceof Player && getMonsterAttacker(context) != null;
    }
    
    @Override
    public int getPriority() {
        return 70; // Medium-high priority for monster scaling
    }
    
    @Override
    public String getName() {
        return "Monster Level Damage Scaling";
    }
    
    /**
     * Extracts the monster attacker from the damage context.
     * Handles both direct attacks and projectile attacks from monsters.
     */
    private LivingEntity getMonsterAttacker(DamageCalculationContext context) {
        Entity attacker = context.getAttacker();
        
        // Direct monster attack
        if (attacker instanceof Monster) {
            return (LivingEntity) attacker;
        }
        
        // Projectile from monster
        if (context.isProjectile() && context.getProjectile().isPresent()) {
            Projectile projectile = context.getProjectile().get();
            if (projectile.getShooter() instanceof Monster) {
                return (LivingEntity) projectile.getShooter();
            }
        }
        
        return null;
    }
}