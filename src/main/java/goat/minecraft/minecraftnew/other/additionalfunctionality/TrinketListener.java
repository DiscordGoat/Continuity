package goat.minecraft.minecraftnew.other.additionalfunctionality;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class TrinketListener implements Listener {

    private final BankAccountManager bankAccountManager;

    public TrinketListener() {
        this.bankAccountManager = BankAccountManager.getInstance();
    }

    @EventHandler
    public void onBackpackClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().equals("Backpack")) return;

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || !clicked.hasItemMeta()) return;

        ItemMeta meta = clicked.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) return;

        String name = ChatColor.stripColor(meta.getDisplayName());
        Player player = (Player) event.getWhoClicked();
        ClickType click = event.getClick();

        switch (name) {
            case "Workbench":
                if (click.isLeftClick()) {
                    event.setCancelled(true);
                    player.openWorkbench(player.getLocation(), true);
                }
                break;
            case "Divider":
                if (click.isLeftClick()) {
                    event.setCancelled(true);
                }
                break;
            case "Bank Account":
                if (click == ClickType.SHIFT_RIGHT) {
                    event.setCancelled(true);
                    int amount = bankAccountManager.withdrawAll(player.getUniqueId());
                    dropEmeralds(player, amount);
                    updateBankLore(clicked, meta, player);
                } else if (click.isLeftClick()) {
                    event.setCancelled(true);
                    int total = extractEmeraldsFromInventory(player);
                    total += CustomBundleGUI.getInstance().removeAllEmeraldItems(player);
                    bankAccountManager.deposit(player.getUniqueId(), total);
                    updateBankLore(clicked, meta, player);
                    if (total > 0) {
                        player.sendMessage(ChatColor.GREEN + "Deposited " + total + " emeralds.");
                    }
                }
                break;
            default:
                break;
        }
    }

    private void updateBankLore(ItemStack item, ItemMeta meta, Player player) {
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + "Stored: " + bankAccountManager.getBalance(player.getUniqueId()));
        meta.setLore(lore);
        item.setItemMeta(meta);
    }

    private int extractEmeraldsFromInventory(Player player) {
        int total = 0;
        Inventory inv = player.getInventory();
        for (int i = 0; i < inv.getSize(); i++) {
            ItemStack stack = inv.getItem(i);
            if (stack == null) continue;
            if (stack.getType() == Material.EMERALD) {
                total += stack.getAmount();
                inv.setItem(i, null);
            } else if (stack.getType() == Material.EMERALD_BLOCK) {
                total += stack.getAmount() * 9;
                inv.setItem(i, null);
            }
        }
        return total;
    }

    private void dropEmeralds(Player player, int amount) {
        while (amount > 0) {
            int give = Math.min(64, amount);
            player.getWorld().dropItemNaturally(player.getLocation(), new ItemStack(Material.EMERALD, give));
            amount -= give;
        }
    }
}
