package goat.minecraft.minecraftnew.utils.devtools;

import java.util.*;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.entity.Villager;

public class VillagerNameRepository {

    private static final Random RANDOM = new Random();

    // Tracks how many times each name has been assigned
    private static final Map<String, Integer> NAME_USAGE_COUNTS = new HashMap<>();

    // Map of names organized by starting letter. Each letter has 10 names; two of these are troll names.
    private static final Map<Character, List<String>> NAMES_BY_LETTER = new HashMap<>();
    private static final Set<String> TROLL_NAMES = new HashSet<>();

    static {
        NAMES_BY_LETTER.put('A', Arrays.asList(
                "Aaron", "Abel", "Adrian", "Aiden", "Alex", "Albert", "Archer", "Anthony", "Anarchy", "Apocalypse" // Trolls: Anarchy, Apocalypse
        ));
        TROLL_NAMES.addAll(Arrays.asList("Anarchy", "Apocalypse"));
        NAMES_BY_LETTER.put('B', Arrays.asList(
                "Benjamin", "Brandon", "Brian", "Bradley", "Blake", "Bernard", "Boris", "Bruce", "BrokeBoy", "Buffoon" // Trolls: BrokeBoy, Buffoon
        ));
        TROLL_NAMES.addAll(Arrays.asList("BrokeBoy", "Buffoon"));
        NAMES_BY_LETTER.put('C', Arrays.asList(
                "Christopher", "Charles", "Connor", "Caleb", "Curtis", "Cody", "Craig", "Carson", "Crank", "Clown" // Trolls: Crank, Clown
        ));
        TROLL_NAMES.addAll(Arrays.asList("Crank", "Clown"));
        NAMES_BY_LETTER.put('D', Arrays.asList(
                "Daniel", "David", "Dylan", "Dominic", "Dennis", "Darren", "Damon", "Donald", "Derp", "Dork" // Trolls: Derp, Dork
        ));
        TROLL_NAMES.addAll(Arrays.asList("Derp", "Dork"));
        NAMES_BY_LETTER.put('E', Arrays.asList(
                "Edward", "Ethan", "Elijah", "Eric", "Evan", "Edgar", "Emmanuel", "Eugene", "Eyeroll", "Exile" // Trolls: Eyeroll, Exile
        ));
        TROLL_NAMES.addAll(Arrays.asList("Eyeroll", "Exile"));
        NAMES_BY_LETTER.put('F', Arrays.asList(
                "Frederick", "Frank", "Felix", "Finn", "Francis", "Fabian", "Forrest", "Floyd", "Fiasco", "Fudge" // Trolls: Fiasco, Fudge
        ));
        TROLL_NAMES.addAll(Arrays.asList("Fiasco", "Fudge"));
        NAMES_BY_LETTER.put('G', Arrays.asList(
                "Gabriel", "George", "Gavin", "Grant", "Graham", "Gregory", "Gordon", "Gerald", "Goof", "Goon" // Trolls: Goof, Goon
        ));
        TROLL_NAMES.addAll(Arrays.asList("Goof", "Goon"));
        NAMES_BY_LETTER.put('H', Arrays.asList(
                "Henry", "Harrison", "Hugo", "Howard", "Harvey", "Hayden", "Heath", "Hugh", "Hiccup", "Honker" // Trolls: Hiccup, Honker
        ));
        TROLL_NAMES.addAll(Arrays.asList("Hiccup", "Honker"));
        NAMES_BY_LETTER.put('I', Arrays.asList(
                "Isaac", "Ian", "Ivan", "Isaiah", "Irving", "Idris", "Ivor", "Ignacio", "Idiot", "Iggy" // Trolls: Idiot, Iggy
        ));
        TROLL_NAMES.addAll(Arrays.asList("Idiot", "Iggy"));
        NAMES_BY_LETTER.put('J', Arrays.asList(
                "Jack", "Jacob", "James", "John", "Justin", "Jason", "Joseph", "Jordan", "Jester", "Junk" // Trolls: Jester, Junk
        ));
        TROLL_NAMES.addAll(Arrays.asList("Jester", "Junk"));
        NAMES_BY_LETTER.put('K', Arrays.asList(
                "Kevin", "Kyle", "Keith", "Kenneth", "Kurt", "Kaden", "Kaleb", "Kingsley", "Klutz", "Kook" // Trolls: Klutz, Kook
        ));
        TROLL_NAMES.addAll(Arrays.asList("Klutz", "Kook"));
        NAMES_BY_LETTER.put('L', Arrays.asList(
                "Liam", "Lucas", "Logan", "Leo", "Louis", "Lawrence", "Leonard", "Landon", "Loony", "Lame" // Trolls: Loony, Lame
        ));
        TROLL_NAMES.addAll(Arrays.asList("Loony", "Lame"));
        NAMES_BY_LETTER.put('M', Arrays.asList(
                "Matthew", "Michael", "Mark", "Mason", "Max", "Martin", "Miles", "Mitchell", "Moron", "Muppet" // Trolls: Moron, Muppet
        ));
        TROLL_NAMES.addAll(Arrays.asList("Moron", "Muppet"));
        NAMES_BY_LETTER.put('N', Arrays.asList(
                "Nathaniel", "Nicholas", "Nolan", "Neil", "Norman", "Nigel", "Niles", "Noah", "Nerd", "Numbskull" // Trolls: Nerd, Numbskull
        ));
        TROLL_NAMES.addAll(Arrays.asList("Nerd", "Numbskull"));
        NAMES_BY_LETTER.put('O', Arrays.asList(
                "Oliver", "Owen", "Oscar", "Omar", "Orion", "Otto", "Oakley", "Oswald", "Oaf", "Oinky" // Trolls: Oaf, Oinky
        ));
        TROLL_NAMES.addAll(Arrays.asList("Oaf", "Oinky"));
        NAMES_BY_LETTER.put('P', Arrays.asList(
                "Patrick", "Paul", "Peter", "Philip", "Preston", "Perry", "Porter", "Pierce", "Putz", "Peewee" // Trolls: Putz, Peewee
        ));
        TROLL_NAMES.addAll(Arrays.asList("Putz", "Peewee"));
        NAMES_BY_LETTER.put('Q', Arrays.asList(
                "Quentin", "Quinn", "Quinton", "Quincy", "Quade", "Quillan", "Quinten", "Quoc", "Quirk", "Quack" // Trolls: Quirk, Quack
        ));
        TROLL_NAMES.addAll(Arrays.asList("Quirk", "Quack"));
        NAMES_BY_LETTER.put('R', Arrays.asList(
                "Richard", "Robert", "Ryan", "Raymond", "Ronald", "Russell", "Rudy", "Rafael", "Rube", "Rancor" // Trolls: Rube, Rancor
        ));
        TROLL_NAMES.addAll(Arrays.asList("Rube", "Rancor"));
        NAMES_BY_LETTER.put('S', Arrays.asList(
                "Samuel", "Scott", "Steven", "Simon", "Sean", "Spencer", "Seth", "Stanley", "Silly", "Slacker" // Trolls: Silly, Slacker
        ));
        TROLL_NAMES.addAll(Arrays.asList("Silly", "Slacker"));
        NAMES_BY_LETTER.put('T', Arrays.asList(
                "Thomas", "Timothy", "Tyler", "Trevor", "Tristan", "Troy", "Tanner", "Theodore", "Twit", "Twerp" // Trolls: Twit, Twerp
        ));
        TROLL_NAMES.addAll(Arrays.asList("Twit", "Twerp"));
        NAMES_BY_LETTER.put('U', Arrays.asList(
                "Ulysses", "Umar", "Urban", "Uriel", "Ulrich", "Uziel", "Ugo", "Usher", "Uncool", "Upsy" // Trolls: Uncool, Upsy
        ));
        TROLL_NAMES.addAll(Arrays.asList("Uncool", "Upsy"));
        NAMES_BY_LETTER.put('V', Arrays.asList(
                "Victor", "Vincent", "Vernon", "Vaughn", "Valentino", "Vance", "Virgil", "Valentin", "Vapid", "Vulgar" // Trolls: Vapid, Vulgar
        ));
        TROLL_NAMES.addAll(Arrays.asList("Vapid", "Vulgar"));
        NAMES_BY_LETTER.put('W', Arrays.asList(
                "William", "Wesley", "Warren", "Wyatt", "Walter", "Wayne", "Winston", "Wade", "Wimp", "Wacky" // Trolls: Wimp, Wacky
        ));
        TROLL_NAMES.addAll(Arrays.asList("Wimp", "Wacky"));
        NAMES_BY_LETTER.put('X', Arrays.asList(
                "Xavier", "Xander", "Xeno", "Xerxes", "Xavi", "Xylon", "Ximenez", "Xanthus", "Xtra", "Xerox" // Trolls: Xtra, Xerox
        ));
        TROLL_NAMES.addAll(Arrays.asList("Xtra", "Xerox"));
        NAMES_BY_LETTER.put('Y', Arrays.asList(
                "Yahir", "Yosef", "Yannick", "Yannis", "Yandel", "Yancy", "Yuri", "Yvan", "Yucky", "Yodel" // Trolls: Yucky, Yodel
        ));
        TROLL_NAMES.addAll(Arrays.asList("Yucky", "Yodel"));
        NAMES_BY_LETTER.put('Z', Arrays.asList(
                "Zachary", "Zane", "Zander", "Zeke", "Zion", "Zoltan", "Zavier", "Zen", "Zany", "Zipper" // Trolls: Zany, Zipper
        ));
        TROLL_NAMES.addAll(Arrays.asList("Zany", "Zipper"));
    }

