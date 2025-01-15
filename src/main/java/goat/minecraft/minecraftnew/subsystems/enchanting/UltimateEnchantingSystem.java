package goat.minecraft.minecraftnew.subsystems.enchanting;

import goat.minecraft.minecraftnew.utils.ItemRegistry;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

import java.util.*;

public class UltimateEnchantingSystem implements Listener {


    // Hard-coded costs
    private static final int ULTIMATE_ENCHANT_COST_LAPIS = 64;
    private static final int COOLDOWN_UPGRADE_COST_LAPIS = 64;
    private static final int FORBIDDEN_BOOK_COST = 4;

    /**
     * Instead of a class, we store a mapping from Material -> List<String> (enchantment names).
     * E.g.: BOW -> ["Explosive Shots","Homing Arrows"]
     */
    private static final Map<Material, List<String>> CUSTOM_ENCHANTS_BY_TYPE = new HashMap<>();


    /**
     * Registers 8 custom enchantments:
     *   - 2 for bows
     *   - 3 for melee
     *   - 3 for tools
     *
     * We also populate the CUSTOM_ENCHANTS_BY_TYPE map so the GUI knows which
     * enchants to show for which item types.
     */
    public void registerCustomEnchants() {
        // 1) Bows
        registerEnchantmentForType("Ultimate: Leg Shot", 1, false, Material.BOW, Material.CROSSBOW);
        registerEnchantmentForType("Ultimate: Headshot", 1, false, Material.BOW, Material.CROSSBOW);
        registerEnchantmentForType("Ultimate: Homing Arrows",   1, false, Material.BOW, Material.CROSSBOW);

        // 2) Melee
        registerEnchantmentForType("Ultimate: Inferno",   1, false, MELEE.toArray(new Material[0]));
        registerEnchantmentForType("Ultimate: Rage Mode",      1, false, MELEE.toArray(new Material[0]));
        registerEnchantmentForType("Ultimate: Warp",      1, false, MELEE.toArray(new Material[0]));
        registerEnchantmentForType("Ultimate: Snowstorm",      1, false, MELEE.toArray(new Material[0]));
        registerEnchantmentForType("Ultimate: Parry",      1, false, MELEE.toArray(new Material[0]));
        registerEnchantmentForType("Ultimate: Disc Seeker",      1, false, MELEE.toArray(new Material[0]));
        registerEnchantmentForType("Ultimate: Leap",      1, false, MELEE.toArray(new Material[0]));

        // 3) Tools
        registerEnchantmentForType("Ultimate: Hammer",      1, false, TOOLS.toArray(new Material[0]));
        registerEnchantmentForType("Ultimate: Treecapitator",       1, false, TOOLS.toArray(new Material[0]));
        registerEnchantmentForType("Ultimate: Excavate",      1, false, TOOLS.toArray(new Material[0]));
    }

    /**
     * Helper to both register enchantments with CustomEnchantmentManager
     * AND populate the map of which items can show these enchants in the GUI.
     */
    private void registerEnchantmentForType(String enchantName, int maxLevel, boolean isTreasure, Material... types) {
        // Register with your CustomEnchantmentManager
        CustomEnchantmentManager.registerEnchantment(enchantName, maxLevel, isTreasure);

        // Populate the mapping for each material
        for (Material mat : types) {
            CUSTOM_ENCHANTS_BY_TYPE.computeIfAbsent(mat, k -> new ArrayList<>()).add(enchantName);
        }
    }

    @EventHandler
    public void onPlayerRightClickEnchantmentTable(PlayerInteractEvent event) {
        if (event.getClickedBlock() == null) return;
        if (event.getClickedBlock().getType() == Material.ENCHANTING_TABLE &&
                event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            event.setCancelled(true);
            openUltimateEnchantmentGUI(event.getPlayer());
        }
    }

    public void openUltimateEnchantmentGUI(Player player) {
        // The item the player is holding
        ItemStack heldItem = player.getInventory().getItemInMainHand();

        // Validate it's at least a sword, tool, or bow
        // Otherwise, we bail out
        if (!isBow(heldItem) && !MELEE.contains(heldItem.getType()) && !TOOLS.contains(heldItem.getType())) {
            player.sendMessage(ChatColor.RED + "You must be holding a sword, tool, or bow to use this!");
            return;
        }

        // Get the relevant enchants for this item type
        List<String> relevantEnchants = CUSTOM_ENCHANTS_BY_TYPE
                .getOrDefault(heldItem.getType(), Collections.emptyList());

        // Create a large 6x9 GUI
        Inventory inv = Bukkit.createInventory(
                new UltimateEnchantInventoryHolder(),
                54,
                ChatColor.DARK_PURPLE + "Ultimate Enchantment"
        );

        // Put the held item in the center
        int editableSlot = 22;
        inv.setItem(editableSlot, heldItem.clone());

        // Fill everything else with filler glass
        for (int i = 0; i < 54; i++) {
            if (i == editableSlot) continue;
            inv.setItem(i, createGuiFiller());
        }

        // We'll place the icons for up to 8 relevant enchants in these slots
        int[] iconSlots = {12, 13, 14, 21, 23, 30, 31, 32};
        int enchantCount = Math.min(relevantEnchants.size(), 8);

        for (int i = 0; i < enchantCount && i < iconSlots.length; i++) {
            String enchantName = relevantEnchants.get(i);
            inv.setItem(iconSlots[i], createCustomIcon(enchantName));
        }

        player.openInventory(inv);
    }

