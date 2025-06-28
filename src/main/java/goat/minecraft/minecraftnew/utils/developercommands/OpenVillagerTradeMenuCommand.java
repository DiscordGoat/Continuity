package goat.minecraft.minecraftnew.utils.developercommands;

import goat.minecraft.minecraftnew.subsystems.villagers.VillagerTradeManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Locale;

/**
 * Admin command to open a villager trade menu remotely.
 * Usage: /openVillagerTradeMenu <profession> <tier>
 */
public class OpenVillagerTradeMenuCommand implements CommandExecutor {
    private final JavaPlugin plugin;

    public OpenVillagerTradeMenuCommand(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be used by players.");
            return true;
        }
        if (!player.hasPermission("continuity.admin")) {
            player.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
            return true;
        }
        if (args.length != 2) {
            player.sendMessage(ChatColor.YELLOW + "Usage: /" + label + " <profession> <tier>");
            return true;
        }
        Villager.Profession profession;
        try {
            profession = Villager.Profession.valueOf(args[0].toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            player.sendMessage(ChatColor.RED + "Unknown profession: " + args[0]);
            return true;
        }
        int tier;
        try {
            tier = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            player.sendMessage(ChatColor.RED + "Tier must be a number.");
            return true;
        }
        VillagerTradeManager.getInstance(plugin).openTradeMenu(player, profession, tier);
        return true;
    }
}
