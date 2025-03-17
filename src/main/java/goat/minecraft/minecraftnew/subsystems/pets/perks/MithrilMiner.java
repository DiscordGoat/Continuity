package goat.minecraft.minecraftnew.subsystems.pets.perks;

import goat.minecraft.minecraftnew.subsystems.pets.PetManager;
import goat.minecraft.minecraftnew.utils.devtools.ItemRegistry;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Random;

public class MithrilMiner implements Listener {

    private final PetManager petManager;

    public MithrilMiner(JavaPlugin plugin) {
        this.petManager = PetManager.getInstance(plugin);
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        // Ensure the player is breaking a block
        Player player = event.getPlayer();

        // Get the player's active pet
        PetManager.Pet activePet = petManager.getActivePet(player);

        // Check if the player has the MITHRIL_MINER perk
        if (activePet != null && activePet.hasPerk(PetManager.PetPerk.MITHRIL_MINER)) {
            if (event.getBlock().getType() == Material.DEEPSLATE) {
                Random random = new Random();

                // Determine the chance based on pet level (e.g., level * 0.005 = base chance)
                double baseChance = 0.005; // 0.5% base chance
                double chance = baseChance + (activePet.getLevel() * 0.0005); // Adds 0.05% per level, capped later

                // Cap the chance at 5% (0.05)
                chance = Math.min(chance, 0.05);

                // Check if the random chance triggers
                if (random.nextDouble() <= chance) {
                    // Drop a Mithril Chunk at the block's location
                    Location dropLocation = event.getBlock().getLocation();
                    ItemStack mithrilChunk = ItemRegistry.getMithrilChunk();
                    dropLocation.getWorld().dropItemNaturally(dropLocation, mithrilChunk);

                    // Notify the player
                    player.sendMessage(ChatColor.GOLD + "Your Mithril Miner perk activated! You mined a rare Mithril Chunk!");
                }
            }
        }
    }
}
