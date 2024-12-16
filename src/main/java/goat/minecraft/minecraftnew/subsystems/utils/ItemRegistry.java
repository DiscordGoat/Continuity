package goat.minecraft.minecraftnew.subsystems.utils;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class ItemRegistry {
    private ItemRegistry() {
    } // Private constructor to prevent instantiation

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
        return CustomItemManager.createCustomItem(Material.BOW, ChatColor.YELLOW +
            "Bowstring", Arrays.asList(
            ChatColor.GRAY + "Air Technology.",
            ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Apply it to equipment to unlock the secrets of Power.",
            ChatColor.DARK_PURPLE + "Smithing Item"
    ), 1, false, true);
    }
    public static ItemStack getDrownedDrop() {
        return CustomItemManager.createCustomItem(
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
        return CustomItemManager.createCustomItem(
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
        return CustomItemManager.createCustomItem(
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
        return CustomItemManager.createCustomItem(
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
        return CustomItemManager.createCustomItem(
                Material.SPIDER_EYE,
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
        return CustomItemManager.createCustomItem(
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
        return CustomItemManager.createCustomItem(
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
        return CustomItemManager.createCustomItem(
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
        return CustomItemManager.createCustomItem(
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
        return CustomItemManager.createCustomItem(
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
        return CustomItemManager.createCustomItem(
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
        return CustomItemManager.createCustomItem(
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
        return CustomItemManager.createCustomItem(
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
        return CustomItemManager.createCustomItem(
                Material.ARROW,
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
        return CustomItemManager.createCustomItem(
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
        return CustomItemManager.createCustomItem(
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
        return CustomItemManager.createCustomItem(
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
        return CustomItemManager.createCustomItem(
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
        return CustomItemManager.createCustomItem(
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
        return CustomItemManager.createCustomItem(
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
        return CustomItemManager.createCustomItem(
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
        return CustomItemManager.createCustomItem(
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
        return CustomItemManager.createCustomItem(
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
        return CustomItemManager.createCustomItem(
                Material.HEART_OF_THE_SEA,
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
                Material.TURTLE_HELMET,
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
                        ChatColor.GRAY + "Restores 10000 durability.",
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

    public static ItemStack getSwiftSneak() {
        return createCustomItem(Material.LEATHER_LEGGINGS, ChatColor.YELLOW +
                "Swim Trunks", Arrays.asList(
                ChatColor.GRAY + "Water Technology.",
                ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Apply it to equipment to unlock the secrets of Swift Sneak.",
                ChatColor.DARK_PURPLE + "Smithing Item"
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

    public static ItemStack getSweepingEdge() {
        return createCustomItem(Material.IRON_SWORD, ChatColor.YELLOW +
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
