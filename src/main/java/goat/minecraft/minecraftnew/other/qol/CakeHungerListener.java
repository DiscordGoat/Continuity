package goat.minecraft.minecraftnew.other.qol;

import goat.minecraft.minecraftnew.MinecraftNew;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.block.Action;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class CakeHungerListener implements Listener {

    @EventHandler
    public void onCakeRightClick(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        if (event.getClickedBlock() == null || event.getClickedBlock().getType() != Material.CAKE) {
            return;
        }

        Player player = event.getPlayer();

        // Record the player's saturation before vanilla handles the eating
        float startSaturation = player.getSaturation();

        // Force the player to be able to eat the cake even when at full hunger
        player.setFoodLevel(19);

        // Apply saturation changes after the slice is consumed so the cake block
        // still decreases normally
        new BukkitRunnable() {
            @Override
            public void run() {
                float newSaturation;
                if (startSaturation < 20f) {
                    newSaturation = 20f;
                } else {
                    newSaturation = Math.min(startSaturation + 10f, 60f);
                }
                player.setSaturation(newSaturation);
            }
        }.runTaskLater(MinecraftNew.getInstance(), 1L);
    }
}
