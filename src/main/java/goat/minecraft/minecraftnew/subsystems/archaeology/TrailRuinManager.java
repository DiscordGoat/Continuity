package goat.minecraft.minecraftnew.subsystems.archaeology;

import goat.minecraft.minecraftnew.subsystems.fishing.Rarity;
import goat.minecraft.minecraftnew.utils.devtools.ItemRegistry;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

/**
 * Handles brushing of suspicious sand and gravel in trail ruins.
 * Drops items from the {@link TrailRuinRegistry} instead of the
 * vanilla loot table.
 */
public class TrailRuinManager implements Listener {

    public TrailRuinManager() {
        // Register default drops such as verdant relic seeds
        TrailRuinRegistry.addItem(ItemRegistry.getVerdantRelicEntionPlastSeed(), Rarity.UNCOMMON);
        TrailRuinRegistry.addItem(ItemRegistry.getVerdantRelicEntropySeed(), Rarity.UNCOMMON);
        TrailRuinRegistry.addItem(ItemRegistry.getVerdantRelicSunflareSeed(), Rarity.UNCOMMON);
        TrailRuinRegistry.addItem(ItemRegistry.getVerdantRelicStarlightSeed(), Rarity.UNCOMMON);
        TrailRuinRegistry.addItem(ItemRegistry.getVerdantRelicTideSeed(), Rarity.UNCOMMON);
        TrailRuinRegistry.addItem(ItemRegistry.getVerdantRelicShinyEmeraldSeed(), Rarity.UNCOMMON);
        TrailRuinRegistry.addItem(ItemRegistry.getVerdantRelicTreasury(), Rarity.RARE);
        TrailRuinRegistry.addItem(ItemRegistry.getVerdantRelicMarrow(), Rarity.RARE);
    }

    @EventHandler
    public void onBrush(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (event.getHand() != EquipmentSlot.HAND) return;

        Block block = event.getClickedBlock();
        if (block == null) return;
        Material type = block.getType();
        if (type != Material.SUSPICIOUS_SAND && type != Material.SUSPICIOUS_GRAVEL) return;

        Player player = event.getPlayer();
        if (player.getInventory().getItemInMainHand().getType() != Material.BRUSH) return;

        // Cancel default brushing behaviour
        event.setCancelled(true);
        block.setType(Material.AIR);

        ItemStack drop = TrailRuinRegistry.getRandomItem();
        if (drop != null) {
            block.getWorld().dropItemNaturally(block.getLocation().add(0.5, 0.5, 0.5), drop);
        }
    }
}
