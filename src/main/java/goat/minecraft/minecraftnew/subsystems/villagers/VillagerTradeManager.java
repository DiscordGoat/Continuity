package goat.minecraft.minecraftnew.subsystems.villagers;

import goat.minecraft.minecraftnew.MinecraftNew;
import goat.minecraft.minecraftnew.subsystems.fishing.SeaCreatureRegistry;
import goat.minecraft.minecraftnew.subsystems.generators.ResourceGeneratorSubsystem;
import goat.minecraft.minecraftnew.subsystems.pets.PetManager;
import goat.minecraft.minecraftnew.subsystems.utils.CustomItemManager;
import goat.minecraft.minecraftnew.subsystems.utils.ItemRegistry;
import goat.minecraft.minecraftnew.subsystems.utils.StructureUtils;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

import static goat.minecraft.minecraftnew.subsystems.fishing.SeaCreatureRegistry.createAlchemyItem;
import static goat.minecraft.minecraftnew.subsystems.utils.CustomItemManager.createCustomItem;

public class VillagerTradeManager implements Listener {

    private final JavaPlugin plugin;
    private final Map<Player, Villager> playerVillagerMap = new HashMap<>(); // Map to store player-villager interactions

    // Whitelists for trades
    private final Map<Villager.Profession, List<TradeItem>> purchaseWhitelist = new HashMap<>();
    private final Map<Villager.Profession, List<TradeItem>> sellWhitelist = new HashMap<>();

    // Max villager level
    private static final int MAX_VILLAGER_LEVEL = 5;

    public VillagerTradeManager(JavaPlugin plugin) {
        this.plugin = plugin;

        // Initialize whitelists
        initializeWhitelists();

        // Register events
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }
    ItemStack secretsOfInfinity = CustomItemManager.createCustomItem(
            Material.ARROW,
            ChatColor.DARK_PURPLE + "Secrets of Infinity",
            Arrays.asList(
                    ChatColor.GRAY + "A piece of wood imbued with knowledge.",
                    ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Adds 1 level of Infinity.",
                    ChatColor.DARK_PURPLE + "Smithing Ingredient"
            ),
            1,
            true, // Unbreakable
            true  // Add enchantment shimmer
    );
    ResourceGeneratorSubsystem resourceGeneratorSubsystem = new ResourceGeneratorSubsystem(MinecraftNew.getInstance());
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
    ItemStack forbiddenBook = CustomItemManager.createCustomItem(
            Material.WRITTEN_BOOK,
            ChatColor.YELLOW + "Forbidden Book",
            Arrays.asList(
                    ChatColor.GRAY + "A dangerous book full of experimental magic.",
                    ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Apply to equipment to push the limits of enchantments.",
                    ChatColor.DARK_PURPLE + "Enchanting Item"
            ),
            1,
            false, // Not unbreakable
            true   // Add enchantment shimmer
    );
    ItemStack deepShell = createAlchemyItem("Deep Shell", Material.TURTLE_HELMET, List.of(
            ChatColor.GRAY + "A resilient shell from the depths.",
            ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Repairs equipment greatly.",
            ChatColor.GRAY + "Restores 400 durability.",
            ChatColor.DARK_PURPLE + "Smithing Ingredient"
    ));
    public ItemStack diamondGemstone() {
        return createCustomItem(
                Material.DIAMOND,
                ChatColor.DARK_PURPLE + "Diamond Gemstone",
                List.of(ChatColor.GRAY + "A rare mineral.",
                        "Apply it to equipment to unlock triple drop chance.",
                        "Smithing Item"),
                1,
                false,
                true
        );
    }
    public ItemStack lapisGemstone() {
        return createCustomItem(
                Material.LAPIS_LAZULI,
                ChatColor.DARK_PURPLE + "Lapis Gemstone",
                List.of(ChatColor.GRAY + "A rare mineral.",
                        "Apply it to equipment to enrich mining XP gains.",
                        "Smithing Item"),
                1,
                false,
                true
        );
    }
    public ItemStack redstoneGemstone() {
        return createCustomItem(
                Material.REDSTONE,
                ChatColor.DARK_PURPLE + "Redstone Gemstone",
                List.of(ChatColor.GRAY + "A rare mineral.",
                        "Apply it to equipment to enrich Gold Fever.",
                        "Smithing Item"),
                1,
                false,
                true
        );
    }
    public ItemStack emeraldGemstone() {
        return createCustomItem(
                Material.EMERALD,
                ChatColor.DARK_PURPLE + "Emerald Gemstone",
                List.of(ChatColor.GRAY + "A rare mineral.",
                        "Apply it to equipment to unlock night vision chance.",
                        "Smithing Item"),
                1,
                false,
                true
        );
    }
    ItemStack deepInk = createAlchemyItem("Deep Ink", Material.BLACK_DYE, List.of(
            ChatColor.GRAY + "A handful of ink from deep waters.",
            ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Glowstone substitute.",
            ChatColor.GRAY + "",
            ChatColor.DARK_PURPLE + "Brewing Modifier"
    ));
    ItemStack swiftSneak = createCustomItem(Material.LEATHER_LEGGINGS, ChatColor.YELLOW +
            "Swim Trunks", Arrays.asList(
            ChatColor.GRAY + "Water Technology.",
            ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Apply it to equipment to unlock the secrets of Swift Sneak.",
            ChatColor.DARK_PURPLE + "Smithing Item"
    ), 1,false, true);
    ItemStack deepTear = createAlchemyItem("Deep Tooth", Material.IRON_NUGGET, List.of(
            ChatColor.GRAY + "A tooth from a fierce predator.",
            ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Brewing alternative for strength.",
            ChatColor.GRAY + "Adds Strength II",
            ChatColor.DARK_PURPLE + "Brewing Modifier"
    ));
    ItemStack aquaAffinity = createCustomItem(Material.TURTLE_EGG, ChatColor.YELLOW +
            "Turtle Tactics", Arrays.asList(
            ChatColor.GRAY + "Water Technology.",
            ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Apply it to equipment to unlock the secrets of Aqua Affinity.",
            ChatColor.DARK_PURPLE + "Smithing Item"
    ), 1,false, true);
    ItemStack abyssalInk = createAlchemyItem("Abyssal Ink", Material.BLACK_DYE, List.of(
            ChatColor.GRAY + "Ink from the deepest abyss.",
            ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Exceptionally powerful Potency Modifier.",
            ChatColor.GRAY + "",
            ChatColor.DARK_PURPLE + "Mastery Brewing Modifier"
    ));
    public ItemStack singularity() {
        return createCustomItem(
                Material.IRON_NUGGET,
                ChatColor.BLUE + "Singularity",
                List.of(ChatColor.GRAY + "A rare blueprint entrusted to the Knights",
                        ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Reforges Items to the first Tier.",
                        ChatColor.DARK_PURPLE + "Smithing Item"),
                1,
                false // Set to true if you want it to be unbreakable
                , true
        );
    }

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
    ItemStack trident = createAlchemyItem("Trident", Material.TRIDENT, List.of(
            ChatColor.GRAY + "A Trident.",
            ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Trident-ing.",
            ChatColor.GRAY + "",
            ChatColor.DARK_PURPLE + "Trident"
    ));

    ItemStack farmerEnchant = CustomItemManager.createCustomItem(Material.RABBIT_STEW, ChatColor.YELLOW +
            "Well Balanced Meal", Arrays.asList(
            ChatColor.GRAY + "Max level of III",
            ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Adds 1 Level of Feed to items.",
            ChatColor.DARK_PURPLE + "Smithing Item"
    ), 1,false, true);
    ItemStack butcherEnchant = CustomItemManager.createCustomItem(Material.GOLDEN_AXE, ChatColor.YELLOW +
            "Brutal Tactics", Arrays.asList(
            ChatColor.GRAY + "Max level of V",
            ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Adds 1 Level of Cleaver to items.",
            ChatColor.DARK_PURPLE + "Smithing Item"
    ), 1,false, true);
    ItemStack fisherEnchant = CustomItemManager.createCustomItem(Material.COD, ChatColor.YELLOW +
            "Call of the Void", Arrays.asList(
            ChatColor.GRAY + "Max level of V",
            ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Adds 1 Level of Call of the Void to items.",
            ChatColor.DARK_PURPLE + "Smithing Item"
    ), 1,false, true);
    ItemStack ironGolem = CustomItemManager.createCustomItem(Material.IRON_BLOCK, ChatColor.YELLOW +
            "Iron Golem", Arrays.asList(
            ChatColor.GRAY + "Ancient Summoning Artifact.",
            ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Summon an Iron Golem.",
            ChatColor.DARK_PURPLE + "Summoning Artifact"
    ), 1,false, true);
    ItemStack librarianEnchant = CustomItemManager.createCustomItem(Material.EXPERIENCE_BOTTLE, ChatColor.YELLOW +
            "Savant", Arrays.asList(
            ChatColor.GRAY + "Max level of 1",
            ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Adds 1 Level of Savant to items.",
            ChatColor.DARK_PURPLE + "Smithing Item"
    ), 1,false, true);
    ItemStack librarianEnchant2 = CustomItemManager.createCustomItem(Material.SOUL_LANTERN, ChatColor.YELLOW +
            "Soul Lantern", Arrays.asList(
            ChatColor.GRAY + "Max level of 5",
            ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Adds 1 Level of Experience to items.",
            ChatColor.DARK_PURPLE + "Smithing Item"
    ), 1,false, true);
    ItemStack randomTrim = CustomItemManager.createCustomItem(Material.PAPER, ChatColor.YELLOW +
            "Draw Random Armor Trim", Arrays.asList(
            ChatColor.GRAY + "A collection of materials and tools",
            ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Creates a random Armor Trim.",
            ChatColor.DARK_PURPLE + "Artifact"
    ), 1,false, true);
    ItemStack armorerEnchant = CustomItemManager.createCustomItem(Material.GLASS_BOTTLE, ChatColor.YELLOW +
            "Oxygen Tank", Arrays.asList(
            ChatColor.GRAY + "Max level of 4",
            ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Adds 1 Level of Ventilation to items.",
            ChatColor.DARK_PURPLE + "Smithing Item"
    ), 1,false, true);
    ItemStack toolsmithEnchant = CustomItemManager.createCustomItem(Material.TORCH, ChatColor.YELLOW +
            "Everflame", Arrays.asList(
            ChatColor.GRAY + "Max level of 5",
            ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Adds 1 Level of Forge to items.",
            ChatColor.DARK_PURPLE + "Smithing Item"
    ), 1,false, true);
    ItemStack toolsmithEnchantTwo = CustomItemManager.createCustomItem(Material.CHAIN, ChatColor.YELLOW +
            "Climbing Rope", Arrays.asList(
            ChatColor.GRAY + "Max level of 1",
            ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Adds 1 Level of Rappel to items.",
            ChatColor.DARK_PURPLE + "Smithing Item"
    ), 1,false, true);
    ItemStack shepherdArtifact = CustomItemManager.createCustomItem(Material.BRUSH, ChatColor.YELLOW +
            "Creative Mind", Arrays.asList(
            ChatColor.GRAY + "A collection of Colors and Mixes",
            ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Hydrates All Concrete",
            ChatColor.DARK_PURPLE + "Artifact"
    ), 1,false, true);
    ItemStack shepherdEnchant = CustomItemManager.createCustomItem(Material.SHEARS, ChatColor.YELLOW +
            "Laceration", Arrays.asList(
            ChatColor.GRAY + "Max level of 5",
            ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Adds 1 Level of Shear to items.",
            ChatColor.DARK_PURPLE + "Smithing Item"
    ), 1,false, true);
    ItemStack fishingEnchant = CustomItemManager.createCustomItem(Material.GOLD_NUGGET, ChatColor.YELLOW +
            "Golden Hook", Arrays.asList(
            ChatColor.GRAY + "Max level of 5",
            ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Adds 1 Level of Piracy to items.",
            ChatColor.DARK_PURPLE + "Smithing Item"
    ), 1,false, true);
    ItemStack leatherworkerEnchant = CustomItemManager.createCustomItem(Material.LEATHER, ChatColor.YELLOW +
            "Hide", Arrays.asList(
            ChatColor.GRAY + "Max level of 4",
            ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Adds 1 Level of Physical Protection to items.",
            ChatColor.DARK_PURPLE + "Smithing Item"
    ), 1,false, true);
    ItemStack leatherworkerArtifact = CustomItemManager.createCustomItem(Material.BOOK, ChatColor.YELLOW +
            "Backpack", Arrays.asList(
            ChatColor.GRAY + "A storage device for items",
            ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Opens Backpack.",
            ChatColor.DARK_PURPLE + "Artifact"
    ), 1,false, true);
    ItemStack clericEnchant = CustomItemManager.createCustomItem(Material.SUGAR_CANE, ChatColor.YELLOW +
            "Alchemical Bundle", Arrays.asList(
            ChatColor.GRAY + "Max level of 4",
            ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Adds 1 Level of Alchemy to items.",
            ChatColor.DARK_PURPLE + "Smithing Item"
    ), 1,false, true);





