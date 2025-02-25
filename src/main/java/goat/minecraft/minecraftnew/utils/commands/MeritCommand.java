package goat.minecraft.minecraftnew.utils.commands;

import goat.minecraft.minecraftnew.utils.devtools.PlayerDataManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;


import java.util.ArrayList;
import java.util.List;

public class MeritCommand implements CommandExecutor, Listener {

    private final JavaPlugin plugin;
    private final PlayerDataManager playerData;

    public MeritCommand(JavaPlugin plugin, PlayerDataManager playerData) {
        this.plugin = plugin;
        this.playerData = playerData;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be used by a player!");
            return true;
        }
        Player player = (Player) sender;
        openMeritGUI(player);
        return true;
    }

    /**
     * Create and open the 54-slot "Merit" GUI for the player.
     */
    private void openMeritGUI(Player player) {
        Inventory inv = Bukkit.createInventory(null, 54, ChatColor.DARK_GREEN + "Merits Menu");

        // === FIRST ROW (index 0..8) ===
        // Fill with black stained glass, except index 4 which holds the diamond
        ItemStack blackGlass = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta blackGlassMeta = blackGlass.getItemMeta();
        blackGlassMeta.setDisplayName(ChatColor.BLACK + "");
        blackGlass.setItemMeta(blackGlassMeta);

        for (int i = 0; i < 9; i++) {
            inv.setItem(i, blackGlass.clone());
        }

        // Diamond in center (index 4) to show available points
        int currentMeritPoints = playerData.getMeritPoints(player.getUniqueId());
        ItemStack diamond = new ItemStack(Material.DIAMOND);
        ItemMeta diamondMeta = diamond.getItemMeta();
        diamondMeta.setDisplayName(ChatColor.AQUA + "Available Merit Points: " + currentMeritPoints);
        diamond.setItemMeta(diamondMeta);
        inv.setItem(4, diamond);

        // === REMAINING 45 SLOTS (index 9..53) ===
        // For demonstration, we’ll add some filler perks with random costs
        // In a real scenario, you’d load perk data from a config file, or define a separate class.
        int slotIndex = 9;
        for (int i = 1; i <= 45; i++) {
            // Example perk data
            String perkTitle = "Perk #" + i;
            int cost = i; // just as an example

            ItemStack perkItem = new ItemStack(Material.PAPER);
            ItemMeta perkMeta = perkItem.getItemMeta();
            perkMeta.setDisplayName(ChatColor.GOLD + perkTitle);

            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.YELLOW + "Cost: " + cost);
            lore.add(ChatColor.GRAY + "This is a filler perk. Just an example.");
            perkMeta.setLore(lore);

            perkItem.setItemMeta(perkMeta);
            inv.setItem(slotIndex, perkItem);
            slotIndex++;
        }

        player.openInventory(inv);
    }

    /**
     * Handles clicks within the Merit GUI.
     */
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();

        // Check if it's our custom inventory by matching title (do it however you prefer)
        if (event.getView().getTitle().equals(ChatColor.DARK_GREEN + "Merits Menu")) {
            event.setCancelled(true); // prevent item pickup/moving

            ItemStack clickedItem = event.getCurrentItem();
            if (clickedItem == null || clickedItem.getType() == Material.AIR) return;

            // If player clicks the diamond, ignore
            if (clickedItem.getType() == Material.DIAMOND) {
                player.sendMessage(ChatColor.GREEN + "You have "
                        + playerData.getMeritPoints(player.getUniqueId()) + " available merit points.");
                return;
            }

            // If player clicked on one of the perk items
            if (clickedItem.getType() == Material.PAPER) {
                ItemMeta meta = clickedItem.getItemMeta();
                if (meta == null) return;
                String perkTitle = ChatColor.stripColor(meta.getDisplayName());

                // Extract cost from lore
                int cost = 0;
                if (meta.getLore() != null && !meta.getLore().isEmpty()) {
                    for (String line : meta.getLore()) {
                        if (ChatColor.stripColor(line).startsWith("Cost: ")) {
                            try {
                                cost = Integer.parseInt(
                                        ChatColor.stripColor(line)
                                                .replace("Cost: ", "")
                                );
                            } catch (NumberFormatException e) {
                                // Just ignore or handle quietly
                            }
                            break;
                        }
                    }
                }

                // Check if player can afford
                int currentPoints = playerData.getMeritPoints(player.getUniqueId());
                if (currentPoints < cost) {
                    player.sendMessage(ChatColor.RED + "You do not have enough merit points to purchase this perk.");
                    return;
                }

                // Check if already purchased
                if (playerData.hasPerk(player.getUniqueId(), perkTitle)) {
                    player.sendMessage(ChatColor.RED + "You have already purchased this perk!");
                    return;
                }

                // Deduct cost, add perk to the player's purchased list
                playerData.setMeritPoints(player.getUniqueId(), currentPoints - cost);
                playerData.addPerk(player.getUniqueId(), perkTitle);

                player.sendMessage(ChatColor.GREEN + "You purchased perk: " + perkTitle
                        + " for " + cost + " merit points!");

                // Reopen or update the GUI to reflect new points
                openMeritGUI(player);
            }
        }
    }
}
