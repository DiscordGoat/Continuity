package goat.minecraft.minecraftnew.other.qol;

import goat.minecraft.minecraftnew.utils.devtools.SkinManager;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

/**
 * Removes an item's skin when right-clicking a water cauldron.
 */
public class SkinRemovalCauldron implements Listener {

    @EventHandler
    public void onCauldronUse(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (event.getHand() != EquipmentSlot.HAND) return;

        Block block = event.getClickedBlock();
        if (block == null || block.getType() != Material.WATER_CAULDRON) return;

        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null) return;

        // Attempt to remove skin information
        int before = (item.hasItemMeta() && item.getItemMeta().hasLore()) ? item.getItemMeta().getLore().size() : 0;
        SkinManager.removeSkin(item);
        int after = (item.hasItemMeta() && item.getItemMeta().hasLore()) ? item.getItemMeta().getLore().size() : 0;

        if (after < before) {
            player.sendMessage(ChatColor.GREEN + "Skin removed from item!");
        }
    }
}
