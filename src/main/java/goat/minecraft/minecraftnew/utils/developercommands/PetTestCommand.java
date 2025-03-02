package goat.minecraft.minecraftnew.utils.developercommands;

import goat.minecraft.minecraftnew.subsystems.pets.PetManager;
import goat.minecraft.minecraftnew.subsystems.pets.PetRegistry;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PetTestCommand implements CommandExecutor {

    private final PetManager petManager;

    public PetTestCommand(PetManager petManager) {
        this.petManager = petManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Ensure only players can run this command.
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can execute this command.");
            return true;
        }

        // Check for at least one argument.
        if (args.length < 1) {
            sender.sendMessage(ChatColor.RED + "Usage: /testpet <petName>");
            return true;
        }

        // Get the pet name from the arguments.
        String petName = args[0];
        Player player = (Player) sender;

        // Call the addPet method that creates and adds the pet.
        PetRegistry petRegistry = new PetRegistry();
        petRegistry.addPetByName(player, petName);

        return true;
    }
}
