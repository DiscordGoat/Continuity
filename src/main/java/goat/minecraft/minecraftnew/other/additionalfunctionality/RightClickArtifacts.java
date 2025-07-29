package goat.minecraft.minecraftnew.other.additionalfunctionality;

import goat.minecraft.minecraftnew.MinecraftNew;
import goat.minecraft.minecraftnew.other.trinkets.TrinketManager;
import goat.minecraft.minecraftnew.subsystems.culinary.CulinarySubsystem;
import goat.minecraft.minecraftnew.subsystems.farming.SeederType;
import goat.minecraft.minecraftnew.subsystems.pets.PetManager;
import goat.minecraft.minecraftnew.subsystems.fishing.SeaCreature;
import goat.minecraft.minecraftnew.subsystems.fishing.SeaCreatureRegistry;
import goat.minecraft.minecraftnew.utils.biomeutils.StructureUtils;
import goat.minecraft.minecraftnew.utils.devtools.ItemRegistry;
import goat.minecraft.minecraftnew.utils.devtools.VillagerNameRepository;
import goat.minecraft.minecraftnew.utils.devtools.XPManager;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Ageable;
import org.bukkit.block.data.type.Farmland;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.bukkit.plugin.Plugin;
import goat.minecraft.minecraftnew.other.skilltree.SkillTreeManager;
import goat.minecraft.minecraftnew.other.skilltree.Skill;
import goat.minecraft.minecraftnew.other.skilltree.Talent;

import java.util.*;

public class RightClickArtifacts implements Listener {
    private static final List<ItemStack> armorTrims = new ArrayList<>();
    private static final Random random = new Random();



    static {
        // Add every armor trim ItemStack to the list
        armorTrims.add(new ItemStack(Material.COAST_ARMOR_TRIM_SMITHING_TEMPLATE));
        armorTrims.add(new ItemStack(Material.DUNE_ARMOR_TRIM_SMITHING_TEMPLATE));
        armorTrims.add(new ItemStack(Material.EYE_ARMOR_TRIM_SMITHING_TEMPLATE));
        armorTrims.add(new ItemStack(Material.HOST_ARMOR_TRIM_SMITHING_TEMPLATE));
        armorTrims.add(new ItemStack(Material.RAISER_ARMOR_TRIM_SMITHING_TEMPLATE));
        armorTrims.add(new ItemStack(Material.RIB_ARMOR_TRIM_SMITHING_TEMPLATE));
        armorTrims.add(new ItemStack(Material.SENTRY_ARMOR_TRIM_SMITHING_TEMPLATE));
        armorTrims.add(new ItemStack(Material.SHAPER_ARMOR_TRIM_SMITHING_TEMPLATE));
        armorTrims.add(new ItemStack(Material.SNOUT_ARMOR_TRIM_SMITHING_TEMPLATE));
        armorTrims.add(new ItemStack(Material.SILENCE_ARMOR_TRIM_SMITHING_TEMPLATE));
        armorTrims.add(new ItemStack(Material.TIDE_ARMOR_TRIM_SMITHING_TEMPLATE));
        armorTrims.add(new ItemStack(Material.SPIRE_ARMOR_TRIM_SMITHING_TEMPLATE));
        armorTrims.add(new ItemStack(Material.VEX_ARMOR_TRIM_SMITHING_TEMPLATE));
        armorTrims.add(new ItemStack(Material.WILD_ARMOR_TRIM_SMITHING_TEMPLATE));
        armorTrims.add(new ItemStack(Material.WARD_ARMOR_TRIM_SMITHING_TEMPLATE));
        armorTrims.add(new ItemStack(Material.WAYFINDER_ARMOR_TRIM_SMITHING_TEMPLATE));

        // Add other armor trims as necessary
    }
    public static ItemStack getRandomArmorTrim() {
        return armorTrims.get(random.nextInt(armorTrims.size()));
    }

    private static ItemStack getRandomSeaCreatureAlchemyItem() {
        List<SeaCreature> seaCreatures = SeaCreatureRegistry.getSeaCreatures();
        if (seaCreatures.isEmpty()) return null;
        SeaCreature creature = seaCreatures.get(random.nextInt(seaCreatures.size()));
        List<ItemStack> drops = creature.getDrops();
        if (drops.isEmpty()) return null;
        return drops.get(random.nextInt(drops.size()));
    }

