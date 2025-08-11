package goat.minecraft.minecraftnew.utils.developercommands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

/**
 * Saves the executing player's armor contents to a YAML file in the plugin's
 * champions directory. Usage: /savearmorcontents <filename>
 */
public class SaveArmorContentsCommand implements CommandExecutor {

    private final JavaPlugin plugin;

    public SaveArmorContentsCommand(JavaPlugin plugin) {
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
        dir.mkdirs();
        File file = new File(dir, args[0] + ".yml");

        YamlConfiguration config = new YamlConfiguration();
        ItemStack[] armor = player.getInventory().getArmorContents();
        config.set("armor", Arrays.asList(armor));
        try {
            config.save(file);
            player.sendMessage(ChatColor.GREEN + "Armor saved to " + file.getName());
        } catch (IOException e) {
            player.sendMessage(ChatColor.RED + "Failed to save armor: " + e.getMessage());
        }
        return true;
    }
}
