package goat.minecraft.minecraftnew.utils.developercommands;

import goat.minecraft.minecraftnew.subsystems.farming.VerdantRelicsSubsystem;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class SetVerdantRelicGrowthPercentageCommand implements CommandExecutor {
    private final VerdantRelicsSubsystem subsystem;

    public SetVerdantRelicGrowthPercentageCommand(JavaPlugin plugin, VerdantRelicsSubsystem subsystem) {
        this.subsystem = subsystem;
        plugin.getCommand("setverdantrelicgrowthpercentage").setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }
        Player player = (Player) sender;
        if (!player.hasPermission("continuity.admin")) {
            player.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
            return true;
        }
        if (args.length != 1) {
            player.sendMessage(ChatColor.RED + "Usage: /setverdantrelicgrowthpercentage <1-100>");
            return true;
        }
        try {
            int percent = Integer.parseInt(args[0]);
            if (percent < 1 || percent > 100) {
                player.sendMessage(ChatColor.RED + "Percentage must be between 1 and 100.");
                return true;
            }
            subsystem.setAllRelicsGrowthPercentage(percent);
            player.sendMessage(ChatColor.GREEN + "Set all Verdant relics to " + percent + "% grown.");
        } catch (NumberFormatException e) {
            player.sendMessage(ChatColor.RED + "Invalid number.");
        }
        return true;
    }
}
