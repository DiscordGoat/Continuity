package goat.minecraft.minecraftnew.subsystems.farming;

import goat.minecraft.minecraftnew.MinecraftNew;
import goat.minecraft.minecraftnew.other.beacon.Catalyst;
import goat.minecraft.minecraftnew.other.beacon.CatalystManager;
import goat.minecraft.minecraftnew.other.beacon.CatalystType;
import goat.minecraft.minecraftnew.subsystems.farming.CropCountManager;
import goat.minecraft.minecraftnew.utils.devtools.ItemRegistry;
import goat.minecraft.minecraftnew.utils.devtools.XPManager;
import goat.minecraft.minecraftnew.other.skilltree.Skill;
import goat.minecraft.minecraftnew.other.skilltree.Talent;
import goat.minecraft.minecraftnew.other.skilltree.SkillTreeManager;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
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
    private static final Set<Material> RARE_DROP_CROPS = EnumSet.noneOf(Material.class);
    // Define rarity probabilities

    MinecraftNew plugin = MinecraftNew.getInstance();
    public XPManager xpManager = new XPManager(plugin);
    Random random = new Random();
    private static final String PLAYER_PLACED_KEY = "player_placed";

    private static final Map<Material, Integer> cropXP = new HashMap<>();

    static {
        cropXP.put(Material.WHEAT, 3); // Common crops
        cropXP.put(Material.NETHER_WART, 5); // Common crops
        cropXP.put(Material.POTATOES, 3);
        cropXP.put(Material.CARROTS, 4);
        cropXP.put(Material.CARROT, 4);
        cropXP.put(Material.BEETROOTS, 4); // Slightly rarer crops
        cropXP.put(Material.MELON, 6); // Uncommon crops
        cropXP.put(Material.PUMPKIN, 6);
    }


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
            orb.setExperience(2);
            xpManager.addXP(player, "Farming", xp);
            // Play harvest sound
            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 2.0f);

            if (blockType == Material.CARROTS || blockType == Material.POTATOES ||
                    blockType == Material.BEETROOTS || blockType == Material.PUMPKIN ||
                    blockType == Material.MELON || blockType == Material.COCOA) {
                CropCountManager.getInstance(plugin).increment(player, blockType);
            }

            int talentLevel = SkillTreeManager.getInstance()
                    .getTalentLevel(player.getUniqueId(), Skill.FARMING, Talent.BOUNTIFUL_HARVEST);
            boolean doubled = random.nextDouble() < (talentLevel * 0.04);

            CatalystManager catalystManager = CatalystManager.getInstance();
            boolean tripled = false;
            if (catalystManager != null && catalystManager.isNearCatalyst(player.getLocation(), CatalystType.PROSPERITY)) {
                Catalyst catalyst = catalystManager.findNearestCatalyst(player.getLocation(), CatalystType.PROSPERITY);
                if (catalyst != null) {
                    int tier = catalystManager.getCatalystTier(catalyst);
                    double chance = 0.40 + (tier * 0.10);
                    chance = Math.min(chance, 1.0);
                    tripled = random.nextDouble() < chance;
                }
            }

            if (doubled || tripled) {
                Collection<ItemStack> drops = block.getDrops();
                for (ItemStack drop : drops) {
                    ItemStack extra = drop.clone();
                    extra.setAmount(tripled ? drop.getAmount() * 2 : drop.getAmount());
                    Objects.requireNonNull(e.getBlock().getLocation().getWorld()).dropItem(e.getBlock().getLocation(), extra);
                }
                player.playSound(player.getLocation(), Sound.BLOCK_ROOTED_DIRT_PLACE, 1.0f, 1.0f);


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
            if (random.nextInt(1600) == 0) {
                // Retrieve the rare item for this specific crop from ItemRegistry
                ItemStack rareItem = ItemRegistry.getRareItem(blockType); // Ensure this method is implemented in ItemRegistry

                if (rareItem != null) {
                    // Drop the rare item naturally at the block's location
                    block.getWorld().dropItemNaturally(block.getLocation(), rareItem);
 //yup
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

}
