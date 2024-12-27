package goat.minecraft.minecraftnew.utils;

import goat.minecraft.minecraftnew.MinecraftNew;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Utility class for handling enchantments on ItemStacks.
 */
public class EnchantmentUtils {
    /**
     * Sets an enchantment on an ItemStack regardless of type or level restrictions.
     * Allows unsafe enchantments.
     *
     * @param item        The ItemStack to enchant.
     * @param enchantment The Enchantment to apply.
     * @param level       The level of the enchantment to apply.
     * @return The modified ItemStack with the applied enchantment,
     *         or the original item if modification failed.
     */
    public static ItemStack setEnchantment(ItemStack item, Enchantment enchantment, int level) {
        if (item == null || item.getType().isAir()) {
            System.err.println("Error: Cannot enchant a null or AIR item.");
            return item;
        }

        // Apply the enchantment
        item.addUnsafeEnchantment(enchantment, level);

        System.out.println("Set " + enchantment.getKey().getKey() + " level to "
                + level + " for item " + item.getType());
        return item;
    }
    /**
     * Increments an enchantment by 1 level, provided that the enchantment is already at maxLevel + 1.
     * This method allows for unsafe enchantments beyond the vanilla max level.
     *
     * @param item        The ItemStack to modify.
     * @param billItem    The ItemStack representing the cost (e.g., currency) for the enchantment.
     * @param enchantment The Enchantment to increment.
     */
    /**
     * Increments an enchantment by 1 level, provided that the enchantment is already at maxLevel + 1.
     * This method allows for unsafe enchantments beyond the vanilla max level.
     *
     * @param item        The ItemStack to modify.
     * @param billItem    The ItemStack representing the cost (e.g., currency) for the enchantment.
     * @param enchantment The Enchantment to increment.
     * @return The modified ItemStack with the incremented enchantment level,
     *         or the original item if the enchantment is not at maxLevel + 1.
     */
    public static ItemStack incrementInfernalEnchantment(ItemStack item, ItemStack billItem, Enchantment enchantment) {
        if (item == null || item.getType().isAir()) {
            System.err.println("Error: Cannot enchant a null or AIR item.");
            return item;
        }

        if (!isItemEnchantable(item)) {
            System.err.println("Error: The item " + item.getType() + " cannot be enchanted.");
            return item;
        }

        int currentLevel = item.getEnchantmentLevel(enchantment);
        int vanillaMaxLevel = enchantment.getMaxLevel(); // Vanilla max level
        int infernalMaxLevel = vanillaMaxLevel + 1; // Infernal max level (maxLevel + 1)

        // Check if the enchantment is already at maxLevel + 1
        if (currentLevel != infernalMaxLevel) {
            System.out.println("Enchantment must be at maxLevel + 1 to use Infernal Enchantments.");
            return item;
        }

        // Increment the enchantment level by 1
        int newLevel = currentLevel + 1;
        item.addUnsafeEnchantment(enchantment, newLevel);

        // Decrease the billItem amount by 1 only after successful enchantment
        billItem.setAmount(billItem.getAmount() - 1);

        System.out.println("Incremented " + enchantment.getKey().getKey() + " level from "
                + currentLevel + " to " + newLevel + " for item " + item.getType());

        return item;
    }


    /**
     * Increments the specified enchantment level by 1.
     * If the enchantment is not present, it adds it with level 1.
     * Allows unsafe enchantments.
     *
     * @param item        The ItemStack to modify.
     * @param enchantment The Enchantment to increment.
     * @return The modified ItemStack with the incremented enchantment level,
     *         or the original item if modification failed.
     */
    public static ItemStack incrementEnchantment(Player player, ItemStack item, ItemStack billItem, Enchantment enchantment) {
        XPManager xpManager = new XPManager(MinecraftNew.getInstance());
        if (item == null || item.getType().isAir()) {
            System.err.println("Error: Cannot enchant a null or AIR item.");
            return item;
        }

        if (!isItemEnchantable(item)) {
            System.err.println("Error: The item " + item.getType() + " cannot be enchanted.");
            return item;
        }

        int currentLevel = item.getEnchantmentLevel(enchantment);
        int maxLevel = enchantment.getMaxLevel();

        if (currentLevel >= maxLevel) {
            System.out.println("Enchantment level limit reached.");
            return item;
        }

        int newLevel = currentLevel + 1;
        item.addUnsafeEnchantment(enchantment, newLevel);

        // Handle billItem if it's not null
        if (billItem != null && billItem.getAmount() > 0) {
            billItem.setAmount(billItem.getAmount() - 1);
            xpManager.addXP(player, "Smithing", 100);
        } else if (billItem == null) {
            System.out.println("No billItem was required for this enchantment.");
        } else {
            System.err.println("Error: Bill item amount invalid.");
        }

        return item;
    }


    public static ItemStack incrementEnchantmentUnsafely(Player player, ItemStack item, ItemStack billItem, Enchantment enchantment) {
        XPManager xpManager = new XPManager(MinecraftNew.getInstance());
        if (item == null || item.getType().isAir()) {
            System.err.println("Error: Cannot enchant a null or AIR item.");
            return item;
        }

        if (!isItemEnchantable(item)) {
            System.err.println("Error: The item " + item.getType() + " cannot be enchanted.");
            return item;
        }

        int currentLevel = item.getEnchantmentLevel(enchantment);
        int maxLevel = enchantment.getMaxLevel() + 2; // Set cap at double the base level
        int vanillaMaxLevel = enchantment.getMaxLevel(); // Set cap at double the base level
        if (currentLevel != vanillaMaxLevel) {
            // Cap reached, add billItem to player's inventory
            System.out.println("Enchantment must be max level to use Mastery Enchantments.");
            return item;
        }
        if (currentLevel == maxLevel) {
            // Cap reached, add billItem to player's inventory
            System.out.println("Enchantment level limit reached.");
            return item;
        }

        int newLevel = currentLevel + 1;
        item.addUnsafeEnchantment(enchantment, newLevel);

        System.out.println("Incremented " + enchantment.getKey().getKey() + " level from "
                + currentLevel + " to " + newLevel + " for item " + item.getType());
        if(billItem == null){

        }else {
            billItem.setAmount(billItem.getAmount() - 1);
            xpManager.addXP(player, "Smithing", 1000);
        }
        return item;
    }

    // Method to add billItem to the player's inventory



    private static boolean isItemEnchantable(ItemStack item) {
        // Retrieve the item's metadata
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return false;
        }

        // Define enchantable criteria:
        // - Item is not unbreakable
        // - Item has a max durability greater than 0
        // - Item is not edible (to exclude items like golden apples)
        return !meta.isUnbreakable() &&
                item.getType().getMaxDurability() > 0 &&
                !item.getType().isEdible();
    }
}
