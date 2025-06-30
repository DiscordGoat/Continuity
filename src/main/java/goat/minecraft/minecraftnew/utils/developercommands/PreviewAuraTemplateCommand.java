package goat.minecraft.minecraftnew.utils.developercommands;

import goat.minecraft.minecraftnew.subsystems.auras.Aura;
import goat.minecraft.minecraftnew.subsystems.auras.AuraManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Locale;

/**
 * Command: /previewauratemplate <name>
 * Activates a predefined aura template for the player.
 */
public class PreviewAuraTemplateCommand implements CommandExecutor {
    private final AuraManager auraManager;

    public PreviewAuraTemplateCommand(AuraManager auraManager) {
        this.auraManager = auraManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be used by players.");
            return true;
        }
        if (!player.hasPermission("continuity.admin")) {
            player.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
            return true;
        }
        if (args.length != 1) {
            player.sendMessage(ChatColor.YELLOW + "Usage: /" + label + " <name>");
            return true;
        }
        Aura aura;
        try {
            aura = Aura.valueOf(args[0].toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            player.sendMessage(ChatColor.RED + "Unknown aura: " + args[0]);
            return true;
        }
        auraManager.activateAura(player, aura);
        player.sendMessage(ChatColor.GRAY + "Use /aura to toggle visibility.");
        return true;
    }
}
