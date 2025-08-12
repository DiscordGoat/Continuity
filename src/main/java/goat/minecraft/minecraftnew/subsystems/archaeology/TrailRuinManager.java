package goat.minecraft.minecraftnew.subsystems.archaeology;

import goat.minecraft.minecraftnew.subsystems.fishing.Rarity;
import goat.minecraft.minecraftnew.utils.devtools.ItemRegistry;
import org.bukkit.NamespacedKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.LootGenerateEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.loot.LootTable;

import java.util.List;

/**
 * Replaces Trail Ruins archaeology loot (suspicious sand & gravel)
 * with items from TrailRuinRegistry, preserving vanilla brushing.
 */
public final class TrailRuinManager implements Listener {

    public TrailRuinManager() {
        // move your seeds here (and remove them from graves)
        TrailRuinRegistry.addItem(ItemRegistry.getVerdantRelicEntionPlastSeed(), Rarity.UNCOMMON);
        TrailRuinRegistry.addItem(ItemRegistry.getVerdantRelicEntropySeed(),     Rarity.UNCOMMON);
        TrailRuinRegistry.addItem(ItemRegistry.getVerdantRelicSunflareSeed(),    Rarity.UNCOMMON);
        TrailRuinRegistry.addItem(ItemRegistry.getVerdantRelicStarlightSeed(),   Rarity.UNCOMMON);
        TrailRuinRegistry.addItem(ItemRegistry.getVerdantRelicTideSeed(),        Rarity.UNCOMMON);
        TrailRuinRegistry.addItem(ItemRegistry.getVerdantRelicShinyEmeraldSeed(),Rarity.UNCOMMON);
        TrailRuinRegistry.addItem(ItemRegistry.getVerdantRelicTreasury(),        Rarity.RARE);
        TrailRuinRegistry.addItem(ItemRegistry.getVerdantRelicMarrow(),          Rarity.RARE);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onLootGenerate(LootGenerateEvent event) {
        LootTable table = event.getLootTable();
        NamespacedKey key = (table == null) ? null : table.getKey();
        if (key == null) return;

        // Matches both: minecraft:archaeology/trail_ruins_common and ..._rare
        final String path = key.getKey(); // "archaeology/trail_ruins_common"
        if (!path.startsWith("archaeology/") || !path.contains("trail_ruins")) return;

        // Replace vanilla drops with a registry roll
        List<ItemStack> loot = event.getLoot();
        loot.clear();

        ItemStack drop = TrailRuinRegistry.getRandomItem();
        if (drop != null) {
            // Spawn at loot context location
            var loc = event.getLootContext().getLocation();
            if (loc != null && loc.getWorld() != null) {
                loc.getWorld().dropItemNaturally(loc.add(0.5, 0.5, 0.5), drop);
            }
        }
        // Optional: add a second roll sometimes:
        // if (event.getLootContext().getLuck() > 0 && Math.random() < 0.15) loot.add(TrailRuinRegistry.getRandomItem());
    }
}
