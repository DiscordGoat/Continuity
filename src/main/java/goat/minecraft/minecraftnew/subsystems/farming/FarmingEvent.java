package goat.minecraft.minecraftnew.subsystems.farming;

import goat.minecraft.minecraftnew.MinecraftNew;
import goat.minecraft.minecraftnew.subsystems.pets.PetManager;
import goat.minecraft.minecraftnew.utils.CustomItemManager;
import goat.minecraft.minecraftnew.utils.ItemRegistry;
import goat.minecraft.minecraftnew.utils.XPManager;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.*;

public class FarmingEvent implements Listener {
    private static final Set<Material> RARE_DROP_CROPS = EnumSet.of(
            Material.CARROTS,
            Material.POTATOES,
            Material.WHEAT,
            Material.BEETROOTS
    );
    // Define rarity probabilities
    private static final double COMMON_CHANCE = 1.0;       // 1%
    private static final double UNCOMMON_CHANCE = 0.5;     // 0.5%
    private static final double RARE_CHANCE = 0.25;        // 0.25%
    private static final double EPIC_CHANCE = 0.125;       // 0.125%
    private static final double LEGENDARY_CHANCE = 0.01;   // 0.01%

    // Total chance to grant a pet
    private static final double TOTAL_PET_CHANCE = COMMON_CHANCE
            + UNCOMMON_CHANCE
            + RARE_CHANCE
            + EPIC_CHANCE
            + LEGENDARY_CHANCE; // 1.885%

    MinecraftNew plugin = MinecraftNew.getInstance();
    public XPManager xpManager = new XPManager(plugin);
    Random random = new Random();
    private static final String PLAYER_PLACED_KEY = "player_placed";

    private static final Map<Material, Integer> cropXP = new HashMap<>();

    static {
        cropXP.put(Material.WHEAT, 5); // Common crops
        cropXP.put(Material.NETHER_WART, 7); // Common crops
        cropXP.put(Material.POTATOES, 5);
        cropXP.put(Material.CARROTS, 6);
        cropXP.put(Material.CARROT, 6);
        cropXP.put(Material.BEETROOTS, 6); // Slightly rarer crops
        cropXP.put(Material.MELON, 8); // Uncommon crops
        cropXP.put(Material.PUMPKIN, 8);
    }

