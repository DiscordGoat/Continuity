package goat.minecraft.minecraftnew.subsystems.combat.damage.strategies;

import goat.minecraft.minecraftnew.other.additionalfunctionality.BlessingUtils;
import goat.minecraft.minecraftnew.other.skilltree.Skill;
import goat.minecraft.minecraftnew.subsystems.brewing.PotionManager;
import goat.minecraft.minecraftnew.subsystems.brewing.PotionEffectPreferences;
import goat.minecraft.minecraftnew.subsystems.combat.config.CombatConfiguration;
import goat.minecraft.minecraftnew.subsystems.combat.damage.DamageCalculationContext;
import goat.minecraft.minecraftnew.subsystems.combat.damage.DamageCalculationResult;
import goat.minecraft.minecraftnew.subsystems.combat.damage.DamageCalculationStrategy;
import goat.minecraft.minecraftnew.other.skilltree.SkillTreeManager;
import goat.minecraft.minecraftnew.other.skilltree.Talent;
import goat.minecraft.minecraftnew.utils.devtools.XPManager;
import org.bukkit.Bukkit;
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
            // Base arrow damage no longer scales with combat level
            double skillMultiplier = 1.0;

            // Apply Bow Mastery talent bonus
            if (SkillTreeManager.getInstance() != null) {
                int bowLevel = SkillTreeManager.getInstance()
                        .getTalentLevel(shooter.getUniqueId(), Skill.COMBAT, Talent.BOW_MASTERY);
                if (bowLevel > 0) {
                    double talentMult = 1.0 + (bowLevel * 0.08);
                    finalDamage *= talentMult;
                    modifiers.add(DamageCalculationResult.DamageModifier.multiplicative(
                            "Bow Mastery", talentMult, "+" + (bowLevel * 8) + "% Arrow Damage"));
                }
            }
            
            // Apply Potion of Recurve bonus if active
            if (PotionManager.isActive("Potion of Recurve", shooter)
                    && PotionEffectPreferences.isEnabled(shooter, "Potion of Recurve")) {
                double potionMultiplier = config.getRecurveDamageBonus();
                finalDamage *= potionMultiplier;
                
                modifiers.add(DamageCalculationResult.DamageModifier.multiplicative(
                    "Potion of Recurve", 
                    potionMultiplier, 
                    "Recurve potion effect"
                ));
            }
            if (SkillTreeManager.getInstance() != null &&
                    SkillTreeManager.getInstance().hasTalent(shooter, Talent.RECURVE_MASTERY)) {
                finalDamage *= 1.05;
                modifiers.add(DamageCalculationResult.DamageModifier.multiplicative(
                        "Recurve Mastery", 1.05, "+5% Arrow Damage"));
            }
            if(BlessingUtils.hasFullSetBonus(shooter.getPlayer(), "Lost Legion")){
                modifiers.add(DamageCalculationResult.DamageModifier.multiplicative("Lost Legion Set", 1.25, "Full set bonus"));
                Bukkit.getLogger().info("Lost Legion buff applied");
            }
            
            logger.fine(String.format("Applied ranged damage bonuses: %s -> %.1f total damage",
                       shooter.getName(), finalDamage));
            
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