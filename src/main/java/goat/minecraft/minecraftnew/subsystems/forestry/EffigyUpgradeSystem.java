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

public class EffigyUpgradeSystem implements Listener {
    private final MinecraftNew plugin;
    /**
     * Percentage of Spirit Energy consumed per upgrade level.
     * This mirrors the cost structure of gemstone upgrades.
     */
    private static final int UPGRADE_COST = 8;

    public enum UpgradeType {
        // Row 1: Log Yield I
        OAK_YIELD("Oak Yield", "Additional oak logs from oak trees", Material.OAK_LOG, 5, 2),
        BIRCH_YIELD("Birch Yield", "Additional birch logs from birch trees", Material.BIRCH_LOG, 5, 3),
        SPRUCE_YIELD("Spruce Yield", "Additional spruce logs from spruce trees", Material.SPRUCE_LOG, 5, 4),
        JUNGLE_YIELD("Jungle Yield", "Additional jungle logs from jungle trees", Material.JUNGLE_LOG, 5, 5),
        ACACIA_YIELD("Acacia Yield", "Additional acacia logs from acacia trees", Material.ACACIA_LOG, 5, 6),
        // Row 2: Log Yield II
        DARK_OAK_YIELD("Dark Oak Yield", "Additional dark oak logs", Material.DARK_OAK_LOG, 5, 11),
        MANGROVE_YIELD("Mangrove Yield", "Additional mangrove logs", Material.MANGROVE_LOG, 5, 12),
        CHERRY_YIELD("Cherry Yield", "Additional cherry logs", Material.CHERRY_LOG, 5, 13),
        CRIMSON_YIELD("Crimson Yield", "Additional crimson stems", Material.CRIMSON_STEM, 5, 14),
        WARPED_YIELD("Warped Yield", "Additional warped stems", Material.WARPED_STEM, 5, 15),

        // Row 3: Utility
        EFFIGY_YIELD("Effigy Yield", "+0.5% spirit chance per level", Material.OAK_WOOD, 6, 20),
        FORESTRY_XP_BOOST("Forestry XP Boost", "Increases forestry XP gained", Material.EXPERIENCE_BOTTLE, 3, 21),
        FAKE_NEWS("Fake News", "Chance to reduce notoriety", Material.PAPER, 5, 22),
        FEED("Feed", "Chance to restore hunger on log break", Material.BREAD, 3, 23),
        PAYOUT("Payout", "Sell 64 logs/sticks/saplings for 8 emeralds", Material.EMERALD_BLOCK, 4, 24),

        // Row 4: Harvest
        ORCHARD("Orchard", "Boost perfect apple droprate", Material.APPLE, 4, 29),
        GOLDEN_APPLE("Golden Apple", "Chance to drop enchanted golden apple", Material.ENCHANTED_GOLDEN_APPLE, 3, 30),

        // Row 5: Combat
        TRESPASSER("Trespasser", "+3 notoriety per level", Material.WOODEN_AXE, 6, 38),
        HEADHUNTER("Headhunter", "+10% damage to forest spirits", Material.IRON_AXE, 5, 39),
        SPECTRAL_ARMOR("Spectral Armor", "+10% damage reduction from spirits", Material.LEATHER_CHESTPLATE, 5, 40),
        ANCIENT_CONFUSION("Ancient Confusion", "Reduce spirit level", Material.END_CRYSTAL, 4, 41);

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

    public void openUpgradeGUIFromExternal(Player player, ItemStack axe) {
        int totalEnergy = getTotalEnergy(axe);
        if (totalEnergy == 0) {
            player.sendMessage(ChatColor.RED + "This axe has no Spirit Energy! Apply effigies first.");
            return;
        }
        openUpgradeGUI(player, axe);
    }

