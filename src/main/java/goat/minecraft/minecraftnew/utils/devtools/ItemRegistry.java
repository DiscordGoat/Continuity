package goat.minecraft.minecraftnew.utils.devtools;


import goat.minecraft.minecraftnew.MinecraftNew;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
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


    public static ItemStack getHostility() {
        // Create the base ItemStack
        ItemStack book = new ItemStack(Material.WRITTEN_BOOK, 1);

        // Create BookMeta and set title, author, and pages
        BookMeta bookMeta = (BookMeta) book.getItemMeta();
        if (bookMeta != null) {
            bookMeta.setTitle("Hostility");  // Title shown on hover and in inventory
            bookMeta.setAuthor("DiscordGoat");  // Author of the book

            // Example pages (you can customize further)
            bookMeta.addPage(ChatColor.GRAY + "A deep dive into the nature of hostility.\n\n"
                    + ChatColor.BLUE + "Hostility can manifest in many forms.\n"
                    + ChatColor.GOLD + "It is both a weapon and a shield.\n\n"
                    + ChatColor.YELLOW + "Use it wisely.");
        }
        book.setItemMeta(bookMeta);

        // Add lore and other item customizations using your method
        ItemStack finalBook = createCustomItem(
                book.getType(),
                ChatColor.RED + "Hostility",
                Arrays.asList(
                        ChatColor.GRAY + "A deep dive into the nature of hostility."
                ),
                1,
                false,
                false
        );

        // Merge the BookMeta from book into finalBook
        BookMeta finalMeta = (BookMeta) finalBook.getItemMeta();
        if (finalMeta != null) {
            finalMeta.setTitle(bookMeta.getTitle());
            finalMeta.setAuthor(bookMeta.getAuthor());
            finalMeta.setPages(bookMeta.getPages());
            finalBook.setItemMeta(finalMeta);
        }

        return finalBook;
    }


    public static ItemStack getExpandingStairs() {
        return createCustomItem(
                Material.OAK_STAIRS,
                ChatColor.GOLD + "Expanding Stairs",
                Arrays.asList(
                        ChatColor.GRAY + "A mystical building item that constructs",
                        ChatColor.GRAY + "large roof portions horizontally.",
                        "",
                        ChatColor.YELLOW + "Right-click on block",
                        ChatColor.YELLOW + "to expand a roof segment!"
                ),
                1,
                false,  // not unbreakable
                false   // no enchantment shimmer
        );
    }


    public static ItemStack getShelfItem() {
        return createCustomItem(
                Material.OAK_TRAPDOOR,
                ChatColor.GOLD + "Shelf",
                Arrays.asList(
                        ChatColor.GRAY + "Place on a wall to create a custom Shelf.",
                        ChatColor.BLUE + "Right-click" + ChatColor.GRAY + " to open its storage.",
                        ChatColor.BLUE + "Left-click" + ChatColor.GRAY + " to extract one item.",
                        ChatColor.DARK_PURPLE + "Stores up to 64 of a single item.",
                        ChatColor.DARK_PURPLE + "Persistent across restarts."
                ),
                1,
                false,  // not unbreakable
                false   // no enchantment shimmer
        );
    }


    public static ItemStack getLime() {
        return createCustomItem(
                Material.LIME_DYE,
                ChatColor.GREEN + "Lime",
                Arrays.asList(
                        ChatColor.GRAY + "A zesty citrus fruit from the Oceanic Port.",
                        ChatColor.BLUE   + "Use: " + ChatColor.GRAY + "Add tang to cocktails and desserts.",
                        ChatColor.DARK_PURPLE + "Culinary Ingredient"
                ),
                1,      // amount
                false,  // not unbreakable
                false   // no enchantment glow
        );
    }

    public static ItemStack getPineapple() {
        return createCustomItem(
                Material.BUCKET,
                ChatColor.YELLOW + "Pineapple",
                Arrays.asList(
                        ChatColor.GRAY + "An exclusive Ingredient in the Oceanic Port.",
                        ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Apply to food to add Pineapple. Crazy.",
                        ChatColor.DARK_PURPLE + "Culinary Ingredient"
                ),
                1,
                false, // Not unbreakable
                false   // Add enchantment shimmer
        );
    }
    public static ItemStack getCoconut() {
        return createCustomItem(
                Material.MELON_SLICE,
                ChatColor.YELLOW + "Coconut",
                Arrays.asList(
                        ChatColor.GRAY + "An exclusive Ingredient in the Oceanic Port.",
                        ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Apply to food to add Coconut. Crazy.",
                        ChatColor.DARK_PURPLE + "Culinary Ingredient"
                ),
                1,
                false, // Not unbreakable
                false   // Add enchantment shimmer
        );
    }
    public static ItemStack getRum() {
        return createCustomItem(
                Material.BUCKET,
                ChatColor.YELLOW + "Rum",
                Arrays.asList(
                        ChatColor.GRAY + "An exclusive Ingredient in the Oceanic Port.",
                        ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Apply to food to add Alcohol. Crazy.",
                        ChatColor.DARK_PURPLE + "Culinary Ingredient"
                ),
                1,
                false, // Not unbreakable
                false   // Add enchantment shimmer
        );
    }
    public static ItemStack getChocolate() {
        return createCustomItem(
                Material.COCOA_BEANS,
                ChatColor.YELLOW + "Chocolate",
                Arrays.asList(
                        ChatColor.GRAY + "An exclusive Ingredient in the Oceanic Port.",
                        ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Apply to food to add Chocolate. Crazy.",
                        ChatColor.DARK_PURPLE + "Culinary Ingredient"
                ),
                1,
                false, // Not unbreakable
                false   // Add enchantment shimmer
        );
    }
    public static ItemStack getBanana() {
        return createCustomItem(
                Material.MELON_SLICE,
                ChatColor.YELLOW + "Banana",
                Arrays.asList(
                        ChatColor.GRAY + "An exclusive Fruit in the Oceanic Port.",
                        ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Apply to food to add bananas. Crazy.",
                        ChatColor.DARK_PURPLE + "Culinary Ingredient"
                ),
                1,
                false, // Not unbreakable
                false   // Add enchantment shimmer
        );
    }
    public static ItemStack getMarrow() {
        return createCustomItem(
                Material.BONE_BLOCK,
                ChatColor.GOLD + "Marrow",
                Arrays.asList(
                        ChatColor.GRAY + "A relic forged from the essence of precision.",
                        ChatColor.BLUE + "Used in brewing the Potion of Recurve."
                ),
                1,
                false,
                true
        );
    }

    public static ItemStack getVerdantRelicMarrow() {
        return createCustomItem(
                Material.BONE_MEAL,
                ChatColor.GOLD + "Verdant Relic Marrow",
                Arrays.asList(
                        ChatColor.GRAY + "A refined echo of lethal essence.",
                        ChatColor.BLUE + "Used in brewing the Potion of Recurve."
                ),
                1,
                false,
                true
        );
    }


    public static ItemStack getTreasury() {
        return createCustomItem(
                Material.GOLD_BLOCK,
                ChatColor.GOLD + "Treasury",
                Arrays.asList(
                        ChatColor.GRAY + "A relic of unmatched opulence and power.",
                        ChatColor.BLUE + "Used in brewing the Potion of Liquid Luck"
                ),
                1,
                false,
                true
        );
    }

    public static ItemStack getVerdantRelicTreasury() {
        return createCustomItem(
                Material.GOLD_INGOT,
                ChatColor.GOLD + "Verdant Relic Treasury",
                Arrays.asList(
                        ChatColor.GRAY + "An echo of ancient wealth reimagined.",
                        ChatColor.BLUE + "Used in brewing the Potion of Liquid Luck"
                ),
                1,
                false,
                true
        );
    }


    public static ItemStack getGravity() {
        return createCustomItem(
                Material.OBSIDIAN,
                ChatColor.GOLD + "Gravity",
                Arrays.asList(
                        ChatColor.GRAY + "A theoretical relic, more idea than matter.",
                        ChatColor.BLUE + "Used in brewing the Potion of Strength."
                ),
                1,
                false,
                true
        );
    }

    public static ItemStack getVerdantRelicGravity() {
        return createCustomItem(
                Material.BEETROOT_SEEDS,
                ChatColor.GOLD + "Verdant Relic Gravity",
                Arrays.asList(
                        ChatColor.GRAY + "A Theoretical relic, more idea than matter.",
                        ChatColor.BLUE + "Used in brewing the Potion of Strength."
                ),
                1,
                false,
                true
        );
    }
    public static ItemStack getPesticide() {
        return createCustomItem(
                Material.POTION,
                ChatColor.YELLOW + "Pesticide",
                Arrays.asList(
                        ChatColor.GRAY + "A specialized concoction sold by farmers.",
                        ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Cures Infested complications on Verdant Relics.",
                        ChatColor.DARK_PURPLE + "Artifact"
                ),
                1,
                false,
                true
        );
    }

    /**
     * Vaccination artifact used to cure Zombie Villagers.
     */
    public static ItemStack getVaccination() {
        return createCustomItem(
                Material.HONEY_BOTTLE,
                ChatColor.YELLOW + "Vaccination",
                Arrays.asList(
                        ChatColor.GRAY + "A potent cure for zombification.",
                        ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Right-click a Zombie Villager to cure it.",
                        ChatColor.DARK_PURPLE + "Artifact"
                ),
                1,
                false,
                true
        );
    }


    public static ItemStack getEntionPlastIngredient() {
        return createCustomItem(
                Material.PRISMARINE_SHARD,
                ChatColor.GOLD + "EntionPlast",
                Arrays.asList(
                        ChatColor.GRAY + "A rare, mysterious substance",
                        ChatColor.BLUE + "Key ingredient in the Potion of Fountains."
                ),
                1,
                false,
                true
        );
    }
    public static ItemStack getShatterproof() {
        return createCustomItem(
                Material.AMETHYST_SHARD,
                ChatColor.GOLD + "Shatterproof",
                Arrays.asList(
                        ChatColor.GRAY + "A crystalline relic, infused with sturdy magical essence.",
                        ChatColor.BLUE + "Used in brewing the Potion of Sovereignty."
                ),
                1,
                false,
                true
        );
    }

    public static ItemStack getVerdantRelicShatterproof() {
        return createCustomItem(
                Material.AMETHYST_SHARD,
                ChatColor.GOLD + "Verdant Relic Shatterproof",
                Arrays.asList(
                        ChatColor.GRAY + "A crystalline relic, infused with sturdy magical essence.",
                        ChatColor.BLUE + "Used in brewing the Potion of Sovereignty."
                ),
                1,
                false,
                true
        );
    }


    public static ItemStack getVerdantRelicEntionPlastSeed() {
        return createCustomItem(
                Material.WHEAT_SEEDS,
                ChatColor.GOLD + "Verdant Relic EntionPlast",
                Arrays.asList(
                        ChatColor.GRAY + "A relic seed imbued with the essence of EntionPlast.",
                        ChatColor.BLUE + "Dropped by divers. Right-click on dirt/grass to plant."
                ),
                1,
                false,
                true
        );
    }

    public static ItemStack getEntropyIngredient() {
        return createCustomItem(
                Material.PRISMARINE_SHARD,
                ChatColor.GOLD + "Entropy",
                Arrays.asList(
                        ChatColor.GRAY + "A rare, mysterious substance",
                        ChatColor.BLUE + "Key ingredient in the Potion of Swift Step."
                ),
                1,
                false,
                true
        );
    }

    public static ItemStack getVerdantRelicEntropySeed() {
        return createCustomItem(
                Material.WHEAT_SEEDS,
                ChatColor.GOLD + "Verdant Relic Entropy",
                Arrays.asList(
                        ChatColor.GRAY + "A relic seed imbued with the essence of Entropy.",
                        ChatColor.BLUE + "Dropped by divers. Right-click on dirt/grass to plant."
                ),
                1,
                false,
                true
        );
    }

    // ------------------------------------------------------------------
    // Sunflare Relic & Seed
    // ------------------------------------------------------------------

    /**
     * Mature Sunflare relic used in brewing the Potion of Solar Fury.
     */
    public static ItemStack getSunflare() {
        return createCustomItem(
                Material.BLAZE_POWDER,
                ChatColor.GOLD + "Sunflare",
                Arrays.asList(
                        ChatColor.GRAY + "A blazing relic radiating intense heat.",
                        ChatColor.BLUE + "Used in brewing the Potion of Solar Fury."
                ),
                1,
                false,
                true
        );
    }

    /**
     * Seed form dropped from fiery deaths, plant to grow Sunflare.
     */
    public static ItemStack getVerdantRelicSunflareSeed() {
        return createCustomItem(
                Material.WHEAT_SEEDS,
                ChatColor.GOLD + "Verdant Relic Sunflare",
                Arrays.asList(
                        ChatColor.GRAY + "A relic seed ignited with solar energy.",
                        ChatColor.BLUE + "Dropped from monsters slain by Fire Level.",
                        ChatColor.BLUE + "Right-click on dirt/grass to plant."
                ),
                1,
                false,
                true
        );
    }

    // ------------------------------------------------------------------
    // Starlight Relic & Seed
    // ------------------------------------------------------------------

    /**
     * Mature Starlight relic used in brewing the Potion of Night Vision.
     */
    public static ItemStack getStarlight() {
        return createCustomItem(
                Material.GHAST_TEAR,
                ChatColor.GOLD + "Starlight",
                Arrays.asList(
                        ChatColor.GRAY + "A radiant relic shimmering with cosmic energy.",
                        ChatColor.BLUE + "Used in brewing the Potion of Night Vision."
                ),
                1,
                false,
                true
        );
    }

    /**
     * Seed form dropped from invisible spiders, plant to grow Starlight.
     */
    public static ItemStack getVerdantRelicStarlightSeed() {
        return createCustomItem(
                Material.WHEAT_SEEDS,
                ChatColor.GOLD + "Verdant Relic Starlight",
                Arrays.asList(
                        ChatColor.GRAY + "A relic seed infused with celestial light.",
                        ChatColor.BLUE + "Dropped by invisible spiders.",
                        ChatColor.BLUE + "Right-click on dirt/grass to plant."
                ),
                1,
                false,
                true
        );
    }

    // ------------------------------------------------------------------
    // Tide Relic & Seed
    // ------------------------------------------------------------------

    /**
     * Mature Tide relic used in brewing the Potion of Riptide.
     */
    public static ItemStack getTide() {
        return createCustomItem(
                Material.HEART_OF_THE_SEA,
                ChatColor.GOLD + "Tide",
                Arrays.asList(
                        ChatColor.GRAY + "A relic pulsing with oceanic power.",
                        ChatColor.BLUE + "Used in brewing the Potion of Riptide."
                ),
                1,
                false,
                true
        );
    }

    /**
     * Seed form dropped from Poseidon, plant to grow Tide.
     */
    public static ItemStack getVerdantRelicTideSeed() {
        return createCustomItem(
                Material.WHEAT_SEEDS,
                ChatColor.GOLD + "Verdant Relic Tide",
                Arrays.asList(
                        ChatColor.GRAY + "A relic seed brimming with ocean energy.",
                        ChatColor.BLUE + "Dropped by Poseidon.",
                        ChatColor.BLUE + "Right-click on dirt/grass to plant."
                ),
                1,
                false,
                true
        );
    }

    // ------------------------------------------------------------------
    // Shiny Emerald Relic & Seed
    // ------------------------------------------------------------------

    /**
     * Mature Shiny Emerald relic used in brewing the Potion of Charismatic Bartering.
     */
    public static ItemStack getShinyEmerald() {
        return createCustomItem(
                Material.EMERALD,
                ChatColor.GOLD + "Shiny Emerald",
                Arrays.asList(
                        ChatColor.GRAY + "A gem shimmering with mercantile power.",
                        ChatColor.BLUE + "Used in brewing the Potion of Charismatic Bartering."
                ),
                1,
                false,
                true
        );
    }

    /**
     * Seed form obtained from mining ores, plant to grow a Shiny Emerald.
     */
    public static ItemStack getVerdantRelicShinyEmeraldSeed() {
        return createCustomItem(
                Material.WHEAT_SEEDS,
                ChatColor.GOLD + "Verdant Relic Shiny Emerald",
                Arrays.asList(
                        ChatColor.GRAY + "A relic seed radiating with emerald light.",
                        ChatColor.BLUE + "5% drop from Emerald Ore.",
                        ChatColor.BLUE + "1% drop from Diamond/Lapis/Redstone Ore."
                ),
                1,
                false,
                true
        );
    }

    // ------------------------------------------------------------------
    // Monster Relic & Seed
    // ------------------------------------------------------------------

    /**
     * Mature Monster relic used in brewing the Potion of Vitality.
     */
    public static ItemStack getMonster() {
        return createCustomItem(
                Material.ROTTEN_FLESH,
                ChatColor.GOLD + "Monster",
                Arrays.asList(
                        ChatColor.GRAY + "A relic pulsating with feral energy.",
                        ChatColor.BLUE + "Used in brewing the Potion of Vitality."
                ),
                1,
                false,
                true
        );
    }

    /**
     * Seed form dropped from powerful monsters, plant to grow Monster.
     */
    public static ItemStack getVerdantRelicMonsterSeed() {
        return createCustomItem(
                Material.WHEAT_SEEDS,
                ChatColor.GOLD + "Verdant Relic Monster",
                Arrays.asList(
                        ChatColor.GRAY + "A relic seed brimming with monstrous power.",
                        ChatColor.BLUE + "Dropped from high level monsters.",
    // Ghost Relic
    // ------------------------------------------------------------------

    public static ItemStack getGhost() {
        return createCustomItem(
                Material.GHAST_TEAR,
                ChatColor.GOLD + "Ghost",
                Arrays.asList(
                        ChatColor.GRAY + "A relic humming with spectral energy.",
                        ChatColor.BLUE + "Used in brewing the Potion of Oxygen Recovery."
                ),
                1,
                false,
                true
        );
    }


    public static ItemStack getRecurvePotionRecipePaper() {
        // This returns a piece of PAPER with a custom name + lore that says "Potion of Recurve Recipe".
        return createCustomItem(
                Material.PAPER,
                ChatColor.LIGHT_PURPLE + "Potion of Recurve Recipe (Potion Recipe)",
                Arrays.asList(
                        ChatColor.GRAY + "Brewing instructions for Recurve",
                        ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Right-click a Brewing Stand to begin",
                        ChatColor.DARK_PURPLE + "Potion Recipe"
                ),
                1,
                false,
                true
        );
    }
    public static ItemStack getSwiftStepPotionRecipePaper() {
        // This returns a piece of PAPER with a custom name + lore that says "Potion of Swift Step Recipe".
        return createCustomItem(
                Material.PAPER,
                ChatColor.LIGHT_PURPLE + "Potion of Swift Step Recipe (Potion Recipe)",
                Arrays.asList(
                        ChatColor.GRAY + "Brewing instructions for Swift Step",
                        ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Right-click a Brewing Stand to begin",
                        ChatColor.DARK_PURPLE + "Potion Recipe"
                ),
                1,
                false,
                true
        );
    }

    public static ItemStack getStrengthPotionRecipePaper() {
        // This returns a piece of PAPER with a custom name + lore that says "Potion of Strength Recipe".
        return createCustomItem(
                Material.PAPER,
                ChatColor.LIGHT_PURPLE + "Potion of Strength Recipe (Potion Recipe)",
                Arrays.asList(
                        ChatColor.GRAY + "Brewing instructions for Strength",
                        ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Right-click a Brewing Stand to begin",
                        ChatColor.DARK_PURPLE + "Potion Recipe"
                ),
                1,
                false,
                true
        );
    }
    public static ItemStack getSovereigntyPotionRecipePaper() {
        return createCustomItem(
                Material.PAPER,
                ChatColor.LIGHT_PURPLE + "Potion of Sovereignty Recipe (Potion Recipe)",
                Arrays.asList(
                        ChatColor.GRAY + "Brewing instructions for Sovereignty",
                        ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Right-click a Brewing Stand to begin",
                        ChatColor.DARK_PURPLE + "Potion Recipe"
                ),
                1,
                false,
                true
        );
    }
    public static ItemStack getLiquidLuckRecipePaper() {
        return createCustomItem(
                Material.PAPER,
                ChatColor.LIGHT_PURPLE + "Potion of Liquid Luck Recipe (Potion Recipe)",
                Arrays.asList(
                        ChatColor.GRAY + "Brewing instructions for Liquid Luck",
                        ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Right-click a Brewing Stand to begin",
                        ChatColor.DARK_PURPLE + "Potion Recipe"
                ),
                1,
                false,
                true
        );
    }
    public static ItemStack getFountainsRecipePaper() {
        // This returns a piece of PAPER with a custom name + lore that says "Potion of Strength Recipe".
        return createCustomItem(
                Material.PAPER,
                ChatColor.LIGHT_PURPLE + "Potion of Fountains Recipe (Potion Recipe)",
                Arrays.asList(
                        ChatColor.GRAY + "Brewing instructions for Sea Creature Chance",
                        ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Right-click a Brewing Stand to begin",
                        ChatColor.DARK_PURPLE + "Potion Recipe"
                ),
                1,
                false,
                true
        );
    }

    public static ItemStack getVitalityRecipePaper() {
        return createCustomItem(
                Material.PAPER,
                ChatColor.LIGHT_PURPLE + "Potion of Vitality Recipe (Potion Recipe)",
                Arrays.asList(
                        ChatColor.GRAY + "Brewing instructions for Vitality",
                        ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Right-click a Brewing Stand to begin",
                        ChatColor.DARK_PURPLE + "Potion Recipe"
                ),
                1,
                false,
                true
        );
    }

    public static ItemStack getRiptideRecipePaper() {
        return createCustomItem(
                Material.PAPER,
                ChatColor.LIGHT_PURPLE + "Potion of Riptide Recipe (Potion Recipe)",
                Arrays.asList(
                        ChatColor.GRAY + "Brewing instructions for Riptide",
                        ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Right-click a Brewing Stand to begin",
                        ChatColor.DARK_PURPLE + "Potion Recipe"
                ),
                1,
                false,
                true
        );
    }

    public static ItemStack getSolarFuryRecipePaper() {
        return createCustomItem(
                Material.PAPER,
                ChatColor.LIGHT_PURPLE + "Potion of Solar Fury Recipe (Potion Recipe)",
                Arrays.asList(
                        ChatColor.GRAY + "Brewing instructions for Solar Fury",
                        ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Right-click a Brewing Stand to begin",
                        ChatColor.DARK_PURPLE + "Potion Recipe"
                ),
                1,
                false,
                true
        );
    }

    public static ItemStack getNightVisionRecipePaper() {
        return createCustomItem(
                Material.PAPER,
                ChatColor.LIGHT_PURPLE + "Potion of Night Vision Recipe (Potion Recipe)",
                Arrays.asList(
                        ChatColor.GRAY + "Brewing instructions for Night Vision",
                        ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Right-click a Brewing Stand to begin",
                        ChatColor.DARK_PURPLE + "Potion Recipe"
                ),
                1,
                false,
                true
        );
    }

    public static ItemStack getCharismaticBarteringRecipePaper() {
        return createCustomItem(
                Material.PAPER,
                ChatColor.LIGHT_PURPLE + "Potion of Charismatic Bartering Recipe (Potion Recipe)",
                Arrays.asList(
                        ChatColor.GRAY + "Brewing instructions for Charismatic Bartering",
                        ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Right-click a Brewing Stand to begin",
                        ChatColor.DARK_PURPLE + "Potion Recipe"
                ),
                1,
                false,
                true
        );
    }

    public static ItemStack getOxygenRecoveryRecipePaper() {
        return createCustomItem(
                Material.PAPER,
                ChatColor.LIGHT_PURPLE + "Potion of Oxygen Recovery Recipe (Potion Recipe)",
                Arrays.asList(
                        ChatColor.GRAY + "Brewing instructions for Oxygen Recovery",
                        ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Right-click a Brewing Stand to begin",
                        ChatColor.DARK_PURPLE + "Potion Recipe"
                ),
                1,
                false,
                true
        );
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

    public static ItemStack getWaterAspectEnchant() {
        return createCustomItem(Material.PRISMARINE_CRYSTALS, ChatColor.YELLOW +
                "Water Aspect", Arrays.asList(
                ChatColor.GRAY + "Max level of IV",
                ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Adds 1 Level of Water Aspect to items.",
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
    public static ItemStack getQualifiedItem() {
        return createCustomItem(Material.DIAMOND, ChatColor.YELLOW +
                "Qualification", Arrays.asList(
                ChatColor.GRAY + "Max level of 5",
                ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Adds 1 Level of Merit to Pickaxes.",
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

    // === Trinkets ===
    public static ItemStack getWorkbenchTrinket() {
        return createCustomItem(
                Material.CRAFTING_TABLE,
                ChatColor.YELLOW + "Workbench Trinket",
                List.of(
                        ChatColor.BLUE + "Left-click" + ChatColor.GRAY + ": Open Crafting",
                        ChatColor.BLUE + "Right-click" + ChatColor.GRAY + ": Pick up"
                ),
                1,
                false,
                true
        );
    }

    public static ItemStack getDividerTrinket() {
        return createCustomItem(
                Material.GRAY_STAINED_GLASS_PANE,
                ChatColor.YELLOW + "Divider Trinket",
                List.of(
                        ChatColor.GRAY + "Use to separate backpack slots",
                        ChatColor.BLUE + "Right-click" + ChatColor.GRAY + ": Pick up"
                ),
                1,
                false,
                false
        );
    }

    public static ItemStack getBankAccountTrinket() {
        return createCustomItem(
                Material.GOLD_BLOCK,
                ChatColor.YELLOW + "Bank Account",
                List.of(
                        ChatColor.GRAY + "Stores emeralds safely",
                        ChatColor.BLUE + "Left-click" + ChatColor.GRAY + ": Deposit all",
                        ChatColor.BLUE + "Shift-Right-click" + ChatColor.GRAY + ": Withdraw all"
                ),
                1,
                false,
                true
        );
    }

    public static ItemStack getBlueSatchelTrinket() {
        return createCustomItem(
                Material.BLUE_WOOL,
                ChatColor.YELLOW + "Blue Satchel",
                List.of(
                        ChatColor.BLUE + "Left-click" + ChatColor.GRAY + ": Open",
                        ChatColor.BLUE + "Right-click" + ChatColor.GRAY + ": Pick up"
                ),
                1,
                false,
                true
        );
    }

    public static ItemStack getBlackSatchelTrinket() {
        return createCustomItem(
                Material.BLACK_WOOL,
                ChatColor.YELLOW + "Black Satchel",
                List.of(
                        ChatColor.BLUE + "Left-click" + ChatColor.GRAY + ": Open",
                        ChatColor.BLUE + "Right-click" + ChatColor.GRAY + ": Pick up"
                ),
                1,
                false,
                true
        );
    }

    public static ItemStack getGreenSatchelTrinket() {
        return createCustomItem(
                Material.GREEN_WOOL,
                ChatColor.YELLOW + "Green Satchel",
                List.of(
                        ChatColor.BLUE + "Left-click" + ChatColor.GRAY + ": Open",
                        ChatColor.BLUE + "Right-click" + ChatColor.GRAY + ": Pick up"
                ),
                1,
                false,
                true
        );
    }

    public static ItemStack getAnvilTrinket() {
        return createCustomItem(
                Material.ANVIL,
                ChatColor.YELLOW + "Anvil Trinket",
                List.of(
                        ChatColor.BLUE + "Left-click" + ChatColor.GRAY + ": Open Anvil",
                        ChatColor.BLUE + "Right-click" + ChatColor.GRAY + ": Pick up"
                ),
                1,
                false,
                true
        );
    }

    public static ItemStack getSeedPouchTrinket() {
        return createCustomItem(
                Material.GUNPOWDER,
                ChatColor.YELLOW + "Pouch of Seeds",
                List.of(
                        ChatColor.BLUE + "Left-click" + ChatColor.GRAY + ": Store seeds",
                        ChatColor.BLUE + "Shift-Right-click" + ChatColor.GRAY + ": Open pouch"
                ),
                1,
                false,
                true
        );
    }

    public static ItemStack getPotionPouchTrinket() {
        return createCustomItem(
                Material.BUNDLE,
                ChatColor.YELLOW + "Pouch of Potions",
                List.of(
                        ChatColor.BLUE + "Left-click" + ChatColor.GRAY + ": Store potions",
                        ChatColor.BLUE + "Shift-Right-click" + ChatColor.GRAY + ": Open pouch"
                ),
                1,
                false,
                true
        );
    }

    public static ItemStack getCulinaryPouchTrinket() {
        return createCustomItem(
                Material.BUNDLE,
                ChatColor.YELLOW + "Pouch of Culinary Delights",
                List.of(
                        ChatColor.BLUE + "Left-click" + ChatColor.GRAY + ": Store delights",
                        ChatColor.BLUE + "Shift-Right-click" + ChatColor.GRAY + ": Open pouch"
                ),
                1,
                false,
                true
        );
    }

    public static ItemStack getMiningPouchTrinket() {
        return createCustomItem(
                Material.IRON_PICKAXE,
                ChatColor.YELLOW + "Mining Pouch",
                List.of(
                        ChatColor.BLUE + "Left-click" + ChatColor.GRAY + ": Store ores",
    public static ItemStack getSeaCreaturePouchTrinket() {
        return createCustomItem(
                Material.BUNDLE,
                ChatColor.YELLOW + "Pouch of Sea Creatures",
                List.of(
                        ChatColor.BLUE + "Left-click" + ChatColor.GRAY + ": Store drops",
                        ChatColor.BLUE + "Shift-Right-click" + ChatColor.GRAY + ": Open pouch"
                ),
                1,
                false,
                true
        );
    }

    public static ItemStack getEnchantedLavaBucketTrinket() {
        return createCustomItem(
                Material.LAVA_BUCKET,
                ChatColor.YELLOW + "Enchanted Lava Bucket",
                List.of(
                        ChatColor.BLUE + "Left-click" + ChatColor.GRAY + ": Trash cursor item",
                        ChatColor.BLUE + "Shift-Right-click" + ChatColor.GRAY + ": Open trash"
                ),
                1,
                false,
                true
        );
    }

    public static ItemStack getTransfigurationPouchTrinket() {
        return createCustomItem(
                Material.GLOWSTONE,
                ChatColor.YELLOW + "Transfiguration Pouch",
                List.of(
                        ChatColor.BLUE + "Left-click" + ChatColor.GRAY + ": Store items",
                        ChatColor.BLUE + "Shift-Left-click" + ChatColor.GRAY + ": Convert to XP",
                        ChatColor.BLUE + "Shift-Right-click" + ChatColor.GRAY + ": Open pouch"
                ),
                1,
                false,
                true
        );
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

    public static ItemStack getCursedClock() {
        return createCustomItem(
                Material.CLOCK,
                ChatColor.YELLOW + "Cursed Clock",
                Arrays.asList(
                        ChatColor.GRAY + "A clock that ticks with decay.",
                        ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Applies the Accelerate enchantment.",
                        ChatColor.DARK_PURPLE + "Smithing Item"
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

    /**
     * Basic bait used as an ingredient for higher tiers.
     */
    public static ItemStack getCommonBait() {
        return createCustomItem(
                Material.WHEAT_SEEDS,
                ChatColor.YELLOW + "Common Bait",
                Arrays.asList(
                        ChatColor.GRAY + "Simple feed that attracts small fish.",
                        ChatColor.BLUE + "Energy: " + ChatColor.WHITE + "+1 Angler Energy",
                        ChatColor.GREEN + "Bait"
                ),
                1,
                false,
                true
        );
    }

    public static ItemStack getShrimpBait() {
        return createCustomItem(
                Material.SALMON,
                ChatColor.YELLOW + "Shrimp Bait",
                Arrays.asList(
                        ChatColor.GRAY + "A tasty treat prized by anglers.",
                        ChatColor.BLUE + "Energy: " + ChatColor.WHITE + "+3 Angler Energy",
                        ChatColor.GREEN + "Bait"
                ),
                1,
                false,
                true
        );
    }

    public static ItemStack getLeechBait() {
        return createCustomItem(
                Material.SPIDER_EYE,
                ChatColor.YELLOW + "Leech Bait",
                Arrays.asList(
                        ChatColor.GRAY + "Hard to find and favored by big fish.",
                        ChatColor.BLUE + "Energy: " + ChatColor.WHITE + "+7 Angler Energy",
                        ChatColor.GREEN + "Bait"
                ),
                1,
                false,
                true
        );
    }

    public static ItemStack getFrogBait() {
        return createCustomItem(
                Material.SLIME_BALL,
                ChatColor.YELLOW + "Frog Bait",
                Arrays.asList(
                        ChatColor.GRAY + "Rare bait coveted by expert anglers.",
                        ChatColor.BLUE + "Energy: " + ChatColor.WHITE + "+10 Angler Energy",
                        ChatColor.GREEN + "Bait"
                ),
                1,
                false,
                true
        );
    }

    public static ItemStack getCaviarBait() {
        return createCustomItem(
                Material.EGG,
                ChatColor.YELLOW + "Caviar Bait",
                Arrays.asList(
                        ChatColor.GRAY + "Exquisite and exceedingly expensive.",
                        ChatColor.BLUE + "Energy: " + ChatColor.WHITE + "+20 Angler Energy",
                        ChatColor.GREEN + "Bait"
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
                        ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Summons a hired Villager.",
                        ChatColor.DARK_PURPLE + "Summoning Item"),
                1,
                false // Set to true if you want it to be unbreakable
                , true
        );
    }
    public static ItemStack getHireBartender() {
        return createCustomItem(
                Material.GOLDEN_HOE,
                ChatColor.YELLOW + "Hire Bartender",
                List.of(ChatColor.GRAY + "A definitely useful companion.",
                        ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Summons a Bartender.",
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

    public static ItemStack getGeneratorItem() {
        return createCustomItem(
                Material.SCULK_SHRIEKER,
                ChatColor.YELLOW + "Generator",
                Arrays.asList(
                        ChatColor.BLUE + "Right-click" + ChatColor.GRAY + ": Place",
                        ChatColor.BLUE + "Shift-Right-click" + ChatColor.GRAY + ": Pick up"
                ),
                1,
                false,
                true
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

    public static ItemStack getJackhammer() {
        return createCustomItem(
                Material.IRON_NUGGET,
                ChatColor.LIGHT_PURPLE + "Jackhammer",
                Arrays.asList(
                        ChatColor.GRAY + "A powerful mining device.",
                        ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Break an entire vein instantly.",
                        ChatColor.DARK_PURPLE + "Artifact"
                ),
                1,
                false,
                true
        );
    }

    public static ItemStack getUnbreakableShears() {
        ItemStack item = createCustomItem(
                Material.SHEARS,
                ChatColor.LIGHT_PURPLE + "Unbreakable Shears",
                Arrays.asList(
                        ChatColor.GRAY + "Shears that never dull.",
                        ChatColor.DARK_PURPLE + "Artifact"
                ),
                1,
                true,
                true
        );
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setUnbreakable(true);
            meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
            item.setItemMeta(meta);
        }
        return item;
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
                ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Grants 16 random non-feast Culinary Recipes.",
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

    // ===== GEMSTONES =====
    
    // COMMON GEMSTONES (+1 Gemstone Power)
    public static ItemStack getQuartz() {
        return createCustomItem(
                Material.QUARTZ,
                ChatColor.WHITE + "Quartz",
                Arrays.asList(
                        ChatColor.GRAY + "A translucent crystal commonly found in nature.",
                        ChatColor.BLUE + "Power: " + ChatColor.WHITE + "+1 Gemstone Power",
                        "",
                        ChatColor.YELLOW + "Click onto a diamond tool in your inventory",
                        ChatColor.YELLOW + "to apply its Gemstone Power to the tool.",
                        ChatColor.GREEN + "Gemstone"
                ),
                1,
                false,
                false
        );
    }

    public static ItemStack getHematite() {
        return createCustomItem(
                Material.IRON_NUGGET,
                ChatColor.GRAY + "Hematite",
                Arrays.asList(
                        ChatColor.GRAY + "A metallic, iron-rich mineral with a dark luster.",
                        ChatColor.BLUE + "Power: " + ChatColor.WHITE + "+1 Gemstone Power",
                        "",
                        ChatColor.YELLOW + "Click onto a diamond tool in your inventory",
                        ChatColor.YELLOW + "to apply its Gemstone Power to the tool.",
                        ChatColor.GREEN + "Gemstone"
                ),
                1,
                false,
                false
        );
    }

    public static ItemStack getObsidian() {
        return createCustomItem(
                Material.OBSIDIAN,
                ChatColor.DARK_GRAY + "Obsidian",
                Arrays.asList(
                        ChatColor.GRAY + "Volcanic glass formed from rapidly cooling lava.",
                        ChatColor.BLUE + "Power: " + ChatColor.WHITE + "+1 Gemstone Power",
                        "",
                        ChatColor.YELLOW + "Click onto a diamond tool in your inventory",
                        ChatColor.YELLOW + "to apply its Gemstone Power to the tool.",
                        ChatColor.GREEN + "Gemstone"
                ),
                1,
                false,
                false
        );
    }

    public static ItemStack getAgate() {
        return createCustomItem(
                Material.BROWN_DYE,
                ChatColor.YELLOW + "Agate",
                Arrays.asList(
                        ChatColor.GRAY + "A banded variety of chalcedony with earthy tones.",
                        ChatColor.BLUE + "Power: " + ChatColor.WHITE + "+1 Gemstone Power",
                        "",
                        ChatColor.YELLOW + "Click onto a diamond tool in your inventory",
                        ChatColor.YELLOW + "to apply its Gemstone Power to the tool.",
                        ChatColor.GREEN + "Gemstone"
                ),
                1,
                false,
                false
        );
    }

    // UNCOMMON GEMSTONES (+3 Gemstone Power)
    public static ItemStack getTurquoise() {
        return createCustomItem(
                Material.CYAN_DYE,
                ChatColor.DARK_AQUA + "Turquoise",
                Arrays.asList(
                        ChatColor.GRAY + "A blue-green mineral prized for its vibrant color.",
                        ChatColor.BLUE + "Power: " + ChatColor.AQUA + "+3 Gemstone Power",
                        "",
                        ChatColor.YELLOW + "Click onto a diamond tool in your inventory",
                        ChatColor.YELLOW + "to apply its Gemstone Power to the tool.",
                        ChatColor.GREEN + "Gemstone"
                ),
                1,
                false,
                false
        );
    }

    public static ItemStack getAmethyst() {
        return createCustomItem(
                Material.AMETHYST_SHARD,
                ChatColor.LIGHT_PURPLE + "Amethyst",
                Arrays.asList(
                        ChatColor.GRAY + "A purple variety of quartz known for its beauty.",
                        ChatColor.BLUE + "Power: " + ChatColor.AQUA + "+3 Gemstone Power",
                        "",
                        ChatColor.YELLOW + "Click onto a diamond tool in your inventory",
                        ChatColor.YELLOW + "to apply its Gemstone Power to the tool.",
                        ChatColor.GREEN + "Gemstone"
                ),
                1,
                false,
                false
        );
    }

    public static ItemStack getCitrine() {
        return createCustomItem(
                Material.YELLOW_DYE,
                ChatColor.GOLD + "Citrine",
                Arrays.asList(
                        ChatColor.GRAY + "A golden yellow variety of quartz that gleams warmly.",
                        ChatColor.BLUE + "Power: " + ChatColor.AQUA + "+3 Gemstone Power",
                        "",
                        ChatColor.YELLOW + "Click onto a diamond tool in your inventory",
                        ChatColor.YELLOW + "to apply its Gemstone Power to the tool.",
                        ChatColor.GREEN + "Gemstone"
                ),
                1,
                false,
                false
        );
    }

    public static ItemStack getGarnet() {
        return createCustomItem(
                Material.RED_DYE,
                ChatColor.DARK_RED + "Garnet",
                Arrays.asList(
                        ChatColor.GRAY + "A deep red gemstone symbolizing passion and energy.",
                        ChatColor.BLUE + "Power: " + ChatColor.AQUA + "+3 Gemstone Power",
                        "",
                        ChatColor.YELLOW + "Click onto a diamond tool in your inventory",
                        ChatColor.YELLOW + "to apply its Gemstone Power to the tool.",
                        ChatColor.GREEN + "Gemstone"
                ),
                1,
                false,
                false
        );
    }

    // RARE GEMSTONES (+7 Gemstone Power)
    public static ItemStack getTopaz() {
        return createCustomItem(
                Material.ORANGE_DYE,
                ChatColor.GOLD + "Topaz",
                Arrays.asList(
                        ChatColor.GRAY + "A brilliant orange gemstone with exceptional clarity.",
                        ChatColor.BLUE + "Power: " + ChatColor.YELLOW + "+7 Gemstone Power",
                        "",
                        ChatColor.YELLOW + "Click onto a diamond tool in your inventory",
                        ChatColor.YELLOW + "to apply its Gemstone Power to the tool.",
                        ChatColor.GREEN + "Gemstone"
                ),
                1,
                false,
                true
        );
    }

    public static ItemStack getPeridot() {
        return createCustomItem(
                Material.LIME_DYE,
                ChatColor.GREEN + "Peridot",
                Arrays.asList(
                        ChatColor.GRAY + "An olive-green gemstone formed in volcanic rock.",
                        ChatColor.BLUE + "Power: " + ChatColor.YELLOW + "+7 Gemstone Power",
                        "",
                        ChatColor.YELLOW + "Click onto a diamond tool in your inventory",
                        ChatColor.YELLOW + "to apply its Gemstone Power to the tool.",
                        ChatColor.GREEN + "Gemstone"
                ),
                1,
                false,
                true
        );
    }

    public static ItemStack getAquamarine() {
        return createCustomItem(
                Material.LIGHT_BLUE_DYE,
                ChatColor.AQUA + "Aquamarine",
                Arrays.asList(
                        ChatColor.GRAY + "A pale blue beryl reminiscent of clear ocean waters.",
                        ChatColor.BLUE + "Power: " + ChatColor.YELLOW + "+7 Gemstone Power",
                        "",
                        ChatColor.YELLOW + "Click onto a diamond tool in your inventory",
                        ChatColor.YELLOW + "to apply its Gemstone Power to the tool.",
                        ChatColor.GREEN + "Gemstone"
                ),
                1,
                false,
                true
        );
    }

    public static ItemStack getTanzanite() {
        return createCustomItem(
                Material.PURPLE_DYE,
                ChatColor.BLUE + "Tanzanite",
                Arrays.asList(
                        ChatColor.GRAY + "A rare blue-violet gemstone found only in Tanzania.",
                        ChatColor.BLUE + "Power: " + ChatColor.YELLOW + "+7 Gemstone Power",
                        "",
                        ChatColor.YELLOW + "Click onto a diamond tool in your inventory",
                        ChatColor.YELLOW + "to apply its Gemstone Power to the tool.",
                        ChatColor.GREEN + "Gemstone"
                ),
                1,
                false,
                true
        );
    }

    // EPIC GEMSTONES (+10 Gemstone Power)
    public static ItemStack getSapphire() {
        return createCustomItem(
                Material.LAPIS_LAZULI,
                ChatColor.DARK_BLUE + "Sapphire",
                Arrays.asList(
                        ChatColor.GRAY + "A precious blue corundum of extraordinary hardness.",
                        ChatColor.BLUE + "Power: " + ChatColor.LIGHT_PURPLE + "+10 Gemstone Power",
                        "",
                        ChatColor.YELLOW + "Click onto a diamond tool in your inventory",
                        ChatColor.YELLOW + "to apply its Gemstone Power to the tool.",
                        ChatColor.GREEN + "Gemstone"
                ),
                1,
                false,
                true
        );
    }

    public static ItemStack getRuby() {
        return createCustomItem(
                Material.REDSTONE,
                ChatColor.RED + "Ruby",
                Arrays.asList(
                        ChatColor.GRAY + "A precious red corundum that burns like fire.",
                        ChatColor.BLUE + "Power: " + ChatColor.LIGHT_PURPLE + "+10 Gemstone Power",
                        "",
                        ChatColor.YELLOW + "Click onto a diamond tool in your inventory",
                        ChatColor.YELLOW + "to apply its Gemstone Power to the tool.",
                        ChatColor.GREEN + "Gemstone"
                ),
                1,
                false,
                true
        );
    }

    // LEGENDARY GEMSTONES (+20 Gemstone Power)
    public static ItemStack getEmerald() {
        return createCustomItem(
                Material.EMERALD,
                ChatColor.DARK_GREEN + "Emerald",
                Arrays.asList(
                        ChatColor.GRAY + "The most precious green beryl, rarer than diamonds.",
                        ChatColor.BLUE + "Power: " + ChatColor.GOLD + "+20 Gemstone Power",
                        "",
                        ChatColor.YELLOW + "Click onto a diamond tool in your inventory",
                        ChatColor.YELLOW + "to apply its Gemstone Power to the tool.",
                        ChatColor.GREEN + "Gemstone"
                ),
                1,
                false,
                true
        );
    }

    public static ItemStack getDiamond() {
        return createCustomItem(
                Material.DIAMOND,
                ChatColor.AQUA + "Diamond",
                Arrays.asList(
                        ChatColor.GRAY + "The hardest natural substance, brilliant and eternal.",
                        ChatColor.BLUE + "Power: " + ChatColor.GOLD + "+20 Gemstone Power",
                        "",
                        ChatColor.YELLOW + "Click onto a diamond tool in your inventory",
                        ChatColor.YELLOW + "to apply its Gemstone Power to the tool.",
                        ChatColor.GREEN + "Gemstone"
                ),
                1,
                false,
                true
        );
    }
    
    public static ItemStack getPowerCrystal() {
        return createCustomItem(
                Material.PRISMARINE_CRYSTALS,
                ChatColor.LIGHT_PURPLE + "Power Crystal",
                Arrays.asList(
                        ChatColor.GRAY + "A rare crystal that expands the",
                        ChatColor.GRAY + "gemstone power capacity of tools",
                        "",
                        ChatColor.YELLOW + "Effect: " + ChatColor.WHITE + "+100% Power Cap",
                        ChatColor.YELLOW + "Maximum: " + ChatColor.WHITE + "500% Total Cap",
                        "",
                        ChatColor.DARK_PURPLE + "Drag onto diamond tools to apply"
                ),
                1,
                false,
                true
        );
    }

    public static ItemStack getPearlOfTheDeep() {
        return createCustomItem(
                Material.ENDER_PEARL,
                ChatColor.AQUA + "Pearl of the Deep",
                Arrays.asList(
                        ChatColor.GRAY + "A mystical pearl that expands the",
                        ChatColor.GRAY + "angler energy capacity of fishing rods",
                        "",
                        ChatColor.YELLOW + "Effect: " + ChatColor.WHITE + "+100% Power Cap",
                        ChatColor.YELLOW + "Maximum: " + ChatColor.WHITE + "500% Total Cap",
                        "",
                        ChatColor.DARK_PURPLE + "Drag onto fishing rods to apply"
                ),
                1,
                false,
                true
        );
    }

    /**
     * Creates a Redstone Gem with the specified power. Each gem is given a
     * unique identifier so that individual gems do not stack together. The
     * initial power value is stored in the item's persistent data container for
     * later retrieval.
     *
     * @param power The power level of the gem.
     * @return A customized ItemStack representing the Redstone Gem.
     */
    public static ItemStack getRedstoneGem(int power) {
        ItemStack gem = new ItemStack(Material.REDSTONE, 1);
        ItemMeta meta = gem.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.RED + "Redstone Gem: " + power + " Power");

            PersistentDataContainer container = meta.getPersistentDataContainer();
            NamespacedKey idKey = new NamespacedKey(MinecraftNew.getInstance(), "gem_id");
            NamespacedKey powerKey = new NamespacedKey(MinecraftNew.getInstance(), "power");
            container.set(idKey, PersistentDataType.STRING, UUID.randomUUID().toString());
            container.set(powerKey, PersistentDataType.INTEGER, power);

            gem.setItemMeta(meta);
        }
        return gem;
    }

    // ===== FORESTRY ITEMS =====

    // COMMON EFFIGIES (+1 Spirit Energy)
    public static ItemStack getOakEffigy() {
        return createCustomItem(
                Material.OAK_WOOD,
                ChatColor.YELLOW + "Oak Effigy",
                Arrays.asList(
                        ChatColor.GRAY + "Before the forests fell, a shape crawled.",
                        ChatColor.GRAY + "Untouchable. Unkillable. Unseen by light.",
                        ChatColor.BLUE + "Energy: " + ChatColor.WHITE + "+1 Spirit Energy",
                        "",
                        ChatColor.YELLOW + "Click onto a diamond or netherite axe",
                        ChatColor.YELLOW + "to imbue it with Spirit Energy.",
                        ChatColor.GREEN + "Effigy"
                ),
                1, false, false
        );
    }

    public static ItemStack getBirchEffigy() {
        return createCustomItem(
                Material.BIRCH_WOOD,
                ChatColor.YELLOW + "Birch Effigy",
                Arrays.asList(
                        ChatColor.GRAY + "It crawled, not walked —",
                        ChatColor.GRAY + "and clawed at roots it could not name.",
                        ChatColor.BLUE + "Energy: " + ChatColor.WHITE + "+1 Spirit Energy",
                        "",
                        ChatColor.YELLOW + "Click onto a diamond or netherite axe",
                        ChatColor.YELLOW + "to imbue it with Spirit Energy.",
                        ChatColor.GREEN + "Effigy"
                ),
                1, false, false
        );
    }


    public static ItemStack getSpruceEffigy() {
        return createCustomItem(
                Material.SPRUCE_WOOD,
                ChatColor.AQUA + "Spruce Effigy",
                Arrays.asList(
                        ChatColor.GRAY + "Forests died screaming. Caskets shattered.",
                        ChatColor.GRAY + "The dead stirred, but not to rise.",
                        ChatColor.BLUE + "Energy: " + ChatColor.WHITE + "+3 Spirit Energy",
                        "",
                        ChatColor.YELLOW + "Click onto a diamond or netherite axe",
                        ChatColor.YELLOW + "to imbue it with Spirit Energy.",
                        ChatColor.GREEN + "Effigy"
                ),
                1, false, false
        );
    }


    public static ItemStack getAcaciaEffigy() {
        return createCustomItem(
                Material.ACACIA_WOOD,
                ChatColor.LIGHT_PURPLE + "Acacia Effigy",
                Arrays.asList(
                        ChatColor.GRAY + "Spirits wept, then rose in fury.",
                        ChatColor.GRAY + "Bound by oath, they formed the Pact of Vigil.",
                        ChatColor.BLUE + "Energy: " + ChatColor.WHITE + "+7 Spirit Energy",
                        "",
                        ChatColor.YELLOW + "Click onto a diamond or netherite axe",
                        ChatColor.YELLOW + "to imbue it with Spirit Energy.",
                        ChatColor.GREEN + "Effigy"
                ),
                1, false, false
        );
    }


    public static ItemStack getDarkOakEffigy() {
        return createCustomItem(
                Material.DARK_OAK_WOOD,
                ChatColor.DARK_PURPLE + "Dark Oak Effigy",
                Arrays.asList(
                        ChatColor.GRAY + "In a winter without end, the Pact struck.",
                        ChatColor.GRAY + "It was not slain, only cast away.",
                        ChatColor.BLUE + "Energy: " + ChatColor.WHITE + "+10 Spirit Energy",
                        "",
                        ChatColor.YELLOW + "Click onto a diamond or netherite axe",
                        ChatColor.YELLOW + "to imbue it with Spirit Energy.",
                        ChatColor.GREEN + "Effigy"
                ),
                1, false, false
        );
    }


    public static ItemStack getCrimsonEffigy() {
        return createCustomItem(
                Material.CRIMSON_HYPHAE,
                ChatColor.GOLD + "Crimson Effigy",
                Arrays.asList(
                        ChatColor.GRAY + "So the Insanity was sealed away,",
                        ChatColor.GRAY + "its name carved only into legend: " + ChatColor.DARK_RED + "Grievance.",
                        ChatColor.GRAY + "Still, the Pact holds vigil, awaiting its return.",
                        ChatColor.BLUE + "Energy: " + ChatColor.WHITE + "+20 Spirit Energy",
                        "",
                        ChatColor.YELLOW + "Click onto a diamond or netherite axe",
                        ChatColor.YELLOW + "to imbue it with Spirit Energy.",
                        ChatColor.GREEN + "Effigy"
                ),
                1, false, false
        );
    }

    public static ItemStack getWarpedEffigy() {
        return createCustomItem(
                Material.WARPED_HYPHAE,
                ChatColor.GOLD + "Warped Effigy",
                Arrays.asList(
                        ChatColor.GRAY + "So the Insanity was sealed away,",
                        ChatColor.GRAY + "its name carved only into legend: " + ChatColor.DARK_RED + "Grievance.",
                        ChatColor.GRAY + "Still, the Pact holds vigil, awaiting its return.",
                        ChatColor.BLUE + "Energy: " + ChatColor.WHITE + "+20 Spirit Energy",
                        "",
                        ChatColor.YELLOW + "Click onto a diamond or netherite axe",
                        ChatColor.YELLOW + "to imbue it with Spirit Energy.",
                        ChatColor.GREEN + "Effigy"
                ),
                1, false, false
        );
    }


    public static ItemStack getEntBark() {
        return createCustomItem(
                Material.STRIPPED_OAK_WOOD,
                ChatColor.LIGHT_PURPLE + "Ent Bark",
                Arrays.asList(
                        ChatColor.GRAY + "A mystical bark that expands Spirit Energy capacity.",
                        "",
                        ChatColor.YELLOW + "Effect: " + ChatColor.WHITE + "+100% Spirit Cap",
                        ChatColor.YELLOW + "Maximum: " + ChatColor.WHITE + "500% Total Cap",
                        "",
                        ChatColor.DARK_PURPLE + "Drag onto diamond or netherite axes to apply"
                ),
                1,
                false,
                true
        );
    }

    // ===== COMBAT SOUL ITEMS =====

    // COMMON (+1 Soul Power)
    public static ItemStack getSoulshard() {
        return createCustomItem(
                Material.QUARTZ,
                ChatColor.GRAY + "Soulshard",
                Arrays.asList(
                        ChatColor.BLUE + "Power: " + ChatColor.WHITE + "+1 Soul Power",
                        "",
                        ChatColor.YELLOW + "Click onto a sword",
                        ChatColor.YELLOW + "to infuse its Soul Power.",
                        ChatColor.GREEN + "Soul Item"
                ),
                1,
                false,
                false
        );
    }

    // UNCOMMON (+3 Soul Power)
    public static ItemStack getWisp() {
        return createCustomItem(
                Material.PHANTOM_MEMBRANE,
                ChatColor.AQUA + "Wisp",
                Arrays.asList(
                        ChatColor.BLUE + "Power: " + ChatColor.AQUA + "+3 Soul Power",
                        "",
                        ChatColor.YELLOW + "Click onto a sword",
                        ChatColor.YELLOW + "to infuse its Soul Power.",
                        ChatColor.GREEN + "Soul Item"
                ),
                1,
                false,
                false
        );
    }

    // RARE (+7 Soul Power)
    public static ItemStack getWraith() {
        return createCustomItem(
                Material.GHAST_TEAR,
                ChatColor.BLUE + "Wraith",
                Arrays.asList(
                        ChatColor.BLUE + "Power: " + ChatColor.YELLOW + "+7 Soul Power",
                        "",
                        ChatColor.YELLOW + "Click onto a sword",
                        ChatColor.YELLOW + "to infuse its Soul Power.",
                        ChatColor.GREEN + "Soul Item"
                ),
                1,
                false,
                true
        );
    }

    // EPIC (+10 Soul Power)
    public static ItemStack getRemnant() {
        return createCustomItem(
                Material.ECHO_SHARD,
                ChatColor.DARK_PURPLE + "Remnant",
                Arrays.asList(
                        ChatColor.BLUE + "Power: " + ChatColor.LIGHT_PURPLE + "+10 Soul Power",
                        "",
                        ChatColor.YELLOW + "Click onto a sword",
                        ChatColor.YELLOW + "to infuse its Soul Power.",
                        ChatColor.GREEN + "Soul Item"
                ),
                1,
                false,
                true
        );
    }

    // LEGENDARY (+20 Soul Power)
    public static ItemStack getShade() {
        return createCustomItem(
                Material.SOUL_LANTERN,
                ChatColor.GOLD + "Shade",
                Arrays.asList(
                        ChatColor.BLUE + "Power: " + ChatColor.GOLD + "+20 Soul Power",
                        "",
                        ChatColor.YELLOW + "Click onto a sword",
                        ChatColor.YELLOW + "to infuse its Soul Power.",
                        ChatColor.GREEN + "Soul Item"
                ),
                1,
                false,
                true
        );
    }

    // POWER CAP ITEM
    public static ItemStack getBlueLantern() {
        return createCustomItem(
                Material.SOUL_TORCH,
                ChatColor.AQUA + "Blue Lantern",
                Arrays.asList(
                        ChatColor.GRAY + "A mystical lantern that expands",
                        ChatColor.GRAY + "soul power capacity of weapons",
                        "",
                        ChatColor.YELLOW + "Effect: " + ChatColor.WHITE + "+100% Soul Cap",
                        ChatColor.YELLOW + "Maximum: " + ChatColor.WHITE + "500% Total Cap",
                        "",
                        ChatColor.DARK_PURPLE + "Drag onto swords to apply"
                ),
                1,
                false,
                true
        );
    }

    // ===== SOUL ITEM UTILITY =====
    public static ItemStack getRandomSoulItem() {
        Random random = new Random();
        double roll = random.nextDouble() * 100;

        if (roll < 4.0) { // 4% total for all common
            ItemStack[] common = {getSoulshard()};
            return common[random.nextInt(common.length)];
        } else if (roll < 6.0) { // 2% total for all uncommon
            ItemStack[] uncommon = {getWisp()};
            return uncommon[random.nextInt(uncommon.length)];
        } else if (roll < 7.0) { // 1% total for all rare
            ItemStack[] rare = {getWraith()};
            return rare[random.nextInt(rare.length)];
        } else if (roll < 7.25) { // 0.25% total for all epic
            ItemStack[] epic = {getRemnant()};
            return epic[random.nextInt(epic.length)];
        } else if (roll < 7.35) { // 0.1% total for all legendary
            ItemStack[] legendary = {getShade()};
            return legendary[random.nextInt(legendary.length)];
        }

        return getSoulshard();
    }
    
    // ===== GEMSTONE UTILITY METHODS =====
    
    /**
     * Gets a random gemstone based on rarity weights
     * @return A random gemstone ItemStack
     */
    public static ItemStack getRandomGemstone() {
        Random random = new Random();
        double roll = random.nextDouble() * 100;
        
        if (roll < 4.0) { // 4% total for all common
            ItemStack[] commonGems = {getQuartz(), getHematite(), getObsidian(), getAgate()};
            return commonGems[random.nextInt(commonGems.length)];
        } else if (roll < 6.0) { // 2% total for all uncommon  
            ItemStack[] uncommonGems = {getTurquoise(), getAmethyst(), getCitrine(), getGarnet()};
            return uncommonGems[random.nextInt(uncommonGems.length)];
        } else if (roll < 7.0) { // 1% total for all rare
            ItemStack[] rareGems = {getTopaz(), getPeridot(), getAquamarine(), getTanzanite()};
            return rareGems[random.nextInt(rareGems.length)];
        } else if (roll < 7.25) { // 0.25% total for all epic
            ItemStack[] epicGems = {getSapphire(), getRuby()};
            return epicGems[random.nextInt(epicGems.length)];
        } else if (roll < 7.35) { // 0.1% total for all legendary
            ItemStack[] legendaryGems = {getEmerald(), getDiamond()};
            return legendaryGems[random.nextInt(legendaryGems.length)];
        }
        
        // Default fallback (should never happen)
        return getQuartz();
    }
    
    /**
     * Gets all gemstone ItemStacks
     * @return Array of all 16 gemstone ItemStacks
     */
    public static ItemStack[] getAllGemstones() {
        return new ItemStack[]{
            // Common
            getQuartz(), getHematite(), getObsidian(), getAgate(),
            // Uncommon
            getTurquoise(), getAmethyst(), getCitrine(), getGarnet(),
            // Rare
            getTopaz(), getPeridot(), getAquamarine(), getTanzanite(),
            // Epic
            getSapphire(), getRuby(),
            // Legendary
            getEmerald(), getDiamond()
        };
    }

    /** Creates the Nether Stardust item used to empower beacons. */
    public static ItemStack getNetherStardust() {
        return createCustomItem(
                Material.NETHER_STAR,
                ChatColor.LIGHT_PURPLE + "Nether Stardust",
                Arrays.asList(
                        ChatColor.GRAY + "A rare residue of Piglin festivities.",
                        ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Click onto a Beacon Charm to add 1,000 Beacon Power.",
                        ChatColor.DARK_PURPLE + "Special Item"
                ),
                1,
                false,
                true
        );
    }

    /** Smithing item unlocking Unbreaking VI. */
    public static ItemStack getUnbreakingVI() {
        return createCustomItem(
                Material.NETHERITE_INGOT,
                ChatColor.GOLD + "Unbreaking VI",
                Arrays.asList(
                        ChatColor.GRAY + "A masterwork manual on durability.",
                        ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Apply to equipment to unlock Unbreaking VI.",
                        ChatColor.DARK_PURPLE + "Smithing Item"
                ),
                1,
                false,
                true
        );
    }

    /** Smithing item unlocking Sharpness VIII. */
    public static ItemStack getSharpnessVIII() {
        return createCustomItem(
                Material.NETHERITE_SWORD,
                ChatColor.GOLD + "Sharpness VIII",
                Arrays.asList(
                        ChatColor.GRAY + "A masterwork manual on lethality.",
                        ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Apply to equipment to unlock Sharpness VIII.",
                        ChatColor.DARK_PURPLE + "Smithing Item"
                ),
                1,
                false,
                true
        );
    }

    /** Smithing item unlocking Smite VIII. */
    public static ItemStack getSmiteVIII() {
        return createCustomItem(
                Material.BONE,
                ChatColor.GOLD + "Smite VIII",
                Arrays.asList(
                        ChatColor.GRAY + "A masterwork manual on undead slaying.",
                        ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Apply to equipment to unlock Smite VIII.",
                        ChatColor.DARK_PURPLE + "Smithing Item"
                ),
                1,
                false,
                true
        );
    }

    /** Smithing item unlocking Bane of Arthropods VIII. */
    public static ItemStack getBaneOfArthropodsVIII() {
        return createCustomItem(
                Material.FERMENTED_SPIDER_EYE,
                ChatColor.GOLD + "Bane of Arthropods VIII",
                Arrays.asList(
                        ChatColor.GRAY + "A masterwork manual on insect eradication.",
                        ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Apply to equipment to unlock Bane of Arthropods VIII.",
                        ChatColor.DARK_PURPLE + "Smithing Item"
                ),
                1,
                false,
                true
        );
    }
}
