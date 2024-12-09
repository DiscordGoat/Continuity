package goat.minecraft.minecraftnew.subsystems.pets.perks;

import goat.minecraft.minecraftnew.subsystems.pets.PetManager;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class Collector implements Listener {

    private final PetManager petManager;

    public Collector(JavaPlugin plugin) {
        this.petManager = PetManager.getInstance(plugin);
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        PetManager.Pet activePet = petManager.getActivePet(player);

        // Check if the player has the COLLECTOR perk
        if (activePet != null && activePet.hasPerk(PetManager.PetPerk.COLLECTOR)) {
            // Define the collection radius based on pet level (e.g., base 15 blocks + 1 block per level)
            int petLevel = activePet.getLevel();
            double radius = 15.0 + petLevel;

            Location playerLocation = player.getLocation();
            World world = player.getWorld();

            // Find nearby items within the radius
            List<Item> nearbyItems = world.getNearbyEntities(playerLocation, radius, radius, radius, entity -> entity instanceof Item)
                    .stream()
                    .map(entity -> (Item) entity)
                    .collect(Collectors.toList());

            if (nearbyItems.isEmpty()) {
                return; // No items to collect
            }

            Inventory playerInventory = player.getInventory();
            int itemsCollected = 0;

            for (Item itemEntity : nearbyItems) {
                ItemStack itemStack = itemEntity.getItemStack();

                // Attempt to add the item to the player's inventory
                HashMap<Integer, ItemStack> remaining = playerInventory.addItem(itemStack.clone());

                if (remaining.isEmpty()) {
                    // Successfully added to inventory; remove the item entity
                    itemEntity.remove();
                    itemsCollected++;
                }
            }

            // Notify the player about items collected, if any
            if (itemsCollected > 0) {
                player.playSound(player.getLocation(), Sound.BLOCK_AZALEA_PLACE, 5, 100);
            }
        }
    }
}
