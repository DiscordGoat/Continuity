package goat.minecraft.minecraftnew.utils.commands;

import goat.minecraft.minecraftnew.subsystems.combat.notification.MonsterHealthBarManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Command to toggle monster health bar display for a player.
 */
public class ToggleHealthBarsCommand implements CommandExecutor {
    private final MonsterHealthBarManager manager;

    public ToggleHealthBarsCommand(MonsterHealthBarManager manager) {
        this.manager = manager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command.");
            return true;
        }
        manager.toggle(player);
        return true;
    }
}
