package goat.minecraft.minecraftnew.utils.developercommands;

import goat.minecraft.minecraftnew.subsystems.pets.PetManager;
import goat.minecraft.minecraftnew.subsystems.pets.PetRegistry;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class GrantGhostPetCommand implements CommandExecutor {

    private final PetManager petManager;

    public GrantGhostPetCommand(PetManager petManager) {
        this.petManager = petManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("continuity.admin")) {
            sender.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
            return true;
        }

        if (args.length != 1) {
            sender.sendMessage(ChatColor.RED + "Usage: /grantGhostPet <player>");
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Player not found.");
            return true;
        }

        PetManager.Pet ghost = PetRegistry.getPetByName("Ghost", petManager);
        if (ghost == null) {
            sender.sendMessage(ChatColor.RED + "Ghost pet not registered.");
            return true;
        }

        petManager.addPet(target, ghost);
        sender.sendMessage(ChatColor.GREEN + "Granted Ghost pet to " + target.getName() + ".");
        target.sendMessage(ChatColor.DARK_RED + "An Admin Ghost pet has been granted to you!");
        return true;
    }
}
