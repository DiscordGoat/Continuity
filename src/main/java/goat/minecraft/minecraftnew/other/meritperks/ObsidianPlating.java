package goat.minecraft.minecraftnew.other.meritperks;

import goat.minecraft.minecraftnew.utils.devtools.PlayerMeritManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemDamageEvent;

import java.util.Random;

public class ObsidianPlating implements Listener {

    private final PlayerMeritManager playerData;
    private final Random random = new Random();

    public ObsidianPlating(PlayerMeritManager playerData) {
        this.playerData = playerData;
    }

    @EventHandler
    public void onItemDamage(PlayerItemDamageEvent event) {
        Player player = event.getPlayer();
        // Check if the player has purchased the ObsidianPlating perk
        if (playerData.hasPerk(player.getUniqueId(), "Unbreaking")) {
            // 15% chance to cancel durability loss
            if (random.nextDouble() < 0.15) {
                event.setCancelled(true);
                // Optionally, you can notify the player or trigger a visual effect here.
            }
        }
    }
}
