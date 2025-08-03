package goat.minecraft.minecraftnew.subsystems.dragons;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Simple registry of available dragon types. For now only the {@link WaterDragon}
 * is registered but the structure allows additional dragons to be added later
 * without touching existing fight logic.
 */
public final class DragonRegistry {

    private static final List<Dragon> REGISTERED = new ArrayList<>();
    private static final Random RANDOM = new Random();

    static {
        // Register available dragon types.
        REGISTERED.add(new WaterDragon());
        REGISTERED.add(new FireDragon());
    }

    private DragonRegistry() {
        // Utility class
    }

    public static List<Dragon> getRegistered() {
        return Collections.unmodifiableList(REGISTERED);
    }

    /**
     * Fetches a random dragon from the registry. The current implementation
     * will always return the Water Dragon but supports future expansion.
     */
    public static Dragon randomDragon() {
        return REGISTERED.get(RANDOM.nextInt(REGISTERED.size()));
    }
}
