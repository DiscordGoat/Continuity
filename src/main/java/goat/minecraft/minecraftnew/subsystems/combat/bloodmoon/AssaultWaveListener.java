package goat.minecraft.minecraftnew.subsystems.combat.bloodmoon;

import goat.minecraft.minecraftnew.MinecraftNew;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;

/**
 * Forwards combat events to the AssaultWaveManager.
 */
public class AssaultWaveListener implements Listener {

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        AssaultWaveManager manager = AssaultWaveManager.getInstance(MinecraftNew.getInstance());
        manager.onWaveEntityDamaged(event.getEntity());
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        AssaultWaveManager manager = AssaultWaveManager.getInstance(MinecraftNew.getInstance());
        manager.onWaveEntityDamaged(event.getEntity());
    }
}
