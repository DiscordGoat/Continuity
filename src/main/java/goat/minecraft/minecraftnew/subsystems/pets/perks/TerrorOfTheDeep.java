package goat.minecraft.minecraftnew.subsystems.pets.perks;

import goat.minecraft.minecraftnew.subsystems.pets.PetManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class TerrorOfTheDeep implements Listener {

    private final JavaPlugin plugin;

    public TerrorOfTheDeep(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerDamageEntityMelee(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player player) {
            Entity entity = event.getEntity();

            // Get the player's active pet
            PetManager petManager = PetManager.getInstance(plugin);
            PetManager.Pet activePet = petManager.getActivePet(player);

            // Check if the player has the Terror of the Deep perk
            if (activePet != null && activePet.hasPerk(PetManager.PetPerk.TERROR_OF_THE_DEEP)) {
                activateTerrorOfTheDeep(player, entity);
            }
        }
    }

    /**
     * Activates the Terror of the Deep effects for the player if the entity has SEA_CREATURE_METADATA.
     *
     * @param player The player dealing damage.
     * @param entity The entity being attacked.
     */
    private void activateTerrorOfTheDeep(Player player, Entity entity) {
        // Check if the entity has metadata
        if (entity.hasMetadata("SEA_CREATURE")) {
            // Retrieve the metadata value
            MetadataValue metadataValue = entity.getMetadata("SEA_CREATURE").stream()
                    .filter(value -> value.getOwningPlugin().equals(plugin))
                    .findFirst()
                    .orElse(null);

            if (metadataValue != null) {
                String creatureName = metadataValue.asString();
                Bukkit.getLogger().info("Detected SEA_CREATURE: " + creatureName);

                // Apply Bloodlust effects
                player.playSound(player.getLocation(), Sound.ENTITY_WARDEN_HEARTBEAT, 1.0f, 1.0f);
                player.addPotionEffect(new PotionEffect(PotionEffectType.FAST_DIGGING, 600, 1)); // Haste II
                player.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 600, 1)); // Strength II
                player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 600, 1)); // Speed II
                player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 600, 1)); // Regeneration II

                // Notify the player
                player.sendMessage(ChatColor.DARK_RED + "You feel enraged after striking a " + creatureName + "!");
            } else {
                Bukkit.getLogger().info("Metadata found but not from this plugin.");
            }
        } else {
            Bukkit.getLogger().info("No SEA_CREATURE found on entity: " + entity.getType());
        }
    }

}
