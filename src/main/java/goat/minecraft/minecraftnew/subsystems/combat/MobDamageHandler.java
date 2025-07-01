package goat.minecraft.minecraftnew.subsystems.combat;

import goat.minecraft.minecraftnew.MinecraftNew;
import goat.minecraft.minecraftnew.other.additionalfunctionality.BlessingUtils;
import goat.minecraft.minecraftnew.subsystems.armorsets.FlowManager;
import goat.minecraft.minecraftnew.subsystems.combat.notification.DamageNotificationService;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import goat.minecraft.minecraftnew.subsystems.combat.utils.EntityLevelExtractor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class MobDamageHandler implements Listener {
    private final EntityLevelExtractor levelExtractor = new EntityLevelExtractor();

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
                    int attackerLevel = levelExtractor.extractLevelFromName(attacker); // Extract the attacker's level
                    double originalDamage = event.getDamage();
                    // Calculate the damage multiplier (4% per level)
                        double damageMultiplier = 1 + (attackerLevel * 0.06); // Multiplier should be 1 + (percentage increase)
                        double newDamage = originalDamage * damageMultiplier; // Apply the multiplier
                    if(BlessingUtils.hasFullSetBonus(player, "Monolith")){
                        newDamage *= 0.8;
                        FlowManager.getInstance(MinecraftNew.getInstance()).addFlow(player, 1);
                    }
                        event.setDamage(newDamage);

                }
            }
        }
    }


}
