package goat.minecraft.minecraftnew.subsystems.combat;

import goat.minecraft.minecraftnew.MinecraftNew;
import goat.minecraft.minecraftnew.utils.devtools.XPManager;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;

/**
 * Applies effects from Soul Power sword upgrades during combat.
 */
public class SwordUpgradeListener implements Listener {
    private final Random random = new Random();
    private final Map<UUID, Long> furyCooldown = new HashMap<>();
    private final XPManager xpManager;

    public SwordUpgradeListener(MinecraftNew plugin) {
        this.xpManager = new XPManager(plugin);
    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player player)) return;
        ItemStack weapon = player.getInventory().getItemInMainHand();
        if (weapon == null) return;
        if (!(event.getEntity() instanceof LivingEntity target)) return;

        int lethality = SoulUpgradeSystem.getUpgradeLevel(weapon, SoulUpgradeSystem.SwordUpgrade.LETHALITY);
        if (lethality > 0) {
            event.setDamage(event.getDamage() * (1 + lethality * 0.02));
        }
        if (target instanceof Creeper) {
            int diamond = SoulUpgradeSystem.getUpgradeLevel(weapon, SoulUpgradeSystem.SwordUpgrade.DIAMOND_ESSENCE);
            if (diamond > 0) {
                event.setDamage(event.getDamage() * (1 + diamond * 0.10));
            }
        }

        int regenLvl = SoulUpgradeSystem.getUpgradeLevel(weapon, SoulUpgradeSystem.SwordUpgrade.LIFESTEAL_REGEN);
        if (regenLvl > 0 && random.nextDouble() < regenLvl * 0.05) {
            int pot = SoulUpgradeSystem.getUpgradeLevel(weapon, SoulUpgradeSystem.SwordUpgrade.LIFESTEAL_POTENCY);
            int dur = SoulUpgradeSystem.getUpgradeLevel(weapon, SoulUpgradeSystem.SwordUpgrade.LIFESTEAL_DURATION);
            int ticks = (5 + dur * 5) * 20;
            player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, ticks, pot));
        }

        if (SoulUpgradeSystem.getUpgradeLevel(weapon, SoulUpgradeSystem.SwordUpgrade.FEED) > 0 && target instanceof Monster) {
            if (random.nextDouble() < 0.15) {
                player.setFoodLevel(Math.min(20, player.getFoodLevel() + 1));
                player.setSaturation(Math.min(20f, player.getSaturation() + 2f));
                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_BURP, 1f, 1f);
            }
        }

        if (SoulUpgradeSystem.getUpgradeLevel(weapon, SoulUpgradeSystem.SwordUpgrade.FURY) > 0) {
            if (player.getHealth() <= player.getMaxHealth() / 2) {
                long last = furyCooldown.getOrDefault(player.getUniqueId(), 0L);
                if (System.currentTimeMillis() - last > 30_000L) {
                    target.getWorld().strikeLightning(target.getLocation());
                    furyCooldown.put(player.getUniqueId(), System.currentTimeMillis());
                }
            }
        }
    }

    @EventHandler
    public void onKill(EntityDeathEvent event) {
        if (!(event.getEntity().getKiller() instanceof Player player)) return;
        ItemStack weapon = player.getInventory().getItemInMainHand();
        if (weapon == null) return;
        LivingEntity mob = event.getEntity();

        if (mob instanceof Creeper) {
            int level = SoulUpgradeSystem.getUpgradeLevel(weapon, SoulUpgradeSystem.SwordUpgrade.BETRAYAL);
            if (level > 0 && random.nextDouble() < level * 0.04) {
                mob.getWorld().dropItemNaturally(mob.getLocation(), new ItemStack(Material.MUSIC_DISC_11));
            }
        }

        if (SoulUpgradeSystem.getUpgradeLevel(weapon, SoulUpgradeSystem.SwordUpgrade.STARLESS_NIGHT) > 0 && mob instanceof Monster) {
            World world = mob.getWorld();
            long time = world.getTime();
            if (time > 13000 && time < 23000) {
                world.setTime(Math.max(0, time - 60));
            }
        }

        int challenge = SoulUpgradeSystem.getUpgradeLevel(weapon, SoulUpgradeSystem.SwordUpgrade.CHALLENGE);
        if (challenge > 0 && mob instanceof Monster && random.nextDouble() < challenge * 0.25) {
            mob.getWorld().spawnEntity(mob.getLocation(), mob.getType());
        }

        int bloodMoon = SoulUpgradeSystem.getUpgradeLevel(weapon, SoulUpgradeSystem.SwordUpgrade.BLOOD_MOON);
        if (bloodMoon > 0 && mob instanceof Monster && random.nextDouble() < bloodMoon * 0.20) {
            mob.getWorld().spawnEntity(mob.getLocation(), mob.getType());
        }

        int apocalypse = SoulUpgradeSystem.getUpgradeLevel(weapon, SoulUpgradeSystem.SwordUpgrade.APOCALYPSE);
        if (apocalypse > 0 && mob instanceof Monster && random.nextDouble() < apocalypse * 0.15) {
            LivingEntity newMob = (LivingEntity) mob.getWorld().spawnEntity(mob.getLocation(), mob.getType());
            SpawnMonsters.getInstance(xpManager).applyEnragedMutation(newMob);
        }

        int cats = SoulUpgradeSystem.getUpgradeLevel(weapon, SoulUpgradeSystem.SwordUpgrade.BALLAD_OF_THE_CATS);
        if (cats > 0 && mob instanceof Monster) {
            LivingEntity newMob = (LivingEntity) mob.getWorld().spawnEntity(mob.getLocation(), mob.getType());
            SpawnMonsters.getInstance(xpManager).applyMobAttributes(newMob, cats * 20);
        }
    }
}
