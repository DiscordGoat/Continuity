package goat.minecraft.minecraftnew.other.additionalfunctionality;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * Utility methods for handling blessed armor sets.
 */
public final class BlessingUtils {

    private BlessingUtils() {
        // Utility class
    }

    /**
     * Extract the blessing name from an armor piece item.
     *
     * @param item The item to read.
     * @return The blessing prefix of the display name or {@code null} if none.
     */
    public static String getBlessing(ItemStack item) {
        if (item == null || item.getType() == Material.AIR || !item.hasItemMeta()) {
            return null;
        }
        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) {
            return null;
        }
        String name = ChatColor.stripColor(meta.getDisplayName());
        int idx = name.lastIndexOf(' ');
        return idx > 0 ? name.substring(0, idx) : null;
    }

    /**
     * Determine if the player is wearing a full blessed set matching the given blessing.
     *
     * @param player    The player to inspect.
     * @param blessing  The blessing name to match.
     * @return {@code true} if all armor pieces are blessed with the given name.
     */
    public static boolean hasFullSetBonus(Player player, String blessing) {
        if (player == null || blessing == null) {
            return false;
        }
        String h = getBlessing(player.getInventory().getHelmet());
        String c = getBlessing(player.getInventory().getChestplate());
        String l = getBlessing(player.getInventory().getLeggings());
        String b = getBlessing(player.getInventory().getBoots());
        Bukkit.getLogger().info("Blessings → H=" + h + ", C=" + c + ", L=" + l + ", B=" + b);


        return blessing.equalsIgnoreCase(h) &&
               blessing.equalsIgnoreCase(c) &&
               blessing.equalsIgnoreCase(l) &&
               blessing.equalsIgnoreCase(b);
    }
}
