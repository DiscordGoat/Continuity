package goat.minecraft.minecraftnew;

import goat.minecraft.minecraftnew.cut_content.CancelBrewing;
import goat.minecraft.minecraftnew.cut_content.Collections;
import goat.minecraft.minecraftnew.other.additionalfunctionality.*;
import goat.minecraft.minecraftnew.subsystems.villagers.professions.bartender.BartenderVillagerManager;
import goat.minecraft.minecraftnew.subsystems.villagers.professions.engineer.EngineerVillagerManager;
import goat.minecraft.minecraftnew.subsystems.villagers.professions.engineer.EngineeringProfessionListener;
import goat.minecraft.minecraftnew.other.meritperks.*;
import goat.minecraft.minecraftnew.other.qol.*;
import goat.minecraft.minecraftnew.cut_content.recipes.LockedRecipeManager;
import goat.minecraft.minecraftnew.cut_content.recipes.RecipeManager;
import goat.minecraft.minecraftnew.cut_content.recipes.RecipesCommand;
import goat.minecraft.minecraftnew.cut_content.recipes.ViewRecipeCommand;
import goat.minecraft.minecraftnew.subsystems.brewing.*;
import goat.minecraft.minecraftnew.subsystems.brewing.custompotions.*;
import goat.minecraft.minecraftnew.subsystems.combat.*;
import goat.minecraft.minecraftnew.subsystems.combat.CombatSubsystemManager;

import goat.minecraft.minecraftnew.subsystems.culinary.ShelfManager;
import goat.minecraft.minecraftnew.subsystems.enchanting.*;
import goat.minecraft.minecraftnew.subsystems.farming.VerdantRelicsSubsystem;
import goat.minecraft.minecraftnew.subsystems.forestry.Forestry;
import goat.minecraft.minecraftnew.subsystems.forestry.ForestryPetManager;

import goat.minecraft.minecraftnew.subsystems.pets.petdrops.*;
import goat.minecraft.minecraftnew.subsystems.pets.petdrops.WitherPetGrantListener;
import goat.minecraft.minecraftnew.subsystems.smithing.tierreforgelisteners.*;
import goat.minecraft.minecraftnew.subsystems.villagers.HireVillager;
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
import goat.minecraft.minecraftnew.subsystems.smithing.talismans.*;
import goat.minecraft.minecraftnew.subsystems.pets.perks.AutoComposter;
import goat.minecraft.minecraftnew.subsystems.fishing.SeaCreatureDeathEvent;
import goat.minecraft.minecraftnew.subsystems.mining.Mining;
import goat.minecraft.minecraftnew.subsystems.smithing.AnvilRepair;
import goat.minecraft.minecraftnew.subsystems.villagers.VillagerTradeManager;
import goat.minecraft.minecraftnew.subsystems.villagers.VillagerWorkCycleManager;

import goat.minecraft.minecraftnew.utils.commands.DiscsCommand;
import goat.minecraft.minecraftnew.utils.commands.MeritCommand;
import goat.minecraft.minecraftnew.utils.commands.SkillsCommand;
import goat.minecraft.minecraftnew.utils.developercommands.*;
import goat.minecraft.minecraftnew.utils.devtools.*;
import goat.minecraft.minecraftnew.utils.dimensions.end.BetterEnd;

