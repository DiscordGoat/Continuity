package goat.minecraft.minecraftnew.subsystems.dragons;

import org.bukkit.ChatColor;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.entity.EnderDragon;

/**
 * Represents a custom dragon type used in Continuity's dragon fights.
 *
 * <p>Abilities and advanced behaviour are intentionally left out for now.
 * Implementations only need to expose basic attributes such as name, boss bar
 * colour and base statistics. These values can later be used by the fight
 * manager to drive more complex behaviour.</p>
 */
public interface Dragon {

    /**
     * @return the display colour for the dragon's name.
     */
    ChatColor getNameColor();

    /**
     * @return the raw (uncoloured) display name of the dragon.
     */
    String getName();

    /**
     * Convenience method to build a coloured name suitable for the boss bar
     * and entity custom name.
     */
    default String getDisplayName() {
        return getNameColor() + getName();
    }

    /**
     * @return the boss bar colour used when this dragon is active.
     */
    BarColor getBarColor();

    /**
     * @return the boss bar style. For segmented bars use the SEGMENTED styles.
     */
    BarStyle getBarStyle();

    int getCrystalBias();

    int getFlightSpeed();

    int getBaseRage();

    /**
     * @return the maximum amount of health this dragon should have.
     */
    int getMaxHealth();

    /**
     * How often (in ticks) the dragon should reconsider its actions.  This is
     * purely a placeholder for future AI work and is currently unused.
     */
    int getDecisionInterval();

    /**
     * Placeholder hook for future decision making logic.  Implementations can
     * override this method to provide custom AI behaviour when the decision
     * interval elapses.
     */
    default void decide(EnderDragon dragon) {
        // no-op for now
    }

    /**
     * Apply basic attributes to the supplied EnderDragon entity.
     * Implementations should avoid ability logic – this method is only for
     * name and simple attribute assignment.
     */
    void applyAttributes(EnderDragon dragon);
}
