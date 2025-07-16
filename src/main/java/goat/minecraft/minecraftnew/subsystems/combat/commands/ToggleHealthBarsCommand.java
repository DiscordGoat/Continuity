package goat.minecraft.minecraftnew.subsystems.combat.commands;

import goat.minecraft.minecraftnew.subsystems.combat.notification.MonsterHealthBarService;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Command allowing players to toggle monster health bar visibility.
 */
public class ToggleHealthBarsCommand implements CommandExecutor {

    private final MonsterHealthBarService service;

    public ToggleHealthBarsCommand(MonsterHealthBarService service) {
        this.service = service;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("This command can only be used by players.");
            return true;
        }
        service.toggle(player);
        return true;
    }
}
