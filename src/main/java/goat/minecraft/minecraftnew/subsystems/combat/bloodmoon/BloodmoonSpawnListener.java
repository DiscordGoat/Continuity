package goat.minecraft.minecraftnew.subsystems.combat.bloodmoon;

import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.PhantomPreSpawnEvent;

public class BloodmoonSpawnListener implements Listener {

    @EventHandler
    public void onPhantomSpawn(PhantomPreSpawnEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        if (event.getSpawnReason() != CreatureSpawnEvent.SpawnReason.NATURAL) return;
        EntityType type = event.getEntityType();
        if (type == EntityType.ZOMBIE || type == EntityType.SKELETON ||
            type == EntityType.SPIDER || type == EntityType.CREEPER) {
            World world = event.getLocation().getWorld();
            if (world != null) {
                long time = world.getTime() % 24000;
                if (time >= 13000 && time <= 23000) {
                    event.setCancelled(true);
                }
            }
        }
    }
}
