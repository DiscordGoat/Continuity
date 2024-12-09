package goat.minecraft.minecraftnew.subsystems.pets.perks;

import goat.minecraft.minecraftnew.subsystems.pets.PetManager;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashSet;
import java.util.Set;

public class SecretLegion implements Listener {

    private final PetManager petManager;
    private final Set<PigZombie> summonedPiglins = new HashSet<>();
    private final JavaPlugin plugin;

    public SecretLegion(JavaPlugin plugin) {
        this.petManager = PetManager.getInstance(plugin);
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerDamaged(EntityDamageByEntityEvent event) {
        // Check if the entity being damaged is a player
        if (!(event.getEntity() instanceof Player player)) return;

        // Get the attacker
        Entity attacker = event.getDamager();

        // Get the player's active pet
        PetManager.Pet activePet = petManager.getActivePet(player);

        // Check if the player has the SECRET_LEGION perk
        if (activePet != null && activePet.hasPerk(PetManager.PetPerk.SECRET_LEGION)) {
            // Summon a Zombified Piglin
            PigZombie pigman = (PigZombie) player.getWorld().spawnEntity(
                    player.getLocation().add(0, 1, 0), // Spawn slightly above the player
                    EntityType.ZOMBIFIED_PIGLIN
            );

            // Set custom properties for the pigman
            pigman.setCustomName(ChatColor.GOLD + "Secret Legion Ally");
            pigman.setCustomNameVisible(true);
            pigman.setTarget(attacker instanceof LivingEntity ? (LivingEntity) attacker : null);
            pigman.setAngry(true);

            // Equip the pigman with full gold armor
            pigman.getEquipment().setHelmet(new ItemStack(Material.GOLDEN_HELMET));
            pigman.getEquipment().setChestplate(new ItemStack(Material.GOLDEN_CHESTPLATE));
            pigman.getEquipment().setLeggings(new ItemStack(Material.GOLDEN_LEGGINGS));
            pigman.getEquipment().setBoots(new ItemStack(Material.GOLDEN_BOOTS));
            pigman.getEquipment().setItemInMainHand(new ItemStack(Material.GOLDEN_SWORD));
            pigman.getEquipment().setHelmetDropChance(0.0f);
            pigman.getEquipment().setChestplateDropChance(0.0f);
            pigman.getEquipment().setLeggingsDropChance(0.0f);
            pigman.getEquipment().setBootsDropChance(0.0f);
            pigman.getEquipment().setItemInMainHandDropChance(0.0f);

            // Apply Speed III effect
            pigman.addPotionEffect(new PotionEffect(
                    PotionEffectType.SPEED, // Effect type
                    20 * 30,                // Duration (30 seconds)
                    2,                      // Amplifier (Speed III = 2)
                    false,                  // Ambient
                    false                   // Particles
            ));

            // Add the pigman to the tracking set
            summonedPiglins.add(pigman);

            // Schedule removal of the pigman after 30 seconds
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                if (pigman.isValid()) {
                    pigman.remove();
                }
                summonedPiglins.remove(pigman);
            }, 20 * 10); // 30 seconds in ticks

            // Notify the player
            player.setSaturation(player.getSaturation()-2);
            // Play a sound effect for summoning
            player.playSound(player.getLocation(), Sound.ENTITY_ZOMBIE_VILLAGER_CONVERTED, 1.0f, 1.0f);
        }
    }

    // Clean up all remaining piglins on plugin disable
    public void cleanupSummonedPiglins() {
        for (PigZombie piglin : summonedPiglins) {
            if (piglin.isValid()) {
                piglin.remove();
            }
        }
        summonedPiglins.clear();
    }
}
