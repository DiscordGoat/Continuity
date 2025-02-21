package goat.minecraft.minecraftnew.other.engineer;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public class EngineerVillagerManager implements Listener {

    private final JavaPlugin plugin;
    private final Map<Player, Villager> engineerInteractionMap = new HashMap<>();

    /**
     * Now we only store how many Redstone Blocks are required.
     */
    private static class TradeCost {
        public final int redstoneBlockCost;

        public TradeCost(int redstoneBlockCost) {
            this.redstoneBlockCost = redstoneBlockCost;
        }
    }

    /**
     * Define the cost of each recognized redstone component in Redstone Blocks.
     * Adjust the numbers as desired.
     */
    private static final Map<Material, TradeCost> COMPONENT_COSTS = new HashMap<>() {{
        put(Material.REDSTONE_BLOCK,    new TradeCost(1));
        put(Material.REDSTONE_TORCH,    new TradeCost(1));
        put(Material.REPEATER,          new TradeCost(2));
        put(Material.COMPARATOR,        new TradeCost(3));
        put(Material.TARGET,            new TradeCost(2));
        put(Material.LEVER,             new TradeCost(1));
        put(Material.DAYLIGHT_DETECTOR, new TradeCost(3));
        put(Material.PISTON,            new TradeCost(2));
        put(Material.STICKY_PISTON,     new TradeCost(4));
        put(Material.SLIME_BLOCK,       new TradeCost(4));
        put(Material.HONEY_BLOCK,       new TradeCost(4));
        put(Material.DISPENSER,         new TradeCost(3));
        put(Material.DROPPER,           new TradeCost(2));
        put(Material.HOPPER,            new TradeCost(4));
        put(Material.OBSERVER,          new TradeCost(3));
        put(Material.RAIL,              new TradeCost(2));
        put(Material.POWERED_RAIL,      new TradeCost(3));
        put(Material.DETECTOR_RAIL,     new TradeCost(3));
        put(Material.ACTIVATOR_RAIL,    new TradeCost(3));
        put(Material.TNT,               new TradeCost(4));
        put(Material.REDSTONE_LAMP,     new TradeCost(2));
        put(Material.IRON_DOOR,         new TradeCost(2));
        put(Material.IRON_TRAPDOOR,     new TradeCost(2));
        put(Material.STONE_BUTTON,      new TradeCost(1));
        put(Material.OAK_BUTTON,        new TradeCost(1));
    }};

    // All redstone-related blocks we want to detect
    private static final Set<Material> REDSTONE_COMPONENTS = COMPONENT_COSTS.keySet();

    /**
     * For storing each player's dynamic trades in this session:
     * Key = Player, Value = Map<displayName, TradeCost> so we know the cost when they click.
     */
    private final Map<Player, Map<String, TradeCost>> tradeCostsByPlayer = new HashMap<>();

    public EngineerVillagerManager(JavaPlugin plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        if (!(event.getRightClicked() instanceof Villager villager)) return;
        if (villager.getCustomName() == null) return;

        if (!villager.getCustomName().equals(ChatColor.RED + "Engineer")) {
            // Not our custom Engineer
            return;
        }

        // Cancel default trades
        event.setCancelled(true);

        Player player = event.getPlayer();
        engineerInteractionMap.put(player, villager);

        // Open the engineer’s custom trade GUI
        openEngineerTradeGUI(player, villager);
    }

    /**
     * Opens a custom inventory to display all engineer trades based on nearby blocks.
     */
    private void openEngineerTradeGUI(Player player, Villager villager) {
        Inventory inv = Bukkit.createInventory(null, 54, ChatColor.DARK_RED + "Engineer Trades");

        // Gather all distinct redstone components in a 20-block radius
        Set<Material> foundComponents = checkNearbyBlocks(villager, 20);

        if (foundComponents.isEmpty()) {
            // No redstone components found => no trades
            player.sendMessage(ChatColor.GRAY + "[Engineer] "
                    + ChatColor.RED + "I can craft more of whatever’s around here... but there's nothing!");
            inv.setItem(22, createNoComponentsItem());
        } else {
            // Build a map of "displayName -> TradeCost" for referencing when the player clicks
            Map<String, TradeCost> costMapping = new HashMap<>();

            int slotIndex = 0;
            for (Material mat : foundComponents) {
                TradeCost cost = COMPONENT_COSTS.get(mat);
                if (cost == null) continue; // Shouldn't happen if mat is in REDSTONE_COMPONENTS

                // Create the "engineer item" for this component
                ItemStack tradeItem = createEngineerTradeItem(mat, cost);
                inv.setItem(slotIndex, tradeItem);

                // Record the cost so we can look it up on click by item name
                costMapping.put(tradeItem.getItemMeta().getDisplayName(), cost);

                slotIndex++;
                if (slotIndex >= 54) break; // inventory is full
            }
            // Store this cost mapping for the player
            tradeCostsByPlayer.put(player, costMapping);
        }

        player.openInventory(inv);
    }

    /**
     * Checks a given radius around the villager for any block in REDSTONE_COMPONENTS.
     * Returns a set of distinct Materials found.
     */
    private Set<Material> checkNearbyBlocks(Villager villager, int radius) {
        Set<Material> foundComponents = new HashSet<>();
        Block center = villager.getLocation().getBlock();

        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -radius; dy <= radius; dy++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    Block block = center.getRelative(dx, dy, dz);
                    Material mat = block.getType();
                    if (REDSTONE_COMPONENTS.contains(mat)) {
                        foundComponents.add(mat);
                    }
                }
            }
        }
        return foundComponents;
    }

    /**
     * Create a BARRIER item to represent "No Redstone Found."
     */
    private ItemStack createNoComponentsItem() {
        ItemStack lockedItem = new ItemStack(Material.BARRIER);
        ItemMeta meta = lockedItem.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.GRAY + "No Redstone Components Nearby");
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.RED + "The Engineer cannot produce any trades!");
            meta.setLore(lore);
            lockedItem.setItemMeta(meta);
        }
        return lockedItem;
    }

    /**
     * Creates an engineer trade item representing one discovered component.
     * Display name uses a prettified version of the Material name.
     * Lore shows the cost in Redstone Blocks.
     */
    private ItemStack createEngineerTradeItem(Material componentMat, TradeCost cost) {
        // Use the component material as the base item
        ItemStack item = new ItemStack(componentMat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            // Convert the material name to properly capitalized words
            String matName = formatMaterialName(componentMat.name());
            meta.setDisplayName(ChatColor.YELLOW + matName);

            // Add the cost details to the lore
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "Left-click: Buy 1 for "
                    + ChatColor.RED + cost.redstoneBlockCost + " Redstone Block(s)");
            lore.add(ChatColor.GRAY + "Right-click: Buy 4 for "
                    + ChatColor.RED + (cost.redstoneBlockCost * 4) + " Redstone Block(s)");
            lore.add(ChatColor.GRAY + "Click to purchase!");
            meta.setLore(lore);

            item.setItemMeta(meta);
        }
        return item;
    }

    /**
     * Formats a material name into a properly capitalized string.
     * Example: REDSTONE_BLOCK -> "Redstone Block"
     */
    private String formatMaterialName(String materialName) {
        String[] words = materialName.toLowerCase(Locale.ROOT).split("_");
        StringBuilder formattedName = new StringBuilder();
        for (String word : words) {
            formattedName.append(Character.toUpperCase(word.charAt(0)))
                    .append(word.substring(1))
                    .append(" ");
        }
        return formattedName.toString().trim();
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().equals(ChatColor.DARK_RED + "Engineer Trades")) return;

        event.setCancelled(true);
        if (event.getClickedInventory() == null) return;

        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null || clickedItem.getType() == Material.AIR) return;

        Player player = (Player) event.getWhoClicked();
        Map<String, TradeCost> costMapping = tradeCostsByPlayer.get(player);
        if (costMapping == null) return;

        String displayName = (clickedItem.hasItemMeta() && clickedItem.getItemMeta().hasDisplayName())
                ? clickedItem.getItemMeta().getDisplayName()
                : "";
        TradeCost cost = costMapping.get(displayName);
        if (cost == null) return; // Not one of our trades

        // Determine if the user did left-click or right-click
        ClickType clickType = event.getClick();
        if (clickType.isRightClick()) {
            // Buying 4 items
            processEngineerPurchase(player, clickedItem, cost, 4);
        } else {
            // Default: left-click => buy 1
            processEngineerPurchase(player, clickedItem, cost, 1);
        }
    }

    /**
     * Removes the required number of Redstone Blocks and gives the purchased item(s).
     * 'quantity' is how many of the item to buy in one click.
     */
    private void processEngineerPurchase(Player player, ItemStack itemForSale, TradeCost cost, int quantity) {
        int totalRedstoneBlockCost = cost.redstoneBlockCost * quantity;

        // Check if player has enough Redstone Blocks
        if (!hasEnoughMaterial(player, Material.REDSTONE_BLOCK, totalRedstoneBlockCost)) {
            player.sendMessage(ChatColor.RED + "You need at least "
                    + totalRedstoneBlockCost + " Redstone Block(s) to buy "
                    + quantity + " of this item!");
            return;
        }

        // Remove the required Redstone Blocks
        removeMaterial(player, Material.REDSTONE_BLOCK, totalRedstoneBlockCost);

        // Give the purchased items to the player
        ItemStack purchasedStack = new ItemStack(itemForSale.getType(), quantity);
        player.getInventory().addItem(purchasedStack);

        // Feedback
        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.2f);
        player.sendMessage(ChatColor.GREEN + "Purchase successful! You got " + quantity + "x "
                + formatMaterialName(itemForSale.getType().name()) + ".");
    }

    private boolean hasEnoughMaterial(Player player, Material mat, int requiredAmount) {
        int count = 0;
        for (ItemStack stack : player.getInventory().getContents()) {
            if (stack != null && stack.getType() == mat) {
                count += stack.getAmount();
                if (count >= requiredAmount) return true;
            }
        }
        return false;
    }

    private void removeMaterial(Player player, Material mat, int amountToRemove) {
        int remaining = amountToRemove;
        for (ItemStack stack : player.getInventory().getContents()) {
            if (stack == null) continue;
            if (stack.getType() == mat) {
                int stackAmt = stack.getAmount();
                if (stackAmt <= remaining) {
                    // Remove this entire stack
                    player.getInventory().removeItem(stack);
                    remaining -= stackAmt;
                    if (remaining <= 0) break;
                } else {
                    // Reduce the stack and finish
                    stack.setAmount(stackAmt - remaining);
                    remaining = 0;
                    break;
                }
            }
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getView().getTitle().equals(ChatColor.DARK_RED + "Engineer Trades")) {
            engineerInteractionMap.remove(event.getPlayer());
            tradeCostsByPlayer.remove(event.getPlayer());
        }
    }
}
