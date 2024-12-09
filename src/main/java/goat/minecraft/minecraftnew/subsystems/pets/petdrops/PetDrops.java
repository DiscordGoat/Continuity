package goat.minecraft.minecraftnew.subsystems.pets.petdrops;

import goat.minecraft.minecraftnew.subsystems.pets.PetManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

public class PetDrops implements Listener {

    private final JavaPlugin plugin;
    private final PetManager petManager;
    private final Map<EntityType, String> mobToPetMap; // Maps mobs to pet names
    private final Random random = new Random();

    public PetDrops(JavaPlugin plugin, PetManager petManager) {
        this.plugin = plugin;
        this.petManager = petManager;
        this.mobToPetMap = new HashMap<>();

        initializeMobToPetMap();
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    private void initializeMobToPetMap() {
        // Map mobs to their pet variants
        mobToPetMap.put(EntityType.ZOMBIFIED_PIGLIN, "Zombie Pigman");
        mobToPetMap.put(EntityType.COW, "Cow");
        mobToPetMap.put(EntityType.PIG, "Pig");
        mobToPetMap.put(EntityType.SHEEP, "Sheep");
        mobToPetMap.put(EntityType.CAT, "Cat");
        mobToPetMap.put(EntityType.PARROT, "Parrot");
        mobToPetMap.put(EntityType.HORSE, "Horse");
        mobToPetMap.put(EntityType.CHICKEN, "Chicken");
        mobToPetMap.put(EntityType.DOLPHIN, "Dolphin");
        mobToPetMap.put(EntityType.GLOW_SQUID, "Glow Squid");
        mobToPetMap.put(EntityType.TURTLE, "Turtle");
        mobToPetMap.put(EntityType.BAT, "Bat");
        mobToPetMap.put(EntityType.IRON_GOLEM, "Iron Golem");
        mobToPetMap.put(EntityType.GUARDIAN, "Guardian");
        mobToPetMap.put(EntityType.STRAY, "Stray");
        mobToPetMap.put(EntityType.PILLAGER, "Pillager");
        mobToPetMap.put(EntityType.AXOLOTL, "Axolotl");
        mobToPetMap.put(EntityType.MUSHROOM_COW, "Mooshroom");
        mobToPetMap.put(EntityType.ZOMBIE, "Zombie");
        mobToPetMap.put(EntityType.VINDICATOR, "Vindicator");
        mobToPetMap.put(EntityType.ZOMBIFIED_PIGLIN, "Zombie Pigman");
        // Add more mappings as needed
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        Entity entity = event.getEntity();
        EntityType entityType = entity.getType();
        Player player = event.getEntity().getKiller();

        // Check if the mob has a pet variant
        if (mobToPetMap.containsKey(entityType)) {
            String petName = mobToPetMap.get(entityType);
            if(entityType.equals(EntityType.BLAZE)) {
                // Configurable drop chance (e.g., 5%)
                if (random.nextDouble() < 0.01) {
                    // Drop the pet item
                    petManager.createPet(player, "Blaze", PetManager.Rarity.LEGENDARY, 100, Particle.ASH, PetManager.PetPerk.BLACKLUNG, PetManager.PetPerk.FIREPROOF, PetManager.PetPerk.FLIGHT);
                }
            }
            if(entityType.equals(EntityType.WITHER_SKELETON)) {
                // Configurable drop chance (e.g., 5%)
                if (random.nextDouble() < 0.005) {
                    // Drop the pet item
                    petManager.createPet(player, "Wither Skeleton", PetManager.Rarity.LEGENDARY, 100, Particle.ASH, PetManager.PetPerk.BLACKLUNG, PetManager.PetPerk.SPEED_BOOST, PetManager.PetPerk.DEVOUR, PetManager.PetPerk.FIREPROOF, PetManager.PetPerk.DECAY);
                }
            }
            if(entityType.equals(EntityType.ENDERMAN)) {
                // Configurable drop chance (e.g., 5%)
                if (random.nextDouble() < 0.01) {
                    // Drop the pet item
                    petManager.createPet(player, "Enderman", PetManager.Rarity.LEGENDARY, 100, Particle.ASH, PetManager.PetPerk.ELITE, PetManager.PetPerk.ASPECT_OF_THE_END, PetManager.PetPerk.COLLECTOR);
                }
            }
            if(entityType.equals(EntityType.STRAY)) {
                // Configurable drop chance (e.g., 5%)
                if (random.nextDouble() < 0.005) {
                    // Drop the pet item
                    petManager.createPet(player, "Stray", PetManager.Rarity.LEGENDARY, 100, Particle.WHITE_ASH, PetManager.PetPerk.SHOTCALLING, PetManager.PetPerk.RECOVERY, PetManager.PetPerk.QUICK_DRAW, PetManager.PetPerk.TIPPED_SLOWNESS, PetManager.PetPerk.BONE_COLD);
                }
            }
            if(entityType.equals(EntityType.PILLAGER)) {
                // Configurable drop chance (e.g., 5%)
                if (random.nextDouble() < 0.01) {
                    // Drop the pet item
                    petManager.createPet(player, "Vindicator", PetManager.Rarity.LEGENDARY, 100, Particle.FIREWORKS_SPARK, PetManager.PetPerk.SPEED_BOOST, PetManager.PetPerk.SKEPTICISM, PetManager.PetPerk.GREED, PetManager.PetPerk.ELITE);
                }
            }
            if (entityType.equals(EntityType.HORSE)) {
                event.getDrops().clear(); // Clear the list of drops
                entity.remove(); // Remove the horse entity
                event.setDroppedExp(0);
            }

            if(entityType.equals(EntityType.PILLAGER)) {
                // Configurable drop chance (e.g., 5%)
                if (random.nextDouble() < 0.1) {
                    // Drop the pet item
                    petManager.createPet(player, "Pillager", PetManager.Rarity.RARE, 100, Particle.WHITE_ASH, PetManager.PetPerk.SHOTCALLING, PetManager.PetPerk.RECOVERY);
                }
            }
            if(entityType.equals(EntityType.GUARDIAN)) {
                // Configurable drop chance (e.g., 5%)
                if (random.nextDouble() < 0.01) {
                    // Drop the pet item
                    petManager.createPet(player, "Guardian", PetManager.Rarity.EPIC, 100, Particle.WHITE_ASH, PetManager.PetPerk.SHOTCALLING, PetManager.PetPerk.RECOVERY, PetManager.PetPerk.LASER_BEAM);
                }
            }
            if(entityType.equals(EntityType.ZOMBIFIED_PIGLIN)) {
                // Configurable drop chance (e.g., 5%)
                if (random.nextDouble() < 0.002) {
                    // Drop the pet item
                    petManager.createPet(player, "Zombie Pigman", PetManager.Rarity.LEGENDARY, 100, Particle.ASH, PetManager.PetPerk.BLACKLUNG, PetManager.PetPerk.SPEED_BOOST, PetManager.PetPerk.SECRET_LEGION, PetManager.PetPerk.DEVOUR, PetManager.PetPerk.FIREPROOF);
                }
            }
            if(entityType.equals(EntityType.SKELETON)) {
                // Configurable drop chance (e.g., 5%)
                if (random.nextDouble() < 0.01) {
                    // Drop the pet item
                    petManager.createPet(player, "Skeleton", PetManager.Rarity.UNCOMMON, 100, Particle.WHITE_ASH, PetManager.PetPerk.SHOTCALLING, PetManager.PetPerk.BONE_PLATING_WEAK);
                }
            }
            if(entityType.equals(EntityType.ZOMBIE)) {
                // Configurable drop chance (e.g., 5%)
                if (random.nextDouble() < 0.01) {
                    // Drop the pet item
                    petManager.createPet(player, "Zombie", PetManager.Rarity.RARE, 100, Particle.CRIT_MAGIC, PetManager.PetPerk.SECOND_WIND, PetManager.PetPerk.DEVOUR);
                }
            }
            if(entityType.equals(EntityType.IRON_GOLEM)) {
                // Configurable drop chance (e.g., 5%)
                if (random.nextDouble() < 1) {
                    // Drop the pet item
                    petManager.createPet(player, "Iron Golem", PetManager.Rarity.RARE, 100, Particle.ASH, PetManager.PetPerk.WALKING_FORTRESS, PetManager.PetPerk.ELITE);
                }
            }
            if(entityType.equals(EntityType.BAT)) {
                // Configurable drop chance (e.g., 5%)
                if (random.nextDouble() < 1) {
                    // Drop the pet item
                    petManager.createPet(player, "Bat", PetManager.Rarity.RARE, 100, Particle.DAMAGE_INDICATOR, PetManager.PetPerk.ECHOLOCATION);
                }
            }
            if(entityType.equals(EntityType.WARDEN)) {
                // Configurable drop chance (e.g., 5%)
                if (random.nextDouble() < 0.5) {
                    // Drop the pet item
                    petManager.createPet(player, "Warden", PetManager.Rarity.LEGENDARY, 100, Particle.WARPED_SPORE, PetManager.PetPerk.DIGGING_CLAWS, PetManager.PetPerk.ECHOLOCATION, PetManager.PetPerk.ELITE);
                }
            }
            if(entityType.equals(EntityType.ZOMBIFIED_PIGLIN)) {
                // Configurable drop chance (e.g., 5%)
                if (random.nextDouble() < 0.01) {
                    // Drop the pet item
                    petManager.createPet(player, "Zombie Pigman", PetManager.Rarity.LEGENDARY, 100, Particle.ASH, PetManager.PetPerk.SPEED_BOOST, PetManager.PetPerk.DEVOUR, PetManager.PetPerk.FIREPROOF);
                }
            }
        }
    }

}
