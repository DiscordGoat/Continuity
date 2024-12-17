package goat.minecraft.minecraftnew.subsystems.combat;

import goat.minecraft.minecraftnew.MinecraftNew;
import goat.minecraft.minecraftnew.subsystems.pets.PetManager;
import goat.minecraft.minecraftnew.utils.CustomItemManager;
import goat.minecraft.minecraftnew.utils.ItemRegistry;
import org.bukkit.Particle;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Random;

public class RareCombatDrops implements Listener {
    PetManager petManager = PetManager.getInstance(MinecraftNew.getInstance());

    ItemStack infernalLooting = ItemRegistry.getInfernalLooting();
    ItemStack infernalUnbreaking = ItemRegistry.getInfernalUnbreaking();
    ItemStack infernalDepthStrider = ItemRegistry.getInfernalDepthStrider();
    ItemStack infernalBaneofAnthropods = ItemRegistry.getInfernalBaneofAnthropods();
    ItemStack infernalEfficiency = ItemRegistry.getInfernalEfficiency();
    ItemStack infernalFireAspect = ItemRegistry.getInfernalFireAspect();
    ItemStack infernalSharpness = ItemRegistry.getInfernalSharpness();
    ItemStack infernalSmite = ItemRegistry.getInfernalSmite();
    ItemStack infernalLure = ItemRegistry.getInfernalLure();

    ItemStack undeadDrop = ItemRegistry.getUndeadDrop();
    ItemStack creeperDrop = ItemRegistry.getCreeperDrop();
    ItemStack spiderDrop = ItemRegistry.getSpiderDrop();
    ItemStack enderDrop = ItemRegistry.getEnderDrop();
    ItemStack blazeDrop = ItemRegistry.getBlazeDrop();
    ItemStack witchDrop = ItemRegistry.getWitchDrop();
    ItemStack witherSkeletonDrop = ItemRegistry.getWitherSkeletonDrop();
    ItemStack guardianDrop = ItemRegistry.getGuardianDrop();
    ItemStack elderGuardianDrop = ItemRegistry.getElderGuardianDrop();
    ItemStack pillagerDrop = ItemRegistry.getPillagerDrop();
    ItemStack vindicatorDrop = ItemRegistry.getVindicatorDrop();
    ItemStack piglinDrop = ItemRegistry.getPiglinDrop();
    ItemStack piglinBruteDrop = ItemRegistry.getPiglinBruteDrop();
    ItemStack zombifiedPiglinDrop = ItemRegistry.getZombifiedPiglinDrop();
    ItemStack drownedDrop = ItemRegistry.getDrownedDrop();
    ItemStack skeletonDrop = ItemRegistry.getSkeletonDrop();

