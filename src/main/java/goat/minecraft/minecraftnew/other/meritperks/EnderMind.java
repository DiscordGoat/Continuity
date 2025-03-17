package goat.minecraft.minecraftnew.other.meritperks;

import goat.minecraft.minecraftnew.utils.devtools.PlayerMeritManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class EnderMind implements Listener {

    private final PlayerMeritManager playerData;
    // Store players who teleported with an ender pearl and should be immune to its damage.
    private final Set<UUID> immunePlayers = new HashSet<>();

    public EnderMind(PlayerMeritManager playerData) {
        this.playerData = playerData;
    }

    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        // Check if the teleport was triggered by an ender pearl
        if (event.getCause() == TeleportCause.ENDER_PEARL) {
            Player player = event.getPlayer();
            // If the player has the EnderMind perk, mark them as immune to damage from the pearl
            // and feed them to max hunger and saturation.
            if (playerData.hasPerk(player.getUniqueId(), "EnderMind")) {
                immunePlayers.add(player.getUniqueId());
                player.setFoodLevel(20);
                player.setSaturation(20.0f);
            }
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        // Only apply to players
        if (!(event.getEntity() instanceof Player)) return;
        Player player = (Player) event.getEntity();
        // If the player is marked as immune (recently teleported via ender pearl), cancel the damage event.
        if (immunePlayers.contains(player.getUniqueId())) {
            event.setCancelled(true);
            // Remove the player from the set so that immunity applies only once.
            immunePlayers.remove(player.getUniqueId());
        }
    }
}
