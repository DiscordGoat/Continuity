package goat.minecraft.minecraftnew.subsystems.music.discs.disc13;

import goat.minecraft.minecraftnew.MinecraftNew;
import goat.minecraft.minecraftnew.subsystems.music.discs.MusicDisc;
import goat.minecraft.minecraftnew.subsystems.oxygen.PlayerOxygenManager;
import org.bukkit.*;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;

public class Disc13 implements MusicDisc {
    private final JavaPlugin plugin;
    private static final List<ItemStack> LOOT_ITEMS = new ArrayList<>();

    static {
        LOOT_ITEMS.add(new ItemStack(Material.MUSIC_DISC_CAT));
        LOOT_ITEMS.add(new ItemStack(Material.MUSIC_DISC_BLOCKS));
        LOOT_ITEMS.add(new ItemStack(Material.MUSIC_DISC_CHIRP));
        LOOT_ITEMS.add(new ItemStack(Material.MUSIC_DISC_FAR));
        LOOT_ITEMS.add(new ItemStack(Material.MUSIC_DISC_MALL));
        LOOT_ITEMS.add(new ItemStack(Material.MUSIC_DISC_MELLOHI));
        LOOT_ITEMS.add(new ItemStack(Material.MUSIC_DISC_STAL));
        LOOT_ITEMS.add(new ItemStack(Material.MUSIC_DISC_STRAD));
        LOOT_ITEMS.add(new ItemStack(Material.MUSIC_DISC_WARD));
        LOOT_ITEMS.add(new ItemStack(Material.MUSIC_DISC_WAIT));
        LOOT_ITEMS.add(new ItemStack(Material.MUSIC_DISC_PIGSTEP));
        LOOT_ITEMS.add(new ItemStack(Material.MUSIC_DISC_OTHERSIDE));
        LOOT_ITEMS.add(new ItemStack(Material.MUSIC_DISC_RELIC));
    }

    public Disc13(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    private ItemStack getRandomLootItem() {
        return LOOT_ITEMS.get(new Random().nextInt(LOOT_ITEMS.size()));
    }

    @Override
    public Material getDiscMaterial() {
        return Material.MUSIC_DISC_13;
    }

    @Override
    public void onUse(Player player) {
        Bukkit.getWorld("world").setTime(18000);
        Bukkit.broadcastMessage(ChatColor.AQUA + "The BaroTrauma Virus (BT) has been activated for 2 minutes 58 seconds!");
        Bukkit.broadcastMessage(ChatColor.AQUA + "Beware! BT-infected monsters are slower but carry rare loot. Don't get infected!");
        player.playSound(player.getLocation(), Sound.MUSIC_DISC_13, 1000.0f, 1.0f);

        Listener btListener = new Listener() {
            @EventHandler
            public void onEntitySpawn(CreatureSpawnEvent event) {
                if (Math.random() < 0.3) {
                    LivingEntity entity = event.getEntity();
                    Location location = entity.getLocation();
                    if (location.getWorld().getHighestBlockYAt(location) <= location.getBlockY()) {
                        entity.setCustomName(ChatColor.AQUA + "BT-Infected " + entity.getType().name());
                        entity.setCustomNameVisible(true);
                        entity.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, (2 * 60 + 58) * 20, 0));
                        entity.getWorld().spawnParticle(Particle.WATER_SPLASH, entity.getLocation(), 50, 0.5, 0.5, 0.5, 0.1);
                        entity.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, (2 * 60 + 58) * 20, 2));
                        entity.getPersistentDataContainer().set(new NamespacedKey(MinecraftNew.getInstance(), "bt_monster"), PersistentDataType.BYTE, (byte)1);
                    }
                }
            }

            @EventHandler
            public void onEntityDeath(EntityDeathEvent event) {
                if (event.getEntity().getPersistentDataContainer().has(new NamespacedKey(MinecraftNew.getInstance(), "bt_monster"), PersistentDataType.BYTE)) {
                    Location deathLocation = event.getEntity().getLocation();
                    deathLocation.getWorld().spawn(deathLocation, ExperienceOrb.class).setExperience(20);
                    if (Math.random() < 0.2) {
                        event.getDrops().add(getRandomLootItem());
                    }
                }
            }

            @EventHandler
            public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
                if (event.getDamager() instanceof LivingEntity && event.getEntity() instanceof Player) {
                    LivingEntity damager = (LivingEntity) event.getDamager();
                    Player damagedPlayer = (Player) event.getEntity();
                    if (damager.getPersistentDataContainer().has(new NamespacedKey(MinecraftNew.getInstance(), "bt_monster"), PersistentDataType.BYTE)) {
                        if (Math.random() < 0.5) {
                            Bukkit.broadcastMessage(ChatColor.RED + damagedPlayer.getName() + " has been infected with the BaroTrauma Virus!");
                            PlayerOxygenManager playerOxygenManager = PlayerOxygenManager.getInstance();
                            playerOxygenManager.setPlayerOxygenLevel(player, 0);
                            damagedPlayer.sendMessage(ChatColor.DARK_AQUA + "You lost your oxygen!");
                        }
                    }
                }
            }
        };

        Bukkit.getPluginManager().registerEvents(btListener, MinecraftNew.getInstance());
        Bukkit.getScheduler().runTaskLater(MinecraftNew.getInstance(), () -> {
            Bukkit.broadcastMessage(ChatColor.RED + "The BaroTrauma Virus has subsided. Infected monsters are no longer spawning.");
            HandlerList.unregisterAll(btListener);
        }, ((2 * 60) + 58) * 20L);
    }
}
