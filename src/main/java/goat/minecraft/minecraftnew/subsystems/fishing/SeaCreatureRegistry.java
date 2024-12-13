package goat.minecraft.minecraftnew.subsystems.fishing;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import goat.minecraft.minecraftnew.subsystems.utils.CustomItemManager;
import goat.minecraft.minecraftnew.subsystems.utils.ItemRegistry;
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
        ItemStack fishBone = ItemRegistry.getFishBone();
        ItemStack tooth = ItemRegistry.getTooth();
        ItemStack shallowShell = ItemRegistry.getShallowShell();
        ItemStack shell = ItemRegistry.getShell();
        ItemStack luminescentInk = ItemRegistry.getLuminescentInk();
        ItemStack leviathanHeart = ItemRegistry.getLeviathanHeart();
        ItemStack deepShell = ItemRegistry.getDeepShell();
        ItemStack abyssalInk = ItemRegistry.getAbyssalInk();
        ItemStack abyssalShell = ItemRegistry.getAbyssalShell();
        ItemStack abyssalVenom = ItemRegistry.getAbyssalVenom();

        ItemStack aquaAffinity = ItemRegistry.getAquaAffinity();
        ItemStack respiration = ItemRegistry.getRespiration();
        ItemStack swiftSneak = ItemRegistry.getSwiftSneak();
        ItemStack impaling = ItemRegistry.getImpaling();
        ItemStack sweepingEdge = ItemRegistry.getSweepingEdge();
        ItemStack channeling = ItemRegistry.getChanneling();
        ItemStack riptide = ItemRegistry.getRiptide();






        List<SeaCreature.DropItem> sharkDrops = new ArrayList<>();
        sharkDrops.add(new SeaCreature.DropItem(tooth, 3, 1, 5));
        sharkDrops.add(new SeaCreature.DropItem(fishBone, 3, 1, 5));
        SEA_CREATURES.add(new SeaCreature(
                "Shark",
                Rarity.RARE,
                EntityType.ZOMBIE,
                sharkDrops,
                Color.fromRGB(47, 47, 47),
                "Shark",  // unique texture for this creature
                RARITY_LEVELS.get(Rarity.RARE)
        ));
        SEA_CREATURES.add(new SeaCreature(
                "Pirate",
                Rarity.RARE,
                EntityType.SKELETON,
                null,
                Color.fromRGB(0, 0, 0),
                "Pirate",  // unique texture for this creature
                RARITY_LEVELS.get(Rarity.RARE)
        ));





        List<SeaCreature.DropItem> greatWhiteSharkDrops = new ArrayList<>();
        greatWhiteSharkDrops.add(new SeaCreature.DropItem(fishBone, 7, 1, 5));
        greatWhiteSharkDrops.add(new SeaCreature.DropItem(tooth, 7, 1, 4));
        SEA_CREATURES.add(new SeaCreature(
                "Great White Shark",
                Rarity.EPIC,
                EntityType.ZOMBIE,
                greatWhiteSharkDrops,
                Color.fromRGB(0, 51, 102),
                "Great_White_Shark",  // unique texture for this creature
                RARITY_LEVELS.get(Rarity.EPIC)
        ));






        List<SeaCreature.DropItem> megalodonDrops = new ArrayList<>();
        megalodonDrops.add(new SeaCreature.DropItem(tooth, 31, 1, 4));
        megalodonDrops.add(new SeaCreature.DropItem(fishBone, 31, 1, 4));
        SEA_CREATURES.add(new SeaCreature(
                "Megalodon",
                Rarity.LEGENDARY,
                EntityType.ZOMBIE,
                megalodonDrops,
                Color.fromRGB(211, 211, 211),
                "Megalodon",  // unique texture for this creature
                RARITY_LEVELS.get(Rarity.LEGENDARY)
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
