package goat.minecraft.minecraftnew.subsystems.villagers;

import goat.minecraft.minecraftnew.MinecraftNew;
import goat.minecraft.minecraftnew.other.qol.ItemDisplayManager;
import goat.minecraft.minecraftnew.utils.devtools.ItemRegistry;
import goat.minecraft.minecraftnew.utils.devtools.Speech;
import goat.minecraft.minecraftnew.utils.devtools.XPManager;
import org.bukkit.*;
import org.bukkit.block.*;
import org.bukkit.block.data.Ageable;
import org.bukkit.block.data.type.Slab;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class VillagerWorkCycleManager implements Listener, CommandExecutor {

    private final JavaPlugin plugin;
    private static VillagerWorkCycleManager instance;


    private static final int WORK_CYCLE_TICKS = 5*20*60;

    // We will count down from WORK_CYCLE_TICKS to 0 every second.
    private int ticksUntilNextWorkCycle = WORK_CYCLE_TICKS;

    private VillagerWorkCycleManager(JavaPlugin plugin) {
        this.plugin = plugin;
        startGlobalScheduler();  // Start the new 1-second countdown
    }
    /**
 * Checks if a villager is eligible for work cycles.
 * Only villagers with custom names (green male names) are eligible.
 *
 * @param villager The villager to check
 * @return true if the villager is eligible for work cycles, false otherwise
 */
    public boolean isEligibleForWorkCycle(Villager villager) {
    // Check if villager has a custom name
    if(villager.getCustomName() != null){
        return true;
    } else {
        return false;
    }
}
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("forceworkcycle")) {
            // Set the work cycle timer to 20 ticks (1 second)
            if (!sender.hasPermission("continuity.admin")) {
                sender.sendMessage(ChatColor.RED + "You do not have permission to use this command!");
                return true;
            }
            ticksUntilNextWorkCycle = 100;
            return true;
        }
        return false;
    }
    public static VillagerWorkCycleManager getInstance(JavaPlugin plugin) {
        if (instance == null) {
            instance = new VillagerWorkCycleManager(plugin);
        }
        return instance;
    }

    /**
     * Starts a 1-second interval scheduler that counts down ticksUntilNextWorkCycle.
     */
    private void startGlobalScheduler() {
        new BukkitRunnable() {
            @Override
            public void run() {
                // Decrement the countdown by 20 (because this runs every 20 ticks = 1 second).
                ticksUntilNextWorkCycle -= 20;

                // Once we reach zero or below, run the cycle and reset the timer
                if (ticksUntilNextWorkCycle <= 0) {
                    runVillagerWorkCycle(); // your existing logic
                    ticksUntilNextWorkCycle = WORK_CYCLE_TICKS;
                }
            }
        }.runTaskTimer(plugin, 0L, 20L); // run every 20 ticks (1 second)
    }

    /**
     * Returns how many seconds remain before the next work cycle triggers.
     */
    public int getSecondsUntilNextWorkCycle() {
        // Convert the remaining ticks to seconds
        return ticksUntilNextWorkCycle / 20;
    }

    /**
     * Runs the actual villager work cycle logic (previously in startGlobalScheduler()).
     */
    private void runVillagerWorkCycle() {
        // Iterate over all villagers in all worlds
        for (Villager villager : plugin.getServer().getWorlds().stream()
                .flatMap(world -> world.getEntitiesByClass(Villager.class).stream())
                .toList()) {
            // Perform work for each villager
            if(!isEligibleForWorkCycle(villager)){
                return; // Skip non-eligible villagers
            }
            else{
                performVillagerWork(villager);
            }

        }
    }

    private void performVillagerWork(Villager villager) {
        Villager.Profession profession = villager.getProfession();
        int searchRadius = 10; // Configurable radius
        if(Bukkit.getOnlinePlayers().isEmpty()){
            Bukkit.getLogger().info("No players online, skipping work cycle for villagers");
            return; // No players online, do not perform work
        }

        switch (profession) {
            case FARMER -> performFarmerWork(villager);
            case BUTCHER -> performButcherWork(villager, searchRadius);
            case FISHERMAN -> performFishermanWork(villager, searchRadius);
            case LIBRARIAN -> performLibrarianWork(villager, searchRadius);
            case CLERIC -> performClericWork(villager);
            case CARTOGRAPHER -> performCartographerWork(villager, 20);
            case FLETCHER -> performFletcherWork(villager, 50);
            case LEATHERWORKER -> performLeatherworkerWork(villager, searchRadius);
            case MASON -> performMasonWork(villager, searchRadius);
            case SHEPHERD -> performShepherdWork(villager, searchRadius);
            case TOOLSMITH -> performToolsmithWork(villager, searchRadius);
            case WEAPONSMITH -> performWeaponsmithWork(villager, searchRadius);
            case ARMORER -> performArmorerWork(villager, 20);

            // Add other professions as needed
            default -> {
                // No action for other professions
            }
        }
    }

    // Farmers harvest nearby crops
    private void performFarmerWork(Villager villager) {
        // First check if there's a hay bale nearby within radius 10
        Block hayBale = findNearestBlock(villager, List.of(Material.HAY_BLOCK), 10);
        if (hayBale == null) {
            Speech speech = new Speech(plugin);
            speech.createText(villager.getLocation(), "If you place a hay bale nearby, I can harvest your crops for you.", 30);
            return; // No hay bale nearby, don't perform farming
        }

        int radius = 40; // Set the search radius to 40 blocks

        List<Material> harvestableCrops = List.of(
                Material.WHEAT,
                Material.CARROTS,
                Material.POTATOES,
                Material.BEETROOTS,
                Material.PUMPKIN,
                Material.MELON
        );

        List<Block> cropsToHarvest = findCropsInRadius(villager.getLocation(), harvestableCrops, radius);

        if (cropsToHarvest.isEmpty()) {
            return;
        }

        Map<Material, Integer> harvestYield = new HashMap<>();
        Random random = new Random();

        for (Block block : cropsToHarvest) {
            Material blockType = block.getType();
            Material itemType = null;
            int yield = 0;
            XPManager xpManager = new XPManager(MinecraftNew.getInstance());
            int playerBoost = xpManager.getPlayerLevel(getNearestPlayer(villager.getLocation()), "Farming");
            yield += playerBoost / 10;
            switch (blockType) {
                case WHEAT -> {
                    if (block.getBlockData() instanceof Ageable crop) {
                        if (crop.getAge() == crop.getMaximumAge()) {
                            // Harvest wheat
                            yield = 1 + random.nextInt(6); // Yield 1-2 wheat
                            itemType = Material.WHEAT;

                            // Also yield seeds
                            int seedYield = 1 + random.nextInt(2);
                            harvestYield.put(Material.WHEAT_SEEDS, harvestYield.getOrDefault(Material.WHEAT_SEEDS, 0) + seedYield);

                            // Reset crop growth
                            crop.setAge(0);
                            block.setBlockData(crop);
                        }
                    }
                }
                case CARROTS -> {
                    if (block.getBlockData() instanceof Ageable crop) {
                        if (crop.getAge() == crop.getMaximumAge()) {
                            yield = 2 + random.nextInt(3); // Yield 2-4 carrots
                            itemType = Material.CARROT;

                            crop.setAge(0);
                            block.setBlockData(crop);
                            Bukkit.getLogger().info("Resetting crop: CARROTS Yield: " + yield);
                        }
                    }
                }
                case POTATOES -> {
                    if (block.getBlockData() instanceof Ageable crop) {
                        if (crop.getAge() == crop.getMaximumAge()) {
                            yield = 2 + random.nextInt(3); // Yield 2-4 potatoes
                            itemType = Material.POTATO;

                            crop.setAge(0);
                            block.setBlockData(crop);
                            Bukkit.getLogger().info("Resetting crop: POTATOES Yield: " + yield);
                        }
                    }
                }
                case BEETROOTS -> {
                    if (block.getBlockData() instanceof Ageable crop) {
                        if (crop.getAge() == crop.getMaximumAge()) {
                            yield = 1 + random.nextInt(2); // Yield 1-2 beetroots
                            itemType = Material.BEETROOT;

                            // Also yield seeds
                            int seedYield = 1 + random.nextInt(2);
                            harvestYield.put(Material.BEETROOT_SEEDS, harvestYield.getOrDefault(Material.BEETROOT_SEEDS, 0) + seedYield);

                            crop.setAge(0);
                            block.setBlockData(crop);
                            Bukkit.getLogger().info("Resetting crop: BEETROOTS Yield: " + yield + ", Seeds: " + seedYield);
                        }
                    }
                }
                case PUMPKIN -> {
                    block.setType(Material.AIR);
                    yield = 1 + random.nextInt(2); // Yield 1-2 pumpkins
                    itemType = Material.PUMPKIN;
                    Bukkit.getLogger().info("Harvested PUMPKIN Yield: " + yield);
                }
                case MELON -> {
                    block.setType(Material.AIR);
                    yield = 3 + random.nextInt(5); // Yield 3-7 melon slices
                    itemType = Material.MELON_SLICE;
                    Bukkit.getLogger().info("Harvested MELON Yield: " + yield);
                }
                default -> Bukkit.getLogger().warning("Unexpected block type encountered: " + blockType);
            }

            if (itemType != null && yield > 0) {
                harvestYield.put(itemType, harvestYield.getOrDefault(itemType, 0) + yield);
            }
        }

        sendHarvestToChest(villager, harvestYield);
        Speech speech = new Speech(plugin);
        speech.createText(villager.getLocation(), "I harvested your nearby crops and put them in that chest!", 30);
        villager.getWorld().playSound(villager.getLocation(), Sound.BLOCK_ROOTED_DIRT_BREAK, 1.0f, 1.0f);
    }



    private List<Block> findCropsInRadius(Location center, List<Material> materials, int radius) {
        List<Block> blocks = new ArrayList<>();
        World world = center.getWorld();

        int minX = center.getBlockX() - radius;
        int maxX = center.getBlockX() + radius;
        int minY = Math.max(center.getBlockY() - 1, 0);
        int maxY = Math.min(center.getBlockY() + 2, world.getMaxHeight() - 1);
        int minZ = center.getBlockZ() - radius;
        int maxZ = center.getBlockZ() + radius;

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    Block block = world.getBlockAt(x, y, z);
                    if (materials.contains(block.getType())) {
                        blocks.add(block);
                    }
                }
            }
        }
        return blocks;
    }

    private void sendHarvestToChest(Villager villager, Map<Material, Integer> harvestYield) {
        Chest chestBlock = findNearestChest(villager, 10); // Adjust radius as needed
        if (chestBlock != null && chestBlock.getBlock().getState() instanceof Chest chest) {
            Inventory chestInventory = chest.getInventory();

            for (Map.Entry<Material, Integer> entry : harvestYield.entrySet()) {
                ItemStack itemStack = new ItemStack(entry.getKey(), entry.getValue());
                chestInventory.addItem(itemStack);
            }
            villager.getWorld().playSound(villager.getLocation(), Sound.ENTITY_ITEM_PICKUP, 1.0f, 1.0f);
        } else {
            // Drop items if no chest is found
            for (Map.Entry<Material, Integer> entry : harvestYield.entrySet()) {
                ItemStack itemStack = new ItemStack(entry.getKey(), entry.getValue());
                villager.getWorld().dropItemNaturally(villager.getLocation(), itemStack);
            }
        }
    }


    private void performButcherWork(Villager villager, int radius) {
        Block smoker = findNearestBlock(villager, List.of(Material.SMOKER), radius);
    
        if (smoker == null) {
            return;
        }
    
        Map<Material, Integer> harvestYield = new HashMap<>();
        Random random = new Random();
    
        // List of all edible foods
        Material[] edibleFoods = {
                Material.BAKED_POTATO, Material.COOKED_BEEF,
                Material.COOKED_CHICKEN, Material.COOKED_MUTTON,
                Material.COOKED_PORKCHOP, Material.COOKED_RABBIT,
                Material.DRIED_KELP
        };
    
        Material food;
        int yield;
    
        // Occasionally make a cake
        if (random.nextInt(100) < 1) {
            food = Material.CAKE;
            yield = 1; // Assuming one whole cake
        } else {
            food = edibleFoods[random.nextInt(edibleFoods.length)];
            yield = 4; // Always cook 4 items
        }
    
        harvestYield.put(food, yield); // Final yield

        storeOrDropHarvest(villager, harvestYield);
        Speech speech = new Speech(plugin);
        speech.createText(villager.getLocation(), "I cooked you some food.", 30);
        villager.getWorld().playSound(villager.getLocation(), Sound.BLOCK_FIRE_AMBIENT, 10, 10);
    }

    // Fishermen offer fish if there's water nearby
    private void performFishermanWork(Villager villager, int radius) {
        if (!isWaterNearby(villager, radius)) {
            Speech speech = new Speech(plugin);
            speech.createText(villager.getLocation(), "I can't fish here, but I can if theres some water nearby.", 30);
            return;
        }

        // Create a map to hold potential block influences
        List<Material> nearbyInfluenceBlocks = List.of(Material.BARREL, Material.LANTERN, Material.WATER);
        Map<Material, Integer> influenceMap = new HashMap<>();
        Set<Block> processedBlocks = new HashSet<>(); // To ensure each block is counted only once

        // Check for nearby influence blocks
        for (Material blockType : nearbyInfluenceBlocks) {
            Block influenceBlock = findNearestBlock(villager, List.of(blockType), radius);
            if (influenceBlock != null && !processedBlocks.contains(influenceBlock)) {
                influenceMap.put(blockType, influenceMap.getOrDefault(blockType, 0) + 1);
                processedBlocks.add(influenceBlock); // Mark block as processed
            }
        }

        Map<Material, Integer> harvestYield = new HashMap<>();
        Random random = new Random();

        // Offer fish items with chance for treasure and junk items
        Material[] fishTypes = {
                Material.COD,
                Material.SALMON,
                Material.INK_SAC,
                Material.PUFFERFISH,
                Material.TROPICAL_FISH,

        };
        Material[] treasureItems = {
                Material.TROPICAL_FISH,
                Material.PUFFERFISH,
                Material.NAUTILUS_SHELL,
                Material.SADDLE,
                Material.DIAMOND,
                Material.EMERALD,
                Material.ANCIENT_DEBRIS,
                Material.GOLD_INGOT,
                Material.ENCHANTED_GOLDEN_APPLE,
                Material.TOTEM_OF_UNDYING,
                Material.HEART_OF_THE_SEA,
                Material.SHULKER_SHELL,
                Material.TRIDENT,
                Material.SPONGE,
                Material.SCUTE,
                Material.WITHER_SKELETON_SKULL,
                Material.CREEPER_HEAD,
                Material.ZOMBIE_HEAD,
                Material.SKELETON_SKULL,
                Material.NETHER_WART,
                Material.EXPERIENCE_BOTTLE,
                Material.MUSIC_DISC_13,
                Material.MUSIC_DISC_CAT,
                Material.MUSIC_DISC_BLOCKS,
                Material.MUSIC_DISC_CHIRP,
                Material.MUSIC_DISC_FAR,
                Material.MUSIC_DISC_MALL,
                Material.MUSIC_DISC_MELLOHI,
                Material.MUSIC_DISC_STAL,
                Material.MUSIC_DISC_STRAD,
                Material.MUSIC_DISC_WARD,
                Material.MUSIC_DISC_11,
                Material.MUSIC_DISC_WAIT,
                Material.MUSIC_DISC_PIGSTEP,
                Material.MUSIC_DISC_OTHERSIDE,
                Material.MUSIC_DISC_RELIC,
                Material.MUSIC_DISC_5
        };
        Material[] junkItems = {
                Material.ROTTEN_FLESH,
                Material.STICK,
                Material.BONE,
        };

        int roll = random.nextInt(100);
        Material item;
        int quantity = 5;
        if (influenceMap.containsKey(Material.WATER)) {
            quantity += 2; // Increase yield if water is nearby
        }
        if (influenceMap.containsKey(Material.BARREL)) {
            quantity += 2; // Increase yield if a barrel is nearby
        }
        if (influenceMap.containsKey(Material.LANTERN)) {
            quantity += 2; // Increase yield if a lantern is nearby
        }
        if (roll < 5) {
            // 10% chance for treasure
            item = treasureItems[random.nextInt(treasureItems.length)];
            quantity = 1;
        } else if (roll < 5) {
            // 20% chance for junk
            item = junkItems[random.nextInt(junkItems.length)];
            quantity = 1 + random.nextInt(2);
        } else {
            // 70% chance for fish
            item = fishTypes[random.nextInt(fishTypes.length)];
            quantity = 1 + random.nextInt(3); // 1-3 items
        }

        // Modify yield based on nearby blocks


        harvestYield.put(item, quantity);
        Speech speech = new Speech(plugin);
        speech.createText(villager.getLocation(), "I caught you some fish, and a few pieces of junk.", 30);
        storeOrDropHarvest(villager, harvestYield);
        villager.getWorld().playSound(villager.getLocation(), Sound.ENTITY_FISHING_BOBBER_SPLASH, 1.0f, 1.0f);
    }

    private void performLibrarianWork(Villager villager, int radius) {
        // List of nearby influence blocks to check for
        List<Material> nearbyInfluenceBlocks = List.of(
                Material.BOOKSHELF, Material.ENCHANTING_TABLE, Material.LECTERN, Material.LANTERN, Material.GLASS
        );
        Map<Material, Integer> influenceMap = new HashMap<>();
        Set<Block> processedBlocks = new HashSet<>(); // To ensure each block (except bookshelves) is counted once

        // Check for nearby influence blocks and update influenceMap
        for (Material blockType : nearbyInfluenceBlocks) {
            List<Block> nearbyBlocks = findNearbyBlocks(villager, Collections.singletonList(blockType), radius); // Method to find all blocks within radius
            for (Block block : nearbyBlocks) {
                if (blockType == Material.BOOKSHELF || !processedBlocks.contains(block)) {
                    influenceMap.put(blockType, influenceMap.getOrDefault(blockType, 0) + 1);
                    processedBlocks.add(block); // Mark block as processed, except for bookshelves which can be counted multiple times
                }
            }
        }

        Random random = new Random();
        int bottlesProduced = 0;

        // Calculate the number of Bottles of Enchanting to produce based on influence
        if (influenceMap.getOrDefault(Material.BOOKSHELF, 0) > 0) {
            bottlesProduced += random.nextInt(3) + 1; // 1-3 bottles from Bookshelves
        }
        if (influenceMap.getOrDefault(Material.ENCHANTING_TABLE, 0) > 0) {
            bottlesProduced += random.nextInt(2) + 1; // 1-2 bottles from Enchanting Table
        }
        if (influenceMap.getOrDefault(Material.LECTERN, 0) > 0) {
            bottlesProduced += random.nextInt(3) + 1; // 1-3 bottles from Lectern
        }

        // Ensure at least one bottle is produced
        bottlesProduced = Math.max(bottlesProduced, 1);

        // Create the Bottles of Enchanting item
        ItemStack bottlesOfEnchanting = new ItemStack(Material.EXPERIENCE_BOTTLE, bottlesProduced);

        // Store or drop the bottles
        storeOrDropHarvestItemStack(villager, Collections.singletonMap(bottlesOfEnchanting, bottlesProduced));
        Speech speech = new Speech(plugin);
        speech.createText(villager.getLocation(), "I made you some Bottles of Enchanting!", 30);
        // Play sound for the work
        villager.getWorld().playSound(villager.getLocation(), Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1.0f, 1.0f);
    }


    // Armorers produce armor pieces if a blast furnace is nearby
    private List<ArmorStand> findNearbyArmorStands(Villager villager, int radius) {
        Location loc = villager.getLocation();
        List<ArmorStand> armorStands = new ArrayList<>();

        Collection<Entity> nearbyEntities = loc.getWorld().getNearbyEntities(loc, radius, radius, radius);
        for (Entity entity : nearbyEntities) {
            if (entity instanceof ArmorStand armorStand) {
                armorStands.add(armorStand);
            }
        }
        return armorStands;
    }
    public static Player getNearestPlayer(Location location) {
        Player nearestPlayer = null;
        double nearestDistanceSquared = Double.MAX_VALUE;

        for (Player player : Bukkit.getOnlinePlayers()) {
            double distanceSquared = location.distanceSquared(player.getLocation());
            if (distanceSquared < nearestDistanceSquared) {
                nearestDistanceSquared = distanceSquared;
                nearestPlayer = player;
            }
        }

        return nearestPlayer;
    }

    // Updated ArmorerWork method to repair armor on armor stands
    private void performArmorerWork(Villager villager, int radius) {
        // Search for nearby armor stands within the radius
        List<ArmorStand> armorStands = findNearbyArmorStands(villager, radius);

        if (armorStands.isEmpty()) {
            Speech speech = new Speech(plugin);
            speech.createText(villager.getLocation(), "If you want to display your armor on an armor stand nearby, I can repair it for you.", 30);
            return; // No armor stands nearby
        }

        // Map to hold gains per armor type for repair calculation
        Map<String, Integer> armorGains = Map.of(
                "LEATHER", 1,
                "GOLDEN", 2,
                "CHAINMAIL", 7,
                "IRON", 3,
                "DIAMOND", 4,
                "NETHERITE", 5
        );

        for (ArmorStand armorStand : armorStands) {
            ItemStack[] equipment = armorStand.getEquipment().getArmorContents();
            for (int i = 0; i < equipment.length; i++) {
                ItemStack item = equipment[i];
                if (item != null && (item.getType().name().toUpperCase().endsWith("_HELMET") ||
                        item.getType().name().toUpperCase().endsWith("_CHESTPLATE") ||
                        item.getType().name().toUpperCase().endsWith("_LEGGINGS") ||
                        item.getType().name().toUpperCase().endsWith("_BOOTS"))) {
                    XPManager xpManager = new XPManager(plugin);
                    int miningLevel = xpManager.getPlayerLevel(getNearestPlayer(villager), "Mining");
                    int repairAmount = miningLevel * 2;
                    Speech speech = new Speech(plugin);
                    speech.createText(villager.getLocation(), "I repaired your nearby armor for " + repairAmount + " for you.", 30);
                    // Repair the armor item
                    repairArmor(item, repairAmount);
                    // Update the modified item back to the equipment array
                    equipment[i] = item;
                    Location effectLocation = armorStand.getLocation().clone().add(0, 1, 0); // Adjust vertical offset as needed
                    villager.getWorld().spawnParticle(Particle.END_ROD, effectLocation, 10, 0.5, 0.5, 0.5, 0.05);
                }
            }

            // Update the armor stand with the modified equipment
            armorStand.getEquipment().setArmorContents(equipment);
            // Play sound to indicate repair
            villager.getWorld().playSound(villager.getLocation(), Sound.BLOCK_ANVIL_USE, 1.0f, 1.0f);
        }


        // Play sound to indicate repair action
        villager.getWorld().playSound(villager.getLocation(), Sound.BLOCK_ANVIL_USE, 1.0f, 1.0f);
    }
    private void repairArmor(ItemStack item, int repairAmount) {
        if (!(item.getItemMeta() instanceof org.bukkit.inventory.meta.Damageable)) {
            return;
        }

        // Clone the item to avoid direct modification issues
        ItemStack modifiedItem = item.clone();
        org.bukkit.inventory.meta.Damageable damageableMeta = (org.bukkit.inventory.meta.Damageable) modifiedItem.getItemMeta();
        int currentDamage = damageableMeta.getDamage();
        int newDamage = Math.max(currentDamage - repairAmount, 0); // Ensure new damage is not below zero

        // Apply new damage value
        damageableMeta.setDamage(newDamage);
        modifiedItem.setItemMeta(damageableMeta);

        // Update the original item stack
        item.setType(modifiedItem.getType());
        item.setItemMeta(modifiedItem.getItemMeta());

    }


    // Event for right-clicking on armor stands to transfer armor
    @EventHandler
    public void onPlayerInteractWithArmorStand(PlayerInteractAtEntityEvent event) {
        if (!(event.getRightClicked() instanceof ArmorStand armorStand)) {
            return; // Exit if the interacted entity is not an armor stand
        }
        if(!armorStand.isVisible()){
            return;
        }

        Player player = event.getPlayer();
        ItemStack[] playerArmor = player.getInventory().getArmorContents();
        ItemStack[] standArmor = armorStand.getEquipment().getArmorContents();

        boolean playerHasArmor = Arrays.stream(playerArmor).anyMatch(item -> item != null && item.getType() != Material.AIR);
        boolean armorStandHasArmor = Arrays.stream(standArmor).anyMatch(item -> item != null && item.getType() != Material.AIR);

        // Case 1: Player has armor and the armor stand is empty
        if (playerHasArmor && !armorStandHasArmor) {
            // Clone player armor and equip to the armor stand
            armorStand.getEquipment().setArmorContents(Arrays.stream(playerArmor)
                    .map(item -> item != null ? item.clone() : null)
                    .toArray(ItemStack[]::new));

            // Remove armor from player
            player.getInventory().setArmorContents(new ItemStack[4]);

            event.setCancelled(true);
            player.playSound(player.getLocation(), Sound.ITEM_ARMOR_EQUIP_GENERIC, 1.0f, 1.0f);
        }
        // Case 2: Player has no armor equipped and the armor stand has armor
        else if (!playerHasArmor && armorStandHasArmor) {
            // Clone armor from the armor stand and equip it to the player
            player.getInventory().setArmorContents(Arrays.stream(standArmor)
                    .map(item -> item != null ? item.clone() : null)
                    .toArray(ItemStack[]::new));

            // Clear armor on the armor stand
            armorStand.getEquipment().setArmorContents(new ItemStack[4]);

            event.setCancelled(true);
            player.playSound(player.getLocation(), Sound.ITEM_ARMOR_EQUIP_GENERIC, 1.0f, 1.0f);
        }
    }

    // Utility method to repair items

  private Player getNearestPlayer(Villager villager) {
    Player nearestPlayer = null;
    double nearestDistanceSquared = Double.MAX_VALUE;

    for (Player player : Bukkit.getOnlinePlayers()) {
        double distanceSquared = villager.getLocation().distanceSquared(player.getLocation());
        if (distanceSquared < nearestDistanceSquared) {
            nearestDistanceSquared = distanceSquared;
            nearestPlayer = player;
        }
    }

    return nearestPlayer;
}



    /**
     * Combines both ItemFrames and ItemDisplays to get all items on display
     *
     * @param villager The villager to search around
     * @param radius The radius to search within
     * @return Map of entities to their displayed items
     */

