package goat.minecraft.minecraftnew.subsystems.combat;

import goat.minecraft.minecraftnew.subsystems.forestry.Forestry;
import goat.minecraft.minecraftnew.other.additionalfunctionality.Pathfinder;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Random;

/**
 * Spawns hordes targeting players at night based on notoriety.
 */
public class NightHordeTask extends BukkitRunnable {

    private final JavaPlugin plugin;
    private final Pathfinder pathfinder = new Pathfinder();
    private final Random random = new Random();

    public NightHordeTask(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    /** Start the repeating task. */
    public void start() {
        runTaskTimer(plugin, 0L, 1200L); // once per minute
    }

    @Override
    public void run() {
        for (World world : Bukkit.getWorlds()) {
            long time = world.getTime();
            if (time >= 13000 && time <= 23000) { // night only
                for (Player player : world.getPlayers()) {
                    int hostility = HostilityManager.getInstance(plugin)
                            .getPlayerHostility(player);
                    spawnHorde(player, hostility);
                }
            }
        }
    }

    private void spawnHorde(Player player, int size) {
        if (size <= 0) return;
        World world = player.getWorld();
        Location base = player.getLocation();
        for (int i = 0; i < size; i++) {
            Location spawn = base.clone().add(randomOffset(), 0, randomOffset());
            Zombie zombie = (Zombie) world.spawnEntity(spawn, EntityType.ZOMBIE);
            pathfinder.moveTo(zombie, player.getLocation());
        }
    }

    private double randomOffset() {
        return (random.nextDouble() * 10) - 5; // -5 to 5 blocks
    }
}
