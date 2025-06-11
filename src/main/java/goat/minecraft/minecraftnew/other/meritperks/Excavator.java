package goat.minecraft.minecraftnew.other.meritperks;

import goat.minecraft.minecraftnew.utils.devtools.PlayerMeritManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Random;

/**
 * Excavator merit perk.
 *
 * Grants shovels a 90% chance to not lose durability when used.
 */
public class Excavator implements Listener {

    private final PlayerMeritManager playerData;
    private final Random random = new Random();

    public Excavator(PlayerMeritManager playerData) {
        this.playerData = playerData;
    }

    @EventHandler
    public void onItemDamage(PlayerItemDamageEvent event) {
        Player player = event.getPlayer();
        if (!playerData.hasPerk(player.getUniqueId(), "Excavator")) {
            return;
        }

        ItemStack item = event.getItem();
        if (item == null) {
            return;
        }
        Material type = item.getType();
        if (type.toString().endsWith("_SHOVEL")) {
            if (random.nextDouble() < 0.9) {
                event.setCancelled(true);
            }
        }
    }
}
