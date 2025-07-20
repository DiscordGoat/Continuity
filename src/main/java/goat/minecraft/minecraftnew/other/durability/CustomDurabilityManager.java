package goat.minecraft.minecraftnew.other.durability;

import goat.minecraft.minecraftnew.other.additionalfunctionality.CustomBundleGUI;
import goat.minecraft.minecraftnew.other.enchanting.CustomEnchantmentManager;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

/**
 * Manages custom durability values for items and intercepts vanilla
 * durability loss events. Handles conversion and bonuses for the custom
 * Unbreaking enchantment.
 */
public class CustomDurabilityManager implements Listener {
    private static CustomDurabilityManager instance;
    private final JavaPlugin plugin;
    private final NamespacedKey currentKey;
    private final NamespacedKey maxKey;
    private final NamespacedKey bonusKey;

    private CustomDurabilityManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.currentKey = new NamespacedKey(plugin, "custom_durability");
        this.maxKey = new NamespacedKey(plugin, "custom_max_durability");
        this.bonusKey = new NamespacedKey(plugin, "custom_bonus_durability");
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public static void init(JavaPlugin plugin) {
        if (instance == null) {
            instance = new CustomDurabilityManager(plugin);
        }
    }

    public static CustomDurabilityManager getInstance() {
        return instance;
    }

    /**
     * Sets custom durability values on the given item and updates its lore.
     */
    public void setCustomDurability(ItemStack item, int current, int max) {
        if (item == null || max <= 0) return;
        if (current < 0) current = 0;

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        int bonus = getBonusDurability(item);
        if (current > max) current = max;

        PersistentDataContainer data = meta.getPersistentDataContainer();
        data.set(currentKey, PersistentDataType.INTEGER, current);
        data.set(maxKey, PersistentDataType.INTEGER, max - bonus);
        item.setItemMeta(meta);

        updateLore(item, current, max);
        updateVanillaDamage(item, current, max);
    }

    /**
     * Returns the current durability stored on the item.
     */
    public int getCurrentDurability(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item.getType().getMaxDurability();
        PersistentDataContainer data = meta.getPersistentDataContainer();
        Integer value = data.get(currentKey, PersistentDataType.INTEGER);
        if (value != null) return value;

        int vanillaMax = item.getType().getMaxDurability();
        if (meta instanceof Damageable damageable) {
            return vanillaMax - damageable.getDamage();
        }
        return vanillaMax;
    }

    /**
     * Returns the max durability stored on the item.
     */
    public int getMaxDurability(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item.getType().getMaxDurability();
        PersistentDataContainer data = meta.getPersistentDataContainer();
        Integer base = data.get(maxKey, PersistentDataType.INTEGER);
        int bonus = getBonusDurability(item);
        int result = (base != null ? base : item.getType().getMaxDurability()) + bonus;
        return result;
    }

    /**
     * Returns the additional max durability applied from bonuses.
     */
    public int getBonusDurability(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return 0;
        PersistentDataContainer data = meta.getPersistentDataContainer();
        Integer bonus = data.get(bonusKey, PersistentDataType.INTEGER);
        return bonus != null ? bonus : 0;
    }

    /**
     * Adds bonus max durability to the item and updates lore.
     */
    public void addMaxDurabilityBonus(ItemStack item, int amount) {
        if (item == null || amount == 0) return;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;
        PersistentDataContainer data = meta.getPersistentDataContainer();
        int bonus = getBonusDurability(item) + amount;
        data.set(bonusKey, PersistentDataType.INTEGER, bonus);
        item.setItemMeta(meta);

        int current = getCurrentDurability(item);
        int max = getMaxDurability(item);
        if (current > max) current = max;
        updateLore(item, current, max);
        updateVanillaDamage(item, current, max);
    }

    /**
     * Ensures items with the custom Unbreaking enchantment have their
     * associated durability bonus applied. This allows preexisting
     * enchanted items to receive the bonus when they first take damage.
     */
    private void ensureUnbreakingBonus(ItemStack item) {
        if (CustomEnchantmentManager.hasEnchantment(item, "Unbreaking")) {
            int unbreakingLevel = CustomEnchantmentManager.getEnchantmentLevel(item, "Unbreaking");
            int currentBonus = getBonusDurability(item);
            if (currentBonus < 100 * unbreakingLevel) {
                addMaxDurabilityBonus(item, (100 * unbreakingLevel) - currentBonus);
            }
        }
    }

