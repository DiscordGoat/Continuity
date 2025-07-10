package goat.minecraft.minecraftnew.other.enchanting.enchantingeffects;

import goat.minecraft.minecraftnew.other.enchanting.CustomEnchantmentManager;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;

/**
 * The Shear class implements the "Shear" enchantment effect.
 * When a player with a Shear-enchanted sword hits a mob, there's a (ShearLevel * 5)% chance
 * to cause the mob to drop its normal loot again at its location immediately upon being hit.
 */
public class Shear implements Listener {

    private final Random random = new Random();

    // Mapping of EntityType to their standard drops
    private final Map<EntityType, List<ItemStack>> mobStandardDrops = new HashMap<>();

    /**
     * Constructor to initialize the Shear listener and define standard drops.
     */
    public Shear() {
        initializeMobStandardDrops();
    }

    /**
     * Initializes the mobStandardDrops map with predefined drops for all standard living entities.
     * This method covers all standard mobs as of Minecraft 1.20. Adjust as needed for newer versions or custom mobs.
     */
    private void initializeMobStandardDrops() {
        // Hostile Mobs
        mobStandardDrops.put(EntityType.ZOMBIE, Arrays.asList(
                new ItemStack(Material.ROTTEN_FLESH, 1)
        ));
        mobStandardDrops.put(EntityType.SKELETON, Arrays.asList(
                new ItemStack(Material.BONE, 1),
                new ItemStack(Material.ARROW, 1)
        ));
        mobStandardDrops.put(EntityType.CREEPER, Collections.singletonList(
                new ItemStack(Material.GUNPOWDER, 1)
        ));
        mobStandardDrops.put(EntityType.SPIDER, Collections.singletonList(
                new ItemStack(Material.STRING, 1)
        ));
        mobStandardDrops.put(EntityType.ZOMBIFIED_PIGLIN, Arrays.asList( // Deprecated in newer versions; use ZOMBIFIED_PIGLIN
                new ItemStack(Material.ROTTEN_FLESH, 1),
                new ItemStack(Material.GOLD_NUGGET, 1)
        ));
        mobStandardDrops.put(EntityType.BLAZE, Collections.singletonList(
                new ItemStack(Material.BLAZE_POWDER, 1)
        ));
        mobStandardDrops.put(EntityType.GHAST, Arrays.asList(
                new ItemStack(Material.GHAST_TEAR, 1),
                new ItemStack(Material.GUNPOWDER, 1)
        ));
        mobStandardDrops.put(EntityType.SILVERFISH, Collections.singletonList(
                new ItemStack(Material.IRON_NUGGET, 1) // Not a typical drop; adjust as needed
        ));
        mobStandardDrops.put(EntityType.WITCH, Arrays.asList(
                new ItemStack(Material.GLOWSTONE_DUST, 1),
                new ItemStack(Material.SPIDER_EYE, 1),
                new ItemStack(Material.REDSTONE, 1),
                new ItemStack(Material.STICK, 1),
                new ItemStack(Material.GLASS_BOTTLE, 1)
        ));
        mobStandardDrops.put(EntityType.PILLAGER, Arrays.asList(
                new ItemStack(Material.EMERALD, 1),
                new ItemStack(Material.FEATHER, 1),
                new ItemStack(Material.BONE, 1)
        ));
        // Passive Mobs
        mobStandardDrops.put(EntityType.PIG, Collections.singletonList(
                new ItemStack(Material.PORKCHOP, 1)
        ));
        mobStandardDrops.put(EntityType.CHICKEN, Arrays.asList(
                new ItemStack(Material.FEATHER, 1),
                new ItemStack(Material.CHICKEN, 1)
        ));
        mobStandardDrops.put(EntityType.COW, Arrays.asList(
                new ItemStack(Material.LEATHER, 1),
                new ItemStack(Material.BEEF, 1)
        ));
        mobStandardDrops.put(EntityType.SHEEP, Arrays.asList(
                new ItemStack(Material.MUTTON, 1),
                new ItemStack(Material.WHITE_WOOL, 1)
        ));
        mobStandardDrops.put(EntityType.RABBIT, Arrays.asList(
                new ItemStack(Material.RABBIT_HIDE, 1),
                new ItemStack(Material.RABBIT_FOOT, 1),
                new ItemStack(Material.RABBIT, 1)
        ));
        mobStandardDrops.put(EntityType.HOGLIN, Arrays.asList(
                new ItemStack(Material.PORKCHOP, 1),
                new ItemStack(Material.LEATHER, 1) // Adjust as needed
        ));
        mobStandardDrops.put(EntityType.GOAT, Arrays.asList(
                new ItemStack(Material.GOAT_HORN, 1),
                new ItemStack(Material.LEATHER, 1)
        ));
        mobStandardDrops.put(EntityType.TURTLE, Arrays.asList(
                new ItemStack(Material.TURTLE_SCUTE, 1)
        ));
        mobStandardDrops.put(EntityType.SQUID, Collections.singletonList(
                new ItemStack(Material.INK_SAC, 1)
        ));
        mobStandardDrops.put(EntityType.GLOW_SQUID, Arrays.asList(
                new ItemStack(Material.GLOW_INK_SAC, 1),
                new ItemStack(Material.PRISMARINE_CRYSTALS, 1)
        ));
        mobStandardDrops.put(EntityType.POLAR_BEAR, Arrays.asList(
                new ItemStack(Material.COD, 1),
                new ItemStack(Material.LEATHER, 1)
        ));
        mobStandardDrops.put(EntityType.WITHER_SKELETON, Arrays.asList(
                new ItemStack(Material.BONE, 1),
                new ItemStack(Material.BLACKSTONE, 1) // Not a standard drop; adjust as needed
        ));
        mobStandardDrops.put(EntityType.GUARDIAN, Arrays.asList(
                new ItemStack(Material.PRISMARINE_CRYSTALS, 1),
                new ItemStack(Material.PRISMARINE_SHARD, 1)
        ));
        mobStandardDrops.put(EntityType.PHANTOM, Collections.singletonList(
                new ItemStack(Material.PHANTOM_MEMBRANE, 1)
        ));

        mobStandardDrops.put(EntityType.CAMEL, Arrays.asList(
                new ItemStack(Material.LEATHER, 1),
                new ItemStack(Material.SAND, 1)
        ));
        mobStandardDrops.put(EntityType.BEE, Arrays.asList(
                new ItemStack(Material.HONEY_BOTTLE, 1)
        ));
        mobStandardDrops.put(EntityType.BAT, Collections.singletonList(
                new ItemStack(Material.BONE, 1)
        ));
        mobStandardDrops.put(EntityType.PARROT, Collections.singletonList(
                new ItemStack(Material.FEATHER, 1)
        ));
        mobStandardDrops.put(EntityType.HORSE, Arrays.asList(
                new ItemStack(Material.LEATHER, 1)
        ));
        mobStandardDrops.put(EntityType.PIGLIN_BRUTE, Arrays.asList(
                new ItemStack(Material.GOLD_BLOCK, 1),
                new ItemStack(Material.ROTTEN_FLESH, 1)
        ));
        mobStandardDrops.put(EntityType.IRON_GOLEM, Arrays.asList(
                new ItemStack(Material.IRON_INGOT, 1),
                new ItemStack(Material.POPPY, 1)
        ));
        // Add any additional mobs as necessary
    }

