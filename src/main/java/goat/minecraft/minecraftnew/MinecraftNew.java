package goat.minecraft.minecraftnew;

import goat.minecraft.minecraftnew.other.*;
import goat.minecraft.minecraftnew.other.engineer.EngineerVillagerManager;
import goat.minecraft.minecraftnew.other.engineer.EngineeringProfessionListener;
import goat.minecraft.minecraftnew.other.recipes.LockedRecipeManager;
import goat.minecraft.minecraftnew.other.recipes.RecipeManager;
import goat.minecraft.minecraftnew.other.recipes.RecipesCommand;
import goat.minecraft.minecraftnew.other.recipes.ViewRecipeCommand;
import goat.minecraft.minecraftnew.subsystems.combat.*;
import goat.minecraft.minecraftnew.subsystems.enchanting.*;
import goat.minecraft.minecraftnew.subsystems.forestry.Forestry;
import goat.minecraft.minecraftnew.subsystems.forestry.ForestryManager;
import goat.minecraft.minecraftnew.subsystems.villagers.HireVillager;
import goat.minecraft.minecraftnew.subsystems.brewing.CancelBrewing;
import goat.minecraft.minecraftnew.subsystems.culinary.CulinaryCauldron;
import goat.minecraft.minecraftnew.subsystems.culinary.CulinarySubsystem;
import goat.minecraft.minecraftnew.subsystems.combat.KnightMob;
import goat.minecraft.minecraftnew.subsystems.enchanting.enchantingeffects.*;
import goat.minecraft.minecraftnew.subsystems.farming.FarmingEvent;
import goat.minecraft.minecraftnew.subsystems.fishing.FishingEvent;
import goat.minecraft.minecraftnew.subsystems.fishing.SeaCreatureRegistry;
import goat.minecraft.minecraftnew.subsystems.fishing.SpawnSeaCreatureCommand;
import goat.minecraft.minecraftnew.subsystems.forestry.ForestSpiritManager;
import goat.minecraft.minecraftnew.subsystems.mining.PlayerOxygenManager;
import goat.minecraft.minecraftnew.subsystems.music.MusicDiscManager;
import goat.minecraft.minecraftnew.subsystems.pets.*;
import goat.minecraft.minecraftnew.subsystems.pets.perks.*;
import goat.minecraft.minecraftnew.subsystems.pets.perks.Float;
import goat.minecraft.minecraftnew.subsystems.pets.petdrops.AllayInteractEvent;
import goat.minecraft.minecraftnew.subsystems.pets.petdrops.AxolotlInteractEvent;
import goat.minecraft.minecraftnew.subsystems.pets.petdrops.CatTameEvent;
import goat.minecraft.minecraftnew.subsystems.pets.petdrops.PetDrops;
import goat.minecraft.minecraftnew.subsystems.smithing.tierreforgelisteners.ReforgeManager;
import goat.minecraft.minecraftnew.subsystems.smithing.talismans.*;
import goat.minecraft.minecraftnew.subsystems.smithing.tierreforgelisteners.ArmorReforge;
import goat.minecraft.minecraftnew.subsystems.smithing.tierreforgelisteners.SwordReforge;
import goat.minecraft.minecraftnew.subsystems.smithing.tierreforgelisteners.ToolReforge;
import goat.minecraft.minecraftnew.subsystems.pets.perks.AutoComposter;
import goat.minecraft.minecraftnew.subsystems.forestry.CustomItemManager;
import goat.minecraft.minecraftnew.subsystems.fishing.SeaCreatureDeathEvent;
import goat.minecraft.minecraftnew.subsystems.mining.Mining;
import goat.minecraft.minecraftnew.other.PlayerLevel;
import goat.minecraft.minecraftnew.subsystems.smithing.AnvilRepair;
import goat.minecraft.minecraftnew.utils.*;
import goat.minecraft.minecraftnew.subsystems.villagers.VillagerTradeManager;
import goat.minecraft.minecraftnew.subsystems.villagers.VillagerWorkCycleManager;
import goat.minecraft.minecraftnew.utils.commands.SkillsCommand;
import goat.minecraft.minecraftnew.utils.developercommands.*;
import goat.minecraft.minecraftnew.utils.end.CustomEndArchipelago;

