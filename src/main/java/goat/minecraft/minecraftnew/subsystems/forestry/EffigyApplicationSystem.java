package goat.minecraft.minecraftnew.subsystems.forestry;

import goat.minecraft.minecraftnew.utils.devtools.ItemRegistry;
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

public class EffigyApplicationSystem implements Listener {

    private final JavaPlugin plugin;

    private static final Map<String, Integer> EFFIGY_POWER_VALUES = new HashMap<>();
    static {
        // Common (+1)
        EFFIGY_POWER_VALUES.put("Oak Effigy", 1);
        EFFIGY_POWER_VALUES.put("Birch Effigy", 1);
        // Uncommon (+3)
        EFFIGY_POWER_VALUES.put("Spruce Effigy", 3);
        // Rare (+7)
        EFFIGY_POWER_VALUES.put("Acacia Effigy", 7);
        // Epic (+10)
        EFFIGY_POWER_VALUES.put("Dark Oak Effigy", 10);
        // Legendary (+20)
        EFFIGY_POWER_VALUES.put("Crimson Effigy", 20);
        EFFIGY_POWER_VALUES.put("Warped Effigy", 20);
    }

    public EffigyApplicationSystem(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();

        ItemStack cursor = event.getCursor();
        ItemStack clicked = event.getCurrentItem();
        if (cursor == null || clicked == null) return;

        if (!isEffigy(cursor)) return;

        if (!isEligibleAxe(clicked)) {
            if (clicked.getType().name().contains("AXE")) {
                player.sendMessage(ChatColor.RED + "Only diamond or netherite axes can harness Spirit Energy!");
                event.setCancelled(true);
            }
            return;
        }

        if (applyEffigy(cursor, clicked, player)) {
            if (cursor.getAmount() > 1) {
                cursor.setAmount(cursor.getAmount() - 1);
                event.setCursor(cursor);
            } else {
                event.setCursor(null);
            }
            event.setCancelled(true);
            player.playSound(player.getLocation(), Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1.0f, 1.2f);
            String name = ChatColor.stripColor(cursor.getItemMeta().getDisplayName());
            int gain = EFFIGY_POWER_VALUES.get(name);
            player.sendMessage(ChatColor.GREEN + "Applied " + ChatColor.YELLOW + name + ChatColor.GREEN +
                    " (+" + gain + "% Spirit Energy) to your axe!");
        }
    }

    private boolean isEffigy(ItemStack item) {
        if (item == null || !item.hasItemMeta() || !item.getItemMeta().hasLore()) return false;
        List<String> lore = item.getItemMeta().getLore();
        if (lore == null) return false;
        for (String line : lore) {
            if (ChatColor.stripColor(line).equals("Effigy")) {
                return true;
            }
        }
        return false;
    }

    private boolean isEligibleAxe(ItemStack item) {
        if (item == null) return false;
        Material m = item.getType();
        return m == Material.DIAMOND_AXE || m == Material.NETHERITE_AXE;
    }

    private boolean applyEffigy(ItemStack effigy, ItemStack axe, Player player) {
        String name = ChatColor.stripColor(effigy.getItemMeta().getDisplayName());
        Integer value = EFFIGY_POWER_VALUES.get(name);
        if (value == null) {
            player.sendMessage(ChatColor.RED + "Unknown effigy type!");
            return false;
        }
        int current = getCurrentEnergy(axe);
        int cap = getEnergyCap(axe);
        int newVal = Math.min(current + value, cap);
        if (newVal == current) {
            player.sendMessage(ChatColor.RED + "This axe is already at maximum Spirit Energy (" + cap + "%)!");
            return false;
        }
        updateEnergyLore(axe, newVal);
        return true;
    }

    private int getCurrentEnergy(ItemStack axe) {
        if (!axe.hasItemMeta() || !axe.getItemMeta().hasLore()) return 0;
        for (String line : axe.getItemMeta().getLore()) {
            String stripped = ChatColor.stripColor(line);
            if (stripped.startsWith("Spirit Energy: ")) {
                try {
                    String txt = stripped.replace("Spirit Energy: ", "");
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

    private int getEnergyCap(ItemStack axe) {
        if (!axe.hasItemMeta() || !axe.getItemMeta().hasLore()) return 100;
        for (String line : axe.getItemMeta().getLore()) {
            String stripped = ChatColor.stripColor(line);
            if (stripped.startsWith("Spirit Cap: ")) {
                String txt = stripped.substring("Spirit Cap: ".length()).replace("%", "");
                try { return Integer.parseInt(txt); } catch (NumberFormatException e) { return 100; }
            }
        }
        return 100;
    }

    private void updateEnergyLore(ItemStack axe, int newVal) {
        ItemMeta meta = axe.getItemMeta();
        List<String> lore = meta.hasLore() ? new ArrayList<>(meta.getLore()) : new ArrayList<>();
        int cap = getEnergyCap(axe);
        lore.removeIf(line -> {
            String stripped = ChatColor.stripColor(line);
            return stripped.startsWith("Spirit Energy: ") ||
                   (line.contains("[") && line.contains("|") && line.contains("]")) ||
                   (stripped.isEmpty() && lore.indexOf(line) <= 2);
        });

        String line;
        if (cap > 100) {
            line = ChatColor.AQUA + "Spirit Energy: " + ChatColor.YELLOW + newVal + "%" +
                    ChatColor.GRAY + " / " + ChatColor.YELLOW + cap + "%";
        } else {
            line = ChatColor.AQUA + "Spirit Energy: " + ChatColor.YELLOW + newVal + "%";
        }
        String bar = createExtendedBar(newVal, cap);
        lore.add(0, "");
        lore.add(0, bar);
        lore.add(0, line);
        meta.setLore(lore);
        axe.setItemMeta(meta);
    }

    private String createExtendedBar(int current, int cap) {
        int base = 20;
        int extra = (cap - 100) / 100;
        int total = base + (extra * 5);
        int filled = (int)((double)current / cap * total);
        int empty = total - filled;
        StringBuilder b = new StringBuilder();
        b.append(ChatColor.DARK_GRAY).append("[");
        b.append(ChatColor.YELLOW);
        for(int i=0;i<filled;i++) b.append("|");
        b.append(ChatColor.GRAY);
        for(int i=0;i<empty;i++) b.append("|");
        b.append(ChatColor.DARK_GRAY).append("]");
        return b.toString();
    }

    public static int getAxeSpiritEnergy(ItemStack axe) {
        if (axe == null || !axe.hasItemMeta() || !axe.getItemMeta().hasLore()) return 0;
        for (String line : axe.getItemMeta().getLore()) {
            String stripped = ChatColor.stripColor(line);
            if (stripped.startsWith("Spirit Energy: ")) {
                try {
                    String txt = stripped.replace("Spirit Energy: ", "");
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
