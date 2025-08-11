package goat.minecraft.minecraftnew;
import goat.minecraft.minecraftnew.other.beacon.*;
import goat.minecraft.minecraftnew.other.additionalfunctionality.*;
import goat.minecraft.minecraftnew.other.resourcepack.ResourcePackListener;
import goat.minecraft.minecraftnew.subsystems.cartography.CartographyManager;
import goat.minecraft.minecraftnew.subsystems.gravedigging.Gravedigging;
import goat.minecraft.minecraftnew.subsystems.villagers.professions.bartender.BartenderVillagerManager;
import goat.minecraft.minecraftnew.subsystems.villagers.professions.engineer.EngineerVillagerManager;
import goat.minecraft.minecraftnew.subsystems.villagers.professions.engineer.EngineeringProfessionListener;
import goat.minecraft.minecraftnew.other.meritperks.*;
import goat.minecraft.minecraftnew.other.qol.*;
import goat.minecraft.minecraftnew.subsystems.brewing.*;
import goat.minecraft.minecraftnew.subsystems.brewing.custompotions.*;
import goat.minecraft.minecraftnew.subsystems.combat.*;
import goat.minecraft.minecraftnew.subsystems.combat.CombatSubsystemManager;
import goat.minecraft.minecraftnew.subsystems.combat.NightHordeTask;

import goat.minecraft.minecraftnew.subsystems.culinary.ShelfManager;
import goat.minecraft.minecraftnew.other.enchanting.*;
import goat.minecraft.minecraftnew.subsystems.farming.VerdantRelicsSubsystem;
import goat.minecraft.minecraftnew.subsystems.forestry.Forestry;
import goat.minecraft.minecraftnew.subsystems.forestry.ForestryPetManager;
import goat.minecraft.minecraftnew.subsystems.forestry.SaplingManager;
import goat.minecraft.minecraftnew.subsystems.farming.FestivalBeeManager;

import goat.minecraft.minecraftnew.subsystems.pets.petdrops.*;
import goat.minecraft.minecraftnew.subsystems.pets.petdrops.WitherPetGrantListener;
import goat.minecraft.minecraftnew.subsystems.smithing.tierreforgelisteners.*;
import goat.minecraft.minecraftnew.subsystems.villagers.HireVillager;
import goat.minecraft.minecraftnew.subsystems.culinary.CulinaryCauldron;
import goat.minecraft.minecraftnew.subsystems.culinary.CulinarySubsystem;
import goat.minecraft.minecraftnew.subsystems.culinary.CustomNutritionManager;
import goat.minecraft.minecraftnew.subsystems.culinary.NutritionCommand;
import goat.minecraft.minecraftnew.subsystems.combat.KnightMob;
import goat.minecraft.minecraftnew.other.enchanting.enchantingeffects.*;
import goat.minecraft.minecraftnew.subsystems.farming.FarmingEvent;
import goat.minecraft.minecraftnew.subsystems.fishing.FishingEvent;
import goat.minecraft.minecraftnew.subsystems.fishing.SeaCreatureRegistry;
import goat.minecraft.minecraftnew.subsystems.fishing.SpawnSeaCreatureCommand;
import goat.minecraft.minecraftnew.subsystems.fishing.SeaCreatureChanceCommand;
import goat.minecraft.minecraftnew.subsystems.fishing.TreasureChanceCommand;
import goat.minecraft.minecraftnew.subsystems.forestry.ForestSpiritManager;
import goat.minecraft.minecraftnew.subsystems.forestry.SpiritChanceCommand;
import goat.minecraft.minecraftnew.subsystems.mining.PlayerOxygenManager;
import goat.minecraft.minecraftnew.subsystems.mining.FortuneRemover;
import goat.minecraft.minecraftnew.subsystems.mining.MiningTalentFeatures;
import goat.minecraft.minecraftnew.subsystems.music.MusicDiscManager;
import goat.minecraft.minecraftnew.subsystems.pets.*;
import goat.minecraft.minecraftnew.subsystems.pets.perks.*;
import goat.minecraft.minecraftnew.subsystems.pets.perks.Float;
import goat.minecraft.minecraftnew.subsystems.smithing.talismans.*;
import goat.minecraft.minecraftnew.subsystems.pets.perks.AutoComposter;
import goat.minecraft.minecraftnew.subsystems.fishing.SeaCreatureDeathEvent;
import goat.minecraft.minecraftnew.subsystems.mining.Mining;
import goat.minecraft.minecraftnew.subsystems.smithing.AnvilRepair;
import goat.minecraft.minecraftnew.subsystems.smithing.ReforgeSubsystem;
import goat.minecraft.minecraftnew.subsystems.villagers.VillagerTradeManager;
import goat.minecraft.minecraftnew.subsystems.villagers.VillagerWorkCycleManager;
import goat.minecraft.minecraftnew.subsystems.villagers.MarketTrendManager;

