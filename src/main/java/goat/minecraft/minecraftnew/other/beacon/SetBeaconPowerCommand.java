package goat.minecraft.minecraftnew.other.beacon;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class SetBeaconPowerCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be used by players!");
            return true;
        }

        Player player = (Player) sender;

        // Check permission
        if (!player.hasPermission("continuity.admin")) {
            player.sendMessage(ChatColor.RED + "You don't have permission to use this command!");
            return true;
        }

        // Check arguments
        if (args.length != 1) {
            player.sendMessage(ChatColor.RED + "Usage: /setbeaconpower <power>");
            player.sendMessage(ChatColor.GRAY + "Power must be between 0 and 10,000");
            return true;
        }

        // Parse power value
        int power;
        try {
            power = Integer.parseInt(args[0]);
        } catch (NumberFormatException e) {
            player.sendMessage(ChatColor.RED + "Invalid number! Power must be between 0 and 10,000");
            return true;
        }

        // Validate power range
        if (power < 0 || power > 10000) {
            player.sendMessage(ChatColor.RED + "Power must be between 0 and 10,000!");
            return true;
        }

        // Check if player is holding a beacon
        ItemStack heldItem = player.getInventory().getItemInMainHand();
        if (!isCustomBeacon(heldItem)) {
            player.sendMessage(ChatColor.RED + "You must be holding a Beacon Charm to use this command!");
            return true;
        }

        // Determine the old tier before setting power
        int oldTier = BeaconManager.getBeaconTier(heldItem);

        // Set the beacon power
        setBeaconPower(heldItem, power);

        // Check if the tier increased and play a toast sound
        int newTier = BeaconManager.getBeaconTier(heldItem);
        if (newTier > oldTier) {
            player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
        }

        // Send success message
        int tier = BeaconManager.getBeaconTier(heldItem);
        int range = BeaconManager.getBeaconRange(heldItem);
        int duration = BeaconManager.getBeaconDuration(heldItem);
        
        player.sendMessage(ChatColor.GREEN + "Beacon power set to " + ChatColor.YELLOW + String.format("%,d", power) + ChatColor.GREEN + "!");
        player.sendMessage(ChatColor.GRAY + "⚡ " + ChatColor.GOLD + "Tier " + tier + 
                         ChatColor.GRAY + " | " + ChatColor.BLUE + "⭐ " + range + " blocks" + 
                         ChatColor.GRAY + " | " + ChatColor.GREEN + "⏰ " + duration + "s");

        return true;
    }

    private boolean isCustomBeacon(ItemStack item) {
        if (item == null || item.getType() != Material.BEACON) return false;
        if (!item.hasItemMeta() || !item.getItemMeta().hasDisplayName()) return false;
        
        String displayName = ChatColor.stripColor(item.getItemMeta().getDisplayName());
        return displayName.equals("Beacon Charm");
    }

    private void setBeaconPower(ItemStack beacon, int power) {
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
        
        lore = newLore;
        
        // Create new beacon power line with progress bar
        String powerLine = ChatColor.AQUA + "Beacon Power: " + ChatColor.YELLOW + String.format("%,d", power);
        String progressBar = createBeaconProgressBar(power);
        
        // Add tier, range, and duration info with better formatting
        int tier = getBeaconTierFromPower(power);
        int range = getBeaconRangeFromTier(tier);
        int duration = getBeaconDurationFromTier(tier);
        String tierInfo = ChatColor.GRAY + "⚡ " + ChatColor.GOLD + "Tier " + tier + 
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

    private int getBeaconTierFromPower(int power) {
        if (power < 2000) return 1; // 0-20%
        if (power < 4000) return 2; // 20-40%
        if (power < 6000) return 3; // 40-60%
        if (power < 8000) return 4; // 60-80%
        return 5; // 80-100%
    }

    private int getBeaconRangeFromTier(int tier) {
        switch (tier) {
            case 1: return 30;
            case 2: return 60;
            case 3: return 90;
            case 4: return 120;
            case 5: return 180;
            default: return 30;
        }
    }

    private int getBeaconDurationFromTier(int tier) {
        switch (tier) {
            case 1: return 30;
            case 2: return 35;
            case 3: return 50;
            case 4: return 60;
            case 5: return 80;
            default: return 30;
        }
    }
}