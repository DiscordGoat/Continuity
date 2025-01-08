package goat.minecraft.minecraftnew.other.recipes;

import goat.minecraft.minecraftnew.utils.ItemRegistry;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;

public class RecipeManager {

    private final JavaPlugin plugin;
    // Store your custom recipes in a map for easy retrieval
    private final Map<NamespacedKey, Recipe> customRecipes = new HashMap<>();

    public RecipeManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void registerAllRecipes() {
        // Example: "Redstone" recipe
        NamespacedKey redstoneKey = new NamespacedKey(plugin, "engineering_degree_recipe");
        ShapedRecipe redstoneRecipe = new ShapedRecipe(redstoneKey, ItemRegistry.getEngineeringDegree());
        redstoneRecipe.shape("RRR", "RRR", "RRR");
        redstoneRecipe.setIngredient('R', Material.REDSTONE_BLOCK);

        // Add to Bukkit and store it
        Bukkit.addRecipe(redstoneRecipe);
        customRecipes.put(redstoneKey, redstoneRecipe);


    }

    public Map<NamespacedKey, Recipe> getCustomRecipes() {
        return customRecipes;
    }
}
