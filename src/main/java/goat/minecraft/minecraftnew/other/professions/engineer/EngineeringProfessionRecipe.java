package goat.minecraft.minecraftnew.other.professions.engineer;

import goat.minecraft.minecraftnew.utils.devtools.ItemRegistry;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.plugin.java.JavaPlugin;

public class EngineeringProfessionRecipe {

    private final JavaPlugin plugin;

    public EngineeringProfessionRecipe(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void registerRecipe() {
        // Create your "Engineering Profession" item
        ItemStack engineeringItem = ItemRegistry.getEngineeringDegree();
        NamespacedKey key = new NamespacedKey(plugin, "engineering_profession");

        // Create the shaped recipe: 3×3 redstone blocks
        ShapedRecipe recipe = new ShapedRecipe(key, engineeringItem);
        recipe.shape("RRR", "RRR", "RRR");

        // R = Redstone Block
        recipe.setIngredient('R', Material.REDSTONE_BLOCK);

        // Register this recipe with the server
        Bukkit.addRecipe(recipe);
    }
}
