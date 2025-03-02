package goat.minecraft.minecraftnew.subsystems.pets.petdrops;

import goat.minecraft.minecraftnew.subsystems.pets.PetManager;
import goat.minecraft.minecraftnew.subsystems.pets.PetRegistry;
import org.bukkit.ChatColor;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Axolotl;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Material;

public class AxolotlInteractEvent implements Listener {

    private final PetManager petManager;

    public AxolotlInteractEvent(PetManager petManager) {
        this.petManager = petManager;
    }

    @EventHandler
    public void onAxolotlInteract(PlayerInteractAtEntityEvent event) {
        // Check if the entity interacted with is an Axolotl
        if (event.getRightClicked() instanceof Axolotl) {
            Player player = event.getPlayer();
            Axolotl axolotl = (Axolotl) event.getRightClicked();

            // Check if the player is holding a water bucket in their main hand
            ItemStack itemInHand = player.getInventory().getItemInMainHand();
            if (itemInHand.getType() == Material.BUCKET) {

                // Grant the player the Axolotl pet
                PetRegistry petRegistry = new PetRegistry();
                petRegistry.addPetByName(player, "Axolotl");

                // Notify the player
                player.playSound(player.getLocation(), Sound.ENTITY_AXOLOTL_SPLASH, 1.0f, 1.0f);

                // Spawn a particle effect where the Axolotl was
                axolotl.getWorld().spawnParticle(Particle.BUBBLE_POP, axolotl.getLocation(), 10, 0.5, 0.5, 0.5);

                // Remove the Axolotl from the world to simulate "taking it with you"
                axolotl.remove();

                // Cancel the interaction to prevent further actions
                event.setCancelled(true);
            }
        }
    }
}
