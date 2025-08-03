package goat.minecraft.minecraftnew.subsystems.dragons;

import goat.minecraft.minecraftnew.MinecraftNew;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Centralised controller for custom dragon fights. Health and behaviour for
 * dragons are tracked here rather than on the entity itself to avoid the
 * vanilla 2000 HP cap and to support future abilities.
 */
public class DragonFightManager implements Listener {

    private final MinecraftNew plugin;

    private EnderDragon activeDragon;
    private Dragon activeDragonType;
    private BossBar bossBar;
    private DragonFight fight;
    private BukkitRunnable decisionTask;

    private static class DragonFight {
        double maxHealth;
        double currentHealth;
        int baseRage;
        int flightSpeed;
    }

    public DragonFightManager(MinecraftNew plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    /**
     * Begin a dragon fight in the given world. If a fight is already active the
     * player is simply added to the existing boss bar.
     */
    public void startFight(World world) {
        if (fight != null) {
            if (bossBar != null) {
                for (Player p : world.getPlayers()) {
                    bossBar.addPlayer(p);
                }
            }
            return;
        }

        Dragon type = DragonRegistry.randomDragon();
        Location spawn = new Location(world, 0, 80, 0);
        EnderDragon dragon = (EnderDragon) world.spawnEntity(spawn, EntityType.ENDER_DRAGON);
        type.applyAttributes(dragon);
        double mult = type.getFlightSpeed() / 5.0;
        dragon.setVelocity(dragon.getVelocity().multiply(mult));

        activeDragon = dragon;
        activeDragonType = type;

        fight = new DragonFight();
        fight.maxHealth = type.getMaxHealth();
        fight.currentHealth = fight.maxHealth;
        fight.baseRage = type.getBaseRage();
        fight.flightSpeed = type.getFlightSpeed();

        bossBar = Bukkit.createBossBar(type.getDisplayName(), type.getBarColor(), type.getBarStyle());
        bossBar.setProgress(1.0);
        for (Player p : world.getPlayers()) {
            bossBar.addPlayer(p);
        }

        startDecisionTask();
    }

    private void startDecisionTask() {
        if (decisionTask != null) {
            decisionTask.cancel();
        }
        int cooldown = Math.max(15, 65 - 5 * fight.baseRage); // seconds
        decisionTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (activeDragon == null) {
                    cancel();
                    return;
                }
                double chance = fight.baseRage * 0.10; // (baseRage*10)/100
                if (Math.random() < chance) {
                    Bukkit.getLogger().info("dragon has made a decision");
                }
            }
        };
        decisionTask.runTaskTimer(plugin, cooldown * 20L, cooldown * 20L);
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (activeDragon == null) return;
        if (!event.getEntity().getUniqueId().equals(activeDragon.getUniqueId())) return;

        event.setCancelled(true);
        double damage = event.getFinalDamage();
        fight.currentHealth -= damage;
        if (fight.currentHealth <= 0) {
            handleDragonDeath();
        } else if (bossBar != null) {
            bossBar.setProgress(fight.currentHealth / fight.maxHealth);
        }
    }

    private void handleDragonDeath() {
        if (activeDragon == null) return;
        World world = activeDragon.getWorld();
        activeDragon.remove();
        activeDragon = null;

        if (bossBar != null) {
            bossBar.removeAll();
            bossBar = null;
        }

        if (decisionTask != null) {
            decisionTask.cancel();
            decisionTask = null;
        }

        String title = ChatColor.GREEN + "You Defeated the " + activeDragonType.getName() + " Dragon!";
        for (Player p : world.getPlayers()) {
            p.sendTitle(title, "", 10, 70, 20);
        }

        new BukkitRunnable() {
            int countdown = 60;
            @Override
            public void run() {
                if (countdown == 60) {
                    for (Player p : world.getPlayers()) {
                        p.sendMessage(ChatColor.DARK_PURPLE + "Returning to spawn in 60 seconds");
                    }
                }
                if (countdown <= 0) {
                    for (Player p : world.getPlayers()) {
                        Location respawn = p.getBedSpawnLocation();
                        if (respawn == null) {
                            respawn = Bukkit.getWorlds().get(0).getSpawnLocation();
                        }
                        p.teleport(respawn);
                    }
                    reset();
                    cancel();
                }
                countdown--;
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }

    private void reset() {
        activeDragonType = null;
        fight = null;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (bossBar != null && activeDragon != null && event.getPlayer().getWorld().equals(activeDragon.getWorld())) {
            bossBar.addPlayer(event.getPlayer());
        }
    }

    @EventHandler
    public void onPlayerChangedWorld(PlayerChangedWorldEvent event) {
        if (bossBar != null && activeDragon != null && event.getPlayer().getWorld().equals(activeDragon.getWorld())) {
            bossBar.addPlayer(event.getPlayer());
        }
    }
}
