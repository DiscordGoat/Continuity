package goat.minecraft.minecraftnew.other.enchanting;

import goat.minecraft.minecraftnew.MinecraftNew;
import goat.minecraft.minecraftnew.utils.devtools.XPManager;
import org.bukkit.ChatColor;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

/**
 * Manages custom enchantments by adding enchantment lore to items.
 */
public class CustomEnchantmentManager {

    // Map to store all registered custom enchantments
    private static final Map<String, CustomEnchantment> enchantments = new HashMap<>();

    // Represents an Ultimate Enchantment found on an item’s lore
    public static class UltimateEnchantmentData {
        private final String name;   // e.g. "Inferno Blade"
        private final int level;     // e.g. 3

        public UltimateEnchantmentData(String name, int level) {
            this.name = name;
            this.level = level;
        }
        public String getName() {
            return name;
        }
        public int getLevel() {
            return level;
        }
    }

    /**
     * Adds a "Ultimate: <Name> <RomanNumeral>" line to the item,
     * while removing any existing lines that start with "Ultimate: ".
     *
     * @param billItem         The item stack consumed upon enchant (like in addEnchantment).
     * @param item             The item to enchant.
     * @param enchantmentName  The custom ultimate enchantment name.
     * @param level            The level of the enchantment.
     * @return                 The enchanted item.
     */
    public static ItemStack addUltimateEnchantment(Player player, ItemStack billItem, ItemStack item, String enchantmentName, int level) {
        if (item == null) return null;

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;

        List<String> lore = meta.hasLore() ? meta.getLore() : new ArrayList<>();

        // 1) Remove any lines that start with "Ultimate: "
        Iterator<String> iterator = lore.iterator();
        while (iterator.hasNext()) {
            String line = ChatColor.stripColor(iterator.next());
            if (line.startsWith("Ultimate: ")) {
                iterator.remove();
            }
        }

        // 2) Build the new enchantment line, e.g. "Ultimate: Inferno Blade III"
        String numeral = toRomanNumeral(level);
        String newLine = ChatColor.GRAY + enchantmentName + " " + numeral;

        // 3) Insert at the front of the lore
        lore.add(0, newLine);

        meta.setLore(lore);
        item.setItemMeta(meta);

        // 4) Decrement the billItem by 1 if it's not null
        if (billItem != null && billItem.getAmount() > 0) {
            billItem.setAmount(billItem.getAmount() - 1);
        }
        player.getLocation().getWorld().spawnParticle(Particle.ENCHANTMENT_TABLE, player.getLocation(), 30, 0.5, 0.5, 0.5, 0.1);
        player.playSound(player.getLocation(), Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1.0f, 1.0f);

        // 6) Send a message to the player
        player.sendMessage(ChatColor.GOLD + "✨ " + ChatColor.BOLD + "Enchantment Success!" + ChatColor.GOLD + " ✨");
        player.sendMessage(ChatColor.GRAY + "Your item has been enchanted with " + ChatColor.GREEN + enchantmentName + " " + numeral + ChatColor.GRAY + "!");

        return item;
    }

    /**
     * NEW: Parses an ItemStack's lore to find the line that starts with "Ultimate: "
     * and returns an UltimateEnchantmentData object (name + level).
     *
     * @param item The ItemStack to check.
     * @return UltimateEnchantmentData if found, otherwise null.
     */
    public static UltimateEnchantmentData getUltimateEnchantment(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return null;
        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasLore()) return null;

