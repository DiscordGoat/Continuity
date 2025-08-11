package goat.minecraft.minecraftnew.other.arenas;

import org.bukkit.Location;

/**
 * Represents a generated arena location and whether its structure has been placed.
 */
public class Arena {
    private final Location location;
    private boolean isPlaced;

    public Arena(Location location, boolean isPlaced) {
        this.location = location;
        this.isPlaced = isPlaced;
    }

    public Location getLocation() {
        return location;
    }

    public boolean isPlaced() {
        return isPlaced;
    }

    public void setPlaced(boolean placed) {
        this.isPlaced = placed;
    }
}
