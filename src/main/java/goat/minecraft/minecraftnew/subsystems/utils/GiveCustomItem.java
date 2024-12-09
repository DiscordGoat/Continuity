package goat.minecraft.minecraftnew.subsystems.utils;

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

        // Add all custom items to the player's inventory
        player.getInventory().addItem(perfectDiamond());
        player.getInventory().addItem(infernalLooting());
        player.getInventory().addItem(infernalUnbreaking());
        player.getInventory().addItem(infernalDepthStrider());
        player.getInventory().addItem(infernalBaneOfAnthropods());
        player.getInventory().addItem(infernalEfficiency());
        player.getInventory().addItem(infernalFireAspect());
        player.getInventory().addItem(infernalSharpness());
        player.getInventory().addItem(infernalSmite());
        player.getInventory().addItem(infernalLure());

        player.sendMessage(ChatColor.GREEN + "You have received all custom items!");

        return true;
    }

    // Custom item methods
    public ItemStack perfectDiamond() {
        return CustomItemManager.createCustomItem(
                Material.DIAMOND,
                ChatColor.BLUE + "Perfect Diamond",
                List.of(ChatColor.GRAY + "A rare mineral.",
                        "Apply it to a pickaxe to unlock the secrets of Fortune.",
                        "Smithing Item"),
                1,
                false,
                true
        );
    }

    public ItemStack infernalLooting() {
        return CustomItemManager.createCustomItem(Material.GOLD_BLOCK, ChatColor.GOLD + "Midas Gold", Arrays.asList(
                ChatColor.GRAY + "A hellish material that inspires replication.",
                ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Apply it to equipment to unlock the secrets of Looting V.",
                ChatColor.AQUA + "Mythical Enchantment"
        ), 1, false, true);
    }

    public ItemStack infernalUnbreaking() {
        return CustomItemManager.createCustomItem(Material.BEDROCK, ChatColor.GOLD + "Unbreakable", Arrays.asList(
                ChatColor.GRAY + "A hellish material that inspires Unbreakability.",
                ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Apply it to equipment to unlock the secrets of Unbreaking V.",
                ChatColor.AQUA + "Mythical Enchantment"
        ), 1, false, true);
    }

    public ItemStack infernalDepthStrider() {
        return CustomItemManager.createCustomItem(Material.GOLDEN_BOOTS, ChatColor.GOLD + "LavaStride", Arrays.asList(
                ChatColor.GRAY + "A hellish material that inspires the ocean's current.",
                ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Apply it to equipment to unlock the secrets of Depth Strider V.",
                ChatColor.AQUA + "Mythical Enchantment"
        ), 1, false, true);
    }

    public ItemStack infernalBaneOfAnthropods() {
        return CustomItemManager.createCustomItem(Material.COBWEB, ChatColor.GOLD + "Extinction", Arrays.asList(
                ChatColor.GRAY + "A hellish material that inspires lethal options against Anthropods.",
                ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Apply it to equipment to unlock the secrets of Bane of Anthropods VII.",
                ChatColor.AQUA + "Mythical Enchantment"
        ), 1, false, true);
    }

    public ItemStack infernalEfficiency() {
        return CustomItemManager.createCustomItem(Material.OBSIDIAN, ChatColor.GOLD + "Weak Spot", Arrays.asList(
                ChatColor.GRAY + "A hellish material that inspires lethal options against Blocks.",
                ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Apply it to equipment to unlock the secrets of Efficiency VI.",
                ChatColor.AQUA + "Mythical Enchantment"
        ), 1, false, true);
    }

    public ItemStack infernalFireAspect() {
        return CustomItemManager.createCustomItem(Material.LAVA_BUCKET, ChatColor.GOLD + "Hellfire", Arrays.asList(
                ChatColor.GRAY + "A hellish material that inspires Fire.",
                ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Apply it to equipment to unlock the secrets of Fire Aspect IV.",
                ChatColor.AQUA + "Mythical Enchantment"
        ), 1, false, true);
    }

    public ItemStack infernalSharpness() {
        return CustomItemManager.createCustomItem(Material.IRON_SWORD, ChatColor.GOLD + "Shrapnel", Arrays.asList(
                ChatColor.GRAY + "A hellish material that inspires lethal options against mobs.",
                ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Apply it to equipment to unlock the secrets of Sharpness VII.",
                ChatColor.AQUA + "Mythical Enchantment"
        ), 1, false, true);
    }

    public ItemStack infernalSmite() {
        return CustomItemManager.createCustomItem(Material.WITHER_SKELETON_SKULL, ChatColor.GOLD + "Cure", Arrays.asList(
                ChatColor.GRAY + "A hellish material that inspires lethal options against Undead.",
                ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Apply it to equipment to unlock the secrets of Smite VII.",
                ChatColor.AQUA + "Mythical Enchantment"
        ), 1, false, true);
    }

    public ItemStack infernalLure() {
        return CustomItemManager.createCustomItem(Material.HEART_OF_THE_SEA, ChatColor.GOLD + "Howl", Arrays.asList(
                ChatColor.GRAY + "A hellish material that inspires fish hunger.",
                ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Apply it to equipment to unlock the secrets of Lure V.",
                ChatColor.AQUA + "Mythical Enchantment"
        ), 1, false, true);
    }
}
