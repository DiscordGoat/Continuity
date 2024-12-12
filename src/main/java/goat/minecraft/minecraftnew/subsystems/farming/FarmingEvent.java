package goat.minecraft.minecraftnew.subsystems.farming;

import goat.minecraft.minecraftnew.MinecraftNew;
import goat.minecraft.minecraftnew.subsystems.pets.PetManager;
import goat.minecraft.minecraftnew.subsystems.utils.CustomItemManager;
import goat.minecraft.minecraftnew.subsystems.utils.XPManager;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class FarmingEvent implements Listener {
    MinecraftNew plugin = MinecraftNew.getInstance();
    public XPManager xpManager = new XPManager(plugin);
    Random random = new Random();

    private static final Map<Material, Integer> cropXP = new HashMap<>();

    static {
        cropXP.put(Material.WHEAT, 5); // Common crops
        cropXP.put(Material.NETHER_WART, 7); // Common crops
        cropXP.put(Material.POTATOES, 5);
        cropXP.put(Material.CARROTS, 6);
        cropXP.put(Material.BEETROOTS, 6); // Slightly rarer crops
        cropXP.put(Material.MELON, 8); // Uncommon crops
        cropXP.put(Material.PUMPKIN, 8);
    }

    CustomItemManager customItemManager = new CustomItemManager();
    PetManager petManager = PetManager.getInstance(plugin);

    @EventHandler
    public void onCropHarvest(BlockBreakEvent e) {
        Block block = e.getBlock();
        Player player = e.getPlayer();
        Material blockType = block.getType();

        // Check if the block is a recognized crop
        if (cropXP.containsKey(blockType)) {

            // Handle crops that have growth stages (Ageable blocks)
            if (block.getBlockData() instanceof Ageable) {
                Ageable crop = (Ageable) block.getBlockData();

                // Only proceed if the crop is fully grown
                if (crop.getAge() != crop.getMaximumAge()) {
                    return; // Crop is not fully grown, ignore the event
                }
            }

            // Award Farming XP
            int xp = cropXP.get(blockType);
            xpManager.addXP(player, "Farming", xp);

            // Villager drop chance (1 in 200)
            if (random.nextInt(400) == 0) {
                player.getInventory().addItem(customItemManager.createHireVillager());
            }

            // Play harvest sound
            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);

            // Calculate level/2 chance for double drops
            int farmingLevel = xpManager.getPlayerLevel(player, "Farming");
            if (random.nextInt(100) < farmingLevel / 2) {
                Collection<ItemStack> drops = block.getDrops();
                for (ItemStack drop : drops) {
                    player.getInventory().addItem(drop.clone());
                }
                player.sendMessage(ChatColor.AQUA + "You harvested extra " + blockType.toString().toLowerCase() + "!");
                player.playSound(player.getLocation(), Sound.BLOCK_CROP_BREAK, 1.0f, 1.0f);
            }

            // Apply rare effect if the crop is a Torchflower

            // Farming pets drop logic
            grantFarmingPet(player, blockType);
        }
    }

    // Farming pets drop logic
    private void grantFarmingPet(Player player, Material blockType) {
        // Define rarity tiers and drop rates (out of 100,000 for better precision)
        final int COMMON_THRESHOLD = 1000;      // 1.0% chance
        final int UNCOMMON_THRESHOLD = 100;    // 0.1% chance
        final int RARE_THRESHOLD = 10;         // 0.01% chance
        final int EPIC_THRESHOLD = 1;          // 0.001% chance
        final int LEGENDARY_THRESHOLD = 1;     // 0.001% chance

        // Define approximate crops needed for each rarity
        final int COMMON_CROPS = 100;          // 1 in 100 crops
        final int UNCOMMON_CROPS = 1000;       // 1 in 1,000 crops
        final int RARE_CROPS = 10000;          // 1 in 10,000 crops
        final int EPIC_CROPS = 100000;         // 1 in 100,000 crops
        final int LEGENDARY_CROPS = 100000;    // 1 in 100,000 crops

        // Roll for pet rarity
        int roll = random.nextInt(100000); // Random roll between 0-99,999

        // Determine rarity and create pet
        if (roll < LEGENDARY_THRESHOLD) {
            petManager.createPet(
                    player,
                    "Pig",
                    PetManager.Rarity.LEGENDARY,
                    100,
                    Particle.FIREWORKS_SPARK,
                    PetManager.PetPerk.GREEN_THUMB,
                    PetManager.PetPerk.CULTIVATION,
                    PetManager.PetPerk.SUPERIOR_ENDURANCE
            );
            player.sendMessage(ChatColor.GOLD + "You obtained a LEGENDARY pet! Keep farming to find more.");
            player.sendMessage(ChatColor.GRAY + "You were lucky! This usually takes about " + LEGENDARY_CROPS + " crops.");
        } else if (roll < EPIC_THRESHOLD) {
            petManager.createPet(
                    player,
                    "Mooshroom",
                    PetManager.Rarity.EPIC,
                    100,
                    Particle.FIREWORKS_SPARK,
                    PetManager.PetPerk.GREEN_THUMB,
                    PetManager.PetPerk.CULTIVATION
            );
            player.sendMessage(ChatColor.LIGHT_PURPLE + "You obtained an EPIC pet! Keep farming to find more.");
            player.sendMessage(ChatColor.GRAY + "This usually takes about " + EPIC_CROPS + " crops.");
        } else if (roll < RARE_THRESHOLD) {
            petManager.createPet(
                    player,
                    "Cow",
                    PetManager.Rarity.RARE,
                    100,
                    Particle.FIREWORKS_SPARK,
                    PetManager.PetPerk.GREEN_THUMB,
                    PetManager.PetPerk.ANTIDOTE
            );

        } else if (roll < UNCOMMON_THRESHOLD) {
            petManager.createPet(
                    player,
                    "Sheep",
                    PetManager.Rarity.UNCOMMON,
                    100,
                    Particle.FIREWORKS_SPARK,
                    PetManager.PetPerk.GREEN_THUMB
            );

        } else if (roll < COMMON_THRESHOLD) {
            petManager.createPet(
                    player,
                    "Squirrel",
                    PetManager.Rarity.COMMON,
                    100,
                    Particle.FIREWORKS_SPARK,
                    PetManager.PetPerk.GREEN_THUMB
            );

        }
    }



    // Special bonus for harvesting Torchflowers
}
