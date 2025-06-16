package goat.minecraft.minecraftnew.subsystems.combat;

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
 * Simple upgrade GUI for weapons with Soul Power.
 * Supports sword and bow variants with a few upgrades each.
 */
public class SoulUpgradeSystem implements Listener {
    private final MinecraftNew plugin;

    public SoulUpgradeSystem(MinecraftNew plugin) {
        this.plugin = plugin;
    }

    // ----- ENUMERATIONS -----

    public enum SwordUpgrade {
        DIAMOND_ESSENCE("Diamond Essence", "+10% creeper damage per level", Material.DIAMOND, 5, 2),
        LIFESTEAL_REGEN("Regeneration", "+5% chance to gain regeneration per level", Material.GHAST_TEAR, 3, 3),
        LIFESTEAL_POTENCY("Potency", "+1 regen potency per level", Material.BLAZE_POWDER, 2, 4),
        LIFESTEAL_DURATION("Duration", "+5 seconds of regeneration per level", Material.CLOCK, 3, 5),
        LOYAL_AUGMENT("Loyal Augment", "-50% base damage, -1s cooldown per level", Material.NETHER_STAR, 4, 6),
        SHRED_AUGMENT("Shred Augment", "+3 stacks of shredders per level", Material.IRON_SWORD, 6, 7),
        WARP_AUGMENT("Warp Augment", "+3 warp charges per level", Material.ENDER_PEARL, 6, 8),
        FURY("Fury", "Strike lightning when below 50% HP (30s CD)", Material.LIGHTNING_ROD, 1, 11),
        BETRAYAL("Betrayal", "+4% creeper disc chance per level", Material.MUSIC_DISC_11, 4, 12);

        private final String name;
        private final String desc;
        private final Material icon;
        private final int maxLevel;
        private final int slot;

        SwordUpgrade(String n, String d, Material m, int max, int slot) {
            this.name = n;
            this.desc = d;
            this.icon = m;
            this.maxLevel = max;
            this.slot = slot;
        }
        public String getName() { return name; }
        public String getDesc() { return desc; }
        public Material getIcon() { return icon; }
        public int getMaxLevel() { return maxLevel; }
        public int getSlot() { return slot; }
    }

    public enum BowUpgrade {
        LIFESTEAL("Lifesteal", "+5% chance to heal 50% missing health per level", Material.GHAST_TEAR, 5, 11),
        HEADSHOT("Headshot Augment", "+10 damage per level", Material.ARROW, 5, 12);

        private final String name;
        private final String desc;
        private final Material icon;
        private final int maxLevel;
        private final int slot;

        BowUpgrade(String n, String d, Material m, int max, int slot) {
            this.name = n;
            this.desc = d;
            this.icon = m;
            this.maxLevel = max;
            this.slot = slot;
        }
        public String getName() { return name; }
        public String getDesc() { return desc; }
        public Material getIcon() { return icon; }
        public int getMaxLevel() { return maxLevel; }
        public int getSlot() { return slot; }
    }

    // ----- GUI OPENING -----

    public void openUpgradeGUI(Player player, ItemStack weapon) {
        if (weapon.getType() == Material.BOW) {
            openBowUpgradeGUI(player, weapon);
        } else {
            openSwordUpgradeGUI(player, weapon);
        }
    }

    private void openSwordUpgradeGUI(Player player, ItemStack weapon) {
        int power = getTotalPower(weapon);
        if (power == 0) {
            player.sendMessage(ChatColor.RED + "This weapon has no Soul Power!");
            return;
        }

        Inventory gui = Bukkit.createInventory(new SoulUpgradeHolder(), 54,
                ChatColor.DARK_AQUA + "✦ Sword Upgrades");

        for (int i = 0; i < 54; i++) gui.setItem(i, createFiller());

        int available = calculateAvailablePower(weapon, false);

        gui.setItem(0, createHeader(Material.DIAMOND_SWORD, ChatColor.RED + "⚔ Sword"));
        gui.setItem(1, createColoredPane(Material.RED_STAINED_GLASS_PANE, ""));
        for (SwordUpgrade up : SwordUpgrade.values()) {
            gui.setItem(up.getSlot(), createUpgradeItem(weapon, up, available));
        }

        gui.setItem(49, createExtendedPowerDisplay(power, getPowerCap(weapon), available));
        ItemStack respec = new ItemStack(Material.BARRIER);
        ItemMeta rMeta = respec.getItemMeta();
        rMeta.setDisplayName(ChatColor.RED + "⚠ Reset Upgrades");
        List<String> lore = new ArrayList<>();
        int spent = power - available;
        lore.add(ChatColor.GRAY + "Damages tool by " + ChatColor.RED + "20% durability");
        lore.add(ChatColor.GRAY + "Returns all allocated power");
        lore.add("");
        if (spent > 0) {
            lore.add(ChatColor.GRAY + "Will refund: " + ChatColor.GREEN + spent + "% power");
            lore.add(ChatColor.YELLOW + "Shift+Right-click to confirm");
        } else {
            lore.add(ChatColor.DARK_GRAY + "No upgrades to reset");
        }
        rMeta.setLore(lore);
        respec.setItemMeta(rMeta);
        gui.setItem(53, respec);

        player.openInventory(gui);
    }

