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

/**
 * Potion of Solar Fury - doubles fire level gains on monsters.
 */
public class PotionOfSolarFury implements Listener {

    @EventHandler
    public void onPotionDrink(PlayerItemConsumeEvent event) {
        ItemStack item = event.getItem();
        if (item != null && item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
            String displayName = ChatColor.stripColor(item.getItemMeta().getDisplayName());
            XPManager xpManager = new XPManager(MinecraftNew.getInstance());
            int duration = (60 * 3);
            if (displayName.equals("Potion of Solar Fury")) {
                Player player = event.getPlayer();
                PotionManager.addCustomPotionEffect("Potion of Solar Fury", player, duration);
                player.sendMessage(ChatColor.RED + "Potion of Solar Fury effect activated for " + duration + " seconds!");
                xpManager.addXP(player, "Brewing", 100);
            }
        }
    }
}
