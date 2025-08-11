package goat.minecraft.minecraftnew.other.arenas.champions;

import goat.minecraft.minecraftnew.MinecraftNew;
import goat.minecraft.minecraftnew.subsystems.fishing.Rarity;
import goat.minecraft.minecraftnew.subsystems.gravedigging.corpses.CorpseTrait;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.npc.NPCRegistry;
import net.citizensnpcs.api.npc.MemoryNPCDataStore;
import net.citizensnpcs.trait.SkinTrait;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

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
        npc.spawn(loc);
        if (npc.getEntity() instanceof Player player) {
            player.setCustomName(type.getName());
            player.setCustomNameVisible(true);
            // 2: Set skin
            npc.getOrAddTrait(SkinTrait.class)
                    .setSkinPersistent("champion", type.getSkinSig(), type.getSkinValue());
            // 3: Set armor contents
            ChampionEquipmentUtil.setArmorContentsFromFile(plugin, player, type.getArmorFile());
            // 4: set held item as the sword
            ChampionEquipmentUtil.setHeldItemFromFile(plugin, player, type.getSwordFile());
        }

        // 5: set corpse trait (placeholder)
        npc.addTrait(new CorpseTrait(plugin, Rarity.COMMON, false, 0));
    }
}
