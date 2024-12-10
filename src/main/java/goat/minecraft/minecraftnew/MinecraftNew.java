package goat.minecraft.minecraftnew;

import goat.minecraft.minecraftnew.subsystems.artifacts.RightClickArtifacts;
import goat.minecraft.minecraftnew.subsystems.brewing.CancelBrewing;
import goat.minecraft.minecraftnew.subsystems.chocolatemisc.CakeHungerListener;
import goat.minecraft.minecraftnew.subsystems.chocolatemisc.InventoryClickListener;
import goat.minecraft.minecraftnew.subsystems.combat.EpicEnderDragonFight;
import goat.minecraft.minecraftnew.subsystems.combat.MobDamageHandler;
import goat.minecraft.minecraftnew.subsystems.elitemonsters.KnightMob;
import goat.minecraft.minecraftnew.subsystems.combat.RareCombatDrops;
import goat.minecraft.minecraftnew.subsystems.enchanting.CustomEnchantmentManager;
import goat.minecraft.minecraftnew.subsystems.enchanting.EnchantmentTableInventoryInteractCancel;
import goat.minecraft.minecraftnew.subsystems.enchanting.EnchantmentTableRightClick;
import goat.minecraft.minecraftnew.subsystems.enchanting.enchantingeffects.*;
import goat.minecraft.minecraftnew.subsystems.farming.FarmingEvent;
import goat.minecraft.minecraftnew.subsystems.fishing.FishingEvent;
import goat.minecraft.minecraftnew.subsystems.fishing.SeaCreatureRegistry;
import goat.minecraft.minecraftnew.subsystems.forestry.ForestSpiritManager;
import goat.minecraft.minecraftnew.subsystems.generators.ResourceGeneratorSubsystem;
import goat.minecraft.minecraftnew.subsystems.mining.PlayerOxygenManager;
import goat.minecraft.minecraftnew.subsystems.music.MusicDiscManager;
import goat.minecraft.minecraftnew.subsystems.pets.*;
import goat.minecraft.minecraftnew.subsystems.pets.perks.*;
import goat.minecraft.minecraftnew.subsystems.pets.perks.Float;
import goat.minecraft.minecraftnew.subsystems.pets.petdrops.AllayInteractEvent;
import goat.minecraft.minecraftnew.subsystems.pets.petdrops.AxolotlInteractEvent;
import goat.minecraft.minecraftnew.subsystems.pets.petdrops.CatTameEvent;
import goat.minecraft.minecraftnew.subsystems.pets.petdrops.PetDrops;
import goat.minecraft.minecraftnew.subsystems.skillbuffs.CombatBuffs;
import goat.minecraft.minecraftnew.subsystems.skillbuffs.FarmingBuff;
import goat.minecraft.minecraftnew.subsystems.smithing.tierreforgelisteners.ReforgeManager;
import goat.minecraft.minecraftnew.subsystems.smithing.talismans.*;
import goat.minecraft.minecraftnew.subsystems.smithing.tierreforgelisteners.ArmorReforge;
import goat.minecraft.minecraftnew.subsystems.smithing.tierreforgelisteners.SwordReforge;
import goat.minecraft.minecraftnew.subsystems.smithing.tierreforgelisteners.ToolReforge;
import goat.minecraft.minecraftnew.subsystems.structures.Mineshafts;
import goat.minecraft.minecraftnew.subsystems.utils.CustomItemManager;
import goat.minecraft.minecraftnew.subsystems.combat.KillMonster;
import goat.minecraft.minecraftnew.subsystems.fishing.SeaCreatureDeathEvent;
import goat.minecraft.minecraftnew.subsystems.mining.Mining;
import goat.minecraft.minecraftnew.subsystems.player.PlayerLevel;
import goat.minecraft.minecraftnew.subsystems.smithing.AnvilRepair;
import goat.minecraft.minecraftnew.subsystems.utils.*;
import goat.minecraft.minecraftnew.subsystems.villagers.VillagerTradeManager;
import goat.minecraft.minecraftnew.subsystems.villagers.VillagerWorkCycleManager;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public class MinecraftNew extends JavaPlugin implements Listener {

    private VillagerWorkCycleManager villagerWorkCycleManager;

    private XPManager xpManager;
    //instancing
    private static MinecraftNew instance;
    CancelBrewing cancelBrewing = new CancelBrewing(this);
    private AnvilRepair anvilRepair;

    private PlayerOxygenManager playerOxygenManager;
    @Override

    public void onEnable() {




        PetManager petManager = PetManager.getInstance(this);
        new SpeedBoost(petManager);
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

        SpawnMonsters spawnMonsters = new SpawnMonsters(this, xpManager);

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
        xpManager = new XPManager(this);
        new PlayerTabListUpdater(this, xpManager);
        this.getCommand("xp").setExecutor(xpManager);
        this.getCommand("loadsubsystems").setExecutor(new LoadSubsystemsCommand(this));
        this.getCommand("skills").setExecutor(new SkillsCommand(xpManager));

        getCommand("getpet").setExecutor(new PetCommand(petManager));

        getServer().getPluginManager().registerEvents(new SpawnMonsters(this, xpManager), this);
        getServer().getPluginManager().registerEvents(new KillMonster(), this);
        getServer().getPluginManager().registerEvents(new CustomItemManager(), this);
        getServer().getPluginManager().registerEvents(new Mining(), this);
        getServer().getPluginManager().registerEvents(new PlayerLevel(this, xpManager), this);
        getServer().getPluginManager().registerEvents(new FishingEvent(), this);
        getServer().getPluginManager().registerEvents(new FarmingEvent(), this);
        getServer().getPluginManager().registerEvents(new SeaCreatureDeathEvent(), this);
        getServer().getPluginManager().registerEvents(new CancelBrewing(this), this);
        getServer().getPluginManager().registerEvents(new EnchantmentTableInventoryInteractCancel(), this);
        getServer().getPluginManager().registerEvents(new EnchantmentTableRightClick(), this);
        getServer().getPluginManager().registerEvents(new RightClickArtifacts(this), this);
        getServer().getPluginManager().registerEvents(new AnvilRepair(this), this);
        getServer().getPluginManager().registerEvents(new InventoryClickListener(), this);
        getServer().getPluginManager().registerEvents(new EpicEnderDragonFight(this), this);
        getServer().getPluginManager().registerEvents(new ForestSpiritManager(this), this);
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
        getServer().getPluginManager().registerEvents(new VillagerWorkCycleManager(this), this);
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
        getServer().getPluginManager().registerEvents(new FarmingBuff(xpManager), this);

        villagerWorkCycleManager = new VillagerWorkCycleManager(this);
        getServer().getPluginManager().registerEvents(new MusicDiscManager(this), this);
        //nms >

        getServer().getPluginManager().registerEvents(new Mineshafts(this), this);


        if (!getDataFolder().exists()) {
            getDataFolder().mkdirs();
        }
        System.out.println("[MinecraftNew] Plugin enabled.");















    }
    @Override
    public void onDisable() {

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
}