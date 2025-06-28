package goat.minecraft.minecraftnew.other.trinkets;

import goat.minecraft.minecraftnew.other.additionalfunctionality.CustomBundleGUI;
import goat.minecraft.minecraftnew.MinecraftNew;
import goat.minecraft.minecraftnew.other.trinkets.PotionPouchManager;
import goat.minecraft.minecraftnew.other.trinkets.TransfigurationPouchManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.ArrayList;
import java.util.List;

public class TrinketManager implements Listener {
    private static TrinketManager instance;

    public static void init(JavaPlugin plugin) {
        if (instance == null) {
            instance = new TrinketManager();
            Bukkit.getPluginManager().registerEvents(instance, plugin);
        }
    }

    public static TrinketManager getInstance() {
        return instance;
    }

    @EventHandler
    public void onBackpackClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().equals("Backpack")) return;
        ItemStack item = event.getCurrentItem();
        if (item == null || item.getType() == Material.AIR) return;
        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) return;
        String name = ChatColor.stripColor(meta.getDisplayName());
        Player player = (Player) event.getWhoClicked();

        switch (name) {
            case "Workbench Trinket" -> {
                if (event.getClick().isLeftClick()) {
                    player.openWorkbench(null, true);
                    event.setCancelled(true);
                }
            }
            case "Anvil Trinket" -> {
                if (event.getClick().isLeftClick()) {
                    MinecraftNew.getInstance().getAnvilRepair().openAnvilGui(player);
                    event.setCancelled(true);
                }
            }
            case "Divider Trinket" -> {
                if (!event.getClick().isRightClick()) {
                    event.setCancelled(true);
                }
            }
            case "Bank Account" -> {
                if (event.getClick() == ClickType.LEFT) {
                    int deposited = BankAccountManager.getInstance().depositAll(player);
                    updateBankLore(item, BankAccountManager.getInstance().getBalance(player.getUniqueId()));
                    refreshBankLore(player);
                    player.sendMessage(ChatColor.GREEN + "Deposited " + deposited + " emeralds.");
                    event.setCancelled(true);
                } else if (event.getClick() == ClickType.SHIFT_RIGHT) {
                    int amount = BankAccountManager.getInstance().withdrawAll(player);
                    dropEmeralds(player, amount);
                    updateBankLore(item, 0);
                    refreshBankLore(player);
                    event.setCancelled(true);
                }
            }
            case "Blue Satchel" -> {
                if (event.getClick().isLeftClick()) {
                    SatchelManager.getInstance().openSatchel(player, "Blue");
                    event.setCancelled(true);
                }
            }
            case "Black Satchel" -> {
                if (event.getClick().isLeftClick()) {
                    SatchelManager.getInstance().openSatchel(player, "Black");
                    event.setCancelled(true);
                }
            }
            case "Green Satchel" -> {
                if (event.getClick().isLeftClick()) {
                    SatchelManager.getInstance().openSatchel(player, "Green");
                    event.setCancelled(true);
                }
            }
            case "Pouch of Potions" -> {
                if (event.getClick() == ClickType.LEFT) {
                    PotionPouchManager.getInstance().depositPotions(player);
                    PotionPouchManager.getInstance().refreshPouchLore(player);
                    event.setCancelled(true);
                } else if (event.getClick() == ClickType.SHIFT_RIGHT) {
                    PotionPouchManager.getInstance().openPouch(player);
                    event.setCancelled(true);
                }
            }
            case "Pouch of Culinary Delights" -> {
                if (event.getClick() == ClickType.LEFT) {
                    CulinaryPouchManager.getInstance().depositDelights(player);
                    CulinaryPouchManager.getInstance().refreshPouchLore(player);
                    event.setCancelled(true);
                } else if (event.getClick() == ClickType.SHIFT_RIGHT) {
                    CulinaryPouchManager.getInstance().openPouch(player);
                    event.setCancelled(true);
                }
            }
            case "Pouch of Seeds" -> {
                if (event.getClick() == ClickType.LEFT) {
                    SeedPouchManager.getInstance().depositSeeds(player);
                    SeedPouchManager.getInstance().refreshPouchLore(player);
                    event.setCancelled(true);
                } else if (event.getClick() == ClickType.SHIFT_RIGHT) {
                    SeedPouchManager.getInstance().openPouch(player);
                    event.setCancelled(true);
                }
            }
            case "Transfiguration Pouch" -> {
                if (event.getClick() == ClickType.LEFT) {
                    TransfigurationPouchManager.getInstance().depositItems(player);
                    TransfigurationPouchManager.getInstance().refreshLore(player);
                    event.setCancelled(true);
                } else if (event.getClick() == ClickType.SHIFT_LEFT) {
                    TransfigurationPouchManager.getInstance().convertToXP(player);
                    event.setCancelled(true);
                } else if (event.getClick() == ClickType.SHIFT_RIGHT) {
                    TransfigurationPouchManager.getInstance().openPouch(player);
                    event.setCancelled(true);
                }
            }
            case "Enchanted Lava Bucket" -> {
                if (event.getClick() == ClickType.LEFT && event.getCursor() != null && event.getCursor().getType() != Material.AIR) {
                    event.getWhoClicked().setItemOnCursor(null);
                    event.setCancelled(true);
                } else if (event.getClick() == ClickType.SHIFT_RIGHT) {
                    LavaBucketManager.getInstance().openTrash(player);
                    event.setCancelled(true);
                }
            }
        }
    }

    private void dropEmeralds(Player player, int amount) {
        while (amount >= 64) {
            player.getWorld().dropItemNaturally(player.getLocation(), new ItemStack(Material.EMERALD, 64));
            amount -= 64;
        }
        if (amount > 0) {
            player.getWorld().dropItemNaturally(player.getLocation(), new ItemStack(Material.EMERALD, amount));
        }
    }

    private void updateBankLore(ItemStack item, int balance) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + "Stores emeralds safely");
        lore.add(ChatColor.BLUE + "Left-click" + ChatColor.GRAY + ": Deposit all");
        lore.add(ChatColor.BLUE + "Shift-Right-click" + ChatColor.GRAY + ": Withdraw all");
        lore.add(ChatColor.GRAY + "Balance: " + ChatColor.GREEN + balance + ChatColor.GRAY + " emeralds");
        meta.setLore(lore);
        item.setItemMeta(meta);
    }

    public void refreshBankLore(Player player) {
        int balance = BankAccountManager.getInstance().getBalance(player.getUniqueId());
        for (ItemStack stack : player.getInventory().getContents()) {
            if (stack == null) continue;
            ItemMeta meta = stack.getItemMeta();
            if (meta == null || !meta.hasDisplayName()) continue;
            if (ChatColor.stripColor(meta.getDisplayName()).equals("Bank Account")) {
                updateBankLore(stack, balance);
            }
        }
        player.updateInventory();
        CustomBundleGUI.getInstance().refreshBankLoreInStorage(player, balance);
    }

    private void updatePouchLore(ItemStack item, int count) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + "Stores Verdant Relic seeds");
        lore.add(ChatColor.BLUE + "Left-click" + ChatColor.GRAY + ": Store seeds");
        lore.add(ChatColor.BLUE + "Shift-Right-click" + ChatColor.GRAY + ": Open pouch");
        lore.add(ChatColor.GRAY + "Seeds: " + ChatColor.GREEN + count);
        meta.setLore(lore);
        item.setItemMeta(meta);
    }

    private void updatePotionPouchLore(ItemStack item, int count) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + "Stores potions");
        lore.add(ChatColor.BLUE + "Left-click" + ChatColor.GRAY + ": Store potions");
        lore.add(ChatColor.BLUE + "Shift-Right-click" + ChatColor.GRAY + ": Open pouch");
        lore.add(ChatColor.GRAY + "Potions: " + ChatColor.GREEN + count);
        meta.setLore(lore);
        item.setItemMeta(meta);
    }

    private void updateCulinaryPouchLore(ItemStack item, int count) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + "Stores Culinary Delights");
        lore.add(ChatColor.BLUE + "Left-click" + ChatColor.GRAY + ": Store delights");
        lore.add(ChatColor.BLUE + "Shift-Right-click" + ChatColor.GRAY + ": Open pouch");
        lore.add(ChatColor.GRAY + "Delights: " + ChatColor.GREEN + count);
        meta.setLore(lore);
        item.setItemMeta(meta);
    }

    public void refreshPouchLore(Player player) {
        int count = SeedPouchManager.getInstance().countSeeds(player.getUniqueId());
        for (ItemStack stack : player.getInventory().getContents()) {
            if (stack == null) continue;
            ItemMeta meta = stack.getItemMeta();
            if (meta == null || !meta.hasDisplayName()) continue;
            if (ChatColor.stripColor(meta.getDisplayName()).equals("Pouch of Seeds")) {
                updatePouchLore(stack, count);
            }
        }
        player.updateInventory();
    }

    public void refreshPotionPouchLore(Player player) {
        int count = PotionPouchManager.getInstance().countPotions(player.getUniqueId());
        for (ItemStack stack : player.getInventory().getContents()) {
            if (stack == null) continue;
            ItemMeta meta = stack.getItemMeta();
            if (meta == null || !meta.hasDisplayName()) continue;
            if (ChatColor.stripColor(meta.getDisplayName()).equals("Pouch of Potions")) {
                updatePotionPouchLore(stack, count);
            }
        }
        player.updateInventory();
    }

    public void refreshCulinaryPouchLore(Player player) {
        int count = CulinaryPouchManager.getInstance().countDelights(player.getUniqueId());
        for (ItemStack stack : player.getInventory().getContents()) {
            if (stack == null) continue;
            ItemMeta meta = stack.getItemMeta();
            if (meta == null || !meta.hasDisplayName()) continue;
            if (ChatColor.stripColor(meta.getDisplayName()).equals("Pouch of Culinary Delights")) {
                updateCulinaryPouchLore(stack, count);
            }
        }
        player.updateInventory();
    }
}
