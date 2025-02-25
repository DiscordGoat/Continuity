package goat.minecraft.minecraftnew.subsystems.combat;

import goat.minecraft.minecraftnew.MinecraftNew;
import goat.minecraft.minecraftnew.other.additionalfunctionality.Pathfinder;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class HordeInstinct implements Listener {
    Pathfinder pathfinder = new Pathfinder();
    @EventHandler
    public void hordeInstinct(EntitySpawnEvent event){
        Entity zombie = event.getEntity();
        if(zombie instanceof Zombie) {
            int zombieLevel = pathfinder.getMobLevel(zombie);
            if (zombieLevel >= 50 && Math.random() < 0.05) {
                zombie.setCustomName("§4Alpha " + zombie.getCustomName());
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (zombie.isDead()) {
                            this.cancel();
                            return;
                        }
                        Player nearestPlayer = pathfinder.getNearestPlayer(zombie, 100);
                        if (nearestPlayer != null) {
                            pathfinder.moveTo((Mob) zombie, nearestPlayer.getLocation());
                        }
                    }
                }.runTaskTimer(MinecraftNew.getInstance(), 0L, 20L); // Run every second
            }
        }
    }

}
