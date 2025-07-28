package goat.minecraft.minecraftnew.subsystems.combat.damage.strategies;

import goat.minecraft.minecraftnew.other.skilltree.Skill;
import goat.minecraft.minecraftnew.other.skilltree.SkillTreeManager;
import goat.minecraft.minecraftnew.other.skilltree.Talent;
import goat.minecraft.minecraftnew.subsystems.combat.damage.DamageCalculationContext;
import goat.minecraft.minecraftnew.subsystems.combat.damage.DamageCalculationResult;
import goat.minecraft.minecraftnew.subsystems.combat.damage.DamageCalculationStrategy;
import org.bukkit.entity.Player;
import org.bukkit.entity.Entity;

/**
 * Increases player damage against Corpse mobs based on Post-Mortem Complications talents.
 */
public class PostMortemDamageStrategy implements DamageCalculationStrategy {

    @Override
    public DamageCalculationResult calculateDamage(DamageCalculationContext context) {
        if (!isApplicable(context)) {
            return DamageCalculationResult.noChange(context.getBaseDamage());
        }

        Player attacker = context.getAttackerPlayer().get();
        SkillTreeManager mgr = SkillTreeManager.getInstance();
        if (mgr == null) {
            return DamageCalculationResult.noChange(context.getBaseDamage());
        }

        int totalLevel = 0;
        totalLevel += mgr.getTalentLevel(attacker.getUniqueId(), Skill.TERRAFORMING, Talent.POST_MORTEM_COMPLICATIONS_I);
        totalLevel += mgr.getTalentLevel(attacker.getUniqueId(), Skill.TERRAFORMING, Talent.POST_MORTEM_COMPLICATIONS_II);
        totalLevel += mgr.getTalentLevel(attacker.getUniqueId(), Skill.TERRAFORMING, Talent.POST_MORTEM_COMPLICATIONS_III);
        totalLevel += mgr.getTalentLevel(attacker.getUniqueId(), Skill.TERRAFORMING, Talent.POST_MORTEM_COMPLICATIONS_IV);
        totalLevel += mgr.getTalentLevel(attacker.getUniqueId(), Skill.TERRAFORMING, Talent.POST_MORTEM_COMPLICATIONS_V);
        if (totalLevel <= 0) {
            return DamageCalculationResult.noChange(context.getBaseDamage());
        }

        double multiplier = 1.0 + (totalLevel * 0.05);
        double finalDamage = context.getBaseDamage() * multiplier;

        DamageCalculationResult.DamageModifier mod =
                DamageCalculationResult.DamageModifier.multiplicative(
                        "Post-Mortem", multiplier,
                        "+" + (int)(multiplier * 100 - 100) + "% vs Corpses"
                );

        return DamageCalculationResult.withModifier(context.getBaseDamage(), finalDamage, mod);
    }

    @Override
    public boolean isApplicable(DamageCalculationContext context) {
        if (context.getAttackerPlayer().isEmpty()) return false;
        Entity target = context.getTarget();
        return target != null && target.hasMetadata("CORPSE");
    }

    @Override
    public int getPriority() {
        return 73;
    }

    @Override
    public String getName() {
        return "Post-Mortem Damage";
    }
}
