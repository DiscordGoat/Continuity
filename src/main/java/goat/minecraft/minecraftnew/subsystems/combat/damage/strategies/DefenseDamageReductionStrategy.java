package goat.minecraft.minecraftnew.subsystems.combat.damage.strategies;

import goat.minecraft.minecraftnew.subsystems.combat.damage.DamageCalculationContext;
import goat.minecraft.minecraftnew.subsystems.combat.damage.DamageCalculationResult;
import goat.minecraft.minecraftnew.subsystems.combat.damage.DamageCalculationStrategy;
import goat.minecraft.minecraftnew.utils.stats.DefenseManager;
import goat.minecraft.minecraftnew.utils.stats.DefenseManager.DamageTag;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;

/**
 * Applies Defense-based damage reduction to players.
 */
public class DefenseDamageReductionStrategy implements DamageCalculationStrategy {

    @Override
    public DamageCalculationResult calculateDamage(DamageCalculationContext context) {
        if (!isApplicable(context)) {
            return DamageCalculationResult.noChange(context.getBaseDamage());
        }

        Player player = (Player) context.getTarget();
        DamageTag tag = mapTag(context.getEvent().getCause());
        double base = context.getBaseDamage();
        double finalDamage = DefenseManager.computeFinalDamage(base, player, tag);
        double multiplier = finalDamage / base;

        // Remove vanilla armor/protection reductions
        EntityDamageEvent event = context.getEvent();
        try {
            event.setDamage(EntityDamageEvent.DamageModifier.ARMOR, 0);
            event.setDamage(EntityDamageEvent.DamageModifier.MAGIC, 0);
        } catch (Throwable ignored) {
            // API may not support modifiers; ignore
        }

        DamageCalculationResult.DamageModifier mod =
                DamageCalculationResult.DamageModifier.multiplicative(
                        "Defense", multiplier,
                        "-" + String.format("%d%%", (int) Math.round((1 - multiplier) * 100))
                                + " from Defense " + DefenseManager.EMOJI
                );
        return DamageCalculationResult.withModifier(base, finalDamage, mod);
    }

    @Override
    public boolean isApplicable(DamageCalculationContext context) {
        return context.getTarget() instanceof Player;
    }

    @Override
    public int getPriority() {
        return 1; // run after other modifiers
    }

    @Override
    public String getName() {
        return "Defense Damage Reduction";
    }

    private DamageTag mapTag(EntityDamageEvent.DamageCause cause) {
        switch (cause) {
            case ENTITY_ATTACK:
            case ENTITY_SWEEP_ATTACK:
                return DamageTag.ENTITY_ATTACK;
            case PROJECTILE:
                return DamageTag.PROJECTILE;
            case FALL:
                return DamageTag.FALL;
            case ENTITY_EXPLOSION:
            case BLOCK_EXPLOSION:
                return DamageTag.BLAST;
            case FIRE_TICK:
                return DamageTag.FIRE_TICK;
            case HOT_FLOOR:
                return DamageTag.HOT_FLOOR;
            case LAVA:
                return DamageTag.LAVA;
            case FIRE:
                return DamageTag.FIRE;
            default:
                return DamageTag.GENERIC;
        }
    }
}
