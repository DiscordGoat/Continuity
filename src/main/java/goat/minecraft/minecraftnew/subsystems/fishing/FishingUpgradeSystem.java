package goat.minecraft.minecraftnew.subsystems.fishing;

import goat.minecraft.minecraftnew.MinecraftNew;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

/**
 * Upgrade system for fishing rods using Angler Energy.
 * Mirrors the gemstone/effigy upgrade GUIs.
 */
public class FishingUpgradeSystem implements Listener {
    private final MinecraftNew plugin;

    /**
     * Enumeration of all fishing upgrades and their GUI properties.
     */
    public enum UpgradeType {
        // Row 1: Catch Upgrades
        FINDING_NEMO("Finding Nemo", "+15% chance to gain +1 tropical fish per non-sea creature reel in", Material.TROPICAL_FISH, 3, 2),
        TREASURE_HUNTER("Treasure Hunter", "+1% treasure chance per level", Material.CHEST, 5, 3),
        SONAR("Sonar", "+1% sea creature chance per level", Material.NAUTILUS_SHELL, 5, 4),
        KRAKEN("Kraken", "5% chance to reel in 2 sea creatures", Material.INK_SAC, 3, 5),
        BIGGER_FISH("Bigger Fish", "-10% sea creature level", Material.PUFFERFISH, 5, 6),
        DIAMOND_HOOK("Diamond Hook", "Instantly kill sea creatures on reel", Material.DIAMOND, 1, 7),

        // Row 2: Luck Upgrades
        CHARMED("Charmed", "+15% chance to gain luck", Material.ENCHANTED_BOOK, 3, 11),
        RABBITS_FOOT("Rabbit's Foot", "+1 potency of luck", Material.RABBIT_FOOT, 3, 12),
        GOOD_DAY("Good Day", "+15 seconds of luck", Material.CLOCK, 3, 13),

        // Row 3: Misc Upgrades
        RAIN_DANCE("Rain Dance", "If raining, 15% chance reel-ins add 10 seconds of rain", Material.WATER_BUCKET, 5, 20),
        PAYOUT("Payout", "Reel-ins sell common fish for emeralds", Material.EMERALD, 4, 21),
        PASSION("Passion", "15% chance health is set to max on reel-in", Material.GOLDEN_APPLE, 3, 22),
        FEED("Feed", "15% chance to feed on reel-in", Material.COOKED_SALMON, 3, 23);

        private final String displayName;
        private final String description;
        private final Material icon;
        private final int maxLevel;
        private final int slot;

        UpgradeType(String displayName, String description, Material icon, int maxLevel, int slot) {
            this.displayName = displayName;
            this.description = description;
            this.icon = icon;
            this.maxLevel = maxLevel;
            this.slot = slot;
        }

        public String getDisplayName() { return displayName; }
        public String getDescription() { return description; }
        public Material getIcon() { return icon; }
        public int getMaxLevel() { return maxLevel; }
        public int getSlot() { return slot; }
    }

    public FishingUpgradeSystem(MinecraftNew plugin) {
        this.plugin = plugin;
    }

