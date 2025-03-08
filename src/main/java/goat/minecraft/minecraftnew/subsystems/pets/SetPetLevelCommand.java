package goat.minecraft.minecraftnew.subsystems.pets;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SetPetLevelCommand implements CommandExecutor {

    private final PetManager petManager;

    public SetPetLevelCommand(PetManager petManager) {
        this.petManager = petManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Ensure the command is run by a player
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be used by a player.");
            return true;
        }
        if (!sender.hasPermission("continuity.admin")) {
            sender.sendMessage(ChatColor.RED + "You do not have permission to use this command!");
            return true;
        }
        Player player = (Player) sender;

        // Check if the correct number of arguments is provided
        if (args.length != 1) {
            player.sendMessage(ChatColor.RED + "Usage: /setpetlevel <level>");
            return true;
        }

        int level;
        try {
            level = Integer.parseInt(args[0]);
        } catch (NumberFormatException e) {
            player.sendMessage(ChatColor.RED + "The level must be a valid number.");
            return true;
        }

        // Validate the level range
        if (level < 1 || level > 100) {
            player.sendMessage(ChatColor.RED + "The pet level must be between 1 and 100.");
            return true;
        }

        PetManager.Pet activePet = petManager.getActivePet(player);

        // Check if the player has an active pet
        if (activePet == null) {
            player.sendMessage(ChatColor.RED + "You do not have an active pet.");
            return true;
        }

        // Set the active pet's level
        activePet.setLevel(level);
        petManager.savePets(); // Ensure the new level is saved

        // Notify the player
        player.sendMessage(ChatColor.GREEN + "Your active pet has been set to level " + level + "!");

        return true;
    }
}
