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

public class SwordTalentDamageStrategy implements DamageCalculationStrategy {

    @Override
    public DamageCalculationResult calculateDamage(DamageCalculationContext context) {
        if (!isApplicable(context)) {
            return DamageCalculationResult.noChange(context.getBaseDamage());
        }

        Player player = context.getAttackerPlayer().get();
        ItemStack weapon = context.getWeapon().get();
        Material type = weapon.getType();

        SkillTreeManager manager = SkillTreeManager.getInstance();
        if (manager == null) {
            return DamageCalculationResult.noChange(context.getBaseDamage());
        }

        Talent talent;
        switch (type) {
            case WOODEN_SWORD -> talent = Talent.WOODEN_SWORD;
            case STONE_SWORD -> talent = Talent.STONE_SWORD;
            case IRON_SWORD -> talent = Talent.IRON_SWORD;
            case GOLDEN_SWORD -> talent = Talent.GOLD_SWORD;
            case DIAMOND_SWORD -> talent = Talent.DIAMOND_SWORD;
            case NETHERITE_SWORD -> talent = Talent.NETHERITE_SWORD;
            default -> {
                return DamageCalculationResult.noChange(context.getBaseDamage());
            }
        }

        int level = manager.getTalentLevel(player.getUniqueId(), Skill.COMBAT, talent);
        if (level <= 0) {
            return DamageCalculationResult.noChange(context.getBaseDamage());
        }

        double multiplier = 1.0 + (level * 0.08);
        double finalDamage = context.getBaseDamage() * multiplier;

        DamageCalculationResult.DamageModifier modifier =
                DamageCalculationResult.DamageModifier.multiplicative(
                        talent.getName(),
                        multiplier,
                        "+" + (level * 8) + "% Damage"
                );

        return DamageCalculationResult.withModifier(context.getBaseDamage(), finalDamage, modifier);
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
