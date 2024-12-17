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
            Material.BEETROOT
            // Add more crops if needed
    );
    private final Map<Player, Location> playerLastLocations = new HashMap<>();
    private final ItemStack organicSoilItem;

    public AutoComposter(MinecraftNew plugin) {
        this.plugin = plugin;
        plugin.getLogger().info("[AutoComposter] Initializing Auto Composter...");
        initializeMappings();
        this.organicSoilItem = ItemRegistry.getOrganicSoil();
        plugin.getLogger().info("[AutoComposter] Organic Soil Item initialized.");
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

        // Add more crops and their required amounts as needed

    }

    /**
     * Starts the scheduled task that runs every 30 seconds.
     */
    private void startAutoComposterTask() {
        plugin.getLogger().info("[AutoComposter] Starting Auto Composter scheduled task (every 30 seconds).");
        new BukkitRunnable() {
            @Override
            public void run() {
                plugin.getLogger().info("[AutoComposter] Running Auto Composter task...");
                for (Player player : Bukkit.getOnlinePlayers()) {
                    Location currentLocation = player.getLocation();
                    Location lastLocation = playerLastLocations.get(player);

                    boolean isMoving = isPlayerMoving(lastLocation, currentLocation);
                    plugin.getLogger().info("[AutoComposter] Checking player: " + player.getName() + " | Moving: " + isMoving);

                    if (isMoving) {
                        if (hasAutoComposter(player)) {
                            plugin.getLogger().info("[AutoComposter] Player " + player.getName() + " has Auto Composter. Performing conversion.");
                            performConversion(player);
                        } else {
                            plugin.getLogger().info("[AutoComposter] Player " + player.getName() + " does NOT have Auto Composter.");
                        }
                    } else {
                        plugin.getLogger().info("[AutoComposter] Player " + player.getName() + " is not moving. Skipping conversion.");
                    }

                    // Update the player's last known location
                    playerLastLocations.put(player, currentLocation.clone());
                    plugin.getLogger().info("[AutoComposter] Updated last known location for player: " + player.getName());
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
        plugin.getLogger().info("[AutoComposter] Checking inventory for Auto Composter for player: " + player.getName());
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && isAutoComposterItem(item)) {
                plugin.getLogger().info("[AutoComposter] Auto Composter found in inventory for player: " + player.getName());
                return true;
            }
        }
        plugin.getLogger().info("[AutoComposter] No Auto Composter found in inventory for player: " + player.getName());
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
            plugin.getLogger().info("[AutoComposter] Item does not have meta data.");
            return false;
        }
        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) {
            plugin.getLogger().info("[AutoComposter] Item meta is null or does not have a display name.");
            return false;
        }
        String itemName = meta.getDisplayName();
        boolean isAutoComposter = (ChatColor.YELLOW + "Auto-Composter").equals(itemName);
        plugin.getLogger().info("[AutoComposter] Item display name: " + itemName + " | Is Auto Composter: " + isAutoComposter);
        return isAutoComposter;
    }

    /**
     * Performs the crop to Organic Soil conversion for the player.
     *
     * @param player The player performing the conversion.
     */
    private void performConversion(Player player) {
        plugin.getLogger().info("[AutoComposter] Performing conversion for player: " + player.getName());
        Map<Material, Integer> playerCropCounts = getPlayerCropCounts(player);
        plugin.getLogger().info("[AutoComposter] Player " + player.getName() + " has the following crops: " + playerCropCounts.toString());

        for (Map.Entry<Material, Integer> entry : CROPS_TO_CONVERT.entrySet()) {
            Material crop = entry.getKey();
            int requiredAmount = entry.getValue();

            int playerCropCount = playerCropCounts.getOrDefault(crop, 0);
            plugin.getLogger().info("[AutoComposter] - Crop: " + crop.name() + " | Required: " + requiredAmount + " | Player has: " + playerCropCount);

            if (playerCropCount >= requiredAmount) {
                int conversions = playerCropCount / requiredAmount;
                plugin.getLogger().info("[AutoComposter] - Number of conversions: " + conversions);

                // Subtract the required crops
                subtractCrops(player, crop, requiredAmount * conversions);
                plugin.getLogger().info("[AutoComposter] - Subtracted " + (requiredAmount * conversions) + " " + crop.name().toLowerCase() + " from player: " + player.getName());

                // Add Organic Soil blocks
                addOrganicSoil(player, conversions);
                plugin.getLogger().info("[AutoComposter] - Added " + conversions + " Organic Soil to player: " + player.getName());

                // Notify the player
                player.sendMessage(ChatColor.YELLOW + "Converted " + (requiredAmount * conversions) + " " + crop.name().toLowerCase() +
                        " into " + conversions + " Organic Soil.");
            } else {
                plugin.getLogger().info("[AutoComposter] - Player " + player.getName() + " does not have enough " + crop.name().toLowerCase() + " to convert.");
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

        plugin.getLogger().info("[AutoComposter] Counting eligible crops in player " + player.getName() + "'s inventory.");
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && AUTO_COMPOSTER_ELIGIBLE_CROPS.contains(item.getType())) {
                cropCounts.put(item.getType(),
                        cropCounts.getOrDefault(item.getType(), 0) + item.getAmount());
                plugin.getLogger().info("[AutoComposter] - Found " + item.getAmount() + " " + item.getType().name().toLowerCase() + "(s).");
            }
        }

        plugin.getLogger().info("[AutoComposter] Total crops for player " + player.getName() + ": " + cropCounts.toString());
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
        plugin.getLogger().info("[AutoComposter] Subtracting " + amount + " " + crop.name().toLowerCase() + "(s) from player " + player.getName() + ".");
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.getType() == crop) {
                if (item.getAmount() > amount) {
                    item.setAmount(item.getAmount() - amount);
                    plugin.getLogger().info("[AutoComposter] - Subtracted " + amount + " from stack. Remaining in stack: " + item.getAmount());
                    break;
                } else {
                    amount -= item.getAmount();
                    plugin.getLogger().info("[AutoComposter] - Removing entire stack of " + item.getAmount() + " " + crop.name().toLowerCase() + "(s).");
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
        plugin.getLogger().info("[AutoComposter] Adding " + quantity + " Organic Soil(s) to player " + player.getName() + "'s inventory.");
        ItemStack organicSoil = ItemRegistry.getOrganicSoil();

        for (int i = 0; i < quantity; i++) {
            HashMap<Integer, ItemStack> overflow = player.getInventory().addItem(organicSoil.clone());
            if (!overflow.isEmpty()) {
                // Inventory is full, drop the item at the player's location
                player.getWorld().dropItemNaturally(player.getLocation(), organicSoil);
                player.sendMessage(ChatColor.RED + "Your inventory is full! Organic Soil has been dropped on the ground.");
                plugin.getLogger().warning("[AutoComposter] - Player " + player.getName() + "'s inventory is full. Dropped Organic Soil on ground.");
            } else {
                plugin.getLogger().info("[AutoComposter] - Organic Soil added to player " + player.getName() + "'s inventory.");
            }
        }
    }
}
