package goat.minecraft.minecraftnew.subsystems.dragons.phases;

import goat.minecraft.minecraftnew.MinecraftNew;
import goat.minecraft.minecraftnew.subsystems.dragons.DragonFight;
import goat.minecraft.minecraftnew.subsystems.dragons.WaterDragonTrait;
import net.citizensnpcs.api.ai.Navigator;
import net.citizensnpcs.api.ai.NavigatorParameters;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;

/**
 * Phase that has the Water Dragon charge a player, continuously tracking
 * the player's location.
 */
public class ChargePhase implements Phase {

    private final MinecraftNew plugin;
    private final DragonFight fight;
    private final WaterDragonTrait trait;

    public ChargePhase(MinecraftNew plugin, DragonFight fight, WaterDragonTrait trait) {
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
        // target nearest player
        Player target = players.get(0);
        double closest = target.getLocation().distanceSquared(dragon.getLocation());
        for (Player p : players) {
            double dist = p.getLocation().distanceSquared(dragon.getLocation());
            if (dist < closest) {
                closest = dist;
                target = p;
            }
        }

        NPC npc = trait.getNPC();
        npc.data().set(NPC.Metadata.FLYABLE, true);
        Navigator navigator = npc.getNavigator();
        NavigatorParameters params = navigator.getLocalParameters();
        params.range(5000.0F);
        params.speedModifier(3f);

        dragon.setAI(false);

        Player finalTarget = target;
        new BukkitRunnable() {
            int ticks = 0;
            @Override
            public void run() {
                if (ticks++ > 100 || !finalTarget.isValid() || dragon.isDead()) {
                    navigator.cancelNavigation();
                    dragon.setAI(true);
                    trait.onPhaseComplete();
                    cancel();
                    return;
                }
                Location loc = finalTarget.getLocation();
                navigator.setTarget(loc);
                if (npc.getEntity().getLocation().distanceSquared(loc) < 4) {
                    finalTarget.damage(20.0, dragon);
                    navigator.cancelNavigation();
                    dragon.setAI(true);
                    trait.onPhaseComplete();
                    cancel();
                }
            }
        }.runTaskTimer(plugin, 0L, 5L);
    }
}
