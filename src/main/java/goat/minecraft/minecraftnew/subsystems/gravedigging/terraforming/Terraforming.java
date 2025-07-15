package goat.minecraft.minecraftnew.subsystems.gravedigging.terraforming;

import goat.minecraft.minecraftnew.MinecraftNew;
import goat.minecraft.minecraftnew.utils.devtools.XPManager;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

/**
 * Awards Terraforming XP when players mine blocks that are exposed to the sky.
 */
public class Terraforming implements Listener {
    private final XPManager xpManager = new XPManager(MinecraftNew.getInstance());

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();
        Location loc = block.getLocation();
        World world = loc.getWorld();
        if (world == null) return;
        int highest = world.getHighestBlockYAt(loc);
        if (loc.getBlockY() >= highest) {
            xpManager.addXP(player, "Terraforming", 1);
        }
    }
}
