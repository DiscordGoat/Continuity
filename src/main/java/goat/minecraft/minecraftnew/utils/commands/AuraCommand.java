package goat.minecraft.minecraftnew.utils.commands;

import goat.minecraft.minecraftnew.subsystems.auras.AuraManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Player command to toggle visibility of their active aura.
 */
public class AuraCommand implements CommandExecutor {
    private final AuraManager auraManager;

    public AuraCommand(AuraManager auraManager) {
        this.auraManager = auraManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command.");
            return true;
        }
        auraManager.toggleAura(player);
        return true;
    }
}
