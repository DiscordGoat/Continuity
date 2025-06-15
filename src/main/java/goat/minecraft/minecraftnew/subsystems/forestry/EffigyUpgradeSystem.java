package goat.minecraft.minecraftnew.subsystems.forestry;

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
 * Upgrade system for Spirit Energy axes. Mimics the Gemstone upgrade GUI
 * but uses Spirit Energy obtained from effigies.
 */
public class EffigyUpgradeSystem implements Listener {
    private final MinecraftNew plugin;

    /**
     * Upgrade definitions. Each upgrade has a name, lore description,
     * icon material, maximum level and GUI slot.
     */
    public enum UpgradeType {
        OAK_YIELD("Oak Yield", "Bonus drops from oak logs", Material.OAK_LOG, 5, 2),
        SPRUCE_YIELD("Spruce Yield", "Bonus drops from spruce logs", Material.SPRUCE_LOG, 5, 3),
        BIRCH_YIELD("Birch Yield", "Bonus drops from birch logs", Material.BIRCH_LOG, 5, 4),
        JUNGLE_YIELD("Jungle Yield", "Bonus drops from jungle logs", Material.JUNGLE_LOG, 5, 5),
        ACACIA_YIELD("Acacia Yield", "Bonus drops from acacia logs", Material.ACACIA_LOG, 5, 6),
        DARK_OAK_YIELD("Dark Oak Yield", "Bonus drops from dark oak logs", Material.DARK_OAK_LOG, 5, 7),
        CRIMSON_YIELD("Crimson Yield", "Bonus drops from crimson stems", Material.CRIMSON_STEM, 5, 8),
        WARPED_YIELD("Warped Yield", "Bonus drops from warped stems", Material.WARPED_STEM, 5, 11),

        EFFIGY_YIELD("Effigy Yield", "+0.5% spirit chance per level", Material.TOTEM_OF_UNDYING, 6, 20),
        FORESTRY_XP("Forestry XP Boost", "More forestry XP", Material.EXPERIENCE_BOTTLE, 3, 21),
        FAKE_NEWS("Fake News", "Chance to reduce notoriety", Material.PAPER, 5, 22),
        FEED("Feed", "Chance to restore hunger when chopping", Material.BREAD, 3, 23),
        PAYOUT("Payout", "Sells stacks of logs for emeralds", Material.EMERALD, 4, 24),
        ORCHARD("Orchard", "Higher perfect apple droprate", Material.APPLE, 4, 29),
        GOLDEN_APPLE("Golden Apple", "Chance to drop enchanted apple", Material.ENCHANTED_GOLDEN_APPLE, 3, 30),
        TRESPASSER("Trespasser", "+3 notoriety per level", Material.BARRIER, 6, 31),
        HEADHUNTER("Headhunter", "+10% damage to forest spirits", Material.IRON_AXE, 5, 32),
        SPECTRAL_ARMOR("Spectral Armor", "Damage reduction from spirits", Material.CHAINMAIL_CHESTPLATE, 5, 33),
        ANCIENT_CONFUSION("Ancient Confusion", "Lowers spirit level", Material.FERMENTED_SPIDER_EYE, 4, 34);

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

    public EffigyUpgradeSystem(MinecraftNew plugin) {
        this.plugin = plugin;
    }

