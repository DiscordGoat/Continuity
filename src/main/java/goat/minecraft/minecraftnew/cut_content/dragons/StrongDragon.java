package goat.minecraft.minecraftnew.cut_content.dragons;

import goat.minecraft.minecraftnew.MinecraftNew;
import me.gamercoder215.mobchip.DragonBrain;
import me.gamercoder215.mobchip.ai.enderdragon.CustomPhase;
import me.gamercoder215.mobchip.bukkit.BukkitBrain;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Objects;

public class StrongDragon {

    private final NamespacedKey flyAbovePlayer = new NamespacedKey(MinecraftNew.getInstance(), "fly_above_player");
    private final EnderDragon dragon;
    private final int level = 300; // Strong dragon level
    private final double maxHealthCap = 2000; // Cap for health calculation

    public StrongDragon(EnderDragon dragon) {
        this.dragon = dragon;
        applyStrongDragonAttributes();
        // Schedule height check and random phase reset tasks.
        scheduleHeightCheck();
        scheduleRandomPhaseReset();
    }

    private void applyStrongDragonAttributes() {
        // Apply long-duration potion effects.
        dragon.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, Integer.MAX_VALUE, 1, true));
        dragon.addPotionEffect(new PotionEffect(PotionEffectType.HEALTH_BOOST, Integer.MAX_VALUE, 255, true));
        dragon.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, Integer.MAX_VALUE, 255, true));
        // DAMAGE_RESISTANCE level 4 gives 80% damage reduction.
        dragon.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, Integer.MAX_VALUE, 1, true));

        // Set knockback resistance to 100% (immune to knockback).
        Objects.requireNonNull(dragon.getAttribute(Attribute.GENERIC_KNOCKBACK_RESISTANCE))
                .setBaseValue(1.0);

        // Calculate new health based on level and multiplier.
        double healthMultiplier = 1 + (level * 0.1);
        double originalHealth = dragon.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue();
        double newHealth = Math.min(originalHealth * healthMultiplier, maxHealthCap);

        // Set the new health values.
        Objects.requireNonNull(dragon.getAttribute(Attribute.GENERIC_MAX_HEALTH)).setBaseValue(newHealth);
        dragon.setHealth(newHealth);

        // Store level metadata.
        dragon.setMetadata("mobLevel", new FixedMetadataValue(MinecraftNew.getInstance(), level));

        // Set custom name to indicate a strong dragon.
        String customName = ChatColor.RED + "[Lv: " + level + "] Strong Dragon";
        dragon.setCustomName(customName);
        dragon.setCustomNameVisible(true);

        // Remove boss bar display.
        BossBar bossBar = dragon.getBossBar();
        if (bossBar != null) {
            bossBar.removeAll();
        }

        // --- Scoreboard part for glowing color ---
        Scoreboard board = Bukkit.getScoreboardManager().getMainScoreboard();
        Team team = board.getTeam("dragonRed");
        if (team == null) {
            team = board.registerNewTeam("dragonRed");
            team.setColor(ChatColor.RED); // Sets team color.
            team.setPrefix(ChatColor.RED.toString());
        }
        team.addEntry(dragon.getUniqueId().toString());
        Bukkit.getLogger().info("Team dragonRed entries: " + team.getEntries());
        dragon.setGlowing(true);
        // --- End scoreboard section ---

        // Set initial phase.
        dragon.setPhase(EnderDragon.Phase.CIRCLING);
    }

    /**
     * Schedules a task to check every 5 ticks that the dragon does not exceed Y=150.
     * If it does, it is teleported back to Y=150 and an Enderman teleport sound is played.
     */
    private void scheduleHeightCheck() {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!dragon.isValid() || dragon.isDead()) {
                    cancel();
                    return;
                }
                if (dragon.getLocation().getY() > 150) {
                    Location loc = dragon.getLocation();
                    loc.setY(150);
                    dragon.teleport(loc);
                    // Play a deep pitched Enderman teleport sound with high volume.
                    dragon.getWorld().playSound(loc, Sound.ENTITY_ENDERMAN_TELEPORT, 10000.0f, 0.1f);
                }
            }
        }.runTaskTimer(MinecraftNew.getInstance(), 0L, 5L);
    }

    /**
     * Schedules a task that, every 60 seconds, randomly resets the dragon's phase to CIRCLING,
     * but only if this dragon is not the initial dragon.
     */
    private void scheduleRandomPhaseReset() {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!dragon.isValid() || dragon.isDead()) {
                    cancel();
                    return;
                }
                // Assuming StrongDragon instances are not the initial dragon.
                // With 50% chance, reset the phase.
                if (Math.random() < 0.5) {
                    dragon.setPhase(EnderDragon.Phase.CIRCLING);
                }
            }
        }.runTaskTimer(MinecraftNew.getInstance(), 1200L, 1200L); // 1200 ticks = 60 seconds.
    }

    /**
     * This method uses MobChip to change the dragon's phase and then makes it fly above the player.
     * It also supports projectile damage and redirects perching phases to circling.
     * After teleporting above the target, it fires 50 fireballs one at a time, rapidly.
     *
     * @param target The player being counterattacked.
     */
    public void performCounterattack(Player target) {
        DragonBrain dragonBrain = BukkitBrain.getBrain(dragon);

        CustomPhase customPhase = new CustomPhase(dragon, flyAbovePlayer) {
            @NotNull
            @Override
            public Location getTargetLocation() {
                return target.getLocation();
            }

            @Override
            public void start() {
                // Teleport the dragon 20 blocks above the target.
                Location targetLoc = target.getLocation();
                Location abovePlayer = targetLoc.clone().add(0, 20, 0);
                dragon.teleport(abovePlayer);
            }

            @Override
            public float onDamage(EntityDamageEvent.DamageCause cause, float amount) {
                if (cause == EntityDamageEvent.DamageCause.PROJECTILE) {
                    return amount;
                }
                return super.onDamage(cause, amount);
            }

            @Override
            public void serverTick() {
                if (dragon.getPhase() == EnderDragon.Phase.FLY_TO_PORTAL || dragon.getPhase() == EnderDragon.Phase.LAND_ON_PORTAL) {
                    dragon.setPhase(EnderDragon.Phase.CIRCLING);
                }
                super.serverTick();
            }
        };
        dragonBrain.setCustomPhase(customPhase);

        // Fire 50 fireballs one at a time, every 2 ticks.
        final int totalFireballs = 50;
        final long delayBetweenFireballs = 2L;

        new BukkitRunnable() {
            int count = 0;
            @Override
            public void run() {
                if (count >= totalFireballs) {
                    cancel();
                    return;
                }
                Location dragonLoc = dragon.getLocation();
                Vector direction = target.getLocation().toVector().subtract(dragonLoc.toVector());
                direction.normalize();
                // Multiply to increase speed.
                direction.multiply(20.0);

                Fireball fireball = dragon.getWorld().spawn(dragonLoc, Fireball.class);
                fireball.setDirection(direction);
                // Set yield to 3.0f to allow explosions that may destroy endstone.
                fireball.setYield(3.0f);

                count++;
            }
        }.runTaskTimer(MinecraftNew.getInstance(), delayBetweenFireballs, delayBetweenFireballs);
    }

    /**
     * Applies incoming damage to the dragon, factoring in 80% damage reduction.
     *
     * @param damage The raw damage attempted.
     */
    public void applyDamage(double damage) {
        double effectiveDamage = damage * 0.2;
        double currentHealth = dragon.getHealth();
        double newHealth = Math.max(currentHealth - effectiveDamage, 0);
        dragon.setHealth(newHealth);
    }

    public EnderDragon getDragon() {
        return dragon;
    }
}
