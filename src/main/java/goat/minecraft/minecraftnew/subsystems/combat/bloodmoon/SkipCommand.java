package goat.minecraft.minecraftnew.subsystems.combat.bloodmoon;

import goat.minecraft.minecraftnew.MinecraftNew;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class SkipCommand implements CommandExecutor {

    private final JavaPlugin plugin;

    public SkipCommand(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("This command can only be used by players.");
            return true;
        }
        AssaultWaveManager.getInstance(plugin).skipRest();
        player.sendMessage(ChatColor.YELLOW + "Skipping rest phase.");
        return true;
    }
}
