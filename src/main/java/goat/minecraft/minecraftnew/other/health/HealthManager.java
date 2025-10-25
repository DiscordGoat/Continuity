package goat.minecraft.minecraftnew.other.health;

import goat.minecraft.minecraftnew.MinecraftNew;
import goat.minecraft.minecraftnew.other.additionalfunctionality.BlessingUtils;
import goat.minecraft.minecraftnew.other.beacon.BeaconPassivesGUI;
import goat.minecraft.minecraftnew.other.skilltree.Skill;
import goat.minecraft.minecraftnew.other.skilltree.SkillTreeManager;
import goat.minecraft.minecraftnew.other.skilltree.Talent;
import goat.minecraft.minecraftnew.subsystems.pets.PetManager;
import goat.minecraft.minecraftnew.subsystems.pets.PetTrait;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 * Utility class for calculating and applying player health values.
 */
public final class HealthManager {

    /** Chat color used when displaying Health. */
    public static final ChatColor COLOR = ChatColor.RED;

    /** Emoji used alongside the Health name. */
    public static final String EMOJI = "❤";

    /** Preformatted display name for Health with color and emoji. */
    public static final String DISPLAY_NAME = COLOR + "Health " + EMOJI;

    private HealthManager() {
        // Utility class
    }

    /**
     * Computes the maximum health for a player from all current sources.
     *
     * @param player player to calculate Health for
     * @return total max health value
     */
    public static double getMaxHealth(Player player) {
        double health = 20.0;

        int talentLevel = 0;
        SkillTreeManager stm = SkillTreeManager.getInstance();
        if (stm != null) {
            talentLevel += stm.getTalentLevel(player.getUniqueId(), Skill.PLAYER, Talent.HEALTH_I);
            talentLevel += stm.getTalentLevel(player.getUniqueId(), Skill.PLAYER, Talent.HEALTH_II);
            talentLevel += stm.getTalentLevel(player.getUniqueId(), Skill.PLAYER, Talent.HEALTH_III);
            talentLevel += stm.getTalentLevel(player.getUniqueId(), Skill.PLAYER, Talent.HEALTH_IV);
            talentLevel += stm.getTalentLevel(player.getUniqueId(), Skill.PLAYER, Talent.HEALTH_V);
        }
        health += talentLevel;

        double beaconBonus = 0.0;
        if (BeaconPassivesGUI.hasBeaconPassives(player)
                && BeaconPassivesGUI.hasPassiveEnabled(player, "mending")) {
            beaconBonus = 20.0;
            health += beaconBonus;
        }

        double monolithBonus = 0.0;
        if (BlessingUtils.hasFullSetBonus(player, "Monolith")) {
            monolithBonus = 20.0;
            health += monolithBonus;
        }

        double petBonus = 0.0;
        PetManager pm = PetManager.getInstance(MinecraftNew.getInstance());
        if (pm != null) {
            PetManager.Pet active = pm.getActivePet(player);
            if (active != null && active.getTrait() == PetTrait.HEALTHY) {
                double percent = active.getTrait().getValueForRarity(active.getTraitRarity());
                if (stm != null) {
                    int q = stm.getTalentLevel(player.getUniqueId(), Skill.TAMING, Talent.QUIRKY);
                    percent *= (1 + q * 0.20);
                }
                petBonus = Math.floor((health * percent / 100.0) / 2) * 2; // round down to full hearts
                health += petBonus;
            }
        }

        return health;
    }

    /**
     * Sends a detailed breakdown of the player's Health calculation.
     *
     * @param player the player to report Health for
     */
    public static void sendHealthBreakdown(Player player) {
        player.sendMessage(COLOR + "Health Breakdown:");

        double base = 20.0;
        player.sendMessage(COLOR + "Base: " + ChatColor.YELLOW + base);

        double total = base;

        double talentBonus = 0.0;
        SkillTreeManager stm = SkillTreeManager.getInstance();
        if (stm != null) {
            talentBonus += stm.getTalentLevel(player.getUniqueId(), Skill.PLAYER, Talent.HEALTH_I);
            talentBonus += stm.getTalentLevel(player.getUniqueId(), Skill.PLAYER, Talent.HEALTH_II);
            talentBonus += stm.getTalentLevel(player.getUniqueId(), Skill.PLAYER, Talent.HEALTH_III);
            talentBonus += stm.getTalentLevel(player.getUniqueId(), Skill.PLAYER, Talent.HEALTH_IV);
            talentBonus += stm.getTalentLevel(player.getUniqueId(), Skill.PLAYER, Talent.HEALTH_V);
        }
        total += talentBonus;
        player.sendMessage(COLOR + "Health Talents: " + ChatColor.YELLOW + talentBonus);

        double beaconBonus = 0.0;
        if (BeaconPassivesGUI.hasBeaconPassives(player)
                && BeaconPassivesGUI.hasPassiveEnabled(player, "mending")) {
            beaconBonus = 20.0;
        }
        total += beaconBonus;
        player.sendMessage(COLOR + "Beacon Mending Passive: " + ChatColor.YELLOW + beaconBonus);

        double monolithBonus = 0.0;
        if (BlessingUtils.hasFullSetBonus(player, "Monolith")) {
            monolithBonus = 20.0;
        }
        total += monolithBonus;
        player.sendMessage(COLOR + "Monolith Set Bonus: " + ChatColor.YELLOW + monolithBonus);

        double petBonus = 0.0;
        PetManager pm = PetManager.getInstance(MinecraftNew.getInstance());
        if (pm != null) {
            PetManager.Pet active = pm.getActivePet(player);
            if (active != null && active.getTrait() == PetTrait.HEALTHY) {
                double percent = active.getTrait().getValueForRarity(active.getTraitRarity());
                if (stm != null) {
                    int q = stm.getTalentLevel(player.getUniqueId(), Skill.TAMING, Talent.QUIRKY);
                    percent *= (1 + q * 0.20);
                }
                petBonus = Math.floor((total * percent / 100.0) / 2) * 2; // round down to full hearts
            }
        }
        total += petBonus;
        player.sendMessage(COLOR + "Healthy Pet Trait: " + ChatColor.YELLOW + petBonus);

        player.sendMessage(COLOR + "Total Health: " + ChatColor.YELLOW + total);
    }

    /**
     * Apply computed max health to the player, keeping current health within bounds.
     */
    public static void updateHealth(Player player) {
        double max = getMaxHealth(player);
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
    public static void applyAndFill(Player player) {
        double max = getMaxHealth(player);
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
    public static void startup() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            applyAndFill(player);
        }
    }

    /**
     * Reset all player health to the default 20 before shutdown to avoid stacking.
     */
    public static void shutdown() {
        for (Player player : Bukkit.getOnlinePlayers()) {
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

