package goat.minecraft.minecraftnew.other.skilltree;

import org.bukkit.ChatColor;
import org.bukkit.Material;

/**
 * Enumeration of all available talents in the plugin.  This acts as a
 * registry so other classes can easily reference a specific talent by
 * using <code>Talent.REDSTONE</code> for example.
 */
public enum Talent {
    TRIPLE_BATCH(
            "Triple Batch Upgrade",
            ChatColor.GRAY + "Catches the excess potion in a glass bottle",
            ChatColor.YELLOW + "+10% " + ChatColor.GRAY + "Chance to brew 3 Potions.",
            8,
            50,
            Material.CAULDRON
    ),
    OPTIMAL_CONFIGURATION(
            "Optimal Brewing Stand Settings",
            ChatColor.GRAY + "Installs an ad-blocker to prevent wasted time",
            ChatColor.YELLOW + "-5s " + ChatColor.GOLD + "Brew Time.",
            10,
            1,
            Material.BREWING_STAND
    ),
    REDSTONE_ONE(
            "Redstone I",
            ChatColor.GRAY + "Allows Potions to steep for longer",
            ChatColor.YELLOW + "+10s " + ChatColor.LIGHT_PURPLE + "Potion Duration, " + ChatColor.GOLD + "+10s " + ChatColor.GOLD + "Brew Time.",
            10,
            1,
            Material.REDSTONE
    ),
    REDSTONE_TWO(
            "Redstone II",
            ChatColor.GRAY + "Allows Potions to steep for even longer",
            ChatColor.YELLOW + "+10s " + ChatColor.LIGHT_PURPLE + "Potion Duration, " + ChatColor.GOLD + "+10s " + ChatColor.GOLD + "Brew Time.",
            10,
            20,
            Material.REDSTONE_BLOCK
    ),
    REDSTONE_THREE(
            "Redstone III",
            ChatColor.GRAY + "Allows Potions to steep for even longer",
            ChatColor.YELLOW + "+20s " + ChatColor.LIGHT_PURPLE + "Potion Duration, " + ChatColor.GOLD + "+20s " + ChatColor.GOLD + "Brew Time.",
            10,
            50,
            Material.REDSTONE_TORCH
    ),
    REDSTONE_FOUR(
            "Redstone IV",
            ChatColor.GRAY + "Allows Potions to steep for even longer",
            ChatColor.YELLOW + "+30s " + ChatColor.LIGHT_PURPLE + "Potion Duration, " + ChatColor.GOLD + "+30s " + ChatColor.GOLD + "Brew Time.",
            10,
            60,
            Material.REDSTONE_ORE
    ),
    RECURVE_MASTERY(
            "Recurve Mastery",
            ChatColor.GRAY + "Add a Skeleton Skull",
            ChatColor.YELLOW + "+200s " + ChatColor.LIGHT_PURPLE + "Recurve Duration",
            2,
            25,
            Material.BOW
    ),
    REJUVENATION(
            "Rejuvenation",
            ChatColor.GRAY + "Add a Golden Apple",
            ChatColor.YELLOW + "+50s " + ChatColor.GREEN + "Bonus Health " + ChatColor.GRAY + "and " + ChatColor.GREEN + "Potion Surge",
            4,
            45,
            Material.GHAST_TEAR
    ),
    SOVEREIGNTY_MASTERY(
            "Sovereignty Mastery",
            ChatColor.GRAY + "Add a diamond",
            ChatColor.YELLOW + "+200s " + ChatColor.LIGHT_PURPLE + "Sovereignty Duration",
            2,
            50,
            Material.PRISMARINE_SHARD
    ),
    STRENGTH_MASTERY(
            "Strength Mastery",
            ChatColor.GRAY + "Add a Singularity",
            ChatColor.YELLOW + "+200s " + ChatColor.LIGHT_PURPLE + "Strength Duration",
            2,
            25,
            Material.DIAMOND_SWORD
    ),
    LIQUID_LUCK_MASTERY(
            "Liquid Luck Mastery",
            ChatColor.GRAY + "Add a Golden Ingot",
            ChatColor.YELLOW + "+200s " + ChatColor.LIGHT_PURPLE + "Liquid Luck Duration",
            2,
            60,
            Material.HONEY_BOTTLE
    ),
    OXYGEN_MASTERY(
            "Oxygen Mastery",
            ChatColor.GRAY + "Add an obsidian block",
            ChatColor.YELLOW + "+200s " + ChatColor.AQUA + "Oxygen Recovery Duration",
            2,
            60,
            Material.GLASS_BOTTLE
    ),
    SWIFT_STEP_MASTERY(
            "Swift Step Mastery",
            ChatColor.GRAY + "Add Sugar for a pep in your step",
            ChatColor.YELLOW + "+200s " + ChatColor.LIGHT_PURPLE + "Swift Step Duration",
            2,
            35,
            Material.FEATHER
    ),
    METAL_DETECTION_MASTERY(
            "Metal Detection Mastery",
            ChatColor.GRAY + "Add a Zombie Skull",
            ChatColor.YELLOW + "+200s " + ChatColor.LIGHT_PURPLE + "Metal Detection Duration",
            2,
            60,
            Material.ZOMBIE_HEAD
),
    NIGHT_VISION_MASTERY(
            "Night Vision Mastery",
            ChatColor.GRAY + "Add a Spider eye",
            ChatColor.YELLOW + "+200s " + ChatColor.AQUA + "Night Vision Duration",
            2,
            30,
            Material.SPIDER_EYE
      ),
    SOLAR_FURY_MASTERY(
            "Solar Fury Mastery",
            ChatColor.GRAY + "Add Blaze Powder",
            ChatColor.YELLOW + "+200s " + ChatColor.GOLD + "Solar Fury Duration",
            2,
            35,
            Material.FIRE_CHARGE
    ),
    FOUNTAIN_MASTERY(
            "Fountain Mastery",
            ChatColor.GRAY + "Add a heart of the sea",
            ChatColor.YELLOW + "+200s " + ChatColor.LIGHT_PURPLE + "Fountains Duration",
            2,
            60,
            Material.DARK_PRISMARINE
    ),
    ANGLERS_INSTINCT(
            "Angler's Instinct",
            ChatColor.GRAY + "Hone your talent for reeling in the unusual",
            ChatColor.YELLOW + "+0.25% " + ChatColor.AQUA + "Sea Creature Chance per level",
            25,
            1,
            Material.NAUTILUS_SHELL
    ),
    CHARISMA_MASTERY(
            "Charisma Mastery",
            ChatColor.GRAY + "Add a bribe",
            ChatColor.YELLOW + "+200s " + ChatColor.LIGHT_PURPLE + "Charismatic Bartering Duration",
            2,
            60,
            Material.GOLD_BLOCK
    ),
    REDSTONE_FIVE(
            "Redstone V",
            ChatColor.GRAY + "Allows Potions to steep for absurdly long",
            ChatColor.YELLOW + "+30s " + ChatColor.LIGHT_PURPLE + "Potion Duration, " + ChatColor.GOLD + "+30s " + ChatColor.GOLD + "Brew Time.",
            10,
            80,
            Material.REDSTONE_ORE
    ),
    NUTRITION_MASTERY(
            "Nutrition Mastery",
            ChatColor.GRAY + "Add Sea Salt to the recipe",
            ChatColor.YELLOW + "+200s " + ChatColor.LIGHT_PURPLE + "Optimal Eating Duration",
            2,
            80,
            Material.DRIED_KELP
    ),
    ETERNAL_ELIXIR(
            "Eternal Elixir",
            ChatColor.GRAY + "Potions may last forever",
            ChatColor.YELLOW + "+0.25% " + ChatColor.GRAY + "Chance for infinite duration",
            8,
            80,
            Material.TOTEM_OF_UNDYING
    ),
    ARROW_DAMAGE_INCREASE_I(
            "Arrow Damage Increase I",
            ChatColor.GRAY + "Improve basic archery form",
            ChatColor.RED + "+4% Arrow Damage",
            3,
            1,
            Material.ARROW
    ),
    SWORD_DAMAGE_I(
            "Sword Damage I",
            ChatColor.GRAY + "Sharpen your striking technique",
            ChatColor.RED + "+4% Sword Damage",
            5,
            1,
            Material.WOODEN_SWORD
    ),
    VAMPIRIC_STRIKE(
            "Vampiric Strike",
            ChatColor.GRAY + "Harvest souls for brief vitality",
            ChatColor.YELLOW + "1% chance per level to spawn a Soul Orb",
            6,
            1,
            Material.GHAST_TEAR
    ),
    BLOODLUST(
            "Bloodlust",
            ChatColor.GRAY + "Killing feeds your frenzy",
            ChatColor.RED + "Activates Bloodlust for 5s on kill",
            1,
            1,
            Material.NETHER_STAR
    ),
    BLOODLUST_DURATION_I(
            "Bloodlust Duration I",
            ChatColor.GRAY + "Channel your rage for longer",
            ChatColor.YELLOW + "+4s Bloodlust Duration",
            5,
            1,
            Material.CLOCK
    ),
    ARROW_DAMAGE_INCREASE_II(
            "Arrow Damage Increase II",
            ChatColor.GRAY + "Greater focus when aiming",
            ChatColor.RED + "+8% Arrow Damage",
            3,
            20,
            Material.SPECTRAL_ARROW
    ),
    SWORD_DAMAGE_II(
            "Sword Damage II",
            ChatColor.GRAY + "Refined swordplay",
            ChatColor.RED + "+4% Sword Damage",
            5,
            20,
            Material.STONE_SWORD
    ),
    BLOODLUST_DURATION_II(
            "Bloodlust Duration II",
            ChatColor.GRAY + "Maintain fury even longer",
            ChatColor.YELLOW + "+4s Bloodlust Duration",
            5,
            20,
            Material.CLOCK
    ),
    RETRIBUTION(
            "Retribution",
            ChatColor.GRAY + "Striking fuels your frenzy",
            ChatColor.YELLOW + "+1% chance to gain +10 Bloodlust Stacks",
            5,
            20,
            Material.SHIELD
    ),
    VENGEANCE(
            "Vengeance",
            ChatColor.GRAY + "Hits may extend your rampage",
            ChatColor.YELLOW + "+1% chance to gain +20s Bloodlust Duration",
            2,
            20,
            Material.ENDER_EYE
    ),
    ARROW_DAMAGE_INCREASE_III(
            "Arrow Damage Increase III",
            ChatColor.GRAY + "Masterful archery techniques",
            ChatColor.RED + "+12% Arrow Damage",
            3,
            40,
            Material.TIPPED_ARROW
    ),
    SWORD_DAMAGE_III(
            "Sword Damage III",
            ChatColor.GRAY + "Polish your edge",
            ChatColor.RED + "+4% Sword Damage",
            5,
            40,
            Material.IRON_SWORD
    ),
    DONT_MINE_AT_NIGHT(
            "Don't Mine at Night",
            ChatColor.GRAY + "Creepers beware of seasoned fighters",
            ChatColor.YELLOW + "+(10*level)% " + ChatColor.RED + "Creeper Damage",
            6,
            40,
            Material.TNT
    ),
    HELLBENT(
            "Hellbent",
            ChatColor.GRAY + "Fight harder when near death",
            ChatColor.RED + "+25% Damage below (10*level)% health",
            6,
            40,
            Material.TOTEM_OF_UNDYING
    ),
    ARROW_DAMAGE_INCREASE_IV(
            "Arrow Damage Increase IV",
            ChatColor.GRAY + "Expert precision",
            ChatColor.RED + "+16% Arrow Damage",
            3,
            60,
            Material.BOW
    ),
    SWORD_DAMAGE_IV(
            "Sword Damage IV",
            ChatColor.GRAY + "Deadly technique",
            ChatColor.RED + "+4% Sword Damage",
            5,
            60,
            Material.DIAMOND_SWORD
    ),
    BLOODLUST_DURATION_III(
            "Bloodlust Duration III",
            ChatColor.GRAY + "Fury knows no bounds",
            ChatColor.YELLOW + "+4s Bloodlust Duration",
            5,
            60,
            Material.CLOCK
    ),
    ANTAGONIZE(
            "Antagonize",
            ChatColor.GRAY + "Delay the pain you feel",
            ChatColor.YELLOW + "Damage taken spread over (1*level)s",
            7,
            60,
            Material.IRON_CHESTPLATE
    ),
    ARROW_DAMAGE_INCREASE_V(
            "Arrow Damage Increase V",
            ChatColor.GRAY + "Legendary archery prowess",
            ChatColor.RED + "+20% Arrow Damage",
            4,
            80,
            Material.CROSSBOW
    ),
    SWORD_DAMAGE_V(
            "Sword Damage V",
            ChatColor.GRAY + "Unmatched sword mastery",
            ChatColor.RED + "+4% Sword Damage",
            5,
            80,
            Material.NETHERITE_SWORD
    ),
    ULTIMATUM(
            "Ultimatum",
            ChatColor.GRAY + "Occasionally unleash devastating fury",
            ChatColor.YELLOW + "+0.25% Fury Chance",
            5,
            80,
            Material.LIGHTNING_ROD
    ),
    REVENANT(
            "Revenant",
            ChatColor.GRAY + "Death cannot quell your rage",
            ChatColor.YELLOW + "Dying with 100 Bloodlust Stacks triggers Fury",
            1,
            80,
            Material.SKELETON_SKULL
    ),
    BLOODLUST_DURATION_IV(
            "Bloodlust Duration IV",
            ChatColor.GRAY + "Transcendent ferocity",
            ChatColor.YELLOW + "+4s Bloodlust Duration",
            5,
            80,
            Material.CLOCK
    ),
    REPAIR_ONE(
            "Repair Mastery I",
            ChatColor.GRAY + "Improve basic repair techniques",
            ChatColor.GREEN + "+(1*level) " + ChatColor.GRAY + "Repair Amount",
            10,
            1,
            Material.ANVIL
    ),
    REPAIR_TWO(
            "Repair Mastery II",
            ChatColor.GRAY + "Further hone repair skills",
            ChatColor.GREEN + "+(2*level) " + ChatColor.GRAY + "Repair Amount",
            10,
            30,
            Material.ANVIL
    ),
    REPAIR_THREE(
            "Repair Mastery III",
            ChatColor.GRAY + "Advanced repair expertise",
            ChatColor.GREEN + "+(3*level) " + ChatColor.GRAY + "Repair Amount",
            10,
            50,
            Material.ANVIL
    ),
    REPAIR_FOUR(
            "Repair Mastery IV",
            ChatColor.GRAY + "Masterful repair proficiency",
            ChatColor.GREEN + "+(4*level) " + ChatColor.GRAY + "Repair Amount",
            10,
            70,
            Material.ANVIL
      ),
    SATIATION_MASTERY(
            "Satiation Mastery",
            ChatColor.GRAY + "Cooked meals keep you fuller",
            ChatColor.YELLOW + "+1 " + ChatColor.GRAY + "Saturation per level",
            5,
            10,
            Material.COOKED_BEEF
    ),
    FEASTING_CHANCE(
            "Feasting Chance",
            ChatColor.GRAY + "Occasionally feel extra nourished",
            ChatColor.YELLOW + "+4% " + ChatColor.GRAY + "chance for Saturation V",
            16,
            1,
            Material.GOLDEN_CARROT
    ),
    MASTER_CHEF(
            "Master Chef",
            ChatColor.GRAY + "Expertise yields extra portions",
            ChatColor.YELLOW + "+4% " + ChatColor.GRAY + "chance to double output",
            16,
            20,
            Material.CAKE
      ),
    BARTER_DISCOUNT(
            "Barter Discount",
            ChatColor.GRAY + "Sharpen your haggling skills",
            ChatColor.YELLOW + "+4% Discount per level",
            10,
            1,
            Material.EMERALD
    ),
    FREE_TRANSACTION(
            "Free Transaction",
            ChatColor.GRAY + "Occasionally pay nothing",
            ChatColor.YELLOW + "1% chance per level for free purchase",
            10,
            20,
            Material.EMERALD_BLOCK
    ),
    SELL_PRICE_BOOST(
            "Sell Price Boost",
            ChatColor.GRAY + "Villagers pay more",
            ChatColor.YELLOW + "+4% Sell Price per level",
            20,
            1,
            Material.EMERALD
    ),
    WORK_CYCLE_EFFICIENCY(
            "Work Cycle Efficiency",
            ChatColor.GRAY + "Reduce villager downtime",
            ChatColor.YELLOW + "-5s Workcycle per level",
            20,
            10,
            Material.CLOCK
      ),
    DOUBLE_LOGS(
            "Double Logs",
            ChatColor.GRAY + "Chance for extra logs",
            ChatColor.YELLOW + "+10% Double Log Chance.",
            10,
            1,
            Material.OAK_LOG
    ),
    FORESTRY_HASTE(
            "Forestry Haste",
            ChatColor.GRAY + "Chance to gain Haste while chopping",
            ChatColor.YELLOW + "+10% Haste chance.",
            10,
            20,
            Material.SUGAR
    ),
    HASTE_POTENCY(
            "Haste Potency",
            ChatColor.GRAY + "Increase Forestry Haste strength",
            ChatColor.YELLOW + "+1 Haste level.",
            4,
            40,
            Material.REDSTONE_TORCH
    ),
    TREECAP_SPIRIT(
            "Treecap Spirit",
            ChatColor.GRAY + "More spirit chance from Treecapitator",
            ChatColor.YELLOW + "+0.1% Spirit Chance",
            15,
            50,
            Material.SOUL_TORCH
    ),
    PET_TRAINER(
            "Pet Trainer",
            ChatColor.GRAY + "Sharpen your pet handling skills",
            ChatColor.YELLOW + "+4% " + ChatColor.GRAY + "Double Pet XP Chance",
            25,
            1,
            Material.BONE
    ),
  VITALITY(
            "Vitality",
            ChatColor.GRAY + "Fortify your body to survive longer",
            ChatColor.GREEN + "+1 Max Health per level",
            20,
            1,
            Material.APPLE
     ),
    CONSERVATIONIST(
            "Conservationist",
            ChatColor.GRAY + "Reduce wear on your tools",
            ChatColor.YELLOW + "+(1*level)% " + ChatColor.GRAY + "durability save chance",
            25,
            1,
            Material.DIAMOND_PICKAXE
    ),
    GRAVE_INTUITION(
            "Grave Intuition",
            ChatColor.GRAY + "Sense where graves may appear",
            ChatColor.YELLOW + "+(0.001*level) " + ChatColor.GRAY + "grave chance",
            10,
            25,
            Material.BONE
    ),
    BOUNTIFUL_HARVEST(
            "Bountiful Harvest",
            ChatColor.GRAY + "Increases chances for extra crops",
            ChatColor.YELLOW + "+(4*level)% " + ChatColor.GRAY + "chance to harvest " + ChatColor.GREEN + "double crops.",
            25,
            1,
            Material.WHEAT
    ),
    VERDANT_TENDING(
            "Verdant Tending",
            ChatColor.GRAY + "Expertise with relic cultivation",
            ChatColor.YELLOW + "-" + ChatColor.WHITE + "(2.5*level)m" + ChatColor.GRAY + " Verdant Relic growth time",
            10,
            40,
            Material.BONE_MEAL
    ),
    RICH_VEINS(
            "Rich Veins",
            ChatColor.GRAY + "Find extra ore when mining",
            ChatColor.YELLOW + "+(4*level)% " + ChatColor.GRAY + "Double Drop Chance",
            25,
            1,
            Material.IRON_PICKAXE
    ),

