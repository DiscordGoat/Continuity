package goat.minecraft.minecraftnew.subsystems.combat;

import goat.minecraft.minecraftnew.MinecraftNew;
import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.*;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.*;

import java.util.*;

public class EpicEnderDragonFight implements Listener {
    private final MinecraftNew plugin;
    private final Random random = new Random();

    public EpicEnderDragonFight(MinecraftNew plugin) {
        this.plugin = plugin;
    }

    /**
     * Handles projectile hitting End Crystals.
     * End Crystals have a chance to deflect projectiles.
     */
    @EventHandler
    public void onProjectileHit(ProjectileHitEvent event) {
        if (event.getHitEntity() instanceof EnderCrystal || event.getHitBlock() != null && event.getHitBlock().getType() == Material.END_CRYSTAL) {
            if (random.nextDouble() < 0.5) { // 50% chance to deflect
                event.setCancelled(true);
                Projectile projectile = event.getEntity();
                projectile.setVelocity(projectile.getVelocity().multiply(-1));
                projectile.getWorld().playSound(projectile.getLocation(), Sound.ITEM_SHIELD_BLOCK, 1000.0f, 1.0f);
                projectile.getWorld().spawnParticle(Particle.CRIT_MAGIC, projectile.getLocation(), 10, 0.2, 0.2, 0.2, 0.1);
            }
        }
    }

    /**
     * Handles End Crystal destruction.
     * Spawns a Knight of the End upon destruction.
     */
    @EventHandler
    public void onEndCrystalDestroy(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof EnderCrystal) {
            EnderCrystal enderCrystal = (EnderCrystal) event.getEntity();
            // Check if the damage will destroy the crystal
                Location location = new Location(Bukkit.getWorld("world"), 0, 66, 0, 0, 0);
                // Play explosion sound and particles
                World world = location.getWorld();
                world.playSound(location, Sound.ENTITY_WITHER_SPAWN, 1.0f, 0.5f);
                world.spawnParticle(Particle.EXPLOSION_HUGE, location, 1);
                world.spawnParticle(Particle.SMOKE_LARGE, location, 20, 1, 1, 1, 0.1);
        }
    }


    /**
     * Creates an unbreakable item of the specified material.
     *
     * @param material The material of the item.
     * @return The unbreakable ItemStack.
     */
    private ItemStack createUnbreakableItem(Material material) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setUnbreakable(true);
            meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
            item.setItemMeta(meta);
        }
        return item;
    }

    /**
     * Handles the death of the Knight of the End.
     * Drops unbreakable Netherite armor and a Forbidden Book.
     */
    @EventHandler
    public void onKnightDeath(EntityDeathEvent event) {
        if (event.getEntity() instanceof WitherSkeleton) {
            WitherSkeleton knight = (WitherSkeleton) event.getEntity();
            if (knight.getCustomName() != null && knight.getCustomName().equals(ChatColor.DARK_PURPLE + "Knight of the End")) {
                // Clear default drops
                event.getDrops().clear();

                // Drop unbreakable Netherite armor with a chance
                if (random.nextDouble() < 0.5) {
                    event.getDrops().add(createUnbreakableItem(Material.NETHERITE_HELMET));
                }
                if (random.nextDouble() < 0.5) {
                    event.getDrops().add(createUnbreakableItem(Material.NETHERITE_CHESTPLATE));
                }
                if (random.nextDouble() < 0.5) {
                    event.getDrops().add(createUnbreakableItem(Material.NETHERITE_LEGGINGS));
                }
                if (random.nextDouble() < 0.5) {
                    event.getDrops().add(createUnbreakableItem(Material.NETHERITE_BOOTS));
                }
                if (random.nextDouble() < 0.5) {
                    event.getDrops().add(createUnbreakableItem(Material.NETHERITE_SWORD));
                }
                knight.playEffect(EntityEffect.ENTITY_POOF);
                knight.playEffect(EntityEffect.ENTITY_POOF);
                knight.playEffect(EntityEffect.ENTITY_POOF);
                // Always drop a Forbidden Book
                event.getDrops().add(createForbiddenBook());
            }
        }
    }

    /**
     * Creates a Forbidden Book item.
     *
     * @return The Forbidden Book ItemStack.
     */
    private ItemStack createForbiddenBook() {
        ItemStack forbiddenBook = new ItemStack(Material.ENCHANTED_BOOK);
        ItemMeta meta = forbiddenBook.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.DARK_PURPLE + "Forbidden Book");
            meta.addEnchant(Enchantment.LURE, 1, true); // Dummy enchant to make it enchanted
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            forbiddenBook.setItemMeta(meta);
        }
        return forbiddenBook;
    }

    /**
     * Optional: Modify the Ender Dragon's death to add custom behavior.
     */
    @EventHandler
    public void onDragonDeath(EntityDeathEvent event) {
        if (event.getEntity() instanceof EnderDragon) {
            // Add custom logic upon dragon's death
            World world = event.getEntity().getWorld();
            Location location = event.getEntity().getLocation();

            // Play sound and spawn particles
            world.playSound(location, Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
            world.spawnParticle(Particle.FIREWORKS_SPARK, location, 100, 3, 3, 3, 0.1);

            // Optionally, spawn additional loot or mobs
        }
    }
}
