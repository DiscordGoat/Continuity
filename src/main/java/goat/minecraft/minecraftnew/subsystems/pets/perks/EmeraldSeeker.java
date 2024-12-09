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

import java.util.Random;

public class EmeraldSeeker implements Listener {

    private final PetManager petManager;

    public EmeraldSeeker(JavaPlugin plugin) {
        this.petManager = PetManager.getInstance(plugin);
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        // Ensure the player is breaking a block
        Player player = event.getPlayer();

        // Get the player's active pet
        PetManager.Pet activePet = petManager.getActivePet(player);

        // Check if the player has the EMERALD_SEEKER perk
        if (activePet != null && activePet.hasPerk(PetManager.PetPerk.EMERALD_SEEKER)) {
            if (event.getBlock().getType() == Material.DEEPSLATE) {
                Random random = new Random();
                double chance = 0.04; // 4% chance to find emeralds

                // Check if the random chance triggers
                if (random.nextDouble() <= chance) {
                    // Drop an emerald at the block's location
                    Location dropLocation = event.getBlock().getLocation();
                    dropLocation.getWorld().dropItemNaturally(dropLocation, new ItemStack(Material.EMERALD, 4));

                    // Notify the player
                    player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 100000000);
                }
            }
        }
    }
}
