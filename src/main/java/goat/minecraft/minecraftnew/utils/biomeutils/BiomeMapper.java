package goat.minecraft.minecraftnew.utils.biomeutils;

import org.bukkit.block.Biome;
import java.util.HashMap;
import java.util.Map;

public class BiomeMapper {

    private static final Map<String, Biome> BIOME_MAP = new HashMap<>();

    static {
        BIOME_MAP.put("Plains", Biome.PLAINS);
        BIOME_MAP.put("Snowy Plains", Biome.SNOWY_PLAINS);
        BIOME_MAP.put("Mushroom Field", Biome.MUSHROOM_FIELDS);
        BIOME_MAP.put("Savanna", Biome.SAVANNA);
        BIOME_MAP.put("Forest", Biome.FOREST);
        BIOME_MAP.put("Birch Forest", Biome.BIRCH_FOREST);
        BIOME_MAP.put("Dark Forest", Biome.DARK_FOREST);
        BIOME_MAP.put("Flower Forest", Biome.FLOWER_FOREST);
        BIOME_MAP.put("Taiga", Biome.TAIGA);
        BIOME_MAP.put("Jungle", Biome.JUNGLE);
        BIOME_MAP.put("Bamboo Jungle", Biome.BAMBOO_JUNGLE);
        BIOME_MAP.put("Grove", Biome.GROVE);
        BIOME_MAP.put("Cherry Grove", Biome.CHERRY_GROVE);
        BIOME_MAP.put("Deep Dark", Biome.DEEP_DARK);
        BIOME_MAP.put("Dripstone Caves", Biome.DRIPSTONE_CAVES);
        BIOME_MAP.put("Lush Caves", Biome.LUSH_CAVES);
        BIOME_MAP.put("Jagged Peaks", Biome.JAGGED_PEAKS);
        BIOME_MAP.put("Meadow", Biome.MEADOW);
        BIOME_MAP.put("Swamp", Biome.SWAMP);
        BIOME_MAP.put("Mangrove Swamp", Biome.MANGROVE_SWAMP);
        BIOME_MAP.put("Badlands", Biome.BADLANDS);
        BIOME_MAP.put("Wooded Badlands", Biome.WOODED_BADLANDS);
        BIOME_MAP.put("Eroded Badlands", Biome.ERODED_BADLANDS);
        BIOME_MAP.put("Beach", Biome.BEACH);
        BIOME_MAP.put("Desert", Biome.DESERT);
        BIOME_MAP.put("Ocean", Biome.OCEAN);
        BIOME_MAP.put("Cold Ocean", Biome.COLD_OCEAN);
        BIOME_MAP.put("Deep Ocean", Biome.DEEP_OCEAN);
        BIOME_MAP.put("Frozen Ocean", Biome.FROZEN_OCEAN);
        BIOME_MAP.put("Lukewarm Ocean", Biome.LUKEWARM_OCEAN);
        BIOME_MAP.put("Warm Ocean", Biome.WARM_OCEAN);
        // Add additional mappings if necessary
    }

    /**
     * Retrieves the Biome enum corresponding to the given display name.
     *
     * @param displayName The user-friendly biome name.
     * @return The corresponding Biome enum, or null if not found.
     */
    public static Biome getBiome(String displayName) {
        return BIOME_MAP.get(displayName);
    }
}