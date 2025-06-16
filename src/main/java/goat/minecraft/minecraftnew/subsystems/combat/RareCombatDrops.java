package goat.minecraft.minecraftnew.subsystems.combat;

import goat.minecraft.minecraftnew.MinecraftNew;
import goat.minecraft.minecraftnew.subsystems.pets.PetManager;
import goat.minecraft.minecraftnew.subsystems.pets.PetRegistry;
import goat.minecraft.minecraftnew.utils.devtools.ItemRegistry;
import goat.minecraft.minecraftnew.utils.devtools.PlayerMeritManager;
import org.bukkit.ChatColor;
import org.bukkit.Particle;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Random;

public class RareCombatDrops implements Listener {



    PetManager petManager = PetManager.getInstance(MinecraftNew.getInstance());
    PlayerMeritManager playerMeritManager = PlayerMeritManager.getInstance(MinecraftNew.getInstance());

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
    ItemStack waterAspectEnchant = ItemRegistry.getWaterAspectEnchant();
    ItemStack elderGuardianDrop = ItemRegistry.getElderGuardianDrop();
    ItemStack pillagerDrop = ItemRegistry.getPillagerDrop();
    ItemStack vindicatorDrop = ItemRegistry.getVindicatorDrop();
    ItemStack piglinDrop = ItemRegistry.getPiglinDrop();
    ItemStack piglinBruteDrop = ItemRegistry.getPiglinBruteDrop();
    ItemStack zombifiedPiglinDrop = ItemRegistry.getZombifiedPiglinDrop();
    ItemStack drownedDrop = ItemRegistry.getDrownedDrop();
    ItemStack skeletonDrop = ItemRegistry.getSkeletonDrop();

    private Random random = new Random();

    /**
     * Adds a drop to the event while applying the Master Thief perk logic.
     * If the player has the perk, there is a 50% chance the item amount is doubled.
     */
    private void addRareDrop(Player player, EntityDeathEvent event, ItemStack item) {
        ItemStack drop = item.clone();
        if (playerMeritManager.hasPerk(player.getUniqueId(), "Master Thief") && random.nextBoolean()) {
            drop.setAmount(2);
        }
        if (isSoulItem(drop) && playerMeritManager.hasPerk(player.getUniqueId(), "Reaper")) {
            drop.setAmount(drop.getAmount() * 2);
        }
        event.getDrops().add(drop);
    }

