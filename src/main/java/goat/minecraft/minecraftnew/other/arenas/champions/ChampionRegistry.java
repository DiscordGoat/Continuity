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
                Arrays.asList("Hello.", "Greetings", "Beware"),
                "ewogICJ0aW1lc3RhbXAiIDogMTc1MjU5NjE3MTQ4NSwKICAicHJvZmlsZUlkIiA6ICI3ZGEyYWIzYTkzY2E0OGVlODMwNDhhZmMzYjgwZTY4ZSIsCiAgInByb2ZpbGVOYW1lIiA6ICJHb2xkYXBmZWwiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNjFlYWE1M2VhNzVlZTZjNzQyNGUzZDYxMDRlOWNmMDA0Y2M5MjFkMTMwMDkyZGE4MmRlMmZiZjAxY2I0MjYiCiAgICB9CiAgfQp9",
                "u/+jaHspIYPPxSwVlHXjebtMMDQmq3tCQfknwLHhzLVTK3aLGV4TxDAo3BMjL8yorhLn+akbbJ3WMmlyOD8/skymOUEIGqOj5Rx0nyJBdAhRObCY1d0Pc6Cz9iFci6P/K7f02W70cbvU1Gm59X+9s7SM9/kQOTCQJfCbbHzAvmUx/HhoXkWdZVbRyRc67Tadl5C2LL/QznVpTBSeG9re5czoZ0+FSNYtXlfNBIvi+Jsd99m8CNpLc0CuaoJ9J4iSGHnI3kmXx1TrxMtwbVwZqeMk08mM6AyI9SHoE6hi35d9NRhY4VlH2Rk4w+HJkH+0npi6hohS+lu9NhDzQx23ejh6MakQSks6jXiXVgBaNI5qG+hmLWbCEK+PxhIuDDWFKjhbw56knE5+PuGVAC6hAiLW0PsVHhz29Z0qPN8pE5agw9b2pH+JnPwc/HQjHBr5Oki6roN0w0sgKFwDbq+X9+txHq/1fVu/iCN1iYpdQbWw5kQhUuN7m/Jvxp3cx0rpa4DQ1QgRITeMbaCvtV8sjgjeVXbJ9i5BCwNK2fFHEFDs8uxp5K786FLxJit0mp5U7703pDP1seoDZVgSmKA72p/E3f9y3oGAbwZgBJ1A/FteEK0sH+p/hvbvDthCJlGqLbufYYf72Ktz25fu9ctVRx3A2sRJk6N3C28Co71MSWI="
        ));

        CHAMPIONS.put("Monolithian", new ChampionType(
                "Monolithian",
                200,
                "monolithian.yml",
                "legendary_sword.yml",
                "dark_oak_bow.yml",
                Arrays.asList("Hello.", "Greetings", "Beware"),
                "ewogICJ0aW1lc3RhbXAiIDogMTc1MDY1Nzg0NjM5MSwKICAicHJvZmlsZUlkIiA6ICI2NDU4Mjc0MjEyNDg0MDY0YTRkMDBlNDdjZWM4ZjcyZSIsCiAgInByb2ZpbGVOYW1lIiA6ICJUaDNtMXMiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMWEzZTFhMDUwZmZkYWUyY2YzNDJmZTM4YzYxZWJhYzdlMDZhYzU4Y2UxYTYwMDNmZTIzZWE4YWU0YjU4YzYwOCIKICAgIH0KICB9Cn0=",
                "sfsTWZwTrvApbtUZBgLzAS1HSRo6gyKh//e9tOrkLqryLnF/t+gJgF4PS+pGjJXVKXjYaAak/9n+02exmP1E0JIzOPSlqqpeDgD746nXzwavsBwlCStOSCzVEjImWI4xw1F4upjwEeZHFNqUXLEgbRnA+z2xZfXV7V44m2T9bHuFaxl72hPO7xaNwRwACCVNTG0DMUXcmsuylVOhNh06IHTSdYNEZmR8i2NIp90vF9nsuqEVcSSY+aNz6oB9FfUUlHWpI/6NbqSb18cZALe6Ins79kYSMI2atX/CD1KcPfParrht9rsU1EOx8nx+lMfcmsgpYutkrWZz1PXVd48Pck0EOPCGoElJ6gjxwZPVfa2EyWk5Y/12ix4LWcJl91dpu4BOgbCQDY0B6E4F+bWFwuIl3+nEZlhRqy2AvHcWBW9T7Rnr5pRMVxA58RkQ6T7+h8Ban6brcMaaSACWSE+PIULsCNCWaFEi8w78s1VFu/QETXTaVZC9kGgHSLkx4gQxBuvhDbBrwWhw9Ws08dwmt7A4mtgKZNF+d6FdXIjBDuMVm7bqRjDVn6Q5XYPtbAnSEd3rbgGeBIijVpfE8iDKGSZR/TKzBLgm6JQ6fZG8JfYcbMiUuopcJcyCaZ9yJf7w7pFsW3b8WgPXavwFra45PmY4Rgg9UhyIeguqVF4qmOo="
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
