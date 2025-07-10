package goat.minecraft.minecraftnew.subsystems.pets.petdrops;

import goat.minecraft.minecraftnew.subsystems.pets.PetManager;
import goat.minecraft.minecraftnew.subsystems.pets.PetRegistry;
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
        mobToPetMap.put(EntityType.MOOSHROOM, "Mooshroom");
        mobToPetMap.put(EntityType.ZOMBIE, "Zombie");
        mobToPetMap.put(EntityType.VINDICATOR, "Vindicator");
        mobToPetMap.put(EntityType.ZOMBIFIED_PIGLIN, "Zombie Pigman");
        mobToPetMap.put(EntityType.WITCH, "Witch");
        // Add more mappings as needed
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        PetRegistry petRegistry = new PetRegistry();
        Entity entity = event.getEntity();
        EntityType entityType = entity.getType();
        Player player = event.getEntity().getKiller();

        // Check if the mob has a pet variant
        if (mobToPetMap.containsKey(entityType)) {
            String petName = mobToPetMap.get(entityType);
            if (entityType.equals(EntityType.HORSE)) {
                event.getDrops().clear(); // Clear the list of drops
                entity.remove(); // Remove the horse entity
                event.setDroppedExp(0);
            }

            if(entityType.equals(EntityType.IRON_GOLEM)) {
                // Configurable drop chance (e.g., 5%)
                if (random.nextDouble() < 1) {
                    // Drop the pet item
                    petRegistry.addPetByName(player, "Iron Golem");                }
            }
            if(entityType.equals(EntityType.BAT)) {
                // Configurable drop chance (e.g., 5%)
                if (random.nextDouble() < 0.1) {
                    // Drop the pet item
                    petRegistry.addPetByName(player, "Bat");                }
            }
            if(entityType.equals(EntityType.WARDEN)) {
                // Configurable drop chance (e.g., 5%)
                if (random.nextDouble() < 0.5) {
                    // Drop the pet item
                    petRegistry.addPetByName(player, "Warden");                }
            }
            if(entityType.equals(EntityType.WITCH)) {
                if (random.nextDouble() < 0.1) {
                    petRegistry.addPetByName(player, "Witch");
                }
            }
        }
    }

}
