package goat.minecraft.minecraftnew.utils.developercommands;

import goat.minecraft.minecraftnew.other.arenas.ArenaManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

/**
 * Developer command that displays the coordinates of the nearest arena.
 * Clicking the coordinates teleports the player there and sets them to spectator mode.
 */
public class GetNearestArenaCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command!");
            return true;
        }

        // Handle teleport action
        if (args.length >= 5 && args[0].equalsIgnoreCase("tp")) {
            String worldName = args[1];
            World world = Bukkit.getWorld(worldName);
            if (world == null) {
                player.sendMessage(ChatColor.RED + "World '" + worldName + "' not found!");
                return true;
            }
            try {
                double x = Double.parseDouble(args[2]);
                double y = Double.parseDouble(args[3]);
                double z = Double.parseDouble(args[4]);
                Location target = new Location(world, x, y, z);
                player.teleport(target);
                player.setGameMode(GameMode.SPECTATOR);
                player.sendMessage(ChatColor.GREEN + "Teleported to arena.");
            } catch (NumberFormatException e) {
                player.sendMessage(ChatColor.RED + "Invalid coordinates!");
            }
            return true;
        }

        // Default behavior: show nearest arena
        ArenaManager arenaManager;
        try {
            arenaManager = ArenaManager.getInstance();
        } catch (IllegalStateException e) {
            player.sendMessage(ChatColor.RED + "Arena system is not initialized!");
            return true;
        }

        Location nearest = arenaManager.getNearestArena(player.getLocation());
        if (nearest == null) {
            player.sendMessage(ChatColor.YELLOW + "No arena found in this world.");
            return true;
        }

        String coords = String.format("%d %d %d", nearest.getBlockX(), nearest.getBlockY(), nearest.getBlockZ());
        TextComponent base = new TextComponent(ChatColor.GREEN + "Nearest arena: ");
        TextComponent coordComponent = new TextComponent(ChatColor.AQUA + coords);
        String commandStr = String.format("/getnearestarena tp %s %d %d %d",
                nearest.getWorld().getName(), nearest.getBlockX(), nearest.getBlockY(), nearest.getBlockZ());
        coordComponent.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, commandStr));
        coordComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                new ComponentBuilder("Click to teleport").color(net.md_5.bungee.api.ChatColor.YELLOW).create()));
        base.addExtra(coordComponent);
        player.spigot().sendMessage(base);
        return true;
    }
}

