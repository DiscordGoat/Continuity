package goat.minecraft.minecraftnew.subsystems.combat.damage.strategies;

import goat.minecraft.minecraftnew.other.skilltree.Skill;
import goat.minecraft.minecraftnew.other.skilltree.SkillTreeManager;
import goat.minecraft.minecraftnew.other.skilltree.Talent;
import goat.minecraft.minecraftnew.subsystems.combat.damage.DamageCalculationContext;
import goat.minecraft.minecraftnew.subsystems.combat.damage.DamageCalculationResult;
import goat.minecraft.minecraftnew.subsystems.combat.damage.DamageCalculationStrategy;
import goat.minecraft.minecraftnew.subsystems.forestry.SpiritType;
import org.bukkit.ChatColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Skeleton;

/**
 * Applies damage reduction for players with the Spectral Armor talent
 * when attacked by forest spirits. Detection of spirits is done by
 * parsing their custom names rather than relying on metadata.
 */
public class SpectralArmorDamageStrategy implements DamageCalculationStrategy {

    private static final double REDUCTION_PER_LEVEL = 0.10; // 10% per level

    @Override
    public DamageCalculationResult calculateDamage(DamageCalculationContext context) {
        if (!isApplicable(context)) {
            return DamageCalculationResult.noChange(context.getBaseDamage());
        }

        Player player = (Player) context.getTarget();
        Entity attacker = context.getAttacker();
        double original = context.getBaseDamage();

        SkillTreeManager mgr = SkillTreeManager.getInstance();
        if (mgr == null) {
            return DamageCalculationResult.noChange(original);
        }

        int level = mgr.getTalentLevel(player.getUniqueId(), Skill.FORESTRY, Talent.SPECTRAL_ARMOR);
        if (level <= 0) {
            return DamageCalculationResult.noChange(original);
        }

        if (!isForestSpirit(attacker)) {
            return DamageCalculationResult.noChange(original);
        }

        double reduction = Math.min(level * REDUCTION_PER_LEVEL, 0.90); // cap at 90%
        double finalDamage = original * (1.0 - reduction);

        DamageCalculationResult.DamageModifier mod =
                DamageCalculationResult.DamageModifier.multiplicative(
                        "Spectral Armor",
                        1.0 - reduction,
                        String.format("-%d%% Spirit Damage", (int) (reduction * 100))
                );

        return DamageCalculationResult.withModifier(original, finalDamage, mod);
    }

    @Override
    public boolean isApplicable(DamageCalculationContext context) {
        return context.getTarget() instanceof Player && context.getAttacker() != null;
    }

    @Override
    public int getPriority() {
        return 61; // Slightly lower than Insanity Catalyst strategy
    }

    @Override
    public String getName() {
        return "Spectral Armor Spirit Damage Reduction";
    }

    private boolean isForestSpirit(Entity entity) {
        if (!(entity instanceof Skeleton)) {
            return false;
        }

        String name = entity.getCustomName();
        if (name == null) {
            return false;
        }

        String cleaned = ChatColor.stripColor(name);
        if (cleaned.contains("] ")) {
            cleaned = cleaned.substring(cleaned.indexOf("] ") + 2);
        }

        for (SpiritType type : SpiritType.values()) {
            if (cleaned.equalsIgnoreCase(type.getDisplayName())) {
                return true;
            }
        }
        return false;
    }
}
