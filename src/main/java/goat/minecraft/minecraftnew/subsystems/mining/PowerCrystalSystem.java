package goat.minecraft.minecraftnew.subsystems.mining;

import goat.minecraft.minecraftnew.MinecraftNew;
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

import java.util.ArrayList;
import java.util.List;

public class PowerCrystalSystem implements Listener {
    private final MinecraftNew plugin;
    
    public PowerCrystalSystem(MinecraftNew plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        
        ItemStack cursor = event.getCursor();
        ItemStack clicked = event.getCurrentItem();
        
        // Check if dragging power crystal onto diamond tool
        if (isPowerCrystal(cursor) && isDiamondTool(clicked)) {
            event.setCancelled(true);
            applyPowerCrystalToTool(player, cursor, clicked);
        }
    }
    
    private boolean isPowerCrystal(ItemStack item) {
        if (item == null || item.getType() != Material.PRISMARINE_CRYSTALS) return false;
        if (!item.hasItemMeta() || !item.getItemMeta().hasDisplayName()) return false;
        
        String name = ChatColor.stripColor(item.getItemMeta().getDisplayName());
        return name.equals("Power Crystal");
    }
    
    private boolean isDiamondTool(ItemStack item) {
        if (item == null) return false;
        Material type = item.getType();
        return type == Material.DIAMOND_PICKAXE || type == Material.NETHERITE_PICKAXE ||
               type == Material.DIAMOND_SHOVEL || type == Material.DIAMOND_HOE;
    }
    
    private void applyPowerCrystalToTool(Player player, ItemStack crystal, ItemStack tool) {
        // Get current power cap
        int currentCap = getCurrentPowerCap(tool);
        
        // Check if already at max cap (500%)
        if (currentCap >= 500) {
            player.sendMessage(ChatColor.RED + "This tool has already reached the maximum power cap of 500%!");
            return;
        }
        
        // Increase cap by 100%
        int newCap = Math.min(currentCap + 100, 500);
        setPowerCap(tool, newCap);
        
        // Refresh the gemstone power bar with new cap
        refreshGemstonePowerBar(tool, newCap);
        
        // Consume one crystal from cursor
        if (crystal.getAmount() > 1) {
            crystal.setAmount(crystal.getAmount() - 1);
        } else {
            player.setItemOnCursor(null);
        }
        
        // Success feedback
        player.sendMessage(ChatColor.GREEN + "Power Crystal applied! Power cap increased to " + newCap + "%");
        player.playSound(player.getLocation(), Sound.BLOCK_BEACON_POWER_SELECT, 1.0f, 1.5f);
    }
    
    private int getCurrentPowerCap(ItemStack tool) {
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
    
    private void setPowerCap(ItemStack tool, int cap) {
        ItemMeta meta = tool.getItemMeta();
        List<String> lore = meta.hasLore() ? new ArrayList<>(meta.getLore()) : new ArrayList<>();
        
        // Remove existing power cap line
        lore.removeIf(line -> ChatColor.stripColor(line).startsWith("Power Cap: "));
        
        // Add new power cap line after gemstone power section
        int insertIndex = findPowerCapInsertionPoint(lore);
        String capLine = ChatColor.AQUA + "Power Cap: " + ChatColor.YELLOW + cap + "%";
        lore.add(insertIndex, capLine);
        
        meta.setLore(lore);
        tool.setItemMeta(meta);
    }
    
    private int findPowerCapInsertionPoint(List<String> lore) {
        // Insert after gemstone power progress bar
        for (int i = 0; i < lore.size(); i++) {
            String line = lore.get(i);
            if (line.contains("[") && line.contains("|") && line.contains("]")) {
                return i + 1; // Right after progress bar
            }
        }
        // If no gemstone power found, insert at beginning
        return 0;
    }
    
    /**
     * Refreshes the gemstone power bar to reflect the new power cap
     */
    private void refreshGemstonePowerBar(ItemStack tool, int newCap) {
        if (!tool.hasItemMeta() || !tool.getItemMeta().hasLore()) return;
        
        ItemMeta meta = tool.getItemMeta();
        List<String> lore = new ArrayList<>(meta.getLore());
        
        // Get current gemstone power
        int currentPower = getCurrentGemstonePower(tool);
        
        // Remove existing gemstone power lines (power line, progress bar, and empty line)
        lore.removeIf(line -> {
            String stripped = ChatColor.stripColor(line);
            return stripped.startsWith("Gemstone Power: ") || 
                   (line.contains("[") && line.contains("|") && line.contains("]"));
        });
        
        // Create new progress bar with extended length based on cap
        String powerLine = ChatColor.AQUA + "Gemstone Power: " + ChatColor.YELLOW + currentPower + "%" + 
                          ChatColor.GRAY + " / " + ChatColor.YELLOW + newCap + "%";
        String progressBar = createExtendedProgressBar(currentPower, newCap);
        
        // Find insertion point (after any existing empty lines at the top)
        int insertIndex = 0;
        while (insertIndex < lore.size() && ChatColor.stripColor(lore.get(insertIndex)).isEmpty()) {
            insertIndex++;
        }
        
        // Insert new gemstone power lines
        lore.add(insertIndex, "");
        lore.add(insertIndex, progressBar);
        lore.add(insertIndex, powerLine);
        
        meta.setLore(lore);
        tool.setItemMeta(meta);
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
    
    /**
     * Gets the current gemstone power from a tool
     */
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
    
    private void updateToolPowerCapLore(ItemStack tool, int newCap) {
        // This method is no longer needed since refreshGemstonePowerBar handles everything
    }
    
    /**
     * Creates a Power Crystal item
     */
    public static ItemStack createPowerCrystal() {
        ItemStack crystal = new ItemStack(Material.PRISMARINE_CRYSTALS);
        ItemMeta meta = crystal.getItemMeta();
        
        meta.setDisplayName(ChatColor.LIGHT_PURPLE + "Power Crystal");
        
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + "A rare crystal that expands the");
        lore.add(ChatColor.GRAY + "gemstone power capacity of tools");
        lore.add("");
        lore.add(ChatColor.YELLOW + "Effect: " + ChatColor.WHITE + "+100% Power Cap");
        lore.add(ChatColor.YELLOW + "Maximum: " + ChatColor.WHITE + "500% Total Cap");
        lore.add("");
        lore.add(ChatColor.DARK_PURPLE + "Drag onto diamond tools to apply");
        
        meta.setLore(lore);
        crystal.setItemMeta(meta);
        
        return crystal;
    }
}