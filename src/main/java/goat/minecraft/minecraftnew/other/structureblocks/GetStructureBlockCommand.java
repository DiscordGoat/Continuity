package goat.minecraft.minecraftnew.other.structureblocks;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class GetStructureBlockCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be used by players!");
            return true;
        }

        if (!player.hasPermission("continuity.admin")) {
            player.sendMessage(ChatColor.RED + "You don't have permission to use this command!");
            return true;
        }

        StructureBlockManager manager = StructureBlockManager.getInstance();
        if (manager == null) {
            player.sendMessage(ChatColor.RED + "StructureBlockManager not initialized!");
            return true;
        }

        ItemStack block = manager.createStructureBlock();
        player.getInventory().addItem(block);
        player.sendMessage(ChatColor.GREEN + "You have received a Structure Block Charm.");
        return true;
    }
}
