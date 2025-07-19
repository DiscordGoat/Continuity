package goat.minecraft.minecraftnew.utils.developercommands;

import goat.minecraft.minecraftnew.subsystems.smithing.tierreforgelisteners.ReforgeManager;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * Developer command to remove any reforge from the item in the player's hand.
 */
public class StripReforgeCommand implements CommandExecutor {
    private final ReforgeManager reforgeManager = new ReforgeManager();

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command.");
            return true;
        }

        if (!player.hasPermission("continuity.admin")) {
            player.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
            return true;
        }

        ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null || item.getType() == Material.AIR) {
            player.sendMessage(ChatColor.RED + "You must be holding an item.");
            return true;
        }

        if (reforgeManager.getReforgeTier(item) == 0) {
            player.sendMessage(ChatColor.YELLOW + "That item has no reforge.");
            return true;
        }

        reforgeManager.stripReforge(item);
        player.sendMessage(ChatColor.GREEN + "Reforge removed.");
        return true;
    }
}
