package goat.minecraft.minecraftnew.subsystems.mining;

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

public class GemstoneUpgradeSystem implements Listener {
    private final MinecraftNew plugin;
    
    public enum UpgradeType {
        // Row 1: Ore Yields (slots 2-7)
        COAL_YIELD("Coal Yield", "Additional coal drops when mining coal ore", Material.COAL_ORE, 5, 2),
        REDSTONE_YIELD("Redstone Yield", "Additional redstone drops when mining redstone ore", Material.REDSTONE_ORE, 5, 3),
        LAPIS_YIELD("Lapis Yield", "Additional lapis drops when mining lapis ore", Material.LAPIS_ORE, 5, 4),
        DIAMOND_YIELD("Diamond Yield", "Additional diamond drops when mining diamond ore", Material.DIAMOND_ORE, 5, 5),
        
        // Row 2: Metalwork (slots 11-16) 
        METALWORK_IRON("Metalwork Iron", "Additional iron drops when mining iron ore", Material.IRON_ORE, 5, 11),
        METALWORK_GOLD("Metalwork Gold", "Additional gold drops when mining gold ore", Material.GOLD_ORE, 5, 12),
        
        // Row 3: Utilities (slots 20-25)
        GEMSTONE_YIELD("Gemstone Yield", "Increases gemstone drop chance from eligible ores", Material.EMERALD, 6, 20),
        MINING_XP_BOOST("Mining XP Boost", "Increases XP gained from mining", Material.EXPERIENCE_BOTTLE, 3, 21),
        OXYGEN("Oxygen", "Chance to restore oxygen when mining ores", Material.GLASS_BOTTLE, 5, 22),
        FEED("Feed", "Rare chance to restore hunger and saturation", Material.BREAD, 3, 23),
        PAYOUT("Payout", "Chance to sell stone stack for 16 emeralds", Material.EMERALD_BLOCK, 4, 24),

        // Row 4: Gold Fever (slots 29-32)
        GOLD_FEVER_CHANCE("Gold Fever Chance", "Increases trigger chance for Gold Fever effect (+15% per level)", Material.GOLDEN_PICKAXE, 3, 29),
        GOLD_FEVER_DURATION("Gold Fever Duration", "Increases duration of Gold Fever effect (+15 seconds per level)", Material.CLOCK, 3, 30),
        GOLD_FEVER_POTENCY("Gold Fever Potency", "Increases haste level of Gold Fever effect (+1 level per upgrade)", Material.GOLDEN_APPLE, 2, 31),
        GOLD_FEVER_RANGE("Gold Fever Range", "Gold Fever effect also applies to nearby players (+3 block radius per level)", Material.GOLDEN_HELMET, 3, 32);
        
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
    
    public GemstoneUpgradeSystem(MinecraftNew plugin) {
        this.plugin = plugin;
    }
    
    public void openUpgradeGUIFromExternal(Player player, ItemStack tool) {
        int totalPower = getTotalPower(tool);
        if (totalPower == 0) {
            player.sendMessage(ChatColor.RED + "This tool has no gemstone power! Apply gemstones first.");
            return;
        }
        openUpgradeGUI(player, tool);
    }
    
    private void openUpgradeGUI(Player player, ItemStack tool) {
        Inventory gui = Bukkit.createInventory(new GemstoneUpgradeInventoryHolder(), 54, ChatColor.GOLD + "⚒ Gemstone Upgrades");
        
        int totalPower = getTotalPower(tool);
        int powerCap = getPowerCap(tool);
        int availablePower = calculateAvailablePower(tool);
        int upgradeCost = 8; // 8% cost per upgrade level
        
        // Fill GUI with background
        for (int i = 0; i < 54; i++) {
            gui.setItem(i, createFillerItem());
        }
        
        // Add horizontal layout with categories on left
        setupHorizontalLayout(gui, tool, upgradeCost, availablePower);
        
        // Add power display at bottom center
        ItemStack powerDisplay = createExtendedPowerDisplay(totalPower, powerCap, availablePower);
        gui.setItem(49, powerDisplay);
        
        // Add respec button
        ItemStack respecItem = new ItemStack(Material.BARRIER);
        ItemMeta respecMeta = respecItem.getItemMeta();
        respecMeta.setDisplayName(ChatColor.RED + "⚠ Reset Upgrades");
        
        int spentPower = totalPower - availablePower;
        List<String> respecLore = new ArrayList<>();
        respecLore.add(ChatColor.GRAY + "Damages tool by " + ChatColor.RED + "20% durability");
        respecLore.add(ChatColor.GRAY + "Returns all allocated power");
        respecLore.add("");
        
        if (spentPower > 0) {
            respecLore.add(ChatColor.GRAY + "Will refund: " + ChatColor.GREEN + spentPower + "% power");
            respecLore.add(ChatColor.YELLOW + "Shift+Right-click to confirm");
        } else {
            respecLore.add(ChatColor.DARK_GRAY + "No upgrades to reset");
        }
        
        respecMeta.setLore(respecLore);
        respecItem.setItemMeta(respecMeta);
        gui.setItem(53, respecItem);
        
        player.openInventory(gui);
    }
    
