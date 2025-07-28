package goat.minecraft.minecraftnew.subsystems.forestry;

import goat.minecraft.minecraftnew.MinecraftNew;
import goat.minecraft.minecraftnew.other.armorsets.NaturesWrathSetBonus;
import goat.minecraft.minecraftnew.other.beacon.Catalyst;
import goat.minecraft.minecraftnew.other.beacon.CatalystManager;
import goat.minecraft.minecraftnew.other.beacon.CatalystType;
import goat.minecraft.minecraftnew.subsystems.pets.PetManager;
import goat.minecraft.minecraftnew.utils.devtools.XPManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class SpiritChanceCommand implements CommandExecutor {
    private final MinecraftNew plugin;
    private final XPManager xpManager;

    public SpiritChanceCommand(MinecraftNew plugin, XPManager xpManager) {
        this.plugin = plugin;
        this.xpManager = xpManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command.");
            return true;
        }

        sendSpiritChanceBreakdown(player);
        return true;
    }

    private void sendSpiritChanceBreakdown(Player player) {
        double base = 0.01;
        ItemStack axe = player.getInventory().getItemInMainHand();
        double effigyBonus = 0.0;

        PetManager petManager = PetManager.getInstance(plugin);
        PetManager.Pet activePet = petManager.getActivePet(player);
        double petBonus = 0.0;
        if (activePet != null) {
            if (activePet.hasPerk(PetManager.PetPerk.SKEPTICISM)) {
                petBonus += 0.001;
            }
            if (activePet.hasPerk(PetManager.PetPerk.CHALLENGE)) {
                petBonus += 0.002;
            }
        }
        double natureBonus = NaturesWrathSetBonus.getSpiritChanceBonus(player);

        CatalystManager catalystManager = CatalystManager.getInstance();
        double catalystBonus = 0.0;
        if (catalystManager != null && catalystManager.isNearCatalyst(player.getLocation(), CatalystType.INSANITY)) {
            Catalyst nearest = catalystManager.findNearestCatalyst(player.getLocation(), CatalystType.INSANITY);
            if (nearest != null) {
                int tier = catalystManager.getCatalystTier(nearest);
                catalystBonus = 0.0005 + (tier * 0.0005);
            }
        }

        double total = base + effigyBonus + petBonus + natureBonus + catalystBonus;

        player.sendMessage(ChatColor.DARK_AQUA + "Spirit Chance Breakdown:");
        player.sendMessage(ChatColor.AQUA + "Base SC: " + ChatColor.YELLOW + "1%");
        player.sendMessage(ChatColor.AQUA + "SC from Effigy Yield Upgrades: " + ChatColor.YELLOW + String.format("%.2f", effigyBonus * 100) + "%");
        player.sendMessage(ChatColor.AQUA + "SC from Pet Perks: " + ChatColor.YELLOW + String.format("%.2f", petBonus * 100) + "%");
        player.sendMessage(ChatColor.AQUA + "SC from Nature's Wrath: " + ChatColor.YELLOW + String.format("%.2f", natureBonus * 100) + "%");
        player.sendMessage(ChatColor.AQUA + "SC from Insanity Catalyst: " + ChatColor.YELLOW + String.format("%.2f", catalystBonus * 100) + "%");
        player.sendMessage(ChatColor.DARK_AQUA + "Total Spirit Chance: " + ChatColor.YELLOW + String.format("%.2f", total * 100) + "%");
    }
}
