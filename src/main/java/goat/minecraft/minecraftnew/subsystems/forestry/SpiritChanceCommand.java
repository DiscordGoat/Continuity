package goat.minecraft.minecraftnew.subsystems.forestry;

import goat.minecraft.minecraftnew.MinecraftNew;
import goat.minecraft.minecraftnew.other.additionalfunctionality.BlessingUtils;
import goat.minecraft.minecraftnew.subsystems.armorsets.NaturesWrathSetBonus;
import goat.minecraft.minecraftnew.subsystems.beacon.Catalyst;
import goat.minecraft.minecraftnew.subsystems.beacon.CatalystManager;
import goat.minecraft.minecraftnew.subsystems.beacon.CatalystType;
import goat.minecraft.minecraftnew.subsystems.pets.PetManager;
import goat.minecraft.minecraftnew.subsystems.forestry.EffigyUpgradeSystem;
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

        double chance = calculateSpiritChance(player);
        player.sendMessage(ChatColor.DARK_AQUA + "Spirit Chance: " + ChatColor.YELLOW + String.format("%.2f", chance * 100) + "%");
        return true;
    }

    private double calculateSpiritChance(Player player) {
        double spiritChance = 0.02;
        ItemStack axe = player.getInventory().getItemInMainHand();
        int effigyYield = EffigyUpgradeSystem.getUpgradeLevel(axe, EffigyUpgradeSystem.UpgradeType.EFFIGY_YIELD);
        spiritChance += effigyYield * 0.005;

        PetManager petManager = PetManager.getInstance(plugin);
        PetManager.Pet activePet = petManager.getActivePet(player);
        if (activePet != null) {
            if (activePet.hasPerk(PetManager.PetPerk.SKEPTICISM)) {
                spiritChance += 0.02;
            }
            if (activePet.hasPerk(PetManager.PetPerk.CHALLENGE)) {
                spiritChance += 0.05;
            }
        }

        spiritChance += NaturesWrathSetBonus.getSpiritChanceBonus(player);

        CatalystManager catalystManager = CatalystManager.getInstance();
        if (catalystManager != null && catalystManager.isNearCatalyst(player.getLocation(), CatalystType.INSANITY)) {
            Catalyst nearest = catalystManager.findNearestCatalyst(player.getLocation(), CatalystType.INSANITY);
            if (nearest != null) {
                int tier = catalystManager.getCatalystTier(nearest);
                double bonus = 0.05 + (tier * 0.01);
                spiritChance += bonus;
            }
        }

        return spiritChance;
    }
}
