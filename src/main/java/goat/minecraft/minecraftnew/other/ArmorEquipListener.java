package goat.minecraft.minecraftnew.other;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;

public class ArmorEquipListener implements Listener {

    @EventHandler
    public void onPlayerInteractWithArmorStand(PlayerInteractAtEntityEvent event) {
        if (!(event.getRightClicked() instanceof ArmorStand armorStand)) {
            return; // Exit if the interacted entity is not an armor stand
        }
        if (!armorStand.isVisible()) {
            return;
        }

        Player player = event.getPlayer();
        ItemStack[] playerArmor = player.getInventory().getArmorContents();
        ItemStack[] standArmor = armorStand.getEquipment().getArmorContents();

        boolean playerHasArmor = Arrays.stream(playerArmor).anyMatch(item -> item != null && item.getType() != Material.AIR);
        boolean armorStandHasArmor = Arrays.stream(standArmor).anyMatch(item -> item != null && item.getType() != Material.AIR);

        // Case 1: Player has armor and the armor stand is empty
        if (playerHasArmor && !armorStandHasArmor) {
            // Clone player armor and equip to the armor stand
            armorStand.getEquipment().setArmorContents(Arrays.stream(playerArmor)
                    .map(item -> item != null ? item.clone() : null)
                    .toArray(ItemStack[]::new));

            // Remove armor from player
            player.getInventory().setArmorContents(new ItemStack[4]);

            event.setCancelled(true);
            player.playSound(player.getLocation(), Sound.ITEM_ARMOR_EQUIP_GENERIC, 1.0f, 1.0f);
        }
        // Case 2: Player has no armor equipped and the armor stand has armor
        else if (!playerHasArmor && armorStandHasArmor) {
            // Clone armor from the armor stand and equip it to the player
            player.getInventory().setArmorContents(Arrays.stream(standArmor)
                    .map(item -> item != null ? item.clone() : null)
                    .toArray(ItemStack[]::new));

            // Clear armor on the armor stand
            armorStand.getEquipment().setArmorContents(new ItemStack[4]);

            event.setCancelled(true);
            player.playSound(player.getLocation(), Sound.ITEM_ARMOR_EQUIP_GENERIC, 1.0f, 1.0f);
        }
    }
}
