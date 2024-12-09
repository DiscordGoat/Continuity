package goat.minecraft.minecraftnew.subsystems.smithing.talismans;

import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Random;

public class ReforgeDurability implements Listener {

    private static final double DURABILITY_SAVE_CHANCE = 0.04; // 20% chance to prevent durability loss

    @EventHandler
    public void onItemDamage(PlayerItemDamageEvent event) {
        ItemStack item = event.getItem();

        // Check if the item has the "Reforged: Durable" lore
        if (isReforgedDurable(item)) {
            Random random = new Random();
            if (random.nextDouble() < DURABILITY_SAVE_CHANCE) {
                // 20% chance triggered; cancel the event to prevent durability reduction
                event.setCancelled(true);

                event.getPlayer().playSound(event.getPlayer().getLocation(), Sound.BLOCK_ANVIL_USE, 0.5f, 10);
            }
        }
    }

    /**
     * Checks if the item has "Reforged: Durable" in its lore.
     *
     * @param item The ItemStack to check.
     * @return True if the item has "Reforged: Durable" in its lore, false otherwise.
     */
    private boolean isReforgedDurable(ItemStack item) {
        if (item == null) {
            return false;
        }
        ItemMeta meta = item.getItemMeta();
        if (meta != null && meta.hasLore()) {
            for (String lore : meta.getLore()) {
                if (ChatColor.stripColor(lore).equals("Talisman: Durability")) {
                    return true;
                }
            }
        }
        return false;
    }
}
