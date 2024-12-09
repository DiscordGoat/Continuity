package goat.minecraft.minecraftnew.subsystems.utils;

import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class TalismanManager {

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

        // Check if any existing reforge lore line is present and update it
        boolean reforgeLineFound = false;
        for (int i = 0; i < lore.size(); i++) {
            String line = ChatColor.stripColor(lore.get(i));
            if (line.startsWith("Talisman: ")) {
                lore.set(i, ChatColor.GOLD + "Talisman: " + reforgeType); // Update existing reforge line
                reforgeLineFound = true;
                break;
            }
        }

        // If no existing reforge lore line, add it
        if (!reforgeLineFound) {
            lore.add(ChatColor.GOLD + "Talisman: " + reforgeType);
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
                    return strippedLine.substring("Talisman: ".length()); // Return the reforge type
                }
            }
        }
        return null; // No reforge found
    }
}
