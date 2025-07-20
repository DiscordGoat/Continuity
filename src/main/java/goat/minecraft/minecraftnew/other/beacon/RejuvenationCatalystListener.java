package goat.minecraft.minecraftnew.other.beacon;

import goat.minecraft.minecraftnew.subsystems.mining.PlayerOxygenManager;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;


/**
 * Handles the effects of the Catalyst of Rejuvenation.
 * Players within range slowly recover health, hunger, saturation, absorption,
 * oxygen and item durability.
 */
public class RejuvenationCatalystListener {

    private final JavaPlugin plugin;

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
    }
}
