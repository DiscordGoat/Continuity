package goat.minecraft.minecraftnew.subsystems.brewing;

import goat.minecraft.minecraftnew.MinecraftNew;
import goat.minecraft.minecraftnew.utils.devtools.XPManager;
import goat.minecraft.minecraftnew.utils.devtools.PlayerMeritManager;
import goat.minecraft.minecraftnew.subsystems.pets.PetManager;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.*;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * A location-based brewing system that:
 * - Immediately starts a session on recipe placement (like CulinarySubsystem),
 * - Spawns main stand + label stands for unplaced ingredients,
 * - Spawns spinning stands for placed ingredients (with a much faster spin),
 * - Displays a timer if actively brewing, persisting across normal reload calls,
 * - Fires random particles every 2 ticks around the brew stand,
 * - Plays a thunder sound when brew completes,
 * - On server/plugin disable, forcibly drops any incomplete items (recipe + placed ing.)
 *   and ends the session, exactly like the CulinarySubsystem.
 */
public class PotionBrewingSubsystem implements Listener {

    private static PotionBrewingSubsystem instance;
    private final JavaPlugin plugin;
    private final File dataFile;
    private YamlConfiguration dataConfig;
    private boolean isEnabled = false;

    // In-memory sessions: locationKey -> BrewSession
    private final Map<String, BrewSession> activeSessions = new HashMap<>();