        for (String loreLine : meta.getLore()) {
            String stripped = ChatColor.stripColor(loreLine);
            if (stripped.startsWith("Ultimate: ")) {
                // Format is "Ultimate: <Name> <Roman>"
                // We need to parse the name and the level from the line.
                // Example: "Ultimate: Inferno Blade III"
                // Split by spaces:
                // 0 -> "Ultimate:"
                // 1 -> "Inferno"
                // 2 -> "Blade"
                // 3 -> "III"

                String[] parts = stripped.split(" ");
                if (parts.length < 3) {
                    // Not enough parts to have both name and level
                    return null;
                }

                // The first part is "Ultimate:", the last part is the Roman numeral
                // Everything in between is the enchantment name.
                String romanNumeral = parts[parts.length - 1];
                int level = fromRomanNumeral(romanNumeral);

                // Rebuild the enchantment name from parts[1] to parts[parts.length - 2]
                StringBuilder nameBuilder = new StringBuilder();
                for (int i = 1; i < parts.length - 1; i++) {
                    nameBuilder.append(parts[i]);
                    if (i < parts.length - 2) {
                        nameBuilder.append(" ");
                    }
                }
                String enchantName = nameBuilder.toString();

                return new UltimateEnchantmentData(enchantName, level);
            }
        }

        return null;
    }

    /**
     * Registers a new custom enchantment.
     */
    public static void registerEnchantment(String name, int maxLevel, boolean isTreasure) {
        String key = name.toLowerCase().replace(" ", "_");
        if (!enchantments.containsKey(key)) {
            enchantments.put(key, new CustomEnchantment(name, maxLevel, isTreasure));
        }
    }

    /**
     * Applies a custom enchantment to an item. (Non-ultimate usage)
     */
    public static ItemStack addEnchantment(Player player, ItemStack billItem, ItemStack item, String enchantmentName, int level) {
        XPManager xpManager = new XPManager(MinecraftNew.getInstance());
        CustomEnchantment enchantment = getEnchantment(enchantmentName);
        if (enchantment == null) return item;
        if (level < 1 || level > enchantment.getMaxLevel()) {
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
        billItem.setAmount(billItem.getAmount() - 1);
        xpManager.addXP(player, "Smithing", 200);
        return item;
    }

    public static int getMaxLevel(String enchantmentName) {
        CustomEnchantment enchantment = getEnchantment(enchantmentName);
        if (enchantment == null) {
            System.err.println("Error: Enchantment '" + enchantmentName + "' is not registered.");
            return -1;
        }
        return enchantment.getMaxLevel();
    }

    public static ItemStack removeEnchantment(ItemStack item, String enchantmentName) {
        CustomEnchantment enchantment = getEnchantment(enchantmentName);
        if (enchantment == null) return item;

        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasLore()) return item;

        List<String> lore = meta.getLore();
        boolean removed = removeEnchantmentLore(lore, enchantment);

        if (removed) {
            meta.setLore(lore.isEmpty() ? null : lore);
            item.setItemMeta(meta);
        }
        return item;
    }

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

    public static int getEnchantmentLevel(ItemStack item, String enchantmentName) {
        CustomEnchantment enchantment = getEnchantment(enchantmentName);
        if (enchantment == null) return 0;

        if (item != null) {
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

    private static String formatEnchantment(String name, int level) {
        String numeral = toRomanNumeral(level);
        return ChatColor.GRAY + name + " " + numeral;
    }

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

    private static boolean isEnchantmentLine(String line, CustomEnchantment enchantment) {
        String cleanLine = ChatColor.stripColor(line);
        String enchantmentName = enchantment.getName();
        return cleanLine.startsWith(enchantmentName);
    }

    private static int parseEnchantmentLevel(String line) {
        String[] parts = ChatColor.stripColor(line).split(" ");
        if (parts.length < 2) return 1; // Default to level 1 if no numeral
        return fromRomanNumeral(parts[parts.length - 1]);
    }

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

    private static int fromRomanNumeral(String roman) {
        Map<Character, Integer> romanMap = new HashMap<>();
        romanMap.put('M', 1000);
        romanMap.put('D', 500);
        romanMap.put('C', 100);
        romanMap.put('L', 50);
        romanMap.put('X', 10);
        romanMap.put('V', 5);
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

    private static CustomEnchantment getEnchantment(String name) {
        String key = name.toLowerCase().replace(" ", "_");
        return enchantments.get(key);
    }

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
