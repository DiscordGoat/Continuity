package goat.minecraft.minecraftnew.other.meritperks;

import goat.minecraft.minecraftnew.utils.devtools.PlayerMeritManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Resurrection merit perk.
 * <p>
 * Functions like a totem of undying. Prevents player death, grants temporary
 * regeneration and absorption, and then disables itself until repurchased.
 */
public class Resurrection implements Listener {

    private final JavaPlugin plugin;
    private final PlayerMeritManager playerData;

    public Resurrection(JavaPlugin plugin, PlayerMeritManager playerData) {
        this.plugin = plugin;
        this.playerData = playerData;
    }

    /**
     * Checks incoming damage and prevents death if the player has any
     * resurrection charges available.
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
