package goat.minecraft.minecraftnew.subsystems.combat.damage.strategies;

import goat.minecraft.minecraftnew.other.skilltree.Skill;
import goat.minecraft.minecraftnew.other.skilltree.SkillTreeManager;
import goat.minecraft.minecraftnew.other.skilltree.Talent;
import goat.minecraft.minecraftnew.subsystems.combat.damage.DamageCalculationContext;
import goat.minecraft.minecraftnew.subsystems.combat.damage.DamageCalculationResult;
import goat.minecraft.minecraftnew.subsystems.combat.damage.DamageCalculationStrategy;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

/**
 * Reduces damage taken from sea creatures based on Exosuit talents.
 */
public class SeaCreatureDamageReductionStrategy implements DamageCalculationStrategy {
    @Override
    public DamageCalculationResult calculateDamage(DamageCalculationContext context) {
        if (!isApplicable(context)) {
            return DamageCalculationResult.noChange(context.getBaseDamage());
        }
        Player player = (Player) context.getTarget();
        SkillTreeManager mgr = SkillTreeManager.getInstance();
        if (mgr == null) {
            return DamageCalculationResult.noChange(context.getBaseDamage());
        }
        int total = 0;
        total += mgr.getTalentLevel(player.getUniqueId(), Skill.FISHING, Talent.EXOSUIT_I);
        total += mgr.getTalentLevel(player.getUniqueId(), Skill.FISHING, Talent.EXOSUIT_II);
        total += mgr.getTalentLevel(player.getUniqueId(), Skill.FISHING, Talent.EXOSUIT_III);
        total += mgr.getTalentLevel(player.getUniqueId(), Skill.FISHING, Talent.EXOSUIT_IV);
        total += mgr.getTalentLevel(player.getUniqueId(), Skill.FISHING, Talent.EXOSUIT_V);
        if (total <= 0) {
            return DamageCalculationResult.noChange(context.getBaseDamage());
        }
        double reduction = total * 0.02;
        double multiplier = Math.max(0, 1.0 - reduction);
        double finalDamage = context.getBaseDamage() * multiplier;

        DamageCalculationResult.DamageModifier mod =
                DamageCalculationResult.DamageModifier.multiplicative(
                        "Exosuit", multiplier,
                        "-" + (int)(reduction * 100) + "% from Sea Creatures"
                );
        return DamageCalculationResult.withModifier(context.getBaseDamage(), finalDamage, mod);
    }

    @Override
    public boolean isApplicable(DamageCalculationContext context) {
        Entity attacker = context.getAttacker();
        return context.getTarget() instanceof Player && attacker != null && attacker.hasMetadata("SEA_CREATURE");
    }

    @Override
    public int getPriority() {
        return 71;
    }

    @Override
    public String getName() {
        return "Sea Creature Damage Reduction";
    }
}