    private ItemStack createUpgradeItem(UpgradeType upgrade, ItemStack tool, int cost, int availablePower) {
        ItemStack item = new ItemStack(upgrade.getIcon());
        ItemMeta meta = item.getItemMeta();
        
        int currentLevel = getUpgradeLevel(tool, upgrade);
        boolean canAfford = availablePower >= cost;
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
            lore.add(ChatColor.GRAY + "Cost: " + ChatColor.WHITE + cost + "% power");
            if (canAfford) {
                lore.add(ChatColor.GREEN + "Click to upgrade!");
            } else {
                lore.add(ChatColor.RED + "Not enough power!");
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
        if (!(event.getInventory().getHolder() instanceof GemstoneUpgradeInventoryHolder)) return;
        
        event.setCancelled(true);
        
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        
        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null || clickedItem.getType() == Material.AIR) return;
        
        ItemStack tool = player.getInventory().getItemInMainHand();
        if (!isDiamondTool(tool.getType())) {
            player.sendMessage(ChatColor.RED + "You must be holding a diamond tool!");
            return;
        }
        
        // Handle respec (at slot 53)
        if (event.getSlot() == 53 && event.isShiftClick() && event.isRightClick()) {
            handleRespec(player, tool);
            return;
        }
        
        // Find which upgrade was clicked
        UpgradeType clickedUpgrade = null;
        for (UpgradeType upgrade : UpgradeType.values()) {
            if (upgrade.getSlot() == event.getSlot()) {
                clickedUpgrade = upgrade;
                break;
            }
        }
        
        if (clickedUpgrade == null) return;
        
        // Handle upgrade purchase
        int availablePower = calculateAvailablePower(tool);
        int upgradeCost = 8; // 8% per upgrade level
        int currentLevel = getUpgradeLevel(tool, clickedUpgrade);
        
        if (currentLevel >= clickedUpgrade.getMaxLevel()) {
            player.sendMessage(ChatColor.RED + "This upgrade is already at maximum level!");
            return;
        }
        
        if (availablePower < upgradeCost) {
            player.sendMessage(ChatColor.RED + "Not enough gemstone power! Need " + upgradeCost + "% power.");
            return;
        }
        
        // Purchase upgrade
        setUpgradeLevel(tool, clickedUpgrade, currentLevel + 1);
        
        player.sendMessage(ChatColor.GREEN + "Upgraded " + clickedUpgrade.getDisplayName() + " to level " + (currentLevel + 1) + "!");
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
        
        // Refresh GUI
        player.closeInventory();
        openUpgradeGUI(player, tool);
    }
    
    private void handleRespec(Player player, ItemStack tool) {
        int currentDurability = tool.getDurability();
        int maxDurability = tool.getType().getMaxDurability();
        int damageToAdd = (int) Math.ceil(maxDurability * 0.2); // 20% durability damage
        
        if (currentDurability + damageToAdd >= maxDurability) {
            player.sendMessage(ChatColor.RED + "Tool would break from respec damage! Repair it first.");
            return;
        }
        
        // Reset all upgrades
        clearAllUpgrades(tool);
        
        // Apply durability damage
        tool.setDurability((short) (currentDurability + damageToAdd));
        
        player.sendMessage(ChatColor.YELLOW + "Tool respecced! All upgrades reset.");
        player.sendMessage(ChatColor.RED + "Tool took " + damageToAdd + " durability damage.");
        player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 1.0f, 1.0f);
        
