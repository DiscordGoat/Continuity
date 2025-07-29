package goat.minecraft.minecraftnew.utils.commands;

import goat.minecraft.minecraftnew.other.additionalfunctionality.EnvironmentSidebarPreferences;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Toggles display of progress bars on the Environment scoreboard for the player.
 */
public class ToggleBarsCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }
        boolean enabled = EnvironmentSidebarPreferences.toggle(player);
        player.sendMessage(ChatColor.YELLOW + "Environment bars " + (enabled ? "enabled" : "disabled") + ".");
        return true;
    }
}