import goat.minecraft.minecraftnew.utils.commands.DiscsCommand;
import goat.minecraft.minecraftnew.utils.commands.MeritCommand;
import goat.minecraft.minecraftnew.utils.commands.SkillsCommand;
import goat.minecraft.minecraftnew.utils.commands.AuraCommand;
import goat.minecraft.minecraftnew.utils.commands.ToggleCustomEnchantmentsCommand;
import goat.minecraft.minecraftnew.utils.commands.StatsCommand;
import goat.minecraft.minecraftnew.utils.commands.TogglePotionEffectsCommand;
import goat.minecraft.minecraftnew.utils.commands.ToggleBarsCommand;
import goat.minecraft.minecraftnew.utils.developercommands.*;
import goat.minecraft.minecraftnew.utils.developercommands.SetCustomDurabilityCommand;
import goat.minecraft.minecraftnew.utils.stats.StatsCalculator;
import goat.minecraft.minecraftnew.other.skilltree.SkillTreeManager;
import goat.minecraft.minecraftnew.utils.developercommands.AddTalentPointCommand;
import goat.minecraft.minecraftnew.utils.devtools.*;
import goat.minecraft.minecraftnew.utils.dimensions.end.BetterEnd;
import goat.minecraft.minecraftnew.other.trinkets.BankAccountManager;
import goat.minecraft.minecraftnew.other.trinkets.SatchelManager;
import goat.minecraft.minecraftnew.other.trinkets.SeedPouchManager;
import goat.minecraft.minecraftnew.other.trinkets.PotionPouchManager;
import goat.minecraft.minecraftnew.other.trinkets.CulinaryPouchManager;
import goat.minecraft.minecraftnew.other.trinkets.MiningPouchManager;
import goat.minecraft.minecraftnew.other.trinkets.TransfigurationPouchManager;
import goat.minecraft.minecraftnew.other.trinkets.LavaBucketManager;
import goat.minecraft.minecraftnew.other.trinkets.EnchantedClockManager;
import goat.minecraft.minecraftnew.other.trinkets.EnchantedHopperManager;
import goat.minecraft.minecraftnew.other.trinkets.TrinketManager;
import goat.minecraft.minecraftnew.other.auras.AuraManager;
import goat.minecraft.minecraftnew.other.armorsets.FlowManager;
import goat.minecraft.minecraftnew.other.armorsets.MonolithSetBonus;
import goat.minecraft.minecraftnew.other.health.HealthManager;
import goat.minecraft.minecraftnew.other.armorsets.DuskbloodSetBonus;
import goat.minecraft.minecraftnew.other.armorsets.DwellerSetBonus;
import goat.minecraft.minecraftnew.other.armorsets.FathmicIronSetBonus;
import goat.minecraft.minecraftnew.other.armorsets.NaturesWrathSetBonus;
import goat.minecraft.minecraftnew.other.armorsets.PastureshadeSetBonus;
import goat.minecraft.minecraftnew.other.armorsets.ScorechsteelSetBonus;
import goat.minecraft.minecraftnew.other.armorsets.ShadowstepSetBonus;
import goat.minecraft.minecraftnew.other.armorsets.SlayerSetBonus;
import goat.minecraft.minecraftnew.other.armorsets.ThunderforgeSetBonus;
import goat.minecraft.minecraftnew.other.armorsets.LostLegionSetBonus;
import goat.minecraft.minecraftnew.other.armorsets.CountershotSetBonus;
import goat.minecraft.minecraftnew.other.armorsets.StriderSetBonus;
import goat.minecraft.minecraftnew.other.durability.CustomDurabilityManager;
import goat.minecraft.minecraftnew.other.durability.HeirloomManager;
import goat.minecraft.minecraftnew.other.skilltree.SwiftStepMasteryBonus;
import goat.minecraft.minecraftnew.other.skilltree.FastFarmerBonus;
import goat.minecraft.minecraftnew.other.skilltree.SpectralArmorBonus;
import goat.minecraft.minecraftnew.other.structureblocks.StructureBlockManager;
import goat.minecraft.minecraftnew.other.structureblocks.GetStructureBlockCommand;
import goat.minecraft.minecraftnew.other.structureblocks.SetStructureBlockPowerCommand;
import goat.minecraft.minecraftnew.other.warpgate.WarpGateManager;
import goat.minecraft.minecraftnew.other.arenas.ArenaManager;
import goat.minecraft.minecraftnew.other.enchanting.CustomEnchantmentPreferences;
import goat.minecraft.minecraftnew.other.additionalfunctionality.EnvironmentSidebarPreferences;
import goat.minecraft.minecraftnew.subsystems.dragons.DragonFightManager;
import goat.minecraft.minecraftnew.subsystems.dragons.RefreshEndCommand;

import goat.minecraft.minecraftnew.subsystems.music.PigStepArena;
import goat.minecraft.minecraftnew.other.realms.Tropic;
import goat.minecraft.minecraftnew.other.realms.Frozen;
import net.citizensnpcs.api.CitizensAPI;
import org.bukkit.*;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;


import java.util.Objects;

public class MinecraftNew extends JavaPlugin implements Listener {

    private VillagerWorkCycleManager villagerWorkCycleManager;
    private AutoComposter autoComposter;

