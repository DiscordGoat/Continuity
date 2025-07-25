package goat.minecraft.minecraftnew.other.beacon;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 * Applies the effects of the Catalyst of Death.
 * Players near this catalyst gain Haste V and
 * an increased chance to uncover graves handled
 * in the gravedigging subsystem.
 */
public class DeathCatalystListener {

    private final JavaPlugin plugin;

    public DeathCatalystListener(JavaPlugin plugin) {
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
                    if (manager.isNearCatalyst(player.getLocation(), CatalystType.DEATH)) {
                        Catalyst cat = manager.findNearestCatalyst(player.getLocation(), CatalystType.DEATH);
                        if (cat != null) {
                            applyEffects(player);
                        }
                    }
                }
            }
        }.runTaskTimer(plugin, 20L, 20L);
    }

    private void applyEffects(Player player) {
        // amplifier 4 -> Haste V
        player.addPotionEffect(new PotionEffect(PotionEffectType.FAST_DIGGING, 40, 4, true, false));
    }
}