    DEEP_LUNGS(
            "Deep Lungs",
            ChatColor.GRAY + "Increase oxygen capacity underground",
            ChatColor.YELLOW + "+(20*level) " + ChatColor.AQUA + "Oxygen Capacity",
            25,
            10,
            Material.TURTLE_SCUTE
    ),

    // =============================================================
    // Farming Talents
    // =============================================================

    EXTRA_CROP_CHANCE_I(
            "Extra Crop Chance I",
            ChatColor.GRAY + "Increases crop yield",
            ChatColor.YELLOW + "+(8*level)% " + ChatColor.GRAY + "Extra Crop Chance",
            3,
            1,
            Material.WHEAT_SEEDS
    ),
    FOR_THE_STREETS(
            "For The Streets",
            ChatColor.GRAY + "Chance to till a 9x9 area",
            ChatColor.YELLOW + "+(20*level)% " + ChatColor.GRAY + "till 9x9 area chance",
            5,
            1,
            Material.IRON_HOE
    ),
    REAPER_I(
            "Reaper I",
            ChatColor.GRAY + "Reduce harvest requirement",
            ChatColor.YELLOW + "-(1*level)% " + ChatColor.GRAY + "Harvest requirement",
            5,
            1,
            Material.WITHER_ROSE
    ),
    FAST_FARMER(
            "Fast Farmer",
            ChatColor.GRAY + "Gain Speed when harvesting crops",
            ChatColor.YELLOW + "Speed " + ChatColor.GRAY + "I-V on crop break",
            5,
            1,
            Material.LEATHER_BOOTS
    ),
    HARVEST_FESTIVAL(
            "Harvest Festival",
            ChatColor.GRAY + "Chance for 5s of Haste II when harvesting",
            ChatColor.YELLOW + "+(50*level)% " + ChatColor.GRAY + "Haste II chance",
            2,
            1,
            Material.HONEY_BOTTLE
    ),

