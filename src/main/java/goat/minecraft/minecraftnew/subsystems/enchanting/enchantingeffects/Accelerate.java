package goat.minecraft.minecraftnew.subsystems.enchanting.enchantingeffects;

import goat.minecraft.minecraftnew.MinecraftNew;
import goat.minecraft.minecraftnew.subsystems.combat.DeteriorationDamageHandler;
import goat.minecraft.minecraftnew.subsystems.enchanting.CustomEnchantmentManager;
import goat.minecraft.minecraftnew.utils.devtools.PlayerMeritManager;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;

/**
 * Accelerate custom enchantment - applies Deterioration stacks on hit.
 */
public class Accelerate implements Listener {

    private final PlayerMeritManager meritManager = PlayerMeritManager.getInstance(MinecraftNew.getInstance());

    @EventHandler
    public void onHit(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player player)) return;
        if (!(event.getEntity() instanceof LivingEntity target)) return;

        ItemStack weapon = player.getInventory().getItemInMainHand();
        if (!CustomEnchantmentManager.hasEnchantment(weapon, "Accelerate")) return;

        int level = CustomEnchantmentManager.getEnchantmentLevel(weapon, "Accelerate");
        int stacks = level * 5;
        if (meritManager.hasPerk(player.getUniqueId(), "Decay Mastery")) {
            stacks *= 2;
        }
        DeteriorationDamageHandler.getInstance().addDeterioration(target, stacks);
    }
}
