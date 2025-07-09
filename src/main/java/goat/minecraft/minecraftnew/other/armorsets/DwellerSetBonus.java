package goat.minecraft.minecraftnew.other.armorsets;

import goat.minecraft.minecraftnew.MinecraftNew;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.Material;
import org.bukkit.event.block.BlockBreakEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

/**
 * Applies the Dweller full set bonus while the player is wearing the full set.
 * Adds a 25% chance to drop an additional item when mining and adds 500 oxygen to the player's capacity.
 */
public class DwellerSetBonus implements Listener {

    private final Map<UUID, Boolean> applied = new HashMap<>();
    private final Random random = new Random();





    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        
        if (!applied.getOrDefault(player.getUniqueId(), false)) {
            return;
        }

        // Check if it's an ore block
        Material blockType = event.getBlock().getType();
        boolean isOre = blockType.name().contains("_ORE") || 
                       blockType == Material.ANCIENT_DEBRIS ||
                       blockType == Material.NETHER_QUARTZ_ORE ||
                       blockType == Material.NETHER_GOLD_ORE;

        if (isOre) {
            // Activate Flow on ore breaks
            FlowManager flowManager = FlowManager.getInstance(MinecraftNew.getInstance());
            flowManager.addFlowStacks(player, 1);
        }

        // 25% chance to drop an additional item
        if (random.nextDouble() < 0.25) {
            if (!event.getBlock().getDrops(player.getInventory().getItemInMainHand()).isEmpty()) {
                // Add one more of each drop type
                event.getBlock().getDrops(player.getInventory().getItemInMainHand()).forEach(drop -> {
                    event.getBlock().getWorld().dropItemNaturally(event.getBlock().getLocation(), drop.clone());
                });
            }
        }
    }

    /**
     * Returns the oxygen bonus for the given player.
     * Should be called by the PlayerOxygenManager when calculating initial oxygen.
     */
    public static int getOxygenBonus(Player player) {
        DwellerSetBonus instance = getInstance();
        if (instance != null && instance.applied.getOrDefault(player.getUniqueId(), false)) {
            return 500; // 500 oxygen bonus
        }
        return 0;
    }

    private static DwellerSetBonus instance;
    
    private static DwellerSetBonus getInstance() {
        return instance;
    }


    /**
     * Removes all active bonuses. Called on plugin disable to clean up.
     */
    public void removeAllBonuses() {
        applied.clear();
        instance = null;
    }
}