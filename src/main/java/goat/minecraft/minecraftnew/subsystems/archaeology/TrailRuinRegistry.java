package goat.minecraft.minecraftnew.subsystems.archaeology;

import goat.minecraft.minecraftnew.subsystems.fishing.Rarity;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Registry of items that can be obtained from brushing
 * suspicious sand or gravel within trail ruins.
 * Only items registered here are eligible to drop.
 */
public final class TrailRuinRegistry {

    private static final Map<Rarity, List<ItemStack>> ITEMS = new EnumMap<>(Rarity.class);
    private static final Map<Rarity, Double> RARITY_WEIGHTS = new EnumMap<>(Rarity.class);

    static {
        for (Rarity r : Rarity.values()) {
            ITEMS.put(r, new ArrayList<>());
        }
        // Rough weights matching the corpse rarity distribution
        RARITY_WEIGHTS.put(Rarity.COMMON, 0.50);
        RARITY_WEIGHTS.put(Rarity.UNCOMMON, 0.30);
        RARITY_WEIGHTS.put(Rarity.RARE, 0.12);
        RARITY_WEIGHTS.put(Rarity.EPIC, 0.06);
        RARITY_WEIGHTS.put(Rarity.LEGENDARY, 0.02);
        RARITY_WEIGHTS.put(Rarity.MYTHIC, 0.0);
    }

    private TrailRuinRegistry() {}

    /**
     * Adds an item to the registry under the given rarity.
     */
    public static void addItem(ItemStack stack, Rarity rarity) {
        if (stack == null || rarity == null) return;
        ITEMS.get(rarity).add(stack);
    }

    /**
     * Returns a random item from the registry using the rarity weights.
     * Returns null if no items are registered.
     */
    public static ItemStack getRandomItem() {
        if (ITEMS.values().stream().allMatch(List::isEmpty)) {
            return null;
        }
        Rarity rarity = pickWeightedRarity();
        List<ItemStack> list = ITEMS.get(rarity);
        if (list.isEmpty()) {
            // Fallback to any available item
            List<ItemStack> flat = new ArrayList<>();
            ITEMS.values().forEach(flat::addAll);
            if (flat.isEmpty()) return null;
            return flat.get(ThreadLocalRandom.current().nextInt(flat.size())).clone();
        }
        return list.get(ThreadLocalRandom.current().nextInt(list.size())).clone();
    }

    private static Rarity pickWeightedRarity() {
        double rand = ThreadLocalRandom.current().nextDouble();
        double cumulative = 0.0;
        for (Map.Entry<Rarity, Double> entry : RARITY_WEIGHTS.entrySet()) {
            cumulative += entry.getValue();
            if (rand <= cumulative) {
                return entry.getKey();
            }
        }
        return Rarity.COMMON;
    }
}
