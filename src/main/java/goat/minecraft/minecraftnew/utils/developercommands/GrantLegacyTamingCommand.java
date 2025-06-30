package goat.minecraft.minecraftnew.utils.developercommands;

import goat.minecraft.minecraftnew.subsystems.pets.PetManager;
import goat.minecraft.minecraftnew.utils.devtools.XPManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Map;

public class GrantLegacyTamingCommand implements CommandExecutor {
    private final XPManager xpManager;
    private final PetManager petManager;

    public GrantLegacyTamingCommand(JavaPlugin plugin, PetManager petManager, XPManager xpManager) {
        this.petManager = petManager;
        this.xpManager = xpManager;
        plugin.getCommand("grantLegacyTaming").setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("continuity.admin")) {
            sender.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
            return true;
        }
        if (args.length != 1) {
            sender.sendMessage(ChatColor.RED + "Usage: /grantLegacyTaming <player>");
            return true;
        }
        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Player not found.");
            return true;
        }
        double total = 0;
        for (PetManager.Pet pet : petManager.getPlayerPets(target).values()) {
            double xp = pet.getXp();
            for (int lvl = 1; lvl < pet.getLevel(); lvl++) {
                xp += 20 + (lvl - 1) * 2;
            }
            total += xp * 10; // Legacy rate
            if (pet.getLevel() >= 100) {
                total += 5000;
            }
        }
        if (total > 0) {
            xpManager.addXP(target, "Taming", total);
        }
        sender.sendMessage(ChatColor.GREEN + "Granted " + (int) total + " Taming XP to " + target.getName() + ".");
        return true;
    }
}