    EXTRA_CROP_CHANCE_II(
            "Extra Crop Chance II",
            ChatColor.GRAY + "Further increases crop yield",
            ChatColor.YELLOW + "+(16*level)% " + ChatColor.GRAY + "Extra Crop Chance",
            3,
            20,
            Material.POTATO
    ),
    UNRIVALED(
            "Unrivaled",
            ChatColor.GRAY + "Grow nearby crops when harvesting",
            ChatColor.YELLOW + "+(1*level)% " + ChatColor.GRAY + "grow nearby crops",
            5,
            20,
            Material.BONE_MEAL
    ),
    REAPER_II(
            "Reaper II",
            ChatColor.GRAY + "Reduce harvest requirement",
            ChatColor.YELLOW + "-(1*level)% " + ChatColor.GRAY + "Harvest requirement",
            5,
            20,
            Material.WITHER_ROSE
    ),
    HYDRO_FARMER(
            "Hydro Farmer",
            ChatColor.GRAY + "Irrigation grows crops more",
            ChatColor.YELLOW + "+(20*level)% " + ChatColor.GRAY + "extra irrigation growth",
            5,
            20,
            Material.WATER_BUCKET
    ),
    FESTIVAL_BEES_I(
            "Festival Bees I",
            ChatColor.GRAY + "Chance to spawn Festival Bee",
            ChatColor.YELLOW + "+(0.25*level)% " + ChatColor.GRAY + "Festival Bee chance",
            2,
            20,
            Material.BEEHIVE
    ),

