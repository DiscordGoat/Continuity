package goat.minecraft.minecraftnew.subsystems.corpses;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.npc.NPCRegistry;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Optional;

/**
 * Developer command to spawn a Corpse NPC by name.
 */
public class SpawnCorpseCommand implements CommandExecutor {
    private final JavaPlugin plugin;

    public SpawnCorpseCommand(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "Only players may use this command.");
            return true;
        }
        if (!player.hasPermission("continuity.admin")) {
            player.sendMessage(ChatColor.RED + "You do not have permission to use this command!");
            return true;
        }
        if (args.length != 1) {
            player.sendMessage(ChatColor.RED + "Usage: /spawncorpse <name>");
            return true;
        }
        String name = args[0].replace("_", " ");
        Optional<Corpse> optional = CorpseRegistry.getCorpseByName(name);
        if (!optional.isPresent()) {
            player.sendMessage(ChatColor.RED + "Corpse " + name + " not found.");
            return true;
        }
        spawnCorpse(optional.get(), player);
        return true;
    }

    private void spawnCorpse(Corpse corpse, Player player) {
        NPCRegistry registry = CitizensAPI.getNPCRegistry();
        NPC npc = registry.createNPC(EntityType.PLAYER, corpse.getDisplayName());
        npc.spawn(player.getLocation());
        npc.setPersistent(false); // ensure corpses are not saved between restarts

        if (npc.getEntity() instanceof org.bukkit.entity.LivingEntity le) {
            EntityEquipment eq = le.getEquipment();
            if (eq != null && corpse.getWeaponMaterial() != null && corpse.getWeaponMaterial() != org.bukkit.Material.AIR) {
                eq.setItemInMainHand(new ItemStack(corpse.getWeaponMaterial()));
            }
        }
        npc.data().setPersistent(NPC.DEFAULT_PROTECTED_METADATA, false);
        npc.addTrait(new CorpseTrait(plugin, corpse.getLevel(), corpse.usesBow(),
                corpse.getDisplayName().equalsIgnoreCase("Duskblood") ? 100 : 0));
        npc.getEntity().setCustomName(ChatColor.GRAY + "[Lv: " + corpse.getLevel() + "] " + corpse.getDisplayName());
        npc.getEntity().setCustomNameVisible(true);
        npc.getEntity().setMetadata("CORPSE", new FixedMetadataValue(plugin, corpse.getDisplayName()));
    }
}
