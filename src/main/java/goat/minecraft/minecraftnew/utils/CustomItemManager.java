package goat.minecraft.minecraftnew.utils;


import goat.minecraft.minecraftnew.MinecraftNew;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Random;

import static goat.minecraft.minecraftnew.subsystems.fishing.SeaCreatureRegistry.createAlchemyItem;

public class CustomItemManager implements Listener {
    // Method to create a custom item
    Random random = new Random();
    MinecraftNew plugin = MinecraftNew.getInstance();

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



    // Method to create the Silk Worm item
    public ItemStack createSilkWorm() {
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
    public ItemStack createHireVillager() {
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
    public ItemStack createForbiddenBook() {
        return createCustomItem(
                Material.WRITTEN_BOOK,
                ChatColor.YELLOW + "Forbidden Book",
                List.of(ChatColor.GRAY + "A dangerous book full of experimental magic.",
                        ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Apply to equipment to push the limits of enchantments.",
                        ChatColor.DARK_PURPLE + "Enchanting Item"),
                1,
                false // Set to true if you want it to be unbreakable
                , true
        );
    }

    public ItemStack createCustomQuote() {
        return createCustomItem(
                Material.PAPER,
                ChatColor.DARK_PURPLE + "Inspiration",
                List.of("§7A randomly selected inspirational quote.",
                        "§7Apply it to equipment to add custom descriptions.",
                        "Smithing Cosmetic"),
                1,
                false // Set to true if you want it to be unbreakable
                , true
        );
    }

    public ItemStack mithrilChunk() {
        return createCustomItem(
                Material.LIGHT_BLUE_DYE,
                ChatColor.BLUE + "Mithril Chunk",
                List.of(ChatColor.GRAY + "A rare mineral.",
                        "Apply it to equipment to unlock the secrets of Unbreaking.",
                        "Smithing Item"),
                1,
                false // Set to true if you want it to be unbreakable
                , true
        );
    }

    public ItemStack perfectDiamond() {
        return createCustomItem(
                Material.DIAMOND,
                ChatColor.BLUE + "Perfect Diamond",
                List.of(ChatColor.GRAY + "A rare mineral.",
                        "Apply it to a pickaxe to unlock the secrets of Fortune.",
                        "Smithing Item"),
                1,
                false // Set to true if you want it to be unbreakable
                , true
        );
    }

    public ItemStack fishBone() {
        return createCustomItem(
                Material.BONE,
                ChatColor.LIGHT_PURPLE + "Fish Bone",
                List.of(
                        ChatColor.GRAY + "A bone from a fish.",
                        "Use: " + ChatColor.GRAY + "Turns potions into splash potions.",
                        "Brewing Modifier"),
                1,
                false // Set to true if you want it to be unbreakable
                , false);
    }

    ItemStack shallowShell = createAlchemyItem("Shallow Shell", Material.SCUTE, List.of(
            ChatColor.GRAY + "A shell found in shallow waters.",
            ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Repairs equipment slightly.",
            ChatColor.GRAY + "Restores 15% durability.",
            ChatColor.DARK_PURPLE + "Smithing Ingredient"
    ));
    ItemStack shell = createAlchemyItem("Shell", Material.CYAN_DYE, List.of(
            ChatColor.GRAY + "A sturdy shell.",
            ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Repairs equipment moderately.",
            ChatColor.GRAY + "Restores 50% durability.",
            ChatColor.DARK_PURPLE + "Smithing Ingredient"
    ));
    ItemStack deepShell = createAlchemyItem("Deep Shell", Material.TURTLE_HELMET, List.of(
            ChatColor.GRAY + "A resilient shell from the depths.",
            ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Repairs equipment greatly.",
            ChatColor.GRAY + "Restores 80% durability.",
            ChatColor.DARK_PURPLE + "Smithing Ingredient"
    ));
    ItemStack abyssalShell = createAlchemyItem("Abyssal Shell", Material.YELLOW_DYE, List.of(
            ChatColor.GRAY + "A shell from the deepest abyss.",
            ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Repairs equipment massively.",
            ChatColor.GRAY + "Restores 100% durability.",
            ChatColor.DARK_PURPLE + "Smithing Ingredient"
    ));




    // Method to alter drops of a mob



    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        XPManager xpManager = new XPManager(plugin);
        int forbiddenBookChance = random.nextInt(100) + 1; // Generates a number between 1 and 100

        if (event.getEntity().getKiller() != null) {
            double playerLevel = xpManager.getPlayerLevel(Objects.requireNonNull(event.getEntity().getKiller()), "Combat");
            if (event.getEntity().getKiller() instanceof Player) {
                // Check if the forbiddenBookChance equals 1 for a 1% chance, and you can factor in playerLevel if needed
                if (forbiddenBookChance == 1) {
                    event.getEntity().getLocation().getWorld().dropItem(event.getEntity().getLocation(), createForbiddenBook());
                    event.getEntity().getKiller().sendMessage(ChatColor.DARK_PURPLE + "A strange book falls to the ground...");
                    event.getEntity().getKiller().playSound(event.getEntity().getLocation(), Sound.ITEM_TRIDENT_THUNDER, 100, 100);
                }
            }
        }
    }

    public static List<Material> logsAndStems = Arrays.asList(
            Material.OAK_LOG,
            Material.SPRUCE_LOG,
            Material.BIRCH_LOG,
            Material.JUNGLE_LOG,
            Material.ACACIA_LOG,
            Material.DARK_OAK_LOG,
            Material.CRIMSON_STEM,
            Material.WARPED_STEM,
            Material.MANGROVE_LOG
    );

    // Method to alter drops of a block
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Material brokenBlock = event.getBlock().getType();
        Player player = event.getPlayer();

        // Check if the broken block is within the logs and stems types
        if (logsAndStems.contains(brokenBlock)) {
            int silkWormChance = random.nextInt(2400) + 1; // Generates a number between 1 and 500

            // Implement a fixed 1/500 chance
            if (silkWormChance == 1) {
                // Drop the custom Silk Worm item at the block's location
                event.getBlock().getLocation().getWorld().dropItem(event.getBlock().getLocation(), createSilkWorm());

                // Send a message to the player
                player.sendMessage(ChatColor.DARK_PURPLE + "A small insect falls to the ground...");

                // Play a sound effect to the player
                player.playSound(player.getLocation(), Sound.BLOCK_WET_GRASS_BREAK, 1.0f, 1.0f);
            }
        }
    }
}
