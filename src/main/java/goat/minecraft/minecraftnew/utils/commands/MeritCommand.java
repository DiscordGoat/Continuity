package goat.minecraft.minecraftnew.utils.commands;

import goat.minecraft.minecraftnew.utils.devtools.PlayerMeritManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MeritCommand implements CommandExecutor, Listener {

    private final JavaPlugin plugin;
    private final PlayerMeritManager playerData;

    /**
     * Simple container for perk data: supports & color codes in name,
     * a cost, a custom Material icon, and a description as a list of lore lines.
     */
    private static class Perk {
        private final String name;     // Can include & color codes
        private final int cost;
        private final Material icon;
        private final List<String> description;

        public Perk(String name, int cost, Material icon, List<String> description) {
            this.name = name;
            this.cost = cost;
            this.icon = icon;
            this.description = description;
        }

        public String getName() {
            return name;
        }

        public int getCost() {
            return cost;
        }

        public Material getIcon() {
            return icon;
        }

        public List<String> getDescription() {
            return description;
        }
    }

    /**
     * 45 perks from the concept list.
     * Edit the text, chat colors, materials, and descriptions as needed.
     */
    private static final List<Perk> perks = Arrays.asList(
            new Perk(ChatColor.DARK_GRAY + "Shulkl Box", 7, Material.SHULKER_SHELL,
                    Arrays.asList(
                            ChatColor.GRAY + "Allows opening Shulker Boxes only inside backpacks.",
                            ChatColor.BLUE + "Right Click Shulker Box in Backpack: " + ChatColor.GRAY + "Opens it."
                    )),
            new Perk(ChatColor.DARK_GRAY + "Motion Sensor", 1, Material.OAK_DOOR,
                    Arrays.asList(
                            ChatColor.GRAY + "Enables Autoclose doors.",
                            ChatColor.BLUE + "On Left Click Door: " + ChatColor.GRAY + "Opens doors for 2 seconds."
                    )),
            new Perk(ChatColor.DARK_GRAY + "QuickSwap", 3, Material.TRIPWIRE_HOOK,
                    Arrays.asList(
                            ChatColor.GRAY + "Replenishes your supply of blocks.",
                            ChatColor.BLUE + "On Place Block: " + ChatColor.GRAY + "Replenishes your block supply."
                    )),
            new Perk(ChatColor.DARK_GRAY + "Restock", 1, Material.ARROW,
                    Arrays.asList(
                            ChatColor.GRAY + "Grants arrows when holding a bow with none left.",
                            ChatColor.BLUE + "On Out Of Arrows While Bow Equipped: " + ChatColor.GRAY + "Gives one arrow."
                    )),
            new Perk(ChatColor.DARK_GRAY + "Rebreather", 2, Material.TURTLE_HELMET,
                    Arrays.asList(
                            ChatColor.GRAY + "Regenerates breath underwater below Y=50.",
                            ChatColor.BLUE + "When Underwater Below Y=50: " + ChatColor.GRAY + "+1 Oxygen every 3s."
                    )),
            new Perk(ChatColor.DARK_GRAY + "Keepinventory", 3, Material.CHEST,
                    Arrays.asList(
                            ChatColor.GRAY + "Keeps your items on death after login.",
                            ChatColor.BLUE + "On Login: " + ChatColor.GRAY + "Keep inventory enabled for that session."
                    )),
            new Perk(ChatColor.DARK_GRAY + "Master Trader", 3, Material.EMERALD,
                    Arrays.asList(
                            ChatColor.GRAY + "5% chance for purchases to be free.",
                            ChatColor.BLUE + "On Purchase: " + ChatColor.GRAY + "5% chance to cost nothing."
                    )),
            new Perk(ChatColor.DARK_GRAY + "Haggler", 5, Material.EMERALD,
                    Arrays.asList(
                            ChatColor.GRAY + "Grants 10% discount on villager trades.",
                            ChatColor.BLUE + "On Villager Trade: " + ChatColor.GRAY + "Prices reduced by 10%."
                    )),
            new Perk(ChatColor.DARK_GRAY + "Deal", 2, Material.EMERALD_BLOCK,
                    Arrays.asList(
                            ChatColor.GRAY + "+25% emeralds when selling items.",
                            ChatColor.BLUE + "On Sell: " + ChatColor.GRAY + "+25% emerald reward."
                    )),
            new Perk(ChatColor.DARK_GRAY + "Master Employer", 3, Material.BELL,
                    Arrays.asList(
                            ChatColor.GRAY + "50% chance to halve villager work timers.",
                            ChatColor.BLUE + "On Workcycle: " + ChatColor.GRAY + "50% chance next timer reduced 50%."
                    )),
            new Perk(ChatColor.DARK_GRAY + "Icarus", 2, Material.ELYTRA,
                    Arrays.asList(
                            ChatColor.GRAY + "Doubles flight pet distance.",
                            ChatColor.BLUE + "On Flight Pet Use: " + ChatColor.GRAY + "Flying limit doubled."
                    )),
            new Perk(ChatColor.DARK_GRAY + "Tuxedo", 4, Material.BLACK_WOOL,
                    Arrays.asList(
                            ChatColor.GRAY + "Displays two extra auction items.",
                            ChatColor.BLUE + "On Auction: " + ChatColor.GRAY + "2 bonus items with rare chance"
                    ))
            );



    public MeritCommand(JavaPlugin plugin, PlayerMeritManager playerData) {
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
        openMeritGUI(player, 1);
        return true;
    }

    /**
     * Create and open the 54-slot "Merit" GUI for the player.
     */
    private void openMeritGUI(Player player) {
        openMeritGUI(player, 1);
    }

    private void openMeritGUI(Player player, int page) {
        int totalPages = (int) Math.ceil(perks.size() / 40.0);
        if (page < 1) page = 1;
        if (page > totalPages) page = totalPages;

        Inventory inv = Bukkit.createInventory(null, 54,
                ChatColor.DARK_GREEN + "Merits: Page " + page + "/" + totalPages);

        // === prepare a black‐pane template ===
        ItemStack blackGlass = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta blackMeta = blackGlass.getItemMeta();
        blackMeta.setDisplayName(ChatColor.BLACK + "");
        blackGlass.setItemMeta(blackMeta);

        // === fill top row (0–8) with black panes ===
        for (int i = 0; i < 9; i++) {
            inv.setItem(i, blackGlass.clone());
        }

        // === fill left column (slots 9,18,27,36,45) with black panes ===
        for (int row = 0; row < 5; row++) {
            inv.setItem(9 + row * 9, blackGlass.clone());
        }

        // === place page selectors in the top row ===
        for (int p = 1; p <= totalPages && p <= 8; p++) {
            ItemStack icon = new ItemStack(Material.PAPER);
            ItemMeta im = icon.getItemMeta();
            im.setDisplayName(ChatColor.GOLD + "Page " + p);
            icon.setItemMeta(im);
            inv.setItem(p - 1, icon);
        }

        // === diamond in slot 8 to show available points ===
        int pts = playerData.getMeritPoints(player.getUniqueId());
        ItemStack diamond = new ItemStack(Material.DIAMOND);
        ItemMeta dmeta = diamond.getItemMeta();
        dmeta.setDisplayName(ChatColor.AQUA + "Available Merit Points: " + pts);
        diamond.setItemMeta(dmeta);
        inv.setItem(8, diamond);

        // === populate perks starting just below the top row ===
        int startIndex = (page - 1) * 40;
        int endIndex = Math.min(perks.size(), startIndex + 40);
        int slotIndex = 9;  // first slot of second row
        for (int i = startIndex; i < endIndex; i++) {
            Perk perk = perks.get(i);
            // skip the left‐column pane
            if (slotIndex % 9 == 0) slotIndex++;
            if (slotIndex >= 54) break;

            ItemStack perkItem = new ItemStack(perk.getIcon());
            ItemMeta pm = perkItem.getItemMeta();
            String name = ChatColor.translateAlternateColorCodes('&', perk.getName());
            pm.setDisplayName(name);

            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.YELLOW + "Cost: " + perk.getCost());
            lore.addAll(perk.getDescription());
            pm.setLore(lore);

            // add glow if already purchased
            String stripped = ChatColor.stripColor(name);
            if (playerData.hasPerk(player.getUniqueId(), stripped)) {
                pm.addEnchant(Enchantment.UNBREAKING, 1, true);
                pm.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            }

            perkItem.setItemMeta(pm);
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
        if (!(event.getWhoClicked() instanceof Player))
            return;
        Player player = (Player) event.getWhoClicked();

        // Check if this is our custom inventory
        if (event.getView().getTitle().startsWith(ChatColor.DARK_GREEN + "Merits")) {
            event.setCancelled(true);

            String title = ChatColor.stripColor(event.getView().getTitle());
            int currentPage = 1;
            if (title.contains("Page")) {
                try {
                    String pagePart = title.substring(title.indexOf("Page") + 5);
                    currentPage = Integer.parseInt(pagePart.split("/")[0]);
                } catch (Exception ignored) { }
            }

            ItemStack clickedItem = event.getCurrentItem();
            if (clickedItem == null || clickedItem.getType() == Material.AIR)
                return;

            // If player clicks the diamond, show available points
            if (clickedItem.getType() == Material.DIAMOND) {
                player.sendMessage(ChatColor.GREEN + "You have "
                        + playerData.getMeritPoints(player.getUniqueId()) + " available merit points.");
                return;
            }

            ItemMeta meta = clickedItem.getItemMeta();
            if (meta == null)
                return;

            String name = ChatColor.stripColor(meta.getDisplayName());
            if (name.startsWith("Page ")) {
                try {
                    int page = Integer.parseInt(name.replace("Page ", ""));
                    openMeritGUI(player, page);
                } catch (NumberFormatException ignored) { }
                return;
            }

            // Extract cost from lore
            int cost = 0;
            if (meta.getLore() != null) {
                for (String line : meta.getLore()) {
                    String stripped = ChatColor.stripColor(line);
                    if (stripped.startsWith("Cost: ")) {
                        try {
                            cost = Integer.parseInt(stripped.replace("Cost: ", ""));
                        } catch (NumberFormatException ignored) { }
                        break;
                    }
                }
            }

            // Get the perk title (using stripped name)
            String perkTitle = ChatColor.stripColor(meta.getDisplayName());

            ClickType click = event.getClick();

            if (click.isRightClick() && player.isOp()) {
                if (playerData.hasPerk(player.getUniqueId(), perkTitle)) {
                    playerData.removePerk(player.getUniqueId(), perkTitle);
                    int pts = playerData.getMeritPoints(player.getUniqueId());
                    playerData.setMeritPoints(player.getUniqueId(), pts + cost);
                    player.sendMessage(ChatColor.GREEN + "Refunded perk: " + perkTitle);
                }
                openMeritGUI(player, currentPage);
                return;
            }

            // If already purchased, inform the player
            if (playerData.hasPerk(player.getUniqueId(), perkTitle)) {
                player.sendMessage(ChatColor.RED + "You have already purchased this perk!");
                return;
            }

            // Check if the player can afford the perk
            int currentPoints = playerData.getMeritPoints(player.getUniqueId());
            if (currentPoints < cost) {
                player.sendMessage(ChatColor.RED + "You do not have enough merit points to purchase this perk.");
                return;
            }

            // Deduct points and mark the perk as purchased
            playerData.setMeritPoints(player.getUniqueId(), currentPoints - cost);
            playerData.addPerk(player.getUniqueId(), perkTitle);

            player.sendMessage(ChatColor.GREEN + "You purchased perk: " + perkTitle
                    + " for " + cost + " merit points!");

            // Refresh the GUI to update the available points and add the enchant glow to the purchased perk
            openMeritGUI(player, currentPage);
        }
    }
}
