package goat.minecraft.minecraftnew.subsystems.corpses;

import goat.minecraft.minecraftnew.MinecraftNew;
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
import org.bukkit.metadata.FixedMetadataValue;

import java.util.Optional;

public class SpawnCorpseCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command!");
            return true;
        }
        Player player = (Player) sender;
        if (!player.hasPermission("continuity.admin")) {
            player.sendMessage(ChatColor.RED + "You do not have permission to use this command!");
            return true;
        }

        Corpse corpse;
        if (args.length == 0 || args[0].equalsIgnoreCase("random")) {
            Optional<Corpse> opt = CorpseRegistry.getRandomCorpse();
            if (!opt.isPresent()) {
                player.sendMessage(ChatColor.RED + "No corpse found.");
                return true;
            }
            corpse = opt.get();
        } else {
            String name = args[0].replace("_", " ");
            Optional<Corpse> opt = CorpseRegistry.getCorpseByName(name);
            if (!opt.isPresent()) {
                player.sendMessage(ChatColor.RED + "Corpse with name '" + name + "' not found!");
                return true;
            }
            corpse = opt.get();
        }

        NPCRegistry registry = CitizensAPI.getNPCRegistry();
        NPC npc = registry.createNPC(EntityType.PLAYER, corpse.getName());
        npc.spawn(player.getLocation());
        npc.addTrait(new CorpseTrait(MinecraftNew.getInstance(), corpse));

        if (npc.getEntity() instanceof org.bukkit.entity.LivingEntity living) {
            EntityEquipment eq = living.getEquipment();
            if (eq != null && corpse.getWeapon() != null) {
                eq.setItemInMainHand(corpse.getWeapon());
                eq.setItemInMainHandDropChance(0);
            }
            living.setCustomName(ChatColor.RED + corpse.getName());
            living.setCustomNameVisible(true);
            living.setMetadata("CORPSE", new FixedMetadataValue(MinecraftNew.getInstance(), corpse.getName()));
            living.setMetadata("CORPSE_LEVEL", new FixedMetadataValue(MinecraftNew.getInstance(), corpse.getLevel()));
        }

        player.sendMessage(ChatColor.GREEN + "Spawned corpse: " + corpse.getName());
        return true;
    }
}