    EXTRA_CROP_CHANCE_III(
            "Extra Crop Chance III",
            ChatColor.GRAY + "Greatly increases crop yield",
            ChatColor.YELLOW + "+(24*level)% " + ChatColor.GRAY + "Extra Crop Chance",
            3,
            40,
            Material.CARROT
    ),
    REAPER_III(
            "Reaper III",
            ChatColor.GRAY + "Reduce harvest requirement",
            ChatColor.YELLOW + "-(1*level)% " + ChatColor.GRAY + "Harvest requirement",
            5,
            40,
            Material.WITHER_ROSE
    ),
    HALLOWEEN(
            "Halloween",
            ChatColor.GRAY + "Reduce Scythe durability cost",
            ChatColor.YELLOW + "-" + "(1*level)" + ChatColor.GRAY + " Scythe durability",
            5,
            40,
            Material.PUMPKIN
    ),
    FESTIVAL_BEE_DURATION_I(
            "Festival Bee Duration I",
            ChatColor.GRAY + "Longer Festival Bees",
            ChatColor.YELLOW + "+(10*level)s Festival Bee Duration",
            5,
            40,
            Material.CLOCK
    ),
    FESTIVAL_BEES_II(
            "Festival Bees II",
            ChatColor.GRAY + "Chance to spawn Festival Bee",
            ChatColor.YELLOW + "+(0.25*level)% " + ChatColor.GRAY + "Festival Bee chance",
            2,
            40,
            Material.HONEYCOMB
    ),