        player.closeInventory();
    }
    
    private boolean isDiamondTool(Material material) {
        return material == Material.DIAMOND_PICKAXE || material == Material.NETHERITE_PICKAXE;
    }
    
    private int getTotalPower(ItemStack item) {
        return GemstoneApplicationSystem.getToolGemstonePower(item);
    }
    
    private int calculateAvailablePower(ItemStack tool) {
        int totalPower = getTotalPower(tool);
        int spentPower = 0;
        
        for (UpgradeType upgrade : UpgradeType.values()) {
            int level = getUpgradeLevel(tool, upgrade);
            spentPower += level * 8; // 8% per level
        }
        
        return totalPower - spentPower;
    }
    
    private int getUpgradeLevel(ItemStack item, UpgradeType upgrade) {
        if (!item.hasItemMeta() || !item.getItemMeta().hasLore()) return 0;
        
        List<String> lore = item.getItemMeta().getLore();
        
        // Parse from "Gemstone Upgrades:" symbolic line
        for (String line : lore) {
            String stripped = ChatColor.stripColor(line);
            if (stripped.startsWith("Gemstone Upgrades:")) {
                return parseUpgradeLevelFromSymbolic(line, upgrade);
            }
        }
        
        return 0;
    }
    
    /**
     * Parses upgrade level from symbolic line by finding the upgrade's icon and roman numeral
     */
    private int parseUpgradeLevelFromSymbolic(String symbolicLine, UpgradeType upgrade) {
        String symbol = getPlainUpgradeSymbol(upgrade);
        String stripped = ChatColor.stripColor(symbolicLine);
        
        // Find the symbol in the line
        int symbolIndex = stripped.indexOf(symbol);
        if (symbolIndex == -1) return 0;
        
        // Get the text immediately after the symbol (should be roman numeral)
        String afterSymbol = stripped.substring(symbolIndex + symbol.length());
        
        // Parse roman numeral at the start of afterSymbol
        if (afterSymbol.startsWith("ⱽᴵ")) return 6;
        if (afterSymbol.startsWith("ᴵᴵᴵ")) return 3;
        if (afterSymbol.startsWith("ᴵᴵ")) return 2;
        if (afterSymbol.startsWith("ᴵⱽ")) return 4;
        if (afterSymbol.startsWith("ⱽ")) return 5;
        if (afterSymbol.startsWith("ᴵ")) return 1;
        
        return 0;
    }
    
    private void setUpgradeLevel(ItemStack item, UpgradeType upgrade, int level) {
        ItemMeta meta = item.getItemMeta();
        List<String> lore = meta.hasLore() ? new ArrayList<>(meta.getLore()) : new ArrayList<>();
        
        // Remove any old UPGRADE_ lines from previous versions
        lore.removeIf(line -> {
            String stripped = ChatColor.stripColor(line);
            return stripped.startsWith("UPGRADE_");
        });
        
        // Add new symbolic upgrade line if level > 0
        if (level > 0) {
            updateSymbolicUpgradeLore(lore, upgrade, level);
        }
        
        meta.setLore(lore);
        item.setItemMeta(meta);
    }
    
