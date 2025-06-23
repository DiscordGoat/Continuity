package goat.minecraft.minecraftnew.subsystems.combat.bloodmoon;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class SimulateCommand implements CommandExecutor {

    private final WaveManager waveManager;

    public SimulateCommand(JavaPlugin plugin) {
        this.waveManager = new WaveManager(plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("This command can only be used by players.");
            return true;
        }
        if (args.length < 1) {
            player.sendMessage(ChatColor.RED + "Usage: /simulate <skirmish|clash|assault|onslaught|carnage>");
            return true;
        }
        WaveDifficulty diff;
        try {
            diff = WaveDifficulty.valueOf(args[0].toUpperCase());
        } catch (IllegalArgumentException e) {
            player.sendMessage(ChatColor.RED + "Unknown difficulty type.");
            return true;
        }
        waveManager.startSimulation(player, diff);
        player.sendMessage(ChatColor.GREEN + "Starting simulation: " + diff.name().toLowerCase());
        return true;
    }
}
