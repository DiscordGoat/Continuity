package goat.minecraft.minecraftnew.subsystems.combat;

import goat.minecraft.minecraftnew.MinecraftNew;
import goat.minecraft.minecraftnew.subsystems.pets.PetManager;
import goat.minecraft.minecraftnew.subsystems.utils.CustomItemManager;
import org.bukkit.ChatColor;
import org.bukkit.Particle;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Material;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;
import java.util.Random;

public class RareCombatDrops implements Listener {


    ItemStack infernalLooting = CustomItemManager.createCustomItem(Material.GOLD_BLOCK, ChatColor.GOLD +
            "Midas Gold", Arrays.asList(
            ChatColor.GRAY + "A hellish material that inspires replication.",
            ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Apply it to equipment to unlock the secrets of Looting V.",
            ChatColor.AQUA + "Mythical Enchantment"
    ), 1,false, true);
    ItemStack infernalUnbreaking = CustomItemManager.createCustomItem(Material.BEDROCK, ChatColor.GOLD +
            "Unbreakable", Arrays.asList(
            ChatColor.GRAY + "A hellish material that inspires Unbreakability.",
            ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Apply it to equipment to unlock the secrets of Unbreaking V.",
            ChatColor.AQUA + "Mythical Enchantment"
    ), 1,false, true);
    ItemStack infernalDepthStrider = CustomItemManager.createCustomItem(Material.GOLDEN_BOOTS, ChatColor.GOLD +
            "LavaStride", Arrays.asList(
            ChatColor.GRAY + "A hellish material that inspires the ocean's current.",
            ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Apply it to equipment to unlock the secrets of Depth Strider V.",
            ChatColor.AQUA + "Mythical Enchantment"
    ), 1,false, true);
    ItemStack infernalBaneofAnthropods = CustomItemManager.createCustomItem(Material.COBWEB, ChatColor.GOLD +
            "Extinction", Arrays.asList(
            ChatColor.GRAY + "A hellish material that inspires lethal options against Anthropods.",
            ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Apply it to equipment to unlock the secrets of Bane of Anthropods VII.",
            ChatColor.AQUA + "Mythical Enchantment"
    ), 1,false, true);
    ItemStack infernalEfficiency = CustomItemManager.createCustomItem(Material.OBSIDIAN, ChatColor.GOLD +
            "Weak Spot", Arrays.asList(
            ChatColor.GRAY + "A hellish material that inspires lethal options against Blocks.",
            ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Apply it to equipment to unlock the secrets of Bane of Efficiency VI.",
            ChatColor.AQUA + "Mythical Enchantment"
    ), 1,false, true);
    ItemStack infernalFireAspect = CustomItemManager.createCustomItem(Material.LAVA_BUCKET, ChatColor.GOLD +
            "Hellfire", Arrays.asList(
            ChatColor.GRAY + "A hellish material that inspires Fire.",
            ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Apply it to equipment to unlock the secrets of Fire Aspect IV.",
            ChatColor.AQUA + "Mythical Enchantment"
    ), 1,false, true);
    ItemStack infernalSharpness = CustomItemManager.createCustomItem(Material.IRON_SWORD, ChatColor.GOLD +
            "Shrapnel", Arrays.asList(
            ChatColor.GRAY + "A hellish material that inspires lethal options against mobs.",
            ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Apply it to equipment to unlock the secrets of Sharpness VII.",
            ChatColor.AQUA + "Mythical Enchantment"
    ), 1,false, true);
    ItemStack infernalSmite = CustomItemManager.createCustomItem(Material.WITHER_SKELETON_SKULL, ChatColor.GOLD +
            "Cure", Arrays.asList(
            ChatColor.GRAY + "A hellish material that inspires lethal options against Undead.",
            ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Apply it to equipment to unlock the secrets of Smite VII.",
            ChatColor.AQUA + "Mythical Enchantment"
    ), 1,false, true);
    ItemStack infernalLure = CustomItemManager.createCustomItem(Material.HEART_OF_THE_SEA, ChatColor.GOLD +
            "Howl", Arrays.asList(
            ChatColor.GRAY + "A hellish material that inspires fish hunger.",
            ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Apply it to equipment to unlock the secrets of Lure V.",
            ChatColor.AQUA + "Mythical Enchantment"
    ), 1,false, true);









    ItemStack undeadDrop = CustomItemManager.createCustomItem(Material.ROTTEN_FLESH, ChatColor.YELLOW +
            "Beating Heart", Arrays.asList(
            ChatColor.GRAY + "An undead heart still beating with undead life.",
            ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Apply it to equipment to unlock the secrets of Smite.",
            ChatColor.DARK_PURPLE + "Smithing Item"
    ), 1,false, true);

