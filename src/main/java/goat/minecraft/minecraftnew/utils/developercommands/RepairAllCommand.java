package goat.minecraft.minecraftnew.utils.developercommands;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * Developer command that repairs all durable items in a player's inventory.
 * Requires the "continuity.admin" permission.
 */
public class RepairAllCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command.");
            return true;
        }

        if (!player.hasPermission("continuity.admin")) {
            player.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
            return true;
        }

        for (ItemStack item : player.getInventory().getContents()) {
            if (item == null || item.getType() == Material.AIR) {
                continue;
            }
            ItemMeta meta = item.getItemMeta();
            if (meta instanceof Damageable damageable && damageable.getDamage() > 0) {
                CustomDurabilityManager.getInstance().repairFully(item);
            }
        }

        player.sendMessage(ChatColor.GREEN + "\u2714 All items repaired to full durability!");
        return true;
    }
}
