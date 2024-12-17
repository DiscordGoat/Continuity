package goat.minecraft.minecraftnew.utils.player;

import goat.minecraft.minecraftnew.MinecraftNew;
import goat.minecraft.minecraftnew.utils.XPManager;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
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
        int level = xpManager.getPlayerLevel(player, "Player");

        // Cap level at 100
        level = Math.min(level, 100);

        // Calculate health multiplier (max double health at level 50)
        double healthMultiplier = 1 + ((Math.min(level, 50) * 0.02)); // 2% per level up to level 50

        // Apply health (max double)
        AttributeInstance healthAttribute = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);
        if (healthAttribute != null) {
            double baseHealth = 20.0; // Default player health
            double newHealth = baseHealth * healthMultiplier;
            healthAttribute.setBaseValue(newHealth);

            // Ensure current health does not exceed max health
            if (player.getHealth() > newHealth) {
                player.setHealth(newHealth);
            }
        }
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
