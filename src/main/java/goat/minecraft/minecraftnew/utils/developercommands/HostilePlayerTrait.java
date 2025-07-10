package goat.minecraft.minecraftnew.utils.developercommands;

import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.util.DataKey;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.UUID;

/**
 * Trait that makes an NPC chase and melee attack a player target.
 */
public class HostilePlayerTrait extends Trait {

    private final JavaPlugin plugin;
    private final UUID targetId;
    private int taskId = -1;

    private static final double ATTACK_RANGE = 2.5;
    private static final double DAMAGE = 2.0;

    public HostilePlayerTrait(JavaPlugin plugin, UUID targetId) {
        super("hostileplayertrait");
        this.plugin = plugin;
        this.targetId = targetId;
    }

    @Override
    public void onAttach() {
        npc.setProtected(false); // allow the NPC to take damage
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

            npc.getNavigator().setTarget(target, true);
            Entity entity = npc.getEntity();
            if (entity.getLocation().distanceSquared(target.getLocation()) <= ATTACK_RANGE * ATTACK_RANGE) {
                target.damage(DAMAGE, entity);
            }
        }, 0L, 10L);
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