    EXTRA_CROP_CHANCE_IV(
            "Extra Crop Chance IV",
            ChatColor.GRAY + "Massively increases crop yield",
            ChatColor.YELLOW + "+(32*level)% " + ChatColor.GRAY + "Extra Crop Chance",
            3,
            60,
            Material.BEETROOT
    ),
    REAPER_IV(
            "Reaper IV",
            ChatColor.GRAY + "Reduce harvest requirement",
            ChatColor.YELLOW + "-(1*level)% " + ChatColor.GRAY + "Harvest requirement",
            5,
            60,
            Material.GOLDEN_HOE
    ),
    FERTILIZER_EFFICIENCY(
            "Fertilizer Efficiency",
            ChatColor.GRAY + "Chance for double fertilizer growth",
            ChatColor.YELLOW + "+(20*level)% " + ChatColor.GRAY + "double growth chance",
            5,
            60,
            Material.BONE_MEAL
    ),
    FESTIVAL_BEE_DURATION_II(
            "Festival Bee Duration II",
            ChatColor.GRAY + "Even longer Festival Bees",
            ChatColor.YELLOW + "+(10*level)s Festival Bee Duration",
            5,
            60,
            Material.CLOCK
    ),
    FESTIVAL_BEES_III(
            "Festival Bees III",
            ChatColor.GRAY + "Chance to spawn Festival Bee",
            ChatColor.YELLOW + "+(0.25*level)% " + ChatColor.GRAY + "Festival Bee chance",
            2,
            60,
            Material.HONEY_BLOCK
    ),

