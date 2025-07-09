package goat.minecraft.minecraftnew.other.enchanting.enchantingeffects;

import goat.minecraft.minecraftnew.other.enchanting.CustomEnchantmentManager;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Bloodlust implements Listener {

    private final Map<UUID, BloodlustData> playerBloodlustMap = new HashMap<>();
    private final Plugin plugin;

    public Bloodlust(Plugin plugin) {
        this.plugin = plugin;
    }

    private static class BloodlustData {
        int killCount = 0;
        BukkitRunnable currentTask;
        int remainingTime = 0; // Time remaining in seconds
    }

    @EventHandler
    public void onMonsterKill(EntityDeathEvent event) {
        LivingEntity killed = event.getEntity();
        if (!(killed instanceof Monster)) return;

        Player killer = killed.getKiller();
        if (killer == null) return;

        ItemStack weapon = killer.getInventory().getItemInMainHand();
        if (!CustomEnchantmentManager.hasEnchantment(weapon, "Bloodlust")) return;

        UUID playerId = killer.getUniqueId();
        BloodlustData data = playerBloodlustMap.computeIfAbsent(playerId, k -> new BloodlustData());

        // Add 10 seconds to timer, cap at 40 seconds
        data.remainingTime = Math.min(data.remainingTime + 10, 40);
        data.killCount = Math.min(data.killCount + 1, 30); // Cap at 30 kills

        // Cancel previous timer task
        if (data.currentTask != null) {
            data.currentTask.cancel();
        }

        // Apply stacking effects
        applyBloodlustEffects(killer, data.killCount);

        // Play escalating sound effects
        float pitch = 1.0f + (data.killCount * 0.03f);
        float volume = 1.0f + (data.killCount * 0.02f); // Slightly increase volume too
        killer.playSound(killer.getLocation(), Sound.ENTITY_WARDEN_HEARTBEAT, volume, pitch);

        // Display kill count and time remaining
        sendActionBar(killer, ChatColor.RED + "BLOODLUST: " + data.killCount + "/30 kills (" + data.remainingTime + "s)");

        // Start countdown timer
        startBloodlustTimer(killer, data);
    }
    private void sendActionBar(Player player, String message) {
        player.spigot().sendMessage(net.md_5.bungee.api.ChatMessageType.ACTION_BAR, new net.md_5.bungee.api.chat.TextComponent(message));
    }
    private void applyBloodlustEffects(Player player, int killCount) {
        // Remove existing effects first
        removeBloodlustEffects(player);

        // Apply effects based on specific kill thresholds
        // 1 kill: Haste I
        if (killCount >= 1) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.FAST_DIGGING, Integer.MAX_VALUE, 0, true, false));
        }

        // 3 kills: Strength I + previous effects
        if (killCount >= 3) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, Integer.MAX_VALUE, 0, true, false));
        }

        // 5 kills: Speed I + previous effects
        if (killCount >= 5) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 0, true, false));
        }

        // 10 kills: Double all effects (Haste II, Strength II, Speed II)
        if (killCount >= 10) {
            player.removePotionEffect(PotionEffectType.FAST_DIGGING);
            player.removePotionEffect(PotionEffectType.INCREASE_DAMAGE);
            player.removePotionEffect(PotionEffectType.SPEED);
            
            player.addPotionEffect(new PotionEffect(PotionEffectType.FAST_DIGGING, Integer.MAX_VALUE, 1, true, false));
            player.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, Integer.MAX_VALUE, 1, true, false));
            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 1, true, false));
        }

        // 15 kills: Regeneration II + previous effects
        if (killCount >= 15) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, Integer.MAX_VALUE, 1, true, false));
        }

        // 20 kills: Resistance II + previous effects
        if (killCount >= 20) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, Integer.MAX_VALUE, 1, true, false));
        }

        // 25 kills: Absorption V + previous effects
        if (killCount >= 25) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, Integer.MAX_VALUE, 4, true, false));
        }

        // 30 kills: Max bloodlust - all effects at level III
        if (killCount >= 30) {
            // Remove all current effects and apply max level versions
            removeBloodlustEffects(player);
            
            player.addPotionEffect(new PotionEffect(PotionEffectType.FAST_DIGGING, Integer.MAX_VALUE, 2, true, false));
            player.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, Integer.MAX_VALUE, 2, true, false));
            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 2, true, false));
            player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, Integer.MAX_VALUE, 2, true, false));
            player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, Integer.MAX_VALUE, 2, true, false));
            player.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, Integer.MAX_VALUE, 4, true, false)); // Keep Absorption V
        }
    }

    private void startBloodlustTimer(Player player, BloodlustData data) {
        data.currentTask = new BukkitRunnable() {
            @Override
            public void run() {
                data.remainingTime--;
                
                if (data.remainingTime <= 0) {
                    // Bloodlust expires
                    removeBloodlustEffects(player);
                    data.killCount = 0;
                    data.remainingTime = 0;
                    sendActionBar(player, ChatColor.GRAY + "Bloodlust fades...");
                    this.cancel();
                } else {
                    // Update action bar with remaining time
                    sendActionBar(player, ChatColor.RED + "BLOODLUST: " + data.killCount + "/30 kills (" + data.remainingTime + "s)");
                }
            }
        };
        data.currentTask.runTaskTimer(plugin, 20L, 20L); // Run every second
    }

    private void removeBloodlustEffects(Player player) {
        player.removePotionEffect(PotionEffectType.INCREASE_DAMAGE);
        player.removePotionEffect(PotionEffectType.SPEED);
        player.removePotionEffect(PotionEffectType.FAST_DIGGING);
        player.removePotionEffect(PotionEffectType.REGENERATION);
        player.removePotionEffect(PotionEffectType.DAMAGE_RESISTANCE);
        player.removePotionEffect(PotionEffectType.ABSORPTION);
    }

    public void cleanup() {
        for (BloodlustData data : playerBloodlustMap.values()) {
            if (data.currentTask != null) {
                data.currentTask.cancel();
            }
        }
        playerBloodlustMap.clear();
    }
}
