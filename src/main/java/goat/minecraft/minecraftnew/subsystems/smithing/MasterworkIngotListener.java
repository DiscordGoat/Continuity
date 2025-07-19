package goat.minecraft.minecraftnew.subsystems.smithing;

import goat.minecraft.minecraftnew.other.durability.CustomDurabilityManager;
import goat.minecraft.minecraftnew.utils.devtools.ItemRegistry;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class MasterworkIngotListener implements Listener {
    private boolean isMasterworkIngot(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        ItemMeta meta = item.getItemMeta();
        return meta.hasDisplayName() && ChatColor.stripColor(meta.getDisplayName()).equals("Masterwork Ingot");
    }

    @EventHandler
    public void onUse(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        Player player = event.getPlayer();
        ItemStack ingot = player.getInventory().getItemInMainHand();
        if (!isMasterworkIngot(ingot)) return;

        ItemStack target = player.getInventory().getItemInOffHand();
        if (target == null || target.getType() == Material.AIR) return;

        CustomDurabilityManager mgr = CustomDurabilityManager.getInstance();
        if (mgr != null) {
            mgr.addMaxDurabilityBonus(target, 10);
            mgr.repairFully(target);
        }

        ingot.setAmount(ingot.getAmount() - 1);
        player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 1.0f, 1.0f);
        event.setCancelled(true);
    }
}
