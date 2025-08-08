package goat.minecraft.minecraftnew.subsystems.combat.damage.strategies;

import goat.minecraft.minecraftnew.subsystems.combat.damage.DamageCalculationContext;
import goat.minecraft.minecraftnew.subsystems.combat.damage.DamageCalculationResult;
import goat.minecraft.minecraftnew.subsystems.combat.damage.DamageCalculationStrategy;
import goat.minecraft.minecraftnew.utils.stats.StrengthManager;
import org.bukkit.entity.Player;

/**
 * Applies the player's Strength stat as a damage multiplier.
 * Each point of Strength grants +1% damage.
 */
public class StrengthDamageStrategy implements DamageCalculationStrategy {

    @Override
    public DamageCalculationResult calculateDamage(DamageCalculationContext context) {
        if (!isApplicable(context)) {
            return DamageCalculationResult.noChange(context.getBaseDamage());
        }

        Player player = context.getAttackerPlayer().get();
        int strength = StrengthManager.getStrength(player);
        if (strength <= 0) {
            return DamageCalculationResult.noChange(context.getBaseDamage());
        }

        double base = context.getBaseDamage();
        double multiplier = 1.0 + strength / 100.0;
        double target = base * multiplier;

        DamageCalculationResult.DamageModifier mod =
                DamageCalculationResult.DamageModifier.multiplicative(
                        "Strength",
                        multiplier,
                        "+" + strength + " Strength " + StrengthManager.EMOJI
                );

        return DamageCalculationResult.withModifier(base, target, mod);
    }

    @Override
    public boolean isApplicable(DamageCalculationContext context) {
        return context.getAttackerPlayer().isPresent();
    }

    @Override
    public int getPriority() {
        return 75;
    }


    @Override
    public String getName() {
        return "Strength Damage";
    }
}

