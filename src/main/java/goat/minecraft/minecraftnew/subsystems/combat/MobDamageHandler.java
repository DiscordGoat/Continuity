package goat.minecraft.minecraftnew.subsystems.combat;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class MobDamageHandler implements Listener {

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        // Check if the entity taking damage is a player
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            Entity damager = event.getDamager();

            // Check if the damager is a LivingEntity (monster) or a Projectile shot by a monster
            if (damager instanceof LivingEntity || (damager instanceof Projectile && ((Projectile) damager).getShooter() instanceof LivingEntity)) {
                LivingEntity attacker;

                // If the damager is a projectile, get the shooter
                if (damager instanceof Projectile) {
                    Projectile projectile = (Projectile) damager;
                    // Ensure the shooter is a valid entity (e.g., Monster)
                    if (projectile.getShooter() instanceof LivingEntity) {
                        attacker = (LivingEntity) projectile.getShooter();
                    } else {
                        return; // Exit if the shooter is not a valid living entity
                    }
                } else {
                    attacker = (LivingEntity) damager; // If it's a direct attack from a LivingEntity
                }

                // Check if the attacker is a monster (e.g., Skeleton, Zombie, etc.)
                if (attacker instanceof Monster) {
                    int attackerLevel = extractIntegerFromEntityName(attacker); // Extract the attacker's level
                    double originalDamage = event.getDamage();
                    // Calculate the damage multiplier (4% per level)
                        double damageMultiplier = 1 + (attackerLevel * 0.06); // Multiplier should be 1 + (percentage increase)
                        double newDamage = originalDamage * damageMultiplier; // Apply the multiplier
                    Bukkit.getLogger().info(player.getName() + "'s original damage: " + originalDamage);
                    Bukkit.getLogger().info(player.getName() + "'s new Damage: " + newDamage);
                        Bukkit.getLogger().info(player.getName() + " took an additional damage of: " + (newDamage - originalDamage) + " from " + attacker.getType());

                        // Set the new damage to the event
                        event.setDamage(newDamage);
                }
            }
        }
    }


    public int extractIntegerFromEntityName(Entity entity) {
        String name = entity.getName(); // Get the entity's name
        System.out.println("Entity Name: " + name); // Debug output

        // Remove color codes (e.g., "§a") and all non-numeric characters
        String cleanedName = name.replaceAll("(?i)§[0-9a-f]", ""); // Remove color codes
        String numberString = cleanedName.replaceAll("[^0-9]", ""); // Remove all non-numeric characters
        System.out.println("Cleaned Name: " + cleanedName); // Debug output
        System.out.println("Extracted Number String: " + numberString); // Debug output

        // Check if the resulting string is empty, and return 0 or parse the integer
        if (numberString.isEmpty()) {
            return 0; // Return 0 if no numbers found
        }

        try {
            return Integer.parseInt(numberString); // Parse the remaining string to an integer
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return 0; // Return 0 if parsing fails
        }
    }
}
