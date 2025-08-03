package goat.minecraft.minecraftnew.subsystems.dragons.phases;

import goat.minecraft.minecraftnew.MinecraftNew;
import goat.minecraft.minecraftnew.subsystems.dragons.DragonFight;
import goat.minecraft.minecraftnew.subsystems.dragons.FireDragonTrait;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.EntityType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.Random;

/**
 * Special phase summoning waves of ghasts around the dragon.
 */
public class ScorchPhase implements Phase {

    private final MinecraftNew plugin;
    private final DragonFight fight;
    private final FireDragonTrait trait;
    private final Random random = new Random();

    public ScorchPhase(MinecraftNew plugin, DragonFight fight, FireDragonTrait trait) {
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

        new BukkitRunnable() {
            int seconds = 0;
            int spawned = 0;

            @Override
            public void run() {
                if (!trait.getNPC().isSpawned() || dragon.isDead()) {
                    dragon.setAI(true);
                    trait.onPhaseComplete();
                    cancel();
                    return;
                }
                dragon.teleport(freezeLoc);
                world.spawnParticle(Particle.FLAME, freezeLoc, 30, 1, 1, 1, 0.1);
                seconds++;
                if (seconds % 3 == 0 && spawned < 16) {
                    double angle = random.nextDouble() * 2 * Math.PI;
                    double dist = random.nextDouble() * 20;
                    Location loc = freezeLoc.clone().add(Math.cos(angle) * dist, 1, Math.sin(angle) * dist);
                    world.spawnEntity(loc, EntityType.GHAST);
                    spawned++;
                    if (spawned >= 16) {
                        dragon.setAI(true);
                        trait.onPhaseComplete();
                        cancel();
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }
}
