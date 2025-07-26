package goat.minecraft.minecraftnew.subsystems.combat.damage.strategies;

import goat.minecraft.minecraftnew.subsystems.combat.damage.DamageCalculationContext;
import goat.minecraft.minecraftnew.subsystems.combat.damage.DamageCalculationResult;
import goat.minecraft.minecraftnew.subsystems.combat.damage.DamageCalculationStrategy;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

/**
 * Simple damage multiplier for spiders to keep them competitive
 * with skeletons in raw damage output.
 */
public class SpiderDamageStrategy implements DamageCalculationStrategy {

    private static final double MULTIPLIER = 1.5; // +50% damage

    @Override
    public DamageCalculationResult calculateDamage(DamageCalculationContext context) {
        if (!isApplicable(context)) {
            return DamageCalculationResult.noChange(context.getBaseDamage());
        }
        double original = context.getBaseDamage();
        double finalDamage = original * MULTIPLIER;
        return DamageCalculationResult.withModifier(original, finalDamage,
                DamageCalculationResult.DamageModifier.multiplicative(
                        "Spider Damage Buff", MULTIPLIER, "+50% Damage"));
    }

    @Override
    public boolean isApplicable(DamageCalculationContext context) {
        Entity attacker = context.getAttacker();
        if (!(context.getTarget() instanceof Player)) return false;
        if (!(attacker instanceof LivingEntity)) return false;
        EntityType type = attacker.getType();
        return type == EntityType.SPIDER || type == EntityType.CAVE_SPIDER;
    }

    @Override
    public int getPriority() {
        return 65; // after monster level scaling
    }

    @Override
    public String getName() {
        return "Spider Damage Buff";
    }
}
