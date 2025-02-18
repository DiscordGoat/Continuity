package goat.minecraft.minecraftnew.utils.developercommands;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class EndCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be used by a player!");
            return true;
        }

        Player player = (Player) sender;
        World endWorld = Bukkit.getWorlds().stream()
                .filter(world -> world.getEnvironment() == World.Environment.THE_END)
                .findFirst()
                .orElse(null);

        if (endWorld == null) {
            player.sendMessage("The End world is not available!");
            return true;
        }

        player.teleport(new Location(endWorld, 0, 80, 0)); // Safe spawn height
        player.sendMessage("You have been teleported to The End!");
        return true;
    }
}
