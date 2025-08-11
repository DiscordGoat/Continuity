package goat.minecraft.minecraftnew.other.arenas.champions;

import java.util.*;

/**
 * Registry holding all predefined champion types for arenas.
 */
public class ChampionRegistry {
    private static final Map<String, ChampionType> CHAMPIONS = new LinkedHashMap<>();

    static {
        CHAMPIONS.put("Legionnaire", new ChampionType(
                "Legionnaire",
                100,
                "legionnaire.yml",
                "legendary_sword.yml",
                "dark_oak_bow.yml",
                Arrays.asList("Hello.", "Greetings", "Beware")
        ));

        CHAMPIONS.put("Monolithian", new ChampionType(
                "Monolithian",
                200,
                "monolithian.yml",
                "legendary_sword.yml",
                "dark_oak_bow.yml",
                Arrays.asList("Hello.", "Greetings", "Beware")
        ));
    }

    private ChampionRegistry() { }

    public static ChampionType getChampion(String name) {
        return CHAMPIONS.get(name);
    }

    public static Collection<ChampionType> getChampions() {
        return Collections.unmodifiableCollection(CHAMPIONS.values());
    }
}
