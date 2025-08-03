package goat.minecraft.minecraftnew.subsystems.dragons.phases;

import goat.minecraft.minecraftnew.MinecraftNew;
import goat.minecraft.minecraftnew.subsystems.dragons.DragonFight;
import goat.minecraft.minecraftnew.subsystems.dragons.WaterDragonTrait;
import org.bukkit.World;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Ultimate phase striking all players repeatedly with lightning.
 */
public class FuryPhase implements Phase {

    private final MinecraftNew plugin;
    private final DragonFight fight;
    private final WaterDragonTrait trait;

    public FuryPhase(MinecraftNew plugin, DragonFight fight, WaterDragonTrait trait) {
        this.plugin = plugin;
        this.fight = fight;
        this.trait = trait;
    }

    @Override
    public void start() {
        EnderDragon dragon = fight.getDragonEntity();
        World world = dragon.getWorld();
        dragon.setAI(false);
        new BukkitRunnable() {
            int count = 0;
            @Override
            public void run() {
                if (count++ >= 50 || dragon.isDead()) {
                    dragon.setAI(true);
                    trait.onPhaseComplete();
                    cancel();
                    return;
                }
                for (Player p : world.getPlayers()) {
                    world.strikeLightning(p.getLocation());
                    p.damage(5.0, dragon);
                }
            }
        }.runTaskTimer(plugin, 0L, 2L);
    }
}
