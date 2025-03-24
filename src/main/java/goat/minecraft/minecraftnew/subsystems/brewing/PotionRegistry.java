package goat.minecraft.minecraftnew.subsystems.brewing;

import goat.minecraft.minecraftnew.utils.devtools.ItemRegistry;
import org.bukkit.inventory.ItemStack;
import java.util.ArrayList;
import java.util.List;

public class PotionRegistry {
    private static final List<CustomPotion> potions = new ArrayList<>();

    public static void registerPotion(CustomPotion potion) {
        potions.add(potion);
    }

    public static List<CustomPotion> getRegisteredPotions() {
        return new ArrayList<>(potions);
    }

    /**
     * Matches the given ingredients (an array of up to 4 ItemStacks) to a registered custom potion.
     * This sample matching logic assumes that if an expected ingredient is not null,
     * then the ingredient in the corresponding slot must be similar.
     */
    public static CustomPotion matchPotion(ItemStack[] ingredients) {
        for (CustomPotion potion : potions) {
            ItemStack[] required = potion.getIngredients();
            boolean match = true;
            for (int i = 0; i < required.length; i++) {
                if (required[i] != null) {
                    // If the provided ingredient is missing or not similar, then not a match.
                    if (ingredients.length <= i || ingredients[i] == null || !required[i].isSimilar(ingredients[i])) {
                        match = false;
                        break;
                    }
                }
            }
            if (match) {
                return potion;
            }
        }
        return null;
    }

    // Initialize with test potions.
    public static void initTestPotions() {
        // For example, Potion of Strength requires Nether Wart and a singularity.
        // ItemRegistry.getSingularity() is used as a placeholder.
        List<String> lore = new ArrayList<>();
        lore.add("Ingredients: Nether Wart, Singularity");
        lore.add("Brew Time: 10s");
        lore.add("Duration: 15s");

        CustomPotion potionOfStrength = new CustomPotion(
                "Potion of Strength",
                lore,
                org.bukkit.Color.RED,
                15,      // duration in seconds
                10,      // brew time in seconds
                new org.bukkit.inventory.ItemStack(org.bukkit.Material.NETHER_WART),
                ItemRegistry.getSingularity(),  // Placeholder; adjust as needed
                null,
                null
        );

        registerPotion(potionOfStrength);
    }
}
