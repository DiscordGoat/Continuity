package goat.minecraft.minecraftnew.other.auras;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Manages player auras and handles particle display tasks.
 */
public class AuraManager {
    private final JavaPlugin plugin;
    private final Map<UUID, Aura> activeAuras = new HashMap<>();
    private final Map<UUID, Integer> taskIds = new HashMap<>();

    public AuraManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Activate the given aura for the player.
     * Any previously active aura will be replaced.
     */
    public void activateAura(Player player, Aura aura) {
        deactivateAura(player);
        activeAuras.put(player.getUniqueId(), aura);
        startAuraTask(player, aura);
        player.sendMessage(ChatColor.GREEN + "Activated aura: " + aura.name().toLowerCase());
    }

    /**
     * Toggle display of the player's current aura.
     */
    public void toggleAura(Player player) {
        Aura aura = activeAuras.get(player.getUniqueId());
        if (aura == null) {
            player.sendMessage(ChatColor.RED + "You have no active aura.");
            return;
        }
        Integer id = taskIds.remove(player.getUniqueId());
        if (id != null) {
            Bukkit.getScheduler().cancelTask(id);
            player.sendMessage(ChatColor.GRAY + "Aura hidden.");
        } else {
            startAuraTask(player, aura);
            player.sendMessage(ChatColor.GREEN + "Aura shown.");
        }
    }

    /**
     * Stop and remove any active aura for the player.
     */
    public void deactivateAura(Player player) {
        Integer id = taskIds.remove(player.getUniqueId());
        if (id != null) {
            Bukkit.getScheduler().cancelTask(id);
        }
        activeAuras.remove(player.getUniqueId());
    }

    private void startAuraTask(Player player, Aura aura) {
        final Location[] center = {player.getLocation()};
        double radius = 1.5;
        int count = Math.max(1, aura.getCount());
        long period = Math.max(1L, Math.round(20.0 / aura.getFrequency()));
        BukkitRunnable runnable = new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline()) {
                    cancel();
                    taskIds.remove(player.getUniqueId());
                    return;
                }
                switch (aura.getStyle()) {
                    case RING -> {
                        if (player.getLocation().distanceSquared(center[0]) > 0.01) {
                            center[0] = player.getLocation();
                        }
                        for (int i = 0; i < count; i++) {
                            double angle = (2 * Math.PI / count) * i;
                            double x = radius * Math.cos(angle);
                            double z = radius * Math.sin(angle);
                            Location loc = center[0].clone().add(x, 0.5, z);
                            player.getWorld().spawnParticle(aura.getParticle(), loc, 0, 0, 0, 0, 0);
                        }
                    }
                    case AMBIENT -> {
                        Location base = player.getLocation();
                        for (int i = 0; i < count; i++) {
                            double dx = (Math.random() - 0.5) * 4;
                            double dy = Math.random() * 2;
                            double dz = (Math.random() - 0.5) * 4;
                            Location loc = base.clone().add(dx, dy + 0.5, dz);
                            player.getWorld().spawnParticle(aura.getParticle(), loc, 0, 0, 0, 0, 0);
                        }
                    }
                    case TRAIL -> {
                        Location loc = player.getLocation().add(0, 0.1, 0);
                        player.getWorld().spawnParticle(aura.getParticle(), loc, count, 0, 0, 0, 0);
                    }
                    case RAIN -> {
                        Location base = player.getLocation();
                        for (int i = 0; i < count; i++) {
                            double dx = (Math.random() - 0.5) * 4;
                            double dz = (Math.random() - 0.5) * 4;
                            Location loc = base.clone().add(dx, 5 + Math.random() * 2, dz);
                            player.getWorld().spawnParticle(aura.getParticle(), loc, 0, 0, -0.2, 0, 0);
                        }
                    }
                    case BURST -> {
                        Vector back = player.getLocation().getDirection().multiply(-1);
                        Location start = player.getLocation().add(back);
                        for (int i = 0; i < count; i++) {
                            Vector offset = new Vector((Math.random() - 0.5) * 0.5, Math.random() * 0.5,
                                    (Math.random() - 0.5) * 0.5);
                            Vector vel = offset.clone().multiply(0.5);
                            player.getWorld().spawnParticle(aura.getParticle(), start, 0,
                                    vel.getX(), vel.getY(), vel.getZ(), 0.1);
                        }
                    }
                }
            }
        };
        int id = runnable.runTaskTimer(plugin, 0L, period).getTaskId();
        taskIds.put(player.getUniqueId(), id);
    }
}
