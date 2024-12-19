package goat.minecraft.minecraftnew.subsystems.fishing;

import goat.minecraft.minecraftnew.utils.ItemRegistry;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class SeaCreatureRegistry implements Listener {
    private static final List<SeaCreature> SEA_CREATURES = new ArrayList<>();
    private static final Map<Rarity, Double> RARITY_WEIGHTS = new HashMap<>();
    private static final Map<Rarity, Integer> RARITY_LEVELS = new HashMap<>();
    private static final Random RANDOM = new Random();



    static {
        // Define weights for each rarity (sum should be 1.0)
        RARITY_WEIGHTS.put(Rarity.COMMON, 0.25);
        RARITY_WEIGHTS.put(Rarity.UNCOMMON, 0.25);
        RARITY_WEIGHTS.put(Rarity.RARE, 0.20);
        RARITY_WEIGHTS.put(Rarity.EPIC, 0.15);
        RARITY_WEIGHTS.put(Rarity.LEGENDARY, 0.15);

        // Assign levels based on rarity
        RARITY_LEVELS.put(Rarity.COMMON, 10);
        RARITY_LEVELS.put(Rarity.UNCOMMON, 20);
        RARITY_LEVELS.put(Rarity.RARE, 30);
        RARITY_LEVELS.put(Rarity.EPIC, 40);
        RARITY_LEVELS.put(Rarity.LEGENDARY, 50);



        //COMMON
        List<SeaCreature.DropItem> turtleDrops = new ArrayList<>();
        turtleDrops.add(new SeaCreature.DropItem(ItemRegistry.getShallowShell(), 1, 1, 1)); // Common drop for a cod fish
        SEA_CREATURES.add(new SeaCreature(
                "Shallow Turtle", // Name of the sea creature
                Rarity.COMMON, // Rarity of the sea creature
                EntityType.TURTLE, // Entity type for the cod fish
                turtleDrops, // Drops for the cod fish
                Color.fromRGB(162, 162, 162), // Light gray color to represent a cod fish
                "Fish", // Unique texture for this creature
                RARITY_LEVELS.get(Rarity.COMMON) // Rarity level for common creatures
        ));
        List<SeaCreature.DropItem> codFishDrops = new ArrayList<>();
        codFishDrops.add(new SeaCreature.DropItem(ItemRegistry.getFishBone(), 1, 1, 2)); // Common drop for a cod fish
        codFishDrops.add(new SeaCreature.DropItem(ItemRegistry.getSeaSalt(), 1, 1, 2)); // Common drop for a cod fish
        SEA_CREATURES.add(new SeaCreature(
                "Fish", // Name of the sea creature
                Rarity.COMMON, // Rarity of the sea creature
                EntityType.COD, // Entity type for the cod fish
                codFishDrops, // Drops for the cod fish
                Color.fromRGB(162, 162, 162), // Light gray color to represent a cod fish
                "Fish", // Unique texture for this creature
                RARITY_LEVELS.get(Rarity.COMMON) // Rarity level for common creatures
        ));
        List<SeaCreature.DropItem> salmonDrops = new ArrayList<>();
        salmonDrops.add(new SeaCreature.DropItem(ItemRegistry.getFishBone(), 1, 1, 2)); // Common drop for a salmon
        salmonDrops.add(new SeaCreature.DropItem(ItemRegistry.getSeaSalt(), 1, 1, 2)); // Common drop for a salmon
        SEA_CREATURES.add(new SeaCreature(
                "Salmon", // Name of the sea creature
                Rarity.COMMON, // Rarity of the sea creature
                EntityType.SALMON, // Entity type for the salmon
                salmonDrops, // Drops for the salmon
                Color.fromRGB(255, 105, 180), // Pink color to represent a salmon
                "Salmon", // Unique texture for this creature
                RARITY_LEVELS.get(Rarity.COMMON) // Rarity level for common creatures
        ));
        List<SeaCreature.DropItem> pufferfishDrops = new ArrayList<>();
        pufferfishDrops.add(new SeaCreature.DropItem(ItemRegistry.getFishBone(), 1, 1, 2)); // Common drop for a pufferfish
        pufferfishDrops.add(new SeaCreature.DropItem(ItemRegistry.getSeaSalt(), 1, 1, 2)); // Common drop for a pufferfish
        SEA_CREATURES.add(new SeaCreature(
                "Pufferfish", // Name of the sea creature
                Rarity.COMMON, // Rarity of the sea creature
                EntityType.PUFFERFISH, // Entity type for the pufferfish
                pufferfishDrops, // Drops for the pufferfish
                Color.fromRGB(255, 165, 0), // Orange color to represent a pufferfish
                "Pufferfish", // Unique texture for this creature
                RARITY_LEVELS.get(Rarity.COMMON) // Rarity level for common creatures
        ));
        List<SeaCreature.DropItem> tropicalFishDrops = new ArrayList<>();
        tropicalFishDrops.add(new SeaCreature.DropItem(ItemRegistry.getFishBone(), 1, 1, 2)); // Common drop for a tropical fish
        tropicalFishDrops.add(new SeaCreature.DropItem(ItemRegistry.getSeaSalt(), 1, 1, 2)); // Common drop for a tropical fish
        SEA_CREATURES.add(new SeaCreature(
                "Tropical Fish", // Name of the sea creature
                Rarity.COMMON, // Rarity of the sea creature
                EntityType.TROPICAL_FISH, // Entity type for the tropical fish
                tropicalFishDrops, // Drops for the tropical fish
                Color.fromRGB(255, 215, 0), // Gold color to represent a tropical fish
                "Tropical_Fish", // Unique texture for this creature
                RARITY_LEVELS.get(Rarity.COMMON) // Rarity level for common creatures
        ));

        //UNCOMMON
        List<SeaCreature.DropItem> turdleDrops = new ArrayList<>();
        turdleDrops.add(new SeaCreature.DropItem(ItemRegistry.getShell(), 1, 1, 1)); // Common drop for a cod fish
        SEA_CREATURES.add(new SeaCreature(
                "Sea Turtle", // Name of the sea creature
                Rarity.UNCOMMON, // Rarity of the sea creature
                EntityType.TURTLE, // Entity type for the cod fish
                turdleDrops, // Drops for the cod fish
                Color.fromRGB(162, 162, 162), // Light gray color to represent a cod fish
                "Fish", // Unique texture for this creature
                RARITY_LEVELS.get(Rarity.UNCOMMON) // Rarity level for common creatures
        ));
        List<SeaCreature.DropItem> luminescentDrownedDrops = new ArrayList<>();
        luminescentDrownedDrops.add(new SeaCreature.DropItem(ItemRegistry.getFishBone(), 1, 1, 2)); // Common drop for a luminescent drowned
        luminescentDrownedDrops.add(new SeaCreature.DropItem(ItemRegistry.getLuminescentInk(), 1, 1, 1)); // Luminescent Ink drop
        luminescentDrownedDrops.add(new SeaCreature.DropItem(ItemRegistry.getSeaSalt(), 1, 1, 1)); // Luminescent Ink drop
        luminescentDrownedDrops.add(new SeaCreature.DropItem(ItemRegistry.getAquaAffinity(), 1, 1, 10)); // Luminescent Ink drop
        SEA_CREATURES.add(new SeaCreature(
                "Luminescent Drowned", // Name of the sea creature
                Rarity.UNCOMMON, // Rarity of the sea creature
                EntityType.DROWNED, // Entity type for the luminescent drowned
                luminescentDrownedDrops, // Drops for the luminescent drowned
                Color.fromRGB(50, 150, 255), // Light blue color to represent a luminescent drowned
                "Luminescent_Drowned", // Unique texture for this creature
                RARITY_LEVELS.get(Rarity.UNCOMMON) // Rarity level for uncommon creatures
        ));
        List<SeaCreature.DropItem> squidDrops = new ArrayList<>();
        squidDrops.add(new SeaCreature.DropItem(ItemRegistry.getSeaSalt(), 1, 1, 1)); // Luminescent Ink drop
        squidDrops.add(new SeaCreature.DropItem(ItemRegistry.getCalamari(), 1, 1, 4)); // Luminescent Ink drop
        SEA_CREATURES.add(new SeaCreature(
                "Squid", // Name of the sea creature
                Rarity.UNCOMMON, // Rarity of the sea creature
                EntityType.SQUID, // Entity type for the luminescent drowned
                squidDrops, // Drops for the luminescent drowned
                Color.fromRGB(50, 150, 255), // Light blue color to represent a luminescent drowned
                "Luminescent_Drowned", // Unique texture for this creature
                RARITY_LEVELS.get(Rarity.UNCOMMON) // Rarity level for uncommon creatures
        ));

        //RARE
        List<SeaCreature.DropItem> deepTurtleDrops = new ArrayList<>();
        deepTurtleDrops.add(new SeaCreature.DropItem(ItemRegistry.getDeepShell(), 1, 1, 1)); // Common drop for a cod fish
        deepTurtleDrops.add(new SeaCreature.DropItem(ItemRegistry.getRainArtifact(), 1, 1, 1)); // Common drop for a cod fish
        SEA_CREATURES.add(new SeaCreature(
                "Deep Sea Turtle", // Name of the sea creature
                Rarity.RARE, // Rarity of the sea creature
                EntityType.TURTLE, // Entity type for the cod fish
                deepTurtleDrops, // Drops for the cod fish
                Color.fromRGB(162, 162, 162), // Light gray color to represent a cod fish
                "Fish", // Unique texture for this creature
                RARITY_LEVELS.get(Rarity.RARE) // Rarity level for common creatures
        ));
        List<SeaCreature.DropItem> poseidonDrops = new ArrayList<>();
        poseidonDrops.add(new SeaCreature.DropItem(ItemRegistry.getSeaSalt(), 3, 1, 5));
        poseidonDrops.add(new SeaCreature.DropItem(ItemRegistry.getTrident(), 1, 1, 1));
        poseidonDrops.add(new SeaCreature.DropItem(ItemRegistry.getRiptide(), 1, 1, 4));
        poseidonDrops.add(new SeaCreature.DropItem(ItemRegistry.getChanneling(), 1, 1, 4));
        SEA_CREATURES.add(new SeaCreature(
                "Poseidon",
                Rarity.RARE,
                EntityType.DROWNED,
                poseidonDrops,
                Color.fromRGB(0, 0, 255),
                "Poseidon",  // unique texture for this creature
                RARITY_LEVELS.get(Rarity.RARE)
        ));
        List<SeaCreature.DropItem> sharkDrops = new ArrayList<>();
        sharkDrops.add(new SeaCreature.DropItem(ItemRegistry.getTooth(), 3, 1, 5));
        sharkDrops.add(new SeaCreature.DropItem(ItemRegistry.getTooth(), 3, 1, 5));
        sharkDrops.add(new SeaCreature.DropItem(ItemRegistry.getSeaSalt(), 3, 1, 5));
        SEA_CREATURES.add(new SeaCreature(
                "Shark",
                Rarity.RARE,
                EntityType.ZOMBIE,
                sharkDrops,
                Color.fromRGB(47, 47, 47),
                "Shark",  // unique texture for this creature
                RARITY_LEVELS.get(Rarity.RARE)
        ));
        List<SeaCreature.DropItem> pirateDrops = new ArrayList<>();
        pirateDrops.add(new SeaCreature.DropItem(ItemRegistry.getTooth(), 3, 1, 5));
        pirateDrops.add(new SeaCreature.DropItem(ItemRegistry.getTooth(), 3, 1, 5));
        pirateDrops.add(new SeaCreature.DropItem(ItemRegistry.getSeaSalt(), 3, 1, 5));
        pirateDrops.add(new SeaCreature.DropItem(ItemRegistry.getImpaling(), 1, 1, 4));
        SEA_CREATURES.add(new SeaCreature(
                "Pirate",
                Rarity.RARE,
                EntityType.SKELETON,
                null,
                Color.fromRGB(0, 0, 0),
                "Pirate",  // unique texture for this creature
                RARITY_LEVELS.get(Rarity.RARE)
        ));




        //EPIC
        List<SeaCreature.DropItem> abyssalTurtleDrops = new ArrayList<>();
        abyssalTurtleDrops.add(new SeaCreature.DropItem(ItemRegistry.getAbyssalShell(), 1, 1, 1)); // Common drop for a cod fish
        SEA_CREATURES.add(new SeaCreature(
                "Abyssal Sea Turtle", // Name of the sea creature
                Rarity.EPIC, // Rarity of the sea creature
                EntityType.TURTLE, // Entity type for the cod fish
                abyssalTurtleDrops, // Drops for the cod fish
                Color.fromRGB(162, 162, 162), // Light gray color to represent a cod fish
                "Fish", // Unique texture for this creature
                RARITY_LEVELS.get(Rarity.EPIC) // Rarity level for common creatures
        ));
        List<SeaCreature.DropItem> waterSpiderDrops = new ArrayList<>();
        waterSpiderDrops.add(new SeaCreature.DropItem(ItemRegistry.getFishBone(), 7, 1, 5));
        waterSpiderDrops.add(new SeaCreature.DropItem(ItemRegistry.getTooth(), 7, 1, 4));
        waterSpiderDrops.add(new SeaCreature.DropItem(ItemRegistry.getAbyssalVenom(), 1, 1, 1));
        SEA_CREATURES.add(new SeaCreature(
                "Water Spider",
                Rarity.EPIC,
                EntityType.CAVE_SPIDER,
                waterSpiderDrops,
                Color.fromRGB(0, 51, 102),
                "waterspider",  // unique texture for this creature
                RARITY_LEVELS.get(Rarity.EPIC)
        ));
        List<SeaCreature.DropItem> greatWhiteSharkDrops = new ArrayList<>();
        greatWhiteSharkDrops.add(new SeaCreature.DropItem(ItemRegistry.getFishBone(), 7, 1, 5));
        greatWhiteSharkDrops.add(new SeaCreature.DropItem(ItemRegistry.getTooth(), 7, 1, 4));
        greatWhiteSharkDrops.add(new SeaCreature.DropItem(ItemRegistry.getRespiration(), 1, 1, 4));
        SEA_CREATURES.add(new SeaCreature(
                "Great White Shark",
                Rarity.EPIC,
                EntityType.ZOMBIE,
                greatWhiteSharkDrops,
                Color.fromRGB(0, 51, 102),
                "Great_White_Shark",  // unique texture for this creature
                RARITY_LEVELS.get(Rarity.EPIC)
        ));
        List<SeaCreature.DropItem> leviathanDrops = new ArrayList<>();
        leviathanDrops.add(new SeaCreature.DropItem(ItemRegistry.getTooth(), 15, 1, 4));
        leviathanDrops.add(new SeaCreature.DropItem(ItemRegistry.getFishBone(), 15, 1, 4));
        leviathanDrops.add(new SeaCreature.DropItem(ItemRegistry.getLeviathanHeart(), 1, 1, 4)); // Add Leviathan heart drop
        leviathanDrops.add(new SeaCreature.DropItem(ItemRegistry.getSweepingEdge(), 1, 1, 4)); // Add Leviathan heart drop
        SEA_CREATURES.add(new SeaCreature(
                "Leviathan", // Changed name to Leviathan
                Rarity.EPIC,
                EntityType.ZOMBIE, // You can change this to a more appropriate entity type if needed
                leviathanDrops,
                Color.fromRGB(0, 100, 100), // Dark aqua color
                "Leviathan", // Unique texture for this creature
                RARITY_LEVELS.get(Rarity.EPIC)
        ));
        List<SeaCreature.DropItem> yetiDrops = new ArrayList<>();
        yetiDrops.add(new SeaCreature.DropItem(ItemRegistry.getTooth(), 10, 1, 3)); // Example drop
        yetiDrops.add(new SeaCreature.DropItem(ItemRegistry.getFishBone(), 10, 1, 3)); // Example drop
        yetiDrops.add(new SeaCreature.DropItem(ItemRegistry.getInfernalSharpness(), 1, 1, 10)); // Yeti Fur drop
        SEA_CREATURES.add(new SeaCreature(
                "Yeti", // Name of the sea creature
                Rarity.EPIC, // Rarity of the sea creature
                EntityType.ZOMBIE, // Entity type for the Yeti (you can change this to a more appropriate entity type if needed)
                yetiDrops, // Drops for the Yeti
                Color.fromRGB(255, 255, 255), // White color to represent a Yeti
                "Yeti", // Unique texture for this creature
                RARITY_LEVELS.get(Rarity.EPIC) // Rarity level for epic creatures
        ));






        //LEGENDARY
        List<SeaCreature.DropItem> bioluminescentGuardianDrops = new ArrayList<>();
        bioluminescentGuardianDrops.add(new SeaCreature.DropItem(ItemRegistry.getAbyssalInk(), 1, 1, 1));
        bioluminescentGuardianDrops.add(new SeaCreature.DropItem(ItemRegistry.getLuminescentInk(), 1, 1, 1));
        SEA_CREATURES.add(new SeaCreature(
                "Bioluminescent Guardian",
                Rarity.LEGENDARY,
                EntityType.IRON_GOLEM,
                bioluminescentGuardianDrops,
                Color.fromRGB(211, 211, 211),
                "Megalodon",  // unique texture for this creature
                RARITY_LEVELS.get(Rarity.LEGENDARY)
        ));
        List<SeaCreature.DropItem> megalodonDrops = new ArrayList<>();
        megalodonDrops.add(new SeaCreature.DropItem(ItemRegistry.getTooth(), 31, 1, 4));
        megalodonDrops.add(new SeaCreature.DropItem(ItemRegistry.getFishBone(), 31, 1, 4));
        megalodonDrops.add(new SeaCreature.DropItem(ItemRegistry.getInfernalLure(), 1, 1, 4));
        SEA_CREATURES.add(new SeaCreature(
                "Megalodon",
                Rarity.LEGENDARY,
                EntityType.ZOMBIE,
                megalodonDrops,
                Color.fromRGB(211, 211, 211),
                "Megalodon",  // unique texture for this creature
                RARITY_LEVELS.get(Rarity.LEGENDARY)
        ));
        List<SeaCreature.DropItem> abominationDrops = new ArrayList<>();
        abominationDrops.add(new SeaCreature.DropItem(ItemRegistry.getTooth(), 31, 1, 4));
        abominationDrops.add(new SeaCreature.DropItem(ItemRegistry.getFishBone(), 31, 1, 4));
        abominationDrops.add(new SeaCreature.DropItem(ItemRegistry.getInfernalUnbreaking(), 1, 1, 1));
        abominationDrops.add(new SeaCreature.DropItem(ItemRegistry.getSwiftSneak(), 1, 1, 4));
        SEA_CREATURES.add(new SeaCreature(
                "Abomination",
                Rarity.LEGENDARY,
                EntityType.ZOMBIE,
                abominationDrops,
                Color.fromRGB(250, 10, 20),
                "Abomination",  // unique texture for this creature
                RARITY_LEVELS.get(Rarity.LEGENDARY)
        ));
        //MYTHIC

    }

    /**
     * Creates a placeholder alchemy item with the given name.
     *
     * @param name The name of the alchemy item.
     * @return An ItemStack representing the alchemy item.
     */
    public static ItemStack createAlchemyItem(String name, Material material, List<String> lore) {
        ItemStack item = new ItemStack(material); // Changed to GREEN_DYE as placeholder
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.LIGHT_PURPLE + name);
        // Optionally, add lore or other metadata
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    /**
     * Retrieves all registered sea creatures.
     *
     * @return An unmodifiable list of all sea creatures.
     */
    public static List<SeaCreature> getSeaCreatures() {
        return Collections.unmodifiableList(SEA_CREATURES);
    }

    /**
     * Retrieves a sea creature by its display name.
     *
     * @param name The display name of the sea creature.
     * @return An Optional containing the sea creature if found.
     */
    public static Optional<SeaCreature> getSeaCreatureByName(String name) {
        return SEA_CREATURES.stream()
                .filter(creature -> creature.getDisplayName().equalsIgnoreCase(name))
                .findFirst();
    }

    /**
     * Retrieves a random sea creature from the registry based on rarity.
     *
     * @return An Optional containing a random sea creature if available.
     */
    public static Optional<SeaCreature> getRandomSeaCreature() {
        if (SEA_CREATURES.isEmpty()) return Optional.empty();

        // Step 1: Select a rarity based on weights
        double rand = RANDOM.nextDouble();
        double cumulative = 0.0;
        Rarity selectedRarity = Rarity.COMMON; // Default

        for (Map.Entry<Rarity, Double> entry : RARITY_WEIGHTS.entrySet()) {
            cumulative += entry.getValue();
            if (rand <= cumulative) {
                selectedRarity = entry.getKey();
                break;
            }
        }

        // Step 2: Filter creatures by selected rarity
        List<SeaCreature> filtered = new ArrayList<>();
        for (SeaCreature creature : SEA_CREATURES) {
            if (creature.getRarity() == selectedRarity) {
                filtered.add(creature);
            }
        }

        // Step 3: Select a random creature from the filtered list
        if (filtered.isEmpty()) return Optional.empty();

        SeaCreature selectedCreature = filtered.get(RANDOM.nextInt(filtered.size()));
        return Optional.of(selectedCreature);
    }

    /**
     * Applies a full set of dyed armor and a player head to the sea creature.
     *
     * @param creature The sea creature entity to which armor and head will be applied.
     */

    /**
     * Creates a dyed leather armor piece with the specified color.
     *
     * @param material The type of leather armor.
     * @param color    The color to dye the armor.
     * @return The dyed leather armor ItemStack.
     */
    static ItemStack createDyedLeatherArmor(Material material, Color color) {
        ItemStack armor = new ItemStack(material);
        LeatherArmorMeta meta = (LeatherArmorMeta) armor.getItemMeta();
        meta.setColor(color);
        armor.setItemMeta(meta);
        return armor;
    }

    /**
     * Creates a player head with the specified display name.
     *
     * @param displayName The display name for the head.
     * @return The player head ItemStack.
     */
}
