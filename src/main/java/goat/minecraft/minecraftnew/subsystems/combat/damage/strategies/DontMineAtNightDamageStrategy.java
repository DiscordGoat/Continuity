package goat.minecraft.minecraftnew.subsystems.combat.damage.strategies;

import goat.minecraft.minecraftnew.other.skilltree.Skill;
import goat.minecraft.minecraftnew.other.skilltree.SkillTreeManager;
import goat.minecraft.minecraftnew.other.skilltree.Talent;
import goat.minecraft.minecraftnew.subsystems.combat.damage.DamageCalculationContext;
import goat.minecraft.minecraftnew.subsystems.combat.damage.DamageCalculationResult;
import goat.minecraft.minecraftnew.subsystems.combat.damage.DamageCalculationStrategy;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

/**
 * Increases player damage against Creepers based on the
 * "Don't Mine at Night" combat talent.
 * Each talent level grants +10% damage to Creepers.
 */
public class DontMineAtNightDamageStrategy implements DamageCalculationStrategy {

    private static final double PER_LEVEL_BONUS = 0.10;

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

        int level = mgr.getTalentLevel(player.getUniqueId(), Skill.COMBAT, Talent.DONT_MINE_AT_NIGHT);
        if (level <= 0) {
            return DamageCalculationResult.noChange(context.getBaseDamage());
        }

        double bonus = level * PER_LEVEL_BONUS;
        double multiplier = 1.0 + bonus;
        double finalDamage = context.getBaseDamage() * multiplier;

        DamageCalculationResult.DamageModifier mod =
                DamageCalculationResult.DamageModifier.multiplicative(
                        "Don't Mine at Night", multiplier,
                        "+" + (int)(bonus * 100) + "% Creeper Damage"
                );

        return DamageCalculationResult.withModifier(context.getBaseDamage(), finalDamage, mod);
    }

    @Override
    public boolean isApplicable(DamageCalculationContext context) {
        if (context.getAttackerPlayer().isEmpty()) return false;
        Entity target = context.getTarget();
        return target instanceof Creeper;
    }

    @Override
    public int getPriority() {
        return 74; // around other combat talent bonuses
    }

    @Override
    public String getName() {
        return "Don't Mine at Night";
    }
}