    public PotionBrewingSubsystem(JavaPlugin plugin) {
        this.plugin = plugin;
        dataFile = new File(plugin.getDataFolder(), "potion_brews.yml");
        if (!dataFile.exists()) {
            try {
                dataFile.createNewFile();
            } catch (IOException ignored) {}
        }
        dataConfig = YamlConfiguration.loadConfiguration(dataFile);

        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public static PotionBrewingSubsystem getInstance(JavaPlugin plugin) {
        if (instance == null) {
            instance = new PotionBrewingSubsystem(plugin);
            instance.onEnable();
        }
        return instance;
    }

    public void onEnable() {
        if (!isEnabled) {
            // Your initialization code here
            loadAllBrews();
            isEnabled = true;
        }
    }

    public void onDisable() {
        saveAllBrews();
    }

    // ========================================================================
    // Data Load & Save (new "database" approach)
    // ========================================================================
    private void loadAllBrews() {
        dataConfig = YamlConfiguration.loadConfiguration(dataFile);

        for (String locKey : dataConfig.getKeys(false)) {
            // Check if the session is already active
            if (!activeSessions.containsKey(locKey)) {
                String potionName = dataConfig.getString(locKey + ".potionName", null);
                int timeLeft = dataConfig.getInt(locKey + ".timeLeft", 0);

                // Load placed ingredients with their ItemStacks
                Map<String, ItemStack> placedIngs = new HashMap<>();
                ConfigurationSection ingSection = dataConfig.getConfigurationSection(locKey + ".placedIngredients");
                if (ingSection != null) {
                    for (String ing : ingSection.getKeys(false)) {
                        ItemStack item = ingSection.getItemStack(ing);
                        if (item != null) {
                            placedIngs.put(ing, item);
                        }
                    }
                }

                // If we have a known recipe, create a session for it
                PotionRecipe found = findRecipeByName(potionName);
                if (found == null) {
                    continue; // unknown recipe => skip
                }
                BrewSession session = new BrewSession(locKey, found);
                session.brewTimeRemaining = timeLeft;

                // Restore placed ingredients
                for (Map.Entry<String, ItemStack> entry : placedIngs.entrySet()) {
                    session.placeIngredient(entry.getKey(), entry.getValue());
                }

                // Re-summon stands for main stand, label stands, spinning stands, etc.
                session.summonAllStandsAndTasks();

                // Save to memory
                activeSessions.put(locKey, session);
            }
        }
        plugin.getLogger().info("[PotionBrewing] Loaded " + activeSessions.size() + " persistent brew session(s).");
    }

    private void saveAllBrews() {
        // Clear old data
        for (String key : dataConfig.getKeys(false)) {
            dataConfig.set(key, null);
        }
        // Store each active session
        for (String locKey : activeSessions.keySet()) {
            BrewSession session = activeSessions.get(locKey);
            dataConfig.set(locKey + ".potionName", session.recipe.getName());
            dataConfig.set(locKey + ".timeLeft", session.brewTimeRemaining);

            // Store placed ingredients with their ItemStacks
            for (Map.Entry<String, ItemStack> entry : session.placedIngredientItems.entrySet()) {
                dataConfig.set(locKey + ".placedIngredients." + entry.getKey(), entry.getValue());
            }
        }
        try {
            dataConfig.save(dataFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
        plugin.getLogger().info("[PotionBrewing] Saved " + activeSessions.size() + " brew session(s) to disk.");
    }


    public void finalizeAllSessionsOnShutdown() {
        for (BrewSession session : activeSessions.values()) {
            session.forciblyFinalizeOnShutdown();
        }
        activeSessions.clear();
        plugin.getLogger().info("[PotionBrewingSubsystem] All sessions finalized & cleared on shutdown.");
    }

    // ========================================================================
    // Helper
    // ========================================================================
    private String toLocKey(Location loc) {
        return loc.getWorld().getName() + ":" + loc.getBlockX() + ":" + loc.getBlockY() + ":" + loc.getBlockZ();
    }

    private Location fromLocKey(String key) {
        String[] parts = key.split(":");
        World w = Bukkit.getWorld(parts[0]);
        int x = Integer.parseInt(parts[1]);
        int y = Integer.parseInt(parts[2]);
        int z = Integer.parseInt(parts[3]);
        return new Location(w, x, y, z);
    }

    private boolean isRecipePaper(ItemStack item) {
        if (item == null || item.getType() != Material.PAPER) return false;
        if (!item.hasItemMeta() || !item.getItemMeta().hasDisplayName()) return false;
        return item.getItemMeta().getDisplayName().contains("Potion Recipe");
    }

    private PotionRecipe parseRecipeFromPaper(ItemStack paper) {
        String name = ChatColor.stripColor(paper.getItemMeta().getDisplayName())
                .replace("Recipe (Potion Recipe)", "")
                .trim();
        return findRecipeByName(name);
    }

    // Candidate particles for random selection
    private static final Particle[] CANDIDATE_PARTICLES = {
            Particle.FLAME,
            Particle.SMOKE_NORMAL,
            Particle.CRIT,
            Particle.END_ROD,
            Particle.CLOUD,
            Particle.SPELL_WITCH,
            Particle.SPELL,
            Particle.PORTAL,
            Particle.SLIME
    };

    private static final List<PotionRecipe> recipeRegistry = new ArrayList<>();
    static {
        // Existing recipe for Potion of Strength
        //strength is farmed from Knights
        List<String> strengthIngredients = Arrays.asList("Glass Bottle", "Nether Wart", "Gravity");
        int baseDuration = (60 * 3);
        List<String> strengthLore = Arrays.asList("Increases melee damage by 25%", "Base Duration of " + baseDuration);
        Color strengthColor = Color.fromRGB(101, 67, 33);
        recipeRegistry.add(
                new PotionRecipe("Potion of Strength", strengthIngredients, 60 * 10, new ItemStack(Material.POTION), strengthColor, strengthLore)
        );

        // New recipe for Sovereignty
        List<String> sovereigntyIngredients = Arrays.asList("Glass Bottle", "Nether Wart", "Obsidian", "Shatterproof");
        baseDuration = 60 * 3; // 120 seconds cooldown (adjust as needed for effect duration)
        List<String> sovereigntyLore = Arrays.asList("Deflects the first 5 attacks", "Cooldown: 120 seconds");
        // Choose a color that fits a regal theme (adjust the RGB values as needed)
        Color sovereigntyColor = Color.fromRGB(0, 255, 174);
        recipeRegistry.add(
                new PotionRecipe("Potion of Sovereignty", sovereigntyIngredients, 60*10, new ItemStack(Material.POTION), sovereigntyColor, sovereigntyLore)
        );

        List<String> liquidLuckIngredients = Arrays.asList("Glass Bottle", "Nether Wart", "Treasury");
        baseDuration = 60 * 3; // 120 seconds cooldown (adjust as needed for effect duration)
        List<String> liquidLuckLore = Arrays.asList("Increases Treasure Chance by 20%", "Base Duration of " + baseDuration);
        // Choose a color that fits a regal theme (adjust the RGB values as needed)
        Color liquidLuckColor = Color.fromRGB(253, 217, 5);
        recipeRegistry.add(
                new PotionRecipe("Potion of Liquid Luck", liquidLuckIngredients, 60*20, new ItemStack(Material.POTION), liquidLuckColor, liquidLuckLore)
        );
        List<String> fountainsIngredients = Arrays.asList("Glass Bottle", "Nether Wart", "Heart of the Sea", "EntionPlast");
        List<String> fountainsLore = Arrays.asList("Boosts sea creature chance by 10%", "Base Duration of " + baseDuration);
        Color fountainsColor = Color.fromRGB(0, 255, 171);
        recipeRegistry.add(
                new PotionRecipe("Potion of Fountains", fountainsIngredients, 60*4, new ItemStack(Material.POTION), fountainsColor, fountainsLore)
        );
        List<String> swiftStepIngredients = Arrays.asList("Glass Bottle", "Nether Wart", "Sugar", "Entropy");
        List<String> swiftStepLore = Arrays.asList("Increases movement speed by 30%", "Base Duration of " + baseDuration);
        Color swiftStepColor = Color.fromRGB(150, 200, 255); // A light blue-ish color
        recipeRegistry.add(
                new PotionRecipe("Potion of Swift Step", swiftStepIngredients, 60*30, new ItemStack(Material.POTION), swiftStepColor, swiftStepLore)
        );
        List<String> recurveIngredients = Arrays.asList("Glass Bottle", "Nether Wart", "Marrow");
        List<String> recurveLore = Arrays.asList("Increases arrow damage by 25%", "Base Duration of " + baseDuration);
        Color recurveColor = Color.fromRGB(0, 0, 0); // A sleek violet-tinted archery vibe
        recipeRegistry.add(
                new PotionRecipe("Potion of Recurve", recurveIngredients, 60*10, new ItemStack(Material.POTION), recurveColor, recurveLore)
        );

        // Potion of Solar Fury
        List<String> solarFuryIngredients = Arrays.asList("Glass Bottle", "Sunflare", "Magma Cream", "Nether Wart");
        List<String> solarFuryLore = Arrays.asList("Doubles fire level gains to monsters.", "Base Duration of " + baseDuration);
        Color solarFuryColor = Color.fromRGB(255, 120, 0);
        recipeRegistry.add(
                new PotionRecipe("Potion of Solar Fury", solarFuryIngredients, 60*10, new ItemStack(Material.POTION), solarFuryColor, solarFuryLore)
        );

        // Potion of Riptide
        List<String> riptideIngredients = Arrays.asList("Glass Bottle", "Nether Wart", "Heart of the Sea", "Kelp", "Tide");
        List<String> riptideLore = Arrays.asList("Boosts riptide velocity", "Base Duration of " + 60*30);
        Color riptideColor = Color.fromRGB(173, 216, 230);
        recipeRegistry.add(
                new PotionRecipe("Potion of Riptide", riptideIngredients, 60*10, new ItemStack(Material.POTION), riptideColor, riptideLore)
        );

        // Potion of Night Vision
        List<String> nightVisionIngredients = Arrays.asList("Glass Bottle", "Nether Wart", "Starlight", "Fermented Spider Eye");
        List<String> nightVisionLore = Arrays.asList("Grants Night Vision while moving", "Base Duration of " + 60*30);
        Color nightVisionColor = Color.fromRGB(255, 255, 255);
        recipeRegistry.add(
                new PotionRecipe("Potion of Night Vision", nightVisionIngredients, 60*10, new ItemStack(Material.POTION), nightVisionColor, nightVisionLore)
        );

        // Potion of Charismatic Bartering
        List<String> charismaticIngredients = Arrays.asList("Glass Bottle", "Nether Wart", "Gold Block", "Shiny Emerald");
        List<String> charismaticLore = Arrays.asList("Villager trades 20% cheaper", "Base Duration of " + baseDuration);
        Color charismaticColor = Color.fromRGB(80, 200, 120); // emerald green tone
        recipeRegistry.add(
                new PotionRecipe("Potion of Charismatic Bartering", charismaticIngredients, 60*10, new ItemStack(Material.POTION), charismaticColor, charismaticLore)
        );

        // Potion of Oxygen Recovery
        List<String> oxygenRecoveryIngredients = Arrays.asList("Glass Bottle", "Nether Wart", "Sponge", "Ghost");
        List<String> oxygenRecoveryLore = Arrays.asList("Recover oxygen faster while mining", "Base Duration of " + baseDuration);
        Color oxygenRecoveryColor = Color.fromRGB(0, 0, 0);
        recipeRegistry.add(
                new PotionRecipe("Potion of Oxygen Recovery", oxygenRecoveryIngredients, 60*10, new ItemStack(Material.POTION), oxygenRecoveryColor, oxygenRecoveryLore)
        );
    }

    private PotionRecipe findRecipeByName(String potionName) {
        for (PotionRecipe rec : recipeRegistry) {
            if (rec.getName().equalsIgnoreCase(potionName)) {
                return rec;
            }
        }
        return null;
    }

    // ========================================================================
    // Events
    // ========================================================================
    @EventHandler
    public void onStandInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK && event.getAction() != Action.LEFT_CLICK_BLOCK) return;
        if (event.getClickedBlock() == null || event.getClickedBlock().getType() != Material.BREWING_STAND) return;


        Player player = event.getPlayer();
        String locKey = toLocKey(event.getClickedBlock().getLocation());

        if (activeSessions.containsKey(locKey) && activeSessions.get(locKey).brewInProgress()) {
            player.sendMessage(ChatColor.RED + "That stand is currently busy brewing!");
            event.setCancelled(true);
            return;
        }

        // Left-click => finalize
        if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
            if (!activeSessions.containsKey(locKey)) {
                player.sendMessage(ChatColor.RED + "No potion recipe is placed here yet.");
                return;
            }
            BrewSession session = activeSessions.get(locKey);
            if (session.allIngredientsPlaced()) {
                player.sendMessage(ChatColor.GREEN + "All ingredients are placed! Brewing started...");
                session.beginBrewing(false, event.getPlayer());
                event.setCancelled(true);
            } else {
                player.sendMessage(ChatColor.RED + "Not all required ingredients have been placed yet!");
            }
            return;
        }
        event.setCancelled(true);
        // Right-click => either start or place an ingredient
        ItemStack hand = player.getInventory().getItemInMainHand();
        if (hand == null || hand.getType() == Material.AIR) {
            player.sendMessage(ChatColor.RED + "You must hold a potion recipe or an ingredient!");
            return;
        }

        if (!activeSessions.containsKey(locKey)) {
            // Start new session
            if (!isRecipePaper(hand)) {
                player.sendMessage(ChatColor.RED + "That is not a valid potion recipe item.");
                return;
            }
            PotionRecipe recipe = parseRecipeFromPaper(hand);
            if (recipe == null) {
                player.sendMessage(ChatColor.RED + "Unrecognized or invalid potion recipe item.");
                return;
            }
            BrewSession session = new BrewSession(locKey, recipe);
            session.setRecipePaper(hand.clone());
            activeSessions.put(locKey, session);

            hand.setAmount(hand.getAmount() - 1);
            player.sendMessage(ChatColor.GREEN + "Placed recipe for " + recipe.getName()
                    + ". Right-click with each ingredient, then left-click to finalize.");

            // Show stands
            session.spawnMainArmorStand();
            session.updateIngredientLabels();
        } else {
            // Place an ingredient
            BrewSession session = activeSessions.get(locKey);
            if (session.allIngredientsPlaced()) {
                player.sendMessage(ChatColor.YELLOW + "All ingredients are already placed! Left-click to finalize brew.");
                return;
            }
            String matched = session.matchIngredient(hand);
            if (matched == null) {
                player.sendMessage(ChatColor.RED + "That item is not required or is already placed.");
                return;
            }
            session.placeIngredient(matched, hand);
            hand.setAmount(hand.getAmount() - 1);
            player.sendMessage(ChatColor.GREEN + "Placed ingredient: " + matched);

            session.updateIngredientLabels();

            if (session.allIngredientsPlaced()) {
                player.sendMessage(ChatColor.YELLOW + "All ingredients placed! Left-click to finalize brew.");
            }
        }
    }

