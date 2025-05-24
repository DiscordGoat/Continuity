// File: goat/minecraft/minecraftnew/utils/developercommands/GetCulinaryRecipeCommand.java
package goat.minecraft.minecraftnew.utils.developercommands;

import goat.minecraft.minecraftnew.subsystems.culinary.CulinarySubsystem;
import org.bukkit.ChatColor;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public class GetCulinaryRecipeCommand implements CommandExecutor {
    private final JavaPlugin plugin;

    public GetCulinaryRecipeCommand(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players may use this command.");
            return true;
        }
        if (args.length < 1) {
            sender.sendMessage(ChatColor.YELLOW + "Usage: /getculinaryrecipe <recipe_name>");
            return true;
        }

        // join args and replace underscores with spaces
        String recipeName = String.join(" ", args).replace("_", " ");

        // use the CulinarySubsystem's lookup
        ItemStack recipePaper = CulinarySubsystem
                .getInstance(plugin)
                .getRecipeItemByName(recipeName);

        if (recipePaper == null) {
            sender.sendMessage(ChatColor.RED + "Recipe not found: " + recipeName);
            return true;
        }

        Player player = (Player) sender;
        player.getInventory().addItem(recipePaper)
                .values()
                .forEach(item -> player.getWorld().dropItemNaturally(player.getLocation(), item));

        sender.sendMessage(ChatColor.GREEN + "Gave you the recipe for: "
                + ChatColor.GOLD + recipeName);
        return true;
    }
}
