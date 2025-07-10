package goat.minecraft.minecraftnew.subsystems.corpses;

import goat.minecraft.minecraftnew.MinecraftNew;
import goat.minecraft.minecraftnew.subsystems.combat.SpawnMonsters;
import goat.minecraft.minecraftnew.utils.devtools.XPManager;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.npc.NPCRegistry;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.Optional;

public class SpawnCorpseCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command!");
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
        String corpseName = args[0].replace("_", " ");
        Optional<Corpse> optionalCorpse = CorpseRegistry.getCorpseByName(corpseName);
        if (optionalCorpse.isEmpty()) {
            player.sendMessage(ChatColor.RED + "Corpse with name '" + corpseName + "' not found!");
            return true;
        }
        Corpse corpse = optionalCorpse.get();
        NPCRegistry registry = CitizensAPI.getNPCRegistry();
        NPC npc = registry.createNPC(org.bukkit.entity.EntityType.PLAYER, corpse.getName());
        npc.spawn(player.getLocation());
        npc.setProtected(false);
        npc.setName(ChatColor.GRAY + "[Lvl " + corpse.getLevel() + "] " + corpse.getName());
        npc.data().setPersistent(false);
        npc.addTrait(new CorpseTrait(MinecraftNew.getInstance(), corpse));

        EntityEquipment eq = ((Player) npc.getEntity()).getEquipment();
        if (eq != null && corpse.getWeapon() != Material.AIR) {
            eq.setItemInMainHand(new ItemStack(corpse.getWeapon()));
        }

        npc.getEntity().setMetadata("CORPSE", new FixedMetadataValue(MinecraftNew.getInstance(), corpse.getName()));

        SpawnMonsters.getInstance(new XPManager(MinecraftNew.getInstance())).applyMobAttributes((Player) npc.getEntity(), corpse.getLevel());
        player.sendMessage(ChatColor.GREEN + "Spawned " + corpse.getName());
        return true;
    }
}