import goat.minecraft.minecraftnew.subsystems.music.PigStepArena;
import goat.minecraft.minecraftnew.subsystems.realms.Tropic;
import org.bukkit.*;
import org.bukkit.event.Listener;
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
    private ItemDisplayManager displayManager;
    private PlayerOxygenManager playerOxygenManager;
    private UltimateEnchantingSystem ultimateEnchantmentManager;
    private static Collections collectionsManager;
    private EngineerVillagerManager engineerVillagerManager;
    private LockedRecipeManager lockedRecipeManager;
    private RecipeManager recipeManager;
    private ForestryPetManager forestryPetManager;
    private ShelfManager shelfManager;
    private DoubleEnderchest doubleEnderchest;



    private PotionBrewingSubsystem potionBrewingSubsystem;
    private VerdantRelicsSubsystem verdantRelicsSubsystem;
    private CombatSubsystemManager combatSubsystemManager;

    public ItemDisplayManager getItemDisplayManager() {
        return displayManager;
    }
    public ShelfManager getShelfManager() {
        return shelfManager;
    }

    @Override
    public void onEnable() {
        instance = this;
        ArmorStandCommand armorStandCommand = new ArmorStandCommand(this);
        armorStandCommand.removeInvisibleArmorStands();



        PotionManager.initialize(this);
        potionBrewingSubsystem = PotionBrewingSubsystem.getInstance(this);

        verdantRelicsSubsystem = VerdantRelicsSubsystem.getInstance(this);

        this.getCommand("pasteSchem").setExecutor(new PasteSchemCommand(this));

        this.shelfManager = new ShelfManager(this);


        Tropic tropicCommand = new Tropic(this);
        getCommand("tropic").setExecutor(tropicCommand);
        getCommand("tropic").setTabCompleter(tropicCommand);
        getCommand("decomission").setExecutor(tropicCommand);




        new BartenderVillagerManager(this);
        this.getCommand("getculinaryrecipe")
                .setExecutor(new GetCulinaryRecipeCommand(this));


// In your onEnable method



        PlayerOxygenManager oxygenManager = new PlayerOxygenManager(this);
        this.getCommand("setplayeroxygen").setExecutor(new SetPlayerOxygenCommand());



        new SetDurabilityCommand(this);
        this.getCommand("skin").setExecutor(new SkinCommand());
        if (getCommand("testdragon") != null) {
            getCommand("testdragon").setExecutor(new TestDragonCommand());
        } else {
            getLogger().severe("Command 'testdragon' not found in plugin.yml!");
        }
        PetManager petManager = PetManager.getInstance(this);
        this.getCommand("testpet").setExecutor(new PetTestCommand(petManager));
        this.getCommand("island").setExecutor(new IslandCommand());


        PigStepArena.init(this);
        if (!getDataFolder().exists()) {
            getDataFolder().mkdirs();
        }
        PlayerMeritManager playerData = PlayerMeritManager.getInstance(this);

        // Register commands
        getCommand("merits").setExecutor(new MeritCommand(this, playerData));
        getCommand("grantmerit").setExecutor(new GrantMerit(this, playerData));

        // Register event listener for inventory clicks

        //potions
        getServer().getPluginManager().registerEvents(new PotionOfStrength(), this);
        getServer().getPluginManager().registerEvents(new PotionOfSovereignty(), this);
        getServer().getPluginManager().registerEvents(new PotionOfLiquidLuck(), this);
        getServer().getPluginManager().registerEvents(new PotionOfFountains(), this);
        getServer().getPluginManager().registerEvents(new PotionOfSwiftStep(), this);
        getServer().getPluginManager().registerEvents(new PotionOfRecurve(), this);





        getServer().getPluginManager().registerEvents(new WitherPetGrantListener(petManager), this);
        getServer().getPluginManager().registerEvents(new MeritCommand(this, playerData), this);

        //add merit perks
        getServer().getPluginManager().registerEvents(new EnderMind(playerData), this);
        getServer().getPluginManager().registerEvents(new ObsidianPlating(playerData), this);
        getServer().getPluginManager().registerEvents(new BerserkersRage(this, playerData), this);
        getServer().getPluginManager().registerEvents(new TacticalRetreat(this, playerData), this);
        getServer().getPluginManager().registerEvents(new VampiricStrike(this, playerData), this);
        getServer().getPluginManager().registerEvents(new LordOfThunder(this, playerData), this);
        getServer().getPluginManager().registerEvents(new QuickSwap(this, playerData), this);
        getServer().getPluginManager().registerEvents(new Trainer(playerData), this);
        getServer().getPluginManager().registerEvents(new Restock(this, playerData), this);
        getServer().getPluginManager().registerEvents(new Rebreather(this, playerData), this);
        getServer().getPluginManager().registerEvents(new Keepinventory(this, playerData), this);
        getServer().getPluginManager().registerEvents(new MasterSmith(this, playerData), this);
        getServer().getPluginManager().registerEvents(new MasterBotanist(this, playerData), this);
        getServer().getPluginManager().registerEvents(new MasterBrewer(this, playerData), this);
        getServer().getPluginManager().registerEvents(new MasterChef(this, playerData), this);
        getServer().getPluginManager().registerEvents(new MasterThief(this, playerData), this);
        getServer().getPluginManager().registerEvents(new MasterDuelist(this, playerData), this);
        getServer().getPluginManager().registerEvents(new MasterAngler(this, playerData), this);
        getServer().getPluginManager().registerEvents(new MasterDiplomat(this, playerData), this);
        getServer().getPluginManager().registerEvents(new MasterDiffuser(this, playerData), this);
        getServer().getPluginManager().registerEvents(new MasterTrader(this, playerData), this);
        getServer().getPluginManager().registerEvents(new MasterEmployer(this, playerData), this);
        getServer().getPluginManager().registerEvents(new LoyaltyII(this, playerData), this);
        doubleEnderchest = new DoubleEnderchest(this, playerData);
        getServer().getPluginManager().registerEvents(doubleEnderchest, this);
        getServer().getPluginManager().registerEvents(new StrongDigestion(this, playerData), this);
        getServer().getPluginManager().registerEvents(new Icarus(this, playerData), this);
        getServer().getPluginManager().registerEvents(new AutoStrad(this, playerData), this);
        getServer().getPluginManager().registerEvents(new Resurrection(this, playerData), this);
        getServer().getPluginManager().registerEvents(new ResurrectionCharge2(this, playerData), this);
        getServer().getPluginManager().registerEvents(new ResurrectionCharge3(this, playerData), this);




        getServer().getPluginManager().registerEvents(new AFKDetector.PlayerMoveListener(), this);
        BetterEnd.init(this);

        new Sleep(this);
        getServer().getPluginManager().registerEvents(new ShulkerBox(), this);


        getCommand("end").setExecutor(new EndCommand());
        getCommand("nether").setExecutor(new NetherCommand());
        getCommand("overworld").setExecutor(new OverworldCommand());
        getCommand("resetend").setExecutor(new ResetEndCommand());
        getCommand("generatecontinuityisland").setExecutor(new GenerateContinuityIslandCommand());
        getCommand("continuitytp").setExecutor(new ContinuityTpCommand());


        xpManager = new XPManager(this);

        // Initialize the new combat subsystem (replaces old combat event registrations)
        try {
            combatSubsystemManager = new CombatSubsystemManager(this, xpManager);
            combatSubsystemManager.initialize();
            getLogger().info("[Combat] New combat subsystem initialized successfully");
        } catch (Exception e) {
            getLogger().severe("[Combat] Failed to initialize new combat subsystem: " + e.getMessage());
            e.printStackTrace();
        }

        CustomBundleGUI.init(this);
        //getServer().getPluginManager().registerEvents(new GamblingTable(this), this);

        forestryPetManager = new ForestryPetManager(this);

        Objects.requireNonNull(Bukkit.getWorld("world")).setGameRule(GameRule.DO_MOB_SPAWNING, true); // Re-enable monster spawns

        new ArmorStandCommand(this); // This will automatically set up the command

        recipeManager = new RecipeManager(this);
        recipeManager.registerAllRecipes();

        getCommand("recipes").setExecutor(new RecipesCommand(recipeManager));
        getCommand("discs").setExecutor(new DiscsCommand());
        getServer().getPluginManager().registerEvents(new DiscsCommand(), this);
        getCommand("viewrecipe").setExecutor(new ViewRecipeCommand(recipeManager));
        getServer().getPluginManager().registerEvents(new Doors(), this);
        getServer().getPluginManager().registerEvents(new ViewRecipeCommand.ViewRecipeListener(), this);

        displayManager = new ItemDisplayManager(this);

        getServer().getPluginManager().registerEvents(new EngineeringProfessionListener(this), this);
        engineerVillagerManager = new EngineerVillagerManager(this);

        getServer().getPluginManager().registerEvents(new UltimateEnchantmentListener(this), this);
        getServer().getPluginManager().registerEvents(new LightningFirePreventionListener(), this);
        getServer().getPluginManager().registerEvents(new Lavabug(this), this);

        UltimateEnchantingSystem ultimateEnchantingSystem = new UltimateEnchantingSystem();
        ultimateEnchantingSystem.registerCustomEnchants();
        // NOTE: Old HostilityManager commented out - new system handles hostility
        // HostilityManager hostilityManagermanager = HostilityManager.getInstance(this);
        // getCommand("hostility").setExecutor(hostilityManagermanager.new HostilityCommand());



        autoComposter = new AutoComposter(this);
        VillagerWorkCycleManager.getInstance(this);

        if (!getDataFolder().exists()) {
            getDataFolder().mkdirs();
        }
        this.getCommand("spawnseacreature").setExecutor(new SpawnSeaCreatureCommand());
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
// getServer().getPluginManager().registerEvents(new MobDamageHandler(), this); // Handled by new combat subsystem
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
        getServer().getPluginManager().registerEvents(new Elite(this), this);
        getServer().getPluginManager().registerEvents(new Claw(this), this);
        getServer().getPluginManager().registerEvents(new Alpha(this), this);
        getServer().getPluginManager().registerEvents(new Fetch(this), this);
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
        getServer().getPluginManager().registerEvents(new Groot(), this);
        getServer().getPluginManager().registerEvents(new ParkourRoll(this), this);
        getServer().getPluginManager().registerEvents(new Obsession(this), this);
        getServer().getPluginManager().registerEvents(new Earthworm(this), this);
        getServer().getPluginManager().registerEvents(new SpiderSteve(this), this);

        this.getCommand("givecustomitem").setExecutor(new GiveCustomItem());


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
        instance = this;

        new PlayerTabListUpdater(this, xpManager);
        this.getCommand("xp").setExecutor(xpManager);
        this.getCommand("loadsubsystems").setExecutor(new LoadSubsystemsCommand(this));
        this.getCommand("skills").setExecutor(new SkillsCommand(xpManager));

        getCommand("getpet").setExecutor(new PetCommand(petManager));
        getServer().getPluginManager().registerEvents(new FishingEvent(), MinecraftNew.getInstance());
        getServer().getPluginManager().registerEvents(SpawnMonsters.getInstance(xpManager), this);
        getServer().getPluginManager().registerEvents(new KillMonster(), MinecraftNew.getInstance());
        getServer().getPluginManager().registerEvents(new Mining(), MinecraftNew.getInstance());
        getServer().getPluginManager().registerEvents(new goat.minecraft.minecraftnew.subsystems.mining.GemstoneApplicationSystem(this), this);
        // Initialize and register GemstoneUpgradeSystem, then set reference in Mining class
        goat.minecraft.minecraftnew.subsystems.mining.GemstoneUpgradeSystem gemstoneUpgradeSystem = new goat.minecraft.minecraftnew.subsystems.mining.GemstoneUpgradeSystem(this);
        getServer().getPluginManager().registerEvents(gemstoneUpgradeSystem, this);
        goat.minecraft.minecraftnew.subsystems.mining.Mining.setUpgradeSystemInstance(gemstoneUpgradeSystem);
        getServer().getPluginManager().registerEvents(new goat.minecraft.minecraftnew.subsystems.mining.PowerCrystalSystem(this), this);

        // Register all gemstone upgrade listeners
        goat.minecraft.minecraftnew.subsystems.mining.gemstoneupgrades.YieldUpgradeListener yieldUpgradeListener = new goat.minecraft.minecraftnew.subsystems.mining.gemstoneupgrades.YieldUpgradeListener(this);
        yieldUpgradeListener.setUpgradeSystemInstance(gemstoneUpgradeSystem);
        getServer().getPluginManager().registerEvents(yieldUpgradeListener, this);
        

        goat.minecraft.minecraftnew.subsystems.mining.gemstoneupgrades.UtilityUpgradeListener utilityUpgradeListener = new goat.minecraft.minecraftnew.subsystems.mining.gemstoneupgrades.UtilityUpgradeListener(this);
        utilityUpgradeListener.setUpgradeSystemInstance(gemstoneUpgradeSystem);
        getServer().getPluginManager().registerEvents(utilityUpgradeListener, this);

        goat.minecraft.minecraftnew.subsystems.mining.gemstoneupgrades.MetalworkUpgradeListener metalworkUpgradeListener = new goat.minecraft.minecraftnew.subsystems.mining.gemstoneupgrades.MetalworkUpgradeListener(this);
        metalworkUpgradeListener.setUpgradeSystemInstance(gemstoneUpgradeSystem);
        getServer().getPluginManager().registerEvents(metalworkUpgradeListener, this);

        goat.minecraft.minecraftnew.subsystems.mining.gemstoneupgrades.GoldFeverUpgradeListener goldFeverUpgradeListener = new goat.minecraft.minecraftnew.subsystems.mining.gemstoneupgrades.GoldFeverUpgradeListener(this);
        goldFeverUpgradeListener.setUpgradeSystemInstance(gemstoneUpgradeSystem);
        getServer().getPluginManager().registerEvents(goldFeverUpgradeListener, this);
        
        getServer().getPluginManager().registerEvents(new PlayerLevel(MinecraftNew.getInstance(), xpManager), MinecraftNew.getInstance());

        getServer().getPluginManager().registerEvents(new FarmingEvent(), MinecraftNew.getInstance());
        getServer().getPluginManager().registerEvents(new SeaCreatureDeathEvent(), MinecraftNew.getInstance());
        //getServer().getPluginManager().registerEvents(new CancelBrewing(MinecraftNew.getInstance()), MinecraftNew.getInstance());
        getServer().getPluginManager().registerEvents(new RightClickArtifacts(this), this);
        getServer().getPluginManager().registerEvents(new AnvilRepair(MinecraftNew.getInstance()), this);
        getServer().getPluginManager().registerEvents(new InventoryClickListener(), this);
        getServer().getPluginManager().registerEvents(new EpicEnderDragonFight(this), this);
        ForestSpiritManager manager = ForestSpiritManager.getInstance(this);
        getServer().getPluginManager().registerEvents(manager, this);

        getServer().getPluginManager().registerEvents(new RareCombatDrops(), this);
        getServer().getPluginManager().registerEvents(new PlayerOxygenManager(this), this);


        Forestry forestry = Forestry.getInstance(this);
        forestry.init(this);


        getServer().getPluginManager().registerEvents(new ContinuityBoardManager(), this);
        getServer().getPluginManager().registerEvents(new SeaCreatureRegistry(), this);
        VillagerTradeManager tradeManager = VillagerTradeManager.getInstance(this);
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
        getServer().getPluginManager().registerEvents(new Merit(playerData), this);
        getServer().getPluginManager().registerEvents(new Cleaver(), this);
        getServer().getPluginManager().registerEvents(new Forge(), this);
        getServer().getPluginManager().registerEvents(new Shear(), this);
        getServer().getPluginManager().registerEvents(new MagicProtectionListener(), this);
        getServer().getPluginManager().registerEvents(new Alchemy(), this);
        getServer().getPluginManager().registerEvents(new AspectOfTheJourney(), this);
        getServer().getPluginManager().registerEvents(new Stun(), this);
        getServer().getPluginManager().registerEvents(new LethalReaction(), this);
        getServer().getPluginManager().registerEvents(new Bloodlust(this), this);
        getServer().getPluginManager().registerEvents(new Rappel(), this);
        getServer().getPluginManager().registerEvents(new Preservation(), this);

        CustomEnchantmentManager.registerEnchantment("Feed", 3, true);
        CustomEnchantmentManager.registerEnchantment("Merit", 5, true);
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
        getServer().getPluginManager().registerEvents(new WolfTameEvent(petManager), this);
        getServer().getPluginManager().registerEvents(new AxolotlInteractEvent(petManager), this);
        getServer().getPluginManager().registerEvents(new AllayInteractEvent(petManager), this);

        this.getCommand("clearpets").setExecutor(new ClearPetsCommand(this, petManager));
        // In your onEnable method
        // NOTE: Old combat handlers commented out - replaced by new combat subsystem
        // DamageNotifier damageNotifier = new DamageNotifier(this);
        // getServer().getPluginManager().registerEvents(damageNotifier, this);
        // getServer().getPluginManager().registerEvents(new CombatBuffs(), this);
        getServer().getPluginManager().registerEvents(new BowReforge(), this);
        villagerWorkCycleManager = VillagerWorkCycleManager.getInstance(this);
        getCommand("forceworkcycle").setExecutor(villagerWorkCycleManager);
        getCommand("repair").setExecutor(new RepairCommand());

        getServer().getPluginManager().registerEvents(new MusicDiscManager(this), this);

        //nms >


        if (!getDataFolder().exists()) {
            getDataFolder().mkdirs();
        }
        System.out.println("[MinecraftNew] Plugin enabled.");



        getServer().getPluginManager().registerEvents(new HordeInstinct(), this);











        //getServer().getPluginManager().registerEvents(new RealTimeDamageFeedback(), this);
    }


    @Override
    public void onDisable() {
        if (shelfManager != null) {
            shelfManager.onDisable();
        }
        if(potionBrewingSubsystem != null){
            potionBrewingSubsystem.onDisable();
        }
        if (verdantRelicsSubsystem != null) {
            verdantRelicsSubsystem.onDisable();
        }



        Speech speech = new Speech(this);
        speech.removeAllSpeech();

        MusicDiscManager musicDiscManager = new MusicDiscManager(this);
        musicDiscManager.resetHostilityLevelsOnDisable();
        if (displayManager != null) {
            displayManager.shutdown();
        }
//        if (meatCookingManager != null) {
//            meatCookingManager.cancelAllCookingsOnShutdown();
//        }
        if(culinarySubsystem != null) {
            culinarySubsystem.finalizeAllSessionsOnShutdown();
            getLogger().info("MinecraftNew disabled, all sessions finalized.");
            Bukkit.getLogger().info("[CulinarySubsystem] Data saved and plugin disabled.");
        }
        if (playerOxygenManager != null) {
            playerOxygenManager.saveOnShutdown();
        }

        if (combatSubsystemManager != null) {
            combatSubsystemManager.shutdown();
        }

        PetManager.getInstance(this).savePets();
        anvilRepair.saveAllInventories();
        cancelBrewing.saveAllInventories();
        if (doubleEnderchest != null) {
            doubleEnderchest.saveAllInventories();
        }
        System.out.println("[MinecraftNew] Plugin disabled.");//
    }
    public static MinecraftNew getInstance() {

        return instance; // Provide a static method to get the instance
    }
    public ForestryPetManager getForestryManager() {
        return forestryPetManager;
    }
}