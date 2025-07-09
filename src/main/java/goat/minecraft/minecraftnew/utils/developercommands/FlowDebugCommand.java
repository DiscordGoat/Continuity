package goat.minecraft.minecraftnew.utils.developercommands;

import goat.minecraft.minecraftnew.other.armorsets.FlowManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Debug command to display a player's current Flow level.
 */
public class FlowDebugCommand implements CommandExecutor {

    private final FlowManager flowManager;

    public FlowDebugCommand(FlowManager manager) {
        this.flowManager = manager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("This command can only be used by players.");
            return true;
        }
        int flow = flowManager.getFlow(player);
        player.sendMessage(ChatColor.YELLOW + "Current Flow: " + flow);
        return true;
    }
}
