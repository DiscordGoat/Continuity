package goat.minecraft.minecraftnew.subsystems.combat.damage.strategies;

import goat.minecraft.minecraftnew.other.skilltree.Skill;
import goat.minecraft.minecraftnew.other.skilltree.SkillTreeManager;
import goat.minecraft.minecraftnew.other.skilltree.Talent;
import goat.minecraft.minecraftnew.subsystems.combat.damage.DamageCalculationContext;
import goat.minecraft.minecraftnew.subsystems.combat.damage.DamageCalculationResult;
import goat.minecraft.minecraftnew.subsystems.combat.damage.DamageCalculationStrategy;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class SwordTalentDamageStrategy implements DamageCalculationStrategy {

    // all of the sword‐type talents on the Combat tree
    private static final List<Talent> SWORD_TALENTS = List.of(
            Talent.SWORD_DAMAGE_I,
            Talent.SWORD_DAMAGE_II,
            Talent.SWORD_DAMAGE_III,
            Talent.SWORD_DAMAGE_IV,
            Talent.SWORD_DAMAGE_V
    );

    @Override
    public DamageCalculationResult calculateDamage(DamageCalculationContext context) {
        if (!isApplicable(context)) {
            return DamageCalculationResult.noChange(context.getBaseDamage());
        }

        Player player   = context.getAttackerPlayer().get();
        ItemStack weapon= context.getWeapon().get();
        Material type   = weapon.getType();

        // only apply to any *_SWORD
        if (!type.name().endsWith("_SWORD")) {
            return DamageCalculationResult.noChange(context.getBaseDamage());
        }

        SkillTreeManager mgr = SkillTreeManager.getInstance();
        if (mgr == null) {
            return DamageCalculationResult.noChange(context.getBaseDamage());
        }

        double totalBonus = 0.0;
        // sum up 4% per level across all sword damage talents
        for (Talent t : SWORD_TALENTS) {
            int lvl = mgr.getTalentLevel(player.getUniqueId(), Skill.COMBAT, t);
            totalBonus += lvl * 0.04;
        }

        if (totalBonus <= 0) {
            return DamageCalculationResult.noChange(context.getBaseDamage());
        }

        double base   = context.getBaseDamage();
        double target = base * (1.0 + totalBonus);

        // one aggregated modifier
        DamageCalculationResult.DamageModifier mod =
                DamageCalculationResult.DamageModifier.multiplicative(
                        "Sword Talents",
                        1.0 + totalBonus,
                        "+" + (int)(totalBonus * 100) + "% Sword Damage"
                );

        return DamageCalculationResult.withModifier(base, target, mod);
    }

    @Override
    public boolean isApplicable(DamageCalculationContext context) {
        return context.getAttackerPlayer().isPresent()
                && context.getWeapon().isPresent()
                && context.getDamageType() == DamageCalculationContext.DamageType.MELEE;
    }

    @Override
    public int getPriority() {
        return 75;
    }

    @Override
    public String getName() {
        return "Sword Talent Damage";
    }
}
