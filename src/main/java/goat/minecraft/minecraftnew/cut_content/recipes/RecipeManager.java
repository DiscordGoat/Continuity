package goat.minecraft.minecraftnew.cut_content.recipes;

import goat.minecraft.minecraftnew.utils.devtools.ItemRegistry;
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
        NamespacedKey musicDiscKey = new NamespacedKey(plugin, "custom_music_disc_recipe");

        // Create the music disc item
        ItemStack musicDiscArtifact = ItemRegistry.getMusicDiscArtifact(); // Choose the type of disc you want


        // Define the shaped recipe
        ShapedRecipe musicDiscRecipe = new ShapedRecipe(musicDiscKey, musicDiscArtifact);
        musicDiscRecipe.shape("DDD", "DOD", "DDD");
        musicDiscRecipe.setIngredient('D', Material.DIAMOND);
        musicDiscRecipe.setIngredient('O', Material.OBSIDIAN);

        // Register it with Bukkit
        Bukkit.addRecipe(musicDiscRecipe);

        // Optionally, store it in a customRecipes map if you manage recipes this way
        customRecipes.put(musicDiscKey, musicDiscRecipe);



        NamespacedKey petTrainingKey = new NamespacedKey(plugin, "pet_training_recipe");

        // Create the music disc item
        ItemStack artifact = ItemRegistry.getPetTraining(); // Choose the type of disc you want


        // Define the shaped recipe
        ShapedRecipe petTrainingRecipe = new ShapedRecipe(petTrainingKey, artifact);
        petTrainingRecipe.shape("DDD", "DDD", "DDD");
        petTrainingRecipe.setIngredient('D', Material.LAPIS_BLOCK);

        // Register it with Bukkit
        Bukkit.addRecipe(petTrainingRecipe);

        // Optionally, store it in a customRecipes map if you manage recipes this way
        customRecipes.put(petTrainingKey, petTrainingRecipe);



        NamespacedKey notchAppleKey = new NamespacedKey(plugin, "notch_apple_recipe");

        // 2) Define your custom shaped recipe
        // For example, 8 Gold Blocks + 1 Apple in the middle:
        ItemStack enchantedGoldenApple = new ItemStack(Material.ENCHANTED_GOLDEN_APPLE);
        ShapedRecipe notchAppleRecipe = new ShapedRecipe(notchAppleKey, enchantedGoldenApple);
        notchAppleRecipe.shape("GGG", "GAG", "GGG");
        notchAppleRecipe.setIngredient('G', Material.GOLD_BLOCK);
        notchAppleRecipe.setIngredient('A', Material.APPLE);

        // 3) Register it with Bukkit
        Bukkit.addRecipe(notchAppleRecipe);

        // 4) Store it in our customRecipes map
        customRecipes.put(notchAppleKey, notchAppleRecipe);





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
