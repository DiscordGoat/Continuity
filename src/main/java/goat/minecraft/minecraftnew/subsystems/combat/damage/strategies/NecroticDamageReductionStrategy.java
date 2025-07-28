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
 * Reduces damage taken from Corpse mobs based on Necrotic talents.
 */
public class NecroticDamageReductionStrategy implements DamageCalculationStrategy {

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

        int totalLevel = 0;
        totalLevel += mgr.getTalentLevel(player.getUniqueId(), Skill.TERRAFORMING, Talent.NECROTIC_I);
        totalLevel += mgr.getTalentLevel(player.getUniqueId(), Skill.TERRAFORMING, Talent.NECROTIC_II);
        totalLevel += mgr.getTalentLevel(player.getUniqueId(), Skill.TERRAFORMING, Talent.NECROTIC_III);
        totalLevel += mgr.getTalentLevel(player.getUniqueId(), Skill.TERRAFORMING, Talent.NECROTIC_IV);
        if (totalLevel <= 0) {
            return DamageCalculationResult.noChange(context.getBaseDamage());
        }

        double reduction = totalLevel * 0.05;
        double multiplier = Math.max(0, 1.0 - reduction);
        double finalDamage = context.getBaseDamage() * multiplier;

        DamageCalculationResult.DamageModifier mod =
                DamageCalculationResult.DamageModifier.multiplicative(
                        "Necrotic", multiplier,
                        "-" + (int)(reduction * 100) + "% from Corpses"
                );

        return DamageCalculationResult.withModifier(context.getBaseDamage(), finalDamage, mod);
    }

    @Override
    public boolean isApplicable(DamageCalculationContext context) {
        Entity attacker = context.getAttacker();
        return context.getTarget() instanceof Player && attacker != null && attacker.hasMetadata("CORPSE");
    }

    @Override
    public int getPriority() {
        return 71;
    }

    @Override
    public String getName() {
        return "Necrotic Damage Reduction";
    }
}
