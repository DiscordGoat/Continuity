package goat.minecraft.minecraftnew.subsystems.forestry;

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

import java.util.ArrayList;
import java.util.List;

public class EntBarkSystem implements Listener {
    private final JavaPlugin plugin;

    public EntBarkSystem(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();

        ItemStack cursor = event.getCursor();
        ItemStack clicked = event.getCurrentItem();

        if (!isEntBark(cursor) || !isEligibleAxe(clicked)) return;

        event.setCancelled(true);
        applyEntBark(player, cursor, clicked);
    }

    private boolean isEntBark(ItemStack item) {
        if (item == null || item.getType() != Material.STRIPPED_OAK_WOOD) return false;
        if (!item.hasItemMeta() || !item.getItemMeta().hasDisplayName()) return false;
        return ChatColor.stripColor(item.getItemMeta().getDisplayName()).equals("Ent Bark");
    }

    private boolean isEligibleAxe(ItemStack item) {
        if (item == null) return false;
        Material type = item.getType();
        return type == Material.DIAMOND_AXE || type == Material.NETHERITE_AXE;
    }

    private void applyEntBark(Player player, ItemStack bark, ItemStack axe) {
        int currentCap = getCurrentCap(axe);
        if (currentCap >= 500) {
            player.sendMessage(ChatColor.RED + "This axe has already reached the maximum spirit cap of 500%!");
            return;
        }

        int newCap = Math.min(currentCap + 100, 500);
        setCap(axe, newCap);
        refreshBar(axe, newCap);

        if (bark.getAmount() > 1) {
            bark.setAmount(bark.getAmount() - 1);
        } else {
            player.setItemOnCursor(null);
        }

        player.sendMessage(ChatColor.GREEN + "Ent Bark applied! Spirit cap increased to " + newCap + "%");
        player.playSound(player.getLocation(), Sound.BLOCK_BEACON_POWER_SELECT, 1.0f, 1.5f);
    }

    private int getCurrentCap(ItemStack axe) {
        if (!axe.hasItemMeta() || !axe.getItemMeta().hasLore()) return 100;
        for (String line : axe.getItemMeta().getLore()) {
            String stripped = ChatColor.stripColor(line);
            if (stripped.startsWith("Spirit Cap: ")) {
                String capStr = stripped.substring("Spirit Cap: ".length()).replace("%", "");
                try { return Integer.parseInt(capStr); } catch (NumberFormatException e) { return 100; }
            }
        }
        return 100;
    }

    private void setCap(ItemStack axe, int cap) {
        ItemMeta meta = axe.getItemMeta();
        List<String> lore = meta.hasLore() ? new ArrayList<>(meta.getLore()) : new ArrayList<>();
        lore.removeIf(line -> ChatColor.stripColor(line).startsWith("Spirit Cap:"));
        int index = findInsertIndex(lore);
        lore.add(index, ChatColor.AQUA + "Spirit Cap: " + ChatColor.YELLOW + cap + "%");
        meta.setLore(lore);
        axe.setItemMeta(meta);
    }

    private int findInsertIndex(List<String> lore) {
        for (int i = 0; i < lore.size(); i++) {
            String line = lore.get(i);
            if (line.contains("[") && line.contains("|") && line.contains("]")) {
                return i + 1;
            }
        }
        return 0;
    }

    private void refreshBar(ItemStack axe, int cap) {
        if (!axe.hasItemMeta() || !axe.getItemMeta().hasLore()) return;
        ItemMeta meta = axe.getItemMeta();
        List<String> lore = new ArrayList<>(meta.getLore());
        int current = EffigyApplicationSystem.getAxeSpiritEnergy(axe);
        lore.removeIf(line -> {
            String stripped = ChatColor.stripColor(line);
            return stripped.startsWith("Spirit Energy: ") ||
                   (line.contains("[") && line.contains("|") && line.contains("]"));
        });
        String line = ChatColor.AQUA + "Spirit Energy: " + ChatColor.YELLOW + current + "%" +
                ChatColor.GRAY + " / " + ChatColor.YELLOW + cap + "%";
        String bar = createExtendedBar(current, cap);
        int insert = 0;
        while (insert < lore.size() && ChatColor.stripColor(lore.get(insert)).isEmpty()) {
            insert++;
        }
        lore.add(insert, "");
        lore.add(insert, bar);
        lore.add(insert, line);
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
}
