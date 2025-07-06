package goat.minecraft.minecraftnew.subsystems.pets;

import goat.minecraft.minecraftnew.MinecraftNew;
import org.bukkit.ChatColor;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import java.util.Map;
import java.util.HashMap;
import java.util.Arrays;
import java.util.List;

/**
 * A registry of pet definitions. Each definition holds the default
 * properties used to create a new pet instance.
 */
public class PetRegistry {

    /**
     * Inner class representing the properties for a pet type.
     */
    public static class PetDefinition {
        private final String name;
        private final PetManager.Rarity rarity;
        private final int maxLevel;
        private final Particle particle;
        private final List<PetManager.PetPerk> perks;

        public PetDefinition(String name, PetManager.Rarity rarity, int maxLevel, Particle particle, List<PetManager.PetPerk> perks) {
            this.name = name;
            this.rarity = rarity;
            this.maxLevel = maxLevel;
            this.particle = particle;
            this.perks = perks;
        }

        public String getName() {
            return name;
        }

        public PetManager.Rarity getRarity() {
            return rarity;
        }

        public int getMaxLevel() {
            return maxLevel;
        }

        public Particle getParticle() {
            return particle;
        }

        public List<PetManager.PetPerk> getPerks() {
            return perks;
        }
    }

    // Registry mapping pet names to their definitions.
    private static final Map<String, PetDefinition> registry = new HashMap<>();

