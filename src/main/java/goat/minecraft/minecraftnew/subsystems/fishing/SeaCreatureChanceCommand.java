package goat.minecraft.minecraftnew.subsystems.fishing;

import goat.minecraft.minecraftnew.MinecraftNew;
import goat.minecraft.minecraftnew.other.additionalfunctionality.BlessingUtils;
import goat.minecraft.minecraftnew.subsystems.beacon.Catalyst;
import goat.minecraft.minecraftnew.subsystems.beacon.CatalystManager;
import goat.minecraft.minecraftnew.subsystems.beacon.CatalystType;
import goat.minecraft.minecraftnew.subsystems.brewing.PotionManager;
import goat.minecraft.minecraftnew.subsystems.enchanting.CustomEnchantmentManager;
import goat.minecraft.minecraftnew.subsystems.pets.PetManager;
import goat.minecraft.minecraftnew.utils.devtools.PlayerMeritManager;
import goat.minecraft.minecraftnew.utils.devtools.XPManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class SeaCreatureChanceCommand implements CommandExecutor {
    private final MinecraftNew plugin;
    private final XPManager xpManager;

    public SeaCreatureChanceCommand(MinecraftNew plugin, XPManager xpManager) {
        this.plugin = plugin;
        this.xpManager = xpManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command.");
            return true;
        }

        double chance = calculateSeaCreatureChance(player) ;
        player.sendMessage(ChatColor.AQUA + "Sea Creature Chance: " + ChatColor.YELLOW + String.format("%.2f", chance) + "%");
        return true;
    }

    private double calculateSeaCreatureChance(Player player) {
        double seaCreatureChance = 0.0;
        int fishingLevel = xpManager.getPlayerLevel(player, "Fishing");
        seaCreatureChance += fishingLevel / 4.0;

        int callOfTheVoidLevel = CustomEnchantmentManager.getEnchantmentLevel(player.getInventory().getItemInMainHand(), "Call of the Void");
        seaCreatureChance += callOfTheVoidLevel;

        if (PotionManager.isActive("Potion of Fountains", player)) {
            seaCreatureChance += 10;
        }

        CatalystManager catalystManager = CatalystManager.getInstance();
        if (catalystManager != null && catalystManager.isNearCatalyst(player.getLocation(), CatalystType.DEPTH)) {
            Catalyst nearest = catalystManager.findNearestCatalyst(player.getLocation(), CatalystType.DEPTH);
            if (nearest != null) {
                int tier = catalystManager.getCatalystTier(nearest);
                double bonus = 0.05 + (tier * 0.01);
                seaCreatureChance += bonus;
            }
        }

        if (isReforgedForSeaCreatures(player.getInventory().getItemInMainHand())) {
            seaCreatureChance += 5;
        }

        PlayerMeritManager meritManager = PlayerMeritManager.getInstance(plugin);
        if (meritManager.hasPerk(player.getUniqueId(), "Master Angler")) {
            seaCreatureChance += 5;
        }

        if (BlessingUtils.hasFullSetBonus(player, "Fathmic Iron")) {
            seaCreatureChance -= 20;
        }

        ItemStack rod = player.getInventory().getItemInMainHand();
        int sonarLevel = FishingUpgradeSystem.getUpgradeLevel(rod, FishingUpgradeSystem.UpgradeType.SONAR);
        seaCreatureChance += sonarLevel;

        PetManager petManager = PetManager.getInstance(plugin);
        PetManager.Pet activePet = petManager.getActivePet(player);
        if (activePet != null) {
            if (activePet.hasPerk(PetManager.PetPerk.ANGLER)) {
                seaCreatureChance += 5;
            }
            if (activePet.hasPerk(PetManager.PetPerk.HEART_OF_THE_SEA)) {
                seaCreatureChance += 10;
            }
            if (activePet.hasPerk(PetManager.PetPerk.BUDDY_SYSTEM)) {
                for (Player other : player.getWorld().getPlayers()) {
                    if (!other.equals(player) && other.getLocation().distance(player.getLocation()) <= 20) {
                        seaCreatureChance += 5;
                        break;
                    }
                }
            }
            if (activePet.hasPerk(PetManager.PetPerk.BAIT)) {
                seaCreatureChance += (double) activePet.getLevel() / 10.0;
            }
        }

        return seaCreatureChance;
    }

    private boolean isReforgedForSeaCreatures(ItemStack item) {
        if (item == null) {
            return false;
        }
        ItemMeta meta = item.getItemMeta();
        if (meta != null && meta.hasLore()) {
            for (String lore : meta.getLore()) {
                if (ChatColor.stripColor(lore).equals("Talisman: Sea Creature Chance")) {
                    return true;
                }
            }
        }
        return false;
    }
}
