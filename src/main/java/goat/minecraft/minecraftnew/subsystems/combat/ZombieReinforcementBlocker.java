package goat.minecraft.minecraftnew.subsystems.combat;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.entity.Zombie;

/**
 * Prevents zombies from spawning reinforcements.
 */
public class ZombieReinforcementBlocker implements Listener {
    @EventHandler
    public void onZombieReinforcementSpawn(CreatureSpawnEvent event) {
        if (event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.REINFORCEMENTS
                && event.getEntity() instanceof Zombie) {
            event.setCancelled(true);
        }
    }
}
