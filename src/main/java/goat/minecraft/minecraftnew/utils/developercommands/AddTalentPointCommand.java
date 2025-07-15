package goat.minecraft.minecraftnew.utils.developercommands;

import goat.minecraft.minecraftnew.other.skilltree.Skill;
import goat.minecraft.minecraftnew.other.skilltree.SkillTreeManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class AddTalentPointCommand implements CommandExecutor {
    private final JavaPlugin plugin;
    private final SkillTreeManager manager;

    public AddTalentPointCommand(JavaPlugin plugin, SkillTreeManager manager) {
        this.plugin = plugin;
        this.manager = manager;
        plugin.getCommand("addtalentpoint").setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("continuity.admin")) {
            sender.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
            return true;
        }
        if (args.length != 3) {
            sender.sendMessage(ChatColor.RED + "Usage: /addTalentPoint <player> <skill> <amount>");
            return true;
        }
        OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Player not found.");
            return true;
        }
        Skill skill = Skill.fromDisplay(args[1]);
        if (skill == null) {
            sender.sendMessage(ChatColor.RED + "Unknown skill.");
            return true;
        }
        int amount;
        try {
            amount = Integer.parseInt(args[2]);
        } catch (NumberFormatException e) {
            sender.sendMessage(ChatColor.RED + "Amount must be a number.");
            return true;
        }
        manager.addExtraTalentPoints(target.getUniqueId(), skill, amount);
        sender.sendMessage(ChatColor.GREEN + "Added " + amount + " talent points to " + target.getName());
        if (target.isOnline()) {
            Player p = target.getPlayer();
            if (p != null) {
                p.sendMessage(ChatColor.GREEN + "You received " + amount + " talent points for " + skill.getDisplayName());
            }
        }
        return true;
    }
}