    /** Opens the upgrade GUI if the rod has any Angler Energy. */
    public void openUpgradeGUI(Player player, ItemStack rod) {
        int totalEnergy = getTotalEnergy(rod);
        if (totalEnergy == 0) {
            player.sendMessage(ChatColor.RED + "This rod has no Angler Energy!");
            return;
        }

        Inventory gui = Bukkit.createInventory(new FishingUpgradeInventoryHolder(), 54,
                ChatColor.AQUA + "🎣 Angler Upgrades");

        for (int i = 0; i < 54; i++) gui.setItem(i, createFiller());
        int available = calculateAvailableEnergy(rod);

        // Row 1 header
        gui.setItem(0, createHeader(Material.FISHING_ROD, ChatColor.GREEN + "🎣 Catch"));
        gui.setItem(1, createColoredPane(Material.GREEN_STAINED_GLASS_PANE, ""));
        gui.setItem(UpgradeType.FINDING_NEMO.getSlot(), createUpgradeItem(UpgradeType.FINDING_NEMO, rod, getUpgradeCost(UpgradeType.FINDING_NEMO), available));
        gui.setItem(UpgradeType.TREASURE_HUNTER.getSlot(), createUpgradeItem(UpgradeType.TREASURE_HUNTER, rod, getUpgradeCost(UpgradeType.TREASURE_HUNTER), available));
        gui.setItem(UpgradeType.SONAR.getSlot(), createUpgradeItem(UpgradeType.SONAR, rod, getUpgradeCost(UpgradeType.SONAR), available));
        gui.setItem(UpgradeType.KRAKEN.getSlot(), createUpgradeItem(UpgradeType.KRAKEN, rod, getUpgradeCost(UpgradeType.KRAKEN), available));
        gui.setItem(UpgradeType.BIGGER_FISH.getSlot(), createUpgradeItem(UpgradeType.BIGGER_FISH, rod, getUpgradeCost(UpgradeType.BIGGER_FISH), available));
        gui.setItem(UpgradeType.DIAMOND_HOOK.getSlot(), createUpgradeItem(UpgradeType.DIAMOND_HOOK, rod, getUpgradeCost(UpgradeType.DIAMOND_HOOK), available));

        // Row 2 header
        gui.setItem(9, createHeader(Material.RABBIT_FOOT, ChatColor.LIGHT_PURPLE + "✦ Luck"));
        gui.setItem(10, createColoredPane(Material.PURPLE_STAINED_GLASS_PANE, ""));
        gui.setItem(UpgradeType.CHARMED.getSlot(), createUpgradeItem(UpgradeType.CHARMED, rod, getUpgradeCost(UpgradeType.CHARMED), available));
        gui.setItem(UpgradeType.RABBITS_FOOT.getSlot(), createUpgradeItem(UpgradeType.RABBITS_FOOT, rod, getUpgradeCost(UpgradeType.RABBITS_FOOT), available));
        gui.setItem(UpgradeType.GOOD_DAY.getSlot(), createUpgradeItem(UpgradeType.GOOD_DAY, rod, getUpgradeCost(UpgradeType.GOOD_DAY), available));

        // Row 3 header
        gui.setItem(18, createHeader(Material.WATER_BUCKET, ChatColor.GOLD + "✨ Misc"));
        gui.setItem(19, createColoredPane(Material.YELLOW_STAINED_GLASS_PANE, ""));
        gui.setItem(UpgradeType.RAIN_DANCE.getSlot(), createUpgradeItem(UpgradeType.RAIN_DANCE, rod, getUpgradeCost(UpgradeType.RAIN_DANCE), available));
        gui.setItem(UpgradeType.PAYOUT.getSlot(), createUpgradeItem(UpgradeType.PAYOUT, rod, getUpgradeCost(UpgradeType.PAYOUT), available));
        gui.setItem(UpgradeType.PASSION.getSlot(), createUpgradeItem(UpgradeType.PASSION, rod, getUpgradeCost(UpgradeType.PASSION), available));
        gui.setItem(UpgradeType.FEED.getSlot(), createUpgradeItem(UpgradeType.FEED, rod, getUpgradeCost(UpgradeType.FEED), available));

        gui.setItem(49, createEnergyDisplay(totalEnergy, getEnergyCap(rod), available));

        ItemStack respec = new ItemStack(Material.BARRIER);
        ItemMeta rMeta = respec.getItemMeta();
        rMeta.setDisplayName(ChatColor.RED + "⚠ Reset Upgrades");
        List<String> lore = new ArrayList<>();
        int spent = totalEnergy - available;
        lore.add(ChatColor.GRAY + "Damages tool by " + ChatColor.RED + "20% durability");
        lore.add(ChatColor.GRAY + "Returns all allocated energy");
        lore.add("");
        if (spent > 0) {
            lore.add(ChatColor.GRAY + "Will refund: " + ChatColor.GREEN + spent + "% energy");
            lore.add(ChatColor.YELLOW + "Shift+Right-click to confirm");
        } else {
            lore.add(ChatColor.DARK_GRAY + "No upgrades to reset");
        }
        rMeta.setLore(lore);
        respec.setItemMeta(rMeta);
        gui.setItem(53, respec);

        player.openInventory(gui);
    }

