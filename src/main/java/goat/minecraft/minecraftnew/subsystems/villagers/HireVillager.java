package goat.minecraft.minecraftnew.subsystems.villagers;

import goat.minecraft.minecraftnew.utils.devtools.ItemRegistry;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public class HireVillager implements Listener {

    @EventHandler
    public void onPlayerShiftRightClickVillager(PlayerInteractEntityEvent event) {
        // Check if the clicked entity is a Villager
        if (!(event.getRightClicked() instanceof Villager)) {
            return;
        }

        Villager villager = (Villager) event.getRightClicked();
        Player player = event.getPlayer();

        // Check if the player is sneaking (shift-right-clicking)
        if (!player.isSneaking()) {
            event.setCancelled(true);
            return;
        }

        // Check if the player has at least 4 emeralds in their inventory
        ItemStack emeraldStack = new ItemStack(Material.EMERALD, 4);
        if (player.getInventory().getItemInMainHand().getType().equals(Material.EMERALD) && player.getInventory().getItemInMainHand().getAmount() > 3) {
            ItemStack customItem = ItemRegistry.getHireVillager();
            villager.getWorld().dropItemNaturally(villager.getLocation(), customItem);

            // Play cure villager sound effect
            villager.getWorld().playSound(villager.getLocation(), Sound.BLOCK_BELL_RESONATE, 1.0f, 1.0f);

            // Spawn happy villager particles
            villager.getWorld().spawnParticle(Particle.VILLAGER_HAPPY, villager.getLocation().add(0, 1, 0), 30, 0.5, 0.5, 0.5, 0.2);

            villager.remove();
        }
        else {
            player.sendMessage(ChatColor.RED + "You need at least 4 emeralds to hire this villager.");
        }
        // Remove 4 emeralds from the player's inventory
        player.getInventory().getItemInMainHand().setAmount(player.getInventory().getItemInMainHand().getAmount() - 4);

        // Drop the custom item at the villager's location


        // Remove the villager


        closeInventoryAfterDelay(player);
    }
    public void closeInventoryAfterDelay(Player player) {
        Bukkit.getScheduler().runTaskLater(JavaPlugin.getProvidingPlugin(getClass()), () -> {
            player.closeInventory();
        }, 6L); // 6 ticks = 0.3 seconds (1 second = 20 ticks)
    }

}
