package goat.minecraft.minecraftnew.utils.chocolatemisc;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.block.Action;
import org.bukkit.entity.Player;

public class CakeHungerListener implements Listener {

    @EventHandler
    public void onCakeRightClick(PlayerInteractEvent event) {
        // Check if the action is a right-click on a block
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            // Check if the clicked block is a cake
            if (event.getClickedBlock() != null && event.getClickedBlock().getType() == Material.CAKE) {
                Player player = event.getPlayer();
                // Check if the player has less than full hunger
                if (player.getFoodLevel() < 20) {
                    // Set the player's hunger and saturation to full
                    player.setFoodLevel(19);
                    player.setSaturation(20);
                }
            }
        }
    }
}