    /**
     * Updates the symbolic upgrade lore by either adding or updating the consolidated upgrade line
     */
    private void updateSymbolicUpgradeLore(List<String> lore, UpgradeType upgrade, int level) {
        // Find if we already have a "Gemstone Upgrades:" line
        int upgradeLineIndex = -1;
        for (int i = 0; i < lore.size(); i++) {
            if (ChatColor.stripColor(lore.get(i)).startsWith("Gemstone Upgrades:")) {
                upgradeLineIndex = i;
                break;
            }
        }
        
        // Collect all current upgrades from lore directly
        Map<UpgradeType, Integer> allUpgrades = getAllUpgradesFromLore(lore);
        
        // Update the level for the upgrade we're setting
        if (level > 0) {
            allUpgrades.put(upgrade, level);
        } else {
            allUpgrades.remove(upgrade);
        }
        
        // Build the consolidated upgrade line
        if (!allUpgrades.isEmpty()) {
            StringBuilder upgradeLine = new StringBuilder();
            upgradeLine.append(ChatColor.GRAY).append("Gemstone Upgrades: ");
            
            boolean first = true;
            for (Map.Entry<UpgradeType, Integer> entry : allUpgrades.entrySet()) {
                if (!first) upgradeLine.append(" ");
                upgradeLine.append(getUpgradeSymbol(entry.getKey(), entry.getValue()));
                first = false;
            }
            
            String finalLine = upgradeLine.toString();
            
            if (upgradeLineIndex >= 0) {
                // Update existing line
                lore.set(upgradeLineIndex, finalLine);
            } else {
                // Add new line after gemstone power section
                int insertIndex = findInsertionPoint(lore);
                lore.add(insertIndex, finalLine);
            }
        } else if (upgradeLineIndex >= 0) {
            // Remove the line if no upgrades exist
            lore.remove(upgradeLineIndex);
        }
    }
    
    /**
     * Extracts all upgrade levels from existing lore (used during updates)
     */
    private Map<UpgradeType, Integer> getAllUpgradesFromLore(List<String> lore) {
        Map<UpgradeType, Integer> upgrades = new HashMap<>();
        
        // Parse from "Gemstone Upgrades:" symbolic line only
        for (String line : lore) {
            String stripped = ChatColor.stripColor(line);
            if (stripped.startsWith("Gemstone Upgrades:")) {
                // Parse all upgrades from this symbolic line
                for (UpgradeType upgradeType : UpgradeType.values()) {
                    int level = parseUpgradeLevelFromSymbolic(line, upgradeType);
                    if (level > 0) {
                        upgrades.put(upgradeType, level);
                    }
                }
                break; // Only one symbolic line should exist
            }
        }
        
        return upgrades;
    }
    
    /**
     * Gets the colored Unicode symbol for an upgrade type and level
     */
    private String getUpgradeSymbol(UpgradeType upgrade, int level) {
        String symbol = getPlainUpgradeSymbol(upgrade);
        ChatColor color = getUpgradeColor(level);
        return color + symbol + getUpgradeLevelIndicator(level);
    }
    
    /**
     * Gets the plain Unicode symbol for each upgrade type
     */
    private String getPlainUpgradeSymbol(UpgradeType upgrade) {
        switch (upgrade) {
            case COAL_YIELD: return "⚫"; // Black circle for coal
            case REDSTONE_YIELD: return "⚡"; // Lightning for redstone  
            case LAPIS_YIELD: return "🔵"; // Blue circle for lapis
            case DIAMOND_YIELD: return "💎"; // Diamond for diamond
            case METALWORK_IRON: return "⚒"; // Hammer for metalwork
            case METALWORK_GOLD: return "⚱"; // Urn for gold
            case GEMSTONE_YIELD: return "✦"; // Four-pointed star for gemstones
            case MINING_XP_BOOST: return "📈"; // Chart for XP boost
            case OXYGEN: return "💨"; // Wind for oxygen
            case FEED: return "🍖"; // Meat for feed
            case PAYOUT: return "💰"; // Money bag for payout
            case GOLD_FEVER_CHANCE: return "🎯"; // Target for chance
            case GOLD_FEVER_DURATION: return "⏰"; // Clock for duration  
            case GOLD_FEVER_POTENCY: return "💪"; // Muscle for potency
            case GOLD_FEVER_RANGE: return "📡"; // Antenna for range/broadcast
            default: return "⬡"; // Hexagon as fallback
        }
    }
    
    /**
     * Gets the color based on upgrade level (higher level = more epic color)
     */
    private ChatColor getUpgradeColor(int level) {
        if (level >= 6) return ChatColor.DARK_RED;  // Mythic (level 6)
        if (level >= 5) return ChatColor.GOLD;      // Legendary (level 5)
        if (level >= 4) return ChatColor.LIGHT_PURPLE; // Epic (level 4)
        if (level >= 3) return ChatColor.AQUA;      // Rare (level 3)
        if (level >= 2) return ChatColor.GREEN;     // Uncommon (level 2)
        return ChatColor.WHITE;                     // Common (level 1)
    }
    
