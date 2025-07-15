package goat.minecraft.minecraftnew.subsystems.fishing.npc;

import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.util.DataKey;
import org.bukkit.Bukkit;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import java.util.UUID;

public class RangedAttackTrait extends Trait {
    private final JavaPlugin plugin;
    private final UUID targetId;
    private int taskId = -1;

    public RangedAttackTrait(JavaPlugin plugin, UUID targetId) {
        super("seacreatureranged");
        this.plugin = plugin;
        this.targetId = targetId;
    }

    @Override
    public void onAttach() {
        start();
    }

    private void start() {
        taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
            Player target = Bukkit.getPlayer(targetId);
            if (target == null || !npc.isSpawned()) {
                if (npc.isSpawned()) npc.destroy();
                Bukkit.getScheduler().cancelTask(taskId);
                return;
            }
            LivingEntity entity = (LivingEntity) npc.getEntity();
            if (entity.getLocation().distanceSquared(target.getLocation()) <= 100) {
                Arrow arrow = entity.launchProjectile(Arrow.class);
                Vector v = target.getLocation().toVector().subtract(entity.getLocation().toVector()).normalize();
                arrow.setVelocity(v.multiply(1.2));
            }
        }, 40L, 40L);
    }

    @Override
    public void onRemove() {
        if (taskId != -1) {
            Bukkit.getScheduler().cancelTask(taskId);
        }
    }

    @Override public void load(DataKey key) {}
    @Override public void save(DataKey key) {}
}