    EXTRA_CROP_CHANCE_V(
            "Extra Crop Chance V",
            ChatColor.GRAY + "Ultimate crop yield",
            ChatColor.YELLOW + "+(40*level)% " + ChatColor.GRAY + "Extra Crop Chance",
            4,
            80,
            Material.NETHERITE_HOE
    ),
    REAPER_V(
            "Reaper V",
            ChatColor.GRAY + "Reduce harvest requirement",
            ChatColor.YELLOW + "-(1*level)% " + ChatColor.GRAY + "Harvest requirement",
            5,
            80,
            Material.NETHER_STAR
    ),
    FESTIVAL_BEES_IV(
            "Festival Bees IV",
            ChatColor.GRAY + "Chance to spawn Festival Bee",
            ChatColor.YELLOW + "+(0.25*level)% " + ChatColor.GRAY + "Festival Bee chance",
            2,
            80,
            Material.BEE_NEST
    ),
    SWARM(
            "Swarm",
            ChatColor.GRAY + "Chance to spawn double Festival Bees",
            ChatColor.YELLOW + "+(10*level)% " + ChatColor.GRAY + "double bee chance",
            5,
            80,
            Material.HONEYCOMB_BLOCK
    ),
    HIVEMIND(
            "Hivemind",
            ChatColor.GRAY + "Festival Bees last longer",
            ChatColor.YELLOW + "+(25*level)% " + ChatColor.GRAY + "Festival Bee Duration",
            4,
            80,
            Material.BEE_SPAWN_EGG
    );


    private final String name;
    private final String description;
    private final String technicalDescription;
    private final int maxLevel;
    private final int levelRequirement;
    private final Material icon;

    Talent(String name, String description, String technicalDescription, int maxLevel, int levelRequirement, Material icon) {
        this.name = name;
        this.description = description;
        this.technicalDescription = technicalDescription;
        this.maxLevel = maxLevel;
        this.levelRequirement = levelRequirement;
        this.icon = icon;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }
    public String getTechnicalDescription() {
        return technicalDescription;
    }

    public int getMaxLevel() {
        return maxLevel;
    }

    public int getLevelRequirement() {
        return levelRequirement;
    }

    public Material getIcon() {
        return icon;
    }

    public TalentRarity getRarity() {
        return TalentRarity.fromRequirement(levelRequirement);
    }
}
