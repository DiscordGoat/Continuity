package goat.minecraft.minecraftnew.subsystems.dragons.phases;

import goat.minecraft.minecraftnew.MinecraftNew;
import goat.minecraft.minecraftnew.subsystems.dragons.DragonFight;
import goat.minecraft.minecraftnew.subsystems.dragons.WaterDragonTrait;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.List;
import java.util.Random;

/**
 * Phase that launches a random player high into the air.
 */
public class LaunchPhase implements Phase {

    private final MinecraftNew plugin;
    private final DragonFight fight;
    private final WaterDragonTrait trait;
    private final Random random = new Random();

    public LaunchPhase(MinecraftNew plugin, DragonFight fight, WaterDragonTrait trait) {
        this.plugin = plugin;
        this.fight = fight;
        this.trait = trait;
    }

    @Override
    public void start() {
        EnderDragon dragon = fight.getDragonEntity();
        World world = dragon.getWorld();
        List<Player> players = world.getPlayers();
        if (players.isEmpty()) {
            trait.onPhaseComplete();
            return;
        }
        Player target = players.get(random.nextInt(players.size()));
        Location loc = target.getLocation();
        world.playSound(loc, Sound.ITEM_TRIDENT_RIPTIDE_3, 2.0f, 1.0f);
        target.setVelocity(new Vector(0, 5, 0));
        Bukkit.getScheduler().runTask(plugin, () -> {
            target.setVelocity(new Vector(0, 5, 0));
            target.teleport(target.getLocation().add(0, 100, 0));
            trait.onPhaseComplete();
        });
    }
}
