package goat.minecraft.minecraftnew.subsystems.combat.bloodmoon;

import org.bukkit.Location;
import org.bukkit.entity.Monster;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;

/**
 * Listener to update morale and cleanup when wave mobs die.
 */
public class WaveBehaviorListener implements Listener {

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        if (event.getEntity() instanceof Monster mob) {
            Location loc = mob.getLocation();
            WaveBehaviorManager.unregister(mob);
            if (mob.getCustomName() != null && mob.getCustomName().contains("Captain")) {
                if (event.getEntity().getKiller() != null) {
                    WaveBehaviorManager.onCaptainDeath(event.getEntity().getKiller(), loc);
                } else {
                    WaveBehaviorManager.onCaptainDeath(null, loc);
                }
            }
        }
    }
}
