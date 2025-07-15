package goat.minecraft.minecraftnew.subsystems.brewing.custompotions;

import goat.minecraft.minecraftnew.MinecraftNew;
import goat.minecraft.minecraftnew.subsystems.brewing.PotionManager;
import goat.minecraft.minecraftnew.utils.devtools.XPManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PotionOfSovereignty implements Listener {

    // Map to track the number of available deflections per player
    private static Map<UUID, Integer> activeDeflections = new HashMap<>();

    /**
     * When a player drinks a potion named "Potion of Sovereignty", their deflection count is reset to 5.
     * (The potion's duration stacks via PotionManager, but the deflection count remains fixed at 5.)
     */
    @EventHandler
    public void onPotionDrink(PlayerItemConsumeEvent event) {
        ItemStack item = event.getItem();
        if (item == null || !item.hasItemMeta() || !item.getItemMeta().hasDisplayName()) {
            return;
        }
        String displayName = ChatColor.stripColor(item.getItemMeta().getDisplayName());
        if (displayName.equals("Potion of Sovereignty")) {
            Player player = event.getPlayer();
            UUID uuid = player.getUniqueId();
            // Reset deflections to 5
            activeDeflections.put(uuid, 5);

            XPManager xpManager = new XPManager(MinecraftNew.getInstance());
            int duration = (60 * 3);
            PotionManager.addCustomPotionEffect("Potion of Sovereignty", player, duration);

            player.sendMessage(ChatColor.GREEN + "Sovereignty activated! Your deflections have been refreshed to 5.");
            xpManager.addXP(player, "Brewing", 100);
        }
    }

    /**
     * When a player receives damage, if they have any available deflections,
     * the damage is cancelled (deflected) and one deflection is consumed.
     * That deflection will be restored after 120 seconds with a sound cue.
     */
    @EventHandler
    public void onPlayerDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }
        if(PotionManager.isActive("Potion of Sovereignty", player)) {
            UUID uuid = player.getUniqueId();
            if(!activeDeflections.containsKey(uuid)){
                activeDeflections.put(uuid, 5);
            }
            int availableDeflections = activeDeflections.getOrDefault(uuid, 0);
            if (availableDeflections > 0) {
                // Cancel the damage event, effectively deflecting the attack
                event.setCancelled(true);
                // Consume one deflection
                activeDeflections.put(uuid, availableDeflections - 1);
                player.sendMessage(ChatColor.AQUA + "Sovereignty deflected an attack! Remaining deflections: " + (availableDeflections - 1));
                // Schedule restoration of one deflection after 2 minutes (120 seconds)
                Bukkit.getScheduler().runTaskLater(MinecraftNew.getInstance(), () -> {
                    int current = activeDeflections.getOrDefault(uuid, 0);
                    // Restore a deflection but never exceed 5 total
                    activeDeflections.put(uuid, Math.min(current + 1, 5));
                    // Play a sound to indicate the deflection is restored
                    player.playSound(player.getLocation(), Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1.0f, 10.0f);
                    player.sendMessage(ChatColor.GREEN + "A Sovereignty deflection has been restored! You now have " + activeDeflections.get(uuid) + " deflections available.");
                }, 120 * 20L); // 120 seconds * 20 ticks per second
            }
        }
    }
}
