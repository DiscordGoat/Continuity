package goat.minecraft.minecraftnew.other.arenas.champions;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Dev command to spawn a champion by name.
 */
public class SpawnChampionCommand implements CommandExecutor {

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
            player.sendMessage(ChatColor.RED + "Usage: /spawnchampion <name>");
            return true;
        }
        String name = args[0].replace("_", " ");
        ChampionType type = ChampionRegistry.getChampion(name);
        if (type == null) {
            player.sendMessage(ChatColor.RED + "Champion with name '" + name + "' not found!");
            return true;
        }
        ChampionSpawner.spawnChampion(type, player.getLocation());
        player.sendMessage(ChatColor.GREEN + "Spawned champion " + type.getName() + ".");
        return true;
    }
}
