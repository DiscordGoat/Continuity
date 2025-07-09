package goat.minecraft.minecraftnew.other.additionalfunctionality;

import goat.minecraft.minecraftnew.MinecraftNew;
import goat.minecraft.minecraftnew.utils.devtools.XPManager;
import goat.minecraft.minecraftnew.subsystems.health.HealthManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class PlayerLevel implements Listener {
    private MinecraftNew plugin;
    private XPManager xpManager;

    public PlayerLevel(MinecraftNew plugin, XPManager xpManager) {
        this.plugin = plugin;
        this.xpManager = xpManager;
    }

    // Method to apply attribute bonuses to a player
    public void applyPlayerAttributes(Player player) {
        HealthManager.getInstance(plugin, xpManager).recalculate(player);
    }

    // Event handlers to apply attributes when necessary

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        applyPlayerAttributes(player);
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();

        // Schedule a delayed task to apply attributes after respawn
        new BukkitRunnable() {
            @Override
            public void run() {
                applyPlayerAttributes(player);
            }
        }.runTaskLater(plugin, 1L); // Delay of 1 tick
    }
}
