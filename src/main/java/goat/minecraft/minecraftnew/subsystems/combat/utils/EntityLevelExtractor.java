package goat.minecraft.minecraftnew.subsystems.combat.utils;

import org.bukkit.entity.Entity;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.logging.Logger;

/**
 * Utility class for extracting level information from entity names.
 * Handles various naming conventions and color codes.
 */
public class EntityLevelExtractor {
    
    private static final Logger logger = Logger.getLogger(EntityLevelExtractor.class.getName());
    
    // Pattern to match level indicators in entity names
    // Matches: [Level 5], (Lv.10), Level: 15, Lvl 20, etc.
    private static final Pattern LEVEL_PATTERN = Pattern.compile(
        "(?i)(?:level|lv\\.?|lvl)\\s*:?\\s*\\[?(\\d+)\\]?|\\[\\s*(?:level|lv\\.?|lvl)\\s*:?\\s*(\\d+)\\s*\\]|\\(\\s*(?:level|lv\\.?|lvl)\\s*:?\\s*(\\d+)\\s*\\)"
    );
    
    // Pattern to match standalone numbers (fallback)
    private static final Pattern NUMBER_PATTERN = Pattern.compile("\\d+");
    
    // Pattern to remove Minecraft color codes
    private static final Pattern COLOR_CODE_PATTERN = Pattern.compile("(?i)§[0-9a-fk-or]");
    
    /**
     * Extracts the level from an entity's display name.
     * 
     * @param entity The entity to extract level from
     * @return The level found in the name, or 0 if no level is found
     */
    public int extractLevelFromName(Entity entity) {
        if (entity == null) {
            return 0;
        }
        
        String name = entity.getCustomName();
        if (name == null || name.trim().isEmpty()) {
            name = entity.getName();
        }
        
        return extractLevelFromString(name);
    }
    
    /**
     * Extracts the level from a string name.
     * 
     * @param name The name string to parse
     * @return The level found in the name, or 0 if no level is found
     */
    public int extractLevelFromString(String name) {
        if (name == null || name.trim().isEmpty()) {
            return 0;
        }
        
        try {
            // Remove color codes first
            String cleanName = COLOR_CODE_PATTERN.matcher(name).replaceAll("");
            
            logger.finest(String.format("Extracting level from name: '%s' -> '%s'", name, cleanName));
            
            // Try to match structured level patterns first
            Matcher levelMatcher = LEVEL_PATTERN.matcher(cleanName);
            if (levelMatcher.find()) {
                // Check each capture group (pattern has multiple alternatives)
                for (int i = 1; i <= levelMatcher.groupCount(); i++) {
                    String group = levelMatcher.group(i);
                    if (group != null && !group.isEmpty()) {
                        int level = Integer.parseInt(group);
                        logger.fine(String.format("Found structured level %d in name: %s", level, name));
                        return level;
                    }
                }
            }
            
            // Fallback: look for any number in the name
            Matcher numberMatcher = NUMBER_PATTERN.matcher(cleanName);
            if (numberMatcher.find()) {
                String numberStr = numberMatcher.group();
                int level = Integer.parseInt(numberStr);
                
                // Sanity check: level should be reasonable (1-1000)
                if (level >= 1 && level <= 1000) {
                    logger.fine(String.format("Found fallback level %d in name: %s", level, name));
                    return level;
                }
            }
            
            logger.finest(String.format("No level found in name: %s", name));
            return 0;
            
        } catch (NumberFormatException e) {
            logger.warning(String.format("Failed to parse number from entity name '%s': %s", name, e.getMessage()));
            return 0;
        } catch (Exception e) {
            logger.warning(String.format("Unexpected error extracting level from name '%s': %s", name, e.getMessage()));
            return 0;
        }
    }
    
    /**
     * Validates if a level value is reasonable.
     * 
     * @param level The level to validate
     * @return true if the level is within acceptable bounds
     */
    public boolean isValidLevel(int level) {
        return level >= 1 && level <= 1000;
    }
    
    /**
     * Removes color codes from a string.
     * 
     * @param text The text to clean
     * @return The text without color codes
     */
    public String removeColorCodes(String text) {
        if (text == null) {
            return null;
        }
        return COLOR_CODE_PATTERN.matcher(text).replaceAll("");
    }
}