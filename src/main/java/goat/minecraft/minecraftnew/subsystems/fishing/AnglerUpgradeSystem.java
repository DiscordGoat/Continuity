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
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

/**
 * Handles fishing rod upgrades powered by Angler Energy.
 */
public class AnglerUpgradeSystem implements Listener {

    private final MinecraftNew plugin;

    /**
     * Types of upgrades available for fishing rods.
     */
    public enum UpgradeType {
        FINDING_NEMO("Finding Nemo", "Chance to gain extra tropical fish", Material.TROPICAL_FISH, 3, 10),
        TREASURE_HUNTER("Treasure Hunter", "Increased treasure chance", Material.CHEST, 5, 11),
        SONAR("Sonar", "Increased sea creature chance", Material.TRIDENT, 6, 12),
        CHARMED("Charmed", "Chance to gain Luck", Material.EMERALD, 3, 13),
        RABBITS_FOOT("Rabbit's Foot", "Increases Luck potency", Material.RABBIT_FOOT, 3, 14),
        GOOD_DAY("Good Day", "Extends Luck duration", Material.CLOCK, 3, 15),
        RAIN_DANCE("Rain Dance", "Chance to extend rain", Material.WATER_BUCKET, 4, 16),
        PAYOUT("Payout", "Automatically sells common fish", Material.EMERALD, 1, 17),
        PASSION("Passion", "Chance to fully heal", Material.TOTEM_OF_UNDYING, 4, 18),
        FEED("Feed", "Chance to restore hunger", Material.COOKED_BEEF, 3, 19),
        KRAKEN("Kraken", "Chance to reel in two sea creatures", Material.PRISMARINE_SHARD, 3, 20),
        BIGGER_FISH("Bigger Fish", "Reduces sea creature level", Material.FISHING_ROD, 4, 21),
        DIAMOND_HOOK("Diamond Hook", "Instantly kill sea creatures", Material.TRIPWIRE_HOOK, 3, 22);

        private final String name;
        private final String description;
        private final Material icon;
        private final int maxLevel;
        private final int slot;

        UpgradeType(String n, String d, Material i, int m, int s) {
            this.name = n;
            this.description = d;
            this.icon = i;
            this.maxLevel = m;
            this.slot = s;
        }

        public String getName() { return name; }
        public String getDescription() { return description; }
        public Material getIcon() { return icon; }
        public int getMaxLevel() { return maxLevel; }
        public int getSlot() { return slot; }
    }

    public AnglerUpgradeSystem(MinecraftNew plugin) {
        this.plugin = plugin;
    }

    /**
     * Opens the upgrade GUI if the rod has any Angler Energy.
     */
    public void openUpgradeGUIFromExternal(Player player, ItemStack rod) {
        if (getTotalEnergy(rod) == 0) {
            player.sendMessage(ChatColor.RED + "This rod has no Angler Energy!");
            return;
        }
        openUpgradeGUI(player, rod);
    }

    private void openUpgradeGUI(Player player, ItemStack rod) {
        int total = getTotalEnergy(rod);
        int available = calcAvailable(rod);
        int cost = 8; // cost per level

        Inventory gui = Bukkit.createInventory(new AnglerUpgradeHolder(), 27,
                ChatColor.AQUA + "Fishing Upgrades");

        for (int i = 0; i < 27; i++) gui.setItem(i, createPane());

        for (UpgradeType u : UpgradeType.values()) {
            gui.setItem(u.getSlot(), createUpgradeItem(u, rod, cost, available));
        }
        gui.setItem(26, createPowerDisplay(total, getPowerCap(rod), available));

        player.openInventory(gui);
    }

