package goat.minecraft.minecraftnew.subsystems.pets.perks;

import goat.minecraft.minecraftnew.subsystems.pets.PetManager;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.EnumMap;
import java.util.Map;
import java.util.Random;

public class Cultivation implements Listener {

    private final PetManager petManager;
    private final Random random = new Random();

    // Crop-to-drop mapping for better extensibility
    private static final Map<Material, Material> CROP_DROPS = new EnumMap<>(Material.class);

    static {
        CROP_DROPS.put(Material.WHEAT, Material.WHEAT);
        CROP_DROPS.put(Material.CARROTS, Material.CARROT);
        CROP_DROPS.put(Material.POTATOES, Material.POTATO);
        CROP_DROPS.put(Material.BEETROOTS, Material.BEETROOT);
        CROP_DROPS.put(Material.NETHER_WART, Material.NETHER_WART);
        CROP_DROPS.put(Material.MELON, Material.MELON);
        CROP_DROPS.put(Material.PUMPKIN, Material.PUMPKIN);
    }

    public Cultivation(JavaPlugin plugin) {
        this.petManager = PetManager.getInstance(plugin);
    }

    @EventHandler
    public void onCropBreak(BlockBreakEvent event) {
        // Ensure the broken block is a valid crop
        Material cropType = event.getBlock().getType();
        if (!CROP_DROPS.containsKey(cropType)) {
            return;
        }

        Player player = event.getPlayer();

        // Check if the player has an active pet with the Cultivation perk
        PetManager.Pet activePet = petManager.getActivePet(player);
        if (activePet == null || !activePet.hasPerk(PetManager.PetPerk.CULTIVATION)) {
            return;
        }

        // Calculate the chance of dropping extra crops based on pet level
        int petLevel = activePet.getLevel();
        int chance = petLevel; // Pet level equals the percentage chance (e.g., level 50 = 50%)

        if (random.nextInt(100) < chance) {
            dropExtraCrops(event.getBlock().getLocation(), cropType);
            player.playSound(player.getLocation(), Sound.BLOCK_WET_GRASS_BREAK, 5, 100);
        }
    }

    private void dropExtraCrops(Location location, Material cropType) {
        Material dropType = CROP_DROPS.get(cropType);
        if (dropType != null) {
            location.getWorld().dropItemNaturally(location, new ItemStack(dropType, 1));
        }
    }
}
