package goat.minecraft.minecraftnew.subsystems.corpses;

import goat.minecraft.minecraftnew.subsystems.fishing.Rarity;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Corpse {
    private final String name;
    private final Rarity rarity;
    private final int level;
    private final Material weapon;
    private final boolean bow;
    private final String skinUrl;
    private final List<DropItem> dropItems;
    private final Random random = new Random();

    public Corpse(String name, Rarity rarity, int level, Material weapon, boolean bow, String skinUrl, List<DropItem> dropItems) {
        this.name = name;
        this.rarity = rarity;
        this.level = level;
        this.weapon = weapon;
        this.bow = bow;
        this.skinUrl = skinUrl;
        this.dropItems = dropItems == null ? new ArrayList<>() : dropItems;
    }

    public String getName() {
        return name;
    }

    public Rarity getRarity() {
        return rarity;
    }

    public int getLevel() {
        return level;
    }

    public Material getWeapon() {
        return weapon;
    }

    public boolean usesBow() {
        return bow;
    }

    public String getSkinUrl() {
        return skinUrl;
    }

    public List<org.bukkit.inventory.ItemStack> getDrops() {
        List<org.bukkit.inventory.ItemStack> drops = new ArrayList<>();
        for (DropItem di : dropItems) {
            int qty = 0;
            for (int i = 0; i < di.rollCount; i++) {
                if (random.nextInt(di.rollDenominator) < di.rollNumerator) {
                    qty++;
                }
            }
            if (qty > 0) {
                org.bukkit.inventory.ItemStack item = di.item.clone();
                item.setAmount(qty);
                drops.add(item);
            }
        }
        return drops;
    }

    public static class DropItem {
        private final org.bukkit.inventory.ItemStack item;
        private final int rollCount;
        private final int rollNumerator;
        private final int rollDenominator;

        public DropItem(org.bukkit.inventory.ItemStack item, int rollCount, int rollNumerator, int rollDenominator) {
            this.item = item;
            this.rollCount = rollCount;
            this.rollNumerator = rollNumerator;
            this.rollDenominator = rollDenominator;
        }
    }
}
