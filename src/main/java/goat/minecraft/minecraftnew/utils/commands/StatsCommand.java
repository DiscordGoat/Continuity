package goat.minecraft.minecraftnew.utils.commands;

import goat.minecraft.minecraftnew.utils.stats.StatsCalculator;
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
        this.calculator = StatsCalculator.getInstance(plugin);
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
        Inventory inv = Bukkit.createInventory(null, 45, ChatColor.DARK_GREEN + "Player Stats");

        ItemStack filler = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta fmeta = filler.getItemMeta();
        if (fmeta != null) {
            fmeta.setDisplayName(" ");
            filler.setItemMeta(fmeta);
        }

        for (int i = 0; i < 9; i++) {
            inv.setItem(i, filler);
            inv.setItem(36 + i, filler);
        }
        for (int r = 1; r < 4; r++) {
            inv.setItem(r * 9, filler);
            inv.setItem(r * 9 + 8, filler);
        }

        int[] slots = {10,11,12,13,14,15,16,19,20,21,22,23,24,25,28,29,30,31,32,33,34};
        int index = 0;
        addStatItem(inv, slots[index++], Material.REDSTONE, "Health", String.format("%.1f", calculator.getHealth(player)));
        addStatItem(inv, slots[index++], Material.IRON_SWORD, "Damage +%", String.format("%.1f%%", calculator.getDamageIncrease(player)));
        addStatItem(inv, slots[index++], Material.BOW, "Arrow Damage +%", String.format("%.1f%%", calculator.getArrowDamageIncrease(player)));
        addStatItem(inv, slots[index++], Material.SHIELD, "Resistance", String.format("%.1f%%", calculator.getResistance(player)));
        addStatItem(inv, slots[index++], Material.ELYTRA, "Flight Distance", String.format("%.2f km", calculator.getFlightDistance(player)));
        addStatItem(inv, slots[index++], Material.SOUL_TORCH, "Grave Chance", String.format("%.3f%%", calculator.getGraveChance(player)));
        addStatItem(inv, slots[index++], Material.NAUTILUS_SHELL, "Sea Creature Chance", String.format("%.2f%%", calculator.getSeaCreatureChance(player)));
        addStatItem(inv, slots[index++], Material.CHEST, "Treasure Chance", String.format("%.2f%%", calculator.getTreasureChance(player)));
        addStatItem(inv, slots[index++], Material.SOUL_LANTERN, "Spirit Chance", String.format("%.2f%%", calculator.getSpiritChance(player)));
        addStatItem(inv, slots[index++], Material.EMERALD, "Discount", String.format("%.2f%%", calculator.getDiscount(player)));
        addStatItem(inv, slots[index++], Material.FEATHER, "Speed", String.format("%.1f%%", calculator.getSpeed(player)));
        addStatItem(inv, slots[index++], Material.POTION, "Brew Time Reduction", String.format("%.1f%%", calculator.getBrewTimeReduction(player)));
        addStatItem(inv, slots[index++], Material.DIAMOND_ORE, "Double Ore Chance", String.format("%.1f%%", calculator.getDoubleOreChance(player)));
        addStatItem(inv, slots[index++], Material.OAK_LOG, "Double Log Chance", String.format("%.1f%%", calculator.getDoubleLogChance(player)));
        addStatItem(inv, slots[index++], Material.WHEAT, "Double Crop Chance", String.format("%.1f%%", calculator.getDoubleCropChance(player)));
        addStatItem(inv, slots[index++], Material.ANVIL, "Repair Amount", String.format("%.1f", calculator.getRepairAmount(player)));
        addStatItem(inv, slots[index++], Material.ENCHANTED_BOOK, "Repair Quality", String.format("%.1f", calculator.getRepairQuality(player)));

        player.openInventory(inv);
    }

    private void addStatItem(Inventory inv, int slot, Material mat, String name, String value) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.GOLD + name);
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.YELLOW + value);
            lore.add(ChatColor.DARK_GRAY + "Calculated via StatsCalculator");
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        inv.setItem(slot, item);
    }
}
