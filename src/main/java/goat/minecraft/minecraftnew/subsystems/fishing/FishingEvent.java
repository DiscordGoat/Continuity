package goat.minecraft.minecraftnew.subsystems.fishing;

import goat.minecraft.minecraftnew.MinecraftNew;
import goat.minecraft.minecraftnew.subsystems.combat.HostilityManager;
import goat.minecraft.minecraftnew.subsystems.enchanting.CustomEnchantmentManager;
import goat.minecraft.minecraftnew.subsystems.pets.PetManager;
import goat.minecraft.minecraftnew.utils.ItemRegistry;
import goat.minecraft.minecraftnew.utils.SpawnMonsters;
import goat.minecraft.minecraftnew.utils.XPManager;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.*;

import static goat.minecraft.minecraftnew.subsystems.fishing.SeaCreature.createDyedLeatherArmor;



public class FishingEvent implements Listener {
    private final MinecraftNew plugin = MinecraftNew.getInstance();
    private final XPManager xpManager = new XPManager(plugin);
    private final Random random = new Random();
    private static final String SEA_CREATURE_METADATA = "SEA_CREATURE";
    private static final double SEA_CREATURE_REFORGE_BONUS = 4.0; // 4% bonus if reforged for sea creatures
    @EventHandler(priority = EventPriority.HIGHEST)

    public void onPlayerFish(PlayerFishEvent e) {
        //Bukkit.broadcastMessage("Luck chance = " + (0.05 + ((double) getLuckOfTheSeaLevel(e.getPlayer().getItemInUse()) /100)));
        if (e.getState() == PlayerFishEvent.State.CAUGHT_ENTITY) {
            Entity caught = e.getCaught();
            if (caught != null && caught.hasMetadata(SEA_CREATURE_METADATA)) {
                handleSeaCreatureCatch(caught, e.getPlayer());
            }
        } else if (e.getState() == PlayerFishEvent.State.CAUGHT_FISH) {
            handleRegularFishCatch(e);
        }
    }