    private ItemStack createHeader(Material m, String name) {
        ItemStack item = new ItemStack(m);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createColoredPane(Material material, String name) {
        ItemStack pane = new ItemStack(material);
        ItemMeta meta = pane.getItemMeta();
        meta.setDisplayName(ChatColor.BLACK + name);
        pane.setItemMeta(meta);
        return pane;
    }

    private ItemStack createFiller() {
        ItemStack filler = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = filler.getItemMeta();
        meta.setDisplayName(ChatColor.BLACK + "");
        filler.setItemMeta(meta);
        return filler;
    }

    private ItemStack createUpgradeItem(UpgradeType up, ItemStack rod, int cost, int available) {
        ItemStack item = new ItemStack(up.getIcon());
        ItemMeta meta = item.getItemMeta();
        int level = getUpgradeLevel(rod, up);
        boolean max = level >= up.getMaxLevel();
        boolean afford = available >= cost;

        String name = (max ? ChatColor.GOLD : (afford ? ChatColor.GREEN : ChatColor.RED)) +
                up.getDisplayName() + ChatColor.GRAY + " (" + level + "/" + up.getMaxLevel() + ")";
        meta.setDisplayName(name);

        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + up.getDescription());
        if (!max) {
            lore.add(ChatColor.GRAY + "Cost: " + cost + "% energy");
            lore.add(afford ? ChatColor.GREEN + "Click to upgrade" : ChatColor.RED + "Not enough energy");
        } else {
            lore.add(ChatColor.GOLD + "Max level reached");
        }
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof FishingUpgradeInventoryHolder)) return;
        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        ItemStack rod = player.getInventory().getItemInMainHand();
        if (rod == null || rod.getType() != Material.FISHING_ROD) return;

        if (event.getSlot() == 53 && event.isShiftClick() && event.isRightClick()) {
            handleRespec(player, rod);
            return;
        }

        for (UpgradeType type : UpgradeType.values()) {
            if (type.getSlot() == event.getSlot()) {
                handlePurchase(player, rod, type);
                break;
            }
        }
    }

    private void handlePurchase(Player player, ItemStack rod, UpgradeType type) {
        int available = calculateAvailableEnergy(rod);
        int cost = getUpgradeCost(type);
        int level = getUpgradeLevel(rod, type);
        if (level >= type.getMaxLevel()) {
            player.sendMessage(ChatColor.RED + "Upgrade at max level");
            return;
        }
        if (available < cost) {
            player.sendMessage(ChatColor.RED + "Not enough Angler Energy");
            return;
        }
        setUpgradeLevel(rod, type, level + 1);
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
        player.closeInventory();
        openUpgradeGUI(player, rod);
    }

    private void handleRespec(Player player, ItemStack rod) {
        int currentDurability = rod.getDurability();
        int maxDurability = rod.getType().getMaxDurability();
        int damageToAdd = (int) Math.ceil(maxDurability * 0.2);
        if (currentDurability + damageToAdd >= maxDurability) {
            player.sendMessage(ChatColor.RED + "Tool would break from respec damage! Repair it first.");
            return;
        }
        clearAllUpgrades(rod);
        rod.setDurability((short) (currentDurability + damageToAdd));
        player.sendMessage(ChatColor.YELLOW + "Tool respecced! All upgrades reset.");
        player.sendMessage(ChatColor.RED + "Tool took " + damageToAdd + " durability damage.");
        player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 1.0f, 1.0f);
        player.closeInventory();
    }

    private int getTotalEnergy(ItemStack rod) {
        return BaitApplicationSystem.getRodAnglerEnergyStatic(rod);
    }

    private int calculateAvailableEnergy(ItemStack rod) {
        int spent = 0;
        for (UpgradeType t : UpgradeType.values()) {
            spent += getUpgradeLevel(rod, t) * getUpgradeCost(t);
        }
        return getTotalEnergy(rod) - spent;
    }

    public static int getUpgradeLevel(ItemStack rod, UpgradeType type) {
        if (!rod.hasItemMeta() || !rod.getItemMeta().hasLore()) return 0;
        for (String line : rod.getItemMeta().getLore()) {
            String stripped = ChatColor.stripColor(line);
            if (stripped.startsWith("Fishing Upgrades:")) {
                return parseLevel(line, type);
            }
        }
        return 0;
    }

    private static int parseLevel(String line, UpgradeType type) {
        String symbol = getSymbol(type);
        String stripped = ChatColor.stripColor(line);
        int idx = stripped.indexOf(symbol);
        if (idx == -1) return 0;
        String after = stripped.substring(idx + symbol.length());
        if (after.startsWith("ⱽᴵ")) return 6;
        if (after.startsWith("ⱽ")) return 5;
        if (after.startsWith("ᴵⱽ")) return 4;
        if (after.startsWith("ᴵᴵᴵ")) return 3;
        if (after.startsWith("ᴵᴵ")) return 2;
        if (after.startsWith("ᴵ")) return 1;
        return 0;
    }

    private void setUpgradeLevel(ItemStack rod, UpgradeType type, int level) {
        ItemMeta meta = rod.getItemMeta();
        List<String> lore = meta.hasLore() ? new ArrayList<>(meta.getLore()) : new ArrayList<>();
        int lineIndex = -1;
        for (int i = 0; i < lore.size(); i++) {
            if (ChatColor.stripColor(lore.get(i)).startsWith("Fishing Upgrades:")) {
                lineIndex = i; break; }
        }
        Map<UpgradeType, Integer> levels = new LinkedHashMap<>();
        if (lineIndex >= 0) {
            for (UpgradeType t : UpgradeType.values()) {
                int lvl = parseLevel(lore.get(lineIndex), t);
                if (lvl > 0) levels.put(t, lvl);
            }
            lore.remove(lineIndex);
        }
        if (level > 0) levels.put(type, level); else levels.remove(type);
        if (!levels.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            sb.append(ChatColor.GRAY).append("Fishing Upgrades: ");
            boolean first = true;
            for (Map.Entry<UpgradeType, Integer> e : levels.entrySet()) {
                if (!first) sb.append(" ");
                sb.append(getColoredSymbol(e.getKey(), e.getValue()));
                first = false;
            }
            if (lineIndex < 0) lineIndex = lore.size();
            lore.add(lineIndex, sb.toString());
        }
        meta.setLore(lore);
        rod.setItemMeta(meta);
    }

    private void clearAllUpgrades(ItemStack rod) {
        if (!rod.hasItemMeta()) return;
        ItemMeta meta = rod.getItemMeta();
        List<String> lore = meta.getLore();
        if (lore == null) return;
        lore.removeIf(l -> ChatColor.stripColor(l).startsWith("Fishing Upgrades:"));
        meta.setLore(lore);
        rod.setItemMeta(meta);
    }

    private ItemStack createEnergyDisplay(int total, int cap, int available) {
        ItemStack item = new ItemStack(Material.PRISMARINE_CRYSTALS);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.AQUA + "Angler Energy");

        int spent = total - available;
        String bar = createBar(total, cap, available);

        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + "Total Energy: " + ChatColor.WHITE + total + "%" +
                ChatColor.GRAY + " / " + ChatColor.YELLOW + cap + "%");
        lore.add(ChatColor.GRAY + "Available: " + ChatColor.GREEN + available + "% " +
                ChatColor.GRAY + "Spent: " + ChatColor.RED + spent + "%");
        lore.add("");
        lore.add(bar);
        lore.add("");

        if (cap > 100) {
            lore.add(ChatColor.AQUA + "Enhanced Power Cap: " + ChatColor.YELLOW + cap + "%");
            lore.add(ChatColor.GRAY + "Apply " + ChatColor.LIGHT_PURPLE + "Pearls of the Deep" +
                    ChatColor.GRAY + " to increase cap");
        } else {
            lore.add(ChatColor.GRAY + "Apply " + ChatColor.LIGHT_PURPLE + "Pearls of the Deep" +
                    ChatColor.GRAY + " to increase cap beyond 100%");
        }
        lore.add(ChatColor.GRAY + "Apply bait to increase current energy");

        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    private String createBar(int total, int cap, int available) {
        // Use the same bar scaling logic as the gemstone upgrade GUI
        int base = 20;
        int extraSegments = (cap - 100) / 100; // each +100% adds 5 bars
        int len = base + extraSegments * 5;

        int filled = (int)((double) total / cap * len);
        int spent = (int)((double)(total - available) / cap * len);

        StringBuilder bar = new StringBuilder(ChatColor.DARK_GRAY + "[");
        for (int i = 0; i < spent; i++) bar.append(ChatColor.RED + "|");
        for (int i = spent; i < filled; i++) bar.append(ChatColor.GREEN + "|");
        for (int i = filled; i < len; i++) bar.append(ChatColor.GRAY + "|");
        bar.append(ChatColor.DARK_GRAY + "]");
        return bar.toString();
    }

    private int getEnergyCap(ItemStack rod) {
        if (!rod.hasItemMeta() || !rod.getItemMeta().hasLore()) return 100;
        for (String line : rod.getItemMeta().getLore()) {
            String s = ChatColor.stripColor(line);
            if (s.startsWith("Power Cap: ")) {
                try { return Integer.parseInt(s.substring(10).replace("%", "")); }
                catch (NumberFormatException ignored) {}
            }
        }
        return 100;
    }

    private int getUpgradeCost(UpgradeType type) {
        if (type == UpgradeType.DIAMOND_HOOK) {
            return 50;
        }
        return 8;
    }

    private static String getSymbol(UpgradeType t) {
        switch (t) {
            case FINDING_NEMO: return "🐠";
            case TREASURE_HUNTER: return "💰";
            case SONAR: return "📡";
            case CHARMED: return "✨";
            case RABBITS_FOOT: return "🐇";
            case GOOD_DAY: return "⏰";
            case RAIN_DANCE: return "🌧";
            case PAYOUT: return "💵";
            case PASSION: return "❤️";
            case FEED: return "🍖";
            case KRAKEN: return "🐙";
            case BIGGER_FISH: return "⬇";
            case DIAMOND_HOOK: return "🪝";
            default: return "⬡";
        }
    }

    private String getColoredSymbol(UpgradeType t, int level) {
        ChatColor color = ChatColor.WHITE;
        if (level >= 6) color = ChatColor.DARK_RED;
        else if (level >= 5) color = ChatColor.GOLD;
        else if (level >= 4) color = ChatColor.LIGHT_PURPLE;
        else if (level >= 3) color = ChatColor.AQUA;
        else if (level >= 2) color = ChatColor.GREEN;
        return color + getSymbol(t) + getLevelIndicator(level);
    }

    private String getLevelIndicator(int level) {
        switch (level) {
            case 1: return "ᴵ";
            case 2: return "ᴵᴵ";
            case 3: return "ᴵᴵᴵ";
            case 4: return "ᴵⱽ";
            case 5: return "ⱽ";
            case 6: return "ⱽᴵ";
            default: return String.valueOf(level);
        }
    }

    private static class FishingUpgradeInventoryHolder implements org.bukkit.inventory.InventoryHolder {
        @Override
        public Inventory getInventory() { return null; }
    }
}
