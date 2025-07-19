package goat.minecraft.minecraftnew.other.durability;

import goat.minecraft.minecraftnew.MinecraftNew;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemBreakEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Manages custom durability values for items. All durability events should
 * pass through this manager so we can track a custom current and maximum
 * durability stored in the item's persistent data container. The durability
 * is displayed on the last line of the item's lore.
 */
public class CustomDurabilityManager implements Listener {

    private static CustomDurabilityManager instance;

    private final JavaPlugin plugin;
    private final NamespacedKey currentKey;
    private final NamespacedKey maxKey;

    private CustomDurabilityManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.currentKey = new NamespacedKey(plugin, "custom_durability_current");
        this.maxKey = new NamespacedKey(plugin, "custom_durability_max");
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    /**
     * Initializes the durability manager.
     */
    public static CustomDurabilityManager init(JavaPlugin plugin) {
        if (instance == null) {
            instance = new CustomDurabilityManager(plugin);
        }
        return instance;
    }

    /**
     * Gets the singleton instance.
     */
    public static CustomDurabilityManager getInstance() {
        return instance;
    }

    /**
     * Sets custom durability values on the provided item.
     */
    public void setCustomDurability(ItemStack item, int current, int max) {
        if (item == null) return;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;
        PersistentDataContainer container = meta.getPersistentDataContainer();
        container.set(currentKey, PersistentDataType.INTEGER, Math.max(current, 0));
        container.set(maxKey, PersistentDataType.INTEGER, Math.max(max, 1));
        item.setItemMeta(meta);
        updateLore(item);
    }

    /**
     * Updates the last lore line of the item to display durability.
     */
    private void updateLore(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;
        PersistentDataContainer container = meta.getPersistentDataContainer();
        Integer current = container.get(currentKey, PersistentDataType.INTEGER);
        Integer max = container.get(maxKey, PersistentDataType.INTEGER);
        if (current == null || max == null) return;
        java.util.List<String> lore = meta.getLore();
        if (lore == null) lore = new java.util.ArrayList<>();
        String line = "Durability: " + current + "/" + max;
        if (lore.isEmpty()) {
            lore.add(line);
        } else {
            if (lore.size() == 1) {
                lore.set(0, line);
            } else {
                lore.set(lore.size() - 1, line);
            }
        }
        meta.setLore(lore);
        item.setItemMeta(meta);
    }

    /**
     * Handles durability usage for items with custom durability. Unbreaking is
     * ignored for now.
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onItemDamage(PlayerItemDamageEvent event) {
        if (event.isCancelled()) return;
        ItemStack item = event.getItem();
        if (item == null) return;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;
        PersistentDataContainer container = meta.getPersistentDataContainer();
        Integer current = container.get(currentKey, PersistentDataType.INTEGER);
        Integer max = container.get(maxKey, PersistentDataType.INTEGER);
        if (current == null || max == null) return; // Not a custom item

        event.setCancelled(true); // ignore vanilla durability handling

        int newCurrent = current - event.getDamage();
        if (newCurrent < 0) newCurrent = 0;
        container.set(currentKey, PersistentDataType.INTEGER, newCurrent);
        item.setItemMeta(meta);
        updateLore(item);

        if (newCurrent == 0) {
            Player player = event.getPlayer();
            PlayerItemBreakEvent breakEvent = new PlayerItemBreakEvent(player, item);
            Bukkit.getPluginManager().callEvent(breakEvent);
            if (!breakEvent.isCancelled()) {
                EquipmentSlot slot = event.getHand();
                if (slot != null && player.getInventory().getItem(slot) != null) {
                    player.getInventory().setItem(slot, new ItemStack(Material.AIR));
                }
            }
        }
    }
}
