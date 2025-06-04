package goat.minecraft.minecraftnew.subsystems.mining;

import goat.minecraft.minecraftnew.utils.devtools.ItemRegistry;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class  GemstoneApplicationSystem implements Listener {

    private final JavaPlugin plugin;
    
    // Map gemstone names to their power values
    private static final Map<String, Integer> GEMSTONE_POWER_VALUES = new HashMap<>();
    
    static {
        // Common gemstones (+1 power)
        GEMSTONE_POWER_VALUES.put("Quartz", 1);
        GEMSTONE_POWER_VALUES.put("Hematite", 1);
        GEMSTONE_POWER_VALUES.put("Obsidian", 1);
        GEMSTONE_POWER_VALUES.put("Agate", 1);
        
        // Uncommon gemstones (+3 power)
        GEMSTONE_POWER_VALUES.put("Turquoise", 3);
        GEMSTONE_POWER_VALUES.put("Amethyst", 3);
        GEMSTONE_POWER_VALUES.put("Citrine", 3);
        GEMSTONE_POWER_VALUES.put("Garnet", 3);
        
        // Rare gemstones (+7 power)
        GEMSTONE_POWER_VALUES.put("Topaz", 7);
        GEMSTONE_POWER_VALUES.put("Peridot", 7);
        GEMSTONE_POWER_VALUES.put("Aquamarine", 7);
        GEMSTONE_POWER_VALUES.put("Tanzanite", 7);
        
        // Epic gemstones (+10 power)
        GEMSTONE_POWER_VALUES.put("Sapphire", 10);
        GEMSTONE_POWER_VALUES.put("Ruby", 10);
        
        // Legendary gemstones (+20 power)
        GEMSTONE_POWER_VALUES.put("Emerald", 20);
        GEMSTONE_POWER_VALUES.put("Diamond", 20);
    }

    public GemstoneApplicationSystem(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        
        // Check if player is clicking with a gemstone
        ItemStack cursor = event.getCursor();
        ItemStack clicked = event.getCurrentItem();
        
        if (cursor == null || clicked == null) return;
        
        // Check if cursor item is a gemstone
        if (!isGemstone(cursor)) return;
        
        // Check if clicked item is a diamond tool
        if (!isDiamondTool(clicked)) {
            if (isAnyTool(clicked)) {
                player.sendMessage(ChatColor.RED + "Only diamond tools can withstand Gemstone Power!");
                event.setCancelled(true);
            }
            return;
        }
        
        // Apply the gemstone to the tool
        if (applyGemstoneToTool(cursor, clicked, player)) {
            // Decrement the gemstone stack by 1
            if (cursor.getAmount() > 1) {
                cursor.setAmount(cursor.getAmount() - 1);
                event.setCursor(cursor);
            } else {
                // If only 1 item, remove it completely
                event.setCursor(null);
            }
            event.setCancelled(true);
            
            // Play success sound
            player.playSound(player.getLocation(), Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1.0f, 1.2f);
            
            // Success message
            String gemstoneName = ChatColor.stripColor(cursor.getItemMeta().getDisplayName());
            int powerGained = GEMSTONE_POWER_VALUES.get(gemstoneName);
            player.sendMessage(ChatColor.GREEN + "Applied " + ChatColor.YELLOW + gemstoneName + 
                             ChatColor.GREEN + " (+" + powerGained + "% Gemstone Power) to your tool!");
        }
    }

    private boolean isGemstone(ItemStack item) {
        if (item == null || !item.hasItemMeta() || !item.getItemMeta().hasLore()) return false;
        
        List<String> lore = item.getItemMeta().getLore();
        if (lore == null) return false;
        
        // Check if the last line of lore contains "Gemstone"
        for (String line : lore) {
            if (ChatColor.stripColor(line).equals("Gemstone")) {
                return true;
            }
        }
        return false;
    }

    private boolean isDiamondTool(ItemStack item) {
        if (item == null) return false;
        Material material = item.getType();
        
        return material == Material.DIAMOND_PICKAXE ||
               material == Material.NETHERITE_PICKAXE;
    }
    
    private boolean isAnyTool(ItemStack item) {
        if (item == null) return false;
        String materialName = item.getType().name();
        
        return materialName.contains("PICKAXE") ||
               materialName.contains("AXE") ||
               materialName.contains("SHOVEL") ||
               materialName.contains("HOE");
    }

    private boolean applyGemstoneToTool(ItemStack gemstone, ItemStack tool, Player player) {
        String gemstoneName = ChatColor.stripColor(gemstone.getItemMeta().getDisplayName());
        Integer powerValue = GEMSTONE_POWER_VALUES.get(gemstoneName);
        
        if (powerValue == null) {
            player.sendMessage(ChatColor.RED + "Unknown gemstone type!");
            return false;
        }
        
        // Get current gemstone power and power cap from tool
        int currentPower = getCurrentGemstonePower(tool);
        int powerCap = getToolPowerCap(tool);
        int newPower = Math.min(currentPower + powerValue, powerCap);
        
        if (newPower == currentPower) {
            player.sendMessage(ChatColor.RED + "This tool is already at maximum Gemstone Power (" + powerCap + "%)!");
            return false;
        }
        
        // Update the tool's lore with new gemstone power
        updateToolGemstonePower(tool, newPower);
        
        return true;
    }

    private int getCurrentGemstonePower(ItemStack tool) {
        if (!tool.hasItemMeta() || !tool.getItemMeta().hasLore()) return 0;
        
        List<String> lore = tool.getItemMeta().getLore();
        for (String line : lore) {
            String stripped = ChatColor.stripColor(line);
            if (stripped.startsWith("Gemstone Power: ")) {
                try {
                    // Handle both "X%" and "X% / Y%" formats
                    String powerText = stripped.replace("Gemstone Power: ", "");
                    if (powerText.contains(" / ")) {
                        powerText = powerText.split(" / ")[0]; // Get the current power part
                    }
                    powerText = powerText.replace("%", "");
                    return Integer.parseInt(powerText);
                } catch (NumberFormatException e) {
                    return 0;
                }
            }
        }
        return 0;
    }

    private void updateToolGemstonePower(ItemStack tool, int newPower) {
        ItemMeta meta = tool.getItemMeta();
        List<String> lore = meta.hasLore() ? new ArrayList<>(meta.getLore()) : new ArrayList<>();
        
        // Get the power cap for this tool
        int powerCap = getToolPowerCap(tool);
        
        // Remove existing gemstone power lines (power line, progress bar, and empty line)
        lore.removeIf(line -> {
            String stripped = ChatColor.stripColor(line);
            return stripped.startsWith("Gemstone Power: ") || 
                   line.contains("[") && line.contains("|") && line.contains("]") ||
                   (stripped.isEmpty() && lore.indexOf(line) <= 2); // Remove empty lines at top
        });
        
        // Create new gemstone power line with progress bar
        String powerLine;
        if (powerCap > 100) {
            powerLine = ChatColor.AQUA + "Gemstone Power: " + ChatColor.YELLOW + newPower + "%" + 
                       ChatColor.GRAY + " / " + ChatColor.YELLOW + powerCap + "%";
        } else {
            powerLine = ChatColor.AQUA + "Gemstone Power: " + ChatColor.YELLOW + newPower + "%";
        }
        
        String progressBar = createExtendedProgressBar(newPower, powerCap);
        
        // Add the gemstone power lines at the beginning of lore
        lore.add(0, "");
        lore.add(0, progressBar);
        lore.add(0, powerLine);
        
        meta.setLore(lore);
        tool.setItemMeta(meta);
    }
    
    /**
     * Gets the power cap for a tool (default 100%, can be increased with Power Crystals)
     */
    private int getToolPowerCap(ItemStack tool) {
        if (!tool.hasItemMeta() || !tool.getItemMeta().hasLore()) return 100;
        
        List<String> lore = tool.getItemMeta().getLore();
        for (String line : lore) {
            String stripped = ChatColor.stripColor(line);
            if (stripped.startsWith("Power Cap: ")) {
                String capStr = stripped.substring("Power Cap: ".length()).replace("%", "");
                try {
                    return Integer.parseInt(capStr);
                } catch (NumberFormatException e) {
                    return 100;
                }
            }
        }
        return 100;
    }
    
    /**
     * Creates an extended progress bar that grows with the power cap
     */
    private String createExtendedProgressBar(int current, int cap) {
        // Base bar length of 20, plus 5 chars for each 100% above base 100%
        int baseBarLength = 20;
        int extraSegments = (cap - 100) / 100;
        int totalBarLength = baseBarLength + (extraSegments * 5);
        
        int filledBars = (int) ((double) current / cap * totalBarLength);
        int emptyBars = totalBarLength - filledBars;
        
        StringBuilder bar = new StringBuilder();
        bar.append(ChatColor.DARK_GRAY + "[");
        bar.append(ChatColor.GREEN);
        
        // Add filled portion
        for (int i = 0; i < filledBars; i++) {
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

    private String createProgressBar(int current, int max, int barLength) {
        int filledBars = (int) ((double) current / max * barLength);
        int emptyBars = barLength - filledBars;
        
        StringBuilder bar = new StringBuilder();
        bar.append(ChatColor.DARK_GRAY + "[");
        bar.append(ChatColor.GREEN);
        
        // Add filled portion
        for (int i = 0; i < filledBars; i++) {
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

    /**
     * Gets the gemstone power of a tool
     * @param tool The tool to check
     * @return The gemstone power (0 to power cap, max 500)
     */
    public static int getToolGemstonePower(ItemStack tool) {
        if (tool == null || !tool.hasItemMeta() || !tool.getItemMeta().hasLore()) return 0;
        
        List<String> lore = tool.getItemMeta().getLore();
        for (String line : lore) {
            String stripped = ChatColor.stripColor(line);
            if (stripped.startsWith("Gemstone Power: ")) {
                try {
                    // Handle both "X%" and "X% / Y%" formats
                    String powerText = stripped.replace("Gemstone Power: ", "");
                    if (powerText.contains(" / ")) {
                        powerText = powerText.split(" / ")[0]; // Get the current power part
                    }
                    powerText = powerText.replace("%", "");
                    return Integer.parseInt(powerText);
                } catch (NumberFormatException e) {
                    return 0;
                }
            }
        }
        return 0;
    }

    /**
     * Checks if a tool has gemstone power
     * @param tool The tool to check
     * @return True if the tool has any gemstone power
     */
    public static boolean hasGemstonePower(ItemStack tool) {
        return getToolGemstonePower(tool) > 0;
    }
}