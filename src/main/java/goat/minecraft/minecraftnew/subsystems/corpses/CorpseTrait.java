package goat.minecraft.minecraftnew.subsystems.corpses;

import goat.minecraft.minecraftnew.subsystems.combat.config.CombatConfiguration;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.util.DataKey;
import org.bukkit.Bukkit;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import java.util.UUID;

public class CorpseTrait extends Trait {
    private final JavaPlugin plugin;
    private final Corpse corpse;
    private int taskId = -1;

    private static final double MELEE_RANGE = 2.5;
    private static final double BOW_RANGE = 16.0;
    private static final double BASE_DAMAGE = 5.0;

    public CorpseTrait(JavaPlugin plugin, Corpse corpse) {
        super("corpse_trait");
        this.plugin = plugin;
        this.corpse = corpse;
    }

    @Override
    public void onAttach() {
        npc.setProtected(false);
        start();
    }

    private void start() {
        taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
            if (!npc.isSpawned()) {
                Bukkit.getScheduler().cancelTask(taskId);
                return;
            }
            Player nearest = getNearestPlayer();
            if (nearest == null) return;

            npc.getNavigator().setTarget(nearest, true);
            Entity entity = npc.getEntity();
            double damage = BASE_DAMAGE * (1 + corpse.getLevel() * CombatConfiguration.getInstance().getDamageConfig().getMonsterPerLevel());
            double distSq = entity.getLocation().distanceSquared(nearest.getLocation());

            if (corpse.usesBow()) {
                if (distSq <= BOW_RANGE * BOW_RANGE) {
                    Arrow arrow = ((Player) entity).launchProjectile(Arrow.class);
                    Vector dir = nearest.getLocation().add(0,1.5,0).toVector().subtract(entity.getLocation().add(0,1.5,0).toVector()).normalize();
                    arrow.setVelocity(dir.multiply(1.6));
                    arrow.setDamage(damage);
                    arrow.setShooter((Player) entity);
                }
            } else {
                if (distSq <= MELEE_RANGE * MELEE_RANGE) {
                    nearest.damage(damage, entity);
                }
            }
        }, 0L, 20L);
    }

    private Player getNearestPlayer() {
        double nearest = Double.MAX_VALUE;
        Player result = null;
        for (Player p : Bukkit.getOnlinePlayers()) {
            double d = npc.getEntity().getLocation().distanceSquared(p.getLocation());
            if (d < nearest) {
                nearest = d;
                result = p;
            }
        }
        return result;
    }

    @Override
    public void onRemove() {
        if (taskId != -1) {
            Bukkit.getScheduler().cancelTask(taskId);
        }
    }

    @Override
    public void load(DataKey key) {}

    @Override
    public void save(DataKey key) {}
}
