package goat.minecraft.minecraftnew.utils.commands;

import goat.minecraft.minecraftnew.utils.stats.StatsCalculator;
import goat.minecraft.minecraftnew.utils.stats.PlayerResistanceManager; // ensure loaded
import goat.minecraft.minecraftnew.utils.devtools.XPManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
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
 * Opens a GUI displaying various aggregated player statistics.
 */
public class StatsCommand implements CommandExecutor, Listener {

    private final JavaPlugin plugin;
    private final StatsCalculator calculator;

    public StatsCommand(JavaPlugin plugin) {
        this.plugin = plugin;
        this.calculator = new StatsCalculator(plugin);
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        if (ChatColor.stripColor(event.getView().getTitle()).equals("Player Stats")) {
            event.setCancelled(true);
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command.");
            return true;
        }
        openStatsGUI(player);
        return true;
    }

    private void openStatsGUI(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, ChatColor.DARK_GREEN + "Player Stats");

        addStatItem(inv, 0, Material.REDSTONE, "Health", String.format("%.1f", calculator.getHealth(player)));
        addStatItem(inv, 1, Material.IRON_SWORD, "Damage +%", String.format("%.1f%%", calculator.getDamageIncrease(player)));
        addStatItem(inv, 2, Material.BOW, "Arrow Damage +%", String.format("%.1f%%", calculator.getArrowDamageIncrease(player)));
        addStatItem(inv, 3, Material.SHIELD, "Resistance", String.format("%.1f%%", calculator.getResistance(player)));
        addStatItem(inv, 4, Material.ELYTRA, "Flight Distance", String.format("%.2f km", calculator.getFlightDistance(player)));
        addStatItem(inv, 5, Material.SOUL_TORCH, "Grave Chance", String.format("%.3f%%", calculator.getGraveChance(player)));
        addStatItem(inv, 6, Material.NAUTILUS_SHELL, "Sea Creature Chance", String.format("%.2f%%", calculator.getSeaCreatureChance(player)));
        addStatItem(inv, 7, Material.CHEST, "Treasure Chance", String.format("%.2f%%", calculator.getTreasureChance(player)));
        addStatItem(inv, 8, Material.SOUL_LANTERN, "Spirit Chance", String.format("%.2f%%", calculator.getSpiritChance(player)));
        addStatItem(inv, 9, Material.EMERALD, "Discount", String.format("%.2f%%", calculator.getDiscount(player)));
        addStatItem(inv, 10, Material.FEATHER, "Speed", String.format("%.1f%%", calculator.getSpeed(player)));
        addStatItem(inv, 11, Material.POTION, "Brew Time Reduction", String.format("%.1f%%", calculator.getBrewTimeReduction(player)));
        addStatItem(inv, 12, Material.DIAMOND_ORE, "Double Ore Chance", String.format("%.1f%%", calculator.getDoubleOreChance(player)));
        addStatItem(inv, 13, Material.OAK_LOG, "Double Log Chance", String.format("%.1f%%", calculator.getDoubleLogChance(player)));
        addStatItem(inv, 14, Material.WHEAT, "Double Crop Chance", String.format("%.1f%%", calculator.getDoubleCropChance(player)));
        addStatItem(inv, 15, Material.ANVIL, "Repair Amount", String.format("%.1f", calculator.getRepairAmount(player)));
        addStatItem(inv, 16, Material.ENCHANTED_BOOK, "Repair Quality", String.format("%.1f", calculator.getRepairQuality(player)));

        player.openInventory(inv);
    }

    private void addStatItem(Inventory inv, int slot, Material mat, String name, String value) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.GOLD + name);
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.YELLOW + value);
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        inv.setItem(slot, item);
    }
}
