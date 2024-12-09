package goat.minecraft.minecraftnew.subsystems.pets.perks;

import goat.minecraft.minecraftnew.subsystems.pets.PetManager;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.plugin.java.JavaPlugin;

public class SecondWind implements Listener {

    private final PetManager petManager;

    public SecondWind(JavaPlugin plugin) {
        this.petManager = PetManager.getInstance(plugin);
    }

    @EventHandler
    public void onPlayerTakeDamage(EntityDamageEvent event) {
        // Ensure the entity taking damage is a player
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        // Get the player's active pet
        PetManager.Pet activePet = petManager.getActivePet(player);

        // Check if the player has the SECOND_WIND perk
        if (activePet != null && activePet.hasPerk(PetManager.PetPerk.SECOND_WIND)) {
            int petLevel = activePet.getLevel();

            // Apply Regeneration I for a duration based on pet level (5 + level seconds)
            int effectDuration = 20 * (5 + petLevel); // Duration in ticks
            player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, effectDuration, 1));

            // Notify the player
            player.playSound(player.getLocation(), Sound.ENTITY_WARDEN_HEARTBEAT, 1, 100);
        }
    }
}
