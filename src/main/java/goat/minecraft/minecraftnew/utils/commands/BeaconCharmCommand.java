package goat.minecraft.minecraftnew.utils.commands;

import goat.minecraft.minecraftnew.MinecraftNew;
import goat.minecraft.minecraftnew.subsystems.beacon.BeaconCharmGUI;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Command to open the Beacon Catalyst GUI.
 */
public class BeaconCharmCommand implements CommandExecutor {
    private final BeaconCharmGUI gui;

    public BeaconCharmCommand(MinecraftNew plugin, BeaconCharmGUI gui) {
        this.gui = gui;
        plugin.getCommand("beaconcharm").setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players may use this command.");
            return true;
        }
        int tier = 1;
        if (args.length > 0) {
            try { tier = Integer.parseInt(args[0]); } catch (NumberFormatException ignored) {}
        }
        if (tier < 0) tier = 0; if (tier > 6) tier = 6;
        gui.open(player, tier);
        return true;
    }
}
