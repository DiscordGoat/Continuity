package goat.minecraft.minecraftnew.subsystems.fishing;

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

public class BaitApplicationSystem implements Listener {
    private final JavaPlugin plugin;

    private static final Map<String, Integer> BAIT_POWER_VALUES = new HashMap<>();
    static {
        BAIT_POWER_VALUES.put("Common Bait", 1);
        BAIT_POWER_VALUES.put("Shrimp Bait", 3);
        BAIT_POWER_VALUES.put("Leech Bait", 7);
        BAIT_POWER_VALUES.put("Frog Bait", 10);
        BAIT_POWER_VALUES.put("Caviar Bait", 20);
    }

    public BaitApplicationSystem(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();

        ItemStack cursor = event.getCursor();
        ItemStack clicked = event.getCurrentItem();
        if (cursor == null || clicked == null) return;

        if (!isBait(cursor)) return;
        if (!isFishingRod(clicked)) {
            if (isAnyTool(clicked)) {
                player.sendMessage(ChatColor.RED + "Only fishing rods can hold Angler Energy!");
                event.setCancelled(true);
            }
            return;
        }

        if (applyBait(cursor, clicked, player)) {
            if (cursor.getAmount() > 1) {
                cursor.setAmount(cursor.getAmount() - 1);
                event.setCursor(cursor);
            } else {
                event.setCursor(null);
            }
            event.setCancelled(true);
            player.playSound(player.getLocation(), Sound.BLOCK_BREWING_STAND_BREW, 1.0f, 1.2f);
            String name = ChatColor.stripColor(cursor.getItemMeta().getDisplayName());
            int gain = BAIT_POWER_VALUES.get(name);
            player.sendMessage(ChatColor.GREEN + "Applied " + ChatColor.YELLOW + name + ChatColor.GREEN +
                    " (+" + gain + "% Angler Energy) to your rod!");
        }
    }

    private boolean isBait(ItemStack item) {
        if (item == null || !item.hasItemMeta() || !item.getItemMeta().hasLore()) return false;
        List<String> lore = item.getItemMeta().getLore();
        if (lore == null) return false;
        for (String line : lore) {
            if (ChatColor.stripColor(line).contains("Bait")) {
                return true;
            }
        }
        return false;
    }

    private boolean isFishingRod(ItemStack item) {
        return item != null && item.getType() == Material.FISHING_ROD;
    }

    private boolean isAnyTool(ItemStack item) {
        if (item == null) return false;
        String n = item.getType().name();
        return n.contains("PICKAXE") || n.contains("AXE") || n.contains("SHOVEL") || n.contains("HOE") || n.contains("SWORD");
    }

    private boolean applyBait(ItemStack bait, ItemStack rod, Player player) {
        String name = ChatColor.stripColor(bait.getItemMeta().getDisplayName());
        Integer val = BAIT_POWER_VALUES.get(name);
        if (val == null) {
            player.sendMessage(ChatColor.RED + "Unknown bait type!");
            return false;
        }
        int current = getRodAnglerEnergy(rod);
        int cap = getRodPowerCap(rod);
        int newVal = Math.min(current + val, cap);
        if (newVal == current) {
            player.sendMessage(ChatColor.RED + "This rod is already at maximum Angler Energy (" + cap + "%)!");
            return false;
        }
        updateRodAnglerEnergy(rod, newVal);
        return true;
    }

    private int getRodAnglerEnergy(ItemStack rod) {
        if (!rod.hasItemMeta() || !rod.getItemMeta().hasLore()) return 0;
        for (String line : rod.getItemMeta().getLore()) {
            String s = ChatColor.stripColor(line);
            if (s.startsWith("Angler Energy: ")) {
                String txt = s.replace("Angler Energy: ", "").replace("%", "");
                try {
                    if (txt.contains(" / ")) txt = txt.split(" / ")[0];
                    return Integer.parseInt(txt);
                } catch (NumberFormatException e) {
                    return 0;
                }
            }
        }
        return 0;
    }

    private int getRodPowerCap(ItemStack rod) {
        if (!rod.hasItemMeta() || !rod.getItemMeta().hasLore()) return 100;
        for (String line : rod.getItemMeta().getLore()) {
            String s = ChatColor.stripColor(line);
            if (s.startsWith("Power Cap: ")) {
                String cap = s.substring("Power Cap: ".length()).replace("%", "");
                try { return Integer.parseInt(cap); } catch (NumberFormatException e) { return 100; }
            }
        }
        return 100;
    }

    private void updateRodAnglerEnergy(ItemStack rod, int newVal) {
        ItemMeta meta = rod.getItemMeta();
        List<String> lore = meta.hasLore() ? new ArrayList<>(meta.getLore()) : new ArrayList<>();
        int cap = getRodPowerCap(rod);
        lore.removeIf(l -> ChatColor.stripColor(l).startsWith("Angler Energy: ") || (l.contains("|") && l.contains("[")));
        String line;
        if (cap > 100) {
            line = ChatColor.AQUA + "Angler Energy: " + ChatColor.YELLOW + newVal + "%" + ChatColor.GRAY + " / " + ChatColor.YELLOW + cap + "%";
        } else {
            line = ChatColor.AQUA + "Angler Energy: " + ChatColor.YELLOW + newVal + "%";
        }
        lore.add(0, createBar(newVal, cap));
        lore.add(0, line);
        meta.setLore(lore);
        rod.setItemMeta(meta);
    }

    private String createBar(int current, int cap) {
        int base = 20;
        // Scale bar length gradually for caps above 100%
        // Matches logic used in the Effigy and Gemstone GUIs
        int extra = (cap - 100) / 20;
        int len = base + extra;
        int filled = (int) ((double) current / cap * len);
        int empty = len - filled;
        StringBuilder sb = new StringBuilder();
        sb.append(ChatColor.DARK_GRAY).append("[");
        sb.append(ChatColor.BLUE);
        for (int i=0;i<filled;i++) sb.append("|");
        sb.append(ChatColor.GRAY);
        for (int i=0;i<empty;i++) sb.append("|");
        sb.append(ChatColor.DARK_GRAY).append("]");
        return sb.toString();
    }

    public static int getRodAnglerEnergyStatic(ItemStack rod) {
        if (rod == null) return 0;
        if (!rod.hasItemMeta() || !rod.getItemMeta().hasLore()) return 0;
        for (String line : rod.getItemMeta().getLore()) {
            String s = ChatColor.stripColor(line);
            if (s.startsWith("Angler Energy: ")) {
                String txt = s.replace("Angler Energy: ", "").replace("%", "");
                try { if (txt.contains(" / ")) txt = txt.split(" / ")[0]; return Integer.parseInt(txt); } catch (NumberFormatException e) { return 0; }
            }
        }
        return 0;
    }
}
