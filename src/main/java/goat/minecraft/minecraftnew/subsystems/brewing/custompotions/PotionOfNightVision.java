package goat.minecraft.minecraftnew.subsystems.brewing.custompotions;

import goat.minecraft.minecraftnew.MinecraftNew;
import goat.minecraft.minecraftnew.subsystems.brewing.PotionManager;
import goat.minecraft.minecraftnew.utils.devtools.XPManager;
import org.bukkit.ChatColor;
import org.bukkit.PotionEffect;
import org.bukkit.PotionEffectType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;

/**
 * Potion of Night Vision - grants Night Vision when moving.
 */
public class PotionOfNightVision implements Listener {

    @EventHandler
    public void onPotionDrink(PlayerItemConsumeEvent event) {
        ItemStack item = event.getItem();
        if (item != null && item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
            String displayName = ChatColor.stripColor(item.getItemMeta().getDisplayName());
            if (displayName.equals("Potion of Night Vision")) {
                Player player = event.getPlayer();
                XPManager xpManager = new XPManager(MinecraftNew.getInstance());
                int brewingLevel = xpManager.getPlayerLevel(player, "Brewing");
                int duration = (60 * 30) + (brewingLevel * 10);
                PotionManager.addCustomPotionEffect("Potion of Night Vision", player, duration);
                player.sendMessage(ChatColor.AQUA + "Potion of Night Vision activated for " + duration + " seconds!");
                xpManager.addXP(player, "Brewing", 100);
            }
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (PotionManager.isActive("Potion of Night Vision", player)) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 15 * 20, 0, false, false));
        }
    }
}
