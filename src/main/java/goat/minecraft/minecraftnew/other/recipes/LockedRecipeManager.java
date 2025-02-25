package goat.minecraft.minecraftnew.other.recipes;

import goat.minecraft.minecraftnew.other.additionalfunctionality.Collections;
import goat.minecraft.minecraftnew.other.additionalfunctionality.Collections.CollectionData;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Manages "locked" recipes that require the player to have completed
 * a certain Collection before being able to craft them.
 */
public class LockedRecipeManager implements Listener {

    // Maps each recipe key to the name of the required collection
    private final Map<NamespacedKey, String> lockedRecipes = new HashMap<>();

    private final JavaPlugin plugin;
    private final Collections collectionsManager;

    public LockedRecipeManager(JavaPlugin plugin, Collections collectionsManager) {
        this.plugin = plugin;
        this.collectionsManager = collectionsManager;
    }

    /**
     * Must be called from onEnable() to register event listeners.
     */
    public void init() {
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    /**
     * Registers a locked recipe that requires a given collection to be "claimed."
     * @param key                 The NamespacedKey of the recipe
     * @param requiredCollection  The name of the collection required to craft
     * @param recipe              The actual recipe object (ShapedRecipe, FurnaceRecipe, etc.)
     * @param addToServer         If true, calls Bukkit.addRecipe(...) so the server knows about it
     */
    public void addLockedRecipe(NamespacedKey key, String requiredCollection, Recipe recipe, boolean addToServer) {
        lockedRecipes.put(key, requiredCollection);
        if (addToServer) {
            Bukkit.addRecipe(recipe);
        }
    }

    /**
     * When the crafting grid is prepared, we check if the recipe is locked.
     * If locked and the player hasn't unlocked the needed collection, we block crafting.
     */
    @EventHandler
    public void onPrepareItemCraft(PrepareItemCraftEvent event) {
        if (!(event.getView().getPlayer() instanceof Player)) {
            return; // Not a player (edge case: NPC or console, etc.)
        }
        Player player = (Player) event.getView().getPlayer();

        Recipe recipe = event.getRecipe();
        if (recipe == null) return;

        // Identify the NamespacedKey of this recipe (if it's a ShapedRecipe, etc.)
        NamespacedKey key = getKeyFromRecipe(recipe);
        if (key == null) return;

        // Is it in our locked recipes map?
        String requiredCollection = lockedRecipes.get(key);
        if (requiredCollection == null) {
            return; // Not locked
        }

        // Check if the player has the required collection unlocked (claimed)
        if (!playerHasCollection(player, requiredCollection)) {
            // Block crafting by removing the result
            event.getInventory().setResult(null);
        }
    }

    /**
     * Attempts to get a NamespacedKey from the given Recipe (handles ShapedRecipe, etc.)
     */
    private NamespacedKey getKeyFromRecipe(Recipe recipe) {
        if (recipe instanceof ShapedRecipe) {
            return ((ShapedRecipe) recipe).getKey();
        }
        // If you have other recipe types (FurnaceRecipe, BlastingRecipe, etc.), handle them similarly
        return null;
    }

    /**
     * Returns whether the player has "unlocked" the specified collection.
     * Here, we define "unlocked" as meaning the player has claimed it.
     */
    private boolean playerHasCollection(Player player, String collectionName) {
        Optional<CollectionData> matchingCollection = collectionsManager.getAllCollections().stream()
                .filter(c -> c.name.equalsIgnoreCase(collectionName))
                .findFirst();

        if (!matchingCollection.isPresent()) {
            // No matching collection means let's block (or return true if you want a fallback).
            return false;
        }

        CollectionData cData = matchingCollection.get();
        return cData.claimedPlayers.contains(player.getUniqueId());
    }

    /**
     * Optionally make the recipe appear in the player's recipe book
     * (call this once they unlock a collection).
     */
    public void discoverRecipeForPlayer(Player player, NamespacedKey key) {
        player.discoverRecipe(key);
    }

    /**
     * Optionally remove from their recipe book if you want to hide it.
     */
    public void undiscoverRecipeForPlayer(Player player, NamespacedKey key) {
        player.undiscoverRecipe(key);
    }
}
