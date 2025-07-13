package goat.minecraft.minecraftnew.subsystems.combat.damage.strategies;

import goat.minecraft.minecraftnew.subsystems.combat.config.CombatConfiguration;
import goat.minecraft.minecraftnew.subsystems.combat.damage.DamageCalculationContext;
import goat.minecraft.minecraftnew.subsystems.combat.damage.DamageCalculationResult;
import goat.minecraft.minecraftnew.subsystems.combat.damage.DamageCalculationStrategy;
import goat.minecraft.minecraftnew.subsystems.combat.utils.EntityLevelExtractor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;

import java.util.logging.Logger;

/**
 * Damage scaling for Corpse NPCs based on their level.
 * Applies a per-level multiplier when a corpse damages a player.
 */
public class CorpseLevelDamageStrategy implements DamageCalculationStrategy {

    private static final Logger logger = Logger.getLogger(CorpseLevelDamageStrategy.class.getName());

    private final CombatConfiguration.DamageConfig config;
    private final EntityLevelExtractor levelExtractor;

    public CorpseLevelDamageStrategy(CombatConfiguration.DamageConfig config) {
        this.config = config;
        this.levelExtractor = new EntityLevelExtractor();
    }

    @Override
    public DamageCalculationResult calculateDamage(DamageCalculationContext context) {
        if (!isApplicable(context)) {
            return DamageCalculationResult.noChange(context.getBaseDamage());
        }

        LivingEntity corpse = getCorpseAttacker(context);
        if (corpse == null) {
            return DamageCalculationResult.noChange(context.getBaseDamage());
        }

        double originalDamage = context.getBaseDamage();
        try {
            int level = levelExtractor.extractLevelFromName(corpse);
            if (level <= 0) {
                return DamageCalculationResult.noChange(originalDamage);
            }

            double multiplier = 1.0 + (level * (config.getMonsterPerLevel()));
            double finalDamage = originalDamage * multiplier;

            DamageCalculationResult.DamageModifier modifier =
                DamageCalculationResult.DamageModifier.multiplicative(
                    "Corpse Level",
                    multiplier,
                    String.format("Level %d corpse", level)
                );

            return DamageCalculationResult.withModifier(originalDamage, finalDamage, modifier);

        } catch (Exception e) {
            logger.warning("Failed to calculate corpse level damage: " + e.getMessage());
            return DamageCalculationResult.noChange(originalDamage);
        }
    }

    @Override
    public boolean isApplicable(DamageCalculationContext context) {
        return context.getTarget() instanceof Player && getCorpseAttacker(context) != null;
    }

    @Override
    public int getPriority() {
        return 71; // Slightly higher than generic monster scaling
    }

    @Override
    public String getName() {
        return "Corpse Level Damage Scaling";
    }

    private LivingEntity getCorpseAttacker(DamageCalculationContext context) {
        Entity attacker = context.getAttacker();
        if (attacker.hasMetadata("CORPSE") && attacker instanceof LivingEntity le) {
            return le;
        }

        if (context.isProjectile() && context.getProjectile().isPresent()) {
            Projectile projectile = context.getProjectile().get();
            Object shooter = projectile.getShooter();
            if (shooter instanceof Entity entity && entity instanceof LivingEntity le && entity.hasMetadata("CORPSE")) {
                return le;
            }
        }

        return null;
    }
}
