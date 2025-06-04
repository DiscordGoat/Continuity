package goat.minecraft.minecraftnew.other.meritperks;

import goat.minecraft.minecraftnew.utils.devtools.PlayerMeritManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.EnumSet;
import java.util.Set;
import java.util.UUID;

/**
 * Unlooting merit perk.
 *
 * When enabled, the player will automatically discard common mob drops
 * like rotten flesh and bones instead of picking them up.
 * This helps keep the inventory clear of clutter.
 */
public class Unlooting implements Listener {

    private final JavaPlugin plugin;
    private final PlayerMeritManager playerData;

    private static final Set<Material> TRASH_ITEMS = EnumSet.of(
            Material.ROTTEN_FLESH,
            Material.BONE,
            Material.ARROW,
            Material.GUNPOWDER,
            Material.STRING,
            Material.SPIDER_EYE
    );

    public Unlooting(JavaPlugin plugin, PlayerMeritManager playerData) {
        this.plugin = plugin;
        this.playerData = playerData;
    }

    @EventHandler
    public void onItemPickup(EntityPickupItemEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        UUID playerId = player.getUniqueId();
        if (!playerData.hasPerk(playerId, "Unlooting")) {
            return;
        }

        ItemStack item = event.getItem().getItemStack();
        if (TRASH_ITEMS.contains(item.getType())) {
            event.setCancelled(true);
            event.getItem().remove();
        }
    }
}