    /**
     * Opens the upgrade GUI if the held axe has any Spirit Energy.
     */
    public void openUpgradeGUI(Player player, ItemStack axe) {
        int totalEnergy = getTotalEnergy(axe);
        if (totalEnergy == 0) {
            player.sendMessage(ChatColor.RED + "This axe has no Spirit Energy!");
            return;
        }

        Inventory gui = Bukkit.createInventory(new EffigyUpgradeInventoryHolder(), 54,
                ChatColor.DARK_GREEN + "⚒ Effigy Upgrades");

        // Fill with glass background
        for (int i = 0; i < 54; i++) gui.setItem(i, createFiller());

        int available = calculateAvailableEnergy(axe);

        // Yield upgrades row
        gui.setItem(0, createHeader(Material.OAK_SAPLING, ChatColor.GREEN + "🌲 Log Yields"));
        for (UpgradeType t : new UpgradeType[]{UpgradeType.OAK_YIELD, UpgradeType.SPRUCE_YIELD,
                UpgradeType.BIRCH_YIELD, UpgradeType.JUNGLE_YIELD,
                UpgradeType.ACACIA_YIELD, UpgradeType.DARK_OAK_YIELD,
                UpgradeType.CRIMSON_YIELD, UpgradeType.WARPED_YIELD}) {
            gui.setItem(t.getSlot(), createUpgradeItem(t, axe, getUpgradeCost(t), available));
        }

        // Utility upgrades
        gui.setItem(18, createHeader(Material.NETHER_STAR, ChatColor.LIGHT_PURPLE + "✦ Utilities"));
        for (UpgradeType t : Arrays.asList(UpgradeType.EFFIGY_YIELD, UpgradeType.FORESTRY_XP,
                UpgradeType.FAKE_NEWS, UpgradeType.FEED, UpgradeType.PAYOUT)) {
            gui.setItem(t.getSlot(), createUpgradeItem(t, axe, getUpgradeCost(t), available));
        }

        // Misc upgrades
        gui.setItem(27, createHeader(Material.GOLDEN_AXE, ChatColor.GOLD + "✨ Special"));
        for (UpgradeType t : Arrays.asList(UpgradeType.ORCHARD, UpgradeType.GOLDEN_APPLE,
                UpgradeType.TRESPASSER, UpgradeType.HEADHUNTER,
                UpgradeType.SPECTRAL_ARMOR, UpgradeType.ANCIENT_CONFUSION)) {
            gui.setItem(t.getSlot(), createUpgradeItem(t, axe, getUpgradeCost(t), available));
        }

        gui.setItem(49, createExtendedEnergyDisplay(totalEnergy, getEnergyCap(axe), available));

        // Add respec button similar to Gemstone upgrade GUI
        ItemStack respecItem = new ItemStack(Material.BARRIER);
        ItemMeta respecMeta = respecItem.getItemMeta();
        respecMeta.setDisplayName(ChatColor.RED + "⚠ Reset Upgrades");

        int spentEnergy = totalEnergy - available;
        List<String> respecLore = new ArrayList<>();
        respecLore.add(ChatColor.GRAY + "Damages tool by " + ChatColor.RED + "20% durability");
        respecLore.add(ChatColor.GRAY + "Returns all allocated energy");
        respecLore.add("");

        if (spentEnergy > 0) {
            respecLore.add(ChatColor.GRAY + "Will refund: " + ChatColor.GREEN + spentEnergy + "% energy");
            respecLore.add(ChatColor.YELLOW + "Shift+Right-click to confirm");
        } else {
            respecLore.add(ChatColor.DARK_GRAY + "No upgrades to reset");
        }

        respecMeta.setLore(respecLore);
        respecItem.setItemMeta(respecMeta);
        gui.setItem(53, respecItem);

        player.openInventory(gui);
    }

    private ItemStack createHeader(Material m, String name) {
        ItemStack item = new ItemStack(m);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createFiller() {
        ItemStack filler = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = filler.getItemMeta();
        meta.setDisplayName(ChatColor.BLACK + "");
        filler.setItemMeta(meta);
        return filler;
    }

    private ItemStack createUpgradeItem(UpgradeType up, ItemStack axe, int cost, int available) {
        ItemStack item = new ItemStack(up.getIcon());
        ItemMeta meta = item.getItemMeta();
        int level = getUpgradeLevel(axe, up);
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
        if (!(event.getInventory().getHolder() instanceof EffigyUpgradeInventoryHolder)) return;
        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        ItemStack axe = player.getInventory().getItemInMainHand();
        if (axe == null || !(axe.getType().name().endsWith("AXE"))) return;

        // Handle respec via dedicated button
        if (event.getSlot() == 53 && event.isShiftClick() && event.isRightClick()) {
            handleRespec(player, axe);
            return;
        }

        // Legacy respec via energy display
        if (event.getSlot() == 49 && event.isShiftClick() && event.isRightClick()) {
            handleRespec(player, axe);
            return;
        }

        for (UpgradeType type : UpgradeType.values()) {
            if (type.getSlot() == event.getSlot()) {
                handlePurchase(player, axe, type);
                break;
            }
        }
    }

    private void handlePurchase(Player player, ItemStack axe, UpgradeType type) {
        int available = calculateAvailableEnergy(axe);
        int cost = getUpgradeCost(type);
        int level = getUpgradeLevel(axe, type);
        if (level >= type.getMaxLevel()) {
            player.sendMessage(ChatColor.RED + "Upgrade at max level");
            return;
        }
        if (available < cost) {
            player.sendMessage(ChatColor.RED + "Not enough Spirit Energy");
            return;
        }
        setUpgradeLevel(axe, type, level + 1);
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
        player.closeInventory();
        openUpgradeGUI(player, axe);
    }

    private void handleRespec(Player player, ItemStack axe) {
        int currentDurability = axe.getDurability();
        int maxDurability = axe.getType().getMaxDurability();
        int damageToAdd = (int) Math.ceil(maxDurability * 0.2); // 20% durability damage

        if (currentDurability + damageToAdd >= maxDurability) {
            player.sendMessage(ChatColor.RED + "Tool would break from respec damage! Repair it first.");
            return;
        }

        clearAllUpgrades(axe);
        axe.setDurability((short) (currentDurability + damageToAdd));

        player.sendMessage(ChatColor.YELLOW + "Tool respecced! All upgrades reset.");
        player.sendMessage(ChatColor.RED + "Tool took " + damageToAdd + " durability damage.");
        player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 1.0f, 1.0f);

        player.closeInventory();
    }

