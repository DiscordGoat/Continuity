package goat.minecraft.minecraftnew.subsystems.smithing.talismans;

import goat.minecraft.minecraftnew.MinecraftNew;
import org.bukkit.ChatColor;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerAnimationEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

public class ReforgeSwiftBlade implements Listener {


    private static final double HASTE_CHANCE = 0.25; // 25% chance for Haste IV
    private static final int HASTE_DURATION = 40; // 2 seconds (in ticks)
    private static final long ATTACK_TIMEOUT = 5000; // 5 seconds in milliseconds
    private static final int MONITOR_INTERVAL = 2; // Monitor every 2 ticks
    private static final Random RANDOM = new Random();

    private final Map<Player, Long> activeAttackers = new ConcurrentHashMap<>();

    public ReforgeSwiftBlade() {
        // Start the repeating task
        new BukkitRunnable() {
            @Override
            public void run() {
                monitorAttackers();
            }
        }.runTaskTimer(MinecraftNew.getInstance(), 0, MONITOR_INTERVAL);
    }

    @EventHandler
    public void onPlayerSwing(PlayerAnimationEvent event) {
        Player player = event.getPlayer();
        ItemStack weapon = player.getInventory().getItemInMainHand();

        // Check if the weapon has "Talisman: Swift Blade" lore
        if (hasSwiftBladeLore(weapon)) {
            LivingEntity target = getTargetEntity(player);
            if (target != null) {
                activeAttackers.put(player, System.currentTimeMillis()); // Update last attack time
            }
        }
    }

    private void monitorAttackers() {
        long now = System.currentTimeMillis();

        // Iterate over active attackers
        activeAttackers.forEach((player, lastAttackTime) -> {
            if (now - lastAttackTime > ATTACK_TIMEOUT) {
                // Player hasn't attacked in 5 seconds, remove Haste effect

                // Check if the player has Haste IV before removing it
                PotionEffect hasteEffect = player.getPotionEffect(PotionEffectType.HASTE);
                if (hasteEffect != null && hasteEffect.getAmplifier() == 3) { // Amplifier 3 means Haste IV
                    player.removePotionEffect(PotionEffectType.HASTE);
                }
            } else {
                // Apply Haste if chance triggers
                if (RANDOM.nextDouble() < HASTE_CHANCE) {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.HASTE, HASTE_DURATION, 3, true, true));
                }
            }
        });

        // Remove entries for inactive attackers
        activeAttackers.entrySet().removeIf(entry -> !entry.getKey().isOnline());
    }

    /**
     * Retrieves the target entity the player is looking at.
     *
     * @param player The player to check.
     * @return The LivingEntity the player is targeting, or null if none.
     */
    private LivingEntity getTargetEntity(Player player) {
        return player.getWorld()
                .getNearbyEntities(player.getEyeLocation(), 5, 5, 5).stream()
                .filter(entity -> entity instanceof LivingEntity)
                .map(entity -> (LivingEntity) entity)
                .findFirst()
                .orElse(null);
    }

    /**
     * Checks if the item has "Talisman: Swift Blade" in its lore.
     *
     * @param item The ItemStack to check.
     * @return True if the item has "Talisman: Swift Blade" in its lore, false otherwise.
     */
    private boolean hasSwiftBladeLore(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        ItemMeta meta = item.getItemMeta();
        if (meta.hasLore()) {
            for (String lore : meta.getLore()) {
                if (ChatColor.stripColor(lore).equals("Talisman: Swift Blade")) {
                    return true;
                }
            }
        }
        return false;
    }
}