    /**
     * If a brewing stand involved in a session is broken, cancel the session
     * and drop any stored items.
     */
    @EventHandler
    public void onStandBreak(BlockBreakEvent event) {
        Block b = event.getBlock();
        if (b.getType() != Material.BREWING_STAND) return;

        String locKey = toLocKey(b.getLocation());
        if (!activeSessions.containsKey(locKey)) return;

        event.setDropItems(false);
        BrewSession session = activeSessions.remove(locKey);
        if (session != null) {
            session.destroySession(b.getLocation());
        }
        saveAllBrews();
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        // no GUI
    }

    // ========================================================================
    // The BrewSession
    // ========================================================================
    public static class PotionRecipe {
        private final String name;
        private final List<String> requiredIngredients;
        private final int brewTime;
        private final ItemStack outputItem;
        private final Color finalColor;  // color for final potion
        private final List<String> effectLore; // lines describing effect

        public PotionRecipe(String name,
                            List<String> requiredIngredients,
                            int brewTime,
                            ItemStack outputItem,
                            Color finalColor,
                            List<String> effectLore) {
            this.name = name;
            this.requiredIngredients = requiredIngredients;
            this.brewTime = brewTime;
            this.outputItem = outputItem;
            this.finalColor = finalColor;
            this.effectLore = effectLore;
        }
        public String getName() { return name; }
        public List<String> getRequiredIngredients() { return requiredIngredients; }
        public int getBrewTime() { return brewTime; }
        public ItemStack getOutputItem() { return outputItem.clone(); }
        public Color getFinalColor() { return finalColor; }
        public List<String> getEffectLore() { return effectLore; }
    }

