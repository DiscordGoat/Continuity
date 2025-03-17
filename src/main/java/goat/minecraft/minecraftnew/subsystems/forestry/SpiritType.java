package goat.minecraft.minecraftnew.subsystems.forestry;

import org.bukkit.Color;

/**
 * Represents different types of forest spirits with their display names and colors
 */
public enum SpiritType {
    OAK_SPIRIT("Oak Spirit", Color.fromRGB(59, 38, 28)),
    BIRCH_SPIRIT("Birch Spirit", Color.fromRGB(216, 215, 210)),
    SPRUCE_SPIRIT("Spruce Spirit", Color.fromRGB(44, 34, 30)),
    JUNGLE_SPIRIT("Jungle Spirit", Color.fromRGB(87, 96, 41)),
    ACACIA_SPIRIT("Acacia Spirit", Color.fromRGB(169, 92, 51)),
    DARK_OAK_SPIRIT("Dark Oak Spirit", Color.fromRGB(48, 27, 12)),
    CRIMSON_SPIRIT("Crimson Spirit", Color.fromRGB(148, 63, 97)),
    WARPED_SPIRIT("Warped Spirit", Color.fromRGB(43, 104, 99)),
    MANGROVE_SPIRIT("Mangrove Spirit", Color.fromRGB(117, 54, 48)),
    CHERRY_SPIRIT("Cherry Spirit", Color.fromRGB(217, 174, 183));

    private final String displayName;
    private final Color color;

    SpiritType(String displayName, Color color) {
        this.displayName = displayName;
        this.color = color;
    }

    public String getDisplayName() {
        return displayName;
    }

    public Color getColor() {
        return color;
    }
}