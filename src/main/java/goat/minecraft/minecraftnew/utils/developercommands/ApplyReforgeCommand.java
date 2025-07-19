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
 * Developer command that applies a reforge tier to the held item.
 * Usage: /applyreforge <reforgename> <rarity>
 */
public class ApplyReforgeCommand implements CommandExecutor {
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

        if (args.length != 2) {
            player.sendMessage(ChatColor.RED + "Usage: /applyreforge <reforgename> <rarity>");
            return true;
        }

        ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null || item.getType() == Material.AIR) {
            player.sendMessage(ChatColor.RED + "You must be holding an item.");
            return true;
        }

        String type = args[0].toLowerCase();
        boolean matches = switch (type) {
            case "sword" -> reforgeManager.isSword(item);
            case "armor" -> reforgeManager.isArmor(item);
            case "tool" -> reforgeManager.isTool(item);
            case "bow" -> reforgeManager.isBow(item);
            default -> false;
        };

        if (!matches) {
            player.sendMessage(ChatColor.RED + "Held item is not a " + type + ".");
            return true;
        }

        ReforgeManager.ReforgeTier tier = parseTier(args[1]);
        if (tier == null) {
            player.sendMessage(ChatColor.RED + "Unknown rarity. Use common, uncommon, rare, epic or legendary.");
            return true;
        }

        reforgeManager.applyReforge(item, tier);
        player.sendMessage(ChatColor.GREEN + "Applied " + args[1].toLowerCase() + " reforge.");
        return true;
    }

    private ReforgeManager.ReforgeTier parseTier(String arg) {
        return switch (arg.toLowerCase()) {
            case "common", "1" -> ReforgeManager.ReforgeTier.TIER_1;
            case "uncommon", "2" -> ReforgeManager.ReforgeTier.TIER_2;
            case "rare", "3" -> ReforgeManager.ReforgeTier.TIER_3;
            case "epic", "4" -> ReforgeManager.ReforgeTier.TIER_4;
            case "legendary", "5" -> ReforgeManager.ReforgeTier.TIER_5;
            case "0" -> ReforgeManager.ReforgeTier.TIER_0;
            default -> null;
        };
    }
}
