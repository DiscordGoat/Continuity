package goat.minecraft.minecraftnew.subsystems.corpses;

import goat.minecraft.minecraftnew.subsystems.fishing.Rarity;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Corpse {
    private final String name;
    private final Rarity rarity;
    private final int level;
    private final ItemStack weapon;
    private final boolean ranged;
    private final String skinUrl;
    private final List<DropItem> dropItems;
    private final Random random = new Random();

    public Corpse(String name, Rarity rarity, int level, Material weaponType, boolean ranged, String skinUrl, List<DropItem> drops) {
        this.name = name;
        this.rarity = rarity;
        this.level = level;
        this.weapon = weaponType == null ? null : new ItemStack(weaponType);
        this.ranged = ranged;
        this.skinUrl = skinUrl;
        this.dropItems = drops;
    }

    public String getName() { return name; }
    public Rarity getRarity() { return rarity; }
    public int getLevel() { return level; }
    public ItemStack getWeapon() { return weapon == null ? null : weapon.clone(); }
    public boolean isRanged() { return ranged; }
    public String getSkinUrl() { return skinUrl; }

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
