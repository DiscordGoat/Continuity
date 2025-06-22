package goat.minecraft.minecraftnew.subsystems.beacon;

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

public class BeaconCharmGUI implements Listener {

    private final JavaPlugin plugin;
    private final ItemStack beacon;
    private final String guiTitle;

    public BeaconCharmGUI(JavaPlugin plugin, ItemStack beacon) {
        this.plugin = plugin;
        this.beacon = beacon;
        this.guiTitle = ChatColor.GOLD + "Beacon Charm";
    }

    public void openGUI(Player player) {
        Inventory gui = Bukkit.createInventory(null, 27, guiTitle);
        
        // Create the two main buttons
        ItemStack passivesButton = createButton(Material.SHIELD, 
            ChatColor.GREEN + "Passives", 
            "View and manage passive effects",
            "that activate while the beacon",
            "is in your inventory.");
        
        ItemStack catalystsButton = createButton(Material.BREWING_STAND, 
            ChatColor.BLUE + "Catalysts", 
            "Configure active effects that",
            "spawn floating beacons when used.",
            "Grants buffs while in range.");
        
        // Place buttons in the GUI (centered)
        gui.setItem(11, passivesButton);  // Left position
        gui.setItem(15, catalystsButton); // Right position
        
        // Fill empty slots with glass panes
        fillEmptySlots(gui);
        
        player.openInventory(gui);
    }

    private ItemStack createButton(Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        
        meta.setDisplayName(name);
        
        List<String> loreList = new ArrayList<>();
        loreList.add("");
        for (String line : lore) {
            loreList.add(ChatColor.GRAY + line);
        }
        loreList.add("");
        loreList.add(ChatColor.YELLOW + "Click to open!");
        
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
        
        switch (itemName) {
            case "Passives":
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
                player.closeInventory();
                BeaconPassivesGUI passivesGUI = new BeaconPassivesGUI(plugin, beacon);
                passivesGUI.openGUI(player);
                break;
                
            case "Catalysts":
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
                player.closeInventory();
                BeaconCatalystsGUI catalystsGUI = new BeaconCatalystsGUI(plugin, beacon);
                catalystsGUI.openGUI(player);
                break;
        }
    }
}