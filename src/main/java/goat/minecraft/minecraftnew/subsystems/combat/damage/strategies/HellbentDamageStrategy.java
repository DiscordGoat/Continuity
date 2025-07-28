package goat.minecraft.minecraftnew.subsystems.combat.damage.strategies;

import goat.minecraft.minecraftnew.other.skilltree.Skill;
import goat.minecraft.minecraftnew.other.skilltree.SkillTreeManager;
import goat.minecraft.minecraftnew.other.skilltree.Talent;
import goat.minecraft.minecraftnew.subsystems.combat.damage.DamageCalculationContext;
import goat.minecraft.minecraftnew.subsystems.combat.damage.DamageCalculationResult;
import goat.minecraft.minecraftnew.subsystems.combat.damage.DamageCalculationStrategy;
import org.bukkit.entity.Player;

/**
 * Damage bonus for players with the Hellbent talent when at low health.
 * Grants +25% damage while the player's health percentage is below
 * (10 * level)% of their maximum health.
 */
public class HellbentDamageStrategy implements DamageCalculationStrategy {

    private static final double BONUS_MULTIPLIER = 1.25; // 25% damage increase

    @Override
    public DamageCalculationResult calculateDamage(DamageCalculationContext context) {
        if (!isApplicable(context)) {
            return DamageCalculationResult.noChange(context.getBaseDamage());
        }

        Player player = context.getAttackerPlayer().get();
        SkillTreeManager mgr = SkillTreeManager.getInstance();
        if (mgr == null) {
            return DamageCalculationResult.noChange(context.getBaseDamage());
        }

        int level = mgr.getTalentLevel(player.getUniqueId(), Skill.COMBAT, Talent.HELLBENT);
        if (level <= 0) {
            return DamageCalculationResult.noChange(context.getBaseDamage());
        }

        double thresholdPct = level * 10.0;
        double currentPct = (player.getHealth() / player.getMaxHealth()) * 100.0;

        if (currentPct >= thresholdPct) {
            return DamageCalculationResult.noChange(context.getBaseDamage());
        }

        double base = context.getBaseDamage();
        double finalDamage = base * BONUS_MULTIPLIER;

        DamageCalculationResult.DamageModifier mod =
                DamageCalculationResult.DamageModifier.multiplicative(
                        "Hellbent", BONUS_MULTIPLIER,
                        "+25% Damage below " + (int) thresholdPct + "% HP");

        return DamageCalculationResult.withModifier(base, finalDamage, mod);
    }

    @Override
    public boolean isApplicable(DamageCalculationContext context) {
        return context.getAttackerPlayer().isPresent();
    }

    @Override
    public int getPriority() {
        // After weapon/skill calculations, before catalysts
        return 74;
    }

    @Override
    public String getName() {
        return "Hellbent Damage Bonus";
    }
}
