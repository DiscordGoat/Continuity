package goat.minecraft.minecraftnew.subsystems.pets.perks;

import goat.minecraft.minecraftnew.subsystems.pets.PetManager;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Random;

public class Greed implements Listener {

    private final PetManager petManager;
    private final Random random;

    public Greed(JavaPlugin plugin) {
        this.petManager = PetManager.getInstance(plugin);
        this.random = new Random();
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        Entity deadEntity = event.getEntity();
        if(deadEntity.getLastDamageCause() != null) return;
        if (deadEntity.getLastDamageCause().getEntity() instanceof Player) {
            Player killer = (Player) deadEntity.getLastDamageCause().getEntity();

            // Ensure the killer is a player

            // Get the active pet for the player
            PetManager.Pet activePet = petManager.getActivePet(killer);

            // Ensure the player has an active pet with the GREED perk
            if (activePet == null || !activePet.hasPerk(PetManager.PetPerk.GREED)) {
                return;
            }

            // Check if the killed entity is a Monster
            if (deadEntity instanceof Monster) {
                int petLevel = activePet.getLevel();

                // Calculate chance (max 4%)
                double chance = 0.04; // Scale with pet level

                // Attempt perk activation
                if (random.nextDouble() <= chance) {
                    // Calculate emerald drop amount (up to 32 at level 100)
                    int maxEmeralds = (int) Math.round(16 + (petLevel / 100.0) * 16);
                    int emeraldCount = random.nextInt(maxEmeralds) + 1;

                    // Drop the emeralds at the entity's death location
                    deadEntity.getWorld().dropItemNaturally(deadEntity.getLocation(), new ItemStack(Material.EMERALD, emeraldCount));

                    // Notify the player
                    killer.sendMessage(ChatColor.GOLD + "Your pet's GREED perk activated! Dropped " + emeraldCount + " emeralds.");
                }
            }
        }
    }
}
