package goat.minecraft.minecraftnew.subsystems.corpses;

import goat.minecraft.minecraftnew.subsystems.fishing.Rarity;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Represents a hostile Corpse NPC definition.
 */
public class Corpse {
    private final String displayName;
    private final Rarity rarity;
    private final int level;
    private final Material weaponMaterial;
    private final String skinUrl;
    private final boolean usesBow;
    private final List<DropItem> dropItems;

    private final Random random = new Random();

    public Corpse(String displayName, Rarity rarity, int level,
                  Material weaponMaterial, String skinUrl,
                  boolean usesBow, List<DropItem> dropItems) {
        this.displayName = displayName;
        this.rarity = rarity;
        this.level = level;
        this.weaponMaterial = weaponMaterial;
        this.skinUrl = skinUrl;
        this.usesBow = usesBow;
        this.dropItems = dropItems;
    }

    public String getDisplayName() { return displayName; }
    public Rarity getRarity() { return rarity; }
    public int getLevel() { return level; }
    public Material getWeaponMaterial() { return weaponMaterial; }
    public String getSkinUrl() { return skinUrl; }
    public boolean usesBow() { return usesBow; }

    public List<ItemStack> getDrops() {
        List<ItemStack> drops = new ArrayList<>();
        if (dropItems == null) return drops;
        for (DropItem drop : dropItems) {
            int qty = 0;
            for (int i = 0; i < drop.getRollCount(); i++) {
                if (random.nextInt(drop.getRollDenominator()) < drop.getRollNumerator()) {
                    qty++;
                }
            }
            if (qty > 0) {
                ItemStack item = drop.getItemStack().clone();
                item.setAmount(qty);
                drops.add(item);
            }
        }
        return drops;
    }

    /** Represents an item drop with roll chance information. */
    public static class DropItem {
        private final ItemStack itemStack;
        private final int rollCount;
        private final int rollNumerator;
        private final int rollDenominator;

        public DropItem(ItemStack itemStack, int rollCount, int rollNumerator, int rollDenominator) {
            this.itemStack = itemStack;
            this.rollCount = rollCount;
            this.rollNumerator = rollNumerator;
            this.rollDenominator = rollDenominator;
        }

        public ItemStack getItemStack() { return itemStack; }
        public int getRollCount() { return rollCount; }
        public int getRollNumerator() { return rollNumerator; }
        public int getRollDenominator() { return rollDenominator; }
    }
}
