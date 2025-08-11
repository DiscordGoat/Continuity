package goat.minecraft.minecraftnew.other.arenas.champions;

import goat.minecraft.minecraftnew.MinecraftNew;
import goat.minecraft.minecraftnew.subsystems.fishing.Rarity;
import goat.minecraft.minecraftnew.subsystems.gravedigging.corpses.CorpseTrait;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.npc.NPCRegistry;
import net.citizensnpcs.api.npc.MemoryNPCDataStore;
import net.citizensnpcs.api.trait.trait.Equipment;
import net.citizensnpcs.api.trait.trait.Inventory;
import net.citizensnpcs.trait.SkinTrait;
import org.bukkit.*;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.Set;

/**
 * Utility for spawning champions at a given location with phase-based AI.
 */
public final class ChampionSpawner {

    private ChampionSpawner() {
    }

    /**
     * Spawns the given champion type at the supplied location with ChampionTrait AI.
     *
     * @param type the champion definition
     * @param loc  where to spawn the champion
     */
    public static void spawnChampion(ChampionType type, Location loc) {
        JavaPlugin plugin = MinecraftNew.getInstance();
        World world = loc.getWorld();
        if (world == null) {
            return;
        }

        // a: particle explosion
        world.spawnParticle(Particle.EXPLOSION, loc, 1);
        // b: dragon roar sound
        world.playSound(loc, Sound.ENTITY_ENDER_DRAGON_GROWL, 1f, 1f);

        NPCRegistry registry = CitizensAPI.createAnonymousNPCRegistry(new MemoryNPCDataStore());
        NPC npc = registry.createNPC(EntityType.PLAYER, type.getName());

        // 1: Set name
        npc.setName(type.getName());
        SkinTrait skin = npc.getOrAddTrait(SkinTrait.class);
        skin.setFetchDefaultSkin(false);
        skin.setShouldUpdateSkins(false);
        skin.setSkinPersistent("champion", type.getSkinSig(), type.getSkinValue());
        npc.spawn(loc);
        if (npc.getEntity() instanceof Player player) {
            player.setCustomName(type.getName());
            player.setCustomNameVisible(true);

            // Champion starts in STATUE phase - ChampionTrait will handle skin and equipment
            npc.setProtected(false);

            // Get blessings for this champion type
            Set<ChampionBlessing> blessings = ChampionRegistry.getBlessings(type.getName());
            
            // Add ChampionTrait - this handles all phase-based AI, equipment, and skin management
            npc.addTrait(new ChampionTrait(plugin, type, blessings));
        }
    }
}
