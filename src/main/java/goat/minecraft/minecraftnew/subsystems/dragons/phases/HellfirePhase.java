package goat.minecraft.minecraftnew.subsystems.dragons.phases;

import goat.minecraft.minecraftnew.MinecraftNew;
import goat.minecraft.minecraftnew.subsystems.dragons.DragonFight;
import goat.minecraft.minecraftnew.subsystems.dragons.FireDragonTrait;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Fireball;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.Random;

/**
 * Ultimate phase raining fireballs across the island.
 */
public class HellfirePhase implements Phase {

    private final MinecraftNew plugin;
    private final DragonFight fight;
    private final FireDragonTrait trait;
    private final Random random = new Random();

    public HellfirePhase(MinecraftNew plugin, DragonFight fight, FireDragonTrait trait) {
        this.plugin = plugin;
        this.fight = fight;
        this.trait = trait;
    }

    @Override
    public void start() {
        EnderDragon dragon = fight.getDragonEntity();
        World world = dragon.getWorld();
        Location freezeLoc = dragon.getLocation().clone();
        dragon.setAI(false);
        dragon.setVelocity(new Vector(0, 0, 0));

        // Particle task and unlock after 5 seconds
        new BukkitRunnable() {
            int ticks = 0;
            @Override
            public void run() {
                if (ticks++ >= 100 || dragon.isDead()) {
                    dragon.setAI(true);
                    trait.onPhaseComplete();
                    cancel();
                    return;
                }
                dragon.teleport(freezeLoc);
                world.spawnParticle(Particle.FLAME, freezeLoc, 50, 1, 1, 1, 0.1);
            }
        }.runTaskTimer(plugin, 0L, 1L);

        for (int i = 0; i < 1000; i++) {
            double x = freezeLoc.getX() + random.nextInt(400) - 200;
            double z = freezeLoc.getZ() + random.nextInt(400) - 200;
            Location loc = new Location(world, x, 100, z);
            Fireball fb = world.spawn(loc, Fireball.class);
            fb.setIsIncendiary(true);
            fb.setYield(6f);
            fb.setVelocity(new Vector(0, -1, 0));
            fb.setShooter(dragon);
        }
    }
}
