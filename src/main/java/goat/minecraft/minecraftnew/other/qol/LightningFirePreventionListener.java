package goat.minecraft.minecraftnew.other.qol;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockIgniteEvent.IgniteCause;

public class LightningFirePreventionListener implements Listener {

    @EventHandler
    public void onBlockIgnite(BlockIgniteEvent event) {
        // Check if the cause of the ignition is lightning
        if (event.getCause() == IgniteCause.LIGHTNING) {
            event.setCancelled(true);  // Prevent the block from igniting
        }
    }
}