    private void openUpgradeGUI(Player player, ItemStack axe) {
        Inventory gui = Bukkit.createInventory(new EffigyUpgradeInventoryHolder(), 54, ChatColor.GOLD + "✧ Effigy Upgrades");

        int totalEnergy = getTotalEnergy(axe);
        int energyCap = getEnergyCap(axe);
        int availableEnergy = calculateAvailableEnergy(axe);

        for (int i = 0; i < 54; i++) {
            gui.setItem(i, createFillerItem());
        }

        // Row 1 header
        ItemStack header1 = new ItemStack(Material.OAK_LOG);
        ItemMeta hm1 = header1.getItemMeta();
        hm1.setDisplayName(ChatColor.DARK_GREEN + "🌲 Log Yields I");
        header1.setItemMeta(hm1);
        gui.setItem(0, header1);
        gui.setItem(1, createColoredPane(Material.GREEN_STAINED_GLASS_PANE, ""));

        gui.setItem(2, createUpgradeItem(UpgradeType.OAK_YIELD, axe, UPGRADE_COST, availableEnergy));
        gui.setItem(3, createUpgradeItem(UpgradeType.BIRCH_YIELD, axe, UPGRADE_COST, availableEnergy));
        gui.setItem(4, createUpgradeItem(UpgradeType.SPRUCE_YIELD, axe, UPGRADE_COST, availableEnergy));
        gui.setItem(5, createUpgradeItem(UpgradeType.JUNGLE_YIELD, axe, UPGRADE_COST, availableEnergy));
        gui.setItem(6, createUpgradeItem(UpgradeType.ACACIA_YIELD, axe, UPGRADE_COST, availableEnergy));

        // Row 2 header
        ItemStack header2 = new ItemStack(Material.DARK_OAK_LOG);
        ItemMeta hm2 = header2.getItemMeta();
        hm2.setDisplayName(ChatColor.DARK_GREEN + "🌳 Log Yields II");
        header2.setItemMeta(hm2);
        gui.setItem(9, header2);
        gui.setItem(10, createColoredPane(Material.GREEN_STAINED_GLASS_PANE, ""));

        gui.setItem(11, createUpgradeItem(UpgradeType.DARK_OAK_YIELD, axe, UPGRADE_COST, availableEnergy));
        gui.setItem(12, createUpgradeItem(UpgradeType.MANGROVE_YIELD, axe, UPGRADE_COST, availableEnergy));
        gui.setItem(13, createUpgradeItem(UpgradeType.CHERRY_YIELD, axe, UPGRADE_COST, availableEnergy));
        gui.setItem(14, createUpgradeItem(UpgradeType.CRIMSON_YIELD, axe, UPGRADE_COST, availableEnergy));
        gui.setItem(15, createUpgradeItem(UpgradeType.WARPED_YIELD, axe, UPGRADE_COST, availableEnergy));

        // Row 3 header - Utilities
        ItemStack utilHeader = new ItemStack(Material.NETHER_STAR);
        ItemMeta utilMeta = utilHeader.getItemMeta();
        utilMeta.setDisplayName(ChatColor.LIGHT_PURPLE + "✦ Utilities");
        utilHeader.setItemMeta(utilMeta);
        gui.setItem(18, utilHeader);
        gui.setItem(19, createColoredPane(Material.PURPLE_STAINED_GLASS_PANE, ""));

        gui.setItem(20, createUpgradeItem(UpgradeType.EFFIGY_YIELD, axe, UPGRADE_COST, availableEnergy));
        gui.setItem(21, createUpgradeItem(UpgradeType.FORESTRY_XP_BOOST, axe, UPGRADE_COST, availableEnergy));
        gui.setItem(22, createUpgradeItem(UpgradeType.FAKE_NEWS, axe, UPGRADE_COST, availableEnergy));
        gui.setItem(23, createUpgradeItem(UpgradeType.FEED, axe, UPGRADE_COST, availableEnergy));
        gui.setItem(24, createUpgradeItem(UpgradeType.PAYOUT, axe, UPGRADE_COST, availableEnergy));

        // Row 4 header - Harvest
        ItemStack harvestHeader = new ItemStack(Material.GOLDEN_APPLE);
        ItemMeta harvestMeta = harvestHeader.getItemMeta();
        harvestMeta.setDisplayName(ChatColor.GOLD + "🍎 Harvest");
        harvestHeader.setItemMeta(harvestMeta);
        gui.setItem(27, harvestHeader);
        gui.setItem(28, createColoredPane(Material.ORANGE_STAINED_GLASS_PANE, ""));

        gui.setItem(29, createUpgradeItem(UpgradeType.ORCHARD, axe, UPGRADE_COST, availableEnergy));
        gui.setItem(30, createUpgradeItem(UpgradeType.GOLDEN_APPLE, axe, UPGRADE_COST, availableEnergy));

        // Row 5 header - Combat
        ItemStack combatHeader = new ItemStack(Material.SHIELD);
        ItemMeta combatMeta = combatHeader.getItemMeta();
        combatMeta.setDisplayName(ChatColor.RED + "⚔ Combat");
        combatHeader.setItemMeta(combatMeta);
        gui.setItem(36, combatHeader);
        gui.setItem(37, createColoredPane(Material.RED_STAINED_GLASS_PANE, ""));

        gui.setItem(38, createUpgradeItem(UpgradeType.TRESPASSER, axe, UPGRADE_COST, availableEnergy));
        gui.setItem(39, createUpgradeItem(UpgradeType.HEADHUNTER, axe, UPGRADE_COST, availableEnergy));
        gui.setItem(40, createUpgradeItem(UpgradeType.SPECTRAL_ARMOR, axe, UPGRADE_COST, availableEnergy));
        gui.setItem(41, createUpgradeItem(UpgradeType.ANCIENT_CONFUSION, axe, UPGRADE_COST, availableEnergy));

        // Power display
        ItemStack powerDisplay = createEnergyDisplay(totalEnergy, energyCap, availableEnergy);
        gui.setItem(49, powerDisplay);

        // Respec button
        ItemStack respecItem = new ItemStack(Material.BARRIER);
        ItemMeta respecMeta = respecItem.getItemMeta();
        respecMeta.setDisplayName(ChatColor.RED + "⚠ Reset Upgrades");
        int spentEnergy = totalEnergy - availableEnergy;
        List<String> respecLore = new ArrayList<>();
        respecLore.add(ChatColor.GRAY + "Damages axe by " + ChatColor.RED + "20% durability");
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

    private ItemStack createUpgradeItem(UpgradeType upgrade, ItemStack axe, int cost, int availableEnergy) {
        ItemStack item = new ItemStack(upgrade.getIcon());
        ItemMeta meta = item.getItemMeta();

        int currentLevel = getUpgradeLevel(axe, upgrade);
        boolean canAfford = availableEnergy >= cost;
        boolean maxLevel = currentLevel >= upgrade.getMaxLevel();

        String displayName;
        if (maxLevel) {
            displayName = ChatColor.GOLD + upgrade.getDisplayName() + " (MAX)";
        } else if (canAfford) {
            displayName = ChatColor.GREEN + upgrade.getDisplayName() + " (" + currentLevel + "/" + upgrade.getMaxLevel() + ")";
        } else {
            displayName = ChatColor.RED + upgrade.getDisplayName() + " (" + currentLevel + "/" + upgrade.getMaxLevel() + ")";
        }

        meta.setDisplayName(displayName);

        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + upgrade.getDescription());
        lore.add(ChatColor.GRAY + "Current Level: " + ChatColor.WHITE + currentLevel + "/" + upgrade.getMaxLevel());

        if (!maxLevel) {
            lore.add(ChatColor.GRAY + "Cost: " + ChatColor.WHITE + cost + "% energy");
            if (canAfford) {
                lore.add(ChatColor.GREEN + "Click to upgrade!");
            } else {
                lore.add(ChatColor.RED + "Not enough energy!");
            }
        } else {
            lore.add(ChatColor.GOLD + "Maximum level reached!");
        }

        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createFillerItem() {
        ItemStack filler = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = filler.getItemMeta();
        meta.setDisplayName(ChatColor.BLACK + "");
        filler.setItemMeta(meta);
        return filler;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof EffigyUpgradeInventoryHolder)) return;
        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null || clickedItem.getType() == Material.AIR) return;
        ItemStack axe = player.getInventory().getItemInMainHand();
        if (axe.getType() != Material.DIAMOND_AXE && axe.getType() != Material.NETHERITE_AXE) {
            player.sendMessage(ChatColor.RED + "You must be holding a diamond or netherite axe!");
            return;
        }

