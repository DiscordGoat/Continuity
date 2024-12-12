package goat.minecraft.minecraftnew.subsystems.fishing;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import goat.minecraft.minecraftnew.subsystems.utils.CustomItemManager;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.lang.reflect.Field;
import java.util.*;

import static goat.minecraft.minecraftnew.subsystems.utils.CustomItemManager.createCustomItem;

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
        RARITY_WEIGHTS.put(Rarity.EPIC, 0.10);
        RARITY_WEIGHTS.put(Rarity.LEGENDARY, 0.10);
        RARITY_WEIGHTS.put(Rarity.MYTHIC, 0.10);

        // Assign levels based on rarity
        RARITY_LEVELS.put(Rarity.COMMON, 20);
        RARITY_LEVELS.put(Rarity.UNCOMMON, 40);
        RARITY_LEVELS.put(Rarity.RARE, 60);
        RARITY_LEVELS.put(Rarity.EPIC, 80);
        RARITY_LEVELS.put(Rarity.LEGENDARY, 100);

        // Define alchemy items with detailed lore
        ItemStack fishBone = createAlchemyItem("Fish Bone", Material.BONE, List.of(
                ChatColor.GRAY + "A bone from a fish.",
                ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Turns potions into splash potions.",
                ChatColor.DARK_PURPLE + "Brewing Modifier"
        ));
        ItemStack fishOil = createAlchemyItem("Fish Oil", Material.POTION, List.of(
                ChatColor.GRAY + "A bottle of rich fish oil.",
                ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Ghast Tear substitute",
                ChatColor.GRAY + "",
                ChatColor.DARK_PURPLE + "Brewing Ingredient"
        ));
        ItemStack shallowShell = createAlchemyItem("Shallow Shell", Material.SCUTE, List.of(
                ChatColor.GRAY + "A shell found in shallow waters.",
                ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Repairs equipment slightly.",
                ChatColor.GRAY + "Restores 100 durability.",
                ChatColor.DARK_PURPLE + "Smithing Ingredient"
        ));
        ItemStack shallowInk = createAlchemyItem("Shallow Ink", Material.INK_SAC, List.of(
                ChatColor.GRAY + "A handful of shallow ink.",
                ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Glowstone Dust substitute.",
                ChatColor.GRAY + "Adds I Potency",
                ChatColor.DARK_PURPLE + "Brewing Modifier"
        ));
        ItemStack shallowVenom = createAlchemyItem("Shallow Venom", Material.LIME_DYE, List.of(
                ChatColor.GRAY + "Venom from shallow creatures.",
                ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Redstone Dust substitute",
                ChatColor.DARK_PURPLE + "Brewing Modifier"
        ));
        ItemStack shell = createAlchemyItem("Shell", Material.CYAN_DYE, List.of(
                ChatColor.GRAY + "A sturdy shell.",
                ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Repairs equipment moderately.",
                ChatColor.GRAY + "Restores 200 durability.",
                ChatColor.DARK_PURPLE + "Smithing Ingredient"
        ));

        ItemStack luminescentInk = createAlchemyItem("Luminescent Ink", Material.GLOW_INK_SAC, List.of(
                ChatColor.GRAY + "A glowing ink.",
                ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Brewing ingredient for Glowing.",
                ChatColor.GRAY + "Adds Glowing I",
                ChatColor.DARK_PURPLE + "Brewing Ingredient"
        ));

        ItemStack leviathanHeart = createAlchemyItem("Leviathan Heart", Material.RED_DYE, List.of(
                ChatColor.GRAY + "The beating heart of a mighty creature.",
                ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Artifact for health.",
                ChatColor.GRAY + "Adds Regeneration 8 for 180 seconds.",
                ChatColor.DARK_PURPLE + "Artifact"
        ));

        ItemStack deepVenom = createAlchemyItem("Deep Venom", Material.GREEN_DYE, List.of(
                ChatColor.GRAY + "A potent venom from deep waters.",
                ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Redstone Block substitute.",
                ChatColor.GRAY + "",
                ChatColor.DARK_PURPLE + "Brewing Modifier"
        ));

        ItemStack deepShell = createAlchemyItem("Deep Shell", Material.TURTLE_HELMET, List.of(
                ChatColor.GRAY + "A resilient shell from the depths.",
                ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Repairs equipment greatly.",
                ChatColor.GRAY + "Restores 400 durability.",
                ChatColor.DARK_PURPLE + "Smithing Ingredient"
        ));

        ItemStack deepInk = createAlchemyItem("Deep Ink", Material.BLACK_DYE, List.of(
                ChatColor.GRAY + "A handful of ink from deep waters.",
                ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Glowstone substitute.",
                ChatColor.GRAY + "",
                ChatColor.DARK_PURPLE + "Brewing Modifier"
        ));

        ItemStack deepTear = createAlchemyItem("Deep Tooth", Material.IRON_NUGGET, List.of(
                ChatColor.GRAY + "A tooth from a fierce predator.",
                ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Grants Strength 4 for 30 seconds.",
                ChatColor.GRAY + "Adds Strength IV",
                ChatColor.DARK_PURPLE + "Artifact"
        ));

        ItemStack abyssalInk = createAlchemyItem("Abyssal Ink", Material.BLACK_DYE, List.of(
                ChatColor.GRAY + "Ink from the deepest abyss.",
                ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Exceptionally powerful Potency Modifier.",
                ChatColor.GRAY + "",
                ChatColor.DARK_PURPLE + "Mastery Brewing Modifier"
        ));


        ItemStack abyssalShell = createAlchemyItem("Abyssal Shell", Material.YELLOW_DYE, List.of(
                ChatColor.GRAY + "A shell from the deepest abyss.",
                ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Repairs equipment massively.",
                ChatColor.GRAY + "Restores 10000 durability.",
                ChatColor.DARK_PURPLE + "Smithing Ingredient"
        ));

        ItemStack abyssalVenom = createAlchemyItem("Abyssal Venom", Material.POTION, List.of(
                ChatColor.GRAY + "A vial of fatal venom.",
                ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Exceptionally powerful Duration Modifier.",
                ChatColor.GRAY + "",
                ChatColor.DARK_PURPLE + "Mastery Brewing Modifier"
        ));
        ItemStack trident = new ItemStack(Material.TRIDENT);


        ItemStack aquaAffinity = createCustomItem(Material.TURTLE_EGG, ChatColor.YELLOW +
                "Turtle Tactics", Arrays.asList(
                ChatColor.GRAY + "Water Technology.",
                ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Apply it to equipment to unlock the secrets of Aqua Affinity.",
                ChatColor.DARK_PURPLE + "Smithing Item"
        ), 1,false, true);
        ItemStack respiration = createCustomItem(Material.GLASS, ChatColor.YELLOW +
                "Diving Helmet", Arrays.asList(
                ChatColor.GRAY + "Water Technology.",
                ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Apply it to equipment to unlock the secrets of Respiration.",
                ChatColor.DARK_PURPLE + "Smithing Item"
        ), 1,false, true);
        ItemStack swiftSneak = createCustomItem(Material.LEATHER_LEGGINGS, ChatColor.YELLOW +
                "Swim Trunks", Arrays.asList(
                ChatColor.GRAY + "Water Technology.",
                ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Apply it to equipment to unlock the secrets of Swift Sneak.",
                ChatColor.DARK_PURPLE + "Smithing Item"
        ), 1,false, true);
        ItemStack impaling = createCustomItem(Material.BONE, ChatColor.YELLOW +
                "Narwhal Tusk", Arrays.asList(
                ChatColor.GRAY + "Water Technology.",
                ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Apply it to equipment to unlock the secrets of Impaling.",
                ChatColor.DARK_PURPLE + "Smithing Item"
        ), 1,false, true);
        ItemStack sweepingEdge = createCustomItem(Material.IRON_SWORD, ChatColor.YELLOW +
                "Sweeping Edge", Arrays.asList(
                ChatColor.GRAY + "Air Technology.",
                ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Apply it to equipment to unlock the secrets of Sweeping Edge.",
                ChatColor.DARK_PURPLE + "Smithing Item"
        ), 1,false, true);
        ItemStack channeling = createCustomItem(Material.LIGHTNING_ROD, ChatColor.YELLOW +
                "Lightning Bolt", Arrays.asList(
                ChatColor.GRAY + "Air Technology.",
                ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Apply it to equipment to unlock the secrets of Channeling.",
                ChatColor.DARK_PURPLE + "Smithing Item"
        ), 1,false, true);
        ItemStack riptide = createCustomItem(Material.CONDUIT, ChatColor.YELLOW +
                "Anaklusmos", Arrays.asList(
                ChatColor.GRAY + "Water Technology.",
                ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Apply it to equipment to unlock the secrets of Riptide.",
                ChatColor.DARK_PURPLE + "Smithing Item"
        ), 1,false, true);







        SEA_CREATURES.add(new SeaCreature(
                "Shark",
                Rarity.RARE,
                EntityType.ZOMBIE,
                sweepingEdge,
                Color.fromRGB(192, 220, 220),
                "Shark",  // unique texture for this creature
                RARITY_LEVELS.get(Rarity.RARE)
        ));




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
