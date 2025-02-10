package goat.minecraft.minecraftnew.subsystems.pets.perks;

import goat.minecraft.minecraftnew.MinecraftNew;
import goat.minecraft.minecraftnew.subsystems.pets.PetManager;
import goat.minecraft.minecraftnew.utils.ItemRegistry;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class AutoComposter {

    private final MinecraftNew plugin;

    /**
     * Crops eligible for auto-composting.
     */
    private final Set<Material> AUTO_COMPOSTER_ELIGIBLE_CROPS = EnumSet.of(
            Material.POTATO,
            Material.CARROT,
            Material.WHEAT,
            Material.WHEAT_SEEDS,
            Material.BEETROOT_SEEDS,
            Material.POISONOUS_POTATO,
            Material.MELON_SLICE,
            Material.PUMPKIN,
            Material.BEETROOT,
            Material.PUMPKIN_SEEDS
            // Add more crops if desired
    );

    private final Map<Player, Location> playerLastLocations = new HashMap<>();

    public AutoComposter(MinecraftNew plugin) {
        this.plugin = plugin;
        startAutoComposterTask();
    }

    /**
     * Starts the scheduled task that runs every ~3 seconds (adjust as you like).
     */
    private void startAutoComposterTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    Location currentLocation = player.getLocation();
                    Location lastLocation = playerLastLocations.get(player);

                    // Check if player is moving (this method returns true for all, adjust if needed)
                    boolean isMoving = isPlayerMoving(lastLocation, currentLocation);

                    if (isMoving) {
                        // Only run if player's active pet has the COMPOSTER perk
                        if (hasComposterPetPerk(player)) {
                            performConversion(player);
                        }
                    }

                    // Update the player's last known location
                    playerLastLocations.put(player, currentLocation.clone());
                }
            }
        }.runTaskTimer(plugin, 0L, 20L * 3); // every 3 seconds (60 ticks = 3s)
    }

    /**
     * Determines if the player is moving based on their last and current locations.
     *
     * @param last    The last known location.
     * @param current The current location.
     * @return True if the player has moved, false otherwise.
     */
    private boolean isPlayerMoving(Location last, Location current) {
        // Simplified to always 'true' in your sample. Replace with actual logic if needed:
        return true;
    }

    /**
     * Checks if the player's active pet has the COMPOSTER perk.
     * Replace with your actual logic to detect the perk from your pet system.
     */
    private boolean hasComposterPetPerk(Player player) {
        PetManager petManager = PetManager.getInstance(plugin);
        if(petManager.getActivePet(player) == null){
            return false;
        }
        PetManager.Pet activePet = petManager.getActivePet(player);
        return activePet.hasPerk(PetManager.PetPerk.COMPOSTER);
    }

    /**
     * Gets the composter level from the player's active pet.
     * Replace with your actual logic for retrieving the pet's level.
     */
    private int getPetComposterLevel(Player player) {
        PetManager petManager = PetManager.getInstance(plugin);
        PetManager.Pet activePet = petManager.getActivePet(player);
        return activePet.getLevel();
    }

    /**
     * Performs the crop-to-Organic-Soil conversion for the player, using the
     * dynamic "required materials" formula:
     *
     *  requiredMaterialsOrganic = max(256 - (level - 1)*(256 - 64)/99, 64)
     */
    private void performConversion(Player player) {
        // 1) Get the player's composter level from their active pet
        int level = getPetComposterLevel(player);

        // 2) Calculate how many crops are required for 1 organic soil
        int requiredMaterialsOrganic = Math.max(
                256 - (level - 1) * (256 - 64) / 99,
                64
        );

        // 3) Count how many of each eligible crop the player holds
        Map<Material, Integer> playerCropCounts = getPlayerCropCounts(player);

        // 4) For each eligible crop, see how many conversions we can do
        for (Material crop : AUTO_COMPOSTER_ELIGIBLE_CROPS) {
            int playerCropCount = playerCropCounts.getOrDefault(crop, 0);

            if (playerCropCount >= requiredMaterialsOrganic) {
                // Number of times we can convert to organic soil
                int conversions = playerCropCount / requiredMaterialsOrganic;

                // Remove the used crops
                subtractCrops(player, crop, requiredMaterialsOrganic * conversions);

                // Give player the organic soil
                addOrganicSoil(player, conversions);
            }
        }
    }

    /**
     * Retrieves the count of each eligible crop in the player's inventory.
     */
    private Map<Material, Integer> getPlayerCropCounts(Player player) {
        Map<Material, Integer> cropCounts = new HashMap<>();

        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && AUTO_COMPOSTER_ELIGIBLE_CROPS.contains(item.getType())) {
                cropCounts.put(item.getType(),
                        cropCounts.getOrDefault(item.getType(), 0) + item.getAmount());
            }
        }

        return cropCounts;
    }

    /**
     * Subtracts the specified amount of a given crop from the player's inventory.
     */
    private void subtractCrops(Player player, Material crop, int amount) {
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.getType() == crop) {
                if (item.getAmount() > amount) {
                    item.setAmount(item.getAmount() - amount);
                    break;
                } else {
                    amount -= item.getAmount();
                    player.getInventory().remove(item);

                    if (amount <= 0) {
                        break;
                    }
                }
            }
        }
    }

    /**
     * Adds the given number of Organic Soil items to the player's inventory.
     * If full, drops them at the player's feet.
     */
    private void addOrganicSoil(Player player, int quantity) {
        ItemStack organicSoil = ItemRegistry.getOrganicSoil();

        for (int i = 0; i < quantity; i++) {
            Map<Integer, ItemStack> overflow = player.getInventory().addItem(organicSoil.clone());
            if (!overflow.isEmpty()) {
                // If the player's inventory is full, drop on the ground
                player.getWorld().dropItemNaturally(player.getLocation(), organicSoil);
                player.sendMessage(ChatColor.RED
                        + "Your inventory is full! Organic Soil has been dropped on the ground.");
            }
        }
    }
}
