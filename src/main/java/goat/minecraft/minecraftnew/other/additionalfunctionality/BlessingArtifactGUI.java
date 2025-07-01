package goat.minecraft.minecraftnew.other.additionalfunctionality;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public class BlessingArtifactGUI implements Listener {
    private static final String GUI_TITLE = ChatColor.DARK_PURPLE + "Select Blessing";
    private final JavaPlugin plugin;

    public BlessingArtifactGUI(JavaPlugin plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    private ItemStack createChoice(Material material, String name) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            item.setItemMeta(meta);
        }
        return item;
    }

    private void openGUI(Player player) {
        Inventory inv = Bukkit.createInventory(null, 9, GUI_TITLE);
        inv.setItem(2, createChoice(Material.FEATHER, ChatColor.GREEN + "Feather Armor"));
        inv.setItem(4, createChoice(Material.SHIELD, ChatColor.GREEN + "Shield Armor"));
        inv.setItem(6, createChoice(Material.SUGAR, ChatColor.GREEN + "Speed Armor"));
        Inventory inv = Bukkit.createInventory(null, 18, GUI_TITLE);
        inv.setItem(0, createChoice(Material.BOW, ChatColor.GREEN + "Lost Legion"));
        inv.setItem(1, createChoice(Material.DIAMOND_CHESTPLATE, ChatColor.GREEN + "Monolith"));
        inv.setItem(2, createChoice(Material.NETHERRACK, ChatColor.GREEN + "Scorchsteel"));
        inv.setItem(3, createChoice(Material.BEDROCK, ChatColor.GREEN + "Dweller"));
        inv.setItem(4, createChoice(Material.HAY_BLOCK, ChatColor.GREEN + "Pastureshade"));
        inv.setItem(5, createChoice(Material.IRON_AXE, ChatColor.GREEN + "Nature's Wrath"));
        inv.setItem(6, createChoice(Material.SHIELD, ChatColor.GREEN + "Countershot"));
        inv.setItem(7, createChoice(Material.ENDER_PEARL, ChatColor.GREEN + "Shadowstep"));
        inv.setItem(8, createChoice(Material.FEATHER, ChatColor.GREEN + "Strider"));
        inv.setItem(9, createChoice(Material.DIAMOND_SWORD, ChatColor.GREEN + "Slayer"));
        inv.setItem(10, createChoice(Material.ENDER_EYE, ChatColor.GREEN + "Duskblood"));
        inv.setItem(11, createChoice(Material.LIGHTNING_ROD, ChatColor.GREEN + "Thunderforge"));
        inv.setItem(12, createChoice(Material.SEA_LANTERN, ChatColor.GREEN + "Fathmic Iron"));
        player.openInventory(inv);
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK)
            return;
        ItemStack item = event.getItem();
        if (item == null || item.getType() == Material.AIR) return;
        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) return;
        if (!meta.getDisplayName().equals(ChatColor.YELLOW + "Blessing Artifact")) return;
        event.setCancelled(true);
        openGUI(event.getPlayer());
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().equals(GUI_TITLE)) return;
        event.setCancelled(true);
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;
        ItemMeta meta = clicked.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) return;
        String blessing = ChatColor.stripColor(meta.getDisplayName());
        Player player = (Player) event.getWhoClicked();
        ItemStack artifact = player.getInventory().getItemInMainHand();
        if (artifact == null || !artifact.hasItemMeta()) return;
        ItemMeta artMeta = artifact.getItemMeta();
        if (artMeta == null || !artMeta.hasDisplayName()) return;
        if (!artMeta.getDisplayName().equals(ChatColor.YELLOW + "Blessing Artifact")) return;
        artMeta.setDisplayName(ChatColor.YELLOW + "Blessing Artifact - " + blessing);
        artMeta.setLore(List.of(ChatColor.GRAY + "Blessing: " + blessing));
        artifact.setItemMeta(artMeta);
        player.closeInventory();
    }
}
