package goat.minecraft.minecraftnew.subsystems.combat;

import goat.minecraft.minecraftnew.MinecraftNew;
import org.bukkit.*;
import org.bukkit.boss.*;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.*;

public class EpicEnderDragonFight implements Listener {
    private final MinecraftNew plugin;
    private final Random random = new Random();
    private static final int DRAGON_LEVEL = 200;

    public EpicEnderDragonFight(MinecraftNew plugin) {
        this.plugin = plugin;
    }

    
    @EventHandler
    public void onProjectileHit(ProjectileHitEvent event) {
        if (event.getHitEntity() instanceof EnderCrystal || event.getHitBlock() != null && event.getHitBlock().getType() == Material.END_CRYSTAL) {
            if (random.nextDouble() < 0.5) { // 50% chance to deflect
                event.setCancelled(true);
                Projectile projectile = event.getEntity();
                projectile.setVelocity(projectile.getVelocity().multiply(-1));
                projectile.getWorld().playSound(projectile.getLocation(), Sound.ITEM_SHIELD_BLOCK, 1000.0f, 1.0f);
                projectile.getWorld().spawnParticle(Particle.CRIT, projectile.getLocation(), 10, 0.2, 0.2, 0.2, 0.1);
            }
        }
    }

    /**
     * Handles End Crystal destruction.
     * Spawns a Knight of the End upon destruction.
     */
    @EventHandler
    public void onEndCrystalDestroy(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof EnderCrystal) {
            EnderCrystal enderCrystal = (EnderCrystal) event.getEntity();
            // Check if the damage will destroy the crystal
                Location location = new Location(Bukkit.getWorld("custom_end"), 0, 66, 0, 0, 0);
                // Play explosion sound and particles
                World world = location.getWorld();
                world.playSound(location, Sound.ENTITY_WITHER_SPAWN, 1000.0f, 0.5f);
                world.spawnParticle(Particle.EXPLOSION, location, 1);
                world.spawnParticle(Particle.SMOKE, location, 20, 1, 1, 1, 0.1);
        }
    }

    /**
     * Optional: Modify the Ender Dragon's death to add custom behavior.
     */
    @EventHandler
    public void onDragonDeath(EntityDeathEvent event) {
        if (event.getEntity() instanceof EnderDragon) {
            // Add custom logic upon dragon's death
            World world = event.getEntity().getWorld();
            Location location = event.getEntity().getLocation();

            // Play sound and spawn particles
            world.playSound(location, Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
            world.spawnParticle(Particle.FIREWORK, location, 100, 3, 3, 3, 0.1);

            // Optionally, spawn additional loot or mobs
        }
    }
}