    private ItemStack createUpgradeItem(UpgradeType up, ItemStack rod, int cost, int available) {
        ItemStack item = new ItemStack(up.getIcon());
        ItemMeta meta = item.getItemMeta();
        int level = getUpgradeLevel(rod, up);
        boolean max = level >= up.getMaxLevel();
        boolean canAfford = available >= cost;

        String name;
        if (max) {
            name = ChatColor.GOLD + up.getName() + " (MAX)";
        } else if (canAfford) {
            name = ChatColor.GREEN + up.getName() + " (" + level + "/" + up.getMaxLevel() + ")";
        } else {
            name = ChatColor.RED + up.getName() + " (" + level + "/" + up.getMaxLevel() + ")";
        }
        meta.setDisplayName(name);

        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + up.getDescription());
        lore.add(ChatColor.GRAY + "Current: " + ChatColor.WHITE + level + "/" + up.getMaxLevel());
        if (!max) {
            lore.add(ChatColor.GRAY + "Cost: " + ChatColor.WHITE + cost + "% energy");
            lore.add(canAfford ? ChatColor.GREEN + "Click to upgrade!" : ChatColor.RED + "Not enough energy!");
        } else {
            lore.add(ChatColor.GOLD + "Maximum level reached!");
        }
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof AnglerUpgradeHolder)) return;
        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player)) return;

        Player player = (Player) event.getWhoClicked();
        ItemStack rod = player.getInventory().getItemInMainHand();
        if (rod == null || rod.getType() != Material.FISHING_ROD) {
            player.sendMessage(ChatColor.RED + "Hold a fishing rod!");
            return;
        }

        UpgradeType clicked = null;
        for (UpgradeType u : UpgradeType.values()) {
            if (u.getSlot() == event.getSlot()) {
                clicked = u;
                break;
            }
        }
        if (clicked == null) return;

        int available = calcAvailable(rod);
        int cost = 8;
        int level = getUpgradeLevel(rod, clicked);

        if (level >= clicked.getMaxLevel()) {
            player.sendMessage(ChatColor.RED + "Upgrade maxed!");
            return;
        }
        if (available < cost) {
            player.sendMessage(ChatColor.RED + "Not enough Angler Energy!");
            return;
        }

        setUpgradeLevel(rod, clicked, level + 1);
        player.sendMessage(ChatColor.GREEN + "Upgraded " + clicked.getName() + " to level " + (level + 1));
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);

        player.closeInventory();
        openUpgradeGUI(player, rod);
    }

    // ----- Data helpers -----

    private int getTotalEnergy(ItemStack rod) {
        return BaitApplicationSystem.getRodAnglerEnergyStatic(rod);
    }

    private int calcAvailable(ItemStack rod) {
        int total = getTotalEnergy(rod);
        int spent = 0;
        for (UpgradeType u : UpgradeType.values()) {
            spent += getUpgradeLevel(rod, u) * 8;
        }
        return total - spent;
    }

    private int getPowerCap(ItemStack rod) {
        if (!rod.hasItemMeta() || !rod.getItemMeta().hasLore()) return 100;
        for (String line : rod.getItemMeta().getLore()) {
            String s = ChatColor.stripColor(line);
            if (s.startsWith("Power Cap: ")) {
                String c = s.substring(10).replace("%", "");
                try {
                    return Integer.parseInt(c);
                } catch (Exception ignored) {
                    return 100;
                }
            }
        }
        return 100;
    }

    private int getUpgradeLevel(ItemStack rod, UpgradeType up) {
        if (!rod.hasItemMeta() || !rod.getItemMeta().hasLore()) return 0;
        for (String line : rod.getItemMeta().getLore()) {
            String st = ChatColor.stripColor(line);
            if (st.startsWith("Fishing Upgrades:")) {
                return parseFromLine(line, up);
            }
        }
        return 0;
    }

    private int parseFromLine(String line, UpgradeType up) {
        String sym = getPlainSymbol(up);
        String stripped = ChatColor.stripColor(line);
        int idx = stripped.indexOf(sym);
        if (idx == -1) return 0;
        String after = stripped.substring(idx + sym.length());
        if (after.startsWith("ⱽᴵ")) return 6;
        if (after.startsWith("ⱽ")) return 5;
        if (after.startsWith("ᴵⱽ")) return 4;
        if (after.startsWith("ᴵᴵᴵ")) return 3;
        if (after.startsWith("ᴵᴵ")) return 2;
        if (after.startsWith("ᴵ")) return 1;
        return 0;
    }

    private void setUpgradeLevel(ItemStack rod, UpgradeType up, int level) {
        ItemMeta meta = rod.getItemMeta();
        List<String> lore = meta.hasLore() ? new ArrayList<>(meta.getLore()) : new ArrayList<>();
        lore.removeIf(l -> ChatColor.stripColor(l).startsWith("UPGRADE_"));
        if (level > 0) {
            updateLore(lore, up, level);
        }
        meta.setLore(lore);
        rod.setItemMeta(meta);
    }

    private void updateLore(List<String> lore, UpgradeType up, int level) {
        int idx = -1;
        for (int i = 0; i < lore.size(); i++) {
            if (ChatColor.stripColor(lore.get(i)).startsWith("Fishing Upgrades:")) {
                idx = i;
                break;
            }
        }
        Map<UpgradeType, Integer> map = getAll(lore);
        if (level > 0) map.put(up, level); else map.remove(up);
        if (!map.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            sb.append(ChatColor.GRAY).append("Fishing Upgrades:");
            boolean first = true;
            for (Map.Entry<UpgradeType, Integer> e : map.entrySet()) {
                if (!first) sb.append(" ");
                sb.append(getSymbol(e.getKey(), e.getValue()));
                first = false;
            }
            String line = sb.toString();
            if (idx >= 0) lore.set(idx, line); else lore.add(findInsert(lore), line);
        } else if (idx >= 0) {
            lore.remove(idx);
        }
    }

    private Map<UpgradeType, Integer> getAll(List<String> lore) {
        Map<UpgradeType, Integer> m = new LinkedHashMap<>();
        for (String line : lore) {
            String stripped = ChatColor.stripColor(line);
            if (stripped.startsWith("Fishing Upgrades:")) {
                for (UpgradeType u : UpgradeType.values()) {
                    int lv = parseFromLine(line, u);
                    if (lv > 0) m.put(u, lv);
                }
                break;
            }
        }
        return m;
    }

    private int findInsert(List<String> lore) {
        return lore.size();
    }

    private String getSymbol(UpgradeType up, int level) {
        String s = getPlainSymbol(up);
        ChatColor c = getColor(level);
        return c + s + getNumeral(level);
    }

    private String getPlainSymbol(UpgradeType up) {
        return switch (up) {
            case FINDING_NEMO -> "🐠";
            case TREASURE_HUNTER -> "💰";
            case SONAR -> "📡";
            case CHARMED -> "✨";
            case RABBITS_FOOT -> "🐇";
            case GOOD_DAY -> "⏰";
            case RAIN_DANCE -> "🌧";
            case PAYOUT -> "💵";
            case PASSION -> "❤";
            case FEED -> "🍗";
            case KRAKEN -> "🐙";
            case BIGGER_FISH -> "🦈";
            case DIAMOND_HOOK -> "💎";
        };
    }

    private ChatColor getColor(int level) {
        if (level >= 6) return ChatColor.DARK_RED;
        if (level >= 5) return ChatColor.GOLD;
        if (level >= 4) return ChatColor.LIGHT_PURPLE;
        if (level >= 3) return ChatColor.AQUA;
        if (level >= 2) return ChatColor.GREEN;
        return ChatColor.WHITE;
    }

    private String getNumeral(int level) {
        return switch (level) {
            case 1 -> "ᴵ";
            case 2 -> "ᴵᴵ";
            case 3 -> "ᴵᴵᴵ";
            case 4 -> "ᴵⱽ";
            case 5 -> "ⱽ";
            case 6 -> "ⱽᴵ";
            default -> "";
        };
    }

    private ItemStack createPowerDisplay(int total, int cap, int available) {
        ItemStack it = new ItemStack(Material.PRISMARINE_CRYSTALS);
        ItemMeta m = it.getItemMeta();
        m.setDisplayName(ChatColor.AQUA + "Angler Energy Status");
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + "Total: " + ChatColor.WHITE + total + "%" + ChatColor.GRAY + " / " + ChatColor.YELLOW + cap + "%");
        lore.add(ChatColor.GRAY + "Available: " + ChatColor.GREEN + available + "%" + ChatColor.GRAY + " Spent: " + ChatColor.RED + (total - available) + "%");
        lore.add("");
        lore.add(createBar(total, cap, available));
        lore.add("");
        lore.add(ChatColor.GRAY + "Apply bait to increase energy");
        lore.add(ChatColor.GRAY + "Use Pearls to raise cap");
        m.setLore(lore);
        it.setItemMeta(m);
        return it;
    }

    private String createBar(int total, int cap, int available) {
        int len = 20 + (cap - 100) / 100 * 5;
        int filled = (int) ((double) total / cap * len);
        int spent = (int) ((double) (total - available) / cap * len);
        StringBuilder sb = new StringBuilder();
        sb.append(ChatColor.DARK_GRAY + "[");
        for (int i = 0; i < spent; i++) sb.append(ChatColor.RED + "|");
        for (int i = spent; i < filled; i++) sb.append(ChatColor.GREEN + "|");
        for (int i = filled; i < len; i++) sb.append(ChatColor.GRAY + "|");
        sb.append(ChatColor.DARK_GRAY + "]");
        return sb.toString();
    }

    private ItemStack createPane() {
        ItemStack it = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta m = it.getItemMeta();
        m.setDisplayName(ChatColor.BLACK + "");
        it.setItemMeta(m);
        return it;
    }

    private static class AnglerUpgradeHolder implements InventoryHolder {
        @Override
        public Inventory getInventory() {
            return null;
        }
    }
}