    /**
     * Gets level indicator (Roman numerals for cleaner look)
     */
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
    
    /**
     * Finds the appropriate insertion point for the upgrade line in lore
     */
    private int findInsertionPoint(List<String> lore) {
        // Insert after gemstone power section (after progress bar)
        for (int i = 0; i < lore.size(); i++) {
            String line = lore.get(i);
            if (line.contains("[") && line.contains("|") && line.contains("]")) {
                return i + 2; // After progress bar + empty line
            }
        }
        // If no gemstone power found, insert at beginning
        return 0;
    }
    
    
    private void clearAllUpgrades(ItemStack tool) {
        if (!tool.hasItemMeta()) return;
        ItemMeta meta = tool.getItemMeta();
        List<String> lore = meta.hasLore() ? new ArrayList<>(meta.getLore()) : new ArrayList<>();
        
        // Remove both symbolic upgrade line and any old UPGRADE_ lines
        lore.removeIf(line -> {
            String stripped = ChatColor.stripColor(line);
            return stripped.startsWith("Gemstone Upgrades:") || stripped.startsWith("UPGRADE_");
        });
        
        meta.setLore(lore);
        tool.setItemMeta(meta);
    }
    
    public int getUpgradeLevel(Player player, ItemStack tool, UpgradeType upgrade) {
        return getUpgradeLevel(tool, upgrade);
    }
    
    /**
     * Gets Gold Fever upgrade values for a tool
     * @param tool The diamond tool to check
     * @return Array with [chance_bonus_percent, duration_bonus_seconds, potency_bonus_levels, range_bonus_blocks]
     */
    public int[] getGoldFeverUpgrades(ItemStack tool) {
        int chanceLevel = getUpgradeLevel(tool, UpgradeType.GOLD_FEVER_CHANCE);
        int durationLevel = getUpgradeLevel(tool, UpgradeType.GOLD_FEVER_DURATION);
        int potencyLevel = getUpgradeLevel(tool, UpgradeType.GOLD_FEVER_POTENCY);
        int rangeLevel = getUpgradeLevel(tool, UpgradeType.GOLD_FEVER_RANGE);
        
        // Base: 5% chance, 15 seconds, potency 1, range 0
        // Chance: +15% per level (so max: 5% + 3*15% = 50%)
        // Duration: +15 seconds per level (so max: 15 + 3*15 = 60 seconds)
        // Potency: +1 per level (so max: 1 + 2*1 = potency 3)
        // Range: +3 blocks per level (so max: 0 + 3*3 = 9 block radius)
        
        int chanceBonus = chanceLevel * 15; // 15% per level
        int durationBonus = durationLevel * 15; // 15 seconds per level  
        int potencyBonus = potencyLevel; // 1 level per upgrade
        int rangeBonus = rangeLevel * 3; // 3 blocks per level
        
        return new int[]{chanceBonus, durationBonus, potencyBonus, rangeBonus};
    }
    
