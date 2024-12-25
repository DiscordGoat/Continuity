package goat.minecraft.minecraftnew.subsystems.villagers;

import goat.minecraft.minecraftnew.MinecraftNew;
import goat.minecraft.minecraftnew.subsystems.culinary.CulinarySubsystem;
import goat.minecraft.minecraftnew.utils.generators.ResourceGeneratorSubsystem;
import goat.minecraft.minecraftnew.subsystems.pets.PetManager;
import goat.minecraft.minecraftnew.utils.CustomItemManager;
import goat.minecraft.minecraftnew.utils.ItemRegistry;
import goat.minecraft.minecraftnew.utils.XPManager;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

import static goat.minecraft.minecraftnew.subsystems.culinary.CulinarySubsystem.recipeRegistry;
import static goat.minecraft.minecraftnew.subsystems.fishing.SeaCreatureRegistry.createAlchemyItem;
import static goat.minecraft.minecraftnew.utils.CustomItemManager.createCustomItem;

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

    /**
     * Initializes the purchase and sell whitelists with predefined trades.
     */

    private void initializeWhitelists() {
// Mason
        List<TradeItem> masonPurchases = new ArrayList<>();
        masonPurchases.add(new TradeItem(new ItemStack(Material.BRICKS, 4), 3, 4, 1, 29 * 1)); // Placeholder trade
        masonPurchases.add(new TradeItem(new ItemStack(Material.STONE, 8), 3, 8, 1,29 * 1)); // Placeholder trade

        masonPurchases.add(new TradeItem(new ItemStack(Material.ANDESITE, 8), 3, 8, 2,29 * 1)); // Placeholder trade
        masonPurchases.add(new TradeItem(new ItemStack(Material.DIORITE, 8), 3, 8, 2,29 * 1)); // Placeholder trade
        masonPurchases.add(new TradeItem(new ItemStack(Material.GRANITE, 8), 3, 8, 2,29 * 1)); // Placeholder trade

        masonPurchases.add(new TradeItem(new ItemStack(Material.TERRACOTTA, 8), 3, 8, 3,29 * 1)); // Placeholder trade
        masonPurchases.add(new TradeItem(new ItemStack(Material.QUARTZ_BLOCK, 8), 5, 8, 3,29 * 2)); // Placeholder trade
        masonPurchases.add(new TradeItem(new ItemStack(Material.SMOOTH_STONE, 8), 5, 8, 3,29 * 2)); // Placeholder trade
        masonPurchases.add(new TradeItem(new ItemStack(Material.SANDSTONE, 8), 5, 8, 3,29 * 2)); // Placeholder trade
        masonPurchases.add(new TradeItem(ItemRegistry.getItemDisplayItem(), 8, 1, 3,29 * 2)); // Placeholder trade

        masonPurchases.add(new TradeItem(new ItemStack(Material.PRISMARINE, 8), 7, 8, 4,29 * 4)); // Placeholder trade
        masonPurchases.add(new TradeItem(new ItemStack(Material.DARK_PRISMARINE, 8), 7, 8, 4,29 * 4)); // Placeholder trade
        masonPurchases.add(new TradeItem(new ItemStack(Material.PRISMARINE_BRICKS, 8), 7, 8, 4,29 * 4)); // Placeholder trade
        masonPurchases.add(new TradeItem(new ItemStack(Material.NETHER_BRICKS, 8), 7, 8, 4,29 * 4)); // Placeholder trade
        masonPurchases.add(new TradeItem(new ItemStack(Material.BLACKSTONE, 8), 7, 8, 4,29 * 4)); // Placeholder trade
        masonPurchases.add(new TradeItem(new ItemStack(Material.SOUL_SAND, 8), 7, 8, 4,29 * 4)); // Placeholder trade
        masonPurchases.add(new TradeItem(new ItemStack(Material.TUBE_CORAL_BLOCK, 8), 7, 8, 4,29 * 3)); // Placeholder trade
        masonPurchases.add(new TradeItem(new ItemStack(Material.BRAIN_CORAL_BLOCK, 8), 7, 8, 4,29 * 3)); // Placeholder trade
        masonPurchases.add(new TradeItem(new ItemStack(Material.BUBBLE_CORAL_BLOCK, 8), 7, 8, 4,29 * 4)); // Placeholder trade
        masonPurchases.add(new TradeItem(new ItemStack(Material.FIRE_CORAL_BLOCK, 8), 7, 8, 4,29 * 4)); // Placeholder trade
        masonPurchases.add(new TradeItem(new ItemStack(Material.HORN_CORAL_BLOCK, 8), 7, 8, 4,29 * 4)); // Placeholder trade
        purchaseWhitelist.put(Villager.Profession.MASON, masonPurchases);

        List<TradeItem> masonSells = new ArrayList<>();
        masonSells.add(new TradeItem(new ItemStack(Material.CLAY, 4), 1, 4, 1,29 * 1)); // Placeholder trade
        masonSells.add(new TradeItem(new ItemStack(Material.COBBLESTONE, 32), 1, 32, 1,29 * 1)); // Placeholder trade
        masonSells.add(new TradeItem(new ItemStack(Material.COPPER_INGOT, 3), 1, 3, 1,29 * 1)); // Placeholder trade
        sellWhitelist.put(Villager.Profession.MASON, masonSells);


// Weaponsmith
        List<TradeItem> weaponsmithPurchases = new ArrayList<>();
        weaponsmithPurchases.add(new TradeItem(new ItemStack(Material.IRON_INGOT, 1), 4, 1, 1,29 * 2)); // Placeholder trade
        weaponsmithPurchases.add(new TradeItem(new ItemStack(Material.COAL, 4), 2, 4, 1,29 * 1)); // Placeholder trade
        weaponsmithPurchases.add(new TradeItem(ItemRegistry.getCommonSwordReforge(), 32, 1, 1,29 * 16)); // Placeholder trade

        weaponsmithPurchases.add(new TradeItem(ItemRegistry.getUncommonSwordReforge(), 64, 1, 2,29 * 32)); // Placeholder trade


        weaponsmithPurchases.add(new TradeItem(new ItemStack(Material.BELL, 1), 63, 1, 3,29 * 32)); // Placeholder trade
        weaponsmithPurchases.add(new TradeItem(ItemRegistry.getWeaponsmithReforge(), 64, 1, 3,29 * 32)); // Placeholder trade
        weaponsmithPurchases.add(new TradeItem(ItemRegistry.getWeaponsmithReforgeTwo(), 128, 1, 3,29 * 64)); // Placeholder trade

        weaponsmithPurchases.add(new TradeItem(ItemRegistry.getRareSwordReforge(), 128, 1, 3,29 * 64)); // Placeholder trade

        weaponsmithPurchases.add(new TradeItem(ItemRegistry.getWeaponsmithSharpness(), 64, 1, 4,29 * 32)); // Placeholder trade
        weaponsmithPurchases.add(new TradeItem(ItemRegistry.getEpicSwordReforge(), 256, 1, 4,29 * 128)); // Placeholder trade

        weaponsmithPurchases.add(new TradeItem(ItemRegistry.getWeaponsmithSweepingEdge(), 63, 1, 4,29 * 32)); // Placeholder trade
        weaponsmithPurchases.add(new TradeItem(ItemRegistry.getWeaponsmithLooting(), 64, 1, 4,29 * 32)); // Placeholder trade
        weaponsmithPurchases.add(new TradeItem(ItemRegistry.getWeaponsmithKnockback(), 64, 1, 4,29 * 32)); // Placeholder trade
        weaponsmithPurchases.add(new TradeItem(ItemRegistry.getWeaponsmithFireAspect(), 64, 1, 4,29 * 32)); // Placeholder trade
        weaponsmithPurchases.add(new TradeItem(ItemRegistry.getWeaponsmithSmite(), 64, 1, 4,29 * 32)); // Placeholder trade
        weaponsmithPurchases.add(new TradeItem(ItemRegistry.getWeaponsmithBaneofAnthropods(), 64, 1, 4,29 * 32)); // Placeholder trade

        weaponsmithPurchases.add(new TradeItem(ItemRegistry.getWeaponsmithEnchant(), 64, 1, 5,29 * 32)); // Placeholder trade
        weaponsmithPurchases.add(new TradeItem(ItemRegistry.getLegendarySwordReforge(), 512, 1, 5,29 * 256)); // Placeholder trade

        purchaseWhitelist.put(Villager.Profession.WEAPONSMITH, weaponsmithPurchases);
        ItemStack undeadDrop = ItemRegistry.getUndeadDrop();
        ItemStack creeperDrop = ItemRegistry.getCreeperDrop();
        ItemStack spiderDrop = ItemRegistry.getSpiderDrop();
        ItemStack enderDrop = ItemRegistry.getEnderDrop();
        ItemStack blazeDrop = ItemRegistry.getBlazeDrop();
        ItemStack witchDrop = ItemRegistry.getWitchDrop();
        ItemStack guardianDrop = ItemRegistry.getGuardianDrop();
        ItemStack elderGuardianDrop = ItemRegistry.getElderGuardianDrop();
        ItemStack vindicatorDrop = ItemRegistry.getVindicatorDrop();
        ItemStack piglinDrop = ItemRegistry.getPiglinDrop();
        ItemStack piglinBruteDrop = ItemRegistry.getPiglinBruteDrop();
        ItemStack drownedDrop = ItemRegistry.getDrownedDrop();
        ItemStack skeletonDrop = ItemRegistry.getSkeletonDrop();
        List<TradeItem> weaponsmithSells = new ArrayList<>();
        weaponsmithSells.add(new TradeItem(new ItemStack(Material.IRON_INGOT, 3), 1, 3, 1,29 * 1)); // Placeholder trade
        weaponsmithSells.add(new TradeItem(new ItemStack(Material.GOLD_INGOT, 2), 1, 2, 1,29 * 1)); // Placeholder trade
        weaponsmithSells.add(new TradeItem(new ItemStack(Material.DIAMOND, 1), 8, 1, 1,29 * 4)); // Placeholder trade
        weaponsmithSells.add(new TradeItem(new ItemStack(Material.ZOMBIE_HEAD, 1), 8, 1, 1,29 * 4)); // Placeholder trade
        weaponsmithSells.add(new TradeItem(new ItemStack(Material.SKELETON_SKULL, 1), 8, 1, 1,29 * 3)); // Placeholder trade
        weaponsmithSells.add(new TradeItem(new ItemStack(Material.CREEPER_HEAD, 1), 8, 1, 1,29 * 4)); // Placeholder trade
        weaponsmithSells.add(new TradeItem(ItemRegistry.getSingularity(), 8, 1, 1,29 * 4)); // Placeholder trade
        weaponsmithSells.add(new TradeItem(skeletonDrop, 150, 1, 1,29 * 75)); // Placeholder trade
        weaponsmithSells.add(new TradeItem(drownedDrop, 8, 1, 1,29 * 4)); // Placeholder trade
        weaponsmithSells.add(new TradeItem(creeperDrop, 8, 1, 1,29 * 4)); // Placeholder trade
        weaponsmithSells.add(new TradeItem(blazeDrop, 8, 1, 1,29 * 4)); // Placeholder trade
        weaponsmithSells.add(new TradeItem(enderDrop, 24, 1, 1,29 * 12)); // Placeholder trade
        weaponsmithSells.add(new TradeItem(guardianDrop, 8, 1, 1,29 * 4)); // Placeholder trade
        weaponsmithSells.add(new TradeItem(elderGuardianDrop, 4, 1, 1,29 * 2)); // Placeholder trade
        weaponsmithSells.add(new TradeItem(piglinBruteDrop, 8, 1, 1,29 * 4)); // Placeholder trade
        weaponsmithSells.add(new TradeItem(piglinDrop, 8, 1, 1,29 * 4)); // Placeholder trade
        weaponsmithSells.add(new TradeItem(spiderDrop, 100, 1, 1,29 * 50)); // Placeholder trade
        weaponsmithSells.add(new TradeItem(undeadDrop, 100, 1, 1,29 * 50)); // Placeholder trade
        weaponsmithSells.add(new TradeItem(vindicatorDrop, 8, 1, 1,29 * 4)); // Placeholder trade
        weaponsmithSells.add(new TradeItem(witchDrop, 32, 1, 1,29 * 16)); // Placeholder trade
        sellWhitelist.put(Villager.Profession.WEAPONSMITH, weaponsmithSells);



//Fletcher
        List<TradeItem> fletcherPurchases = new ArrayList<>();
        fletcherPurchases.add(new TradeItem(new ItemStack(Material.ARROW, 4), 1, 4, 1,29 * 1)); // Placeholder trade
        fletcherPurchases.add(new TradeItem(new ItemStack(Material.BOW, 1), 4, 1, 1,29 * 2)); // Placeholder trade
        fletcherPurchases.add(new TradeItem(new ItemStack(Material.CROSSBOW, 1), 4, 1, 2,29 * 2)); // Placeholder trade
        fletcherPurchases.add(new TradeItem(new ItemStack(Material.FEATHER, 1), 2, 1, 2,29 * 1)); // Placeholder trade

        fletcherPurchases.add(new TradeItem(new ItemStack(Material.OAK_SAPLING, 4), 2, 4, 3,29 * 1)); // Placeholder trade
        fletcherPurchases.add(new TradeItem(new ItemStack(Material.JUNGLE_SAPLING, 4), 2, 4, 3,29 * 1)); // Placeholder trade
        fletcherPurchases.add(new TradeItem(new ItemStack(Material.DARK_OAK_SAPLING, 4), 2, 4, 3,29 * 1)); // Placeholder trade
        fletcherPurchases.add(new TradeItem(new ItemStack(Material.SPRUCE_SAPLING, 4), 2, 4, 3,29 * 1)); // Placeholder trade
        fletcherPurchases.add(new TradeItem(new ItemStack(Material.BIRCH_SAPLING, 4), 2, 4, 3,29 * 1)); // Placeholder trade
        fletcherPurchases.add(new TradeItem(new ItemStack(Material.ACACIA_SAPLING, 4), 2, 4, 3,29 * 1)); // Placeholder trade
        fletcherPurchases.add(new TradeItem(new ItemStack(Material.CHERRY_SAPLING, 4), 2, 4, 3,29 * 1)); // Placeholder trade

        fletcherPurchases.add(new TradeItem(ItemRegistry.getFletcherBowEnchant(), 16, 1, 4,29 * 8)); // Placeholder trade
        fletcherPurchases.add(new TradeItem(ItemRegistry.getFletcherPower(), 64, 1, 4,29 * 32)); // Placeholder trade
        fletcherPurchases.add(new TradeItem(ItemRegistry.getFletcherCrossbowEnchant(), 64, 1, 5,29 * 32)); // Placeholder trade


        purchaseWhitelist.put(Villager.Profession.FLETCHER, fletcherPurchases);


        List<TradeItem> fletcherSells = new ArrayList<>();
        fletcherSells.add(new TradeItem(new ItemStack(Material.STICK, 64), 1, 64, 1,29 * 32)); // Placeholder trade
        fletcherSells.add(new TradeItem(new ItemStack(Material.FLINT, 2), 1, 2, 1,29 * 1)); // Placeholder trade
        fletcherSells.add(new TradeItem(new ItemStack(Material.STRING, 2), 1, 2, 1,29 * 1)); // Placeholder trade
        fletcherSells.add(new TradeItem(new ItemStack(Material.FEATHER, 2), 1, 2, 1,29 * 1)); // Placeholder trade
        fletcherSells.add(new TradeItem(new ItemStack(Material.ARROW, 16), 1, 16, 1,29 * 1)); // Placeholder trade
        fletcherSells.add(new TradeItem(new ItemStack(ItemRegistry.getSecretsOfInfinity()), 128, 1, 128,29 * 64)); // Placeholder trade

        sellWhitelist.put(Villager.Profession.FLETCHER, fletcherSells);


// Cartographer
        List<TradeItem> cartographerPurchases = new ArrayList<>();
        cartographerPurchases.add(new TradeItem(ItemRegistry.getCartographerMineshaft(), 16, 1, 1,29 * 8)); // Placeholder trade
        cartographerPurchases.add(new TradeItem(ItemRegistry.getCartographerVillage(), 16, 1, 1,29 * 8)); // Placeholder trade
        cartographerPurchases.add(new TradeItem(ItemRegistry.getCartographerShipwreck(), 16, 1, 1,29 * 8)); // Placeholder trade

        cartographerPurchases.add(new TradeItem(ItemRegistry.getCartographerBuriedTreasure(), 16, 1, 2,29 * 8)); // Placeholder trade
        cartographerPurchases.add(new TradeItem(ItemRegistry.getCartographerIgloo(), 20, 1, 2,29 * 10)); // Placeholder trade
        cartographerPurchases.add(new TradeItem(ItemRegistry.getCartographerOceanMonument(), 20, 1, 2,29 * 10)); // Placeholder trade
        cartographerPurchases.add(new TradeItem(ItemRegistry.getCartographerOceanRuins(), 20, 1, 2,29 * 10)); // Placeholder trade

        cartographerPurchases.add(new TradeItem(ItemRegistry.getCartographerDesertPyramid(), 32, 1, 3,29 * 16)); // Placeholder trade
        cartographerPurchases.add(new TradeItem(ItemRegistry.getCartographerJungleTemple(), 32, 1, 3,29 * 16)); // Placeholder trade
        cartographerPurchases.add(new TradeItem(ItemRegistry.getCartographerPillagerOutpost(), 32, 1, 3,29 * 16)); // Placeholder trade
        cartographerPurchases.add(new TradeItem(ItemRegistry.getCartographerSwampHut(), 32, 1, 3,29 * 16)); // Placeholder trade
        cartographerPurchases.add(new TradeItem(ItemRegistry.getCartographerNetherFortress(), 32, 1, 3,29 * 16)); // Placeholder trade


        cartographerPurchases.add(new TradeItem(ItemRegistry.getCartographerStronghold(), 64, 1, 4,29 * 32)); // Placeholder trade
        cartographerPurchases.add(new TradeItem(ItemRegistry.getCartographerBastionRemnant(), 64, 1, 4,29 * 32)); // Placeholder trade

        cartographerPurchases.add(new TradeItem(ItemRegistry.getCartographerWoodlandMansion(), 128, 1, 5,29 * 64)); // Placeholder trade
        //cartographerPurchases.add(new TradeItem(ItemRegistry.getAspectoftheJourney(), 128, 1, 5,29 * 64)); // Placeholder trade
        purchaseWhitelist.put(Villager.Profession.CARTOGRAPHER, cartographerPurchases);

        List<TradeItem> cartographerSells = new ArrayList<>();
        cartographerSells.add(new TradeItem(new ItemStack(Material.PAPER, 8), 1, 8, 1,29 * 1)); // Placeholder trade
        cartographerSells.add(new TradeItem(new ItemStack(Material.COMPASS, 1), 3, 1, 1,29 * 1)); // Placeholder trade
        sellWhitelist.put(Villager.Profession.CARTOGRAPHER, cartographerSells);




// Cleric
        List<TradeItem> clericPurchases = new ArrayList<>();
        clericPurchases.add(new TradeItem(new ItemStack(Material.GLASS_BOTTLE, 1), 2, 1, 1,29 * 1)); // Placeholder trade
        clericPurchases.add(new TradeItem(new ItemStack(Material.NETHER_WART, 1), 64, 1, 1,29 * 32));
        clericPurchases.add(new TradeItem(new ItemStack(Material.SUGAR, 1), 2, 1, 1,29 * 1));

        clericPurchases.add(new TradeItem(new ItemStack(Material.RABBIT_FOOT, 1), 12, 1, 2,29 * 6));
        clericPurchases.add(new TradeItem(new ItemStack(Material.GLISTERING_MELON_SLICE, 1), 8, 1, 2,29 * 4));
        clericPurchases.add(new TradeItem(new ItemStack(Material.FERMENTED_SPIDER_EYE, 3), 15, 3, 2,29 * 7)); // Placeholder trade
        clericPurchases.add(new TradeItem(new ItemStack(Material.SPIDER_EYE, 3), 12, 3, 2,29 * 6)); // Placeholder trade

        clericPurchases.add(new TradeItem(new ItemStack(Material.GUNPOWDER, 3), 6, 3, 3,29 * 3)); // Placeholder trade
        clericPurchases.add(new TradeItem(new ItemStack(Material.MAGMA_CREAM, 3), 16, 3, 3,29 * 8)); // Placeholder trade
        clericPurchases.add(new TradeItem(new ItemStack(Material.BLAZE_POWDER, 2), 48, 2, 3,29 * 24)); // Placeholder trade
        clericPurchases.add(new TradeItem(new ItemStack(Material.PHANTOM_MEMBRANE, 2), 8, 2, 3,29 * 4)); // Placeholder trade

        clericPurchases.add(new TradeItem(new ItemStack(Material.GHAST_TEAR, 2), 20, 2, 4,29 * 10)); // Placeholder trade

        clericPurchases.add(new TradeItem(new ItemStack(Material.DRAGON_BREATH, 4), 64, 4, 5,29 * 32)); // Placeholder trade
        clericPurchases.add(new TradeItem(new ItemStack(Material.TURTLE_HELMET, 1), 64, 1, 5,29 * 32)); // Placeholder trade
        clericPurchases.add(new TradeItem(ItemRegistry.getClericEnchant(), 64, 1, 5,29 * 32)); // Placeholder trade

        purchaseWhitelist.put(Villager.Profession.CLERIC, clericPurchases);

        List<TradeItem> clericSells = new ArrayList<>();
        clericSells.add(new TradeItem(new ItemStack(Material.ROTTEN_FLESH, 4), 1, 4, 1,29 * 1)); // Placeholder trade
        clericSells.add(new TradeItem(new ItemStack(Material.BONE, 8), 3, 8, 1,29 * 1)); // Placeholder trade
        clericSells.add(new TradeItem(new ItemStack(Material.SPIDER_EYE, 4), 3, 4, 1,29 * 1)); // Placeholder trade
        clericSells.add(new TradeItem(new ItemStack(Material.LAPIS_LAZULI, 8), 1, 8, 2,29 * 1)); // Placeholder trade
        clericSells.add(new TradeItem(new ItemStack(Material.REDSTONE, 8), 1, 8, 2,29 * 1)); // Placeholder trade
        clericSells.add(new TradeItem(new ItemStack(Material.ENDER_PEARL, 2), 8, 2, 3,29 * 4)); // Placeholder trade
        clericSells.add(new TradeItem(new ItemStack(Material.GLOWSTONE, 1), 3, 1, 4,29 * 1)); // Placeholder trade

        sellWhitelist.put(Villager.Profession.CLERIC, clericSells);



// Leatherworker
        List<TradeItem> leatherworkerPurchases = new ArrayList<>();
        {
            leatherworkerPurchases.add(new TradeItem(new ItemStack(Material.LEATHER, 1), 3, 1, 1,29 * 1)); // Placeholder trade
            leatherworkerPurchases.add(new TradeItem(new ItemStack(Material.ITEM_FRAME, 1), 3, 1, 1,29 * 1)); // Placeholder trade
            leatherworkerPurchases.add(new TradeItem(new ItemStack(Material.SHULKER_SHELL), 64, 1, 3,29 * 32)); // Placeholder trade

            leatherworkerPurchases.add(new TradeItem(new ItemStack(Material.BUNDLE, 1), 64, 1, 3,29 * 32)); // Placeholder trade
            leatherworkerPurchases.add(new TradeItem(ItemRegistry.getLeatherworkerEnchant(), 32, 1, 4,29 * 16)); // Placeholder trade
            leatherworkerPurchases.add(new TradeItem(ItemRegistry.getLeatherworkerArtifact(), 64, 1, 5,29 * 32)); // Placeholder trade
        }
        purchaseWhitelist.put(Villager.Profession.LEATHERWORKER, leatherworkerPurchases);

        List<TradeItem> leatherworkerSells = new ArrayList<>();
        {
            leatherworkerSells.add(new TradeItem(new ItemStack(Material.SADDLE), 12, 1, 1,29 * 6));
            leatherworkerSells.add(new TradeItem(new ItemStack(Material.LEATHER_BOOTS), 1, 1, 1,29 * 1));
        }
        sellWhitelist.put(Villager.Profession.LEATHERWORKER, leatherworkerSells);



        // Shepherd
        List<TradeItem> shepherdPurchases = new ArrayList<>();
        shepherdPurchases.add(new TradeItem(new ItemStack(Material.PAINTING, 4), 8, 4, 1,29 * 4)); // Placeholder trade

        shepherdPurchases.add(new TradeItem(new ItemStack(Material.WHITE_DYE, 4), 3, 4, 2,29 * 1)); // Placeholder trade
        shepherdPurchases.add(new TradeItem(new ItemStack(Material.GRAY_DYE, 4), 3, 4, 2,29 * 1)); // Placeholder trade
        shepherdPurchases.add(new TradeItem(new ItemStack(Material.BLACK_DYE, 4), 3, 4, 2,29 * 1)); // Placeholder trade
        shepherdPurchases.add(new TradeItem(new ItemStack(Material.CYAN_DYE, 4), 3, 4, 2,29 * 1)); // Placeholder trade
        shepherdPurchases.add(new TradeItem(new ItemStack(Material.LIME_DYE, 4), 3, 4, 2,29 * 1)); // Placeholder trade

        shepherdPurchases.add(new TradeItem(new ItemStack(Material.YELLOW_DYE, 4), 6, 4, 3,29 * 3)); // Placeholder trade
        shepherdPurchases.add(new TradeItem(new ItemStack(Material.ORANGE_DYE, 4), 6, 4, 3,29 * 3)); // Placeholder trade
        shepherdPurchases.add(new TradeItem(new ItemStack(Material.RED_DYE, 4), 6, 4, 3,29 * 3)); // Placeholder trade
        shepherdPurchases.add(new TradeItem(new ItemStack(Material.PINK_DYE, 4), 6, 4, 3,29 * 3)); // Placeholder trade
        shepherdPurchases.add(new TradeItem(new ItemStack(Material.LIGHT_GRAY_DYE, 4), 6, 4, 3,29 * 3)); // Placeholder trade

        shepherdPurchases.add(new TradeItem(new ItemStack(Material.MAGENTA_DYE, 4), 9, 4, 4,29 * 3)); // Placeholder trade
        shepherdPurchases.add(new TradeItem(new ItemStack(Material.PURPLE_DYE, 4), 9, 4, 4,29 * 3)); // Placeholder trade
        shepherdPurchases.add(new TradeItem(new ItemStack(Material.BLUE_DYE, 4), 9, 4, 4,29 * 3)); // Placeholder trade
        shepherdPurchases.add(new TradeItem(new ItemStack(Material.CYAN_DYE, 4), 9, 4, 4,29 * 3)); // Placeholder trade
        shepherdPurchases.add(new TradeItem(new ItemStack(Material.GREEN_DYE, 4), 9, 4, 4,29 * 3)); // Placeholder trade
        shepherdPurchases.add(new TradeItem(new ItemStack(Material.BROWN_DYE, 4), 9, 4, 4,29 * 3)); // Placeholder trade

        shepherdPurchases.add(new TradeItem(new ItemStack(Material.TERRACOTTA, 8), 4, 4, 5,29 * 2)); // Placeholder trade
        shepherdPurchases.add(new TradeItem(new ItemStack(Material.GRAVEL, 8), 4, 4, 5,29 * 2)); // Placeholder trade
        shepherdPurchases.add(new TradeItem(new ItemStack(Material.SAND, 8), 4, 4, 5,29 * 2)); // Placeholder trade
        shepherdPurchases.add(new TradeItem(ItemRegistry.getShepherdArtifact(), 16, 8, 5,29 * 4)); // Placeholder trade
        shepherdPurchases.add(new TradeItem(ItemRegistry.getShepherdEnchant(), 32, 1, 5,29 * 4)); // Placeholder trade


        purchaseWhitelist.put(Villager.Profession.SHEPHERD, shepherdPurchases);

        List<TradeItem> shepherdSells = new ArrayList<>();
        shepherdSells.add(new TradeItem(new ItemStack(Material.SHEARS, 1), 3, 1, 1,29 * 1)); // Placeholder trade
        shepherdSells.add(new TradeItem(new ItemStack(Material.BLACK_WOOL, 6), 2, 6, 1,29 * 1)); // Placeholder trade
        shepherdSells.add(new TradeItem(new ItemStack(Material.WHITE_WOOL, 16), 1, 16, 1,29 * 1)); // Placeholder trade
        sellWhitelist.put(Villager.Profession.SHEPHERD, shepherdSells);


// Toolsmith
        List<TradeItem> toolsmithPurchases = new ArrayList<>();
        toolsmithPurchases.add(new TradeItem(new ItemStack(Material.FISHING_ROD, 1), 6, 1, 1,29 * 3)); // Placeholder trade
        toolsmithPurchases.add(new TradeItem(new ItemStack(Material.SHEARS, 1), 6, 1, 1,29 * 3)); // Placeholder trade
        toolsmithPurchases.add(new TradeItem(new ItemStack(Material.BUCKET, 1), 8, 1, 1,29 * 4)); // Placeholder trade
        toolsmithPurchases.add(new TradeItem(ItemRegistry.getCommonToolReforge(), 4, 1, 1,29 * 2)); // Placeholder trade

        toolsmithPurchases.add(new TradeItem(new ItemStack(Material.SHIELD, 1), 10, 1, 2,29 * 5)); // Placeholder trade
        toolsmithPurchases.add(new TradeItem(ItemRegistry.getUncommonToolReforge(), 8, 1, 2,29 * 4)); // Placeholder trade



        toolsmithPurchases.add(new TradeItem(ItemRegistry.getToolsmithReforge(), 64, 1, 3,29 * 32)); // Placeholder trade
        toolsmithPurchases.add(new TradeItem(ItemRegistry.getRareToolReforge(), 16, 1, 3,29 * 8)); // Placeholder trade

        toolsmithPurchases.add(new TradeItem(ItemRegistry.getToolsmithEfficiency(), 64, 1, 4,29 * 32)); // Placeholder trade
        toolsmithPurchases.add(new TradeItem(ItemRegistry.getToolsmithUnbreaking(), 64, 1, 4,29 * 32)); // Placeholder trade
        toolsmithPurchases.add(new TradeItem(ItemRegistry.getEpicToolReforge(), 32, 1, 4,29 * 16)); // Placeholder trade

        toolsmithPurchases.add(new TradeItem(new ItemStack(Material.ANCIENT_DEBRIS, 1), 64, 1, 5,29 * 32)); // Placeholder trade
        toolsmithPurchases.add(new TradeItem(ItemRegistry.getToolsmithEnchant(), 64, 1, 5,29 * 32)); // Placeholder trade
        toolsmithPurchases.add(new TradeItem(ItemRegistry.getToolsmithEnchantTwo(), 128, 1, 5,29 * 64)); // Placeholder trade
        toolsmithPurchases.add(new TradeItem(ItemRegistry.getLegendaryToolReforge(), 64, 1, 5,29 * 32)); // Placeholder trade


        purchaseWhitelist.put(Villager.Profession.TOOLSMITH, toolsmithPurchases);

        List<TradeItem> toolsmithSells = new ArrayList<>();
        toolsmithSells.add(new TradeItem(new ItemStack(Material.COAL, 3), 1, 3, 1,29 * 1)); // Placeholder trade
        toolsmithSells.add(new TradeItem(new ItemStack(Material.CHARCOAL, 3), 2, 3, 1,29 * 1)); // Placeholder trade
        toolsmithSells.add(new TradeItem(new ItemStack(Material.IRON_INGOT, 3), 1, 3, 1,29 * 1)); // Placeholder trade
        toolsmithSells.add(new TradeItem(new ItemStack(Material.GOLD_INGOT, 3), 2, 3, 1,29 * 1)); // Placeholder trade
        toolsmithSells.add(new TradeItem(new ItemStack(Material.DIAMOND, 1), 6, 1, 1,29 * 3)); // Placeholder trade        toolsmithSells.add(new TradeItem(diamondGemstone(), 128, 1, 3)); // Placeholder trade
        toolsmithSells.add(new TradeItem(ItemRegistry.getLapisGemstone(), 32, 1, 3,29 * 16)); // Placeholder trade
        toolsmithSells.add(new TradeItem(ItemRegistry.getEmeraldGemstone(), 64, 1, 3,29 * 32)); // Placeholder trade
        toolsmithSells.add(new TradeItem(ItemRegistry.getRedstoneGemstone(), 32, 1, 3,29 * 16)); // Placeholder trade
        toolsmithSells.add(new TradeItem(ItemRegistry.getDiamondGemstone(), 64, 1, 3,29 * 32)); // Placeholder trade


        sellWhitelist.put(Villager.Profession.TOOLSMITH, toolsmithSells);

// Armorer
        List<TradeItem> armorerPurchases = new ArrayList<>();
        armorerPurchases.add(new TradeItem(new ItemStack(Material.IRON_ORE, 4), 7, 4, 1,29 * 3)); // Placeholder trade
        armorerPurchases.add(new TradeItem(ItemRegistry.getCommonArmorReforge(), 8, 1, 1,29 * 4)); // Placeholder trade

        armorerPurchases.add(new TradeItem(new ItemStack(Material.ANVIL, 1), 24, 1, 2,29 * 12)); // Placeholder trade
        armorerPurchases.add(new TradeItem(ItemRegistry.getUncommonArmorReforge(), 16, 1, 2,29 * 8)); // Placeholder trade

        armorerPurchases.add(new TradeItem(new ItemStack(Material.GOLD_ORE, 4), 6, 4, 3,29 * 3)); // Placeholder trade
        armorerPurchases.add(new TradeItem(ItemRegistry.getRareArmorReforge(), 32, 1, 3,29 * 16)); // Placeholder trade

        armorerPurchases.add(new TradeItem(new ItemStack(Material.NETHERITE_UPGRADE_SMITHING_TEMPLATE, 2), 32, 2, 4,29 * 16)); // Placeholder trade
        armorerPurchases.add(new TradeItem(ItemRegistry.getEpicArmorReforge(), 64, 1, 4,29 * 32)); // Placeholder trade

        armorerPurchases.add(new TradeItem(ItemRegistry.getRandomArmorTrim(), 64, 1, 4,29 * 32)); // Placeholder trade
        armorerPurchases.add(new TradeItem(ItemRegistry.getArmorSmithProtection(), 64, 1, 4,29 * 32)); // Placeholder trade
        armorerPurchases.add(new TradeItem(ItemRegistry.getArmorSmithRespiration(), 64, 1, 4,29 * 32)); // Placeholder trade
        armorerPurchases.add(new TradeItem(ItemRegistry.getArmorSmithThorns(), 64, 1, 4,29 * 32)); // Placeholder trade
        armorerPurchases.add(new TradeItem(ItemRegistry.getArmorSmithFeatherFalling(), 64, 1, 4,29 * 32)); // Placeholder trade

        armorerPurchases.add(new TradeItem(ItemRegistry.getLegendaryArmorReforge(), 128, 1, 5,29 * 64)); // Placeholder trade
        armorerPurchases.add(new TradeItem(ItemRegistry.getArmorerEnchant(), 16, 1, 5,29 * 8)); // Placeholder trade
        armorerPurchases.add(new TradeItem(ItemRegistry.getArmorsmithReforge(), 32, 1, 5,29 * 16)); // Placeholder trade
        armorerPurchases.add(new TradeItem(ItemRegistry.getArmorsmithReforgeTwo(), 64, 1, 5,29 * 32)); // Placeholder trade
        //armorerPurchases.add(new TradeItem(armorsmithReforgeThree, 64, 1, 5)); // Placeholder trade



        armorerPurchases.add(new TradeItem(new ItemStack(Material.ANCIENT_DEBRIS, 1), 64, 1, 5,29 * 32)); // Placeholder trade
        purchaseWhitelist.put(Villager.Profession.ARMORER, armorerPurchases);

        List<TradeItem> armorerSells = new ArrayList<>();
        armorerSells.add(new TradeItem(new ItemStack(Material.LEATHER_HORSE_ARMOR, 1), 6, 1, 1,29 * 3)); // Placeholder trade
        armorerSells.add(new TradeItem(new ItemStack(Material.IRON_HORSE_ARMOR, 1), 12, 1, 1,29 * 6)); // Placeholder trade
        armorerSells.add(new TradeItem(new ItemStack(Material.GOLDEN_HORSE_ARMOR, 1), 24, 1, 1,29 * 12)); // Placeholder trade
        armorerSells.add(new TradeItem(new ItemStack(Material.DIAMOND_HORSE_ARMOR, 1), 48, 1, 1,29 * 24)); // Placeholder trade

        armorerSells.add(new TradeItem(new ItemStack(Material.IRON_INGOT, 3), 1, 3, 1,29 * 1)); // Placeholder trade
        armorerSells.add(new TradeItem(new ItemStack(Material.GOLD_INGOT, 3), 2, 3, 1,29 * 1)); // Placeholder trade
        armorerSells.add(new TradeItem(new ItemStack(Material.DIAMOND, 1), 6, 1, 1,29 * 3)); // Placeholder trade
        armorerSells.add(new TradeItem(ItemRegistry.getAquaAffinity(), 16, 1, 2,29 * 8)); // Placeholder trade
        armorerSells.add(new TradeItem(ItemRegistry.getSwiftSneak(), 16, 1, 2,29 * 8)); // Placeholder trade

        sellWhitelist.put(Villager.Profession.ARMORER, armorerSells);



        // Librarian
        List<TradeItem> librarianPurchases = new ArrayList<>();
        librarianPurchases.add(new TradeItem(new ItemStack(Material.BOOK, 1), 3, 1, 1,29 * 1)); // Placeholder trade

        librarianPurchases.add(new TradeItem(new ItemStack(Material.BOOKSHELF, 3), 12, 3, 2,29 * 6)); // Placeholder trade
        librarianPurchases.add(new TradeItem(new ItemStack(Material.LANTERN, 3), 4, 3, 2,29 * 2)); // Placeholder trade

        librarianPurchases.add(new TradeItem(new ItemStack(Material.GLASS, 6), 9, 3, 3,29 * 3)); // Placeholder trade
        librarianPurchases.add(new TradeItem(ItemRegistry.getLibrarianEnchantmentTwo(), 16, 1, 3,29 * 8)); // Placeholder trade

        librarianPurchases.add(new TradeItem(ItemRegistry.getIronGolem(), 16, 1, 4,29 * 8)); // Placeholder trade

        librarianPurchases.add(new TradeItem(ItemRegistry.getLibrarianEnchant(), 32, 1, 5,29 * 16)); // Placeholder trade


        purchaseWhitelist.put(Villager.Profession.LIBRARIAN, librarianPurchases);

        List<TradeItem> librarianSells = new ArrayList<>();
        librarianSells.add(new TradeItem(new ItemStack(Material.NAME_TAG, 1), 8, 1, 3,29 * 4)); // Placeholder trade
        librarianSells.add(new TradeItem(new ItemStack(Material.PAPER, 3), 1, 3, 1,29 * 1)); // Placeholder trade
        librarianSells.add(new TradeItem(new ItemStack(Material.BOOK, 3), 1, 3, 1,29 * 1)); // Placeholder trade
        librarianSells.add(new TradeItem(new ItemStack(Material.ENCHANTED_BOOK, 1), 8, 1, 1,29 * 4)); // Placeholder trade
        librarianSells.add(new TradeItem(new ItemStack(Material.ENCHANTED_GOLDEN_APPLE, 1), 16, 1, 1,29 * 8)); // Placeholder trade
        librarianSells.add(new TradeItem(ItemRegistry.getForbiddenBook(), 8, 1, 1,29 * 4)); // Placeholder trade


        sellWhitelist.put(Villager.Profession.LIBRARIAN, librarianSells);
// Fishermans
        List<TradeItem> fishingPurchases = new ArrayList<>();
        {
            fishingPurchases.add(new TradeItem(new ItemStack(Material.FISHING_ROD, 1), 6, 1, 1,29 * 3));
            fishingPurchases.add(new TradeItem(new ItemStack(Material.BUCKET, 1), 8, 1, 2,29 * 4));
            fishingPurchases.add(new TradeItem(new ItemStack(Material.LAPIS_LAZULI, 4),  8, 4, 2,29 * 4));
            fishingPurchases.add(new TradeItem(ItemRegistry.getShallowShell(),  16, 1, 2,29 * 8));

            fishingPurchases.add(new TradeItem(ItemRegistry.getShell(),  32, 1, 3,29 * 16));
            fishingPurchases.add(new TradeItem(ItemRegistry.getFishermanReforge(),  64, 1, 3,29 * 32));
            fishingPurchases.add(new TradeItem(new ItemStack(Material.CAMPFIRE, 2), 12, 2, 3,29 * 6));

            fishingPurchases.add(new TradeItem(ItemRegistry.getDeepShell(),  64, 1, 4,29 * 32));
            fishingPurchases.add(new TradeItem(ItemRegistry.getFishermanLure(),  64, 1, 4,29 * 32));
            fishingPurchases.add(new TradeItem(ItemRegistry.getFishermanLuckoftheSea(),  64, 1, 4,29 * 32));

            fishingPurchases.add(new TradeItem(ItemRegistry.getAbyssalShell(),  64, 1, 5,29 * 32));
            fishingPurchases.add(new TradeItem(ItemRegistry.getAbyssalInk(),  64, 1, 5,29 * 32));
            fishingPurchases.add(new TradeItem(ItemRegistry.getAbyssalVenom(),  64, 1, 5,29 * 32));
            fishingPurchases.add(new TradeItem(ItemRegistry.getFisherEnchant(),  40, 1, 5,29 * 20));
            fishingPurchases.add(new TradeItem(ItemRegistry.getFishingEnchant(),  40, 1, 5,29 * 20));
            //add sea creature drops
        }
        purchaseWhitelist.put(Villager.Profession.FISHERMAN, fishingPurchases);

        List<TradeItem> fishingSells = new ArrayList<>();
        {
            fishingSells.add(new TradeItem(new ItemStack(Material.STRING, 4), 1, 4, 1,29 * 1)); // Placeholder trade
            fishingSells.add(new TradeItem(new ItemStack(Material.INK_SAC, 4), 1, 4, 1,29 * 1)); // Placeholder trade
            fishingSells.add(new TradeItem(new ItemStack(Material.COD, 8), 1, 8, 1,29 * 1)); // Placeholder trade
            fishingSells.add(new TradeItem(new ItemStack(Material.SALMON, 8), 1, 8, 1,29 * 1)); // Placeholder trade
            fishingSells.add(new TradeItem(new ItemStack(Material.PUFFERFISH, 1), 1, 1, 2,29 * 1)); // Placeholder trade
            fishingSells.add(new TradeItem(new ItemStack(Material.TROPICAL_FISH, 1), 1, 1, 2,29 * 1)); // Placeholder trade
            fishingSells.add(new TradeItem(new ItemStack(Material.GLOW_INK_SAC, 4), 1, 4, 2,29 * 1)); // Placeholder trade
            fishingSells.add(new TradeItem(new ItemStack(ItemRegistry.getTooth()), 2, 1, 2,29 * 2)); // Placeholder trade
            fishingSells.add(new TradeItem(new ItemStack(ItemRegistry.getLuminescentInk()), 2, 1, 2,29 * 1)); // Placeholder trade
            fishingSells.add(new TradeItem(new ItemStack(Material.NAUTILUS_SHELL), 4, 1, 2,29 * 2)); // Placeholder trade
            fishingSells.add(new TradeItem(new ItemStack(Material.HEART_OF_THE_SEA), 16, 1, 2,29 * 8)); // Placeholder trade
            fishingSells.add(new TradeItem(new ItemStack(ItemRegistry.getFishBone()), 1, 1, 2,29 * 1)); // Placeholder trade
            fishingSells.add(new TradeItem(new ItemStack(new ItemStack(Material.TRIDENT)), 24, 1, 2,29 * 12)); // Placeholder trade

        }
        sellWhitelist.put(Villager.Profession.FISHERMAN, fishingSells);

        List<TradeItem> butcherPurchases = new ArrayList<>();
        {



            // Add each recipe to the butcher's trades



            butcherPurchases.add(new TradeItem(new ItemStack(ItemRegistry.getButcherEnchant()), 16, 1, 5,29 * 8));
        }
        purchaseWhitelist.put(Villager.Profession.BUTCHER, butcherPurchases);

        List<TradeItem> butcherSells = new ArrayList<>();
        {
            butcherSells.add(new TradeItem(new ItemStack(Material.COAL, 3), 1, 3, 1,29 * 1));
            butcherSells.add(new TradeItem(new ItemStack(Material.CHICKEN, 1), 1, 1, 1,29 * 1));
            butcherSells.add(new TradeItem(new ItemStack(Material.MUTTON, 1), 1, 1, 1,29 * 1));
            butcherSells.add(new TradeItem(new ItemStack(Material.BEEF, 1), 1, 1, 2,29 * 1));
            butcherSells.add(new TradeItem(new ItemStack(Material.PORKCHOP, 1), 1, 1, 2,29 * 1));
            butcherSells.add(new TradeItem(new ItemStack(Material.RABBIT, 1), 2, 1, 3,29 * 1));

        }
        sellWhitelist.put(Villager.Profession.BUTCHER, butcherSells);



        List<TradeItem> farmerPurchases = new ArrayList<>();
        {
            farmerPurchases.add(new TradeItem(new ItemStack(Material.BREAD, 3), 1, 3, 1,29 * 1)); // Level 1 trade
            farmerPurchases.add(new TradeItem(new ItemStack(Material.WHEAT_SEEDS, 12), 3, 12, 1,29 * 1)); // Level 1 trade

            farmerPurchases.add(new TradeItem(new ItemStack(Material.PUMPKIN_SEEDS, 3), 32, 3, 2,29 * 16)); // Level 1 trade
            farmerPurchases.add(new TradeItem(new ItemStack(Material.MELON_SEEDS, 3), 32, 3, 2,29 * 16)); // Level 1 trade
            farmerPurchases.add(new TradeItem(new ItemStack(Material.CAKE, 1), 6, 1, 3,29 * 3)); // Level 2 trade
            farmerPurchases.add(new TradeItem(ItemRegistry.getWheatSeeder(), 16, 1, 3,29 * 3)); // Level 2 trade
            farmerPurchases.add(new TradeItem(ItemRegistry.getBeetrootSeeder(), 16, 1, 3,29 * 3)); // Level 2 trade
            farmerPurchases.add(new TradeItem(ItemRegistry.getCarrotSeeder(), 16, 1, 3,29 * 3)); // Level 2 trade
            farmerPurchases.add(new TradeItem(ItemRegistry.getPotatoSeeder(), 16, 1, 3,29 * 3)); // Level 2 trade

            farmerPurchases.add(new TradeItem(new ItemStack(Material.WATER_BUCKET, 1), 3, 1, 3,29 * 1)); // Level 1 trade
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

            farmerPurchases.add(new TradeItem(new ItemStack(Material.GOLDEN_CARROT, 4), 3, 4, 4,29 * 1)); // Level 2 trade

            farmerPurchases.add(new TradeItem(new ItemStack(Material.SNIFFER_EGG, 1), 64, 1, 5,29 * 32)); // Level 2 trade
            farmerPurchases.add(new TradeItem(new ItemStack(ItemRegistry.getFarmerEnchant()), 64, 1, 5,29 * 32)); // Level 2 trade
            farmerPurchases.add(new TradeItem(ItemRegistry.getAutoComposter(), 64, 1, 5,29 * 32)); // Level 2 trade
        }
        purchaseWhitelist.put(Villager.Profession.FARMER, farmerPurchases);



        List<TradeItem> farmerSells = new ArrayList<>();
        {
            farmerSells.add(new TradeItem(new ItemStack(Material.WHEAT, 12), 1, 12, 1,29 * 6)); // Level 1 trade
            farmerSells.add(new TradeItem(new ItemStack(Material.CARROT, 12), 1, 12, 1,29 * 6)); // Level 2 trade
            farmerSells.add(new TradeItem(new ItemStack(Material.POTATO, 12), 1, 12, 1,29 * 6)); // Level 2 trade
            farmerSells.add(new TradeItem(new ItemStack(Material.WHEAT_SEEDS, 32), 1, 32, 1,29 * 16)); // Level 2 trade

            farmerSells.add(new TradeItem(new ItemStack(Material.BEETROOT, 4), 1, 4, 2,29 * 2)); // Level 2 trade
            farmerSells.add(new TradeItem(ItemRegistry.getOrganicSoil(), 3, 4, 2,29 * 2)); // Level 2 trade
            farmerSells.add(new TradeItem(new ItemStack(Material.EGG, 6), 2, 6, 2,29 * 1)); // Level 3 trade
            farmerSells.add(new TradeItem(new ItemStack(Material.SUGAR_CANE, 6), 1, 6, 2,29 * 1)); // Level 3 trade
            farmerSells.add(new TradeItem(new ItemStack(Material.APPLE, 1), 1, 1, 2,29 * 1)); // Level 3 trade

            farmerSells.add(new TradeItem(new ItemStack(Material.MELON_SLICE, 12), 1, 12, 3,29 * 1)); // Level 3 trade
            farmerSells.add(new TradeItem(new ItemStack(Material.PUMPKIN, 8), 1, 8, 3,29 * 1)); // Level 3 trade

            farmerSells.add(new TradeItem(new ItemStack(Material.BROWN_MUSHROOM, 1), 1, 1, 4,29 * 1)); // Level 3 trade
            farmerSells.add(new TradeItem(new ItemStack(Material.RED_MUSHROOM, 1), 1, 1, 4,29 * 1)); // Level 3 trade
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
        int barteringExperience = tradeItem.getBarteringExperience(); // Get bartering experience

        // Check if player has enough emeralds
        if (hasEnoughItems(player.getInventory(), new ItemStack(Material.EMERALD), emeraldCost)) {
            // Get the pet manager instance
            PetManager petManager = PetManager.getInstance(MinecraftNew.getInstance());
            PetManager.Pet activePet = petManager.getActivePet(player);

            // Adjust cost based on HAGGLE perk
            double finalCost = emeraldCost; // Use double for intermediate calculations
            if (activePet != null && activePet.hasPerk(PetManager.PetPerk.HAGGLE)) {
                int petLevel = activePet.getLevel(); // Assume Pet has a `getLevel()` method
                double maxDiscount = 0.5; // 50% discount at max level
                int maxLevel = 100; // Define the max level of the pet

                // Calculate discount proportionally to pet's level
                double discountFactor = maxDiscount * ((double) petLevel / maxLevel);
                finalCost *= (1 - discountFactor); // Apply pet discount

                player.sendMessage(ChatColor.GREEN + "Haggle perk applied! You paid " + finalCost + " emeralds.");
            }

            // Apply bartering level discount
            XPManager xpManager = new XPManager(plugin);
            int barteringLevel = xpManager.getPlayerLevel(player, "Bartering"); // Get the player's bartering level

            // Calculate the bartering discount (0.1% per level, max 10%)
            double barteringDiscount = Math.min(0.1, (barteringLevel * 0.001)); // 0.1% per level, max 10%
            finalCost *= (1 - barteringDiscount); // Apply bartering discount

            // Round down to the nearest emerald
            int finalCostRounded = (int) Math.floor(finalCost);
            finalCostRounded = Math.max(1, finalCostRounded); // Ensure minimum cost of 1 emerald

            player.sendMessage(ChatColor.GREEN + "Bartering level discount applied! You paid " + finalCostRounded + " emeralds.");

            // Remove emeralds based on the final cost
            removeItems(player.getInventory(), Material.EMERALD, finalCostRounded);

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

            // Add bartering experience to the player
            xpManager.addXP(player, "Bartering", barteringExperience);
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
        private final int barteringExperience; // New field for bartering experience

        public TradeItem(ItemStack item, int emeraldValue, int quantity, int requiredLevel, int barteringExperience) {
            this.item = item;
            this.emeraldValue = emeraldValue;
            this.quantity = quantity;
            this.requiredLevel = requiredLevel;
            this.barteringExperience = barteringExperience; // Initialize the new field
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

        public int getBarteringExperience() { // New getter method
            return barteringExperience;
        }
    }
}

