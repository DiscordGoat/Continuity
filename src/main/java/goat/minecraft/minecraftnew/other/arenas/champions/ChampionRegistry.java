package goat.minecraft.minecraftnew.other.arenas.champions;

import org.bukkit.ChatColor;
import java.util.*;

/**
 * Registry holding all predefined champion types for arenas with their associated blessings.
 */
public class ChampionRegistry {
    private static final Map<String, ChampionType> CHAMPIONS = new LinkedHashMap<>();
    private static final Map<String, Set<ChampionBlessing>> CHAMPION_BLESSINGS = new LinkedHashMap<>();
    private static final Map<String, ChatColor> CHAMPION_COLORS = new LinkedHashMap<>();

    static {
        // Legionnaire - deals 50% more arrow damage
        CHAMPIONS.put("Legionnaire", new ChampionType(
                "Legionnaire",
                100,
                "legionnaire.yml",
                "legendary_sword.yml",
                "dark_oak_bow.yml",
                Arrays.asList("Hello.", "Greetings", "Beware"),
                "ewogICJ0aW1lc3RhbXAiIDogMTc1MjU5NjE3MTQ4NSwKICAicHJvZmlsZUlkIiA6ICI3ZGEyYWIzYTkzY2E0OGVlODMwNDhhZmMzYjgwZTY4ZSIsCiAgInByb2ZpbGVOYW1lIiA6ICJHb2xkYXBmZWwiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJNiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNjFlYWE1M2VhNzVlZTZjNzQyNGUzZDYxMDRlOWNmMDA0Y2M5MjFkMTMwMDkyZGE4MmRlMmZiZjAxY2I0MjYiCiAgICB9CiAgfQp9",
                "u/+jaHspIYPPxSwVlHXjebtMMDQmq3tCQfknwLHhzLVTK3aLGV4TxDAo3BMjL8yorhLn+akbbJ3WMmlyOD8/skymOUEIGqOj5Rx0nyJBdAhRObCY1d0Pc6Cz9iFci6P/K7f02W70cbvU1Gm59X+9s7SM9/kQOTCQJfCbbHzAvmUx/HhoXkWdZVbRyRc67Tadl5C2LL/QznVpTBSeG9re5czoZ0+FSNYtXlfNBIvi+Jsd99m8CNpLc0CuaoJ9J4iSGHnI3kmXx1TrxMtwbVwZqeMk08mM6AyI9SHoE6hi35d9NRhY4VlH2Rk4w+HJkH+0npi6hohS+lu9NhDzQx23ejh6MakQSks6jXiXVgBaNI5qG+hmLWbCEK+PxhIuDDWFKjhbw56knE5+PuGVAC6hAiLW0PsVHhz29Z0qPN8pE5agw9b2pH+JnPwc/HQjHBr5Oki6roN0w0sgKFwDbq+X9+txHq/1fVu/iCN1iYpdQbWw5kQhUuN7m/Jvxp3cx0rpa4DQ1QgRITeMbaCvtV8sjgjeVXbJ9i5BCwNK2fFHEFDs8uxp5K786FLxJit0mp5U7703pDP1seoDZVgSmKA72p/E3f9y3oGAbwZgBJ1A/FteEK0sH+p/hvbvDthCJlGqLbufYYf72Ktz25fu9ctVRx3A2sRJk6N3C28Co71MSWI="
        ));
        CHAMPION_BLESSINGS.put("Legionnaire", Set.of(ChampionBlessing.ENHANCED_ARCHERY));
        CHAMPION_COLORS.put("Legionnaire", ChatColor.DARK_RED);

        // Monolithian - has 200 HP instead of 100
        CHAMPIONS.put("Monolithian", new ChampionType(
                "Monolithian",
                200,
                "monolithian.yml",
                "legendary_sword.yml",
                "dark_oak_bow.yml",
                Arrays.asList("Hello.", "Greetings", "Beware"),
                "ewogICJ0aW1lc3RhbXAiIDogMTc1MDY1Nzg0NjM5MSwKICAicHJvZmlsZUlkIiA6ICI2NDU4Mjc0MjEyNDg0MDY0YTRkMDBlNDdjZWM4ZjcyZSIsCiAgInByb2ZpbGVOYW1lIiA6ICJUaDNtMXMiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJNiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMWEzZTFhMDUwZmZkYWUyY2YzNDJmZTM4YzYxZWJhYzdlMDZhYzU4Y2UxYTYwMDNmZTIzZWE4YWU0YjU4YzYwOCIKICAgIH0KICB9Cn0=",
                "sfsTWZwTrvApbtUZBgLzAS1HSRo6gyKh//e9tOrkLqryLnF/t+gJgF4PS+pGjJXVKXjYaAak/9n+02exmP1E0JIzOPSlqqpeDgD746nXzwavsBwlCStOSCzVEjImWI4xw1F4upjwEeZHFNqUXLEgbRnA+z2xZfXV7V44m2T9bHuFaxl72hPO7xaNwRwACCVNTG0DMUXcmsuylVOhNh06IHTSdYNEZmR8i2NIp90vF9nsuqEVcSSY+aNz6oB9FfUUlHWpI/6NbqSb18cZALe6Ins79kYSMI2atX/CD1KcPfParrht9rsU1EOx8nx+lMfcmsgpYutkrWZz1PXVd48Pck0EOPCGoElJ6gjxwZPVfa2EyWk5Y/12ix4LWcJl91dpu4BOgbCQDY0B6E4F+bWFwuIl3+nEZlhRqy2AvHcWBW9T7Rnr5pRMVxA58RkQ6T7+h8Ban6brcMaaSACWSE+PIULsCNCWaFEi8w78s1VFu/QETXTaVZC9kGgHSLkx4gQxBuvhDbBrwWhw9Ws08dwmt7A4mtgKZNF+d6FdXIjBDuMVm7bqRjDVn6Q5XYPtbAnSEd3rbgGeBIijVpfE8iDKGSZR/TKzBLgm6JQ6fZG8JfYcbMiUuopcJcyCaZ9yJf7w7pFsW3b8WgPXavwFra45PmY4Rgg9UhyIeguqVF4qmOo="
        ));
        CHAMPION_BLESSINGS.put("Monolithian", Set.of(ChampionBlessing.ENHANCED_VITALITY));
        CHAMPION_COLORS.put("Monolithian", ChatColor.DARK_RED);

        // Arsonist - lights player on fire when striking them
        CHAMPIONS.put("Arsonist", new ChampionType(
                "Arsonist",
                100,
                "arsonist.yml",
                "legendary_sword.yml",
                "dark_oak_bow.yml",
                Arrays.asList("Burn.", "Feel the flames", "Ashes to ashes"),
                "placeholder_skin_value", // TODO: Add actual skin
                "placeholder_skin_signature" // TODO: Add actual signature
        ));
        CHAMPION_BLESSINGS.put("Arsonist", Set.of(ChampionBlessing.FLAME_STRIKE));
        CHAMPION_COLORS.put("Arsonist", ChatColor.DARK_RED);

        // Dweller - incurs darkness every 50s
        CHAMPIONS.put("Dweller", new ChampionType(
                "Dweller",
                100,
                "dweller.yml",
                "legendary_sword.yml",
                "dark_oak_bow.yml",
                Arrays.asList("Darkness consumes", "From the depths", "Light fades"),
                "placeholder_skin_value", // TODO: Add actual skin
                "placeholder_skin_signature" // TODO: Add actual signature
        ));
        CHAMPION_BLESSINGS.put("Dweller", Set.of(ChampionBlessing.DARKNESS_AURA));
        CHAMPION_COLORS.put("Dweller", ChatColor.DARK_RED);

        // Headless Horseman - disables player's natural regeneration for 5s on 25% of hits
        CHAMPIONS.put("Headless Horseman", new ChampionType(
                "Headless Horseman",
                100,
                "headless_horseman.yml",
                "legendary_sword.yml",
                "dark_oak_bow.yml",
                Arrays.asList("Where is my head?", "Eternal hunt", "No rest"),
                "placeholder_skin_value", // TODO: Add actual skin
                "placeholder_skin_signature" // TODO: Add actual signature
        ));
        CHAMPION_BLESSINGS.put("Headless Horseman", Set.of(ChampionBlessing.REGENERATION_CURSE));
        CHAMPION_COLORS.put("Headless Horseman", ChatColor.DARK_RED);

        // Nature's Spirit - summons a Forest Spirit every 50s
        CHAMPIONS.put("Nature's Spirit", new ChampionType(
                "Nature's Spirit",
                100,
                "natures_spirit.yml",
                "legendary_sword.yml",
                "dark_oak_bow.yml",
                Arrays.asList("Nature calls", "The forest awakens", "Growth and decay"),
                "placeholder_skin_value", // TODO: Add actual skin
                "placeholder_skin_signature" // TODO: Add actual signature
        ));
        CHAMPION_BLESSINGS.put("Nature's Spirit", Set.of(ChampionBlessing.FOREST_SUMMONING));
        CHAMPION_COLORS.put("Nature's Spirit", ChatColor.DARK_RED);

        // Countersniper - immune to arrows
        CHAMPIONS.put("Countersniper", new ChampionType(
                "Countersniper",
                100,
                "countersniper.yml",
                "legendary_sword.yml",
                "dark_oak_bow.yml",
                Arrays.asList("Nice try", "Arrows are useless", "Counter-attack"),
                "placeholder_skin_value", // TODO: Add actual skin
                "placeholder_skin_signature" // TODO: Add actual signature
        ));
        CHAMPION_BLESSINGS.put("Countersniper", Set.of(ChampionBlessing.ARROW_IMMUNITY));
        CHAMPION_COLORS.put("Countersniper", ChatColor.DARK_RED);

        // Shadow Assassin - teleports behind player and enters SWORD phase when hit, 15s cooldown
        CHAMPIONS.put("Shadow Assassin", new ChampionType(
                "Shadow Assassin",
                100,
                "shadow_assassin.yml",
                "legendary_sword.yml",
                "dark_oak_bow.yml",
                Arrays.asList("From the shadows", "Nothing personnel", "Behind you"),
                "placeholder_skin_value", // TODO: Add actual skin
                "placeholder_skin_signature" // TODO: Add actual signature
        ));
        CHAMPION_BLESSINGS.put("Shadow Assassin", Set.of(ChampionBlessing.SHADOW_TELEPORT));
        CHAMPION_COLORS.put("Shadow Assassin", ChatColor.DARK_RED);

        // Butcher - deals 50% more sword damage
        CHAMPIONS.put("Butcher", new ChampionType(
                "Butcher",
                100,
                "butcher.yml",
                "legendary_sword.yml",
                "dark_oak_bow.yml",
                Arrays.asList("Fresh meat", "Time to carve", "The cleaver awaits"),
                "placeholder_skin_value", // TODO: Add actual skin
                "placeholder_skin_signature" // TODO: Add actual signature
        ));
        CHAMPION_BLESSINGS.put("Butcher", Set.of(ChampionBlessing.ENHANCED_MELEE));
        CHAMPION_COLORS.put("Butcher", ChatColor.DARK_RED);

        // Duskblood - teleports sometimes
        CHAMPIONS.put("Duskblood", new ChampionType(
                "Duskblood",
                100,
                "duskblood.yml",
                "legendary_sword.yml",
                "dark_oak_bow.yml",
                Arrays.asList("Between worlds", "Here and there", "Nowhere and everywhere"),
                "placeholder_skin_value", // TODO: Add actual skin
                "placeholder_skin_signature" // TODO: Add actual signature
        ));
        CHAMPION_BLESSINGS.put("Duskblood", Set.of(ChampionBlessing.RANDOM_TELEPORT));
        CHAMPION_COLORS.put("Duskblood", ChatColor.DARK_RED);

        // Fury - strikes player with lightning for 25% true max health damage, 30s cooldown
        CHAMPIONS.put("Fury", new ChampionType(
                "Fury",
                100,
                "fury.yml",
                "legendary_sword.yml",
                "dark_oak_bow.yml",
                Arrays.asList("Thunder roars", "Lightning strikes", "Feel my wrath"),
                "placeholder_skin_value", // TODO: Add actual skin
                "placeholder_skin_signature" // TODO: Add actual signature
        ));
        CHAMPION_BLESSINGS.put("Fury", Set.of(ChampionBlessing.LIGHTNING_STRIKE));
        CHAMPION_COLORS.put("Fury", ChatColor.DARK_RED);

        // Leviathan - summons a random legendary sea creature every 30s
        CHAMPIONS.put("Leviathan", new ChampionType(
                "Leviathan",
                100,
                "leviathan.yml",
                "legendary_sword.yml",
                "dark_oak_bow.yml",
                Arrays.asList("From the depths", "The ocean calls", "Behold the abyss"),
                "placeholder_skin_value", // TODO: Add actual skin
                "placeholder_skin_signature" // TODO: Add actual signature
        ));
        CHAMPION_BLESSINGS.put("Leviathan", Set.of(ChampionBlessing.SEA_SUMMONING));
        CHAMPION_COLORS.put("Leviathan", ChatColor.DARK_RED);
    }

