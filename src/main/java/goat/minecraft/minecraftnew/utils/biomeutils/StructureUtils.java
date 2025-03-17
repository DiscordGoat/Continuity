package goat.minecraft.minecraftnew.utils.biomeutils;

import org.bukkit.*;
import org.bukkit.entity.Entity;

/**
 * Utility class for handling structure-related operations.
 */
public class StructureUtils {

    /**
     * Finds the nearest structure of the specified type from the player's location within a 100,000 block radius.
     *
     * @param type   The type of structure to find.
     * @return A formatted string with the coordinates of the found structure, or null if not found.
     */
    public String getStructureCoordinates(StructureType type, Entity player) {


        // Locate the nearest structure of the specified type within a 100,000 block radius
        Location structureLocation = player.getLocation().getWorld().locateNearestStructure(player.getLocation(), type, 100000, false);

        if (structureLocation != null) {
            // Format the coordinates as a string
            String coordinates = ChatColor.GREEN + "Nearest " + ChatColor.AQUA + formatStructureName(type) +
                    ChatColor.GREEN + " found at: " +
                    ChatColor.AQUA + "X: " + structureLocation.getBlockX() + ", " +
                    ChatColor.AQUA + "Y: " + structureLocation.getBlockY() + ", " +
                    ChatColor.AQUA + "Z: " + structureLocation.getBlockZ();

            return coordinates;
        }

        return null;
    }


    /**
     * Formats the structure type name to a more readable format.
     *
     * @param type The StructureType to format.
     * @return A formatted string representing the structure type.
     */
    private String formatStructureName(StructureType type) {
        String name = type.getName().toLowerCase().replace("_", " ");
        String[] words = name.split(" ");
        StringBuilder formattedName = new StringBuilder();
        for (String word : words) {
            if(word.length() == 0) continue;
            formattedName.append(Character.toUpperCase(word.charAt(0)))
                    .append(word.substring(1))
                    .append(" ");
        }
        return formattedName.toString().trim();
    }
}