    private int getTotalEnergy(ItemStack axe) {
        return EffigyApplicationSystem.getAxeSpiritEnergy(axe);
    }

    private int calculateAvailableEnergy(ItemStack axe) {
        int spent = 0;
        for (UpgradeType t : UpgradeType.values()) {
            spent += getUpgradeLevel(axe, t) * getUpgradeCost(t);
        }
        return getTotalEnergy(axe) - spent;
    }

    private int getUpgradeCost(UpgradeType type) {
        switch (type) {
            case ANCIENT_CONFUSION:
            case FEED:
            case OAK_YIELD:
            case SPRUCE_YIELD:
            case BIRCH_YIELD:
            case JUNGLE_YIELD:
            case ACACIA_YIELD:
            case DARK_OAK_YIELD:
            case CRIMSON_YIELD:
            case WARPED_YIELD:
                return 4;
            default:
                return 8;
        }
    }

    public static int getUpgradeLevel(ItemStack axe, UpgradeType type) {
        if (!axe.hasItemMeta() || !axe.getItemMeta().hasLore()) return 0;
        for (String line : axe.getItemMeta().getLore()) {
            String stripped = ChatColor.stripColor(line);
            if (stripped.startsWith("Effigy Upgrades:")) {
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

    private void setUpgradeLevel(ItemStack axe, UpgradeType type, int level) {
        ItemMeta meta = axe.getItemMeta();
        List<String> lore = meta.hasLore() ? new ArrayList<>(meta.getLore()) : new ArrayList<>();
        int lineIndex = -1;
        for (int i = 0; i < lore.size(); i++) {
            if (ChatColor.stripColor(lore.get(i)).startsWith("Effigy Upgrades:")) {
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
            sb.append(ChatColor.GRAY).append("Effigy Upgrades: ");
            boolean first = true;
            for (Map.Entry<UpgradeType, Integer> e : levels.entrySet()) {
                if (!first) sb.append(" ");
                sb.append(getColoredSymbol(e.getKey(), e.getValue()));
                first = false;
            }
            if (lineIndex < 0) lineIndex = findUpgradeInsertIndex(lore);
            lore.add(lineIndex, sb.toString());
        }
        meta.setLore(lore);
        axe.setItemMeta(meta);
    }

    /**
     * Finds the index immediately after the Spirit Cap line to insert upgrades.
     * If no cap line exists, appends to the end.
     */
    private int findUpgradeInsertIndex(List<String> lore) {
        for (int i = 0; i < lore.size(); i++) {
            String stripped = ChatColor.stripColor(lore.get(i));
            if (stripped.startsWith("Spirit Cap:")) {
                return i + 1;
            }
        }
        return lore.size();
    }

    private void clearAllUpgrades(ItemStack axe) {
        if (!axe.hasItemMeta()) return;
        ItemMeta meta = axe.getItemMeta();
        List<String> lore = meta.getLore();
        if (lore == null) return;
        lore.removeIf(l -> ChatColor.stripColor(l).startsWith("Effigy Upgrades:"));
        meta.setLore(lore);
        axe.setItemMeta(meta);
    }

    private ItemStack createEnergyDisplay(int total, int cap, int available) {
        ItemStack item = new ItemStack(Material.SOUL_TORCH);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.AQUA + "Spirit Energy");
        String bar = createBar(total, cap, available);
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + "Total: " + ChatColor.YELLOW + total + "%" + ChatColor.GRAY +
                " / " + ChatColor.YELLOW + cap + "%");
        lore.add(ChatColor.GRAY + "Available: " + ChatColor.GREEN + available + "%");
        lore.add(bar);
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    /**
     * Creates an extended energy display supporting caps beyond 100%.
     * Mirrors the implementation used in the gemstone and fishing upgrade systems.
     */
    private ItemStack createExtendedEnergyDisplay(int total, int cap, int available) {
        ItemStack item = new ItemStack(Material.SOUL_TORCH);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.AQUA + "Spirit Energy");

        int baseBarLength = 20;
        int extraSegments = (cap - 100) / 100; // each +100% adds 5 bars
        int totalBarLength = baseBarLength + extraSegments * 5;

        int filled = (int) ((double) total / cap * totalBarLength);
        int spent = (int) ((double) (total - available) / cap * totalBarLength);

        StringBuilder bar = new StringBuilder();
        bar.append(ChatColor.DARK_GRAY + "[");
        for (int i = 0; i < spent; i++) bar.append(ChatColor.RED + "|");
        for (int i = spent; i < filled; i++) bar.append(ChatColor.GREEN + "|");
        for (int i = filled; i < totalBarLength; i++) bar.append(ChatColor.GRAY + "|");
        bar.append(ChatColor.DARK_GRAY + "]");

        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + "Total Energy: " + ChatColor.WHITE + total + "%" +
                ChatColor.GRAY + " / " + ChatColor.YELLOW + cap + "%");
        lore.add(ChatColor.GRAY + "Available: " + ChatColor.GREEN + available + "% " +
                ChatColor.GRAY + "Spent: " + ChatColor.RED + (total - available) + "%");
        lore.add("");
        lore.add(bar.toString());
        lore.add("");

        if (cap > 100) {
            lore.add(ChatColor.AQUA + "Enhanced Power Cap: " + ChatColor.YELLOW + cap + "%");
            lore.add(ChatColor.GRAY + "Apply " + ChatColor.LIGHT_PURPLE + "Ent Bark" + ChatColor.GRAY + " to increase cap");
        } else {
            lore.add(ChatColor.GRAY + "Apply " + ChatColor.LIGHT_PURPLE + "Ent Bark" + ChatColor.GRAY + " to increase cap beyond 100%");
        }
        lore.add(ChatColor.GRAY + "Apply effigies to increase current energy");

        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    private String createBar(int total, int cap, int available) {
        int len = 20 + (cap - 100) / 20;
        int filled = (int)((double)total / cap * len);
        int spent = (int)((double)(total - available) / cap * len);
        StringBuilder b = new StringBuilder(ChatColor.DARK_GRAY + "[");
        for (int i = 0; i < spent; i++) b.append(ChatColor.RED + "|");
        for (int i = spent; i < filled; i++) b.append(ChatColor.GREEN + "|");
        for (int i = filled; i < len; i++) b.append(ChatColor.GRAY + "|");
        b.append(ChatColor.DARK_GRAY + "]");
        return b.toString();
    }

    private int getEnergyCap(ItemStack axe) {
        if (!axe.hasItemMeta() || !axe.getItemMeta().hasLore()) return 100;
        for (String line : axe.getItemMeta().getLore()) {
            String s = ChatColor.stripColor(line);
            if (s.startsWith("Spirit Cap: ")) {
                try { return Integer.parseInt(s.substring(12).replace("%", "")); }
                catch (NumberFormatException ignored) {}
            }
        }
        return 100;
    }

    private static String getSymbol(UpgradeType t) {
        switch (t) {
            case OAK_YIELD: return "🌳";
            case SPRUCE_YIELD: return "🎄";
            case BIRCH_YIELD: return "🌲";
            case JUNGLE_YIELD: return "🌴";
            case ACACIA_YIELD: return "🌵";
            case DARK_OAK_YIELD: return "🪓";
            case CRIMSON_YIELD: return "🍂";
            case WARPED_YIELD: return "🪵";
            case EFFIGY_YIELD: return "✦";
            case FORESTRY_XP: return "📈";
            case FAKE_NEWS: return "📰";
            case FEED: return "🍖";
            case PAYOUT: return "💰";
            case ORCHARD: return "🍎";
            case GOLDEN_APPLE: return "🏆";
            case TRESPASSER: return "☠";
            case HEADHUNTER: return "🗡";
            case SPECTRAL_ARMOR: return "🛡";
            case ANCIENT_CONFUSION: return "❓";
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

    private static class EffigyUpgradeInventoryHolder implements org.bukkit.inventory.InventoryHolder {
        @Override
        public Inventory getInventory() { return null; }
    }
}