    private XPManager xpManager;
    //instancing
    private static MinecraftNew instance;
    private AnvilRepair anvilRepair;
    private CulinarySubsystem culinarySubsystem;
    private ItemDisplayManager displayManager;
    private PlayerOxygenManager playerOxygenManager;
    private UltimateEnchantingSystem ultimateEnchantmentManager;
    private EngineerVillagerManager engineerVillagerManager;
    private ForestryPetManager forestryPetManager;
    private SaplingManager saplingManager;
    private ShelfManager shelfManager;
    private DoubleEnderchest doubleEnderchest;
    private WarpGateManager warpGateManager;
    private ArenaManager arenaManager;
    private DragonFightManager dragonFightManager;
    private BeaconPassiveEffects beaconPassiveEffects;
    private MonolithSetBonus monolithSetBonus;
    private DuskbloodSetBonus duskbloodSetBonus;
    private DwellerSetBonus dwellerSetBonus;
    private FathmicIronSetBonus fathmicIronSetBonus;
    private NaturesWrathSetBonus naturesWrathSetBonus;
    private PastureshadeSetBonus pastureshadeSetBonus;
    private ScorechsteelSetBonus scorechsteelSetBonus;
    private ShadowstepSetBonus shadowstepSetBonus;
    private SlayerSetBonus slayerSetBonus;
    private ThunderforgeSetBonus thunderforgeSetBonus;
    private LostLegionSetBonus lostLegionSetBonus;
    private CountershotSetBonus countershotSetBonus;
    private StriderSetBonus striderSetBonus;
    private SwiftStepMasteryBonus swiftStepMasteryBonus;
    private FastFarmerBonus fastFarmerBonus;
    private SpectralArmorBonus spectralArmorBonus;
    private RejuvenationCatalystListener rejuvenationCatalystListener;
    private DeathCatalystListener deathCatalystListener;



    private PotionBrewingSubsystem potionBrewingSubsystem;
    private VerdantRelicsSubsystem verdantRelicsSubsystem;
    private ReforgeSubsystem reforgeSubsystem;
    private CombatSubsystemManager combatSubsystemManager;
    private AuraManager auraManager;
    private FlowManager flowManager;

    public ItemDisplayManager getItemDisplayManager() {
        return displayManager;
    }
    public ShelfManager getShelfManager() {
        return shelfManager;
    }

    public WarpGateManager getWarpGateManager() {
        return warpGateManager;
    }

    public ArenaManager getArenaManager() {
        return arenaManager;
    }

