package goat.minecraft.minecraftnew.utils.stats;

import goat.minecraft.minecraftnew.other.skilltree.Skill;
import goat.minecraft.minecraftnew.other.skilltree.SkillTreeManager;
import goat.minecraft.minecraftnew.other.skilltree.Talent;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

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

        return strength;
    }
}