    CustomItemManager customItemManager = new CustomItemManager();
    PetManager petManager = PetManager.getInstance(plugin);
    PetGrantingManager petGrantingManager = new PetGrantingManager(petManager);

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Block block = event.getBlock();
        if (block.getType() == Material.PUMPKIN || block.getType() == Material.MELON) {
            block.setMetadata(PLAYER_PLACED_KEY, new FixedMetadataValue(plugin, true));
            System.out.println("Metadata set for player-placed block: " + block.getType());
        }
    }

    @EventHandler
    public void onCropHarvest(BlockBreakEvent e) {
        Block block = e.getBlock();
        Player player = e.getPlayer();
        Material blockType = block.getType();
        //System.out.println("Breaking block: " + blockType.name());

        // Check if the block is a recognized crop
        if (cropXP.containsKey(blockType)) {
            // Check if the block was placed by a player
            if (block.hasMetadata(PLAYER_PLACED_KEY)) {
                if (block.getType() == Material.PUMPKIN || block.getType() == Material.MELON) {
                    System.out.println("player placed block broken: " + blockType.name());

                    return;
                }
            }

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
            ExperienceOrb orb = (ExperienceOrb) block.getWorld().spawn(block.getLocation(), ExperienceOrb.class);
            orb.setExperience(4);
            xpManager.addXP(player, "Farming", xp);
            // Play harvest sound
            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 2.0f);

            // Calculate level/2 chance for double drops
            int farmingLevel = xpManager.getPlayerLevel(player, "Farming");
            if (random.nextInt(100) < farmingLevel) {
                Collection<ItemStack> drops = block.getDrops();
                for (ItemStack drop : drops) {
                    player.getInventory().addItem(drop.clone());
                }
                player.playSound(player.getLocation(), Sound.BLOCK_ROOTED_DIRT_PLACE, 1.0f, 1.0f);
                petGrantingManager.attemptGrantPet(player);


            }
            handleRareItemDrop(block, player, blockType);
        }
    }
    /**
     * Handles the rare item drop for eligible crops with a 1/400 chance.
     *
     * @param block     The block that was harvested.
     * @param player    The player who harvested the block.
     * @param blockType The type of the harvested block.
     */
    private void handleRareItemDrop(Block block, Player player, Material blockType) {
        // Check if the harvested crop is eligible for rare drops
        if (RARE_DROP_CROPS.contains(blockType)) {
            // 1/400 chance to drop a rare item
            if (random.nextInt(400) == 0) {
                // Retrieve the rare item for this specific crop from ItemRegistry
                ItemStack rareItem = ItemRegistry.getRareItem(blockType); // Ensure this method is implemented in ItemRegistry

                if (rareItem != null) {
                    // Drop the rare item naturally at the block's location
                    block.getWorld().dropItemNaturally(block.getLocation(), rareItem);

                    // Optional: Play a unique sound to indicate a rare drop
                    player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);

                    // Optional: Send a message to the player
                    player.sendMessage(ChatColor.LIGHT_PURPLE + "✨ A rare item has dropped!");
                } else {
                    // Log a warning if the rare item is not defined
                    plugin.getLogger().warning("Rare item is not defined in ItemRegistry.getRareItem(Material). Crop: " + blockType.name());
                }
            }
        }
    }

    public class PetGrantingManager {
        private Random random = new Random();
        private PetManager petManager; // Assume this is initialized elsewhere

        public PetGrantingManager(PetManager petManager) {
            this.petManager = petManager;
        }

        /**
         * Attempts to grant a pet to the target player based on predefined rarity probabilities.
         *
         * @param targetPlayer The player to whom the pet will be granted.
         */
        public void attemptGrantPet(Player targetPlayer) {
            double roll = random.nextDouble() * 100; // Generates a number between 0.0 and 100.0

            if (roll <= TOTAL_PET_CHANCE) {
                // Determine which rarity to grant
                double cumulative = 0.0;

                cumulative += LEGENDARY_CHANCE;
                if (roll <= cumulative) {
                    grantPet(targetPlayer, PetManager.Rarity.LEGENDARY);
                    return;
                }

                cumulative += EPIC_CHANCE;
                if (roll <= cumulative) {
                    grantPet(targetPlayer, PetManager.Rarity.EPIC);
                    return;
                }

                cumulative += RARE_CHANCE;
                if (roll <= cumulative) {
                    grantPet(targetPlayer, PetManager.Rarity.RARE);
                    return;
                }

                cumulative += UNCOMMON_CHANCE;
                if (roll <= cumulative) {
                    grantPet(targetPlayer, PetManager.Rarity.UNCOMMON);
                    return;
                }

                cumulative += COMMON_CHANCE;
                if (roll <= cumulative) {
                    grantPet(targetPlayer, PetManager.Rarity.COMMON);
                    return;
                }
            }
            // No pet granted
        }

        /**
         * Grants a pet of the specified rarity to the player.
         *
         * @param player The player to receive the pet.
         * @param rarity The rarity of the pet.
         */
        private void grantPet(Player player, PetManager.Rarity rarity) {
            switch (rarity) {
                case COMMON:
                    petManager.createPet(player, "Squirrel", rarity, 100, Particle.FIREWORKS_SPARK, PetManager.PetPerk.GREEN_THUMB);
                    break;
                case UNCOMMON:
                    petManager.createPet(player, "Sheep", rarity, 100, Particle.FIREWORKS_SPARK,
                            PetManager.PetPerk.SPEED_BOOST, PetManager.PetPerk.GREEN_THUMB);
                    break;
                case RARE:
                    petManager.createPet(player, "Cow", rarity, 100, Particle.FIREWORKS_SPARK,
                            PetManager.PetPerk.SPEED_BOOST, PetManager.PetPerk.GREEN_THUMB,
                            PetManager.PetPerk.ANTIDOTE, PetManager.PetPerk.COLLECTOR);
                    break;
                case EPIC:
                    petManager.createPet(player, "Mooshroom", rarity, 100, Particle.FIREWORKS_SPARK,
                            PetManager.PetPerk.SPEED_BOOST, PetManager.PetPerk.GREEN_THUMB,
                            PetManager.PetPerk.CULTIVATION, PetManager.PetPerk.COLLECTOR);
                    break;
                case LEGENDARY:
                    petManager.createPet(player, "Pig", rarity, 100, Particle.FIREWORKS_SPARK,
                            PetManager.PetPerk.SPEED_BOOST, PetManager.PetPerk.GREEN_THUMB,
                            PetManager.PetPerk.CULTIVATION, PetManager.PetPerk.SUPERIOR_ENDURANCE,
                            PetManager.PetPerk.COLLECTOR);
                    break;
                default:
                    break;
            }
            // Optionally, you can add sounds or messages to notify the player
            player.playSound(player.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_LAUNCH, 1.0f, 1.0f);

        }
    }
}
