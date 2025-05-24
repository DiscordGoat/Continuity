package goat.minecraft.minecraftnew.utils.developercommands;

import goat.minecraft.minecraftnew.utils.devtools.SchemManager;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Delegates the actual paste logic to SchemManager#placeStructure(...)
 */
public class PasteSchemCommand implements CommandExecutor {
    private final SchemManager schemManager;

    public PasteSchemCommand(JavaPlugin plugin) {
        this.schemManager = new SchemManager(plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can run this.");
            return true;
        }
        if (args.length != 1) {
            sender.sendMessage("Usage: /pasteSchem <name>");
            return true;
        }

        Player player = (Player) sender;
        String name = args[0];
        Location loc = player.getLocation();

        schemManager.placeStructure(name, loc);
        player.sendMessage("§aAttempted to paste schematic \"" + name + "\" at your location.");
        return true;
    }
}