    private static ItemStack getRandomRareSapling() {
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

    private static ItemStack getRandomFishingTreasure() {
        List<LootItem> lootTable = Arrays.asList(
                new LootItem(new ItemStack(Material.NAUTILUS_SHELL, 8), 10),
                new LootItem(new ItemStack(Material.SADDLE), 10),
                new LootItem(new ItemStack(Material.DIAMOND, random.nextInt(10) + 1), 10),
                new LootItem(new ItemStack(Material.EMERALD, 64), 10),
                new LootItem(new ItemStack(Material.ANCIENT_DEBRIS), 7),
                new LootItem(new ItemStack(Material.ENCHANTED_GOLDEN_APPLE), 3),
                new LootItem(new ItemStack(Material.TOTEM_OF_UNDYING), 13),
                new LootItem(new ItemStack(Material.HEART_OF_THE_SEA), 5),
                new LootItem(new ItemStack(Material.SHULKER_SHELL), 10),
                new LootItem(new ItemStack(Material.SPONGE), 8),
                new LootItem(new ItemStack(Material.TURTLE_SCUTE), 7),
                new LootItem(new ItemStack(Material.WITHER_SKELETON_SKULL), 4),
                new LootItem(new ItemStack(Material.CREEPER_HEAD), 7),
                new LootItem(new ItemStack(Material.ZOMBIE_HEAD), 7),
                new LootItem(new ItemStack(Material.SKELETON_SKULL), 7),
                new LootItem(new ItemStack(Material.NETHER_WART, random.nextInt(3) + 1), 10),
                new LootItem(new ItemStack(Material.EXPERIENCE_BOTTLE, 64), 7),
                new LootItem(new ItemStack(Material.MUSIC_DISC_13), 2),
                new LootItem(new ItemStack(Material.MUSIC_DISC_CAT), 2),
                new LootItem(new ItemStack(Material.MUSIC_DISC_BLOCKS), 2),
                new LootItem(new ItemStack(Material.MUSIC_DISC_CHIRP), 2),
                new LootItem(new ItemStack(Material.MUSIC_DISC_FAR), 2),
                new LootItem(new ItemStack(Material.MUSIC_DISC_MALL), 2),
                new LootItem(new ItemStack(Material.MUSIC_DISC_MELLOHI), 2),
                new LootItem(new ItemStack(Material.MUSIC_DISC_STAL), 2),
                new LootItem(new ItemStack(Material.MUSIC_DISC_STRAD), 2),
                new LootItem(new ItemStack(Material.MUSIC_DISC_WARD), 2),
                new LootItem(new ItemStack(Material.MUSIC_DISC_11), 2),
                new LootItem(new ItemStack(Material.MUSIC_DISC_WAIT), 2),
                new LootItem(new ItemStack(Material.MUSIC_DISC_OTHERSIDE), 2),
                new LootItem(new ItemStack(Material.MUSIC_DISC_RELIC), 2),
                new LootItem(new ItemStack(Material.MUSIC_DISC_5), 2),
                new LootItem(getRandomSeaCreatureAlchemyItem(), 1),
                new LootItem(getRandomRareSapling(), 10)
        );

        int totalWeight = lootTable.stream().mapToInt(LootItem::getWeight).sum();
        int roll = random.nextInt(totalWeight);
        int cumulative = 0;
        for (LootItem item : lootTable) {
            cumulative += item.getWeight();
            if (roll < cumulative) {
                return item.getItem();
            }
        }
        return null;
    }

    private static class LootItem {
        private final ItemStack item;
        private final int weight;

        LootItem(ItemStack item, int weight) {
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
    private final ItemStack enderDrop = ItemRegistry.getEnderDrop();
    private final Plugin plugin;

    /**
     * Constructor to initialize the listener with the plugin instance.
     *
     * @param plugin The main plugin instance.
     */
    public RightClickArtifacts(Plugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Drops 16 random recipes at the specified location.
     *
     * @param location The location where the recipes will be dropped.
     */
    public void dropRandomRecipes(Location location) {
        // Get all recipe items from the CulinarySubsystem
        List<ItemStack> allRecipeItems = CulinarySubsystem.getInstance(MinecraftNew.getInstance()).getAllNonFeastRecipeItems();

        // Ensure there are enough recipes to drop
        if (allRecipeItems.isEmpty()) {
            Bukkit.getLogger().warning("No recipes found in CulinarySubsystem.");
            return;
        }

        // Randomly select 16 recipes from the list
        Random random = new Random();
        for (int i = 0; i < 16; i++) {
            // Randomly select a recipe
            ItemStack recipeItem = allRecipeItems.get(random.nextInt(allRecipeItems.size())).clone();

            // Drop the recipe at the specified location
            location.getWorld().dropItemNaturally(location, recipeItem);
        }

        // Optional: Play a sound or send a message
        location.getWorld().playSound(location, Sound.ENTITY_ITEM_PICKUP, 1.0f, 1.0f);
    }



    public static void summonXP(Player player) {
        // Get the player's location
        Location location = player.getLocation();

        // Calculate the number of orbs needed
        int totalXP = 25000;
        int orbCount = totalXP / 10; // Each orb contains roughly 10 XP on average

        // Summon XP orbs at the player's location
        for (int i = 0; i < orbCount; i++) {
            ExperienceOrb xpOrb = location.getWorld().spawn(location, ExperienceOrb.class);
            xpOrb.setExperience(10*100); // Set XP amount for each orb
        }

        // Send a message to the player
        player.sendMessage("You have been granted 25,000 XP!");
    }
    /**
     * Spawns 50 TNT blocks around the specified player with a 10-second fuse.
     * Sends a message to the player informing them they have 10 seconds to run.
     *
     * @param player The player to notify and around whom TNT blocks will be spawned.
     */
    public void spawnExplosiveWarning(Player player) {
        if (player == null) {
            System.err.println("Error: Player is null. Cannot spawn TNT blocks.");
            return;
        }

        // Send a message to the player
        player.sendMessage(ChatColor.RED + "⚠️ You have 10 seconds to run!");

        // Get the player's current location and the world they're in
        Location playerLocation = player.getLocation();
        World world = playerLocation.getWorld();

        if (world == null) {
            player.sendMessage(ChatColor.RED + "Error: Unable to determine your current world.");
            return;
        }

        // Define the radius around the player where TNT will spawn
        double radius = 10.0; // 10 blocks radius

        // Spawn 50 TNT blocks
        for (int i = 0; i < 50; i++) {
            // Calculate random offsets within the defined radius
            double offsetX = (random.nextDouble() - 0.5) * 2 * radius; // Range: -radius to +radius
            double offsetZ = (random.nextDouble() - 0.5) * 2 * radius; // Range: -radius to +radius

            // Determine the spawn location for the TNT block
            Location tntLocation = playerLocation.clone().add(offsetX, 0, offsetZ);

            // Adjust Y-coordinate to ensure TNT spawns above the ground

            // Spawn the TNTPrimed entity (TNT block with a fuse)
            TNTPrimed tnt = world.spawn(tntLocation, TNTPrimed.class);
            tnt.setSource(player);

            // Set the fuse ticks to 200 (10 seconds)
            tnt.setFuseTicks(200);

            // Customize the TNT's velocity to spread them out
            double velocityX = (random.nextDouble() - 0.5) * 0.2;
            double velocityY = random.nextDouble() * 0.2;
            double velocityZ = (random.nextDouble() - 0.5) * 0.2;
            tnt.setVelocity(new Vector(velocityX, velocityY, velocityZ));
        }
    }
    XPManager xpManager = new XPManager(MinecraftNew.getInstance());
    /**
     * Plants a wheat seed on the given FARMLAND block.
     *
     * @param farmlandBlock The FARMLAND block where the seed will be planted.
     * @return True if the seed was successfully planted, false otherwise.
     */
    /**
     * Plants a seed on the given FARMLAND block based on the crop material.
     *
     * @param farmlandBlock The FARMLAND block where the seed will be planted.
     * @param cropMaterial  The material representing the crop to plant.
     * @return True if the seed was successfully planted, false otherwise.
     */
    private boolean plantSeed(Block farmlandBlock, Material cropMaterial) {
        Block cropBlock = farmlandBlock.getRelative(BlockFace.UP);

        // Check if the crop block is air (empty)
        if (cropBlock.getType() == Material.AIR) {
            cropBlock.setType(getPlantMaterial(cropMaterial));
            // Set the crop's age to 0 (just planted)
            if (cropBlock.getBlockData() instanceof Ageable) {
                Ageable cropData = (Ageable) cropBlock.getBlockData();
                cropData.setAge(0);
                cropBlock.setBlockData(cropData);
            }
            return true;
        }

        // Optionally, handle cases where the crop block is already planted
        return false;
    }

    /**
     * Maps the crop material to the correct plantable block.
     *
     * @param cropMaterial The material representing the crop.
     * @return The plantable material corresponding to the crop.
     */
    private Material getPlantMaterial(Material cropMaterial) {
        switch (cropMaterial) {
            case WHEAT_SEEDS:
                return Material.WHEAT;
            case POTATOES:
                return Material.POTATOES;
            case CARROTS:
                return Material.CARROTS;
            case BEETROOTS:
                return Material.BEETROOTS;
            default:
                return Material.AIR;
        }
    }
    private static final Material[] MUSIC_DISCS = {
            Material.MUSIC_DISC_13,
            Material.MUSIC_DISC_BLOCKS,
            Material.MUSIC_DISC_CAT,
            Material.MUSIC_DISC_CHIRP,
            Material.MUSIC_DISC_FAR,
            Material.MUSIC_DISC_MALL,
            Material.MUSIC_DISC_MELLOHI,
            Material.MUSIC_DISC_STAL,
            Material.MUSIC_DISC_STRAD,
            Material.MUSIC_DISC_WAIT,
            Material.MUSIC_DISC_WARD,
            Material.MUSIC_DISC_5
    };

    public static ItemStack getRandomMusicDisc() {
        Random random = new Random();
        Material discMaterial = MUSIC_DISCS[random.nextInt(MUSIC_DISCS.length)];
        return new ItemStack(discMaterial);
    }
    /**
     * Event handler for player right-click interactions.
     * Handles usage of custom artifacts like Leviathan Heart, Hydrogen Bomb, and End Pearl.
     *
     * @param e The PlayerInteractEvent triggered when a player interacts.
     */
    @EventHandler
    public void onRightClick(PlayerInteractEvent e) {
        // Ensure the event only triggers for the main hand
        if(e.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
            Player player = e.getPlayer();
            ItemStack itemInHand = player.getInventory().getItemInMainHand();
            if (e.getHand().equals(EquipmentSlot.HAND)) {
                // Validate the item in hand
                if (itemInHand == null || !itemInHand.hasItemMeta()) {
                    return;
                }

                ItemMeta meta = itemInHand.getItemMeta();
                if (meta == null || !meta.hasDisplayName()) {
                    return;
                }
                String displayName = meta.getDisplayName();
                if (displayName.equals(ChatColor.YELLOW + "Irrigation")) {
                    Block clickedBlock = e.getClickedBlock();
                    if (clickedBlock == null || clickedBlock.getType() != Material.FARMLAND) {
                        player.sendMessage(ChatColor.RED + "You must right-click tilled soil to use the Irrigation.");
                        return;
                    }

                    Queue<Block> queue = new LinkedList<>();
                    Set<Block> visited = new HashSet<>();
                    List<Block> farmlandBlocks = new ArrayList<>();

                    queue.add(clickedBlock);
                    visited.add(clickedBlock);

                    while (!queue.isEmpty() && farmlandBlocks.size() < 256) {
                        Block current = queue.poll();
                        farmlandBlocks.add(current);
                        for (BlockFace face : new BlockFace[]{BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST}) {
                            Block adjacent = current.getRelative(face);
                            if (adjacent.getType() == Material.FARMLAND && !visited.contains(adjacent)) {
                                queue.add(adjacent);
                                visited.add(adjacent);
                            }
                        }
                    }

                    for (Block farmland : farmlandBlocks) {
                        if (farmland.getBlockData() instanceof Farmland land) {
                            land.setMoisture(land.getMaximumMoisture());
                            farmland.setBlockData(land);
                        }
                        Block cropBlock = farmland.getRelative(BlockFace.UP);
                        if (cropBlock.getBlockData() instanceof Ageable crop) {
                            if (crop.getAge() < crop.getMaximumAge()) {
                                crop.setAge(crop.getAge() + 1);
                                cropBlock.setBlockData(crop);
                            }
                        }
                    }

                    // Hydro Farmer talent extra growth chance
                    int hydro = SkillTreeManager.getInstance().getTalentLevel(player.getUniqueId(), Skill.FARMING, Talent.HYDRO_FARMER);
                    if (hydro > 0 && Math.random() < hydro * 0.20) {
                        for (Block farmland : farmlandBlocks) {
                            Block cropBlock = farmland.getRelative(BlockFace.UP);
                            if (cropBlock.getBlockData() instanceof Ageable crop) {
                                if (crop.getAge() < crop.getMaximumAge()) {
                                    crop.setAge(Math.min(crop.getAge() + 1, crop.getMaximumAge()));
                                    cropBlock.setBlockData(crop);
                                }
                            }
                        }
                    }

                    List<Block> copy = new ArrayList<>(farmlandBlocks);
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            for (Block farmland : copy) {
                                Block cropBlock = farmland.getRelative(BlockFace.UP);
                                if (cropBlock.getBlockData() instanceof Ageable crop) {
                                    if (crop.getAge() < crop.getMaximumAge()) {
                                        crop.setAge(Math.min(crop.getAge() + 1, crop.getMaximumAge()));
                                        cropBlock.setBlockData(crop);
                                    }
                                }
                            }
                        }
                    }.runTaskLater(plugin, 1200L);

                    player.playSound(player.getLocation(), Sound.ITEM_BUCKET_EMPTY, 1.0f, 1.0f);
                    decrementItemAmount(itemInHand, player);
                    player.sendMessage(ChatColor.GREEN + "Irrigated " + farmlandBlocks.size() + " farmland blocks!");
                    clickedBlock.getWorld().spawnParticle(Particle.SPLASH, clickedBlock.getLocation().add(0.5, 1, 0.5), 50, 0.5, 1, 0.5, 0.05);
                    return;
                }

                if (displayName.equals(ChatColor.YELLOW + "Fertilizer")) {
                    Block clickedBlock = e.getClickedBlock();
                    if(clickedBlock == null) return;
                    Block soil = clickedBlock.getType() == Material.FARMLAND ? clickedBlock :
                            clickedBlock.getRelative(BlockFace.DOWN);
                    if (soil.getType() != Material.FARMLAND) {
                        player.sendMessage(ChatColor.RED + "You must right-click soil or crops on soil to use the Fertilizer.");
                        return;
                    }

                    Queue<Block> queue = new LinkedList<>();
                    Set<Block> visited = new HashSet<>();
                    List<Block> farmlandBlocks = new ArrayList<>();

                    queue.add(soil);
                    visited.add(soil);

                    while(!queue.isEmpty()) {
                        Block current = queue.poll();
                        farmlandBlocks.add(current);
                        for(BlockFace face : new BlockFace[]{BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST}) {
                            Block adj = current.getRelative(face);
                            if(adj.getType() == Material.FARMLAND && visited.add(adj)) {
                                queue.add(adj);
                            }
                        }
                    }

                    int grown = 0;
                    for(Block farmland : farmlandBlocks) {
                        Block cropBlock = farmland.getRelative(BlockFace.UP);
                        if(cropBlock.getBlockData() instanceof Ageable crop) {
                            Material mat = cropBlock.getType();
                            if(mat == Material.WHEAT || mat == Material.CARROTS || mat == Material.POTATOES || mat == Material.BEETROOTS) {
                                if(crop.getAge() < crop.getMaximumAge()) {
                                    crop.setAge(Math.min(crop.getAge() + 1, crop.getMaximumAge()));
                                    cropBlock.setBlockData(crop);
                                    grown++;
                                }
                            }
                        }
                    }

                    int fert = SkillTreeManager.getInstance().getTalentLevel(player.getUniqueId(), Skill.FARMING, Talent.FERTILIZER_EFFICIENCY);
                    if (fert > 0 && Math.random() < fert * 0.20) {
                        for(Block farmland : farmlandBlocks) {
                            Block cropBlock = farmland.getRelative(BlockFace.UP);
                            if(cropBlock.getBlockData() instanceof Ageable crop) {
                                Material mat = cropBlock.getType();
                                if(mat == Material.WHEAT || mat == Material.CARROTS || mat == Material.POTATOES || mat == Material.BEETROOTS) {
                                    if(crop.getAge() < crop.getMaximumAge()) {
                                        crop.setAge(Math.min(crop.getAge() + 1, crop.getMaximumAge()));
                                        cropBlock.setBlockData(crop);
                                    }
                                }
                            }
                        }
                    }

                    player.playSound(player.getLocation(), Sound.ITEM_BONE_MEAL_USE, 1.0f, 1.0f);
                    decrementItemAmount(itemInHand, player);
                    player.sendMessage(ChatColor.GREEN + "Fertilized " + grown + " crops!");
                    clickedBlock.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, clickedBlock.getLocation().add(0.5,1,0.5), 50,0.5,0.5,0.5,0.05);
                    return;
                }
                SeederType seederType = SeederType.fromDisplayName(displayName);
                if (seederType == null) {
                    return; // The item is not a recognized seeder
                }

                // Get the clicked block
                Block clickedBlock = e.getClickedBlock();
                if (clickedBlock == null) {
                    return;
                }

                // Check if the clicked block is FARMLAND
                if (clickedBlock.getType() != Material.FARMLAND) {
                    player.sendMessage(ChatColor.RED + "You must right-click on tilled soil to use the " + seederType.name().replace("_", "") + ".");
                    return;
                }

                // Gather FARMLAND blocks using BFS. We traverse through all
                // connected farmland blocks but only record those without crops
                // on top so we don't waste the limit on already-planted blocks.
                Queue<Block> queue = new LinkedList<>();
                Set<Block> visited = new HashSet<>();
                List<Block> farmlandBlocks = new ArrayList<>();

                queue.add(clickedBlock);
                visited.add(clickedBlock);

                while (!queue.isEmpty() && farmlandBlocks.size() < 256) {
                    Block current = queue.poll();

                    // Only add farmland that doesn't already have a crop
                    if (current.getRelative(BlockFace.UP).getType() == Material.AIR) {
                        farmlandBlocks.add(current);
                    }

                    // Iterate over adjacent blocks (North, South, East, West)
                    for (BlockFace face : new BlockFace[]{BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST}) {
                        Block adjacent = current.getRelative(face);

                        // Check if the adjacent block is FARMLAND and not yet visited
                        if (adjacent.getType() == Material.FARMLAND && !visited.contains(adjacent)) {
                            queue.add(adjacent);
                            visited.add(adjacent);
                        }
                    }
                }

                int plantedCount = 0;
                // Plant from farthest to nearest
                for (int i = farmlandBlocks.size() - 1; i >= 0; i--) {
                    if (plantSeed(farmlandBlocks.get(i), seederType.getCropMaterial())) {
                        plantedCount++;
                    }
                }

                // Provide feedback to the player
                player.playSound(player.getLocation(), Sound.BLOCK_AZALEA_LEAVES_BREAK, 1.0f, 1.0f);
                decrementItemAmount(itemInHand, player);
                player.sendMessage(ChatColor.GREEN + "Seeds planted on " + plantedCount + " blocks!");

                // Optional: Display particles at the clicked location
                clickedBlock.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, clickedBlock.getLocation().add(0.5, 1, 0.5), 50, 0.5, 1, 0.5, 0.05);

            }
            if (itemInHand.getItemMeta().getDisplayName().equals(ChatColor.LIGHT_PURPLE + "Jackhammer")) {
                Block clickedBlock = e.getClickedBlock();
                if (clickedBlock == null) {
                    return;
                }

                Material targetType = clickedBlock.getType();
                Queue<Block> queue = new LinkedList<>();
                Set<Block> visited = new HashSet<>();

                queue.add(clickedBlock);
                visited.add(clickedBlock);


                while (!queue.isEmpty() && visited.size() <= 501) {
                    Block current = queue.poll();
                    for (BlockFace face : new BlockFace[]{BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST, BlockFace.UP, BlockFace.DOWN}) {
                        Block adjacent = current.getRelative(face);
                        if (adjacent.getType() == targetType && !visited.contains(adjacent)) {
                            visited.add(adjacent);
                            queue.add(adjacent);
                            if (visited.size() > 501) break;
                        }
                    }
                }

                if (visited.size() > 10000) {
                    player.sendMessage(ChatColor.RED + "Vein is too large to jackhammer!");
                    return;
                }

                for (Block b : visited) {
                    b.breakNaturally();
                }
                player.playSound(player.getLocation(), Sound.BLOCK_STONE_BREAK, 1.0f, 1.0f);
                decrementItemAmount(itemInHand, player);
            } else {
                return;
            }
        }
        if (e.getAction() == Action.RIGHT_CLICK_AIR) {
            if (!e.getHand().equals(EquipmentSlot.HAND)) {
                return;
            }


            Player player = e.getPlayer();
            ItemStack itemInHand = player.getInventory().getItemInMainHand();

            if (itemInHand == null || !itemInHand.hasItemMeta()) {
                return;
            }

            ItemMeta meta = itemInHand.getItemMeta();
            if (meta == null || !meta.hasDisplayName()) {
                return;
            }


            String displayName = meta.getDisplayName();

            if (displayName.equals(ChatColor.GOLD + "Perfect Apple")) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 4 * 20, 0));
                player.setFoodLevel(20);
                player.setSaturation(20);
                player.playSound(player.getLocation(), Sound.ENTITY_GENERIC_EAT, 1.0f, 1.0f);
                decrementItemAmount(itemInHand, player);
                return;
            }
            if (displayName.equals(ChatColor.GOLD + "Golden Fish")) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 4 * 20, 0));
                player.setFoodLevel(20);
                player.setSaturation(20);
                player.setAbsorptionAmount(player.getAbsorptionAmount() + 80.0);
                player.playSound(player.getLocation(), Sound.ENTITY_GENERIC_EAT, 1.0f, 1.0f);
                decrementItemAmount(itemInHand, player);
                return;
            }
            if (displayName.equals(ChatColor.YELLOW + "Cookbook")) {
                player.playSound(player.getLocation(), Sound.ITEM_BOOK_PAGE_TURN, 1.0f, 1.0f);
                dropRandomRecipes(player.getLocation());
                decrementItemAmount(itemInHand, player);
                player.closeInventory();
                return;
            }
            if (displayName.equals(ChatColor.YELLOW + "Inscriber")) {
                player.playSound(player.getLocation(), Sound.ITEM_BOOK_PAGE_TURN, 1.0f, 1.0f);
                openDiscSelectionGUI(player, itemInHand);
                return;
            }

            // Handle Leviathan Heart
            if (displayName.equals(ChatColor.LIGHT_PURPLE + "Leviathan Heart")) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 180 * 20, 7));
                player.playSound(player.getLocation(), Sound.ENTITY_WARDEN_ROAR, 1.0f, 1.0f);
                decrementItemAmount(itemInHand, player);
                return;
            }
            if (displayName.equals(ChatColor.YELLOW + "Experience Artifact Tier 1")) {
                summonXP(player);
                decrementItemAmount(itemInHand, player);
                return;
            }
            if (displayName.equals(ChatColor.YELLOW + "Treasure Chest")) {
                ItemStack treasure = getRandomFishingTreasure();
                if (treasure != null) {
                    player.getInventory().addItem(treasure);
                }
                player.playSound(player.getLocation(), Sound.BLOCK_CHEST_OPEN, 1.0f, 1.0f);
                player.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, player.getLocation(), 20, 1, 1, 1, 0.1);
                decrementItemAmount(itemInHand, player);
                return;
            }


            // Handle books with a tier
            if (meta.hasLore()) {
                List<String> lore = meta.getLore();
                if (lore != null) {
                    String theme = null;
                    int tier = 1;
                    for (String line : lore) {
                        if (line.contains("Theme:")) {
                            // Extract the theme name from the lore
                            theme = ChatColor.stripColor(line).replace("Theme: ", "").trim();
                        }
                        if (line.contains("Tier:")) {
                            try {
                                // Extract the tier number from the lore
                                String tierString = ChatColor.stripColor(line).replaceAll("[^0-9]", "");
                                tier = Integer.parseInt(tierString);
                            } catch (NumberFormatException ex) {
                                player.sendMessage(ChatColor.RED + "Failed to determine the tier of the book.");
                                return;
                            }
                        }
                    }

                    // Ensure theme is found
                    if (theme != null) {
                        // Calculate XP based on the tier (e.g., 50 XP per tier)
                        double xpToAdd = tier * 10;

                        // Use your custom addXP method with the theme as the skill
                        xpManager.addXP(player, theme, xpToAdd);

                        // Feedback to the player
                        player.sendMessage(ChatColor.DARK_PURPLE + "You gained " + xpToAdd + " XP for using a Tier " + tier + " book themed on " + theme + "!");
                        decrementItemAmount(itemInHand, player);
                    } else {
                        //player.sendMessage(ChatColor.RED + "No theme found in the book's lore.");
                    }
                }
            }


            // Handle Hydrogen Bomb
            if (displayName.equals(ChatColor.YELLOW + "Hydrogen Bomb")) {
                spawnExplosiveWarning(player);
                player.playSound(player.getLocation(), Sound.ENTITY_WARDEN_ROAR, 1.0f, 1.0f);
                decrementItemAmount(itemInHand, player);
                return;
            }

            // Handle End Pearl
            if (displayName.equals(ChatColor.YELLOW + "End Pearl")) {
                player.playSound(player.getLocation(), Sound.BLOCK_WART_BLOCK_PLACE, 1.0f, 1.0f);
                //decrementItemAmount(itemInHand, player);
                // The Ender Pearl will be thrown automatically by Bukkit
                // Custom handling will be done in ProjectileLaunchEvent and ProjectileHitEvent
            }
            if (displayName.equals(ChatColor.YELLOW + "Rain")) {
                player.playSound(player.getLocation(), Sound.ENTITY_DOLPHIN_SPLASH, 1.0f, 1.0f);
                changeWeatherToRain(player);
                decrementItemAmount(itemInHand, player);
                return;
            }
            if (displayName.equals(ChatColor.LIGHT_PURPLE + "Deep Tooth")) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 30 * 20, 3, true));
                decrementItemAmount(itemInHand, player);
                return;
            }
            if (displayName.equals(ChatColor.YELLOW + "Iron Golem")) {
                player.playSound(player.getLocation(), Sound.ENTITY_IRON_GOLEM_DEATH, 1.0f, 1.0f);
                player.getWorld().spawnEntity(player.getLocation(), EntityType.IRON_GOLEM);
                decrementItemAmount(itemInHand, player);
                return;
            }
            if (displayName.equals(ChatColor.YELLOW + "Hire Villager")) {
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_YES, 1.0f, 1.0f);
                Villager villager = (Villager) player.getWorld().spawnEntity(player.getLocation(), EntityType.VILLAGER);

                // Retrieve a funny name based on the villager's current profession.
                // If there's no matching funny name, a random male name is returned.
                String villagerName = VillagerNameRepository.getRandomMaleName();

                // Set the custom name of the villager and make it visible.
                villager.setCustomName(ChatColor.GREEN + villagerName);
                villager.setCustomNameVisible(true);

                decrementItemAmount(itemInHand, player);
                return;
            }
            if (displayName.equals(ChatColor.YELLOW + "Hire Bartender")) {
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_YES, 1.0f, 2.0f);
                Villager villager = (Villager) player.getWorld().spawnEntity(player.getLocation(), EntityType.VILLAGER);
                String villagerName = "Bartender";

                // Set the custom name of the villager and make it visible.
                villager.setCustomName(ChatColor.GOLD + villagerName);
                villager.setCustomNameVisible(true);

                decrementItemAmount(itemInHand, player);
                return;
            }


            if (displayName.equals(ChatColor.YELLOW + "Pet Training")) {
                PetManager petManager = PetManager.getInstance(MinecraftNew.getInstance());
                PetManager.Pet activePet = petManager.getActivePet(player);
                if(activePet != null){
                    player.playSound(player.getLocation(), Sound.ENTITY_WOLF_AMBIENT, 1.0f, 1.0f);
                    player.getWorld().spawnParticle(Particle.FLAME, player.getLocation().add(0, 1, 0), 50, 0.5, 1.0, 0.5, 0.05);
                    activePet.addXP(1000);
                    decrementItemAmount(itemInHand, player);
                }else {
                    player.sendMessage(ChatColor.RED + "You do not have a Pet summoned!");
                    return;
                }
            }
            if(displayName.equals(ChatColor.YELLOW + "Draw Random Armor Trim")){
                player.getWorld().dropItem(player.getLocation(), getRandomArmorTrim());
                player.playSound(player.getLocation(), Sound.ITEM_BOOK_PAGE_TURN, 10, 10);
                decrementItemAmount(itemInHand, player);
            }
            if(displayName.equals(ChatColor.YELLOW + "Creative Mind")){
                convertConcretePowder(player);
                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_SPLASH, 10, 10);
                decrementItemAmount(itemInHand, player);
            }
            if(displayName.equals(ChatColor.YELLOW + "Backpack")){
                CustomBundleGUI.getInstance().openBundleGUIDelayed(player);
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        // <-- your code here
                        TrinketManager.getInstance().refreshBankLore(player);
                    }
                }.runTaskLater(MinecraftNew.getInstance(), 20L);

            }
            StructureUtils structureUtils = new StructureUtils();
            if(displayName.equals(ChatColor.YELLOW + "Mineshaft Location")){
                player.playSound(player.getLocation(), Sound.ITEM_BOOK_PAGE_TURN, 10, 10);
                ItemStack paper = new ItemStack(Material.PAPER);
                ItemMeta paperMeta = paper.getItemMeta();
                paperMeta.setLore(List.of(structureUtils.getStructureCoordinates(StructureType.MINESHAFT, player)));
                paper.setItemMeta(paperMeta);
                decrementItemAmount(itemInHand, player);
                player.getLocation().getWorld().dropItem(player.getLocation(), paper);
            }
            if(displayName.equals(ChatColor.YELLOW + "Village Location")){
                player.playSound(player.getLocation(), Sound.ITEM_BOOK_PAGE_TURN, 10, 10);
                ItemStack paper = new ItemStack(Material.PAPER);
                ItemMeta paperMeta = paper.getItemMeta();
                paperMeta.setLore(List.of(structureUtils.getStructureCoordinates(StructureType.VILLAGE, player)));
                paper.setItemMeta(paperMeta);
                decrementItemAmount(itemInHand, player);
                player.getLocation().getWorld().dropItem(player.getLocation(), paper);
            }
            if(displayName.equals(ChatColor.YELLOW + "Buried Treasure Location")){
                player.playSound(player.getLocation(), Sound.ITEM_BOOK_PAGE_TURN, 10, 10);
                ItemStack paper = new ItemStack(Material.PAPER);
                ItemMeta paperMeta = paper.getItemMeta();
                paperMeta.setLore(List.of(structureUtils.getStructureCoordinates(StructureType.BURIED_TREASURE, player)));
                paper.setItemMeta(paperMeta);
                decrementItemAmount(itemInHand, player);
                player.getLocation().getWorld().dropItem(player.getLocation(), paper);
            }
            if(displayName.equals(ChatColor.YELLOW + "Igloo Location")){
                player.playSound(player.getLocation(), Sound.ITEM_BOOK_PAGE_TURN, 10, 10);
                ItemStack paper = new ItemStack(Material.PAPER);
                ItemMeta paperMeta = paper.getItemMeta();
                paperMeta.setLore(List.of(structureUtils.getStructureCoordinates(StructureType.IGLOO, player)));
                paper.setItemMeta(paperMeta);
                decrementItemAmount(itemInHand, player);
                player.getLocation().getWorld().dropItem(player.getLocation(), paper);
            }
            if(displayName.equals(ChatColor.YELLOW + "Stronghold Location")){
                player.playSound(player.getLocation(), Sound.ITEM_BOOK_PAGE_TURN, 10, 10);
                ItemStack paper = new ItemStack(Material.PAPER);
                ItemMeta paperMeta = paper.getItemMeta();
                paperMeta.setLore(List.of(structureUtils.getStructureCoordinates(StructureType.STRONGHOLD, player)));
                paper.setItemMeta(paperMeta);
                decrementItemAmount(itemInHand, player);
                player.getLocation().getWorld().dropItem(player.getLocation(), paper);
            }
            if(displayName.equals(ChatColor.YELLOW + "Desert Pyramid Location")){
                player.playSound(player.getLocation(), Sound.ITEM_BOOK_PAGE_TURN, 10, 10);
                ItemStack paper = new ItemStack(Material.PAPER);
                ItemMeta paperMeta = paper.getItemMeta();
                paperMeta.setLore(List.of(structureUtils.getStructureCoordinates(StructureType.DESERT_PYRAMID, player)));
                paper.setItemMeta(paperMeta);
                decrementItemAmount(itemInHand, player);
                player.getLocation().getWorld().dropItem(player.getLocation(), paper);
            }
            if(displayName.equals(ChatColor.YELLOW + "Jungle Pyramid Location")){
                player.playSound(player.getLocation(), Sound.ITEM_BOOK_PAGE_TURN, 10, 10);
                ItemStack paper = new ItemStack(Material.PAPER);
                ItemMeta paperMeta = paper.getItemMeta();
                paperMeta.setLore(List.of(structureUtils.getStructureCoordinates(StructureType.JUNGLE_PYRAMID, player)));
                paper.setItemMeta(paperMeta);
                decrementItemAmount(itemInHand, player);
                player.getLocation().getWorld().dropItem(player.getLocation(), paper);
            }
            if(displayName.equals(ChatColor.YELLOW + "Warp")){
                player.playSound(player.getLocation(), Sound.ENTITY_FOX_TELEPORT, 10, 10);
                Vector direction = player.getLocation().getDirection().normalize();
                Vector offset = direction.multiply(8);
                player.teleport(player.getLocation().add(offset));
                decrementItemAmount(itemInHand, player);

            }
            if(displayName.equals(ChatColor.YELLOW + "Ocean Monument Location")){
                player.playSound(player.getLocation(), Sound.ITEM_BOOK_PAGE_TURN, 10, 10);
                ItemStack paper = new ItemStack(Material.PAPER);
                ItemMeta paperMeta = paper.getItemMeta();
                paperMeta.setLore(List.of(structureUtils.getStructureCoordinates(StructureType.OCEAN_MONUMENT, player)));
                paper.setItemMeta(paperMeta);
                decrementItemAmount(itemInHand, player);
                player.getLocation().getWorld().dropItem(player.getLocation(), paper);
            }
            if(displayName.equals(ChatColor.YELLOW + "Pillager Outpost Location")){
                player.playSound(player.getLocation(), Sound.ITEM_BOOK_PAGE_TURN, 10, 10);
                ItemStack paper = new ItemStack(Material.PAPER);
                ItemMeta paperMeta = paper.getItemMeta();
                paperMeta.setLore(List.of(structureUtils.getStructureCoordinates(StructureType.PILLAGER_OUTPOST, player)));
                paper.setItemMeta(paperMeta);
                decrementItemAmount(itemInHand, player);
                player.getLocation().getWorld().dropItem(player.getLocation(), paper);
            }
            if(displayName.equals(ChatColor.YELLOW + "Swamp Hut Location")){
                player.playSound(player.getLocation(), Sound.ITEM_BOOK_PAGE_TURN, 10, 10);
                ItemStack paper = new ItemStack(Material.PAPER);
                ItemMeta paperMeta = paper.getItemMeta();
                paperMeta.setLore(List.of(structureUtils.getStructureCoordinates(StructureType.SWAMP_HUT, player)));
                paper.setItemMeta(paperMeta);
                decrementItemAmount(itemInHand, player);
                player.getLocation().getWorld().dropItem(player.getLocation(), paper);
            }
            if(displayName.equals(ChatColor.YELLOW + "Woodland Mansion Location")){
                player.playSound(player.getLocation(), Sound.ITEM_BOOK_PAGE_TURN, 10, 10);
                ItemStack paper = new ItemStack(Material.PAPER);
                ItemMeta paperMeta = paper.getItemMeta();
                paperMeta.setLore(List.of(structureUtils.getStructureCoordinates(StructureType.WOODLAND_MANSION, player)));
                paper.setItemMeta(paperMeta);
                decrementItemAmount(itemInHand, player);
                player.getLocation().getWorld().dropItem(player.getLocation(), paper);
            }
            if(displayName.equals(ChatColor.YELLOW + "Bastion Remnant Location")){
                player.playSound(player.getLocation(), Sound.ITEM_BOOK_PAGE_TURN, 10, 10);
                ItemStack paper = new ItemStack(Material.PAPER);
                ItemMeta paperMeta = paper.getItemMeta();
                paperMeta.setLore(List.of(structureUtils.getStructureCoordinates(StructureType.BASTION_REMNANT, player)));
                paper.setItemMeta(paperMeta);
                decrementItemAmount(itemInHand, player);
                player.getLocation().getWorld().dropItem(player.getLocation(), paper);
            }
            if(displayName.equals(ChatColor.YELLOW + "End City Location")){
                player.playSound(player.getLocation(), Sound.ITEM_BOOK_PAGE_TURN, 10, 10);
                ItemStack paper = new ItemStack(Material.PAPER);
                ItemMeta paperMeta = paper.getItemMeta();
                paperMeta.setLore(List.of(structureUtils.getStructureCoordinates(StructureType.END_CITY, player)));
                paper.setItemMeta(paperMeta);
                decrementItemAmount(itemInHand, player);
                player.getLocation().getWorld().dropItem(player.getLocation(), paper);
            }
            if(displayName.equals(ChatColor.YELLOW + "Nether Fortress Location")){
                player.playSound(player.getLocation(), Sound.ITEM_BOOK_PAGE_TURN, 10, 10);
                ItemStack paper = new ItemStack(Material.PAPER);
                ItemMeta paperMeta = paper.getItemMeta();
                paperMeta.setLore(List.of(structureUtils.getStructureCoordinates(StructureType.NETHER_FORTRESS, player)));
                paper.setItemMeta(paperMeta);
                decrementItemAmount(itemInHand, player);
                player.getLocation().getWorld().dropItem(player.getLocation(), paper);
            }
            if(displayName.equals(ChatColor.YELLOW + "Ocean Ruin Location")){
                player.playSound(player.getLocation(), Sound.ITEM_BOOK_PAGE_TURN, 10, 10);
                ItemStack paper = new ItemStack(Material.PAPER);
                ItemMeta paperMeta = paper.getItemMeta();
                paperMeta.setLore(List.of(structureUtils.getStructureCoordinates(StructureType.OCEAN_RUIN, player)));
                paper.setItemMeta(paperMeta);
                decrementItemAmount(itemInHand, player);
                player.getLocation().getWorld().dropItem(player.getLocation(), paper);
            }
            if(displayName.equals(ChatColor.YELLOW + "Shipwreck Location")){
                player.playSound(player.getLocation(), Sound.ITEM_BOOK_PAGE_TURN, 10, 10);
                ItemStack paper = new ItemStack(Material.PAPER);
                ItemMeta paperMeta = paper.getItemMeta();
                paperMeta.setLore(List.of(structureUtils.getStructureCoordinates(StructureType.SHIPWRECK, player)));
                paper.setItemMeta(paperMeta);
                decrementItemAmount(itemInHand, player);
                player.getLocation().getWorld().dropItem(player.getLocation(), paper);
            }
        }
    }
    public static void convertConcretePowder(Player player) {
        Inventory inventory = player.getInventory();

        // Iterate over each item slot in the player's inventory
        for (int i = 0; i < inventory.getSize(); i++) {
            ItemStack item = inventory.getItem(i);

            if (item == null) continue; // Skip empty slots

            // Check if the item is a concrete powder variant
            Material concreteBlock = getConcreteEquivalent(item.getType());
            if (concreteBlock != null) {
                // Replace concrete powder with the equivalent concrete block
                inventory.setItem(i, new ItemStack(concreteBlock, item.getAmount()));
            }
        }
    }

    /**
     * Gets the corresponding concrete block for a given concrete powder type.
     *
     * @param powderType The concrete powder material type.
     * @return The equivalent concrete block material, or null if not a concrete powder type.
     */
    private static Material getConcreteEquivalent(Material powderType) {
        switch (powderType) {
            case WHITE_CONCRETE_POWDER:
                return Material.WHITE_CONCRETE;
            case ORANGE_CONCRETE_POWDER:
                return Material.ORANGE_CONCRETE;
            case MAGENTA_CONCRETE_POWDER:
                return Material.MAGENTA_CONCRETE;
            case LIGHT_BLUE_CONCRETE_POWDER:
                return Material.LIGHT_BLUE_CONCRETE;
            case YELLOW_CONCRETE_POWDER:
                return Material.YELLOW_CONCRETE;
            case LIME_CONCRETE_POWDER:
                return Material.LIME_CONCRETE;
            case PINK_CONCRETE_POWDER:
                return Material.PINK_CONCRETE;
            case GRAY_CONCRETE_POWDER:
                return Material.GRAY_CONCRETE;
            case LIGHT_GRAY_CONCRETE_POWDER:
                return Material.LIGHT_GRAY_CONCRETE;
            case CYAN_CONCRETE_POWDER:
                return Material.CYAN_CONCRETE;
            case PURPLE_CONCRETE_POWDER:
                return Material.PURPLE_CONCRETE;
            case BLUE_CONCRETE_POWDER:
                return Material.BLUE_CONCRETE;
            case BROWN_CONCRETE_POWDER:
                return Material.BROWN_CONCRETE;
            case GREEN_CONCRETE_POWDER:
                return Material.GREEN_CONCRETE;
            case RED_CONCRETE_POWDER:
                return Material.RED_CONCRETE;
            case BLACK_CONCRETE_POWDER:
                return Material.BLACK_CONCRETE;
            default:
                return null; // Not a concrete powder type
        }
    }
    private void changeWeatherToRain(Player player) {
        World world = Bukkit.getWorld("world"); // Replace "world" with your target world name if different

        if (world == null) {
            player.sendMessage(ChatColor.RED + "Error: Unable to find the world.");
            return;
        }

        // Set the storm to true (start raining)
        world.setStorm(true);

        // Set the duration of the weather (e.g., 10 minutes)
        int duration = (10*60)*20; //
        world.setWeatherDuration(duration);

        // Optionally, send a message to the player
        player.sendMessage(ChatColor.YELLOW + "🌧️ You have summoned rain! It will last for " + (duration/20)/60 + " minutes.");
    }
    /**
     * Helper method to decrement the amount of an item in hand.
     *
     * @param item   The ItemStack to decrement.
     * @param player The player holding the item.
     */
    private void decrementItemAmount(ItemStack item, Player player) {
        int amount = item.getAmount();
        if (amount > 1) {
            item.setAmount(amount - 1);
        } else {
            player.getInventory().removeItem(item);
        }
    }

    /**
     * Handles using the Vaccination artifact on Zombie Villagers or Witches.
     */
    @EventHandler
    public void onEntityInteract(PlayerInteractEntityEvent event) {
        Entity clicked = event.getRightClicked();
        ZombieVillager zombie = clicked instanceof ZombieVillager z ? z : null;
        Witch witch = clicked instanceof Witch w ? w : null;
        if (zombie == null && witch == null) {
            return;
        }
        Player player = event.getPlayer();
        ItemStack itemInHand = player.getInventory().getItemInMainHand();
        if (itemInHand == null || !itemInHand.hasItemMeta()) {
            return;
        }
        ItemMeta meta = itemInHand.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) {
            return;
        }
        if (!meta.getDisplayName().equals(ChatColor.YELLOW + "Vaccination")) {
            return;
        }

        event.setCancelled(true);
        if (zombie != null) {
            cureZombieVillager(player, zombie);
        } else {
            cureWitch(player, Objects.requireNonNull(witch));
        }
        decrementItemAmount(itemInHand, player);
    }

    /**
     * Converts the given ZombieVillager back to a Villager after a short delay.
     */
    private void cureZombieVillager(Player player, ZombieVillager zombie) {
        World world = zombie.getWorld();
        Location loc = zombie.getLocation();
        Villager.Profession profession = zombie.getVillagerProfession();
        Villager.Type type = zombie.getVillagerType();

        world.playSound(loc, Sound.ENTITY_ZOMBIE_VILLAGER_CURE, 1f, 1f);

        new BukkitRunnable() {
            @Override
            public void run() {
                if (!zombie.isValid()) return;
                zombie.remove();
                Villager villager = (Villager) world.spawnEntity(loc, EntityType.VILLAGER);
                villager.setProfession(profession);
                villager.setVillagerType(type);

                String rawName = zombie.getCustomName() != null ? zombie.getCustomName() : "";
                // Remove color codes then strip any level prefix such as "[Lv: 10] "
                String stripped = ChatColor.stripColor(rawName).replaceFirst("(?i)^\\s*\\[?\\s*(?:level|lv\\.?|lvl)\\s*:?\\s*\\d+\\s*\\]?\\s*", "");
                if (!stripped.isEmpty()) {
                    ChatColor nameColor = stripped.equalsIgnoreCase("Bartender") ? ChatColor.GOLD : ChatColor.GREEN;
                    villager.setCustomName(nameColor + stripped);
                    villager.setCustomNameVisible(true);
                }

                world.spawnParticle(Particle.HAPPY_VILLAGER, loc.add(0, 1, 0), 30, 0.5, 0.5, 0.5, 0.1);
                world.playSound(loc, Sound.ENTITY_VILLAGER_CELEBRATE, 1f, 1f);
            }
        }.runTaskLater(MinecraftNew.getInstance(), 100L); // 5 seconds
    }

    /**
     * Converts the given Witch back to a Villager after a short delay.
     */
    private void cureWitch(Player player, Witch witch) {
        World world = witch.getWorld();
        Location loc = witch.getLocation();

        world.playSound(loc, Sound.ENTITY_ZOMBIE_VILLAGER_CURE, 1f, 1f);

        new BukkitRunnable() {
            @Override
            public void run() {
                if (!witch.isValid()) return;
                witch.remove();
                Villager villager = (Villager) world.spawnEntity(loc, EntityType.VILLAGER);
                villager.setProfession(Villager.Profession.NONE);
                villager.setVillagerType(Villager.Type.PLAINS);

                String rawName = witch.getCustomName() != null ? witch.getCustomName() : "";
                String stripped = ChatColor.stripColor(rawName).replaceFirst("(?i)^\\s*\\[?\\s*(?:level|lv\\.?|lvl)\\s*:?\\s*\\d+\\s*\\]?\\s*", "");
                if (!stripped.isEmpty()) {
                    villager.setCustomName(ChatColor.GREEN + stripped);
                    villager.setCustomNameVisible(true);
                }

                world.spawnParticle(Particle.HAPPY_VILLAGER, loc.add(0, 1, 0), 30, 0.5, 0.5, 0.5, 0.1);
                world.playSound(loc, Sound.ENTITY_VILLAGER_CELEBRATE, 1f, 1f);
            }
        }.runTaskLater(MinecraftNew.getInstance(), 100L); // 5 seconds
    }

    /**
     * Event handler to mark the thrown EnderPearl as custom if it was thrown from the custom EnderPearl item.
     *
     * @param event The ProjectileLaunchEvent triggered when a projectile is launched.
     */
    @EventHandler
    public void onProjectileLaunch(ProjectileLaunchEvent event) {
        if (!(event.getEntity() instanceof EnderPearl)) {
            return;
        }

        EnderPearl pearl = (EnderPearl) event.getEntity();
        Player shooter = null;

        if (pearl.getShooter() instanceof Player) {
            shooter = (Player) pearl.getShooter();
        }

        if (shooter == null) {
            return;
        }

        ItemStack itemInHand = shooter.getInventory().getItemInMainHand();

        if (itemInHand == null || !itemInHand.hasItemMeta()) {
            return;
        }

        ItemMeta meta = itemInHand.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) {
            return;
        }

        String displayName = meta.getDisplayName();

        if (displayName.equals(enderDrop.getItemMeta().getDisplayName())) {
            // Mark the EnderPearl with a custom name to identify it later
            pearl.setCustomName(ChatColor.YELLOW + "End Pearl");
            pearl.setCustomNameVisible(false);
        }
    }



    /**
     * Event handler to check if the landed EnderPearl is custom and spawn another one if it is.
     *
     * @param event The ProjectileHitEvent triggered when a projectile hits something.
     */
    @EventHandler
    public void onProjectileHit(ProjectileHitEvent event) {
        if (!(event.getEntity() instanceof EnderPearl)) {
            return;
        }

        EnderPearl pearl = (EnderPearl) event.getEntity();

        // Check if the EnderPearl has the custom name
        if (pearl.getCustomName() != null && pearl.getCustomName().equals(enderDrop.getItemMeta().getDisplayName())) {
            Location landingLocation = pearl.getLocation();
            Player shooter = null;

            if (pearl.getShooter() instanceof Player) {
                shooter = (Player) pearl.getShooter();
            }

            // Spawn another custom EnderPearl at landing location
            if (landingLocation.getWorld() != null) {
                landingLocation.getWorld().dropItem(landingLocation, enderDrop);
                // Inform the player
                if (shooter != null) {
                    shooter.sendMessage(ChatColor.YELLOW + "Another Ender Pearl has spawned at your landing location!");
                }
            }
        }
    }
    public void openDiscSelectionGUI(Player player, ItemStack inscriber) {
        // Create a 27-slot GUI titled "Select a Music Disc"
        Inventory gui = Bukkit.createInventory(null, 27, ChatColor.DARK_AQUA + "Select a Music Disc");

        // Populate the GUI with discs using our disc data map.
        Map<Material, DiscData> discDataMap = getDiscDataMap();
        int slot = 0;
        for (Map.Entry<Material, DiscData> entry : discDataMap.entrySet()) {
            Material discMaterial = entry.getKey();
            DiscData data = entry.getValue();
            ItemStack discItem = new ItemStack(discMaterial);
            ItemMeta meta = discItem.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(data.getName());
                meta.setLore(data.getLore());
                meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                discItem.setItemMeta(meta);
            }
            gui.setItem(slot, discItem);
            slot++;
        }
        player.openInventory(gui);

        // Register a temporary listener to handle the disc selection.
        Bukkit.getPluginManager().registerEvents(new Listener() {
            @EventHandler
            public void onInventoryClick(InventoryClickEvent event) {
                // Check that the click is in our custom disc GUI.
                if (!event.getView().getTitle().equals(ChatColor.DARK_AQUA + "Select a Music Disc")) {
                    return;
                }
                event.setCancelled(true);
                if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) {
                    return;
                }
                // Get the selected disc.
                ItemStack selectedDisc = event.getCurrentItem();
                // Add the disc to the player's inventory.
                player.getInventory().addItem(selectedDisc);
                player.sendMessage(ChatColor.GREEN + "You have taken the "
                        + selectedDisc.getItemMeta().getDisplayName() + "!");
                // Remove one Inscriber item from the player's hand.
                decrementItemAmount(inscriber, player);
                // Close the GUI.
                player.closeInventory();
                // Unregister this temporary listener.
                HandlerList.unregisterAll(this);
            }
        }, plugin);
    }

    // Example helper method to create a disc data map.
    private Map<Material, DiscData> getDiscDataMap() {
        Map<Material, DiscData> map = new HashMap<>();

        map.put(Material.MUSIC_DISC_11, new DiscData(
                ChatColor.DARK_RED + "Music Disc 11",
                ChatColor.RED,
                List.of(
                        ChatColor.GRAY + "Boosts monster hostility to Tier 20",
                        ChatColor.GRAY + "Lasts 20 minutes, making mobs very aggressive"
                )
        ));

        map.put(Material.MUSIC_DISC_13, new DiscData(
                ChatColor.AQUA + "Music Disc 13",
                ChatColor.AQUA,
                List.of(
                        ChatColor.GRAY + "Activates the BaroTrauma Virus for 3 minutes",
                        ChatColor.GRAY + "Infected mobs glow and drop extra XP"
                )
        ));

        map.put(Material.MUSIC_DISC_BLOCKS, new DiscData(
                ChatColor.GREEN + "Music Disc Blocks",
                ChatColor.GREEN,
                List.of(
                        ChatColor.GRAY + "Activates Recipe Writer feature",
                        ChatColor.GRAY + "Grants 32 random recipes over time"
                )
        ));

        map.put(Material.MUSIC_DISC_CAT, new DiscData(
                ChatColor.LIGHT_PURPLE + "Music Disc Cat",
                ChatColor.LIGHT_PURPLE,
                List.of(
                        ChatColor.GRAY + "Starts Harvest Frenzy for 3 minutes 5 seconds",
                        ChatColor.GRAY + "Boosts crop growth and extra resource yield"
                )
        ));

        map.put(Material.MUSIC_DISC_CHIRP, new DiscData(
                ChatColor.YELLOW + "Music Disc Chirp",
                ChatColor.YELLOW,
                List.of(
                        ChatColor.GRAY + "Triggers Timber Boost for 3 minutes 5 seconds",
                        ChatColor.GRAY + "Chance to yield bonus logs and extra Forestry XP, as well as Forest Spirits",
                        ChatColor.GRAY + "Speeds up Verdant Relic growth by 3 seconds every second"
                )
        ));

        map.put(Material.MUSIC_DISC_FAR, new DiscData(
                ChatColor.GOLD + "Music Disc Far",
                ChatColor.GOLD,
                List.of(
                        ChatColor.GRAY + "Begins a Random Loot Crate event",
                        ChatColor.GRAY + "Spawns 16 loot chests with themed drops"
                )
        ));

        map.put(Material.MUSIC_DISC_MALL, new DiscData(
                ChatColor.AQUA + "Music Disc Mall",
                ChatColor.AQUA,
                List.of(
                        ChatColor.GRAY + "Starts a 10-minute rainstorm",
                        ChatColor.GRAY + "Disables mob spawns and grants Conduit Power"
                )
        ));

        map.put(Material.MUSIC_DISC_MELLOHI, new DiscData(
                ChatColor.DARK_GREEN + "Music Disc Mellohi",
                ChatColor.DARK_GREEN,
                List.of(
                        ChatColor.GRAY + "Initiates a Zombie Apocalypse for 96 seconds",
                        ChatColor.GRAY + "Transforms spawns into zombies with extra drops"
                )
        ));

        map.put(Material.MUSIC_DISC_STAL, new DiscData(
                ChatColor.DARK_PURPLE + "Music Disc Stal",
                ChatColor.DARK_PURPLE,
                List.of(
                        ChatColor.GRAY + "Launches the Grand Auction Event",
                        ChatColor.GRAY + "Displays auction items for purchase with emeralds"
                )
        ));

        map.put(Material.MUSIC_DISC_STRAD, new DiscData(
                ChatColor.LIGHT_PURPLE + "Music Disc Strad",
                ChatColor.LIGHT_PURPLE,
                List.of(
                        ChatColor.GRAY + "Repairs your items gradually over 188 seconds",
                        ChatColor.GRAY + "Restores durability for inventory and armor"
                )
        ));

        map.put(Material.MUSIC_DISC_WAIT, new DiscData(
                ChatColor.BLUE + "Music Disc Wait",
                ChatColor.BLUE,
                List.of(
                        ChatColor.GRAY + "Activates an Experience Surge event for 231 seconds",
                        ChatColor.GRAY + "Randomly awards small amounts of XP to skills"
                )
        ));

        map.put(Material.MUSIC_DISC_WARD, new DiscData(
                ChatColor.AQUA + "Music Disc Ward",
                ChatColor.AQUA,
                List.of(
                        ChatColor.GRAY + "Rains XP near a jukebox for 251 seconds",
                        ChatColor.GRAY + "Spawns XP orbs with visual effects"
                )
        ));


        map.put(Material.MUSIC_DISC_5, new DiscData(
                ChatColor.GRAY + "Music Disc 5",
                ChatColor.GRAY,
                List.of(
                        ChatColor.GRAY + "Places regenerating emerald ore above a jukebox",
                        ChatColor.GRAY + "Mine as many emeralds as possible before the song ends"
                )
        ));

        map.put(Material.MUSIC_DISC_OTHERSIDE, new DiscData(
                ChatColor.GOLD + "Music Disc Otherside",
                ChatColor.GOLD,
                List.of(
                        ChatColor.GRAY + "Accelerates time for 195 seconds",
                        ChatColor.GRAY + "Spawns parrots or fireworks depending on time of day"
                )
        ));

        map.put(Material.MUSIC_DISC_RELIC, new DiscData(
                ChatColor.DARK_AQUA + "Music Disc Relic",
                ChatColor.DARK_AQUA,
                List.of(
                        ChatColor.GRAY + "Opens a teleportation session",
                        ChatColor.GRAY + "Choose a target biome and teleport with optional return"
                )
        ));

        return map;
    }

    // Example helper class for disc data.
    private static class DiscData {
        private final String name;
        private final ChatColor color;
        private final List<String> lore;

        public DiscData(String name, ChatColor color, List<String> lore) {
            this.name = name;
            this.color = color;
            // Optionally, prepend a gray color to each lore line.
            this.lore = new ArrayList<>();
            for (String line : lore) {
                this.lore.add(line);
            }
        }

        public String getName() {
            return name;
        }

        public ChatColor getColor() {
            return color;
        }

        public List<String> getLore() {
            return lore;
        }
    }

}
