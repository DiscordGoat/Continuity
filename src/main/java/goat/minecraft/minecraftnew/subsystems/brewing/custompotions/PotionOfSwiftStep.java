package goat.minecraft.minecraftnew.subsystems.brewing.custompotions;

import goat.minecraft.minecraftnew.MinecraftNew;
import goat.minecraft.minecraftnew.subsystems.brewing.PotionManager;
import goat.minecraft.minecraftnew.utils.devtools.XPManager;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PotionOfSwiftStep implements Listener {

    // Track whether each player was holding an item in their main hand on the previous move event.
    // true = holding an item; false = empty.
    private static final Map<UUID, Boolean> wasHoldingItem = new HashMap<>();

    /**
     * When a player drinks a Potion of Swift Step, apply the custom effect for a duration based on Brewing level.
     */
    @EventHandler
    public void onPotionDrink(PlayerItemConsumeEvent event) {
        ItemStack item = event.getItem();
        if (item != null && item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
            String displayName = ChatColor.stripColor(item.getItemMeta().getDisplayName());
            if (displayName.equals("Potion of Swift Step")) {
                Player player = event.getPlayer();
                XPManager xpManager = new XPManager(MinecraftNew.getInstance());
                int brewingLevel = xpManager.getPlayerLevel(player, "Brewing");
                int duration = (60 * 3) + (brewingLevel * 10); // Custom scaling
                PotionManager.addCustomPotionEffect("Potion of Swift Step", player, duration);
                player.sendMessage(ChatColor.AQUA + "Potion of Swift Step activated for " + duration + " seconds!");
                xpManager.addXP(player, "Brewing", 100);
            }
        }
    }

    /**
     * When the effect is active and the player is sprinting with an empty main hand,
     * add a subtle forward boost to simulate dashing.
     * Also, play a leather-equip sound if the player transitions from holding an item to an empty hand.
     */
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        boolean effectActive = PotionManager.isActive("Potion of Swift Step", player);
        boolean sprinting = player.isSprinting();
        ItemStack mainHand = player.getInventory().getItemInMainHand();
        boolean handEmpty = (mainHand == null || mainHand.getType() == Material.AIR);
        boolean shouldDash = effectActive && sprinting && handEmpty;

        // Determine current holding state: true if player is holding something.
        boolean currentlyHolding = !handEmpty;
        // Default to true (assuming they start with an item in hand).
        boolean previouslyHolding = wasHoldingItem.getOrDefault(uuid, true);

        // Only play the sound when the player transitions from holding an item to an empty hand.
        if (shouldDash && !currentlyHolding && previouslyHolding) {
            player.getWorld().playSound(player.getLocation(), Sound.ITEM_ARMOR_EQUIP_LEATHER, 0.5f, 1.0f);
        }

        // Update the player's holding state.
        wasHoldingItem.put(uuid, currentlyHolding);

        // If conditions are met, apply the forward dash boost.
        if (shouldDash && player.isOnGround()) {
            Vector forward = player.getLocation().getDirection().normalize();
            double multiplier = 0.2;  // Subtle boost multiplier.
            Vector boost = forward.multiply(multiplier);
            player.setVelocity(player.getVelocity().add(boost));
        }
    }
}
