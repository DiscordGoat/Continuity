package goat.minecraft.minecraftnew.subsystems.combat.damage.strategies;

import goat.minecraft.minecraftnew.other.skilltree.Skill;
import goat.minecraft.minecraftnew.other.skilltree.SkillTreeManager;
import goat.minecraft.minecraftnew.other.skilltree.Talent;
import goat.minecraft.minecraftnew.subsystems.combat.damage.DamageCalculationContext;
import goat.minecraft.minecraftnew.subsystems.combat.damage.DamageCalculationResult;
import goat.minecraft.minecraftnew.subsystems.combat.damage.DamageCalculationStrategy;
import goat.minecraft.minecraftnew.subsystems.gravedigging.corpses.Corpse;
import goat.minecraft.minecraftnew.subsystems.gravedigging.corpses.CorpseRegistry;
import goat.minecraft.minecraftnew.subsystems.fishing.Rarity;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.metadata.MetadataValue;

import java.util.List;
import java.util.Optional;

/**
 * Grants bonus damage against legendary Corpse mobs when the Murder Mystery talent is learned.
 */
public class MurderMysteryDamageStrategy implements DamageCalculationStrategy {

    @Override
    public DamageCalculationResult calculateDamage(DamageCalculationContext context) {
        if (!isApplicable(context)) {
            return DamageCalculationResult.noChange(context.getBaseDamage());
        }

        Player attacker = context.getAttackerPlayer().get();
        SkillTreeManager mgr = SkillTreeManager.getInstance();
        if (mgr == null) {
            return DamageCalculationResult.noChange(context.getBaseDamage());
        }

        int level = mgr.getTalentLevel(attacker.getUniqueId(), Skill.TERRAFORMING, Talent.MURDER_MYSTERY);
        if (level <= 0) {
            return DamageCalculationResult.noChange(context.getBaseDamage());
        }

        double multiplier = 1.0 + (level * 0.50);
        double finalDamage = context.getBaseDamage() * multiplier;

        DamageCalculationResult.DamageModifier mod =
                DamageCalculationResult.DamageModifier.multiplicative(
                        "Murder Mystery", multiplier,
                        "+" + (int)(level * 50) + "% vs Mass Murderers"
                );

        return DamageCalculationResult.withModifier(context.getBaseDamage(), finalDamage, mod);
    }

    @Override
    public boolean isApplicable(DamageCalculationContext context) {
        if (context.getAttackerPlayer().isEmpty()) return false;
        Entity target = context.getTarget();
        if (target == null || !target.hasMetadata("CORPSE")) return false;
        List<MetadataValue> meta = target.getMetadata("CORPSE");
        if (meta.isEmpty()) return false;
        String name = meta.get(0).asString();
        Optional<Corpse> corpse = CorpseRegistry.getCorpseByName(name);
        return corpse.isPresent() && corpse.get().getRarity() == Rarity.LEGENDARY;
    }

    @Override
    public int getPriority() {
        return 72;
    }

    @Override
    public String getName() {
        return "Murder Mystery";
    }
}