    public class BrewSession {
        private final String locationKey;
        private final PotionRecipe recipe;
        private ItemStack recipePaper;

        // track which ingredients are placed
        private final Set<String> placedIngredients = new HashSet<>();

        // track actual items placed for each ing, so we can drop them if never finished
        private final Map<String, ItemStack> placedIngredientItems = new HashMap<>();

        // Ticking time left
        private int brewTimeRemaining = 0;
        private boolean brewing = false;
        private BukkitTask brewTask;
        private BukkitTask particleTask;
        private BukkitTask soundTask;

        // Armor stands
        private UUID mainArmorStand;
        private UUID timerStand;
        private final Map<String, UUID> labelStands = new HashMap<>();
        private final Map<String, UUID> spinStands = new HashMap<>();

        public BrewSession(String locKey, PotionRecipe recipe) {
            this.locationKey = locKey;
            this.recipe = recipe;
        }

        public boolean brewInProgress() {
            return brewing;
        }

        public boolean allIngredientsPlaced() {
            return placedIngredients.size() >= recipe.getRequiredIngredients().size();
        }

        public void setRecipePaper(ItemStack paper) {
            this.recipePaper = paper;
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
        /**
         * Summon all armor stands: main stand (recipe name), label stands (unplaced ing),
         * plus any spinning stands for already-placed ingredients (if loaded from DB).
         * If we had timeLeft>0 => we also resume the brew timer/particles.
         */
        public void summonAllStandsAndTasks() {
            spawnMainArmorStand();
            updateIngredientLabels();
            // For each placed ingredient, we re-summon a spinning stand
            // If brewTimeRemaining > 0 => we are still brewing => resume
            if (brewTimeRemaining > 0) {
                beginBrewing(true, null);  // resume = true
            }
        }


        /**
         * Actually begin the brew (or resume).
         * We spawn the timer stand, start the brew countdown, and start the particles + sound tasks.
         */
        public void beginBrewing(boolean resuming, Player player) {
            brewing = true;
            if (!resuming) {
                brewTimeRemaining = recipe.getBrewTime();
                if (player != null) {
                    PlayerMeritManager meritManager = PlayerMeritManager.getInstance(plugin);
                    if (meritManager.hasPerk(player.getUniqueId(), "Master Brewer")) {
                        brewTimeRemaining = (int) Math.ceil(brewTimeRemaining * 0.5);
                    }

                    PetManager petManager = PetManager.getInstance(plugin);
                    PetManager.Pet pet = petManager.getActivePet(player);
                    if (pet != null && pet.hasPerk(PetManager.PetPerk.SPLASH_POTION)) {
                        double reduction = pet.getLevel() / 100.0;
                        brewTimeRemaining = (int) Math.ceil(brewTimeRemaining * (1 - reduction));
                    }
                }
            }
            spawnTimerArmorStand();
            startBrewTimer();
            startParticleTask();
            startSoundTask();
            updateDB();

            if(player == null){
                return;
            }
            XPManager xpManager = new XPManager(plugin);
            xpManager.addXP(player, "Brewing", 500);
        }

        public void spawnMainArmorStand() {
            Location tableLoc = fromLocKey(locationKey).add(0.5, 1.7, 0.5);
            ArmorStand stand = (ArmorStand) tableLoc.getWorld().spawnEntity(tableLoc, EntityType.ARMOR_STAND);

            stand.setInvisible(true);
            stand.setCustomNameVisible(true);
            stand.setCustomName(ChatColor.GOLD + recipe.getName());
            stand.setGravity(false);
            stand.setInvulnerable(true);
            stand.setMarker(true);

            mainArmorStand = stand.getUniqueId();
        }

        public void updateIngredientLabels() {
            // remove old
            for (UUID u : labelStands.values()) {
                removeEntityByUUID(u);
            }
            labelStands.clear();

            Location anchor;
            Entity e = Bukkit.getEntity(mainArmorStand);
            if (e instanceof ArmorStand && e.isValid()) {
                anchor = e.getLocation().clone();
            } else {
                anchor = fromLocKey(locationKey).add(0.5, 1.7, 0.5);
            }

            double offsetY = -0.25;
            for (String ing : recipe.getRequiredIngredients()) {
                if (placedIngredients.contains(ing)) {
                    continue;
                }
                offsetY -= 0.3;
                Location ingLoc = anchor.clone().add(0, offsetY, 0);
                ArmorStand ingStand = (ArmorStand) ingLoc.getWorld().spawnEntity(ingLoc, EntityType.ARMOR_STAND);

                ingStand.setInvisible(true);
                ingStand.setMarker(true);
                ingStand.setCustomNameVisible(true);
                ingStand.setGravity(false);
                ingStand.setInvulnerable(true);

                ingStand.setCustomName(ChatColor.LIGHT_PURPLE + ing);

                labelStands.put(ing, ingStand.getUniqueId());
            }
        }

        private void spawnTimerArmorStand() {
            Location anchor = fromLocKey(locationKey).add(0.5, 2.0, 0.5);
            ArmorStand stand = (ArmorStand) anchor.getWorld().spawnEntity(anchor, EntityType.ARMOR_STAND);
            stand.setInvisible(true);
            stand.setCustomNameVisible(true);
            stand.setGravity(false);
            stand.setInvulnerable(true);
            stand.setMarker(true);

            stand.setCustomName(ChatColor.YELLOW + "" + brewTimeRemaining + "s");
            timerStand = stand.getUniqueId();
        }

        private void startBrewTimer() {
            if (brewTask != null && !brewTask.isCancelled()) {
                brewTask.cancel();
            }
            brewTask = new BukkitRunnable() {
                @Override
                public void run() {
                    if (Bukkit.getOnlinePlayers().isEmpty()) {
                        return; // pause brewing when no players are online
                    }

                    brewTimeRemaining--;
                    updateDB();
                    Entity ent = Bukkit.getEntity(timerStand);
                    if (ent instanceof ArmorStand && ent.isValid()) {
                        ((ArmorStand) ent).setCustomName(ChatColor.YELLOW + "" + brewTimeRemaining + "s");
                    }
                    if (brewTimeRemaining <= 0) {
                        cancel();
                        finishBrew();
                    }
                }
            }.runTaskTimer(plugin, 20L, 20L);
        }

        private void startParticleTask() {
            if (particleTask != null && !particleTask.isCancelled()) {
                particleTask.cancel();
            }
            final Location center = fromLocKey(locationKey).add(0.5, 1.0, 0.5);
            final Random rand = new Random();
            particleTask = new BukkitRunnable() {
                @Override
                public void run() {
                    if (!brewInProgress()) {
                        cancel();
                        return;
                    }
                    World w = center.getWorld();
                    if (w == null) {
                        cancel();
                        return;
                    }
                    Particle p = CANDIDATE_PARTICLES[rand.nextInt(CANDIDATE_PARTICLES.length)];
                    float speed = 0.2f;
                    int count = 5;
                    double rx = rand.nextDouble() - 0.5;
                    double ry = rand.nextDouble() * 1.5;
                    double rz = rand.nextDouble() - 0.5;
                    Location loc = center.clone().add(rx, ry, rz);
                    w.spawnParticle(p, loc, count, 0.0, 0.0, 0.0, speed);
                }
            }.runTaskTimer(plugin, 0L, 2L);
        }

        private void startSoundTask() {
            if (soundTask != null && !soundTask.isCancelled()) {
                soundTask.cancel();
            }
            final Location loc = fromLocKey(locationKey).add(0.5, 1, 0.5);
            soundTask = new BukkitRunnable() {
                @Override
                public void run() {
                    if (!brewInProgress()) {
                        cancel();
                        return;
                    }
                    World w = loc.getWorld();
                    if (w == null) {
                        cancel();
                        return;
                    }
                    w.playSound(loc, Sound.BLOCK_BREWING_STAND_BREW, 10.0f, 1.0f);
                }
            }.runTaskTimer(plugin, 0L, 100L); // every 5 seconds
        }

        private void finishBrew() {
            brewing = false;
            brewTimeRemaining = 0;
            if (brewTask != null) brewTask.cancel();
            if (particleTask != null) particleTask.cancel();
            if (soundTask != null) soundTask.cancel();

            // Summon final potion
            Location standLoc = fromLocKey(locationKey).add(0.5, 1, 0.5);
            World w = standLoc.getWorld();
            if (w != null) {
                w.playSound(standLoc, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 2.0f, 1.0f);

                // build final potion
                ItemStack finalPotion = buildFinalPotion();
                Item dropped = w.dropItem(standLoc, finalPotion);
                dropped.setUnlimitedLifetime(true);

                // Cleanup stands
                removeEntityByUUID(mainArmorStand);
                for (UUID u : labelStands.values()) removeEntityByUUID(u);
                labelStands.clear();
                for (UUID u : spinStands.values()) removeEntityByUUID(u);
                spinStands.clear();
                removeEntityByUUID(timerStand);

                Bukkit.broadcastMessage(ChatColor.GREEN + "[Brewing] " + recipe.getName() + " brew has finished!");
            }
            // remove from memory & DB
            activeSessions.remove(locationKey);
            dataConfig.set(locationKey, null);
            try { dataConfig.save(dataFile); } catch (IOException ex) { ex.printStackTrace(); }
        }

        /**
         * Example: build the final potion with color, lore, glint, etc.
         */
        private ItemStack buildFinalPotion() {
            ItemStack finalPotion = recipe.getOutputItem();
            ItemMeta meta = finalPotion.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(ChatColor.GOLD + recipe.getName());
                List<String> lore = new ArrayList<>();
                lore.add(ChatColor.DARK_GRAY + "--------------------");
                if (recipe.getEffectLore() != null && !recipe.getEffectLore().isEmpty()) {
                    for (String line : recipe.getEffectLore()) {
                        lore.add(ChatColor.LIGHT_PURPLE + line);
                    }
                } else {
                    lore.add(ChatColor.LIGHT_PURPLE + "An unknown effect awaits");
                }
                lore.add(ChatColor.DARK_GRAY + "--------------------");
                meta.setLore(lore);

                if (meta instanceof PotionMeta && recipe.getFinalColor() != null) {
                    PotionMeta pm = (PotionMeta) meta;
                    pm.setColor(recipe.getFinalColor());
                    meta = pm;
                }
                finalPotion.setItemMeta(meta);

                finalPotion.addUnsafeEnchantment(Enchantment.DURABILITY, 1);
                finalPotion.getItemMeta().addItemFlags(ItemFlag.HIDE_ENCHANTS);
            }
            return finalPotion;
        }

