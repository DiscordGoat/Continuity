package goat.minecraft.minecraftnew.subsystems.dragons;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Simple registry that holds all available dragons.  At the moment only the
 * {@link WaterDragon} is registered but the structure allows additional
 * dragons to be added later.
 */
public class DragonRegistry {

    private static final List<Dragon> DRAGONS = new ArrayList<>();
    private static final Random RANDOM = new Random();

    static {
        // Register the initial dragon.
        DRAGONS.add(new WaterDragon());
    }

    private DragonRegistry() {
        // Utility class
    }

    /**
     * Returns a random dragon from the registry.
     */
    public static Dragon getRandomDragon() {
        return DRAGONS.get(RANDOM.nextInt(DRAGONS.size()));
    }
}