import org.bukkit.*;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public class MinecraftNew extends JavaPlugin implements Listener {

    private VillagerWorkCycleManager villagerWorkCycleManager;
    private AutoComposter autoComposter;

    private XPManager xpManager;
    //instancing
    private static MinecraftNew instance;
    CancelBrewing cancelBrewing = new CancelBrewing(this);
    private AnvilRepair anvilRepair;
    private CulinarySubsystem culinarySubsystem;
    //private MeatCookingManager meatCookingManager;
    private ItemDisplayManager displayManager;
    private PlayerOxygenManager playerOxygenManager;
    private UltimateEnchantingSystem ultimateEnchantmentManager;
    private static Collections collectionsManager;
    private EngineerVillagerManager engineerVillagerManager;
    private LockedRecipeManager lockedRecipeManager;
    private RecipeManager recipeManager;
    private ForestryManager forestryManager;

    public static Collections getCollectionsManager() {
        return collectionsManager;
    }


    private void registerCustomRecipeCluster() {
        ItemStack engineeringItem = ItemRegistry.getEngineeringDegree();
        NamespacedKey key = new NamespacedKey(this, "engineering_profession");

        ShapedRecipe recipe = new ShapedRecipe(key, engineeringItem);
        recipe.shape("RRR", "RRR", "RRR");
        recipe.setIngredient('R', Material.REDSTONE_BLOCK);

        // Let’s lock this recipe behind the collection named "Redstone Collection"
        lockedRecipeManager.addLockedRecipe(
                key,
                "Redstone Collection",
                recipe,
                true // add to server
        );

        NamespacedKey notchAppleKey = new NamespacedKey(this, "notch_apple_recipe");
        lockedRecipeManager.addLockedRecipe(
                notchAppleKey,
                "Apple Mania", // EXACT name of the collection
                recipeManager.getCustomRecipes().get(notchAppleKey),
                false // We already called Bukkit.addRecipe(...) above
        );



        NamespacedKey petTrainingKey = new NamespacedKey(this, "pet_training_recipe");
        lockedRecipeManager.addLockedRecipe(
                petTrainingKey,
                "Minerals", // EXACT name of the collection
                recipeManager.getCustomRecipes().get(petTrainingKey),
                false // We already called Bukkit.addRecipe(...) above
        );



        NamespacedKey customMusicDiscKey = new NamespacedKey(this, "custom_music_disc_recipe");
        lockedRecipeManager.addLockedRecipe(
                customMusicDiscKey,
                "Collector of Skulls", // EXACT name of the collection
                recipeManager.getCustomRecipes().get(customMusicDiscKey),
                false // We already called Bukkit.addRecipe(...) above
        );
        NamespacedKey amethystKey = new NamespacedKey(this, "amethyst_block_to_shards");
        ShapedRecipe amethystRecipe = new ShapedRecipe(amethystKey, new ItemStack(Material.AMETHYST_SHARD, 4));
        amethystRecipe.shape("B"); // Single block in the center
        amethystRecipe.setIngredient('B', Material.AMETHYST_BLOCK);

        // Register the recipe
        Bukkit.addRecipe(amethystRecipe);

    }
    public LockedRecipeManager getLockedRecipeManager() {
        return lockedRecipeManager;
    }
    @Override
    public void onEnable() {
        CustomEndArchipelago.init(this);




        new Sleep(this);
        getServer().getPluginManager().registerEvents(new ShulkerBox(), this);


        getCommand("end").setExecutor(new EndCommand());
        getCommand("nether").setExecutor(new NetherCommand());
        getCommand("overworld").setExecutor(new OverworldCommand());
        getCommand("resetend").setExecutor(new ResetEndCommand());


        xpManager = new XPManager(this);
        CustomBundleGUI.init(this);
        //getServer().getPluginManager().registerEvents(new GamblingTable(this), this);

        forestryManager = new ForestryManager(this);
        getServer().getPluginManager().registerEvents(new Forestry(), this);

        Objects.requireNonNull(Bukkit.getWorld("world")).setGameRule(GameRule.DO_MOB_SPAWNING, true); // Re-enable monster spawns

        new ArmorStandCommand(this); // This will automatically set up the command

        recipeManager = new RecipeManager(this);
        recipeManager.registerAllRecipes();

        getCommand("recipes").setExecutor(new RecipesCommand(recipeManager));
        getCommand("viewrecipe").setExecutor(new ViewRecipeCommand(recipeManager));
        getServer().getPluginManager().registerEvents(new Doors(), this);
        getServer().getPluginManager().registerEvents(new ViewRecipeCommand.ViewRecipeListener(), this);

        displayManager = new ItemDisplayManager(this);
        collectionsManager = new Collections(this, displayManager);

        Objects.requireNonNull(getCommand("collection")).setExecutor(collectionsManager);


        lockedRecipeManager = new LockedRecipeManager(this, collectionsManager);
        lockedRecipeManager.init(); // registers event listener

        // Now inject the lockedRecipeManager back into the collections manager
        collectionsManager.setLockedRecipeManager(lockedRecipeManager);

        // Register any locked recipes
        registerCustomRecipeCluster();


        getServer().getPluginManager().registerEvents(new EngineeringProfessionListener(this), this);
        engineerVillagerManager = new EngineerVillagerManager(this);

        getServer().getPluginManager().registerEvents(new UltimateEnchantmentListener(this), this);

        UltimateEnchantingSystem ultimateEnchantingSystem = new UltimateEnchantingSystem();
        ultimateEnchantingSystem.registerCustomEnchants();
        HostilityManager hostilityManagermanager = HostilityManager.getInstance(this);




        // Register the /hostility command executor
        getCommand("hostility").setExecutor(hostilityManagermanager.new HostilityCommand());



        autoComposter = new AutoComposter(this);
        VillagerWorkCycleManager.getInstance(this);

        if (!getDataFolder().exists()) {
            getDataFolder().mkdirs();
        }
        this.getCommand("spawnseacreature").setExecutor(new SpawnSeaCreatureCommand());
        PetManager petManager = PetManager.getInstance(this);
        new SpeedBoost(petManager);
        // Initialize the culinary subsystem
        culinarySubsystem = CulinarySubsystem.getInstance(this);
        new CulinaryCauldron(this);
        new ParticlePetEffects(this);

        getLogger().info("MyPlugin has been enabled!");

        //meatCookingManager = new MeatCookingManager(this);



        Bukkit.getPluginManager().registerEvents(new HireVillager(), this);

        getServer().getPluginManager().registerEvents(new UltimateEnchantingSystem(), this);

        getServer().getPluginManager().registerEvents(new ArmorEquipListener(), this);
        getServer().getPluginManager().registerEvents(new Leap(this), this);
        getServer().getPluginManager().registerEvents(new MobDamageHandler(), this);
        getServer().getPluginManager().registerEvents(new SoftPaw(this), this);
        getServer().getPluginManager().registerEvents(new Comfortable(this), this);
        getServer().getPluginManager().registerEvents(new TerrorOfTheDeep(this), this);
        getServer().getPluginManager().registerEvents(new BonePlating(this), this);
        getServer().getPluginManager().registerEvents(new StrongSwimmer(this), this);
        getServer().getPluginManager().registerEvents(new ShotCalling(this), this);
        getServer().getPluginManager().registerEvents(new Recovery(this), this);
        getServer().getPluginManager().registerEvents(new TippedSlowness(this), this);
        getServer().getPluginManager().registerEvents(new LaserBeamPerkHandler(petManager), this);
        getServer().getPluginManager().registerEvents(new WeakBonePlating(this), this);
        getServer().getPluginManager().registerEvents(new Devour(this), this);
        getServer().getPluginManager().registerEvents(new Fireproof(this), this);
        getServer().getPluginManager().registerEvents(new AspectOfTheFrost(this), this);
        getServer().getPluginManager().registerEvents(new Blizzard(this), this);
        getServer().getPluginManager().registerEvents(new SecondWind(this), this);
        getServer().getPluginManager().registerEvents(new Rebirth(this), this);
        getServer().getPluginManager().registerEvents(new WalkingFortress(this), this);
        getServer().getPluginManager().registerEvents(new DiggingClaws(this), this);
        getServer().getPluginManager().registerEvents(new XRay(this), this);
        getServer().getPluginManager().registerEvents(new NoHibernation(this), this);
        getServer().getPluginManager().registerEvents(new Echolocation(this), this);
        getServer().getPluginManager().registerEvents(new MithrilMiner(this), this);
        getServer().getPluginManager().registerEvents(new EmeraldSeeker(this), this);
        getServer().getPluginManager().registerEvents(new Flight(this), this);
        getServer().getPluginManager().registerEvents(new Lullaby(this), this);
        getServer().getPluginManager().registerEvents(new Collector(this), this);
        getServer().getPluginManager().registerEvents(new Float(this), this);
        getServer().getPluginManager().registerEvents(new Greed(this), this);
        getServer().getPluginManager().registerEvents(new GreenThumb(this), this);
        getServer().getPluginManager().registerEvents(new Cultivation(this), this);
        getServer().getPluginManager().registerEvents(new Antidote(this), this);
        getServer().getPluginManager().registerEvents(new QuickDraw(this), this);
        getServer().getPluginManager().registerEvents(new SuperiorEndurance(this), this);
        getServer().getPluginManager().registerEvents(new BoneCold(this), this);
        getServer().getPluginManager().registerEvents(new Decay(this), this);
        getServer().getPluginManager().registerEvents(new SecretLegion(this), this);
        getServer().getPluginManager().registerEvents(new Blaze(this), this);
        getServer().getPluginManager().registerEvents(new AspectOfTheEnd(petManager), this);

        this.getCommand("givecustomitem").setExecutor(new GiveCustomItem());

        SpawnMonsters spawnMonsters = new SpawnMonsters(xpManager);
        getCommand("testskill").setExecutor(new TestSkillMessageCommand(xpManager));
        // Register events
        getServer().getPluginManager().registerEvents(petManager, this);
        // Register commands
        getCommand("pet").setExecutor((sender, command, label, args) -> {
            if (!(sender instanceof org.bukkit.entity.Player)) {
                sender.sendMessage("Only players can use this command.");
                return true;
            }
            org.bukkit.entity.Player player = (org.bukkit.entity.Player) sender;
            petManager.openPetGUI(player);
            return true;
        });
        // Load pets
        petManager.loadPets();

//        disableFlightForAllPlayers();
//        Bukkit.getWorlds().forEach(world -> {
//            world.setGameRule(GameRule.RANDOM_TICK_SPEED, 3); // Reset to default value
//        });

        getCommand("setpetlevel").setExecutor(new SetPetLevelCommand(petManager));

        anvilRepair = new AnvilRepair(this);
        CustomItemManager customItemManager = new CustomItemManager();
        instance = this;

        new PlayerTabListUpdater(this, xpManager);
        this.getCommand("xp").setExecutor(xpManager);
        this.getCommand("loadsubsystems").setExecutor(new LoadSubsystemsCommand(this));
        this.getCommand("skills").setExecutor(new SkillsCommand(xpManager));

        getCommand("getpet").setExecutor(new PetCommand(petManager));
        getServer().getPluginManager().registerEvents(new FishingEvent(), MinecraftNew.getInstance());
        getServer().getPluginManager().registerEvents(new SpawnMonsters(xpManager), MinecraftNew.getInstance());
        getServer().getPluginManager().registerEvents(new KillMonster(), MinecraftNew.getInstance());
        getServer().getPluginManager().registerEvents(new CustomItemManager(), MinecraftNew.getInstance());
        getServer().getPluginManager().registerEvents(new Mining(), MinecraftNew.getInstance());
        getServer().getPluginManager().registerEvents(new PlayerLevel(MinecraftNew.getInstance(), xpManager), MinecraftNew.getInstance());

        getServer().getPluginManager().registerEvents(new FarmingEvent(), MinecraftNew.getInstance());
        getServer().getPluginManager().registerEvents(new SeaCreatureDeathEvent(), MinecraftNew.getInstance());
        getServer().getPluginManager().registerEvents(new CancelBrewing(MinecraftNew.getInstance()), MinecraftNew.getInstance());
        getServer().getPluginManager().registerEvents(new RightClickArtifacts(MinecraftNew.getInstance()), MinecraftNew.getInstance());
        getServer().getPluginManager().registerEvents(new AnvilRepair(MinecraftNew.getInstance()), this);
        getServer().getPluginManager().registerEvents(new InventoryClickListener(), this);
        getServer().getPluginManager().registerEvents(new EpicEnderDragonFight(this), this);
        ForestSpiritManager manager = ForestSpiritManager.getInstance(this);
        getServer().getPluginManager().registerEvents(manager, this);

        getServer().getPluginManager().registerEvents(new RareCombatDrops(), this);
        getServer().getPluginManager().registerEvents(new PlayerOxygenManager(this), this);
        getServer().getPluginManager().registerEvents(new SeaCreatureRegistry(), this);
        getServer().getPluginManager().registerEvents(new VillagerTradeManager(this), this);
        getServer().getPluginManager().registerEvents(new CakeHungerListener(), this);
        getServer().getPluginManager().registerEvents(new CakeHungerListener(), this);
        getServer().getPluginManager().registerEvents(new PetDrops(this, PetManager.getInstance(this)), this);
        playerOxygenManager = PlayerOxygenManager.getInstance();
        getServer().getPluginManager().registerEvents(new ReforgeDamage(), this);
        getServer().getPluginManager().registerEvents(new ReforgeArmorToughness(), this);
        getServer().getPluginManager().registerEvents(new ReforgeArmor(), this);
        getServer().getPluginManager().registerEvents(new ReforgeDurability(), this);
        getServer().getPluginManager().registerEvents(new ReforgeSwiftBlade(), this);
        getServer().getPluginManager().registerEvents(new WaterLogged(this), this);

        getServer().getPluginManager().registerEvents(new Feed(), this);
        getServer().getPluginManager().registerEvents(new Cleaver(), this);
        getServer().getPluginManager().registerEvents(new Forge(), this);
        getServer().getPluginManager().registerEvents(new Shear(), this);
        getServer().getPluginManager().registerEvents(new MagicProtectionListener(), this);
        getServer().getPluginManager().registerEvents(new Alchemy(), this);
        getServer().getPluginManager().registerEvents(new AspectOfTheJourney(), this);
        getServer().getPluginManager().registerEvents(new Stun(), this);
        getServer().getPluginManager().registerEvents(new LethalReaction(), this);
        getServer().getPluginManager().registerEvents(new Bloodlust(), this);
        getServer().getPluginManager().registerEvents(new Rappel(), this);
        getServer().getPluginManager().registerEvents(new Preservation(), this);

        CustomEnchantmentManager.registerEnchantment("Feed", 3, true);
        CustomEnchantmentManager.registerEnchantment("Cleaver", 5, true);
        CustomEnchantmentManager.registerEnchantment("Call of the Void", 5, true);
        CustomEnchantmentManager.registerEnchantment("Savant", 1, true);
        CustomEnchantmentManager.registerEnchantment("Ventilation", 4, true);
        CustomEnchantmentManager.registerEnchantment("Forge", 5, true);
        CustomEnchantmentManager.registerEnchantment("Piracy", 5, true);
        CustomEnchantmentManager.registerEnchantment("Shear", 5, true);
        CustomEnchantmentManager.registerEnchantment("Physical Protection", 4, true);
        CustomEnchantmentManager.registerEnchantment("Alchemy", 4, true);
        CustomEnchantmentManager.registerEnchantment("Aspect of the Journey", 1, true);
        CustomEnchantmentManager.registerEnchantment("Stun", 5, true);
        CustomEnchantmentManager.registerEnchantment("Lethal Reaction", 10, true);
        CustomEnchantmentManager.registerEnchantment("Bloodlust", 5, true);
        CustomEnchantmentManager.registerEnchantment("Experience", 5, true);
        CustomEnchantmentManager.registerEnchantment("Rappel", 1, true);
        CustomEnchantmentManager.registerEnchantment("Preservation", 1, true);


        getServer().getPluginManager().registerEvents(new KnightMob(this), this);
        getServer().getPluginManager().registerEvents(new SwordReforge(new ReforgeManager()), this);
        getServer().getPluginManager().registerEvents(new ArmorReforge(new ReforgeManager()), this);
        getServer().getPluginManager().registerEvents(new ToolReforge(new ReforgeManager()), this);
        getServer().getPluginManager().registerEvents(new Experience(), this);

        getServer().getPluginManager().registerEvents(new CatTameEvent(petManager), this);
        getServer().getPluginManager().registerEvents(new AxolotlInteractEvent(petManager), this);
        getServer().getPluginManager().registerEvents(new AllayInteractEvent(petManager), this);

        this.getCommand("clearpets").setExecutor(new ClearPetsCommand(this, petManager));

        getServer().getPluginManager().registerEvents(new CombatBuffs(), this);

        villagerWorkCycleManager = VillagerWorkCycleManager.getInstance(this);
        getServer().getPluginManager().registerEvents(new MusicDiscManager(this), this);
        //nms >


        if (!getDataFolder().exists()) {
            getDataFolder().mkdirs();
        }
        System.out.println("[MinecraftNew] Plugin enabled.");



        getServer().getPluginManager().registerEvents(new HordeInstinct(), this);












    }


    @Override
    public void onDisable() {

        MusicDiscManager musicDiscManager = new MusicDiscManager(this);
        musicDiscManager.resetHostilityLevelsOnDisable();
        if (displayManager != null) {
            displayManager.shutdown();
        }
//        if (meatCookingManager != null) {
//            meatCookingManager.cancelAllCookingsOnShutdown();
//        }
        culinarySubsystem.finalizeAllSessionsOnShutdown();
        getLogger().info("MinecraftNew disabled, all sessions finalized.");
        Bukkit.getLogger().info("[CulinarySubsystem] Data saved and plugin disabled.");
        if (playerOxygenManager != null) {
            playerOxygenManager.saveOnShutdown();
        }
        PetManager.getInstance(this).savePets();
        anvilRepair.saveAllInventories();
        cancelBrewing.saveAllInventories();
        System.out.println("[MinecraftNew] Plugin disabled.");
    }
    public static MinecraftNew getInstance() {

        return instance; // Provide a static method to get the instance
    }
    public ForestryManager getForestryManager() {
        return forestryManager;
    }
}