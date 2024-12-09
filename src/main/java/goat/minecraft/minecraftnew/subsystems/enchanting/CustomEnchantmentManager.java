package goat.minecraft.minecraftnew.subsystems.enchanting;

import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

/**
 * Manages custom enchantments by adding enchantment lore to items.
 */
public class CustomEnchantmentManager {

    // Map to store all registered custom enchantments
    private static final Map<String, CustomEnchantment> enchantments = new HashMap<>();

    /**
     * Registers a new custom enchantment.
     *
     * @param name        The name of the enchantment (e.g., "Lifesteal").
     * @param maxLevel    The maximum level of the enchantment.
     * @param isTreasure  Whether the enchantment is a treasure enchantment.
     */
    public static void registerEnchantment(String name, int maxLevel, boolean isTreasure) {
        String key = name.toLowerCase().replace(" ", "_");
        if (!enchantments.containsKey(key)) {
            enchantments.put(key, new CustomEnchantment(name, maxLevel, isTreasure));
        }
    }

    /**
     * Applies a custom enchantment to an item.
     *
     * @param item        The item to enchant.
     * @param enchantmentName The name of the enchantment.
     * @param level       The level of the enchantment.
     * @return The enchanted item.
     */
    public static ItemStack addEnchantment(ItemStack billItem, ItemStack item, String enchantmentName, int level) {
        CustomEnchantment enchantment = getEnchantment(enchantmentName);
        if (enchantment == null) return item;
        if (level < 1 || level > enchantment.getMaxLevel()){

            return item;
        }

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;



        List<String> lore = meta.hasLore() ? meta.getLore() : new ArrayList<>();

        // Remove existing enchantment if present
        removeEnchantmentLore(lore, enchantment);

        // Add enchantment to lore
        String enchantmentLine = formatEnchantment(enchantment.getName(), level);
        lore.add(0, enchantmentLine); // Add to the beginning of the lore

        meta.setLore(lore);
        item.setItemMeta(meta);
        billItem.setAmount(billItem.getAmount() -1);
        return item;
    }
    public static int getMaxLevel(String enchantmentName) {
        CustomEnchantment enchantment = getEnchantment(enchantmentName);
        if (enchantment == null) {
            System.err.println("Error: Enchantment '" + enchantmentName + "' is not registered.");
            return -1;
        }
        return enchantment.getMaxLevel(); // Double the base max level
    }
    /**
     * Removes a custom enchantment from an item.
     *
     * @param item            The item to modify.
     * @param enchantmentName The name of the enchantment to remove.
     * @return The modified item.
     */
    public static ItemStack removeEnchantment(ItemStack item, String enchantmentName) {
        CustomEnchantment enchantment = getEnchantment(enchantmentName);
        if (enchantment == null) return item;

        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasLore()) return item;

        List<String> lore = meta.getLore();

        // Remove enchantment from lore
        boolean removed = removeEnchantmentLore(lore, enchantment);

