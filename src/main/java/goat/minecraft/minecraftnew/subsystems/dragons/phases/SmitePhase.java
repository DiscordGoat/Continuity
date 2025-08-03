package goat.minecraft.minecraftnew.subsystems.dragons.phases;

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
 * Phase representing the Water Dragon's basic lightning attack.
 */
public class SmitePhase implements Phase {

    private final MinecraftNew plugin;
    private final DragonFight fight;
    private final WaterDragonTrait trait;

    public SmitePhase(MinecraftNew plugin, DragonFight fight, WaterDragonTrait trait) {
        this.plugin = plugin;
        this.fight = fight;
        this.trait = trait;
    }

    @Override
    public void start() {
        EnderDragon dragon = fight.getDragonEntity();
        World world = dragon.getWorld();
        Location origin = dragon.getLocation();

        dragon.setAI(false);
        dragon.setVelocity(new Vector(0, 0, 0));

        world.playSound(origin, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 10.0f, 1.0f);

        List<Player> targets = new ArrayList<>(world.getPlayers());
        for (Player p : targets) {
            world.spawnParticle(Particle.FLASH, p.getLocation(), 1);
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player p : targets) {
                    world.strikeLightning(p.getLocation());
                    p.damage(50.0, dragon);
                }
                dragon.setAI(true);
                trait.onPhaseComplete();
            }
        }.runTaskLater(plugin, 60L);
    }
}