    /**
     * Creates an ItemStack representing a particular enchantment in the GUI.
     * We'll label it "Ultimate: <EnchantmentName>"
     */
    private ItemStack createCustomIcon(String enchantName) {
        ItemStack icon = new ItemStack(Material.ENCHANTED_BOOK);
        ItemMeta meta = icon.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.GOLD + enchantName);
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.BLUE + "Cost: 64 Lapis & 4 Forbidden Books.");
            meta.setLore(lore);
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            icon.setItemMeta(meta);
        }
        return icon;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof UltimateEnchantInventoryHolder)) return;
        event.setCancelled(true);

        Player player = (Player) event.getWhoClicked();
        ItemStack handItem = player.getInventory().getItemInMainHand();
        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null || clickedItem.getType() == Material.AIR) return;

        // If they click the middle slot (the actual item), allow them to take it
        if (event.getSlot() == 22) {
            event.setCancelled(true);
            return;
        }
        // Check cost
        if (!playerHasEnoughMaterial(player, Material.LAPIS_LAZULI, ULTIMATE_ENCHANT_COST_LAPIS) ||
                !playerHasEnoughForbiddenBooks(player, FORBIDDEN_BOOK_COST)) {
            player.sendMessage(ChatColor.RED + "You need 64 Lapis Lazuli and 16 Forbidden Books to apply this enchantment!");
            return;
        }
        if(clickedItem.getItemMeta().getDisplayName().equals(ChatColor.GOLD + "Ultimate: Inferno")){
            CustomEnchantmentManager.addUltimateEnchantment(player, null, handItem, "Ultimate: Inferno", 1);
        }
        if(clickedItem.getItemMeta().getDisplayName().equals(ChatColor.GOLD + "Ultimate: Rage Mode")){
            CustomEnchantmentManager.addUltimateEnchantment(player,null, handItem, "Ultimate: Rage Mode", 1);
        }
        if(clickedItem.getItemMeta().getDisplayName().equals(ChatColor.GOLD + "Ultimate: Warp")){
            CustomEnchantmentManager.addUltimateEnchantment(player,null, handItem, "Ultimate: Warp", 1);
        }
        if(clickedItem.getItemMeta().getDisplayName().equals(ChatColor.GOLD + "Ultimate: Snowstorm")){
            CustomEnchantmentManager.addUltimateEnchantment(player,null, handItem, "Ultimate: Snowstorm", 1);
        }
        if(clickedItem.getItemMeta().getDisplayName().equals(ChatColor.GOLD + "Ultimate: Parry")){
            CustomEnchantmentManager.addUltimateEnchantment(player,null, handItem, "Ultimate: Parry", 1);
        }
        if(clickedItem.getItemMeta().getDisplayName().equals(ChatColor.GOLD + "Ultimate: Disc Seeker")){
            CustomEnchantmentManager.addUltimateEnchantment(player,null, handItem, "Ultimate: Disc Seeker", 1);
        }
        if(clickedItem.getItemMeta().getDisplayName().equals(ChatColor.GOLD + "Ultimate: Leap")){
            CustomEnchantmentManager.addUltimateEnchantment(player,null, handItem, "Ultimate: Leap", 1);
        }
        if(clickedItem.getItemMeta().getDisplayName().equals(ChatColor.GOLD + "Ultimate: Excavate")){
            CustomEnchantmentManager.addUltimateEnchantment(player,null, handItem, "Ultimate: Excavate", 1);
        }
        if(clickedItem.getItemMeta().getDisplayName().equals(ChatColor.GOLD + "Ultimate: Treecapitator")){
            CustomEnchantmentManager.addUltimateEnchantment(player,null, handItem, "Ultimate: Treecapitator", 1);
        }
        if(clickedItem.getItemMeta().getDisplayName().equals(ChatColor.GOLD + "Ultimate: Hammer")){
            CustomEnchantmentManager.addUltimateEnchantment(player,null, handItem, "Ultimate: Hammer", 1);
        }
        if(clickedItem.getItemMeta().getDisplayName().equals(ChatColor.GOLD + "Ultimate: Homing Arrows")){
            CustomEnchantmentManager.addUltimateEnchantment(player,null, handItem, "Ultimate: Homing Arrows", 1);
        }
        if(clickedItem.getItemMeta().getDisplayName().equals(ChatColor.GOLD + "Ultimate: Leg Shot")){
            CustomEnchantmentManager.addUltimateEnchantment(player,null, handItem, "Ultimate: Leg Shot", 1);
        }
        if(clickedItem.getItemMeta().getDisplayName().equals(ChatColor.GOLD + "Ultimate: Headshot")){
            CustomEnchantmentManager.addUltimateEnchantment(player,null, handItem, "Ultimate: Headshot", 1);
        }


        // Remove cost from player's inventory
        removeMaterialFromPlayer(player, Material.LAPIS_LAZULI, ULTIMATE_ENCHANT_COST_LAPIS);
        removeForbiddenBooksFromPlayer(player, FORBIDDEN_BOOK_COST);


    }

    /**
     * Returns true if the given ItemStack is a bow (but not a crossbow).
     */
    public boolean isBow(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) return false;
        String name = item.getType().toString().toUpperCase();
        return name.endsWith("BOW") && !name.endsWith("CROSSBOW");
    }

    /**
     * A set of Materials that count as "melee" (swords, trident).
     */
    public static final Set<Material> MELEE = EnumSet.of(
            Material.WOODEN_SWORD,  Material.STONE_SWORD,
            Material.IRON_SWORD,    Material.GOLDEN_SWORD,
            Material.DIAMOND_SWORD, Material.NETHERITE_SWORD,
            Material.TRIDENT
    );

    /**
     * A set of Materials that count as "tools" (pickaxe, axe, shovel, hoe, etc.).
     */
    public static final Set<Material> TOOLS = EnumSet.of(
            Material.WOODEN_PICKAXE,  Material.STONE_PICKAXE,
            Material.IRON_PICKAXE,    Material.GOLDEN_PICKAXE,
            Material.DIAMOND_PICKAXE, Material.NETHERITE_PICKAXE,
            Material.WOODEN_AXE,      Material.STONE_AXE,
            Material.IRON_AXE,        Material.GOLDEN_AXE,
            Material.DIAMOND_AXE,     Material.NETHERITE_AXE,
            Material.WOODEN_SHOVEL,   Material.STONE_SHOVEL,
            Material.IRON_SHOVEL,     Material.GOLDEN_SHOVEL,
            Material.DIAMOND_SHOVEL,  Material.NETHERITE_SHOVEL,
            Material.WOODEN_HOE,      Material.STONE_HOE,
            Material.IRON_HOE,        Material.GOLDEN_HOE,
            Material.DIAMOND_HOE,     Material.NETHERITE_HOE,
            Material.SHEARS,          Material.FISHING_ROD,
            Material.FLINT_AND_STEEL
    );

    /**
     * Creates the black glass pane filler item.
     */
    private ItemStack createGuiFiller() {
        ItemStack filler = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta meta = filler.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.BLACK + "");
            filler.setItemMeta(meta);
        }
        return filler;
    }

    /**
     * Simple InventoryHolder so we can distinguish this GUI in onInventoryClick.
     */
    private static class UltimateEnchantInventoryHolder implements InventoryHolder {
        @Override
        public Inventory getInventory() {
            return null;
        }
    }

    /**
     * Checks if a player has at least 'amount' of the given material in their inventory.
     */
    private boolean playerHasEnoughMaterial(Player player, Material mat, int amount) {
        int count = 0;
        for (ItemStack is : player.getInventory()) {
            if (is != null && is.getType() == mat) {
                count += is.getAmount();
                if (count >= amount) return true;
            }
        }
        return false;
    }

    /**
     * Removes 'amount' of 'mat' from the player's inventory.
     */
    private void removeMaterialFromPlayer(Player player, Material mat, int amount) {
        int toRemove = amount;
        for (ItemStack is : player.getInventory()) {
            if (is == null || is.getType() != mat) continue;

            if (is.getAmount() > toRemove) {
                is.setAmount(is.getAmount() - toRemove);
                return;
            } else {
                toRemove -= is.getAmount();
                is.setAmount(0);
            }
            if (toRemove <= 0) break;
        }
    }

    /**
     * Checks if a player has at least 'amount' forbidden books.
     */
    private boolean playerHasEnoughForbiddenBooks(Player player, int amount) {
        int count = 0;
        for (ItemStack is : player.getInventory()) {
            if (is != null && is.isSimilar(ItemRegistry.getForbiddenBook())) {
                count += is.getAmount();
                if (count >= amount) return true;
            }
        }
        return false;
    }

    /**
     * Removes 'amount' forbidden books from the player's inventory.
     */
    private void removeForbiddenBooksFromPlayer(Player player, int amount) {
        int toRemove = amount;
        for (ItemStack is : player.getInventory()) {
            if (is == null || !is.isSimilar(ItemRegistry.getForbiddenBook())) continue;

            if (is.getAmount() > toRemove) {
                is.setAmount(is.getAmount() - toRemove);
                return;
            } else {
                toRemove -= is.getAmount();
                is.setAmount(0);
            }
            if (toRemove <= 0) break;
        }
    }
}
