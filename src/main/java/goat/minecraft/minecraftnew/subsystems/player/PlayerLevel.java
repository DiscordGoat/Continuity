package goat.minecraft.minecraftnew.subsystems.player;

import goat.minecraft.minecraftnew.MinecraftNew;
import goat.minecraft.minecraftnew.subsystems.utils.XPManager;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.Player;
import org.bukkit.ChatColor;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
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
        double maxHealth = 20.0 * healthMultiplier;

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

    // Handle outgoing damage (increase player damage)


    // Handle incoming damage (reduce damage taken by player)
    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            int level = xpManager.getPlayerLevel(player, "Player");

            // Cap level at 100
            level = Math.min(level, 100);

            // Calculate damage reduction
            double damageReduction = level * 0.001; // 0.4% damage reduction per level
            // Cap damageReduction at 0.8 (80% reduction)
            damageReduction = Math.min(damageReduction, 0.1);

            double defenseMultiplier = 1 - damageReduction; // Remaining damage to be taken

            // Apply defense multiplier
            double originalDamage = event.getDamage();
            double newDamage = originalDamage * defenseMultiplier;

            event.setDamage(newDamage);
        }

    }
}