    private Random random = new Random();
    public CustomItemManager customItemManager = new CustomItemManager();
    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        LivingEntity entity = event.getEntity();
        EntityType type = entity.getType();
        if (entity.getKiller() instanceof Player) {
            Player player = event.getEntity().getKiller();
            switch (type) {
                case WITHER_SKELETON:
                    if (rollChance(1, 100)) { // 4% chance
                        event.getDrops().add(infernalSmite);
                    }
                    handleWitherSkeletonDrop(event); // Ensure this method is defined
                    break;

                case ZOMBIFIED_PIGLIN:
                    if (rollChance(1, 300)) { // 4% chance
                        event.getDrops().add(infernalLooting);
                    }
                    if (rollChance(1, 200)) { // 4% chance
                        petManager.createPet(player, "Zombie Pigman", PetManager.Rarity.LEGENDARY, 100, Particle.ASH, PetManager.PetPerk.BLACKLUNG, PetManager.PetPerk.SPEED_BOOST, PetManager.PetPerk.SECRET_LEGION, PetManager.PetPerk.DEVOUR, PetManager.PetPerk.FIREPROOF);
                    }
                    handleZombifiedPiglinDrop(event); // Ensure this method is defined
                    break;

                case PIGLIN:
                    if (rollChance(1, 50)) { // 3% chance
                        event.getDrops().add(infernalFireAspect);
                    }
                    handlePiglinDrop(event); // Ensure this method is defined
                    break;

                case PIGLIN_BRUTE:
                    if (rollChance(1, 2)) { // 2% chance
                        event.getDrops().add(infernalSharpness);
                    }
                    break;

                case MAGMA_CUBE:
                    if (rollChance(1, 100)) { // 3% chance
                        event.getDrops().add(infernalUnbreaking);
                    }
                    break;

                case GHAST:
                    if (rollChance(1, 25)) { // 3% chance
                        event.getDrops().add(infernalEfficiency);
                    }
                    break;
                case HOGLIN:
                    if (rollChance(1, 50)) { // 5% chance
                        event.getDrops().add(infernalBaneofAnthropods);
                    }
                    break;
                case STRIDER:
                    if (rollChance(1, 5)) { // 5% chance
                        event.getDrops().add(infernalDepthStrider);
                    }
                    break;

                case BLAZE:
                    if (rollChance(1, 200)) { // 3% chance
                        event.getDrops().add(infernalLure);
                    }
                    if (rollChance(1, 300)) { // 3% chance
                        petManager.createPet(player, "Blaze", PetManager.Rarity.LEGENDARY, 100, Particle.ASH, PetManager.PetPerk.BLACKLUNG, PetManager.PetPerk.FIREPROOF, PetManager.PetPerk.FLIGHT);
                    }

                    handleBlazeDrop(event); // Ensure this method is defined
                    break;

                case ZOMBIE:
                    handleZombieDrop(event);
                    break;
                case SKELETON:
                    handleSkeletonDrop(event);
                    break;
                case CREEPER:
                    handleCreeperDrop(event);
                    break;
                case SPIDER:
                    handleSpiderDrop(event); // Ensure this method is defined
                    break;
                case CAVE_SPIDER:
                    handleSpiderDrop(event);
                    break;
                case ENDERMAN:
                    handleEndermanDrop(event); // Ensure this method is defined
                    break;
                case WITCH:
                    handleWitchDrop(event); // Ensure this method is defined
                    break;
                case GUARDIAN:
                    handleGuardianDrop(event); // Ensure this method is defined
                    break;
                case ELDER_GUARDIAN:
                    handleElderGuardianDrop(event); // Ensure this method is defined
                    break;
                case SHULKER:
                    handleEndermanDrop(event);
                    break;
                case EVOKER, VINDICATOR, PILLAGER, RAVAGER:
                    handleIlligerDrop(event); // Ensure this method is defined
                    break;
                case SLIME:
                    handleVindicatorDrop(event); // Ensure this method is defined
                    break;

                case HUSK:
                    handleZombieDrop(event); // Ensure this method is defined
                    break;

                case STRAY:
                    handleStrayDrop(event); // Ensure this method is defined
                    break;
                case DROWNED:
                    handleDrownedDrop(event); // Ensure this method is defined
                    break;
                default:
                    break;
            }
        }
    }


    private void handleIlligerDrop(EntityDeathEvent event) {
        if (rollChance(1,200)) { // 1-4% chance
            event.getDrops().add(piglinDrop);
        }
        if (rollChance(1,200)) { // 1-4% chance
            petManager.createPet(event.getEntity().getKiller(), "Vindicator", PetManager.Rarity.LEGENDARY, 100, Particle.FIREWORKS_SPARK, PetManager.PetPerk.SPEED_BOOST, PetManager.PetPerk.SKEPTICISM, PetManager.PetPerk.GREED, PetManager.PetPerk.ELITE);
        }

    }
    private void handleStrayDrop(EntityDeathEvent event) {
        if (rollChance(1,360)) { // 1-4% chance
            event.getDrops().add(undeadDrop);
        }
        if (rollChance(1,200)) { // 1-4% chance
            petManager.createPet(event.getEntity().getKiller(), "Stray", PetManager.Rarity.LEGENDARY, 100, Particle.WHITE_ASH, PetManager.PetPerk.SHOTCALLING, PetManager.PetPerk.RECOVERY, PetManager.PetPerk.QUICK_DRAW, PetManager.PetPerk.TIPPED_SLOWNESS, PetManager.PetPerk.BONE_COLD);
        }

    }
    private void handleZombieDrop(EntityDeathEvent event) {
        if (rollChance(1,250)) { // 1-4% chance
            event.getDrops().add(undeadDrop);
        }
        if (rollChance(1,100)) { // 1-4% chance
            petManager.createPet(event.getEntity().getKiller(), "Zombie", PetManager.Rarity.RARE, 100, Particle.CRIT_MAGIC, PetManager.PetPerk.SECOND_WIND, PetManager.PetPerk.DEVOUR, PetManager.PetPerk.ECHOLOCATION);
        }

    }
    private void handleSkeletonDrop(EntityDeathEvent event) {
        if (rollChance(1,250)) { // 1-4% chance
            event.getDrops().add(skeletonDrop);
        }
        if (rollChance(1,100)) { // 1-4% chance
            petManager.createPet(event.getEntity().getKiller(), "Skeleton", PetManager.Rarity.UNCOMMON, 100, Particle.WHITE_ASH, PetManager.PetPerk.SHOTCALLING, PetManager.PetPerk.BONE_PLATING_WEAK);
        }

    }
    private void handleCreeperDrop(EntityDeathEvent event) {
        if (rollChance(1,25)) { // 1-4% chance
            event.getDrops().add(creeperDrop);
        }
    }
    private void handleSpiderDrop(EntityDeathEvent event) {
        if (rollChance(1,250)) { // 1-4% chance
            event.getDrops().add(spiderDrop);
        }
    }
    private void handleEndermanDrop(EntityDeathEvent event) {
        if (rollChance(1,75)) { // 1-4% chance
            event.getDrops().add(enderDrop);
        }
        if (rollChance(1,100)) { // 1-4% chance
            petManager.createPet(event.getEntity().getKiller(), "Enderman", PetManager.Rarity.LEGENDARY, 100, Particle.ASH, PetManager.PetPerk.ELITE, PetManager.PetPerk.ASPECT_OF_THE_END, PetManager.PetPerk.COLLECTOR);
        }

    }
    private void handleBlazeDrop(EntityDeathEvent event) {
        if (rollChance(1,100)) { // 1-4% chance
            event.getDrops().add(blazeDrop);
        }
    }
    private void handleWitchDrop(EntityDeathEvent event) {
        if (rollChance(1,100)) { // 1-4% chance
            event.getDrops().add(witchDrop);
        }
    }
    private void handleWitherSkeletonDrop(EntityDeathEvent event) {
        if (rollChance(1,100)) { // 1-4% chance
            event.getDrops().add(witherSkeletonDrop);
        }
        if (rollChance(1,200)) { // 1-4% chance
            petManager.createPet(event.getEntity().getKiller(), "Wither Skeleton", PetManager.Rarity.LEGENDARY, 100, Particle.ASH, PetManager.PetPerk.BLACKLUNG, PetManager.PetPerk.SPEED_BOOST, PetManager.PetPerk.DEVOUR, PetManager.PetPerk.FIREPROOF, PetManager.PetPerk.DECAY);
        }

    }
    private void handleGuardianDrop(EntityDeathEvent event) {
        if (rollChance(1,4)) { // 1-4% chance
            event.getDrops().add(guardianDrop);
        }
        if (rollChance(1,10)) { // 1-4% chance
            petManager.createPet(event.getEntity().getKiller(), "Guardian", PetManager.Rarity.EPIC, 100, Particle.WHITE_ASH, PetManager.PetPerk.SHOTCALLING, PetManager.PetPerk.RECOVERY, PetManager.PetPerk.LASER_BEAM);

        }

    }
    private void handleElderGuardianDrop(EntityDeathEvent event) {
        if (rollChance(1,2)) { // 1-4% chance
            event.getDrops().add(elderGuardianDrop);
        }
    }
    private void handleVindicatorDrop(EntityDeathEvent event) {
        if (rollChance(1,25)) { // 1-4% chance
            event.getDrops().add(vindicatorDrop);
        }
    }
    private void handlePiglinDrop(EntityDeathEvent event) {
        if (rollChance(1,25)) { // 1-4% chance
            event.getDrops().add(piglinDrop);
        }
    }
    private void handleZombifiedPiglinDrop(EntityDeathEvent event) {
        if (rollChance(1,100)) { // 1-4% chance
            event.getDrops().add(zombifiedPiglinDrop);
        }
    }
    private void handleDrownedDrop(EntityDeathEvent event) {
        if (rollChance(1,30)) {
            event.getDrops().add(drownedDrop);
        }
        if (rollChance(1,100)) {
            PetManager petManager = PetManager.getInstance(MinecraftNew.getInstance());
            petManager.createPet(event.getEntity().getKiller(), "Drowned", PetManager.Rarity.EPIC, 100, Particle.DAMAGE_INDICATOR, PetManager.PetPerk.WATERLOGGED, PetManager.PetPerk.STRONG_SWIMMER, PetManager.PetPerk.DEVOUR);
        }
    }
    /**
     * Determines whether to drop the item based on a percentage range.
     *
     * @return true if the item should drop, false otherwise
     */

    private boolean rollChance(int numerator, int denominator) {
        return random.nextInt(denominator) < numerator;
    }

    /**
     * Creates a custom ItemStack. Replace this with your custom item creation logic.
     *
     * @param material The material of the item
     * @param name     The display name of the item
     * @return The custom ItemStack
     */
}
