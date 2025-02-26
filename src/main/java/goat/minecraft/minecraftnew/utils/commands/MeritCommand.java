package goat.minecraft.minecraftnew.utils.commands;

import goat.minecraft.minecraftnew.utils.devtools.PlayerDataManager;
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
    private final PlayerDataManager playerData;

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
            new Perk(ChatColor.DARK_GRAY + "EnderMind", 1, Material.ENDER_PEARL,
                    Arrays.asList(
                            ChatColor.GRAY + "Nullifies Ender Pearl Damage by 100%",
                            ChatColor.BLUE + "On Pearl Land: " + ChatColor.GRAY + "+20 Hunger, +20 Saturation."
                    )),
            new Perk(ChatColor.DARK_GRAY + "Workbench", 1, Material.CRAFTING_TABLE,
                    Arrays.asList(
                            ChatColor.GRAY + "Enables Backpack Crafting Table usage.",
                            ChatColor.BLUE + "On Right Click Crafting Table In Backpack: " + ChatColor.GRAY + "Opens Crafting Table."
                    )),
            new Perk(ChatColor.DARK_GRAY + "Shulkl Box", 1, Material.SHULKER_SHELL,
                    Arrays.asList(
                            ChatColor.GRAY + "Enables Backpack Shulker Box usage.",
                            ChatColor.BLUE + "On Right Click Shulker Box In Backpack: " + ChatColor.GRAY + "Opens it."
                    )),
            new Perk(ChatColor.DARK_GRAY + "Motion Sensor", 1, Material.OAK_DOOR,
                    Arrays.asList(
                            ChatColor.GRAY + "Enables Autoclose doors.",
                            ChatColor.BLUE + "On Left Click Door: " + ChatColor.GRAY + "Opens doors for 2 seconds."
                    )),
            new Perk(ChatColor.DARK_GRAY + "Unbreaking", 1, Material.OBSIDIAN,
                    Arrays.asList(
                            ChatColor.GRAY + "Prevents 15% of durability losses.",
                            ChatColor.BLUE + "On Lose Durability: " + ChatColor.GRAY + "15% chance to refund."
                    )),
            new Perk(ChatColor.DARK_GRAY + "Berserkers Rage", 1, Material.FERMENTED_SPIDER_EYE,
                    Arrays.asList(
                            ChatColor.GRAY + "Enrages you when struck by a competent foe.",
                            ChatColor.BLUE + "On Take High Damage (30%) In One Hit: " + ChatColor.GRAY + "Strength 5 for 20 seconds."
                    )),
            new Perk(ChatColor.DARK_GRAY + "Tactical Retreat", 1, Material.FEATHER,
                    Arrays.asList(
                            ChatColor.GRAY + "Speeds your step you when struck by a competent foe.",
                            ChatColor.BLUE + "On Take High Damage (30%) In One Hit: " + ChatColor.GRAY + "Speed 2 and Invisibility for 20 seconds."
                    )),
            new Perk(ChatColor.DARK_GRAY + "Vampiric Strike", 1, Material.GHAST_TEAR,
                    Arrays.asList(
                            ChatColor.GRAY + "Heals you and kills your foe.",
                            ChatColor.BLUE + "On Deal Damage Rarely (5%): " + ChatColor.GRAY + "Fully Heals you and kills your foe."
                    )),
            new Perk(ChatColor.DARK_GRAY + "Lord of Thunder", 1, Material.LIGHTNING_ROD,
                    Arrays.asList(
                            ChatColor.GRAY + "Smites your foe.",
                            ChatColor.BLUE + "On Deal Damage Rarely (5%): " + ChatColor.GRAY + "Strike your foe with lightning."
                    )),
            new Perk(ChatColor.DARK_GRAY + "QuickSwap", 1, Material.TRIPWIRE_HOOK,
                    Arrays.asList(
                            ChatColor.GRAY + "Replenishes your supply of blocks.",
                            ChatColor.BLUE + "On Place Block: " + ChatColor.GRAY + "Replenishes your block supply."
                    ))
            );

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

        // === FIRST ROW (indices 0..8) ===
        ItemStack blackGlass = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta blackGlassMeta = blackGlass.getItemMeta();
        blackGlassMeta.setDisplayName(ChatColor.BLACK + "");
        blackGlass.setItemMeta(blackGlassMeta);
        for (int i = 0; i < 9; i++) {
            inv.setItem(i, blackGlass.clone());
        }

        // Diamond in center (slot 4) shows available merit points
        int currentMeritPoints = playerData.getMeritPoints(player.getUniqueId());
        ItemStack diamond = new ItemStack(Material.DIAMOND);
        ItemMeta diamondMeta = diamond.getItemMeta();
        diamondMeta.setDisplayName(ChatColor.AQUA + "Available Merit Points: " + currentMeritPoints);
        diamond.setItemMeta(diamondMeta);
        inv.setItem(4, diamond);

        // === REMAINING 45 SLOTS (indices 9..53) ===
        int slotIndex = 9;
        for (Perk perk : perks) {
            ItemStack perkItem = new ItemStack(perk.getIcon());
            ItemMeta perkMeta = perkItem.getItemMeta();

            // Translate & codes in the perk name for fancy titles
            String displayName = ChatColor.translateAlternateColorCodes('&', perk.getName());
            perkMeta.setDisplayName(displayName);

            // Set lore with cost and description from our List
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.YELLOW + "Cost: " + perk.getCost());
            lore.addAll(perk.getDescription());
            perkMeta.setLore(lore);

            // If the player has already purchased this perk, add an enchant glow
            String strippedName = ChatColor.stripColor(displayName);
            if (playerData.hasPerk(player.getUniqueId(), strippedName)) {
                perkMeta.addEnchant(Enchantment.DURABILITY, 1, true);
                perkMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            }
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
        if (!(event.getWhoClicked() instanceof Player))
            return;
        Player player = (Player) event.getWhoClicked();

        // Check if this is our custom inventory
        if (event.getView().getTitle().equals(ChatColor.DARK_GREEN + "Merits Menu")) {
            event.setCancelled(true);

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
            openMeritGUI(player);
        }
    }
}
