package goat.minecraft.minecraftnew.subsystems.fishing;

import goat.minecraft.minecraftnew.MinecraftNew;
import goat.minecraft.minecraftnew.subsystems.brewing.PotionManager;
import goat.minecraft.minecraftnew.subsystems.enchanting.CustomEnchantmentManager;
import goat.minecraft.minecraftnew.subsystems.pets.PetManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class TreasureChanceCommand implements CommandExecutor {
    private final MinecraftNew plugin;

    public TreasureChanceCommand(MinecraftNew plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command.");
            return true;
        }

        double chance = calculateTreasureChance(player);
        player.sendMessage(ChatColor.GOLD + "Treasure Chance: " + ChatColor.YELLOW + String.format("%.2f", chance * 100) + "%");
        return true;
    }

    private double calculateTreasureChance(Player player) {
        double treasureChance = 0.05; // Base 5%
        ItemStack rod = player.getInventory().getItemInMainHand();
        int upgradeLevel = FishingUpgradeSystem.getUpgradeLevel(rod, FishingUpgradeSystem.UpgradeType.TREASURE_HUNTER);
        treasureChance += upgradeLevel / 100.0;

        PetManager petManager = PetManager.getInstance(plugin);
        PetManager.Pet activePet = petManager.getActivePet(player);
        if (activePet != null && activePet.hasPerk(PetManager.PetPerk.TREASURE_HUNTER)) {
            treasureChance += activePet.getLevel() * 0.0010;
        }

        if (PotionManager.isActive("Potion of Liquid Luck", player)) {
            treasureChance += 0.2;
        }

        int piracyLevel = CustomEnchantmentManager.getEnchantmentLevel(player.getInventory().getItemInMainHand(), "Piracy");
        treasureChance += piracyLevel / 100.0;

        return treasureChance;
    }
}
