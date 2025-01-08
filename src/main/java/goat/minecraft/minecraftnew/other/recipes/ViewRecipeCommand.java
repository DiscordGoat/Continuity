package goat.minecraft.minecraftnew.other.recipes;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class ViewRecipeCommand implements CommandExecutor {

    private final RecipeManager recipeManager;

    public ViewRecipeCommand(RecipeManager recipeManager) {
        this.recipeManager = recipeManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Players only.");
            return true;
        }
        Player player = (Player) sender;

        if (args.length < 1) {
            player.sendMessage(ChatColor.RED + "Usage: /viewrecipe <namespace:key>");
            return true;
        }

        // Manually parse <namespace:key> to avoid 1.18+ fromString() issues
        String[] parts = args[0].split(":");
        if (parts.length < 2) {
            player.sendMessage(ChatColor.RED + "Invalid NamespacedKey format. Use <namespace>:<key>");
            return true;
        }

        NamespacedKey key = new NamespacedKey(parts[0], parts[1]);

        // Attempt to retrieve the recipe from our manager
        Recipe recipe = recipeManager.getCustomRecipes().get(key);
        if (recipe == null) {
            player.sendMessage(ChatColor.RED + "No recipe found for key: " + args[0]);
            return true;
        }

        openRecipeGUI(player, recipe, key);
        return true;
    }

    /**
     * Opens a 54-slot GUI to display the recipe, shifted down by 1 row.
     */
    private void openRecipeGUI(Player player, Recipe recipe, NamespacedKey key) {
        Inventory gui = Bukkit.createInventory(null, 54, ChatColor.DARK_GREEN + "Recipe: " + key.getKey());

        // Fill with filler
        ItemStack filler = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta fillerMeta = filler.getItemMeta();
        if (fillerMeta != null) {
            fillerMeta.setDisplayName(" ");
            filler.setItemMeta(fillerMeta);
        }
        for (int i = 0; i < gui.getSize(); i++) {
            gui.setItem(i, filler);
        }

        // If it's a shaped recipe, place items in the second row downward
        // That means row 0 of the shape -> inventory row 1, row 1 -> inventory row 2, etc.
        if (recipe instanceof ShapedRecipe) {
            ShapedRecipe shaped = (ShapedRecipe) recipe;
            String[] shape = shaped.getShape(); // Typically 3 lines for a 3x3

            // We'll place the pattern starting at row 1 -> slots 9..11, row 2 -> 18..20, row 3 -> 27..29
            // general slot formula: baseSlot = (rowIndex + 1) * 9
            for (int row = 0; row < shape.length; row++) {
                String line = shape[row];
                for (int col = 0; col < line.length(); col++) {
                    char symbol = line.charAt(col);
                    ItemStack ingredient = shaped.getIngredientMap().get(symbol);
                    if (ingredient != null && ingredient.getType() != Material.AIR) {
                        int slotIndex = ((row + 1) * 9) + col;
                        gui.setItem(slotIndex, new ItemStack(ingredient.getType()));
                    }
                }
            }
        } else if (recipe instanceof ShapelessRecipe) {
            // Handle shapeless recipes if needed
        } else if (recipe instanceof FurnaceRecipe) {
            // Handle furnace recipes if needed
        }

        // Place the recipe result in bottom-right corner (slot 53)
        ItemStack result = recipe.getResult().clone();
        ItemMeta meta = result.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.GREEN + "Result");
            result.setItemMeta(meta);
        }
        gui.setItem(53, result);

        player.openInventory(gui);
    }


    public static class ViewRecipeListener implements Listener {

        @EventHandler
        public void onInventoryClick(InventoryClickEvent event) {
            // Check if the inventory title starts with "Recipe: "
            String title = ChatColor.stripColor(event.getView().getTitle());
            if (title.startsWith("Recipe: ")) {
                // Cancel clicks
                event.setCancelled(true);
            }
        }
    }

}
