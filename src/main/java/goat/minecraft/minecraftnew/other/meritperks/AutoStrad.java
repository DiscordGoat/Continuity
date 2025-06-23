package goat.minecraft.minecraftnew.other.meritperks;

import goat.minecraft.minecraftnew.utils.devtools.PlayerMeritManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * AutoStrad merit perk.
 * <p>
 * Automatically repairs all durable items in the player's inventory by
 * 100 durability every five minutes. Costs 20 merit points.
 */
public class AutoStrad implements Listener {

    private final JavaPlugin plugin;
    private final PlayerMeritManager playerData;

    public AutoStrad(JavaPlugin plugin, PlayerMeritManager playerData) {
        this.plugin = plugin;
        this.playerData = playerData;
        startRepairTask();
    }

    /**
     * Starts a repeating task that repairs durable items for players
     * who own the AutoStrad perk.
     */
    private void startRepairTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (!playerData.hasPerk(player.getUniqueId(), "AutoStrad")) {
                        continue;
                    }
                    repairInventory(player);
                }
            }
        }.runTaskTimer(plugin, 0L, 20L * 60 * 5); // every 5 minutes
    }

    /**
     * Repairs all damageable items in the player's inventory by up to
     * 100 durability.
     */
    public void repairInventory(Player player) {
        for (ItemStack item : player.getInventory().getContents()) {
            if (item == null || item.getType().getMaxDurability() <= 0) {
                continue;
            }

            ItemMeta meta = item.getItemMeta();
            if (meta instanceof Damageable damageable) {
                if (damageable.hasDamage()) {
                    int newDamage = damageable.getDamage() - 100;
                    if (newDamage < 0) {
                        newDamage = 0;
                    }
                    damageable.setDamage(newDamage);
                    item.setItemMeta((ItemMeta) damageable);
                }
            }
        }
    }
}
