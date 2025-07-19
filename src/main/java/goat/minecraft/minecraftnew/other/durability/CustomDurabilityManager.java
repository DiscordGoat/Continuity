package goat.minecraft.minecraftnew.other.durability;

import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

/**
 * Manages custom durability values for items and intercepts vanilla
 * durability loss events. Unbreaking is intentionally ignored for now.
 */
public class CustomDurabilityManager implements Listener {
    private static CustomDurabilityManager instance;
    private final JavaPlugin plugin;
    private final NamespacedKey currentKey;
    private final NamespacedKey maxKey;

    private CustomDurabilityManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.currentKey = new NamespacedKey(plugin, "custom_durability");
        this.maxKey = new NamespacedKey(plugin, "custom_max_durability");
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
        if (current > max) current = max;

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        PersistentDataContainer data = meta.getPersistentDataContainer();
        data.set(currentKey, PersistentDataType.INTEGER, current);
        data.set(maxKey, PersistentDataType.INTEGER, max);
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
        Integer value = data.get(maxKey, PersistentDataType.INTEGER);
        if (value != null) return value;
        return item.getType().getMaxDurability();
    }

    /**
     * Applies damage using the custom durability system.
     */
    public void applyDamage(Player player, ItemStack item, int amount) {
        int current = getCurrentDurability(item);
        int max = getMaxDurability(item);
        current -= amount;
        if (current < 0) current = 0;
        setCustomDurability(item, current, max);
        if (current == 0 && player != null) {
            item.setAmount(0);
        }
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
}
