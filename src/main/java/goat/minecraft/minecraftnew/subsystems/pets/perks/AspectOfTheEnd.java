package goat.minecraft.minecraftnew.subsystems.pets.perks;

import goat.minecraft.minecraftnew.subsystems.pets.PetManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.Random;

public class AspectOfTheEnd implements Listener {

    private final PetManager petManager;
    private final Random random = new Random();

    public AspectOfTheEnd(PetManager petManager) {
        this.petManager = petManager;
    }

    @EventHandler
    public void onRightClick(PlayerInteractEvent event) {
        // Debug: Check if the event is firing


        // Check if the player right-clicked air
        if (!event.getAction().toString().contains("RIGHT_CLICK_AIR")) {

            return;
        }

        Player player = event.getPlayer();
        ItemStack itemInHand = player.getInventory().getItemInMainHand();

        // Debug: Check if the player has an item in hand
        if (itemInHand == null) {

            return;
        }

        // Debug: Check player's active pet
        PetManager.Pet activePet = petManager.getActivePet(player);
        if (activePet == null) {

            return;
        }
        if (!activePet.hasPerk(PetManager.PetPerk.ASPECT_OF_THE_END)) {

            return;
        }

        // Calculate the teleportation distance, scaling with the pet's level
        int petLevel = activePet.getLevel();
        int distance = 8 + (petLevel / 10); // Base 8 blocks + 1 block for every 10 levels


        // Calculate the teleport destination
        Vector direction = player.getLocation().getDirection().normalize();
        Vector offset = direction.multiply(distance);


        // Debug: Teleporting player
        player.teleport(player.getLocation().add(offset));


        // Play teleport sound effect
        player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);

        // Deduct 2 hunger or 2 saturation
        float currentSaturation = player.getSaturation();

        // Notify the player

    }
}
