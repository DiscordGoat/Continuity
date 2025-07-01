package goat.minecraft.minecraftnew.subsystems.armorsets;

import goat.minecraft.minecraftnew.MinecraftNew;
import goat.minecraft.minecraftnew.other.additionalfunctionality.BlessingUtils;
import goat.minecraft.minecraftnew.subsystems.armorsets.FlowType;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.EulerAngle;

import java.util.ArrayList;

import java.util.HashMap;
import java.util.List;
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
    private final Map<UUID, Integer> animationTasks = new HashMap<>();
    private final Map<UUID, List<ArmorStand>> animationStands = new HashMap<>();

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

    /**
     * Adds flow stacks and triggers the appropriate animation if the player
     * is wearing a blessed armor set.
     */
    public void addFlowStacks(Player player, int amount) {
        if (amount <= 0) return;
        FlowData data = flowMap.computeIfAbsent(player.getUniqueId(), k -> new FlowData());
        data.flow += amount;
        data.lastActivity = System.currentTimeMillis();
        if (data.flow <= 0) {
            stopAnimation(player);
            return;
        }
        refreshAnimation(player, data.flow);
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
                                stopAnimation(p);
                            }
                        }
                    }
                }
            }
        }.runTaskTimer(plugin, 20L, 20L);
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        addFlowStacks(event.getPlayer(), 1);
    }

    @EventHandler
    public void onEntityKill(EntityDeathEvent event) {
        Player killer = event.getEntity().getKiller();
        if (killer != null) {
            addFlowStacks(killer, 1);
        }
    }

    private void refreshAnimation(Player player, int intensity) {
        String blessing = BlessingUtils.getBlessing(player.getInventory().getHelmet());
        if (blessing == null || !BlessingUtils.hasFullSetBonus(player, blessing)) {
            stopAnimation(player);
            return;
        }
        String enumName = blessing.toUpperCase().replace(" ", "_").replace("'", "");
        FlowType type;
        try {
            type = FlowType.valueOf(enumName);
        } catch (IllegalArgumentException e) {
            return;
        }
        startAnimation(player, type, intensity);
    }

    private void startAnimation(Player player, FlowType type, int intensity) {
        intensity = Math.max(1, Math.min(intensity, 24));
        Integer existing = animationTasks.remove(player.getUniqueId());
        if (existing != null) {
            Bukkit.getScheduler().cancelTask(existing);
            List<ArmorStand> prev = animationStands.remove(player.getUniqueId());
            if (prev != null) prev.forEach(Entity::remove);
        }
        List<ArmorStand> spawned = new ArrayList<>();
        Location base = player.getLocation();
        double radius = 12.0;
        for (int i = 0; i < intensity; i++) {
            double angle = 2 * Math.PI * i / intensity;
            double x = radius * Math.cos(angle);
            double z = radius * Math.sin(angle);
            Location loc = base.clone().add(x, 0.5, z);
            ArmorStand stand = player.getWorld().spawn(loc, ArmorStand.class, s -> {
                s.setGravity(false);
                s.setVisible(false);
                s.setMarker(true);
                ItemStack item = type.createItem();
                if (item.getType() != Material.AIR) {
                    s.setItemInHand(item);
                }
            });
            spawned.add(stand);
        }
        animationStands.put(player.getUniqueId(), spawned);
        final Location[] center = {player.getLocation()};
        BukkitRunnable runnable = new BukkitRunnable() {
            double angle = 0;
            int tick = 0;
            @Override
            public void run() {
                if (!player.isOnline()) {
                    cancel();
                    spawned.forEach(Entity::remove);
                    animationTasks.remove(player.getUniqueId());
                    animationStands.remove(player.getUniqueId());
                    return;
                }
                if (++tick % 2 == 0) {
                    center[0] = player.getLocation();
                }
                angle += 0.05;
                for (int i = 0; i < spawned.size(); i++) {
                    ArmorStand stand = spawned.get(i);
                    if (!stand.isValid()) continue;
                    double off = angle + 2 * Math.PI * i / spawned.size();
                    double x = radius * Math.cos(off);
                    double z = radius * Math.sin(off);
                    Location loc = center[0].clone().add(x, 0.5, z);
                    stand.teleport(loc);
                    EulerAngle pose = stand.getRightArmPose();
                    stand.setRightArmPose(new EulerAngle(
                            pose.getX() + Math.toRadians(15),
                            pose.getY(),
                            pose.getZ()
                    ));
                    stand.getWorld().spawnParticle(
                            type.getParticle(),
                            loc, 1, 0, 0, 0, 0
                    );
                }
            }
        };
        int id = runnable.runTaskTimer(plugin, 0L, 1L).getTaskId();
        animationTasks.put(player.getUniqueId(), id);
    }

    private void stopAnimation(Player player) {
        Integer id = animationTasks.remove(player.getUniqueId());
        if (id != null) {
            Bukkit.getScheduler().cancelTask(id);
        }
        List<ArmorStand> list = animationStands.remove(player.getUniqueId());
        if (list != null) {
            list.forEach(Entity::remove);
        }
    }
}
