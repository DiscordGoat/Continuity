package goat.minecraft.minecraftnew.subsystems.mining;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

/**
 * Listener that removes the Fortune enchantment from a player's
 * tool whenever they break a block.
 */
public class FortuneRemover implements Listener {

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        ItemStack tool = player.getInventory().getItemInMainHand();
        if (tool != null && tool.containsEnchantment(Enchantment.LOOT_BONUS_BLOCKS)) {
            tool.removeEnchantment(Enchantment.LOOT_BONUS_BLOCKS);
        }
    }
}
