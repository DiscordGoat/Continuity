package goat.minecraft.minecraftnew.subsystems.combat.damage.strategies;

import goat.minecraft.minecraftnew.subsystems.combat.config.CombatConfiguration;
import goat.minecraft.minecraftnew.subsystems.combat.damage.DamageCalculationContext;
import goat.minecraft.minecraftnew.subsystems.combat.damage.DamageCalculationResult;
import goat.minecraft.minecraftnew.subsystems.combat.damage.DamageCalculationStrategy;
import goat.minecraft.minecraftnew.other.skilltree.SkillTreeManager;
import goat.minecraft.minecraftnew.other.skilltree.Talent;
import goat.minecraft.minecraftnew.utils.devtools.XPManager;
import org.bukkit.entity.Player;

import java.util.logging.Logger;

/**
 * Strategy for calculating melee damage bonuses based on player combat skill level.
 */
public class MeleeDamageStrategy implements DamageCalculationStrategy {
    
    private static final Logger logger = Logger.getLogger(MeleeDamageStrategy.class.getName());
    
    private final CombatConfiguration.DamageConfig config;
    private final XPManager xpManager;
    
    public MeleeDamageStrategy(CombatConfiguration.DamageConfig config, XPManager xpManager) {
        this.config = config;
        this.xpManager = xpManager;
    }
    
    @Override
    public DamageCalculationResult calculateDamage(DamageCalculationContext context) {
        if (!isApplicable(context)) {
            return DamageCalculationResult.noChange(context.getBaseDamage());
        }
        
        Player player = context.getAttackerPlayer().get();
        double originalDamage = context.getBaseDamage();
        
        try {
            // Base melee damage no longer scales with the player's combat level
            double multiplier = 1.0; // placeholder for future adjustments
            double finalDamage = originalDamage * multiplier;

            boolean strengthTalent = SkillTreeManager.getInstance() != null &&
                    SkillTreeManager.getInstance().hasTalent(player, Talent.STRENGTH_MASTERY);
            if (strengthTalent) {
                finalDamage *= 1.05;
            }
            
            DamageCalculationResult.DamageModifier modifier =
                DamageCalculationResult.DamageModifier.multiplicative(
                    "Combat Skill",
                    multiplier,
                    "Base melee bonus"
                );

            logger.fine(String.format("Applied melee damage bonus: %s -> %.1f%% increase",
                       player.getName(), (multiplier - 1.0) * 100));
            
            DamageCalculationResult result = DamageCalculationResult.withModifier(originalDamage, finalDamage, modifier);

            if (strengthTalent) {
                result.getAppliedModifiers().add(DamageCalculationResult.DamageModifier.multiplicative(
                        "Strength Mastery", 1.05, "+5% Damage"));
            }

            return result;
            
        } catch (Exception e) {
            logger.warning(String.format("Failed to calculate melee damage for player %s: %s", 
                          player.getName(), e.getMessage()));
            return DamageCalculationResult.noChange(originalDamage);
        }
    }
    
    @Override
    public boolean isApplicable(DamageCalculationContext context) {
        return context.getAttackerPlayer().isPresent() && 
               context.getDamageType() == DamageCalculationContext.DamageType.MELEE &&
               !context.isProjectile();
    }
    
    @Override
    public int getPriority() {
        return 80; // High priority for base skill calculations
    }
    
    @Override
    public String getName() {
        return "Melee Combat Skill Damage";
    }
}