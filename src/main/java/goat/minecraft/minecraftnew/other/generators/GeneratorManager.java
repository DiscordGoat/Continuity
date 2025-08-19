package goat.minecraft.minecraftnew.other.generators;

import goat.minecraft.minecraftnew.MinecraftNew;
import goat.minecraft.minecraftnew.utils.devtools.ItemLoreFormatter;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Manages power values and tiers for generator items.
 */
public class GeneratorManager {
    private static GeneratorManager instance;
    private final NamespacedKey powerKey;
    private final NamespacedKey powerLimitKey;
    private final NamespacedKey tierKey;
    private final NamespacedKey activeKey;
    private final NamespacedKey idKey;
    private final NamespacedKey typeKey;

    private GeneratorManager(JavaPlugin plugin) {
        this.powerKey = new NamespacedKey(plugin, "generator_power");
        this.powerLimitKey = new NamespacedKey(plugin, "generator_power_limit");
        this.tierKey = new NamespacedKey(plugin, "generator_tier");
        this.activeKey = new NamespacedKey(plugin, "generator_active");
        this.idKey = new NamespacedKey(plugin, "generator_id");
        this.typeKey = new NamespacedKey(plugin, "generator_type");
    }

    public static void init(JavaPlugin plugin) {
        if (instance == null) {
            instance = new GeneratorManager(plugin);
        }
    }

    public static GeneratorManager getInstance() {
        return instance;
    }

    public boolean isGenerator(ItemStack item) {
        if (item == null) return false;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;
        PersistentDataContainer data = meta.getPersistentDataContainer();
        return data.has(tierKey, PersistentDataType.INTEGER);

    }

    public int getPower(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return 0;
        Integer val = meta.getPersistentDataContainer().get(powerKey, PersistentDataType.INTEGER);
        return val != null ? val : 0;
    }

    public int getPowerLimit(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return 0;
        Integer val = meta.getPersistentDataContainer().get(powerLimitKey, PersistentDataType.INTEGER);
        return val != null ? val : 0;
    }

    public int getTier(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return 0;
        Integer val = meta.getPersistentDataContainer().get(tierKey, PersistentDataType.INTEGER);
        return val != null ? val : 0;
    }

    public boolean isActive(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;
        Integer val = meta.getPersistentDataContainer().get(activeKey, PersistentDataType.INTEGER);
        return val != null && val == 1;
    }

    public String getId(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return null;
        return meta.getPersistentDataContainer().get(idKey, PersistentDataType.STRING);
    }
    public String getGeneratorType(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return "";
        return meta.getPersistentDataContainer().getOrDefault(typeKey, PersistentDataType.STRING, "");
    }
    public void setGeneratorType(ItemStack item, String type) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;
        PersistentDataContainer data = meta.getPersistentDataContainer();
        data.set(typeKey, PersistentDataType.STRING, type);
        item.setItemMeta(meta);
    }
    public void setGenerator(ItemStack item, int power, int powerLimit, int tier, boolean active) {
        if (item == null) return;
        if (power < 0) power = 0;
        if (powerLimit < 0) powerLimit = 0;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;
        PersistentDataContainer data = meta.getPersistentDataContainer();
        if (!data.has(idKey, PersistentDataType.STRING)) {
            data.set(idKey, PersistentDataType.STRING, java.util.UUID.randomUUID().toString());
        }
        data.set(powerKey, PersistentDataType.INTEGER, power);
        data.set(powerLimitKey, PersistentDataType.INTEGER, powerLimit);
        data.set(tierKey, PersistentDataType.INTEGER, tier);
        data.set(activeKey, PersistentDataType.INTEGER, active ? 1 : 0);
        data.set(typeKey, PersistentDataType.STRING,
                data.get(typeKey, PersistentDataType.STRING) != null
                        ? data.get(typeKey, PersistentDataType.STRING)
                        : "");
        item.setItemMeta(meta);
        updateName(item);
        updateLore(item);
        updateDurabilityBar(item);
    }

    public void addPower(ItemStack item, int amount) {
        if (!isGenerator(item)) return;
        int tier = getTier(item);
        if (tier >= 10) return;
        int power = getPower(item) + amount;
        int limit = getPowerLimit(item);
        if (power >= limit) {
            tier++;
            power = 0;
            if (tier >= 10) {
                tier = 10;
                limit = 0;
            } else {
                limit += 1 + new Random().nextInt(9);
            }
        }
        setGenerator(item, power, limit, tier, isActive(item));
    }

    public void toggleActivation(ItemStack item) {
        if (!isGenerator(item)) return;
        boolean active = !isActive(item);
        setGenerator(item, getPower(item), getPowerLimit(item), getTier(item), active);
    }

    private void updateName(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;
        int tier = getTier(item);
        String base = getBaseName(meta.getDisplayName());
        meta.setDisplayName(base + " Tier " + toRomanNumeral(tier));
        item.setItemMeta(meta);
    }

    private String getBaseName(String name) {
        int idx = name.lastIndexOf(" Tier ");
        if (idx >= 0) {
            return name.substring(0, idx);
        }
        return name;
    }

    private String toRomanNumeral(int number) {
        String[] numerals = {"I","II","III","IV","V","VI","VII","VIII","IX","X"};
        if (number >= 1 && number <= 10) {
            return numerals[number - 1];
        }
        return String.valueOf(number);
    }

    private void updateLore(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;
        List<String> lore = meta.hasLore() ? new ArrayList<>(meta.getLore()) : new ArrayList<>();
        boolean active = isActive(item);
        int power = getPower(item);
        int limit = getPowerLimit(item);
        int tier = getTier(item);

        String ioLine = (active ? ChatColor.GREEN + "I/O: Activated" : ChatColor.RED + "I/O: Deactivated");
        replaceOrAdd(lore, "I/O:", ioLine);

        if (tier >= 10) {
            removeLine(lore, "Power:");
            replaceOrAdd(lore, "MAX TIER", ChatColor.GOLD + "MAX TIER");
        } else {
            String powerLine = ChatColor.GOLD + "Power: " + power + "/" + limit;
            replaceOrAdd(lore, "Power:", powerLine);
            removeLine(lore, "MAX TIER");
        }

        meta.setLore(lore);
        item.setItemMeta(meta);
        ItemLoreFormatter.formatLore(item);
    }

    private void replaceOrAdd(List<String> lore, String prefix, String line) {
        int index = findIndex(lore, prefix);
        if (index >= 0) {
            lore.set(index, line);
        } else {
            lore.add(line);
        }
    }

    private void removeLine(List<String> lore, String prefix) {
        int index = findIndex(lore, prefix);
        if (index >= 0) {
            lore.remove(index);
        }
    }

    private int findIndex(List<String> lore, String prefix) {
        for (int i = 0; i < lore.size(); i++) {
            String stripped = ChatColor.stripColor(lore.get(i));
            if (stripped.startsWith(prefix)) {
                return i;
            }
        }
        return -1;
    }

    private void updateDurabilityBar(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta instanceof Damageable damageable) {
            int tier = getTier(item);
            int limit = getPowerLimit(item);
            int power = getPower(item);
            int vanillaMax = item.getType().getMaxDurability();
            if (vanillaMax > 0) {
                int newDamage;
                if (tier >= 10 || limit <= 0) {
                    newDamage = 0;
                } else {
                    newDamage = vanillaMax - (int)Math.round(((double) power / limit) * vanillaMax);
                    if (newDamage < 0) newDamage = 0;
                    if (newDamage > vanillaMax) newDamage = vanillaMax;
                }
                damageable.setDamage(newDamage);
                item.setItemMeta(meta);
            }
        }
    }
}