        private void updateDB() {
            dataConfig.set(locationKey + ".potionName", recipe.getName());
            dataConfig.set(locationKey + ".timeLeft", brewTimeRemaining);
            // store placedIngs as a list
            dataConfig.set(locationKey + ".placedIngredients", new ArrayList<>(placedIngredients));
            try { dataConfig.save(dataFile); } catch (IOException e) { e.printStackTrace(); }
        }

        public void placeIngredient(String ingName, ItemStack hand) {
            placedIngredients.add(ingName);
            ItemStack copy = hand.clone();
            copy.setAmount(1);
            placedIngredientItems.put(ingName, copy);
            // remove label stand
            if (labelStands.containsKey(ingName)) {
                removeEntityByUUID(labelStands.get(ingName));
                labelStands.remove(ingName);
            }
            spawnSpinningIngredient(ingName, hand.clone());
            updateDB();
        }

        private void spawnSpinningIngredient(String ingName, ItemStack item) {
            double offsetX = (Math.random() - 0.5) * 0.6;
            double offsetZ = (Math.random() - 0.5) * 0.6;
            Location base = fromLocKey(locationKey).clone().add(0.5, 0.7, 0.5);
            Location loc = base.add(offsetX, 0, offsetZ);

            ArmorStand stand = (ArmorStand) loc.getWorld().spawnEntity(loc, EntityType.ARMOR_STAND);
            stand.setInvisible(true);
            stand.setMarker(true);
            stand.setInvulnerable(true);
            stand.setGravity(false);
            stand.setSmall(true);
            stand.setArms(true);

            item.setAmount(1);
            stand.getEquipment().setItemInMainHand(item);
            stand.setCustomNameVisible(false);

            spinStands.put(ingName, stand.getUniqueId());
            startSpinning(stand);
        }

