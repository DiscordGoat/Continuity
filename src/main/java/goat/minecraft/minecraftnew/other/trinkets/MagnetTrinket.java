package goat.minecraft.minecraftnew.other.trinkets;

import goat.minecraft.minecraftnew.other.additionalfunctionality.CustomBundleGUI;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class MagnetTrinket {
    private final JavaPlugin plugin;

    public MagnetTrinket(JavaPlugin plugin) {
        this.plugin = plugin;
        startTask();
    }

    private void startTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (hasMagnet(player)) {
                        attractOrbs(player);
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }

    private boolean hasMagnet(Player player) {
        return CustomBundleGUI.getInstance().hasItemWithDisplayName(player, "Magnet");
    }

    private void attractOrbs(Player player) {
        Location loc = player.getLocation();
        double radius = 30.0;
        for (Entity entity : player.getWorld().getNearbyEntities(loc, radius, radius, radius)) {
            if (entity instanceof ExperienceOrb orb) {
                orb.setVelocity(loc.toVector().subtract(orb.getLocation().toVector()).normalize().multiply(1.5));
            }
        }
    }
}