    // Returns the set of villager names currently present in the world
    private static Set<String> getCurrentlyUsedNames() {
        Set<String> used = new HashSet<>();
        for (World world : Bukkit.getWorlds()) {
            for (Villager villager : world.getEntitiesByClass(Villager.class)) {
                String custom = villager.getCustomName();
                if (custom != null) {
                    used.add(ChatColor.stripColor(custom));
                }
            }
        }
        return used;
    }

    // Map of funny names for specific villager professions.
    private static final Map<Villager.Profession, String> FUNNY_PROFESSION_NAMES = new HashMap<>();

    static {
        FUNNY_PROFESSION_NAMES.put(Villager.Profession.FARMER, "McDonald");
        FUNNY_PROFESSION_NAMES.put(Villager.Profession.LIBRARIAN, "Bookworm");
        FUNNY_PROFESSION_NAMES.put(Villager.Profession.CLERIC, "Holy Smokes");
        FUNNY_PROFESSION_NAMES.put(Villager.Profession.MASON, "Mason");
        FUNNY_PROFESSION_NAMES.put(Villager.Profession.WEAPONSMITH, "Swordy McSwordface");
        FUNNY_PROFESSION_NAMES.put(Villager.Profession.FLETCHER, "Fletcher");
        FUNNY_PROFESSION_NAMES.put(Villager.Profession.CARTOGRAPHER, "Mapman");
        FUNNY_PROFESSION_NAMES.put(Villager.Profession.LEATHERWORKER, "Leatherface");
        FUNNY_PROFESSION_NAMES.put(Villager.Profession.SHEPHERD, "Baa-baa Bob");
        FUNNY_PROFESSION_NAMES.put(Villager.Profession.TOOLSMITH, "Toolman");
        FUNNY_PROFESSION_NAMES.put(Villager.Profession.ARMORER, "Plate");
        FUNNY_PROFESSION_NAMES.put(Villager.Profession.FISHERMAN, "Fishy");
        FUNNY_PROFESSION_NAMES.put(Villager.Profession.BUTCHER, "Meathead");
    }