    private ChampionRegistry() { }

    public static ChampionType getChampion(String name) {
        return CHAMPIONS.get(name);
    }

    public static Collection<ChampionType> getChampions() {
        return Collections.unmodifiableCollection(CHAMPIONS.values());
    }

    /**
     * Gets the blessings associated with a specific Champion type.
     */
    public static Set<ChampionBlessing> getBlessings(String championName) {
        return CHAMPION_BLESSINGS.getOrDefault(championName, Set.of());
    }

    /**
     * Checks if a Champion has a specific blessing.
     */
    public static boolean hasBlessing(String championName, ChampionBlessing blessing) {
        Set<ChampionBlessing> blessings = getBlessings(championName);
        return blessings.contains(blessing);
    }

    /**
     * Gets all Champion names.
     */
    public static Set<String> getChampionNames() {
        return Collections.unmodifiableSet(CHAMPIONS.keySet());
    }

    /**
     * Gets the color associated with a specific Champion type.
     */
    public static ChatColor getChampionColor(String championName) {
        return CHAMPION_COLORS.getOrDefault(championName, ChatColor.DARK_RED);
    }

    /**
     * Gets a random greeting from the specified Champion's greeting list.
     */
    public static String getRandomGreeting(String championName) {
        ChampionType champion = CHAMPIONS.get(championName);
        if (champion == null || champion.getGreetingMessages().isEmpty()) {
            return "..."; // Default silent greeting
        }
        
        List<String> greetings = champion.getGreetingMessages();
        return greetings.get((int) (Math.random() * greetings.size()));
    }
}
