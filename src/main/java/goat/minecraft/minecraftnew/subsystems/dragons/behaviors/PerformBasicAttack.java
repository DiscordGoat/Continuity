package goat.minecraft.minecraftnew.subsystems.dragons.behaviors;

import goat.minecraft.minecraftnew.MinecraftNew;
import goat.minecraft.minecraftnew.subsystems.dragons.DragonFight;
import goat.minecraft.minecraftnew.subsystems.dragons.WaterDragonTrait;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

/**
 * Behaviour representing the Water Dragon's basic lightning attack.
 */
public class PerformBasicAttack implements Behavior {

    private final MinecraftNew plugin;
    private final DragonFight fight;
    private final WaterDragonTrait trait;

    public PerformBasicAttack(MinecraftNew plugin, DragonFight fight, WaterDragonTrait trait) {
        this.plugin = plugin;
        this.fight = fight;
        this.trait = trait;
    }

    @Override
    public void run() {
        EnderDragon dragon = fight.getDragonEntity();
        World world = dragon.getWorld();
        Location origin = dragon.getLocation();

        // Freeze the dragon in place
        dragon.setAI(false);
        dragon.setVelocity(new Vector(0, 0, 0));

        world.playSound(origin, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 10.0f, 1.0f);

        List<Location> targets = new ArrayList<>();
        for (Player p : world.getPlayers()) {
            Location loc = p.getLocation().clone();
            targets.add(loc);
            world.spawnParticle(Particle.FLASH, loc, 1);
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                for (Location loc : targets) {
                    world.strikeLightning(loc);
                }
                dragon.setAI(true);
                trait.onAttackComplete();
            }
        }.runTaskLater(plugin, 60L); // 3 seconds later
    }
}