    /**
     * Sets up the new horizontal layout with categories on the left
     */
    private void setupHorizontalLayout(Inventory gui, ItemStack tool, int upgradeCost, int availablePower) {
        // Row 1: Ore Yields
        // Category icon at slot 0
        ItemStack oreHeader = new ItemStack(Material.DIAMOND_ORE);
        ItemMeta oreMeta = oreHeader.getItemMeta();
        oreMeta.setDisplayName(ChatColor.DARK_GREEN + "⛏ Ore Yields");
        oreMeta.setLore(Arrays.asList(ChatColor.GRAY + "Increase drops from specific ores"));
        oreHeader.setItemMeta(oreMeta);
        gui.setItem(0, oreHeader);
        
        // Glass pane separator at slot 1
        gui.setItem(1, createColoredPane(Material.GREEN_STAINED_GLASS_PANE, ""));
        
        // Ore yield upgrades in slots 2-5
        gui.setItem(2, createUpgradeItem(UpgradeType.COAL_YIELD, tool, upgradeCost, availablePower));
        gui.setItem(3, createUpgradeItem(UpgradeType.REDSTONE_YIELD, tool, upgradeCost, availablePower));
        gui.setItem(4, createUpgradeItem(UpgradeType.LAPIS_YIELD, tool, upgradeCost, availablePower));
        gui.setItem(5, createUpgradeItem(UpgradeType.DIAMOND_YIELD, tool, upgradeCost, availablePower));
        
        // Row 2: Metalwork
        // Category icon at slot 9
        ItemStack metalHeader = new ItemStack(Material.ANVIL);
        ItemMeta metalMeta = metalHeader.getItemMeta();
        metalMeta.setDisplayName(ChatColor.GOLD + "⚒ Metalwork");
        metalMeta.setLore(Arrays.asList(ChatColor.GRAY + "Enhanced processing for metal ores"));
        metalHeader.setItemMeta(metalMeta);
        gui.setItem(9, metalHeader);
        
        // Glass pane separator at slot 10
        gui.setItem(10, createColoredPane(Material.YELLOW_STAINED_GLASS_PANE, ""));
        
        // Metalwork upgrades in slots 11-12
        gui.setItem(11, createUpgradeItem(UpgradeType.METALWORK_IRON, tool, upgradeCost, availablePower));
        gui.setItem(12, createUpgradeItem(UpgradeType.METALWORK_GOLD, tool, upgradeCost, availablePower));
        
        // Row 3: Utilities
        // Category icon at slot 18
        ItemStack utilHeader = new ItemStack(Material.NETHER_STAR);
        ItemMeta utilMeta = utilHeader.getItemMeta();
        utilMeta.setDisplayName(ChatColor.LIGHT_PURPLE + "✦ Utilities");
        utilMeta.setLore(Arrays.asList(ChatColor.GRAY + "Special mining enhancements"));
        utilHeader.setItemMeta(utilMeta);
        gui.setItem(18, utilHeader);
        
        // Glass pane separator at slot 19
        gui.setItem(19, createColoredPane(Material.PURPLE_STAINED_GLASS_PANE, ""));
        
        // Utility upgrades in slots 20-24
        gui.setItem(20, createUpgradeItem(UpgradeType.GEMSTONE_YIELD, tool, upgradeCost, availablePower));
        gui.setItem(21, createUpgradeItem(UpgradeType.MINING_XP_BOOST, tool, upgradeCost, availablePower));
        gui.setItem(22, createUpgradeItem(UpgradeType.OXYGEN, tool, upgradeCost, availablePower));
        gui.setItem(23, createUpgradeItem(UpgradeType.FEED, tool, upgradeCost, availablePower));
        gui.setItem(24, createUpgradeItem(UpgradeType.PAYOUT, tool, upgradeCost, availablePower));
        
        // Row 4: Gold Fever
        // Category icon at slot 27
        ItemStack goldFeverHeader = new ItemStack(Material.GOLDEN_PICKAXE);
        ItemMeta goldFeverMeta = goldFeverHeader.getItemMeta();
        goldFeverMeta.setDisplayName(ChatColor.GOLD + "⚡ Gold Fever");
        goldFeverMeta.setLore(Arrays.asList(ChatColor.GRAY + "Enhance the Gold Fever mining effect"));
        goldFeverHeader.setItemMeta(goldFeverMeta);
        gui.setItem(27, goldFeverHeader);
        
        // Glass pane separator at slot 28
        gui.setItem(28, createColoredPane(Material.ORANGE_STAINED_GLASS_PANE, ""));
        
        // Gold Fever upgrades in slots 29-32
        gui.setItem(29, createUpgradeItem(UpgradeType.GOLD_FEVER_CHANCE, tool, upgradeCost, availablePower));
        gui.setItem(30, createUpgradeItem(UpgradeType.GOLD_FEVER_DURATION, tool, upgradeCost, availablePower));
        gui.setItem(31, createUpgradeItem(UpgradeType.GOLD_FEVER_POTENCY, tool, upgradeCost, availablePower));
        gui.setItem(32, createUpgradeItem(UpgradeType.GOLD_FEVER_RANGE, tool, upgradeCost, availablePower));
    }
    
