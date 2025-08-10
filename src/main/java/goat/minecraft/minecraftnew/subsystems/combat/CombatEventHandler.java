package goat.minecraft.minecraftnew.subsystems.combat;

import goat.minecraft.minecraftnew.subsystems.combat.config.CombatConfiguration;
import goat.minecraft.minecraftnew.subsystems.combat.damage.DamageCalculationResult;
import goat.minecraft.minecraftnew.subsystems.combat.damage.DamageCalculationService;
import goat.minecraft.minecraftnew.subsystems.combat.notification.DamageNotificationService;
import goat.minecraft.minecraftnew.subsystems.combat.notification.PlayerFeedbackService;
import goat.minecraft.minecraftnew.utils.stats.DefenseManager;
import goat.minecraft.minecraftnew.utils.stats.DefenseManager.DamageTag;
import goat.minecraft.minecraftnew.subsystems.combat.DamageDebugManager;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;

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
    private final CombatConfiguration config;
    
    public CombatEventHandler(DamageCalculationService damageCalculationService,
                             DamageNotificationService notificationService,
                             PlayerFeedbackService feedbackService,
                             CombatConfiguration config) {
        this.damageCalculationService = damageCalculationService;
        this.notificationService = notificationService;
        this.feedbackService = feedbackService;
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

            double baseDamage = event.getDamage();

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

            if (event.getEntity() instanceof Player player && DamageDebugManager.isEnabled(player)) {
                sendDebugInfo(player, event.getCause(), baseDamage, result.getFinalDamage());
                DamageTag tag = mapTag(event.getCause());
                double expected = DefenseManager.computeFinalDamage(baseDamage, player, tag);
                player.sendMessage(ChatColor.GRAY + "Expected Damage: " + ChatColor.YELLOW + String.format("%.2f", expected));
                player.sendMessage(ChatColor.GRAY + "Actual Damage: " + ChatColor.YELLOW + String.format("%.2f", result.getFinalDamage()));
            }

        } catch (DamageCalculationService.DamageCalculationException e) {
            logger.log(Level.SEVERE, "Critical error in damage calculation", e);
            // Don't modify damage on critical errors to maintain game stability

        } catch (Exception e) {
            logger.log(Level.WARNING, "Unexpected error in combat event handling", e);
            // Continue processing to avoid breaking the game
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageEvent event) {
        if (event instanceof EntityDamageByEntityEvent) {
            return; // handled by onEntityDamageByEntity
        }
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }
        if (!DamageDebugManager.isEnabled(player)) {
            return;
        }
        double baseDamage = event.getDamage();
        double finalDamage = event.getFinalDamage();
        sendDebugInfo(player, event.getCause(), baseDamage, finalDamage);
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

    private DamageTag mapTag(EntityDamageEvent.DamageCause cause) {
        switch (cause) {
            case ENTITY_ATTACK:
            case ENTITY_SWEEP_ATTACK:
                return DamageTag.ENTITY_ATTACK;
            case PROJECTILE:
                return DamageTag.PROJECTILE;
            case FALL:
                return DamageTag.FALL;
            case ENTITY_EXPLOSION:
            case BLOCK_EXPLOSION:
                return DamageTag.BLAST;
            case FIRE_TICK:
                return DamageTag.FIRE_TICK;
            case HOT_FLOOR:
                return DamageTag.HOT_FLOOR;
            case LAVA:
                return DamageTag.LAVA;
            case FIRE:
                return DamageTag.FIRE;
            default:
                return DamageTag.GENERIC;
        }
    }
    private void sendDebugInfo(Player player, EntityDamageEvent.DamageCause cause,
                               double baseDamage, double finalDamage) {
        DamageTag tag = mapTag(cause);
        double expected = DefenseManager.computeFinalDamage(baseDamage, player, tag);
        double reduction = baseDamage - expected;
        double percent = baseDamage > 0 ? (reduction / baseDamage) * 100.0 : 0.0;
        double totalDefense = DefenseManager.getDefense(player, tag);
        double baseDefense = DefenseManager.getDefense(player, DamageTag.GENERIC);
        double bonusDefense = totalDefense - baseDefense;

        player.sendMessage(ChatColor.GRAY + "DamageCause: " + ChatColor.YELLOW + cause.name());
        player.sendMessage(ChatColor.GRAY + "Damage Before Defense: " + ChatColor.YELLOW + String.format("%.2f", baseDamage));
        player.sendMessage(ChatColor.GRAY + "Damage Reduction from Defense: " + ChatColor.YELLOW + String.format("%.2f", reduction));
        player.sendMessage(ChatColor.GRAY + "Final Damage: " + ChatColor.YELLOW + String.format("%.2f", finalDamage));
        player.sendMessage(ChatColor.GRAY + "Bonus Defense Applied: " + ChatColor.YELLOW + String.format("%.2f", bonusDefense));
        player.sendMessage(ChatColor.GRAY + "Damage Reduction %: " + ChatColor.YELLOW + String.format("%.2f%%", percent));
    }
}