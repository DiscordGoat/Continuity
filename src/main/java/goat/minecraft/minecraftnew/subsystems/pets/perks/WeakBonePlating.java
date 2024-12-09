package goat.minecraft.minecraftnew.subsystems.pets.perks;

import goat.minecraft.minecraftnew.subsystems.pets.PetManager;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class WeakBonePlating implements Listener {
    private final JavaPlugin plugin;

    public WeakBonePlating(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerDamage(EntityDamageEvent event) {
        // Ensure the entity taking damage is a player
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        // Get the player's active pet
        PetManager petManager = PetManager.getInstance(plugin);
        PetManager.Pet activePet = petManager.getActivePet(player);

        // Check if the player has the WEAK_BONE_PLATING perk
        if (activePet != null && activePet.hasPerk(PetManager.PetPerk.BONE_PLATING_WEAK)) {
            int petLevel = activePet.getLevel();

            // Apply Resistance I effect for <level> seconds
            if (!player.hasPotionEffect(PotionEffectType.DAMAGE_RESISTANCE)) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, petLevel * 20, 0));

                // Notify the player
                player.sendMessage(ChatColor.GRAY + "Your " + ChatColor.DARK_GRAY + "Weak Bone Plating" + ChatColor.GRAY +
                        " perk absorbed some damage for " + petLevel + " seconds!");
            }
        }
    }
}
