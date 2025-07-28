package goat.minecraft.minecraftnew.subsystems.villagers;

import goat.minecraft.minecraftnew.MinecraftNew;
import goat.minecraft.minecraftnew.other.additionalfunctionality.CustomBundleGUI;
import goat.minecraft.minecraftnew.other.additionalfunctionality.PlayerTabListUpdater;
import goat.minecraft.minecraftnew.subsystems.culinary.CulinarySubsystem;
import goat.minecraft.minecraftnew.subsystems.pets.PetManager;
import goat.minecraft.minecraftnew.subsystems.pets.PetRegistry;
import goat.minecraft.minecraftnew.subsystems.pets.PetTrait;
import goat.minecraft.minecraftnew.subsystems.villagers.MarketTrendManager;
import goat.minecraft.minecraftnew.subsystems.villagers.VillagerChatLines;
import goat.minecraft.minecraftnew.subsystems.villagers.WorkCycleMessages;
import goat.minecraft.minecraftnew.utils.devtools.ItemRegistry;
import goat.minecraft.minecraftnew.utils.devtools.AFKDetector;
import goat.minecraft.minecraftnew.utils.devtools.VillagerNameRepository;
import goat.minecraft.minecraftnew.utils.devtools.XPManager;
import goat.minecraft.minecraftnew.subsystems.brewing.PotionManager;
import goat.minecraft.minecraftnew.subsystems.brewing.PotionEffectPreferences;
import goat.minecraft.minecraftnew.other.skilltree.Skill;
import goat.minecraft.minecraftnew.other.skilltree.SkillTreeManager;
import goat.minecraft.minecraftnew.other.skilltree.Talent;
import goat.minecraft.minecraftnew.other.trinkets.BankAccountManager;
import goat.minecraft.minecraftnew.other.trinkets.TrinketManager;
import org.bukkit.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class VillagerTradeManager implements Listener {

    private final JavaPlugin plugin;
    private final Map<Player, Villager> playerVillagerMap = new HashMap<>(); // Map to store player-villager interactions


    // Whitelists for trades
    private static VillagerTradeManager instance;

    private final Map<Villager.Profession, List<TradeItem>> purchaseWhitelist = new HashMap<>();
    private final Map<Villager.Profession, List<TradeItem>> sellWhitelist = new HashMap<>();

    // alongside your existing purchaseWhitelist & sellWhitelist:
    private final Map<String, List<TradeItem>> bartenderPurchaseWhitelist = new HashMap<>();
    private final Map<String, List<TradeItem>> bartenderSellWhitelist     = new HashMap<>();

    private final Map<UUID, Map<String, BulkInfo>> bulkDiscounts = new HashMap<>();


    // Max villager level
    private static final int MAX_VILLAGER_LEVEL = 5;

    public VillagerTradeManager(JavaPlugin plugin) {
        this.plugin = plugin;

        // Initialize whitelists from YAML
        initializeWhitelists();

        // Register events
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }
    public static VillagerTradeManager getInstance(JavaPlugin plugin) {
        if (instance == null) {
            instance = new VillagerTradeManager(plugin);
        }
        return instance;
    }
    /**
     * Initializes the purchase and sell whitelists by reading from villagerTrades.yml.
     */
    private void initializeWhitelists() {
        // Define config path
        File configDir = new File(plugin.getDataFolder(), "configs");
        File configFile = new File(configDir, "villagerTrades.yml");

        // Create directory/file if missing
        try {
            if (!configDir.exists() && !configDir.mkdirs()) {
                plugin.getLogger().warning("Failed to create directory: " + configDir.getAbsolutePath());
            }

            if (!configFile.exists()) {
                // Optional: Save a default file from resources or create one
                // If you have a default in resources, use:
                // plugin.saveResource("villagerTrades.yml", false);
                // or create one programmatically:
                createDefaultTradesYAML(configFile);
                plugin.getLogger().info("Created default villagerTrades.yml with sample trades.");
            }
        } catch (Exception e) {
            plugin.getLogger().severe("Error creating configuration file or directory: " + e.getMessage());
            e.printStackTrace();
            return;
        }

        // Load config
        FileConfiguration config = YamlConfiguration.loadConfiguration(configFile);

        // For each profession in the YAML, load purchases and sells
        for (String professionName : config.getKeys(false)) {
            Villager.Profession profession;
            try {
                profession = Villager.Profession.valueOf(professionName.toUpperCase());
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Invalid profession in config: " + professionName);
                continue;
            }

            List<TradeItem> purchases = new ArrayList<>();
            List<TradeItem> sells = new ArrayList<>();

            // Load purchase trades
            List<Map<?, ?>> purchaseList = config.getMapList(professionName + ".purchases");
            for (Map<?, ?> map : purchaseList) {
                try {
                    String itemId = (String) map.get("item");        // Material name or custom ID
                    int quantity = (int) map.get("quantity");
                    int emeralds = (int) map.get("emeralds");
                    int level = (int) map.get("level");
                    ItemStack item = getItemByIdentifier(itemId);
                    if (item == null) {
                        plugin.getLogger().warning("Item not recognized: " + itemId);
                        continue;
                    }
                    // Set the item quantity
                    item.setAmount(quantity);

                    purchases.add(new TradeItem(item, emeralds, quantity, level));
                } catch (Exception e) {
                    plugin.getLogger().warning("Error loading purchase trade for " + professionName + ": " + e.getMessage());
                }
            }

            // Load sell trades
            List<Map<?, ?>> sellList = config.getMapList(professionName + ".sells");
            for (Map<?, ?> map : sellList) {
                try {
                    String itemId = (String) map.get("item");       // Material name or custom ID
                    int quantity = (int) map.get("quantity");
                    int emeralds = (int) map.get("emeralds");
                    int level = (int) map.get("level");

                    ItemStack item = getItemByIdentifier(itemId);
                    if (item == null) {
                        plugin.getLogger().warning("Item not recognized: " + itemId);
                        continue;
                    }
                    item.setAmount(quantity);

                    sells.add(new TradeItem(item, emeralds, quantity, level));
                } catch (Exception e) {
                    plugin.getLogger().warning("Error loading sell trade for " + professionName + ": " + e.getMessage());
                }
            }

            purchaseWhitelist.put(profession, purchases);
            sellWhitelist.put(profession, sells);
        }
    }

    /**
     * Example method that creates a default villagerTrades.yml if none exists.
     * You can pre-populate it with minimal or sample trades.
     */
    private void createDefaultTradesYAML(File configFile) {
        FileConfiguration defaultConfig = new YamlConfiguration();

        // Example for MASON
        List<Map<String, Object>> masonPurchases = new ArrayList<>();
        masonPurchases.add(createTradeMap("BRICKS", 4, 1, 1));    // item, quantity, emeralds, level, experience
        masonPurchases.add(createTradeMap("STONE", 8, 1, 1));    // item, quantity, emeralds, level, experience
        masonPurchases.add(createTradeMap("ANDESITE", 8, 1, 2));
        masonPurchases.add(createTradeMap("DIORITE", 8, 1, 2));
        masonPurchases.add(createTradeMap("GRANITE", 8, 1, 2));
        masonPurchases.add(createTradeMap("TERRACOTTA", 8, 1, 3));
        masonPurchases.add(createTradeMap("QUARTZ_BLOCK", 8, 2, 3));
        masonPurchases.add(createTradeMap("SMOOTH_STONE", 8, 2, 3));
        masonPurchases.add(createTradeMap("SANDSTONE", 8, 2, 3));
        masonPurchases.add(createTradeMap("CUSTOM_ITEM_DISPLAY", 1, 8, 3));    // ItemRegistry.getItemDisplayItem()
        masonPurchases.add(createTradeMap("PRISMARINE", 8, 4, 4));
        masonPurchases.add(createTradeMap("DARK_PRISMARINE", 8, 4, 4));
        masonPurchases.add(createTradeMap("PRISMARINE_BRICKS", 8, 4, 4));
        masonPurchases.add(createTradeMap("NETHER_BRICKS", 8, 4, 4));
        masonPurchases.add(createTradeMap("BLACKSTONE", 8, 4, 4));
        masonPurchases.add(createTradeMap("SOUL_SAND", 8, 4, 4));
        masonPurchases.add(createTradeMap("OBSIDIAN", 4, 12, 4));
        defaultConfig.set("MASON.purchases", masonPurchases);

        List<Map<String, Object>> masonSells = new ArrayList<>();
        masonSells.add(createTradeMap("CLAY", 1, 1, 1));
        masonSells.add(createTradeMap("COBBLESTONE", 16, 1, 1));
        masonSells.add(createTradeMap("COPPER_INGOT", 3, 1, 1));
        masonSells.add(createTradeMap("OBSIDIAN", 1, 1, 1));
        masonSells.add(createTradeMap("COMPACT_STONE", 4, 8, 3)); // ItemRegistry.getRareSwordReforge()
        defaultConfig.set("MASON.sells", masonSells);


// Example for WEAPONSMITH
        List<Map<String, Object>> weaponsmithPurchases = new ArrayList<>();
        weaponsmithPurchases.add(createTradeMap("IRON_INGOT", 1, 4, 1));    // item, quantity, emeralds, level, experience
        weaponsmithPurchases.add(createTradeMap("COAL", 4, 2, 1));


        weaponsmithPurchases.add(createTradeMap("BELL", 1, 63, 3));

        weaponsmithPurchases.add(createTradeMap("WATER_ASPECT_ENCHANT", 1, 128, 3)); // ItemRegistry.getRareSwordReforge()

        weaponsmithPurchases.add(createTradeMap("WEAPONSMITH_SHARPNESS", 1, 64, 4)); // ItemRegistry.getWeaponsmithSharpness()

        weaponsmithPurchases.add(createTradeMap("WEAPONSMITH_SWEEPING_EDGE", 1, 63, 4)); // ItemRegistry.getWeaponsmithSweepingEdge()
        weaponsmithPurchases.add(createTradeMap("WEAPONSMITH_LOOTING", 1, 64, 4)); // ItemRegistry.getWeaponsmithLooting()
        weaponsmithPurchases.add(createTradeMap("WEAPONSMITH_KNOCKBACK", 1, 64, 4)); // ItemRegistry.getWeaponsmithKnockback()
        weaponsmithPurchases.add(createTradeMap("WEAPONSMITH_FIRE_ASPECT", 1, 64, 4)); // ItemRegistry.getWeaponsmithFireAspect()
        weaponsmithPurchases.add(createTradeMap("WEAPONSMITH_SMITE", 1, 64, 4)); // ItemRegistry.getWeaponsmithSmite()
        weaponsmithPurchases.add(createTradeMap("WEAPONSMITH_BANE_OF_ARTHROPODS", 1, 64, 4)); // ItemRegistry.getWeaponsmithBaneofAnthropods()

        weaponsmithPurchases.add(createTradeMap("BLUE_LANTERN", 1, 512, 5)); // Custom Item

        defaultConfig.set("WEAPONSMITH.purchases", weaponsmithPurchases);

        List<Map<String, Object>> weaponsmithSells = new ArrayList<>();
        weaponsmithSells.add(createTradeMap("ZOMBIE_HEAD", 1, 8, 1));
        weaponsmithSells.add(createTradeMap("SKELETON_SKULL", 1, 8, 1));
        weaponsmithSells.add(createTradeMap("CREEPER_HEAD", 1, 8, 1));
        weaponsmithSells.add(createTradeMap("SINGULARITY", 1, 8, 1)); // ItemRegistry.getSingularity()
        weaponsmithSells.add(createTradeMap("SKELETON_DROP", 1, 16, 1)); // skeletonDrop
        weaponsmithSells.add(createTradeMap("DROWNED_DROP", 1, 8, 1)); // drownedDrop
        weaponsmithSells.add(createTradeMap("ENDER_DROP", 1, 24, 1)); // enderDrop
        weaponsmithSells.add(createTradeMap("GUARDIAN_DROP", 1, 8, 1)); // guardianDrop
        weaponsmithSells.add(createTradeMap("MIDAS", 1, 64, 1)); // piglinDrop
        weaponsmithSells.add(createTradeMap("GOLD_BAR", 1, 16, 1)); // piglinDrop
        weaponsmithSells.add(createTradeMap("SPIDER_DROP", 1, 16, 1)); // spiderDrop
        weaponsmithSells.add(createTradeMap("UNDEAD_DROP", 1, 16, 1)); // undeadDrop
        weaponsmithSells.add(createTradeMap("VINDICATOR_DROP", 1, 8, 1)); // vindicatorDrop
        weaponsmithSells.add(createTradeMap("SWEEPING", 1, 32, 1)); // witchDrop
        weaponsmithSells.add(createTradeMap("WEAPONSMITH_SHARPNESS_7", 1, 64, 1)); // witchDrop
        weaponsmithSells.add(createTradeMap("REVENANT", 1, 256, 5));
        weaponsmithSells.add(createTradeMap("DRAUPNIR", 1, 256, 5));
        defaultConfig.set("WEAPONSMITH.sells", weaponsmithSells);

        List<Map<String, Object>> fletcherPurchases = new ArrayList<>();
        fletcherPurchases.add(createTradeMap("ARROW", 4, 1, 1)); // item, quantity, emeralds, level, experience
        fletcherPurchases.add(createTradeMap("BOW", 1, 4, 1));
        fletcherPurchases.add(createTradeMap("OAK_BOW", 1, 16, 1));
        fletcherPurchases.add(createTradeMap("CROSSBOW", 1, 4, 2));
        fletcherPurchases.add(createTradeMap("BIRCH_BOW", 1, 32, 2));
        fletcherPurchases.add(createTradeMap("FEATHER", 1, 2, 2));

        fletcherPurchases.add(createTradeMap("SPRUCE_BOW", 1, 64, 3));
        fletcherPurchases.add(createTradeMap("OAK_SAPLING", 4, 2, 3));
        fletcherPurchases.add(createTradeMap("JUNGLE_SAPLING", 4, 2, 3));
        fletcherPurchases.add(createTradeMap("DARK_OAK_SAPLING", 4, 2, 3));
        fletcherPurchases.add(createTradeMap("SPRUCE_SAPLING", 4, 2, 3));
        fletcherPurchases.add(createTradeMap("BIRCH_SAPLING", 4, 2, 3));
        fletcherPurchases.add(createTradeMap("ACACIA_SAPLING", 4, 2, 3));
        fletcherPurchases.add(createTradeMap("CHERRY_SAPLING", 4, 2, 3));
        fletcherPurchases.add(createTradeMap("SHELF", 4, 16, 1));

        fletcherPurchases.add(createTradeMap("ACACIA_BOW", 1, 128, 4)); // Custom item
        fletcherPurchases.add(createTradeMap("FLETCHER_BOW_ENCHANT", 1, 16, 2)); // Custom item
        fletcherPurchases.add(createTradeMap("FLETCHER_VELOCITY", 1, 16, 2)); // Custom item
        fletcherPurchases.add(createTradeMap("FLETCHER_DEFENESTRATION", 1, 64, 4)); // Custom item
        fletcherPurchases.add(createTradeMap("FLETCHER_POWER", 1, 64, 4)); // Custom item
        fletcherPurchases.add(createTradeMap("DARK_OAK_BOW", 1, 128*2, 5)); // Custom item
        fletcherPurchases.add(createTradeMap("FLETCHER_CROSSBOW_ENCHANT", 1, 32, 5)); // Custom item
        defaultConfig.set("FLETCHER.purchases", fletcherPurchases);

        List<Map<String, Object>> fletcherSells = new ArrayList<>();
        fletcherSells.add(createTradeMap("STICK", 64, 1, 1));
        fletcherSells.add(createTradeMap("FLINT", 8, 1, 1));
        fletcherSells.add(createTradeMap("STRING", 8, 1, 1));
        fletcherSells.add(createTradeMap("FEATHER", 8, 1, 1));
        fletcherSells.add(createTradeMap("ARROW", 16, 1, 1));
        fletcherSells.add(createTradeMap("COMPACT_WOOD", 1, 2, 3));
        fletcherSells.add(createTradeMap("SECRETS_OF_INFINITY", 1, 128, 1)); // Custom item
        fletcherSells.add(createTradeMap("THE_LAW_OF_GRAVITY", 1, 256, 5));

        defaultConfig.set("FLETCHER.sells", fletcherSells);
// Cartographer Purchases
        List<Map<String, Object>> cartographerPurchases = new ArrayList<>();
        //cartographerPurchases.add(createTradeMap("ASPECT_OF_THE_JOURNEY", 1, 512, 1)); // Custom item
        cartographerPurchases.add(createTradeMap("CARTOGRAPHER_MINESHAFT", 1, 16, 1)); // Custom item
        cartographerPurchases.add(createTradeMap("CARTOGRAPHER_VILLAGE", 1, 16, 1)); // Custom item
        cartographerPurchases.add(createTradeMap("CARTOGRAPHER_SHIPWRECK", 1, 16, 1)); // Custom item

        cartographerPurchases.add(createTradeMap("CARTOGRAPHER_BURIED_TREASURE", 1, 16, 2)); // Custom item
        cartographerPurchases.add(createTradeMap("CARTOGRAPHER_IGLOO", 1, 16, 2)); // Custom item
        cartographerPurchases.add(createTradeMap("CARTOGRAPHER_OCEAN_MONUMENT", 1, 20, 2)); // Custom item
        cartographerPurchases.add(createTradeMap("CARTOGRAPHER_OCEAN_RUINS", 1, 20, 2)); // Custom item

        cartographerPurchases.add(createTradeMap("CARTOGRAPHER_DESERT_PYRAMID", 1, 32, 3)); // Custom item
        cartographerPurchases.add(createTradeMap("CARTOGRAPHER_JUNGLE_TEMPLE", 1, 32, 3)); // Custom item
        cartographerPurchases.add(createTradeMap("CARTOGRAPHER_PILLAGER_OUTPOST", 1, 32, 3)); // Custom item
        cartographerPurchases.add(createTradeMap("CARTOGRAPHER_SWAMP_HUT", 1, 32, 3)); // Custom item
        cartographerPurchases.add(createTradeMap("CARTOGRAPHER_NETHER_FORTRESS", 1, 32, 3)); // Custom item

        cartographerPurchases.add(createTradeMap("CARTOGRAPHER_STRONGHOLD", 1, 64, 4)); // Custom item
        cartographerPurchases.add(createTradeMap("CARTOGRAPHER_BASTION_REMNANT", 1, 64, 4)); // Custom item

        cartographerPurchases.add(createTradeMap("CARTOGRAPHER_WOODLAND_MANSION", 1, 128, 5)); // Custom item
        defaultConfig.set("CARTOGRAPHER.purchases", cartographerPurchases);

// Cartographer Sells
        List<Map<String, Object>> cartographerSells = new ArrayList<>();
        cartographerSells.add(createTradeMap("PAPER", 8, 1, 1));
        cartographerSells.add(createTradeMap("COMPASS", 1, 3, 1));
        cartographerSells.add(createTradeMap("WARP_GATE", 1, 64, 4));
        cartographerSells.add(createTradeMap("QUANTUM_PHYSICS", 1, 256, 5));

        defaultConfig.set("CARTOGRAPHER.sells", cartographerSells);
        // Cleric Purchases
        List<Map<String, Object>> clericPurchases = new ArrayList<>();
        clericPurchases.add(createTradeMap("GLASS_BOTTLE", 1, 2, 1)); // Material
        clericPurchases.add(createTradeMap("NETHER_WART", 1, 64, 1)); // Material
        clericPurchases.add(createTradeMap("RECURVE", 1, 16, 2)); // Material
        clericPurchases.add(createTradeMap("STRENGTH", 1, 16, 2)); // Material
        clericPurchases.add(createTradeMap("SWIFT_STEP", 1, 16, 2)); // Material
        clericPurchases.add(createTradeMap("OXYGEN_RECOVERY", 1, 16, 2)); // Material
        clericPurchases.add(createTradeMap("SOVEREIGNTY", 1, 16, 2)); // Material
        clericPurchases.add(createTradeMap("LIQUID_LUCK", 1, 32, 2)); // Material
        clericPurchases.add(createTradeMap("FOUNTAINS", 1, 32, 2)); // Material
        clericPurchases.add(createTradeMap("SOLAR_FURY", 1, 32, 2)); // Material
        clericPurchases.add(createTradeMap("NIGHT_VISION", 1, 32, 2)); // Material
        clericPurchases.add(createTradeMap("METAL_DETECTION", 1, 32, 2)); // Material
        clericPurchases.add(createTradeMap("VACCINATION", 1, 32, 2)); // Custom item
        clericPurchases.add(createTradeMap("CHARISMATIC_BARTERING", 1, 64, 2)); // Material

        clericPurchases.add(createTradeMap("CLERIC_ENCHANT", 1, 64, 3)); // Custom Item
        defaultConfig.set("CLERIC.purchases", clericPurchases);

// Cleric Sells
        List<Map<String, Object>> clericSells = new ArrayList<>();
        clericSells.add(createTradeMap("ROTTEN_FLESH", 8, 1, 1)); // Material
        clericSells.add(createTradeMap("BONE", 8, 1, 1)); // Material
        clericSells.add(createTradeMap("SPIDER_EYE", 4, 2, 1)); // Material
        clericSells.add(createTradeMap("LAPIS_LAZULI", 64, 8, 2)); // Material
        clericSells.add(createTradeMap("REDSTONE", 64, 8, 2)); // Material
        clericSells.add(createTradeMap("ENDER_PEARL", 1, 4, 3)); // Material
        clericSells.add(createTradeMap("GLOWSTONE", 1, 3, 4)); // Material
        clericSells.add(createTradeMap("STRING", 8, 1, 4)); // Material
        clericSells.add(createTradeMap("GUNPOWDER", 4, 1, 4)); // Material
        clericSells.add(createTradeMap("PHANTOM_MEMBRANE", 4, 1, 4)); // Material
        clericSells.add(createTradeMap("TOTEM_OF_UNDYING", 1, 26, 4)); // Material
        // Sell Sunflare relic for brewing Potion of Solar Fury

        defaultConfig.set("CLERIC.sells", clericSells);
// Leatherworker Purchases
        List<Map<String, Object>> leatherworkerPurchases = new ArrayList<>();
        leatherworkerPurchases.add(createTradeMap("LEATHER", 1, 3, 1)); // Material
        leatherworkerPurchases.add(createTradeMap("ITEM_FRAME", 1, 3, 1)); // Material
        leatherworkerPurchases.add(createTradeMap("DIVIDER_TRINKET", 1, 1, 2));
        leatherworkerPurchases.add(createTradeMap("WORKBENCH_TRINKET", 1, 90, 2));
        leatherworkerPurchases.add(createTradeMap("BANK_ACCOUNT_TRINKET", 1, 90, 3));
        leatherworkerPurchases.add(createTradeMap("BLUE_SATCHEL", 1, 90, 3));
        leatherworkerPurchases.add(createTradeMap("BLACK_SATCHEL", 1, 90, 3));
        leatherworkerPurchases.add(createTradeMap("GREEN_SATCHEL", 1, 90, 3));
        leatherworkerPurchases.add(createTradeMap("POUCH_OF_SEEDS", 1, 90, 3));
        leatherworkerPurchases.add(createTradeMap("POUCH_OF_POTIONS", 1, 90, 3));
        leatherworkerPurchases.add(createTradeMap("POUCH_OF_DELIGHTS", 1, 90, 3));
        leatherworkerPurchases.add(createTradeMap("ENCHANTED_CLOCK", 1, 90, 3));
        leatherworkerPurchases.add(createTradeMap("ENCHANTED_HOPPER", 1, 90, 3));
        leatherworkerPurchases.add(createTradeMap("MINING_POUCH", 1, 90, 3));
        leatherworkerPurchases.add(createTradeMap("TRANSFIGURATION_POUCH", 1, 90, 3));
        leatherworkerPurchases.add(createTradeMap("ENCHANTED_LAVA_BUCKET_TRINKET", 1, 90, 3));
        leatherworkerPurchases.add(createTradeMap("ANVIL_TRINKET", 1, 90, 4));
        leatherworkerPurchases.add(createTradeMap("POCKET_PORTAL", 1, 90, 4));


        leatherworkerPurchases.add(createTradeMap("BUNDLE", 1, 64, 3)); // Material
        leatherworkerPurchases.add(createTradeMap("LEATHERWORKER_ENCHANT", 1, 32, 5)); // Custom Item
        leatherworkerPurchases.add(createTradeMap("LEATHERWORKER_ARTIFACT", 1, 512, 1)); // Custom Item
        defaultConfig.set("LEATHERWORKER.purchases", leatherworkerPurchases);

// Leatherworker Sells
        List<Map<String, Object>> leatherworkerSells = new ArrayList<>();
        leatherworkerSells.add(createTradeMap("SADDLE", 1, 12, 1)); // Material
        leatherworkerSells.add(createTradeMap("LEATHER_BOOTS", 1, 1, 1)); // Material

        defaultConfig.set("LEATHERWORKER.sells", leatherworkerSells);
// Shepherd Purchases
        List<Map<String, Object>> shepherdPurchases = new ArrayList<>();
        shepherdPurchases.add(createTradeMap("PAINTING", 4, 8, 1)); // Material

        shepherdPurchases.add(createTradeMap("WHITE_DYE", 4, 3, 2)); // Material
        shepherdPurchases.add(createTradeMap("GRAY_DYE", 4, 3, 2)); // Material
        shepherdPurchases.add(createTradeMap("BLACK_DYE", 4, 3, 2)); // Material
        shepherdPurchases.add(createTradeMap("CYAN_DYE", 4, 3, 2)); // Material
        shepherdPurchases.add(createTradeMap("LIME_DYE", 4, 3, 2)); // Material

        shepherdPurchases.add(createTradeMap("YELLOW_DYE", 4, 6, 3)); // Material
        shepherdPurchases.add(createTradeMap("ORANGE_DYE", 4, 6, 3)); // Material
        shepherdPurchases.add(createTradeMap("RED_DYE", 4, 6, 3)); // Material
        shepherdPurchases.add(createTradeMap("PINK_DYE", 4, 6, 3)); // Material
        shepherdPurchases.add(createTradeMap("LIGHT_GRAY_DYE", 4, 6, 3)); // Material

        shepherdPurchases.add(createTradeMap("MAGENTA_DYE", 4, 9, 4)); // Material
        shepherdPurchases.add(createTradeMap("PURPLE_DYE", 4, 9, 4)); // Material
        shepherdPurchases.add(createTradeMap("BLUE_DYE", 4, 9, 4)); // Material
        shepherdPurchases.add(createTradeMap("CYAN_DYE", 4, 9, 4)); // Material
        shepherdPurchases.add(createTradeMap("GREEN_DYE", 4, 9, 4)); // Material
        shepherdPurchases.add(createTradeMap("BROWN_DYE", 4, 9, 4)); // Material

        shepherdPurchases.add(createTradeMap("TERRACOTTA", 8, 4, 5)); // Material
        shepherdPurchases.add(createTradeMap("GRAVEL", 8, 4, 5)); // Material
        shepherdPurchases.add(createTradeMap("SAND", 8, 4, 5)); // Material
        shepherdPurchases.add(createTradeMap("SHEPHERD_ARTIFACT", 8, 16, 5)); // Custom Item
        shepherdPurchases.add(createTradeMap("SHEPHERD_ENCHANT", 1, 32, 4)); // Custom Item
        defaultConfig.set("SHEPHERD.purchases", shepherdPurchases);

// Shepherd Sells
        List<Map<String, Object>> shepherdSells = new ArrayList<>();
        shepherdSells.add(createTradeMap("SHEARS", 1, 3, 1)); // Material
        shepherdSells.add(createTradeMap("BLACK_WOOL", 6, 2, 1)); // Material
        shepherdSells.add(createTradeMap("WHITE_WOOL", 16, 1, 1)); // Material
        defaultConfig.set("SHEPHERD.sells", shepherdSells);
// Toolsmith Purchases
        List<Map<String, Object>> toolsmithPurchases = new ArrayList<>();
        toolsmithPurchases.add(createTradeMap("IRON_INGOT", 1, 6, 1)); // Material
        toolsmithPurchases.add(createTradeMap("FISHING_ROD", 1, 6, 1)); // Material
        toolsmithPurchases.add(createTradeMap("SHEARS", 1, 6, 1)); // Material
        toolsmithPurchases.add(createTradeMap("BUCKET", 1, 8, 1)); // Material

        toolsmithPurchases.add(createTradeMap("SHIELD", 1, 10, 2)); // Material
        toolsmithPurchases.add(createTradeMap("CAULDRON", 1, 5, 2)); // Material


        toolsmithPurchases.add(createTradeMap("TOOLSMITH_EFFICIENCY", 1, 64, 4)); // Custom Item
        toolsmithPurchases.add(createTradeMap("TOOLSMITH_UNBREAKING", 1, 64, 4)); // Custom Item

        toolsmithPurchases.add(createTradeMap("ANCIENT_DEBRIS", 1, 64, 5)); // Material
        toolsmithPurchases.add(createTradeMap("TOOLSMITH_ENCHANT", 1, 64, 3)); // Custom Item
        toolsmithPurchases.add(createTradeMap("TOOLSMITH_ENCHANT_TWO", 1, 128, 3)); // Custom Item
        toolsmithPurchases.add(createTradeMap("LYNCH_ENCHANT", 1, 64, 4)); // Custom Item
        toolsmithPurchases.add(createTradeMap("UNBREAKABLE_SHEARS", 1, 128, 3)); // Custom Item
        toolsmithPurchases.add(createTradeMap("POWER_CRYSTAL", 1, 512, 5)); // Custom Item
        toolsmithPurchases.add(createTradeMap("COMPOSTER_ENCHANT", 1, 32, 3)); // Custom Item


        defaultConfig.set("TOOLSMITH.purchases", toolsmithPurchases);

// Toolsmith Sells
        List<Map<String, Object>> toolsmithSells = new ArrayList<>();
        toolsmithSells.add(createTradeMap("COAL", 3, 1, 1)); // Material
        toolsmithSells.add(createTradeMap("CHARCOAL", 3, 2, 1)); // Material
        toolsmithSells.add(createTradeMap("IRON_INGOT", 3, 1, 1)); // Material
        toolsmithSells.add(createTradeMap("GOLD_INGOT", 3, 2, 1)); // Material
        toolsmithSells.add(createTradeMap("DIAMOND", 1, 2, 1)); // Material
        toolsmithSells.add(createTradeMap("EVISCERATION", 1, 256, 5));
        toolsmithSells.add(createTradeMap("UNSTOPPABLE_VS_IMMOVABLE", 1, 256, 5));

        defaultConfig.set("TOOLSMITH.sells", toolsmithSells);
// Armorer Purchases
        List<Map<String, Object>> armorerPurchases = new ArrayList<>();
        armorerPurchases.add(createTradeMap("IRON_ORE", 3, 12, 1)); // Material
        armorerPurchases.add(createTradeMap("CONTINGENCY", 1, 64, 1)); // Custom Item

        armorerPurchases.add(createTradeMap("ANVIL", 1, 24, 2)); // Material

        armorerPurchases.add(createTradeMap("GOLD_ORE", 4, 6, 3)); // Material

        armorerPurchases.add(createTradeMap("NETHERITE_UPGRADE_SMITHING_TEMPLATE", 2, 32, 2)); // Material

        armorerPurchases.add(createTradeMap("RANDOM_ARMOR_TRIM", 1, 64, 4)); // Custom Item
        armorerPurchases.add(createTradeMap("ARMOR_SMITH_PROTECTION", 1, 64, 4)); // Custom Item
        armorerPurchases.add(createTradeMap("ARMOR_SMITH_RESPIRATION", 1, 64, 4)); // Custom Item
        armorerPurchases.add(createTradeMap("ARMOR_SMITH_THORNS", 1, 64, 4)); // Custom Item
        armorerPurchases.add(createTradeMap("ARMOR_SMITH_FEATHER_FALLING", 1, 64, 4)); // Custom Item
        armorerPurchases.add(createTradeMap("ARMORER_ENCHANT", 1, 16, 4)); // Custom Item
        armorerPurchases.add(createTradeMap("BLESSING_ARTIFACT", 1, 64, 4)); // Custom Item


        armorerPurchases.add(createTradeMap("ANCIENT_DEBRIS", 1, 64, 5)); // Material

        defaultConfig.set("ARMORER.purchases", armorerPurchases);

// Armorer Sells
        List<Map<String, Object>> armorerSells = new ArrayList<>();
        armorerSells.add(createTradeMap("LEATHER_HORSE_ARMOR", 1, 3, 1)); // Material
        armorerSells.add(createTradeMap("IRON_HORSE_ARMOR", 1, 12, 1)); // Material
        armorerSells.add(createTradeMap("GOLDEN_HORSE_ARMOR", 1, 24, 1)); // Material
        armorerSells.add(createTradeMap("DIAMOND_HORSE_ARMOR", 1, 48, 1)); // Material

        armorerSells.add(createTradeMap("IRON_INGOT", 3, 1, 1)); // Material
        armorerSells.add(createTradeMap("GOLD_INGOT", 3, 2, 1)); // Material
        armorerSells.add(createTradeMap("DIAMOND", 1, 2, 1)); // Material
        armorerSells.add(createTradeMap("AQUA_AFFINITY", 1, 16, 2)); // Custom Item
        armorerSells.add(createTradeMap("SWIFT_SNEAK", 1, 16, 2)); // Custom Item

        defaultConfig.set("ARMORER.sells", armorerSells);
// Librarian Purchases
        List<Map<String, Object>> librarianPurchases = new ArrayList<>();
        librarianPurchases.add(createTradeMap("BOOK", 1, 3, 1)); // Material
        librarianPurchases.add(createTradeMap("BOOKSHELF", 3, 12, 2)); // Material
        librarianPurchases.add(createTradeMap("LANTERN", 3, 4, 2)); // Material
        librarianPurchases.add(createTradeMap("GLASS", 6, 9, 3)); // Material
        librarianPurchases.add(createTradeMap("LIBRARIAN_ENCHANT", 1, 32, 3)); // Custom Item
        librarianPurchases.add(createTradeMap("LIBRARIAN_ENCHANTMENT_TWO", 1, 16, 3)); // Custom Item
        librarianPurchases.add(createTradeMap("IRON_GOLEM", 1, 16, 4)); // Custom Item
        librarianPurchases.add(createTradeMap("FORBIDDEN_BOOK", 1, 8, 2)); // Custom Item


        defaultConfig.set("LIBRARIAN.purchases", librarianPurchases);

// Librarian Sells
        List<Map<String, Object>> librarianSells = new ArrayList<>();
        librarianSells.add(createTradeMap("NAME_TAG", 1, 8, 2)); // Material
        librarianSells.add(createTradeMap("PAPER", 3, 1, 1)); // Material
        librarianSells.add(createTradeMap("BOOK", 3, 1, 1)); // Material
        librarianSells.add(createTradeMap("ENCHANTED_BOOK", 1, 8, 1)); // Material
        librarianSells.add(createTradeMap("ENCHANTED_GOLDEN_APPLE", 1, 16, 1)); // Material

        defaultConfig.set("LIBRARIAN.sells", librarianSells);
// Fisherman Purchases
        List<Map<String, Object>> fishermanPurchases = new ArrayList<>();
        fishermanPurchases.add(createTradeMap("FISHER_ENCHANT", 1, 40, 1)); // Custom Item
        fishermanPurchases.add(createTradeMap("FISHING_ENCHANT", 1, 40, 1)); // Custom Item
        fishermanPurchases.add(createTradeMap("BUCKET", 1, 8, 2)); // Material
        fishermanPurchases.add(createTradeMap("TROPICAL_FISH", 1, 8, 2)); // Material
        fishermanPurchases.add(createTradeMap("LAPIS_LAZULI", 4, 8, 2)); // Material
        fishermanPurchases.add(createTradeMap("SHALLOW_SHELL", 1, 8, 2)); // Custom Item
        fishermanPurchases.add(createTradeMap("SHELL", 1, 12, 3)); // Custom Item
        fishermanPurchases.add(createTradeMap("PEARL_OF_THE_DEEP", 1, 512, 3)); // Custom Item
        fishermanPurchases.add(createTradeMap("CAMPFIRE", 2, 12, 3)); // Material
        fishermanPurchases.add(createTradeMap("DEEP_SHELL", 1, 24, 4)); // Custom Item
        fishermanPurchases.add(createTradeMap("FISHERMAN_LURE", 1, 64, 4)); // Custom Item
        fishermanPurchases.add(createTradeMap("FISHERMAN_LUCK_OF_THE_SEA", 1, 64, 4)); // Custom Item
        fishermanPurchases.add(createTradeMap("CALAMARI", 1, 16, 3)); // Custom Item
        fishermanPurchases.add(createTradeMap("IMPALING", 1, 12, 2)); // Custom Item
        fishermanPurchases.add(createTradeMap("INFERNAL_UNBREAKING", 1, 128, 2)); // Custom Item
        fishermanPurchases.add(createTradeMap("SWIFT_SNEAK", 1, 32, 2)); // Custom Item
        fishermanPurchases.add(createTradeMap("ABYSSAL_SHELL", 1, 32, 5)); // Custom Item

        defaultConfig.set("FISHERMAN.purchases", fishermanPurchases);

// Fisherman Sells
        List<Map<String, Object>> fishermanSells = new ArrayList<>();
        fishermanSells.add(createTradeMap("STRING", 4, 1, 1)); // Material
        fishermanSells.add(createTradeMap("INK_SAC", 4, 1, 1)); // Material
        fishermanSells.add(createTradeMap("COD", 8, 1, 1)); // Material
        fishermanSells.add(createTradeMap("SALMON", 8, 1, 1)); // Material
        fishermanSells.add(createTradeMap("PUFFERFISH", 1, 1, 2)); // Material
        fishermanSells.add(createTradeMap("TROPICAL_FISH", 1, 1, 2)); // Material
        fishermanSells.add(createTradeMap("GLOW_INK_SAC", 4, 1, 2)); // Material
        fishermanSells.add(createTradeMap("TOOTH", 1, 2, 2)); // Custom Item
        fishermanSells.add(createTradeMap("NAUTILUS_SHELL", 1, 4, 2)); // Material
        fishermanSells.add(createTradeMap("HEART_OF_THE_SEA", 1, 16, 2)); // Material
        fishermanSells.add(createTradeMap("FISH_BONE", 1, 1, 2)); // Custom Item
        fishermanSells.add(createTradeMap("TRIDENT", 1, 12, 2)); // Material
        fishermanSells.add(createTradeMap("LIGHTNING_BOLT", 1, 12, 2)); // Material
        fishermanSells.add(createTradeMap("BAIT", 1, 12, 2)); // Material
        fishermanSells.add(createTradeMap("LOYAL_DECLARATION", 1, 12, 2)); // Material
        fishermanSells.add(createTradeMap("ANAKLUSMOS", 1, 12, 2)); // Material
        fishermanSells.add(createTradeMap("SWIM_TRUNKS", 1, 12, 2)); // Material
        fishermanSells.add(createTradeMap("HOWL", 1, 32, 2)); // Material
        fishermanSells.add(createTradeMap("LUCK", 1, 8, 2)); // Custom Item
        fishermanSells.add(createTradeMap("ABYSSAL_INK", 1, 64, 5)); // Custom Item
        defaultConfig.set("FISHERMAN.sells", fishermanSells);
// Butcher Purchases
        List<Map<String, Object>> butcherPurchases = new ArrayList<>();
        butcherPurchases.add(createTradeMap("BUTCHER_ENCHANT", 1, 16, 1)); // Custom Item
        butcherPurchases.add(createTradeMap("BEEF", 1, 1, 1)); // Custom Item
        butcherPurchases.add(createTradeMap("COOKBOOK", 1, 16, 1)); // Custom Item
        butcherPurchases.add(createTradeMap("PORKCHOP", 1, 1, 1)); // Custom Item
        butcherPurchases.add(createTradeMap("MUTTON", 1, 1, 1)); // Custom Item
        butcherPurchases.add(createTradeMap("CHICKEN", 1, 1, 1)); // Custom Item
        butcherPurchases.add(createTradeMap("RABBIT", 1, 1, 1)); // Custom Item
        butcherPurchases.add(createTradeMap("SEA_SALT", 1, 4, 3)); // Custom Item
        butcherPurchases.add(createTradeMap("CALAMARI", 1, 16, 3)); // Custom Item
        butcherPurchases.add(createTradeMap("CHEESE", 1, 8, 3)); // Custom Item
        butcherPurchases.add(createTradeMap("COAL", 4, 15, 5)); // Custom Item


        defaultConfig.set("BUTCHER.purchases", butcherPurchases);

// Butcher Sells
        List<Map<String, Object>> butcherSells = new ArrayList<>();
        butcherSells.add(createTradeMap("COAL", 3, 1, 1)); // Material
        butcherSells.add(createTradeMap("CHICKEN", 3, 1, 1)); // Material
        butcherSells.add(createTradeMap("MUTTON", 3, 1, 1)); // Material
        butcherSells.add(createTradeMap("BEEF", 3, 1, 2)); // Material
        butcherSells.add(createTradeMap("PORKCHOP", 3, 1, 2)); // Material
        butcherSells.add(createTradeMap("RABBIT", 3, 2, 3)); // Material

        defaultConfig.set("BUTCHER.sells", butcherSells);
// Farmer Purchases
        List<Map<String, Object>> farmerPurchases = new ArrayList<>();
        farmerPurchases.add(createTradeMap("BREAD", 3, 1, 1)); // Level 1 trade
        farmerPurchases.add(createTradeMap("WHEAT_SEEDS", 12, 3, 1)); // Level 1 trade
        farmerPurchases.add(createTradeMap("PUMPKIN_SEEDS", 3, 32, 2)); // Level 2 trade
        farmerPurchases.add(createTradeMap("MELON_SEEDS", 3, 32, 2)); // Level 2 trade
        farmerPurchases.add(createTradeMap("FARMER_ENCHANT", 1, 64, 2)); // Custom item
        farmerPurchases.add(createTradeMap("CAKE", 1, 8, 3)); // Level 3 trade
        // Seeder items now obtained via farming milestones
        farmerPurchases.add(createTradeMap("JACK_O_LANTERN", 1, 3, 3));
        farmerPurchases.add(createTradeMap("MILK_BUCKET", 5, 15, 3)); // Level 3 trade
        farmerPurchases.add(createTradeMap("EGG", 12, 12, 3)); // Level 3 trade
        farmerPurchases.add(createTradeMap("GOLDEN_CARROT", 4, 3, 4)); // Level 4 trade
        farmerPurchases.add(createTradeMap("HONEY_BOTTLE", 1, 8, 4)); // Level 4 trade
        farmerPurchases.add(createTradeMap("IRRIGATION", 1, 32, 4)); // Custom item
        farmerPurchases.add(createTradeMap("SNIFFER_EGG", 1, 64, 5)); // Level 5 trade

        defaultConfig.set("FARMER.purchases", farmerPurchases);

// Farmer Sells
        List<Map<String, Object>> farmerSells = new ArrayList<>();
        farmerSells.add(createTradeMap("WHEAT", 24, 1, 1)); // Level 1 trade
        farmerSells.add(createTradeMap("CARROT", 24, 1, 1)); // Level 2 trade
        farmerSells.add(createTradeMap("POTATO", 24, 1, 1)); // Level 2 trade
        farmerSells.add(createTradeMap("WHEAT_SEEDS", 32, 1, 1)); // Level 2 trade
        farmerSells.add(createTradeMap("BEETROOT", 8, 1, 2)); // Level 2 trade
        farmerSells.add(createTradeMap("ORGANIC_SOIL", 4, 2, 2)); // Custom item
        farmerSells.add(createTradeMap("EGG", 6, 1, 2)); // Level 3 trade
        farmerSells.add(createTradeMap("SUGAR_CANE", 6, 1, 2)); // Level 3 trade
        farmerSells.add(createTradeMap("APPLE", 4, 1, 2)); // Level 3 trade
        farmerSells.add(createTradeMap("MELON", 8, 3, 3)); // Level 3 trade
        farmerSells.add(createTradeMap("PUMPKIN", 8, 1, 3)); // Level 3 trade
        farmerSells.add(createTradeMap("BROWN_MUSHROOM", 1, 1, 4)); // Level 4 trade
        farmerSells.add(createTradeMap("RED_MUSHROOM", 1, 1, 4)); // Level 4 trade
        farmerSells.add(createTradeMap("HARVEST_MOON", 1, 256, 5)); // Ultimate enchant item

        defaultConfig.set("FARMER.sells", farmerSells);









        try {
            defaultConfig.save(configFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save default villagerTrades.yml: " + e.getMessage());
        }
    }

    /**
     * Helper to build a single trade in Map format for YAML saving/loading.
     */
    private Map<String, Object> createTradeMap(String item, int quantity, int emeralds, int level) {
        Map<String, Object> map = new HashMap<>();
        map.put("item", item);
        map.put("quantity", quantity);
        map.put("emeralds", emeralds);
        map.put("level", level);
        return map;
    }

    /**
     * Converts a string identifier to an ItemStack.
     * - For normal items, it should match a Material enum.
     * - For custom items, map the string to the corresponding ItemRegistry method.
     */
    private ItemStack getItemByIdentifier(String identifier) {
        // 1) Try standard Material
        try {
            Material mat = Material.valueOf(identifier.toUpperCase());
            return new ItemStack(mat);
        } catch (IllegalArgumentException e) {
            // Not a valid Material, so check for custom items
        }

        // 2) Map custom IDs to ItemRegistry items
        // 2) Map custom IDs to ItemRegistry items
        switch (identifier.toUpperCase()) {

            case "STRENGTH":
                return ItemRegistry.getStrengthPotionRecipePaper();
            case "RECURVE":
                return ItemRegistry.getRecurvePotionRecipePaper();
            case "SWIFT_STEP":
                return ItemRegistry.getSwiftStepPotionRecipePaper();
            case "OXYGEN_RECOVERY":
                return ItemRegistry.getOxygenRecoveryRecipePaper();
            case "SOVEREIGNTY":
                return ItemRegistry.getSovereigntyPotionRecipePaper();
            case "LIQUID_LUCK":
                return ItemRegistry.getLiquidLuckRecipePaper();
            case "FOUNTAINS":
                return ItemRegistry.getFountainsRecipePaper();
            case "SOLAR_FURY":
                return ItemRegistry.getSolarFuryRecipePaper();
            case "NIGHT_VISION":
                return ItemRegistry.getNightVisionRecipePaper();
            case "METAL_DETECTION":
                return ItemRegistry.getMetalDetectionRecipePaper();
            case "CHARISMATIC_BARTERING":
                return ItemRegistry.getCharismaticBarteringRecipePaper();


            case "LOYAL_DECLARATION":
                return ItemRegistry.getLoyaltyContract();
            case "BAIT":
                return ItemRegistry.getBait();
            case "COMPACT_STONE":
                return ItemRegistry.getCompactStone();
            case "SWIM_TRUNKS":
                return ItemRegistry.getSwiftSneak();
            case "FARMER_ENCHANT":
                return ItemRegistry.getFarmerEnchant();
            case "AUTO_COMPOSTER":
                return ItemRegistry.getAutoComposter();
            case "IRRIGATION":
                return ItemRegistry.getIrrigation();
            case "ORGANIC_SOIL":
                return ItemRegistry.getOrganicSoil();
            case "COOKBOOK":
                return ItemRegistry.getCulinaryRecipe();
            case "SHELF":
                return ItemRegistry.getShelfItem();
            case "BUTCHER_ENCHANT":
                return ItemRegistry.getButcherEnchant();
            case "SEA_SALT":
                return ItemRegistry.getSeaSalt();
            case "CALAMARI":
                return ItemRegistry.getCalamari();
            case "SHALLOW_SHELL":
                return ItemRegistry.getShallowShell();
            case "SHELL":
                return ItemRegistry.getShell();
            case "ANAKLUSMOS":
                return ItemRegistry.getRiptide();
            case "FISHERMAN_REFORGE":
                return ItemRegistry.getFishermanReforge();
            case "DEEP_SHELL":
                return ItemRegistry.getDeepShell();
            case "FISHERMAN_LURE":
                return ItemRegistry.getFishermanLure();
            case "FISHERMAN_LUCK_OF_THE_SEA":
                return ItemRegistry.getFishermanLuckoftheSea();
            case "ABYSSAL_SHELL":
                return ItemRegistry.getAbyssalShell();
            case "ABYSSAL_INK":
                return ItemRegistry.getAbyssalInk();
            case "ABYSSAL_VENOM":
                return ItemRegistry.getAbyssalVenom();
            case "PEARL_OF_THE_DEEP":
                return ItemRegistry.getPearlOfTheDeep();
            case "FISHER_ENCHANT":
                return ItemRegistry.getFisherEnchant();
            case "FISHING_ENCHANT":
                return ItemRegistry.getFishingEnchant();
            case "TOOTH":
                return ItemRegistry.getTooth();
            case "LUMINESCENT_INK":
                return ItemRegistry.getLuminescentInk();
            case "FISH_BONE":
                return ItemRegistry.getFishBone();
            case "LIBRARIAN_ENCHANTMENT_TWO":
                return ItemRegistry.getLibrarianEnchantmentTwo();
            case "LIGHTNING_BOLT":
                return ItemRegistry.getChanneling();
            case "IRON_GOLEM":
                return ItemRegistry.getIronGolem();
            case "LIBRARIAN_ENCHANT":
                return ItemRegistry.getLibrarianEnchant();
            case "FORBIDDEN_BOOK":
                return ItemRegistry.getForbiddenBook();
            case "COMMON_ARMOR_REFORGE":
                return ItemRegistry.getCommonArmorReforge();
            case "UNCOMMON_ARMOR_REFORGE":
                return ItemRegistry.getUncommonArmorReforge();
            case "RARE_ARMOR_REFORGE":
                return ItemRegistry.getRareArmorReforge();
            case "EPIC_ARMOR_REFORGE":
                return ItemRegistry.getEpicArmorReforge();
            case "LEGENDARY_ARMOR_REFORGE":
                return ItemRegistry.getLegendaryArmorReforge();
            case "RANDOM_ARMOR_TRIM":
                return ItemRegistry.getRandomArmorTrim();
            case "ARMOR_SMITH_PROTECTION":
                return ItemRegistry.getArmorSmithProtection();
            case "ARMOR_SMITH_RESPIRATION":
                return ItemRegistry.getArmorSmithRespiration();
            case "ARMOR_SMITH_THORNS":
                return ItemRegistry.getArmorSmithThorns();
            case "ARMOR_SMITH_FEATHER_FALLING":
                return ItemRegistry.getArmorSmithFeatherFalling();
            case "ARMORER_ENCHANT":
                return ItemRegistry.getArmorerEnchant();
            case "ARMORSMITH_REFORGE":
                return ItemRegistry.getArmorsmithReforge();
            case "ARMORSMITH_REFORGE_TWO":
                return ItemRegistry.getArmorsmithReforgeTwo();
            case "AQUA_AFFINITY":
                return ItemRegistry.getAquaAffinity();
            case "SWIFT_SNEAK":
                return ItemRegistry.getSwiftSneak();
            case "COMMON_TOOL_REFORGE":
                return ItemRegistry.getCommonToolReforge();
            case "UNCOMMON_TOOL_REFORGE":
                return ItemRegistry.getUncommonToolReforge();
            case "COMPACT_WOOD":
                return ItemRegistry.getCompactWood();
            case "TOOLSMITH_REFORGE":
                return ItemRegistry.getToolsmithReforge();
            case "RARE_TOOL_REFORGE":
                return ItemRegistry.getRareToolReforge();
            case "TOOLSMITH_EFFICIENCY":
                return ItemRegistry.getToolsmithEfficiency();
            case "TOOLSMITH_UNBREAKING":
                return ItemRegistry.getToolsmithUnbreaking();
            case "EPIC_TOOL_REFORGE":
                return ItemRegistry.getEpicToolReforge();
            case "TOOLSMITH_ENCHANT":
                return ItemRegistry.getToolsmithEnchant();
            case "TOOLSMITH_ENCHANT_TWO":
                return ItemRegistry.getToolsmithEnchantTwo();
            case "COMPOSTER_ENCHANT":
                return ItemRegistry.getComposterEnchant();
            case "LYNCH_ENCHANT":
                return ItemRegistry.getLynchEnchant();
            case "LEGENDARY_TOOL_REFORGE":
                return ItemRegistry.getLegendaryToolReforge();
            case "MERIT_ITEM":
                return ItemRegistry.getQualifiedItem();
            case "LAPIS_GEMSTONE":
                return ItemRegistry.getLapisGemstone();
            case "EMERALD_GEMSTONE":
                return ItemRegistry.getEmeraldGemstone();
            case "REDSTONE_GEMSTONE":
                return ItemRegistry.getRedstoneGemstone();
            case "DIAMOND_GEMSTONE":
                return ItemRegistry.getDiamondGemstone();
            case "JACKHAMMER":
                return ItemRegistry.getJackhammer();
            case "UNBREAKABLE_SHEARS":
                return ItemRegistry.getUnbreakableShears();
            case "SHEPHERD_ARTIFACT":
                return ItemRegistry.getShepherdArtifact();
            case "SHEPHERD_ENCHANT":
                return ItemRegistry.getShepherdEnchant();
            case "LEATHERWORKER_ENCHANT":
                return ItemRegistry.getLeatherworkerEnchant();
            case "LEATHERWORKER_ARTIFACT":
                return ItemRegistry.getLeatherworkerArtifact();
            case "BLESSING_ARTIFACT":
                return ItemRegistry.getBlessingArtifact();
            case "WORKBENCH_TRINKET":
                return ItemRegistry.getWorkbenchTrinket();
            case "DIVIDER_TRINKET":
                return ItemRegistry.getDividerTrinket();
            case "BANK_ACCOUNT_TRINKET":
                return ItemRegistry.getBankAccountTrinket();
            case "ANVIL_TRINKET":
                return ItemRegistry.getAnvilTrinket();
            case "POCKET_PORTAL":
                return ItemRegistry.getPocketPortal();
            case "BLUE_SATCHEL":
                return ItemRegistry.getBlueSatchelTrinket();
            case "BLACK_SATCHEL":
                return ItemRegistry.getBlackSatchelTrinket();
            case "GREEN_SATCHEL":
                return ItemRegistry.getGreenSatchelTrinket();
            case "POUCH_OF_SEEDS":
                return ItemRegistry.getSeedPouchTrinket();
            case "POUCH_OF_POTIONS":
                return ItemRegistry.getPotionPouchTrinket();
            case "POUCH_OF_DELIGHTS":
                return ItemRegistry.getCulinaryPouchTrinket();
            case "ENCHANTED_CLOCK":
                return ItemRegistry.getEnchantedClockTrinket();
            case "ENCHANTED_HOPPER":
                return ItemRegistry.getEnchantedHopperTrinket();
            case "MINING_POUCH":
                return ItemRegistry.getMiningPouchTrinket();
            case "TRANSFIGURATION_POUCH":
                return ItemRegistry.getTransfigurationPouchTrinket();
            case "ENCHANTED_LAVA_BUCKET_TRINKET":
                return ItemRegistry.getEnchantedLavaBucketTrinket();
            case "CLERIC_ENCHANT":
                return ItemRegistry.getClericEnchant();
            case "SUNFLARE":
                return ItemRegistry.getSunflare();
            case "STARLIGHT":
                return ItemRegistry.getStarlight();
            case "SHINY_EMERALD":
                return ItemRegistry.getShinyEmerald();
            case "DINOSAUR_BONES":
                return ItemRegistry.getDinosaurBones();
            case "PESTICIDE":
                return ItemRegistry.getPesticide();
            case "VACCINATION":
                return ItemRegistry.getVaccination();
            case "CARTOGRAPHER_MINESHAFT":
                return ItemRegistry.getCartographerMineshaft();
            case "CARTOGRAPHER_VILLAGE":
                return ItemRegistry.getCartographerVillage();
            case "ASPECT_OF_THE_JOURNEY":
                return ItemRegistry.getAspectoftheJourney();
            case "SWEEPING":
                return ItemRegistry.getSweepingEdge();
            case "CARTOGRAPHER_SHIPWRECK":
                return ItemRegistry.getCartographerShipwreck();
            case "CONTINGENCY":
                return ItemRegistry.getPreservation();
            case "CARTOGRAPHER_BURIED_TREASURE":
                return ItemRegistry.getCartographerBuriedTreasure();
            case "CARTOGRAPHER_IGLOO":
                return ItemRegistry.getCartographerIgloo();
            case "CARTOGRAPHER_OCEAN_MONUMENT":
                return ItemRegistry.getCartographerOceanMonument();
            case "CARTOGRAPHER_OCEAN_RUINS":
                return ItemRegistry.getCartographerOceanRuins();
            case "CARTOGRAPHER_DESERT_PYRAMID":
                return ItemRegistry.getCartographerDesertPyramid();
            case "CARTOGRAPHER_JUNGLE_TEMPLE":
                return ItemRegistry.getCartographerJungleTemple();
            case "CARTOGRAPHER_PILLAGER_OUTPOST":
                return ItemRegistry.getCartographerPillagerOutpost();
            case "CARTOGRAPHER_SWAMP_HUT":
                return ItemRegistry.getCartographerSwampHut();
            case "CARTOGRAPHER_NETHER_FORTRESS":
                return ItemRegistry.getCartographerNetherFortress();
            case "CARTOGRAPHER_STRONGHOLD":
                return ItemRegistry.getCartographerStronghold();
            case "CARTOGRAPHER_BASTION_REMNANT":
                return ItemRegistry.getCartographerBastionRemnant();
            case "CARTOGRAPHER_WOODLAND_MANSION":
                return ItemRegistry.getCartographerWoodlandMansion();
            case "POWER_CRYSTAL":
                return ItemRegistry.getPowerCrystal();
            case "FLETCHER_BOW_ENCHANT":
                return ItemRegistry.getFletcherBowEnchant();
            case "FLETCHER_VELOCITY":
                return ItemRegistry.getFletcherVelocityEnchant();
            case "FLETCHER_DEFENESTRATION":
                return ItemRegistry.getFletcherDefenestrationEnchant();
            case "FLETCHER_POWER":
                return ItemRegistry.getFletcherPower();
            case "FLETCHER_CROSSBOW_ENCHANT":
                return ItemRegistry.getFletcherCrossbowEnchant();
            case "SECRETS_OF_INFINITY":
                return ItemRegistry.getSecretsOfInfinity();
            case "MIDAS":
                return ItemRegistry.getInfernalLooting();
            case "GOLD_BAR":
                return ItemRegistry.getZombifiedPiglinDrop();
            case "CUSTOM_ITEM_DISPLAY":
                return ItemRegistry.getItemDisplayItem();
            case "COMMON_SWORD_REFORGE":
                return ItemRegistry.getCommonSwordReforge();
            case "UNCOMMON_SWORD_REFORGE":
                return ItemRegistry.getUncommonSwordReforge();
            case "CHEESE":
                return CulinarySubsystem.getInstance(plugin).getRecipeItemByName("Slice of Cheese");
            case "WEAPONSMITH_REFORGE":
                return ItemRegistry.getWeaponsmithReforge();
            case "WEAPONSMITH_SHARPNESS_7":
                return ItemRegistry.getInfernalSharpness();
            case "WEAPONSMITH_REFORGE_TWO":
                return ItemRegistry.getWeaponsmithReforgeTwo();
            case "HOWL":
                return ItemRegistry.getInfernalLure();
            case "RARE_SWORD_REFORGE":
                return ItemRegistry.getRareSwordReforge();
            case "WEAPONSMITH_SHARPNESS":
                return ItemRegistry.getWeaponsmithSharpness();
            case "EPIC_SWORD_REFORGE":
                return ItemRegistry.getEpicSwordReforge();
            case "WEAPONSMITH_SWEEPING_EDGE":
                return ItemRegistry.getWeaponsmithSweepingEdge();
            case "OAK_BOW":
                return ItemRegistry.getOakBowFrameUpgrade();
            case "BIRCH_BOW":
                return ItemRegistry.getBirchBowFrameUpgrade();
            case "SPRUCE_BOW":
                return ItemRegistry.getSpruceBowFrameUpgrade();
            case "ACACIA_BOW":
                return ItemRegistry.getAcaciaBowFrameUpgrade();
            case "DARK_OAK_BOW":
                return ItemRegistry.getDarkOakBowFrameUpgrade();
            case "WEAPONSMITH_LOOTING":
                return ItemRegistry.getWeaponsmithLooting();
            case "WEAPONSMITH_KNOCKBACK":
                return ItemRegistry.getWeaponsmithKnockback();
            case "WEAPONSMITH_FIRE_ASPECT":
                return ItemRegistry.getWeaponsmithFireAspect();
            case "CURSED_CLOCK":
                return ItemRegistry.getCursedClock();
            case "WEAPONSMITH_SMITE":
                return ItemRegistry.getWeaponsmithSmite();
            case "WEAPONSMITH_BANE_OF_ARTHROPODS":
                return ItemRegistry.getWeaponsmithBaneofAnthropods();
            case "LEGENDARY_SWORD_REFORGE":
                return ItemRegistry.getLegendarySwordReforge();
            case "BLUE_LANTERN":
                return ItemRegistry.getBlueLantern();
            case "SINGULARITY":
                return ItemRegistry.getSingularity();
            case "SKELETON_DROP":
                return ItemRegistry.getSkeletonDrop();
            case "DROWNED_DROP":
                return ItemRegistry.getDrownedDrop();
            case "CREEPER_DROP":
                return ItemRegistry.getCreeperDrop();
            case "BLAZE_DROP":
                return ItemRegistry.getBlazeDrop();
            case "ENDER_DROP":
                return ItemRegistry.getEnderDrop();
            case "GUARDIAN_DROP":
                return ItemRegistry.getGuardianDrop();
            case "WATER_ASPECT_ENCHANT":
                return ItemRegistry.getWaterAspectEnchant();
            case "ELDER_GUARDIAN_DROP":
                return ItemRegistry.getElderGuardianDrop();
            case "PIGLIN_DROP":
                return ItemRegistry.getPiglinDrop();
            case "PIGLIN_BRUTE_DROP":
                return ItemRegistry.getPiglinBruteDrop();
            case "SPIDER_DROP":
                return ItemRegistry.getSpiderDrop();
            case "UNDEAD_DROP":
                return ItemRegistry.getUndeadDrop();
            case "VINDICATOR_DROP":
                return ItemRegistry.getVindicatorDrop();
            case "WITCH_DROP":
                return ItemRegistry.getWitchDrop();
            case "VERDANT_RELIC_TREASURY":
                return ItemRegistry.getVerdantRelicTreasury();
            case "RESPIRATION":
                return ItemRegistry.getRespiration();
            case "LEVIATHAN_HEART":
                return ItemRegistry.getLeviathanHeart();
            case "IMPALING":
                return ItemRegistry.getImpaling();
            case "LUCK":
                return ItemRegistry.getLuck();
            case "INFERNAL_UNBREAKING":
                return ItemRegistry.getInfernalUnbreaking();
            case "INFERNAL_SHARPNESS":
                return ItemRegistry.getInfernalSharpness();
            case "WARP_GATE":
                return ItemRegistry.getWarpGate();
            case "THE_LAW_OF_GRAVITY":
                return ItemRegistry.getLawOfGravity();
            case "UNSTOPPABLE_VS_IMMOVABLE":
                return ItemRegistry.getUnstoppableVsImmovable();
            case "DRAUPNIR":
                return ItemRegistry.getDraupnir();
            case "QUANTUM_PHYSICS":
                return ItemRegistry.getQuantumPhysics();
            case "EVISCERATION":
                return ItemRegistry.getEvisceration();
            case "REVENANT":
                return ItemRegistry.getRevenant();
            case "HARVEST_MOON":
                return ItemRegistry.getHarvestMoon();
            case "FERTILIZER":
                return ItemRegistry.getFertilizer();
            default:
                plugin.getLogger().warning("Unknown custom item ID: " + identifier);
                return null;
        }
    }

    /* -----------------------------------------------------------------------
       The rest of your original code remains largely unchanged.
       ----------------------------------------------------------------------- */

    @EventHandler
    public void onPlayerInteractVillager(PlayerInteractEntityEvent event) {
        if (event.getRightClicked() instanceof Villager villager) {
            event.setCancelled(true); // Cancel default trading
            Player player = event.getPlayer();
            VillagerWorkCycleManager villagerWorkCycleManager = VillagerWorkCycleManager.getInstance(MinecraftNew.getInstance());
            if(!(villagerWorkCycleManager.isEligibleForWorkCycle(villager))){
                if(player.isSneaking()){
                    return;
                }
                player.sendMessage("If you had 4 emeralds and crouched, you could hire me to work at your base!");
                return;
            }
            playerVillagerMap.put(player, villager); // Store the villager in the map with the player
            if(!(villager.getProfession() == Villager.Profession.NONE)) {
                if(villager.getCustomName().equals(ChatColor.GOLD + "Bartender")){
                    return;
                }
                openVillagerTradeGUI(player);
            }


        }
    }
    public int calculateDiscountedPrice(Player player, int basePrice, ItemStack item) {
        PetManager petManager = PetManager.getInstance(MinecraftNew.getInstance());
        PetManager.Pet activePet = petManager.getActivePet(player);

        double finalCost = basePrice;

        // Apply pet Haggle perk
        if (activePet != null && activePet.hasPerk(PetManager.PetPerk.HAGGLE)) {
            int petLevel = activePet.getLevel();
            double discountFactor;
            if (petLevel >= 100) {
                discountFactor = 0.25;
            } else if (petLevel >= 75) {
                discountFactor = 0.20;
            } else if (petLevel >= 50) {
                discountFactor = 0.15;
            } else if (petLevel >= 25) {
                discountFactor = 0.10;
            } else if (petLevel >= 1) {
                discountFactor = 0.05;
            } else {
                discountFactor = 0.0;
            }
            finalCost *= (1 - discountFactor);
        }

        SkillTreeManager manager = SkillTreeManager.getInstance();
        if (manager != null) {
            double discount = 0.0;
            discount += manager.getTalentLevel(player.getUniqueId(), Skill.BARTERING, Talent.HAGGLER_I) * 0.005;
            discount += manager.getTalentLevel(player.getUniqueId(), Skill.BARTERING, Talent.HAGGLER_II) * 0.01;
            discount += manager.getTalentLevel(player.getUniqueId(), Skill.BARTERING, Talent.HAGGLER_III) * 0.015;
            discount += manager.getTalentLevel(player.getUniqueId(), Skill.BARTERING, Talent.HAGGLER_IV) * 0.02;
            discount += manager.getTalentLevel(player.getUniqueId(), Skill.BARTERING, Talent.HAGGLER_V) * 0.025;
            discount += manager.getTalentLevel(player.getUniqueId(), Skill.BARTERING, Talent.BILLIONAIRE_DISCOUNT) * 0.05;
            finalCost *= (1 - discount);

            // Bulk discount if applicable
            Map<String, BulkInfo> active = bulkDiscounts.get(player.getUniqueId());
            if (active != null) {
                String key = getItemKey(item);
                BulkInfo info = active.get(key);
                if (info != null && info.expireTime > System.currentTimeMillis()) {
                    finalCost *= (1 - info.discount);
                } else if (info != null) {
                    active.remove(key);
                }
            }
        }
        if ( activePet != null && activePet.getTrait() == PetTrait.FINANCIAL ) {
            double rawPct    = activePet.getTrait()
                    .getValueForRarity(activePet.getTraitRarity()); // e.g. 8
            double discount = rawPct / 100.0;                   // 0.08
            finalCost *= (1 - discount);                       // 92% of original
        }
        // No discount from Bartering level

        if (PotionManager.isActive("Potion of Charismatic Bartering", player)
                && PotionEffectPreferences.isEnabled(player, "Potion of Charismatic Bartering")) {
            double discount = 0.20;
            finalCost *= (1 - discount);
        }

        // Apply global market trend
        MarketTrendManager trendManager = MarketTrendManager.getInstance();
        if (trendManager != null) {
            finalCost *= trendManager.getTrendMultiplier();
        }

        return Math.max(1, (int) Math.floor(finalCost));
    }

    /**
     * Opens the villager trade menu using a temporary villager instance.
     * The spawned villager is removed once the menu closes.
     */
    public void openTradeMenu(Player player, Villager.Profession profession, int tier) {
        int level = Math.max(1, Math.min(tier, MAX_VILLAGER_LEVEL));
        Villager temp = player.getWorld().spawn(player.getLocation(), Villager.class);
        temp.setAI(false);
        temp.setInvisible(true);
        temp.setProfession(profession);
        temp.setVillagerLevel(level);
        temp.setMetadata("tempTradeVillager", new FixedMetadataValue(plugin, true));
        playerVillagerMap.put(player, temp);
        openVillagerTradeGUI(player);
    }






    private void openVillagerTradeGUI(Player player) {
        Inventory tradeGUI = Bukkit.createInventory(null, 54, ChatColor.GREEN + "Villager Trading");
        Villager villager = playerVillagerMap.get(player); // Retrieve the stored villager directly

        // Get villager profession and player's days played
        Villager.Profession profession = villager.getProfession();
        PlayerTabListUpdater playerTabListUpdater = new PlayerTabListUpdater(MinecraftNew.getInstance(), new XPManager(MinecraftNew.getInstance()));
        int daysPlayed = playerTabListUpdater.getDaysPlayed(player);
        SkillTreeManager mgr = SkillTreeManager.getInstance();
        int offset = 0;
        if (mgr != null) {
            offset = mgr.getTalentLevel(player.getUniqueId(), Skill.BARTERING, Talent.CORPORATE_BENEFITS) * 5;
        }

        // Determine villager level based on days played:
        // day 20 = level 1, day 40 = level 2, day 60 = level 3, day 80 = level 4, day 100 = level 5
        int villagerLevel;
        if (daysPlayed >= 160 - offset) {
            villagerLevel = 5;
            villager.setVillagerLevel(5);
        } else if (daysPlayed >= 120 - offset) {
            villagerLevel = 4;
            villager.setVillagerLevel(4);
        } else if (daysPlayed >= 80 - offset) {
            villagerLevel = 3;
            villager.setVillagerLevel(3);
        } else if (daysPlayed >= 40 - offset) {
            villagerLevel = 2;
            villager.setVillagerLevel(2);
        } else {
            villagerLevel = 1; // Below day 20, no level or level 0.
            villager.setVillagerLevel(1);
        }
        //make villagers invisible when trading


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

        // Populate the GUI with purchases (items villager is selling)
        int purchaseIndex = 0;
        for (TradeItem tradeItem : purchases) {
            if (villagerLevel >= tradeItem.getRequiredLevel()) {
                ItemStack displayItem = tradeItem.getItem().clone();
                ItemMeta meta = displayItem.getItemMeta();
                if (meta != null) {
                    List<String> lore = new ArrayList<>();

                    // Calculate prices
                    int basePrice = tradeItem.getEmeraldValue();
                    int finalPrice = calculateDiscountedPrice(player, basePrice, tradeItem.getItem());

                    lore.add(ChatColor.RED + "Original Price: " + basePrice + " emerald(s)");
                    lore.add(ChatColor.GREEN + "Discounted Price: " + finalPrice + " emerald(s)");
                    lore.add(ChatColor.YELLOW + "Click to purchase " + tradeItem.getQuantity() + " item(s)");
                    meta.setLore(lore);
                    displayItem.setItemMeta(meta);
                }
                int row = purchaseIndex / 4;
                int col = purchaseIndex % 4;
                int slotIndex = row * 9 + col;
                tradeGUI.setItem(slotIndex, displayItem);
                purchaseIndex++;
            } else {
                // Locked trade
                int row = purchaseIndex / 4;
                int col = purchaseIndex % 4;
                int slotIndex = row * 9 + col;
                tradeGUI.setItem(slotIndex, createLockedTradeItem());
                purchaseIndex++;
            }
        }

        // Populate the GUI with sells (items villager is buying)
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
                int row = sellIndex / 4;
                int col = (sellIndex % 4) + 5;
                int slotIndex = row * 9 + col;
                tradeGUI.setItem(slotIndex, displayItem);
                sellIndex++;
            } else {
                // Locked trade
                int row = sellIndex / 4;
                int col = (sellIndex % 4) + 5;
                int slotIndex = row * 9 + col;
                tradeGUI.setItem(slotIndex, createLockedTradeItem());
                sellIndex++;
            }
        }

        // Add Villager Pet Button (slot 49)
        addVillagerPetButton(player, tradeGUI);

        player.openInventory(tradeGUI);

        String rawName = ChatColor.stripColor(villager.getCustomName() == null ? "Villager" : villager.getCustomName());
        boolean isTroll = VillagerNameRepository.isTrollName(rawName);
        player.sendMessage(ChatColor.GRAY + "<" + rawName + "> " +
                VillagerChatLines.getOpenLine(rawName, isTroll));
        if (Math.random() < 0.10) {
            String req = WorkCycleMessages.getRequirement(profession);
            if (req != null) {
                player.sendMessage(ChatColor.GRAY + "<" + rawName + "> " + req);
            }
        }
    }


    /**
     * Adds a Villager Pet Button at the bottom-middle of the GUI (slot 49).
     * If the player has a Villager pet, show a simple "Summon Villager Pet" button.
     * Otherwise, show a locked (gray pane) button.
     */
    private void addVillagerPetButton(Player player, Inventory tradeGUI) {
        PetManager petManager = PetManager.getInstance(MinecraftNew.getInstance());
        PetManager.Pet villagerPet = petManager.getPet(player, "Villager");

        if (villagerPet != null) {
            // Player owns a Villager pet
            ItemStack petButton = new ItemStack(Material.VILLAGER_SPAWN_EGG);
            ItemMeta meta = petButton.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(ChatColor.GOLD + "Summon Villager Pet");
                List<String> lore = new ArrayList<>();
                lore.add(ChatColor.YELLOW + "Click to summon your Villager pet.");
                meta.setLore(lore);
                petButton.setItemMeta(meta);
            }
            tradeGUI.setItem(49, petButton);
        } else {
            // Player does not own a Villager pet
            ItemStack lockedButton = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
            ItemMeta meta = lockedButton.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(ChatColor.RED + "No Villager Pet Found");
                lockedButton.setItemMeta(meta);
            }
            tradeGUI.setItem(49, lockedButton);
        }
    }

    /**
     * Handles clicks in the "Villager Trading" GUI.
     * Checks if the player clicked the bottom-middle slot (49) and summons the Villager pet.
     */
    @EventHandler
    public void onVillagerPetButtonClick(InventoryClickEvent event) {
        // Check if it's our Villager Trading GUI and the correct slot
        if (!event.getView().getTitle().equals(ChatColor.GREEN + "Villager Trading")) return;
        if (event.getSlot() != 49) return;

        event.setCancelled(true);

        // Avoid double-click spam


            Player player = (Player) event.getWhoClicked();
            PetManager petManager = PetManager.getInstance(MinecraftNew.getInstance());
            PetManager.Pet villagerPet = petManager.getPet(player, "Villager");

            // If the slot is a pane or player doesn't have a Villager pet, do nothing
            if (event.getCurrentItem() == null
                    || event.getCurrentItem().getType() == Material.GRAY_STAINED_GLASS_PANE
                    || villagerPet == null) {

                return;
            }

            // Remember the old pet in metadata
            PetManager.Pet oldPet = petManager.getActivePet(player);
            player.setMetadata("previousPet", new FixedMetadataValue(plugin,
                    oldPet != null ? oldPet.getName() : null));

            // Replace the Villager button with a Gray Stained Glass Pane
            // to prevent clicking it again while the GUI is still open
            ItemStack grayPane = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
            ItemMeta meta = grayPane.getItemMeta();
            meta.setDisplayName(ChatColor.GRAY + "Villager Pet Active");
            grayPane.setItemMeta(meta);
            event.getInventory().setItem(49, grayPane);

            // Summon the Villager pet
            petManager.summonPet(player, "Villager");
            player.sendMessage(ChatColor.GREEN + "Villager Pet summoned while trading!");
            openVillagerTradeGUI(player);
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        // Check if it's our Villager Trading GUI
        if (!event.getView().getTitle().equals(ChatColor.GREEN + "Villager Trading")) {
            return;
        }

        Player player = (Player) event.getPlayer();

        // Delay the logic by 1 second (20 ticks)
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            // Only run if the player is still not in the Villager Trading GUI
            if (!player.getOpenInventory().getTitle().equals(ChatColor.GREEN + "Villager Trading")) {

                // Retrieve stored old-pet name from metadata
                if (player.hasMetadata("previousPet")) {
                    String previousPetName = player.getMetadata("previousPet").get(0).asString();
                    player.removeMetadata("previousPet", plugin);  // Clean up metadata

                    PetManager petManager = PetManager.getInstance(MinecraftNew.getInstance());
                    if (previousPetName != "") {
                        // Resummon old pet
                        petManager.summonPet(player, previousPetName);
                    } else {
                        // Despawn if there was no previously active pet
                        petManager.despawnPet(player);
                    }
                    player.playSound(player.getLocation(), Sound.BLOCK_FIRE_EXTINGUISH, 1.0f, 1.0f);
                }

                Villager stored = playerVillagerMap.get(player);
                if (stored != null) {
                    String rawName = ChatColor.stripColor(stored.getCustomName() == null ? "Villager" : stored.getCustomName());
                    boolean isTroll = VillagerNameRepository.isTrollName(rawName);
                    player.sendMessage(ChatColor.GRAY + "<" + rawName + "> " +
                            VillagerChatLines.getCloseLine(rawName, isTroll));
                    if (Math.random() < 0.10) {
                        String req = WorkCycleMessages.getRequirement(stored.getProfession());
                        if (req != null) {
                            player.sendMessage(ChatColor.GRAY + "<" + rawName + "> " + req);
                        }
                    }
                    if (stored.hasMetadata("tempTradeVillager")) {
                        stored.remove();
                    }
                    playerVillagerMap.remove(player);
                }

                // Reset the toggle-flag on close

            }
        }, 20L); // 20 ticks = 1 second
    }




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



    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Inventory clickedInventory = event.getClickedInventory();
        if (clickedInventory == null) return;
        if (event.getView().getTitle().equals(ChatColor.GREEN + "Villager Trading")) {
            if (clickedInventory.equals(event.getWhoClicked().getInventory())) {
                return; // Let the event proceed normally
            }
            event.setCancelled(true); // Cancel all clicks

            if (event.getCurrentItem() == null) return;
            ItemStack clickedItem = event.getCurrentItem();
            if (clickedItem.getType() == Material.BARRIER
                    || clickedItem.getType() == Material.GRAY_STAINED_GLASS_PANE) {
                return; // Locked trade or divider
            }


                int slot = event.getSlot();
                Player player = (Player) event.getWhoClicked();
                Villager villager = playerVillagerMap.get(player);
                if (villager == null) return;

                Villager.Profession profession = villager.getProfession();
                int villagerLevel = villager.getVillagerLevel();

                // Determine if the player is buying or selling
                int column = slot % 9;
                if (column <= 3) {
                    // Purchases (columns 0-3)
                    List<TradeItem> purchases = purchaseWhitelist.getOrDefault(profession, Collections.emptyList());
                    int purchaseIndex = (slot / 9) * 4 + column;
                    if (purchaseIndex < purchases.size()) {
                        TradeItem tradeItem = purchases.get(purchaseIndex);
                        if (villagerLevel >= tradeItem.getRequiredLevel()) {
                            int times = 1;
                            if (event.getClick().isRightClick()) {
                                SkillTreeManager mgr = SkillTreeManager.getInstance();
                                if (mgr != null && mgr.getTalentLevel(player.getUniqueId(), Skill.BARTERING, Talent.SHUT_UP_AND_TAKE_MY_MONEY) > 0) {
                                    times = 2;
                                }
                            }
                            processPurchase(player, villager, tradeItem, times);
                        }
                    }
                } else if (column >= 5) {
                    // Sells (columns 5-8)
                    List<TradeItem> sells = sellWhitelist.getOrDefault(profession, Collections.emptyList());
                    int sellIndex = (slot / 9) * 4 + (column - 5);
                    if (sellIndex < sells.size()) {
                        TradeItem tradeItem = sells.get(sellIndex);
                        if (villagerLevel >= tradeItem.getRequiredLevel()) {
                            processSell(player, villager, tradeItem);
                        }
                    }
                }
        }
    }

    /**
     * Checks if the player has a certain quantity of the targetItem in inventory.
     */
    private boolean hasEnoughItems(Inventory inventory, ItemStack targetItem, int quantity) {
        int totalCount = 0;

        for (ItemStack item : inventory.getContents()) {
            if (item == null) continue;
            if (isMatchingItem(item, targetItem)) {
                totalCount += item.getAmount();
                if (totalCount >= quantity) return true;
            }
        }
        return false;
    }


    /**
     * Determines if two ItemStacks match by type, display name, and lore.
     */
    private boolean isMatchingItem(ItemStack item, ItemStack targetItem) {
        if (item.getType() != targetItem.getType()) return false;

        ItemMeta itemMeta = item.getItemMeta();
        ItemMeta targetMeta = targetItem.getItemMeta();

        if (itemMeta == null && targetMeta == null) return true;
        if (itemMeta == null || targetMeta == null) return false;

        // Compare DisplayName
        if (itemMeta.hasDisplayName() && targetMeta.hasDisplayName()) {
            if (!itemMeta.getDisplayName().equals(targetMeta.getDisplayName())) {
                return false;
            }
        } else if (itemMeta.hasDisplayName() || targetMeta.hasDisplayName()) {
            return false;
        }

        // Compare Lore
        if (itemMeta.hasLore() && targetMeta.hasLore()) {
            if (!itemMeta.getLore().equals(targetMeta.getLore())) {
                return false;
            }
        } else if (itemMeta.hasLore() || targetMeta.hasLore()) {
            return false;
        }

        return true;
    }

    /**
     * Processes a purchase (player buys item from villager).
     */
    public void processPurchase(Player player, Villager villager, TradeItem tradeItem, int times) {
        int emeraldCost = tradeItem.getEmeraldValue() * times;
        int quantity = tradeItem.getQuantity() * times;

        // --- Pet HAGGLE perk logic ---
        PetManager petManager = PetManager.getInstance(MinecraftNew.getInstance());
        PetManager.Pet activePet = petManager.getActivePet(player);

        double finalCost = emeraldCost;
        if (activePet != null && activePet.hasPerk(PetManager.PetPerk.HAGGLE)) {
            int petLevel = activePet.getLevel();
            double discountFactor;
            if (petLevel >= 100) {
                discountFactor = 0.25;
            } else if (petLevel >= 75) {
                discountFactor = 0.20;
            } else if (petLevel >= 50) {
                discountFactor = 0.15;
            } else if (petLevel >= 25) {
                discountFactor = 0.10;
            } else if (petLevel >= 1) {
                discountFactor = 0.05;
            } else {
                discountFactor = 0.0;
            }
            finalCost *= (1 - discountFactor);
            finalCost = Math.floor(finalCost);
        }

        SkillTreeManager manager = SkillTreeManager.getInstance();
        if (manager != null) {
            double discount = 0.0;
            discount += manager.getTalentLevel(player.getUniqueId(), Skill.BARTERING, Talent.HAGGLER_I) * 0.005;
            discount += manager.getTalentLevel(player.getUniqueId(), Skill.BARTERING, Talent.HAGGLER_II) * 0.01;
            discount += manager.getTalentLevel(player.getUniqueId(), Skill.BARTERING, Talent.HAGGLER_III) * 0.015;
            discount += manager.getTalentLevel(player.getUniqueId(), Skill.BARTERING, Talent.HAGGLER_IV) * 0.02;
            discount += manager.getTalentLevel(player.getUniqueId(), Skill.BARTERING, Talent.HAGGLER_V) * 0.025;
            discount += manager.getTalentLevel(player.getUniqueId(), Skill.BARTERING, Talent.BILLIONAIRE_DISCOUNT) * 0.05;
            finalCost *= (1 - discount);

            Map<String, BulkInfo> active = bulkDiscounts.get(player.getUniqueId());
            if (active != null) {
                String key = getItemKey(tradeItem.getItem());
                BulkInfo info = active.get(key);
                if (info != null && info.expireTime > System.currentTimeMillis()) {
                    finalCost *= (1 - info.discount);
                } else if (info != null) {
                    active.remove(key);
                }
            }
        }

        // --- Bartering discount logic removed ---

        if (PotionManager.isActive("Potion of Charismatic Bartering", player)
                && PotionEffectPreferences.isEnabled(player, "Potion of Charismatic Bartering")) {
            double discount = 0.20;
            finalCost *= (1 - discount);
        }

        // Ensure at least cost of 1
        int finalCostRounded = Math.max(1, (int) Math.floor(finalCost));

        // --- Free transaction talent ---
        boolean freePurchase = false;
        if (manager != null) {
            int freeLevel = manager.getTalentLevel(player.getUniqueId(), Skill.BARTERING, Talent.OVERSTOCKED);
            if (freeLevel > 0 && Math.random() < freeLevel * 0.02) {
                finalCostRounded = 0;
                freePurchase = true;
            }
        }
        if (freePurchase) {
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_YES, 1.0f, 1.0f);
        }

        if (finalCostRounded > 0) {
            boolean success = BankAccountManager.getInstance().removeEmeralds(player, finalCostRounded);
            TrinketManager.getInstance().refreshBankLore(player);
            if (!success) {
                player.sendMessage(ChatColor.RED + "You don't have enough emeralds.");
                return;
            }
        }

        // If we get here, we've successfully removed "finalCostRounded" emeralds overall.
        // --- Give the purchased item ---
        ItemStack itemToGive = tradeItem.getItem().clone();
        itemToGive.setAmount(quantity);

        Map<Integer, ItemStack> remainingItems = player.getInventory().addItem(itemToGive);
        if (!remainingItems.isEmpty()) {
            // If the player's inventory is full, drop leftover on the ground
            for (ItemStack leftover : remainingItems.values()) {
                player.getWorld().dropItemNaturally(player.getLocation(), leftover);
            }
            player.sendMessage(ChatColor.YELLOW + "Your inventory was full, so leftover items were dropped on the ground.");
        }

        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.2f);

        // Refresh bank account lore so the displayed balance is up-to-date
        TrinketManager.getInstance().refreshBankLore(player);
        XPManager xpManager = new XPManager(MinecraftNew.getInstance());
        // --- Add villager experience ---
        int expGain = 1;
        if (activePet != null && activePet.hasPerk(PetManager.PetPerk.PRACTICE)) {
            expGain += 3;
        }
        addVillagerExperience(villager, expGain);

        // --- Rare chance to get Villager pet ---
        if (Math.random() < 0.001) {
            PetRegistry petRegistry = new PetRegistry();

            petRegistry.addPetByName(player, "Villager");
            player.sendMessage(ChatColor.GOLD + "Congratulations! You have received the Villager pet!");
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_YES, 1.0f, 1.0f);
        }

        // --- Add bartering XP ---
        int barterXP = 11;
        if (activePet != null &&
                activePet.getName().equalsIgnoreCase("Villager") &&
                activePet.hasPerk(PetManager.PetPerk.PRACTICE)) {
            barterXP *= 3;
        }
        xpManager.addXP(player, "Bartering", barterXP);

        if (manager != null) {
            int bulkLevel = manager.getTalentLevel(player.getUniqueId(), Skill.BARTERING, Talent.BULK);
            if (bulkLevel > 0) {
                Map<String, BulkInfo> active = bulkDiscounts.computeIfAbsent(player.getUniqueId(), k -> new HashMap<>());
                String key = getItemKey(tradeItem.getItem());
                BulkInfo info = new BulkInfo();
                info.discount = bulkLevel * 0.10;
                info.expireTime = System.currentTimeMillis() + 20000; // 20s
                active.put(key, info);
            }
        }
    }

    /**
     * Example helper to count how many Emeralds are in a player's main inventory.
     */
    private int countEmeraldsInInventory(Player player) {
        int count = 0;
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.getType() == Material.EMERALD) {
                count += item.getAmount();
            }
        }
        return count;
    }





    // Main method to process the selling transaction
    private void processSell(Player player, Villager villager, TradeItem tradeItem) {
        XPManager xpManager = new XPManager(plugin);
        int emeraldReward = tradeItem.getEmeraldValue();
        SkillTreeManager manager = SkillTreeManager.getInstance();
        if (manager != null) {
            int level = 0;
            level += manager.getTalentLevel(player.getUniqueId(), Skill.BARTERING, Talent.STONKS_I);
            level += manager.getTalentLevel(player.getUniqueId(), Skill.BARTERING, Talent.STONKS_II);
            level += manager.getTalentLevel(player.getUniqueId(), Skill.BARTERING, Talent.STONKS_III);
            level += manager.getTalentLevel(player.getUniqueId(), Skill.BARTERING, Talent.STONKS_IV);
            level += manager.getTalentLevel(player.getUniqueId(), Skill.BARTERING, Talent.STONKS_V);
            emeraldReward += (int) Math.floor(emeraldReward * (level * 0.02));
        }
        int quantity = tradeItem.getQuantity();
        ItemStack tradeItemStack = tradeItem.getItem();

        // Attempt to remove items that match the trade item's criteria and reward emeralds.
        boolean success = removeCustomItemsAndReward(player, tradeItemStack, quantity, emeraldReward);
        if (success) {
            // Give villager XP
            addVillagerExperience(villager, 1);

            // Play sound and notify the player
            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.2f);
            player.sendMessage(ChatColor.GREEN + "You sold " + quantity + " items for " + emeraldReward + " emeralds!");

            // Award Bartering XP to the player
            int barterXP = 11;
            PetManager petManager = PetManager.getInstance(plugin);
            PetManager.Pet activePet = petManager.getActivePet(player);
            if (activePet != null &&
                    activePet.getName().equalsIgnoreCase("Villager") &&
                    activePet.hasPerk(PetManager.PetPerk.PRACTICE)) {
                barterXP *= 3;
            }
            xpManager.addXP(player, "Bartering", barterXP);

            if (manager != null) {
                int interest = manager.getTalentLevel(player.getUniqueId(), Skill.BARTERING, Talent.INTEREST);
                if (interest > 0 && Math.random() < interest * 0.01) {
                    int bonus = (int)Math.floor(BankAccountManager.getInstance().getBalance(player.getUniqueId()) * 0.01);
                    BankAccountManager.getInstance().addEmeralds(player.getUniqueId(), bonus);
                    TrinketManager.getInstance().refreshBankLore(player);
                    player.sendMessage(ChatColor.GOLD + "Interest payout: " + bonus + " emeralds!");
                }
            }

        } else {
            // Not enough valid items were removed
            player.sendMessage(ChatColor.RED + "You don't have enough required items to sell.");
        }
    }

    /**
     * Removes items from the player's inventory that match the target item's criteria.
     *
     * For items with a custom name, only those with the exact same display name are counted.
     * For default items (no custom name), all items without a custom display name (or without ItemMeta)
     * of the same type are counted.
     *
     * If enough items are found and removed, the player is rewarded with emeralds.
     */
    private boolean removeCustomItemsAndReward(
            Player player,
            ItemStack targetItem,
            int quantity,
            int emeraldReward
    ) {
        Inventory inventory = player.getInventory();
        int needed = quantity;

        // Determine whether the target item is custom (has a display name) or default.
        boolean isCustom = targetItem.hasItemMeta() && targetItem.getItemMeta().hasDisplayName();
        String targetName = isCustom ? targetItem.getItemMeta().getDisplayName() : null;
        Material targetType = targetItem.getType();

        // First pass: Determine which inventory slots contain valid items.
        List<RemovalData> itemsToRemove = new ArrayList<>();
        for (int slot = 0; slot < inventory.getSize(); slot++) {
            if (needed <= 0) break;

            ItemStack currentItem = inventory.getItem(slot);
            if (currentItem == null) continue;

            // Must match the same type.
            if (!currentItem.getType().equals(targetType)) continue;

            if (isCustom) {
                // For custom items, only count items with a matching display name.
                if (currentItem.hasItemMeta() && currentItem.getItemMeta().hasDisplayName()) {
                    String currentName = currentItem.getItemMeta().getDisplayName();
                    if (targetName.equals(currentName)) {
                        int itemAmount = currentItem.getAmount();
                        if (itemAmount <= needed) {
                            itemsToRemove.add(new RemovalData(slot, itemAmount));
                            needed -= itemAmount;
                        } else {
                            itemsToRemove.add(new RemovalData(slot, needed));
                            needed = 0;
                        }
                    }
                }
            } else {
                // For default items, count only items that don't have a custom display name.
                if (!currentItem.hasItemMeta() || !currentItem.getItemMeta().hasDisplayName()) {
                    int itemAmount = currentItem.getAmount();
                    if (itemAmount <= needed) {
                        itemsToRemove.add(new RemovalData(slot, itemAmount));
                        needed -= itemAmount;
                    } else {
                        itemsToRemove.add(new RemovalData(slot, needed));
                        needed = 0;
                    }
                }
            }
        }

        // If not enough valid items were found, return false.
        if (needed > 0) {
            return false;
        }

        // Second pass: Remove the determined items from the inventory.
        for (RemovalData data : itemsToRemove) {
            ItemStack stack = inventory.getItem(data.slot);
            if (stack == null) continue;
            int currentAmount = stack.getAmount();
            int removeAmount = data.amountToRemove;
            if (removeAmount >= currentAmount) {
                inventory.setItem(data.slot, null);
            } else {
                stack.setAmount(currentAmount - removeAmount);
            }
        }

        // Reward emeralds only if all items were successfully removed.
        ItemStack emeralds = new ItemStack(Material.EMERALD, emeraldReward);
        Map<Integer, ItemStack> leftoverEmeralds = inventory.addItem(emeralds);

        // If the inventory is full, drop any leftover emeralds on the ground.
        if (!leftoverEmeralds.isEmpty()) {
            for (ItemStack leftover : leftoverEmeralds.values()) {
                player.getWorld().dropItemNaturally(player.getLocation(), leftover);
            }
            player.sendMessage(ChatColor.YELLOW
                    + "Your inventory was full, so the emerald(s) were dropped on the ground.");
        }
        return true; // Items were successfully removed and emeralds rewarded.
    }

    // Helper class to store removal data for a given inventory slot.
    private static class RemovalData {
        final int slot;
        final int amountToRemove;

        RemovalData(int slot, int amountToRemove) {
            this.slot = slot;
            this.amountToRemove = amountToRemove;
        }
    }

    /**
     * Removes a specified amount of a material from an inventory.
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
            plugin.getLogger().warning("Could not remove the required amount of " + material + ".");
        }
    }

    private Villager findVillagerNearPlayer(Player player) {
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
     */
    private void addVillagerExperience(Villager villager, int experience) {
        int currentXP = villager.getVillagerExperience();
        int newXP = currentXP + experience;
        villager.setVillagerExperience(newXP);

        int villagerLevel = villager.getVillagerLevel();
        int xpForNextLevel = getExperienceForNextLevel(villagerLevel);

        if (newXP >= xpForNextLevel && villagerLevel < MAX_VILLAGER_LEVEL) {
            villagerLevel++;
            villager.setVillagerLevel(villagerLevel);
            villager.setVillagerExperience(0);
            // Reset xpThreshold for the new level
            int newBaseThreshold = getExperienceForNextLevel(villagerLevel);
            villager.setMetadata("xpThreshold", new FixedMetadataValue(plugin, newBaseThreshold));
            villager.getWorld().playSound(villager.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
        }

    }
    /**
     * Passively adds XP to the villager when it "works".
     * The villager gains 5% of the XP required for the next level (rounded down, minimum 1 XP),
     * but only if at least one player is within 150 blocks.
     * This method should be called every workcycle (e.g., every 5 minutes).
     *
     * @param villager The villager to add XP to.
     */
    public void passivelyAddVillagerXP(Villager villager) {
        // Check for any active (non-AFK) player within 1000 blocks of the villager.
        boolean activePlayerNearby = false;
        for (Player player : villager.getWorld().getPlayers()) {
            double distance = player.getLocation().distance(villager.getLocation());
            if (distance <= 1000) {
                if (!AFKDetector.isPlayerAFK(player)) {
                    activePlayerNearby = true;
                    plugin.getLogger().info("[XP Update] Active player '" + player.getName()
                            + "' is within " + distance + " blocks.");
                    break;
                } else {
                    plugin.getLogger().info("[XP Update] Player '" + player.getName()
                            + "' is within " + distance + " blocks but is AFK.");
                }
            }
        }
        if (!activePlayerNearby) {
            plugin.getLogger().info("[XP Update] No active (non-AFK) player found within 1000 blocks. No XP will be added this cycle.");
            return;
        }

        // Get the villager's current level and XP information.
        int currentLevel = villager.getVillagerLevel();
        int xpForNextLevel = getExperienceForNextLevel(currentLevel);
        int currentXP = villager.getVillagerExperience();
        if (currentXP < -5) {
            currentXP = 1000;
        }
        plugin.getLogger().info("[XP Update] Villager Level: " + currentLevel
                + " | Current XP: " + currentXP
                + " | XP required for next level: " + xpForNextLevel);

        // Calculate 5% of xpForNextLevel (rounded down), ensuring at least 1 XP is gained.
        int xpIncrease = (int) Math.floor(xpForNextLevel * 0.05);
        if (xpIncrease < 1) {
            xpIncrease = 1;
        }
        // If villager is at max level, no XP is added.
        if (villager.getVillagerLevel() == 5) {
            xpIncrease = 0;
        }
        plugin.getLogger().info("[XP Update] Calculated 5% XP increase: " + xpIncrease);

        // Add the calculated XP to the villager's current XP.
        int newXP = currentXP + xpIncrease;
        villager.setVillagerExperience(newXP);
        plugin.getLogger().info("[XP Update] New XP after addition: " + newXP);

        // If newXP is somehow negative, reset it.
        if (newXP < 0) {
            newXP = 1000;
        }
        // Check if the new XP meets or exceeds the threshold for leveling up.
        if (newXP >= xpForNextLevel && currentLevel < MAX_VILLAGER_LEVEL) {
            int leftoverXP = newXP - xpForNextLevel; // Carry over any extra XP.
            int newLevel = currentLevel + 1;
            villager.setVillagerLevel(newLevel);
            villager.setVillagerExperience(leftoverXP);
            plugin.getLogger().info("[XP LevelUp] Villager leveled up from " + currentLevel
                    + " to " + newLevel + ". XP reset to " + leftoverXP + " (excess XP carried over).");
            villager.getWorld().playSound(villager.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
        } else {
            plugin.getLogger().info("[XP Update] Villager has not reached the next level yet.");
        }
    }




    /**
     * Determines how much XP is needed for a villager to level up.
     */
    private int getExperienceForNextLevel(int currentLevel) {
        switch (currentLevel) {
            case 1: return 10 *3;
            case 2: return 70*3;
            case 3: return 150 *3;
            case 4: return 250 *3;
            default: return Integer.MAX_VALUE;
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getView().getTitle().equals(ChatColor.GREEN + "Nearest Villager Trading")) {
            Player player = (Player) event.getPlayer();
            playerVillagerMap.remove(player);
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

    private static class BulkInfo {
        double discount;
        long expireTime;
    }

    private String getItemKey(ItemStack item) {
        String name = "";
        if (item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
            name = item.getItemMeta().getDisplayName();
        }
        return item.getType().toString() + "|" + name;
    }
}
