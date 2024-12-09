package goat.minecraft.minecraftnew.subsystems.enchanting.enchantingeffects;

import goat.minecraft.minecraftnew.subsystems.enchanting.CustomEnchantmentManager;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;

public class Experience implements Listener {

    @EventHandler
    public void onEntityKill(EntityDeathEvent event) {
        Entity killedEntity = event.getEntity();
        Player killer = event.getEntity().getKiller();

        // Ensure the killer is a player
        if (!(killer instanceof Player)) {
            return;
        }

        ItemStack weapon = killer.getInventory().getItemInMainHand();

        // Check if the weapon has the "Experience" enchantment
        if (!CustomEnchantmentManager.hasEnchantment(weapon, "Experience")) {
            return;
        }

        // Get the level of the "Experience" enchantment
        int enchantmentLevel = CustomEnchantmentManager.getEnchantmentLevel(weapon, "Experience");

        // Ensure the enchantment level is valid
        if (enchantmentLevel < 1) {
            return;
        }

        // Calculate bonus XP orbs to drop, based on enchantment level (max 5 orbs)
        int bonusXP = Math.min(enchantmentLevel, 5);

        // Drop bonus XP at the killed entity's location
        killedEntity.getWorld().spawn(killedEntity.getLocation(), EntityType.EXPERIENCE_ORB.getEntityClass(), entity -> {
            ((org.bukkit.entity.ExperienceOrb) entity).setExperience(bonusXP);
        });

        // Notify the player
        killer.playSound(killedEntity.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 5, 10);
        int experienceLEvel = CustomEnchantmentManager.getEnchantmentLevel(weapon, "Experience");
        killer.getInventory().getItemInMainHand().setDurability((short) (killer.getInventory().getItemInMainHand().getDurability() +(1*experienceLEvel)));

    }
}