    /**
     * Handles the event when a player damages an entity.
     * If the player's weapon has the "Shear" enchantment, there's a chance to trigger the Shear effect.
     *
     * @param event The EntityDamageByEntityEvent triggered when an entity is damaged by another entity.
     */
    @EventHandler
    public void onHit(EntityDamageByEntityEvent event) {
        Entity damagerEntity = event.getDamager();
        Entity targetEntity = event.getEntity();

        // Check if the damager is a player and the target is a LivingEntity
        if (!(damagerEntity instanceof Player) || !(targetEntity instanceof LivingEntity)) {
            return;
        }

        Player player = (Player) damagerEntity;
        ItemStack weapon = player.getInventory().getItemInMainHand();

        // Check if the weapon has the "Shear" enchantment
        if (!CustomEnchantmentManager.hasEnchantment(weapon, "Shear")) {
            return;
        }

        // Get the level of the "Shear" enchantment
        int shearLevel = CustomEnchantmentManager.getEnchantmentLevel(weapon, "Shear");

        // Ensure the enchantment level is within bounds (1 to 5)
        if (shearLevel < 1 || shearLevel > 5) {
            return;
        }

        // Calculate the activation chance (ShearLevel * 5)%
        int chancePercentage = shearLevel * 2;
        int randomNumber = random.nextInt(100) + 1; // Generates a number between 1 and 100

        if (randomNumber <= chancePercentage) {
            LivingEntity mob = (LivingEntity) targetEntity;
            EntityType mobType = mob.getType();

            // Retrieve the standard drops for the mob type
            List<ItemStack> standardDrops = getStandardDrops(mobType);

            if (standardDrops.isEmpty()) {
                return; // No defined drops for this mob type
            }

            // Drop the items naturally at the mob's location
            Location dropLocation = mob.getLocation();
            for (ItemStack item : standardDrops) {
                // Only drop items that are not AIR and have a positive amount
                if (item != null && item.getType() != Material.AIR && item.getAmount() > 0) {
                    mob.getWorld().dropItemNaturally(dropLocation, item);
                }
            }

            // Send feedback to the player
            player.playSound(player.getLocation(), Sound.ENTITY_SHEEP_SHEAR, 5, 10);
            int enchantmentLevel = CustomEnchantmentManager.getEnchantmentLevel(weapon, "Shear");
            player.getInventory().getItemInMainHand().setDurability((short) (player.getInventory().getItemInMainHand().getDurability() +(1*enchantmentLevel)));

        }
    }

    /**
     * Retrieves the standard drops for a given mob type.
     *
     * @param mobType The EntityType of the mob.
     * @return A list of ItemStacks representing the mob's standard drops.
     */
    private List<ItemStack> getStandardDrops(EntityType mobType) {
        return mobStandardDrops.getOrDefault(mobType, Collections.emptyList());
    }
}
