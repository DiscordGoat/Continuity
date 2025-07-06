package goat.minecraft.minecraftnew.subsystems.pets;

import org.bukkit.ChatColor;

/**
 * Standard pet traits. Each trait defines a description of its
 * effect and an array of stat values for each {@link TraitRarity}.
 * The array order matches the enum order of TraitRarity.
 */

public enum PetTrait {
    HEALTHY(ChatColor.RED, "Health Increase", new double[]{2,4,6,8,10,12,15}),
    FAST(ChatColor.YELLOW, "Speed Increase", new double[]{2,3,4,5,6,7,8}),
    STRONG(ChatColor.RED, "Damage Increase", new double[]{2,4,6,8,10,12,15}),
    RESILIENT(ChatColor.DARK_GRAY, "Damage Reduction", new double[]{1,2,3,4,5,6,8}),
    NAUTICAL(ChatColor.AQUA, "Sea Creature Chance", new double[]{1,2,3,4,5,6,8}),
    HAUNTED(ChatColor.AQUA, "Spirit Chance", new double[]{1,2,3,4,5,6,8}),
    PRECISE(ChatColor.RED, "Arrow Damage Increase", new double[]{1,2,3,4,5,6,8}),
    FINANCIAL(ChatColor.YELLOW, "Discount", new double[]{1,2,3,4,5,6,8}),
    EVASIVE(ChatColor.LIGHT_PURPLE, "Dodge Chance", new double[]{1,2,3,4,5,6,8}),
    TREASURED(ChatColor.GOLD, "Treasure Chance", new double[]{1,2,3,4,5,6,8});

    private final ChatColor color;
    private final String description;
    private final double[] values;

    PetTrait(ChatColor color, String description, double[] values) {
        this.color = color;
        this.description = description;
        this.values = values;
    }

    public ChatColor getColor() {
        return color;
    }

    public String getDescription() {
        return description;
    }

    public double getValueForRarity(TraitRarity rarity) {
        int index = rarity.ordinal();
        if (index < 0 || index >= values.length) {
            return 0;
        }
        return values[index];
    }

    public String getDisplayName() {
        String lower = name().toLowerCase();
        return Character.toUpperCase(lower.charAt(0)) + lower.substring(1);
    }
}
