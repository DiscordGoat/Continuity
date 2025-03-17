package goat.minecraft.minecraftnew.subsystems.music;

import goat.minecraft.minecraftnew.MinecraftNew;
import goat.minecraft.minecraftnew.other.additionalfunctionality.SpawnMonsters;
import goat.minecraft.minecraftnew.subsystems.combat.HostilityManager;
import goat.minecraft.minecraftnew.subsystems.culinary.CulinarySubsystem;
import goat.minecraft.minecraftnew.subsystems.forestry.ForestSpiritManager;
import goat.minecraft.minecraftnew.subsystems.forestry.Forestry;
import goat.minecraft.minecraftnew.subsystems.mining.PlayerOxygenManager;
import goat.minecraft.minecraftnew.subsystems.pets.PetManager;
import goat.minecraft.minecraftnew.subsystems.pets.PetRegistry;
import goat.minecraft.minecraftnew.utils.devtools.ItemRegistry;
import goat.minecraft.minecraftnew.utils.devtools.XPManager;
import goat.minecraft.minecraftnew.utils.biomeutils.BiomeMapper;
import org.bukkit.*;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.block.Jukebox;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.loot.LootContext;
import org.bukkit.loot.LootTable;
import org.bukkit.loot.LootTables;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.EulerAngle;

import java.util.*;


public class MusicDiscManager implements Listener {

    private final JavaPlugin plugin;

    // Constructor to pass the main plugin instance

