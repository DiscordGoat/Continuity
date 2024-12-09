package goat.minecraft.minecraftnew.subsystems.skillbuffs;

import goat.minecraft.minecraftnew.subsystems.utils.XPManager;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;

public class FarmingBuff implements Listener {

    private final XPManager xpManager;

    public FarmingBuff(XPManager xpManager) {
        this.xpManager = xpManager;
    }

    @EventHandler
    public void onItemConsume(PlayerItemConsumeEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        // Check if the item is consumable
        if (isConsumable(item)) {
            int farmingLevel = xpManager.getPlayerLevel(player, "Farming");

            // Calculate additional saturation based on Farming level
            double additionalSaturation = Math.min(farmingLevel * 0.05, 5); // Max 20 saturation at level 100

            player.setSaturation((float) Math.min(player.getSaturation() + additionalSaturation, 20.0));

            // Notify the player (optional)
            player.sendMessage(ChatColor.GREEN + "Farming Buff applied! Extra Saturation: " + ChatColor.YELLOW + String.format("%.2f", additionalSaturation));
        }
    }

    /**
     * Checks if an item is consumable.
     *
     * @param item The item to check.
     * @return True if the item is consumable, false otherwise.
     */
    private boolean isConsumable(ItemStack item) {
        Material type = item.getType();
        return type.isEdible();
    }
}