        private void startSpinning(ArmorStand stand) {
            new BukkitRunnable() {
                double yawAngle = 0.0;
                double armAngle = 0.0;
                @Override
                public void run() {
                    if (stand == null || !stand.isValid()) {
                        cancel();
                        return;
                    }
                    // spin faster
                    yawAngle += 10.0;
                    if (yawAngle >= 360.0) yawAngle -= 360.0;
                    Location loc = stand.getLocation();
                    loc.setYaw((float) yawAngle);
                    stand.teleport(loc);

                    armAngle += 10.0;
                    if (armAngle >= 360.0) armAngle -= 360.0;
                    double radians = Math.toRadians(armAngle);
                    stand.setRightArmPose(new org.bukkit.util.EulerAngle(radians, 0, 0));
                }
            }.runTaskTimer(plugin, 0L, 1L);
        }

        public String matchIngredient(ItemStack hand) {
            if (hand == null || hand.getType() == Material.AIR) return null;
            String name;
            if (hand.hasItemMeta() && hand.getItemMeta().hasDisplayName()) {
                name = ChatColor.stripColor(hand.getItemMeta().getDisplayName()).toLowerCase();
            } else {
                name = hand.getType().toString().replace("_", " ").toLowerCase();
            }
            for (String needed : recipe.getRequiredIngredients()) {
                if (placedIngredients.contains(needed)) continue;
                if (name.contains(needed.toLowerCase())) {
                    return needed;
                }
            }
            return null;
        }