    public MusicDiscManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }
    private final Map<UUID, TeleportSession> relicSessions = new HashMap<>();
    private static class TeleportSession {
        private final Player player;
        private final Location originalLocation;
        private String chosenBiome;
        private long returnDelay; // in ticks (0 if never)
        private boolean isNear;   // true = near, false = far

        public TeleportSession(Player player) {
            this.player = player;
            this.originalLocation = player.getLocation().clone();
        }
        // getters / setters ...
        public Player getPlayer() { return player; }
        public Location getOriginalLocation() { return originalLocation; }
        public String getChosenBiome() { return chosenBiome; }
        public void setChosenBiome(String chosenBiome) { this.chosenBiome = chosenBiome; }
        public long getReturnDelay() { return returnDelay; }
        public void setReturnDelay(long returnDelay) { this.returnDelay = returnDelay; }
        public boolean isNear() { return isNear; }
        public void setNear(boolean near) { this.isNear = near; }
    }

    /**
     * List of interesting biomes to show in the first GUI. You can expand this as needed.
     */

    private static final List<String> BIOME_OPTIONS = Arrays.asList(
            "Plains", "Snowy Plains", "Mushroom Field",
            "Savanna", "Forest", "Birch Forest", "Dark Forest", "Flower Forest",
            "Taiga", "Jungle", "Bamboo Jungle", "Grove", "Cherry Grove",
            "Deep Dark", "Dripstone Caves", "Lush Caves", "Jagged Peaks",
            "Meadow", "Swamp", "Mangrove Swamp", "Badlands", "Wooded Badlands",
            "Eroded Badlands", "Beach", "Desert", "Ocean", "Cold Ocean",
            "Deep Ocean", "Frozen Ocean", "Lukewarm Ocean", "Warm Ocean"
    );
    private void openBiomeGUI(Player player) {
        // We’ll use 54 slots so we can list multiple biomes
        Inventory gui = Bukkit.createInventory(null, 54, ChatColor.DARK_AQUA + "Select a Biome");

        for (int i = 0; i < BIOME_OPTIONS.size() && i < 54; i++) {
            String biomeName = BIOME_OPTIONS.get(i);

            // We'll just use GRASS_BLOCK as an icon for all.
            // You could pick different icons for each biome if you want.
            ItemStack icon = new ItemStack(Material.GRASS_BLOCK);
            ItemMeta meta = icon.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(ChatColor.GREEN + biomeName);
                icon.setItemMeta(meta);
            }
            gui.setItem(i, icon);
        }

        // open for the player
        player.openInventory(gui);
    }
    private void openDurationGUI(Player player) {
        Inventory gui = Bukkit.createInventory(null, 9, ChatColor.BLUE + "Select Duration");

        // (slot 0) 1 minute
        gui.setItem(0, makeItem(Material.CLOCK, ChatColor.YELLOW + "1 Minute"));
        // (slot 1) 5 minutes
        gui.setItem(1, makeItem(Material.CLOCK, ChatColor.YELLOW + "5 Minutes"));
        // (slot 2) 10 minutes
        gui.setItem(2, makeItem(Material.CLOCK, ChatColor.YELLOW + "10 Minutes"));
        // (slot 3) 20 minutes
        gui.setItem(3, makeItem(Material.CLOCK, ChatColor.YELLOW + "20 Minutes"));
        // (slot 4) 30 minutes
        gui.setItem(4, makeItem(Material.CLOCK, ChatColor.YELLOW + "30 Minutes"));
        // (slot 5) 1 hour
        gui.setItem(5, makeItem(Material.CLOCK, ChatColor.YELLOW + "1 Hour"));
        // (slot 6) 2 hours
        gui.setItem(6, makeItem(Material.CLOCK, ChatColor.YELLOW + "2 Hours"));
        // (slot 7) Never
        gui.setItem(7, makeItem(Material.BARRIER, ChatColor.RED + "Never"));

        player.openInventory(gui);
    }

    private void openRangeGUI(Player player) {
        Inventory gui = Bukkit.createInventory(null, 9, ChatColor.DARK_PURPLE + "Select Range");

        gui.setItem(0, makeItem(Material.ENDER_PEARL, ChatColor.GREEN + "Nearest (sub 20k blocks)"));


        player.openInventory(gui);
    }
    private ItemStack makeItem(Material mat, String displayName) {
        ItemStack stack = new ItemStack(mat);
        ItemMeta meta = stack.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(displayName);
        }
        stack.setItemMeta(meta);
        return stack;
    }
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        UUID uuid = player.getUniqueId();

        if (!relicSessions.containsKey(uuid)) return;  // Not in the middle of a relic session

        // Cancel any item pickup/move
        String title = ChatColor.stripColor(event.getView().getTitle());
        if (title == null) return;
        if (title.equalsIgnoreCase("Select a Biome") || title.equalsIgnoreCase("Select Duration") || title.equalsIgnoreCase("Select Range")) {
            event.setCancelled(true);
        }
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || !clicked.hasItemMeta()) return;

        // Get the raw, stripped title

        String clickedName = ChatColor.stripColor(clicked.getItemMeta().getDisplayName());

        TeleportSession session = relicSessions.get(uuid);

        // 1) BIOME GUI
        if (title.equalsIgnoreCase("Select a Biome")) {
            // The item name is the biome name
            session.setChosenBiome(clickedName);
            // Now open the Duration GUI
            Bukkit.getScheduler().runTask(plugin, () -> openDurationGUI(player));

            // 2) DURATION GUI
        } else if (title.equalsIgnoreCase("Select Duration")) {
            // See what they clicked
            if (clickedName.contains("1 Minute")) {
                session.setReturnDelay(1 * 60L * 20L);
            } else if (clickedName.contains("5 Minutes")) {
                session.setReturnDelay(5 * 60L * 20L);
            } else if (clickedName.contains("10 Minutes")) {
                session.setReturnDelay(10 * 60L * 20L);
            } else if (clickedName.contains("20 Minutes")) {
                session.setReturnDelay(20 * 60L * 20L);
            } else if (clickedName.contains("30 Minutes")) {
                session.setReturnDelay(30 * 60L * 20L);
            } else if (clickedName.contains("1 Hour")) {
                session.setReturnDelay(60 * 60L * 20L);
            } else if (clickedName.contains("2 Hours")) {
                session.setReturnDelay(120 * 60L * 20L);
            } else if (clickedName.contains("Never")) {
                session.setReturnDelay(0L);
            } else {
                return;
            }
            Bukkit.getScheduler().runTask(plugin, () -> openRangeGUI(player));
        } else if (title.equalsIgnoreCase("Select Range")) {
            if (clickedName.contains("Near")) {
                session.setNear(true);
            } else if (clickedName.contains("Far")) {
                session.setNear(false);
            } else {
                return;
            }
            // We have all choices now => do the teleport and end the session
            event.getWhoClicked().closeInventory();
            teleportPlayer(session);
        }
    }

    private void teleportPlayer(TeleportSession session) {
        Player player = session.getPlayer();
        String chosenBiomeName = session.getChosenBiome(); // e.g., "Snowy Plains"
        boolean near = session.isNear();
        long returnDelay = session.getReturnDelay();

        // Retrieve the corresponding Biome enum using the mapper
        Biome targetBiome = BiomeMapper.getBiome(chosenBiomeName);

        if (targetBiome == null) {
            player.sendMessage(ChatColor.RED + "Invalid biome: " + chosenBiomeName);
            relicSessions.remove(player.getUniqueId());
            plugin.getLogger().warning("Invalid biome selected: " + chosenBiomeName + " by player: " + player.getName());
            return;
        }

        String range = near ? "near" : "far";

        player.sendMessage(ChatColor.YELLOW + "Searching for a " + chosenBiomeName + " biome location...");

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            World world = player.getWorld();
            Location foundLocation = findRandomBiomeLocation(world, targetBiome, 300, 20000);

            if (foundLocation == null) {
                Bukkit.getScheduler().runTask(plugin, () -> {
                    player.sendMessage(ChatColor.RED + "No biome nearby: " + chosenBiomeName + " (near=" + near + ")");
                    relicSessions.remove(player.getUniqueId());
                });
                plugin.getLogger().warning("Biome not found: " + chosenBiomeName + " for player: " + player.getName());
                return;
            }

            Bukkit.getScheduler().runTask(plugin, () -> {
                player.teleport(foundLocation);
                player.sendMessage(ChatColor.GREEN + "Teleported you to " + chosenBiomeName + "! Return time: "
                        + (returnDelay > 0 ? (returnDelay / 20) + "s" : "Never"));

                if (returnDelay > 0) {
                    Bukkit.getScheduler().runTaskLater(plugin, () -> {
                        player.teleport(session.getOriginalLocation());
                        player.sendMessage(ChatColor.YELLOW + "Returning you to your original location...");
                        relicSessions.remove(player.getUniqueId());
                    }, returnDelay);
                } else {
                    relicSessions.remove(player.getUniqueId());
                }
            });
        });
    }

    private Location findRandomBiomeLocation(World world, Biome biome, int minRadius, int maxRadius) {
        Random random = new Random();
        Location referenceLocation = world.getSpawnLocation(); // Consider using player's current location
        int attempts = 0;
        int maxAttempts = 10000; // Increased attempts for better chances
        int skipOvers = random.nextInt(10) + 1; // Randomly determine 1-10 skipovers

        while (attempts < maxAttempts) {
            double angle = random.nextDouble() * 2 * Math.PI;
            int distance = random.nextInt(maxRadius - minRadius) + minRadius;

            int x = (int) (referenceLocation.getX() + distance * Math.cos(angle));
            int z = (int) (referenceLocation.getZ() + distance * Math.sin(angle));

            Biome biomeAtLocation = world.getBiome(x, z);

            if (biomeAtLocation == biome) {
                if (skipOvers > 0) {
                    skipOvers--;
                    continue; // Skip this positive match and continue
                }
                // Get the highest block Y to ensure the location is on the surface
                int y = world.getHighestBlockYAt(x, z);
                return new Location(world, x + 0.5, y, z + 0.5); // +0.5 for center alignment
            }

            attempts++;
        }

        return null; // No suitable location found within maxAttempts
    }


    /**
     * Event handler for player interactions.
     * Detects when a player right-clicks a jukebox.
     */
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        // Check if the action is a right-click on a block
        if (event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
            Block clickedBlock = event.getClickedBlock();

            if (clickedBlock != null && clickedBlock.getType() == Material.JUKEBOX) {
                Jukebox jukebox = (Jukebox) clickedBlock.getState();
                event.setCancelled(true);

                Player player = event.getPlayer();
                ItemStack heldItem = player.getInventory().getItemInMainHand();

                // Check if the player is holding a music disc
                if (isMusicDisc(heldItem)) {
                    Material discType = heldItem.getType();
                    identifyAndHandleDisc(player, discType);
                    player.getInventory().getItemInMainHand().setAmount(player.getInventory().getItemInMainHand().getAmount()-1);
                }
            }
        }
    }

    /**
     * Checks if the given ItemStack is a music disc.
     *
     * @param item The ItemStack to check.
     * @return True if it's a music disc, false otherwise.
     */
    private boolean isMusicDisc(ItemStack item) {
        if (item == null) return false;

        Material type = item.getType();
        return type.toString().startsWith("MUSIC_DISC_");
    }

    /**
     * Identifies the type of music disc and calls the corresponding method.
     *
     * @param player    The player who is holding the disc.
     * @param discType  The type of the music disc.
     */
    private void identifyAndHandleDisc(Player player, Material discType) {
        switch (discType) {
            case MUSIC_DISC_11:
                handleMusicDisc11(player);
                break;
            case MUSIC_DISC_13:
                handleMusicDisc13(player);
                break;
            case MUSIC_DISC_BLOCKS:
                handleMusicDiscBlocks(player);
                break;
            case MUSIC_DISC_CAT:
                handleMusicDiscCat(player);
                break;
            case MUSIC_DISC_CHIRP:
                handleMusicDiscChirp(player);
                break;
            case MUSIC_DISC_FAR:
                handleMusicDiscFar(player);
                break;
            case MUSIC_DISC_MALL:
                handleMusicDiscMall(player);
                break;
            case MUSIC_DISC_MELLOHI:
                handleMusicDiscMellohi(player);
                break;
            case MUSIC_DISC_STAL:
                handleMusicDiscStal(player);
                break;
            case MUSIC_DISC_STRAD:
                handleMusicDiscStrad(player);
                break;
            case MUSIC_DISC_WAIT:
                handleMusicDiscWait(player);
                break;
            case MUSIC_DISC_WARD:
                handleMusicDiscWard(player);
                break;
            case MUSIC_DISC_PIGSTEP:
                handleMusicDiscPigstep(player);
                break;
            case MUSIC_DISC_5:
                handleMusicDisc5(player);
                break;
            case MUSIC_DISC_RELIC:
                handleMusicDiscRelic(player, player.getLocation());
                break;
            case MUSIC_DISC_OTHERSIDE:
                handleMusicDiscOtherside(player);
                break;
            // Add more cases if there are additional discs in newer Minecraft versions
            default:
                handleUnknownMusicDisc(player, discType);
                break;
        }
    }

    private void handleMusicDiscBlocks(Player player) {
        // Broadcast the activation message to all players
        Bukkit.broadcastMessage(ChatColor.GREEN + "Recipe Writer Feature is now active!");

        // Play the MUSIC_DISC_BLOCKS sound to the activating player
        player.playSound(player.getLocation(), Sound.MUSIC_DISC_BLOCKS, 3.0f, 1.0f);

        // Get all recipe items from the CulinarySubsystem
        List<ItemStack> allRecipeItems = CulinarySubsystem.getInstance(plugin).getAllRecipeItems();

        // Define the total number of recipes to give (64)
        final int totalRecipes = 32;

        // Define the total duration of the song in ticks (345 seconds * 20 ticks per second)
        final long totalDurationTicks = 345 * 20L;

        // Calculate the interval between each recipe drop (in ticks)
        final long intervalTicks = totalDurationTicks / totalRecipes;

        // Debug: Log the interval
        plugin.getLogger().info("Recipe interval: " + intervalTicks + " ticks");

        // Schedule a repeating task to give recipes over the duration of the song
        new BukkitRunnable() {
            int recipesGiven = 0;

            @Override
            public void run() {
                if (recipesGiven >= totalRecipes || !player.isOnline()) {
                    // Cancel the task if all recipes have been given or the player is offline
                    this.cancel();
                    player.sendMessage(ChatColor.GREEN + "You have received all 32 random recipes!");
                    return;
                }

                // Randomly select a recipe from the list
                ItemStack recipeItem = allRecipeItems.get(new Random().nextInt(allRecipeItems.size())).clone();

                // Give the recipe to the player
                HashMap<Integer, ItemStack> remaining = player.getInventory().addItem(recipeItem);
                if (!remaining.isEmpty()) {
                    // If the inventory is full, drop the remaining items at the player's location
                    for (ItemStack leftover : remaining.values()) {
                        player.getWorld().dropItemNaturally(player.getLocation(), leftover);
                    }
                }

                // Notify the player
                player.sendMessage(ChatColor.YELLOW + "You received a recipe: " + recipeItem.getItemMeta().getDisplayName());

                // Increment the count of recipes given
                recipesGiven++;
            }
        }.runTaskTimer(plugin, 0L, intervalTicks); // Start immediately and repeat every intervalTicks
    }

    public void resetHostilityLevelsOnDisable() {
        Bukkit.getOnlinePlayers().forEach(player -> {
            HostilityManager hostilityManager = HostilityManager.getInstance(plugin);
            int currentTier = hostilityManager.getPlayerDifficultyTier(player);
            if (currentTier > 10) {
                hostilityManager.setPlayerTier(player, 10);
            }
        });
    }
    private void handleMusicDisc11(Player player) {
        HostilityManager hostilityManager = HostilityManager.getInstance(plugin);
        hostilityManager.setPlayerTier(player, 20);
        Bukkit.broadcastMessage(ChatColor.DARK_RED + "Somehow, you've made monsters even angrier... Hostility set to Tier 20 for 20 minutes");

        // Schedule to reset the hostility after 20 minutes (20 minutes * 60 seconds * 20 ticks)
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            hostilityManager.setPlayerTier(player, 0); // Reset hostility to default
            player.sendMessage(ChatColor.GREEN + "The increased hostility has subsided.");
        }, 20 * 60 * 20L);
    }
    private void handleMusicDiscFar(Player player) {
        // Play the music disc sound
        player.playSound(player.getLocation(), Sound.MUSIC_DISC_FAR, 3.0f, 1.0f);
        Bukkit.broadcastMessage(ChatColor.GOLD + "Random Loot crates event Activated!");

        // Define the duration of the disc in ticks (120 seconds + 54 seconds * 20 ticks per second)
        int durationTicks = (120 + 54) * 20;

        // Define the interval at which chests spawn
        int intervalTicks = durationTicks / 16; // Spawn 16 chests during the song

        // Define the list of loot tables
        List<NamespacedKey> lootTables = Arrays.asList(
                LootTables.BASTION_TREASURE.getKey(),
                LootTables.BASTION_OTHER.getKey(),
                LootTables.BASTION_BRIDGE.getKey(),
                LootTables.BASTION_HOGLIN_STABLE.getKey(),
                LootTables.DESERT_PYRAMID.getKey(),
                LootTables.END_CITY_TREASURE.getKey(),
                LootTables.IGLOO_CHEST.getKey(),
                LootTables.JUNGLE_TEMPLE.getKey(),
                LootTables.JUNGLE_TEMPLE_DISPENSER.getKey(),
                LootTables.ABANDONED_MINESHAFT.getKey(),
                LootTables.NETHER_BRIDGE.getKey(),
                LootTables.PILLAGER_OUTPOST.getKey(),
                LootTables.RUINED_PORTAL.getKey(),
                LootTables.SHIPWRECK_MAP.getKey(),
                LootTables.SHIPWRECK_SUPPLY.getKey(),
                LootTables.SHIPWRECK_TREASURE.getKey(),
                LootTables.STRONGHOLD_CORRIDOR.getKey(),
                LootTables.STRONGHOLD_CROSSING.getKey(),
                LootTables.STRONGHOLD_LIBRARY.getKey(),
                LootTables.UNDERWATER_RUIN_BIG.getKey(),
                LootTables.UNDERWATER_RUIN_SMALL.getKey(),
                LootTables.VILLAGE_ARMORER.getKey(),
                LootTables.VILLAGE_BUTCHER.getKey(),
                LootTables.VILLAGE_CARTOGRAPHER.getKey(),
                LootTables.VILLAGE_DESERT_HOUSE.getKey(),
                LootTables.VILLAGE_FISHER.getKey(),
                LootTables.VILLAGE_FLETCHER.getKey(),
                LootTables.VILLAGE_MASON.getKey(),
                LootTables.VILLAGE_PLAINS_HOUSE.getKey(),
                LootTables.VILLAGE_SAVANNA_HOUSE.getKey(),
                LootTables.VILLAGE_SHEPHERD.getKey(),
                LootTables.VILLAGE_SNOWY_HOUSE.getKey(),
                LootTables.VILLAGE_TAIGA_HOUSE.getKey(),
                LootTables.VILLAGE_TANNERY.getKey(),
                LootTables.VILLAGE_TEMPLE.getKey(),
                LootTables.VILLAGE_TOOLSMITH.getKey(),
                LootTables.VILLAGE_WEAPONSMITH.getKey(),
                LootTables.WOODLAND_MANSION.getKey()
        );

        // Schedule a repeating task to spawn chests
        new BukkitRunnable() {
            int chestsSpawned = 0;

            @Override
            public void run() {
                if (chestsSpawned >= 16 || !player.isOnline()) {
                    this.cancel(); // Stop spawning chests if the limit is reached or player is offline
                    return;
                }

                // Choose a random loot table
                NamespacedKey randomLootTable = lootTables.get(new Random().nextInt(lootTables.size()));

                // Create a custom chest item with the loot table as its name
                ItemStack chestItem = new ItemStack(Material.CHEST);
                ItemMeta meta = chestItem.getItemMeta();
                if (meta != null) {
                    meta.setDisplayName(ChatColor.GOLD + "Loot Chest: " + randomLootTable.getKey());
                    meta.getPersistentDataContainer().set(
                            new NamespacedKey(plugin, "loot_table"),
                            PersistentDataType.STRING,
                            randomLootTable.toString()
                    );
                    chestItem.setItemMeta(meta);
                }

                // Drop the chest at the player's location
                Location dropLocation = player.getLocation();
                player.getWorld().dropItemNaturally(dropLocation, chestItem);

                // Add particles and sound effects at the drop location
                dropLocation.getWorld().spawnParticle(Particle.ENCHANTMENT_TABLE, dropLocation, 50, 0.5, 1, 0.5, 0.1);
                player.getWorld().playSound(dropLocation, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.2f);

                chestsSpawned++;
            }
        }.runTaskTimer(plugin, 0L, intervalTicks);

        // Handle chest placement to drop loot from the corresponding loot table
        Bukkit.getPluginManager().registerEvents(new Listener() {
            @EventHandler
            public void onPlayerInteract(PlayerInteractEvent event) {
                if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                    Block block = event.getClickedBlock();
                    ItemStack item = event.getItem();

                    // Check if the item is a custom loot chest
                    if (item != null && item.getType() == Material.CHEST) {
                        ItemMeta meta = item.getItemMeta();
                        if (meta != null && meta.getPersistentDataContainer().has(
                                new NamespacedKey(plugin, "loot_table"),
                                PersistentDataType.STRING
                        )) {
                            // Get the loot table from the item's metadata
                            String lootTableKey = meta.getPersistentDataContainer().get(
                                    new NamespacedKey(plugin, "loot_table"),
                                    PersistentDataType.STRING
                            );

                            // Drop the loot at the chest's placement location
                            if (block != null && lootTableKey != null) {
                                NamespacedKey lootTable = NamespacedKey.fromString(lootTableKey);
                                LootTable table = Bukkit.getLootTable(lootTable);

                                if (table != null) {
                                    Location location = block.getLocation();
                                    Collection<ItemStack> loot = table.populateLoot(
                                            new Random(),
                                            new LootContext.Builder(location).build()
                                    );

                                    // Drop each item in the loot table
                                    loot.forEach(itemStack -> location.getWorld().dropItemNaturally(location, itemStack));

                                    // Add particle and sound effects
                                    location.getWorld().spawnParticle(Particle.FIREWORKS_SPARK, location.add(0, 1, 0), 100, 0.5, 1, 0.5, 0.1);
                                    location.getWorld().playSound(location, Sound.BLOCK_CHEST_OPEN, 1.0f, 1.0f);

                                    // Remove the chest from the player's inventory
                                    item.setAmount(item.getAmount() - 1);
                                }
                            }

                            event.setCancelled(true); // Prevent block placement
                        }
                    }
                }
            }
        }, plugin);
    }




    private void handleMusicDiscMall(Player player) {
        // Start a 40-minute rainstorm
        player.playSound(player.getLocation(), Sound.MUSIC_DISC_MALL, 3.0f, 1.0f);
        Bukkit.getWorlds().forEach(world -> {
            world.setStorm(true); // Start rain
            world.setWeatherDuration(10 * 60 * 20); // 10 minutes in ticks
            world.setGameRule(GameRule.DO_MOB_SPAWNING, false); // Disable monster spawns
        });

        // Notify the player and others
        Bukkit.broadcastMessage(ChatColor.AQUA + "A soothing rainstorm has begun, and monster spawns are disabled for 10 minutes!");
        player.sendMessage(ChatColor.GREEN + "You feel empowered by the rain!");

        // Grant the player Conduit Power for 40 minutes
        int durationTicks = 40 * 60 * 20; // 40 minutes in ticks
        player.addPotionEffect(new PotionEffect(PotionEffectType.CONDUIT_POWER, durationTicks, 0, true, false, false));

        // Schedule a task to reset the gamerule after the rainstorm ends
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            Bukkit.getWorlds().forEach(world -> {
                world.setGameRule(GameRule.DO_MOB_SPAWNING, true); // Re-enable monster spawns
            });

            Bukkit.broadcastMessage(ChatColor.RED + "The rainstorm has ended, and monsters are free to spawn again.");
        }, durationTicks);
    }

    // Empty methods for each music disc variant


    private static final List<ItemStack> LOOT_ITEMS = new ArrayList<>();

    static {
        LOOT_ITEMS.add(new ItemStack(Material.MUSIC_DISC_CAT));
        LOOT_ITEMS.add(new ItemStack(Material.MUSIC_DISC_BLOCKS));
        LOOT_ITEMS.add(new ItemStack(Material.MUSIC_DISC_CHIRP));
        LOOT_ITEMS.add(new ItemStack(Material.MUSIC_DISC_FAR));
        LOOT_ITEMS.add(new ItemStack(Material.MUSIC_DISC_MALL));
        LOOT_ITEMS.add(new ItemStack(Material.MUSIC_DISC_MELLOHI));
        LOOT_ITEMS.add(new ItemStack(Material.MUSIC_DISC_STAL));
        LOOT_ITEMS.add(new ItemStack(Material.MUSIC_DISC_STRAD));
        LOOT_ITEMS.add(new ItemStack(Material.MUSIC_DISC_WARD));
        LOOT_ITEMS.add(new ItemStack(Material.MUSIC_DISC_WAIT));
        LOOT_ITEMS.add(new ItemStack(Material.MUSIC_DISC_PIGSTEP)); // Rare item
        LOOT_ITEMS.add(new ItemStack(Material.MUSIC_DISC_OTHERSIDE)); // Rare item
        LOOT_ITEMS.add(new ItemStack(Material.MUSIC_DISC_RELIC)); // Rare item
    }

    /**
     * Returns a random ItemStack from the list.
     *
     * @return A randomly selected ItemStack.
     */
    public static ItemStack getRandomLootItem() {
        Random random = new Random();
        return LOOT_ITEMS.get(random.nextInt(LOOT_ITEMS.size()));
    }
    private void handleMusicDisc13(Player player) {
        // Set time to midnight
        Bukkit.getWorld("world").setTime(18000); // Midnight in Minecraft time (18,000 ticks)

        // Broadcast the activation message to all players
        Bukkit.broadcastMessage(ChatColor.AQUA + "The BaroTrauma Virus (BT) has been activated for 2 minutes 58 seconds!");
        Bukkit.broadcastMessage(ChatColor.AQUA + "Beware! BT-infected monsters are slower but carry rare loot. Don't get infected!");

        // Play the MUSIC_DISC_13 sound to the activating player
        player.playSound(player.getLocation(), Sound.MUSIC_DISC_13, 1000.0f, 1.0f);

        // Create the listener
        Listener btListener = new Listener() {
            @EventHandler
            public void onEntitySpawn(CreatureSpawnEvent event) {
                if (Math.random() < 0.3) { // 30% chance for the monster to be infected with BT
                    LivingEntity entity = event.getEntity();
                    Location location = entity.getLocation();

                    // Check if the monster is spawning on the surface
                    if (location.getWorld().getHighestBlockYAt(location) <= location.getBlockY()) {
                        entity.setCustomName(ChatColor.AQUA + "BT-Infected " + entity.getType().name());
                        entity.setCustomNameVisible(true);
                        entity.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, (2 * 60 + 58) * 20, 0)); // Aqua glowing effect
                        entity.getWorld().spawnParticle(Particle.WATER_SPLASH, entity.getLocation(), 50, 0.5, 0.5, 0.5, 0.1); // Aqua particles

                        // Apply BT behavior: Slower movement
                        entity.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, (2 * 60 + 58) * 20, 2)); // Slower movement
                        entity.getPersistentDataContainer().set(new NamespacedKey(MinecraftNew.getInstance(), "bt_monster"), PersistentDataType.BYTE, (byte) 1);
                    }
                }
            }

            @EventHandler
            public void onEntityDeath(EntityDeathEvent event) {
                if (event.getEntity().getPersistentDataContainer().has(new NamespacedKey(MinecraftNew.getInstance(), "bt_monster"), PersistentDataType.BYTE)) {
                    // Drop 20 experience orbs that explode around the entity
                    Location deathLocation = event.getEntity().getLocation();
                    World world = deathLocation.getWorld();
                        ExperienceOrb orb = (ExperienceOrb) deathLocation.getWorld().spawn(deathLocation, ExperienceOrb.class);
                        orb.setExperience(20);


                    // 10% chance to drop a random music disc
                    if (Math.random() < 0.2) {
                        event.getDrops().add(getRandomLootItem()); // Replace with a random disc if needed
                    }
                }
            }

            @EventHandler
            public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
                if (event.getDamager() instanceof LivingEntity && event.getEntity() instanceof Player) {
                    LivingEntity damager = (LivingEntity) event.getDamager();
                    Player damagedPlayer = (Player) event.getEntity();

                    // Check if the damager is a BT monster
                    if (damager.getPersistentDataContainer().has(new NamespacedKey(MinecraftNew.getInstance(), "bt_monster"), PersistentDataType.BYTE)) {
                        if (Math.random() < 0.5) { // 10% chance to infect the player with BT
                            Bukkit.broadcastMessage(ChatColor.RED + damagedPlayer.getName() + " has been infected with the BaroTrauma Virus!");
                            PlayerOxygenManager playerOxygenManager = PlayerOxygenManager.getInstance();
                            playerOxygenManager.setPlayerOxygenLevel(player, 0);
                            damagedPlayer.sendMessage(ChatColor.DARK_AQUA + "You lost your oxygen!");
                        }
                    }
                }
            }
        };

        // Register the listener
        Bukkit.getPluginManager().registerEvents(btListener, MinecraftNew.getInstance());

        // Schedule the deactivation task
        Bukkit.getScheduler().runTaskLater(MinecraftNew.getInstance(), () -> {
            Bukkit.broadcastMessage(ChatColor.RED + "The BaroTrauma Virus has subsided. Infected monsters are no longer spawning.");

            // Unregister the listener to stop spawning infected monsters
            HandlerList.unregisterAll(btListener);
        }, ((2 * 60) + 58) * 20L); // Runs after 2 minutes and 58 seconds
    }

    private void handleMusicDiscRelic(Player player, Location jukeboxLocation) {
        // Create a new TeleportSession for this player
        TeleportSession session = new TeleportSession(player);
        relicSessions.put(player.getUniqueId(), session);

        // Open the first GUI (pick a biome)
        openBiomeGUI(player);
    }

    /**
     * Spawns a cluster of 20 fireworks at random locations up to 100 blocks away from the jukebox.
     *
     * @param jukeboxLocation The location of the jukebox.
     */
    private void spawnFireworks(Location jukeboxLocation) {
        Random random = new Random();
        for (int i = 0; i < 50; i++) {
            // Generate random offset within 100 blocks
            double offsetX = (random.nextDouble() * 200) - 100; // Range: -100 to +100
            double offsetZ = (random.nextDouble() * 200) - 100; // Range: -100 to +100

            Location fireworkLocation = jukeboxLocation.clone().add(offsetX, 0, offsetZ);

            // Find the highest Y at this X,Z
            int highestY = fireworkLocation.getWorld().getHighestBlockYAt(fireworkLocation);
            fireworkLocation.setY(highestY + 1); // Spawn above the highest block

            // Spawn the Firework entity
            Firework firework = fireworkLocation.getWorld().spawn(fireworkLocation, Firework.class);
            FireworkMeta meta = firework.getFireworkMeta();

            // Random Firework Effect
            FireworkEffect effect = FireworkEffect.builder()
                    .flicker(random.nextBoolean())
                    .trail(random.nextBoolean())
                    .with(FireworkEffect.Type.values()[random.nextInt(FireworkEffect.Type.values().length)])
                    .withColor(getRandomColor())
                    .withFade(getRandomColor())
                    .build();

            meta.addEffect(effect);
            meta.setPower(random.nextInt(2) + 1); // Power between 1-2
            firework.setFireworkMeta(meta);
        }
    }
    /**
     * Generates a random color.
     *
     * @return A randomly generated Color.
     */
    private Color getRandomColor() {
        Random random = new Random();
        float r = random.nextFloat();
        float g = random.nextFloat();
        float b = random.nextFloat();
        return Color.fromRGB((int) (r * 255), (int) (g * 255), (int) (b * 255));
    }

    /**
     * Handles the MUSIC_DISC_RELIC functionality.
     * This method was previously defined and implemented above.
     */

    private void handleMusicDiscCat(Player player) {
        // Broadcast the activation message to all players
        Bukkit.broadcastMessage(ChatColor.GREEN + "Harvest Frenzy Activated for 3 minutes 5 seconds!");

        // Play the MUSIC_DISC_CAT sound to the activating player
        player.playSound(player.getLocation(), Sound.MUSIC_DISC_CAT, 3.0f, 1.0f);

        // Set the randomTickSpeed gamerule to 1000
        Bukkit.getWorlds().forEach(world -> {
            world.setGameRule(GameRule.RANDOM_TICK_SPEED, 1000);
        });

        // Schedule a task to reset the randomTickSpeed after 3 minutes (3 * 60 * 20 ticks)
        Bukkit.getScheduler().runTaskLater(MinecraftNew.getInstance(), () -> {
            Bukkit.getWorlds().forEach(world -> {
                world.setGameRule(GameRule.RANDOM_TICK_SPEED, 3); // Reset to default value
            });
            Bukkit.broadcastMessage(ChatColor.RED + "Harvest Frenzy has ended. Tick speed has been reset.");
        }, ((3 * 60)+(5)) * 20L); // 3 minutes in ticks (20 ticks per second)
    }


    private void handleMusicDiscChirp(Player player) {
        XPManager xpManager = new XPManager(plugin);
        Bukkit.broadcastMessage(ChatColor.YELLOW + "Timber Boost! Bonus log-chopping event activated for 3 minutes and 5 seconds!");
        player.playSound(player.getLocation(), Sound.MUSIC_DISC_CHIRP, 1000.0f, 1.0f);

        // Rapidly increase notoriety by 1 every tick for the duration of the song (185 seconds)
        int durationTicks = 185 * 20; // 185 seconds * 20 ticks per second
        new BukkitRunnable() {
            int ticks = 0;
            @Override
            public void run() {
                if (ticks >= durationTicks) {
                    cancel();
                    return;
                }
                // Increment notoriety by 1 every tick
                Forestry.getInstance(MinecraftNew.getInstance()).incrementNotoriety(player);
                ticks++;
            }
        }.runTaskTimer(plugin, 0L, 1L);

        // Listener for block-breaking events during the Timber Boost
        Listener logBreakListener = new Listener() {
            @EventHandler
            public void onLogChop(org.bukkit.event.block.BlockBreakEvent event) {
                // Ensure the event is triggered by the same player who activated the disc
                if (!event.getPlayer().equals(player)) {
                    return;
                }

                // Check if the broken block is one of the logs we're interested in
                Material brokenMaterial = event.getBlock().getType();
                if (brokenMaterial == Material.OAK_LOG ||
                        brokenMaterial == Material.BIRCH_LOG ||
                        brokenMaterial == Material.SPRUCE_LOG ||
                        brokenMaterial == Material.JUNGLE_LOG ||
                        brokenMaterial == Material.ACACIA_LOG ||
                        brokenMaterial == Material.DARK_OAK_LOG) {

                    // Determine the bonus logs based on a roll chance
                    double roll = Math.random() * 100; // 0 to 100
                    int bonusLogs = 0;
                    Sound jingle = Sound.BLOCK_NOTE_BLOCK_BASS; // default sound
                    float pitch = 1.0f;

                    if (roll <= 0.2) { // Legendary (0.2% chance)
                        bonusLogs = 64;
                        jingle = Sound.ENTITY_ENDER_DRAGON_GROWL;
                        pitch = 2.0f;
                        xpManager.addXP(player, "Forestry", 500);
                    } else if (roll <= 1) { // Epic (1% chance)
                        bonusLogs = 32;
                        jingle = Sound.UI_TOAST_CHALLENGE_COMPLETE;
                        pitch = 1.8f;
                        xpManager.addXP(player, "Forestry", 250);
                    } else if (roll <= 2) { // Rare (2% chance)
                        bonusLogs = 16;
                        jingle = Sound.ENTITY_PLAYER_LEVELUP;
                        pitch = 1.5f;
                        xpManager.addXP(player, "Forestry", 175);
                    } else if (roll <= 3.5) { // Uncommon (3.5% chance)
                        bonusLogs = 8;
                        jingle = Sound.BLOCK_NOTE_BLOCK_PLING;
                        pitch = 1.2f;
                        xpManager.addXP(player, "Forestry", 100);
                    } else if (roll <= 5.5) { // Common (5.5% chance)
                        bonusLogs = 4;
                        jingle = Sound.BLOCK_NOTE_BLOCK_BASS;
                        pitch = 1.0f;
                        xpManager.addXP(player, "Forestry", 50);
                    }

                    // If bonus logs were rolled, drop them and notify the player
                    if (bonusLogs > 0) {
                        ItemStack bonusLogStack = new ItemStack(brokenMaterial, bonusLogs);
                        event.getBlock().getWorld().dropItem(event.getBlock().getLocation(), bonusLogStack);
                        player.playSound(player.getLocation(), jingle, 3.0f, pitch);
                        player.sendMessage(ChatColor.GOLD + "Bonus Logs! You received " + bonusLogs + " extra " +
                                brokenMaterial.name().toLowerCase().replace("_", " ") + "!");
                    }
                }
            }
        };

        // Register the listener
        Bukkit.getPluginManager().registerEvents(logBreakListener, plugin);

        // Unregister the listener after 185 seconds (3 minutes and 5 seconds)
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            org.bukkit.event.HandlerList.unregisterAll(logBreakListener);
            player.sendMessage(ChatColor.RED + "The Timber Boost event has ended!");
        }, 185 * 20L);
    }


    private void handleMusicDiscMellohi(Player player) {
        // Play the Mellohi music disc sound
        player.playSound(player.getLocation(), Sound.MUSIC_DISC_MELLOHI, 100000, 1.0f);
        Bukkit.getWorld("world").setTime(18000);
        player.sendMessage(ChatColor.RED + "The Zombie Apocalypse has begun!");

        // Set the duration of the effect (1:36 = 96 seconds)
        int durationTicks = 96 * 20;

        // Listener to transform all spawned monsters into zombies
        Listener entitySpawnListener = new Listener() {
            @EventHandler
            public void onEntitySpawn(CreatureSpawnEvent event) {
                if (event.getEntity() instanceof Monster && !(event.getEntity() instanceof Zombie)) {
                    Location spawnLocation = event.getLocation();
                    event.setCancelled(true); // Cancel original monster spawn
                    Zombie zombie = (Zombie) spawnLocation.getWorld().spawnEntity(spawnLocation, EntityType.ZOMBIE);
                    zombie.getWorld().playSound(spawnLocation, Sound.ENTITY_ZOMBIE_AMBIENT, 1.0f, 1.0f);
                }
            }
        };

        // Listener to handle zombie interactions
        Listener zombieDamageListener = new Listener() {
            @EventHandler
            public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
                if (event.getEntity() instanceof Player && event.getDamager() instanceof Zombie) {
                    Player victim = (Player) event.getEntity();
                    HostilityManager hostilityManager = HostilityManager.getInstance(plugin);
                    int hostilityLevel = hostilityManager.getPlayerDifficultyTier(player);
                    // 10% chance to infect player with lethal Wither
                    if (Math.random() < 0.10 * hostilityLevel) {
                        victim.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 200, 2)); // Wither effect for 10 seconds, level 2
                        victim.sendMessage(ChatColor.DARK_RED + "You've been infected!");
                    }
                }
            }
        };

        // Listener to modify zombie drops
        Listener zombieDropListener = new Listener() {
            @EventHandler
            public void onEntityDeath(EntityDeathEvent event) {
                if (event.getEntity() instanceof Zombie) {
                    // Drop 1-2 emeralds
                    int emeralds = 2 + (Math.random() < 0.5 ? 1 : 0);
                    event.getDrops().add(new ItemStack(Material.EMERALD, emeralds));
                }
            }
        };

        // Register listeners
        Bukkit.getPluginManager().registerEvents(entitySpawnListener, MinecraftNew.getInstance());
        Bukkit.getPluginManager().registerEvents(zombieDamageListener, MinecraftNew.getInstance());
        Bukkit.getPluginManager().registerEvents(zombieDropListener, MinecraftNew.getInstance());

        // Schedule a task to end the effect after 1:36 (96 seconds)
        Bukkit.getScheduler().runTaskLater(MinecraftNew.getInstance(), () -> {
            // Unregister listeners to stop the effect
            HandlerList.unregisterAll(entitySpawnListener);
            HandlerList.unregisterAll(zombieDamageListener);
            HandlerList.unregisterAll(zombieDropListener);

            // Notify the player that the effect has ended
            player.sendMessage(ChatColor.RED + "The Zombie Apocalypse has ended.");
        }, durationTicks);
    }


    private void handleMusicDiscStal(Player player) {
        // Find the nearest jukebox to the player
        Block nearestJukeboxBlock = findNearestJukebox(player.getLocation(), 20); // Search radius of 20 blocks

        if (nearestJukeboxBlock == null) {
            player.sendMessage(ChatColor.RED + "No jukebox found nearby to start the auction event.");
            return; // Exit if no jukebox is found
        }

        // Position of the jukebox
        Location jukeboxLocation = nearestJukeboxBlock.getLocation();

        // Play the music disc at the jukebox location
        player.getWorld().playSound(jukeboxLocation, Sound.MUSIC_DISC_STAL, SoundCategory.RECORDS, 100000, 1.0f);
        player.sendMessage(ChatColor.DARK_PURPLE + "The Grand Auction Event has begun!");

        // List of auction items (populate this manually)
        List<AuctionItem> auctionItems = new ArrayList<>();
        ItemStack enderDrop = ItemRegistry.getEnderDrop();
        ItemStack undeadDrop = ItemRegistry.getUndeadDrop();
        ItemStack creeperDrop = ItemRegistry.getCreeperDrop();
        ItemStack spiderDrop = ItemRegistry.getSpiderDrop();
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
        ItemStack singularity = ItemRegistry.getSingularity();
        ItemStack hireVillager = ItemRegistry.getHireVillager();
        ItemStack leviathanHeart = ItemRegistry.getLeviathanHeart();
        ItemStack abyssalInk = ItemRegistry.getAbyssalInk();
        ItemStack abyssalShell = ItemRegistry.getAbyssalShell();
        ItemStack abyssalVenom = ItemRegistry.getAbyssalVenom();
        ItemStack trident = ItemRegistry.getTrident();
        ItemStack forbiddenBook = ItemRegistry.getForbiddenBook();
        ItemStack mithrilChunk = ItemRegistry.getMithrilChunk();
        ItemStack perfectDiamond = ItemRegistry.getPerfectDiamond();

        auctionItems.add(new AuctionItem(new ItemStack(Material.DIAMOND), 12));
        auctionItems.add(new AuctionItem(new ItemStack(Material.ENCHANTED_GOLDEN_APPLE), 64));
        auctionItems.add(new AuctionItem(trident, 16));
        auctionItems.add(new AuctionItem(enderDrop, 64));
        auctionItems.add(new AuctionItem(undeadDrop, 32));
        auctionItems.add(new AuctionItem(skeletonDrop, 32));
        auctionItems.add(new AuctionItem(creeperDrop, 64));
        auctionItems.add(new AuctionItem(spiderDrop, 32));
        auctionItems.add(new AuctionItem(blazeDrop, 32));
        auctionItems.add(new AuctionItem(witchDrop, 128));
        auctionItems.add(new AuctionItem(witherSkeletonDrop, 32));
        auctionItems.add(new AuctionItem(guardianDrop, 32));
        auctionItems.add(new AuctionItem(elderGuardianDrop, 32));
        auctionItems.add(new AuctionItem(pillagerDrop, 32));
        auctionItems.add(new AuctionItem(vindicatorDrop, 32));
        auctionItems.add(new AuctionItem(piglinDrop, 32));
        auctionItems.add(new AuctionItem(piglinBruteDrop, 32));
        auctionItems.add(new AuctionItem(zombifiedPiglinDrop, 32));
        auctionItems.add(new AuctionItem(drownedDrop, 32));
        auctionItems.add(new AuctionItem(skeletonDrop, 32));
        auctionItems.add(new AuctionItem(singularity, 16));
        auctionItems.add(new AuctionItem(hireVillager, 64));
        auctionItems.add(new AuctionItem(leviathanHeart, 64));
        auctionItems.add(new AuctionItem(abyssalInk, 64));
        auctionItems.add(new AuctionItem(abyssalVenom, 64));
        auctionItems.add(new AuctionItem(abyssalShell, 64));
        auctionItems.add(new AuctionItem(forbiddenBook, 16));
        auctionItems.add(new AuctionItem(mithrilChunk, 16));
        auctionItems.add(new AuctionItem(perfectDiamond, 64));

        // Randomly select 5 unique items from the auctionItems list
        List<AuctionItem> selectedItems = new ArrayList<>();
        List<AuctionItem> itemsCopy = new ArrayList<>(auctionItems);

        Random random = new Random();
        int itemsToSelect = Math.min(5, itemsCopy.size()); // Ensure we don't try to select more items than are available

        for (int i = 0; i < itemsToSelect; i++) {
            int randomIndex = random.nextInt(itemsCopy.size());
            AuctionItem selectedItem = itemsCopy.remove(randomIndex);
            selectedItems.add(selectedItem);
        }

        // Map to keep track of which items each player has purchased
        Map<UUID, Set<Integer>> purchasedItems = new HashMap<>();

        // List to store the ArmorStands
        List<ArmorStand> armorStands = new ArrayList<>();

        // Schedule the tasks to display items
        for (int i = 0; i < selectedItems.size(); i++) {
            final int index = i;
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                // Remove previous item if any
                if (index > 0 && index - 1 < armorStands.size()) {
                    ArmorStand previousArmorStand = armorStands.get(index - 1);
                    if (previousArmorStand != null && !previousArmorStand.isDead()) {
                        previousArmorStand.remove();
                    }
                }

                // Get the selected item at this index
                AuctionItem auctionItem = selectedItems.get(index);

                // Display the item over the jukebox
                ArmorStand armorStand = (ArmorStand) jukeboxLocation.getWorld().spawnEntity(
                        jukeboxLocation.clone().add(0.5, 1, 0.5), EntityType.ARMOR_STAND);
                armorStand.setVisible(false);
                armorStand.setGravity(false);
                armorStand.setSmall(true);
                armorStand.setCustomNameVisible(true);

                // Set the custom name to show the item name and cost
                String itemName = auctionItem.getItemStack().getItemMeta().hasDisplayName()
                        ? auctionItem.getItemStack().getItemMeta().getDisplayName()
                        : auctionItem.getItemStack().getType().name();
                armorStand.setCustomName(ChatColor.GREEN + itemName + ChatColor.YELLOW +
                        " - " + auctionItem.getEmeraldCost() + " Emeralds");
                armorStand.setHelmet(auctionItem.getItemStack());
                armorStand.setInvulnerable(true);
                // armorStand.setMarker(true); // Commented out to enable interaction
                armorStands.add(armorStand);

                // Make the ArmorStand spin
                new BukkitRunnable() {
                    double angle = 0;

                    @Override
                    public void run() {
                        if (armorStand.isDead()) {
                            this.cancel();
                        } else {
                            angle += Math.PI / 16;
                            if (angle >= 2 * Math.PI) {
                                angle = 0;
                            }
                            armorStand.setHeadPose(new EulerAngle(0, angle, 0));
                        }
                    }
                }.runTaskTimer(plugin, 1L, 1L); // Corrected scheduling

                // Add a large particle effect when a new item is displayed
                armorStand.getWorld().spawnParticle(
                        Particle.FIREWORKS_SPARK,
                        armorStand.getLocation().add(0, 1, 0),
                        100, // Number of particles
                        0.5, 0.5, 0.5, // Offset
                        0.1 // Speed
                );

                // Play a sound when a new item is displayed
                armorStand.getWorld().playSound(
                        armorStand.getLocation(),
                        Sound.ENTITY_FIREWORK_ROCKET_LAUNCH,
                        SoundCategory.MASTER,
                        1.0f, // Volume
                        1.0f  // Pitch
                );
            }, i * 30 * 20L); // Delay in ticks (i * 30 seconds * 20 ticks per second)
        }

        // Schedule a task to remove the last item after the song ends
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            // Remove last ArmorStand
            if (!armorStands.isEmpty()) {
                ArmorStand lastArmorStand = armorStands.get(armorStands.size() - 1);
                if (lastArmorStand != null && !lastArmorStand.isDead()) {
                    lastArmorStand.remove();
                }
            }
            player.sendMessage(ChatColor.DARK_PURPLE + "The Grand Auction Event has ended!");
        }, 150 * 20L); // 150 seconds

        // Add particle effects around the jukebox
        new BukkitRunnable() {
            int ticksRun = 0;

            @Override
            public void run() {
                if (ticksRun >= 150 * 20L) {
                    this.cancel();
                } else {
                    // Generate particle effects around the jukebox
                    jukeboxLocation.getWorld().spawnParticle(Particle.ENCHANTMENT_TABLE,
                            jukeboxLocation.clone().add(0.5, 1, 0.5), 20, 0.5, 1, 0.5, 0);
                    ticksRun += 20;
                }
            }
        }.runTaskTimer(plugin, 0L, 20L); // Corrected scheduling

        // Handle player interactions
        Listener interactionListener = new Listener() {
            @EventHandler
            public void onPlayerInteractAtEntity(PlayerInteractAtEntityEvent event) {
                Player interactingPlayer = event.getPlayer();
                Entity entity = event.getRightClicked();

                // Debug message
                // Check if the entity is one of our ArmorStands
                if (entity instanceof ArmorStand) {
                    if (armorStands.contains(entity)) {
                        event.setCancelled(true); // Prevent default interaction
                        int itemIndex = armorStands.indexOf(entity);

                        if (itemIndex >= 0 && itemIndex < selectedItems.size()) {
                            AuctionItem auctionItem = selectedItems.get(itemIndex);
                            // Check if the player has already purchased this item
                            Set<Integer> playerPurchasedItems = purchasedItems.getOrDefault(
                                    interactingPlayer.getUniqueId(), new HashSet<>());
                            if (playerPurchasedItems.contains(itemIndex)) {
                                interactingPlayer.sendMessage(ChatColor.RED + "You have already purchased this item.");
                            } else {
                                int emeraldCost = auctionItem.getEmeraldCost();

                                // Process the purchase
                                boolean success = processPurchase(interactingPlayer, emeraldCost, auctionItem.getItemStack());

                                if (success) {
                                    // Mark the item as purchased
                                    playerPurchasedItems.add(itemIndex);
                                    purchasedItems.put(interactingPlayer.getUniqueId(), playerPurchasedItems);

                                    // Place particle effect and sound when a player purchases an item
                                    interactingPlayer.playSound(interactingPlayer.getLocation(),
                                            Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
                                    interactingPlayer.spawnParticle(Particle.VILLAGER_HAPPY,
                                            interactingPlayer.getLocation(), 20, 0.5, 0.5, 0.5);
                                }
                            }
                        } else {
                            interactingPlayer.sendMessage(ChatColor.RED + "No auction item found at this index.");
                        }
                    } else {
                        //interactingPlayer.sendMessage(ChatColor.GRAY + "ArmorStand is not part of the auction.");
                    }
                }
            }
        };
        Bukkit.getPluginManager().registerEvents(interactionListener, plugin);

        // Unregister the interaction listener after the event ends
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            HandlerList.unregisterAll(interactionListener);
        }, 150 * 20L); // 150 seconds
    }


    // Method to find the nearest jukebox within a certain radius
    private Block findNearestJukebox(Location location, int radius) {
        World world = location.getWorld();
        Block nearestJukebox = null;
        double nearestDistanceSquared = Double.MAX_VALUE;
        int cx = location.getBlockX();
        int cy = location.getBlockY();
        int cz = location.getBlockZ();

        for (int x = cx - radius; x <= cx + radius; x++) {
            for (int y = cy - radius; y <= cy + radius; y++) {
                for (int z = cz - radius; z <= cz + radius; z++) {
                    Block block = world.getBlockAt(x, y, z);
                    if (block.getType() == Material.JUKEBOX) {
                        double distanceSquared = block.getLocation().distanceSquared(location);
                        if (distanceSquared < nearestDistanceSquared) {
                            nearestDistanceSquared = distanceSquared;
                            nearestJukebox = block;
                        }
                    }
                }
            }
        }
        return nearestJukebox;
    }

    // Method to check if the player has enough items
    private boolean hasEnoughItems(Inventory inventory, Material material, int amount) {
        int total = 0;
        for (ItemStack item : inventory.getContents()) {
            if (item != null && item.getType() == material) {
                total += item.getAmount();
                if (total >= amount) return true; // Sufficient items found
            }
        }
        return false; // Not enough items
    }

    // Method to remove items from the player's inventory
    private void removeItems(Inventory inventory, Material material, int amount) {
        int remaining = amount;
        for (ItemStack item : inventory.getContents()) {
            if (item != null && item.getType() == material) {
                int itemAmount = item.getAmount();
                if (itemAmount <= remaining) {
                    // Remove the whole stack
                    inventory.remove(item);
                    remaining -= itemAmount;
                } else {
                    // Partially reduce the stack
                    item.setAmount(itemAmount - remaining);
                    remaining = 0;
                    break; // No need to process further
                }
            }
        }
        if (remaining > 0) {
            throw new IllegalStateException("Inventory state mismatch: not enough items removed.");
        }
    }


    // Method to process the purchase
    public boolean processPurchase(Player player, int cost, ItemStack item) {
        Inventory inventory = player.getInventory();
        Material emeraldMaterial = Material.EMERALD;

        // Validate enough emeralds are available
        if (!hasEnoughItems(inventory, emeraldMaterial, cost)) {
            player.sendMessage(ChatColor.RED + "You don't have enough emeralds.");
            return false;
        }

        // Remove emeralds and give the item
        removeItems(inventory, emeraldMaterial, cost);
        ItemStack itemToGive = item.clone();
        itemToGive.setAmount(1); // Ensure only one item is given
        inventory.addItem(itemToGive);

        // Feedback and sound
        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.2f);
        return true;
    }


    // AuctionItem class to hold the item and its emerald cost
    class AuctionItem {
        private ItemStack itemStack;
        private int emeraldCost;

        public AuctionItem(ItemStack itemStack, int emeraldCost) {
            this.itemStack = itemStack;
            this.emeraldCost = emeraldCost;
        }

        public ItemStack getItemStack() {
            return itemStack;
        }

        public int getEmeraldCost() {
            return emeraldCost;
        }
    }




    private void handleMusicDiscStrad(Player player) {
        // Play the music disc sound
        player.playSound(player.getLocation(), Sound.MUSIC_DISC_STRAD, 3.0f, 1.0f);
        player.sendMessage(ChatColor.GREEN + "Your items are being repaired!");
        plugin.getLogger().info("MusicDiscStrad event started for player: " + player.getName());

        // Duration of the song in ticks (188 seconds * 20 ticks per second)
        int durationTicks = 188 * 20;

        // Interval in ticks (20 ticks = 1 second)
        int intervalTicks = 10;

        // Total number of cycles (188 cycles)
        int totalCycles = durationTicks / intervalTicks;

        // Total durability to restore per item: 308
        // Durability per cycle per item: 308 / 188 ≈ 1.638
        double durabilityPerCycle = 608.0 / totalCycles;

        // Schedule a repeating task to repair items
        new BukkitRunnable() {
            int cyclesRun = 0;
            double accumulatedDurability = 0.0;

            @Override
            public void run() {
                plugin.getLogger().info("Repair Cycle " + cyclesRun + " started for player: " + player.getName());

                if (cyclesRun >= totalCycles) {
                    this.cancel();
                    player.sendMessage(ChatColor.RED + "The repair effect has ended!");
                    plugin.getLogger().info("MusicDiscStrad event ended for player: " + player.getName());
                    return;
                }

                // Accumulate the durability to restore
                accumulatedDurability += durabilityPerCycle;
                int repairAmount = (int) Math.floor(accumulatedDurability);
                accumulatedDurability -= repairAmount;

                if (repairAmount > 0) {
                    plugin.getLogger().info("Cycle " + cyclesRun + ": Repairing items by " + repairAmount + " durability points.");
                    // Repair items in inventory and armor
                    repairPlayerItems(player, repairAmount);
                } else {
                    plugin.getLogger().info("Cycle " + cyclesRun + ": No repair this cycle.");
                }

                cyclesRun++;
            }
        }.runTaskTimer(plugin, 0L, intervalTicks); // Start immediately, repeat every second
    }

    private void repairPlayerItems(Player player, int repairAmount) {
        plugin.getLogger().info("Starting to repair items for player: " + player.getName());

        // Repair items in main inventory and off-hand
        ItemStack[] inventoryContents = player.getInventory().getContents();
        for (int i = 0; i < inventoryContents.length; i++) {
            ItemStack item = inventoryContents[i];
            if (item != null) {
                repairItem(item, repairAmount, "Inventory Slot " + i);
            }
        }

        // Repair items in armor slots
        ItemStack[] armorContents = player.getInventory().getArmorContents();
        String[] armorSlots = {"Helmet", "Chestplate", "Leggings", "Boots"};
        for (int i = 0; i < armorContents.length; i++) {
            ItemStack armorItem = armorContents[i];
            if (armorItem != null) {
                repairItem(armorItem, repairAmount, "Armor Slot " + armorSlots[i]);
            }
        }

        // Update player's inventory to reflect changes
        player.updateInventory();
        plugin.getLogger().info("Finished repairing items for player: " + player.getName());
    }

    private void repairItem(ItemStack item, int repairAmount, String slotInfo) {
        if (item == null) return;

        Material material = item.getType();
        short maxDurability = material.getMaxDurability();

        if (maxDurability > 0) {
            // Item is damageable
            short currentDurability = item.getDurability(); // Deprecated but works
            short newDurability = (short) Math.max(currentDurability - repairAmount, 0);

            // Debug messages
            plugin.getLogger().info("Item: " + material);
            plugin.getLogger().info("Current Durability (Damage): " + currentDurability);
            plugin.getLogger().info("Max Durability: " + maxDurability);
            plugin.getLogger().info("Repairing " + material + " in " + slotInfo + " from damage " + currentDurability + " to " + newDurability);

            item.setDurability(newDurability); // Deprecated but necessary here
        } else {
            plugin.getLogger().info(material + " in " + slotInfo + " is not damageable.");
        }
    }





    public void handleMusicDiscWait(Player player) {
        XPManager xpManager = new XPManager(plugin);

        // Find the nearest jukebox to the player
        Block nearestJukeboxBlock = findNearestJukebox(player.getLocation(), 20); // Search radius of 20 blocks

        if (nearestJukeboxBlock == null) {
            player.sendMessage(ChatColor.RED + "No jukebox found nearby to start the experience surge.");
            return; // Exit if no jukebox is found
        }

        // Position of the jukebox
        Location jukeboxLocation = nearestJukeboxBlock.getLocation();

        // Play the music disc sound at the jukebox location
        player.getWorld().playSound(jukeboxLocation, Sound.MUSIC_DISC_WAIT, SoundCategory.RECORDS, 3.0f, 1.0f);
        player.sendMessage(ChatColor.GREEN + "You feel a surge of experience flowing through you!");

        // Duration of the song in ticks (231 seconds * 20 ticks per second)
        int durationTicks = 231 * 20;

        // Interval in ticks (20 ticks = 1 second)
        int intervalTicks = 20;

        // Total number of cycles (231 cycles)
        int totalCycles = durationTicks / intervalTicks;

        // List of skills
        String[] skills = {"Combat", "Fishing", "Forestry", "Mining", "Farming", "Bartering", "Smithing", "Culinary"};

        // Colors for each skill
        Map<String, ChatColor> skillColors = new HashMap<>();
        skillColors.put("Combat", ChatColor.RED);
        skillColors.put("Fishing", ChatColor.AQUA);
        skillColors.put("Forestry", ChatColor.DARK_GREEN);
        skillColors.put("Mining", ChatColor.GRAY);
        skillColors.put("Farming", ChatColor.YELLOW);
        skillColors.put("Bartering", ChatColor.GREEN);
        skillColors.put("Culinary", ChatColor.YELLOW
        );

        Random random = new Random();

        // Schedule a repeating task to give XP and display messages
        new BukkitRunnable() {
            int cyclesRun = 0;

            @Override
            public void run() {
                if (cyclesRun >= totalCycles) {
                    this.cancel();
                    player.sendMessage(ChatColor.GREEN + "The experience surge has ended.");
                    return;
                }

                // Select one random skill
                String skill = skills[random.nextInt(skills.length)];

                int xp = random.nextInt(50) + 1; // Random XP between 1 and 50

                // Call addXP method
                xpManager.addXP(player, skill, xp);

                // Display the XP given using an ArmorStand
                // Spawn an invisible ArmorStand over the jukebox
                Location loc = jukeboxLocation.clone().add(0.5, 1.0, 0.5);
                ArmorStand armorStand = (ArmorStand) player.getWorld().spawnEntity(loc, EntityType.ARMOR_STAND);
                armorStand.setVisible(false);
                armorStand.setGravity(false);
                armorStand.setCustomNameVisible(true);
                armorStand.setMarker(true); // Makes the ArmorStand have no hitbox and be small

                ChatColor color = skillColors.getOrDefault(skill, ChatColor.WHITE);
                armorStand.setCustomName(color + "+" + xp + " " + skill + " XP");

                // Schedule the ArmorStand to be removed after 2 seconds (40 ticks)
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        armorStand.remove();
                    }
                }.runTaskLater(plugin, 20L);

                cyclesRun++;
            }
        }.runTaskTimer(plugin, 0L, intervalTicks);
    }

    // Method to find the nearest jukebox within a certain radius



    public void handleMusicDiscWard(Player player) {
        XPManager xpManager = new XPManager(plugin);

        // Find the nearest jukebox to the player
        Block nearestJukeboxBlock = findNearestJukebox(player.getLocation(), 20); // Search radius of 20 blocks

        if (nearestJukeboxBlock == null) {
            player.sendMessage(ChatColor.RED + "No jukebox found nearby to start the XP rain.");
            return; // Exit if no jukebox is found
        }

        // Position of the jukebox
        Location jukeboxLocation = nearestJukeboxBlock.getLocation();

        // Play the music disc sound at the jukebox location
        player.getWorld().playSound(jukeboxLocation, Sound.MUSIC_DISC_WARD, SoundCategory.RECORDS, 3.0f, 1.0f);
        player.sendMessage(ChatColor.GREEN + "The mystical music fills the air, raining experience!");

        // Duration of the song in ticks (251 seconds * 20 ticks per second)
        int durationTicks = 251 * 20;

        // Interval in ticks (3 ticks per XP spawn)
        int intervalTicks = 3;

        // Total number of cycles
        int totalCycles = durationTicks / intervalTicks;

        Random random = new Random();

        // Schedule a repeating task to spawn XP orbs
        new BukkitRunnable() {
            int cyclesRun = 0;

            @Override
            public void run() {
                if (cyclesRun >= totalCycles) {
                    this.cancel();
                    player.sendMessage(ChatColor.GREEN + "The XP rain has ended.");
                    return;
                }

                // Random XP amount (1-2)
                int xpAmount = random.nextInt(1) + 1;

                // Spawn the XP orb at the jukebox location
                player.getWorld().spawn(jukeboxLocation.clone().add(0.5, 1.0, 0.5), ExperienceOrb.class, orb -> orb.setExperience(xpAmount));

                // Notify the player with a message above the jukebox
                Location loc = jukeboxLocation.clone().add(0.5, 1.0, 0.5);
                ArmorStand armorStand = (ArmorStand) player.getWorld().spawnEntity(loc, EntityType.ARMOR_STAND);
                armorStand.setVisible(false);
                armorStand.setGravity(false);
                armorStand.setCustomNameVisible(true);
                armorStand.setMarker(true); // No hitbox

                armorStand.setCustomName(ChatColor.AQUA + "+" + xpAmount + " XP");

                // Schedule the ArmorStand to be removed after 2 seconds (40 ticks)
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        armorStand.remove();
                    }
                }.runTaskLater(plugin, 40L);

                cyclesRun++;
            }
        }.runTaskTimer(plugin, 0L, intervalTicks);
    }

    private void handleMusicDiscPigstep(Player player) {
        // Play the disc sound


        // Get the custom Nether world (assumes a world named "custom_nether" exists)
        World customNether = Bukkit.getWorld("custom_nether");
        if (customNether == null) {
            player.sendMessage(ChatColor.RED + "Custom Nether not found!");
            return;
        }

        // Define the location of the large, circular bastion-esque platform in the center of a lava ocean.
        // (Adjust these coordinates as necessary for your custom nether.)
        Location platformCenter = new Location(customNether, 0, 103, 0);

        // Teleport the player to the custom nether platform.
        player.teleport(platformCenter);

        // Spawn the boss in the center of the platform.
        // Boss level should be 300. (Leave the custom boss attribute code blank for now.)
        Entity boss = customNether.spawnEntity(platformCenter, EntityType.PIGLIN_BRUTE);
        boss.setCustomNameVisible(true);

        SpawnMonsters spawnMonsters = SpawnMonsters.getInstance(new XPManager(MinecraftNew.getInstance()));
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            spawnMonsters.applyMobAttributes((LivingEntity) boss, 300);
            player.playSound(player.getLocation(), Sound.MUSIC_DISC_PIGSTEP, 300.0f, 1.0f);
        }, 41L);
        boss.setCustomName(ChatColor.RED + "[Lvl 300] Boss"); // Placeholder name
        ((LivingEntity) boss).addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, Integer.MAX_VALUE, 2));
        // Register a listener to handle if the player dies during the event.
        PlayerDeathListener deathListener = new PlayerDeathListener(player);
        Bukkit.getPluginManager().registerEvents(deathListener, plugin);

        // Register a listener to handle when the boss dies.
        BossDeathListener bossDeathListener = new BossDeathListener(player, boss);
        Bukkit.getPluginManager().registerEvents(bossDeathListener, plugin);

        // Schedule the end of the Pigstep event (e.g. 300 seconds later)
        int eventDurationTicks = 300 * 20; // 300 seconds (5 minutes)
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            // End of event: Teleport the player back to their spawn
            Location spawn = player.getBedSpawnLocation();
            if (spawn == null) {
                spawn = player.getWorld().getSpawnLocation();
            }
            player.teleport(spawn);
            player.sendMessage(ChatColor.RED + "The Pigstep event has ended. You have been returned to your spawn.");

            // Unregister our temporary listeners
            HandlerList.unregisterAll(deathListener);
            HandlerList.unregisterAll(bossDeathListener);

            // If the boss is still alive, remove it.
            if (!boss.isDead()) {
                boss.remove();
            }
        }, eventDurationTicks);
    }

    // Listener to handle player death during the event.
    private class PlayerDeathListener implements Listener {
        private final Player eventPlayer;

        public PlayerDeathListener(Player player) {
            this.eventPlayer = player;
        }

        @EventHandler
        public void onPlayerDeath(org.bukkit.event.entity.PlayerDeathEvent event) {
            if (event.getEntity().equals(eventPlayer)) {
                // Clear default drops so the player keeps their items.
                event.getDrops().clear();
                // Teleport the player back to their spawn shortly after death.
                Location spawn = eventPlayer.getBedSpawnLocation();
                if (spawn == null) {
                    spawn = eventPlayer.getWorld().getSpawnLocation();
                }
                Location finalSpawn = spawn;
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    eventPlayer.teleport(finalSpawn);
                    eventPlayer.sendMessage(ChatColor.RED + "You died during the Pigstep event. Your items have been preserved and you have been returned to your spawn.");
                }, 1L);
            }
        }
    }

    // Listener to handle the boss death.
    private class BossDeathListener implements Listener {
        private final Player eventPlayer;
        private final Entity bossEntity;

        public BossDeathListener(Player player, Entity boss) {
            this.eventPlayer = player;
            this.bossEntity = boss;
        }

        @EventHandler
        public void onBossDeath(org.bukkit.event.entity.EntityDeathEvent event) {
            if (event.getEntity().equals(bossEntity)) {
                // When the boss dies, teleport the player back to spawn and spawn rewards on their feet.
                Location spawn = eventPlayer.getBedSpawnLocation();
                if (spawn == null) {
                    spawn = eventPlayer.getWorld().getSpawnLocation();
                }



                eventPlayer.teleport(spawn);
                eventPlayer.sendMessage(ChatColor.GOLD + "You defeated the boss! Rewards have been dropped at your feet, and you have been returned to your spawn.");
                //TODO rewards
                // Unregister this listener.
                PetManager petManager = PetManager.getInstance(plugin);
                PetRegistry petRegistry = new PetRegistry();
                petRegistry.addPetByName(eventPlayer, "Piglin Brute");
                HandlerList.unregisterAll(this);
            }
        }
    }


    private void handleMusicDisc5(Player player) {
        PlayerOxygenManager playerOxygenManager = PlayerOxygenManager.getInstance();
        UUID playerUUID = player.getUniqueId();
        // Reset the player's oxygen to the initial value
        int initialOxygen = playerOxygenManager.calculateInitialOxygen(player);
        playerOxygenManager.setPlayerOxygenLevel(player, initialOxygen);
        // Apply night vision for 20 minutes (20 ticks/second * 60 seconds/minute * 20 minutes)
        player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 20 * 60 * 20, 0, true, false, false));
        // Notify the player
    }
    public void handleMusicDiscOtherside(Player player) {
        Random random = new Random();
        World world = player.getWorld();
        PetManager petManager = PetManager.getInstance(plugin);
        PetRegistry petRegistry = new PetRegistry();
        petRegistry.addPetByName(player, "Parrot");
        // Play the music disc sound
        world.playSound(player.getLocation(), Sound.MUSIC_DISC_OTHERSIDE, SoundCategory.RECORDS, 1000.0f, 1.0f);
        player.sendMessage(ChatColor.GREEN + "Time accelerates around you!");

        // Duration of the disc in ticks (195 seconds * 20 ticks per second)
        int durationTicks = 195 * 20;

        // Time acceleration factor: 10 seconds per tick (200 ticks per tick)
        long timeAcceleration = 180L;

        // List to keep track of spawned parrots
        List<Parrot> spawnedParrots = new ArrayList<>();

        // Generate a random color for fireworks


        // Variables to keep track of day/night
        final boolean[] isDay = {isDayTime(world)};

        // Schedule repeating task to speed up time and handle events
        new BukkitRunnable() {
            int ticksRun = 0;

            @Override
            public void run() {
                if (ticksRun >= durationTicks) {
                    // Time is up, reset time progression
                    this.cancel();

                    // Despawn all parrots
                    for (Parrot parrot : spawnedParrots) {
                        if (!parrot.isDead()) {
                            parrot.remove();
                        }
                    }

                    player.sendMessage(ChatColor.GREEN + "Time returns to normal.");
                    return;
                }

                // Accelerate time
                long currentTime = world.getTime();
                world.setTime(currentTime + timeAcceleration);

                // Check if day/night has changed
                boolean newIsDay = isDayTime(world);
                if (newIsDay != isDay[0]) {
                    // Day/night transition occurred
                    if (newIsDay) {
                        // It's now day
                        // Spawn parrots up to 100 blocks away
                        for (int i = 0; i < 10; i++) {
                            Location spawnLocation = player.getLocation().clone().add(
                                    (random.nextDouble() - 0.5) * 200,
                                    0,
                                    (random.nextDouble() - 0.5) * 200
                            );
                            // Find highest block at location to spawn parrot on ground
                            spawnLocation.setY(world.getHighestBlockYAt(spawnLocation) + 1);

                            Parrot parrot = (Parrot) world.spawnEntity(spawnLocation, EntityType.PARROT);
                            parrot.setVariant(Parrot.Variant.values()[random.nextInt(Parrot.Variant.values().length)]);
                            spawnedParrots.add(parrot);
                        }
                    } else {
                        // It's now night
                        // Launch fireworks up to 100 blocks away
                        for (int i = 0; i < 45; i++) {
                            Location fireworkLocation = player.getLocation().clone().add(
                                    (random.nextDouble() - 0.5) * 200,
                                    0,
                                    (random.nextDouble() - 0.5) * 200
                            );
                            fireworkLocation.setY(world.getHighestBlockYAt(fireworkLocation) + 1);
                            Color fireworkColor = Color.fromRGB(random.nextInt(256), random.nextInt(256), random.nextInt(256));
                            launchFirework(world, fireworkLocation, fireworkColor);
                        }
                    }
                    isDay[0] = newIsDay;
                }

                ticksRun++;
            }
        }.runTaskTimer(plugin, 0L, 1L); // Run every tick
    }

    // Helper method to check if it's day
    private boolean isDayTime(World world) {
        long time = world.getTime() % 24000;
        return time < 12300 || time > 23850;
    }

    // Helper method to launch a firework at a location with a given color
    private void launchFirework(World world, Location location, Color color) {
        Random random = new Random();
        Firework firework = (Firework) world.spawnEntity(location, EntityType.FIREWORK);
        FireworkMeta meta = firework.getFireworkMeta();

        // Create firework effect with random type and effects
        FireworkEffect effect = FireworkEffect.builder()
                .withColor(color)
                .withFade(color)
                .with(FireworkEffect.Type.values()[random.nextInt(FireworkEffect.Type.values().length)])
                .flicker(random.nextBoolean())
                .trail(random.nextBoolean())
                .build();

        meta.addEffect(effect);
        meta.setPower(1); // Flight duration

        firework.setFireworkMeta(meta);
    }


    private void handleUnknownMusicDisc(Player player, Material discType) {
        // Handle any unknown or new music discs
        player.sendMessage("You used an unrecognized music disc: " + discType.name());
    }
}
