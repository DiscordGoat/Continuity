package goat.minecraft.minecraftnew.subsystems.dragons.phases;

import goat.minecraft.minecraftnew.MinecraftNew;
import goat.minecraft.minecraftnew.subsystems.dragons.DragonFight;
import goat.minecraft.minecraftnew.subsystems.dragons.FireDragonTrait;
import org.bukkit.World;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.List;
import java.util.Random;

/**
 * Basic attack phase launching a powerful fireball at a player.
 */
public class FireballPhase implements Phase {

    private final MinecraftNew plugin;
    private final DragonFight fight;
    private final FireDragonTrait trait;
    private final Random random = new Random();

    public FireballPhase(MinecraftNew plugin, DragonFight fight, FireDragonTrait trait) {
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
        Vector dir = target.getLocation().toVector().subtract(dragon.getLocation().toVector()).normalize().multiply(2);
        Fireball fireball = dragon.launchProjectile(Fireball.class, dir);
        fireball.setIsIncendiary(true);
        fireball.setYield(6f);
        trait.onPhaseComplete();
    }
}
