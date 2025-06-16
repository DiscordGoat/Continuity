package goat.minecraft.minecraftnew.subsystems.combat;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public class SoulApplicationSystem implements Listener {
    private final JavaPlugin plugin;

    private static final Map<String, Integer> SOUL_POWER_VALUES = new HashMap<>();
    static {
        SOUL_POWER_VALUES.put("Soulshard", 1);
        SOUL_POWER_VALUES.put("Wisp", 3);
        SOUL_POWER_VALUES.put("Wraith", 7);
        SOUL_POWER_VALUES.put("Remnant", 10);
        SOUL_POWER_VALUES.put("Shade", 20);
    }

    public SoulApplicationSystem(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();

        ItemStack cursor = event.getCursor();
        ItemStack clicked = event.getCurrentItem();
        if (cursor == null || clicked == null) return;

        if (!isSoulItem(cursor)) return;
        if (!isSoulWeapon(clicked)) {
            if (clicked.getType().name().contains("SWORD") || clicked.getType() == Material.BOW) {
                player.sendMessage(ChatColor.RED + "Only swords or bows can harness Soul Power!");
                event.setCancelled(true);
            }
            return;
        }

        if (applySoulItem(cursor, clicked, player)) {
            if (cursor.getAmount() > 1) {
                cursor.setAmount(cursor.getAmount() - 1);
                event.setCursor(cursor);
            } else {
                event.setCursor(null);
            }
            event.setCancelled(true);
            player.playSound(player.getLocation(), Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1.0f, 1.2f);
            String name = ChatColor.stripColor(cursor.getItemMeta().getDisplayName());
            int gain = SOUL_POWER_VALUES.get(name);
            player.sendMessage(ChatColor.GREEN + "Applied " + ChatColor.YELLOW + name +
                    ChatColor.GREEN + " (+" + gain + "% Soul Power) to your weapon!");
        }
    }

    private boolean isSoulItem(ItemStack item) {
        if (item == null || !item.hasItemMeta() || !item.getItemMeta().hasLore()) return false;
        List<String> lore = item.getItemMeta().getLore();
        if (lore == null) return false;
        for (String line : lore) {
            if (ChatColor.stripColor(line).equals("Soul Item")) {
                return true;
            }
        }
        return false;
    }

    private boolean isSoulWeapon(ItemStack item) {
        if (item == null) return false;
        Material m = item.getType();
        return m.name().endsWith("_SWORD") || m == Material.BOW;
    }

    private boolean applySoulItem(ItemStack soul, ItemStack weapon, Player player) {
        String name = ChatColor.stripColor(soul.getItemMeta().getDisplayName());
        Integer value = SOUL_POWER_VALUES.get(name);
        if (value == null) {
            player.sendMessage(ChatColor.RED + "Unknown soul item!");
            return false;
        }
        int current = getCurrentSoulPower(weapon);
        int cap = getSoulCap(weapon);
        int newVal = Math.min(current + value, cap);
        if (newVal == current) {
            player.sendMessage(ChatColor.RED + "This weapon is already at maximum Soul Power (" + cap + "%)!");
            return false;
        }
        updateSoulLore(weapon, newVal);
        return true;
    }

    private int getCurrentSoulPower(ItemStack weapon) {
        if (!weapon.hasItemMeta() || !weapon.getItemMeta().hasLore()) return 0;
        for (String line : weapon.getItemMeta().getLore()) {
            String stripped = ChatColor.stripColor(line);
            if (stripped.startsWith("Soul Power: ")) {
                try {
                    String txt = stripped.replace("Soul Power: ", "");
                    if (txt.contains(" / ")) txt = txt.split(" / ")[0];
                    txt = txt.replace("%", "");
                    return Integer.parseInt(txt);
                } catch (NumberFormatException e) {
                    return 0;
                }
            }
        }
        return 0;
    }

    private int getSoulCap(ItemStack weapon) {
        if (!weapon.hasItemMeta() || !weapon.getItemMeta().hasLore()) return 100;
        for (String line : weapon.getItemMeta().getLore()) {
            String stripped = ChatColor.stripColor(line);
            if (stripped.startsWith("Soul Cap: ")) {
                String txt = stripped.substring("Soul Cap: ".length()).replace("%", "");
                try { return Integer.parseInt(txt); } catch (NumberFormatException e) { return 100; }
            }
        }
        return 100;
    }

    private void updateSoulLore(ItemStack weapon, int newVal) {
        ItemMeta meta = weapon.getItemMeta();
        List<String> lore = meta.hasLore() ? new ArrayList<>(meta.getLore()) : new ArrayList<>();
        int cap = getSoulCap(weapon);
        lore.removeIf(line -> {
            String s = ChatColor.stripColor(line);
            return s.startsWith("Soul Power: ") || (line.contains("[") && line.contains("|") && line.contains("]")) || (s.isEmpty() && lore.indexOf(line) <= 2);
        });
        String line;
        if (cap > 100) {
            line = ChatColor.AQUA + "Soul Power: " + ChatColor.YELLOW + newVal + "%" + ChatColor.GRAY + " / " + ChatColor.YELLOW + cap + "%";
        } else {
            line = ChatColor.AQUA + "Soul Power: " + ChatColor.YELLOW + newVal + "%";
        }
        String bar = createExtendedBar(newVal, cap);
        lore.add(0, "");
        lore.add(0, bar);
        lore.add(0, line);
        meta.setLore(lore);
        weapon.setItemMeta(meta);
    }

    private String createExtendedBar(int current, int cap) {
        int base = 20;
        int extra = (cap - 100) / 100;
        int total = base + (extra * 5);
        int filled = (int)((double)current / cap * total);
        int empty = total - filled;
        StringBuilder b = new StringBuilder();
        b.append(ChatColor.DARK_GRAY).append("[");
        b.append(ChatColor.DARK_AQUA);
        for(int i=0;i<filled;i++) b.append("|");
        b.append(ChatColor.GRAY);
        for(int i=0;i<empty;i++) b.append("|");
        b.append(ChatColor.DARK_GRAY).append("]");
        return b.toString();
    }

    public static int getWeaponSoulPower(ItemStack weapon) {
        if (weapon == null || !weapon.hasItemMeta() || !weapon.getItemMeta().hasLore()) return 0;
        for (String line : weapon.getItemMeta().getLore()) {
            String stripped = ChatColor.stripColor(line);
            if (stripped.startsWith("Soul Power: ")) {
                try {
                    String txt = stripped.replace("Soul Power: ", "");
                    if (txt.contains(" / ")) txt = txt.split(" / ")[0];
                    txt = txt.replace("%", "");
                    return Integer.parseInt(txt);
                } catch (NumberFormatException e) {
                    return 0;
                }
            }
        }
        return 0;
    }
}