    /**
     * Applies damage using the custom durability system.
     */
    public void applyDamage(Player player, ItemStack item, int amount) {
        convertVanillaUnbreaking(item);
        ensureUnbreakingBonus(item);

        int current = getCurrentDurability(item);
        int max = getMaxDurability(item);
        int updated = current - amount;
        if (updated < 0) updated = 0;

        boolean preserved = false;
        if (updated == 0 && player != null
                && CustomEnchantmentManager.isEnchantmentActive(player, item, "Preservation")) {
            preserved = true;
            updated = Math.max(max - 1, 1);
        }

        setCustomDurability(item, updated, max);

        if (preserved && player != null) {
            player.playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 1.0f, 1.0f);
            player.sendMessage(ChatColor.GREEN + "Your " + item.getType().toString() + " was saved from breaking!");
            removeIfWorn(player, item);
            if (!addToBackpack(player, item)) {
                if (!addToEnderChest(player, item)) {
                    player.getWorld().dropItemNaturally(player.getLocation(), item);
                }
            }
        } else if (updated == 0 && player != null) {
            item.setAmount(0);
        }
    }

    /**
     * Repairs the given item by a specific amount.
     */
    public void repair(ItemStack item, int amount) {
        if (item == null || amount <= 0) return;
        int current = getCurrentDurability(item);
        int max = getMaxDurability(item);
        current += amount;
        if (current > max) current = max;
        setCustomDurability(item, current, max);
    }

    /**
     * Fully repairs the given item to max durability.
     */
    public void repairFully(ItemStack item) {
        if (item == null) return;
        int max = getMaxDurability(item);
        setCustomDurability(item, max, max);
    }

    @EventHandler
    public void onItemDamage(PlayerItemDamageEvent event) {
        ItemStack item = event.getItem();
        if (item == null) return;

        event.setCancelled(true); // Cancel vanilla durability handling
        applyDamage(event.getPlayer(), item, event.getDamage());
    }

    private void updateLore(ItemStack item, int current, int max) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;
        List<String> lore = meta.hasLore() ? new ArrayList<>(meta.getLore()) : new ArrayList<>();
        String line = ChatColor.GRAY + "Durability: " + current + "/" + max;
        if (!lore.isEmpty() && ChatColor.stripColor(lore.get(lore.size() - 1)).startsWith("Durability:")) {
            lore.set(lore.size() - 1, line);
        } else {
            lore.add(line);
        }
        meta.setLore(lore);
        item.setItemMeta(meta);
        goat.minecraft.minecraftnew.utils.devtools.ItemLoreFormatter.formatLore(item);
    }

    private void updateVanillaDamage(ItemStack item, int current, int max) {
        ItemMeta meta = item.getItemMeta();
        if (meta instanceof Damageable damageable) {
            int vanillaMax = item.getType().getMaxDurability();
            if (vanillaMax > 0) {
                double ratio = 1.0 - ((double) current / max);
                int newDamage = (int) Math.round(ratio * vanillaMax);
                if (newDamage < 0) newDamage = 0;
                if (newDamage > vanillaMax) newDamage = vanillaMax;
                damageable.setDamage(newDamage);
                item.setItemMeta(meta);
            }
        }
    }

    /**
     * Converts vanilla Unbreaking enchantments to the custom variant.
     */
    private void convertVanillaUnbreaking(ItemStack item) {
        int level = item.getEnchantmentLevel(Enchantment.UNBREAKING);
        if (level > 0) {
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.removeEnchant(Enchantment.UNBREAKING);
                item.setItemMeta(meta);
            }
            CustomEnchantmentManager.addEnchantment(item, "Unbreaking", level);
        }
    }

    private void removeIfWorn(Player player, ItemStack item) {
        PlayerInventory inv = player.getInventory();
        if (inv.getHelmet() != null && inv.getHelmet().equals(item)) {
            inv.setHelmet(null);
        } else if (inv.getChestplate() != null && inv.getChestplate().equals(item)) {
            inv.setChestplate(null);
        } else if (inv.getLeggings() != null && inv.getLeggings().equals(item)) {
            inv.setLeggings(null);
        } else if (inv.getBoots() != null && inv.getBoots().equals(item)) {
            inv.setBoots(null);
        }
    }

    private boolean addToBackpack(Player player, ItemStack item) {
        ItemStack clone = item.clone();
        item.setAmount(0);
        boolean success = CustomBundleGUI.getInstance().addItemToBackpack(player, clone);
        if (!success) {
            item.setAmount(1);
        }
        return success;
    }

    private boolean addToEnderChest(Player player, ItemStack item) {
        ItemStack clone = item.clone();
        item.setAmount(0);
        if (player.getEnderChest().addItem(clone).isEmpty()) {
            return true;
        }
        item.setAmount(1);
        return false;
    }
}