    static {
        // Economical Pets
        registry.put("Villager", new PetDefinition(
                "Villager",
                PetManager.Rarity.LEGENDARY,
                100,
                Particle.VILLAGER_ANGRY,
                Arrays.asList(PetManager.PetPerk.HAGGLE, PetManager.PetPerk.PRACTICE, PetManager.PetPerk.SPEED_BOOST)
        ));
        registry.put("Golden Steve", new PetDefinition(
                "Golden Steve",
                PetManager.Rarity.LEGENDARY,
                100,
                Particle.VILLAGER_ANGRY,
                Arrays.asList(PetManager.PetPerk.TREASURE_HUNTER, PetManager.PetPerk.COMFORTABLE)
        ));

        // Fishing Pets
        registry.put("Leviathan", new PetDefinition(
                "Leviathan",
                PetManager.Rarity.LEGENDARY,
                100,
                Particle.VILLAGER_ANGRY,
                Arrays.asList(PetManager.PetPerk.ANGLER, PetManager.PetPerk.HEART_OF_THE_SEA, PetManager.PetPerk.TERROR_OF_THE_DEEP, PetManager.PetPerk.ELITE, PetManager.PetPerk.STRONG_SWIMMER, PetManager.PetPerk.COLLECTOR)
        ));
        registry.put("Turtle", new PetDefinition(
                "Turtle",
                PetManager.Rarity.EPIC,
                100,
                Particle.CRIMSON_SPORE,
                Arrays.asList(PetManager.PetPerk.HEART_OF_THE_SEA, PetManager.PetPerk.BONE_PLATING, PetManager.PetPerk.COMFORTABLE, PetManager.PetPerk.BUDDY_SYSTEM)
        ));
        registry.put("Dolphin", new PetDefinition(
                "Dolphin",
                PetManager.Rarity.RARE,
                100,
                Particle.WATER_SPLASH,
                Arrays.asList(PetManager.PetPerk.STRONG_SWIMMER, PetManager.PetPerk.ANGLER)
        ));
        registry.put("Glow Squid", new PetDefinition(
                "Glow Squid",
                PetManager.Rarity.UNCOMMON,
                100,
                Particle.GLOW_SQUID_INK,
                Arrays.asList(PetManager.PetPerk.SPEED_BOOST, PetManager.PetPerk.ANGLER)
        ));
        registry.put("Fish", new PetDefinition(
                "Fish",
                PetManager.Rarity.COMMON,
                100,
                Particle.GLOW_SQUID_INK,
                Arrays.asList(PetManager.PetPerk.ANGLER)
        ));

        // Combat Pets
        registry.put("Stray", new PetDefinition(
                "Stray",
                PetManager.Rarity.LEGENDARY,
                100,
                Particle.WHITE_ASH,
                Arrays.asList(PetManager.PetPerk.SHOTCALLING, PetManager.PetPerk.RECOVERY, PetManager.PetPerk.TIPPED_SLOWNESS, PetManager.PetPerk.BONE_COLD, PetManager.PetPerk.QUICK_DRAW)
        ));
        registry.put("Guardian", new PetDefinition(
                "Guardian",
                PetManager.Rarity.EPIC,
                100,
                Particle.WHITE_ASH,
                Arrays.asList(PetManager.PetPerk.SHOTCALLING, PetManager.PetPerk.RECOVERY, PetManager.PetPerk.LASER_BEAM)
        ));

        registry.put("Skeleton", new PetDefinition(
                "Skeleton",
                PetManager.Rarity.UNCOMMON,
                100,
                Particle.WHITE_ASH,
                Arrays.asList(PetManager.PetPerk.SHOTCALLING, PetManager.PetPerk.BONE_PLATING_WEAK)
        ));

        registry.put("Zombie Pigman", new PetDefinition(
                "Zombie Pigman",
                PetManager.Rarity.LEGENDARY,
                100,
                Particle.ASH,
                Arrays.asList(PetManager.PetPerk.BLACKLUNG, PetManager.PetPerk.SPEED_BOOST, PetManager.PetPerk.SECRET_LEGION, PetManager.PetPerk.DEVOUR, PetManager.PetPerk.FIREPROOF)
        ));
        registry.put("Enderman", new PetDefinition(
                "Enderman",
                PetManager.Rarity.LEGENDARY,
                100,
                Particle.ASH,
                Arrays.asList(PetManager.PetPerk.ELITE, PetManager.PetPerk.ENDLESS_WARP, PetManager.PetPerk.COLLECTOR)
        ));
        registry.put("Blaze", new PetDefinition(
                "Blaze",
                PetManager.Rarity.LEGENDARY,
                100,
                Particle.ASH,
                Arrays.asList(PetManager.PetPerk.BLACKLUNG, PetManager.PetPerk.FIREPROOF, PetManager.PetPerk.FLIGHT)
        ));
        registry.put("Wither Skeleton", new PetDefinition(
                "Wither Skeleton",
                PetManager.Rarity.LEGENDARY,
                100,
                Particle.ASH,
                Arrays.asList(PetManager.PetPerk.BLACKLUNG, PetManager.PetPerk.SPEED_BOOST, PetManager.PetPerk.DEVOUR, PetManager.PetPerk.FIREPROOF, PetManager.PetPerk.DECAY)
        ));
        registry.put("Cat", new PetDefinition(
                "Cat",
                PetManager.Rarity.LEGENDARY,
                100,
                Particle.ASH,
                Arrays.asList(PetManager.PetPerk.CLAW, PetManager.PetPerk.SOFT_PAW, PetManager.PetPerk.SPEED_BOOST, PetManager.PetPerk.LEAP)
        ));
        registry.put("Wolf", new PetDefinition(
                "Wolf",
                PetManager.Rarity.RARE,
                100,
                Particle.CRIT,
                Arrays.asList(PetManager.PetPerk.ALPHA, PetManager.PetPerk.FETCH, PetManager.PetPerk.CLAW, PetManager.PetPerk.DEVOUR)
        ));
        registry.put("Yeti", new PetDefinition(
                "Yeti",
                PetManager.Rarity.EPIC,
                100,
                Particle.CRIT_MAGIC,
                Arrays.asList(PetManager.PetPerk.NO_HIBERNATION, PetManager.PetPerk.ASPECT_OF_THE_FROST, PetManager.PetPerk.BLIZZARD, PetManager.PetPerk.SPEED_BOOST, PetManager.PetPerk.BONE_COLD, PetManager.PetPerk.ELITE)
        ));
        registry.put("Axolotl", new PetDefinition(
                "Axolotl",
                PetManager.Rarity.EPIC,
                100,
                Particle.SPELL_WITCH,
                Arrays.asList(PetManager.PetPerk.DEVOUR, PetManager.PetPerk.SECOND_WIND, PetManager.PetPerk.REBIRTH)
        ));
        registry.put("Zombie", new PetDefinition(
                "Zombie",
                PetManager.Rarity.RARE,
                100,
                Particle.CRIT_MAGIC,
                Arrays.asList(PetManager.PetPerk.SECOND_WIND, PetManager.PetPerk.DEVOUR, PetManager.PetPerk.ECHOLOCATION)
        ));
        registry.put("Iron Golem", new PetDefinition(
                "Iron Golem",
                PetManager.Rarity.RARE,
                100,
                Particle.ASH,
                Arrays.asList(PetManager.PetPerk.WALKING_FORTRESS, PetManager.PetPerk.ELITE)
        ));

        // Mining Pets
        registry.put("Bat", new PetDefinition(
                "Bat",
                PetManager.Rarity.RARE,
                100,
                Particle.DAMAGE_INDICATOR,
                Arrays.asList(PetManager.PetPerk.ECHOLOCATION)
        ));
        registry.put("Warden", new PetDefinition(
                "Warden",
                PetManager.Rarity.LEGENDARY,
                100,
                Particle.WARPED_SPORE,
                Arrays.asList(PetManager.PetPerk.DIGGING_CLAWS, PetManager.PetPerk.ECHOLOCATION, PetManager.PetPerk.LASER_BEAM, PetManager.PetPerk.BONE_PLATING, PetManager.PetPerk.ROCK_EATER)
        ));
        registry.put("Dwarf", new PetDefinition(
                "Dwarf",
                PetManager.Rarity.EPIC,
                100,
                Particle.DAMAGE_INDICATOR,
                Arrays.asList(PetManager.PetPerk.DIGGING_CLAWS, PetManager.PetPerk.MITHRIL_MINER, PetManager.PetPerk.EMERALD_SEEKER)
        ));
        registry.put("Armadillo", new PetDefinition(
                "Armadillo",
                PetManager.Rarity.RARE,
                100,
                Particle.DAMAGE_INDICATOR,
                Arrays.asList(PetManager.PetPerk.BONE_PLATING, PetManager.PetPerk.DIGGING_CLAWS)
        ));
        registry.put("Drowned", new PetDefinition(
                "Drowned",
                PetManager.Rarity.EPIC,
                100,
                Particle.DAMAGE_INDICATOR,
                Arrays.asList(PetManager.PetPerk.WATERLOGGED, PetManager.PetPerk.STRONG_SWIMMER, PetManager.PetPerk.DEVOUR)
        ));

        // Movement Pets
        registry.put("Parrot", new PetDefinition(
                "Parrot",
                PetManager.Rarity.LEGENDARY,
                100,
                Particle.TOTEM,
                Arrays.asList(PetManager.PetPerk.FLIGHT, PetManager.PetPerk.LULLABY)
        ));
        registry.put("Allay", new PetDefinition(
                "Allay",
                PetManager.Rarity.EPIC,
                100,
                Particle.END_ROD,
                Arrays.asList(PetManager.PetPerk.COLLECTOR, PetManager.PetPerk.FLIGHT)
        ));
        registry.put("Horse", new PetDefinition(
                "Horse",
                PetManager.Rarity.COMMON,
                10,
                Particle.HEART,
                Arrays.asList(PetManager.PetPerk.SPEED_BOOST)
        ));

        // Forestry Pets
        registry.put("Piglin Brute", new PetDefinition(
                "Piglin Brute",
                PetManager.Rarity.LEGENDARY,
                100,
                Particle.FIREWORKS_SPARK,
                Arrays.asList(PetManager.PetPerk.SPEED_BOOST, PetManager.PetPerk.CHALLENGE, PetManager.PetPerk.BLACKLUNG, PetManager.PetPerk.SECOND_WIND, PetManager.PetPerk.ELITE)
        ));
        registry.put("Vindicator", new PetDefinition(
                "Vindicator",
                PetManager.Rarity.LEGENDARY,
                100,
                Particle.FIREWORKS_SPARK,
                Arrays.asList(PetManager.PetPerk.SPEED_BOOST, PetManager.PetPerk.SKEPTICISM, PetManager.PetPerk.GREED, PetManager.PetPerk.ELITE)
        ));
        registry.put("Ent", new PetDefinition(
                "Ent",
                PetManager.Rarity.LEGENDARY,
                100,
                Particle.END_ROD,
                Arrays.asList(PetManager.PetPerk.SPEED_BOOST, PetManager.PetPerk.DIGGING_CLAWS, PetManager.PetPerk.COLLECTOR, PetManager.PetPerk.GROOT)
        ));
        registry.put("Monkey", new PetDefinition(
                "Monkey",
                PetManager.Rarity.EPIC,
                100,
                Particle.VILLAGER_HAPPY,
                Arrays.asList(PetManager.PetPerk.SPEED_BOOST, PetManager.PetPerk.DIGGING_CLAWS, PetManager.PetPerk.COLLECTOR)
        ));
        registry.put("Raccoon", new PetDefinition(
                "Raccoon",
                PetManager.Rarity.RARE,
                100,
                Particle.VILLAGER_HAPPY,
                Arrays.asList(PetManager.PetPerk.SPEED_BOOST, PetManager.PetPerk.DIGGING_CLAWS)
        ));
        //Building
        registry.put("Spider", new PetDefinition(
                "Spider",
                PetManager.Rarity.LEGENDARY,
                100,
                Particle.SQUID_INK,
                Arrays.asList(PetManager.PetPerk.LULLABY, PetManager.PetPerk.SPIDER_STEVE, PetManager.PetPerk.OBSESSION, PetManager.PetPerk.SOFT_PAW, PetManager.PetPerk.ECHOLOCATION, PetManager.PetPerk.EARTHWORM)
        ));

        // Farming Pets
        registry.put("Pig", new PetDefinition(
                "Pig",
                PetManager.Rarity.LEGENDARY,
                100,
                Particle.FIREWORKS_SPARK,
                Arrays.asList(PetManager.PetPerk.SPEED_BOOST, PetManager.PetPerk.GREEN_THUMB, PetManager.PetPerk.CULTIVATION, PetManager.PetPerk.SUPERIOR_ENDURANCE, PetManager.PetPerk.COLLECTOR, PetManager.PetPerk.COMPOSTER)
        ));
        registry.put("Mooshroom", new PetDefinition(
                "Mooshroom",
                PetManager.Rarity.EPIC,
                100,
                Particle.FIREWORKS_SPARK,
                Arrays.asList(PetManager.PetPerk.SPEED_BOOST, PetManager.PetPerk.ANTIDOTE, PetManager.PetPerk.GREEN_THUMB, PetManager.PetPerk.CULTIVATION, PetManager.PetPerk.COLLECTOR)
        ));
        registry.put("Cow", new PetDefinition(
                "Cow",
                PetManager.Rarity.RARE,
                100,
                Particle.FIREWORKS_SPARK,
                Arrays.asList(PetManager.PetPerk.SPEED_BOOST, PetManager.PetPerk.GREEN_THUMB, PetManager.PetPerk.ANTIDOTE, PetManager.PetPerk.COLLECTOR)
        ));
        registry.put("Sheep", new PetDefinition(
                "Sheep",
                PetManager.Rarity.UNCOMMON,
                100,
                Particle.FIREWORKS_SPARK,
                Arrays.asList(PetManager.PetPerk.SPEED_BOOST, PetManager.PetPerk.GREEN_THUMB)
        ));
        registry.put("Squirrel", new PetDefinition(
                "Squirrel",
                PetManager.Rarity.COMMON,
                100,
                Particle.FIREWORKS_SPARK,
                Arrays.asList(PetManager.PetPerk.GREEN_THUMB)
        ));
        registry.put("Wither", new PetDefinition(
                "Wither",
                PetManager.Rarity.EPIC,
                100,
                Particle.ASH,
                Arrays.asList(PetManager.PetPerk.SHOTCALLING, PetManager.PetPerk.FLIGHT, PetManager.PetPerk.SPEED_BOOST, PetManager.PetPerk.DEVOUR, PetManager.PetPerk.ELITE, PetManager.PetPerk.DECAY)
        ));
        registry.put("Phoenix", new PetDefinition(
                "Phoenix",
                PetManager.Rarity.LEGENDARY,
                100,
                Particle.FLAME,
                Arrays.asList(PetManager.PetPerk.PHOENIX_REBIRTH, PetManager.PetPerk.FLAME_TRAIL, PetManager.PetPerk.FIREPROOF, PetManager.PetPerk.FLIGHT, PetManager.PetPerk.ELITE)
        ));
        registry.put("Witch", new PetDefinition(
                "Witch",
                PetManager.Rarity.EPIC,
                100,
                Particle.SPELL_WITCH,
                Arrays.asList(PetManager.PetPerk.SPEED_BOOST, PetManager.PetPerk.ANTIDOTE, PetManager.PetPerk.BROOMSTICK, PetManager.PetPerk.SPLASH_POTION, PetManager.PetPerk.EXPERIMENTATION)
        ));
        registry.put("Ghost", new PetDefinition(
                "Ghost",
                PetManager.Rarity.ADMIN,
                1,
                Particle.SOUL,
                Arrays.asList(PetManager.PetPerk.SPECTRAL)
        ));
    }
    // Inside PetManager class
    public void addPetByName(Player player, String petName) {
        // Retrieve a new pet instance from the registry.
        PetManager petManager = PetManager.getInstance(MinecraftNew.getInstance());
        PetManager.Pet pet = PetRegistry.getPetByName(petName, PetManager.getInstance(MinecraftNew.getInstance()));

        // Check if the pet name was valid.
        if (pet == null) {
            player.sendMessage(ChatColor.RED + "Invalid pet name: " + petName);
            return;
        }

        // Use the existing addPet method to add the pet to the player's collection
        // .

        petManager.addPet(player, pet);
    }

    /**
     * Returns a new pet instance based on the given pet name.
     * If no pet is registered under the name, returns null.
     *
     * @param name       The pet name (case-sensitive, as registered).
     * @param petManager An instance of PetManager used to create the pet.
     * @return A new PetManager.Pet instance or null if not found.
     */
    public static PetManager.Pet getPetByName(String name, PetManager petManager) {
        PetDefinition def = registry.get(name);
        if (def == null) {
            return null;
        }
        // Use PetManager's getSkullForPet to retrieve the proper icon.
        ItemStack icon = petManager.getSkullForPet(def.getName());
        // Create a new instance of the pet using PetManager's inner Pet class.
        return petManager.new Pet(def.getName(), def.getRarity(), def.getMaxLevel(), icon,
                def.getParticle(), def.getPerks(), PetTrait.HEALTHY, TraitRarity.COMMON);
    }
}
