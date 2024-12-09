package goat.minecraft.minecraftnew.subsystems.pets;

import goat.minecraft.minecraftnew.subsystems.pets.PetManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.UUID;

public class ClearPetsCommand implements CommandExecutor {

    private JavaPlugin plugin;
    private PetManager petManager;

    public ClearPetsCommand(JavaPlugin plugin, PetManager petManager) {
        this.plugin = plugin;
        this.petManager = petManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        // Check if sender has permission
        if (!sender.hasPermission("pets.clear")) {
            sender.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
            return true;
        }

        // Determine target player
        Player target;
        if (args.length == 0) {
            // No arguments; target is the sender if they are a player
            if (!(sender instanceof Player)) {
                sender.sendMessage(ChatColor.RED + "You must specify a player.");
                return true;
            }
            target = (Player) sender;
        } else if (args.length == 1) {
            // Target is the specified player
            target = Bukkit.getPlayerExact(args[0]);
            if (target == null) {
                sender.sendMessage(ChatColor.RED + "Player '" + args[0] + "' not found.");
                return true;
            }
        } else {
            // Too many arguments
            sender.sendMessage(ChatColor.RED + "Usage: /clearpets [player]");
            return true;
        }

        UUID targetId = target.getUniqueId();

        // Remove all pets from the target
        petManager.clearPets(target);

        // Notify the sender and target
        if (target.equals(sender)) {
            sender.sendMessage(ChatColor.GREEN + "All your pets have been cleared.");
        } else {
            sender.sendMessage(ChatColor.GREEN + "All pets for player '" + target.getName() + "' have been cleared.");
            target.sendMessage(ChatColor.YELLOW + "All your pets have been cleared by " + sender.getName() + ".");
        }

        return true;
    }
}
