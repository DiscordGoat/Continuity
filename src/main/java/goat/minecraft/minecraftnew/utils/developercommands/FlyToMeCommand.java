package goat.minecraft.minecraftnew.utils.developercommands;

import net.citizensnpcs.api.ai.Navigator;
import net.citizensnpcs.api.ai.NavigatorParameters;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Developer command that directs all Ender Dragons on the server to fly toward the issuer.
 * Usage: /flytome
 */
public class FlyToMeCommand implements CommandExecutor {
    private final JavaPlugin plugin;

    public FlyToMeCommand(JavaPlugin plugin) {
        this.plugin = plugin;
        plugin.getCommand("flytome").setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be used by players.");
            return true;
        }
        if (!player.hasPermission("continuity.admin")) {
            player.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
            return true;
        }

        for (World world : Bukkit.getWorlds()) {
            for (EnderDragon dragon : world.getEntitiesByClass(EnderDragon.class)) {
                dragon.setTarget(player);
                dragon.setPhase(EnderDragon.Phase.CHARGE_PLAYER);
            }
        }

        player.sendMessage(ChatColor.GRAY + "Summoning dragons to your location...");
        return true;
    }

}
