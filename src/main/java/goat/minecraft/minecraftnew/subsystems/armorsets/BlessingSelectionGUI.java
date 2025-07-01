package goat.minecraft.minecraftnew.subsystems.armorsets;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
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

/**
 * Simple GUI listing available armor set blessings.
 */
public class BlessingSelectionGUI implements Listener {

    private final JavaPlugin plugin;
    private static final String TITLE = ChatColor.LIGHT_PURPLE + "Select Blessing";

    public BlessingSelectionGUI(JavaPlugin plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    /**
     * Opens the Blessing selection interface showing all armor set names.
     */
    public void openGUI(Player player) {
        Inventory gui = Bukkit.createInventory(null, 54, TITLE);

        // Decorative filler for all slots
        ItemStack filler = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta fMeta = filler.getItemMeta();
        if (fMeta != null) {
            fMeta.setDisplayName(" ");
            filler.setItemMeta(fMeta);
        }
        for (int i = 0; i < gui.getSize(); i++) {
            gui.setItem(i, filler);
        }

        int slot = 10; // start from second row
        for (FlowType type : FlowType.values()) {
            if (slot >= gui.getSize()) break;
            if (slot % 9 == 0) slot++; // skip left border

            ItemStack icon = type.createItem();
            if (icon.getType() == Material.AIR) {
                icon = new ItemStack(Material.PAPER);
            }
            ItemMeta meta = icon.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(ChatColor.GOLD + type.name());
                List<String> lore = new ArrayList<>();
                lore.add(ChatColor.YELLOW + "Click to select");
                meta.setLore(lore);
                icon.setItemMeta(meta);
            }
            gui.setItem(slot, icon);
            slot++;
        }

        player.openInventory(gui);
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (!event.getView().getTitle().equals(TITLE)) return;

        event.setCancelled(true);
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || !clicked.hasItemMeta()) return;

        String name = ChatColor.stripColor(clicked.getItemMeta().getDisplayName());
        try {
            FlowType.valueOf(name.toUpperCase());
            ItemStack artifact = player.getInventory().getItemInMainHand();
            ItemMeta artifactMeta = artifact.getItemMeta();
            artifactMeta.setDisplayName(artifactMeta.getDisplayName() + ": " + name);
            player.closeInventory();
        } catch (IllegalArgumentException ignored) {
        }
    }
}
