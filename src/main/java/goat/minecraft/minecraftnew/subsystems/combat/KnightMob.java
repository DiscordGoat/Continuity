package goat.minecraft.minecraftnew.subsystems.combat;

import goat.minecraft.minecraftnew.MinecraftNew;
import goat.minecraft.minecraftnew.utils.devtools.ItemRegistry;
import goat.minecraft.minecraftnew.other.additionalfunctionality.SpawnMonsters;
import goat.minecraft.minecraftnew.utils.devtools.XPManager;
import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;
import java.util.Random;

public class KnightMob implements Listener {

    private final JavaPlugin plugin;
    public XPManager xpManager = new XPManager(MinecraftNew.getInstance());

    private final Random random = new Random();

    public KnightMob(JavaPlugin plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }
    public void transformToKnight(Entity entity) {
            LivingEntity mob = (LivingEntity) entity;

            // Equip the knight with iron armor and sword
            ItemStack ironSword = new ItemStack(Material.IRON_SWORD);
            ItemStack ironHelmet = new ItemStack(Material.IRON_HELMET);
            ItemStack ironChestplate = new ItemStack(Material.IRON_CHESTPLATE);
            ItemStack ironLeggings = new ItemStack(Material.IRON_LEGGINGS);
            ItemStack ironBoots = new ItemStack(Material.IRON_BOOTS);

            // Optionally add enchantments (e.g., for more challenge)
            ironSword.addEnchantment(Enchantment.DAMAGE_ALL, 1);

            // Set the mob's equipment
            mob.getEquipment().setItemInMainHand(ironSword);
            mob.getEquipment().setHelmet(ironHelmet);
            mob.getEquipment().setChestplate(ironChestplate);
            mob.getEquipment().setLeggings(ironLeggings);
            mob.getEquipment().setBoots(ironBoots);
            mob.setCustomName(ChatColor.GRAY + "Knight");
            // Set mob attributes for increased difficulty
            // Optionally set a custom name for identification

        SpawnMonsters spawnMonsters = SpawnMonsters.getInstance(xpManager);
        spawnMonsters.applyMobAttributes(mob, 100);
            mob.setCustomName(ChatColor.GRAY + "Knight");
            mob.setCustomNameVisible(true);
    }

    // Event listener to play sound and particles when the knight is hit
    @EventHandler
    public void onKnightHit(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof LivingEntity mob &&
                mob.getCustomName() != null &&
                mob.getCustomName().equals(ChatColor.GRAY + "Knight")) {
            Location loc = mob.getLocation();
            World world = mob.getWorld();

            // Play iron clang sound
            world.playSound(loc, Sound.ENTITY_ZOMBIE_ATTACK_IRON_DOOR, 1.0f, 1.0f);

            // Spawn iron particles
            world.spawnParticle(Particle.CRIT, loc, 10, 0.5, 1, 0.5);
        }
    }


    // Event listener to drop a rare item on knight death
    @EventHandler
    public void onKnightDeath(EntityDeathEvent event) {
        if (event.getEntity().getCustomName() != null &&
                event.getEntity().getCustomName().equals(ChatColor.GRAY + "Knight")) {

            // Check if the entity has a persistent data key to avoid double triggering
            if (event.getEntity().getPersistentDataContainer().has(new NamespacedKey(plugin, "processed_knight"), PersistentDataType.INTEGER)) {
                return; // Skip if already processed
            }

            // Mark entity as processed to prevent double handling
            event.getEntity().getPersistentDataContainer().set(new NamespacedKey(plugin, "processed_knight"), PersistentDataType.INTEGER, 1);

            // Increase XP drop
            XPManager xpManager = new XPManager(plugin);
            if (event.getEntity().getKiller() instanceof Player) {
                xpManager.addXP(Objects.requireNonNull(event.getEntity().getKiller()), "Combat", 800);
                event.setDroppedExp(event.getDroppedExp() + 50);
            }

            // Drop a rare item with a chance
            if (random.nextDouble() < 8.0) { // 10% chance to drop the rare item
                Bukkit.getLogger().info("Adding rare item drop for knight: " + event.getEntity().getUniqueId());
                event.getDrops().add(ItemRegistry.getSingularity());
            }
        }
    }

}