    ItemStack creeperDrop = CustomItemManager.createCustomItem(Material.TNT, ChatColor.YELLOW +
            "Hydrogen Bomb", Arrays.asList(
            ChatColor.GRAY + "500 KG of TNT.",
            ChatColor.BLUE + "Use: " + ChatColor.GRAY + "to summon a large quantity of live-fuse TNT.",
            ChatColor.DARK_PURPLE + "Artifact"
    ), 1,false, true);
    ItemStack spiderDrop = CustomItemManager.createCustomItem(Material.SPIDER_EYE, ChatColor.YELLOW +
            "SpiderBane", Arrays.asList(
            ChatColor.GRAY + "A strange substance lethal against spiders.",
            ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Apply it to equipment to unlock the secrets of Bane of Anthropods.",
            ChatColor.DARK_PURPLE + "Smithing Item"
    ), 1,false, true);
    ItemStack enderDrop = CustomItemManager.createCustomItem(Material.ENDER_PEARL, ChatColor.YELLOW +
            "End Pearl", Arrays.asList(
            ChatColor.GRAY + "Something doesn't look normal here...",
            ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Reusable ender pearl.",
            ChatColor.DARK_PURPLE + "Artifact"
    ), 1,false, true);
    ItemStack blazeDrop = CustomItemManager.createCustomItem(Material.FIRE_CHARGE, ChatColor.YELLOW +
            "Fire Ball", Arrays.asList(
            ChatColor.GRAY + "A projectile ball of fire.",
            ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Apply it to equipment to unlock the secrets of Flame.",
            ChatColor.DARK_PURPLE + "Smithing Item"
    ), 1,false, true);
    ItemStack witchDrop = CustomItemManager.createCustomItem(Material.ENCHANTED_BOOK, ChatColor.YELLOW +
            "Mending", Arrays.asList(
            ChatColor.GRAY + "An extremely rare enchantment.",
            ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Apply it to equipment to unlock the secrets of Mending.",
            ChatColor.DARK_PURPLE + "Smithing Item"
    ), 1,false, false);
    ItemStack witherSkeletonDrop = CustomItemManager.createCustomItem(Material.WITHER_SKELETON_SKULL, ChatColor.YELLOW +
            "Wither Skeleton Skull", Arrays.asList(
            ChatColor.GRAY + "A cursed skull.",
            ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Used in spawning the Wither.",
            ChatColor.DARK_PURPLE + "Summoning Item"
    ), 1,false, true);
    ItemStack guardianDrop = CustomItemManager.createCustomItem(Material.PRISMARINE_SHARD, ChatColor.YELLOW +
            "Rain", Arrays.asList(
            ChatColor.GRAY + "A strange object.",
            ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Used in summoning Rain.",
            ChatColor.DARK_PURPLE + "Artifact"
    ), 1,false, true);
    ItemStack elderGuardianDrop = CustomItemManager.createCustomItem(Material.ICE, ChatColor.YELLOW +
            "Frost Heart", Arrays.asList(
            ChatColor.GRAY + "A rare object.",
            ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Apply it to equipment to unlock the secrets of Frost Walker.",
            ChatColor.DARK_PURPLE + "Smithing Item"
    ), 1,false, true);
    ItemStack pillagerDrop = CustomItemManager.createCustomItem(Material.IRON_BLOCK, ChatColor.YELLOW +
            "Iron Golem", Arrays.asList(
            ChatColor.GRAY + "Ancient Summoning Artifact.",
            ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Summon an Iron Golem.",
            ChatColor.DARK_PURPLE + "Summoning Artifact"
    ), 1,false, true);
    ItemStack vindicatorDrop = CustomItemManager.createCustomItem(Material.SLIME_BALL, ChatColor.YELLOW +
            "KB Ball", Arrays.asList(
            ChatColor.GRAY + "An extremely bouncy ball.",
            ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Apply it to equipment to unlock the secrets of Knockback.",
            ChatColor.DARK_PURPLE + "Smithing Item"
    ), 1,false, true);
    ItemStack piglinDrop = CustomItemManager.createCustomItem(Material.ARROW, ChatColor.YELLOW +
            "High Caliber Arrow", Arrays.asList(
            ChatColor.GRAY + "A heavy arrow.",
            ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Apply it to equipment to unlock the secrets of Piercing.",
            ChatColor.DARK_PURPLE + "Smithing Item"
    ), 1,false, true);
    ItemStack piglinBruteDrop = CustomItemManager.createCustomItem(Material.SOUL_SOIL, ChatColor.YELLOW +
            "Grains of Soul", Arrays.asList(
            ChatColor.GRAY + "Soul soil with spirits of speed.",
            ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Apply it to equipment to unlock the secrets of Soul Speed.",
            ChatColor.DARK_PURPLE + "Smithing Item"
    ), 1,false, true);
    ItemStack zombifiedPiglinDrop = CustomItemManager.createCustomItem(Material.GOLD_INGOT, ChatColor.YELLOW +
            "Gold Bar", Arrays.asList(
            ChatColor.GRAY + "High value magnet.",
            ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Apply it to equipment to unlock the secrets of Looting.",
            ChatColor.DARK_PURPLE + "Smithing Item"
    ), 1,false, true);
    ItemStack drownedDrop = CustomItemManager.createCustomItem(Material.LEATHER_BOOTS, ChatColor.YELLOW +
            "Fins", Arrays.asList(
            ChatColor.GRAY + "Water Technology.",
            ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Apply it to equipment to unlock the secrets of Depth Strider.",
            ChatColor.DARK_PURPLE + "Smithing Item"
    ), 1,false, true);
    ItemStack skeletonDrop = CustomItemManager.createCustomItem(Material.BOW, ChatColor.YELLOW +
            "Bowstring", Arrays.asList(
            ChatColor.GRAY + "Air Technology.",
            ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Apply it to equipment to unlock the secrets of Power.",
            ChatColor.DARK_PURPLE + "Smithing Item"
    ), 1,false, true);

