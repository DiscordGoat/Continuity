package goat.minecraft.minecraftnew.utils.developercommands;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.npc.NPCRegistry;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Command to spawn a hostile player NPC for debugging purposes.
 */
public class DebugPlayerCommand implements CommandExecutor {

    private final JavaPlugin plugin;

    public DebugPlayerCommand(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command.");
            return true;
        }

        NPCRegistry registry = CitizensAPI.getNPCRegistry();
        NPC npc = registry.createNPC(EntityType.PLAYER, "HostileDebug");
        npc.setProtected(false);
        npc.spawn(player.getLocation().add(1, 0, 1));

        sender.sendMessage(ChatColor.GREEN + "Hostile player spawned.");
        return true;
    }
}
