package goat.minecraft.minecraftnew.subsystems.farming;

import goat.minecraft.minecraftnew.MinecraftNew;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;

/**
 * Manages per-player batches and flushes them after ~2 seconds.
 */
public class HarvestBatchManager {
    private static final Map<UUID, List<HarvestInstance>> activeBatches = new HashMap<>();

    public static void addToBatch(Player player, Material crop, int xp, boolean fert, boolean bee, boolean music, ItemStack reward) {
        List<HarvestInstance> list = activeBatches.computeIfAbsent(player.getUniqueId(), k -> new ArrayList<>());
        HarvestInstance current = list.isEmpty() ? null : list.get(list.size() - 1);

        if (current == null || current.isExpired()) {
            current = new HarvestInstance();
            list.add(current);
            scheduleFlush(player, current);
        }

        current.addCrop(crop, xp, fert, bee, music, reward);
    }

    private static void scheduleFlush(Player player, HarvestInstance instance) {
        Bukkit.getScheduler().runTaskLater(MinecraftNew.getInstance(), () -> {
            List<HarvestInstance> list = activeBatches.get(player.getUniqueId());
            if (list == null) return;
            if (list.contains(instance) && instance.isExpired()) {
                try {
                    instance.grant(player);
                } finally {
                    list.remove(instance);
                    if (list.isEmpty()) activeBatches.remove(player.getUniqueId());
                }
            } else {
                // Not expired yet; reschedule a short delay to check again.
                scheduleFlush(player, instance);
            }
        }, 40L); // 2 seconds ~= 40 ticks
    }
}

