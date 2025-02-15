package goat.minecraft.minecraftnew.utils;

import goat.minecraft.minecraftnew.utils.XPManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Command: /testskill <skill> <newLevel>
 *
 * Simulates a level-up event for debugging the chat message in XPManager.
 */
public class TestSkillMessageCommand implements CommandExecutor {

    private final XPManager xpManager;

    public TestSkillMessageCommand(XPManager xpManager) {
        this.xpManager = xpManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command!");
            return true;
        }

        Player player = (Player) sender;

        // /testskill <skill> <newLevel>
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Usage: /testskill <skill> <newLevel>");
            return true;
        }

        String skill = args[0];
        int newLevel;
        try {
            newLevel = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            player.sendMessage(ChatColor.RED + "Please specify a valid integer for <newLevel>!");
            return true;
        }

        if (newLevel < 1) {
            player.sendMessage(ChatColor.RED + "New level must be >= 1!");
            return true;
        }

        int oldLevel = newLevel - 1;
        // We now simulate calling the same method used when you really level up:
        xpManager.sendSkillLevelUpMessage(player, skill, newLevel);

        // Optionally let the user know we ran the test:
        player.sendMessage(ChatColor.GRAY + "[Debug] Simulated leveling from " + oldLevel
                + " to " + newLevel + " in " + skill + ".");
        return true;
    }
}
