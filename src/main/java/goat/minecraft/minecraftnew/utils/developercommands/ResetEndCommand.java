package goat.minecraft.minecraftnew.utils.developercommands;

import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.Objects;

public class ResetEndCommand implements CommandExecutor {
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
        World overworld = Bukkit.getWorlds().stream()
                .filter(world -> world.getEnvironment() == World.Environment.NORMAL)
                .findFirst()
                .orElse(null);

        if (overworld == null) {
            player.sendMessage("Overworld is not available!");
            return true;
        }

        // Teleport player to Overworld before resetting The End
        player.teleport(new Location(overworld, 0, 80, 0));
        player.sendMessage("You have been teleported to The Overworld. Resetting The End...");

        Bukkit.getScheduler().runTaskLater(Bukkit.getPluginManager().getPlugin("MinecraftNew"), () -> {
            resetEndWorld();
            player.sendMessage("The End has been reset and is ready to explore again!");
        }, 100L); // Delay to allow the teleport to complete before resetting (5 seconds)

        return true;
    }

    private void resetEndWorld() {
        World endWorld = Bukkit.getWorlds().stream()
                .filter(world -> world.getEnvironment() == World.Environment.THE_END)
                .findFirst()
                .orElse(null);

        if (endWorld == null) {
            Bukkit.getLogger().warning("No End world found to reset!");
            return;
        }

        String worldName = endWorld.getName();
        File endWorldFolder = new File(Bukkit.getWorldContainer(), worldName);

        // Unload and delete The End world
        Bukkit.unloadWorld(endWorld, false);
        deleteDirectory(endWorldFolder);

        // Recreate and load The End world
        WorldCreator worldCreator = new WorldCreator(worldName);
        worldCreator.environment(World.Environment.THE_END);
        Bukkit.createWorld(worldCreator);

        Bukkit.getLogger().info("The End has been successfully reset!");
    }

    private void deleteDirectory(File file) {
        if (file.isDirectory()) {
            for (File subFile : Objects.requireNonNull(file.listFiles())) {
                deleteDirectory(subFile);
            }
        }
        file.delete();
    }
}
