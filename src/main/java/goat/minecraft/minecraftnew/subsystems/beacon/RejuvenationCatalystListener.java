package goat.minecraft.minecraftnew.subsystems.beacon;

import goat.minecraft.minecraftnew.subsystems.mining.PlayerOxygenManager;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Handles the effects of the Catalyst of Rejuvenation.
 * Players within range slowly recover health, hunger, saturation, absorption,
 * oxygen and item durability.
 */
public class RejuvenationCatalystListener {

    private final JavaPlugin plugin;
    private final Map<UUID, Map<Integer, Integer>> repairTargets = new HashMap<>();

    public RejuvenationCatalystListener(JavaPlugin plugin) {
        this.plugin = plugin;
        startTask();
    }

    private void startTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                CatalystManager manager = CatalystManager.getInstance();
                if (manager == null) {
                    return;
                }
                for (Player player : plugin.getServer().getOnlinePlayers()) {
                    if (manager.isNearCatalyst(player.getLocation(), CatalystType.REJUVENATION)) {
                        Catalyst catalyst = manager.findNearestCatalyst(player.getLocation(), CatalystType.REJUVENATION);
                        if (catalyst != null) {
                            int tier = manager.getCatalystTier(catalyst);
                            applyRejuvenation(player, tier);
                        }
                    } else {
                        repairTargets.remove(player.getUniqueId());
                    }
                }
            }
        }.runTaskTimer(plugin, 20L, 20L);
    }

    private void applyRejuvenation(Player player, int tier) {
        double healPercent = 0.025 * (tier + 1);
        double maxHealth = player.getMaxHealth();
        if(player.getHealth() <= 0){
            return;
        }
        player.setHealth(Math.min(player.getHealth() + maxHealth * healPercent, maxHealth));

        player.setFoodLevel(Math.min(player.getFoodLevel() + 1, 20));
        player.setSaturation(Math.min(player.getSaturation() + 1f, 10f));
        player.setAbsorptionAmount(Math.min(player.getAbsorptionAmount() + 1.0, 20.0));

        PlayerOxygenManager oxygenManager = PlayerOxygenManager.getInstance();
        int currentOxy = oxygenManager.getPlayerOxygen(player);
        int maxOxy = oxygenManager.calculateInitialOxygen(player);
        oxygenManager.setPlayerOxygenLevel(player, Math.min(currentOxy + 1, maxOxy));

        handleDurability(player);
    }

    private void handleDurability(Player player) {
        UUID uuid = player.getUniqueId();
        Map<Integer, Integer> targets = repairTargets.computeIfAbsent(uuid, k -> new HashMap<>());

        for (int slot = 0; slot < player.getInventory().getSize(); slot++) {
            ItemStack item = player.getInventory().getItem(slot);
            if (item == null) {
                targets.remove(slot);
                continue;
            }
            int maxDur = item.getType().getMaxDurability();
            if (maxDur <= 0) {
                targets.remove(slot);
                continue;
            }
            if (!(item.getItemMeta() instanceof Damageable)) {
                targets.remove(slot);
                continue;
            }

            Damageable meta = (Damageable) item.getItemMeta();
            int damage = meta.getDamage();
            if (damage <= 0) {
                targets.remove(slot);
                continue;
            }

            if (maxDur <= 100) {
                meta.setDamage(0);
                item.setItemMeta(meta);
                targets.remove(slot);
                continue;
            }

            int target = targets.computeIfAbsent(slot, s -> damage - (int) Math.floor(damage * 0.25));
            if (damage < target) {
                target = damage - (int) Math.floor(damage * 0.25);
                targets.put(slot, target);
            }
            if (damage > target) {
                meta.setDamage(Math.max(damage - 1, target));
                item.setItemMeta(meta);
            } else {
                targets.remove(slot);
            }
        }

        if (targets.isEmpty()) {
            repairTargets.remove(uuid);
        }
    }
}
