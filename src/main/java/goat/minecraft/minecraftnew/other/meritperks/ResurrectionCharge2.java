package goat.minecraft.minecraftnew.other.meritperks;

import goat.minecraft.minecraftnew.utils.devtools.PlayerMeritManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Resurrection Charge 2 perk.
 * <p>
 * Grants the player a second resurrection charge, allowing two automatic
 * deaths to be prevented before needing to repurchase.
 */
public class ResurrectionCharge2 implements Listener {

    private final JavaPlugin plugin;
    private final PlayerMeritManager playerData;

    public ResurrectionCharge2(JavaPlugin plugin, PlayerMeritManager playerData) {
        this.plugin = plugin;
        this.playerData = playerData;
    }

    /**
     * Identical logic to the base Resurrection perk; this class exists to
     * provide an additional charge that is consumed on use.
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
