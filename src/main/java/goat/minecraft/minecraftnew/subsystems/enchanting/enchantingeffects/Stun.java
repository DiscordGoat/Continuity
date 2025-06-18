package goat.minecraft.minecraftnew.subsystems.enchanting.enchantingeffects;

import goat.minecraft.minecraftnew.MinecraftNew;
import goat.minecraft.minecraftnew.subsystems.enchanting.CustomEnchantmentManager;
import org.bukkit.ChatColor;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

public class Stun implements Listener {

    @EventHandler
    public void onEntityHitByArrow(EntityDamageByEntityEvent event) {
        // Check if the damage was caused by an arrow
        if (!(event.getDamager() instanceof Arrow)) return;

        Arrow arrow = (Arrow) event.getDamager();
        if (!(arrow.getShooter() instanceof Player)) return;

        Player shooter = (Player) arrow.getShooter();
        ItemStack bow = shooter.getInventory().getItemInMainHand();

        // Check if the bow has the "Stun" enchantment
        if (CustomEnchantmentManager.hasEnchantment(bow, "Stun")) {
            Entity hitEntity = event.getEntity();
            if (hitEntity instanceof LivingEntity) {
                LivingEntity target = (LivingEntity) hitEntity;

                // Always apply stun for 10 seconds
                target.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 10 * 20, 255));
                if (target instanceof Creature creature) {
                    new BukkitRunnable() {
                        int ticks = 10 * 20;

                        @Override
                        public void run() {
                            if (ticks-- <= 0 || creature.isDead() || !creature.isValid()) {
                                creature.setAI(true);
                                this.cancel();
                                return;
                            }
                            creature.setTarget(null);
                        }
                    }.runTaskTimer(MinecraftNew.getInstance(), 0L, 1L);
                    creature.setAI(false);
                }

                // Notify shooter about the successful stun
                shooter.sendMessage(ChatColor.AQUA + "Stun effect applied to " + target.getName() + "!");
                int enchantmentLevel = CustomEnchantmentManager.getEnchantmentLevel(bow, "Stun");
                shooter.getInventory().getItemInMainHand().setDurability((short) (shooter.getInventory().getItemInMainHand().getDurability() +(1*enchantmentLevel)));
            }
        }
    }
}
