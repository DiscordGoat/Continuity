package goat.minecraft.minecraftnew.subsystems.brewing.custompotions;

import goat.minecraft.minecraftnew.MinecraftNew;
import goat.minecraft.minecraftnew.subsystems.brewing.PotionManager;
import goat.minecraft.minecraftnew.utils.devtools.XPManager;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerRiptideEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

public class PotionOfRiptide implements Listener {

    @EventHandler
    public void onPotionDrink(PlayerItemConsumeEvent event) {
        ItemStack item = event.getItem();
        if (item != null && item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
            String displayName = ChatColor.stripColor(item.getItemMeta().getDisplayName());
            if (displayName.equals("Potion of Riptide")) {
                Player player = event.getPlayer();
                XPManager xpManager = new XPManager(MinecraftNew.getInstance());
                int brewingLevel = xpManager.getPlayerLevel(player, "Brewing");
                int duration = (60 * 30) + (brewingLevel * 10);
                PotionManager.addCustomPotionEffect("Potion of Riptide", player, duration);
                player.sendMessage(ChatColor.AQUA + "Potion of Riptide activated for " + duration + " seconds!");
                xpManager.addXP(player, "Brewing", 100);
            }
        }
    }

    @EventHandler
    public void onRiptide(PlayerRiptideEvent event) {
        Player player = event.getPlayer();
        if (PotionManager.isActive("Potion of Riptide", player)) {
            Vector forward = player.getLocation().getDirection().normalize();
            Vector boost = forward.multiply(0.5);
            player.setVelocity(player.getVelocity().add(boost));
        }
    }
}
