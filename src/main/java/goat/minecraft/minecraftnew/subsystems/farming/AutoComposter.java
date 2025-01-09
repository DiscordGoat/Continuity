package goat.minecraft.minecraftnew.subsystems.farming;

import goat.minecraft.minecraftnew.MinecraftNew;
import goat.minecraft.minecraftnew.utils.ItemRegistry;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class AutoComposter {

    private final MinecraftNew plugin;
    private final Map<Material, Integer> CROPS_TO_CONVERT = new HashMap<>();
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
            // Add more crops if needed
    );
    private final Map<Player, Location> playerLastLocations = new HashMap<>();
    private final ItemStack organicSoilItem;

    public AutoComposter(MinecraftNew plugin) {
        this.plugin = plugin;
        initializeMappings();
        this.organicSoilItem = ItemRegistry.getOrganicSoil();
        startAutoComposterTask();
    }

    /**
     * Initializes the crop to conversion rate mappings.
     */
    private void initializeMappings() {

        CROPS_TO_CONVERT.put(Material.POTATO, 32);

        CROPS_TO_CONVERT.put(Material.CARROT, 32);

        CROPS_TO_CONVERT.put(Material.WHEAT, 32);

        CROPS_TO_CONVERT.put(Material.BEETROOT, 32);

        CROPS_TO_CONVERT.put(Material.BEETROOT_SEEDS, 64);

        CROPS_TO_CONVERT.put(Material.WHEAT_SEEDS, 64);

        CROPS_TO_CONVERT.put(Material.POISONOUS_POTATO, 8);

        CROPS_TO_CONVERT.put(Material.MELON_SLICE, 32);

        CROPS_TO_CONVERT.put(Material.PUMPKIN, 4);
        CROPS_TO_CONVERT.put(Material.PUMPKIN_SEEDS, 16);

        // Add more crops and their required amounts as needed

    }

    /**
     * Starts the scheduled task that runs every 30 seconds.
     */
    private void startAutoComposterTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    Location currentLocation = player.getLocation();
                    Location lastLocation = playerLastLocations.get(player);

                    boolean isMoving = isPlayerMoving(lastLocation, currentLocation);

                    if (isMoving) {
                        if (hasAutoComposter(player)) {
                            performConversion(player);
                        }
                    }

                    // Update the player's last known location
                    playerLastLocations.put(player, currentLocation.clone());
                }
            }
        }.runTaskTimer(plugin, 0L, 20 * 3); // 600 ticks = 30 seconds
    }

    /**
     * Determines if the player is moving based on their last and current locations.
     *
     * @param last    The last known location.
     * @param current The current location.
     * @return True if the player has moved, false otherwise.
     */
    private boolean isPlayerMoving(Location last, Location current) {
        return true;
    }

    /**
     * Checks if the player has the Auto Composter item in their inventory.
     *
     * @param player The player to check.
     * @return True if the player has the Auto Composter, false otherwise.
     */
    private boolean hasAutoComposter(Player player) {

        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && isAutoComposterItem(item)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Determines if the given ItemStack is the Auto Composter item.
     *
     * @param item The ItemStack to check.
     * @return True if it's the Auto Composter, false otherwise.
     */
    private boolean isAutoComposterItem(ItemStack item) {
        if (!item.hasItemMeta()) {

            return false;
        }
        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) {

            return false;
        }
        String itemName = meta.getDisplayName();
        boolean isAutoComposter = (ChatColor.YELLOW + "Auto-Composter").equals(itemName);
        return isAutoComposter;
    }

    /**
     * Performs the crop to Organic Soil conversion for the player.
     *
     * @param player The player performing the conversion.
     */
    private void performConversion(Player player) {
        Map<Material, Integer> playerCropCounts = getPlayerCropCounts(player);

        for (Map.Entry<Material, Integer> entry : CROPS_TO_CONVERT.entrySet()) {
            Material crop = entry.getKey();
            int requiredAmount = entry.getValue();

            int playerCropCount = playerCropCounts.getOrDefault(crop, 0);
            if (playerCropCount >= requiredAmount) {
                int conversions = playerCropCount / requiredAmount;


                // Subtract the required crops
                subtractCrops(player, crop, requiredAmount * conversions);

                // Add Organic Soil blocks
                addOrganicSoil(player, conversions);

            }

        }
    }

    /**
     * Retrieves the count of each eligible crop in the player's inventory.
     *
     * @param player The player to check.
     * @return A map of crop materials to their counts.
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
     * Subtracts a specific amount of a crop from the player's inventory.
     *
     * @param player The player whose inventory is to be modified.
     * @param crop   The crop material to subtract.
     * @param amount The total amount to subtract.
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
                    if (amount <= 0) break;
                }
            }
        }
    }

    /**
     * Adds Organic Soil blocks to the player's inventory.
     *
     * @param player   The player to receive Organic Soil.
     * @param quantity The number of Organic Soil blocks to add.
     */
    private void addOrganicSoil(Player player, int quantity) {
        ItemStack organicSoil = ItemRegistry.getOrganicSoil();

        for (int i = 0; i < quantity; i++) {
            HashMap<Integer, ItemStack> overflow = player.getInventory().addItem(organicSoil.clone());
            if (!overflow.isEmpty()) {
                // Inventory is full, drop the item at the player's location
                player.getWorld().dropItemNaturally(player.getLocation(), organicSoil);
                player.sendMessage(ChatColor.RED + "Your inventory is full! Organic Soil has been dropped on the ground.");
            }
        }
    }
}
