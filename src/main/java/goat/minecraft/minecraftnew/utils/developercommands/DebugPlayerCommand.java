package goat.minecraft.minecraftnew.utils.developercommands;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.npc.NPCRegistry;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.entity.EntityType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class DebugPlayerCommand implements CommandExecutor {

    private final JavaPlugin plugin;

    public DebugPlayerCommand(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players may run this command.");
            return true;
        }

        Player player = (Player) sender;
        NPCRegistry registry = CitizensAPI.getNPCRegistry();
        NPC npc = registry.createNPC(EntityType.PLAYER, "DebugHostile");

        npc.data().setPersistent(NPC.DEFAULT_PROTECTED_METADATA, false);
        npc.spawn(player.getLocation().add(1, 0, 1));

        new BukkitRunnable() {
            @Override
            public void run() {
                if (!npc.isSpawned() || !player.isOnline()) {
                    if (npc.isSpawned()) npc.destroy();
                    cancel();
                    return;
                }
                npc.getNavigator().setTarget(player, true);

                if (npc.getEntity().getLocation().distanceSquared(player.getLocation()) < 3) {
                    player.damage(2.0, npc.getEntity());
                }
            }
        }.runTaskTimer(plugin, 0L, 20L);

        player.sendMessage(ChatColor.RED + "A hostile debug player has been spawned!");
        return true;
    }
}
