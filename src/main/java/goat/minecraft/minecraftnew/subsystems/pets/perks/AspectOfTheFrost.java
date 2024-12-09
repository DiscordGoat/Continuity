package goat.minecraft.minecraftnew.subsystems.pets.perks;

import goat.minecraft.minecraftnew.subsystems.pets.PetManager;
import goat.minecraft.minecraftnew.subsystems.villagers.VillagerWorkCycleManager;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Collections;

public class AspectOfTheFrost implements Listener {

    private final JavaPlugin plugin;
    private final VillagerWorkCycleManager villagerWorkCycleManager;

    public AspectOfTheFrost(JavaPlugin plugin) {
        this.plugin = plugin;
        this.villagerWorkCycleManager = new VillagerWorkCycleManager(plugin);
    }

    @EventHandler
    public void onPlayerTakeDamage(EntityDamageEvent event) {
        // Ensure the damaged entity is a player
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        // Get the player's active pet
        PetManager petManager = PetManager.getInstance(plugin);
        PetManager.Pet activePet = petManager.getActivePet(player);

        // Check if the player has the ASPECT_OF_THE_FROST perk
        if (activePet != null && activePet.hasPerk(PetManager.PetPerk.ASPECT_OF_THE_FROST)) {
            // Check if there's snow within a 10-block radius
            boolean isNearSnow = villagerWorkCycleManager.findNearestBlock(
                    player,
                    Collections.singletonList(Material.SNOW),
                    10
            ) != null;

            // Reduce damage by 50% if near snow
            if (isNearSnow) {
                event.setDamage(event.getDamage() * 0.5);

                // Notify the player (optional)
                player.playSound(player.getLocation(), Sound.BLOCK_SNOW_BREAK, 1, 10);
                player.playSound(player.getLocation(), Sound.ITEM_SHIELD_BLOCK, 1, 10);
            }
        }
    }
}
