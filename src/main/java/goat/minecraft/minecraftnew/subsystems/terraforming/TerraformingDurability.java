package goat.minecraft.minecraftnew.subsystems.terraforming;

import goat.minecraft.minecraftnew.MinecraftNew;
import goat.minecraft.minecraftnew.utils.devtools.XPManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemDamageEvent;

/**
 * Grants a chance to prevent durability loss based on the player's Terraforming level.
 */
public class TerraformingDurability implements Listener {
    private final XPManager xpManager = new XPManager(MinecraftNew.getInstance());

    @EventHandler
    public void onItemDamage(PlayerItemDamageEvent event) {
        Player player = event.getPlayer();
        int level = xpManager.getPlayerLevel(player, "Terraforming");
        double chance = level * 0.0025; // (0.25 * level)% chance

        if (Math.random() < chance) {
            event.setCancelled(true);
        }
    }
}
