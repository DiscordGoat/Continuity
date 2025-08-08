package goat.minecraft.minecraftnew.utils.stats;

import goat.minecraft.minecraftnew.other.beacon.Catalyst;
import goat.minecraft.minecraftnew.other.beacon.CatalystManager;
import goat.minecraft.minecraftnew.other.beacon.CatalystType;
import goat.minecraft.minecraftnew.other.skilltree.Skill;
import goat.minecraft.minecraftnew.other.skilltree.SkillTreeManager;
import goat.minecraft.minecraftnew.other.skilltree.Talent;
import goat.minecraft.minecraftnew.subsystems.smithing.tierreforgelisteners.ReforgeManager;
import goat.minecraft.minecraftnew.subsystems.smithing.tierreforgelisteners.ReforgeManager.ReforgeTier;
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

        return strength;
    }
}

