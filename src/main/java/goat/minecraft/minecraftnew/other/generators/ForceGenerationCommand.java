package goat.minecraft.minecraftnew.other.generators;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

/**
 * Dev command to force all generators to generate items every second.
 */
public class ForceGenerationCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.isOp()) {
            sender.sendMessage(ChatColor.RED + "No permission.");
            return true;
        }
        GeneratorService service = GeneratorService.getInstance();
        if (service != null) {
            service.forceGeneration();
            sender.sendMessage(ChatColor.GREEN + "All generators set to 1s timers.");
        }
        return true;
    }
}