    // Mineshaft Location
    ItemStack cartographerMineshaft = CustomItemManager.createCustomItem(
            Material.FILLED_MAP,
            ChatColor.YELLOW + "Mineshaft Location",
            Arrays.asList(
                    ChatColor.GRAY + "The coords of a location",
                    ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Locates the nearest structure",
                    ChatColor.DARK_PURPLE + "Artifact"
            ),
            1,
            false,
            true
    );

    // Stronghold Location
    ItemStack cartographerStronghold = CustomItemManager.createCustomItem(
            Material.FILLED_MAP,
            ChatColor.YELLOW + "Stronghold Location",
            Arrays.asList(
                    ChatColor.GRAY + "The coords of a location",
                    ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Locates the nearest structure",
                    ChatColor.DARK_PURPLE + "Artifact"
            ),
            1,
            false,
            true
    );

    // Village Location
    ItemStack cartographerVillage = CustomItemManager.createCustomItem(
            Material.FILLED_MAP,
            ChatColor.YELLOW + "Village Location",
            Arrays.asList(
                    ChatColor.GRAY + "The coords of a location",
                    ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Locates the nearest structure",
                    ChatColor.DARK_PURPLE + "Artifact"
            ),
            1,
            false,
            true
    );

    // Igloo Location
    ItemStack cartographerIgloo = CustomItemManager.createCustomItem(
            Material.FILLED_MAP,
            ChatColor.YELLOW + "Igloo Location",
            Arrays.asList(
                    ChatColor.GRAY + "The coords of a location",
                    ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Locates the nearest structure",
                    ChatColor.DARK_PURPLE + "Artifact"
            ),
            1,
            false,
            true
    );

    // Buried Treasure Location
    ItemStack cartographerBuriedTreasure = CustomItemManager.createCustomItem(
            Material.FILLED_MAP,
            ChatColor.YELLOW + "Buried Treasure Location",
            Arrays.asList(
                    ChatColor.GRAY + "The coords of a location",
                    ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Locates the nearest structure",
                    ChatColor.DARK_PURPLE + "Artifact"
            ),
            1,
            false,
            true
    );

    // Desert Pyramid Location
    ItemStack cartographerDesertPyramid = CustomItemManager.createCustomItem(
            Material.FILLED_MAP,
            ChatColor.YELLOW + "Desert Pyramid Location",
            Arrays.asList(
                    ChatColor.GRAY + "The coords of a location",
                    ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Locates the nearest structure",
                    ChatColor.DARK_PURPLE + "Artifact"
            ),
            1,
            false,
            true
    );

    // Jungle Pyramid Location
    ItemStack cartographerJunglePyramid = CustomItemManager.createCustomItem(
            Material.FILLED_MAP,
            ChatColor.YELLOW + "Jungle Pyramid Location",
            Arrays.asList(
                    ChatColor.GRAY + "The coords of a location",
                    ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Locates the nearest structure",
                    ChatColor.DARK_PURPLE + "Artifact"
            ),
            1,
            false,
            true
    );

    // Ocean Monument Location
    ItemStack cartographerOceanMonument = CustomItemManager.createCustomItem(
            Material.FILLED_MAP,
            ChatColor.YELLOW + "Ocean Monument Location",
            Arrays.asList(
                    ChatColor.GRAY + "The coords of a location",
                    ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Locates the nearest structure",
                    ChatColor.DARK_PURPLE + "Artifact"
            ),
            1,
            false,
            true
    );

    // Pillager Outpost Location
    ItemStack cartographerPillagerOutpost = CustomItemManager.createCustomItem(
            Material.FILLED_MAP,
            ChatColor.YELLOW + "Pillager Outpost Location",
            Arrays.asList(
                    ChatColor.GRAY + "The coords of a location",
                    ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Locates the nearest structure",
                    ChatColor.DARK_PURPLE + "Artifact"
            ),
            1,
            false,
            true
    );

    // Swamp Hut Location
    ItemStack cartographerSwampHut = CustomItemManager.createCustomItem(
            Material.FILLED_MAP,
            ChatColor.YELLOW + "Swamp Hut Location",
            Arrays.asList(
                    ChatColor.GRAY + "The coords of a location",
                    ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Locates the nearest structure",
                    ChatColor.DARK_PURPLE + "Artifact"
            ),
            1,
            false,
            true
    );

    // Woodland Mansion Location
    ItemStack cartographerWoodlandMansion = CustomItemManager.createCustomItem(
            Material.FILLED_MAP,
            ChatColor.YELLOW + "Woodland Mansion Location",
            Arrays.asList(
                    ChatColor.GRAY + "The coords of a location",
                    ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Locates the nearest structure",
                    ChatColor.DARK_PURPLE + "Artifact"
            ),
            1,
            false,
            true
    );

    // Bastion Remnant Location
    ItemStack cartographerBastionRemnant = CustomItemManager.createCustomItem(
            Material.FILLED_MAP,
            ChatColor.YELLOW + "Bastion Remnant Location",
            Arrays.asList(
                    ChatColor.GRAY + "The coords of a location",
                    ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Locates the nearest structure",
                    ChatColor.DARK_PURPLE + "Artifact"
            ),
            1,
            false,
            true
    );

    // End City Location
    ItemStack cartographerEndCity = CustomItemManager.createCustomItem(
            Material.FILLED_MAP,
            ChatColor.YELLOW + "End City Location",
            Arrays.asList(
                    ChatColor.GRAY + "The coords of a location",
                    ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Locates the nearest structure",
                    ChatColor.DARK_PURPLE + "Artifact"
            ),
            1,
            false,
            true
    );

    // Nether Fortress Location
    ItemStack cartographerNetherFortress = CustomItemManager.createCustomItem(
            Material.FILLED_MAP,
            ChatColor.YELLOW + "Nether Fortress Location",
            Arrays.asList(
                    ChatColor.GRAY + "The coords of a location",
                    ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Locates the nearest structure",
                    ChatColor.DARK_PURPLE + "Artifact"
            ),
            1,
            false,
            true
    );

    // Ocean Ruin Location
    ItemStack cartographerOceanRuin = CustomItemManager.createCustomItem(
            Material.FILLED_MAP,
            ChatColor.YELLOW + "Ocean Ruin Location",
            Arrays.asList(
                    ChatColor.GRAY + "The coords of a location",
                    ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Locates the nearest structure",
                    ChatColor.DARK_PURPLE + "Artifact"
            ),
            1,
            false,
            true
    );

