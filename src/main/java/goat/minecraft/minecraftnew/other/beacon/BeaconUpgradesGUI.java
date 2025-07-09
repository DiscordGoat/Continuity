package goat.minecraft.minecraftnew.other.beacon;

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
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

public class BeaconUpgradesGUI implements Listener {

    private final JavaPlugin plugin;
    private final ItemStack beacon;
    private final String guiTitle;

    public BeaconUpgradesGUI(JavaPlugin plugin, ItemStack beacon) {
        this.plugin = plugin;
        this.beacon = beacon;
        this.guiTitle = ChatColor.DARK_PURPLE + "Beacon Statistics";
    }

    public void openGUI(Player player) {
        Inventory gui = Bukkit.createInventory(null, 45, guiTitle); // 5 rows

        // Get beacon statistics
        int power = BeaconManager.getBeaconPower(beacon);
        int tier = BeaconManager.getBeaconTier(beacon);
        int range = BeaconManager.getBeaconRange(beacon);
        int duration = BeaconManager.getBeaconDuration(beacon);

        // Beacon Info
        ItemStack beaconInfo = createStatItem(Material.BEACON,
            ChatColor.GOLD + "Beacon Charm Statistics",
            ChatColor.AQUA + "Power: " + ChatColor.YELLOW + String.format("%,d", power) + ChatColor.GRAY + "/10,000",
            ChatColor.GOLD + "Tier: " + ChatColor.YELLOW + tier + ChatColor.GRAY + "/5",
            ChatColor.BLUE + "Range: " + ChatColor.WHITE + range + " blocks",
            ChatColor.GREEN + "Duration: " + ChatColor.WHITE + duration + " seconds"
        );
        gui.setItem(13, beaconInfo);

        // Catalyst Statistics
        ItemStack catalystStats = createStatItem(Material.REDSTONE_BLOCK,
            ChatColor.RED + "Catalyst of Power",
            ChatColor.GRAY + "Active catalyst statistics and",
            ChatColor.GRAY + "damage calculation details.",
            "",
            ChatColor.GOLD + "Damage Calculation:",
            ChatColor.YELLOW + "Base: +25%",
            ChatColor.YELLOW + "Tier Bonus: +" + (tier * 5) + "% (Tier " + tier + " × 5%)",
            ChatColor.RED + "Total Damage: +" + (25 + (tier * 5)) + "%",
            "",
            ChatColor.BLUE + "Effect Range: " + ChatColor.WHITE + range + " blocks",
            ChatColor.GREEN + "Effect Duration: " + ChatColor.WHITE + duration + " seconds"
        );
        gui.setItem(21, catalystStats);

        // Future Upgrades Info
        ItemStack futureUpgrades = createStatItem(Material.ANVIL,
            ChatColor.DARK_PURPLE + "Future Upgrades",
            ChatColor.GRAY + "Coming soon! Future updates will",
            ChatColor.GRAY + "include permanent beacon upgrades:",
            "",
            ChatColor.AQUA + "• Range Extensions",
            ChatColor.AQUA + "• Duration Boosters", 
            ChatColor.AQUA + "• Power Amplifiers",
            ChatColor.AQUA + "• Special Catalyst Unlocks",
            "",
            ChatColor.DARK_GRAY + "Stay tuned for more content!"
        );
        gui.setItem(23, futureUpgrades);

        // Power Breakdown
        ItemStack powerBreakdown = createStatItem(Material.DIAMOND,
            ChatColor.AQUA + "Power Breakdown",
            ChatColor.GRAY + "Understanding your beacon's power:",
            "",
            ChatColor.WHITE + "Tier 1 (0-1,665): " + ChatColor.GRAY + "30 blocks, 30s",
            ChatColor.WHITE + "Tier 2 (1,666-3,332): " + ChatColor.GRAY + "60 blocks, 35s", 
            ChatColor.WHITE + "Tier 3 (3,333-4,999): " + ChatColor.GRAY + "90 blocks, 50s",
            ChatColor.WHITE + "Tier 4 (5,000-6,665): " + ChatColor.GRAY + "120 blocks, 60s",
            ChatColor.WHITE + "Tier 5 (6,666-8,332): " + ChatColor.GRAY + "180 blocks, 80s",
            ChatColor.GOLD + "Tier 6 (8,333-10,000): " + ChatColor.GRAY + "250 blocks, 120s",
            "",
            ChatColor.YELLOW + "Current: " + ChatColor.WHITE + "Tier " + tier
        );
        gui.setItem(31, powerBreakdown);

        // Back button
        ItemStack backButton = createStatItem(Material.ARROW,
            ChatColor.YELLOW + "← Back",
            "Return to the main",
            "Beacon Charm menu."
        );
        gui.setItem(36, backButton);

        // Fill empty slots
        fillEmptySlots(gui);

        player.openInventory(gui);
    }

    private ItemStack createStatItem(Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        meta.setDisplayName(name);

        List<String> loreList = new ArrayList<>();
        loreList.add("");
        for (String line : lore) {
            loreList.add(line);
        }

        meta.setLore(loreList);
        item.setItemMeta(meta);

        return item;
    }

    private void fillEmptySlots(Inventory gui) {
        ItemStack filler = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = filler.getItemMeta();
        meta.setDisplayName(" ");
        filler.setItemMeta(meta);

        for (int i = 0; i < gui.getSize(); i++) {
            if (gui.getItem(i) == null) {
                gui.setItem(i, filler);
            }
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();

        if (!event.getView().getTitle().equals(guiTitle)) return;

        event.setCancelled(true);

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || !clicked.hasItemMeta()) return;

        String itemName = ChatColor.stripColor(clicked.getItemMeta().getDisplayName());

        if (itemName.equals("← Back")) {
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
            player.closeInventory();
            BeaconCharmGUI mainGUI = new BeaconCharmGUI(plugin, beacon);
            mainGUI.openGUI(player);
        }
    }
}