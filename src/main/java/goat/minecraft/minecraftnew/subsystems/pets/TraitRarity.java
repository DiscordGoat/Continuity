package goat.minecraft.minecraftnew.subsystems.pets;

import org.bukkit.ChatColor;

/**
 * Rarity tiers for pet traits. Each tier has an associated weight
 * used for random generation chances.
 */
public enum TraitRarity {
    COMMON(50),
    UNCOMMON(30),
    RARE(10),
    EPIC(5),
    LEGENDARY(2),
    MYTHIC(0.5),
    UNIQUE(2.5);

    private final double weight;

    TraitRarity(double weight) {
        this.weight = weight;
    }

    public double getWeight() {
        return weight;
    }

    /**
     * Returns a chat color for displaying this rarity.
     */
    public ChatColor getColor() {
        switch (this) {
            case COMMON: return ChatColor.WHITE;
            case UNCOMMON: return ChatColor.GREEN;
            case RARE: return ChatColor.BLUE;
            case EPIC: return ChatColor.DARK_PURPLE;
            case LEGENDARY: return ChatColor.GOLD;
            case MYTHIC: return ChatColor.LIGHT_PURPLE;
            case UNIQUE: return ChatColor.AQUA;
            default: return ChatColor.WHITE;
        }
    }
}
