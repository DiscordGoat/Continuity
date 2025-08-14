package goat.minecraft.minecraftnew.utils.developercommands;

import goat.minecraft.minecraftnew.utils.devtools.ChampionEquipmentUtil;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

/**
 * Loads armor contents from a YAML file in the plugin's champions directory and
 * equips them on the executing player. Usage: /equiparmorfromfile <filename>
 */
public class EquipArmorFromFileCommand implements CommandExecutor {

    private final JavaPlugin plugin;

    public EquipArmorFromFileCommand(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command!");
            return true;
        }
        if (!player.hasPermission("continuity.admin")) {
            player.sendMessage(ChatColor.RED + "You do not have permission to use this command!");
            return true;
        }
        if (args.length != 1) {
            player.sendMessage(ChatColor.RED + "Usage: /" + label + " <filename>");
            return true;
        }

        File dir = new File(plugin.getDataFolder(), "champions");
        File file = new File(dir, args[0] + ".yml");
        if (!file.exists()) {
            player.sendMessage(ChatColor.RED + "File not found: " + file.getName());
            return true;
        }

        ChampionEquipmentUtil.setArmorContentsFromFile(plugin, player, file);
        player.sendMessage(ChatColor.GREEN + "Equipped armor from " + file.getName());
        return true;
    }
}
