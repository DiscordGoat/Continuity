package goat.minecraft.minecraftnew.subsystems.enchanting;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.enchantments.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.*;
import org.bukkit.util.Vector;

import java.util.*;

public class EnchantmentTableRightClick implements Listener {
    private Inventory enchantingTableInventory;

    // Define a set of blacklisted enchantments
    private final Set<Enchantment> blacklistedEnchants = Set.of(
            Enchantment.DURABILITY,    // Unbreaking
            Enchantment.MENDING,       // Mending
            Enchantment.LOOT_BONUS_BLOCKS, // Fortune
            Enchantment.SILK_TOUCH,     // Silk Touch
            Enchantment.BINDING_CURSE,     // Silk Touch
            Enchantment.VANISHING_CURSE,     // Silk Touch
            Enchantment.ARROW_INFINITE,     // Silk Touch
            Enchantment.THORNS,     // Silk Touch
            Enchantment.PROTECTION_FIRE,     // Silk Touch
            Enchantment.PROTECTION_PROJECTILE,     // Silk Touch
            Enchantment.PROTECTION_FALL,     // Silk Touch
            Enchantment.PROTECTION_EXPLOSIONS,     // Silk Touch
            Enchantment.DEPTH_STRIDER,     // Silk Touch
            Enchantment.DAMAGE_ALL,     // Silk Touch
            Enchantment.DIG_SPEED,     // Silk Touch
            Enchantment.PROTECTION_ENVIRONMENTAL,     // Silk Touch

            Enchantment.DAMAGE_ARTHROPODS,     // Silk Touch
            Enchantment.FIRE_ASPECT,     // Silk Touch
            Enchantment.LOOT_BONUS_MOBS,     // Silk Touch
            Enchantment.KNOCKBACK,     // Silk Touch
            Enchantment.DAMAGE_UNDEAD,     // Silk Touch
            Enchantment.SWEEPING_EDGE,     // Silk Touch
            Enchantment.ARROW_FIRE,     // Silk Touch
            Enchantment.MULTISHOT,     // Silk Touch
            Enchantment.PIERCING,     // Silk Touch
            Enchantment.ARROW_DAMAGE,     // Silk Touch
            Enchantment.OXYGEN,     // Silk Touch
            Enchantment.WATER_WORKER,     // Silk Touch
            Enchantment.IMPALING,     // Silk Touch
            Enchantment.SWIFT_SNEAK,     // Silk Touch
            Enchantment.FROST_WALKER,     // Silk Touch
            Enchantment.SOUL_SPEED,     // Silk Touch

            Enchantment.ARROW_KNOCKBACK     // Silk Touch
    );

