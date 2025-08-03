package goat.minecraft.minecraftnew.subsystems.dragons;

import goat.minecraft.minecraftnew.MinecraftNew;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Command to remove any existing dragons in the custom End and spawn a fresh one.
 */
public class RefreshEndCommand implements CommandExecutor {

    private final MinecraftNew plugin;
    private final DragonFightManager fightManager;

    public RefreshEndCommand(MinecraftNew plugin, DragonFightManager fightManager) {
        this.plugin = plugin;
        this.fightManager = fightManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command.");
            return true;
        }

        World world = player.getWorld();
        if (!"custom_end".equalsIgnoreCase(world.getName())) {
            player.sendMessage(ChatColor.RED + "You must be in the custom End to use this command.");
            return true;
        }

        plugin.getLogger().info("[DragonAI] /refreshEnd invoked by " + player.getName());
        fightManager.refreshEnd(world);
        player.sendMessage(ChatColor.GREEN + "Spawned a new dragon in the End.");
        return true;
    }
}

