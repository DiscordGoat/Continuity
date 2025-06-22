package goat.minecraft.minecraftnew.subsystems.generator;

import goat.minecraft.minecraftnew.utils.devtools.XPManager;
import goat.minecraft.minecraftnew.MinecraftNew;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OreFabricatorGUI implements Listener {
    private final JavaPlugin plugin;
    private final XPManager xpManager;
    private final String guiTitle = ChatColor.DARK_RED + "Ore Fabricator";

    // Map ore materials to required mining levels
    private static final Map<Material, Integer> ORE_LEVELS = new HashMap<>();
    static {
        ORE_LEVELS.put(Material.COPPER_ORE, 20);
        ORE_LEVELS.put(Material.COAL_ORE, 40);
        ORE_LEVELS.put(Material.IRON_ORE, 60);
        ORE_LEVELS.put(Material.GOLD_ORE, 80);
        ORE_LEVELS.put(Material.REDSTONE_ORE, 100);
        ORE_LEVELS.put(Material.LAPIS_ORE, 100);
        ORE_LEVELS.put(Material.DIAMOND_ORE, 100);
        ORE_LEVELS.put(Material.EMERALD_ORE, 100);
    }

    public OreFabricatorGUI(JavaPlugin plugin) {
        this.plugin = plugin;
        this.xpManager = new XPManager(MinecraftNew.getInstance());
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public void open(Player player) {
        Inventory gui = Bukkit.createInventory(null, 27, guiTitle);

        int miningLevel = xpManager.getPlayerLevel(player, "Mining");

        int slot = 0;
        for (Map.Entry<Material, Integer> entry : ORE_LEVELS.entrySet()) {
            ItemStack item = new ItemStack(entry.getKey());
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(ChatColor.YELLOW + formatName(entry.getKey()));
            List<String> lore = new ArrayList<>();
            if (miningLevel < entry.getValue()) {
                lore.add(ChatColor.RED + "Requires Mining " + entry.getValue());
            }
            meta.setLore(lore);
            item.setItemMeta(meta);
            gui.setItem(slot++, item);
        }

        // Reserve slots for Redstone Gems (row 2)
        for (int i = 9; i < 18; i++) {
            ItemStack gemSlot = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
            ItemMeta meta = gemSlot.getItemMeta();
            meta.setDisplayName(ChatColor.RED + "Redstone Gem Slot");
            gemSlot.setItemMeta(meta);
            gui.setItem(i, gemSlot);
        }

        // Begin Fabrication button (bottom right)
        ItemStack begin = new ItemStack(Material.GREEN_WOOL);
        ItemMeta beginMeta = begin.getItemMeta();
        beginMeta.setDisplayName(ChatColor.GREEN + "Begin Fabrication");
        begin.setItemMeta(beginMeta);
        gui.setItem(26, begin);

        player.openInventory(gui);
    }

    private String formatName(Material mat) {
        String name = mat.name().toLowerCase().replace('_', ' ');
        String[] parts = name.split(" ");
        StringBuilder builder = new StringBuilder();
        for (String p : parts) {
            builder.append(Character.toUpperCase(p.charAt(0))).append(p.substring(1)).append(" ");
        }
        return builder.toString().trim();
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        if (!event.getView().getTitle().equals(guiTitle)) return;
        // Prevent taking preset items (ore buttons & begin button)
        int slot = event.getRawSlot();
        if (slot < 9 || slot == 26) {
            event.setCancelled(true);
        }
    }
}
