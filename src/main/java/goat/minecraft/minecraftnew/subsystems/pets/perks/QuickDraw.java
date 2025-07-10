package goat.minecraft.minecraftnew.subsystems.pets.perks;

import goat.minecraft.minecraftnew.subsystems.pets.PetManager;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

public class QuickDraw implements Listener {

    private final PetManager petManager;

    public QuickDraw(JavaPlugin plugin) {
        this.petManager = PetManager.getInstance(plugin);
    }

    @EventHandler
    public void onPlayerLeftClickBow(PlayerInteractEvent event) {
        // Ensure the interaction is a left-click
        if (!event.getAction().toString().contains("LEFT_CLICK")) {
            return;
        }

        Player player = event.getPlayer();
        ItemStack itemInHand = player.getInventory().getItemInMainHand();

        // Ensure the player is holding a bow
        if (itemInHand == null || itemInHand.getType() != Material.BOW) {
            return;
        }

        // Ensure the bow has at least 25% durability
        if (itemInHand.getItemMeta() instanceof Damageable damageable) {
            int maxDurability = itemInHand.getType().getMaxDurability();
            int currentDurability = maxDurability - damageable.getDamage();
            if (currentDurability < maxDurability * 0.25) {
                player.sendMessage(ChatColor.RED + "Your bow is too damaged to use Quick Draw!");
                return;
            }
        }
        
        // Get the player's active pet
        PetManager.Pet activePet = petManager.getActivePet(player);

        // Check if the player has the Quick Draw perk or unique trait
        if (activePet == null || !(activePet.hasPerk(PetManager.PetPerk.QUICK_DRAW)
                || activePet.hasUniqueTraitPerk(PetManager.PetPerk.QUICK_DRAW))) {
            return;
        }

        // Ensure the player has arrows in their inventory
        if (!player.getInventory().contains(Material.ARROW)) {
            player.sendMessage(ChatColor.RED + "You need arrows to use Quick Draw!");
            return;
        }

        // Remove one arrow from the player's inventory
        ItemStack arrowStack = player.getInventory().getItem(player.getInventory().first(Material.ARROW));
        if (arrowStack != null) {
            arrowStack.setAmount(arrowStack.getAmount() - 1);
        }

        // Damage the bow by 1 durability
        if (itemInHand.getItemMeta() instanceof Damageable damageable) {
            int unbreakingLevel = itemInHand.getEnchantmentLevel(Enchantment.UNBREAKING);
            double chance = 1 - (0.15 * unbreakingLevel);
            if (Math.random() > chance) {
                damageable.setDamage(damageable.getDamage() + 1);
                itemInHand.setItemMeta((ItemMeta) damageable);
            }
        }

        // Fire an arrow from the player's location
        Arrow arrow = player.getWorld().spawnArrow(
                player.getEyeLocation().add(player.getLocation().getDirection().multiply(1.5)), // Slightly in front of the player
                player.getLocation().getDirection(), // Arrow direction
                2.0f, // Speed
                0.0f // Spread
        );
        arrow.setShooter(player);
        arrow.setCritical(true); // Make the arrow critical
        arrow.setPickupStatus(Arrow.PickupStatus.DISALLOWED); // Prevent pickup

        // Play bow firing sound
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ARROW_SHOOT, 1.0f, 1.0f);

        // Notify the player
        player.sendMessage(ChatColor.GREEN + "Quick Draw: You fired an arrow instantly!");
    }
}
