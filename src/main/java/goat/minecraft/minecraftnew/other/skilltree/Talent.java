package goat.minecraft.minecraftnew.other.skilltree;

import org.bukkit.Material;

/**
 * Enumeration of all available talents in the plugin.  This acts as a
 * registry so other classes can easily reference a specific talent by
 * using <code>Talent.REDSTONE</code> for example.
 */
public enum Talent {
    REDSTONE(
            "Redstone",
            "Adds 4s to potions you drink per level.",
            25,
            1,
            Material.REDSTONE
    );

    private final String name;
    private final String description;
    private final int maxLevel;
    private final int levelRequirement;
    private final Material icon;

    Talent(String name, String description, int maxLevel, int levelRequirement, Material icon) {
        this.name = name;
        this.description = description;
        this.maxLevel = maxLevel;
        this.levelRequirement = levelRequirement;
        this.icon = icon;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public int getMaxLevel() {
        return maxLevel;
    }

    public int getLevelRequirement() {
        return levelRequirement;
    }

    public Material getIcon() {
        return icon;
    }

    public TalentRarity getRarity() {
        return TalentRarity.fromRequirement(levelRequirement);
    }
}
