package goat.minecraft.minecraftnew.other.arenas.champions;

import goat.minecraft.minecraftnew.MinecraftNew;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.*;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Set;

/**
 * Utility class for spawning champion statues that will awaken into champions.
 */
public final class ChampionSpawner {

    private ChampionSpawner() {
    }

    /**
     * Spawns a statue for the given champion type at the supplied location.
     * The statue will awaken into the actual champion when a player approaches.
     *
     * @param type the champion definition
     * @param loc  where to spawn the statue
     */
    public static void spawnChampion(ChampionType type, Location loc) {
        JavaPlugin plugin = MinecraftNew.getInstance();
        World world = loc.getWorld();
        if (world == null) {
            return;
        }

        // Particle explosion and dragon roar for dramatic effect
        world.spawnParticle(Particle.EXPLOSION, loc, 1);
        world.playSound(loc, Sound.ENTITY_ENDER_DRAGON_GROWL, 1f, 1f);

        // Create statue NPC (not the champion yet)
        NPC statueNPC = CitizensAPI.getNPCRegistry().createNPC(EntityType.PLAYER, "Ancient Statue");
        statueNPC.spawn(loc);
        
        if (statueNPC.getEntity() instanceof Player statue) {
            // Statue starts with ambiguous name (champion name revealed only on awaken)
            statue.setCustomName("Ancient Statue");
            statue.setCustomNameVisible(true);

            statueNPC.setProtected(false);

            // Get blessings for this champion type
            Set<ChampionBlessing> blessings = ChampionRegistry.getBlessings(type.getName());
            
            // Add StatueTrait - this handles statue behavior and champion spawning
            statueNPC.addTrait(new StatueTrait(plugin, type, blessings));
        }
    }
}
