package goat.minecraft.minecraftnew.other;

import goat.minecraft.minecraftnew.MinecraftNew;
import goat.minecraft.minecraftnew.subsystems.culinary.CulinarySubsystem;
import goat.minecraft.minecraftnew.subsystems.farming.SeederType;
import goat.minecraft.minecraftnew.utils.*;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Ageable;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.bukkit.plugin.Plugin;

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
        List<ItemStack> allRecipeItems = CulinarySubsystem.getInstance(MinecraftNew.getInstance()).getAllRecipeItems();

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
            Material.MUSIC_DISC_PIGSTEP,
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
        if (!e.getHand().equals(EquipmentSlot.HAND) && e.getHand()!=null) {
            return; // Ignore offhand interactions
        }

        if (e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK) {

            if(e.getAction().equals(Action.RIGHT_CLICK_BLOCK)){
                Player player = e.getPlayer();
                ItemStack itemInHand = player.getInventory().getItemInMainHand();

                // Validate the item in hand
                if (itemInHand == null || !itemInHand.hasItemMeta()) {
                    return;
                }

                ItemMeta meta = itemInHand.getItemMeta();
                if (meta == null || !meta.hasDisplayName()) {
                    return;
                }

                String displayName = meta.getDisplayName();
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

                // Initialize BFS structures
                Queue<Block> queue = new LinkedList<>();
                Set<Block> visited = new HashSet<>();
                int plantedCount = 0;

                queue.add(clickedBlock);
                visited.add(clickedBlock);

                while (!queue.isEmpty() && plantedCount < 1000) {
                    Block current = queue.poll();

                    // Plant the specified seed on the current FARMLAND block
                    boolean planted = plantSeed(current, seederType.getCropMaterial());
                    if (planted) {
                        plantedCount++;
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

                // Provide feedback to the player
                player.playSound(player.getLocation(), Sound.BLOCK_AZALEA_LEAVES_BREAK, 1.0f, 1.0f);
                decrementItemAmount(itemInHand, player);
                player.sendMessage(ChatColor.GREEN + "Seeds planted on " + plantedCount + " blocks!");

                // Optional: Display particles at the clicked location
                clickedBlock.getWorld().spawnParticle(Particle.VILLAGER_HAPPY, clickedBlock.getLocation().add(0.5, 1, 0.5), 50, 0.5, 1, 0.5, 0.05);
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
            if (displayName.equals(ChatColor.YELLOW + "Cookbook")) {
                player.playSound(player.getLocation(), Sound.ITEM_BOOK_PAGE_TURN, 1.0f, 1.0f);
                dropRandomRecipes(player.getLocation());
                decrementItemAmount(itemInHand, player);

                return;
            }
            if (displayName.equals(ChatColor.YELLOW + "Inscriber")) {
                player.playSound(player.getLocation(), Sound.ITEM_BOOK_PAGE_TURN, 1.0f, 1.0f);
                player.getLocation().getWorld().dropItemNaturally(player.getLocation(), getRandomMusicDisc());
                decrementItemAmount(itemInHand, player);

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
                player.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 30 * 20, 3, true));
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
                player.getWorld().spawnEntity(player.getLocation(), EntityType.VILLAGER);
                decrementItemAmount(itemInHand, player);
                return;
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
                CustomBundleGUI customBundleGUI = new CustomBundleGUI(MinecraftNew.getInstance());
                customBundleGUI.openBundleGUI(player);
                player.playSound(player.getLocation(), Sound.ITEM_ARMOR_EQUIP_LEATHER, 10, 10);

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
            if(displayName.equals(ChatColor.BLUE + "Blueprints")){
                player.playSound(player.getLocation(), Sound.ITEM_BOOK_PAGE_TURN, 10, 10);

                ReforgeItemProvider reforgeItemProvider = new ReforgeItemProvider();
                ItemStack blueprint = reforgeItemProvider.getRandomReforge();

                decrementItemAmount(itemInHand, player);
                player.getLocation().getWorld().dropItem(player.getLocation(), blueprint);
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
}
