package goat.minecraft.minecraftnew.other.skilltree;

import org.bukkit.Material;

public class Talent {
    private final String name;
    private final String description;
    private final int maxLevel;
    private final int levelRequirement;
    private final Material icon;

    public Talent(String name, String description, int maxLevel, int levelRequirement, Material icon) {
        this.name = name;
        this.description = description;
        this.maxLevel = maxLevel;
        this.levelRequirement = levelRequirement;
        this.icon = icon;
    }

    public String getName() { return name; }

    public String getDescription() { return description; }

    public int getMaxLevel() { return maxLevel; }

    public int getLevelRequirement() { return levelRequirement; }

    public Material getIcon() { return icon; }

    public TalentRarity getRarity() { return TalentRarity.fromRequirement(levelRequirement); }
}