        /**
         * Do NOT finalize. Just keep in DB across restarts. This forcibly ends the brew if we really wanted,
         * but we’re removing that approach so that we can re-summon stands next time.
         */
        public void forciblyFinalizeOnShutdown() {
            // Instead of dropping items, do NOTHING.
            // We keep them in the DB so we can re-summon next time.
            // If you truly wanted to forcibly finalize them anyway, you'd drop items here.
        }

        // Clean up all armor stands & tasks and drop stored items
        public void destroySession(Location dropLoc) {
            if (brewTask != null && !brewTask.isCancelled()) {
                brewTask.cancel();
            }
            if (particleTask != null && !particleTask.isCancelled()) {
                particleTask.cancel();
            }
            if (soundTask != null && !soundTask.isCancelled()) {
                soundTask.cancel();
            }

            removeEntityByUUID(mainArmorStand);
            removeEntityByUUID(timerStand);
            for (UUID u : labelStands.values()) {
                removeEntityByUUID(u);
            }
            labelStands.clear();
            for (UUID u : spinStands.values()) {
                removeEntityByUUID(u);
            }
            spinStands.clear();

            if (dropLoc != null && dropLoc.getWorld() != null) {
                if (recipePaper != null) {
                    dropLoc.getWorld().dropItemNaturally(dropLoc, recipePaper);
                }
                for (ItemStack item : placedIngredientItems.values()) {
                    dropLoc.getWorld().dropItemNaturally(dropLoc, item);
                }
            }
            placedIngredientItems.clear();
            placedIngredients.clear();
        }

        private void removeEntityByUUID(UUID uuid) {
            if (uuid == null) return;
            Entity e = Bukkit.getEntity(uuid);
            if (e != null) {
                e.remove();
            }
        }
    }
}