    /**
     * Returns a random male name from the extensive list.
     * The method randomly selects one letter from A to Z,
     * then returns a random name from that letter’s list.
     *
     * @return A random male name.
     */
    public static String getRandomMaleName() {
        List<String> allNames = NAMES_BY_LETTER.values().stream()
                .flatMap(Collection::stream)
                .collect(Collectors.toList());

        Set<String> usedNames = getCurrentlyUsedNames();
        List<String> available = allNames.stream()
                .filter(name -> !usedNames.contains(name))
                .collect(Collectors.toList());

        String chosenName;
        if (!available.isEmpty()) {
            chosenName = available.get(RANDOM.nextInt(available.size()));
        } else {
            chosenName = allNames.stream()
                    .min(Comparator.comparingInt(n -> NAME_USAGE_COUNTS.getOrDefault(n, 0)))
                    .orElse(allNames.get(RANDOM.nextInt(allNames.size())));
        }

        NAME_USAGE_COUNTS.merge(chosenName, 1, Integer::sum);
        return chosenName;
    }

    /**
     * Returns a funny name based on the villager's profession.
     * If the profession is mapped to a funny name, that name is returned;
     * otherwise, a random male name is returned.
     *
     * @param profession The villager's profession.
     * @return A funny name corresponding to the profession.
     */
    public static String getFunnyNameForProfession(Villager.Profession profession) {
        return FUNNY_PROFESSION_NAMES.getOrDefault(profession, getRandomMaleName());
    }

    /**
     * Checks if the provided name is one of the predefined troll names.
     */
    public static boolean isTrollName(String name) {
        return TROLL_NAMES.contains(name);
    }
}
