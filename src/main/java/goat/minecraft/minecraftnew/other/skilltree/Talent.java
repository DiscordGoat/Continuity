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
            40,
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
            40,
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
            20,
            Material.BOW
    ),
    SOVEREIGNTY_MASTERY(
            "Sovereignty Mastery",
            ChatColor.GRAY + "Add a diamond",
            ChatColor.YELLOW + "+200s " + ChatColor.LIGHT_PURPLE + "Sovereignty Duration",
            2,
            40,
            Material.PRISMARINE_SHARD
    ),
    STRENGTH_MASTERY(
            "Strength Mastery",
            ChatColor.GRAY + "Add a Singularity",
            ChatColor.YELLOW + "+200s " + ChatColor.LIGHT_PURPLE + "Strength Duration",
            2,
            20,
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
            20,
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
            20,
            Material.SPIDER_EYE
      ),
    SOLAR_FURY_MASTERY(
            "Solar Fury Mastery",
            ChatColor.GRAY + "Add Blaze Powder",
            ChatColor.YELLOW + "+200s " + ChatColor.GOLD + "Solar Fury Duration",
            2,
            20,
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
    // Smithing rework talents
    REPAIR_AMOUNT_I(
            "Repair Amount I",
            ChatColor.GRAY + "Improve basic repair techniques",
            ChatColor.GREEN + "+3 Repair Amount",
            3,
            1,
            Material.ANVIL
    ),
    QUALITY_MATERIALS_I(
            "Quality Materials I",
            ChatColor.GRAY + "Use better components",
            ChatColor.GREEN + "+1 Repair Quality",
            3,
            1,
            Material.IRON_INGOT
    ),
    ALLOY_I(
            "Alloy I",
            ChatColor.GRAY + "Chance to improve durability",
            ChatColor.YELLOW + "+1.5% Chance for +1 Max Durability",
            4,
            1,
            Material.COPPER_INGOT
    ),
    NOVICE_SMITH(
            "Novice Smith",
            ChatColor.GRAY + "Practice basic reforging",
            ChatColor.YELLOW + "+25% Common Reforge Chance",
            3,
            1,
            Material.STONE_PICKAXE
    ),
    SCRAPS_I(
            "Scraps I",
            ChatColor.GRAY + "Reuse leftover materials",
            ChatColor.YELLOW + "-3 Reforge Mats",
            3,
            1,
            Material.COBBLESTONE
    ),
    NOVICE_FOUNDATIONS(
            "Novice Foundations",
            ChatColor.GRAY + "Protect your anvil from Failed Common Reforges",
            ChatColor.YELLOW + "-25% Anvil Degrade Chance",
            4,
            1,
            Material.STONE_BRICKS
    ),

    REPAIR_AMOUNT_II(
            "Repair Amount II",
            ChatColor.GRAY + "Refine repair methods",
            ChatColor.GREEN + "+4 Repair Amount",
            3,
            20,
            Material.ANVIL
    ),
    QUALITY_MATERIALS_II(
            "Quality Materials II",
            ChatColor.GRAY + "Stronger alloys",
            ChatColor.GREEN + "+2 Repair Quality",
            3,
            20,
            Material.IRON_BLOCK
    ),
    ALLOY_II(
            "Alloy II",
            ChatColor.GRAY + "Chance to add durability",
            ChatColor.YELLOW + "+1.5% Chance for +2 Max Durability",
            4,
            20,
            Material.GOLD_INGOT
    ),
    APPRENTICE_SMITH(
            "Apprentice Smith",
            ChatColor.GRAY + "Improved reforging",
            ChatColor.YELLOW + "+25% Uncommon Reforge Chance",
            3,
            20,
            Material.IRON_PICKAXE
    ),
    SCRAPS_II(
            "Scraps II",
            ChatColor.GRAY + "Reduce material waste",
            ChatColor.YELLOW + "-3 Reforge Mats",
            3,
            20,
            Material.IRON_NUGGET
    ),
    APPRENTICE_FOUNDATIONS(
            "Apprentice Foundations",
            ChatColor.GRAY + "Protects your Anvil from Failed Uncommon Reforges",
            ChatColor.YELLOW + "-25% Anvil Degrade Chance",
            4,
            20,
            Material.IRON_BLOCK
    ),

    REPAIR_AMOUNT_III(
            "Repair Amount III",
            ChatColor.GRAY + "Skilled repair work",
            ChatColor.GREEN + "+5 Repair Amount",
            3,
            40,
            Material.ANVIL
    ),
    QUALITY_MATERIALS_III(
            "Quality Materials III",
            ChatColor.GRAY + "Expert material handling",
            ChatColor.GREEN + "+3 Repair Quality",
            3,
            40,
            Material.GOLD_INGOT
    ),
    ALLOY_III(
            "Alloy III",
            ChatColor.GRAY + "Superior alloys",
            ChatColor.YELLOW + "+1.5% Chance for +3 Max Durability",
            4,
            40,
            Material.GOLD_BLOCK
    ),
    JOURNEYMAN_SMITH(
            "Journeyman Smith",
            ChatColor.GRAY + "Reliable reforging",
            ChatColor.YELLOW + "+25% Rare Reforge Chance",
            3,
            40,
            Material.GOLDEN_PICKAXE
    ),
    SCRAPS_III(
            "Scraps III",
            ChatColor.GRAY + "Efficient recycling",
            ChatColor.YELLOW + "-3 Reforge Mats",
            3,
            40,
            Material.GOLD_NUGGET
    ),
    JOURNEYMAN_FOUNDATIONS(
            "Journeyman Foundations",
            ChatColor.GRAY + "Protect your Anvil from Failed Rare Reforges",
            ChatColor.YELLOW + "-25% Anvil Degrade Chance",
            4,
            40,
            Material.GOLD_BLOCK
    ),

    REPAIR_AMOUNT_IV(
            "Repair Amount IV",
            ChatColor.GRAY + "Expert repair work",
            ChatColor.GREEN + "+6 Repair Amount",
            3,
            60,
            Material.ANVIL
    ),
    QUALITY_MATERIALS_IV(
            "Quality Materials IV",
            ChatColor.GRAY + "Masterwork materials",
            ChatColor.GREEN + "+4 Repair Quality",
            3,
            60,
            Material.DIAMOND
    ),
    ALLOY_IV(
            "Alloy IV",
            ChatColor.GRAY + "Rare alloys",
            ChatColor.YELLOW + "+1.5% Chance for +4 Max Durability",
            4,
            60,
            Material.AMETHYST_SHARD
    ),
    EXPERT_SMITH(
            "Expert Smith",
            ChatColor.GRAY + "Advanced reforging",
            ChatColor.YELLOW + "+25% Epic Reforge Chance",
            3,
            60,
            Material.DIAMOND_PICKAXE
    ),
    SCRAPS_IV(
            "Scraps IV",
            ChatColor.GRAY + "Minimal waste",
            ChatColor.YELLOW + "-3 Reforge Mats",
            3,
            60,
            Material.DIAMOND
    ),
    EXPERT_FOUNDATIONS(
            "Expert Foundations",
            ChatColor.GRAY + "Protect your Anvil from Failed Epic Reforges",
            ChatColor.YELLOW + "-25% Anvil Degrade Chance",
            4,
            60,
            Material.DIAMOND_BLOCK
    ),

    REPAIR_AMOUNT_V(
            "Repair Amount V",
            ChatColor.GRAY + "Legendary repair mastery",
            ChatColor.GREEN + "+7 Repair Amount",
            3,
            80,
            Material.NETHERITE_INGOT
    ),
    QUALITY_MATERIALS_V(
            "Quality Materials V",
            ChatColor.GRAY + "Flawless materials",
            ChatColor.GREEN + "+5 Repair Quality",
            4,
            80,
            Material.NETHERITE_BLOCK
    ),
    ALLOY_V(
            "Alloy V",
            ChatColor.GRAY + "Miraculous alloys",
            ChatColor.YELLOW + "+0.5% Chance for +100 Max Durability",
            3,
            80,
            Material.NETHERITE_SCRAP
    ),
    MASTER_SMITH(
            "Master Smith",
            ChatColor.GRAY + "Perfect reforging",
            ChatColor.YELLOW + "+25% Legendary Reforge Chance",
            3,
            80,
            Material.NETHERITE_PICKAXE
    ),
    SCRAPS_V(
            "Scraps V",
            ChatColor.GRAY + "Zero waste",
            ChatColor.YELLOW + "-3 Reforge Mats",
            3,
            80,
            Material.NETHERITE_SCRAP
    ),
    MASTER_FOUNDATIONS(
            "Master Foundations",
            ChatColor.GRAY + "Protect your Anvil from Failed Legendary Reforges",
            ChatColor.YELLOW + "-25% Anvil Degrade Chance",
            4,
            80,
            Material.NETHERITE_BLOCK
    ),
    // =============================================================
    // Culinary Talents
    // =============================================================
    SATIATION_MASTERY_I(
            "Satiation Mastery I",
            ChatColor.GRAY + "Cooked meals keep you fuller",
            ChatColor.YELLOW + "+1 Bonus Saturation",
            1,
            1,
            Material.COOKED_BEEF
    ),
    CUTTING_BOARD_I(
            "Cutting Board I",
            ChatColor.GRAY + "Hone your knife skills",
            ChatColor.YELLOW + "+4% Chance For Double Culinary Yield",
            5,
            1,
            Material.STONECUTTER
    ),
    LUNCH_RUSH_I(
            "Lunch Rush I",
            ChatColor.GRAY + "Serve dishes faster",
            ChatColor.YELLOW + "-4% Cook time",
            5,
            1,
            Material.FURNACE
    ),
    SWEET_TOOTH(
            "Sweet Tooth",
            ChatColor.GRAY + "Fruits taste even sweeter",
            ChatColor.YELLOW + "+10% Fruits Gains",
            4,
            1,
            Material.SWEET_BERRIES
    ),
    GOLDEN_APPLE(
            "Golden Apple",
            ChatColor.GRAY + "Healthy snacks for heroes",
            ChatColor.YELLOW + "+3s Regeneration I when eating",
            5,
            1,
            Material.GOLDEN_APPLE
    ),

    SATIATION_MASTERY_II(
            "Satiation Mastery II",
            ChatColor.GRAY + "Cooked meals keep you fuller",
            ChatColor.YELLOW + "+1 Bonus Saturation",
            1,
            20,
            Material.COOKED_BEEF
    ),
    CUTTING_BOARD_II(
            "Cutting Board II",
            ChatColor.GRAY + "Hone your knife skills",
            ChatColor.YELLOW + "+4% Chance For Double Culinary Yield",
            5,
            20,
            Material.STONECUTTER
    ),
    LUNCH_RUSH_II(
            "Lunch Rush II",
            ChatColor.GRAY + "Serve dishes faster",
            ChatColor.YELLOW + "-4% Cook time",
            5,
            20,
            Material.FURNACE
    ),
    GRAINS_GAINS(
            "Grains Gains",
            ChatColor.GRAY + "Breads fill your belly",
            ChatColor.YELLOW + "+10% Grains Gains",
            4,
            20,
            Material.BREAD
    ),
    PORTAL_PANTRY(
            "Portal Pantry",
            ChatColor.GRAY + "Grab ingredients from afar",
            ChatColor.YELLOW + "+20% Chance to automatically grab ingredient",
            5,
            20,
            Material.ENDER_CHEST
    ),

    SATIATION_MASTERY_III(
            "Satiation Mastery III",
            ChatColor.GRAY + "Cooked meals keep you fuller",
            ChatColor.YELLOW + "+1 Bonus Saturation",
            1,
            40,
            Material.COOKED_BEEF
    ),
    CUTTING_BOARD_III(
            "Cutting Board III",
            ChatColor.GRAY + "Hone your knife skills",
            ChatColor.YELLOW + "+4% Chance For Double Culinary Yield",
            5,
            40,
            Material.STONECUTTER
    ),
    LUNCH_RUSH_III(
            "Lunch Rush III",
            ChatColor.GRAY + "Serve dishes faster",
            ChatColor.YELLOW + "-4% Cook time",
            5,
            40,
            Material.FURNACE
    ),
    AXE_BODY_SPRAY(
            "Axe Body Spray",
            ChatColor.GRAY + "Protein-packed musk",
            ChatColor.YELLOW + "+10% Protein Gains",
            4,
            40,
            Material.IRON_AXE
    ),
    I_DO_NOT_NEED_A_SNACK(
            "I Do Not Need A Snack",
            ChatColor.GRAY + "Save your provisions",
            ChatColor.YELLOW + "+5% Chance to Refund eaten items",
            5,
            40,
            Material.BOWL
    ),

    SATIATION_MASTERY_IV(
            "Satiation Mastery IV",
            ChatColor.GRAY + "Cooked meals keep you fuller",
            ChatColor.YELLOW + "+1 Bonus Saturation",
            1,
            60,
            Material.COOKED_BEEF
    ),
    CUTTING_BOARD_IV(
            "Cutting Board IV",
            ChatColor.GRAY + "Hone your knife skills",
            ChatColor.YELLOW + "+4% Chance For Double Culinary Yield",
            5,
            60,
            Material.STONECUTTER
    ),
    LUNCH_RUSH_IV(
            "Lunch Rush IV",
            ChatColor.GRAY + "Serve dishes faster",
            ChatColor.YELLOW + "-4% Cook time",
            5,
            60,
            Material.FURNACE
    ),
    RABBIT(
            "Rabbit",
            ChatColor.GRAY + "Veggies fuel your bounce",
            ChatColor.YELLOW + "+10% Veggie Gains",
            4,
            60,
            Material.CARROT
    ),
    PANTRY_OF_PLENTY(
            "Pantry of Plenty",
            ChatColor.GRAY + "Stocked for any feast",
            ChatColor.YELLOW + "+4% Chance to gain 20 Saturation when eating Culinary Delights",
            5,
            60,
            Material.CHEST
    ),

    SATIATION_MASTERY_V(
            "Satiation Mastery V",
            ChatColor.GRAY + "Cooked meals keep you fuller",
            ChatColor.YELLOW + "+1 Bonus Saturation",
            1,
            80,
            Material.COOKED_BEEF
    ),
    CUTTING_BOARD_V(
            "Cutting Board V",
            ChatColor.GRAY + "Hone your knife skills",
            ChatColor.YELLOW + "+4% Chance For Double Culinary Yield",
            5,
            80,
            Material.STONECUTTER
    ),
    LUNCH_RUSH_V(
            "Lunch Rush V",
            ChatColor.GRAY + "Serve dishes faster",
            ChatColor.YELLOW + "-4% Cook time",
            5,
            80,
            Material.FURNACE
    ),
    CAVITY(
            "Cavity",
            ChatColor.GRAY + "Sugar rush supreme",
            ChatColor.YELLOW + "+10% Sugar Gains",
            4,
            80,
            Material.SUGAR
    ),
    CHEFS_KISS(
            "Chef's Kiss",
            ChatColor.GRAY + "Perfection in every recipe",
            ChatColor.YELLOW + "+20% Chance to Refund Recipe Papers",
            5,
            80,
            Material.PAPER
    ),

    HAGGLER_I(
            "Haggler I",
            ChatColor.GRAY + "Basic bargaining tactics",
            ChatColor.YELLOW + "+0.005 Discount",
            3,
            1,
            Material.EMERALD
    ),
    STONKS_I(
            "Stonks I",
            ChatColor.GRAY + "Slightly better resale value",
            ChatColor.YELLOW + "+2% Emerald Gain when selling",
            5,
            1,
            Material.EMERALD
    ),
    SHUT_UP_AND_TAKE_MY_MONEY(
            "Shut Up And Take My Money",
            ChatColor.GRAY + "Right click to buy twice",
            ChatColor.YELLOW + "Right click to purchase 2x",
            2,
            1,
            Material.GOLD_INGOT
    ),
    SWEATSHOP_SUPERVISOR(
            "Sweatshop Supervisor",
            ChatColor.GRAY + "Reduce villager workcycle cooldown",
            ChatColor.YELLOW + "-10s Villager Workcycle Cooldown",
            5,
            1,
            Material.CLOCK
    ),
    CORPORATE_BENEFITS(
            "Corporate Benefits",
            ChatColor.GRAY + "Villagers rank up sooner",
            ChatColor.YELLOW + "-5d Villager Tier Threshold",
            5,
            1,
            Material.GOLD_NUGGET
    ),

    HAGGLER_II(
            "Haggler II",
            ChatColor.GRAY + "Improved bartering tactics",
            ChatColor.YELLOW + "+0.01 Discount",
            3,
            20,
            Material.EMERALD
    ),
    STONKS_II(
            "Stonks II",
            ChatColor.GRAY + "Better resale value",
            ChatColor.YELLOW + "+2% Emerald Gain when selling",
            5,
            20,
            Material.EMERALD
    ),
    BULK(
            "Bulk",
            ChatColor.GRAY + "Buying reduces future cost",
            ChatColor.YELLOW + "10% cost reduction for 20s",
            2,
            20,
            Material.CHEST
    ),
    DEADLINE_DICTATOR(
            "Deadline Dictator",
            ChatColor.GRAY + "Further reduce workcycle cooldown",
            ChatColor.YELLOW + "-10s Villager Workcycle Cooldown",
            5,
            20,
            Material.CLOCK
    ),
    UNIFORM(
            "Uniform",
            ChatColor.GRAY + "Protect villagers while online",
            ChatColor.YELLOW + "+10% Villager Damage Resistance",
            5,
            20,
            Material.LEATHER_CHESTPLATE
    ),

    HAGGLER_III(
            "Haggler III",
            ChatColor.GRAY + "Advanced haggling techniques",
            ChatColor.YELLOW + "+0.015 Discount",
            3,
            40,
            Material.EMERALD
    ),
    STONKS_III(
            "Stonks III",
            ChatColor.GRAY + "Even better resale value",
            ChatColor.YELLOW + "+2% Emerald Gain when selling",
            5,
            40,
            Material.EMERALD
    ),
    INTEREST(
            "Interest",
            ChatColor.GRAY + "Occasional bank bonus",
            ChatColor.YELLOW + "1% chance to add 1% more Emeralds to Bank",
            2,
            40,
            Material.EMERALD_BLOCK
    ),
    TASKMASTER_TYRANT(
            "Taskmaster Tyrant",
            ChatColor.GRAY + "Further reduce workcycle cooldown",
            ChatColor.YELLOW + "-10s Villager Workcycle Cooldown",
            5,
            40,
            Material.CLOCK
    ),
    OVERSTOCKED(
            "Overstocked",
            ChatColor.GRAY + "Chance to buy for free",
            ChatColor.YELLOW + "+2% Chance to buy an item for free",
            5,
            40,
            Material.CHEST_MINECART
    ),

    HAGGLER_IV(
            "Haggler IV",
            ChatColor.GRAY + "Master negotiator",
            ChatColor.YELLOW + "+0.02 Discount",
            3,
            60,
            Material.EMERALD
    ),
    STONKS_IV(
            "Stonks IV",
            ChatColor.GRAY + "Excellent resale value",
            ChatColor.YELLOW + "+2% Emerald Gain when selling",
            5,
            60,
            Material.EMERALD
    ),
    OVERTIME_OVERLORD(
            "Overtime Overlord",
            ChatColor.GRAY + "Greatly reduce workcycle cooldown",
            ChatColor.YELLOW + "-10s Villager Workcycle Cooldown",
            7,
            60,
            Material.CLOCK
    ),
    ITS_ALIVE(
            "It's Alive!",
            ChatColor.GRAY + "Prevent villager mutations",
            ChatColor.YELLOW + "+20% Chance to fail transformations",
            5,
            60,
            Material.LIGHTNING_ROD
    ),

    HAGGLER_V(
            "Haggler V",
            ChatColor.GRAY + "Legendary bargainer",
            ChatColor.YELLOW + "+0.025 Discount",
            4,
            80,
            Material.EMERALD
    ),
    STONKS_V(
            "Stonks V",
            ChatColor.GRAY + "Best resale value",
            ChatColor.YELLOW + "+2% Emerald Gain when selling",
            5,
            80,
            Material.EMERALD
    ),
    SLAVE_DRIVER(
            "Slave Driver",
            ChatColor.GRAY + "Maximum workcycle reduction",
            ChatColor.YELLOW + "-10s Villager Workcycle Cooldown",
            5,
            80,
            Material.CLOCK
    ),
    BILLIONAIRE_DISCOUNT(
            "Billionaire's Discount",
            ChatColor.GRAY + "Massive trade discounts",
            ChatColor.YELLOW + "+5% Discount",
            6,
            80,
            Material.DIAMOND
    ),
    SPIRIT_CHANCE_I(
            "Spirit Chance I",
            ChatColor.GRAY + "Increase chance to encounter spirits",
            ChatColor.YELLOW + "+0.0002 Spirit Chance",
            3,
            1,
            Material.SOUL_TORCH
    ),
    TIMBER_I(
            "Timber I",
            ChatColor.GRAY + "Chance for extra logs",
            ChatColor.YELLOW + "+20% Double Logs Chance",
            5,
            1,
            Material.OAK_LOG
    ),
    LEVERAGE_I(
            "Leverage I",
            ChatColor.GRAY + "Chance to gain Haste while chopping",
            ChatColor.YELLOW + "+2% Haste Chance",
            5,
            1,
            Material.SUGAR
    ),
    FOREST_FRENZY(
            "Forest Frenzy",
            ChatColor.GRAY + "Extends Forestry Haste duration",
            ChatColor.YELLOW + "+10s Haste Duration",
            2,
            1,
            Material.REDSTONE_TORCH
    ),
    REGROWTH_I(
            "Regrowth I",
            ChatColor.GRAY + "Reduces sapling growth cooldown",
            ChatColor.YELLOW + "-1d Sapling Growth Cooldown",
            5,
            1,
            Material.OAK_SAPLING
    ),
    SPIRIT_CHANCE_II(
            "Spirit Chance II",
            ChatColor.GRAY + "Increase chance to encounter spirits",
            ChatColor.YELLOW + "+0.0004 Spirit Chance",
            3,
            20,
            Material.SOUL_TORCH
    ),
    TIMBER_II(
            "Timber II",
            ChatColor.GRAY + "Chance for even more logs",
            ChatColor.YELLOW + "+20% Triple Logs Chance",
            5,
            20,
            Material.OAK_LOG
    ),
    LEVERAGE_II(
            "Leverage II",
            ChatColor.GRAY + "Chance to gain Haste while chopping",
            ChatColor.YELLOW + "+2% Haste Chance",
            6,
            20,
            Material.SUGAR
    ),
    PHOTOSYNTHESIS(
            "Photosynthesis",
            ChatColor.GRAY + "Recover health when using Treecapitator",
            ChatColor.GREEN + "+1 Health on Treecapitator use",
            6,
            20,
            Material.SUNFLOWER
    ),
    REGROWTH_II(
            "Regrowth II",
            ChatColor.GRAY + "Reduces sapling growth cooldown",
            ChatColor.YELLOW + "-1d Sapling Growth Cooldown",
            5,
            20,
            Material.OAK_SAPLING
    ),
    SPIRIT_CHANCE_III(
            "Spirit Chance III",
            ChatColor.GRAY + "Increase chance to encounter spirits",
            ChatColor.YELLOW + "+0.0006 Spirit Chance",
            3,
            40,
            Material.SOUL_TORCH
    ),
    TIMBER_III(
            "Timber III",
            ChatColor.GRAY + "Chance for a huge haul",
            ChatColor.YELLOW + "+20% Quadruple Logs Chance",
            5,
            40,
            Material.OAK_LOG
    ),
    LEVERAGE_III(
            "Leverage III",
            ChatColor.GRAY + "Chance to gain Haste while chopping",
            ChatColor.YELLOW + "+2% Haste Chance",
            4,
            40,
            Material.SUGAR
    ),
    ONE_HUNDRED_ACRE_WOODS(
            "100 Acre Woods",
            ChatColor.GRAY + "Chance to gain a Honey Bottle when chopping",
            ChatColor.YELLOW + "+1% Honey Bottle Chance",
            5,
            40,
            Material.HONEY_BOTTLE
    ),
    SPECTRAL_ARMOR(
            "Spectral Armor",
            ChatColor.GRAY + "Reduced spirit damage taken",
            ChatColor.YELLOW + "-10% Spirit Damage",
            3,
            40,
            Material.LEATHER_CHESTPLATE
    ),
    SPIRIT_CHANCE_IV(
            "Spirit Chance IV",
            ChatColor.GRAY + "Increase chance to encounter spirits",
            ChatColor.YELLOW + "+0.0008 Spirit Chance",
            3,
            60,
            Material.SOUL_TORCH
    ),
    LEVERAGE_IV(
            "Leverage IV",
            ChatColor.GRAY + "Chance to gain Haste while chopping",
            ChatColor.YELLOW + "+2% Haste Chance",
            6,
            60,
            Material.SUGAR
    ),
    DEFORESTATION(
            "Deforestation",
            ChatColor.GRAY + "Increase Forestry Haste potency",
            ChatColor.YELLOW + "+1 Potency of Haste",
            3,
            60,
            Material.DIAMOND_AXE
    ),
    HEADHUNTER(
            "HeadHunter",
            ChatColor.GRAY + "Deal more damage to spirits",
            ChatColor.RED + "+10% Damage to Spirits",
            3,
            60,
            Material.BOW
    ),
    REGROWTH_III(
            "Regrowth III",
            ChatColor.GRAY + "Reduces sapling growth cooldown",
            ChatColor.YELLOW + "-1d Sapling Growth Cooldown",
            5,
            60,
            Material.OAK_SAPLING
    ),
    SPIRIT_CHANCE_V(
            "Spirit Chance V",
            ChatColor.GRAY + "Increase chance to encounter spirits",
            ChatColor.YELLOW + "+0.0010 Spirit Chance",
            4,
            80,
            Material.SOUL_TORCH
    ),
    LEVERAGE_V(
            "Leverage V",
            ChatColor.GRAY + "Chance to gain Haste while chopping",
            ChatColor.YELLOW + "+2% Haste Chance",
            4,
            80,
            Material.SUGAR
    ),
    ANCIENT_CONFUSION(
            "Ancient Confusion",
            ChatColor.GRAY + "Lose spirit levels when chopping",
            ChatColor.DARK_GRAY + "-10 Spirit Level",
            5,
            80,
            Material.ROTTEN_FLESH
    ),
    REDEMPTION(
            "Redemption",
            ChatColor.GRAY + "Greater chance for Super Saplings",
            ChatColor.GREEN + "Improved Treecapitator rewards",
            2,
            80,
            Material.OAK_SAPLING
    ),
    PET_TRAINER(
            "Pet Trainer",
            ChatColor.GRAY + "Sharpen your pet handling skills",
            ChatColor.YELLOW + "+4% " + ChatColor.GRAY + "Double Pet XP Chance",
            25,
            1,
            Material.BONE
    ),
    HEALTH_I(
            "Health I",
            ChatColor.GRAY + "Bolster your vitality slightly",
            ChatColor.GREEN + "+1 Bonus Health",
            4,
            1,
            Material.APPLE
    ),
    STUDY_BREWING(
            "Study Brewing",
            ChatColor.GRAY + "Dedicate time to potion research",
            ChatColor.YELLOW + "+1 Brewing Talent",
            100,
            1,
            Material.BREWING_STAND
    ),
    STUDY_SMITHING(
            "Study Smithing",
            ChatColor.GRAY + "Practice superior metalwork",
            ChatColor.YELLOW + "+1 Smithing Talent",
            100,
            1,
            Material.ANVIL
    ),
    STUDY_CULINARY(
            "Study Culinary",
            ChatColor.GRAY + "Experiment with new recipes",
            ChatColor.YELLOW + "+1 Culinary Talent",
            100,
            1,
            Material.COOKED_BEEF
    ),
    STUDY_BARTERING(
            "Study Bartering",
            ChatColor.GRAY + "Learn the art of the deal",
            ChatColor.YELLOW + "+1 Bartering Talent",
            100,
            1,
            Material.EMERALD
    ),
    STUDY_FORESTRY(
            "Study Forestry",
            ChatColor.GRAY + "Understand the ways of the woods",
            ChatColor.YELLOW + "+1 Forestry Talent",
            100,
            1,
            Material.OAK_SAPLING
    ),
    STUDY_TAMING(
            "Study Taming",
            ChatColor.GRAY + "Improve animal handling",
            ChatColor.YELLOW + "+1 Taming Talent",
            100,
            1,
            Material.BONE
    ),
    STUDY_COMBAT(
            "Study Combat",
            ChatColor.GRAY + "Train in advanced combat",
            ChatColor.YELLOW + "+1 Combat Talent",
            100,
            1,
            Material.IRON_SWORD
    ),
    STUDY_TERRAFORMING(
            "Study Terraforming",
            ChatColor.GRAY + "Survey the lands with purpose",
            ChatColor.YELLOW + "+1 Terraforming Talent",
            100,
            1,
            Material.DIRT
    ),
    STUDY_MINING(
            "Study Mining",
            ChatColor.GRAY + "Hone excavation techniques",
            ChatColor.YELLOW + "+1 Mining Talent",
            100,
            1,
            Material.IRON_PICKAXE
    ),
    STUDY_FARMING(
            "Study Farming",
            ChatColor.GRAY + "Master the secrets of crops",
            ChatColor.YELLOW + "+1 Farming Talent",
            100,
            1,
            Material.WHEAT
    ),
    STUDY_FISHING(
            "Study Fishing",
            ChatColor.GRAY + "Learn to reel in the big ones",
            ChatColor.YELLOW + "+1 Fishing Talent",
            100,
            1,
            Material.FISHING_ROD
    ),
    HEALTH_II(
            "Health II",
            ChatColor.GRAY + "Further enhance your vitality",
            ChatColor.GREEN + "+1 Bonus Health",
            6,
            20,
            Material.APPLE
    ),
    HEALTH_III(
            "Health III",
            ChatColor.GRAY + "Push your limits for more health",
            ChatColor.GREEN + "+1 Bonus Health",
            8,
            40,
            Material.APPLE
    ),
    HEALTH_IV(
            "Health IV",
            ChatColor.GRAY + "Achieve remarkable endurance",
            ChatColor.GREEN + "+1 Bonus Health",
            10,
            60,
            Material.APPLE
    ),
    HEALTH_V(
            "Health V",
            ChatColor.GRAY + "Reach peak physical condition",
            ChatColor.GREEN + "+1 Bonus Health",
            12,
            80,
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
    ),

    // =============================================================
    // Taming Talents
    // =============================================================

    BONUS_PET_XP_I(
            "Bonus Pet XP I",
            ChatColor.GRAY + "Increase pet training efficiency",
            ChatColor.YELLOW + "+0.02 Bonus Pet XP Chance",
            3,
            1,
            Material.EXPERIENCE_BOTTLE
    ),
    LULLABY(
            "Lullaby",
            ChatColor.GRAY + "Soothe creatures from farther away",
            ChatColor.YELLOW + "+50% Range",
            1,
            1,
            Material.MUSIC_DISC_CAT
    ),
    FLIGHT(
            "Flight",
            ChatColor.GRAY + "Improve pet flight distance",
            ChatColor.YELLOW + "+0.1km Flight Distance",
            5,
            1,
            Material.ELYTRA
    ),
    DIGGING_CLAWS(
            "Digging Claws",
            ChatColor.GRAY + "Greatly extends haste effects",
            ChatColor.YELLOW + "Double Haste Duration",
            1,
            1,
            Material.IRON_SHOVEL
    ),
    DEVOUR(
            "Devour",
            ChatColor.GRAY + "Gain more food from combat",
            ChatColor.GREEN + "Double Food Gains",
            1,
            1,
            Material.COOKED_BEEF
    ),
    ANGLER(
            "Angler",
            ChatColor.GRAY + "Hook rarer sea creatures",
            ChatColor.YELLOW + "+50% Bonus Sea Creature Chance",
            2,
            1,
            Material.FISHING_ROD
    ),
    LEAP(
            "Leap",
            ChatColor.GRAY + "Occasionally leap without hunger",
            ChatColor.YELLOW + "+50% Hungerless Leap Chance",
            2,
            1,
            Material.RABBIT_FOOT
    ),
    LUMBERJACK(
            "Lumberjack",
            ChatColor.GRAY + "Chop additional logs",
            ChatColor.YELLOW + "+1 Bonus Log Yield",
            5,
            1,
            Material.IRON_AXE
    ),

    BONUS_PET_XP_II(
            "Bonus Pet XP II",
            ChatColor.GRAY + "Further increase pet training",
            ChatColor.YELLOW + "+0.04 Bonus Pet XP Chance",
            3,
            20,
            Material.EXPERIENCE_BOTTLE
    ),
    ANTIDOTE(
            "Antidote",
            ChatColor.GRAY + "Cleanse harmful effects instantly",
            ChatColor.YELLOW + "Remove cooldown",
            1,
            20,
            Material.MILK_BUCKET
    ),
    GREEN_THUMB(
            "Green Thumb",
            ChatColor.GRAY + "Tend crops more frequently",
            ChatColor.YELLOW + "-25% Cooldown",
            4,
            20,
            Material.WHEAT_SEEDS
    ),
    COLLECTOR(
            "Collector",
            ChatColor.GRAY + "Gather items from farther away",
            ChatColor.YELLOW + "+50% Range",
            1,
            20,
            Material.HOPPER
    ),
    WALKING_FORTRESS(
            "Walking Fortress",
            ChatColor.GRAY + "Bolster your defenses",
            ChatColor.YELLOW + "+10% Damage Reduction",
            2,
            20,
            Material.SHIELD
    ),
    SHOTCALLING(
            "Shotcalling",
            ChatColor.GRAY + "Coordinate deadlier volleys",
            ChatColor.RED + "+5% Bonus Arrow Damage",
            4,
            20,
            Material.CROSSBOW
    ),
    SPEED_BOOST(
            "Speed Boost",
            ChatColor.GRAY + "Move quicker alongside pets",
            ChatColor.YELLOW + "+10% Walk Speed",
            5,
            20,
            Material.SUGAR
    ),

    BONUS_PET_XP_III(
            "Bonus Pet XP III",
            ChatColor.GRAY + "Accelerate pet experience gain",
            ChatColor.YELLOW + "+0.06 Bonus Pet XP Chance",
            3,
            40,
            Material.EXPERIENCE_BOTTLE
    ),
    WATERLOGGED(
            "Waterlogged",
            ChatColor.GRAY + "Recover oxygen more often",
            ChatColor.YELLOW + "-1s Oxygen Recovery Cooldown",
            1,
            40,
            Material.KELP
    ),
    ENDLESS_WARP(
            "Endless Warp",
            ChatColor.GRAY + "Carry additional warp energy",
            ChatColor.YELLOW + "+100 Bonus Warp Stacks",
            1,
            40,
            Material.ENDER_PEARL
    ),
    DECAY(
            "Decay",
            ChatColor.GRAY + "Inflict lingering deterioration",
            ChatColor.DARK_GRAY + "+5 Deteriorate Stacks",
            5,
            40,
            Material.ROTTEN_FLESH
    ),
    ASPECT_OF_FROST(
            "Aspect Of Frost",
            ChatColor.GRAY + "Freeze foes in place",
            ChatColor.YELLOW + "Double Slow Duration",
            1,
            40,
            Material.ICE
    ),
    PRACTICE(
            "Practice",
            ChatColor.GRAY + "Bargain more effectively",
            ChatColor.YELLOW + "+5% Bartering XP",
            9,
            40,
            Material.BOOK
    ),

    BONUS_PET_XP_IV(
            "Bonus Pet XP IV",
            ChatColor.GRAY + "Master pet experience gains",
            ChatColor.YELLOW + "+0.08 Bonus Pet XP Chance",
            3,
            60,
            Material.EXPERIENCE_BOTTLE
    ),
    SECRET_LEGION(
            "Secret Legion",
            ChatColor.GRAY + "Legionnaires cost no hunger",
            ChatColor.YELLOW + "No Hunger Cost",
            1,
            60,
            Material.TOTEM_OF_UNDYING
    ),
    BLACKLUNG(
            "Blacklung",
            ChatColor.GRAY + "Breathe freely underground",
            ChatColor.YELLOW + "No Overworld Oxygen Loss",
            1,
            60,
            Material.COAL
    ),
    COMFORTABLE(
            "Comfortable",
            ChatColor.GRAY + "Longer lasting absorption",
            ChatColor.YELLOW + "Double Absorption Duration and Health",
            1,
            60,
            Material.RED_BED
    ),
    SPLASH_POTION(
            "Splash Potion",
            ChatColor.GRAY + "Brew faster splash potions",
            ChatColor.YELLOW + "-10% Brewtime",
            5,
            60,
            Material.SPLASH_POTION
    ),
    EXPERIMENTATION(
            "Experimentation",
            ChatColor.GRAY + "Improve potion potency",
            ChatColor.YELLOW + "+5% Potion Duration",
            8,
            60,
            Material.ENCHANTING_TABLE
    ),
    REVENANT(
            "Revenant",
            ChatColor.GRAY + "Rise again more quickly",
            ChatColor.YELLOW + "-1m Resurrection Time",
            1,
            60,
            Material.TOTEM_OF_UNDYING
    ),

    BONUS_PET_XP_V(
            "Bonus Pet XP V",
            ChatColor.GRAY + "Ultimate pet expertise",
            ChatColor.YELLOW + "+0.1 Bonus Pet XP Chance",
            4,
            80,
            Material.EXPERIENCE_BOTTLE
    ),
    COMPACT_STONE(
            "Compact Stone",
            ChatColor.GRAY + "Reduce stone requirements",
            ChatColor.YELLOW + "-50% Stone Needed",
            1,
            80,
            Material.STONE
    ),
    GROOT(
            "Groot",
            ChatColor.GRAY + "Reduce wood requirements",
            ChatColor.YELLOW + "-50% Wood Needed",
            1,
            80,
            Material.OAK_LOG
    ),
    COMPOSTER(
            "Composter",
            ChatColor.GRAY + "Reduce dirt requirements",
            ChatColor.YELLOW + "-50% Dirt Needed",
            1,
            80,
            Material.COMPOSTER
    ),
    ELITE(
            "Elite",
            ChatColor.GRAY + "Train pets for greater damage",
            ChatColor.RED + "+10% Damage",
            2,
            80,
            Material.DIAMOND_SWORD
    ),
    HAGGLE(
            "Haggle",
            ChatColor.GRAY + "Secure better trades",
            ChatColor.YELLOW + "+5% Discount",
            1,
            80,
            Material.EMERALD
    ),
    QUIRKY(
            "Quirky",
            ChatColor.GRAY + "Traits become more potent",
            ChatColor.YELLOW + "+20% Trait Effect",
            5,
            80,
            Material.BOOK
    ),
    NATURAL_SELECTION(
            "Natural Selection",
            ChatColor.GRAY + "Cull lesser traits from the pool",
            ChatColor.YELLOW + "Removes lowest rarity",
            5,
            80,
            Material.NETHER_STAR
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