    private void openBowUpgradeGUI(Player player, ItemStack weapon) {
        int power = getTotalPower(weapon);
        if (power == 0) {
            player.sendMessage(ChatColor.RED + "This weapon has no Soul Power!");
            return;
        }

        Inventory gui = Bukkit.createInventory(new SoulUpgradeHolder(), 54,
                ChatColor.DARK_AQUA + "✦ Bow Upgrades");

        for (int i = 0; i < 54; i++) gui.setItem(i, createFiller());

        int available = calculateAvailablePower(weapon, true);

        gui.setItem(0, createHeader(Material.BOW, ChatColor.GOLD + "➹ Bow"));
        gui.setItem(1, createColoredPane(Material.YELLOW_STAINED_GLASS_PANE, ""));
        for (BowUpgrade up : BowUpgrade.values()) {
            gui.setItem(up.getSlot(), createUpgradeItem(weapon, up, available));
        }

        gui.setItem(49, createExtendedPowerDisplay(power, getPowerCap(weapon), available));
        ItemStack respec = new ItemStack(Material.BARRIER);
        ItemMeta rMeta = respec.getItemMeta();
        rMeta.setDisplayName(ChatColor.RED + "⚠ Reset Upgrades");
        List<String> lore = new ArrayList<>();
        int spent = power - available;
        lore.add(ChatColor.GRAY + "Damages tool by " + ChatColor.RED + "20% durability");
        lore.add(ChatColor.GRAY + "Returns all allocated power");
        lore.add("");
        if (spent > 0) {
            lore.add(ChatColor.GRAY + "Will refund: " + ChatColor.GREEN + spent + "% power");
            lore.add(ChatColor.YELLOW + "Shift+Right-click to confirm");
        } else {
            lore.add(ChatColor.DARK_GRAY + "No upgrades to reset");
        }
        rMeta.setLore(lore);
        respec.setItemMeta(rMeta);
        gui.setItem(53, respec);

        player.openInventory(gui);
    }

