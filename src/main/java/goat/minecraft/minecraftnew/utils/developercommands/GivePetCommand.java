package goat.minecraft.minecraftnew.utils.developercommands;

import goat.minecraft.minecraftnew.MinecraftNew;
import goat.minecraft.minecraftnew.subsystems.pets.PetManager;
import goat.minecraft.minecraftnew.subsystems.pets.PetRegistry;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class GivePetCommand implements CommandExecutor, TabCompleter {

    private final MinecraftNew plugin;
    private final PetManager petManager;

    public GivePetCommand(MinecraftNew plugin) {
        this.plugin = plugin;
        this.petManager = PetManager.getInstance(plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("continuity.admin")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
            return true;
        }

        if (args.length < 3) {
            sender.sendMessage(ChatColor.RED + "Usage: /givepet <petname> <player> <level>");
            return true;
        }

        String petName = args[0];
        String playerName = args[1];
        String levelStr = args[2];

        Player targetPlayer = Bukkit.getPlayer(playerName);
        if (targetPlayer == null) {
            sender.sendMessage(ChatColor.RED + "Player '" + playerName + "' not found or not online.");
            return true;
        }

        int level;
        try {
            level = Integer.parseInt(levelStr);
            if (level < 1 || level > 100) {
                sender.sendMessage(ChatColor.RED + "Level must be between 1 and 100.");
                return true;
            }
        } catch (NumberFormatException e) {
            sender.sendMessage(ChatColor.RED + "Invalid level: " + levelStr + ". Must be a number.");
            return true;
        }

        PetManager.Pet pet = PetRegistry.getPetByName(petName, petManager);
        if (pet == null) {
            sender.sendMessage(ChatColor.RED + "Invalid pet name: " + petName);
            sender.sendMessage(ChatColor.YELLOW + "Available pets: " + getAvailablePetNames());
            return true;
        }

        if (level > pet.getMaxLevel()) {
            sender.sendMessage(ChatColor.RED + "Level " + level + " exceeds max level " + pet.getMaxLevel() + " for " + petName);
            return true;
        }

        pet.setLevel(level);
        petManager.addPet(targetPlayer, pet);

        sender.sendMessage(ChatColor.GREEN + "Successfully gave " + targetPlayer.getName() + 
                          " a level " + level + " " + petName + " pet!");
        targetPlayer.sendMessage(ChatColor.GREEN + "You received a level " + level + " " + 
                                 pet.getRarity().getColor() + petName + ChatColor.GREEN + " pet!");

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            completions.addAll(getAvailablePetNamesList());
            return completions.stream()
                    .filter(name -> name.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        } else if (args.length == 2) {
            completions.addAll(Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .collect(Collectors.toList()));
            return completions.stream()
                    .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
        } else if (args.length == 3) {
            completions.add("1");
            completions.add("10");
            completions.add("25");
            completions.add("50");
            completions.add("100");
            return completions.stream()
                    .filter(level -> level.startsWith(args[2]))
                    .collect(Collectors.toList());
        }

        return completions;
    }

    private String getAvailablePetNames() {
        List<String> petNames = getAvailablePetNamesList();
        return String.join(", ", petNames);
    }

    private List<String> getAvailablePetNamesList() {
        List<String> petNames = new ArrayList<>();
        
        petNames.add("Villager");
        petNames.add("Golden Steve");
        petNames.add("Leviathan");
        petNames.add("Turtle");
        petNames.add("Dolphin");
        petNames.add("Glow Squid");
        petNames.add("Fish");
        petNames.add("Stray");
        petNames.add("Guardian");
        petNames.add("Skeleton");
        petNames.add("Zombie Pigman");
        petNames.add("Enderman");
        petNames.add("Blaze");
        petNames.add("Wither Skeleton");
        petNames.add("Cat");
        petNames.add("Yeti");
        petNames.add("Axolotl");
        petNames.add("Zombie");
        petNames.add("Iron Golem");
        petNames.add("Bat");
        petNames.add("Warden");
        petNames.add("Dwarf");
        petNames.add("Armadillo");
        petNames.add("Drowned");
        petNames.add("Parrot");
        petNames.add("Allay");
        petNames.add("Horse");
        petNames.add("Piglin Brute");
        petNames.add("Vindicator");
        petNames.add("Ent");
        petNames.add("Monkey");
        petNames.add("Raccoon");
        petNames.add("Spider");
        petNames.add("Pig");
        petNames.add("Mooshroom");
        petNames.add("Cow");
        petNames.add("Sheep");
        petNames.add("Squirrel");
        petNames.add("Ghost");
        petNames.add("Wither");
        
        return petNames;
    }
}