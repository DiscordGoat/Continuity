package goat.minecraft.minecraftnew.subsystems.villagers;

import java.util.*;

public class VillagerChatLines {
    private static final List<String> GENERIC_OPEN_LINES = Arrays.asList(
            "Hello there!", "Welcome, take a look.", "Greetings, traveler.", "Care to trade?"
    );
    private static final List<String> GENERIC_CLOSE_LINES = Arrays.asList(
            "Come back anytime!", "Pleasure doing business.", "Farewell.", "See you around!"
    );

    private static final Map<String, String> TROLL_OPEN_LINES = new HashMap<>();
    private static final Map<String, String> TROLL_CLOSE_LINES = new HashMap<>();

    static {
        // Populate troll lines
        TROLL_OPEN_LINES.put("Anarchy", "What do you want now?");
        TROLL_CLOSE_LINES.put("Anarchy", "Beat it.");
        TROLL_OPEN_LINES.put("Apocalypse", "Hurry, the end is nigh!");
        TROLL_CLOSE_LINES.put("Apocalypse", "Too late for regrets.");
        TROLL_OPEN_LINES.put("BrokeBoy", "Got any handouts?");
        TROLL_CLOSE_LINES.put("BrokeBoy", "Figures you'd leave.");
        TROLL_OPEN_LINES.put("Buffoon", "Hey boss, wanna waste money?");
        TROLL_CLOSE_LINES.put("Buffoon", "Don't trip on the door on your way out.");
        TROLL_OPEN_LINES.put("Crank", "Ugh, another customer.");
        TROLL_CLOSE_LINES.put("Crank", "Finally, peace.");
        TROLL_OPEN_LINES.put("Clown", "Step right up, spend it all!");
        TROLL_CLOSE_LINES.put("Clown", "The joke's on you.");
        TROLL_OPEN_LINES.put("Derp", "Huh? Oh, trades, right.");
        TROLL_CLOSE_LINES.put("Derp", "Did I do good?");
        TROLL_OPEN_LINES.put("Dork", "Boss, you're back again?");
        TROLL_CLOSE_LINES.put("Dork", "Guess that's it then.");
        TROLL_OPEN_LINES.put("Eyeroll", "Yeah yeah, buy something.");
        TROLL_CLOSE_LINES.put("Eyeroll", "Whatever.");
        TROLL_OPEN_LINES.put("Exile", "You shouldn't even be here.");
        TROLL_CLOSE_LINES.put("Exile", "Get lost.");
        TROLL_OPEN_LINES.put("Fiasco", "Let's ruin your day.");
        TROLL_CLOSE_LINES.put("Fiasco", "Told you it'd end badly.");
        TROLL_OPEN_LINES.put("Fudge", "Sticky fingers? Buy fast.");
        TROLL_CLOSE_LINES.put("Fudge", "Great, now I'm hungry.");
        TROLL_OPEN_LINES.put("Goof", "Heh, let's mess this up.");
        TROLL_CLOSE_LINES.put("Goof", "See ya, sucker.");
        TROLL_OPEN_LINES.put("Goon", "Whatcha lookin' at?");
        TROLL_CLOSE_LINES.put("Goon", "Out you go.");
        TROLL_OPEN_LINES.put("Hiccup", "Oops, I forgot the prices.");
        TROLL_CLOSE_LINES.put("Hiccup", "Burp... bye.");
        TROLL_OPEN_LINES.put("Honker", "Honk honk, buy stuff!");
        TROLL_CLOSE_LINES.put("Honker", "Hooonk, leave already.");
        TROLL_OPEN_LINES.put("Idiot", "Hope you know what you're doing.");
        TROLL_CLOSE_LINES.put("Idiot", "I'm confused, goodbye.");
        TROLL_OPEN_LINES.put("Iggy", "Iggy wants emeralds!");
        TROLL_CLOSE_LINES.put("Iggy", "Iggy tired now.");
        TROLL_OPEN_LINES.put("Jester", "I'll mock your purchases.");
        TROLL_CLOSE_LINES.put("Jester", "Ha! Done already?");
        TROLL_OPEN_LINES.put("Junk", "Selling trash, interested?");
        TROLL_CLOSE_LINES.put("Junk", "My junk misses you already.");
        TROLL_OPEN_LINES.put("Klutz", "Oops! Dropped the goods.");
        TROLL_CLOSE_LINES.put("Klutz", "Hope I didn't break anything.");
        TROLL_OPEN_LINES.put("Kook", "Crazy deals today!");
        TROLL_CLOSE_LINES.put("Kook", "Back to my weirdness.");
        TROLL_OPEN_LINES.put("Loony", "Careful, I'm a bit unstable.");
        TROLL_CLOSE_LINES.put("Loony", "Bye-bye, crazy!");
        TROLL_OPEN_LINES.put("Lame", "These trades? Totally lame.");
        TROLL_CLOSE_LINES.put("Lame", "Lame time's over.");
        TROLL_OPEN_LINES.put("Moron", "Even I know better prices.");
        TROLL_CLOSE_LINES.put("Moron", "Wow, you actually bought it.");
        TROLL_OPEN_LINES.put("Muppet", "Who's the puppet now?");
        TROLL_CLOSE_LINES.put("Muppet", "Snip snip, strings gone.");
        TROLL_OPEN_LINES.put("Nerd", "Let's calculate profit margins.");
        TROLL_CLOSE_LINES.put("Nerd", "That didn't compute.");
        TROLL_OPEN_LINES.put("Numbskull", "Trading with me? Bold.");
        TROLL_CLOSE_LINES.put("Numbskull", "Out of my sight.");
        TROLL_OPEN_LINES.put("Oaf", "Oops, did I break it?");
        TROLL_CLOSE_LINES.put("Oaf", "Time for a nap.");
        TROLL_OPEN_LINES.put("Oinky", "Snort! Buy something.");
        TROLL_CLOSE_LINES.put("Oinky", "Snort... bye.");
        TROLL_OPEN_LINES.put("Putz", "Move it or lose it.");
        TROLL_CLOSE_LINES.put("Putz", "Finally.");
        TROLL_OPEN_LINES.put("Peewee", "Don't underestimate me!");
        TROLL_CLOSE_LINES.put("Peewee", "Big talk, tiny exit.");
        TROLL_OPEN_LINES.put("Quirk", "I've got eccentric prices today.");
        TROLL_CLOSE_LINES.put("Quirk", "Back to my quirks.");
        TROLL_OPEN_LINES.put("Quack", "Quack! Buy quack things.");
        TROLL_CLOSE_LINES.put("Quack", "Quack off.");
        TROLL_OPEN_LINES.put("Rube", "City folk, huh?");
        TROLL_CLOSE_LINES.put("Rube", "Go on, git.");
        TROLL_OPEN_LINES.put("Rancor", "I loathe selling to you.");
        TROLL_CLOSE_LINES.put("Rancor", "Good riddance.");
        TROLL_OPEN_LINES.put("Silly", "Heehee, let's trade!");
        TROLL_CLOSE_LINES.put("Silly", "That was fun, kinda.");
        TROLL_OPEN_LINES.put("Slacker", "Ugh, work again?");
        TROLL_CLOSE_LINES.put("Slacker", "Nap time.");
        TROLL_OPEN_LINES.put("Twit", "Don't expect quality.");
        TROLL_CLOSE_LINES.put("Twit", "Shoo.");
        TROLL_OPEN_LINES.put("Twerp", "Make it quick, twerp.");
        TROLL_CLOSE_LINES.put("Twerp", "That's right, run along.");
        TROLL_OPEN_LINES.put("Uncool", "I'm too cool for this.");
        TROLL_CLOSE_LINES.put("Uncool", "Later, loser.");
        TROLL_OPEN_LINES.put("Upsy", "Oopsie daisy, buy something.");
        TROLL_CLOSE_LINES.put("Upsy", "Oops, bye!");
        TROLL_OPEN_LINES.put("Vapid", "So bored... want trades?");
        TROLL_CLOSE_LINES.put("Vapid", "Finally, some excitement gone.");
        TROLL_OPEN_LINES.put("Vulgar", "I've got crap to sell.");
        TROLL_CLOSE_LINES.put("Vulgar", "Get the hell out.");
        TROLL_OPEN_LINES.put("Wimp", "Please don't hurt me.");
        TROLL_CLOSE_LINES.put("Wimp", "Phew, safe again.");
        TROLL_OPEN_LINES.put("Wacky", "Wackiest deals in town!");
        TROLL_CLOSE_LINES.put("Wacky", "See ya, weirdo!");
        TROLL_OPEN_LINES.put("Xtra", "Everything here is extra.");
        TROLL_CLOSE_LINES.put("Xtra", "That was... extra.");
        TROLL_OPEN_LINES.put("Xerox", "Copy these deals if you can.");
        TROLL_CLOSE_LINES.put("Xerox", "Duplicate that exit.");
        TROLL_OPEN_LINES.put("Yucky", "Eww, customers.");
        TROLL_CLOSE_LINES.put("Yucky", "Gross, you're leaving.");
        TROLL_OPEN_LINES.put("Yodel", "Yodel-ay-hee-hoo! Buy!" );
        TROLL_CLOSE_LINES.put("Yodel", "Yodel-ay-bye.");
        TROLL_OPEN_LINES.put("Zany", "Things are about to get weird.");
        TROLL_CLOSE_LINES.put("Zany", "Normalcy returns... sadly.");
        TROLL_OPEN_LINES.put("Zipper", "Zip in, zip out, pay up.");
        TROLL_CLOSE_LINES.put("Zipper", "Zipped shut.");
    }

    public static String getOpenLine(String name, boolean isTroll) {
        if (isTroll) {
            return TROLL_OPEN_LINES.getOrDefault(name, "What do you want?");
        }
        return GENERIC_OPEN_LINES.get(new Random().nextInt(GENERIC_OPEN_LINES.size()));
    }

    public static String getCloseLine(String name, boolean isTroll) {
        if (isTroll) {
            return TROLL_CLOSE_LINES.getOrDefault(name, "Go away.");
        }
        return GENERIC_CLOSE_LINES.get(new Random().nextInt(GENERIC_CLOSE_LINES.size()));
    }
}
