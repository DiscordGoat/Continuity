package goat.minecraft.minecraftnew.other.arenas.champions;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Test command to spawn Champions and verify phase-based AI functionality.
 * Usage: /testchampion <championName>
 */
public class TestChampionPhaseCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be used by players.");
            return true;
        }

        if (args.length != 1) {
            player.sendMessage(ChatColor.YELLOW + "Usage: /testchampion <championName>");
            player.sendMessage(ChatColor.GRAY + "Available champions: " + String.join(", ", ChampionRegistry.getChampionNames()));
            return true;
        }

        String championName = args[0];
        ChampionType championType = ChampionRegistry.getChampion(championName);
        
        if (championType == null) {
            player.sendMessage(ChatColor.RED + "Unknown champion: " + championName);
            player.sendMessage(ChatColor.GRAY + "Available champions: " + String.join(", ", ChampionRegistry.getChampionNames()));
            return true;
        }

        // Spawn champion at player's location
        ChampionSpawner.spawnChampion(championType, player.getLocation().add(5, 0, 0));
        
        player.sendMessage(ChatColor.GREEN + "Spawned " + championName + " with phase-based AI!");
        player.sendMessage(ChatColor.GRAY + "Champion starts in STATUE phase. Get within 15 blocks to trigger AWAKEN phase.");
        
        // Display champion's blessings
        var blessings = ChampionRegistry.getBlessings(championName);
        if (!blessings.isEmpty()) {
            player.sendMessage(ChatColor.GOLD + "Blessings: " + blessings.toString());
        }
        
        return true;
    }
}
