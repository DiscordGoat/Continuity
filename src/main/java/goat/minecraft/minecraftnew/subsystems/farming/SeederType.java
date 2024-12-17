package goat.minecraft.minecraftnew.subsystems.farming;

import org.bukkit.ChatColor;
import org.bukkit.Material;

public enum SeederType {
    WHEAT_SEEDER(ChatColor.YELLOW + "WheatSeeder", Material.WHEAT_SEEDS),
    POTATO_SEEDER(ChatColor.YELLOW + "PotatoSeeder", Material.POTATOES),
    CARROT_SEEDER(ChatColor.YELLOW + "CarrotSeeder", Material.CARROTS),
    BEETROOT_SEEDER(ChatColor.YELLOW + "BeetrootSeeder", Material.BEETROOTS);

    private final String displayName;
    private final Material cropMaterial;

    SeederType(String displayName, Material cropMaterial) {
        this.displayName = displayName;
        this.cropMaterial = cropMaterial;
    }

    public String getDisplayName() {
        return displayName;
    }

    public Material getCropMaterial() {
        return cropMaterial;
    }

    /**
     * Retrieves the SeederType based on the item's display name.
     *
     * @param displayName The display name of the item.
     * @return The corresponding SeederType, or null if not found.
     */
    public static SeederType fromDisplayName(String displayName) {
        for (SeederType type : SeederType.values()) {
            if (type.getDisplayName().equals(displayName)) {
                return type;
            }
        }
        return null;
    }
}
