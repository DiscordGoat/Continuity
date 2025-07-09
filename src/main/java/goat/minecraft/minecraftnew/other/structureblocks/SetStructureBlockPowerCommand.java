package goat.minecraft.minecraftnew.other.structureblocks;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class SetStructureBlockPowerCommand implements CommandExecutor {

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

        if (args.length != 1) {
            player.sendMessage(ChatColor.RED + "Usage: /setstructureblockpower <power>");
            return true;
        }

        int power;
        try {
            power = Integer.parseInt(args[0]);
        } catch (NumberFormatException e) {
            player.sendMessage(ChatColor.RED + "Invalid number!");
            return true;
        }

        if (power < 0 || power > 10000) {
            player.sendMessage(ChatColor.RED + "Power must be between 0 and 10,000!");
            return true;
        }

        ItemStack item = player.getInventory().getItemInMainHand();
        StructureBlockManager manager = StructureBlockManager.getInstance();
        if (manager == null) {
            player.sendMessage(ChatColor.RED + "StructureBlockManager not initialized!");
            return true;
        }

        if (!manager.isStructureBlock(item)) {
            player.sendMessage(ChatColor.RED + "You must be holding a Structure Block Charm!");
            return true;
        }

        manager.setStructureBlockPower(item, power);
        player.sendMessage(ChatColor.GREEN + "Structure Block power set to " + power + ".");
        return true;
    }
}