// Now update the repair methods to use the combined approach
    

    private void performWeaponsmithWork(Villager villager, int radius) {
        XPManager xpManager = new XPManager(plugin);
        // Get all displayed items (both frames and displays)
        Map<Entity, ItemStack> displayedItems = findAllDisplayedItems(villager, radius);

        // Fixed repair amount of 500 for all weapons
        Player nearestPlayer = getNearestPlayer(villager.getLocation());
        int combatLevel = xpManager.getPlayerLevel(nearestPlayer, "Combat");
        int repairAmount = combatLevel * 5;

        if(displayedItems.isEmpty()){
            Speech speech = new Speech(plugin);
            speech.createText(villager.getLocation(), "If you want, you can display your weapons nearby and I'll repair them.", 30);
            return;
        }
        for (Map.Entry<Entity, ItemStack> entry : displayedItems.entrySet()) {
            Entity entity = entry.getKey();
            ItemStack item = entry.getValue();
            Material type = item.getType();

            // Check if the item is a weapon or shield
            if (type.name().endsWith("_SWORD") ||
                    type.name().endsWith("_AXE") ||
                    type == Material.TRIDENT ||
                    type == Material.SHIELD ||
                    type == Material.BOW ||
                    type == Material.CROSSBOW) {

                // Repair the item
                repairWeapons(item, repairAmount);
                Speech speech = new Speech(plugin);
                speech.createText(villager.getLocation(), "I repaired your displayed weapons " + repairAmount + " for you.", 30);
                // Set the repaired item back
                if (entity instanceof ItemFrame) {
                    ((ItemFrame) entity).setItem(item);
                } else if (entity instanceof ItemDisplay) {
                    ((ItemDisplay) entity).setItemStack(item);
                }
            }
        }

        // Play sound to indicate repair action
        villager.getWorld().playSound(villager.getLocation(), Sound.BLOCK_ANVIL_USE, 1.0f, 1.0f);
    }

    private void repairWeapons(ItemStack item, int repairAmount) {
        if (item.getItemMeta() instanceof org.bukkit.inventory.meta.Damageable damageable) {
            int currentDamage = damageable.getDamage();
            int newDamage = Math.max(currentDamage - repairAmount, 0); // Ensure durability does not exceed maximum
            damageable.setDamage(newDamage);
            item.setItemMeta(damageable);
        }
    }


    // Toolsmiths repair the player's tools if the player is nearby
    private void performToolsmithWork(Villager villager, int radius) {
        // Search for nearby entities displaying items
        Map<Entity, ItemStack> displayedItems = findAllDisplayedItems(villager, radius);

        if(displayedItems.isEmpty()){
            Speech speech = new Speech(plugin);
            speech.createText(villager.getLocation(), "If you want, you can display your tools nearby and I can repair them.", 30);
        }
        // Fixed repair amount for all tools
        XPManager xpManager = new XPManager(plugin);
        int smithingLevel = xpManager.getPlayerLevel(getNearestPlayer(villager.getLocation()), "Smithing");
        int repairAmount = smithingLevel * 10;

        for (Map.Entry<Entity, ItemStack> entry : displayedItems.entrySet()) {
            Entity entity = entry.getKey();
            ItemStack item = entry.getValue();

            if (item == null) continue;

            Material type = item.getType();

            // Check if the item is a tool
            if (type.name().endsWith("_PICKAXE") ||
                    type.name().endsWith("_SHOVEL") ||
                    type.name().endsWith("_HOE") ||
                    type.name().endsWith("_AXE") ||
                    type == Material.SHEARS ||
                    type == Material.FLINT_AND_STEEL ||
                    type == Material.FISHING_ROD) {

                // Repair the item
                repairTools(item, repairAmount);
                Speech speech = new Speech(plugin);
                speech.createText(villager.getLocation(), "I repaired nearby displayed tools " + repairAmount + " for you.", 30);
                // Set the repaired item back
                if (entity instanceof ItemFrame) {
                    ((ItemFrame) entity).setItem(item);
                } else if (entity instanceof ItemDisplay) {
                    ((ItemDisplay) entity).setItemStack(item);
                }
            }
        }

        // Play sound to indicate repair action
        villager.getWorld().playSound(villager.getLocation(), Sound.BLOCK_ANVIL_USE, 1.0f, 1.0f);
    }

    private void repairTools(ItemStack item, int repairAmount) {
        if (item.getItemMeta() instanceof org.bukkit.inventory.meta.Damageable damageable) {
            int currentDamage = damageable.getDamage();
            int newDamage = Math.max(currentDamage - repairAmount, 0); // Ensure durability doesn't exceed maximum
            damageable.setDamage(newDamage);
            item.setItemMeta(damageable);
        }
    }

    private boolean isTool(ItemStack item) {
        String name = item.getType().name();
        return name.endsWith("_PICKAXE") || name.endsWith("_SHOVEL") || name.endsWith("_HOE") || name.endsWith("_AXE") || name.endsWith("_SHEARS");
    }


    private Map<Entity, ItemStack> findAllDisplayedItems(Villager villager, int radius) {
        Map<Entity, ItemStack> displayedItems = new HashMap<>();

        // Add items from item frames
        for (ItemFrame frame : findNearbyItemFrames(villager, radius)) {
            ItemStack item = frame.getItem();
            if (item != null && item.getType() != Material.AIR) {
                displayedItems.put(frame, item);
            }
        }
        
        // Add items from custom ItemDisplays
        ItemDisplayManager displayManager = MinecraftNew.getInstance().getItemDisplayManager();
        if (displayManager != null) {
            Location villagerLoc = villager.getLocation();
            
            // Get all displays from the manager and filter by distance
            for (ItemDisplayManager.ItemDisplay display : displayManager.getAllDisplays()) {
                if (display.storedItem != null && display.blockLocation != null) {
                    // Check if the display is within radius
                    if (display.blockLocation.getWorld().equals(villagerLoc.getWorld()) && 
                            display.blockLocation.distance(villagerLoc) <= radius) {
                        
                        // Get the ArmorStand entity
                        if (display.standUUID != null) {
                            Entity entity = Bukkit.getEntity(display.standUUID);
                            if (entity != null && entity.isValid()) {
                                displayedItems.put(entity, display.storedItem);
                                //Bukkit.broadcastMessage("Found item on display: " + display.storedItem.getType().name());
                            }
                        }
                    }
                }
            }
        }

        return displayedItems;
    }


    private List<ItemFrame> findNearbyItemFrames(Villager villager, int radius) {
        Location loc = villager.getLocation();
        List<ItemFrame> itemFrames = new ArrayList<>();

        Collection<Entity> nearbyEntities = loc.getWorld().getNearbyEntities(loc, radius, radius, radius);
        for (Entity entity : nearbyEntities) {
            if (entity instanceof ItemFrame itemFrame) {
                itemFrames.add(itemFrame);
            }
        }
        return itemFrames;
    }
    private void performShepherdWork(Villager villager, int radius) {
        // Find nearby sheep within the radius
        List<Sheep> nearbySheep = findNearbySheep(villager, radius);

        if (nearbySheep.isEmpty()) {
            Speech speech = new Speech(plugin);
            speech.createText(villager.getLocation(), "I couldn't find any sheep nearby to shear.", 30);
            return; // No sheep nearby
        }

        Map<Material, Integer> harvestYield = new HashMap<>();
        Random random = new Random();

        // Map to keep track of wool counts by color
        Map<DyeColor, Integer> woolCounts = new HashMap<>();

        for (Sheep sheep : nearbySheep) {
            DyeColor color = sheep.getColor();
            woolCounts.put(color, woolCounts.getOrDefault(color, 0) + 1);
        }

        // Generate wool items based on sheep colors and counts
        for (Map.Entry<DyeColor, Integer> entry : woolCounts.entrySet()) {
            DyeColor color = entry.getKey();
            int count = entry.getValue();

            // Determine the wool material based on color
            Material woolMaterial = Material.valueOf(color.name() + "_WOOL");

            // Yield is proportional to the number of sheep of that color
            int yieldAmount = count * (1 + random.nextInt(10)); // Each sheep yields 1-10 wool

            harvestYield.put(woolMaterial, (yieldAmount));
        }

        // Store or drop the harvested wool
        storeOrDropHarvest(villager, harvestYield);
        Speech speech = new Speech(plugin);
        speech.createText(villager.getLocation(), "I sheared your sheep for you and put the wool in that chest.", 30);

        // Play sound to indicate shepherd's work
        villager.getWorld().playSound(villager.getLocation(), Sound.ENTITY_SHEEP_SHEAR, 1.0f, 1.0f);
    }

    private List<Sheep> findNearbySheep(Villager villager, int radius) {
        Location loc = villager.getLocation();
        List<Sheep> sheepList = new ArrayList<>();

        Collection<Entity> nearbyEntities = loc.getWorld().getNearbyEntities(loc, radius, radius, radius);
        for (Entity entity : nearbyEntities) {
            if (entity instanceof Sheep sheep) {
                sheepList.add(sheep);
            }
        }
        return sheepList;
    }

    private void performLeatherworkerWork(Villager villager, int radius) {
        // Find nearby cauldrons
        List<Block> cauldrons = findNearbyBlocks(villager, List.of(Material.CAULDRON), radius);

        if (cauldrons.isEmpty()) {
            Speech speech = new Speech(plugin);
            speech.createText(villager.getLocation(), "I couldn't find any cauldrons nearby to tan my leather.", 30);
            // No cauldrons nearby, no yield
            return;
        }

        Random random = new Random();
        Map<Material, Integer> harvestYield = new HashMap<>();

        // Base leather production
        int leatherYield = 3;

        // Add leather to harvest
        harvestYield.put(Material.LEATHER, leatherYield);

        // 10% chance to produce a saddle
        if (random.nextFloat() < 0.10) {
            harvestYield.put(Material.SADDLE, 1);
        }

        // Store or drop the items
        storeOrDropHarvest(villager, harvestYield);
        Speech speech = new Speech(plugin);
        speech.createText(villager.getLocation(), "I tanned your leather and put the results in that chest.", 30);
        // Play sound effects
        villager.getWorld().playSound(villager.getLocation(), Sound.ITEM_ARMOR_EQUIP_LEATHER, 1.0f, 1.0f);
        if (harvestYield.containsKey(Material.SADDLE)) {
            villager.getWorld().playSound(villager.getLocation(), Sound.ITEM_ARMOR_EQUIP_GENERIC, 1.0f, 0.5f);
        }
    }

    private void performClericWork(Villager villager) {
        Random random = new Random();

        // List of possible ingredients
        Material[] ingredients = {
                Material.SPIDER_EYE,
                Material.GLISTERING_MELON_SLICE,
                Material.MAGMA_CREAM,
                Material.GHAST_TEAR,
                Material.GOLDEN_CARROT,
                Material.RABBIT_FOOT,
                Material.PHANTOM_MEMBRANE,
                Material.FERMENTED_SPIDER_EYE,
                Material.GLOWSTONE_DUST,
                Material.REDSTONE
        };

        // Choose one random ingredient and amount (1-4)
        Material ingredient = ingredients[random.nextInt(ingredients.length)];
        int amount = 1 + random.nextInt(4); // Random amount between 1-4
        Map<Material, Integer> harvestYield = new HashMap<>();
        harvestYield.put(ingredient, amount);

        // 1% chance to brew special 3-hour potions
        if (random.nextFloat() < 0.01) {
            PotionEffectType[] positiveEffects = {
                    PotionEffectType.REGENERATION,
                    PotionEffectType.SPEED,
                    PotionEffectType.FIRE_RESISTANCE,
                    PotionEffectType.WATER_BREATHING,
                    PotionEffectType.NIGHT_VISION,
                    PotionEffectType.JUMP,
                    PotionEffectType.DAMAGE_RESISTANCE,
                    PotionEffectType.LUCK,
                    PotionEffectType.SLOW_FALLING
            };

            // Create 3-hour potion
            ItemStack specialPotion = new ItemStack(Material.POTION);
            PotionMeta meta = (PotionMeta) specialPotion.getItemMeta();
            PotionEffectType effect = positiveEffects[random.nextInt(positiveEffects.length)];
            // 3 hours = 216000 ticks (20 ticks/sec * 60 sec/min * 180 min)
            meta.addCustomEffect(new PotionEffect(effect, 216000, 0), true);
            meta.setDisplayName(ChatColor.LIGHT_PURPLE + "Extended " + effect.getName() + " Potion");
            meta.addEnchant(org.bukkit.enchantments.Enchantment.LUCK, 1, true);
            meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS);
            specialPotion.setItemMeta(meta);

            // Store the special potion
            storeOrDropCustomItem(villager, specialPotion);
            villager.getWorld().playSound(villager.getLocation(), Sound.BLOCK_BREWING_STAND_BREW, 1.0f, 1.0f);
        }

        // Store or drop the ingredients
        storeOrDropHarvest(villager, harvestYield);
        Speech speech = new Speech(plugin);
        speech.createText(villager.getLocation(), "I collected some ingredients and tried to make a potion for you.", 30);
        villager.getWorld().playSound(villager.getLocation(), Sound.ITEM_BOTTLE_FILL, 1.0f, 1.0f);
    }

    private void performCartographerWork(Villager villager, int radius) {

        // Generate a random location item (replace this with the custom item you'll set)
        ItemStack rareItem = ItemRegistry.getWarp();
        // Make sure to create a stack of the item
        rareItem.setAmount(5); // Change the amount as needed

        // Store or drop the item
        storeOrDropCustomItem(villager, rareItem);

        // Play a sound to indicate the cartographer's work
        Speech speech = new Speech(plugin);
        speech.createText(villager.getLocation(), "I found some rare items scattered around the world.", 30);
        villager.getWorld().playSound(villager.getLocation(), Sound.ITEM_BOOK_PAGE_TURN, 1.0f, 1.0f);
    }

    // Utility method to find nearby blocks of specific types
    private List<Block> findNearbyBlocks(Villager villager, List<Material> targetMaterials, int radius) {
        Location loc = villager.getLocation();
        List<Block> blocks = new ArrayList<>();

        int xOrigin = loc.getBlockX();
        int yOrigin = loc.getBlockY();
        int zOrigin = loc.getBlockZ();

        for (int x = xOrigin - radius; x <= xOrigin + radius; x++) {
            for (int y = yOrigin - radius; y <= yOrigin + radius; y++) { // Expanding y range to cover a full radius
                for (int z = zOrigin - radius; z <= zOrigin + radius; z++) {
                    Block block = loc.getWorld().getBlockAt(x, y, z);
                    if (targetMaterials.contains(block.getType())) {
                        blocks.add(block);
                    }
                }
            }
        }
        return blocks;
    }

    private void storeOrDropCustomItem(Villager villager, ItemStack item) {
        // Find the nearest chest within a radius
        Chest nearestChest = findNearestChest(villager, 10); // Configurable radius

        if (nearestChest != null) {
            // Store item in the chest
            nearestChest.getInventory().addItem(item);
            Bukkit.getLogger().info("Villager " + villager.getUniqueId() + " stored items in chest.");
        } else {
            // Drop item at the villager's location
            Location loc = villager.getLocation();
            loc.getWorld().dropItemNaturally(loc, item);
            Bukkit.getLogger().info("Villager " + villager.getUniqueId() + " dropped items on the ground.");
        }
    }
    // Utility method to find the nearest block of specific types
    public Block findNearestBlock(Entity villager, List<Material> targetMaterials, int radius) {
        Location loc = villager.getLocation();
        Block nearestBlock = null;
        double nearestDistanceSquared = Double.MAX_VALUE;

        int xOrigin = loc.getBlockX();
        int yOrigin = loc.getBlockY();
        int zOrigin = loc.getBlockZ();

        for (int x = xOrigin - radius; x <= xOrigin + radius; x++) {
            for (int y = yOrigin - 1; y <= yOrigin + 4; y++) {
                for (int z = zOrigin - radius; z <= zOrigin + radius; z++) {
                    Block block = loc.getWorld().getBlockAt(x, y, z);
                    if (targetMaterials.contains(block.getType())) {
                        double distanceSquared = block.getLocation().distanceSquared(loc);
                        if (distanceSquared < nearestDistanceSquared) {
                            nearestDistanceSquared = distanceSquared;
                            nearestBlock = block;
                        }
                    }
                }
            }
        }
        return nearestBlock;
    }

    // Check if there's water nearby for fishermen
    private boolean isWaterNearby(Villager villager, int radius) {
        List<Material> waterMaterials = List.of(Material.WATER);
        List<Block> waterBlocks = findNearbyBlocks(villager, waterMaterials, radius);
        return !waterBlocks.isEmpty();
    }

    private Chest findNearestChest(Villager villager, int radius) {
        Location loc = villager.getLocation();
        Chest nearestChest = null;
        double nearestDistanceSquared = Double.MAX_VALUE;

        int xOrigin = loc.getBlockX();
        int yOrigin = loc.getBlockY();
        int zOrigin = loc.getBlockZ();

        for (int x = xOrigin - radius; x <= xOrigin + radius; x++) {
            for (int y = yOrigin - 2; y <= yOrigin + 5; y++) {
                for (int z = zOrigin - radius; z <= zOrigin + radius; z++) {
                    Block block = loc.getWorld().getBlockAt(x, y, z);
                    if (block.getType() == Material.CHEST) {
                        double distanceSquared = block.getLocation().distanceSquared(loc);
                        if (distanceSquared < nearestDistanceSquared) {
                            nearestDistanceSquared = distanceSquared;
                            BlockState state = block.getState();
                            if (state instanceof Chest chest) {
                                nearestChest = chest;
                            }
                        }
                    }
                }
            }
        }
        return nearestChest;
    }


    private void performFletcherWork(Villager villager, int radius) {
        // Find nearby log variants
        Set<Material> logVariants = findNearbyLogVariants(villager, radius);
        if (logVariants.isEmpty()) {
            Speech speech = new Speech(plugin);
            speech.createText(villager.getLocation(), "Place a log near, I'll gather that wood for you!", 30);
            return; // No suitable logs found
        }
        // Find nearby target blocks
    
        // Prepare the yield items
        Map<ItemStack, Integer> harvestYield = new HashMap<>();
    
        // Add logs of each variant found
        for (Material logVariant : logVariants) {
            harvestYield.put(new ItemStack(logVariant), 16); // Each variant yields (multiplier * 2) logs
        }
    
        // Add healing arrows
        Random random = new Random();
        for (int i = 0; i < 8; i++) {
            ItemStack arrow;
            if (random.nextFloat() < 0.01) {
                // 1% chance to create an arrow of healing 100
                arrow = createHealingArrow(100);
            } else {
                // 99% chance to create an arrow of healing 2
                arrow = createHealingArrow(2);
            }
            harvestYield.merge(arrow, 1, Integer::sum);
        }
    
        // Store or drop the items
        storeOrDropHarvestItemStack(villager, harvestYield);
        Speech speech = new Speech(plugin);
        speech.createText(villager.getLocation(), "I gathered the wood and crafted some arrows for you!", 30);
        // Play sound to indicate the fletcher's work
        villager.getWorld().playSound(villager.getLocation(), Sound.ENTITY_VILLAGER_WORK_FLETCHER, 1.0f, 1.0f);
    }
    
    private ItemStack createHealingArrow(int healingAmount) {
        ItemStack arrow = new ItemStack(Material.TIPPED_ARROW);
        PotionMeta meta = (PotionMeta) arrow.getItemMeta();
        PotionData potionData = new PotionData(PotionType.INSTANT_HEAL);
        meta.setBasePotionData(potionData);
        meta.addCustomEffect(new PotionEffect(PotionEffectType.HEAL, 1, healingAmount - 1), true);
        arrow.setItemMeta(meta);
        return arrow;
    }

    private Set<Material> findNearbyLogVariants(Villager villager, int radius) {
        Location loc = villager.getLocation();
        Set<Material> logVariants = new HashSet<>();

        int xOrigin = loc.getBlockX();
        int yOrigin = loc.getBlockY();
        int zOrigin = loc.getBlockZ();

        int minY = Math.max(0, yOrigin - radius);
        int maxY = Math.min(loc.getWorld().getMaxHeight(), yOrigin + radius);

        for (int x = xOrigin - radius; x <= xOrigin + radius; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = zOrigin - radius; z <= zOrigin + radius; z++) {
                    Block block = loc.getWorld().getBlockAt(x, y, z);
                    Material blockType = block.getType();

                    if (isLogVariant(blockType)) {
                        logVariants.add(blockType);
                    }
                }
            }
        }
        return logVariants;
    }

    private boolean isLogVariant(Material material) {
        return switch (material) {
            case OAK_LOG, SPRUCE_LOG, BIRCH_LOG, JUNGLE_LOG, ACACIA_LOG, DARK_OAK_LOG,
                 CRIMSON_STEM, WARPED_STEM, MANGROVE_LOG, CHERRY_LOG, BAMBOO_BLOCK -> true;
            default -> false;
        };
    }

    private void performMasonWork(Villager villager, int radius) {
        Location villagerLoc = villager.getLocation();
        World world = villager.getWorld();
        radius = 40;

        // Find blocks to replicate
        List<Block> blocksToReplicate = findBlocksToReplicate(villager, radius);

        if (blocksToReplicate.isEmpty()) {
            Speech speech = new Speech(plugin);
            speech.createText(villagerLoc, "Place a stone-like block nearby on a smooth stone slab, I'll make more of that!", 30);
            // No blocks to replicate, mason does not work
            return;
        }

        // Prepare the yield items and break the blocks
        Map<Material, Integer> harvestYield = new HashMap<>();

        for (Block block : blocksToReplicate) {
            Material blockType = block.getType();
            // Add 48 of the block to the yield
            harvestYield.merge(blockType, 16, Integer::sum);

            // Break the block (set to air)

            // Optionally, play a break effect
            world.playEffect(block.getLocation(), Effect.STEP_SOUND, blockType);
        }

        // Store or drop the items
        storeOrDropHarvest(villager, harvestYield);
        Speech speech = new Speech(plugin);
        speech.createText(villagerLoc, "I made you more of those blocks!", 30);

        // Play sound to indicate the mason's work
        villager.getWorld().playSound(villager.getLocation(), Sound.BLOCK_STONE_PLACE, 1.0f, 1.0f);
    }

    private List<Block> findBlocksToReplicate(Villager villager, int radius) {
        Location loc = villager.getLocation();
        List<Block> blocksToReplicate = new ArrayList<>();

        int xOrigin = loc.getBlockX();
        int yOrigin = loc.getBlockY();
        int zOrigin = loc.getBlockZ();

        int minY = Math.max(0, yOrigin - radius);
        int maxY = Math.min(loc.getWorld().getMaxHeight(), yOrigin + radius);

        for (int x = xOrigin - radius; x <= xOrigin + radius; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = zOrigin - radius; z <= zOrigin + radius; z++) {
                    // Skip the block beneath the villager's feet
                    if (x == xOrigin && y == yOrigin - 1 && z == zOrigin) {
                        continue;
                    }

                    Block block = loc.getWorld().getBlockAt(x, y, z);

                    // Check if block is on top of an upper smooth stone slab
                    Block blockBelow = block.getRelative(BlockFace.DOWN);
                    if (blockBelow.getType() == Material.SMOOTH_STONE_SLAB) {
                        // Check if it's an upper slab
                        if (blockBelow.getBlockData() instanceof Slab slabData && slabData.getType() == Slab.Type.TOP) {
                            if (isReplicableBlock(block.getType())) {
                                blocksToReplicate.add(block);
                            }
                        }
                    }
                }
            }
        }

        return blocksToReplicate;
    }


    private boolean isReplicableBlock(Material material) {
        // List of replicable blocks
        return switch (material) {
            // Concrete Variants
            case WHITE_CONCRETE, ORANGE_CONCRETE, MAGENTA_CONCRETE, LIGHT_BLUE_CONCRETE,
                 YELLOW_CONCRETE, LIME_CONCRETE, PINK_CONCRETE, GRAY_CONCRETE,
                 LIGHT_GRAY_CONCRETE, CYAN_CONCRETE, PURPLE_CONCRETE, BLUE_CONCRETE,
                 BROWN_CONCRETE, GREEN_CONCRETE, RED_CONCRETE, BLACK_CONCRETE -> true;

            // Terracotta Variants
            case TERRACOTTA, WHITE_TERRACOTTA, ORANGE_TERRACOTTA, MAGENTA_TERRACOTTA, LIGHT_BLUE_TERRACOTTA,
                 YELLOW_TERRACOTTA, LIME_TERRACOTTA, PINK_TERRACOTTA, GRAY_TERRACOTTA,
                 LIGHT_GRAY_TERRACOTTA, CYAN_TERRACOTTA, PURPLE_TERRACOTTA, BLUE_TERRACOTTA,
                 BROWN_TERRACOTTA, GREEN_TERRACOTTA, RED_TERRACOTTA, BLACK_TERRACOTTA -> true;

            // Glazed Terracotta Variants
            case WHITE_GLAZED_TERRACOTTA, ORANGE_GLAZED_TERRACOTTA, MAGENTA_GLAZED_TERRACOTTA,
                 LIGHT_BLUE_GLAZED_TERRACOTTA, YELLOW_GLAZED_TERRACOTTA, LIME_GLAZED_TERRACOTTA,
                 PINK_GLAZED_TERRACOTTA, GRAY_GLAZED_TERRACOTTA, LIGHT_GRAY_GLAZED_TERRACOTTA,
                 CYAN_GLAZED_TERRACOTTA, PURPLE_GLAZED_TERRACOTTA, BLUE_GLAZED_TERRACOTTA,
                 BROWN_GLAZED_TERRACOTTA, GREEN_GLAZED_TERRACOTTA, RED_GLAZED_TERRACOTTA,
                 BLACK_GLAZED_TERRACOTTA -> true;

            // Brick Variants
            case DEEPSLATE_BRICKS,DEEPSLATE_TILES, BRICKS, POLISHED_BLACKSTONE_BRICKS, BRICK_STAIRS, BRICK_SLAB, BRICK_WALL,
                 STONE_BRICKS, STONE_BRICK_STAIRS, STONE_BRICK_SLAB, STONE_BRICK_WALL,
                 MOSSY_STONE_BRICKS, MOSSY_STONE_BRICK_STAIRS, MOSSY_STONE_BRICK_SLAB, MOSSY_STONE_BRICK_WALL,
                 NETHER_BRICKS, NETHER_BRICK_STAIRS, NETHER_BRICK_SLAB, NETHER_BRICK_WALL,
                 RED_NETHER_BRICKS, RED_NETHER_BRICK_STAIRS, RED_NETHER_BRICK_SLAB, RED_NETHER_BRICK_WALL -> true;

            // Additional building blocks
            case QUARTZ_BLOCK, QUARTZ_STAIRS, QUARTZ_SLAB, SMOOTH_STONE,
                 PRISMARINE, PRISMARINE_BRICKS, DARK_PRISMARINE,
                 POLISHED_ANDESITE, POLISHED_DIORITE, POLISHED_GRANITE -> true;

            default -> false;
        };
    }


    private void storeOrDropHarvest(Villager villager, Map<Material, Integer> harvestYield) {
        Chest nearestChest = findNearestChest(villager, 10); // Configurable radius
        List<ItemStack> itemsToStore = new ArrayList<>();
        for (Map.Entry<Material, Integer> entry : harvestYield.entrySet()) {
            Material material = entry.getKey();
            int amount = entry.getValue();

            // Ensure material is valid and amount is greater than zero
            if (material == null || material == Material.AIR || amount <= 0) {
                Bukkit.getLogger().warning("Skipping invalid item or zero quantity: Material=" + material + ", Amount=" + amount);
                continue; // Skip any invalid or zero items
            }

            itemsToStore.add(new ItemStack(material, amount));
        }

        if (nearestChest != null) {
            // Store items in the chest
            for (ItemStack item : itemsToStore) {
                if (item != null && item.getType() != Material.AIR) {
                    nearestChest.getInventory().addItem(item);
                }
            }
            Bukkit.getLogger().info("Villager " + villager.getUniqueId() + " stored items in chest.");
        } else {
            // Drop items at the villager's location
            Location loc = villager.getLocation();
            for (ItemStack item : itemsToStore) {
                if (item != null && item.getType() != Material.AIR) {
                    loc.getWorld().dropItemNaturally(loc, item);
                } else {
                    Bukkit.getLogger().warning("Attempted to drop an invalid or air item: " + item);
                }
            }
            Bukkit.getLogger().info("Villager " + villager.getUniqueId() + " dropped items on the ground.");
        }
    }



    private void storeOrDropHarvestItemStack(Villager villager, Map<ItemStack, Integer> harvestYield) {
        // Find the nearest chest within a radius
        Chest nearestChest = findNearestChest(villager, 10); // Configurable radius

        List<ItemStack> itemsToStore = new ArrayList<>();
        for (Map.Entry<ItemStack, Integer> entry : harvestYield.entrySet()) {
            ItemStack item = entry.getKey().clone(); // Clone to ensure no unintended modifications
            item.setAmount(entry.getValue()); // Set the amount from the map
            itemsToStore.add(item);
        }

        if (nearestChest != null) {
            boolean allItemsStored = true;

            for (ItemStack item : itemsToStore) {
                HashMap<Integer, ItemStack> remainingItems = nearestChest.getInventory().addItem(item);
                if (!remainingItems.isEmpty()) {
                    // Chest is full or cannot fit some items
                    allItemsStored = false;
                    for (ItemStack remainingItem : remainingItems.values()) {
                        // Drop remaining items at the villager's location
                        Location loc = villager.getLocation();
                        loc.getWorld().dropItemNaturally(loc, remainingItem);
                    }
                }
            }

            if (allItemsStored) {
                Bukkit.getLogger().info("Villager " + villager.getUniqueId() + " stored all items in chest.");
            } else {
                Bukkit.getLogger().info("Villager " + villager.getUniqueId() + " stored some items in chest and dropped the rest.");
            }
        } else {
            // Drop items at the villager's location if no chest is found
            Location loc = villager.getLocation();
            for (ItemStack item : itemsToStore) {
                loc.getWorld().dropItemNaturally(loc, item);
            }
            Bukkit.getLogger().info("Villager " + villager.getUniqueId() + " dropped items on the ground.");
        }
    }
}
