package goat.minecraft.minecraftnew.subsystems.utils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
public class LoadSubsystemsCommand implements CommandExecutor {
    private final JavaPlugin plugin;
    public LoadSubsystemsCommand(JavaPlugin plugin) {
        this.plugin = plugin;
    }
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Empty command for now
        XPManager xpManager = new XPManager(plugin);
        if (command.getName().equalsIgnoreCase("loadsubsystems")) {
            Player p = (Player) sender;
            sender.sendMessage("Load subsystems command triggered. Creating Databases...");
            xpManager.createDatabase(p.getUniqueId(), "Forestry");
            sender.sendMessage("Successfully Created Forestry Subsystem");
            xpManager.createDatabase(p.getUniqueId(), "Combat");
            sender.sendMessage("Successfully Created Combat Subsystem");
            xpManager.createDatabase(p.getUniqueId(), "Player");
            sender.sendMessage("Successfully Created Player Subsystem");
            xpManager.createDatabase(p.getUniqueId(), "Mining");
            sender.sendMessage("Successfully Created Mining Subsystem");
            xpManager.createDatabase(p.getUniqueId(), "Farming");
            sender.sendMessage("Successfully Created Farming Subsystem");
            xpManager.createDatabase(p.getUniqueId(), "Fishing");
            sender.sendMessage("Successfully Created Fishing Subsystem");
            xpManager.createDatabase(p.getUniqueId(), "Coins");
            sender.sendMessage("Successfully Created Coins Subsystem");
            xpManager.createDatabase(p.getUniqueId(), "Oxygen");
            sender.sendMessage("Successfully Created Oxygen Subsystem");
            return true;
        }
        return false;
    }
}
