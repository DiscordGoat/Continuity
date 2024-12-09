package goat.minecraft.minecraftnew.subsystems.structures;

import goat.minecraft.minecraftnew.subsystems.pets.PetManager;
import goat.minecraft.minecraftnew.subsystems.utils.StructureUtils;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.Random;

/**
 * Handles transformations of mobs near mineshafts into specialized "Mineshaft" mobs.
 */
public class Mineshafts implements Listener {

    private final JavaPlugin plugin;
    private final StructureUtils structureUtils;
    private final Random random = new Random();

    public Mineshafts(JavaPlugin plugin) {
        this.plugin = plugin;
        this.structureUtils = new StructureUtils(); // Utility instance
    }

    @EventHandler
    public void onMobSpawn(CreatureSpawnEvent event) {
        Entity entity = event.getEntity();

        // Check if the mob is near a mineshaft
        if (!isNearMineshaft(entity)) {
            // Add a 4% chance for Skeletons to become Foreman even when not near a mineshaft
            if(entity.getLocation().getBlockY() <= 0) {
                if (entity instanceof Skeleton && new Random().nextInt(100) < 4) { // 4% chance
                    transformToForeman((Skeleton) entity);
                }
            }
            return; // Exit if not near a mineshaft and not a lucky skeleton
        }

        // Transform mobs into "Mineshaft" mobs
        if (entity instanceof Skeleton) {
            transformToForeman((Skeleton) entity);
        } else if (entity instanceof Zombie) {
            transformToMiner((Zombie) entity);
        }
    }

    private boolean isNearMineshaft(Entity entity) {
        if (entity == null || entity.getWorld() == null) {
            return false;
        }

        // Use StructureUtils to get the nearest mineshaft location
        Location structureLocation = structureUtils.getNearestStructureLocation(StructureType.MINESHAFT, entity);

        if (structureLocation == null) {
            return false; // No mineshaft found
        }

        // Calculate the distance between the entity and the structure
        double distance = entity.getLocation().distance(structureLocation);

        // Check if the distance is within 100 blocks
        return distance <= 50;
    }




    private void transformToForeman(Skeleton skeleton) {
        // Change the skeleton's name
        skeleton.setCustomName(ChatColor.YELLOW + "Foreman");
        skeleton.setCustomNameVisible(true);

        // Equip the Foreman with a pickaxe
        ItemStack pickaxe = new ItemStack(Material.IRON_PICKAXE);
        skeleton.getEquipment().setItemInMainHand(pickaxe);

        // Equip the Foreman with armor
        ItemStack helmet = new ItemStack(Material.GOLDEN_HELMET);
        ItemStack chestplate = new ItemStack(Material.CHAINMAIL_CHESTPLATE);
        skeleton.getEquipment().setHelmet(helmet);
        skeleton.getEquipment().setChestplate(chestplate);

        // Set the Foreman's attributes

        // Drop a rare item upon death
        skeleton.getEquipment().setItemInMainHandDropChance(0); // 10% chance to drop the pickaxe
        skeleton.getEquipment().setHelmetDropChance(0); // 10% chance to drop the pickaxe
        skeleton.setPersistent(true);
    }
    @EventHandler
    public void onForemanDeath(EntityDeathEvent event) {
        Entity entity = event.getEntity();

        if(event.getEntity().getKiller() instanceof Player) {
            Player player = event.getEntity().getKiller();
            if (entity.getCustomName() != null && entity.getCustomName().equals(ChatColor.YELLOW + "Foreman")) {
                PetManager petManager = PetManager.getInstance(plugin);
                Random random = new Random();

                // 25% chance to create the "Armadillo" pet
                if (random.nextInt(100) < 25) { // 25% chance
                    petManager.createPet(
                            player,
                            "Armadillo",
                            PetManager.Rarity.RARE,
                            100,
                            Particle.DAMAGE_INDICATOR,
                            PetManager.PetPerk.BONE_PLATING,
                            PetManager.PetPerk.DIGGING_CLAWS
                    );
                    player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
                }

                // 1% chance to create the "Dwarf" pet
                if (random.nextInt(100) < 1) { // 1% chance
                    petManager.createPet(
                            player,
                            "Dwarf",
                            PetManager.Rarity.EPIC,
                            100,
                            Particle.DAMAGE_INDICATOR,
                            PetManager.PetPerk.DIGGING_CLAWS,
                            PetManager.PetPerk.MITHRIL_MINER,
                            PetManager.PetPerk.EMERALD_SEEKER
                    );
                    player.playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 1.0f, 1.0f);
                }
            }

        }
    }
    private void transformToMiner(Zombie zombie) {
        // Change the zombie's name
        zombie.setCustomName(ChatColor.GREEN + "Miner");
        zombie.setCustomNameVisible(true);

        // Equip the Miner with a pickaxe
        ItemStack pickaxe = new ItemStack(Material.STONE_PICKAXE);
        zombie.getEquipment().setItemInMainHand(pickaxe);

        // Equip the Miner with armor
        ItemStack helmet = new ItemStack(Material.LEATHER_HELMET);
        ItemMeta helmetMeta = helmet.getItemMeta();
        if (helmetMeta != null) {
            helmetMeta.setDisplayName(ChatColor.DARK_GREEN + "Miner's Cap");
            helmet.setItemMeta(helmetMeta);
        }
        zombie.getEquipment().setHelmet(helmet);

        // Set the Miner's attributes


        // Set equipment drop chances
        zombie.getEquipment().setItemInMainHandDropChance(0); // 10% chance to drop the pickaxe
        zombie.getEquipment().setHelmetDropChance(0); // 10% chance to drop the pickaxe

        zombie.setPersistent(true);
    }
}
