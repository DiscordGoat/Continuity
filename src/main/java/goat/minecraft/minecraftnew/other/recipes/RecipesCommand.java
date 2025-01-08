package goat.minecraft.minecraftnew.other.recipes;

import goat.minecraft.minecraftnew.other.recipes.RecipeManager;
import net.md_5.bungee.api.chat.*;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

public class RecipesCommand implements CommandExecutor {

    private final RecipeManager recipeManager;

    public RecipesCommand(RecipeManager recipeManager) {
        this.recipeManager = recipeManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Players only.");
            return true;
        }

        Player player = (Player) sender;
        player.sendMessage(ChatColor.GREEN + "Available Recipes:");

        // List each recipe in chat as a clickable link
        for (NamespacedKey key : recipeManager.getCustomRecipes().keySet()) {
            // Display something like "redstone_torch_recipe"
            String clickableName = key.getKey();  // The part after "pluginname:" typically

            // Construct a clickable message
            TextComponent message = new TextComponent(" - " + clickableName);
            message.setColor(net.md_5.bungee.api.ChatColor.YELLOW);

            // When clicked, run /viewrecipe <namespace:key>
            String commandToRun = "/viewrecipe " + key.toString();
            // key.toString() often yields something like "myplugin:redstone_torch_recipe"

            message.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, commandToRun));
            message.setHoverEvent(new HoverEvent(
                    HoverEvent.Action.SHOW_TEXT,
                    new ComponentBuilder("Click to view this recipe").create()
            ));

            player.spigot().sendMessage(message);
        }
        return true;
    }
}