        if (removed) {
            meta.setLore(lore.isEmpty() ? null : lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    /**
     * Checks if an item has a custom enchantment.
     *
     * @param item            The item to check.
     * @param enchantmentName The name of the enchantment.
     * @return True if the item has the enchantment, false otherwise.
     */
    public static boolean hasEnchantment(ItemStack item, String enchantmentName) {
        CustomEnchantment enchantment = getEnchantment(enchantmentName);
        if (enchantment == null) return false;

        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasLore()) return false;

        for (String line : meta.getLore()) {
            if (isEnchantmentLine(line, enchantment)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Gets the level of a custom enchantment on an item.
     *
     * @param item            The item to check.
     * @param enchantmentName The name of the enchantment.
     * @return The level of the enchantment, or 0 if not present.
     */
    public static int getEnchantmentLevel(ItemStack item, String enchantmentName) {
        CustomEnchantment enchantment = getEnchantment(enchantmentName);
        if (enchantment == null) return 0;

        if(item != null) {
            ItemMeta meta = item.getItemMeta();

            if (meta == null || !meta.hasLore()) return 0;

            for (String line : meta.getLore()) {
                if (isEnchantmentLine(line, enchantment)) {
                    return parseEnchantmentLevel(line);
                }
            }
        }
        return 0;
    }

    /**
     * Formats the enchantment line as per vanilla enchantments.
     *
     * @param name  The enchantment name.
     * @param level The enchantment level.
     * @return The formatted enchantment line.
     */
    private static String formatEnchantment(String name, int level) {
        String numeral = toRomanNumeral(level);
        return ChatColor.GRAY + name + " " + numeral;
    }

    /**
     * Removes an enchantment line from the lore.
     *
     * @param lore        The lore list.
     * @param enchantment The enchantment to remove.
     * @return True if the enchantment was removed, false otherwise.
     */
    private static boolean removeEnchantmentLore(List<String> lore, CustomEnchantment enchantment) {
        Iterator<String> iterator = lore.iterator();
        boolean removed = false;
        while (iterator.hasNext()) {
            String line = iterator.next();
            if (isEnchantmentLine(line, enchantment)) {
                iterator.remove();
                removed = true;
                break;
            }
        }
        return removed;
    }

    /**
     * Checks if a lore line represents the given enchantment.
     *
     * @param line         The lore line.
     * @param enchantment  The enchantment to check.
     * @return True if the line represents the enchantment, false otherwise.
     */
    private static boolean isEnchantmentLine(String line, CustomEnchantment enchantment) {
        String cleanLine = ChatColor.stripColor(line);
        String enchantmentName = enchantment.getName();
        return cleanLine.startsWith(enchantmentName);
    }

    /**
     * Parses the enchantment level from a lore line.
     *
     * @param line The lore line.
     * @return The level of the enchantment.
     */
    private static int parseEnchantmentLevel(String line) {
        String[] parts = ChatColor.stripColor(line).split(" ");
        if (parts.length < 2) return 1; // Default to level 1 if no numeral
        return fromRomanNumeral(parts[parts.length - 1]);
    }

    /**
     * Converts an integer to a Roman numeral.
     *
     * @param number The number to convert.
     * @return The Roman numeral representation.
     */
    private static String toRomanNumeral(int number) {
        if (number < 1 || number > 3999) return Integer.toString(number);
        StringBuilder result = new StringBuilder();
        int[] values =     {1000, 900, 500, 400, 100, 90, 50,  40, 10,  9,   5,   4,   1   };
        String[] numerals ={"M",  "CM","D", "CD","C","XC","L","XL","X","IX","V","IV","I"};
        for (int i = 0; i < values.length; i++) {
            while (number >= values[i]) {
                number -= values[i];
                result.append(numerals[i]);
            }
        }
        return result.toString();
    }

    /**
     * Converts a Roman numeral to an integer.
     *
     * @param roman The Roman numeral string.
     * @return The integer value.
     */
    private static int fromRomanNumeral(String roman) {
        Map<Character, Integer> romanMap = new HashMap<>();
        romanMap.put('M', 1000); romanMap.put('D', 500);
        romanMap.put('C', 100);  romanMap.put('L', 50);
        romanMap.put('X', 10);   romanMap.put('V', 5);
        romanMap.put('I', 1);
        int result = 0;
        int lastValue = 0;
        String upperRoman = roman.toUpperCase();
        for (int i = upperRoman.length() - 1; i >= 0; i--) {
            char c = upperRoman.charAt(i);
            int value = romanMap.getOrDefault(c, 0);
            if (value < lastValue) {
                result -= value;
            } else {
                result += value;
                lastValue = value;
            }
        }
        return result;
    }

    /**
     * Retrieves a registered custom enchantment.
     *
     * @param name The name of the enchantment.
     * @return The CustomEnchantment object, or null if not found.
     */
    private static CustomEnchantment getEnchantment(String name) {
        String key = name.toLowerCase().replace(" ", "_");
        return enchantments.get(key);
    }

    /**
     * Class representing a custom enchantment.
     */
    private static class CustomEnchantment {
        private final String name;
        private final int maxLevel;
        private final boolean isTreasure;

        public CustomEnchantment(String name, int maxLevel, boolean isTreasure) {
            this.name = name;
            this.maxLevel = maxLevel;
            this.isTreasure = isTreasure;
        }

        public String getName() {
            return name;
        }

        public int getMaxLevel() {
            return maxLevel;
        }

        public boolean isTreasure() {
            return isTreasure;
        }
    }
}
