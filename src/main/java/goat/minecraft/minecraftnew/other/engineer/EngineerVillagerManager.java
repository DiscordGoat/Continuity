package goat.minecraft.minecraftnew.other.engineer;

import goat.minecraft.minecraftnew.other.additionalfunctionality.CustomBundleGUI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
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
     * TradeCost now holds:
     * - The number of Redstone Blocks required.
     * - The base amount of items given in a single (left-click) purchase.
     */
    private static class TradeCost {
        public final int redstoneBlockCost;
        public final int baseOutput;

        public TradeCost(int redstoneBlockCost, int baseOutput) {
            this.redstoneBlockCost = redstoneBlockCost;
            this.baseOutput = baseOutput;
        }
    }

    /**
     * Define each recognized redstone component with:
     * - The cost in Redstone Blocks (redstoneBlockCost).
     * - The base output of items (baseOutput).
     *
     * If you want the purchase to yield a lot of items, increase `baseOutput`.
     * The total output for a right-click is baseOutput * 4.
     */
    private static final Map<Material, TradeCost> COMPONENT_COSTS = new HashMap<>() {{
        // Material    -> new TradeCost(blockCost, baseOutput)
        put(Material.REDSTONE_TORCH,     new TradeCost(1, 9));
        put(Material.REPEATER,           new TradeCost(1, 3));
        put(Material.COMPARATOR,         new TradeCost(1, 2));
        put(Material.TARGET,             new TradeCost(1, 2));
        put(Material.LEVER,              new TradeCost(1, 5));
        put(Material.DAYLIGHT_DETECTOR,  new TradeCost(1, 3));
        put(Material.PISTON,             new TradeCost(1, 2));
        put(Material.STICKY_PISTON,      new TradeCost(1, 1));
        put(Material.SLIME_BLOCK,        new TradeCost(1, 1));
        put(Material.HONEY_BLOCK,        new TradeCost(2, 1));
        put(Material.DISPENSER,          new TradeCost(1, 2));
        put(Material.DROPPER,            new TradeCost(1, 4));
        put(Material.HOPPER,             new TradeCost(1, 2));
        put(Material.OBSERVER,           new TradeCost(1, 2));
        put(Material.RAIL,               new TradeCost(1, 16));  // Example: 8 rails at once
        put(Material.POWERED_RAIL,       new TradeCost(1, 8));
        put(Material.DETECTOR_RAIL,      new TradeCost(1, 8));
        put(Material.ACTIVATOR_RAIL,     new TradeCost(1, 8));
        put(Material.TNT,                new TradeCost(1, 2));
        put(Material.REDSTONE_LAMP,      new TradeCost(1, 4));
        put(Material.IRON_DOOR,          new TradeCost(1, 2));
        put(Material.IRON_TRAPDOOR,      new TradeCost(1, 2));
        put(Material.STONE_BUTTON,       new TradeCost(1, 9));
        put(Material.OAK_BUTTON,         new TradeCost(1, 9));
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
            Map<String, TradeCost> costMapping = new HashMap<>();

            int slotIndex = 0;
            for (Material mat : foundComponents) {
                TradeCost cost = COMPONENT_COSTS.get(mat);
                if (cost == null) continue; // Shouldn't happen if mat is in REDSTONE_COMPONENTS

                // Create the "engineer item" for this component
                ItemStack tradeItem = createEngineerTradeItem(mat, cost);
                inv.setItem(slotIndex, tradeItem);

                // Record the cost so we can look it up by item display name
                costMapping.put(tradeItem.getItemMeta().getDisplayName(), cost);

                slotIndex++;
                if (slotIndex >= 54) break; // inventory is full
            }
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
     * The lore shows the cost and the *base* amount of items you get on a left-click.
     * Right-click multiplies both the cost & the output by 4.
     */
    private ItemStack createEngineerTradeItem(Material componentMat, TradeCost cost) {
        ItemStack item = new ItemStack(componentMat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            String matName = formatMaterialName(componentMat.name());
            meta.setDisplayName(ChatColor.YELLOW + matName);

            int baseCost = cost.redstoneBlockCost;
            int baseAmount = cost.baseOutput;
            int bulkCost = baseCost * 4;
            int bulkAmount = baseAmount * 4;

            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "Left-click: Buy " + baseAmount + " for "
                    + ChatColor.RED + baseCost + ChatColor.GRAY + " Redstone Block(s)");
            lore.add(ChatColor.GRAY + "Right-click: Buy " + bulkAmount + " for "
                    + ChatColor.RED + bulkCost + ChatColor.GRAY + " Redstone Block(s)");
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

        ClickType clickType = event.getClick();
        if (clickType.isRightClick()) {
            // Buying 4x
            processEngineerPurchase(player, clickedItem, cost, 4);
        } else {
            // Left-click => buy 1x
            processEngineerPurchase(player, clickedItem, cost, 1);
        }
    }

    /**
     * Removes the required number of Redstone Blocks and gives the purchased item(s).
     * 'multiplier' is 1 for left-click, 4 for right-click.
     */
    private void processEngineerPurchase(Player player, ItemStack itemForSale, TradeCost cost, int multiplier) {
        // Total cost in Redstone Blocks
        int totalRedstoneBlockCost = cost.redstoneBlockCost * multiplier;
        // Total number of items the player gets
        int totalOutputAmount = cost.baseOutput * multiplier;

        // 1) Count how many Redstone Blocks the player has in their main inventory
        int blocksInInventory = countRedstoneBlocksInInventory(player);

        if (blocksInInventory >= totalRedstoneBlockCost) {
            // The main inventory alone is enough
            removeMaterial(player, Material.REDSTONE_BLOCK, totalRedstoneBlockCost);
        } else {
            // The player doesn't have enough in main inventory
            // Remove whatever they do have from main inventory
            removeMaterial(player, Material.REDSTONE_BLOCK, blocksInInventory);

            // We still need this many
            int shortfall = totalRedstoneBlockCost - blocksInInventory;

            // Attempt removing shortfall from the backpack
            boolean success = CustomBundleGUI.getInstance().removeRedstoneBlocksFromBackpack(player, shortfall);
            if (!success) {
                // They can’t afford the cost from inventory + backpack
                player.sendMessage(ChatColor.RED + "You don't have enough Redstone Blocks (in inventory or backpack).");
                return; // Stop here
            }
        }

        // If we get here, we've successfully removed enough blocks overall.
        // --- Give the purchased items ---
        ItemStack purchasedStack = new ItemStack(itemForSale.getType(), totalOutputAmount);
        Map<Integer, ItemStack> leftover = player.getInventory().addItem(purchasedStack);
        for (ItemStack leftoverItem : leftover.values()) {
            // If inventory is full, drop any leftover items on the ground
            player.getWorld().dropItemNaturally(player.getLocation(), leftoverItem);
        }

        // Feedback
        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.2f);
        player.sendMessage(ChatColor.GREEN + "Purchase successful! You got " + totalOutputAmount + "x "
                + formatMaterialName(itemForSale.getType().name()) + ".");
    }

    /**
     * Helper to count how many Redstone Blocks are in a player's main inventory.
     */
    private int countRedstoneBlocksInInventory(Player player) {
        int count = 0;
        for (ItemStack stack : player.getInventory().getContents()) {
            if (stack != null && stack.getType() == Material.REDSTONE_BLOCK) {
                count += stack.getAmount();
            }
        }
        return count;
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
