package goat.minecraft.minecraftnew.utils.devtools;


import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class ItemRegistry {
    private ItemRegistry() {
    } // Private constructor to prevent instantiation


    public static ItemStack getItemByName(String itemName) {
        // Replace underscores with spaces
        String formattedName = itemName.replace("_", " ");

        for (Method method : ItemRegistry.class.getDeclaredMethods()) {
            if (method.getReturnType().equals(ItemStack.class)) {
                try {
                    ItemStack item = (ItemStack) method.invoke(null);
                    if (item != null) {
                        ItemMeta meta = item.getItemMeta();
                        if (meta != null) {
                            // Compare stripped color from displayName with stripped color from input
                            String displayNameNoColor = ChatColor.stripColor(meta.getDisplayName());
                            String formattedNoColor = ChatColor.stripColor(formattedName);

                            if (displayNameNoColor.equalsIgnoreCase(formattedNoColor)) {
                                return item;
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }


    public static ItemStack getPetrifiedLog() {
        return createCustomItem(
                Material.OAK_WOOD,
                ChatColor.YELLOW + "Petrified Log",
                Arrays.asList(
                        ChatColor.GRAY + "A hardened log infused with forest magic.",
                        ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Apply it to equipment to unlock the secrets of Unbreaking.",
                        ChatColor.DARK_PURPLE + "Smithing Item"
                ), 1, false, true);

    }
    public static ItemStack getWarp() {
        return createCustomItem(
                Material.GLOWSTONE_DUST,
                ChatColor.YELLOW + "Warp",
                Arrays.asList(
                        ChatColor.GRAY + "A secret traveling technique.",
                        ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Warps you forward 8 blocks.",
                        ChatColor.DARK_PURPLE + "Artifact"
                ), 1, false, true);

    }
    public static ItemStack getPinecone() {
        return createCustomItem(
                Material.SPRUCE_SAPLING,
                ChatColor.YELLOW + "Pinecone",
                Arrays.asList(
                        ChatColor.GRAY + "A pinecone that never withers.",
                        ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Apply it to equipment to unlock the secrets of Blast Protection.",
                        ChatColor.DARK_PURPLE + "Smithing Item"
                ), 1, false, true);

    }

    public static ItemStack getBirchStrip() {
        return createCustomItem(
                Material.PAPER,
                ChatColor.YELLOW + "Birch Strip",
                Arrays.asList(
                        ChatColor.GRAY + "A smooth strip from birch wood.",
                        ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Apply it to equipment to unlock the secrets of Projectile Protection.",
                        ChatColor.DARK_PURPLE + "Smithing Item"
                ), 1, false, true);

    }

    public static ItemStack getHumidBark() {
        return createCustomItem(
                Material.COCOA_BEANS,
                ChatColor.YELLOW + "Humid Bark",
                Arrays.asList(
                        ChatColor.GRAY + "Bark soaked in jungle moisture.",
                        ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Apply it to equipment to unlock the secrets of Feather Falling.",
                        ChatColor.DARK_PURPLE + "Smithing Item"
                ), 1, false, true);

    }

    public static ItemStack getAcaciaGum() {
        return createCustomItem(
                Material.HONEY_BOTTLE,
                ChatColor.YELLOW + "Acacia Gum",
                Arrays.asList(
                        ChatColor.GRAY + "Sticky gum from acacia trees.",
                        ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Apply it to equipment to unlock the secrets of Fire Protection.",
                        ChatColor.DARK_PURPLE + "Smithing Item"
                ), 1, false, true);

    }

    public static ItemStack getAcorn() {
        return createCustomItem(
                Material.BEETROOT_SEEDS,
                ChatColor.YELLOW + "Acorn",
                Arrays.asList(
                        ChatColor.GRAY + "An acorn with dark energy.",
                        ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Apply it to equipment to unlock the secrets of Blast Protection.",
                        ChatColor.DARK_PURPLE + "Smithing Item"
                ), 1, false, true);

    }

    public static ItemStack getCherryBlossom() {
        return createCustomItem(
                Material.PINK_TULIP,
                ChatColor.YELLOW + "Cherry Blossom",
                Arrays.asList(
                        ChatColor.GRAY + "A delicate cherry blossom.",
                        ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Apply it to equipment to unlock the secrets of Unbreaking +II.",
                        ChatColor.DARK_PURPLE + "Smithing Item"
                ), 1, false, true);

    }

    public static ItemStack getMapleBark() {
        return createCustomItem(
                Material.CRIMSON_HYPHAE,
                ChatColor.YELLOW + "Maple Bark",
                Arrays.asList(
                        ChatColor.GRAY + "Bark imbued with crimson essence.",
                        ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Apply it to equipment to unlock the secrets of Fire Protection.",
                        ChatColor.DARK_PURPLE + "Smithing Item"
                ), 1, false, true);

    }

    public static ItemStack getBlueNetherWart() {
        return createCustomItem(
                Material.NETHER_WART,
                ChatColor.YELLOW + "Blue Nether Wart",
                Arrays.asList(
                        ChatColor.GRAY + "A warped growth from the Nether.",
                        ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Apply it to equipment to unlock the secrets of Blast Protection.",
                        ChatColor.DARK_PURPLE + "Smithing Item"
                ), 1, false, true);
    }



    public static ItemStack getExperienceArtifact() {
        return createCustomItem(Material.GLASS_BOTTLE, ChatColor.YELLOW +
                "Experience Artifact Tier 1", Arrays.asList(
                ChatColor.GRAY + "Max level of 3",
                ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Summons 25,000 XP.",
                ChatColor.DARK_PURPLE + "Artifact"
        ), 1, false, true);
    }

    public static ItemStack getFarmerEnchant() {
        return createCustomItem(Material.RABBIT_STEW, ChatColor.YELLOW +
                "Well Balanced Meal", Arrays.asList(
                ChatColor.GRAY + "Max level of III",
                ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Adds 1 Level of Feed to items.",
                ChatColor.DARK_PURPLE + "Smithing Item"
        ), 1, false, true);
    }

    public static ItemStack getButcherEnchant() {
        return createCustomItem(Material.GOLDEN_AXE, ChatColor.YELLOW +
                "Brutal Tactics", Arrays.asList(
                ChatColor.GRAY + "Max level of V",
                ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Adds 1 Level of Cleaver to items.",
                ChatColor.DARK_PURPLE + "Smithing Item"
        ), 1, false, true);
    }

    public static ItemStack getFisherEnchant() {
        return createCustomItem(Material.COD, ChatColor.YELLOW + "Call of the Void", Arrays.asList(
                ChatColor.GRAY + "Max level of V",
                ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Adds 1 Level of Call of the Void to items.",
                ChatColor.DARK_PURPLE + "Smithing Item"
        ), 1, false, true);
    }

    public static ItemStack getIronGolem() {
        return createCustomItem(Material.IRON_BLOCK, ChatColor.YELLOW +
                "Iron Golem", Arrays.asList(
                ChatColor.GRAY + "Ancient Summoning Artifact.",
                ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Summon an Iron Golem.",
                ChatColor.DARK_PURPLE + "Summoning Artifact"
        ), 1, false, true);
    }

    public static ItemStack getLibrarianEnchant() {
        return createCustomItem(Material.EXPERIENCE_BOTTLE, ChatColor.YELLOW +
                "Savant", Arrays.asList(
                ChatColor.GRAY + "Max level of 1",
                ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Adds 1 Level of Savant to items.",
                ChatColor.DARK_PURPLE + "Smithing Item"
        ), 1, false, true);
    }

    public static ItemStack getLibrarianEnchantmentTwo() {
        return createCustomItem(Material.SOUL_LANTERN, ChatColor.YELLOW +
                "Soul Lantern", Arrays.asList(
                ChatColor.GRAY + "Max level of 5",
                ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Adds 1 Level of Experience to items.",
                ChatColor.DARK_PURPLE + "Smithing Item"
        ), 1, false, true);
    }

    public static ItemStack getRandomArmorTrim() {
        return createCustomItem(Material.PAPER, ChatColor.YELLOW +
                "Draw Random Armor Trim", Arrays.asList(
                ChatColor.GRAY + "A collection of materials and tools",
                ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Creates a random Armor Trim.",
                ChatColor.DARK_PURPLE + "Artifact"
        ), 1, false, true);
    }

    public static ItemStack getArmorerEnchant() {
        return createCustomItem(Material.GLASS_BOTTLE, ChatColor.YELLOW +
                "Oxygen Tank", Arrays.asList(
                ChatColor.GRAY + "Max level of 4",
                ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Adds 1 Level of Ventilation to items.",
                ChatColor.DARK_PURPLE + "Smithing Item"
        ), 1, false, true);
    }

    public static ItemStack getToolsmithEnchant() {
        return createCustomItem(Material.TORCH, ChatColor.YELLOW +
                "Everflame", Arrays.asList(
                ChatColor.GRAY + "Max level of 5",
                ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Adds 1 Level of Forge to items.",
                ChatColor.DARK_PURPLE + "Smithing Item"
        ), 1, false, true);
    }

    public static ItemStack getToolsmithEnchantTwo() {
        return createCustomItem(Material.CHAIN, ChatColor.YELLOW +
                "Climbing Rope", Arrays.asList(
                ChatColor.GRAY + "Max level of 1",
                ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Adds 1 Level of Rappel to items.",
                ChatColor.DARK_PURPLE + "Smithing Item"
        ), 1, false, true);
    }

    public static ItemStack getShepherdArtifact() {
        return createCustomItem(Material.BRUSH, ChatColor.YELLOW +
                "Creative Mind", Arrays.asList(
                ChatColor.GRAY + "A collection of Colors and Mixes",
                ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Hydrates All Concrete",
                ChatColor.DARK_PURPLE + "Artifact"
        ), 1, false, true);
    }

    public static ItemStack getShepherdEnchant() {
        return createCustomItem(Material.SHEARS, ChatColor.YELLOW +
                "Laceration", Arrays.asList(
                ChatColor.GRAY + "Max level of 5",
                ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Adds 1 Level of Shear to items.",
                ChatColor.DARK_PURPLE + "Smithing Item"
        ), 1, false, true);
    }

    public static ItemStack getFishingEnchant() {
        return createCustomItem(Material.GOLD_NUGGET, ChatColor.YELLOW +
                "Golden Hook", Arrays.asList(
                ChatColor.GRAY + "Max level of 5",
                ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Adds 1 Level of Piracy to items.",
                ChatColor.DARK_PURPLE + "Smithing Item"
        ), 1, false, true);
    }

    public static ItemStack getLeatherworkerEnchant() {
        return createCustomItem(Material.LEATHER, ChatColor.YELLOW +
                "Hide", Arrays.asList(
                ChatColor.GRAY + "Max level of 4",
                ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Adds 1 Level of Physical Protection to items.",
                ChatColor.DARK_PURPLE + "Smithing Item"
        ), 1, false, true);
    }

    public static ItemStack getLeatherworkerArtifact() {
        return createCustomItem(Material.BOOK, ChatColor.YELLOW +
                "Backpack", Arrays.asList(
                ChatColor.GRAY + "A storage device for items",
                ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Opens Backpack.",
                ChatColor.DARK_PURPLE + "Artifact"
        ), 1, false, true);
    }

    public static ItemStack getClericEnchant() {
        return createCustomItem(Material.SUGAR_CANE, ChatColor.YELLOW +
                "Alchemical Bundle", Arrays.asList(
                ChatColor.GRAY + "Max level of 4",
                ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Adds 1 Level of Alchemy to items.",
                ChatColor.DARK_PURPLE + "Smithing Item"
        ), 1, false, true);
    }


    // Mineshaft Location
    public static ItemStack getCartographerMineshaft() {
        return createCustomItem(
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
    }

    // Stronghold Location
    public static ItemStack getCartographerStronghold() {
        return createCustomItem(
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
    }

    // Village Location
    public static ItemStack getCartographerVillage() {
        return createCustomItem(
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
    }

    // Igloo Location
    public static ItemStack getCartographerIgloo() {
        return createCustomItem(
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
    }

    // Buried Treasure Location
    public static ItemStack getCartographerBuriedTreasure() {
        return createCustomItem(
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
    }

    // Desert Pyramid Location
    public static ItemStack getCartographerDesertPyramid() {
        return createCustomItem(
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
    }

    // Jungle Pyramid Location
    public static ItemStack getCartographerJungleTemple() {
        return createCustomItem(
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
    }

    // Ocean Monument Location
    public static ItemStack getCartographerOceanMonument() {
        return createCustomItem(
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
    }

    // Pillager Outpost Location
    public static ItemStack getCartographerPillagerOutpost() {
        return createCustomItem(
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
    }

    // Swamp Hut Location
    public static ItemStack getCartographerSwampHut() {
        return createCustomItem(
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
    }

    // Woodland Mansion Location
    public static ItemStack getCartographerWoodlandMansion() {
        return createCustomItem(
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
    }

    // Bastion Remnant Location
    public static ItemStack getCartographerBastionRemnant() {
        return createCustomItem(
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
    }

    // End City Location
    public static ItemStack getCartographerEndCity() {
        return createCustomItem(
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
    }

    // Nether Fortress Location
    public static ItemStack getCartographerNetherFortress() {
        return createCustomItem(
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
    }

    // Ocean Ruin Location
    public static ItemStack getCartographerOceanRuins() {
        return createCustomItem(
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
    }

    // Shipwreck Location
    public static ItemStack getCartographerShipwreck() {
        return createCustomItem(
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
    }

    public static ItemStack getAspectoftheJourney() {
        return createCustomItem(
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
    }

    public static ItemStack getFletcherBowEnchant() {
        return createCustomItem(
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
    }

    public static ItemStack getFletcherCrossbowEnchant() {
        return createCustomItem(
                Material.FIRE_CHARGE,
                ChatColor.YELLOW + "Explosive Bolts",
                Arrays.asList(
                        ChatColor.GRAY + "Max level of 10",
                        ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Adds 1 Level of Lethal Reaction to items.",
                        ChatColor.DARK_PURPLE + "Smithing Item"
                ),
                1,
                false,
                true
        );
    }

    public static ItemStack getWeaponsmithEnchant() {
        return createCustomItem(
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
    }

    public static ItemStack getArmorsmithReforge() {
        return createCustomItem(
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
    }

    public static ItemStack getArmorsmithReforgeTwo() {
        return createCustomItem(
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
    }

    public static ItemStack getArmorsmithReforgeThree() {
        return createCustomItem(
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
    }

    public static ItemStack getWeaponsmithReforge() {
        return createCustomItem(
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
    }

    public static ItemStack getWeaponsmithReforgeTwo() {
        return createCustomItem(
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
    }

    public static ItemStack getFishermanReforge() {
        return createCustomItem(
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
    }

    public static ItemStack getToolsmithReforge() {
        return createCustomItem(
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
    }

    public static ItemStack getToolsmithEfficiency() {
        return createCustomItem(
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
    }

    public static ItemStack getToolsmithUnbreaking() {
        return createCustomItem(
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
    }

    public static ItemStack getWeaponsmithSharpness() {
        return createCustomItem(
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
    }

    public static ItemStack getWeaponsmithSweepingEdge() {
        return createCustomItem(
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
    }

    public static ItemStack getWeaponsmithLooting() {
        return createCustomItem(
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
    }

    public static ItemStack getWeaponsmithKnockback() {
        return createCustomItem(
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
    }

    public static ItemStack getWeaponsmithFireAspect() {
        return createCustomItem(
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
    }

    public static ItemStack getWeaponsmithSmite() {
        return createCustomItem(
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
    }

    public static ItemStack getWeaponsmithBaneofAnthropods() {
        return createCustomItem(
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
    }

    public static ItemStack getBait() {
        return createCustomItem(
                Material.OAK_BUTTON,
                ChatColor.YELLOW + "Fish Bait",
                Arrays.asList(
                        ChatColor.GRAY + "Various snacks for fish that make them smile.",
                        ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Apply it to equipment to unlock the secrets of Lure.",
                        ChatColor.DARK_PURPLE + "Smithing Item"
                ),
                1,
                false,
                true
        );
    }

    public static ItemStack getLuck() {
        return createCustomItem(
                Material.GOLD_NUGGET,
                ChatColor.YELLOW + "Lucky",
                Arrays.asList(
                        ChatColor.GRAY + "Various flavours for fish that make them smile.",
                        ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Apply it to equipment to unlock the secrets of Luck of the Sea.",
                        ChatColor.DARK_PURPLE + "Smithing Item"
                ),
                1,
                false,
                true
        );
    }

    public static ItemStack getFishermanLure() {
        return createCustomItem(
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
    }

    public static ItemStack getFishermanLuckoftheSea() {
        return createCustomItem(
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
    }

    public static ItemStack getArmorSmithProtection() {
        return createCustomItem(
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
    }

    public static ItemStack getArmorSmithRespiration() {
        return createCustomItem(
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
    }

    public static ItemStack getArmorSmithThorns() {
        return createCustomItem(
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
    }

    public static ItemStack getArmorSmithFeatherFalling() {
        return createCustomItem(
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
    }

    public static ItemStack getFletcherPower() {
        return createCustomItem(
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
    }

    public static ItemStack getCommonSwordReforge() {
        return createCustomItem(
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
    }

    public static ItemStack getUncommonSwordReforge() {
        return createCustomItem(
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
    }

    public static ItemStack getRareSwordReforge() {
        return createCustomItem(
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
    }

    public static ItemStack getEpicSwordReforge() {
        return createCustomItem(
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
    }
    public static ItemStack getOakBowFrameUpgrade() {
        return createCustomItem(
                Material.OAK_PLANKS,
                ChatColor.WHITE + "Oak Bow",
                Arrays.asList(
                        ChatColor.GRAY + "Upgrades a bow frame using Oak wood",
                        ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Enhances base stats of bows.",
                        ChatColor.DARK_PURPLE + "Smithing Item"
                ),
                1,
                false,
                true
        );
    }

    public static ItemStack getBirchBowFrameUpgrade() {
        return createCustomItem(
                Material.BIRCH_PLANKS,
                ChatColor.GREEN + "Birch Bow",
                Arrays.asList(
                        ChatColor.GRAY + "Upgrades a bow frame using Birch wood",
                        ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Enhances base stats of bows.",
                        ChatColor.DARK_PURPLE + "Smithing Item"
                ),
                1,
                false,
                true
        );
    }

    public static ItemStack getSpruceBowFrameUpgrade() {
        return createCustomItem(
                Material.SPRUCE_PLANKS,
                ChatColor.BLUE + "Spruce Bow",
                Arrays.asList(
                        ChatColor.GRAY + "Upgrades a bow frame using Spruce wood",
                        ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Enhances base stats of bows.",
                        ChatColor.DARK_PURPLE + "Smithing Item"
                ),
                1,
                false,
                true
        );
    }

    public static ItemStack getAcaciaBowFrameUpgrade() {
        return createCustomItem(
                Material.ACACIA_PLANKS,
                ChatColor.DARK_PURPLE + "Acacia Bow",
                Arrays.asList(
                        ChatColor.GRAY + "Upgrades a bow frame using Acacia wood",
                        ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Enhances base stats of bows.",
                        ChatColor.DARK_PURPLE + "Smithing Item"
                ),
                1,
                false,
                true
        );
    }

    public static ItemStack getDarkOakBowFrameUpgrade() {
        return createCustomItem(
                Material.DARK_OAK_PLANKS,
                ChatColor.GOLD + "Dark Oak Bow",
                Arrays.asList(
                        ChatColor.GRAY + "Upgrades a bow frame using Dark Oak wood",
                        ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Enhances base stats of bows.",
                        ChatColor.DARK_PURPLE + "Smithing Item"
                ),
                1,
                false,
                true
        );
    }
    public static ItemStack getLegendarySwordReforge() {
        return createCustomItem(
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
    }


    public static ItemStack getCommonArmorReforge() {
        return createCustomItem(
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
    }

    public static ItemStack getUncommonArmorReforge() {
        return createCustomItem(
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
    }

    public static ItemStack getRareArmorReforge() {
        return createCustomItem(
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
    }

    public static ItemStack getEpicArmorReforge() {
        return createCustomItem(
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
    }

    public static ItemStack getSingularity() {
        return createCustomItem(
                Material.IRON_NUGGET,
                ChatColor.BLUE + "Singularity",
                List.of(ChatColor.GRAY + "A rare blueprint entrusted to the Knights",
                        ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Reforges Equipment to the first Tier.",
                        ChatColor.DARK_PURPLE + "Smithing Item"),
                1,
                false // Set to true if you want it to be unbreakable
                , true
        );
    }

    public static ItemStack getLegendaryArmorReforge() {
        return createCustomItem(
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
    }

    public static ItemStack getCommonToolReforge() {
        return createCustomItem(
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
    }

    public static ItemStack getUncommonToolReforge() {
        return createCustomItem(
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
    }

    public static ItemStack getRareToolReforge() {
        return createCustomItem(
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
    }

    public static ItemStack getEpicToolReforge() {
        return createCustomItem(
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
    }

    public static ItemStack getLegendaryToolReforge() {
        return createCustomItem(
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
    }

    public static ItemStack getDiamondGemstone() {
        return createCustomItem(
                Material.DIAMOND,
                ChatColor.DARK_PURPLE + "Diamond Gemstone",
                List.of(ChatColor.GRAY + "A rare mineral.",
                        ChatColor.GRAY + "Apply it to equipment to unlock triple drop chance.",
                        ChatColor.DARK_PURPLE + "Smithing Item"),
                1,
                false,
                true
        );
    }

    public static ItemStack getLapisGemstone() {
        return createCustomItem(
                Material.LAPIS_LAZULI,
                ChatColor.DARK_PURPLE + "Lapis Gemstone",
                List.of(ChatColor.GRAY + "A rare mineral.",
                        ChatColor.GRAY + "Apply it to equipment to enrich mining XP gains.",
                        ChatColor.DARK_PURPLE + "Smithing Item"),
                1,
                false,
                true
        );
    }

    public static ItemStack getRedstoneGemstone() {
        return createCustomItem(
                Material.REDSTONE,
                ChatColor.DARK_PURPLE + "Redstone Gemstone",
                List.of(ChatColor.GRAY + "A rare mineral.",
                        ChatColor.GRAY + "Apply it to equipment to enrich Gold Fever.",
                        ChatColor.DARK_PURPLE + "Smithing Item"),
                1,
                false,
                true
        );
    }

    public static ItemStack getEmeraldGemstone() {
        return createCustomItem(
                Material.EMERALD,
                ChatColor.DARK_PURPLE + "Emerald Gemstone",
                List.of(ChatColor.GRAY + "A rare mineral.",
                        ChatColor.GRAY + "Apply it to equipment to unlock night vision chance.",
                        ChatColor.DARK_PURPLE + "Smithing Item"),
                1,
                false,
                true
        );
    }

    public static ItemStack getPerfectDiamond() {
        return createCustomItem(
                Material.DIAMOND,
                ChatColor.BLUE + "Perfect Diamond",
                List.of(ChatColor.GRAY + "A rare mineral.",
                        "Apply it to a pickaxe to unlock the secrets of Fortune.",
                        "Smithing Item"),
                1,
                false,
                true
        );
    }

    public static ItemStack getRareItem(Material cropType) {
        switch (cropType) {
            case CARROTS:
                return getCarrotSeeder();
            case POTATOES:
                return getPotatoSeeder();
            case WHEAT:
                return getWheatSeeder();
            case BEETROOTS:
                return getBeetrootSeeder();
            default:
                return null; // No rare item defined for other crops
        }
    }

    public static ItemStack getOrganicSoil() {
        return createCustomItem(
                Material.DIRT,
                ChatColor.YELLOW + "Organic Soil",
                List.of(ChatColor.GRAY + "Strong Soil.",
                        ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Valuable.",
                        ChatColor.DARK_PURPLE + "Trophy Item"),
                1,
                false // Set to true if you want it to be unbreakable
                , true
        );
    }
    public static ItemStack getCompactWood() {
        return createCustomItem(
                Material.STRIPPED_SPRUCE_LOG,
                ChatColor.YELLOW + "Compact Wood",
                List.of(ChatColor.GRAY + "Compressed Wood.",
                        ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Valuable.",
                        ChatColor.DARK_PURPLE + "Trophy Item"),
                1,
                false // Set to true if you want it to be unbreakable
                , true
        );
    }
    public static ItemStack getCompactStone() {
        return createCustomItem(
                Material.CRACKED_DEEPSLATE_BRICKS,
                ChatColor.YELLOW + "Compact Stone",
                List.of(ChatColor.GRAY + "Compressed Stone.",
                        ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Valuable.",
                        ChatColor.DARK_PURPLE + "Trophy Item"),
                1,
                false // Set to true if you want it to be unbreakable
                , true
        );
    }

    public static ItemStack getAutoComposter() {
        return createCustomItem(
                Material.COMPOSTER,
                ChatColor.YELLOW + "Auto-Composter",
                List.of(ChatColor.GRAY + "Automatically composts crops into organic soil.",
                        ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Automatic.",
                        ChatColor.DARK_PURPLE + "Inventory Talisman"),
                1,
                false // Set to true if you want it to be unbreakable
                , true
        );
    }

    public static ItemStack getSpiritBow() {
        return createCustomItem(
                Material.BOW,
                ChatColor.WHITE + "Spirit Bow",
                Arrays.asList(ChatColor.GRAY + "A bow wielded by the spirit."),
                1,
                false, // Unbreakable
                true   // Add enchantment shimmer
        );
    }

    public static ItemStack getHireVillager() {
        return createCustomItem(
                Material.WOODEN_HOE,
                ChatColor.YELLOW + "Hire Villager",
                List.of(ChatColor.GRAY + "A proverbially useful companion.",
                        ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Summons a Villager.",
                        ChatColor.DARK_PURPLE + "Summoning Item"),
                1,
                false // Set to true if you want it to be unbreakable
                , true
        );
    }
    public static ItemStack getBrewingApple() {
        return createCustomItem(
                Material.APPLE,
                ChatColor.GOLD + "Perfect Apple",
                Arrays.asList(
                        ChatColor.GRAY + "An apple a day...",
                        ChatColor.BLUE + "Use: " + ChatColor.GRAY + "A rare consumable that heals and feeds.",
                        ChatColor.DARK_PURPLE + "Artifact"
                ),
                1,
                true, // Unbreakable
                true  // Add enchantment shimmer
        );
    }
    public static ItemStack getMithrilChunk() {
        return createCustomItem(
                Material.LIGHT_BLUE_DYE,
                ChatColor.BLUE + "Mithril Chunk",
                List.of(ChatColor.GRAY + "A rare mineral.",
                        ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Apply it to equipment to unlock the secrets of Unbreaking.",
                        ChatColor.DARK_PURPLE + "Smithing Item"),
                1,
                false // Set to true if you want it to be unbreakable
                , true
        );
    }
    public static ItemStack getSilkWorm() {
        return createCustomItem(
                Material.STRING,
                ChatColor.YELLOW + "Silk Worm",
                List.of(ChatColor.GRAY + "A delicate creature that weaves silk.",
                        ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Apply it to a pickaxe to unlock the secrets of Silk Touch.",
                        ChatColor.DARK_PURPLE + "Smithing Item"),
                1,
                false // Set to true if you want it to be unbreakable
                , true
        );
    }
    public static ItemStack getCarrotSeeder() {
        return createCustomItem(
                Material.GOLDEN_CARROT,
                ChatColor.YELLOW + "CarrotSeeder",
                Arrays.asList(
                        ChatColor.GRAY + "A bag of Carrot Seeds",
                        ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Mass plants seeds.",
                        ChatColor.DARK_PURPLE + "Artifact"
                ),
                1,
                false, // Not unbreakable
                false   // Add enchantment shimmer
        );
    }
    public static ItemStack getPotatoSeeder() {
        return createCustomItem(
                Material.BAKED_POTATO,
                ChatColor.YELLOW + "PotatoSeeder",
                Arrays.asList(
                        ChatColor.GRAY + "A bag of Potato Seeds",
                        ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Mass plants seeds.",
                        ChatColor.DARK_PURPLE + "Artifact"
                ),
                1,
                false, // Not unbreakable
                false   // Add enchantment shimmer
        );
    }
    public static ItemStack getBeetrootSeeder() {
        return createCustomItem(
                Material.RED_DYE,
                ChatColor.YELLOW + "BeetrootSeeder",
                Arrays.asList(
                        ChatColor.GRAY + "A bag of Beet Seeds",
                        ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Mass plants seeds.",
                        ChatColor.DARK_PURPLE + "Artifact"
                ),
                1,
                false, // Not unbreakable
                false   // Add enchantment shimmer
        );
    }
    public static ItemStack getWheatSeeder() {
        return createCustomItem(
                Material.BREAD,
                ChatColor.YELLOW + "WheatSeeder",
                Arrays.asList(
                        ChatColor.GRAY + "A bag of Wheat Seeds",
                        ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Mass plants seeds.",
                        ChatColor.DARK_PURPLE + "Artifact"
                ),
                1,
                false, // Not unbreakable
                false   // Add enchantment shimmer
        );
    }
    public static ItemStack getCalamari() {
        return createCustomItem(
                Material.INK_SAC,
                ChatColor.YELLOW + "Calamari",
                Arrays.asList(
                        ChatColor.GRAY + "A form of fish, I think at least...",
                        ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Apply to food.",
                        ChatColor.DARK_PURPLE + "Culinary Ingredient"
                ),
                1,
                false, // Not unbreakable
                false   // Add enchantment shimmer
        );
    }
    public static ItemStack getDough() {
        return createCustomItem(
                Material.SNOWBALL,
                ChatColor.YELLOW + "Dough",
                Arrays.asList(
                        ChatColor.GRAY + "A form of Bread.",
                        ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Apply to food to add Dough.",
                        ChatColor.DARK_PURPLE + "Culinary Ingredient"
                ),
                1,
                false, // Not unbreakable
                false   // Add enchantment shimmer
        );
    }
    public static ItemStack getButter() {
        return createCustomItem(
                Material.GOLD_INGOT,
                ChatColor.YELLOW + "Butter",
                Arrays.asList(
                        ChatColor.GRAY + "A form of milk.",
                        ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Apply to food to add Butter.",
                        ChatColor.DARK_PURPLE + "Culinary Ingredient"
                ),
                1,
                false, // Not unbreakable
                false   // Add enchantment shimmer
        );
    }
    public static ItemStack getSeaSalt() {
        return createCustomItem(
                Material.SUGAR,
                ChatColor.YELLOW + "Sea Salt",
                Arrays.asList(
                        ChatColor.GRAY + "A rare mineral found deep underwater...",
                        ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Apply to food to add Sea Salt.",
                        ChatColor.DARK_PURPLE + "Culinary Ingredient"
                ),
                1,
                false, // Not unbreakable
                false   // Add enchantment shimmer
        );
    }
    public static ItemStack getRainArtifact() {
        return createCustomItem(
                Material.PRISMARINE_SHARD,
                ChatColor.YELLOW + "Rain",
                Arrays.asList(
                        ChatColor.GRAY + "A strange object.",
                        ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Used in summoning Rain.",
                        ChatColor.DARK_PURPLE + "Artifact"
                ),
                1,
                false,
                true
        );
    }
    public static ItemStack getItemDisplayItem() {
        return createCustomItem(Material.STONE, ChatColor.AQUA + "Item Display",
                Arrays.asList(
                        ChatColor.GRAY + "Took a lot of time to make.",
                        ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Place items on display.",
                        ChatColor.DARK_PURPLE + "Decorative Block"
                ),
                1,
                false, // Not unbreakable
                true   // Add enchantment shimmer
        );
    }


    public static ItemStack getEngineeringDegree() {
        return createCustomItem(
                Material.REDSTONE_TORCH,
                ChatColor.YELLOW + "Engineering Profession",
                Arrays.asList(
                        ChatColor.GRAY + "A bachelors degree.",
                        ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Apply it to a villager to change its profession.",
                        ChatColor.DARK_PURPLE + "Degree Item"
                ),
                1,
                false, // Not unbreakable
                true   // Add enchantment shimmer
        );
    }

    public static ItemStack getLoyaltyContract() {
        return createCustomItem(
                Material.FILLED_MAP,
                ChatColor.YELLOW + "Loyal Declaration",
                Arrays.asList(
                        ChatColor.GRAY + "A binding legal document.",
                        ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Apply it to equipment to unlock the secrets of Loyalty.",
                        ChatColor.DARK_PURPLE + "Enchanting Item"
                ),
                1,
                false, // Not unbreakable
                true   // Add enchantment shimmer
        );
    }
    public static ItemStack getForbiddenBook() {
        return createCustomItem(
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
    }

    public static ItemStack getFishBone() {
        return createCustomItem(Material.BONE, ChatColor.BLUE + "Fish Bone",
                Arrays.asList(
                        ChatColor.GRAY + "A bone from a fish.",
                        ChatColor.BLUE + "Use: " + ChatColor.GREEN + "Valuable",
                        ChatColor.GRAY + "",
                        ChatColor.DARK_PURPLE +"Trophy Item"
                ),
                1,
                false,
                true);
    }
    public static ItemStack getTooth() {
    return createCustomItem(Material.IRON_NUGGET, ChatColor.BLUE + "Creature Tooth",
            Arrays.asList(
            ChatColor.GRAY + "A sharp bone from the mouth of a Sea Creature.",
                    ChatColor.BLUE + "Use: " + ChatColor.GREEN + "Valuable",
                    ChatColor.GRAY + "",
                    ChatColor.DARK_PURPLE + "Trophy Item"
            ),
            1,
            false,
            true);
    }
    public static ItemStack getSkeletonDrop() {
        return createCustomItem(Material.STRING, ChatColor.YELLOW +
            "Bowstring", Arrays.asList(
            ChatColor.GRAY + "Air Technology.",
            ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Apply it to equipment to unlock the secrets of Power.",
            ChatColor.DARK_PURPLE + "Smithing Item"
    ), 1, false, true);
    }
    public static ItemStack getDrownedDrop() {
        return createCustomItem(
                Material.LEATHER_BOOTS,
                ChatColor.YELLOW + "Fins",
                Arrays.asList(
                        ChatColor.GRAY + "Water Technology.",
                        ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Apply it to equipment to unlock the secrets of Depth Strider.",
                        ChatColor.DARK_PURPLE + "Smithing Item"
                ),
                1,
                false,
                true
        );
    }
    public static ItemStack getZombifiedPiglinDrop() {
        return createCustomItem(
                Material.GOLD_INGOT,
                ChatColor.YELLOW + "Gold Bar",
                Arrays.asList( ChatColor.GRAY +
                        "High value magnet.",
                        ChatColor.BLUE + "Use: " +
                                ChatColor.GRAY +
                                "Apply it to equipment to unlock the secrets of Looting.",
                        ChatColor.DARK_PURPLE + "Smithing Item" ),
                1, false, true ); }
    public static ItemStack getUndeadDrop() {
        return createCustomItem(
                Material.ROTTEN_FLESH,
                ChatColor.YELLOW + "Beating Heart",
                Arrays.asList(
                        ChatColor.GRAY + "An undead heart still beating with undead life.",
                        ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Apply it to equipment to unlock the secrets of Smite.",
                        ChatColor.DARK_PURPLE + "Smithing Item"
                ),
                1,
                false,
                true
        );
    }

    public static ItemStack getCreeperDrop() {
        return createCustomItem(
                Material.TNT,
                ChatColor.YELLOW + "Hydrogen Bomb",
                Arrays.asList(
                        ChatColor.GRAY + "500 KG of TNT.",
                        ChatColor.BLUE + "Use: " + ChatColor.GRAY + "to summon a large quantity of live-fuse TNT.",
                        ChatColor.DARK_PURPLE + "Artifact"
                ),
                1,
                false,
                true
        );
    }

    public static ItemStack getSpiderDrop() {
        return createCustomItem(
                Material.FERMENTED_SPIDER_EYE,
                ChatColor.YELLOW + "SpiderBane",
                Arrays.asList(
                        ChatColor.GRAY + "A strange substance lethal against spiders.",
                        ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Apply it to equipment to unlock the secrets of Bane of Anthropods.",
                        ChatColor.DARK_PURPLE + "Smithing Item"
                ),
                1,
                false,
                true
        );
    }

    public static ItemStack getEnderDrop() {
        return createCustomItem(
                Material.ENDER_PEARL,
                ChatColor.YELLOW + "End Pearl",
                Arrays.asList(
                        ChatColor.GRAY + "Something doesn't look normal here...",
                        ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Reusable ender pearl.",
                        ChatColor.DARK_PURPLE + "Artifact"
                ),
                1,
                false,
                true
        );
    }

    public static ItemStack getBlazeDrop() {
        return createCustomItem(
                Material.FIRE_CHARGE,
                ChatColor.YELLOW + "Fire Ball",
                Arrays.asList(
                        ChatColor.GRAY + "A projectile ball of fire.",
                        ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Apply it to equipment to unlock the secrets of Flame.",
                        ChatColor.DARK_PURPLE + "Smithing Item"
                ),
                1,
                false,
                true
        );
    }

    public static ItemStack getWitchDrop() {
        return createCustomItem(
                Material.ENCHANTED_BOOK,
                ChatColor.YELLOW + "Mending",
                Arrays.asList(
                        ChatColor.GRAY + "An extremely rare enchantment.",
                        ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Apply it to equipment to unlock the secrets of Mending.",
                        ChatColor.DARK_PURPLE + "Smithing Item"
                ),
                1,
                false,
                false
        );
    }

    public static ItemStack getWitherSkeletonDrop() {
        return createCustomItem(
                Material.WITHER_SKELETON_SKULL,
                ChatColor.YELLOW + "Wither Skeleton Skull",
                Arrays.asList(
                        ChatColor.GRAY + "A cursed skull.",
                        ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Used in spawning the Wither.",
                        ChatColor.DARK_PURPLE + "Summoning Item"
                ),
                1,
                false,
                true
        );
    }

    public static ItemStack getGuardianDrop() {
        return createCustomItem(
                Material.PRISMARINE_SHARD,
                ChatColor.YELLOW + "Rain",
                Arrays.asList(
                        ChatColor.GRAY + "A strange object.",
                        ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Used in summoning Rain.",
                        ChatColor.DARK_PURPLE + "Artifact"
                ),
                1,
                false,
                true
        );
    }

    public static ItemStack getElderGuardianDrop() {
        return createCustomItem(
                Material.ICE,
                ChatColor.YELLOW + "Frost Heart",
                Arrays.asList(
                        ChatColor.GRAY + "A rare object.",
                        ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Apply it to equipment to unlock the secrets of Frost Walker.",
                        ChatColor.DARK_PURPLE + "Smithing Item"
                ),
                1,
                false,
                true
        );
    }

    public static ItemStack getPillagerDrop() {
        return createCustomItem(
                Material.IRON_BLOCK,
                ChatColor.YELLOW + "Iron Golem",
                Arrays.asList(
                        ChatColor.GRAY + "Ancient Summoning Artifact.",
                        ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Summon an Iron Golem.",
                        ChatColor.DARK_PURPLE + "Summoning Artifact"
                ),
                1,
                false,
                true
        );
    }

    public static ItemStack getVindicatorDrop() {
        return createCustomItem(
                Material.SLIME_BALL,
                ChatColor.YELLOW + "KB Ball",
                Arrays.asList(
                        ChatColor.GRAY + "An extremely bouncy ball.",
                        ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Apply it to equipment to unlock the secrets of Knockback.",
                        ChatColor.DARK_PURPLE + "Smithing Item"
                ),
                1,
                false,
                true
        );
    }

    public static ItemStack getPiglinDrop() {
        return createCustomItem(
                Material.SPECTRAL_ARROW,
                ChatColor.YELLOW + "High Caliber Arrow",
                Arrays.asList(
                        ChatColor.GRAY + "A heavy arrow.",
                        ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Apply it to equipment to unlock the secrets of Piercing.",
                        ChatColor.DARK_PURPLE + "Smithing Item"
                ),
                1,
                false,
                true
        );
    }

    public static ItemStack getPiglinBruteDrop() {
        return createCustomItem(
                Material.SOUL_SOIL,
                ChatColor.YELLOW + "Grains of Soul",
                Arrays.asList(
                        ChatColor.GRAY + "Soul soil with spirits of speed.",
                        ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Apply it to equipment to unlock the secrets of Soul Speed.",
                        ChatColor.DARK_PURPLE + "Smithing Item"
                ),
                1,
                false,
                true
        );
    }
    public static ItemStack getInfernalLooting() {
        return createCustomItem(
                Material.GOLD_BLOCK,
                ChatColor.GOLD + "Midas Gold",
                Arrays.asList(
                        ChatColor.GRAY + "A hellish material that inspires replication.",
                        ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Apply it to equipment to unlock the secrets of Looting V.",
                        ChatColor.AQUA + "Mythical Enchantment"
                ),
                1,
                false,
                true
        );
    }

    public static ItemStack getInfernalUnbreaking() {
        return createCustomItem(
                Material.BEDROCK,
                ChatColor.GOLD + "Unbreakable",
                Arrays.asList(
                        ChatColor.GRAY + "A hellish material that inspires Unbreakability.",
                        ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Apply it to equipment to unlock the secrets of Unbreaking V.",
                        ChatColor.AQUA + "Mythical Enchantment"
                ),
                1,
                false,
                true
        );
    }

    public static ItemStack getInfernalDepthStrider() {
        return createCustomItem(
                Material.GOLDEN_BOOTS,
                ChatColor.GOLD + "LavaStride",
                Arrays.asList(
                        ChatColor.GRAY + "A hellish material that inspires the ocean's current.",
                        ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Apply it to equipment to unlock the secrets of Depth Strider V.",
                        ChatColor.AQUA + "Mythical Enchantment"
                ),
                1,
                false,
                true
        );
    }

    public static ItemStack getInfernalBaneofAnthropods() {
        return createCustomItem(
                Material.COBWEB,
                ChatColor.GOLD + "Extinction",
                Arrays.asList(
                        ChatColor.GRAY + "A hellish material that inspires lethal options against Anthropods.",
                        ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Apply it to equipment to unlock the secrets of Bane of Anthropods VII.",
                        ChatColor.AQUA + "Mythical Enchantment"
                ),
                1,
                false,
                true
        );
    }

    public static ItemStack getInfernalEfficiency() {
        return createCustomItem(
                Material.OBSIDIAN,
                ChatColor.GOLD + "Weak Spot",
                Arrays.asList(
                        ChatColor.GRAY + "A hellish material that inspires lethal options against Blocks.",
                        ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Apply it to equipment to unlock the secrets of Bane of Efficiency VI.",
                        ChatColor.AQUA + "Mythical Enchantment"
                ),
                1,
                false,
                true
        );
    }

    public static ItemStack getInfernalFireAspect() {
        return createCustomItem(
                Material.LAVA_BUCKET,
                ChatColor.GOLD + "Hellfire",
                Arrays.asList(
                        ChatColor.GRAY + "A hellish material that inspires Fire.",
                        ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Apply it to equipment to unlock the secrets of Fire Aspect IV.",
                        ChatColor.AQUA + "Mythical Enchantment"
                ),
                1,
                false,
                true
        );
    }

    public static ItemStack getInfernalSharpness() {
        return createCustomItem(
                Material.IRON_SWORD,
                ChatColor.GOLD + "Shrapnel",
                Arrays.asList(
                        ChatColor.GRAY + "A hellish material that inspires lethal options against mobs.",
                        ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Apply it to equipment to unlock the secrets of Sharpness VII.",
                        ChatColor.AQUA + "Mythical Enchantment"
                ),
                1,
                false,
                true
        );
    }

    public static ItemStack getInfernalSmite() {
        return createCustomItem(
                Material.WITHER_SKELETON_SKULL,
                ChatColor.GOLD + "Cure",
                Arrays.asList(
                        ChatColor.GRAY + "A hellish material that inspires lethal options against Undead.",
                        ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Apply it to equipment to unlock the secrets of Smite VII.",
                        ChatColor.AQUA + "Mythical Enchantment"
                ),
                1,
                false,
                true
        );
    }

    public static ItemStack getInfernalLure() {
        return createCustomItem(
                Material.GOAT_HORN,
                ChatColor.GOLD + "Howl",
                Arrays.asList(
                        ChatColor.GRAY + "A hellish material that inspires fish hunger.",
                        ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Apply it to equipment to unlock the secrets of Lure V.",
                        ChatColor.AQUA + "Mythical Enchantment"
                ),
                1,
                false,
                true
        );
    }
    public static ItemStack getShallowShell() {
        return createCustomItem(
                Material.SCUTE,
                ChatColor.LIGHT_PURPLE + "Shallow Shell",
                Arrays.asList(
                        ChatColor.GRAY + "A shell found in shallow waters.",
                        ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Repairs equipment slightly.",
                        ChatColor.GRAY + "Restores 100 durability.",
                        ChatColor.DARK_PURPLE + "Smithing Item"
                ),
                1,
                false,
                true
        );
    }

    public static ItemStack getShell() {
        return createCustomItem(
                Material.CYAN_DYE,
                ChatColor.LIGHT_PURPLE + "Shell",
                Arrays.asList(
                        ChatColor.GRAY + "A sturdy shell.",
                        ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Repairs equipment moderately.",
                        ChatColor.GRAY + "Restores 200 durability.",
                        ChatColor.DARK_PURPLE + "Smithing Item"
                ),
                1,
                false,
                true
        );
    }

    public static ItemStack getLuminescentInk() {
        return createCustomItem(
                Material.GLOW_INK_SAC,
                ChatColor.LIGHT_PURPLE + "Luminescent Ink",
                Arrays.asList(
                        ChatColor.GRAY + "A glowing ink.",
                        ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Brewing ingredient for Glowing.",
                        ChatColor.GRAY + "Adds Glowing I",
                        ChatColor.DARK_PURPLE + "Brewing Ingredient"
                ),
                1,
                false,
                true
        );
    }

    public static ItemStack getLeviathanHeart() {
        return createCustomItem(
                Material.RED_DYE,
                ChatColor.LIGHT_PURPLE + "Leviathan Heart",
                Arrays.asList(
                        ChatColor.GRAY + "The beating heart of a mighty creature.",
                        ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Artifact for health.",
                        ChatColor.GRAY + "Adds Regeneration 8 for 180 seconds.",
                        ChatColor.DARK_PURPLE + "Artifact"
                ),
                1,
                false,
                true
        );
    }

    public static ItemStack getDeepShell() {
        return createCustomItem(
                Material.PURPLE_DYE,
                ChatColor.LIGHT_PURPLE + "Deep Shell",
                Arrays.asList(
                        ChatColor.GRAY + "A resilient shell from the depths.",
                        ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Repairs equipment greatly.",
                        ChatColor.GRAY + "Restores 400 durability.",
                        ChatColor.DARK_PURPLE + "Smithing Item"
                ),
                1,
                false,
                true
        );
    }

    public static ItemStack getAbyssalInk() {
        return createCustomItem(
                Material.BLACK_DYE,
                ChatColor.LIGHT_PURPLE + "Abyssal Ink",
                Arrays.asList(
                        ChatColor.GRAY + "Ink from the deepest abyss.",
                        ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Exceptionally powerful Potency Modifier.",
                        ChatColor.GRAY + "",
                        ChatColor.DARK_PURPLE + "Mastery Brewing Modifier"
                ),
                1,
                false,
                true
        );
    }

    public static ItemStack getAbyssalShell() {
        return createCustomItem(
                Material.YELLOW_DYE,
                ChatColor.LIGHT_PURPLE + "Abyssal Shell",
                Arrays.asList(
                        ChatColor.GRAY + "A shell from the deepest abyss.",
                        ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Repairs equipment massively.",
                        ChatColor.GRAY + "Restores 800 durability.",
                        ChatColor.DARK_PURPLE + "Smithing Item"
                ),
                1,
                false,
                true
        );
    }

    public static ItemStack getAbyssalVenom() {
        return createCustomItem(
                Material.POTION,
                ChatColor.LIGHT_PURPLE + "Abyssal Venom",
                Arrays.asList(
                        ChatColor.GRAY + "A vial of fatal venom.",
                        ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Exceptionally powerful Duration Modifier.",
                        ChatColor.GRAY + "",
                        ChatColor.DARK_PURPLE + "Mastery Brewing Modifier"
                ),
                1,
                false,
                true
        );
    }


    public static ItemStack getAquaAffinity() {
        return createCustomItem(Material.TURTLE_EGG, ChatColor.YELLOW +
                "Turtle Tactics", Arrays.asList(
                ChatColor.GRAY + "Water Technology.",
                ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Apply it to equipment to unlock the secrets of Aqua Affinity.",
                ChatColor.DARK_PURPLE + "Smithing Item"
        ), 1, false, true);
    }

    public static ItemStack getRespiration() {
        return createCustomItem(Material.GLASS, ChatColor.YELLOW +
                "Diving Helmet", Arrays.asList(
                ChatColor.GRAY + "Water Technology.",
                ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Apply it to equipment to unlock the secrets of Respiration.",
                ChatColor.DARK_PURPLE + "Smithing Item"
        ), 1, false, true);
    }
    public static ItemStack getCulinaryRecipe() {
        return createCustomItem(Material.WRITABLE_BOOK, ChatColor.YELLOW +
                "Cookbook", Arrays.asList(
                ChatColor.GRAY + "Paper Technology.",
                ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Grants 16 random Culinary Recipes.",
                ChatColor.DARK_PURPLE + "Artifact"
        ), 1, false, true);
    }
    public static ItemStack getSwiftSneak() {
        return createCustomItem(Material.LEATHER_LEGGINGS, ChatColor.YELLOW +
                "Swim Trunks", Arrays.asList(
                ChatColor.GRAY + "Water Technology.",
                ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Apply it to equipment to unlock the secrets of Swift Sneak.",
                ChatColor.DARK_PURPLE + "Smithing Item"
        ), 1, false, true);
    }
    public static ItemStack getPreservation() {
        return createCustomItem(Material.ENDER_CHEST, ChatColor.YELLOW +
                "Contingency Plan", Arrays.asList(
                ChatColor.GRAY + "Inventory Technology.",
                ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Adds 1 level of Preservation to Armor.",
                ChatColor.DARK_PURPLE + "Smithing Item"
        ), 1, false, true);
    }
    public static ItemStack getMusicDiscArtifact() {
        return createCustomItem(Material.FEATHER, ChatColor.YELLOW +
                "Inscriber", Arrays.asList(
                ChatColor.GRAY + "Disc Technology.",
                ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Drops a random Music Disc.",
                ChatColor.DARK_PURPLE + "Artifact"
        ), 1, false, true);
    }
    public static ItemStack getPetTraining() {
        return createCustomItem(Material.BONE, ChatColor.YELLOW +
                "Pet Training", Arrays.asList(
                ChatColor.GRAY + "Training Technology.",
                ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Grants 1000 pet XP.",
                ChatColor.DARK_PURPLE + "Artifact"
        ), 1, false, true);
    }

    public static ItemStack getImpaling() {
        return createCustomItem(Material.BONE, ChatColor.YELLOW +
                "Narwhal Tusk", Arrays.asList(
                ChatColor.GRAY + "Water Technology.",
                ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Apply it to equipment to unlock the secrets of Impaling.",
                ChatColor.DARK_PURPLE + "Smithing Item"
        ), 1, false, true);
    }
    public static ItemStack getSecretsOfInfinity() {
        return createCustomItem(
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
    }
    public static ItemStack getSweepingEdge() {
        return createCustomItem(Material.STICK, ChatColor.YELLOW +
                "Sweeping Edge", Arrays.asList(
                ChatColor.GRAY + "Air Technology.",
                ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Apply it to equipment to unlock the secrets of Sweeping Edge.",
                ChatColor.DARK_PURPLE + "Smithing Item"
        ), 1, false, true);
    }

    public static ItemStack getChanneling() {
        return createCustomItem(Material.LIGHTNING_ROD, ChatColor.YELLOW +
                "Lightning Bolt", Arrays.asList(
                ChatColor.GRAY + "Air Technology.",
                ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Apply it to equipment to unlock the secrets of Channeling.",
                ChatColor.DARK_PURPLE + "Smithing Item"
        ), 1, false, true);
    }
    public static ItemStack getTrident() {
        return new ItemStack(Material.TRIDENT);
    }

    public static ItemStack getRiptide() {
        return createCustomItem(Material.CONDUIT, ChatColor.YELLOW +
                "Anaklusmos", Arrays.asList(
                ChatColor.GRAY + "Water Technology.",
                ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Apply it to equipment to unlock the secrets of Riptide.",
                ChatColor.DARK_PURPLE + "Smithing Item"
        ), 1, false, true);
    }




    public static ItemStack getRandomTreasure() {
        Random random = new Random();
        // Define a loot table with items
        List<ItemStack> lootTable = Arrays.asList(
                new ItemStack(Material.NAUTILUS_SHELL),
                new ItemStack(Material.SADDLE),
                new ItemStack(Material.DIAMOND, random.nextInt(10) + 1),
                new ItemStack(Material.EMERALD, random.nextInt(3) + 1),
                new ItemStack(Material.ANCIENT_DEBRIS),
                new ItemStack(Material.ENCHANTED_GOLDEN_APPLE),
                new ItemStack(Material.TOTEM_OF_UNDYING),
                new ItemStack(Material.HEART_OF_THE_SEA),
                new ItemStack(Material.SHULKER_SHELL),
                new ItemStack(Material.SPONGE),
                new ItemStack(Material.SCUTE),
                new ItemStack(Material.WITHER_SKELETON_SKULL),
                new ItemStack(Material.CREEPER_HEAD),
                new ItemStack(Material.ZOMBIE_HEAD),
                new ItemStack(Material.SKELETON_SKULL),
                new ItemStack(Material.NETHER_WART, random.nextInt(3) + 1),
                new ItemStack(Material.ENDER_EYE),
                new ItemStack(Material.EXPERIENCE_BOTTLE, 64),
                new ItemStack(Material.MUSIC_DISC_13),
                new ItemStack(Material.MUSIC_DISC_CAT),
                new ItemStack(Material.MUSIC_DISC_BLOCKS),
                new ItemStack(Material.MUSIC_DISC_CHIRP),
                new ItemStack(Material.MUSIC_DISC_FAR),
                new ItemStack(Material.MUSIC_DISC_MALL),
                new ItemStack(Material.MUSIC_DISC_MELLOHI),
                new ItemStack(Material.MUSIC_DISC_STAL),
                new ItemStack(Material.MUSIC_DISC_STRAD),
                new ItemStack(Material.MUSIC_DISC_WARD),
                new ItemStack(Material.MUSIC_DISC_11),
                new ItemStack(Material.MUSIC_DISC_WAIT),
                new ItemStack(Material.MUSIC_DISC_PIGSTEP),
                new ItemStack(Material.MUSIC_DISC_OTHERSIDE),
                new ItemStack(Material.MUSIC_DISC_RELIC),
                new ItemStack(Material.MUSIC_DISC_5)
        );

        // Select a random item from the loot table and return
        return lootTable.get(random.nextInt(lootTable.size()));
    }

    public static ItemStack createCustomItem(Material material, String name, List<String> lore, int amount, boolean unbreakable, boolean addEnchantmentShimmer) {
        ItemStack item = new ItemStack(material, amount);
        ItemMeta meta = item.getItemMeta();

        // Set custom name
        if (name != null) {
            meta.setDisplayName(name);
        }

        // Set lore
        if (lore != null && !lore.isEmpty()) {
            meta.setLore(lore);
        }

        // Set unbreakable
        //meta.setUnbreakable(unbreakable);

        // Add enchantment shimmer if specified

        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS); // This hides the enchantments from the item's tooltip
        // Set the item meta
        item.setItemMeta(meta);
        if (addEnchantmentShimmer) {
            // Add a dummy enchantment to create the shimmer effect
            item.addUnsafeEnchantment(Enchantment.DURABILITY, 1); // This enchantment does not affect gameplay

        }
        return item;
    }
}
