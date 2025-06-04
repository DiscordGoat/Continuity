package goat.minecraft.minecraftnew.other.meritperks;

import goat.minecraft.minecraftnew.utils.devtools.PlayerMeritManager;
import org.bukkit.GameRule;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Keepinventory merit perk.
 * <p>
 * When a player with this perk logs in, keep inventory is enabled for them.
 * No further logic is implemented here yet.
 */
public class Keepinventory implements Listener {

    private final JavaPlugin plugin;
    private final PlayerMeritManager playerData;

    public Keepinventory(JavaPlugin plugin, PlayerMeritManager playerData) {
        this.plugin = plugin;
        this.playerData = playerData;
    }

    /**
     * Enables the keepInventory gamerule on the player's world when they join
     * if they have purchased the Keepinventory perk.
     */
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (playerData.hasPerk(event.getPlayer().getUniqueId(), "Keepinventory")) {
            event.getPlayer().getWorld().setGameRule(GameRule.KEEP_INVENTORY, true);
        }
    }
}