    public EnchantmentTableRightClick() {}

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e) {
        Player player = e.getPlayer();
        Block block = e.getClickedBlock();
        if (e.getAction() == Action.RIGHT_CLICK_BLOCK && block != null && block.getType() == Material.ENCHANTING_TABLE) {
            e.setCancelled(true);

            ItemStack heldItem = player.getInventory().getItemInMainHand();
            if (heldItem == null || heldItem.getType() == Material.AIR) {
                player.sendMessage(ChatColor.RED + "You must hold an item to enchant!");
                return;
            }

            this.enchantingTableInventory = Bukkit.createInventory(null, 54, "Enchanting Table");

            // Fill the inventory with placeholder panes
            ItemStack glassPane = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
            ItemMeta itemMeta = glassPane.getItemMeta();
            itemMeta.setDisplayName(" ");
            glassPane.setItemMeta(itemMeta);
            for (int i = 0; i < 54; i++) {
                this.enchantingTableInventory.setItem(i, glassPane);
            }

            int slot = 18; // Starting slot
            for (Enchantment enchantment : Enchantment.values()) {
                // Skip blacklisted enchantments
                if (blacklistedEnchants.contains(enchantment)) {
                    continue;
                }

                if (enchantment.canEnchantItem(heldItem)) {
                    int currentLevel = heldItem.getEnchantmentLevel(enchantment);
                    int nextLevel = currentLevel + 1;

                    // Create an ItemStack to represent this enchantment option
                    ItemStack enchantmentItem = new ItemStack(Material.ENCHANTED_BOOK);
                    EnchantmentStorageMeta meta = (EnchantmentStorageMeta) enchantmentItem.getItemMeta();
                    meta.addStoredEnchant(enchantment, nextLevel, true);
                    enchantmentItem.setItemMeta(meta);

                    // Set display name and lore
                    ItemMeta displayMeta = enchantmentItem.getItemMeta();
                    displayMeta.setDisplayName(ChatColor.GREEN + capitalize(enchantment.getKey().getKey().replace('_', ' ')) + " " + nextLevel);
                    List<String> lore = new ArrayList<>();
                    lore.add(ChatColor.GRAY + "Click to enchant your item with " + enchantment.getKey().getKey() + " " + nextLevel);
                    displayMeta.setLore(lore);
                    enchantmentItem.setItemMeta(displayMeta);

                    this.enchantingTableInventory.setItem(slot, enchantmentItem);
                    slot += 2; // Skip a slot
                }
            }

            player.openInventory(this.enchantingTableInventory);
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if (e.getView().getTitle().equals("Enchanting Table")) {
            e.setCancelled(true);
            Player player = (Player) e.getWhoClicked();
            ItemStack clickedItem = e.getCurrentItem();
            ItemStack heldItem = player.getInventory().getItemInMainHand();
            if (clickedItem != null && clickedItem.getType() == Material.ENCHANTED_BOOK) {
                EnchantmentStorageMeta meta = (EnchantmentStorageMeta) clickedItem.getItemMeta();
                Map<Enchantment, Integer> enchantments = meta.getStoredEnchants();

                // There should be only one enchantment
                if (enchantments.size() != 1) {
                    player.sendMessage(ChatColor.RED + "Invalid enchantment.");
                    return;
                }

                Map.Entry<Enchantment, Integer> entry = enchantments.entrySet().iterator().next();
                Enchantment enchantment = entry.getKey();
                int level = entry.getValue();

                // Skip if the enchantment is blacklisted
                if (blacklistedEnchants.contains(enchantment)) {
                    player.sendMessage(ChatColor.RED + "This enchantment is not allowed!");
                    return;
                }

                // Require 8 Lapis Lazuli per enchantment
                int lapisCount = getTotalItemCount(player, Material.LAPIS_LAZULI);
                if (lapisCount < 4) {
                    player.sendMessage(ChatColor.RED + "You need at least 4 Lapis Lazuli to enchant!");
                    return;
                }

                // Require a Forbidden Book for any level greater than 1
                int maxLevel = enchantment.getMaxLevel();
                if (level > maxLevel) {
                    player.sendMessage(ChatColor.RED + "This enchantment cannot exceed level " + (maxLevel) + "!");
                    return;
                }
                ItemStack forbiddenBook = getForbiddenBook(player);
                if (forbiddenBook == null) {
                    player.sendMessage(ChatColor.RED + "You need a Forbidden Book to apply this enchantment!");
                    return;
                } else {
                    // Consume the Forbidden Book
                    removeItem(player, forbiddenBook, 1);
                    player.sendMessage(ChatColor.YELLOW + "You have consumed a Forbidden Book to apply the enchantment!");
                }



                // Apply the enchantment
                heldItem.addUnsafeEnchantment(enchantment, level);
                player.sendMessage(ChatColor.GREEN + "Your item has been enchanted with " + enchantment.getKey().getKey() + " " + level + "!");

                // Play effects
                playEnchantingSound(player, player.getLocation());
                spawnParticlesOnBlock(player.getLocation());
                removeItems(player, Material.LAPIS_LAZULI, 4);
                player.closeInventory();
            }
        }

    }

    private ItemStack getForbiddenBook(Player player) {
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.getType() == Material.WRITTEN_BOOK) {
                ItemMeta meta = item.getItemMeta();
                if (meta != null && meta.hasDisplayName() && meta.getDisplayName().equals(ChatColor.YELLOW + "Forbidden Book")) {
                    return item;
                }
            }
        }
        return null;
    }

    private int getTotalItemCount(Player player, Material material) {
        int count = 0;
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.getType() == material) {
                count += item.getAmount();
            }
        }
        return count;
    }

    private void removeItems(Player player, Material material, int amount) {
        int remaining = amount;
        ItemStack[] contents = player.getInventory().getContents();
        for (int i = 0; i < contents.length; i++) {
            ItemStack item = contents[i];
            if (item != null && item.getType() == material) {
                int stackAmount = item.getAmount();
                if (stackAmount <= remaining) {
                    player.getInventory().setItem(i, null);
                    remaining -= stackAmount;
                    if (remaining == 0) {
                        break;
                    }
                } else {
                    item.setAmount(stackAmount - remaining);
                    player.getInventory().setItem(i, item);
                    remaining = 0;
                    break;
                }
            }
        }
    }

    private void removeItem(Player player, ItemStack itemToRemove, int amount) {
        ItemStack[] contents = player.getInventory().getContents();
        for (int i = 0; i < contents.length; i++) {
            ItemStack item = contents[i];
            if (item != null && item.equals(itemToRemove)) {
                int stackAmount = item.getAmount();
                if (stackAmount <= amount) {
                    player.getInventory().setItem(i, null);
                    amount -= stackAmount;
                    if (amount == 0) {
                        break;
                    }
                } else {
                    item.setAmount(stackAmount - amount);
                    player.getInventory().setItem(i, item);
                    break;
                }
            }
        }
    }

    public static void spawnParticlesOnBlock(Location location) {
        World world = location.getWorld();
        if (world == null) return;

        for (int i = 0; i < 100; i++) {
            double offsetX = Math.random() * 0.5 - 0.25;
            double offsetY = Math.random() * 0.5;
            double offsetZ = Math.random() * 0.5 - 0.25;
            Location particleLocation = location.clone().add(offsetX, offsetY + 1, offsetZ);
            world.spawnParticle(Particle.ENCHANTMENT_TABLE, particleLocation, 1);
        }
    }

    public static void playEnchantingSound(Player player, Location location) {
        Sound sound = Sound.BLOCK_ENCHANTMENT_TABLE_USE;
        float volume = 1.0F;
        float pitch = 1.0F;
        player.playSound(location, sound, volume, pitch);
    }

    private String capitalize(String str) {
        String[] words = str.toLowerCase().split(" ");
        StringBuilder capitalized = new StringBuilder();
        for (String word : words) {
            if (word.isEmpty()) continue;
            capitalized.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1)).append(" ");
        }
        return capitalized.toString().trim();
    }
}
