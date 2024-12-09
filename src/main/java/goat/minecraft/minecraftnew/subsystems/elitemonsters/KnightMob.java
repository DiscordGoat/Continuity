package goat.minecraft.minecraftnew.subsystems.elitemonsters;

import goat.minecraft.minecraftnew.MinecraftNew;
import goat.minecraft.minecraftnew.subsystems.utils.CustomItemManager;
import goat.minecraft.minecraftnew.subsystems.utils.SpawnMonsters;
import goat.minecraft.minecraftnew.subsystems.utils.XPManager;
import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

import static goat.minecraft.minecraftnew.subsystems.utils.CustomItemManager.createCustomItem;

public class KnightMob implements Listener {

    private final JavaPlugin plugin;
    public XPManager xpManager = new XPManager(MinecraftNew.getInstance());

    private final Random random = new Random();

    public KnightMob(JavaPlugin plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    ArrayList<EntityType> zombieList = new ArrayList<EntityType>();
    // Method to transform a mob into a knight
    @EventHandler
    public void zombieSpawn(EntitySpawnEvent e){
        zombieList.add(EntityType.ZOMBIE);
        zombieList.add(EntityType.ZOMBIE_VILLAGER);
        zombieList.add(EntityType.ZOMBIFIED_PIGLIN);
        zombieList.add(EntityType.DROWNED);

        if(zombieList.contains(e.getEntity().getType())){
            if (random.nextInt(100) < 1) { // 4% chance to transform
                transformToKnight(e.getEntity());
            }
        }
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
            // Set mob attributes for increased difficulty
            // Optionally set a custom name for identification

        SpawnMonsters spawnMonsters = new SpawnMonsters(plugin, xpManager);
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
            world.playSound(loc, Sound.BLOCK_METAL_BREAK, 1.0f, 1.0f);

            // Spawn iron particles
            world.spawnParticle(Particle.CRIT, loc, 10, 0.5, 1, 0.5);
        }
    }
    public ItemStack rareItem() {
        return createCustomItem(
                Material.BLUE_CARPET,
                ChatColor.BLUE + "Blueprints",
                List.of(ChatColor.GRAY + "A rare blueprint entrusted to the Knights",
                        ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Draw a random Reforge.",
                        ChatColor.DARK_PURPLE + "Artifact"),
                1,
                false // Set to true if you want it to be unbreakable
                , true
        );
    }
    public ItemStack singularity() {
        return createCustomItem(
                Material.IRON_NUGGET,
                ChatColor.BLUE + "Singularity",
                List.of(ChatColor.GRAY + "A rare blueprint entrusted to the Knights",
                        ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Reforges Items to the first Tier.",
                        ChatColor.DARK_PURPLE + "Smithing Item"),
                1,
                false // Set to true if you want it to be unbreakable
                , true
        );
    }

    // Event listener to drop a rare item on knight death
    @EventHandler
    public void onKnightDeath(EntityDeathEvent event) {
        if (zombieList.contains(event.getEntity().getType()) &&
                event.getEntity().getCustomName() != null &&
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
            if (random.nextDouble() < 1.0) { // 10% chance to drop the rare item
                Bukkit.getLogger().info("Adding rare item drop for knight: " + event.getEntity().getUniqueId());
                event.getDrops().add(singularity());
            }
        }
    }

}