    // Shipwreck Location
    ItemStack cartographerShipwreck = CustomItemManager.createCustomItem(
            Material.FILLED_MAP,
            ChatColor.YELLOW + "Shipwreck Location",
            Arrays.asList(
                    ChatColor.GRAY + "The coords of a location",
                    ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Locates the nearest structure",
                    ChatColor.DARK_PURPLE + "Artifact"
            ),
            1,
            false,
            true
    );
    ItemStack aspectAOfTheJourney = CustomItemManager.createCustomItem(
            Material.ENDER_EYE,
            ChatColor.YELLOW + "Fast Travel",
            Arrays.asList(
                    ChatColor.GRAY + "Max level of I",
                    ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Adds 1 Level of Aspect of the Journey to items.",
                    ChatColor.DARK_PURPLE + "Smithing Item"
            ),
            1,
            false,
            true
    );
    ItemStack fletcherBowEnchant = CustomItemManager.createCustomItem(
            Material.WHITE_DYE,
            ChatColor.YELLOW + "Stun Coating",
            Arrays.asList(
                    ChatColor.GRAY + "Max level of 5",
                    ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Adds 1 Level of Stun to items.",
                    ChatColor.DARK_PURPLE + "Smithing Item"
            ),
            1,
            false,
            true
    );
    ItemStack fletcherCrossbowEnchant = CustomItemManager.createCustomItem(
            Material.FIRE_CHARGE,
            ChatColor.YELLOW + "Explosive Arrows",
            Arrays.asList(
                    ChatColor.GRAY + "Max level of 10",
                    ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Adds 1 Level of Lethal Reaction to items.",
                    ChatColor.DARK_PURPLE + "Smithing Item"
            ),
            1,
            false,
            true
    );
    ItemStack weaponsmithEnchant = CustomItemManager.createCustomItem(
            Material.RED_DYE,
            ChatColor.YELLOW + "Lethal Tempo",
            Arrays.asList(
                    ChatColor.GRAY + "Max level of 5",
                    ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Adds 1 Level of Bloodlust to items.",
                    ChatColor.DARK_PURPLE + "Smithing Item"
            ),
            1,
            false,
            true
    );
    ItemStack armorsmithReforge = CustomItemManager.createCustomItem(
            Material.MOJANG_BANNER_PATTERN,
            ChatColor.YELLOW + "Armor Talisman",
            Arrays.asList(
                    ChatColor.GRAY + "An armorsmiths expertise",
                    ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Talisman for obtaining a higher Armor Rating.",
                    ChatColor.DARK_PURPLE + "Smithing Item"
            ),
            1,
            false,
            true
    );
    ItemStack armorsmithReforgeTwo = CustomItemManager.createCustomItem(
            Material.MOJANG_BANNER_PATTERN,
            ChatColor.YELLOW + "Armor Toughness Talisman",
            Arrays.asList(
                    ChatColor.GRAY + "An armorsmiths expertise",
                    ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Talisman for obtaining a higher Armor Toughness Rating.",
                    ChatColor.DARK_PURPLE + "Smithing Item"
            ),
            1,
            false,
            true
    );
    ItemStack armorsmithReforgeThree = CustomItemManager.createCustomItem(
            Material.MOJANG_BANNER_PATTERN,
            ChatColor.YELLOW + "Knockback Talisman",
            Arrays.asList(
                    ChatColor.GRAY + "An armorsmiths expertise",
                    ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Talisman for obtaining reduced Knockback.",
                    ChatColor.DARK_PURPLE + "Smithing Item"
            ),
            1,
            false,
            true
    );
    ItemStack weaponsmithReforge = CustomItemManager.createCustomItem(
            Material.MOJANG_BANNER_PATTERN,
            ChatColor.YELLOW + "Attack Damage Talisman",
            Arrays.asList(
                    ChatColor.GRAY + "An weaponsmiths expertise",
                    ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Talisman for obtaining a higher Attack Damage Rating.",
                    ChatColor.DARK_PURPLE + "Smithing Item"
            ),
            1,
            false,
            true
    );
    ItemStack weaponsmithReforgeTwo = CustomItemManager.createCustomItem(
            Material.MOJANG_BANNER_PATTERN,
            ChatColor.YELLOW + "Swift Blade Talisman",
            Arrays.asList(
                    ChatColor.GRAY + "An weaponsmiths expertise",
                    ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Talisman for obtaining a higher Attack Damage Rating.",
                    ChatColor.DARK_PURPLE + "Smithing Item"
            ),
            1,
            false,
            true
    );
    ItemStack fishermanReforge = CustomItemManager.createCustomItem(
            Material.MOJANG_BANNER_PATTERN,
            ChatColor.YELLOW + "Sea Creature Talisman",
            Arrays.asList(
                    ChatColor.GRAY + "An fishermans expertise",
                    ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Talisman for obtaining a higher Sea Creature Chance.",
                    ChatColor.DARK_PURPLE + "Smithing Item"
            ),
            1,
            false,
            true
    );
    ItemStack toolsmithReforge = CustomItemManager.createCustomItem(
            Material.MOJANG_BANNER_PATTERN,
            ChatColor.YELLOW + "Durability Talisman",
            Arrays.asList(
                    ChatColor.GRAY + "A Toolsmiths expertise",
                    ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Talisman for obtaining a higher Durability Rating.",
                    ChatColor.DARK_PURPLE + "Smithing Item"
            ),
            1,
            false,
            true
    );
    ItemStack toolsmithEfficiency = CustomItemManager.createCustomItem(
            Material.GOLDEN_PICKAXE,
            ChatColor.YELLOW + "Efficiency Expertise",
            Arrays.asList(
                    ChatColor.GRAY + "A Toolsmiths expertise",
                    ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Refines an Enchantment beyond normal max levels.",
                    ChatColor.DARK_PURPLE + "Mastery Enchant"
            ),
            1,
            false,
            true
    );
    ItemStack toolsmithUnbreaking = CustomItemManager.createCustomItem(
            Material.OBSIDIAN,
            ChatColor.YELLOW + "Unbreaking Expertise",
            Arrays.asList(
                    ChatColor.GRAY + "A Toolsmiths expertise",
                    ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Refines an Enchantment beyond normal max levels.",
                    ChatColor.DARK_PURPLE + "Mastery Enchant"
            ),
            1,
            false,
            true
    );
    ItemStack weaponsmithSharpness = CustomItemManager.createCustomItem(
            Material.GOLDEN_SWORD,
            ChatColor.YELLOW + "Sharpness Expertise",
            Arrays.asList(
                    ChatColor.GRAY + "A Weaponsmiths expertise",
                    ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Refines an Enchantment beyond normal max levels.",
                    ChatColor.DARK_PURPLE + "Mastery Enchant"
            ),
            1,
            false,
            true
    );
    ItemStack weaponsmithSweepingEdge = CustomItemManager.createCustomItem(
            Material.WHEAT,
            ChatColor.YELLOW + "Sweeping Edge Expertise",
            Arrays.asList(
                    ChatColor.GRAY + "A Weaponsmiths expertise",
                    ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Refines an Enchantment beyond normal max levels.",
                    ChatColor.DARK_PURPLE + "Mastery Enchant"
            ),
            1,
            false,
            true
    );
    ItemStack weaponsmithLooting = CustomItemManager.createCustomItem(
            Material.GOLD_INGOT,
            ChatColor.YELLOW + "Looting Expertise",
            Arrays.asList(
                    ChatColor.GRAY + "A Weaponsmiths expertise",
                    ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Refines an Enchantment beyond normal max levels.",
                    ChatColor.DARK_PURPLE + "Mastery Enchant"
            ),
            1,
            false,
            true
    );
    ItemStack weaponsmithKnockback = CustomItemManager.createCustomItem(
            Material.SLIME_BLOCK,
            ChatColor.YELLOW + "Knockback Expertise",
            Arrays.asList(
                    ChatColor.GRAY + "A Weaponsmiths expertise",
                    ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Refines an Enchantment beyond normal max levels.",
                    ChatColor.DARK_PURPLE + "Mastery Enchant"
            ),
            1,
            false,
            true
    );
    ItemStack weaponsmithFireAspect = CustomItemManager.createCustomItem(
            Material.FIRE_CHARGE,
            ChatColor.YELLOW + "Fire Aspect Expertise",
            Arrays.asList(
                    ChatColor.GRAY + "A Weaponsmiths expertise",
                    ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Refines an Enchantment beyond normal max levels.",
                    ChatColor.DARK_PURPLE + "Mastery Enchant"
            ),
            1,
            false,
            true
    );
    ItemStack weaponsmithSmite = CustomItemManager.createCustomItem(
            Material.BONE,
            ChatColor.YELLOW + "Smite Expertise",
            Arrays.asList(
                    ChatColor.GRAY + "A Weaponsmiths expertise",
                    ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Refines an Enchantment beyond normal max levels.",
                    ChatColor.DARK_PURPLE + "Mastery Enchant"
            ),
            1,
            false,
            true
    );
    ItemStack weaponsmithBaneofAnthropods = CustomItemManager.createCustomItem(
            Material.FERMENTED_SPIDER_EYE,
            ChatColor.YELLOW + "Bane of Anthropods Expertise",
            Arrays.asList(
                    ChatColor.GRAY + "A Weaponsmiths expertise",
                    ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Refines an Enchantment beyond normal max levels.",
                    ChatColor.DARK_PURPLE + "Mastery Enchant"
            ),
            1,
            false,
            true
    );
    ItemStack fishermanLure = CustomItemManager.createCustomItem(
            Material.BRAIN_CORAL_BLOCK,
            ChatColor.YELLOW + "Lure Expertise",
            Arrays.asList(
                    ChatColor.GRAY + "A Fishermans expertise",
                    ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Refines an Enchantment beyond normal max levels.",
                    ChatColor.DARK_PURPLE + "Mastery Enchant"
            ),
            1,
            false,
            true
    );
    ItemStack fishermanLuckoftheSea = CustomItemManager.createCustomItem(
            Material.STICK,
            ChatColor.YELLOW + "Luck of the Sea Expertise",
            Arrays.asList(
                    ChatColor.GRAY + "A Fishermans expertise",
                    ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Refines an Enchantment beyond normal max levels.",
                    ChatColor.DARK_PURPLE + "Mastery Enchant"
            ),
            1,
            false,
            true
    );
    ItemStack armorSmithProtection = CustomItemManager.createCustomItem(
            Material.GOLDEN_CHESTPLATE,
            ChatColor.YELLOW + "Protection Expertise",
            Arrays.asList(
                    ChatColor.GRAY + "An Armorsmiths expertise",
                    ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Refines an Enchantment beyond normal max levels.",
                    ChatColor.DARK_PURPLE + "Mastery Enchant"
            ),
            1,
            false,
            true
    );
    ItemStack armorSmithRespiration = CustomItemManager.createCustomItem(
            Material.GOLDEN_HELMET,
            ChatColor.YELLOW + "Respiration Expertise",
            Arrays.asList(
                    ChatColor.GRAY + "An Armorsmiths expertise",
                    ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Refines an Enchantment beyond normal max levels.",
                    ChatColor.DARK_PURPLE + "Mastery Enchant"
            ),
            1,
            false,
            true
    );
    ItemStack armorSmithThorns = CustomItemManager.createCustomItem(
            Material.CACTUS,
            ChatColor.YELLOW + "Thorns Expertise",
            Arrays.asList(
                    ChatColor.GRAY + "An Armorsmiths expertise",
                    ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Refines an Enchantment beyond normal max levels.",
                    ChatColor.DARK_PURPLE + "Mastery Enchant"
            ),
            1,
            false,
            true
    );
    ItemStack armorSmithFeatherFalling = CustomItemManager.createCustomItem(
            Material.FEATHER,
            ChatColor.YELLOW + "Feather Falling Expertise",
            Arrays.asList(
                    ChatColor.GRAY + "An Armorsmiths expertise",
                    ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Refines an Enchantment beyond normal max levels.",
                    ChatColor.DARK_PURPLE + "Mastery Enchant"
            ),
            1,
            false,
            true
    );
    ItemStack fletcherPower = CustomItemManager.createCustomItem(
            Material.FEATHER,
            ChatColor.YELLOW + "Power Expertise",
            Arrays.asList(
                    ChatColor.GRAY + "A fletchers expertise",
                    ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Refines an Enchantment beyond normal max levels.",
                    ChatColor.DARK_PURPLE + "Mastery Enchant"
            ),
            1,
            false,
            true
    );
    ItemStack commonSwordReforge = CustomItemManager.createCustomItem(
            Material.WHITE_DYE,
            ChatColor.YELLOW + "Common Sword Reforge",
            Arrays.asList(
                    ChatColor.GRAY + "Reforges a sword to deal more damage",
                    ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Refines base stats beyond normal levels.",
                    ChatColor.DARK_PURPLE + "Smithing Item"
            ),
            1,
            false,
            true
    );
    ItemStack uncommonSwordReforge = CustomItemManager.createCustomItem(
            Material.LIME_DYE,
            ChatColor.YELLOW + "Uncommon Sword Reforge",
            Arrays.asList(
                    ChatColor.GRAY + "Reforges a sword to deal more damage",
                    ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Refines base stats beyond normal levels.",
                    ChatColor.DARK_PURPLE + "Smithing Item"
            ),
            1,
            false,
            true
    );
    ItemStack rareSwordReforge = CustomItemManager.createCustomItem(
            Material.BLUE_DYE,
            ChatColor.YELLOW + "Rare Sword Reforge",
            Arrays.asList(
                    ChatColor.GRAY + "Reforges a sword to deal more damage",
                    ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Refines base stats beyond normal levels.",
                    ChatColor.DARK_PURPLE + "Smithing Item"
            ),
            1,
            false,
            true
    );
    ItemStack epicSwordReforge = CustomItemManager.createCustomItem(
            Material.MAGENTA_DYE,
            ChatColor.YELLOW + "Epic Sword Reforge",
            Arrays.asList(
                    ChatColor.GRAY + "Reforges a sword to deal more damage",
                    ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Refines base stats beyond normal levels.",
                    ChatColor.DARK_PURPLE + "Smithing Item"
            ),
            1,
            false,
            true
    );
    ItemStack legendarySwordReforge = CustomItemManager.createCustomItem(
            Material.YELLOW_DYE,
            ChatColor.YELLOW + "Legendary Sword Reforge",
            Arrays.asList(
                    ChatColor.GRAY + "Reforges a sword to deal more damage",
                    ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Refines base stats beyond normal levels.",
                    ChatColor.DARK_PURPLE + "Smithing Item"
            ),
            1,
            false,
            true
    );


