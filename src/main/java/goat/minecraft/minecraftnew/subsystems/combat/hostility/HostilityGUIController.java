package goat.minecraft.minecraftnew.subsystems.combat.hostility;

import goat.minecraft.minecraftnew.subsystems.combat.config.CombatConfiguration;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controller for the hostility tier selection GUI.
 * Handles GUI creation, player interactions, and tier selection.
 */
public class HostilityGUIController implements Listener {
    
    private static final Logger logger = Logger.getLogger(HostilityGUIController.class.getName());
    
    // Predefined slots for tier items in the GUI
    private static final int[] TIER_SLOTS = {11, 13, 15, 20, 22, 24, 29, 31, 33, 40};
    
    private final HostilityService hostilityService;
    private final CombatConfiguration.HostilityConfig config;
    
    public HostilityGUIController(HostilityService hostilityService, CombatConfiguration.HostilityConfig config) {
        this.hostilityService = hostilityService;
        this.config = config;
    }
    
    /**
     * Opens the hostility tier selection GUI for a player.
     * 
     * @param player The player to open the GUI for
     */
    public void openHostilityGUI(Player player) {
        if (player == null) {
            return;
        }
        
        try {
            Inventory gui = createHostilityGUI(player);
            player.openInventory(gui);
            
            logger.fine(String.format("Opened hostility GUI for %s", player.getName()));
            
        } catch (Exception e) {
            logger.log(Level.WARNING, 
                      String.format("Failed to open hostility GUI for %s", player.getName()), e);
            player.sendMessage(ChatColor.RED + "Could not open hostility selection GUI.");
        }
    }
    
    /**
     * Handles inventory click events for the hostility GUI.
     */
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        // Check if this is our hostility GUI
        if (!event.getView().getTitle().equals(config.getGuiTitle())) {
            return;
        }
        
