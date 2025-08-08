package goat.minecraft.minecraftnew.utils.devtools;

import goat.minecraft.minecraftnew.utils.stats.StrengthManager;
import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class TalismanManager {

    /**
     * Default Strength granted by a Damage talisman.
     */
    public static final int DAMAGE_STRENGTH_BONUS = 30;

    /**
     * Applies or updates the reforge lore on an ItemStack.
     *
     * @param item        The ItemStack to modify.
     * @param reforgeType The type of reforge to apply.
     */
    public static void applyReforgeLore(ItemStack item, String reforgeType) {
        if (item == null || reforgeType == null || reforgeType.isEmpty()) {
            return;
        }

        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return;
        }

        List<String> lore = meta.getLore();
        if (lore == null) {
            lore = new ArrayList<>();
        }

        String newLine;
        if ("Damage".equalsIgnoreCase(reforgeType)) {
            newLine = ChatColor.GOLD + "Talisman: Damage. "
                    + StrengthManager.COLOR + "+" + DAMAGE_STRENGTH_BONUS + " Strength" + StrengthManager.EMOJI;
        } else {
            newLine = ChatColor.GOLD + "Talisman: " + reforgeType;
        }

        // Check if any existing reforge lore line is present and update it
        boolean reforgeLineFound = false;
        for (int i = 0; i < lore.size(); i++) {
            String line = ChatColor.stripColor(lore.get(i));
            if (line.startsWith("Talisman: ")) {
                lore.set(i, newLine); // Update existing reforge line
                reforgeLineFound = true;
                break;
            }
        }

        // If no existing reforge lore line, add it
        if (!reforgeLineFound) {
            lore.add(newLine);
        }

        meta.setLore(lore);
        item.setItemMeta(meta);
    }

    /**
     * Retrieves the current reforge applied to an item.
     *
     * @param item The ItemStack to check.
     * @return The reforge type if found, or null if no reforge is applied.
     */
    public static String getReforgeType(ItemStack item) {
        if (item == null || item.getItemMeta() == null) {
            return null;
        }

        List<String> lore = item.getItemMeta().getLore();
        if (lore != null) {
            for (String line : lore) {
                String strippedLine = ChatColor.stripColor(line);
                if (strippedLine.startsWith("Talisman: ")) {
                    String remainder = strippedLine.substring("Talisman: ".length());
                    int dot = remainder.indexOf('.');
                    if (dot >= 0) {
                        remainder = remainder.substring(0, dot);
                    }
                    return remainder.trim();
                }
            }
        }
        return null; // No reforge found
    }

    /**
     * Retrieves the Strength bonus provided by a Damage talisman on the item.
     *
     * @param item Item to inspect.
     * @return Strength value granted or 0 if none.
     */
    public static int getDamageStrength(ItemStack item) {
        if (item == null || item.getItemMeta() == null) {
            return 0;
        }

        List<String> lore = item.getItemMeta().getLore();
        if (lore != null) {
            for (String line : lore) {
                String strippedLine = ChatColor.stripColor(line);
                if (strippedLine.startsWith("Talisman: Damage")) {
                    int plus = strippedLine.indexOf('+');
                    if (plus >= 0) {
                        String num = strippedLine.substring(plus + 1).replaceAll("[^0-9]", "");
                        if (!num.isEmpty()) {
                            try {
                                return Integer.parseInt(num);
                            } catch (NumberFormatException ignored) {
                                // fall through to default
                            }
                        }
                    }
                    return DAMAGE_STRENGTH_BONUS;
                }
            }
        }
        return 0;
    }
}
