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

/**
 * Utility for spawning champions at a given location.
 */
public final class ChampionSpawner {

    private ChampionSpawner() {
    }

    /**
     * Spawns the given champion type at the supplied location.
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
            // 2: Set skin

            // 3: Set armor contents via Citizens inventory trait
            ChampionEquipmentUtil.setArmorContentsFromFile(plugin, npc, type.getArmorFile());
            npc.setProtected(false);
            ItemStack sword = ChampionEquipmentUtil.getItemFromFile(plugin, type.getSwordFile());

            Bukkit.getScheduler().runTask(plugin, () -> {
                Equipment eq = npc.getOrAddTrait(Equipment.class);
                ItemStack[] armor = ChampionEquipmentUtil.getArmorForEquipment(plugin, type.getArmorFile());
                eq.set(Equipment.EquipmentSlot.HELMET,     armor[0]);
                eq.set(Equipment.EquipmentSlot.CHESTPLATE, armor[1]);
                eq.set(Equipment.EquipmentSlot.LEGGINGS,   armor[2]);
                eq.set(Equipment.EquipmentSlot.BOOTS,      armor[3]);
            });

// main hand (if you still want it)
            ChampionEquipmentUtil.setHeldItemFromFile(plugin, (Player) npc.getEntity(), type.getSwordFile());
        }

        // 5: set corpse trait (placeholder)
        npc.addTrait(new CorpseTrait(plugin, Rarity.COMMON, false, 0));
    }
}
