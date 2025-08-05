package goat.minecraft.minecraftnew.subsystems.pets.perks;

import goat.minecraft.minecraftnew.MinecraftNew;
import goat.minecraft.minecraftnew.subsystems.pets.PetManager;
import goat.minecraft.minecraftnew.subsystems.pets.PetManager.PetPerk;
import goat.minecraft.minecraftnew.utils.devtools.ItemRegistry;
import goat.minecraft.minecraftnew.other.skilltree.Skill;
import goat.minecraft.minecraftnew.other.skilltree.SkillTreeManager;
import goat.minecraft.minecraftnew.other.skilltree.Talent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.EnumSet;
import java.util.HashMap;
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
                        // Only run if player's active pet has the COMPOSTER or HARVEST_FESTIVAL perk
                        PetPerk perk = getAutoPerk(player);
                        if (perk != null) {
                            performConversion(player, perk);
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
     * Determines which auto-compost perk the player's active pet has, if any.
     */
    private PetPerk getAutoPerk(Player player) {
        PetManager petManager = PetManager.getInstance(plugin);
        PetManager.Pet activePet = petManager.getActivePet(player);
        if (activePet == null) {
            return null;
        }
        if (activePet.hasPerk(PetPerk.COMPOSTER)) {
            return PetPerk.COMPOSTER;
        }
        if (activePet.hasPerk(PetPerk.HARVEST_FESTIVAL)) {
            return PetPerk.HARVEST_FESTIVAL;
        }
        return null;
    }

    /**
     * Gets the pet's level from the player's active pet.
     */
    private int getPetLevel(Player player) {
        PetManager petManager = PetManager.getInstance(plugin);
        PetManager.Pet activePet = petManager.getActivePet(player);
        return activePet.getLevel();
    }

    /**
     * Performs the crop conversion for the player, using the
     * dynamic "required materials" formula:
     *
     *  requiredMaterialsOrganic = max(256 - (level - 1)*(256 - 64)/99, 64)
     */
    private void performConversion(Player player, PetPerk perk) {
        int level = getPetLevel(player);

        // 2) Calculate how many crops are required for 1 organic soil
        int requiredMaterialsOrganic = Math.max(
                256 - (level - 1) * (256 - 64) / 99,
                64
        );
        if (SkillTreeManager.getInstance() != null) {
            int lvl = SkillTreeManager.getInstance()
                    .getTalentLevel(player.getUniqueId(), Skill.TAMING, Talent.COMPOSTER);
            if (lvl > 0) {
                requiredMaterialsOrganic = requiredMaterialsOrganic / 2;
            }
        }

        // 3) Count how many of each eligible crop the player holds
        Map<Material, Integer> playerCropCounts = getPlayerCropCounts(player);

        // 4) For each eligible crop, see how many conversions we can do
        for (Material crop : AUTO_COMPOSTER_ELIGIBLE_CROPS) {
            int playerCropCount = playerCropCounts.getOrDefault(crop, 0);
            int weight = (crop == Material.PUMPKIN || crop == Material.MELON) ? 8 : 1;

            int effective = playerCropCount * weight;
            if (effective >= requiredMaterialsOrganic) {
                int conversions = effective / requiredMaterialsOrganic;
                int effectiveUsed = conversions * requiredMaterialsOrganic;
                int itemsToRemove = (int) Math.ceil(effectiveUsed / (double) weight);

                subtractCrops(player, crop, itemsToRemove);
                if (perk == PetPerk.COMPOSTER) {
                    addOrganicSoil(player, conversions);
                } else {
                    addFertilizer(player, conversions);
                }
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
        addItems(player, organicSoil, ChatColor.RED + "Your inventory is full! Organic Soil has been dropped on the ground.", quantity);
    }

    private void addFertilizer(Player player, int quantity) {
        ItemStack fertilizer = ItemRegistry.getFertilizer();
        addItems(player, fertilizer, ChatColor.RED + "Your inventory is full! Fertilizer has been dropped on the ground.", quantity);
    }

    private void addItems(Player player, ItemStack item, String fullMessage, int quantity) {
        for (int i = 0; i < quantity; i++) {
            Map<Integer, ItemStack> overflow = player.getInventory().addItem(item.clone());
            if (!overflow.isEmpty()) {
                player.getWorld().dropItemNaturally(player.getLocation(), item);
                player.sendMessage(fullMessage);
            }
        }
    }
}