    private boolean isSoulItem(ItemStack item) {
        if (item == null || !item.hasItemMeta() || !item.getItemMeta().hasLore()) return false;
        List<String> lore = item.getItemMeta().getLore();
        if (lore == null) return false;
        for (String line : lore) {
            if (ChatColor.stripColor(line).equals("Soul Item")) {
                return true;
            }
        }
        return false;
    }
    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {

        LivingEntity entity = event.getEntity();
        EntityType type = entity.getType();
        if (entity.getKiller() instanceof Player) {
            Player player = event.getEntity().getKiller();

            HostilityManager hostilityManager = HostilityManager.getInstance(MinecraftNew.getInstance());
            assert player != null;
            int hostilityLevel = hostilityManager.getPlayerDifficultyTier(player);
            PetRegistry petRegistry = new PetRegistry();

            if (rollChance(1, 25, hostilityLevel)) {
                addRareDrop(player, event, ItemRegistry.getRandomSoulItem());
            }
            switch (type) {
                case WITHER_SKELETON:
                    if (rollChance(1, 100, hostilityLevel)) { // 4% chance
                        addRareDrop(player, event, infernalSmite);
                    }
                    handleWitherSkeletonDrop(event); // Ensure this method is defined
                    break;

                case ZOMBIFIED_PIGLIN:
                    if (rollChance(1, 150, hostilityLevel)) { // 4% chance
                        addRareDrop(player, event, infernalLooting);
                    }
                    if (rollChance(1, 100, hostilityLevel)) { // 4% chance
                        petRegistry.addPetByName(player, "Zombie Pigman");
                    }
                    handleZombifiedPiglinDrop(event); // Ensure this method is defined
                    break;

                case PIGLIN:
                    if (rollChance(1, 50, hostilityLevel)) { // 3% chance
                        addRareDrop(player, event, infernalFireAspect);
                    }
                    handlePiglinDrop(event); // Ensure this method is defined
                    break;

                case PIGLIN_BRUTE:
                    if (rollChance(1, 2, hostilityLevel)) { // 2% chance
                        addRareDrop(player, event, infernalSharpness);
                    }
                    break;

                case MAGMA_CUBE:
                    if (rollChance(1, 75, hostilityLevel)) { // 3% chance
                        addRareDrop(player, event, infernalUnbreaking);
                    }
                    break;

                case GHAST:
                    if (rollChance(1, 10, hostilityLevel)) { // 3% chance
                        addRareDrop(player, event, infernalEfficiency);
                    }
                    break;
                case HOGLIN:
                    if (rollChance(1, 15, hostilityLevel)) { // 5% chance
                        addRareDrop(player, event, infernalBaneofAnthropods);
                    }
                    break;
                case STRIDER:
                    if (rollChance(1, 5, hostilityLevel)) { // 5% chance
                        addRareDrop(player, event, infernalDepthStrider);
                    }
                    break;

                case BLAZE:
                    if (rollChance(1, 100, hostilityLevel)) { // 3% chance
                        petRegistry.addPetByName(player, "Blaze");
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
        Player player = event.getEntity().getKiller();
        PetRegistry petRegistry = new PetRegistry();
        HostilityManager hostilityManager = HostilityManager.getInstance(MinecraftNew.getInstance());
        int hostilityLevel = hostilityManager.getPlayerDifficultyTier(player);
        if (rollChance(1,50, hostilityLevel)) { // 1-4% chance
            addRareDrop(player, event, piglinDrop);
        }
        if (rollChance(1,4, hostilityLevel)) { // 1-4% chance
            petRegistry.addPetByName(player, "Vindicator");
        }

    }
    private void handleStrayDrop(EntityDeathEvent event) {
        PetRegistry petRegistry = new PetRegistry();
        Player player = event.getEntity().getKiller();
        HostilityManager hostilityManager = HostilityManager.getInstance(MinecraftNew.getInstance());
        int hostilityLevel = hostilityManager.getPlayerDifficultyTier(player);
        if (rollChance(1,200, hostilityLevel)) { // 1-4% chance
            addRareDrop(player, event, undeadDrop);
        }
        if (rollChance(1,2, hostilityLevel)) { // 1-4% chance
            petRegistry.addPetByName(player, "Stray");
        }

    }
    private void handleZombieDrop(EntityDeathEvent event) {
        Player player = event.getEntity().getKiller();
        PetRegistry petRegistry = new PetRegistry();
        HostilityManager hostilityManager = HostilityManager.getInstance(MinecraftNew.getInstance());
        int hostilityLevel = hostilityManager.getPlayerDifficultyTier(player);
        if (rollChance(1,100, hostilityLevel)) { // 1-4% chance
            addRareDrop(player, event, undeadDrop);
        }
        if (rollChance(1,100, hostilityLevel)) { // 1-4% chance
            petRegistry.addPetByName(player, "Zombie");
        }


    }
    private void handleSkeletonDrop(EntityDeathEvent event) {
        Player player = event.getEntity().getKiller();
        PetRegistry petRegistry = new PetRegistry();
        HostilityManager hostilityManager = HostilityManager.getInstance(MinecraftNew.getInstance());
        int hostilityLevel = hostilityManager.getPlayerDifficultyTier(player);
        if (rollChance(1,100, hostilityLevel)) { // 1-4% chance
            addRareDrop(player, event, skeletonDrop);
        }
        if (rollChance(1,100, hostilityLevel)) { // 1-4% chance
            petRegistry.addPetByName(player, "Skeleton");
        }

    }
    private void handleCreeperDrop(EntityDeathEvent event) {
        Player player = event.getEntity().getKiller();
        HostilityManager hostilityManager = HostilityManager.getInstance(MinecraftNew.getInstance());
        int hostilityLevel = hostilityManager.getPlayerDifficultyTier(player);
        if (rollChance(1,100, hostilityLevel)) { // 1-4% chance
            addRareDrop(player, event, creeperDrop);
        }
    }
    private void handleSpiderDrop(EntityDeathEvent event) {
        PetRegistry petRegistry = new PetRegistry();

        Player player = event.getEntity().getKiller();
        HostilityManager hostilityManager = HostilityManager.getInstance(MinecraftNew.getInstance());
        int hostilityLevel = hostilityManager.getPlayerDifficultyTier(player);
        if (rollChance(1,100, hostilityLevel)) { // 1-4% chance
            addRareDrop(player, event, spiderDrop);
        }
        if (rollChance(1,10, hostilityLevel)) { // 1-4% chance
            petRegistry.addPetByName(player, "Spider");
        }
    }
    private void handleEndermanDrop(EntityDeathEvent event) {
        Player player = event.getEntity().getKiller();
        PetRegistry petRegistry = new PetRegistry();
        HostilityManager hostilityManager = HostilityManager.getInstance(MinecraftNew.getInstance());
        int hostilityLevel = hostilityManager.getPlayerDifficultyTier(player);
        if (rollChance(1,25, hostilityLevel)) { // 1-4% chance
            addRareDrop(player, event, enderDrop);
        }
        if (rollChance(1,100, hostilityLevel)) { // 1-4% chance
            petRegistry.addPetByName(player, "Enderman");
        }

    }
    private void handleBlazeDrop(EntityDeathEvent event) {
        Player player = event.getEntity().getKiller();
        HostilityManager hostilityManager = HostilityManager.getInstance(MinecraftNew.getInstance());
        int hostilityLevel = hostilityManager.getPlayerDifficultyTier(player);
        if (rollChance(1,100, hostilityLevel)) { // 1-4% chance
            addRareDrop(player, event, blazeDrop);
        }
    }
    private void handleWitchDrop(EntityDeathEvent event) {
        Player player = event.getEntity().getKiller();
        HostilityManager hostilityManager = HostilityManager.getInstance(MinecraftNew.getInstance());
        int hostilityLevel = hostilityManager.getPlayerDifficultyTier(player);
        if (rollChance(1,100, hostilityLevel)) { // 1-4% chance
            addRareDrop(player, event, witchDrop);
        }
    }
    private void handleWitherSkeletonDrop(EntityDeathEvent event) {
        Player player = event.getEntity().getKiller();
        PetRegistry petRegistry = new PetRegistry();
        HostilityManager hostilityManager = HostilityManager.getInstance(MinecraftNew.getInstance());
        int hostilityLevel = hostilityManager.getPlayerDifficultyTier(player);
        if (rollChance(1,100, hostilityLevel)) { // 1-4% chance
            addRareDrop(player, event, witherSkeletonDrop);
        }
        if (rollChance(1,100, hostilityLevel)) { // 1-4% chance
            petRegistry.addPetByName(player, "Wither Skeleton");
        }

    }
    private void handleGuardianDrop(EntityDeathEvent event) {
        Player player = event.getEntity().getKiller();
        PetRegistry petRegistry = new PetRegistry();
        HostilityManager hostilityManager = HostilityManager.getInstance(MinecraftNew.getInstance());
        int hostilityLevel = hostilityManager.getPlayerDifficultyTier(player);
        if (rollChance(1,4, hostilityLevel)) { // 1-4% chance
            addRareDrop(player, event, guardianDrop);
        }
        if (random.nextInt(20) == 0) { // 5% base chance
            addRareDrop(player, event, waterAspectEnchant);
        }
        if (rollChance(1,1, hostilityLevel)) { // 1-4% chance
            petRegistry.addPetByName(player, "Guardian");
        }

    }
    private void handleElderGuardianDrop(EntityDeathEvent event) {
        Player player = event.getEntity().getKiller();
        HostilityManager hostilityManager = HostilityManager.getInstance(MinecraftNew.getInstance());
        int hostilityLevel = hostilityManager.getPlayerDifficultyTier(player);
        if (rollChance(1,2, hostilityLevel)) { // 1-4% chance
            addRareDrop(player, event, elderGuardianDrop);
        }
    }
    private void handleVindicatorDrop(EntityDeathEvent event) {
        Player player = event.getEntity().getKiller();
        HostilityManager hostilityManager = HostilityManager.getInstance(MinecraftNew.getInstance());
        int hostilityLevel = hostilityManager.getPlayerDifficultyTier(player);
        if (rollChance(1,25, hostilityLevel)) { // 1-4% chance
            addRareDrop(player, event, vindicatorDrop);
        }
    }
    private void handlePiglinDrop(EntityDeathEvent event) {
        Player player = event.getEntity().getKiller();
        HostilityManager hostilityManager = HostilityManager.getInstance(MinecraftNew.getInstance());
        int hostilityLevel = hostilityManager.getPlayerDifficultyTier(player);
        if (rollChance(1,25, hostilityLevel)) { // 1-4% chance
            addRareDrop(player, event, piglinDrop);
        }
    }
    private void handleZombifiedPiglinDrop(EntityDeathEvent event) {
        Player player = event.getEntity().getKiller();
        HostilityManager hostilityManager = HostilityManager.getInstance(MinecraftNew.getInstance());
        int hostilityLevel = hostilityManager.getPlayerDifficultyTier(player);
        if (rollChance(1,100, hostilityLevel)) { // 1-4% chance
            addRareDrop(player, event, zombifiedPiglinDrop);
        }
    }
    private void handleDrownedDrop(EntityDeathEvent event) {
        Player player = event.getEntity().getKiller();
        PetRegistry petRegistry = new PetRegistry();
        HostilityManager hostilityManager = HostilityManager.getInstance(MinecraftNew.getInstance());
        int hostilityLevel = hostilityManager.getPlayerDifficultyTier(player);
        if (rollChance(1,30, hostilityLevel)) {
            addRareDrop(player, event, drownedDrop);
        }

        if (rollChance(1,100, hostilityLevel)) {
            PetManager petManager = PetManager.getInstance(MinecraftNew.getInstance());
            petRegistry.addPetByName(player, "Drowned");
        }
    }
    /**
     * Determines whether to drop the item based on a percentage range.
     *
     * @return true if the item should drop, false otherwise
     */


    private boolean rollChance(int numerator, int baselineDenominator, int hostilityLevel) {
        // Calculate the adjusted denominator
        int adjustedDenominator = (int) (baselineDenominator * (10.0 / hostilityLevel));
        // Roll the chance using the adjusted denominator
        return random.nextInt(adjustedDenominator) < numerator;
    }

    /**
     * Creates a custom ItemStack. Replace this with your custom item creation logic.
     *
     * @param material The material of the item
     * @param name     The display name of the item
     * @return The custom ItemStack
     */
}
