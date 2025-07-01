package goat.minecraft.minecraftnew.subsystems.armorsets;

import goat.minecraft.minecraftnew.MinecraftNew;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Tracks per-player Flow levels. Flow increases when the player performs
 * blessing related actions like killing monsters or breaking blocks. After
 * 30 seconds of inactivity Flow starts to decay each second until it reaches
 * zero.
 */
public class FlowManager implements Listener {

    private final JavaPlugin plugin;

    private static class FlowData {
        int flow = 0;
        long lastActivity = System.currentTimeMillis();
    }

    private final Map<UUID, FlowData> flowMap = new HashMap<>();

    public FlowManager(JavaPlugin plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
        startDecayTask();
    }

    /**
     * Returns the current flow level for the player.
     */
    public int getFlow(Player player) {
        FlowData data = flowMap.get(player.getUniqueId());
        return data == null ? 0 : data.flow;
    }

    /**
     * Increase the player's flow and update their last activity timestamp.
     */
    public void addFlow(Player player, int amount) {
        if (amount <= 0) return;
        FlowData data = flowMap.computeIfAbsent(player.getUniqueId(), k -> new FlowData());
        data.flow += amount;
        data.lastActivity = System.currentTimeMillis();
    }

    private void startDecayTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                long now = System.currentTimeMillis();
                for (Map.Entry<UUID, FlowData> entry : flowMap.entrySet()) {
                    FlowData data = entry.getValue();
                    if (data.flow > 0 && now - data.lastActivity > 30_000) {
                        data.flow--;
                        if (data.flow == 0) {
                            Player p = Bukkit.getPlayer(entry.getKey());
                            if (p != null && p.isOnline()) {
                                p.sendMessage(ChatColor.GRAY + "Your flow has faded.");
                            }
                        }
                    }
                }
            }
        }.runTaskTimer(plugin, 20L, 20L);
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        addFlow(event.getPlayer(), 1);
    }

    @EventHandler
    public void onEntityKill(EntityDeathEvent event) {
        Player killer = event.getEntity().getKiller();
        if (killer != null) {
            addFlow(killer, 1);
        }
    }
}