    private ItemStack createFiller() {
        ItemStack fill = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = fill.getItemMeta();
        meta.setDisplayName(ChatColor.BLACK + "");
        fill.setItemMeta(meta);
        return fill;
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

    // Overloaded for sword and bow upgrades
    private ItemStack createUpgradeItem(ItemStack weapon, SwordUpgrade up, int available) {
        int level = getUpgradeLevel(weapon, up.name());
        boolean max = level >= up.getMaxLevel();
        boolean afford = available >= 10;
        ItemStack item = new ItemStack(up.getIcon());
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName((max ? ChatColor.GOLD : (afford ? ChatColor.GREEN : ChatColor.RED)) + up.getName());
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + up.getDesc());
        lore.add(ChatColor.GRAY + "Level: " + ChatColor.WHITE + level + "/" + up.getMaxLevel());
        if (!max) {
            lore.add(ChatColor.GRAY + "Cost: 10% power");
            lore.add(afford ? ChatColor.GREEN + "Click to upgrade" : ChatColor.RED + "Not enough power");
        } else {
            lore.add(ChatColor.GOLD + "Max level reached");
        }
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createUpgradeItem(ItemStack weapon, BowUpgrade up, int available) {
        int level = getUpgradeLevel(weapon, up.name());
        boolean max = level >= up.getMaxLevel();
        boolean afford = available >= 10;
        ItemStack item = new ItemStack(up.getIcon());
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName((max ? ChatColor.GOLD : (afford ? ChatColor.GREEN : ChatColor.RED)) + up.getName());
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + up.getDesc());
        lore.add(ChatColor.GRAY + "Level: " + ChatColor.WHITE + level + "/" + up.getMaxLevel());
        if (!max) {
            lore.add(ChatColor.GRAY + "Cost: 10% power");
            lore.add(afford ? ChatColor.GREEN + "Click to upgrade" : ChatColor.RED + "Not enough power");
        } else {
            lore.add(ChatColor.GOLD + "Max level reached");
        }
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    // ----- EVENT HANDLING -----

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof SoulUpgradeHolder)) return;
        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        ItemStack weapon = player.getInventory().getItemInMainHand();
        if (weapon == null) return;

        boolean bow = weapon.getType() == Material.BOW;

        int slot = event.getSlot();
        if (slot == 53 && event.isShiftClick() && event.isRightClick()) {
            handleRespec(player, weapon);
            return;
        }
        if (bow) {
            for (BowUpgrade up : BowUpgrade.values()) {
                if (up.getSlot() == slot) {
                    purchase(player, weapon, up.name(), up.getMaxLevel());
                    return;
                }
            }
        } else {
            for (SwordUpgrade up : SwordUpgrade.values()) {
                if (up.getSlot() == slot) {
                    purchase(player, weapon, up.name(), up.getMaxLevel());
                    return;
                }
            }
        }
    }

    private void purchase(Player player, ItemStack weapon, String key, int max) {
        int level = getUpgradeLevel(weapon, key);
        if (level >= max) {
            player.sendMessage(ChatColor.RED + "Upgrade at max level");
            return;
        }
        int available = calculateAvailablePower(weapon, weapon.getType() == Material.BOW);
        if (available < 10) {
            player.sendMessage(ChatColor.RED + "Not enough Soul Power");
            return;
        }
        setUpgradeLevel(weapon, key, level + 1);
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
        player.closeInventory();
        openUpgradeGUI(player, weapon);
    }

    // ----- POWER AND LORE -----

    private int getTotalPower(ItemStack weapon) {
        return SoulApplicationSystem.getWeaponSoulPower(weapon);
    }

    private int getPowerCap(ItemStack weapon) {
        if (!weapon.hasItemMeta() || !weapon.getItemMeta().hasLore()) return 100;
        for (String line : weapon.getItemMeta().getLore()) {
            String s = ChatColor.stripColor(line);
            if (s.startsWith("Soul Cap: ")) {
                String txt = s.substring(10).replace("%", "");
                try { return Integer.parseInt(txt); } catch (NumberFormatException e) { return 100; }
            }
        }
        return 100;
    }

    private int calculateAvailablePower(ItemStack weapon, boolean bow) {
        int spent = 0;
        if (bow) {
            for (BowUpgrade up : BowUpgrade.values()) {
                spent += getUpgradeLevel(weapon, up.name()) * 10;
            }
        } else {
            for (SwordUpgrade up : SwordUpgrade.values()) {
                spent += getUpgradeLevel(weapon, up.name()) * 10;
            }
        }
        return getTotalPower(weapon) - spent;
    }

    private int getUpgradeLevel(ItemStack weapon, String key) {
        if (!weapon.hasItemMeta() || !weapon.getItemMeta().hasLore()) return 0;
        for (String line : weapon.getItemMeta().getLore()) {
            String stripped = ChatColor.stripColor(line);
            if (stripped.startsWith("Soul Upgrades:")) {
                return parseLevel(line, key);
            }
        }
        return 0;
    }

    private int parseLevel(String line, String key) {
        String symbol = getSymbol(key);
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

    private void setUpgradeLevel(ItemStack weapon, String key, int level) {
        ItemMeta meta = weapon.getItemMeta();
        List<String> lore = meta.hasLore() ? new ArrayList<>(meta.getLore()) : new ArrayList<>();
        int lineIndex = -1;
        for (int i = 0; i < lore.size(); i++) {
            if (ChatColor.stripColor(lore.get(i)).startsWith("Soul Upgrades:")) {
                lineIndex = i; break; }
        }
        Map<String,Integer> levels = new LinkedHashMap<>();
        if (lineIndex >= 0) {
            for (String k : getAllKeys()) {
                int lvl = parseLevel(lore.get(lineIndex), k);
                if (lvl > 0) levels.put(k, lvl);
            }
            lore.remove(lineIndex);
        }
        if (level > 0) levels.put(key, level); else levels.remove(key);
        if (!levels.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            sb.append(ChatColor.GRAY).append("Soul Upgrades: ");
            boolean first = true;
            for (Map.Entry<String,Integer> e : levels.entrySet()) {
                if (!first) sb.append(" ");
                sb.append(getColoredSymbol(e.getKey(), e.getValue()));
                first = false;
            }
            if (lineIndex < 0) lineIndex = findInsertIndex(lore);
            lore.add(lineIndex, sb.toString());
        }
        meta.setLore(lore);
        weapon.setItemMeta(meta);
    }

    private int findInsertIndex(List<String> lore) {
        for (int i = 0; i < lore.size(); i++) {
            String stripped = ChatColor.stripColor(lore.get(i));
            if (stripped.startsWith("Soul Cap:")) {
                return i + 1;
            }
        }
        return lore.size();
    }

    private Set<String> getAllKeys() {
        Set<String> keys = new LinkedHashSet<>();
        for (SwordUpgrade s : SwordUpgrade.values()) keys.add(s.name());
        for (BowUpgrade b : BowUpgrade.values()) keys.add(b.name());
        return keys;
    }

    private void clearAllUpgrades(ItemStack weapon) {
        if (!weapon.hasItemMeta()) return;
        ItemMeta meta = weapon.getItemMeta();
        List<String> lore = meta.hasLore() ? new ArrayList<>(meta.getLore()) : new ArrayList<>();
        lore.removeIf(l -> ChatColor.stripColor(l).startsWith("Soul Upgrades:"));
        meta.setLore(lore);
        weapon.setItemMeta(meta);
    }

    private void handleRespec(Player player, ItemStack weapon) {
        int currentDurability = weapon.getDurability();
        int maxDurability = weapon.getType().getMaxDurability();
        int damageToAdd = (int) Math.ceil(maxDurability * 0.2);
        if (currentDurability + damageToAdd >= maxDurability) {
            player.sendMessage(ChatColor.RED + "Tool would break from respec damage! Repair it first.");
            return;
        }
        clearAllUpgrades(weapon);
        weapon.setDurability((short) (currentDurability + damageToAdd));
        player.sendMessage(ChatColor.YELLOW + "Tool respecced! All upgrades reset.");
        player.sendMessage(ChatColor.RED + "Tool took " + damageToAdd + " durability damage.");
        player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 1.0f, 1.0f);
        player.closeInventory();
    }

    // ----- SYMBOLS & COLORS -----
    private String getSymbol(String key) {
        switch (key) {
            case "DIAMOND_ESSENCE": return "♦";
            case "LIFESTEAL_REGEN": return "❤";
            case "LIFESTEAL_POTENCY": return "✚";
            case "LIFESTEAL_DURATION": return "⌛";
            case "LIFESTEAL": return "❤";
            case "LOYAL_AUGMENT": return "⚔";
            case "SHRED_AUGMENT": return "✂";
            case "WARP_AUGMENT": return "✦";
            case "FURY": return "⚡";
            case "BETRAYAL": return "♬";
            case "HEADSHOT": return "➹";
            default: return "⬡";
        }
    }

    private String getColoredSymbol(String key, int level) {
        ChatColor color = ChatColor.WHITE;
        if (level >= 6) color = ChatColor.DARK_RED;
        else if (level >= 5) color = ChatColor.GOLD;
        else if (level >= 4) color = ChatColor.LIGHT_PURPLE;
        else if (level >= 3) color = ChatColor.AQUA;
        else if (level >= 2) color = ChatColor.GREEN;
        return color + getSymbol(key) + getLevelIndicator(level);
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

    // ----- EXTENDED POWER DISPLAY -----
    private ItemStack createExtendedPowerDisplay(int total, int cap, int available) {
        ItemStack item = new ItemStack(Material.SOUL_TORCH);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.AQUA + "Soul Power");
        int baseBar = 20;
        int extra = (cap - 100) / 100;
        int len = baseBar + extra * 5;
        int filled = (int)((double)total / cap * len);
        int spent = (int)((double)(total - available) / cap * len);
        StringBuilder bar = new StringBuilder();
        bar.append(ChatColor.DARK_GRAY).append("[");
        for (int i=0;i<spent;i++) bar.append(ChatColor.RED + "|");
        for (int i=spent;i<filled;i++) bar.append(ChatColor.GREEN + "|");
        for (int i=filled;i<len;i++) bar.append(ChatColor.GRAY + "|");
        bar.append(ChatColor.DARK_GRAY).append("]");

        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + "Total Power: " + ChatColor.WHITE + total + "%" + ChatColor.GRAY + " / " + ChatColor.YELLOW + cap + "%");
        lore.add(ChatColor.GRAY + "Available: " + ChatColor.GREEN + available + "% " + ChatColor.GRAY + "Spent: " + ChatColor.RED + (total - available) + "%");
        lore.add("");
        lore.add(bar.toString());
        lore.add("");
        if (cap > 100) {
            lore.add(ChatColor.AQUA + "Enhanced Power Cap: " + ChatColor.YELLOW + cap + "%");
            lore.add(ChatColor.GRAY + "Apply Blue Lanterns to increase cap");
        } else {
            lore.add(ChatColor.GRAY + "Apply Blue Lanterns to increase cap beyond 100%");
        }
        lore.add(ChatColor.GRAY + "Apply soul items to increase current power");

        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    // ----- HOLDER -----
    private static class SoulUpgradeHolder implements org.bukkit.inventory.InventoryHolder {
        @Override
        public Inventory getInventory() { return null; }
    }
}