    private Random random = new Random();
    public CustomItemManager customItemManager = new CustomItemManager();
    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        LivingEntity entity = event.getEntity();
        EntityType type = entity.getType();

        switch (type) {
            case WITHER_SKELETON:
                if (rollChance(1,100)) { // 4% chance
                    event.getDrops().add(infernalSmite);
                }
                handleWitherSkeletonDrop(event); // Ensure this method is defined
                break;

            case ZOMBIFIED_PIGLIN:
                if (rollChance(1,300)) { // 4% chance
                    event.getDrops().add(infernalLooting);
                }
                handleZombifiedPiglinDrop(event); // Ensure this method is defined
                break;

            case PIGLIN:
                if (rollChance(1,50)) { // 3% chance
                    event.getDrops().add(infernalFireAspect);
                }
                handlePiglinDrop(event); // Ensure this method is defined
                break;

            case PIGLIN_BRUTE:
                if (rollChance(1,2)) { // 2% chance
                    event.getDrops().add(infernalSharpness);
                }
                break;

            case MAGMA_CUBE:
                if (rollChance(1,100)) { // 3% chance
                    event.getDrops().add(infernalUnbreaking);
                }
                break;

            case GHAST:
                if (rollChance(1,25)) { // 3% chance
                    event.getDrops().add(infernalEfficiency);
                }
                break;
            case HOGLIN:
                if (rollChance(1,50)) { // 5% chance
                    event.getDrops().add(infernalBaneofAnthropods);
                }
                break;
            case STRIDER:
                if (rollChance(1,5)) { // 5% chance
                    event.getDrops().add(infernalDepthStrider);
                }
                break;

            case BLAZE:
                if (rollChance(1,200)) { // 3% chance
                    event.getDrops().add(infernalLure);
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
            case PILLAGER:
                handlePillagerDrop(event); // Ensure this method is defined
                break;
            case SLIME:
                handleVindicatorDrop(event); // Ensure this method is defined
                break;

            case HUSK:
                handleZombieDrop(event); // Ensure this method is defined
                break;
            case STRAY:
                handleZombieDrop(event); // Ensure this method is defined
                break;
            case DROWNED:
                handleDrownedDrop(event); // Ensure this method is defined
                break;
            default:
                break;
        }
    }


    private void handleZombieDrop(EntityDeathEvent event) {
        if (rollChance(1,360)) { // 1-4% chance
            event.getDrops().add(undeadDrop);
        }
    }
    private void handleSkeletonDrop(EntityDeathEvent event) {
        if (rollChance(1,480)) { // 1-4% chance
            event.getDrops().add(skeletonDrop);
        }
    }
    private void handleCreeperDrop(EntityDeathEvent event) {
        if (rollChance(1,25)) { // 1-4% chance
            event.getDrops().add(creeperDrop);
        }
    }
    private void handleSpiderDrop(EntityDeathEvent event) {
        if (rollChance(1,360)) { // 1-4% chance
            event.getDrops().add(spiderDrop);
        }
    }
    private void handleEndermanDrop(EntityDeathEvent event) {
        if (rollChance(1,75)) { // 1-4% chance
            event.getDrops().add(enderDrop);
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
    }
    private void handleGuardianDrop(EntityDeathEvent event) {
        if (rollChance(1,4)) { // 1-4% chance
            event.getDrops().add(guardianDrop);
        }
    }
    private void handleElderGuardianDrop(EntityDeathEvent event) {
        if (rollChance(1,2)) { // 1-4% chance
            event.getDrops().add(elderGuardianDrop);
        }
    }
    private void handlePillagerDrop(EntityDeathEvent event) {
        if (rollChance(1,100)) { // 1-4% chance
            event.getDrops().add(pillagerDrop);
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
