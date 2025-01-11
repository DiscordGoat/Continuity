package goat.minecraft.minecraftnew.other;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.plugin.java.JavaPlugin;

public class ArmorStandCommand implements CommandExecutor {
    private JavaPlugin plugin;

    public ArmorStandCommand(JavaPlugin plugin) {
        this.plugin = plugin;
        // Register the command in the plugin
        plugin.getCommand("removeinvisiblestands").setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("removeinvisiblestands")) {
            removeInvisibleArmorStands();
            sender.sendMessage("All invisible armor stands have been removed.");
            return true;
        }
        return false;
    }

    private void removeInvisibleArmorStands() {
        // Assuming you want to remove armor stands in a specific world
        plugin.getServer().getWorlds().forEach(world -> {
            world.getEntities().stream()
                    .filter(entity -> entity.getType() == EntityType.ARMOR_STAND)
                    .map(entity -> (ArmorStand) entity)
                    .filter(ArmorStand::isInvisible)
                    .forEach(ArmorStand::remove);
        });
    }
}
