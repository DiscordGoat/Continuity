package goat.minecraft.minecraftnew.subsystems.smithing.tierreforgelisteners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * Listener class to handle tool durability reduction based on reforge tiers.
 */
public class ToolReforge implements Listener {

    private final ReforgeManager reforgeManager;

    /**
     * Constructor to initialize the ReforgeManager.
     *
     * @param reforgeManager Instance of ReforgeManager.
     */
    public ToolReforge(ReforgeManager reforgeManager) {
        this.reforgeManager = reforgeManager;
    }

    /**
     * Event handler to modify tool durability consumption based on the tool's reforge tier.
     *
     * @param event The BlockBreakEvent.
     */
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        ItemStack tool = player.getInventory().getItemInMainHand();

        // Check if the item is a tool
        if (!reforgeManager.isTool(tool)) {
            return;
        }

        // Listener retained for backwards compatibility; functionality handled in ReforgeManager
        return;
    }

    /**
     * Prevents durability loss for the given tool.
     *
     * @param tool The tool ItemStack.
     */
    private void preventDurabilityLoss(ItemStack tool) {
        // Since BlockBreakEvent already decreases durability, we need to reverse it
        ItemMeta meta = tool.getItemMeta();
        if (meta == null) {
            return;
        }

        int currentDurability = tool.getDurability();
        if (currentDurability > 0) {
            tool.setDurability((short) (currentDurability - 1));
        }
    }

    /**
     * Retrieves the display name of an item, defaulting to its material name if no name is set.
     *
     * @param item The ItemStack to check.
     * @return The display name of the item.
     */
    private String getItemDisplayName(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta != null && meta.hasDisplayName()) {
            return meta.getDisplayName();
        }
        return item.getType().toString().replace("_", " ").toLowerCase();
    }
}
