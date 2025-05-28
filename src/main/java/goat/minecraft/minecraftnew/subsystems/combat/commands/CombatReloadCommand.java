package goat.minecraft.minecraftnew.subsystems.combat.commands;

import goat.minecraft.minecraftnew.subsystems.combat.CombatSubsystemManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

/**
 * Command to reload the combat subsystem configuration.
 * Useful for testing configuration changes without restarting the server.
 */
public class CombatReloadCommand implements CommandExecutor {
    
    private final CombatSubsystemManager combatManager;
    
    public CombatReloadCommand(CombatSubsystemManager combatManager) {
        this.combatManager = combatManager;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("continuity.admin")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
            return true;
        }
        
        try {
            combatManager.reload();
            sender.sendMessage(ChatColor.GREEN + "Combat configuration reloaded successfully!");
            sender.sendMessage(ChatColor.GRAY + "Changes to damage multipliers, notifications, and hostility settings are now active.");
            
        } catch (Exception e) {
            sender.sendMessage(ChatColor.RED + "Failed to reload combat configuration: " + e.getMessage());
            sender.sendMessage(ChatColor.YELLOW + "Check the server console for more details.");
        }
        
        return true;
    }
}