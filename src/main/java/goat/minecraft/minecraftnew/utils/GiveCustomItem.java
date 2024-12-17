package goat.minecraft.minecraftnew.utils;

import goat.minecraft.minecraftnew.MinecraftNew;
import goat.minecraft.minecraftnew.subsystems.pets.PetManager;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.List;


public class GiveCustomItem implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Check if the command sender is a player
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command.");
            return true;
        }

        Player player = (Player) sender;
        PetManager petManager = PetManager.getInstance(MinecraftNew.getInstance());
        ItemStack customItem = ItemRegistry.getAutoComposter();
        ItemStack customItem1 = ItemRegistry.getBeetrootSeeder();
        ItemStack customItem2 = ItemRegistry.getCarrotSeeder();
        ItemStack customItem3 = ItemRegistry.getPotatoSeeder();
        player.getInventory().addItem(customItem1);
        player.getInventory().addItem(customItem2);
        player.getInventory().addItem(customItem3);
        player.getInventory().addItem(customItem);

        player.sendMessage(ChatColor.GREEN + "You have received all custom items!");

        return true;
    }
}