    ItemStack commonArmorReforge = CustomItemManager.createCustomItem(
            Material.WHITE_STAINED_GLASS,
            ChatColor.YELLOW + "Common Armor Reforge",
            Arrays.asList(
                    ChatColor.GRAY + "Reforges armor to absorb more damage",
                    ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Refines base stats beyond normal levels.",
                    ChatColor.DARK_PURPLE + "Smithing Item"
            ),
            1,
            false,
            true
    );
    ItemStack uncommonArmorReforge = CustomItemManager.createCustomItem(
            Material.LIME_STAINED_GLASS,
            ChatColor.YELLOW + "Uncommon Armor Reforge",
            Arrays.asList(
                    ChatColor.GRAY + "Reforges armor to absorb more damage",
                    ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Refines base stats beyond normal levels.",
                    ChatColor.DARK_PURPLE + "Smithing Item"
            ),
            1,
            false,
            true
    );
    ItemStack rareArmorReforge = CustomItemManager.createCustomItem(
            Material.BLUE_STAINED_GLASS,
            ChatColor.YELLOW + "Rare Armor Reforge",
            Arrays.asList(
                    ChatColor.GRAY + "Reforges armor to absorb more damage",
                    ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Refines base stats beyond normal levels.",
                    ChatColor.DARK_PURPLE + "Smithing Item"
            ),
            1,
            false,
            true
    );
    ItemStack epicArmorReforge = CustomItemManager.createCustomItem(
            Material.MAGENTA_STAINED_GLASS,
            ChatColor.YELLOW + "Epic Armor Reforge",
            Arrays.asList(
                    ChatColor.GRAY + "Reforges armor to absorb more damage",
                    ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Refines base stats beyond normal levels.",
                    ChatColor.DARK_PURPLE + "Smithing Item"
            ),
            1,
            false,
            true
    );
    ItemStack legendaryArmorReforge = CustomItemManager.createCustomItem(
            Material.YELLOW_STAINED_GLASS,
            ChatColor.YELLOW + "Legendary Armor Reforge",
            Arrays.asList(
                    ChatColor.GRAY + "Reforges armor to absorb more damage",
                    ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Refines base stats beyond normal levels.",
                    ChatColor.DARK_PURPLE + "Smithing Item"
            ),
            1,
            false,
            true
    );
    ItemStack commonToolReforge = CustomItemManager.createCustomItem(
            Material.WHITE_STAINED_GLASS_PANE,
            ChatColor.YELLOW + "Common Tool Reforge",
            Arrays.asList(
                    ChatColor.GRAY + "Reforges tools to take less damage",
                    ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Refines base stats beyond normal levels.",
                    ChatColor.DARK_PURPLE + "Smithing Item"
            ),
            1,
            false,
            true
    );
    ItemStack uncommonToolReforge = CustomItemManager.createCustomItem(
            Material.LIME_STAINED_GLASS_PANE,
            ChatColor.YELLOW + "Uncommon Tool Reforge",
            Arrays.asList(
                    ChatColor.GRAY + "Reforges tools to take less damage",
                    ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Refines base stats beyond normal levels.",
                    ChatColor.DARK_PURPLE + "Smithing Item"
            ),
            1,
            false,
            true
    );
    ItemStack rareToolReforge = CustomItemManager.createCustomItem(
            Material.BLUE_STAINED_GLASS_PANE,
            ChatColor.YELLOW + "Rare Tool Reforge",
            Arrays.asList(
                    ChatColor.GRAY + "Reforges tools to take less damage",
                    ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Refines base stats beyond normal levels.",
                    ChatColor.DARK_PURPLE + "Smithing Item"
            ),
            1,
            false,
            true
    );
    ItemStack epicToolReforge = CustomItemManager.createCustomItem(
            Material.MAGENTA_STAINED_GLASS_PANE,
            ChatColor.YELLOW + "Epic Tool Reforge",
            Arrays.asList(
                    ChatColor.GRAY + "Reforges tools to take less damage",
                    ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Refines base stats beyond normal levels.",
                    ChatColor.DARK_PURPLE + "Smithing Item"
            ),
            1,
            false,
            true
    );
    ItemStack legendaryToolReforge = CustomItemManager.createCustomItem(
            Material.YELLOW_STAINED_GLASS_PANE,
            ChatColor.YELLOW + "Legendary Tool Reforge",
            Arrays.asList(
                    ChatColor.GRAY + "Reforges tools to take less damage",
                    ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Refines base stats beyond normal levels.",
                    ChatColor.DARK_PURPLE + "Smithing Item"
            ),
            1,
            false,
            true
    );
    /**
     * Initializes the purchase and sell whitelists with predefined trades.
     */
    private void initializeWhitelists() {
        // Example for Farmer profession

// Wandering Trader
//biome based trades


// Mason
        List<TradeItem> masonPurchases = new ArrayList<>();
        masonPurchases.add(new TradeItem(new ItemStack(Material.BRICKS, 4), 3, 4, 1)); // Placeholder trade
        masonPurchases.add(new TradeItem(new ItemStack(Material.STONE, 8), 3, 8, 1)); // Placeholder trade

        masonPurchases.add(new TradeItem(new ItemStack(Material.ANDESITE, 8), 3, 8, 2)); // Placeholder trade
        masonPurchases.add(new TradeItem(new ItemStack(Material.DIORITE, 8), 3, 8, 2)); // Placeholder trade
        masonPurchases.add(new TradeItem(new ItemStack(Material.GRANITE, 8), 3, 8, 2)); // Placeholder trade

        masonPurchases.add(new TradeItem(new ItemStack(Material.TERRACOTTA, 8), 3, 8, 3)); // Placeholder trade
        masonPurchases.add(new TradeItem(new ItemStack(Material.QUARTZ_BLOCK, 8), 5, 8, 3)); // Placeholder trade
        masonPurchases.add(new TradeItem(new ItemStack(Material.SMOOTH_STONE, 8), 5, 8, 3)); // Placeholder trade
        masonPurchases.add(new TradeItem(new ItemStack(Material.SANDSTONE, 8), 5, 8, 3)); // Placeholder trade

        masonPurchases.add(new TradeItem(new ItemStack(Material.PRISMARINE, 8), 7, 8, 4)); // Placeholder trade
        masonPurchases.add(new TradeItem(new ItemStack(Material.DARK_PRISMARINE, 8), 7, 8, 4)); // Placeholder trade
        masonPurchases.add(new TradeItem(new ItemStack(Material.PRISMARINE_BRICKS, 8), 7, 8, 4)); // Placeholder trade
        masonPurchases.add(new TradeItem(new ItemStack(Material.NETHER_BRICKS, 8), 7, 8, 4)); // Placeholder trade
        masonPurchases.add(new TradeItem(new ItemStack(Material.BLACKSTONE, 8), 7, 8, 4)); // Placeholder trade
        masonPurchases.add(new TradeItem(new ItemStack(Material.SOUL_SAND, 8), 7, 8, 4)); // Placeholder trade
        masonPurchases.add(new TradeItem(new ItemStack(Material.TUBE_CORAL_BLOCK, 8), 7, 8, 4)); // Placeholder trade
        masonPurchases.add(new TradeItem(new ItemStack(Material.BRAIN_CORAL_BLOCK, 8), 7, 8, 4)); // Placeholder trade
        masonPurchases.add(new TradeItem(new ItemStack(Material.BUBBLE_CORAL_BLOCK, 8), 7, 8, 4)); // Placeholder trade
        masonPurchases.add(new TradeItem(new ItemStack(Material.FIRE_CORAL_BLOCK, 8), 7, 8, 4)); // Placeholder trade
        masonPurchases.add(new TradeItem(new ItemStack(Material.HORN_CORAL_BLOCK, 8), 7, 8, 4)); // Placeholder trade
        purchaseWhitelist.put(Villager.Profession.MASON, masonPurchases);

        List<TradeItem> masonSells = new ArrayList<>();
        masonSells.add(new TradeItem(new ItemStack(Material.CLAY, 4), 1, 4, 1)); // Placeholder trade
        masonSells.add(new TradeItem(new ItemStack(Material.COBBLESTONE, 32), 1, 32, 1)); // Placeholder trade
        masonSells.add(new TradeItem(new ItemStack(Material.COPPER_INGOT, 3), 1, 3, 1)); // Placeholder trade
        sellWhitelist.put(Villager.Profession.MASON, masonSells);


// Weaponsmith
        List<TradeItem> weaponsmithPurchases = new ArrayList<>();
        weaponsmithPurchases.add(new TradeItem(new ItemStack(Material.IRON_INGOT, 1), 4, 1, 1)); // Placeholder trade
        weaponsmithPurchases.add(new TradeItem(new ItemStack(Material.COAL, 4), 2, 4, 1)); // Placeholder trade
        weaponsmithPurchases.add(new TradeItem(commonSwordReforge, 32, 1, 1)); // Placeholder trade

        weaponsmithPurchases.add(new TradeItem(uncommonSwordReforge, 64, 1, 2)); // Placeholder trade


        weaponsmithPurchases.add(new TradeItem(new ItemStack(Material.BELL, 1), 63, 1, 3)); // Placeholder trade
        weaponsmithPurchases.add(new TradeItem(weaponsmithReforge, 64, 1, 3)); // Placeholder trade
        weaponsmithPurchases.add(new TradeItem(weaponsmithReforgeTwo, 128, 1, 3)); // Placeholder trade

        weaponsmithPurchases.add(new TradeItem(rareSwordReforge, 128, 1, 3)); // Placeholder trade

        weaponsmithPurchases.add(new TradeItem(weaponsmithSharpness, 64, 1, 4)); // Placeholder trade
        weaponsmithPurchases.add(new TradeItem(epicSwordReforge, 256, 1, 4)); // Placeholder trade

        weaponsmithPurchases.add(new TradeItem(weaponsmithSweepingEdge, 63, 1, 4)); // Placeholder trade
        weaponsmithPurchases.add(new TradeItem(weaponsmithLooting, 64, 1, 4)); // Placeholder trade
        weaponsmithPurchases.add(new TradeItem(weaponsmithKnockback, 64, 1, 4)); // Placeholder trade
        weaponsmithPurchases.add(new TradeItem(weaponsmithFireAspect, 64, 1, 4)); // Placeholder trade
        weaponsmithPurchases.add(new TradeItem(weaponsmithSmite, 64, 1, 4)); // Placeholder trade
        weaponsmithPurchases.add(new TradeItem(weaponsmithBaneofAnthropods, 64, 1, 4)); // Placeholder trade

        weaponsmithPurchases.add(new TradeItem(weaponsmithEnchant, 64, 1, 5)); // Placeholder trade
        weaponsmithPurchases.add(new TradeItem(legendarySwordReforge, 512, 1, 5)); // Placeholder trade

        purchaseWhitelist.put(Villager.Profession.WEAPONSMITH, weaponsmithPurchases);
        ItemStack undeadDrop = CustomItemManager.createCustomItem(Material.ROTTEN_FLESH, ChatColor.YELLOW +
                "Beating Heart", Arrays.asList(
                ChatColor.GRAY + "An undead heart still beating with undead life.",
                ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Apply it to equipment to unlock the secrets of Smite.",
                ChatColor.DARK_PURPLE + "Smithing Item"
        ), 1,false, true);
        ItemStack creeperDrop = CustomItemManager.createCustomItem(Material.TNT, ChatColor.YELLOW +
                "Hydrogen Bomb", Arrays.asList(
                ChatColor.GRAY + "500 KG of TNT.",
                ChatColor.BLUE + "Use: " + ChatColor.GRAY + "to summon a large quantity of live-fuse TNT.",
                ChatColor.DARK_PURPLE + "Artifact"
        ), 1,false, true);
        ItemStack spiderDrop = CustomItemManager.createCustomItem(Material.SPIDER_EYE, ChatColor.YELLOW +
                "SpiderBane", Arrays.asList(
                ChatColor.GRAY + "A strange substance lethal against spiders.",
                ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Apply it to equipment to unlock the secrets of Bane of Anthropods.",
                ChatColor.DARK_PURPLE + "Smithing Item"
        ), 1,false, true);
        ItemStack enderDrop = CustomItemManager.createCustomItem(Material.ENDER_PEARL, ChatColor.YELLOW +
                "End Pearl", Arrays.asList(
                ChatColor.GRAY + "Something doesn't look normal here...",
                ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Reusable ender pearl.",
                ChatColor.DARK_PURPLE + "Artifact"
        ), 1,false, true);
        ItemStack blazeDrop = CustomItemManager.createCustomItem(Material.FIRE_CHARGE, ChatColor.YELLOW +
                "Fire Ball", Arrays.asList(
                ChatColor.GRAY + "A projectile ball of fire.",
                ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Apply it to equipment to unlock the secrets of Flame.",
                ChatColor.DARK_PURPLE + "Smithing Item"
        ), 1,false, true);
        ItemStack witchDrop = CustomItemManager.createCustomItem(Material.ENCHANTED_BOOK, ChatColor.YELLOW +
                "Mending", Arrays.asList(
                ChatColor.GRAY + "An extremely rare enchantment.",
                ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Apply it to equipment to unlock the secrets of Mending.",
                ChatColor.DARK_PURPLE + "Smithing Item"
        ), 1,false, false);
        ItemStack witherSkeletonDrop = CustomItemManager.createCustomItem(Material.WITHER_SKELETON_SKULL, ChatColor.YELLOW +
                "Wither Skeleton Skull", Arrays.asList(
                ChatColor.GRAY + "A cursed skull.",
                ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Used in spawning the Wither.",
                ChatColor.DARK_PURPLE + "Summoning Item"
        ), 1,false, true);
        ItemStack guardianDrop = CustomItemManager.createCustomItem(Material.PRISMARINE_SHARD, ChatColor.YELLOW +
                "Rain", Arrays.asList(
                ChatColor.GRAY + "A strange object.",
                ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Used in summoning Rain.",
                ChatColor.DARK_PURPLE + "Artifact"
        ), 1,false, true);
        ItemStack elderGuardianDrop = CustomItemManager.createCustomItem(Material.ICE, ChatColor.YELLOW +
                "Frost Heart", Arrays.asList(
                ChatColor.GRAY + "A rare object.",
                ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Apply it to equipment to unlock the secrets of Frost Walker.",
                ChatColor.DARK_PURPLE + "Smithing Item"
        ), 1,false, true);
        ItemStack pillagerDrop = CustomItemManager.createCustomItem(Material.IRON_BLOCK, ChatColor.YELLOW +
                "Iron Golem", Arrays.asList(
                ChatColor.GRAY + "Ancient Summoning Artifact.",
                ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Summon an Iron Golem.",
                ChatColor.DARK_PURPLE + "Summoning Artifact"
        ), 1,false, true);
        ItemStack vindicatorDrop = CustomItemManager.createCustomItem(Material.SLIME_BALL, ChatColor.YELLOW +
                "KB Ball", Arrays.asList(
                ChatColor.GRAY + "An extremely bouncy ball.",
                ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Apply it to equipment to unlock the secrets of Knockback.",
                ChatColor.DARK_PURPLE + "Smithing Item"
        ), 1,false, true);
        ItemStack piglinDrop = CustomItemManager.createCustomItem(Material.ARROW, ChatColor.YELLOW +
                "High Caliber Arrow", Arrays.asList(
                ChatColor.GRAY + "A heavy arrow.",
                ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Apply it to equipment to unlock the secrets of Piercing.",
                ChatColor.DARK_PURPLE + "Smithing Item"
        ), 1,false, true);
        ItemStack piglinBruteDrop = CustomItemManager.createCustomItem(Material.SOUL_SOIL, ChatColor.YELLOW +
                "Grains of Soul", Arrays.asList(
                ChatColor.GRAY + "Soul soil with spirits of speed.",
                ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Apply it to equipment to unlock the secrets of Soul Speed.",
                ChatColor.DARK_PURPLE + "Smithing Item"
        ), 1,false, true);
        ItemStack zombifiedPiglinDrop = CustomItemManager.createCustomItem(Material.GOLD_INGOT, ChatColor.YELLOW +
                "Gold Bar", Arrays.asList(
                ChatColor.GRAY + "High value magnet.",
                ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Apply it to equipment to unlock the secrets of Looting.",
                ChatColor.DARK_PURPLE + "Smithing Item"
        ), 1,false, true);
        ItemStack drownedDrop = CustomItemManager.createCustomItem(Material.LEATHER_BOOTS, ChatColor.YELLOW +
                "Fins", Arrays.asList(
                ChatColor.GRAY + "Water Technology.",
                ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Apply it to equipment to unlock the secrets of Depth Strider.",
                ChatColor.DARK_PURPLE + "Smithing Item"
        ), 1,false, true);
        ItemStack skeletonDrop = CustomItemManager.createCustomItem(Material.BOW, ChatColor.YELLOW +
                "Bowstring", Arrays.asList(
                ChatColor.GRAY + "Air Technology.",
                ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Apply it to equipment to unlock the secrets of Power.",
                ChatColor.DARK_PURPLE + "Smithing Item"
        ), 1,false, true);
        List<TradeItem> weaponsmithSells = new ArrayList<>();
        weaponsmithSells.add(new TradeItem(new ItemStack(Material.IRON_INGOT, 3), 1, 3, 1)); // Placeholder trade
        weaponsmithSells.add(new TradeItem(new ItemStack(Material.GOLD_INGOT, 2), 1, 2, 1)); // Placeholder trade
        weaponsmithSells.add(new TradeItem(new ItemStack(Material.DIAMOND, 1), 8, 1, 1)); // Placeholder trade
        weaponsmithSells.add(new TradeItem(new ItemStack(Material.ZOMBIE_HEAD, 1), 8, 1, 1)); // Placeholder trade
        weaponsmithSells.add(new TradeItem(new ItemStack(Material.SKELETON_SKULL, 1), 8, 1, 1)); // Placeholder trade
        weaponsmithSells.add(new TradeItem(new ItemStack(Material.CREEPER_HEAD, 1), 8, 1, 1)); // Placeholder trade
        weaponsmithSells.add(new TradeItem(singularity(), 8, 1, 1)); // Placeholder trade
        weaponsmithSells.add(new TradeItem(skeletonDrop, 150, 1, 1)); // Placeholder trade
        weaponsmithSells.add(new TradeItem(drownedDrop, 8, 1, 1)); // Placeholder trade
        weaponsmithSells.add(new TradeItem(creeperDrop, 8, 1, 1)); // Placeholder trade
        weaponsmithSells.add(new TradeItem(blazeDrop, 8, 1, 1)); // Placeholder trade
        weaponsmithSells.add(new TradeItem(enderDrop, 24, 1, 1)); // Placeholder trade
        weaponsmithSells.add(new TradeItem(guardianDrop, 8, 1, 1)); // Placeholder trade
        weaponsmithSells.add(new TradeItem(elderGuardianDrop, 4, 1, 1)); // Placeholder trade
        weaponsmithSells.add(new TradeItem(piglinBruteDrop, 8, 1, 1)); // Placeholder trade
        weaponsmithSells.add(new TradeItem(piglinDrop, 8, 1, 1)); // Placeholder trade
        weaponsmithSells.add(new TradeItem(spiderDrop, 100, 1, 1)); // Placeholder trade
        weaponsmithSells.add(new TradeItem(undeadDrop, 100, 1, 1)); // Placeholder trade
        weaponsmithSells.add(new TradeItem(vindicatorDrop, 8, 1, 1)); // Placeholder trade
        weaponsmithSells.add(new TradeItem(witchDrop, 32, 1, 1)); // Placeholder trade
        sellWhitelist.put(Villager.Profession.WEAPONSMITH, weaponsmithSells);



//Fletcher
        List<TradeItem> fletcherPurchases = new ArrayList<>();
        fletcherPurchases.add(new TradeItem(new ItemStack(Material.ARROW, 4), 1, 4, 1)); // Placeholder trade
        fletcherPurchases.add(new TradeItem(new ItemStack(Material.BOW, 1), 4, 1, 1)); // Placeholder trade
        fletcherPurchases.add(new TradeItem(new ItemStack(Material.CROSSBOW, 1), 4, 1, 2)); // Placeholder trade
        fletcherPurchases.add(new TradeItem(new ItemStack(Material.FEATHER, 1), 2, 1, 2)); // Placeholder trade

        fletcherPurchases.add(new TradeItem(new ItemStack(Material.OAK_SAPLING, 4), 2, 4, 3)); // Placeholder trade
        fletcherPurchases.add(new TradeItem(new ItemStack(Material.JUNGLE_SAPLING, 4), 2, 4, 3)); // Placeholder trade
        fletcherPurchases.add(new TradeItem(new ItemStack(Material.DARK_OAK_SAPLING, 4), 2, 4, 3)); // Placeholder trade
        fletcherPurchases.add(new TradeItem(new ItemStack(Material.SPRUCE_SAPLING, 4), 2, 4, 3)); // Placeholder trade
        fletcherPurchases.add(new TradeItem(new ItemStack(Material.BIRCH_SAPLING, 4), 2, 4, 3)); // Placeholder trade
        fletcherPurchases.add(new TradeItem(new ItemStack(Material.ACACIA_SAPLING, 4), 2, 4, 3)); // Placeholder trade
        fletcherPurchases.add(new TradeItem(new ItemStack(Material.CHERRY_SAPLING, 4), 2, 4, 3)); // Placeholder trade

        fletcherPurchases.add(new TradeItem(fletcherBowEnchant, 16, 1, 4)); // Placeholder trade
        fletcherPurchases.add(new TradeItem(fletcherPower, 64, 1, 4)); // Placeholder trade
        fletcherPurchases.add(new TradeItem(fletcherCrossbowEnchant, 64, 1, 5)); // Placeholder trade


        purchaseWhitelist.put(Villager.Profession.FLETCHER, fletcherPurchases);


        List<TradeItem> fletcherSells = new ArrayList<>();
        fletcherSells.add(new TradeItem(new ItemStack(Material.STICK, 64), 1, 64, 1)); // Placeholder trade
        fletcherSells.add(new TradeItem(new ItemStack(Material.FLINT, 2), 1, 2, 1)); // Placeholder trade
        fletcherSells.add(new TradeItem(new ItemStack(Material.STRING, 2), 1, 2, 1)); // Placeholder trade
        fletcherSells.add(new TradeItem(new ItemStack(Material.FEATHER, 2), 1, 2, 1)); // Placeholder trade
        fletcherSells.add(new TradeItem(new ItemStack(Material.ARROW, 16), 1, 16, 1)); // Placeholder trade
        fletcherSells.add(new TradeItem(new ItemStack(secretsOfInfinity), 128, 1, 128)); // Placeholder trade

        sellWhitelist.put(Villager.Profession.FLETCHER, fletcherSells);


// Cartographer
        List<TradeItem> cartographerPurchases = new ArrayList<>();
        cartographerPurchases.add(new TradeItem(cartographerMineshaft, 16, 1, 1)); // Placeholder trade
        cartographerPurchases.add(new TradeItem(cartographerVillage, 16, 1, 1)); // Placeholder trade
        cartographerPurchases.add(new TradeItem(cartographerShipwreck, 16, 1, 1)); // Placeholder trade

        cartographerPurchases.add(new TradeItem(cartographerBuriedTreasure, 16, 1, 2)); // Placeholder trade
        cartographerPurchases.add(new TradeItem(cartographerIgloo, 20, 1, 2)); // Placeholder trade
        cartographerPurchases.add(new TradeItem(cartographerOceanMonument, 20, 1, 2)); // Placeholder trade
        cartographerPurchases.add(new TradeItem(cartographerOceanRuin, 20, 1, 2)); // Placeholder trade

        cartographerPurchases.add(new TradeItem(cartographerDesertPyramid, 32, 1, 3)); // Placeholder trade
        cartographerPurchases.add(new TradeItem(cartographerJunglePyramid, 32, 1, 3)); // Placeholder trade
        cartographerPurchases.add(new TradeItem(cartographerPillagerOutpost, 32, 1, 3)); // Placeholder trade
        cartographerPurchases.add(new TradeItem(cartographerSwampHut, 32, 1, 3)); // Placeholder trade
        cartographerPurchases.add(new TradeItem(cartographerNetherFortress, 32, 1, 3)); // Placeholder trade


        cartographerPurchases.add(new TradeItem(cartographerStronghold, 64, 1, 4)); // Placeholder trade
        cartographerPurchases.add(new TradeItem(cartographerBastionRemnant, 64, 1, 4)); // Placeholder trade

        cartographerPurchases.add(new TradeItem(cartographerWoodlandMansion, 128, 1, 5)); // Placeholder trade
        cartographerPurchases.add(new TradeItem(aspectAOfTheJourney, 128, 1, 5)); // Placeholder trade
        purchaseWhitelist.put(Villager.Profession.CARTOGRAPHER, cartographerPurchases);

        List<TradeItem> cartographerSells = new ArrayList<>();
        cartographerSells.add(new TradeItem(new ItemStack(Material.PAPER, 8), 1, 8, 1)); // Placeholder trade
        cartographerSells.add(new TradeItem(new ItemStack(Material.COMPASS, 1), 3, 1, 1)); // Placeholder trade
        sellWhitelist.put(Villager.Profession.CARTOGRAPHER, cartographerSells);




// Cleric
        List<TradeItem> clericPurchases = new ArrayList<>();
        clericPurchases.add(new TradeItem(new ItemStack(Material.GLASS_BOTTLE, 1), 2, 1, 1)); // Placeholder trade
        clericPurchases.add(new TradeItem(new ItemStack(Material.NETHER_WART, 1), 64, 1, 1));
        clericPurchases.add(new TradeItem(new ItemStack(Material.SUGAR, 1), 2, 1, 1));

        clericPurchases.add(new TradeItem(new ItemStack(Material.RABBIT_FOOT, 1), 12, 1, 2));
        clericPurchases.add(new TradeItem(new ItemStack(Material.GLISTERING_MELON_SLICE, 1), 8, 1, 2));
        clericPurchases.add(new TradeItem(new ItemStack(Material.FERMENTED_SPIDER_EYE, 3), 15, 3, 2)); // Placeholder trade
        clericPurchases.add(new TradeItem(new ItemStack(Material.SPIDER_EYE, 3), 12, 3, 2)); // Placeholder trade

        clericPurchases.add(new TradeItem(new ItemStack(Material.GUNPOWDER, 3), 6, 3, 3)); // Placeholder trade
        clericPurchases.add(new TradeItem(new ItemStack(Material.MAGMA_CREAM, 3), 16, 3, 3)); // Placeholder trade
        clericPurchases.add(new TradeItem(new ItemStack(Material.BLAZE_POWDER, 2), 48, 2, 3)); // Placeholder trade
        clericPurchases.add(new TradeItem(new ItemStack(Material.PHANTOM_MEMBRANE, 2), 8, 2, 3)); // Placeholder trade

        clericPurchases.add(new TradeItem(new ItemStack(Material.GHAST_TEAR, 2), 20, 2, 4)); // Placeholder trade

        clericPurchases.add(new TradeItem(new ItemStack(Material.DRAGON_BREATH, 4), 64, 4, 5)); // Placeholder trade
        clericPurchases.add(new TradeItem(new ItemStack(Material.TURTLE_HELMET, 1), 64, 1, 5)); // Placeholder trade
        clericPurchases.add(new TradeItem(clericEnchant, 64, 1, 5)); // Placeholder trade

        purchaseWhitelist.put(Villager.Profession.CLERIC, clericPurchases);

        List<TradeItem> clericSells = new ArrayList<>();
        clericSells.add(new TradeItem(new ItemStack(Material.ROTTEN_FLESH, 4), 1, 4, 1)); // Placeholder trade
        clericSells.add(new TradeItem(new ItemStack(Material.BONE, 8), 3, 8, 1)); // Placeholder trade
        clericSells.add(new TradeItem(new ItemStack(Material.SPIDER_EYE, 4), 3, 4, 1)); // Placeholder trade
        clericSells.add(new TradeItem(new ItemStack(Material.LAPIS_LAZULI, 8), 1, 8, 2)); // Placeholder trade
        clericSells.add(new TradeItem(new ItemStack(Material.REDSTONE, 8), 1, 8, 2)); // Placeholder trade
        clericSells.add(new TradeItem(new ItemStack(Material.ENDER_PEARL, 2), 8, 2, 3)); // Placeholder trade
        clericSells.add(new TradeItem(new ItemStack(Material.GLOWSTONE, 1), 3, 1, 4)); // Placeholder trade

        sellWhitelist.put(Villager.Profession.CLERIC, clericSells);



// Leatherworker
        List<TradeItem> leatherworkerPurchases = new ArrayList<>();
        {
            leatherworkerPurchases.add(new TradeItem(new ItemStack(Material.LEATHER, 1), 3, 1, 1)); // Placeholder trade
            leatherworkerPurchases.add(new TradeItem(new ItemStack(Material.ITEM_FRAME, 1), 3, 1, 1)); // Placeholder trade
            leatherworkerPurchases.add(new TradeItem(new ItemStack(Material.SHULKER_SHELL), 64, 1, 3)); // Placeholder trade

            leatherworkerPurchases.add(new TradeItem(new ItemStack(Material.BUNDLE, 1), 64, 1, 3)); // Placeholder trade
            leatherworkerPurchases.add(new TradeItem(leatherworkerEnchant, 32, 1, 4)); // Placeholder trade
            leatherworkerPurchases.add(new TradeItem(leatherworkerArtifact, 64, 1, 5)); // Placeholder trade
        }
        purchaseWhitelist.put(Villager.Profession.LEATHERWORKER, leatherworkerPurchases);

        List<TradeItem> leatherworkerSells = new ArrayList<>();
        {
            leatherworkerSells.add(new TradeItem(new ItemStack(Material.SADDLE), 12, 1, 1));
            leatherworkerSells.add(new TradeItem(new ItemStack(Material.LEATHER_BOOTS), 1, 1, 1));
        }
        sellWhitelist.put(Villager.Profession.LEATHERWORKER, leatherworkerSells);



        // Shepherd
        List<TradeItem> shepherdPurchases = new ArrayList<>();
        shepherdPurchases.add(new TradeItem(new ItemStack(Material.PAINTING, 4), 8, 4, 1)); // Placeholder trade

        shepherdPurchases.add(new TradeItem(new ItemStack(Material.WHITE_DYE, 4), 3, 4, 2)); // Placeholder trade
        shepherdPurchases.add(new TradeItem(new ItemStack(Material.GRAY_DYE, 4), 3, 4, 2)); // Placeholder trade
        shepherdPurchases.add(new TradeItem(new ItemStack(Material.BLACK_DYE, 4), 3, 4, 2)); // Placeholder trade
        shepherdPurchases.add(new TradeItem(new ItemStack(Material.CYAN_DYE, 4), 3, 4, 2)); // Placeholder trade
        shepherdPurchases.add(new TradeItem(new ItemStack(Material.LIME_DYE, 4), 3, 4, 2)); // Placeholder trade

        shepherdPurchases.add(new TradeItem(new ItemStack(Material.YELLOW_DYE, 4), 6, 4, 3)); // Placeholder trade
        shepherdPurchases.add(new TradeItem(new ItemStack(Material.ORANGE_DYE, 4), 6, 4, 3)); // Placeholder trade
        shepherdPurchases.add(new TradeItem(new ItemStack(Material.RED_DYE, 4), 6, 4, 3)); // Placeholder trade
        shepherdPurchases.add(new TradeItem(new ItemStack(Material.PINK_DYE, 4), 6, 4, 3)); // Placeholder trade
        shepherdPurchases.add(new TradeItem(new ItemStack(Material.LIGHT_GRAY_DYE, 4), 6, 4, 3)); // Placeholder trade

        shepherdPurchases.add(new TradeItem(new ItemStack(Material.MAGENTA_DYE, 4), 9, 4, 4)); // Placeholder trade
        shepherdPurchases.add(new TradeItem(new ItemStack(Material.PURPLE_DYE, 4), 9, 4, 4)); // Placeholder trade
        shepherdPurchases.add(new TradeItem(new ItemStack(Material.BLUE_DYE, 4), 9, 4, 4)); // Placeholder trade
        shepherdPurchases.add(new TradeItem(new ItemStack(Material.CYAN_DYE, 4), 9, 4, 4)); // Placeholder trade
        shepherdPurchases.add(new TradeItem(new ItemStack(Material.GREEN_DYE, 4), 9, 4, 4)); // Placeholder trade
        shepherdPurchases.add(new TradeItem(new ItemStack(Material.BROWN_DYE, 4), 9, 4, 4)); // Placeholder trade

        shepherdPurchases.add(new TradeItem(new ItemStack(Material.TERRACOTTA, 8), 4, 4, 5)); // Placeholder trade
        shepherdPurchases.add(new TradeItem(new ItemStack(Material.GRAVEL, 8), 4, 4, 5)); // Placeholder trade
        shepherdPurchases.add(new TradeItem(new ItemStack(Material.SAND, 8), 4, 4, 5)); // Placeholder trade
        shepherdPurchases.add(new TradeItem(shepherdArtifact, 16, 8, 5)); // Placeholder trade
        shepherdPurchases.add(new TradeItem(shepherdEnchant, 32, 1, 5)); // Placeholder trade


        purchaseWhitelist.put(Villager.Profession.SHEPHERD, shepherdPurchases);

        List<TradeItem> shepherdSells = new ArrayList<>();
        shepherdSells.add(new TradeItem(new ItemStack(Material.SHEARS, 1), 3, 1, 1)); // Placeholder trade
        shepherdSells.add(new TradeItem(new ItemStack(Material.BLACK_WOOL, 6), 2, 6, 1)); // Placeholder trade
        shepherdSells.add(new TradeItem(new ItemStack(Material.WHITE_WOOL, 16), 1, 16, 1)); // Placeholder trade
        sellWhitelist.put(Villager.Profession.SHEPHERD, shepherdSells);


// Toolsmith
        List<TradeItem> toolsmithPurchases = new ArrayList<>();
        toolsmithPurchases.add(new TradeItem(new ItemStack(Material.FISHING_ROD, 1), 6, 1, 1)); // Placeholder trade
        toolsmithPurchases.add(new TradeItem(new ItemStack(Material.SHEARS, 1), 6, 1, 1)); // Placeholder trade
        toolsmithPurchases.add(new TradeItem(new ItemStack(Material.BUCKET, 1), 8, 1, 1)); // Placeholder trade
        toolsmithPurchases.add(new TradeItem(commonToolReforge, 4, 1, 1)); // Placeholder trade

        toolsmithPurchases.add(new TradeItem(new ItemStack(Material.SHIELD, 1), 10, 1, 2)); // Placeholder trade
        toolsmithPurchases.add(new TradeItem(uncommonToolReforge, 8, 1, 2)); // Placeholder trade



        toolsmithPurchases.add(new TradeItem(toolsmithReforge, 64, 1, 3)); // Placeholder trade
        toolsmithPurchases.add(new TradeItem(rareToolReforge, 16, 1, 3)); // Placeholder trade

        toolsmithPurchases.add(new TradeItem(toolsmithEfficiency, 64, 1, 4)); // Placeholder trade
        toolsmithPurchases.add(new TradeItem(toolsmithUnbreaking, 64, 1, 4)); // Placeholder trade
        toolsmithPurchases.add(new TradeItem(epicToolReforge, 32, 1, 4)); // Placeholder trade

        toolsmithPurchases.add(new TradeItem(new ItemStack(Material.ANCIENT_DEBRIS, 1), 64, 1, 5)); // Placeholder trade
        toolsmithPurchases.add(new TradeItem(toolsmithEnchant, 64, 1, 5)); // Placeholder trade
        toolsmithPurchases.add(new TradeItem(toolsmithEnchantTwo, 128, 1, 5)); // Placeholder trade
        toolsmithPurchases.add(new TradeItem(legendaryToolReforge, 64, 1, 5)); // Placeholder trade


        purchaseWhitelist.put(Villager.Profession.TOOLSMITH, toolsmithPurchases);

        List<TradeItem> toolsmithSells = new ArrayList<>();
        toolsmithSells.add(new TradeItem(new ItemStack(Material.COAL, 3), 1, 3, 1)); // Placeholder trade
        toolsmithSells.add(new TradeItem(new ItemStack(Material.CHARCOAL, 3), 2, 3, 1)); // Placeholder trade
        toolsmithSells.add(new TradeItem(new ItemStack(Material.IRON_INGOT, 3), 1, 3, 1)); // Placeholder trade
        toolsmithSells.add(new TradeItem(new ItemStack(Material.GOLD_INGOT, 3), 2, 3, 1)); // Placeholder trade
        toolsmithSells.add(new TradeItem(new ItemStack(Material.DIAMOND, 1), 6, 1, 1)); // Placeholder trade        toolsmithSells.add(new TradeItem(diamondGemstone(), 128, 1, 3)); // Placeholder trade
        toolsmithSells.add(new TradeItem(lapisGemstone(), 32, 1, 3)); // Placeholder trade
        toolsmithSells.add(new TradeItem(emeraldGemstone(), 64, 1, 3)); // Placeholder trade
        toolsmithSells.add(new TradeItem(redstoneGemstone(), 32, 1, 3)); // Placeholder trade
        toolsmithSells.add(new TradeItem(diamondGemstone(), 64, 1, 3)); // Placeholder trade


        sellWhitelist.put(Villager.Profession.TOOLSMITH, toolsmithSells);

// Armorer
        List<TradeItem> armorerPurchases = new ArrayList<>();
        armorerPurchases.add(new TradeItem(new ItemStack(Material.IRON_ORE, 4), 7, 4, 1)); // Placeholder trade
        armorerPurchases.add(new TradeItem(commonArmorReforge, 8, 1, 1)); // Placeholder trade

        armorerPurchases.add(new TradeItem(new ItemStack(Material.ANVIL, 1), 24, 1, 2)); // Placeholder trade
        armorerPurchases.add(new TradeItem(uncommonArmorReforge, 16, 1, 2)); // Placeholder trade

        armorerPurchases.add(new TradeItem(new ItemStack(Material.GOLD_ORE, 4), 6, 4, 3)); // Placeholder trade
        armorerPurchases.add(new TradeItem(rareArmorReforge, 32, 1, 3)); // Placeholder trade

        armorerPurchases.add(new TradeItem(new ItemStack(Material.NETHERITE_UPGRADE_SMITHING_TEMPLATE, 2), 32, 2, 4)); // Placeholder trade
        armorerPurchases.add(new TradeItem(epicArmorReforge, 64, 1, 4)); // Placeholder trade

        armorerPurchases.add(new TradeItem(randomTrim, 64, 1, 4)); // Placeholder trade
        armorerPurchases.add(new TradeItem(armorSmithProtection, 64, 1, 4)); // Placeholder trade
        armorerPurchases.add(new TradeItem(armorSmithRespiration, 64, 1, 4)); // Placeholder trade
        armorerPurchases.add(new TradeItem(armorSmithThorns, 64, 1, 4)); // Placeholder trade
        armorerPurchases.add(new TradeItem(armorSmithFeatherFalling, 64, 1, 4)); // Placeholder trade

        armorerPurchases.add(new TradeItem(legendaryArmorReforge, 128, 1, 5)); // Placeholder trade
        armorerPurchases.add(new TradeItem(armorerEnchant, 16, 1, 5)); // Placeholder trade
        armorerPurchases.add(new TradeItem(armorsmithReforge, 32, 1, 5)); // Placeholder trade
        armorerPurchases.add(new TradeItem(armorsmithReforgeTwo, 64, 1, 5)); // Placeholder trade
        //armorerPurchases.add(new TradeItem(armorsmithReforgeThree, 64, 1, 5)); // Placeholder trade



        armorerPurchases.add(new TradeItem(new ItemStack(Material.ANCIENT_DEBRIS, 1), 64, 1, 5)); // Placeholder trade
        purchaseWhitelist.put(Villager.Profession.ARMORER, armorerPurchases);

        List<TradeItem> armorerSells = new ArrayList<>();
        armorerSells.add(new TradeItem(new ItemStack(Material.LEATHER_HORSE_ARMOR, 1), 6, 1, 1)); // Placeholder trade
        armorerSells.add(new TradeItem(new ItemStack(Material.IRON_HORSE_ARMOR, 1), 12, 1, 1)); // Placeholder trade
        armorerSells.add(new TradeItem(new ItemStack(Material.GOLDEN_HORSE_ARMOR, 1), 24, 1, 1)); // Placeholder trade
        armorerSells.add(new TradeItem(new ItemStack(Material.DIAMOND_HORSE_ARMOR, 1), 48, 1, 1)); // Placeholder trade

        armorerSells.add(new TradeItem(new ItemStack(Material.IRON_INGOT, 3), 1, 3, 1)); // Placeholder trade
        armorerSells.add(new TradeItem(new ItemStack(Material.GOLD_INGOT, 3), 2, 3, 1)); // Placeholder trade
        armorerSells.add(new TradeItem(new ItemStack(Material.DIAMOND, 1), 6, 1, 1)); // Placeholder trade
        armorerSells.add(new TradeItem(aquaAffinity, 16, 1, 2)); // Placeholder trade
        armorerSells.add(new TradeItem(swiftSneak, 16, 1, 2)); // Placeholder trade

        sellWhitelist.put(Villager.Profession.ARMORER, armorerSells);



        // Librarian
        List<TradeItem> librarianPurchases = new ArrayList<>();
        librarianPurchases.add(new TradeItem(new ItemStack(Material.BOOK, 1), 3, 1, 1)); // Placeholder trade

        librarianPurchases.add(new TradeItem(new ItemStack(Material.BOOKSHELF, 3), 12, 3, 2)); // Placeholder trade
        librarianPurchases.add(new TradeItem(new ItemStack(Material.LANTERN, 3), 4, 3, 2)); // Placeholder trade

        librarianPurchases.add(new TradeItem(new ItemStack(Material.GLASS, 6), 9, 3, 3)); // Placeholder trade
        librarianPurchases.add(new TradeItem(librarianEnchant2, 16, 1, 3)); // Placeholder trade

        librarianPurchases.add(new TradeItem(ironGolem, 16, 1, 4)); // Placeholder trade

        librarianPurchases.add(new TradeItem(librarianEnchant, 32, 1, 5)); // Placeholder trade


        purchaseWhitelist.put(Villager.Profession.LIBRARIAN, librarianPurchases);

        List<TradeItem> librarianSells = new ArrayList<>();
        librarianSells.add(new TradeItem(new ItemStack(Material.NAME_TAG, 1), 8, 1, 3)); // Placeholder trade
        librarianSells.add(new TradeItem(new ItemStack(Material.PAPER, 3), 1, 3, 1)); // Placeholder trade
        librarianSells.add(new TradeItem(new ItemStack(Material.BOOK, 3), 1, 3, 1)); // Placeholder trade
        librarianSells.add(new TradeItem(new ItemStack(Material.ENCHANTED_BOOK, 1), 8, 1, 1)); // Placeholder trade
        librarianSells.add(new TradeItem(new ItemStack(Material.ENCHANTED_GOLDEN_APPLE, 1), 16, 1, 1)); // Placeholder trade
        librarianSells.add(new TradeItem(forbiddenBook, 8, 1, 1)); // Placeholder trade


        sellWhitelist.put(Villager.Profession.LIBRARIAN, librarianSells);
// Fishermans
        List<TradeItem> fishingPurchases = new ArrayList<>();
        {
            fishingPurchases.add(new TradeItem(new ItemStack(Material.FISHING_ROD, 1), 6, 1, 1));
            fishingPurchases.add(new TradeItem(new ItemStack(Material.BUCKET, 1), 8, 1, 2));
            fishingPurchases.add(new TradeItem(new ItemStack(Material.LAPIS_LAZULI, 4),  8, 4, 2));
            fishingPurchases.add(new TradeItem(shallowShell,  16, 1, 2));

            fishingPurchases.add(new TradeItem(shell,  32, 1, 3));
            fishingPurchases.add(new TradeItem(fishermanReforge,  64, 1, 3));
            fishingPurchases.add(new TradeItem(new ItemStack(Material.CAMPFIRE, 2), 12, 2, 3));

            fishingPurchases.add(new TradeItem(deepShell,  64, 1, 4));
            fishingPurchases.add(new TradeItem(fishermanLure,  64, 1, 4));
            fishingPurchases.add(new TradeItem(fishermanLuckoftheSea,  64, 1, 4));

            fishingPurchases.add(new TradeItem(abyssalShell,  64, 1, 5));
            fishingPurchases.add(new TradeItem(abyssalInk,  64, 1, 5));
            fishingPurchases.add(new TradeItem(abyssalVenom,  64, 1, 5));
            fishingPurchases.add(new TradeItem(fisherEnchant,  40, 1, 5));
            fishingPurchases.add(new TradeItem(fishingEnchant,  40, 1, 5));
            //add sea creature drops
        }
        purchaseWhitelist.put(Villager.Profession.FISHERMAN, fishingPurchases);

        List<TradeItem> fishingSells = new ArrayList<>();
        {
            fishingSells.add(new TradeItem(new ItemStack(Material.STRING, 4), 1, 4, 1)); // Placeholder trade
            fishingSells.add(new TradeItem(new ItemStack(Material.INK_SAC, 4), 1, 4, 1)); // Placeholder trade
            fishingSells.add(new TradeItem(new ItemStack(Material.COD, 8), 1, 8, 1)); // Placeholder trade
            fishingSells.add(new TradeItem(new ItemStack(Material.SALMON, 8), 1, 8, 1)); // Placeholder trade
            fishingSells.add(new TradeItem(new ItemStack(Material.PUFFERFISH, 1), 1, 1, 2)); // Placeholder trade
            fishingSells.add(new TradeItem(new ItemStack(Material.TROPICAL_FISH, 1), 1, 1, 2)); // Placeholder trade
            fishingSells.add(new TradeItem(new ItemStack(Material.GLOW_INK_SAC, 4), 1, 4, 2)); // Placeholder trade
            fishingSells.add(new TradeItem(new ItemStack(ItemRegistry.getTooth()), 4, 1, 2)); // Placeholder trade
            fishingSells.add(new TradeItem(new ItemStack(luminescentInk), 8, 1, 2)); // Placeholder trade
            fishingSells.add(new TradeItem(new ItemStack(Material.NAUTILUS_SHELL), 4, 1, 2)); // Placeholder trade
            fishingSells.add(new TradeItem(new ItemStack(Material.HEART_OF_THE_SEA), 16, 1, 2)); // Placeholder trade
            fishingSells.add(new TradeItem(new ItemStack(fishBone), 4, 1, 2)); // Placeholder trade
            fishingSells.add(new TradeItem(new ItemStack(new ItemStack(Material.TRIDENT)), 24, 1, 2)); // Placeholder trade
        }
        sellWhitelist.put(Villager.Profession.FISHERMAN, fishingSells);


        List<TradeItem> butcherPurchases = new ArrayList<>();
        {
            butcherPurchases.add(new TradeItem(new ItemStack(Material.COOKED_MUTTON, 1), 3, 1, 1));
            butcherPurchases.add(new TradeItem(new ItemStack(Material.RABBIT_STEW, 16), 20, 16, 1));
            butcherPurchases.add(new TradeItem(new ItemStack(Material.DRIED_KELP_BLOCK, 1), 7, 1, 1));
            butcherPurchases.add(new TradeItem(new ItemStack(Material.COAL, 3), 3, 1, 3));
            butcherPurchases.add(new TradeItem(new ItemStack(Material.COOKED_CHICKEN, 3), 2, 3, 1));
            butcherPurchases.add(new TradeItem(new ItemStack(Material.COOKED_RABBIT, 3), 2, 3, 2));
            butcherPurchases.add(new TradeItem(new ItemStack(Material.COOKED_BEEF, 3), 5, 3, 3));
            butcherPurchases.add(new TradeItem(new ItemStack(Material.COOKED_PORKCHOP, 3), 5, 3, 3));


            butcherPurchases.add(new TradeItem(new ItemStack(butcherEnchant), 16, 1, 5));
        }
        purchaseWhitelist.put(Villager.Profession.BUTCHER, butcherPurchases);

        List<TradeItem> butcherSells = new ArrayList<>();
        {
            butcherSells.add(new TradeItem(new ItemStack(Material.COAL, 3), 1, 3, 1));
            butcherSells.add(new TradeItem(new ItemStack(Material.CHICKEN, 1), 1, 1, 1));
            butcherSells.add(new TradeItem(new ItemStack(Material.MUTTON, 1), 1, 1, 1));
            butcherSells.add(new TradeItem(new ItemStack(Material.BEEF, 1), 1, 1, 2));
            butcherSells.add(new TradeItem(new ItemStack(Material.PORKCHOP, 1), 1, 1, 2));
            butcherSells.add(new TradeItem(new ItemStack(Material.RABBIT, 1), 2, 1, 3));

        }
        sellWhitelist.put(Villager.Profession.BUTCHER, butcherSells);



        List<TradeItem> farmerPurchases = new ArrayList<>();
        {
            farmerPurchases.add(new TradeItem(new ItemStack(Material.BREAD, 3), 1, 3, 1)); // Level 1 trade
            farmerPurchases.add(new TradeItem(new ItemStack(Material.WHEAT_SEEDS, 12), 3, 12, 1)); // Level 1 trade

            farmerPurchases.add(new TradeItem(new ItemStack(Material.PUMPKIN_SEEDS, 3), 32, 3, 2)); // Level 1 trade
            farmerPurchases.add(new TradeItem(new ItemStack(Material.MELON_SEEDS, 3), 32, 3, 2)); // Level 1 trade
            farmerPurchases.add(new TradeItem(new ItemStack(Material.CAKE, 1), 6, 1, 3)); // Level 2 trade

            farmerPurchases.add(new TradeItem(new ItemStack(Material.WATER_BUCKET, 1), 3, 1, 3)); // Level 1 trade
//            ItemStack wheatGenerator = resourceGeneratorSubsystem.createGenerator(Material.WHEAT, 1, 280);
//            ItemStack potatoGenerator = resourceGeneratorSubsystem.createGenerator(Material.POTATO, 3, 280);
//            ItemStack carrotGenerator = resourceGeneratorSubsystem.createGenerator(Material.CARROT, 2, 280);
//            ItemStack beetrootGenerator = resourceGeneratorSubsystem.createGenerator(Material.BEETROOT, 1, 280);
//            ItemStack sugarcaneGenerator = resourceGeneratorSubsystem.createGenerator(Material.SUGAR_CANE, 2, 280);
//            ItemStack bambooGenerator = resourceGeneratorSubsystem.createGenerator(Material.BAMBOO, 1, 280);
//            ItemStack melonGenerator = resourceGeneratorSubsystem.createGenerator(Material.MELON, 1, 280);
//            ItemStack pumpkinGenerator = resourceGeneratorSubsystem.createGenerator(Material.PUMPKIN, 1, 280);

//            farmerPurchases.add(new TradeItem(wheatGenerator, 64, 1, 3)); // Level 1 trade
//            farmerPurchases.add(new TradeItem(potatoGenerator, 64, 1, 3)); // Level 1 trade
//            farmerPurchases.add(new TradeItem(carrotGenerator, 64, 1, 3)); // Level 1 trade
//            farmerPurchases.add(new TradeItem(beetrootGenerator, 64, 1, 3)); // Level 1 trade
//            farmerPurchases.add(new TradeItem(sugarcaneGenerator, 64, 1, 3)); // Level 1 trade
//            farmerPurchases.add(new TradeItem(bambooGenerator, 64, 1, 3)); // Level 1 trade
//            farmerPurchases.add(new TradeItem(melonGenerator, 64, 1, 3)); // Level 1 trade
//            farmerPurchases.add(new TradeItem(pumpkinGenerator, 64, 1, 3)); // Level 1 trade

            farmerPurchases.add(new TradeItem(new ItemStack(Material.GOLDEN_CARROT, 4), 3, 4, 4)); // Level 2 trade

            farmerPurchases.add(new TradeItem(new ItemStack(Material.SNIFFER_EGG, 1), 64, 1, 5)); // Level 2 trade
            farmerPurchases.add(new TradeItem(new ItemStack(farmerEnchant), 64, 1, 5)); // Level 2 trade
        }
        purchaseWhitelist.put(Villager.Profession.FARMER, farmerPurchases);



        List<TradeItem> farmerSells = new ArrayList<>();
        {
            farmerSells.add(new TradeItem(new ItemStack(Material.WHEAT, 12), 1, 12, 1)); // Level 1 trade
            farmerSells.add(new TradeItem(new ItemStack(Material.CARROT, 12), 1, 12, 1)); // Level 2 trade
            farmerSells.add(new TradeItem(new ItemStack(Material.POTATO, 12), 1, 12, 1)); // Level 2 trade
            farmerSells.add(new TradeItem(new ItemStack(Material.WHEAT_SEEDS, 32), 1, 32, 1)); // Level 2 trade

            farmerSells.add(new TradeItem(new ItemStack(Material.BEETROOT, 4), 1, 4, 2)); // Level 2 trade
            farmerSells.add(new TradeItem(new ItemStack(Material.EGG, 6), 2, 6, 2)); // Level 3 trade
            farmerSells.add(new TradeItem(new ItemStack(Material.SUGAR_CANE, 6), 1, 6, 2)); // Level 3 trade
            farmerSells.add(new TradeItem(new ItemStack(Material.APPLE, 1), 1, 1, 2)); // Level 3 trade

            farmerSells.add(new TradeItem(new ItemStack(Material.MELON_SLICE, 12), 1, 12, 3)); // Level 3 trade
            farmerSells.add(new TradeItem(new ItemStack(Material.PUMPKIN, 8), 1, 8, 3)); // Level 3 trade

            farmerSells.add(new TradeItem(new ItemStack(Material.BROWN_MUSHROOM, 1), 1, 1, 4)); // Level 3 trade
            farmerSells.add(new TradeItem(new ItemStack(Material.RED_MUSHROOM, 1), 1, 1, 4)); // Level 3 trade
        }
        sellWhitelist.put(Villager.Profession.FARMER, farmerSells);




        // Initialize other professions similarly...
    }

