package goat.minecraft.minecraftnew.subsystems.pets;

import org.bukkit.ChatColor;

/**
 * Rarity tiers for pet traits. Each tier has an associated weight
 * used for random generation chances.
 */
public enum TraitRarity {
    COMMON(25),
    UNCOMMON(25),
    RARE(20),
    EPIC(15),
    LEGENDARY(7),
    MYTHIC(3),
    UNIQUE(5);

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
            case MYTHIC: return ChatColor.AQUA;
            case UNIQUE: return ChatColor.DARK_RED;
            default: return ChatColor.WHITE;
        }
    }

    /**
     * Returns a nicely formatted name for this rarity.
     */
    public String getDisplayName() {
        String lower = name().toLowerCase();
        return Character.toUpperCase(lower.charAt(0)) + lower.substring(1);
    }
}
