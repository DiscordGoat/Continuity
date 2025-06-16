package goat.minecraft.minecraftnew.other.meritperks;

import goat.minecraft.minecraftnew.utils.devtools.PlayerMeritManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemDamageEvent;
import java.util.UUID;

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
        UUID id = player.getUniqueId();

        double chance = 0.0;
        if (playerData.hasPerk(id, "Unbreaking")) {
            chance += 0.15;
        }

        if (chance > 0 && random.nextDouble() < chance) {
            event.setCancelled(true);
        }
    }
}
