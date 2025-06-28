package goat.minecraft.minecraftnew.utils.developercommands;

import goat.minecraft.minecraftnew.subsystems.villagers.VillagerTradeManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Developer command to open a villager trade GUI remotely for testing.
 * Usage: /openVillagerTradeMenu <profession> <tier>
 */
public class OpenVillagerTradeMenuCommand implements CommandExecutor, Listener {
    private final JavaPlugin plugin;
    private final VillagerTradeManager tradeManager;

    public OpenVillagerTradeMenuCommand(JavaPlugin plugin, VillagerTradeManager tradeManager) {
        this.plugin = plugin;
        this.tradeManager = tradeManager;
        plugin.getCommand("openvillagertrademenu").setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command.");
            return true;
        }

        if (!sender.hasPermission("continuity.admin")) {
            sender.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
            return true;
        }

        if (args.length != 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /openVillagerTradeMenu <profession> <tier>");
            return true;
        }

        Villager.Profession profession;
        int tier;
        try {
            profession = Villager.Profession.valueOf(args[0].toUpperCase());
        } catch (IllegalArgumentException e) {
            sender.sendMessage(ChatColor.RED + "Invalid profession: " + args[0]);
            return true;
        }

        try {
            tier = Integer.parseInt(args[1]);
            if (tier < 1 || tier > 5) {
                sender.sendMessage(ChatColor.RED + "Tier must be between 1 and 5.");
                return true;
            }
        } catch (NumberFormatException e) {
            sender.sendMessage(ChatColor.RED + "Invalid tier: " + args[1]);
            return true;
        }

        Villager villager = player.getWorld().spawn(player.getLocation(), Villager.class, v -> {
            v.setAI(false);
            v.setSilent(true);
            v.setInvulnerable(true);
            v.setInvisible(true);
            v.setProfession(profession);
            v.setVillagerLevel(tier);
        });
        villager.setMetadata("tempTradeVillager", new FixedMetadataValue(plugin, true));

        tradeManager.setPlayerVillager(player, villager);
        tradeManager.openVillagerTradeGUI(player);

        // Listener to clean up after closing the inventory
        Listener listener = new Listener() {
            @EventHandler
            public void onClose(InventoryCloseEvent event) {
                if (event.getPlayer().equals(player) &&
                        ChatColor.stripColor(event.getView().getTitle()).equals("Villager Trading")) {
                    if (villager.isValid()) {
                        villager.remove();
                    }
                    tradeManager.clearPlayerVillager(player);
                    HandlerList.unregisterAll(this);
                }
            }
        };

        Bukkit.getPluginManager().registerEvents(listener, plugin);
        return true;
    }
}
