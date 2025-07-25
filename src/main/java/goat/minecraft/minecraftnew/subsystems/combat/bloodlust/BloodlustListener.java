package goat.minecraft.minecraftnew.subsystems.combat.bloodlust;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.entity.Player;
import org.bukkit.entity.Monster;

/**
 * Listener that connects combat events to the bloodlust manager.
 */
public class BloodlustListener implements Listener {

    private final BloodlustManager manager;

    public BloodlustListener(BloodlustManager manager) {
        this.manager = manager;
    }

    @EventHandler
    public void onKill(EntityDeathEvent event) {
        if (event.getEntity() instanceof Monster && event.getEntity().getKiller() instanceof Player killer) {
            manager.handleKill(killer);
        }
    }

    @EventHandler
    public void onHit(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player player && event.getEntity() instanceof org.bukkit.entity.LivingEntity target) {
            manager.handleHit(player, target);
        }
    }
}