    /**
     * Gets the power cap for a tool (base 100% + cap upgrades)
     */
    private int getPowerCap(ItemStack tool) {
        if (!tool.hasItemMeta() || !tool.getItemMeta().hasLore()) return 100;
        
        List<String> lore = tool.getItemMeta().getLore();
        for (String line : lore) {
            String stripped = ChatColor.stripColor(line);
            if (stripped.startsWith("Power Cap: ")) {
                String capStr = stripped.substring("Power Cap: ".length()).replace("%", "");
                try {
                    return Integer.parseInt(capStr);
                } catch (NumberFormatException e) {
                    return 100;
                }
            }
        }
        return 100;
    }
    
    /**
     * Creates an extended power display that supports caps beyond 100%
     */
    private ItemStack createExtendedPowerDisplay(int totalPower, int powerCap, int availablePower) {
        ItemStack powerItem = new ItemStack(Material.GLOWSTONE);
        ItemMeta powerMeta = powerItem.getItemMeta();
        powerMeta.setDisplayName(ChatColor.GOLD + "⚡ Gemstone Power Status");
        
        // Create extended power bar visualization that grows with cap
        int baseBarLength = 20;
        int extraSegments = (powerCap - 100) / 100; // Each 100% adds segments
        int totalBarLength = baseBarLength + (extraSegments * 5); // +5 chars per 100% cap
        
        int filledBars = (int) ((double) totalPower / powerCap * totalBarLength);
        int spentBars = (int) ((double) (totalPower - availablePower) / powerCap * totalBarLength);
        
        StringBuilder powerBar = new StringBuilder();
        powerBar.append(ChatColor.DARK_GRAY + "[");
        
        // Used power (red)
        for (int i = 0; i < spentBars; i++) {
            powerBar.append(ChatColor.RED + "|");
        }
        
        // Available power (green)
        for (int i = spentBars; i < filledBars; i++) {
            powerBar.append(ChatColor.GREEN + "|");
        }
        
        // Empty space (gray)
        for (int i = filledBars; i < totalBarLength; i++) {
            powerBar.append(ChatColor.GRAY + "|");
        }
        
        powerBar.append(ChatColor.DARK_GRAY + "]");
        
        List<String> powerLore = new ArrayList<>();
        powerLore.add(ChatColor.GRAY + "Total Power: " + ChatColor.WHITE + totalPower + "%" + 
                     ChatColor.GRAY + " / " + ChatColor.YELLOW + powerCap + "%");
        powerLore.add(ChatColor.GRAY + "Available: " + ChatColor.GREEN + availablePower + "% " + 
                     ChatColor.GRAY + "Spent: " + ChatColor.RED + (totalPower - availablePower) + "%");
        powerLore.add("");
        powerLore.add(powerBar.toString());
        powerLore.add("");
        
        if (powerCap > 100) {
            powerLore.add(ChatColor.AQUA + "Enhanced Power Cap: " + ChatColor.YELLOW + powerCap + "%");
            powerLore.add(ChatColor.GRAY + "Apply " + ChatColor.LIGHT_PURPLE + "Power Crystals" + ChatColor.GRAY + " to increase cap");
        } else {
            powerLore.add(ChatColor.GRAY + "Apply " + ChatColor.LIGHT_PURPLE + "Power Crystals" + ChatColor.GRAY + " to increase cap beyond 100%");
        }
        powerLore.add(ChatColor.GRAY + "Apply gemstones to increase current power");
        
        powerMeta.setLore(powerLore);
        powerItem.setItemMeta(powerMeta);
        return powerItem;
    }
    
    /**
     * Creates a colored glass pane with optional name
     */
    private ItemStack createColoredPane(Material material, String name) {
        ItemStack pane = new ItemStack(material);
        ItemMeta meta = pane.getItemMeta();
        meta.setDisplayName(ChatColor.BLACK + name);
        pane.setItemMeta(meta);
        return pane;
    }
    
    /**
     * Custom inventory holder to distinguish gemstone upgrade GUIs
     */
    private static class GemstoneUpgradeInventoryHolder implements org.bukkit.inventory.InventoryHolder {
        @Override
        public org.bukkit.inventory.Inventory getInventory() {
            return null;
        }
    }
}