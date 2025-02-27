package goat.minecraft.minecraftnew.utils.devtools;

import java.util.*;
import org.bukkit.entity.Villager;

public class VillagerUtils {

    private static final Random RANDOM = new Random();

    // Map of names organized by starting letter. Each letter has 10 names; two of these are troll names.
    private static final Map<Character, List<String>> NAMES_BY_LETTER = new HashMap<>();

    static {
        NAMES_BY_LETTER.put('A', Arrays.asList(
                "Aaron", "Abel", "Adrian", "Aiden", "Alex", "Albert", "Archer", "Anthony", "Anarchy", "Apocalypse" // Trolls: Anarchy, Apocalypse
        ));
        NAMES_BY_LETTER.put('B', Arrays.asList(
                "Benjamin", "Brandon", "Brian", "Bradley", "Blake", "Bernard", "Boris", "Bruce", "BrokeBoy", "Buffoon" // Trolls: BrokeBoy, Buffoon
        ));
        NAMES_BY_LETTER.put('C', Arrays.asList(
                "Christopher", "Charles", "Connor", "Caleb", "Curtis", "Cody", "Craig", "Carson", "Crank", "Clown" // Trolls: Crank, Clown
        ));
        NAMES_BY_LETTER.put('D', Arrays.asList(
                "Daniel", "David", "Dylan", "Dominic", "Dennis", "Darren", "Damon", "Donald", "Derp", "Dork" // Trolls: Derp, Dork
        ));
        NAMES_BY_LETTER.put('E', Arrays.asList(
                "Edward", "Ethan", "Elijah", "Eric", "Evan", "Edgar", "Emmanuel", "Eugene", "Eyeroll", "Exile" // Trolls: Eyeroll, Exile
        ));
        NAMES_BY_LETTER.put('F', Arrays.asList(
                "Frederick", "Frank", "Felix", "Finn", "Francis", "Fabian", "Forrest", "Floyd", "Fiasco", "Fudge" // Trolls: Fiasco, Fudge
        ));
        NAMES_BY_LETTER.put('G', Arrays.asList(
                "Gabriel", "George", "Gavin", "Grant", "Graham", "Gregory", "Gordon", "Gerald", "Goof", "Goon" // Trolls: Goof, Goon
        ));
        NAMES_BY_LETTER.put('H', Arrays.asList(
                "Henry", "Harrison", "Hugo", "Howard", "Harvey", "Hayden", "Heath", "Hugh", "Hiccup", "Honker" // Trolls: Hiccup, Honker
        ));
        NAMES_BY_LETTER.put('I', Arrays.asList(
                "Isaac", "Ian", "Ivan", "Isaiah", "Irving", "Idris", "Ivor", "Ignacio", "Idiot", "Iggy" // Trolls: Idiot, Iggy
        ));
        NAMES_BY_LETTER.put('J', Arrays.asList(
                "Jack", "Jacob", "James", "John", "Justin", "Jason", "Joseph", "Jordan", "Jester", "Junk" // Trolls: Jester, Junk
        ));
        NAMES_BY_LETTER.put('K', Arrays.asList(
                "Kevin", "Kyle", "Keith", "Kenneth", "Kurt", "Kaden", "Kaleb", "Kingsley", "Klutz", "Kook" // Trolls: Klutz, Kook
        ));
        NAMES_BY_LETTER.put('L', Arrays.asList(
                "Liam", "Lucas", "Logan", "Leo", "Louis", "Lawrence", "Leonard", "Landon", "Loony", "Lame" // Trolls: Loony, Lame
        ));
        NAMES_BY_LETTER.put('M', Arrays.asList(
                "Matthew", "Michael", "Mark", "Mason", "Max", "Martin", "Miles", "Mitchell", "Moron", "Muppet" // Trolls: Moron, Muppet
        ));
        NAMES_BY_LETTER.put('N', Arrays.asList(
                "Nathaniel", "Nicholas", "Nolan", "Neil", "Norman", "Nigel", "Niles", "Noah", "Nerd", "Numbskull" // Trolls: Nerd, Numbskull
        ));
        NAMES_BY_LETTER.put('O', Arrays.asList(
                "Oliver", "Owen", "Oscar", "Omar", "Orion", "Otto", "Oakley", "Oswald", "Oaf", "Oinky" // Trolls: Oaf, Oinky
        ));
        NAMES_BY_LETTER.put('P', Arrays.asList(
                "Patrick", "Paul", "Peter", "Philip", "Preston", "Perry", "Porter", "Pierce", "Putz", "Peewee" // Trolls: Putz, Peewee
        ));
        NAMES_BY_LETTER.put('Q', Arrays.asList(
                "Quentin", "Quinn", "Quinton", "Quincy", "Quade", "Quillan", "Quinten", "Quoc", "Quirk", "Quack" // Trolls: Quirk, Quack
        ));
        NAMES_BY_LETTER.put('R', Arrays.asList(
                "Richard", "Robert", "Ryan", "Raymond", "Ronald", "Russell", "Rudy", "Rafael", "Rube", "Rancor" // Trolls: Rube, Rancor
        ));
        NAMES_BY_LETTER.put('S', Arrays.asList(
                "Samuel", "Scott", "Steven", "Simon", "Sean", "Spencer", "Seth", "Stanley", "Silly", "Slacker" // Trolls: Silly, Slacker
        ));
        NAMES_BY_LETTER.put('T', Arrays.asList(
                "Thomas", "Timothy", "Tyler", "Trevor", "Tristan", "Troy", "Tanner", "Theodore", "Twit", "Twerp" // Trolls: Twit, Twerp
        ));
        NAMES_BY_LETTER.put('U', Arrays.asList(
                "Ulysses", "Umar", "Urban", "Uriel", "Ulrich", "Uziel", "Ugo", "Usher", "Uncool", "Upsy" // Trolls: Uncool, Upsy
        ));
        NAMES_BY_LETTER.put('V', Arrays.asList(
                "Victor", "Vincent", "Vernon", "Vaughn", "Valentino", "Vance", "Virgil", "Valentin", "Vapid", "Vulgar" // Trolls: Vapid, Vulgar
        ));
        NAMES_BY_LETTER.put('W', Arrays.asList(
                "William", "Wesley", "Warren", "Wyatt", "Walter", "Wayne", "Winston", "Wade", "Wimp", "Wacky" // Trolls: Wimp, Wacky
        ));
        NAMES_BY_LETTER.put('X', Arrays.asList(
                "Xavier", "Xander", "Xeno", "Xerxes", "Xavi", "Xylon", "Ximenez", "Xanthus", "Xtra", "Xerox" // Trolls: Xtra, Xerox
        ));
        NAMES_BY_LETTER.put('Y', Arrays.asList(
                "Yahir", "Yosef", "Yannick", "Yannis", "Yandel", "Yancy", "Yuri", "Yvan", "Yucky", "Yodel" // Trolls: Yucky, Yodel
        ));
        NAMES_BY_LETTER.put('Z', Arrays.asList(
                "Zachary", "Zane", "Zander", "Zeke", "Zion", "Zoltan", "Zavier", "Zen", "Zany", "Zipper" // Trolls: Zany, Zipper
        ));
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
        List<Character> letters = new ArrayList<>(NAMES_BY_LETTER.keySet());
        char randomLetter = letters.get(RANDOM.nextInt(letters.size()));
        List<String> names = NAMES_BY_LETTER.get(randomLetter);
        return names.get(RANDOM.nextInt(names.size()));
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
}
