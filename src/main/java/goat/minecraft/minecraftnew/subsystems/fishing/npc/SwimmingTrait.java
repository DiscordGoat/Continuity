package goat.minecraft.minecraftnew.subsystems.fishing.npc;

import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.util.DataKey;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.UUID;

public class SwimmingTrait extends Trait {
    private final JavaPlugin plugin;
    private final UUID targetId;
    private int taskId = -1;

    public SwimmingTrait(JavaPlugin plugin, UUID targetId) {
        super("seacreatureswim");
        this.plugin = plugin;
        this.targetId = targetId;
    }

    @Override
    public void onAttach() {
        npc.setProtected(false);
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
        }, 0L, 10L);
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
