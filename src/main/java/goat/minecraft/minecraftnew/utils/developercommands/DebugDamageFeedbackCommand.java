package goat.minecraft.minecraftnew.utils.developercommands;

import goat.minecraft.minecraftnew.subsystems.combat.DamageDebugManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Developer command that toggles detailed damage debug messages for the player.
 */
public class DebugDamageFeedbackCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }
        boolean enabled = DamageDebugManager.toggle(player);
        player.sendMessage(ChatColor.YELLOW + "Damage debug feedback " + (enabled ? "enabled" : "disabled") + ".");
        return true;
    }
}