    /**
     * Handles the logic when a player catches a sea creature.
     *
     * @param caught The caught sea creature entity.
     * @param player The player who caught the sea creature.
     */
    private void handleSeaCreatureCatch(Entity caught, Player player) {
        String creatureName = caught.getMetadata(SEA_CREATURE_METADATA).get(0).asString();
        Optional<SeaCreature> optionalSeaCreature = SeaCreatureRegistry.getSeaCreatureByName(creatureName);
        if (!optionalSeaCreature.isPresent()) return;

        SeaCreature seaCreature = optionalSeaCreature.get();
        int level = xpManager.getPlayerLevel(player, "Fishing"); // Get player's fishing level

        // Award base XP
        xpManager.addXP(player, "Fishing", 20);

        // Award boosted XP based on sea creature rarity
        int boostedXP = getBoostedXP(seaCreature.getRarity());
        xpManager.addXP(player, "Fishing", boostedXP);

        player.sendMessage(ChatColor.AQUA + "You caught a " + seaCreature.getColoredDisplayName() + " (" + seaCreature.getRarity().name() + ")!");
        player.sendMessage(ChatColor.GREEN + "Bonus Fishing XP: " + boostedXP);
        player.playSound(player.getLocation(), Sound.ENTITY_FISHING_BOBBER_RETRIEVE, 1.0f, 1.0f);

        // Remove the caught entity
        caught.remove();

        // Grant Luck effect
        grantLuck(player, "Fishing");
    }
    private void handleRegularFishCatch(PlayerFishEvent e) {
        Player player = e.getPlayer();
        int fishingLevel = xpManager.getPlayerLevel(player, "Fishing"); // Get player's fishing level
        double seaCreatureChance = 0;

        // Add fishing level bonus
        seaCreatureChance += fishingLevel / 2.0;

        // Add "Call of the Void" enchantment bonus
        int callOfTheVoidLevel = CustomEnchantmentManager.getEnchantmentLevel(player.getInventory().getItemInMainHand(), "Call of the Void");
        seaCreatureChance += callOfTheVoidLevel;

        // Check for reforged items for sea creatures
        if (isReforgedForSeaCreatures(player.getInventory().getItemInMainHand())) {
            seaCreatureChance += 4; // +4% if the item is reforged
        }

        // Check active pet perks
        PetManager petManager = PetManager.getInstance(plugin);
        PetManager.Pet activePet = petManager.getActivePet(player);
        if (activePet != null) {

            if (activePet.hasPerk(PetManager.PetPerk.ANGLER)) {
                int anglerBonus = 5; // Scales up to +5% at level 100
                seaCreatureChance += anglerBonus;
            }

            if (activePet.hasPerk(PetManager.PetPerk.HEART_OF_THE_SEA)) {
                int heartOfTheSeaBonus = 10; // Scales up to +10% at level 100
                seaCreatureChance += heartOfTheSeaBonus;
            }

            if (activePet.hasPerk(PetManager.PetPerk.BAIT)) {
                seaCreatureChance += (double) activePet.getLevel() / 10;; // +10% from Heart of the Sea perk
            }
        }

        // Convert to a decimal for probability
        seaCreatureChance /= 100.0;

        // Award base XP
        xpManager.addXP(player, "Fishing", 20);

        // Determine if a sea creature should replace this catch
        if (random.nextDouble() <= seaCreatureChance) {
            spawnAndLaunchSeaCreature(player, e.getHook().getLocation());
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR,
                    //String.format("%.2f", damageMultiplier)
                    new TextComponent(ChatColor.DARK_AQUA + "Sea Creature Chance: " + Math.round(seaCreatureChance * 100) + "%")
            );
        } else {
            // Proceed with regular fish catch
            awardRegularFish(player, fishingLevel);
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR,
                    //String.format("%.2f", damageMultiplier)
                    new TextComponent(ChatColor.DARK_AQUA + "Sea Creature Chance: " + Math.round(seaCreatureChance * 100) + "%")
            );
        }

        // Implement the enhanced treasure system
        rollForTreasure(player);

        // Grant Luck effect based on fishing level
        grantLuck(player, "Fishing");
    }

    private boolean isReforgedForSeaCreatures(ItemStack item) {
        if (item == null) {
            return false;
        }
        ItemMeta meta = item.getItemMeta();
        if (meta != null && meta.hasLore()) {
            for (String lore : meta.getLore()) {
                if (ChatColor.stripColor(lore).equals("Talisman: Sea Creature Chance")) {
                    //Bukkit.broadcastMessage("WORKS");
                    return true;
                }
            }
        }
        return false;
    }
    /**
     * Awards a regular fish to the player and handles double fish logic.
     *
     * @param player The player who caught the fish.
     * @param level  The player's fishing level.
     */
    private void awardRegularFish(Player player, int level) {
        // Chance to receive double fish based on player's fishing level
        int rollDoubleFish = random.nextInt(100) + 1;
        if (rollDoubleFish <= level) { // Higher fishing level increases chance
            Material fishType = random.nextBoolean() ? Material.COD : Material.SALMON;
            ItemStack fish = new ItemStack(fishType); // Randomly choose between COD and SALMON
            player.getInventory().addItem(fish);
            player.sendMessage(ChatColor.AQUA + "You caught an extra fish!");
            player.playSound(player.getLocation(), Sound.ENTITY_FISHING_BOBBER_SPLASH, 1.0f, 1.0f);
        }
    }
    private int getAdjustedSeaCreatureLevel(int baseLevel, int hostilityLevel) {
        double multiplier = 1.0 + (hostilityLevel / 10.0);
        return (int) Math.round(baseLevel * multiplier);
    }
    /**
     * Spawns a sea creature and launches it towards the player.
     *
     * @param player         The player towards whom the sea creature will be launched.
     * @param bobberLocation The location of the fishing bobber.
     */
    private void spawnAndLaunchSeaCreature(Player player, Location bobberLocation) {
        PetManager petManager = PetManager.getInstance(plugin);
        Optional<SeaCreature> optionalSeaCreature = SeaCreatureRegistry.getRandomSeaCreature();
        if (!optionalSeaCreature.isPresent()) return;

        SeaCreature seaCreature = optionalSeaCreature.get();
        EntityType entityType = seaCreature.getEntityType();



        // Log sea creature stats to the console
        Bukkit.getLogger().info("Sea Creature Stats:");
        Bukkit.getLogger().info("Name: " + seaCreature.getDisplayName());
        Bukkit.getLogger().info("Rarity: " + seaCreature.getRarity());
        Bukkit.getLogger().info("Level: " + seaCreature.getLevel());

        // Spawn the sea creature at the bobber's location
        Entity spawnedEntity = player.getWorld().spawnEntity(bobberLocation, entityType);
        LivingEntity livingEntity = (LivingEntity) spawnedEntity;
        // Retrieve the creature's custom base64 texture
        String skullName = seaCreature.getSkullName();
        ItemStack helmet = petManager.getSkullForPet(seaCreature.getSkullName());
        ItemStack chest = createDyedLeatherArmor(Material.LEATHER_CHESTPLATE, seaCreature.getArmorColor());
        ItemStack legs = createDyedLeatherArmor(Material.LEATHER_LEGGINGS, seaCreature.getArmorColor());
        ItemStack boots = createDyedLeatherArmor(Material.LEATHER_BOOTS, seaCreature.getArmorColor());

        livingEntity.getEquipment().setHelmet(helmet);
        livingEntity.getEquipment().setChestplate(chest);
        livingEntity.getEquipment().setLeggings(legs);
        livingEntity.getEquipment().setBoots(boots);

        livingEntity.getEquipment().setHelmetDropChance(0);
        livingEntity.getEquipment().setChestplateDropChance(0);
        livingEntity.getEquipment().setLeggingsDropChance(0);
        livingEntity.getEquipment().setBootsDropChance(0);
        ((LivingEntity) spawnedEntity).getEquipment().setItemInOffHandDropChance(1);
        if(seaCreature.getSkullName().equals("Pirate")){
            ((LivingEntity) spawnedEntity).getEquipment().setItemInOffHand(ItemRegistry.getRandomTreasure());
            ((LivingEntity) spawnedEntity).getEquipment().setItemInOffHandDropChance(1);
        }
        if(seaCreature.getSkullName().equals("Poseidon")){
            ((LivingEntity) spawnedEntity).getEquipment().setItemInOffHand(ItemRegistry.getTrident());

        }
        livingEntity.setSwimming(true);
        livingEntity.setSilent(true);

        HostilityManager hostilityManager = HostilityManager.getInstance(plugin);
        int hostilityLevel = hostilityManager.getPlayerDifficultyTier(player);
        plugin.getLogger().info("Hostility Level for player " + player.getName() + ": " + hostilityLevel);

        SpawnMonsters spawnMonsters = new SpawnMonsters(xpManager);
        int baseLevel = seaCreature.getLevel();
        plugin.getLogger().info("Base Level of Sea Creature: " + baseLevel);

        spawnMonsters.applyMobAttributes(livingEntity, baseLevel);

        spawnedEntity.setCustomName(ChatColor.AQUA + "[Lvl " + baseLevel + "] " + seaCreature.getColoredDisplayName());
        spawnedEntity.setCustomNameVisible(true);
        // Attach metadata with the sea creature's name
        spawnedEntity.setMetadata("SEA_CREATURE", new FixedMetadataValue(MinecraftNew.getInstance(), seaCreature.getDisplayName()));
        if (spawnedEntity.hasMetadata("SEA_CREATURE")) {
            Bukkit.getLogger().info("Metadata successfully applied to entity: " + spawnedEntity.getName());
        } else {
            Bukkit.getLogger().warning("Metadata application failed for entity: " + spawnedEntity.getName());
        }

        // Calculate the direction vector from sea creature to player
        Vector direction = player.getLocation().subtract(bobberLocation).toVector().normalize();

        // Modify the vertical component to yank the sea creature upwards
        direction.setY(0.2); // Adjust vertical force as needed

        // Multiply the vector to control overall speed towards the player
        direction.multiply(2); // Adjust speed multiplier as needed

        // Set the sea creature's velocity towards the player, including upward motion
        spawnedEntity.setVelocity(direction);

        player.sendMessage("A " + seaCreature.getColoredDisplayName() + " is approaching!");
        playSplashSound(player, seaCreature.getRarity());

        // Play particles at the bobber location
        bobberLocation.getWorld().spawnParticle(Particle.WATER_SPLASH, bobberLocation, 30, 0.5, 0.5, 0.5, 0.1);
        bobberLocation.getWorld().playSound(bobberLocation, Sound.ENTITY_FISHING_BOBBER_SPLASH, 1.0f, 1.0f);

        ((LivingEntity) spawnedEntity).addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 2, false));
    }


    private void playSplashSound(Player player, Rarity rarity) {
        // Adjusted to include more sounds based on rarity
        Sound sound;
        float volume = 1.0f;
        float pitch = 1.0f;

        switch (rarity) {
            case COMMON, UNCOMMON:
                sound = Sound.ENTITY_DOLPHIN_SPLASH;
                break;
            case RARE:
                sound = Sound.ENTITY_DOLPHIN_SPLASH;
                break;
            case EPIC:
                sound = Sound.ENTITY_ELDER_GUARDIAN_AMBIENT;
                volume = 1.5f;
                pitch = 0.8f;
                break;
            case LEGENDARY:
                sound = Sound.ENTITY_ELDER_GUARDIAN_AMBIENT;
                volume = 2.0f;
                pitch = 0.6f;
                break;
            case MYTHIC:
                sound = Sound.ENTITY_ELDER_GUARDIAN_AMBIENT;
                volume = 2.0f;
                pitch = 0.5f;
                break;
            default:
                sound = Sound.ENTITY_ELDER_GUARDIAN_AMBIENT;
                break;
        }

        player.playSound(player.getLocation(), sound, volume, pitch);
    }

    private int getBoostedXP(Rarity rarity) {
        switch (rarity) {
            case COMMON:
                return 100;
            case UNCOMMON:
                return 200;
            case RARE:
                return 400;
            case EPIC:
                return 800;
            case LEGENDARY:
                return 1600;
            case MYTHIC:
                return 3200;
            default:
                return 10;
        }
    }

    /**
     * Grants a Luck potion effect to the player based on their fishing level.
     *
     * @param player The player to receive the effect.
     * @param skill  The skill name (e.g., "Fishing").
     */
    private void grantLuck(Player player, String skill) {
        int level = xpManager.getPlayerLevel(player, skill); // Get player's fishing level
        int chance = random.nextInt(100) + 1;

        // Check if random roll is successful based on player's level
        if (chance >= 90) {
            int luckDuration = level * 5 * 20; // Duration in ticks (20 ticks = 1 second)

            // Add Luck potion effect for a short duration
            player.addPotionEffect(new PotionEffect(PotionEffectType.LUCK, luckDuration, 0)); // Level 1 Luck effect
            player.sendMessage(ChatColor.GREEN + "You feel lucky!");
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_BREATH, 1.0f, 1.0f);
        }
    }
    public int getLuckOfTheSeaLevel(ItemStack item) {
        // Check if the item is not null and has the Luck of the Sea enchantment
        if (item != null && item.containsEnchantment(Enchantment.LUCK)) {
            return item.getEnchantmentLevel(Enchantment.LUCK);
        }
        // Return 0 if no Luck of the Sea enchantment is found
        return 0;
    }
    /**
     * Implements the enhanced treasure system.
     *
     * @param player The player who is fishing.
     */
    private void rollForTreasure(Player player) {
        PetManager petManager = PetManager.getInstance(plugin);
        double treasureChance = 0.05; // Base chance of 5%
        int petLevel = 1;
        if(petManager.getActivePet(player) != null) {
            petLevel = petManager.getActivePet(player).getLevel();
        }
        // Check if the player has the Treasure Hunter perk
        if (petManager.getActivePet(player) != null && petManager.getActivePet(player).hasPerk(PetManager.PetPerk.TREASURE_HUNTER)) {
            treasureChance += (petLevel * 0.0045); // Scale to add 0.45 at pet level 100
        }

        // Add bonus from "Piracy" enchantment
        int piracyLevel = CustomEnchantmentManager.getEnchantmentLevel(player.getInventory().getItemInMainHand(), "Piracy");
        treasureChance += piracyLevel / 100.0; // 1% bonus per level of Piracy

        // Roll for treasure
        if (random.nextDouble() <= treasureChance) {
            ItemStack treasure = getRandomTreasure(player);

            if (treasure != null) {
                player.getInventory().addItem(treasure);
                player.sendMessage(ChatColor.GOLD + "You fished up a treasure!");
                player.playSound(player.getLocation(), Sound.ITEM_TRIDENT_RETURN, 1.0f, 1.0f);

                // Play particle effect
                player.getWorld().spawnParticle(Particle.VILLAGER_HAPPY, player.getLocation(), 30, 1, 1, 1, 0.1);
            }
        }
    }


    /**
     * Returns a random treasure item from the treasure loot table.
     *
     * @return An ItemStack representing the treasure item.
     */
    public ItemStack getRandomTreasure(Player player) {
        // Define a loot table with items and their weights
        List<LootItem> lootTable = Arrays.asList(
                new LootItem(new ItemStack(Material.NAUTILUS_SHELL), 10),
                new LootItem(new ItemStack(Material.SADDLE), 25),
                new LootItem(new ItemStack(Material.DIAMOND, random.nextInt(10) + 1), 10),
                new LootItem(new ItemStack(Material.EMERALD, 64), 15),
                new LootItem(new ItemStack(Material.ANCIENT_DEBRIS), 5),
                new LootItem(new ItemStack(Material.ENCHANTED_GOLDEN_APPLE), 3),
                new LootItem(new ItemStack(Material.TOTEM_OF_UNDYING), 13),
                new LootItem(new ItemStack(Material.HEART_OF_THE_SEA), 5),
                new LootItem(new ItemStack(Material.SHULKER_SHELL), 12),
                new LootItem(new ItemStack(Material.SPONGE), 8),
                new LootItem(new ItemStack(Material.SCUTE), 7),
                new LootItem(new ItemStack(Material.WITHER_SKELETON_SKULL), 4),
                new LootItem(new ItemStack(Material.CREEPER_HEAD), 11),
                new LootItem(new ItemStack(Material.ZOMBIE_HEAD), 12),
                new LootItem(new ItemStack(Material.SKELETON_SKULL), 10),
                new LootItem(new ItemStack(Material.NETHER_WART, random.nextInt(3) + 1), 10),
                new LootItem(new ItemStack(Material.ENDER_EYE), 5),
                new LootItem(new ItemStack(Material.EXPERIENCE_BOTTLE, 64), 7),
                new LootItem(new ItemStack(Material.MUSIC_DISC_13), 1),
                new LootItem(new ItemStack(Material.MUSIC_DISC_CAT), 1),
                new LootItem(new ItemStack(Material.MUSIC_DISC_BLOCKS), 1),
                new LootItem(new ItemStack(Material.MUSIC_DISC_CHIRP), 1),
                new LootItem(new ItemStack(Material.MUSIC_DISC_FAR), 1),
                new LootItem(new ItemStack(Material.MUSIC_DISC_MALL), 1),
                new LootItem(new ItemStack(Material.MUSIC_DISC_MELLOHI), 1),
                new LootItem(new ItemStack(Material.MUSIC_DISC_STAL), 1),
                new LootItem(new ItemStack(Material.MUSIC_DISC_STRAD), 1),
                new LootItem(new ItemStack(Material.MUSIC_DISC_WARD), 1),
                new LootItem(new ItemStack(Material.MUSIC_DISC_11), 1),
                new LootItem(new ItemStack(Material.MUSIC_DISC_WAIT), 1),
                new LootItem(new ItemStack(Material.MUSIC_DISC_PIGSTEP), 1),
                new LootItem(new ItemStack(Material.MUSIC_DISC_OTHERSIDE), 1),
                new LootItem(new ItemStack(Material.MUSIC_DISC_RELIC), 1),
                new LootItem(new ItemStack(Material.MUSIC_DISC_5), 1),
                new LootItem(getRandomSeaCreatureAlchemyItem(), 15),
                new LootItem(getRandomRareSapling(), 10),
                new LootItem(getMessageInABottle(player), 5)
        );


        // Calculate the total weight
        int totalWeight = lootTable.stream().mapToInt(LootItem::getWeight).sum();

        // Roll for treasure
        int roll = random.nextInt(totalWeight);
        int cumulativeWeight = 0;

        for (LootItem lootItem : lootTable) {
            cumulativeWeight += lootItem.getWeight();
            if (roll < cumulativeWeight) {
                return lootItem.getItem();
            }
        }

        return null; // Should not reach here
    }

    /**
     * Returns a random sea creature alchemy item.
     *
     * @return An ItemStack representing the alchemy item.
     */
    private ItemStack getRandomSeaCreatureAlchemyItem() {
        // Get all sea creatures
        List<SeaCreature> seaCreatures = SeaCreatureRegistry.getSeaCreatures();
        if (seaCreatures.isEmpty()) return null;

        // Choose a random sea creature
        SeaCreature randomCreature = seaCreatures.get(random.nextInt(seaCreatures.size()));

        // Get the drops from the random sea creature
        List<ItemStack> drops = randomCreature.getDrops();

        // If there are no drops, return null
        if (drops.isEmpty()) return null;

        // Return a random item from the drops
        return drops.get(random.nextInt(drops.size()));
    }


    /**
     * Returns a random rare sapling.
     *
     * @return An ItemStack representing the sapling.
     */
    private ItemStack getRandomRareSapling() {
        Material[] rareSaplings = {
                Material.ACACIA_SAPLING,
                Material.DARK_OAK_SAPLING,
                Material.JUNGLE_SAPLING,
                Material.BIRCH_SAPLING,
                Material.SPRUCE_SAPLING,
                Material.OAK_SAPLING
        };
        Material saplingType = rareSaplings[random.nextInt(rareSaplings.length)];
        return new ItemStack(saplingType, random.nextInt(3) + 1);
    }

    /**
     * Creates a map pointing to the nearest structure.
     *
     * @param player The player who will receive the map.
     * @return An ItemStack representing the map.
     */
    public ItemStack getMessageInABottle(Player player) {
        // Define the structures to look for
        StructureType[] structures = {
                StructureType.STRONGHOLD,
                StructureType.OCEAN_MONUMENT,
                StructureType.DESERT_PYRAMID,
                StructureType.VILLAGE,
                StructureType.JUNGLE_PYRAMID,
                StructureType.WOODLAND_MANSION,
                StructureType.BURIED_TREASURE,
                StructureType.IGLOO,
                StructureType.MINESHAFT,
                StructureType.PILLAGER_OUTPOST,
                StructureType.SWAMP_HUT
        };

        // Select a random StructureType from the array
        StructureType selectedStructure = structures[random.nextInt(structures.length)];

        // Get player's current location and world
        Location playerLocation = player.getLocation();
        World world = playerLocation.getWorld();

        if (world == null) {
            player.sendMessage(ChatColor.RED + "Error: World not found.");
            return null;
        }

        // Locate the nearest structure of the selected type within a 100,000 block radius
        Location structureLocation = world.locateNearestStructure(playerLocation, selectedStructure, 100000, true);

        if (structureLocation != null) {
            // Create the "Message in a Bottle" paper item
            ItemStack paperItem = new ItemStack(Material.PAPER);
            ItemMeta paperMeta = paperItem.getItemMeta();

            if (paperMeta != null) {
                // Set the display name with color
                paperMeta.setDisplayName(ChatColor.AQUA + "Message in a Bottle");

                // Prepare the lore with structure details
                List<String> lore = Arrays.asList(
                        ChatColor.GOLD + "Structure Type: " + ChatColor.WHITE + selectedStructure.getName(),
                        ChatColor.GOLD + "Coordinates:",
                        ChatColor.WHITE + "X: " + structureLocation.getBlockX(),
                        ChatColor.WHITE + "Y: " + structureLocation.getBlockY(),
                        ChatColor.WHITE + "Z: " + structureLocation.getBlockZ()
                );

                paperMeta.setLore(lore);
                paperItem.setItemMeta(paperMeta);
                return paperItem;
            }
        } else {
            // Inform the player that no structure was found
            player.sendMessage(ChatColor.RED + "No " + selectedStructure.getName().toLowerCase().replace("_", " ") + " found within 100,000 blocks.");
        }

        return null;
    }

    /**
     * Returns a random spawn egg.
     *
     * @return An ItemStack representing the spawn egg.
     */
    

    /**
     * Helper class to represent a loot item and its weight.
     */
    public static class LootItem {
        private final ItemStack item;
        private final int weight;

        public LootItem(ItemStack item, int weight) {
            this.item = item;
            this.weight = weight;
        }

        public ItemStack getItem() {
            return item;
        }

        public int getWeight() {
            return weight;
        }
    }

}
