package goat.minecraft.minecraftnew.utils.developercommands;

import goat.minecraft.minecraftnew.utils.devtools.SkinManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class SkinCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Ensure the command sender is a player.
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be executed by a player.");
            return true;
        }
        if (!sender.hasPermission("continuity.admin")) {
            sender.sendMessage(ChatColor.RED + "You do not have permission to use this command!");
            return true;
        }

        Player player = (Player) sender;

        // Check if the skin name was provided.
        if (args.length < 1) {
            player.sendMessage(ChatColor.RED + "Usage: /" + label + " <skin_name>");
            return true;
        }

        // Join the args into a single string (in case the skin name contains spaces),
        // then replace underscores with spaces.
        String rawSkinName = String.join(" ", args);
        String skinName = rawSkinName.replace("_", " ");

        // Get the item in the player's main hand.
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null || item.getType().isAir()) {
            player.sendMessage(ChatColor.RED + "You must be holding an item to set its skin.");
            return true;
        }

        // Apply the skin to the item using the SkinManager class.
        SkinManager.setSkin(item, skinName);

        player.sendMessage(ChatColor.GREEN + "Skin applied: " + skinName);
        return true;
    }
}