    public AnvilRepair getAnvilRepair() {
        return anvilRepair;
    }
    public void removeAllEnderDragonsFromOverworld() {
        // Loop through all loaded worlds
        for (World world : Bukkit.getServer().getWorlds()) {
            // Only target the Overworld environment
            if (world.getEnvironment() == World.Environment.NORMAL) {
                // Fetch all EnderDragon instances and remove them
                for (EnderDragon dragon : world.getEntitiesByClass(EnderDragon.class)) {
                    dragon.remove();
                    this.getLogger().info("Removed Ender Dragon from world: " + world.getName());
                }
            }
        }
    }
    @Override
    public void onEnable() {
        instance = this;

        // Initialize stats calculator singleton
        StatsCalculator.getInstance(this);
        getServer().getPluginManager()
                .registerEvents(new ResourcePackListener(), this);

        Bukkit.getOnlinePlayers().forEach(p ->
                p.stopAllSounds());
        removeAllCitizenEntities();

        HealthManager.startup();

        ArmorStandCommand armorStandCommand = new ArmorStandCommand(this);
        armorStandCommand.removeInvisibleArmorStands();



        PotionManager.initialize(this);
        potionBrewingSubsystem = PotionBrewingSubsystem.getInstance(this);

        verdantRelicsSubsystem = VerdantRelicsSubsystem.getInstance(this);
        new SetVerdantRelicGrowthPercentageCommand(this, verdantRelicsSubsystem);
        reforgeSubsystem = ReforgeSubsystem.getInstance(this);

        this.getCommand("pasteSchem").setExecutor(new PasteSchemCommand(this));

        this.shelfManager = new ShelfManager(this);
        this.warpGateManager = new WarpGateManager(this);
        this.dragonFightManager = new DragonFightManager(this);


        Tropic tropicCommand = new Tropic(this);
        getCommand("tropic").setExecutor(tropicCommand);
        getCommand("tropic").setTabCompleter(tropicCommand);
        getCommand("decomission").setExecutor(tropicCommand);

        Frozen frozenCommand = new Frozen(this);
        getCommand("frozen").setExecutor(frozenCommand);
        getCommand("frozen").setTabCompleter(frozenCommand);
        getCommand("defrost").setExecutor(frozenCommand);




        new BartenderVillagerManager(this);
        this.getCommand("getculinaryrecipe")
                .setExecutor(new GetCulinaryRecipeCommand(this));


// In your onEnable method



        PlayerOxygenManager oxygenManager = new PlayerOxygenManager(this);
        this.getCommand("setplayeroxygen").setExecutor(new SetPlayerOxygenCommand());



        new SetDurabilityCommand(this);
        CustomDurabilityManager.init(this);
        HeirloomManager.init(this);
        new SetCustomDurabilityCommand(this);
        new AddGoldenDurabilityCommand(this);
        this.getCommand("skin").setExecutor(new SkinCommand());
        PetManager petManager = PetManager.getInstance(this);
        this.getCommand("testpet").setExecutor(new PetTestCommand(petManager));
        this.getCommand("island").setExecutor(new IslandCommand());


        PigStepArena.init(this);
        if (!getDataFolder().exists()) {
            getDataFolder().mkdirs();
        }
        arenaManager = ArenaManager.getInstance(this);
        arenaManager.activateArenas();
        PlayerMeritManager playerData = PlayerMeritManager.getInstance(this);

        // Register commands
        getCommand("merits").setExecutor(new MeritCommand(this, playerData));
        getCommand("grantmerit").setExecutor(new GrantMerit(this, playerData));
        getCommand("grantGhostPet").setExecutor(new GrantGhostPetCommand(petManager));
        new GrantLegacyTamingCommand(this, petManager, xpManager);

        // Register event listener for inventory clicks
        

        //potions
        getServer().getPluginManager().registerEvents(new PotionOfStrength(), this);
        getServer().getPluginManager().registerEvents(new PotionOfSovereignty(), this);
        getServer().getPluginManager().registerEvents(new PotionOfLiquidLuck(), this);
        getServer().getPluginManager().registerEvents(new PotionOfFountains(), this);
        getServer().getPluginManager().registerEvents(new PotionOfSwiftStep(), this);
        getServer().getPluginManager().registerEvents(new PotionOfRecurve(), this);
        getServer().getPluginManager().registerEvents(new PotionOfOxygenRecovery(), this);
        getServer().getPluginManager().registerEvents(new PotionOfSolarFury(), this);
        getServer().getPluginManager().registerEvents(new PotionOfNightVision(), this);
        getServer().getPluginManager().registerEvents(new PotionOfCharismaticBartering(), this);
        getServer().getPluginManager().registerEvents(new PotionOfMetalDetection(), this);
        getServer().getPluginManager().registerEvents(new PotionOfOptimalEating(), this);





        getServer().getPluginManager().registerEvents(new WitherPetGrantListener(petManager), this);
        getServer().getPluginManager().registerEvents(new MeritCommand(this, playerData), this);

        //add merit perks
        getServer().getPluginManager().registerEvents(new QuickSwap(this, playerData), this);
        getServer().getPluginManager().registerEvents(new Restock(this, playerData), this);
        getServer().getPluginManager().registerEvents(new Unlooting(this, playerData), this);
        getServer().getPluginManager().registerEvents(new Keepinventory(this, playerData), this);

        doubleEnderchest = new DoubleEnderchest(this, playerData);
        getServer().getPluginManager().registerEvents(doubleEnderchest, this);
        getServer().getPluginManager().registerEvents(new Icarus(this, playerData), this);
        getServer().getPluginManager().registerEvents(new Tuxedo(this, playerData), this);




        getServer().getPluginManager().registerEvents(new AFKDetector.PlayerMoveListener(), this);
        BetterEnd.init(this);

        new Sleep(this);
        getServer().getPluginManager().registerEvents(new ShulkerBox(), this);
        getServer().getPluginManager().registerEvents(new SkinRemovalCauldron(), this);
        getServer().getPluginManager().registerEvents(new BeaconManager(this), this);
        getServer().getPluginManager().registerEvents(new BeaconCharmGUI(this, null), this);
        BeaconPassivesGUI.init(this);
        getServer().getPluginManager().registerEvents(new BeaconPassivesGUI(this, null), this);
        getServer().getPluginManager().registerEvents(new BeaconCatalystsGUI(this, null), this);
        getServer().getPluginManager().registerEvents(new BeaconUpgradesGUI(this, null), this);
        beaconPassiveEffects = new BeaconPassiveEffects(this);
        getServer().getPluginManager().registerEvents(beaconPassiveEffects, this);
        beaconPassiveEffects.reapplyAllPassiveEffects();
        monolithSetBonus = new MonolithSetBonus(this);
        duskbloodSetBonus = new DuskbloodSetBonus(this);
        dwellerSetBonus = new DwellerSetBonus();
        fathmicIronSetBonus = new FathmicIronSetBonus(this);
        naturesWrathSetBonus = new NaturesWrathSetBonus(this);
        pastureshadeSetBonus = new PastureshadeSetBonus(this);
        scorechsteelSetBonus = new ScorechsteelSetBonus(this);
        shadowstepSetBonus = new ShadowstepSetBonus(this);
        slayerSetBonus = new SlayerSetBonus(this);
        thunderforgeSetBonus = new ThunderforgeSetBonus(this);
        lostLegionSetBonus = new LostLegionSetBonus(this);
        countershotSetBonus = new CountershotSetBonus(this);
        striderSetBonus = new StriderSetBonus(this);
        swiftStepMasteryBonus = new SwiftStepMasteryBonus(this);
        fastFarmerBonus = new FastFarmerBonus(this);
        spectralArmorBonus = new SpectralArmorBonus(this);
        // Initialize catalyst manager for beacon charm catalysts
        CatalystManager.initialize(this);
        rejuvenationCatalystListener = new RejuvenationCatalystListener(this);
        deathCatalystListener = new DeathCatalystListener(this);



        getCommand("end").setExecutor(new EndCommand());
        getCommand("nether").setExecutor(new NetherCommand());
        getCommand("overworld").setExecutor(new OverworldCommand());
        getCommand("resetend").setExecutor(new ResetEndCommand());
        getCommand("refreshend").setExecutor(new RefreshEndCommand(this, dragonFightManager));
        getCommand("generatecontinuityisland").setExecutor(new GenerateContinuityIslandCommand());
        getCommand("continuitytp").setExecutor(new ContinuityTpCommand());
        getCommand("warp").setExecutor(new WarpCommand());
        getCommand("setbeaconpower").setExecutor(new SetBeaconPowerCommand());
        getCommand("getstructureblock").setExecutor(new GetStructureBlockCommand());
        getCommand("setstructureblockpower").setExecutor(new SetStructureBlockPowerCommand());
        getCommand("getnearestcatalysttype").setExecutor(new GetNearestCatalystTypeCommand());
        getCommand("previewparticle").setExecutor(new PreviewParticleCommand(this));
        getCommand("previewflow").setExecutor(new PreviewFlowCommand(this));
        new RedVignetteCommand(this);
        new GlowingOresCommand(this);
        new FlyToMeCommand(this);
        FlowManager.getInstance(this);
        getCommand("flowdebug").setExecutor(new FlowDebugCommand(flowManager));
        getCommand("debugplayer").setExecutor(new DebugPlayerCommand(this));
        getCommand("debugdamagefeedback").setExecutor(new DebugDamageFeedbackCommand());
        auraManager = new AuraManager(this);
        getCommand("previewauratemplate").setExecutor(new PreviewAuraTemplateCommand(auraManager));
        getCommand("aura").setExecutor(new AuraCommand(auraManager));


        xpManager = new XPManager(this);
        PetManager.getInstance(this).setXPManager(xpManager);

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
        BankAccountManager.init(this);
        SatchelManager.init(this);
        SeedPouchManager.init(this);
        PotionPouchManager.init(this);
        CulinaryPouchManager.init(this);
        MiningPouchManager.init(this);
        TransfigurationPouchManager.init(this);
        LavaBucketManager.init(this);
        EnchantedClockManager.init(this);
        EnchantedHopperManager.init(this);
        TrinketManager.init(this);
        StructureBlockManager.init(this);
        //getServer().getPluginManager().registerEvents(new GamblingTable(this), this);

        CustomEnchantmentPreferences.init(this);
        getServer().getPluginManager().registerEvents(new CustomEnchantmentPreferences(), this);
        PotionEffectPreferences.init(this);
        getServer().getPluginManager().registerEvents(new PotionEffectPreferences(), this);
        EnvironmentSidebarPreferences.init(this);
        getServer().getPluginManager().registerEvents(new EnvironmentSidebarPreferences(), this);

        forestryPetManager = new ForestryPetManager(this);

        Objects.requireNonNull(Bukkit.getWorld("world")).setGameRule(GameRule.DO_MOB_SPAWNING, true); // Re-enable monster spawns

        new ArmorStandCommand(this); // This will automatically set up the command

        getCommand("discs").setExecutor(new DiscsCommand());
        getServer().getPluginManager().registerEvents(new DiscsCommand(), this);
        getServer().getPluginManager().registerEvents(new Doors(), this);

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
        MarketTrendManager.getInstance(this);
        new goat.minecraft.minecraftnew.subsystems.villagers.VillagerTalentListener();

        if (!getDataFolder().exists()) {
            getDataFolder().mkdirs();
        }
        this.getCommand("spawnseacreature").setExecutor(new SpawnSeaCreatureCommand());
        getCommand("seacreaturechance").setExecutor(new SeaCreatureChanceCommand(this, xpManager));
        getCommand("treasurechance").setExecutor(new TreasureChanceCommand(this));
        getCommand("spiritchance").setExecutor(new SpiritChanceCommand(this, xpManager));
        new SpeedBoost(petManager);
        // Register trait-based stat modifications
        getServer().getPluginManager().registerEvents(new goat.minecraft.minecraftnew.subsystems.pets.traits.PetTraitEffects(this), this);
        // Initialize the culinary subsystem
        culinarySubsystem = CulinarySubsystem.getInstance(this);
        new CulinaryCauldron(this);
        CustomNutritionManager.init(this);
        getCommand("nutrients").setExecutor(new NutritionCommand());

        getLogger().info("MyPlugin has been enabled!");

        //meatCookingManager = new MeatCookingManager(this);


        Bukkit.getPluginManager().registerEvents(new HireVillager(), this);

        getServer().getPluginManager().registerEvents(new UltimateEnchantingSystem(), this);

        getServer().getPluginManager().registerEvents(new ArmorEquipListener(), this);

        getServer().getPluginManager().registerEvents(new Leap(this), this);
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
        getServer().getPluginManager().registerEvents(new Alpha(this), this);
        getServer().getPluginManager().registerEvents(new Fetch(this), this);
        getServer().getPluginManager().registerEvents(new Rebirth(this), this);
        getServer().getPluginManager().registerEvents(new DiggingClaws(this), this);
        getServer().getPluginManager().registerEvents(new XRay(this), this);
        getServer().getPluginManager().registerEvents(new NoHibernation(this), this);
        getServer().getPluginManager().registerEvents(new Echolocation(this), this);
        getServer().getPluginManager().registerEvents(new MithrilMiner(this), this);
        getServer().getPluginManager().registerEvents(new EmeraldSeeker(this), this);
        getServer().getPluginManager().registerEvents(new Flight(this), this);
        getServer().getPluginManager().registerEvents(new Broomstick(this), this);
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
        getServer().getPluginManager().registerEvents(new PhoenixRebirth(this), this);
        getServer().getPluginManager().registerEvents(new FlameTrail(this), this);
        getServer().getPluginManager().registerEvents(new Spectral(this), this);
        getServer().getPluginManager().registerEvents(new goat.minecraft.minecraftnew.subsystems.pets.perks.Revenant(this), this);
        getServer().getPluginManager().registerEvents(new goat.minecraft.minecraftnew.other.qol.FastAscend(), this);

        this.getCommand("givecustomitem").setExecutor(new GiveCustomItem());
        this.getCommand("i").setExecutor(new ItemCommand());


        getCommand("testskill").setExecutor(new TestSkillMessageCommand(xpManager));
        // Register events
        getServer().getPluginManager().registerEvents(petManager, this);
        // Register commands
        getCommand("pets").setExecutor((sender, command, label, args) -> {
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
        // Re-summon pets for players already online (e.g., on reload)
        for (Player online : Bukkit.getOnlinePlayers()) {
            String lastPet = petManager.getLastActivePetName(online.getUniqueId());
            if (lastPet != null) {
                petManager.summonPet(online, lastPet);
            }
        }

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
        SkillsCommand skillsCommand = new SkillsCommand(xpManager);
        this.getCommand("skills").setExecutor(skillsCommand);
        getServer().getPluginManager().registerEvents(skillsCommand, this);
        StatsCommand statsCommand = new StatsCommand(this);
        this.getCommand("stats").setExecutor(statsCommand);
        getServer().getPluginManager().registerEvents(statsCommand, this);
        new SetSkillLevelCommand(this, xpManager);

        SkillTreeManager.init(this);
        new AddTalentPointCommand(this, SkillTreeManager.getInstance());

        getCommand("getpet").setExecutor(new PetCommand(petManager));
        getServer().getPluginManager().registerEvents(new FishingEvent(), MinecraftNew.getInstance());
        getServer().getPluginManager().registerEvents(SpawnMonsters.getInstance(xpManager), this);
        getServer().getPluginManager().registerEvents(new KillMonster(), MinecraftNew.getInstance());
        getServer().getPluginManager().registerEvents(new Mining(), MinecraftNew.getInstance());
        getServer().getPluginManager().registerEvents(new goat.minecraft.minecraftnew.subsystems.gravedigging.terraforming.Terraforming(), MinecraftNew.getInstance());

        getServer().getPluginManager().registerEvents(new Gravedigging(this), this);
        Gravedigging gravedigging = new Gravedigging(this);
        gravedigging.startup();
        getServer().getPluginManager().registerEvents(new goat.minecraft.minecraftnew.subsystems.mining.GemstoneApplicationSystem(this), this);
        getServer().getPluginManager().registerEvents(new goat.minecraft.minecraftnew.subsystems.fishing.BaitApplicationSystem(this), this);
        getServer().getPluginManager().registerEvents(new goat.minecraft.minecraftnew.subsystems.fishing.PearlOfTheDeepSystem(this), this);
        getServer().getPluginManager().registerEvents(new goat.minecraft.minecraftnew.subsystems.fishing.FishingUpgradeSystem(this), this);
        // Initialize and register GemstoneUpgradeSystem, then set reference in Mining class
        goat.minecraft.minecraftnew.subsystems.mining.GemstoneUpgradeSystem gemstoneUpgradeSystem = new goat.minecraft.minecraftnew.subsystems.mining.GemstoneUpgradeSystem(this);
        getServer().getPluginManager().registerEvents(gemstoneUpgradeSystem, this);
        goat.minecraft.minecraftnew.subsystems.mining.Mining.setUpgradeSystemInstance(gemstoneUpgradeSystem);
        getServer().getPluginManager().registerEvents(new goat.minecraft.minecraftnew.subsystems.mining.PowerCrystalSystem(this), this);
        getServer().getPluginManager().registerEvents(new goat.minecraft.minecraftnew.subsystems.mining.CompactStoneSystem(this), this);
        getServer().getPluginManager().registerEvents(new goat.minecraft.minecraftnew.subsystems.forestry.CompactWoodSystem(this), this);
        // Combat listeners
        getServer().getPluginManager().registerEvents(new goat.minecraft.minecraftnew.subsystems.combat.CombatTalentListener(), this);
        getServer().getPluginManager().registerEvents(new goat.minecraft.minecraftnew.subsystems.combat.AntagonizeHandler(this), this);


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

        FestivalBeeManager.getInstance(this);
        getServer().getPluginManager().registerEvents(new FarmingEvent(), MinecraftNew.getInstance());
        getServer().getPluginManager().registerEvents(new SeaCreatureDeathEvent(), MinecraftNew.getInstance());
        getServer().getPluginManager().registerEvents(new goat.minecraft.minecraftnew.subsystems.gravedigging.corpses.CorpseDeathEvent(), this);
        CitizensAPI.getNPCRegistry().deregisterAll();
        getServer().getPluginManager().registerEvents(new RightClickArtifacts(this), this);
        getServer().getPluginManager().registerEvents(new BlessingArtifactGUI(this), this);
        getServer().getPluginManager().registerEvents(new BlessingArmorStandListener(), this);
        getServer().getPluginManager().registerEvents(new BlessedSetAuraListener(this, auraManager), this);
        getServer().getPluginManager().registerEvents(new AnvilRepair(MinecraftNew.getInstance()), this);
        getServer().getPluginManager().registerEvents(new InventoryClickListener(), this);
        getServer().getPluginManager().registerEvents(new EpicEnderDragonFight(this), this);
        ForestSpiritManager manager = ForestSpiritManager.getInstance(this);
        getServer().getPluginManager().registerEvents(manager, this);

        getServer().getPluginManager().registerEvents(new RareCombatDrops(), this);
        getServer().getPluginManager().registerEvents(new PlayerOxygenManager(this), this);
        getServer().getPluginManager().registerEvents(new MiningTalentFeatures(this), this);


        Forestry forestry = Forestry.getInstance(this);
        forestry.init(this);

        saplingManager = SaplingManager.getInstance(this);
        new SetSaplingCooldownCommand(this, saplingManager);


        getServer().getPluginManager().registerEvents(new ContinuityBoardManager(), this);
        getServer().getPluginManager().registerEvents(new SeaCreatureRegistry(), this);
        VillagerTradeManager tradeManager = VillagerTradeManager.getInstance(this);
        getServer().getPluginManager().registerEvents(new CakeHungerListener(), this);
        getServer().getPluginManager().registerEvents(new CakeHungerListener(), this);
        getServer().getPluginManager().registerEvents(new PetDrops(this, PetManager.getInstance(this)), this);
        playerOxygenManager = PlayerOxygenManager.getInstance();
        getServer().getPluginManager().registerEvents(new ReforgeArmorToughness(), this);
        getServer().getPluginManager().registerEvents(new ReforgeDurability(), this);
        getServer().getPluginManager().registerEvents(new goat.minecraft.minecraftnew.subsystems.gravedigging.terraforming.TerraformingDurability(), this);
        getServer().getPluginManager().registerEvents(new ReforgeSwiftBlade(), this);
        getServer().getPluginManager().registerEvents(new WaterLogged(this), this);

        getServer().getPluginManager().registerEvents(new Feed(), this);
        Masterwork masterwork = new Masterwork();
        Masterwork.setUpgradeSystemInstance(gemstoneUpgradeSystem);
        getServer().getPluginManager().registerEvents(masterwork, this);
        getServer().getPluginManager().registerEvents(new Cleaver(), this);
        getServer().getPluginManager().registerEvents(new Forge(), this);
        getServer().getPluginManager().registerEvents(new Shear(), this);
        getServer().getPluginManager().registerEvents(new MagicProtectionListener(), this);
        getServer().getPluginManager().registerEvents(new Alchemy(), this);
        getServer().getPluginManager().registerEvents(new AspectOfTheJourney(), this);
        getServer().getPluginManager().registerEvents(new Stun(), this);
        getServer().getPluginManager().registerEvents(new LethalReaction(), this);
        getServer().getPluginManager().registerEvents(new Rappel(), this);
        getServer().getPluginManager().registerEvents(new Preservation(), this);
        getServer().getPluginManager().registerEvents(new WaterAspect(), this);
        getServer().getPluginManager().registerEvents(new Accelerate(), this);
        getServer().getPluginManager().registerEvents(new goat.minecraft.minecraftnew.other.enchanting.enchantingeffects.Composter(), this);
        getServer().getPluginManager().registerEvents(new FortuneRemover(), this);
        getServer().getPluginManager().registerEvents(new Defenestration(), this);
        getServer().getPluginManager().registerEvents(new goat.minecraft.minecraftnew.other.enchanting.enchantingeffects.Velocity(), this);

        CustomEnchantmentManager.registerEnchantment("Feed", 3, true);
        CustomEnchantmentManager.registerEnchantment("Masterwork", 5, true);
        CustomEnchantmentManager.registerEnchantment("Cleaver", 5, true);
        CustomEnchantmentManager.registerEnchantment("Call of the Void", 5, true);
        CustomEnchantmentManager.registerEnchantment("Savant", 1, true);
        CustomEnchantmentManager.registerEnchantment("Ventilation", 4, true);
        CustomEnchantmentManager.registerEnchantment("Forge", 5, true);
        CustomEnchantmentManager.registerEnchantment("Composter", 5, true);
        CustomEnchantmentManager.registerEnchantment("Piracy", 5, true);
        CustomEnchantmentManager.registerEnchantment("Shear", 5, true);
        CustomEnchantmentManager.registerEnchantment("Unbreaking", 6, true);
        CustomEnchantmentManager.registerEnchantment("Physical Protection", 4, true);
        CustomEnchantmentManager.registerEnchantment("Alchemy", 4, true);
        CustomEnchantmentManager.registerEnchantment("Aspect of the Journey", 1, true);
        CustomEnchantmentManager.registerEnchantment("Stun", 5, true);
        CustomEnchantmentManager.registerEnchantment("Lethal Reaction", 10, true);
        CustomEnchantmentManager.registerEnchantment("Experience", 5, true);
        CustomEnchantmentManager.registerEnchantment("Rappel", 1, true);
        CustomEnchantmentManager.registerEnchantment("Preservation", 1, true);
        CustomEnchantmentManager.registerEnchantment("Water Aspect", 4, true);
        CustomEnchantmentManager.registerEnchantment("Accelerate", 4, true);
        CustomEnchantmentManager.registerEnchantment("Velocity", 3, true);
        CustomEnchantmentManager.registerEnchantment("Defenestration", 1, true);
        CustomEnchantmentManager.registerEnchantment("Lynch", 4, true);
        CustomEnchantmentManager.registerEnchantment("Cornfield", 10, true);
        CustomEnchantmentManager.registerEnchantment("What's Up Doc", 10, true);
        CustomEnchantmentManager.registerEnchantment("Venerate", 10, true);
        CustomEnchantmentManager.registerEnchantment("Legend", 10, true);
        CustomEnchantmentManager.registerEnchantment("Clean Cut", 10, true);
        CustomEnchantmentManager.registerEnchantment("Gourd", 10, true);


        getServer().getPluginManager().registerEvents(new KnightMob(this), this);
        getServer().getPluginManager().registerEvents(
                goat.minecraft.minecraftnew.subsystems.combat.champion.ChampionManager.getInstance(this), this);
        getServer().getPluginManager().registerEvents(new ArmorReforge(new ReforgeManager()), this);
        getServer().getPluginManager().registerEvents(new ToolReforge(new ReforgeManager()), this);
        getServer().getPluginManager().registerEvents(new Experience(), this);

        getServer().getPluginManager().registerEvents(new CatTameEvent(petManager), this);
        getServer().getPluginManager().registerEvents(new WolfTameEvent(petManager), this);
        getServer().getPluginManager().registerEvents(new AxolotlInteractEvent(petManager), this);
        getServer().getPluginManager().registerEvents(new AllayInteractEvent(petManager), this);

        this.getCommand("clearpets").setExecutor(new ClearPetsCommand(this, petManager));
        getServer().getPluginManager().registerEvents(new BowReforge(), this);
        villagerWorkCycleManager = VillagerWorkCycleManager.getInstance(this);
        getCommand("forceworkcycle").setExecutor(villagerWorkCycleManager);
        getCommand("repair").setExecutor(new RepairCommand());
        getCommand("repairall").setExecutor(new RepairAllCommand());
        getCommand("finishbrews").setExecutor(new FinishBrewsCommand(this));
        getCommand("setreforgeseconds").setExecutor(new SetReforgeSecondsCommand(this));
        getCommand("openvillagertrademenu").setExecutor(new OpenVillagerTradeMenuCommand(this));
        getCommand("togglecustomenchantments").setExecutor(new ToggleCustomEnchantmentsCommand(this));
        getCommand("togglepotioneffects").setExecutor(new TogglePotionEffectsCommand(this));
        getCommand("togglebars").setExecutor(new ToggleBarsCommand());
        getCommand("stripreforge").setExecutor(new StripReforgeCommand());
        getCommand("applyreforge").setExecutor(new ApplyReforgeCommand());
        new SetAmountCommand(this);

        getServer().getPluginManager().registerEvents(new MusicDiscManager(this), this);

        //nms >


        if (!getDataFolder().exists()) {
            getDataFolder().mkdirs();
        }
        System.out.println("[MinecraftNew] Plugin enabled.");

        // Spawn nightly zombie hordes for online players
        new NightHordeTask(this).start();

        getServer().getPluginManager().registerEvents(new HordeInstinct(), this);











        //getServer().getPluginManager().registerEvents(new RealTimeDamageFeedback(), this);
    }


    @Override
    public void onDisable() {
        HealthManager.shutdown();
        if (shelfManager != null) {
            shelfManager.onDisable();
        }
        if (warpGateManager != null) {
            warpGateManager.onDisable();
        }

        if(potionBrewingSubsystem != null){
            potionBrewingSubsystem.onDisable();
        }
        if (verdantRelicsSubsystem != null) {
            verdantRelicsSubsystem.onDisable();
        }
        if (reforgeSubsystem != null) {
            reforgeSubsystem.onDisable();
        }
        if (saplingManager != null) {
            saplingManager.shutdown();
        }

        Bukkit.getOnlinePlayers().forEach(p ->
                p.stopAllSounds());


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

        if (beaconPassiveEffects != null) {
            beaconPassiveEffects.removeAllPassiveEffects();
        }
        if (monolithSetBonus != null) {
            monolithSetBonus.removeAllBonuses();
        }
        if (duskbloodSetBonus != null) {
            duskbloodSetBonus.removeAllBonuses();
        }
        if (dwellerSetBonus != null) {
            dwellerSetBonus.removeAllBonuses();
        }
        if (fathmicIronSetBonus != null) {
            fathmicIronSetBonus.removeAllBonuses();
        }
        if (naturesWrathSetBonus != null) {
            naturesWrathSetBonus.removeAllBonuses();
        }
        if (pastureshadeSetBonus != null) {
            pastureshadeSetBonus.removeAllBonuses();
        }
        if (scorechsteelSetBonus != null) {
            scorechsteelSetBonus.removeAllBonuses();
        }
        if (shadowstepSetBonus != null) {
            shadowstepSetBonus.removeAllBonuses();
        }
        if (slayerSetBonus != null) {
            slayerSetBonus.removeAllBonuses();
        }
        if (thunderforgeSetBonus != null) {
            thunderforgeSetBonus.removeAllBonuses();
        }
        if (lostLegionSetBonus != null) {
            lostLegionSetBonus.removeAllBonuses();
        }
        if (countershotSetBonus != null) {
            countershotSetBonus.removeAllBonuses();
        }
        if (striderSetBonus != null) {
            striderSetBonus.removeAllBonuses();
        }
        if (swiftStepMasteryBonus != null) {
            swiftStepMasteryBonus.removeAllBonuses();
        }
        CustomEnchantmentPreferences.saveAll();
        PotionEffectPreferences.saveAll();
        EnvironmentSidebarPreferences.saveAll();
        BeaconPassivesGUI.saveAllPassives();
        if (CatalystManager.getInstance() != null) {
            CatalystManager.getInstance().shutdown();
        }

        // Persist forestry notoriety data on shutdown
        try {
            Forestry.getInstance().saveAllNotoriety();
        } catch (Exception e) {
            getLogger().warning("Failed to save notoriety data: " + e.getMessage());
        }


        PetManager.getInstance(this).savePets();
        PetManager.getInstance(this).saveLastActivePets();
        anvilRepair.saveAllInventories();
        if (doubleEnderchest != null) {
            doubleEnderchest.saveAllInventories();
        }
        removeAllCitizenEntities();
        System.out.println("[MinecraftNew] Plugin disabled.");//
    }
    public static MinecraftNew getInstance() {

        return instance; // Provide a static method to get the instance
    }
    public XPManager getXPManager() {
        return xpManager;
    }
    public ForestryPetManager getForestryManager() {
        return forestryPetManager;
    }


    private void removeAllCitizenEntities() {
        for (org.bukkit.World world : Bukkit.getWorlds()) {
            world.getEntities().stream()
                    .filter(e -> e.hasMetadata("NPC"))
                    .forEach(org.bukkit.entity.Entity::remove);
        }
    }
}