        event.setCancelled(true); // Prevent item manipulation
        
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }
        
        Player player = (Player) event.getWhoClicked();
        ItemStack clickedItem = event.getCurrentItem();
        
        if (clickedItem == null || clickedItem.getType() == Material.AIR) {
            return;
        }
        
        try {
            handleGUIClick(player, clickedItem);
        } catch (Exception e) {
            logger.log(Level.WARNING, 
                      String.format("Error handling hostility GUI click for %s", player.getName()), e);
            player.sendMessage(ChatColor.RED + "An error occurred while processing your selection.");
        }
    }
    
    /**
     * Creates the hostility selection GUI for a player.
     */
    private Inventory createHostilityGUI(Player player) {
        Inventory gui = Bukkit.createInventory(null, config.getGuiSize(), config.getGuiTitle());
        
        // Add decorative borders
        addBorders(gui);
        
        // Add tier selection items
        addTierItems(gui, player);
        
        // Add close button
        addCloseButton(gui);
        
        return gui;
    }
    
    /**
     * Adds decorative border items to the GUI.
     */
    private void addBorders(Inventory gui) {
        Material borderMaterial = getMaterialSafely(config.getBorderMaterial(), Material.BLACK_STAINED_GLASS_PANE);
        ItemStack borderItem = createGuiItem(borderMaterial, ChatColor.GRAY + "Decoration", null);
        
        int size = gui.getSize();
        for (int i = 0; i < size; i++) {
            // Top and bottom rows, left and right columns
            if (i < 9 || i >= size - 9 || i % 9 == 0 || i % 9 == 8) {
                gui.setItem(i, borderItem);
            }
        }
    }
    
    /**
     * Adds tier selection items to the GUI.
     */
    private void addTierItems(Inventory gui, Player player) {
        for (int i = 0; i < Math.min(TIER_SLOTS.length, config.getMaxTier()); i++) {
            int tierNumber = i + 1;
            int slot = TIER_SLOTS[i];
            
            ItemStack tierItem = createTierItem(player, tierNumber);
            gui.setItem(slot, tierItem);
        }
    }
    
    /**
     * Creates a tier selection item.
     */
    private ItemStack createTierItem(Player player, int tierNumber) {
        boolean isUnlocked = hostilityService.isTierUnlocked(player, tierNumber);
        int currentTier = hostilityService.getPlayerTier(player);
        
        Material material;
        String displayName;
        String[] lore;
        
        if (isUnlocked) {
            material = getMaterialSafely(config.getUnlockedMaterial(), Material.RED_STAINED_GLASS_PANE);
            displayName = ChatColor.BOLD + "Tier " + tierNumber;
            
            if (currentTier == tierNumber) {
                lore = new String[]{
                    ChatColor.GREEN + "Currently Selected",
                    ChatColor.GRAY + "Click to confirm selection"
                };
            } else {
                lore = new String[]{
                    ChatColor.GRAY + "Click to select Tier " + tierNumber,
                    ChatColor.YELLOW + "Unlocked at level " + hostilityService.getRequiredLevel(tierNumber)
                };
            }
        } else {
            material = getMaterialSafely(config.getLockedMaterial(), Material.GRAY_STAINED_GLASS_PANE);
            displayName = ChatColor.BOLD + "Tier " + tierNumber;
            lore = new String[]{
                ChatColor.RED + "Locked",
                ChatColor.GRAY + "Required level: " + hostilityService.getRequiredLevel(tierNumber),
                ChatColor.GRAY + "Your level: " + player.getLevel()
            };
        }
        
        return createGuiItem(material, displayName, lore);
    }
    
    /**
     * Adds the close button to the GUI.
     */
    private void addCloseButton(Inventory gui) {
        Material closeMaterial = getMaterialSafely(config.getCloseMaterial(), Material.BARRIER);
        ItemStack closeItem = createGuiItem(closeMaterial, ChatColor.RED + "Close", 
                                          new String[]{ChatColor.GRAY + "Click to close this menu"});
        
        // Place close button at bottom center
        int closeSlot = gui.getSize() - 5; // Bottom center for 54-slot inventory
        gui.setItem(closeSlot, closeItem);
    }
    
    /**
     * Handles a click on a GUI item.
     */
    private void handleGUIClick(Player player, ItemStack clickedItem) {
        Material closeMaterial = getMaterialSafely(config.getCloseMaterial(), Material.BARRIER);
        
        // Handle close button
        if (clickedItem.getType() == closeMaterial) {
            player.closeInventory();
            player.sendMessage(ChatColor.YELLOW + "Hostility selection closed.");
            return;
        }
        
        // Handle tier selection
        Material unlockedMaterial = getMaterialSafely(config.getUnlockedMaterial(), Material.RED_STAINED_GLASS_PANE);
        Material lockedMaterial = getMaterialSafely(config.getLockedMaterial(), Material.GRAY_STAINED_GLASS_PANE);
        
        if (clickedItem.getType() == unlockedMaterial || clickedItem.getType() == lockedMaterial) {
            handleTierSelection(player, clickedItem);
        }
    }
    
    /**
     * Handles tier selection from a clicked item.
     */
    private void handleTierSelection(Player player, ItemStack clickedItem) {
        ItemMeta meta = clickedItem.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) {
            return;
        }
        
        String displayName = ChatColor.stripColor(meta.getDisplayName());
        if (!displayName.startsWith("Tier ")) {
            return;
        }
        
        try {
            int tier = Integer.parseInt(displayName.substring(5).trim());
            
            // Validate tier
            if (tier < 1 || tier > config.getMaxTier()) {
                player.sendMessage(ChatColor.RED + "Invalid tier selected.");
                return;
            }
            
            // Check if tier is unlocked
            if (!hostilityService.isTierUnlocked(player, tier)) {
                int requiredLevel = hostilityService.getRequiredLevel(tier);
                player.sendMessage(ChatColor.RED + String.format(
                    "You haven't unlocked Tier %d yet! Required level: %d", tier, requiredLevel));
                player.closeInventory();
                return;
            }
            
            // Set the tier
            if (hostilityService.setPlayerTier(player, tier)) {
                player.sendMessage(ChatColor.GREEN + 
                    String.format("Your hostility tier has been set to Tier %d!", tier));
                player.closeInventory();
                
                logger.fine(String.format("Player %s selected hostility tier %d", player.getName(), tier));
            } else {
                player.sendMessage(ChatColor.RED + "Failed to set hostility tier. Please try again.");
            }
            
        } catch (NumberFormatException e) {
            player.sendMessage(ChatColor.RED + "Invalid tier format.");
            logger.warning(String.format("Invalid tier format in GUI item: %s", displayName));
        }
    }
    
    /**
     * Creates a GUI item with the specified properties.
     */
    private ItemStack createGuiItem(Material material, String displayName, String[] lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        
        if (meta != null) {
            meta.setDisplayName(displayName);
            if (lore != null) {
                meta.setLore(Arrays.asList(lore));
            }
            item.setItemMeta(meta);
        }
        
        return item;
    }
    
    /**
     * Safely gets a Material from a string, with fallback.
     */
    private Material getMaterialSafely(String materialName, Material fallback) {
        try {
            return Material.valueOf(materialName.toUpperCase());
        } catch (IllegalArgumentException e) {
            logger.warning(String.format("Invalid material in config: %s, using fallback: %s", 
                          materialName, fallback));
            return fallback;
        }
    }
}