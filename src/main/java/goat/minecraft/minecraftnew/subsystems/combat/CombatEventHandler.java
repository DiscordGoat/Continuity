package goat.minecraft.minecraftnew.subsystems.combat;

import goat.minecraft.minecraftnew.subsystems.combat.config.CombatConfiguration;
import goat.minecraft.minecraftnew.subsystems.combat.damage.DamageCalculationResult;
import goat.minecraft.minecraftnew.subsystems.combat.damage.DamageCalculationService;
import goat.minecraft.minecraftnew.subsystems.combat.notification.DamageNotificationService;
import goat.minecraft.minecraftnew.subsystems.combat.notification.PlayerFeedbackService;
import goat.minecraft.minecraftnew.subsystems.combat.notification.MonsterHealthBarService;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Main event handler for combat-related events.
 * Coordinates damage calculations, notifications, and player feedback.
 */
public class CombatEventHandler implements Listener {
    
    private static final Logger logger = Logger.getLogger(CombatEventHandler.class.getName());
    
    private final DamageCalculationService damageCalculationService;
    private final DamageNotificationService notificationService;
    private final PlayerFeedbackService feedbackService;
    private final MonsterHealthBarService healthBarService;
    private final CombatConfiguration config;
    
    public CombatEventHandler(DamageCalculationService damageCalculationService,
                             DamageNotificationService notificationService,
                             PlayerFeedbackService feedbackService,
                             MonsterHealthBarService healthBarService,
                             CombatConfiguration config) {
        this.damageCalculationService = damageCalculationService;
        this.notificationService = notificationService;
        this.feedbackService = feedbackService;
        this.healthBarService = healthBarService;
        this.config = config;
    }
    
    /**
     * Handles entity damage events with comprehensive error handling and logging.
     * Applies damage calculations and provides feedback to players.
     */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        try {
            // Skip if damage is zero or negative
            if (event.getDamage() <= 0) {
                logger.finest("Skipping zero/negative damage event");
                return;
            }
            
            // Process damage calculations
            DamageCalculationResult result = damageCalculationService.processDamageEvent(event);
            
            if (result.wasModified()) {
                // Apply the calculated damage
                event.setDamage(result.getFinalDamage());
                
                logger.fine(String.format("Applied damage modification: %s", result.toString()));
                
                // Provide feedback to attacking player if applicable
                result.getAppliedModifiers().stream()
                      .filter(modifier -> modifier.getSource().contains("Combat Skill") || 
                                         modifier.getSource().contains("Recurve"))
                      .findFirst()
                      .ifPresent(modifier -> {
                          if (event.getDamager() instanceof Player) {
                              feedbackService.sendDamageBoostFeedback((Player) event.getDamager(), result);
                          }
                      });
            }
            
            // Show damage notification if enabled and applicable
            if (config.getNotificationConfig().isEnabled() && shouldShowNotification(event)) {
                notificationService.showDamageIndicator(event.getEntity().getLocation(), result.getFinalDamage());
            }

            // Show monster health bar
            if (event.getEntity() instanceof org.bukkit.entity.LivingEntity living
                    && !(event.getEntity() instanceof Player)) {
                double predicted = Math.max(0, living.getHealth() - event.getFinalDamage());
                healthBarService.showHealthBar(living, predicted);
            }
            
        } catch (DamageCalculationService.DamageCalculationException e) {
            logger.log(Level.SEVERE, "Critical error in damage calculation", e);
            // Don't modify damage on critical errors to maintain game stability
            
        } catch (Exception e) {
            logger.log(Level.WARNING, "Unexpected error in combat event handling", e);
            // Continue processing to avoid breaking the game
        }
    }
    
    /**
     * Determines if a damage notification should be shown for this event.
     * Only shows notifications for player-caused damage to avoid spam.
     */
    private boolean shouldShowNotification(EntityDamageByEntityEvent event) {
        // Show notifications for damage caused by or to players
        return event.getDamager() instanceof Player || 
               (event.getDamager() instanceof org.bukkit.entity.Projectile &&
                ((org.bukkit.entity.Projectile) event.getDamager()).getShooter() instanceof Player);
    }
}