    /**
     * Handles the event when a player interacts with a villager.
     * Opens the custom trading GUI.
     */
    @EventHandler
    public void onPlayerInteractVillager(PlayerInteractEntityEvent event) {
        if (event.getRightClicked() instanceof Villager villager) {
            event.setCancelled(true); // Cancel default trading
            Player player = event.getPlayer();

            playerVillagerMap.put(player, villager); // Store the villager in the map with the player
            openVillagerTradeGUI(player, villager);
        }
    }

    /**
     * Opens the custom trading GUI for the player and villager.
     *
     * @param player   The player interacting with the villager.
     * @param villager The villager being interacted with.
     */
    private void openVillagerTradeGUI(Player player, Villager villager) {
        Inventory tradeGUI = Bukkit.createInventory(null, 54, ChatColor.GREEN + "Villager Trading");
        villager = playerVillagerMap.get(player); // Retrieve the stored villager directly

        // Get villager profession and level
        Villager.Profession profession = villager.getProfession();
        int villagerLevel = villager.getVillagerLevel(); // Use built-in method

        // Get the trade items for the villager's profession
        List<TradeItem> purchases = purchaseWhitelist.getOrDefault(profession, Collections.emptyList());
        List<TradeItem> sells = sellWhitelist.getOrDefault(profession, Collections.emptyList());

        // Create the divider item
        ItemStack dividerItem = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta dividerMeta = dividerItem.getItemMeta();
        if (dividerMeta != null) {
            dividerMeta.setDisplayName(ChatColor.DARK_GRAY + " ");
            dividerItem.setItemMeta(dividerMeta);
        }

        // Place the divider between buys and sells (middle column)
        for (int i = 0; i < 6; i++) {
            tradeGUI.setItem(i * 9 + 4, dividerItem);
        }

        // Populate the GUI with purchases (items villager is selling to player)
        int purchaseIndex = 0;
        for (TradeItem tradeItem : purchases) {
            if (villagerLevel >= tradeItem.getRequiredLevel()) {
                ItemStack displayItem = tradeItem.getItem().clone();
                ItemMeta meta = displayItem.getItemMeta();
                if (meta != null) {
                    List<String> lore = new ArrayList<>();
                    lore.add(ChatColor.RED + "Price: " + tradeItem.getEmeraldValue() + " emerald(s)");
                    lore.add(ChatColor.YELLOW + "Click to purchase " + tradeItem.getQuantity() + " item(s)");
                    meta.setLore(lore);
                    displayItem.setItemMeta(meta);
                }
                // Calculate slot index for purchases (columns 0-3)
                int row = purchaseIndex / 4;
                int col = purchaseIndex % 4;
                int slotIndex = row * 9 + col;
                tradeGUI.setItem(slotIndex, displayItem);
                purchaseIndex++;
            } else {
                // Locked trade
                // Calculate slot index for purchases (columns 0-3)
                int row = purchaseIndex / 4;
                int col = purchaseIndex % 4;
                int slotIndex = row * 9 + col;
                tradeGUI.setItem(slotIndex, createLockedTradeItem());
                purchaseIndex++;
            }
        }

        // Populate the GUI with sells (items villager is buying from player)
        int sellIndex = 0;
        for (TradeItem tradeItem : sells) {
            if (villagerLevel >= tradeItem.getRequiredLevel()) {
                ItemStack displayItem = tradeItem.getItem().clone();
                ItemMeta meta = displayItem.getItemMeta();
                if (meta != null) {
                    List<String> lore = new ArrayList<>();
                    lore.add(ChatColor.GREEN + "Earn: " + tradeItem.getEmeraldValue() + " emerald(s)");
                    lore.add(ChatColor.GREEN + "Click to sell " + tradeItem.getQuantity() + " item(s)");
                    meta.setLore(lore);
                    displayItem.setItemMeta(meta);
                }
                // Calculate slot index for sells (columns 5-8)
                int row = sellIndex / 4;
                int col = (sellIndex % 4) + 5;
                int slotIndex = row * 9 + col;
                tradeGUI.setItem(slotIndex, displayItem);
                sellIndex++;
            } else {
                // Locked trade
                // Calculate slot index for sells (columns 5-8)
                int row = sellIndex / 4;
                int col = (sellIndex % 4) + 5;
                int slotIndex = row * 9 + col;
                tradeGUI.setItem(slotIndex, createLockedTradeItem());
                sellIndex++;
            }
        }

        player.openInventory(tradeGUI);
    }