        if (event.getSlot() == 53 && event.isShiftClick() && event.isRightClick()) {
            handleRespec(player, axe);
            return;
        }

        UpgradeType clickedUpgrade = null;
        for (UpgradeType upgrade : UpgradeType.values()) {
            if (upgrade.getSlot() == event.getSlot()) {
                clickedUpgrade = upgrade;
                break;
            }
        }
        if (clickedUpgrade == null) return;

        int availableEnergy = calculateAvailableEnergy(axe);
        int currentLevel = getUpgradeLevel(axe, clickedUpgrade);

        if (currentLevel >= clickedUpgrade.getMaxLevel()) {
            player.sendMessage(ChatColor.RED + "This upgrade is already at maximum level!");
            return;
        }
        if (availableEnergy < UPGRADE_COST) {
            player.sendMessage(ChatColor.RED + "Not enough Spirit Energy! Need " + UPGRADE_COST + "% energy.");
            return;
        }

        setUpgradeLevel(axe, clickedUpgrade, currentLevel + 1);
        player.sendMessage(ChatColor.GREEN + "Upgraded " + clickedUpgrade.getDisplayName() + " to level " + (currentLevel + 1) + "!");
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
        player.closeInventory();
        openUpgradeGUI(player, axe);
    }

    private void handleRespec(Player player, ItemStack axe) {
        int currentDurability = axe.getDurability();
        int maxDurability = axe.getType().getMaxDurability();
        int damageToAdd = (int) Math.ceil(maxDurability * 0.2);

        if (currentDurability + damageToAdd >= maxDurability) {
            player.sendMessage(ChatColor.RED + "Axe would break from respec damage! Repair it first.");
            return;
        }

        clearAllUpgrades(axe);
        axe.setDurability((short) (currentDurability + damageToAdd));
        player.sendMessage(ChatColor.YELLOW + "Axe respecced! All upgrades reset.");
        player.sendMessage(ChatColor.RED + "Axe took " + damageToAdd + " durability damage.");
        player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 1.0f, 1.0f);
        player.closeInventory();
    }

    private int getTotalEnergy(ItemStack axe) {
        return EffigyApplicationSystem.getAxeSpiritEnergy(axe);
    }

    private int calculateAvailableEnergy(ItemStack axe) {
        int total = getTotalEnergy(axe);
        int spent = 0;
        for (UpgradeType upgrade : UpgradeType.values()) {
            int level = getUpgradeLevel(axe, upgrade);
            spent += level * UPGRADE_COST;
        }
        return total - spent;
    }

    private int getUpgradeLevel(ItemStack item, UpgradeType upgrade) {
        if (!item.hasItemMeta() || !item.getItemMeta().hasLore()) return 0;
        List<String> lore = item.getItemMeta().getLore();
        for (String line : lore) {
            String stripped = ChatColor.stripColor(line);
            if (stripped.startsWith("Effigy Upgrades:")) {
                return parseUpgradeLevelFromSymbolic(line, upgrade);
            }
        }
        return 0;
    }

    private int parseUpgradeLevelFromSymbolic(String symbolicLine, UpgradeType upgrade) {
        String symbol = getPlainUpgradeSymbol(upgrade);
        String stripped = ChatColor.stripColor(symbolicLine);
        int symbolIndex = stripped.indexOf(symbol);
        if (symbolIndex == -1) return 0;
        String after = stripped.substring(symbolIndex + symbol.length());
        if (after.startsWith("ⱽᴵ")) return 6;
        if (after.startsWith("ⱽ")) return 5;
        if (after.startsWith("ᴵⱽ")) return 4;
        if (after.startsWith("ᴵᴵᴵ")) return 3;
        if (after.startsWith("ᴵᴵ")) return 2;
        if (after.startsWith("ᴵ")) return 1;
        return 0;
    }

    private void setUpgradeLevel(ItemStack item, UpgradeType upgrade, int level) {
        ItemMeta meta = item.getItemMeta();
        List<String> lore = meta.hasLore() ? new ArrayList<>(meta.getLore()) : new ArrayList<>();

        lore.removeIf(line -> ChatColor.stripColor(line).startsWith("UPGRADE_"));

        if (level > 0) {
            updateSymbolicUpgradeLore(lore, upgrade, level);
        }

        meta.setLore(lore);
        item.setItemMeta(meta);
    }

    private void updateSymbolicUpgradeLore(List<String> lore, UpgradeType upgrade, int level) {
        int upgradeLineIndex = -1;
        for (int i = 0; i < lore.size(); i++) {
            if (ChatColor.stripColor(lore.get(i)).startsWith("Effigy Upgrades:")) {
                upgradeLineIndex = i;
                break;
            }
        }

        Map<UpgradeType, Integer> allUpgrades = getAllUpgradesFromLore(lore);

        if (level > 0) {
            allUpgrades.put(upgrade, level);
        } else {
            allUpgrades.remove(upgrade);
        }

        if (!allUpgrades.isEmpty()) {
            StringBuilder line = new StringBuilder();
            line.append(ChatColor.GRAY).append("Effigy Upgrades: ");
            boolean first = true;
            for (Map.Entry<UpgradeType, Integer> entry : allUpgrades.entrySet()) {
                if (!first) line.append(" ");
                line.append(getUpgradeSymbol(entry.getKey(), entry.getValue()));
                first = false;
            }
            String finalLine = line.toString();
            if (upgradeLineIndex >= 0) {
                lore.set(upgradeLineIndex, finalLine);
            } else {
                int insertIndex = findInsertionPoint(lore);
                lore.add(insertIndex, finalLine);
            }
        } else if (upgradeLineIndex >= 0) {
            lore.remove(upgradeLineIndex);
        }
    }

    private Map<UpgradeType, Integer> getAllUpgradesFromLore(List<String> lore) {
        Map<UpgradeType, Integer> upgrades = new LinkedHashMap<>();
        for (String line : lore) {
            String stripped = ChatColor.stripColor(line);
            if (stripped.startsWith("Effigy Upgrades:")) {
                for (UpgradeType upgradeType : UpgradeType.values()) {
                    int level = parseUpgradeLevelFromSymbolic(line, upgradeType);
                    if (level > 0) {
                        upgrades.put(upgradeType, level);
                    }
                }
                break;
            }
        }
        return upgrades;
    }

    private String getUpgradeSymbol(UpgradeType upgrade, int level) {
        String symbol = getPlainUpgradeSymbol(upgrade);
        ChatColor color = getUpgradeColor(level);
        return color + symbol + getUpgradeLevelIndicator(level);
    }

    private String getPlainUpgradeSymbol(UpgradeType upgrade) {
        switch (upgrade) {
            case OAK_YIELD: return "🌳";
            case BIRCH_YIELD: return "🌲";
            case SPRUCE_YIELD: return "🌲";
            case JUNGLE_YIELD: return "🌴";
            case ACACIA_YIELD: return "🌳";
            case DARK_OAK_YIELD: return "🌲";
            case MANGROVE_YIELD: return "🌳";
            case CHERRY_YIELD: return "🌸";
            case CRIMSON_YIELD: return "🍁";
            case WARPED_YIELD: return "🪵";
            case EFFIGY_YIELD: return "✧";
            case FORESTRY_XP_BOOST: return "📈";
            case FAKE_NEWS: return "📰";
            case FEED: return "🍖";
            case PAYOUT: return "💰";
            case ORCHARD: return "🍎";
            case GOLDEN_APPLE: return "✨";
            case TRESPASSER: return "🚪";
            case HEADHUNTER: return "🗡";
            case SPECTRAL_ARMOR: return "🛡";
            case ANCIENT_CONFUSION: return "❓";
            default: return "⬡";
        }
    }

    private ChatColor getUpgradeColor(int level) {
        if (level >= 6) return ChatColor.DARK_RED;
        if (level >= 5) return ChatColor.GOLD;
        if (level >= 4) return ChatColor.LIGHT_PURPLE;
        if (level >= 3) return ChatColor.AQUA;
        if (level >= 2) return ChatColor.GREEN;
        return ChatColor.WHITE;
    }

    private String getUpgradeLevelIndicator(int level) {
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

    private int findInsertionPoint(List<String> lore) {
        for (int i = 0; i < lore.size(); i++) {
            String line = lore.get(i);
            if (line.contains("[") && line.contains("|") && line.contains("]")) {
                return i + 2;
            }
        }
        return 0;
    }

    private void clearAllUpgrades(ItemStack axe) {
        if (!axe.hasItemMeta()) return;
        ItemMeta meta = axe.getItemMeta();
        List<String> lore = meta.hasLore() ? new ArrayList<>(meta.getLore()) : new ArrayList<>();
        lore.removeIf(line -> {
            String stripped = ChatColor.stripColor(line);
            return stripped.startsWith("Effigy Upgrades:") || stripped.startsWith("UPGRADE_");
        });
        meta.setLore(lore);
        axe.setItemMeta(meta);
    }

    private ItemStack createColoredPane(Material material, String name) {
        ItemStack pane = new ItemStack(material);
        ItemMeta meta = pane.getItemMeta();
        meta.setDisplayName(ChatColor.BLACK + name);
        pane.setItemMeta(meta);
        return pane;
    }

    private ItemStack createEnergyDisplay(int total, int cap, int available) {
        ItemStack item = new ItemStack(Material.GLOWSTONE);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.GOLD + "⚡ Spirit Energy Status");

        int baseBar = 20;
        int extra = (cap - 100) / 100;
        int totalBar = baseBar + (extra * 5);

        int filled = (int)((double) total / cap * totalBar);
        int spent = (int)((double) (total - available) / cap * totalBar);

        StringBuilder bar = new StringBuilder();
        bar.append(ChatColor.DARK_GRAY + "[");
        for (int i = 0; i < spent; i++) {
            bar.append(ChatColor.RED + "|");
        }
        for (int i = spent; i < filled; i++) {
            bar.append(ChatColor.GREEN + "|");
        }
        for (int i = filled; i < totalBar; i++) {
            bar.append(ChatColor.GRAY + "|");
        }
        bar.append(ChatColor.DARK_GRAY + "]");

        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + "Total Energy: " + ChatColor.WHITE + total + "%" + ChatColor.GRAY + " / " + ChatColor.YELLOW + cap + "%");
        lore.add(ChatColor.GRAY + "Available: " + ChatColor.GREEN + available + "% " + ChatColor.GRAY + "Spent: " + ChatColor.RED + (total - available) + "%");
        lore.add("");
        lore.add(bar.toString());
        lore.add("");
        if (cap > 100) {
            lore.add(ChatColor.AQUA + "Enhanced Cap: " + ChatColor.YELLOW + cap + "%");
            lore.add(ChatColor.GRAY + "Apply " + ChatColor.LIGHT_PURPLE + "Ent Bark" + ChatColor.GRAY + " to increase cap");
        } else {
            lore.add(ChatColor.GRAY + "Apply " + ChatColor.LIGHT_PURPLE + "Ent Bark" + ChatColor.GRAY + " to increase cap beyond 100%");
        }
        lore.add(ChatColor.GRAY + "Apply effigies to increase current energy");
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
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

    private static class EffigyUpgradeInventoryHolder implements org.bukkit.inventory.InventoryHolder {
        @Override
        public Inventory getInventory() {
            return null;
        }
    }
}
