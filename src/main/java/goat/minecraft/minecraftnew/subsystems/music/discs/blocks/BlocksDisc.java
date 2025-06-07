package goat.minecraft.minecraftnew.subsystems.music.discs.blocks;

import goat.minecraft.minecraftnew.subsystems.culinary.CulinarySubsystem;
import goat.minecraft.minecraftnew.subsystems.music.discs.MusicDisc;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class BlocksDisc implements MusicDisc {
    private final JavaPlugin plugin;

    public BlocksDisc(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public Material getDiscMaterial() {
        return Material.MUSIC_DISC_BLOCKS;
    }

    @Override
    public void onUse(Player player) {
        // Broadcast the activation message to all players
        Bukkit.broadcastMessage(ChatColor.GREEN + "Recipe Writer Feature is now active!");

        // Play the MUSIC_DISC_BLOCKS sound to the activating player
        player.playSound(player.getLocation(), org.bukkit.Sound.MUSIC_DISC_BLOCKS, 3.0f, 1.0f);

        // Get all recipe items from the CulinarySubsystem
        List<ItemStack> allRecipeItems = CulinarySubsystem.getInstance(plugin).getAllRecipeItems();

        // Define the total number of recipes to give (32)
        final int totalRecipes = 32;

        // Define the total duration of the song in ticks (345 seconds * 20 ticks per second)
        final long totalDurationTicks = 345 * 20L;

        // Calculate the interval between each recipe drop (in ticks)
        final long intervalTicks = totalDurationTicks / totalRecipes;

        // Schedule a repeating task to give recipes over the duration of the song
        new BukkitRunnable() {
            int recipesGiven = 0;

            @Override
            public void run() {
                if (recipesGiven >= totalRecipes || !player.isOnline()) {
                    this.cancel();
                    player.sendMessage(ChatColor.GREEN + "You have received all 32 random recipes!");
                    return;
                }

                // Randomly select a recipe from the list
                ItemStack recipeItem = allRecipeItems.get(new Random().nextInt(allRecipeItems.size())).clone();

                // Give the recipe to the player
                Map<Integer, ItemStack> remaining = player.getInventory().addItem(recipeItem);
                if (!remaining.isEmpty()) {
                    for (ItemStack leftover : remaining.values()) {
                        player.getWorld().dropItemNaturally(player.getLocation(), leftover);
                    }
                }

                // Notify the player
                ItemMeta meta = recipeItem.getItemMeta();
                String name = meta != null && meta.hasDisplayName() ? meta.getDisplayName() : recipeItem.getType().name();
                player.sendMessage(ChatColor.YELLOW + "You received a recipe: " + name);

                recipesGiven++;
            }
        }.runTaskTimer(plugin, 0L, intervalTicks);
    }
}
