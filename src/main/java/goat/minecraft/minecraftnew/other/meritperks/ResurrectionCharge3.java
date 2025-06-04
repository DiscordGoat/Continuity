package goat.minecraft.minecraftnew.other.meritperks;

import goat.minecraft.minecraftnew.utils.devtools.PlayerMeritManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Resurrection Charge 3 perk.
 * <p>
 * Allows the player to have up to three resurrection charges active
 * simultaneously.
 */
public class ResurrectionCharge3 implements Listener {

    private final JavaPlugin plugin;
    private final PlayerMeritManager playerData;

    public ResurrectionCharge3(JavaPlugin plugin, PlayerMeritManager playerData) {
        this.plugin = plugin;
        this.playerData = playerData;
    }

    /**
     * Provides a third resurrection charge that is consumed when used.
     */
    @EventHandler
    public void onPlayerDamage(EntityDamageEvent event) {
        if (event.isCancelled()) {
            return;
        }
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        double newHealth = player.getHealth() - event.getFinalDamage();
        if (newHealth <= 0) {
            boolean resurrected = ResurrectionUtil.tryResurrect(player, playerData);
            if (resurrected) {
                event.setCancelled(true);
            }
        }
    }
}
