package goat.minecraft.minecraftnew.utils.devtools;

import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import java.util.List;
import java.util.ArrayList;

public class SkinManager {

    /**
     * Sets or updates the skin information in the lore of the given ItemStack.
     *
     * @param item     The ItemStack whose lore will be modified.
     * @param skinName The skin name to set.
     */
    public static void setSkin(ItemStack item, String skinName) {
        if (item == null) {
            return; // No item provided
        }

        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return; // Item has no metadata to update
        }

        // Retrieve the lore if it exists, otherwise create a new list.
        List<String> lore = meta.hasLore() ? meta.getLore() : new ArrayList<>();

        boolean skinLineFound = false;
        for (int i = 0; i < lore.size(); i++) {
            String line = lore.get(i);
            if (line.startsWith(ChatColor.DARK_PURPLE + "Skin:")) {
                // Update existing skin line
                lore.set(i, ChatColor.DARK_PURPLE + "Skin: " + skinName);
                skinLineFound = true;
                break;
            }
        }

        // If no skin line was found, add it to the lore.
        if (!skinLineFound) {
            lore.add(ChatColor.DARK_PURPLE + "Skin: " + skinName);
        }

        // Update the item's metadata with the modified lore.
        meta.setLore(lore);
        item.setItemMeta(meta);
    }

    // Additional helper methods can be added here if needed in the future.
}
