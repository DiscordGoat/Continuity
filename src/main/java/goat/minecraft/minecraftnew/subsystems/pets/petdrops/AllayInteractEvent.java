package goat.minecraft.minecraftnew.subsystems.pets.petdrops;

import goat.minecraft.minecraftnew.subsystems.pets.PetManager;
import org.bukkit.ChatColor;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Allay;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Material;

public class AllayInteractEvent implements Listener {

    private final PetManager petManager;

    public AllayInteractEvent(PetManager petManager) {
        this.petManager = petManager;
    }

    @EventHandler
    public void onAllayInteract(PlayerInteractAtEntityEvent event) {
        // Ensure the entity interacted with is an Allay
        if (event.getRightClicked() instanceof Allay) {
            Player player = event.getPlayer();
            Allay allay = (Allay) event.getRightClicked();

            // Check if the player is holding an empty bucket in their main hand
            ItemStack itemInHand = player.getInventory().getItemInMainHand();
            if (itemInHand.getType() == Material.BUCKET) {

                // Grant the player the Allay pet
                petManager.createPet(player, "Allay", PetManager.Rarity.EPIC, 100, Particle.END_ROD, PetManager.PetPerk.COLLECTOR, PetManager.PetPerk.FLIGHT);

                // Notify the player
                player.sendMessage(ChatColor.LIGHT_PURPLE + "You interacted with an Allay and received the Allay pet!");
                player.playSound(player.getLocation(), Sound.ENTITY_ALLAY_AMBIENT_WITHOUT_ITEM, 1.0f, 1.2f);

                // Spawn a particle effect at the Allay's location
                allay.getWorld().spawnParticle(Particle.END_ROD, allay.getLocation(), 20, 0.5, 0.5, 0.5);

                // Remove the Allay from the world to simulate "bucketting"
                allay.remove();

                // Cancel the interaction to prevent further actions
                event.setCancelled(true);
            }
        }
    }
}
