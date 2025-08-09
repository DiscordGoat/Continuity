package goat.minecraft.minecraftnew.utils.stats;

import goat.minecraft.minecraftnew.other.beacon.BeaconPassivesGUI;
import goat.minecraft.minecraftnew.other.beacon.Catalyst;
import goat.minecraft.minecraftnew.other.beacon.CatalystManager;
import goat.minecraft.minecraftnew.other.beacon.CatalystType;
import goat.minecraft.minecraftnew.MinecraftNew;
import goat.minecraft.minecraftnew.subsystems.pets.PetManager;
import goat.minecraft.minecraftnew.other.skilltree.Skill;
import goat.minecraft.minecraftnew.other.skilltree.SkillTreeManager;
import goat.minecraft.minecraftnew.other.skilltree.Talent;
import goat.minecraft.minecraftnew.subsystems.brewing.PotionEffectPreferences;
import goat.minecraft.minecraftnew.subsystems.brewing.PotionManager;
import goat.minecraft.minecraftnew.subsystems.smithing.tierreforgelisteners.ReforgeManager;
import goat.minecraft.minecraftnew.subsystems.smithing.tierreforgelisteners.ReforgeManager.ReforgeTier;
import goat.minecraft.minecraftnew.utils.devtools.TalismanManager;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * Utility class for calculating a player's Strength stat.
 * <p>
 * Strength provides a direct damage bonus where each point of Strength
 * equates to +1% damage.
 */
public final class StrengthManager {

    /** Chat color used when displaying Strength. */
    public static final ChatColor COLOR = ChatColor.RED;

    /** Emoji used alongside the Strength name. */
    public static final String EMOJI = "⚔"; // flexed biceps

    /** Preformatted display name for Strength with color and emoji. */
    public static final String DISPLAY_NAME = COLOR + "Strength " + EMOJI;

    private StrengthManager() {
        // Utility class
    }

    /**
     * Calculates the total Strength for a player from all current sources.
     *
     * @param player the player to calculate Strength for
     * @return total Strength value
     */
    public static int getStrength(Player player) {
        int strength = 0;

        SkillTreeManager mgr = SkillTreeManager.getInstance();
        if (mgr != null) {
            // Sword damage talents each grant 4 Strength per level
            strength += mgr.getTalentLevel(player.getUniqueId(), Skill.COMBAT, Talent.SWORD_DAMAGE_I) * 4;
            strength += mgr.getTalentLevel(player.getUniqueId(), Skill.COMBAT, Talent.SWORD_DAMAGE_II) * 4;
            strength += mgr.getTalentLevel(player.getUniqueId(), Skill.COMBAT, Talent.SWORD_DAMAGE_III) * 4;
            strength += mgr.getTalentLevel(player.getUniqueId(), Skill.COMBAT, Talent.SWORD_DAMAGE_IV) * 4;
            strength += mgr.getTalentLevel(player.getUniqueId(), Skill.COMBAT, Talent.SWORD_DAMAGE_V) * 4;
        }

        // Sword reforges grant Strength based on their tier
        ItemStack weapon = player.getInventory().getItemInMainHand();
        ReforgeManager rm = new ReforgeManager();
        if (rm.isSword(weapon)) {
            ReforgeTier tier = rm.getReforgeTierByTier(rm.getReforgeTier(weapon));
            strength += tier.getWeaponDamageIncrease();

            // Legacy Damage talismans grant additional Strength
            strength += TalismanManager.getDamageStrength(weapon);
        }

        // Catalyst of Power grants Strength when nearby
        CatalystManager cm = CatalystManager.getInstance();
        if (cm != null && cm.isNearCatalyst(player.getLocation(), CatalystType.POWER)) {
            Catalyst catalyst = cm.findNearestCatalyst(player.getLocation(), CatalystType.POWER);
            if (catalyst != null) {
                int tier = cm.getCatalystTier(catalyst);
                strength += 25 + (tier * 5);
            }
        }

        // Beacon Power passive grants flat Strength
        if (BeaconPassivesGUI.hasBeaconPassives(player)
                && BeaconPassivesGUI.hasPassiveEnabled(player, "power")) {
            strength += 15;
        }

        // Pet perks that grant bonus Strength
        PetManager pm = PetManager.getInstance(MinecraftNew.getInstance());
        if (pm != null) {
            PetManager.Pet pet = pm.getActivePet(player);
            if (pet != null) {
                if (pet.hasPerk(PetManager.PetPerk.ELITE)) {
                    // 0.5 Strength per pet level, capped at +25
                    int petStrength = Math.min((int) (pet.getLevel() * 0.5), 25);
                    strength += petStrength;

                    // Elite talent adds +10 Strength per level
                    SkillTreeManager stm = SkillTreeManager.getInstance();
                    if (stm != null) {
                        int lvl = stm.getTalentLevel(player.getUniqueId(), Skill.TAMING, Talent.ELITE);
                        strength += lvl * 10;
                    }
                }
                if (pet.hasPerk(PetManager.PetPerk.CLAW)) {
                    // 0.5 Strength per pet level, capped at +10
                    int petStrength = Math.min((int) (pet.getLevel() * 0.5), 10);
                    strength += petStrength;
                }
            }
        }
      // Potion of Strength grants a flat Strength bonus while active
        if (PotionManager.isActive("Potion of Strength", player)
                && PotionEffectPreferences.isEnabled(player, "Potion of Strength")) {
            strength += 25;
        }
        return strength;
    }

