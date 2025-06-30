package goat.minecraft.minecraftnew.subsystems.armorsets;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

/**
 * Types of Flow effects used for armor sets.
 */
public enum FlowType {
    NATURES_WRATH(Material.IRON_AXE);

    private final Material material;

    FlowType(Material material) {
        this.material = material;
    }

    public ItemStack createItem() {
        return new ItemStack(material);
    }
}
