package goat.minecraft.minecraftnew.subsystems.farming;

import goat.minecraft.minecraftnew.MinecraftNew;
import goat.minecraft.minecraftnew.subsystems.pets.PetManager;
import goat.minecraft.minecraftnew.utils.CustomItemManager;
import goat.minecraft.minecraftnew.utils.XPManager;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class FarmingEvent implements Listener {
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
        cropXP.put(Material.BEETROOTS, 6); // Slightly rarer crops
        cropXP.put(Material.MELON, 8); // Uncommon crops
        cropXP.put(Material.PUMPKIN, 8);
    }

    CustomItemManager customItemManager = new CustomItemManager();
    PetManager petManager = PetManager.getInstance(plugin);

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Block block = event.getBlock();
        if (cropXP.containsKey(block.getType())) {
            block.setMetadata(PLAYER_PLACED_KEY, new FixedMetadataValue(plugin, true));
        }
    }

    @EventHandler
    public void onCropHarvest(BlockBreakEvent e) {
        Block block = e.getBlock();
        Player player = e.getPlayer();
        Material blockType = block.getType();

        // Check if the block is a recognized crop
        if (cropXP.containsKey(blockType)) {
            // Check if the block was placed by a player
            if (block.hasMetadata(PLAYER_PLACED_KEY)) {
                return;
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
            xpManager.addXP(player, "Farming", xp);

            // Villager drop chance (1 in 200)
            if (random.nextInt(400) == 0) {
                player.getLocation().getWorld().dropItem(player.getLocation(), customItemManager.createHireVillager());
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_YES, 1.0f, 1.0f);
            }

            // Play harvest sound
            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);

            // Calculate level/2 chance for double drops
            int farmingLevel = xpManager.getPlayerLevel(player, "Farming");
            if (random.nextInt(100) < farmingLevel) {
                Collection<ItemStack> drops = block.getDrops();
                for (ItemStack drop : drops) {
                    player.getInventory().addItem(drop.clone());
                }
                player.playSound(player.getLocation(), Sound.BLOCK_ROOTED_DIRT_PLACE, 1.0f, 1.0f);
            }
        }
    }

}
