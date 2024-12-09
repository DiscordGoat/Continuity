package goat.minecraft.minecraftnew.subsystems.smithing.tierreforgelisteners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Random;

/**
 * Listener class to handle tool durability reduction based on reforge tiers.
 */
public class ToolReforge implements Listener {

    private final ReforgeManager reforgeManager;
    private final Random random;

    /**
     * Constructor to initialize the ReforgeManager.
     *
     * @param reforgeManager Instance of ReforgeManager.
     */
    public ToolReforge(ReforgeManager reforgeManager) {
        this.reforgeManager = reforgeManager;
        this.random = new Random();
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

        // Retrieve the reforge tier from the tool
        int reforgeTierNumber = reforgeManager.getReforgeTier(tool);
        ReforgeManager.ReforgeTier tier = reforgeManager.getReforgeTierByTier(reforgeTierNumber);

        if (tier == null || tier.getToolDurabilityChance() == 0) {
            return; // No durability chance improvement
        }

        int chance = tier.getToolDurabilityChance();

        // Generate a random number between 1 and 100
        int roll = random.nextInt(100) + 1;

        if (roll <= chance) {
            // Prevent durability loss
            preventDurabilityLoss(tool);
            // Optional: Notify the player
        }
        // Else, durability will decrease normally
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
