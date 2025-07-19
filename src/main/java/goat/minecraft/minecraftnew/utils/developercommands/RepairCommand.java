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

public class RepairCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command.");
            return true;
        }

        Player player = (Player) sender;
        
        if (!player.hasPermission("continuity.admin")) {
            player.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
            return true;
        }

        ItemStack heldItem = player.getInventory().getItemInMainHand();
        
        if (heldItem == null || heldItem.getType() == Material.AIR) {
            player.sendMessage(ChatColor.RED + "You must be holding an item to repair!");
            return true;
        }

        ItemMeta meta = heldItem.getItemMeta();
        if (!(meta instanceof Damageable)) {
            player.sendMessage(ChatColor.RED + "This item cannot be repaired!");
            return true;
        }

        Damageable damageable = (Damageable) meta;

        if (damageable.getDamage() == 0) {
            player.sendMessage(ChatColor.YELLOW + "This item is already at full durability!");
            return true;
        }

        CustomDurabilityManager.getInstance().repairFully(heldItem);

        player.sendMessage(ChatColor.GREEN + "✔ Item repaired to full durability!");
        return true;
    }
}