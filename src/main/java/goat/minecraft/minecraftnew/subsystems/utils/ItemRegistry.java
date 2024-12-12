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
