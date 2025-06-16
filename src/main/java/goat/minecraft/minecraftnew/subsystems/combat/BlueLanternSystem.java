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

import java.util.ArrayList;
import java.util.List;

public class BlueLanternSystem implements Listener {
    private final JavaPlugin plugin;

    public BlueLanternSystem(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();

        ItemStack cursor = event.getCursor();
        ItemStack clicked = event.getCurrentItem();

        if (isLantern(cursor) && isSoulWeapon(clicked)) {
            event.setCancelled(true);
            applyLantern(player, cursor, clicked);
        }
    }

    private boolean isLantern(ItemStack item) {
        if (item == null || item.getType() != Material.SOUL_TORCH) return false;
        if (!item.hasItemMeta() || !item.getItemMeta().hasDisplayName()) return false;
        return ChatColor.stripColor(item.getItemMeta().getDisplayName()).equals("Blue Lantern");
    }

    private boolean isSoulWeapon(ItemStack item) {
        if (item == null) return false;
        Material type = item.getType();
        return type.name().endsWith("_SWORD") || type == Material.BOW;
    }

    private void applyLantern(Player player, ItemStack lantern, ItemStack weapon) {
        int currentCap = getCurrentCap(weapon);
        if (currentCap >= 500) {
            player.sendMessage(ChatColor.RED + "This weapon has already reached the maximum soul cap of 500%!");
            return;
        }
        int newCap = Math.min(currentCap + 100, 500);
        setCap(weapon, newCap);
        refreshBar(weapon, newCap);

        if (lantern.getAmount() > 1) {
            lantern.setAmount(lantern.getAmount() - 1);
        } else {
            player.setItemOnCursor(null);
        }
        player.sendMessage(ChatColor.GREEN + "Blue Lantern applied! Soul cap increased to " + newCap + "%");
        player.playSound(player.getLocation(), Sound.BLOCK_BEACON_POWER_SELECT,1.0f,1.5f);
    }

    private int getCurrentCap(ItemStack weapon) {
        if (!weapon.hasItemMeta() || !weapon.getItemMeta().hasLore()) return 100;
        for (String line : weapon.getItemMeta().getLore()) {
            String stripped = ChatColor.stripColor(line);
            if (stripped.startsWith("Soul Cap: ")) {
                String capStr = stripped.substring("Soul Cap: ".length()).replace("%", "");
                try { return Integer.parseInt(capStr); } catch (NumberFormatException e) { return 100; }
            }
        }
        return 100;
    }

    private void setCap(ItemStack weapon, int cap) {
        ItemMeta meta = weapon.getItemMeta();
        List<String> lore = meta.hasLore() ? new ArrayList<>(meta.getLore()) : new ArrayList<>();
        lore.removeIf(line -> ChatColor.stripColor(line).startsWith("Soul Cap:"));
        int index = findInsertIndex(lore);
        lore.add(index, ChatColor.AQUA + "Soul Cap: " + ChatColor.YELLOW + cap + "%");
        meta.setLore(lore);
        weapon.setItemMeta(meta);
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

    private void refreshBar(ItemStack weapon, int cap) {
        if (!weapon.hasItemMeta() || !weapon.getItemMeta().hasLore()) return;
        ItemMeta meta = weapon.getItemMeta();
        List<String> lore = new ArrayList<>(meta.getLore());
        int current = SoulApplicationSystem.getWeaponSoulPower(weapon);
        lore.removeIf(line -> {
            String stripped = ChatColor.stripColor(line);
            return stripped.startsWith("Soul Power: ") || (line.contains("[") && line.contains("|") && line.contains("]"));
        });
        String line = ChatColor.AQUA + "Soul Power: " + ChatColor.YELLOW + current + "%" +
                ChatColor.GRAY + " / " + ChatColor.YELLOW + cap + "%";
        String bar = createExtendedBar(current, cap);
        int insert = 0;
        lore.add(insert, bar);
        lore.add(insert, line);
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
}
