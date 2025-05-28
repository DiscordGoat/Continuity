package goat.minecraft.minecraftnew.subsystems.combat.notification;

import goat.minecraft.minecraftnew.subsystems.combat.config.CombatConfiguration;
import goat.minecraft.minecraftnew.subsystems.combat.damage.DamageCalculationResult;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Service for providing feedback to players about combat events.
 * Handles action bar messages, sounds, and other player notifications.
 */
public class PlayerFeedbackService {
    
    private static final Logger logger = Logger.getLogger(PlayerFeedbackService.class.getName());
    
    private final CombatConfiguration.SoundConfig soundConfig;
    
    public PlayerFeedbackService(CombatConfiguration.SoundConfig soundConfig) {
        this.soundConfig = soundConfig;
    }
    
    /**
     * Sends damage boost feedback to a player via action bar and sound.
     * 
     * @param player The player to send feedback to
     * @param result The damage calculation result containing modifier information
     */
    public void sendDamageBoostFeedback(Player player, DamageCalculationResult result) {
        if (player == null || !result.wasModified()) {
            return;
        }
        
        try {
            // Calculate total multiplier for display
            double totalMultiplier = result.getTotalMultiplier();
            
            // Create action bar message
            String message = ChatColor.GREEN + "Damage Boost: x" + String.format("%.2f", totalMultiplier);
            
            // Add modifier details for significant boosts
            if (totalMultiplier > 1.5) {
                message += ChatColor.GOLD + " ⚔";
            } else if (totalMultiplier > 1.2) {
                message += ChatColor.YELLOW + " ⚡";
            }
            
            // Send action bar message
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(message));
            
            // Play sound effect
            playDamageBoostSound(player, totalMultiplier);
            
            logger.finest(String.format("Sent damage boost feedback to %s: %.2fx multiplier", 
                         player.getName(), totalMultiplier));
            
        } catch (Exception e) {
            logger.log(Level.WARNING, 
                      String.format("Failed to send damage boost feedback to %s", player.getName()), e);
        }
    }
    
    /**
     * Sends a custom feedback message to a player.
     * 
     * @param player The player to send the message to
     * @param message The message to send
     * @param type The type of message (ACTION_BAR, CHAT, etc.)
     */
    public void sendCustomFeedback(Player player, String message, ChatMessageType type) {
        if (player == null || message == null || type == null) {
            return;
        }
        
        try {
            player.spigot().sendMessage(type, new TextComponent(message));
            
        } catch (Exception e) {
            logger.log(Level.WARNING, 
                      String.format("Failed to send custom feedback to %s: %s", player.getName(), message), e);
        }
    }
    
    /**
     * Sends a combat notification to a player's chat.
     * 
     * @param player The player to notify
     * @param message The notification message
     */
    public void sendCombatNotification(Player player, String message) {
        sendCustomFeedback(player, ChatColor.GRAY + "[Combat] " + message, ChatMessageType.CHAT);
    }
    
    /**
     * Plays a damage boost sound effect with dynamic pitch based on multiplier.
     * 
     * @param player The player to play the sound for
     * @param multiplier The damage multiplier (affects pitch)
     */
    private void playDamageBoostSound(Player player, double multiplier) {
        try {
            // Parse sound from config, fall back to default if invalid
            Sound sound;
            try {
                sound = Sound.valueOf(soundConfig.getDamageBoostSound());
            } catch (IllegalArgumentException e) {
                logger.warning(String.format("Invalid sound in config: %s, using default", 
                              soundConfig.getDamageBoostSound()));
                sound = Sound.ENTITY_ARROW_HIT_PLAYER;
            }
            
            // Adjust pitch based on damage multiplier
            float pitch = soundConfig.getPitch();
            if (multiplier > 2.0) {
                pitch += 0.3f; // Higher pitch for big multipliers
            } else if (multiplier > 1.5) {
                pitch += 0.1f; // Slightly higher pitch for medium multipliers
            }
            
            // Clamp pitch to valid range
            pitch = Math.max(0.5f, Math.min(2.0f, pitch));
            
            player.playSound(player.getLocation(), sound, soundConfig.getVolume(), pitch);
            
        } catch (Exception e) {
            logger.log(Level.WARNING, 
                      String.format("Failed to play damage boost sound for %s", player.getName()), e);
        }
    }
    
    /**
     * Sends a level-up notification for combat skills.
     * 
     * @param player The player who leveled up
     * @param skill The skill that leveled up
     * @param newLevel The new level achieved
     */
    public void sendSkillLevelUpNotification(Player player, String skill, int newLevel) {
        if (player == null || skill == null) {
            return;
        }
        
        String message = String.format("%s%s%s skill leveled up! New level: %s%d", 
                                     ChatColor.GOLD, skill, ChatColor.GREEN, ChatColor.YELLOW, newLevel);
        
        sendCustomFeedback(player, message, ChatMessageType.ACTION_BAR);
        
        // Play a different sound for level ups
        try {
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.7f, 1.2f);
        } catch (Exception e) {
            logger.log(Level.WARNING, "Failed to play level up sound", e);
        }
    }
    
    /**
     * Sends an error message to a player.
     * 
     * @param player The player to send the error to
     * @param error The error message
     */
    public void sendErrorMessage(Player player, String error) {
        if (player == null || error == null) {
            return;
        }
        
        String message = ChatColor.RED + "Combat Error: " + error;
        sendCustomFeedback(player, message, ChatMessageType.CHAT);
    }
}