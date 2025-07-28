package goat.minecraft.minecraftnew.other.health;

import goat.minecraft.minecraftnew.MinecraftNew;
import goat.minecraft.minecraftnew.other.additionalfunctionality.BlessingUtils;
import goat.minecraft.minecraftnew.other.beacon.BeaconPassivesGUI;
import goat.minecraft.minecraftnew.subsystems.pets.PetManager;
import goat.minecraft.minecraftnew.subsystems.pets.PetTrait;
import goat.minecraft.minecraftnew.other.skilltree.SkillTreeManager;
import goat.minecraft.minecraftnew.other.skilltree.Skill;
import goat.minecraft.minecraftnew.other.skilltree.Talent;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class HealthManager {

    private static HealthManager instance;
    private final JavaPlugin plugin;

    private HealthManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public static synchronized HealthManager getInstance(JavaPlugin plugin) {
        if (instance == null) {
            instance = new HealthManager(plugin);
        }
        return instance;
    }

    public static HealthManager getInstance() {
        return instance;
    }

    /**
     * Compute the maximum health for a player based on all bonuses.
     */
    public double computeMaxHealth(Player player) {
        double health = 20.0;

        int talentLevel = 0;
        if (SkillTreeManager.getInstance() != null) {
            talentLevel = SkillTreeManager.getInstance()
                    .getTalentLevel(player.getUniqueId(), Skill.PLAYER, Talent.VITALITY);
        }
        health += talentLevel;

        if (BeaconPassivesGUI.hasBeaconPassives(player) &&
                BeaconPassivesGUI.hasPassiveEnabled(player, "mending")) {
            health += 20.0;
        }

        if (BlessingUtils.hasFullSetBonus(player, "Monolith")) {
            health += 20.0;
        }

        PetManager.Pet active = PetManager.getInstance(plugin).getActivePet(player);
        if (active != null && active.getTrait() == PetTrait.HEALTHY) {
            double percent = active.getTrait().getValueForRarity(active.getTraitRarity());
            double bonus = Math.floor((health * percent / 100.0) / 2) * 2; // round down to full hearts
            health += bonus;
        }

        return health;
    }

    /**
     * Apply computed max health to the player, keeping current health within bounds.
     */
    public void updateHealth(Player player) {
        double max = computeMaxHealth(player);
        AttributeInstance attr = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);
        if (attr != null) {
            attr.setBaseValue(max);
            if (player.getHealth() > max) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 100, 255, true));
            }
        }
    }

    /**
     * Apply computed max health and fully heal the player.
     */
    public void applyAndFill(Player player) {
        double max = computeMaxHealth(player);
        AttributeInstance attr = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);
        player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 100, 255, true));
        if (attr != null) {
            attr.setBaseValue(max);
            player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 100, 255, true));
        }
    }

    /**
     * Recalculate and fill health for all online players on startup.
     */
    public void startup() {
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            applyAndFill(player);
        }
    }

    /**
     * Reset all player health to the default 20 before shutdown to avoid stacking.
     */
    public void shutdown() {
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            AttributeInstance attr = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);
            if (attr != null) {
                attr.setBaseValue(20.0);
                if (player.getHealth() > 20.0) {
                    player.setHealth(20.0);
                }
            }
        }
    }
}
