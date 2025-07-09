package goat.minecraft.minecraftnew.other.beacon;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BeaconManager implements Listener {

    private final JavaPlugin plugin;
    
    // Map material blocks to their beacon power values
    private static final Map<Material, Integer> MATERIAL_POWER_VALUES = new HashMap<>();
    
    static {
        // 1 beacon power
        MATERIAL_POWER_VALUES.put(Material.COPPER_BLOCK, 3);
        MATERIAL_POWER_VALUES.put(Material.COAL_BLOCK, 3);
        
        // 2 beacon power
        MATERIAL_POWER_VALUES.put(Material.IRON_BLOCK, 4);
        
        // 3 beacon power
        MATERIAL_POWER_VALUES.put(Material.REDSTONE_BLOCK, 6);
        MATERIAL_POWER_VALUES.put(Material.LAPIS_BLOCK, 6);
        MATERIAL_POWER_VALUES.put(Material.GOLD_BLOCK, 6);
        MATERIAL_POWER_VALUES.put(Material.EMERALD_BLOCK, 6);
        
        // 4 beacon power
        MATERIAL_POWER_VALUES.put(Material.DIAMOND_BLOCK, 8);
    }

    public BeaconManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        ItemStack item = event.getItemInHand();
        if (item.getType() == Material.BEACON && !isCustomBeacon(item)) {
            // Transform vanilla beacon to custom beacon
            ItemStack customBeacon = createCustomBeacon(0);
            event.getPlayer().getInventory().setItemInMainHand(customBeacon);
            event.setCancelled(true);
            event.getPlayer().sendMessage(ChatColor.GOLD + "Your beacon has been transformed into a Beacon Charm!");
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (event.getBlock().getType() == Material.BEACON) {
            // Drop custom beacon instead of vanilla beacon
            event.setDropItems(false);
            ItemStack customBeacon = createCustomBeacon(0);
            event.getBlock().getWorld().dropItemNaturally(event.getBlock().getLocation(), customBeacon);
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        
        if (item == null || !isCustomBeacon(item)) return;
        
        // Left click in air or right click on block
        if (event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            event.setCancelled(true);
            openBeaconCharmGUI(player, item);
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        
        ItemStack cursor = event.getCursor();
        ItemStack clicked = event.getCurrentItem();
        
        if (cursor == null || clicked == null) return;
        
        // Check if cursor item is a material block
        if (!isMaterialBlock(cursor)) return;
        
        // Check if clicked item is a custom beacon
        if (!isCustomBeacon(clicked)) return;
        
        // Apply the material to the beacon
        if (applyMaterialToBeacon(cursor, clicked, player)) {
            // Remove the entire stack from cursor
            event.setCursor(null);
            event.setCancelled(true);
            
            // Play success sound
            player.playSound(player.getLocation(), Sound.BLOCK_BEACON_POWER_SELECT, 1.0f, 1.2f);
            
            // Success message
            String materialName = formatMaterialName(cursor.getType());
            int powerGained = MATERIAL_POWER_VALUES.getOrDefault(cursor.getType(), isNetherStardust(cursor) ? 1000 : 0) * cursor.getAmount();
            if (isNetherStardust(cursor)) {
                materialName = "Nether Stardust";
            }
            player.sendMessage(ChatColor.GOLD + "Applied " + ChatColor.YELLOW + cursor.getAmount() + "x " + materialName +
                             ChatColor.GOLD + " (+" + powerGained + " Beacon Power) to your beacon!");
        }
    }

    private void openBeaconCharmGUI(Player player, ItemStack beacon) {
        BeaconCharmGUI gui = new BeaconCharmGUI(plugin, beacon);
        gui.openGUI(player);
    }

    private boolean isMaterialBlock(ItemStack item) {
        if (item == null) return false;
        return MATERIAL_POWER_VALUES.containsKey(item.getType()) || isNetherStardust(item);
    }

    private boolean isNetherStardust(ItemStack item) {
        if (item == null || item.getType() != Material.NETHER_STAR) return false;
        if (!item.hasItemMeta() || !item.getItemMeta().hasDisplayName()) return false;
        String name = ChatColor.stripColor(item.getItemMeta().getDisplayName());
        return name.equalsIgnoreCase("Nether Stardust");
    }

    private boolean isCustomBeacon(ItemStack item) {
        if (item == null || item.getType() != Material.BEACON) return false;
        if (!item.hasItemMeta() || !item.getItemMeta().hasDisplayName()) return false;
        
        String displayName = ChatColor.stripColor(item.getItemMeta().getDisplayName());
        return displayName.equals("Beacon Charm");
    }

    private boolean applyMaterialToBeacon(ItemStack material, ItemStack beacon, Player player) {
        Material materialType = material.getType();
        Integer powerPerBlock;

        if (isNetherStardust(material)) {
            powerPerBlock = 1000;
        } else {
            powerPerBlock = MATERIAL_POWER_VALUES.get(materialType);
        }
        
        if (powerPerBlock == null) {
            player.sendMessage(ChatColor.RED + "Unknown material type!");
            return false;
        }
        
        // Get current beacon power and tier
        int currentPower = getCurrentBeaconPower(beacon);
        int oldTier = getBeaconTierFromPower(currentPower);
        int powerToAdd = powerPerBlock * material.getAmount();
        int newPower = Math.min(currentPower + powerToAdd, 10000);
        
        if (newPower == currentPower) {
            player.sendMessage(ChatColor.RED + "This beacon is already at maximum power (10,000)!");
            return false;
        }
        
        // Update the beacon's lore with new power
        updateBeaconPower(beacon, newPower);

        // Play a toast sound if we reached a new tier
        int newTier = getBeaconTierFromPower(newPower);
        if (newTier > oldTier) {
            player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
        }
        
        return true;
    }

    private int getCurrentBeaconPower(ItemStack beacon) {
        if (!beacon.hasItemMeta() || !beacon.getItemMeta().hasLore()) return 0;
        
        List<String> lore = beacon.getItemMeta().getLore();
        for (String line : lore) {
            String stripped = ChatColor.stripColor(line);
            if (stripped.startsWith("Beacon Power: ")) {
                try {
                    String powerText = stripped.replace("Beacon Power: ", "").replace(",", "");
                    return Integer.parseInt(powerText);
                } catch (NumberFormatException e) {
                    return 0;
                }
            }
        }
        return 0;
    }

    private void updateBeaconPower(ItemStack beacon, int newPower) {
        ItemMeta meta = beacon.getItemMeta();
        List<String> lore = meta.hasLore() ? new ArrayList<>(meta.getLore()) : new ArrayList<>();
        
        // Remove existing beacon power lines more precisely
        List<String> newLore = new ArrayList<>();
        boolean skipNext = false;
        
        for (int i = 0; i < lore.size(); i++) {
            String line = lore.get(i);
            String stripped = ChatColor.stripColor(line);
            
            // Skip beacon data lines
            if (stripped.startsWith("Beacon Power: ") || 
                (stripped.contains("⚡") && (stripped.contains("Tier") || stripped.contains("blocks") || stripped.contains("s"))) ||
                (line.contains("[") && line.contains("|") && line.contains("]"))) {
                skipNext = true; // Skip the empty line that follows
                continue;
            }
            
            // Skip the empty line that follows beacon data
            if (skipNext && stripped.isEmpty()) {
                skipNext = false;
                continue;
            }
            
            skipNext = false;
            newLore.add(line);
        }
        
        // Create new beacon power line with progress bar
        String powerLine = ChatColor.AQUA + "Beacon Power: " + ChatColor.YELLOW + String.format("%,d", newPower);
        String progressBar = createBeaconProgressBar(newPower);
        
        // Add tier, range, and duration info with better formatting
        int tier = getBeaconTierFromPower(newPower);
        int range = getBeaconRangeFromTier(tier);
        int duration = getBeaconDurationFromTier(tier);
        String tierInfo = ChatColor.GRAY + "⚡ " + ChatColor.GOLD + "Tier " + tier + ChatColor.GRAY + "/6" + 
                         ChatColor.GRAY + " | " + ChatColor.BLUE + "⭐ " + range + " blocks" + 
                         ChatColor.GRAY + " | " + ChatColor.GREEN + "⏰ " + duration + "s";
        
        // Add the beacon power lines at the beginning of lore
        newLore.add(0, "");
        newLore.add(0, tierInfo);
        newLore.add(0, progressBar);
        newLore.add(0, powerLine);
        
        meta.setLore(newLore);
        beacon.setItemMeta(meta);
    }

    private String createBeaconProgressBar(int power) {
        int barLength = 20;
        double percentage = (double) power / 10000.0;
        int filledBars = (int) (percentage * barLength);
        int emptyBars = barLength - filledBars;
        
        StringBuilder bar = new StringBuilder();
        bar.append(ChatColor.DARK_GRAY + "[");
        
        // Color segments based on progress
        for (int i = 0; i < filledBars; i++) {
            double segmentPercentage = (double) i / barLength;
            if (segmentPercentage < 0.2) {
                bar.append(ChatColor.WHITE); // Iron colored (0-20%)
            } else if (segmentPercentage < 0.4) {
                bar.append(ChatColor.YELLOW); // Gold colored (20-40%)
            } else if (segmentPercentage < 0.6) {
                bar.append(ChatColor.AQUA); // Diamond colored (40-60%)
            } else if (segmentPercentage < 0.8) {
                bar.append(ChatColor.GREEN); // Emerald colored (60-80%)
            } else {
                bar.append(ChatColor.DARK_RED); // Dark red (80-100%)
            }
            bar.append("|");
        }
        
        // Add empty portion
        bar.append(ChatColor.GRAY);
        for (int i = 0; i < emptyBars; i++) {
            bar.append("|");
        }
        
        bar.append(ChatColor.DARK_GRAY + "]");
        return bar.toString();
    }

    private String formatMaterialName(Material material) {
        String name = material.name().toLowerCase().replace("_", " ");
        String[] words = name.split(" ");
        StringBuilder formatted = new StringBuilder();
        
        for (String word : words) {
            if (formatted.length() > 0) formatted.append(" ");
            formatted.append(word.substring(0, 1).toUpperCase()).append(word.substring(1));
        }
        
        return formatted.toString();
    }

    public static ItemStack createCustomBeacon(int power) {
        ItemStack beacon = new ItemStack(Material.BEACON);
        ItemMeta meta = beacon.getItemMeta();
        
        meta.setDisplayName(ChatColor.GOLD + "Beacon Charm");
        
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.AQUA + "Beacon Power: " + ChatColor.YELLOW + String.format("%,d", power));
        
        // Create progress bar
        BeaconManager tempManager = new BeaconManager(null);
        lore.add(tempManager.createBeaconProgressBar(power));
        
        // Add tier info with better formatting
        int tier = getBeaconTierFromPower(power);
        int range = getBeaconRangeFromTier(tier);
        int duration = getBeaconDurationFromTier(tier);
        lore.add(ChatColor.GRAY + "⚡ " + ChatColor.GOLD + "Tier " + tier + ChatColor.GRAY + "/6" + 
                ChatColor.GRAY + " | " + ChatColor.BLUE + "⭐ " + range + " blocks" + 
                ChatColor.GRAY + " | " + ChatColor.GREEN + "⏰ " + duration + "s");
        lore.add("");
        lore.add(ChatColor.GRAY + "A mystical beacon that can be");
        lore.add(ChatColor.GRAY + "powered by material blocks.");
        lore.add("");
        lore.add(ChatColor.YELLOW + "Left-click in air or right-click");
        lore.add(ChatColor.YELLOW + "on a block to open the GUI!");
        
        meta.setLore(lore);
        beacon.setItemMeta(meta);
        
        return beacon;
    }

    /**
     * Gets the beacon power of a beacon item
     * @param beacon The beacon to check
     * @return The beacon power (0 to 10,000)
     */
    public static int getBeaconPower(ItemStack beacon) {
        if (beacon == null || !beacon.hasItemMeta() || !beacon.getItemMeta().hasLore()) return 0;
        
        List<String> lore = beacon.getItemMeta().getLore();
        for (String line : lore) {
            String stripped = ChatColor.stripColor(line);
            if (stripped.startsWith("Beacon Power: ")) {
                try {
                    String powerText = stripped.replace("Beacon Power: ", "").replace(",", "");
                    return Integer.parseInt(powerText);
                } catch (NumberFormatException e) {
                    return 0;
                }
            }
        }
        return 0;
    }

    /**
     * Checks if a beacon has any power
     * @param beacon The beacon to check
     * @return True if the beacon has any power
     */
    public static boolean hasBeaconPower(ItemStack beacon) {
        return getBeaconPower(beacon) > 0;
    }

    /**
     * Gets the tier of a beacon based on its power
     * @param beacon The beacon to check
     * @return Tier (1-5)
     */
    public static int getBeaconTier(ItemStack beacon) {
        return getBeaconTierFromPower(getBeaconPower(beacon));
    }

    private static int getBeaconTierFromPower(int power) {
        if (power < 2000) return 1; // 0-1999
        if (power < 4000) return 2; // 2000-3999 // 16.66-33.33%
        if (power < 6000) return 3; // 4000-5999 // 33.33-50%
        if (power < 8000) return 4; // 6000-7999
        if (power < 10000) return 5; // 8000-9999
        return 6; // 10000 exactly
    }

    /**
     * Gets the range of a beacon based on its tier
     * @param beacon The beacon to check
     * @return Range in blocks
     */
    public static int getBeaconRange(ItemStack beacon) {
        return getBeaconRangeFromTier(getBeaconTier(beacon));
    }

    private static int getBeaconRangeFromTier(int tier) {
        switch (tier) {
            case 1: return 30;
            case 2: return 60;
            case 3: return 90;
            case 4: return 120;
            case 5: return 180;
            case 6: return 250;
            default: return 30;
        }
    }

    /**
     * Gets the duration of a beacon based on its tier
     * @param beacon The beacon to check
     * @return Duration in seconds
     */
    public static int getBeaconDuration(ItemStack beacon) {
        return getBeaconDurationFromTier(getBeaconTier(beacon));
    }

    private static int getBeaconDurationFromTier(int tier) {
        switch (tier) {
            case 1: return 30;
            case 2: return 35;
            case 3: return 50;
            case 4: return 60;
            case 5: return 80;
            case 6: return 120;
            default: return 30;
        }
    }

    /**
     * Gets the selected catalyst from a beacon
     * @param beacon The beacon to check
     * @return The selected catalyst name, or null if none selected
     */
    public static String getSelectedCatalyst(ItemStack beacon) {
        if (!beacon.hasItemMeta() || !beacon.getItemMeta().hasLore()) return null;
        
        List<String> lore = beacon.getItemMeta().getLore();
        for (String line : lore) {
            String stripped = ChatColor.stripColor(line);
            if (stripped.startsWith("Selected Catalyst: ")) {
                return stripped.replace("Selected Catalyst: ", "");
            }
        }
        return null;
    }

    /**
     * Sets the selected catalyst for a beacon
     * @param beacon The beacon to modify
     * @param catalystName The catalyst name to set
     */
    public static void setSelectedCatalyst(ItemStack beacon, String catalystName) {
        ItemMeta meta = beacon.getItemMeta();
        List<String> lore = meta.hasLore() ? new ArrayList<>(meta.getLore()) : new ArrayList<>();
        
        // Remove existing catalyst selection lines
        List<String> newLore = new ArrayList<>();
        boolean skipNext = false;
        
        for (int i = 0; i < lore.size(); i++) {
            String line = lore.get(i);
            String stripped = ChatColor.stripColor(line);
            
            // Skip catalyst selection lines
            if (stripped.startsWith("Selected Catalyst: ") ||
                (stripped.contains("✦") && stripped.contains("Active catalyst"))) {
                skipNext = true; // Skip the empty line that follows
                continue;
            }
            
            // Skip the empty line that follows catalyst data
            if (skipNext && stripped.isEmpty()) {
                skipNext = false;
                continue;
            }
            
            skipNext = false;
            newLore.add(line);
        }
        
        // Find the position to insert catalyst info (after tier info but before description)
        int insertIndex = 4; // Default position after power/progress/tier/empty line
        for (int i = 0; i < newLore.size(); i++) {
            String stripped = ChatColor.stripColor(newLore.get(i));
            if (stripped.contains("A mystical beacon") || stripped.contains("Left-click")) {
                insertIndex = i;
                break;
            }
        }
        
        // Add catalyst selection info
        if (catalystName != null && !catalystName.isEmpty()) {
            String catalystColor = getCatalystColor(catalystName);
            newLore.add(insertIndex, ChatColor.GOLD + "Selected Catalyst: " + catalystColor + catalystName);
            newLore.add(insertIndex + 1, ChatColor.GRAY + "✦ " + ChatColor.WHITE + "Active catalyst effect");
            newLore.add(insertIndex + 2, "");
        }
        
        meta.setLore(newLore);
        beacon.setItemMeta(meta);
    }

    /**
     * Gets the appropriate color for a catalyst name
     */
    private static String getCatalystColor(String catalystName) {
        switch (catalystName) {
            case "Catalyst of Power": return ChatColor.RED.toString();
            case "Catalyst of Flight": return ChatColor.AQUA.toString();
            case "Catalyst of Depth": return ChatColor.DARK_AQUA.toString();
            case "Catalyst of Insanity": return ChatColor.DARK_PURPLE.toString();
            case "Catalyst of Rejuvenation": return ChatColor.GOLD.toString();
            case "Catalyst of Prosperity": return ChatColor.GREEN.toString();
            default: return ChatColor.WHITE.toString();
        }
    }

    /**
     * Finds and updates the beacon in the player's inventory with the selected catalyst
     * @param player The player whose inventory to search
     * @param catalystName The catalyst name to set
     * @return true if beacon was found and updated, false otherwise
     */
    public static boolean updateBeaconInInventory(Player player, String catalystName) {
        // Check all inventory slots for custom beacon
        for (int i = 0; i < player.getInventory().getSize(); i++) {
            ItemStack item = player.getInventory().getItem(i);
            if (item != null && isBeaconCharm(item)) {
                setSelectedCatalyst(item, catalystName);
                // Force inventory update
                player.getInventory().setItem(i, item);
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if an ItemStack is a custom beacon
     */
    private static boolean isBeaconCharm(ItemStack item) {
        if (item == null || item.getType() != Material.BEACON) return false;
        if (!item.hasItemMeta() || !item.getItemMeta().hasDisplayName()) return false;
        
        String displayName = ChatColor.stripColor(item.getItemMeta().getDisplayName());
        return displayName.equals("Beacon Charm");
    }
}