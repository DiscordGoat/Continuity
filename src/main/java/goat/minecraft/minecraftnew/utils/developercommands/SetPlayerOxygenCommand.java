package goat.minecraft.minecraftnew.utils.developercommands;

import goat.minecraft.minecraftnew.subsystems.mining.PlayerOxygenManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SetPlayerOxygenCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Ensure the sender is a player.
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be used by a player.");
            return true;
        }
        Player player = (Player) sender;

        // Check permission.
        if (!player.hasPermission("continuity.admin")) {
            player.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
            return true;
        }

        // Validate the arguments.
        if (args.length != 1) {
            player.sendMessage(ChatColor.YELLOW + "Usage: /setplayeroxygen <amount>");
            return true;
        }

        int oxygenAmount;
        try {
            oxygenAmount = Integer.parseInt(args[0]);
        } catch (NumberFormatException e) {
            player.sendMessage(ChatColor.RED + "The oxygen amount must be a valid integer.");
            return true;
        }

        // Optional: disallow negative values.
        if (oxygenAmount < 0) {
            player.sendMessage(ChatColor.RED + "The oxygen amount cannot be negative.");
            return true;
        }

        // Retrieve the oxygen manager via its singleton instance.
        PlayerOxygenManager oxygenManager = PlayerOxygenManager.getInstance();

        // Option 1: Set absolute oxygen value (similar to your code in the command version).
        oxygenManager.setPlayerOxygenLevel(player, oxygenAmount);

        // Option 2: Alternatively, if you wish to add oxygen (like in the Deep Sea Diver code), you could do:
        // int currentOxygen = oxygenManager.getPlayerOxygen(player);
        // oxygenManager.setPlayerOxygenLevel(player, currentOxygen + oxygenAmount);

        return true;
    }
}
