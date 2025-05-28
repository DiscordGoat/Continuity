package goat.minecraft.minecraftnew.subsystems.combat.damage.strategies;

import goat.minecraft.minecraftnew.subsystems.brewing.PotionManager;
import goat.minecraft.minecraftnew.subsystems.combat.config.CombatConfiguration;
import goat.minecraft.minecraftnew.subsystems.combat.damage.DamageCalculationContext;
import goat.minecraft.minecraftnew.subsystems.combat.damage.DamageCalculationResult;
import goat.minecraft.minecraftnew.subsystems.combat.damage.DamageCalculationStrategy;
import goat.minecraft.minecraftnew.utils.devtools.XPManager;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Strategy for calculating ranged damage bonuses based on player combat skill level and active potions.
 */
public class RangedDamageStrategy implements DamageCalculationStrategy {
    
    private static final Logger logger = Logger.getLogger(RangedDamageStrategy.class.getName());
    
    private final CombatConfiguration.DamageConfig config;
    private final XPManager xpManager;
    
    public RangedDamageStrategy(CombatConfiguration.DamageConfig config, XPManager xpManager) {
        this.config = config;
        this.xpManager = xpManager;
    }
    
    @Override
    public DamageCalculationResult calculateDamage(DamageCalculationContext context) {
        if (!isApplicable(context)) {
            return DamageCalculationResult.noChange(context.getBaseDamage());
        }
        
        Player shooter = context.getAttackerPlayer().get();
        double originalDamage = context.getBaseDamage();
        double finalDamage = originalDamage;
        List<DamageCalculationResult.DamageModifier> modifiers = new ArrayList<>();
        
        try {
            // Apply combat skill bonus
            int combatLevel = xpManager.getPlayerLevel(shooter, "Combat");
            int cappedLevel = Math.min(combatLevel, config.getMaxSkillLevel());
            
            double skillMultiplier = 1.0 + (cappedLevel * config.getRangedPerLevel());
            finalDamage *= skillMultiplier;
            
            modifiers.add(DamageCalculationResult.DamageModifier.multiplicative(
                "Ranged Combat Skill", 
                skillMultiplier, 
                String.format("Level %d ranged bonus", cappedLevel)
            ));
            
            // Apply Potion of Recurve bonus if active
            if (PotionManager.isActive("Potion of Recurve", shooter)) {
                double potionMultiplier = config.getRecurveDamageBonus();
                finalDamage *= potionMultiplier;
                
                modifiers.add(DamageCalculationResult.DamageModifier.multiplicative(
                    "Potion of Recurve", 
                    potionMultiplier, 
                    "Recurve potion effect"
                ));
            }
            
            logger.fine(String.format("Applied ranged damage bonuses: %s (level %d) -> %.1f total damage", 
                       shooter.getName(), cappedLevel, finalDamage));
            
            return new DamageCalculationResult(originalDamage, finalDamage, modifiers);
            
        } catch (Exception e) {
            logger.warning(String.format("Failed to calculate ranged damage for player %s: %s", 
                          shooter.getName(), e.getMessage()));
            return DamageCalculationResult.noChange(originalDamage);
        }
    }
    
    @Override
    public boolean isApplicable(DamageCalculationContext context) {
        return context.getAttackerPlayer().isPresent() && 
               context.isProjectile() &&
               context.getProjectile().isPresent() &&
               context.getProjectile().get() instanceof Arrow;
    }
    
    @Override
    public int getPriority() {
        return 80; // High priority for base skill calculations
    }
    
    @Override
    public String getName() {
        return "Ranged Combat Skill Damage";
    }
}