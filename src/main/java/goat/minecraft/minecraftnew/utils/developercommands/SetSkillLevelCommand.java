package goat.minecraft.minecraftnew.utils.developercommands;

import goat.minecraft.minecraftnew.utils.devtools.XPManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class SetSkillLevelCommand implements CommandExecutor {

    private final XPManager xpManager;

    public SetSkillLevelCommand(JavaPlugin plugin, XPManager xpManager) {
        this.xpManager = xpManager;
        plugin.getCommand("setskilllevel").setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("continuity.admin")) {
            sender.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
            return true;
        }

        if (args.length != 3) {
            sender.sendMessage(ChatColor.RED + "Usage: /setskilllevel <player> <skill> <level>");
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Player " + args[0] + " is not online.");
            return true;
        }

        String skill = args[1];
        int level;
        try {
            level = Integer.parseInt(args[2]);
        } catch (NumberFormatException e) {
            sender.sendMessage(ChatColor.RED + "Level must be a number.");
            return true;
        }

        if (level < 0) {
            sender.sendMessage(ChatColor.RED + "Level must be non-negative.");
            return true;
        }

        int xp = xpManager.getLevelStartXP(level);
        xpManager.setXP(target, skill, xp);

        sender.sendMessage(ChatColor.GREEN + "Set " + target.getName() + "'s " + skill + " level to " + level + ".");
        if (!sender.equals(target)) {
            target.sendMessage(ChatColor.GREEN + "Your " + skill + " level has been set to " + level + ".");
        }
        return true;
    }
}
