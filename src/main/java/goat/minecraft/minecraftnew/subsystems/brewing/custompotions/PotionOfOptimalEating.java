package goat.minecraft.minecraftnew.subsystems.brewing.custompotions;

import goat.minecraft.minecraftnew.MinecraftNew;
import goat.minecraft.minecraftnew.subsystems.brewing.PotionManager;
import goat.minecraft.minecraftnew.utils.devtools.XPManager;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;

public class PotionOfOptimalEating implements Listener {
    @EventHandler
    public void onPotionDrink(PlayerItemConsumeEvent event) {
        ItemStack item = event.getItem();
        if (item != null && item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
            String displayName = ChatColor.stripColor(item.getItemMeta().getDisplayName());
            if (displayName.equals("Potion of Optimal Eating")) {
                Player player = event.getPlayer();
                XPManager xpManager = new XPManager(MinecraftNew.getInstance());
                int duration = 60 * 3;
                PotionManager.addCustomPotionEffect("Potion of Optimal Eating", player, duration);
                player.sendMessage(ChatColor.GREEN + "Potion of Optimal Eating active for " + duration + " seconds!");
                xpManager.addXP(player, "Brewing", 100);
            }
        }
    }
}
