package goat.minecraft.minecraftnew.subsystems.beacon;

import goat.minecraft.minecraftnew.MinecraftNew;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import java.util.HashMap;
import java.util.Map;

/**
 * Simple GUI allowing players to select a beacon catalyst.
 */
public class BeaconCharmGUI implements Listener {
    private static final String TITLE = ChatColor.AQUA + "Beacon Catalyst Selection";
    private final MinecraftNew plugin;
    private final Map<Integer, CatalystType> slotMap = new HashMap<>();

    public BeaconCharmGUI(MinecraftNew plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    /**
     * Opens the catalyst selection GUI for the given beacon tier.
     */
    public void open(Player player, int tier) {
        Inventory gui = Bukkit.createInventory(new Holder(), 27, TITLE);
        slotMap.clear();
        int slot = 10; // start near center
        for (CatalystType type : CatalystType.values()) {
            gui.setItem(slot, type.createItem(tier));
            slotMap.put(slot, type);
            slot += 1;
        }
        player.openInventory(gui);
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        if (!event.getView().getTitle().equals(TITLE)) return;
        event.setCancelled(true);
        Player player = (Player) event.getWhoClicked();
        CatalystType type = slotMap.get(event.getSlot());
        if (type != null) {
            player.sendMessage(ChatColor.GREEN + "Selected " + type.name());
            player.closeInventory();
        }
    }

    private static class Holder implements InventoryHolder {
        @Override
        public Inventory getInventory() { return null; }
    }
}