    /**
     * Creates a locked trade item to display in the GUI.
     *
     * @return The locked trade item.
     */
    private ItemStack createLockedTradeItem() {
        ItemStack lockedItem = new ItemStack(Material.BARRIER);
        ItemMeta meta = lockedItem.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.RED + "Locked");
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "This trade is locked.");
            lore.add(ChatColor.GRAY + "Villager needs to level up.");
            meta.setLore(lore);
            lockedItem.setItemMeta(meta);
        }
        return lockedItem;
    }
    private boolean toggleFlag = false;
    /**
     * Handles clicks within the custom trading GUI.
     * Processes purchases and sells.
     */

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Inventory clickedInventory = event.getClickedInventory();
        if (clickedInventory == null) return;
        if (event.getView().getTitle().equals(ChatColor.GREEN + "Villager Trading")) {
            event.setCancelled(true); // Cancel all clicks

            if (event.getCurrentItem() == null) return;
            ItemStack clickedItem = event.getCurrentItem();
            if (clickedItem.getType() == Material.BARRIER || clickedItem.getType() == Material.GRAY_STAINED_GLASS_PANE)
                return; // Locked trade or divider


            if(!toggleFlag) {
                toggleFlag = true;
                int slot = event.getSlot();
                Player player = (Player) event.getWhoClicked();
                Villager villager = playerVillagerMap.get(player); // Retrieve the stored villager directly
                if (villager == null) return;

                Villager.Profession profession = villager.getProfession();
                int villagerLevel = villager.getVillagerLevel(); // Use built-in method

                // Determine if the player is buying or selling based on slot position
                int column = slot % 9;
                if (column <= 3) {
                    // Purchases (columns 0-3)
                    List<TradeItem> purchases = purchaseWhitelist.getOrDefault(profession, Collections.emptyList());
                    int purchaseIndex = (slot / 9) * 4 + column;
                    if (purchaseIndex < purchases.size()) {
                        TradeItem tradeItem = purchases.get(purchaseIndex);
                        if (villagerLevel >= tradeItem.getRequiredLevel()) {
                            // Process purchase

                            processPurchase(player, villager, tradeItem);
                        }
                    }
                } else if (column >= 5) {
                    // Sells (columns 5-8)
                    List<TradeItem> sells = sellWhitelist.getOrDefault(profession, Collections.emptyList());
                    int sellIndex = (slot / 9) * 4 + (column - 5);
                    if (sellIndex < sells.size()) {
                        TradeItem tradeItem = sells.get(sellIndex);
                        if (villagerLevel >= tradeItem.getRequiredLevel()) {
                            // Process sell
                            processSell(player, villager, tradeItem);
                        }
                    }
                }
            }else{
                toggleFlag = false;
            }
            // Do nothing if the clicked slot is the divider (column 4)
        }
    }
    private boolean hasEnoughItems(Inventory inventory, ItemStack targetItem, int quantity) {
        int totalCount = 0;

        for (ItemStack item : inventory.getContents()) {
            if (item == null) continue;

            // Check if the item matches either by custom name or type
            if (isMatchingItem(item, targetItem)) {
                totalCount += item.getAmount();
                if (totalCount >= quantity) return true; // Early exit if enough items are found
            }
        }

        return false;
    }

    private void removeCustomItems(Inventory inventory, ItemStack targetItem, int quantity) {
        int remaining = quantity;

        for (int slot = 0; slot < inventory.getSize(); slot++) {
            ItemStack item = inventory.getItem(slot);
            if (item == null) continue;

            if (isMatchingItem(item, targetItem)) {
                if (remaining <= 0) break;

                int itemAmount = item.getAmount();
                if (itemAmount <= remaining) {
                    inventory.setItem(slot, null); // Remove entire stack
                    remaining -= itemAmount;
                } else {
                    item.setAmount(itemAmount - remaining);
                    remaining = 0;
                }
            }
        }
    }
    private boolean isMatchingItem(ItemStack item, ItemStack targetItem) {
        // Check if both items are of the same type
        if (item.getType() != targetItem.getType()) return false;

        // Retrieve item metadata
        ItemMeta itemMeta = item.getItemMeta();
        ItemMeta targetMeta = targetItem.getItemMeta();

        // If neither item has metadata, default to type matching
        if (itemMeta == null && targetMeta == null) return true;

        // If one has metadata and the other doesn't, they don't match
        if (itemMeta == null || targetMeta == null) return false;

        // Check if both items have a display name and compare
        if (itemMeta.hasDisplayName() && targetMeta.hasDisplayName()) {
            if (!itemMeta.getDisplayName().equals(targetMeta.getDisplayName())) {
                return false;
            }
        } else if (itemMeta.hasDisplayName() || targetMeta.hasDisplayName()) {
            // If one item has a display name but the other doesn't, they don't match
            return false;
        }

        // Compare lore if present
        if (itemMeta.hasLore() && targetMeta.hasLore()) {
            if (!itemMeta.getLore().equals(targetMeta.getLore())) {
                return false;
            }
        } else if (itemMeta.hasLore() || targetMeta.hasLore()) {
            // If one item has lore but the other doesn't, they don't match
            return false;
        }

        // All checks passed; items match
        return true;
    }





    /**
     * Processes a purchase transaction between the player and the villager.
     *
     * @param player    The player buying the item.
     * @param villager  The villager selling the item.
     * @param tradeItem The item being traded.
     */
    public void processPurchase(Player player, Villager villager, TradeItem tradeItem) {
        int emeraldCost = tradeItem.getEmeraldValue();
        int quantity = tradeItem.getQuantity();

        // Check if player has enough emeralds
        if (hasEnoughItems(player.getInventory(), new ItemStack(Material.EMERALD), emeraldCost)) {
            // Get the pet manager instance
            PetManager petManager = PetManager.getInstance(MinecraftNew.getInstance());
            PetManager.Pet activePet = petManager.getActivePet(player);

            // Adjust cost based on HAGGLE perk
            int finalCost = emeraldCost; // Default to full cost
            if (activePet != null && activePet.hasPerk(PetManager.PetPerk.HAGGLE)) {
                int petLevel = activePet.getLevel(); // Assume Pet has a `getLevel()` method
                double maxDiscount = 0.5; // 50% discount at max level
                int maxLevel = 100; // Define the max level of the pet

                // Calculate discount proportionally to pet's level
                double discountFactor = maxDiscount * ((double) petLevel / maxLevel);
                finalCost = (int) Math.max(1, emeraldCost * (1 - discountFactor)); // Ensure minimum cost of 1 emerald

                player.sendMessage(ChatColor.GREEN + "Haggle perk applied! You paid " + finalCost + " emeralds.");
            }

            // Remove emeralds based on the final cost
            removeItems(player.getInventory(), Material.EMERALD, finalCost);

            // Give the item to the player
            ItemStack itemToGive = tradeItem.getItem().clone();
            itemToGive.setAmount(quantity);
            player.getInventory().addItem(itemToGive);

            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.2f);

            // Villager gains experience
            int experience = 1;
            if (activePet != null && activePet.hasPerk(PetManager.PetPerk.PRACTICE)) {
                experience += 3;
            }
            addVillagerExperience(villager, experience);

            // 1/1000 chance to give the player the Villager pet
            if (Math.random() < 0.001) { // 1/1000 chance
                petManager.createPet(
                        player,
                        "Villager",
                        PetManager.Rarity.LEGENDARY,
                        100,
                        Particle.VILLAGER_HAPPY,
                        PetManager.PetPerk.HAGGLE,
                        PetManager.PetPerk.PRACTICE
                );
                player.sendMessage(ChatColor.GOLD + "Congratulations! You have received the Villager pet!");
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_YES, 1.0f, 1.0f);
            }
        } else {
            player.sendMessage(ChatColor.RED + "You don't have enough emeralds.");
        }
    }




    /**
     * Processes a sell transaction between the player and the villager.
     *
     * @param player    The player selling the item.
     * @param villager  The villager buying the item.
     * @param tradeItem The item being traded.
     */
    private void processSell(Player player, Villager villager, TradeItem tradeItem) {
        int emeraldReward = tradeItem.getEmeraldValue();
        int quantity = tradeItem.getQuantity();
        ItemStack tradeItemStack = tradeItem.getItem();

        // Check if the player has enough items
        if (hasEnoughItems(player.getInventory(), tradeItemStack, quantity)) {
            // Remove the required items from the player's inventory
            removeCustomItems(player.getInventory(), tradeItemStack, quantity);

            // Give emeralds to the player
            ItemStack emeralds = new ItemStack(Material.EMERALD, emeraldReward);
            player.getInventory().addItem(emeralds);

            // Villager gains experience
            addVillagerExperience(villager, 1);

            // Play feedback sound
            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.2f);
            player.sendMessage(ChatColor.GREEN + "You sold " + quantity + " items for " + emeraldReward + " emeralds!");
        } else {
            player.sendMessage(ChatColor.RED + "You don't have enough of the required items to sell.");
        }
    }


    /**
     * Removes a specified amount of a material from an inventory.
     *
     * @param inventory The inventory to remove items from.
     * @param material  The material to remove.
     * @param amount    The amount to remove.
     */
    private void removeItems(Inventory inventory, Material material, int amount) {
        int remaining = amount;
        for (ItemStack item : inventory.getContents()) {
            if (item != null && item.getType() == material) {
                int itemAmount = item.getAmount();
                if (itemAmount <= remaining) {
                    inventory.removeItem(item);
                    remaining -= itemAmount;
                } else {
                    item.setAmount(itemAmount - remaining);
                    remaining = 0;
                    break;
                }
            }
        }
        if (remaining > 0) {
            // This should not happen if the initial check passes, but handle just in case
            System.out.println("Error: Could not remove the required amount of items.");
        }
    }



    /**
     * Finds the nearest villager to the player within a certain radius.
     *
     * @param player The player to search around.
     * @return The nearest villager or null if none found.
     */

    private Villager findVillagerNearPlayer(Player player) {
        // Find the nearest villager within a small radius
        List<Entity> nearbyEntities = player.getNearbyEntities(5, 5, 5);
        for (Entity entity : nearbyEntities) {
            if (entity instanceof Villager) {
                return (Villager) entity;
            }
        }
        return null;
    }

    /**
     * Adds experience to a villager and levels them up if necessary.
     *
     * @param villager   The villager to add experience to.
     * @param experience The amount of experience to add.
     */
    private void addVillagerExperience(Villager villager, int experience) {
        int currentXP = villager.getVillagerExperience();
        int newXP = currentXP + experience;
        villager.setVillagerExperience(newXP);

        // Check if the villager can level up
        int villagerLevel = villager.getVillagerLevel();
        int xpForNextLevel = getExperienceForNextLevel(villagerLevel);

        if (newXP >= xpForNextLevel && villagerLevel < MAX_VILLAGER_LEVEL) {
            villagerLevel++;
            villager.setVillagerLevel(villagerLevel);
            villager.setVillagerExperience(0); // Reset experience after leveling up
            villager.getWorld().playSound(villager.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
            // Optionally send message to nearby players
        }
    }

    /**
     * Calculates the experience needed for the next villager level.
     *
     * @param currentLevel The villager's current level.
     * @return The experience required to reach the next level.
     */
    private int getExperienceForNextLevel(int currentLevel) {
        switch (currentLevel) {
            case 1:
                return 10; // Level 1 to 2
            case 2:
                return 70; // Level 2 to 3
            case 3:
                return 150; // Level 3 to 4
            case 4:
                return 250; // Level 4 to 5
            default:
                return Integer.MAX_VALUE; // Max level reached
        }
    }

    /**
     * Optional: Handles inventory close events if needed.
     *
     * @param event The inventory close event.
     */
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getView().getTitle().equals(ChatColor.GREEN + "Nearest Villager Trading")) {
            Player player = (Player) event.getPlayer();
            playerVillagerMap.remove(player); // Remove the villager reference when the GUI is closed
        }
    }

    /**
     * Class representing a trade item with associated values.
     */
    public static class TradeItem {
        private final ItemStack item;
        private final int emeraldValue;
        private final int quantity;
        private final int requiredLevel;

        public TradeItem(ItemStack item, int emeraldValue, int quantity, int requiredLevel) {
            this.item = item;
            this.emeraldValue = emeraldValue;
            this.quantity = quantity;
            this.requiredLevel = requiredLevel;
        }

        public ItemStack getItem() {
            return item;
        }

        public int getEmeraldValue() {
            return emeraldValue;
        }

        public int getQuantity() {
            return quantity;
        }

        public int getRequiredLevel() {
            return requiredLevel;
        }
    }
}