    /**
     * Sends a detailed breakdown of the player's Strength, listing each
     * contributing component and the total value.
     *
     * @param player the player to report Strength for
     */
    public static void sendStrengthBreakdown(Player player) {
        player.sendMessage(COLOR + "Strength Breakdown:");

        int total = 0;

        // Strength from sword damage talents
        int talentStrength = 0;
        SkillTreeManager mgr = SkillTreeManager.getInstance();
        if (mgr != null) {
            talentStrength += mgr.getTalentLevel(player.getUniqueId(), Skill.COMBAT, Talent.SWORD_DAMAGE_I) * 4;
            talentStrength += mgr.getTalentLevel(player.getUniqueId(), Skill.COMBAT, Talent.SWORD_DAMAGE_II) * 4;
            talentStrength += mgr.getTalentLevel(player.getUniqueId(), Skill.COMBAT, Talent.SWORD_DAMAGE_III) * 4;
            talentStrength += mgr.getTalentLevel(player.getUniqueId(), Skill.COMBAT, Talent.SWORD_DAMAGE_IV) * 4;
            talentStrength += mgr.getTalentLevel(player.getUniqueId(), Skill.COMBAT, Talent.SWORD_DAMAGE_V) * 4;
        }
        total += talentStrength;
        player.sendMessage(COLOR + "Sword Damage Talents: " + ChatColor.YELLOW + talentStrength);

        // Strength from sword reforges and damage talismans
        ItemStack weapon = player.getInventory().getItemInMainHand();
        ReforgeManager rm = new ReforgeManager();
        int reforgeStrength = 0;
        int talismanStrength = 0;
        if (rm.isSword(weapon)) {
            ReforgeTier tier = rm.getReforgeTierByTier(rm.getReforgeTier(weapon));
            reforgeStrength += tier.getWeaponDamageIncrease();
            talismanStrength += TalismanManager.getDamageStrength(weapon);
        }
        total += reforgeStrength + talismanStrength;
        player.sendMessage(COLOR + "Sword Reforge: " + ChatColor.YELLOW + reforgeStrength);
        player.sendMessage(COLOR + "Damage Talismans: " + ChatColor.YELLOW + talismanStrength);

        // Strength from a nearby Catalyst of Power
        int catalystStrength = 0;
        CatalystManager cm = CatalystManager.getInstance();
        if (cm != null && cm.isNearCatalyst(player.getLocation(), CatalystType.POWER)) {
            Catalyst catalyst = cm.findNearestCatalyst(player.getLocation(), CatalystType.POWER);
            if (catalyst != null) {
                int tier = cm.getCatalystTier(catalyst);
                catalystStrength = 25 + (tier * 5);
            }
        }
        total += catalystStrength;
        player.sendMessage(COLOR + "Catalyst of Power: " + ChatColor.YELLOW + catalystStrength);

        // Strength from Beacon Power passive
        int powerPassiveStrength = 0;
        if (BeaconPassivesGUI.hasBeaconPassives(player)
                && BeaconPassivesGUI.hasPassiveEnabled(player, "power")) {
            powerPassiveStrength = 15;
        }
        total += powerPassiveStrength;
        player.sendMessage(COLOR + "Beacon Power Passive: " + ChatColor.YELLOW + powerPassiveStrength);

        // Strength from pet perks
        int petEliteStrength = 0;
        int eliteTalentStrength = 0;
        int petClawStrength = 0;
        PetManager pm = PetManager.getInstance(MinecraftNew.getInstance());
        if (pm != null) {
            PetManager.Pet pet = pm.getActivePet(player);
            if (pet != null) {
                if (pet.hasPerk(PetManager.PetPerk.ELITE)) {
                    petEliteStrength = Math.min((int) (pet.getLevel() * 0.5), 25);
                    SkillTreeManager stm = SkillTreeManager.getInstance();
                    if (stm != null) {
                        int lvl = stm.getTalentLevel(player.getUniqueId(), Skill.TAMING, Talent.ELITE);
                        eliteTalentStrength = lvl * 10;
                    }
                }
                if (pet.hasPerk(PetManager.PetPerk.CLAW)) {
                    petClawStrength = Math.min((int) (pet.getLevel() * 0.5), 10);
                }
            }
        }
        total += petEliteStrength + eliteTalentStrength + petClawStrength;
        player.sendMessage(COLOR + "Pet Elite Perk: " + ChatColor.YELLOW + petEliteStrength);
        player.sendMessage(COLOR + "Elite Talent Bonus: " + ChatColor.YELLOW + eliteTalentStrength);
        player.sendMessage(COLOR + "Pet Claw Perk: " + ChatColor.YELLOW + petClawStrength);

        // Strength from active potion effects
        int potionStrength = (PotionManager.isActive("Potion of Strength", player)
                && PotionEffectPreferences.isEnabled(player, "Potion of Strength")) ? 25 : 0;
        total += potionStrength;
        player.sendMessage(COLOR + "Potion of Strength: " + ChatColor.YELLOW + potionStrength);

        player.sendMessage(COLOR + "Total Strength: " + ChatColor.YELLOW + total);
    }
}

