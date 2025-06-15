package goat.minecraft.minecraftnew.subsystems.fishing;

import goat.minecraft.minecraftnew.MinecraftNew;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class PearlOfTheDeepSystem implements Listener {
    private final MinecraftNew plugin;

    public PearlOfTheDeepSystem(MinecraftNew plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();

        ItemStack cursor = event.getCursor();
        ItemStack clicked = event.getCurrentItem();

        if (isPearl(cursor) && isFishingRod(clicked)) {
            event.setCancelled(true);
            applyPearl(player, cursor, clicked);
        }
    }

    private boolean isPearl(ItemStack item) {
        if (item == null || item.getType() != Material.ENDER_PEARL) return false;
        if (!item.hasItemMeta() || !item.getItemMeta().hasDisplayName()) return false;
        String name = ChatColor.stripColor(item.getItemMeta().getDisplayName());
        return name.equals("Pearl of the Deep");
    }

    private boolean isFishingRod(ItemStack item) {
        return item != null && item.getType() == Material.FISHING_ROD;
    }

    private void applyPearl(Player player, ItemStack pearl, ItemStack rod) {
        int currentCap = getCurrentPowerCap(rod);
        if (currentCap >= 500) {
            player.sendMessage(ChatColor.RED + "This rod has already reached the maximum power cap of 500%!");
            return;
        }
        int newCap = Math.min(currentCap + 100, 500);
        setPowerCap(rod, newCap);
        refreshEnergyBar(rod, newCap);
        if (pearl.getAmount() > 1) {
            pearl.setAmount(pearl.getAmount() - 1);
        } else {
            player.setItemOnCursor(null);
        }
        player.sendMessage(ChatColor.GREEN + "Pearl applied! Power cap increased to " + newCap + "%");
        player.playSound(player.getLocation(), Sound.BLOCK_BEACON_POWER_SELECT,1.0f,1.5f);
    }

    private int getCurrentPowerCap(ItemStack rod) {
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

    private void setPowerCap(ItemStack rod, int cap) {
        ItemMeta meta = rod.getItemMeta();
        List<String> lore = meta.hasLore() ? new ArrayList<>(meta.getLore()) : new ArrayList<>();
        lore.removeIf(l -> ChatColor.stripColor(l).startsWith("Power Cap:"));
        int insert = findInsert(lore);
        lore.add(insert, ChatColor.AQUA + "Power Cap: " + ChatColor.YELLOW + cap + "%");
        meta.setLore(lore);
        rod.setItemMeta(meta);
    }

    private int findInsert(List<String> lore) {
        for (int i=0;i<lore.size();i++) {
            String line = lore.get(i);
            if (line.contains("[") && line.contains("|") && line.contains("]")) {
                return i + 1;
            }
        }
        return 0;
    }

    private void refreshEnergyBar(ItemStack rod, int newCap) {
        if (!rod.hasItemMeta() || !rod.getItemMeta().hasLore()) return;
        ItemMeta meta = rod.getItemMeta();
        List<String> lore = new ArrayList<>(meta.getLore());
        int current = BaitApplicationSystem.getRodAnglerEnergyStatic(rod);
        lore.removeIf(l -> ChatColor.stripColor(l).startsWith("Angler Energy: ") || (l.contains("|") && l.contains("[")));
        String line = ChatColor.AQUA + "Angler Energy: " + ChatColor.YELLOW + current + "%" +
                ChatColor.GRAY + " / " + ChatColor.YELLOW + newCap + "%";
        String bar = createBar(current, newCap);
        int insert = 0;
        lore.add(insert, bar);
        lore.add(insert, line);
        meta.setLore(lore);
        rod.setItemMeta(meta);
    }

    private String createBar(int current, int cap) {
        int base = 20;
        // Use the same scaling approach as other upgrade GUIs
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

    public static ItemStack createPearl() {
        ItemStack item = new ItemStack(Material.ENDER_PEARL);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.AQUA + "Pearl of the Deep");
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + "A mystical pearl that expands the");
        lore.add(ChatColor.GRAY + "angler energy capacity of fishing rods");
        lore.add("");
        lore.add(ChatColor.YELLOW + "Effect: " + ChatColor.WHITE + "+100% Power Cap");
        lore.add(ChatColor.YELLOW + "Maximum: " + ChatColor.WHITE + "500% Total Cap");
        lore.add("");
        lore.add(ChatColor.DARK_PURPLE + "Drag onto fishing rods to apply");
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }
}
