package goat.minecraft.minecraftnew.subsystems.pets.perks;

import goat.minecraft.minecraftnew.MinecraftNew;
import goat.minecraft.minecraftnew.subsystems.pets.PetManager;
import goat.minecraft.minecraftnew.utils.devtools.ItemRegistry;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;

public class Groot implements Listener {
    private final MinecraftNew plugin = MinecraftNew.getInstance();
    private final Random random = new Random();

    // List of wood-based materials to monitor
    public static final List<Material> woodMaterials = Arrays.asList(
            Material.OAK_LOG,
            Material.BIRCH_LOG,
            Material.SPRUCE_LOG,
            Material.JUNGLE_LOG,
            Material.ACACIA_LOG,
            Material.DARK_OAK_LOG,
            Material.MANGROVE_LOG,
            Material.WARPED_STEM,
            Material.CRIMSON_STEM,
            Material.OAK_SAPLING,
            Material.BIRCH_SAPLING,
            Material.SPRUCE_SAPLING,
            Material.JUNGLE_SAPLING,
            Material.ACACIA_SAPLING,
            Material.DARK_OAK_SAPLING,
            Material.MANGROVE_PROPAGULE,
            Material.STICK
    );
    public void compactWoodMaterials(Player player) {
        // Get the active pet level if the player has one
        PetManager petManager = PetManager.getInstance(plugin);
        int petLevel = petManager.getActivePet(player) != null ? petManager.getActivePet(player).getLevel() : 1;

        // Calculate the required amount of materials based on pet level
        int requiredMaterials = Math.max(256 - (petLevel - 1) * (256 - 64) / 99, 64);

        // Count total wood-based materials in the player's inventory
        int totalWoodCount = 0;
        for (Material material : woodMaterials) {
            totalWoodCount += countMaterialInInventory(player, material);
        }

        // Check if the player has enough materials to compact
        if (totalWoodCount >= requiredMaterials) {
            // Remove the required amount of wood-based materials from inventory
            removeMaterialsFromInventory(player, woodMaterials, requiredMaterials);

            // Give the player the custom Compact Wood item
            giveCompactWood(player);

            // Notify the player
            player.playSound(player.getLocation(), Sound.BLOCK_WOOD_BREAK, 1.0f, 1.0f);
        }
    }
    private int countMaterialInInventory(Player player, Material material) {
        ItemStack[] items = player.getInventory().getContents();
        int count = 0;
        for (ItemStack item : items) {
            if (item != null && item.getType() == material) {
                count += item.getAmount();
            }
        }
        return count;
    }
    private void removeMaterialsFromInventory(Player player, List<Material> materials, int count) {
        for (Material material : materials) {
            if (count <= 0) break;
            count = removeMaterialFromInventory(player, material, count);
        }
    }
    private int removeMaterialFromInventory(Player player, Material material, int count) {
        ItemStack[] items = player.getInventory().getContents();
        for (int i = 0; i < items.length; i++) {
            ItemStack item = items[i];
            if (item != null && item.getType() == material) {
                int inStack = item.getAmount();
                if (inStack > count) {
                    item.setAmount(inStack - count);
                    return 0;
                } else {
                    player.getInventory().clear(i);
                    count -= inStack;
                }
            }
        }
        return count;
    }
    private void giveCompactWood(Player player) {
        ItemStack compactWood = ItemRegistry.getCompactWood();
        player.getInventory().addItem(compactWood);
    }
    @EventHandler
    public void onWoodHarvest(BlockBreakEvent event) {
        Block block = event.getBlock();
        Player player = event.getPlayer();

        if (woodMaterials.contains(block.getType())) {
            int silkWormChance = random.nextInt(2400) + 1; // Generates a number between 1 and 2400

            // Implement a fixed 1/2400 chance
            if (silkWormChance == 1) {
                // Drop the custom Silk Worm item at the block's location
                event.getBlock().getLocation().getWorld().dropItem(event.getBlock().getLocation(), ItemRegistry.getSilkWorm());

                // Send a message to the player
                player.sendMessage(ChatColor.DARK_PURPLE + "A small insect falls to the ground...");

                // Play a sound effect to the player
                player.playSound(player.getLocation(), Sound.BLOCK_WET_GRASS_BREAK, 1.0f, 1.0f);
            }

            PetManager petManager = PetManager.getInstance(MinecraftNew.getInstance());
            PetManager.Pet activePet = petManager.getActivePet(player);
            if (activePet != null && activePet.hasPerk(PetManager.PetPerk.GROOT)) {
                // Compact wood materials
                compactWoodMaterials(player);
            }
        }
    }
}
