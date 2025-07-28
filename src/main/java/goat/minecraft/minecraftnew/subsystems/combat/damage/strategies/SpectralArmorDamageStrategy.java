package goat.minecraft.minecraftnew.subsystems.combat.damage.strategies;

import goat.minecraft.minecraftnew.other.skilltree.Skill;
import goat.minecraft.minecraftnew.other.skilltree.SkillTreeManager;
import goat.minecraft.minecraftnew.other.skilltree.Talent;
import goat.minecraft.minecraftnew.subsystems.combat.damage.DamageCalculationContext;
import goat.minecraft.minecraftnew.subsystems.combat.damage.DamageCalculationResult;
import goat.minecraft.minecraftnew.subsystems.combat.damage.DamageCalculationStrategy;
import org.bukkit.ChatColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Skeleton;

import java.util.logging.Logger;

/**
 * Reduces damage from forest spirits when the target has the Spectral Armor talent.
 * Forest spirits are detected by parsing their custom names rather than relying on metadata.
 */
public class SpectralArmorDamageStrategy implements DamageCalculationStrategy {

    private static final Logger logger = Logger.getLogger(SpectralArmorDamageStrategy.class.getName());

    @Override
    public DamageCalculationResult calculateDamage(DamageCalculationContext context) {
        if (!isApplicable(context)) {
            return DamageCalculationResult.noChange(context.getBaseDamage());
        }

        Player player = (Player) context.getTarget();
        double originalDamage = context.getBaseDamage();

        try {
            SkillTreeManager manager = SkillTreeManager.getInstance();
            if (manager == null) {
                return DamageCalculationResult.noChange(originalDamage);
            }
            int level = manager.getTalentLevel(player.getUniqueId(), Skill.FORESTRY, Talent.SPECTRAL_ARMOR);
            if (level <= 0) {
                return DamageCalculationResult.noChange(originalDamage);
            }

            double reduction = Math.min(level * 0.10, 0.95); // cap at 95% for safety
            double multiplier = 1.0 - reduction;
            double finalDamage = originalDamage * multiplier;

            DamageCalculationResult.DamageModifier modifier =
                DamageCalculationResult.DamageModifier.multiplicative(
                    "Spectral Armor",
                    multiplier,
                    String.format("-%d%% Spirit Damage", (int) (reduction * 100))
                );

            return DamageCalculationResult.withModifier(originalDamage, finalDamage, modifier);

        } catch (Exception e) {
            logger.warning(String.format("Failed to apply Spectral Armor reduction for %s: %s",
                    player.getName(), e.getMessage()));
            return DamageCalculationResult.noChange(originalDamage);
        }
    }

    private boolean isForestSpirit(Entity entity) {
        if (!(entity instanceof Skeleton)) {
            return false;
        }
        String name = entity.getCustomName();
        if (name == null) {
            return false;
        }
        name = ChatColor.stripColor(name);
        if (name.contains("] ")) {
            name = name.substring(name.indexOf("] ") + 2);
        }
        return name.toLowerCase().endsWith("spirit");
    }

    @Override
    public boolean isApplicable(DamageCalculationContext context) {
        return context.getTarget() instanceof Player && isForestSpirit(context.getAttacker());
    }

    @Override
    public int getPriority() {
        return 61; // run after Insanity Catalyst but before generic modifiers
    }

    @Override
    public String getName() {
        return "Spectral Armor Damage Reduction";
    }
}
