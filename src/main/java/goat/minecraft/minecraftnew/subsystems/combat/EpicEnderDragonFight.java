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


    /**
     * Handles player joining a world and modifies the Ender Dragon in the "custom_end" world.
     */
    @EventHandler
    public void onPlayerEnterCustomEnd(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        World world = player.getWorld();

        if (world.getName().equalsIgnoreCase("custom_end")) {
            EnderDragon dragon = world.getEntitiesByClass(EnderDragon.class).stream().findFirst().orElse(null);

            if (dragon != null) {
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    dragon.setCustomName(ChatColor.DARK_RED + "Ender Dragon");
                    dragon.setCustomNameVisible(true);

                    // Check if the player already has a boss bar named "Ender Dragon" and remove it

                    // Set BossBar with segments
                    BossBar bossBar = dragon.getBossBar();
                    bossBar.setColor(BarColor.RED);
                    bossBar.setStyle(BarStyle.SEGMENTED_20);
                    // Add health effects
                }, 10*20); // 40 ticks = 2 seconds
            }
        }
    }
    
    @EventHandler
    public void onProjectileHit(ProjectileHitEvent event) {
        if (event.getHitEntity() instanceof EnderCrystal || event.getHitBlock() != null && event.getHitBlock().getType() == Material.END_CRYSTAL) {
            if (random.nextDouble() < 0.5) { // 50% chance to deflect
                event.setCancelled(true);
                Projectile projectile = event.getEntity();
                projectile.setVelocity(projectile.getVelocity().multiply(-1));
                projectile.getWorld().playSound(projectile.getLocation(), Sound.ITEM_SHIELD_BLOCK, 1000.0f, 1.0f);
                projectile.getWorld().spawnParticle(Particle.CRIT_MAGIC, projectile.getLocation(), 10, 0.2, 0.2, 0.2, 0.1);
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
                world.spawnParticle(Particle.EXPLOSION_HUGE, location, 1);
                world.spawnParticle(Particle.SMOKE_LARGE, location, 20, 1, 1, 1, 0.1);
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
            world.spawnParticle(Particle.FIREWORKS_SPARK, location, 100, 3, 3, 3, 0.1);

            // Optionally, spawn additional loot or mobs
        }
    }
}
