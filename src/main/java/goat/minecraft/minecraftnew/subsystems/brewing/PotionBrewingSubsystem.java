package goat.minecraft.minecraftnew.subsystems.brewing;

import goat.minecraft.minecraftnew.MinecraftNew;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.*;
import org.bukkit.event.block.Action;
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
        }
        return instance;
    }

    public void onEnable() {
        loadAllBrews();
    }

    public void onDisable() {
        finalizeAllSessionsOnShutdown();
        saveAllBrews();
    }

    // ========================================================================
    // Data Loading & Saving
    // ========================================================================
    private void loadAllBrews() {
        dataConfig = YamlConfiguration.loadConfiguration(dataFile);
        for (String locKey : dataConfig.getKeys(false)) {
            String potionName = dataConfig.getString(locKey + ".potionName", null);
            int timeLeft = dataConfig.getInt(locKey + ".timeLeft", 0);

            PotionRecipe found = findRecipeByName(potionName);
            if (found == null) {
                continue;
            }
            BrewSession session = new BrewSession(locKey, found);
            session.brewTimeRemaining = timeLeft;

            // If timeLeft > 0 => resume brew
            if (timeLeft > 0) {
                session.beginBrewing(true);
            }
            activeSessions.put(locKey, session);
        }
        plugin.getLogger().info("[PotionBrewing] Loaded " + activeSessions.size() + " active brew(s) from disk.");
    }

    private void saveAllBrews() {
        for (String key : dataConfig.getKeys(false)) {
            dataConfig.set(key, null);
        }
        for (String locKey : activeSessions.keySet()) {
            BrewSession session = activeSessions.get(locKey);
            dataConfig.set(locKey + ".potionName", session.recipe.getName());
            dataConfig.set(locKey + ".timeLeft",
                    session.brewInProgress() ? session.brewTimeRemaining : 0);
        }
        try {
            dataConfig.save(dataFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
        plugin.getLogger().info("[PotionBrewing] Saved " + activeSessions.size() + " brew session(s).");
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
        List<String> ingNames = Arrays.asList("Nether Wart", "Singularity");
        List<String> strengthLore = Arrays.asList("Increases melee damage by 15%", "Lasts for 15 seconds");
        // Using a deep red color (adjust the RGB as needed)
        Color strengthColor = Color.fromRGB(101, 67, 33);
        recipeRegistry.add(
                new PotionRecipe("Potion of Strength", ingNames, 15, new ItemStack(Material.POTION), strengthColor, strengthLore)
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
                session.beginBrewing(false);
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

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        // no GUI
    }

    // ========================================================================
    // Inner Classes
    // ========================================================================
    public static class PotionRecipe {
        private final String name;
        private final List<String> requiredIngredients;
        private final int brewTime;
        private final ItemStack outputItem;
        private final Color finalColor; // the color to apply to the final potion
        private final List<String> effectLore; // the lore that explains what the potion does

        public PotionRecipe(String name, List<String> requiredIngredients, int brewTime, ItemStack outputItem, Color finalColor, List<String> effectLore) {
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
        private final Set<String> placedIngredients = new HashSet<>();
        private final Map<String, ItemStack> placedIngredientItems = new HashMap<>();
        private int brewTimeRemaining = 0;
        private boolean brewing = false;
        private BukkitTask brewTask;
        private BukkitTask particleTask; // for random particle spawning

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

        public void setRecipePaper(ItemStack recipePaper) {
            this.recipePaper = recipePaper.clone();
            this.recipePaper.setAmount(1);
        }

        public void beginBrewing(boolean resuming) {
            brewing = true;
            if (!resuming) {
                brewTimeRemaining = recipe.getBrewTime();
            }
            spawnTimerArmorStand();
            startBrewTimer();

            // Start the 2-tick particle effect loop
            startParticleTask();
            startSoundTask();
            updateFile();
        }

        public void beginBrewing() {
            beginBrewing(false);
        }

        private void spawnMainArmorStand() {
            // same offset for main stand
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

        /** Timer stand ~2 blocks above the stand */
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

        /** Brew timer 1-second ticks, plus finalization. */
        private void startBrewTimer() {
            brewTask = new BukkitRunnable() {
                @Override
                public void run() {
                    brewTimeRemaining--;
                    updateFile();

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

        /**
         * A random-particle task that runs every 2 ticks.
         * We'll spawn them around the stand location.
         */
        private void startParticleTask() {
            particleTask = new BukkitRunnable() {
                final Location center = fromLocKey(locationKey).add(0.5, 1.0, 0.5);
                final Random rand = new Random();
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
                    // pick a random particle from the list
                    Particle p = CANDIDATE_PARTICLES[rand.nextInt(CANDIDATE_PARTICLES.length)];

                    // moderate speed
                    float speed = 0.2f;
                    // bigger count
                    int count = 5;

                    // spawn around the stand in a small radius
                    double rx = rand.nextDouble() - 0.5;
                    double ry = rand.nextDouble() * 1.5;
                    double rz = rand.nextDouble() - 0.5;
                    Location loc = center.clone().add(rx, ry, rz);

                    w.spawnParticle(p, loc, count, 0.0, 0.0, 0.0, speed);

                }
            }.runTaskTimer(plugin, 0L, 2L); // every 2 ticks
        }
        private BukkitTask soundTask; // Declare a field to hold the task

        private void startSoundTask() {
            soundTask = new BukkitRunnable() {
                final Location loc = fromLocKey(locationKey).add(0.5, 1, 0.5);
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
            }.runTaskTimer(plugin, 0L, 100L); // 100 ticks = 5 seconds
        }

        /**
         * Called when timer hits 0 => brew done.
         * Also fires thunder sound.
         */
        private void finishBrew() {
            removeStandData();
            brewing = false;
            brewTimeRemaining = 0;
            if (particleTask != null && !particleTask.isCancelled()) {
                particleTask.cancel();
            }
            if (soundTask != null && !soundTask.isCancelled()) {
                soundTask.cancel();
            }

            Location standLoc = fromLocKey(locationKey).add(0.5, 1, 0.5);
            World w = standLoc.getWorld();
            if (w == null) return;

            // Play thunder sound upon completion
            w.playSound(standLoc, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 2.0f, 1.0f);

            // Create and modify the final potion item
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
                    lore.add(ChatColor.LIGHT_PURPLE + "An unknown effect awaits.");
                }
                lore.add(ChatColor.DARK_GRAY + "--------------------");
                meta.setLore(lore);
                // If the item supports PotionMeta, set the color from the recipe
                if (meta instanceof PotionMeta && recipe.getFinalColor() != null) {
                    PotionMeta pMeta = (PotionMeta) meta;
                    pMeta.setColor(recipe.getFinalColor());
                    meta = pMeta;
                }
                finalPotion.setItemMeta(meta);
                // Add a dummy enchantment to give the glint effect
                finalPotion.addUnsafeEnchantment(Enchantment.DURABILITY, 1);
                finalPotion.getItemMeta().addItemFlags(ItemFlag.HIDE_ENCHANTS);
            }

            Item dropped = w.dropItem(standLoc, finalPotion);
            dropped.setUnlimitedLifetime(true);

            // Cleanup stands
            removeEntityByUUID(mainArmorStand);
            for (UUID u : labelStands.values()) {
                removeEntityByUUID(u);
            }
            labelStands.clear();
            for (UUID u : spinStands.values()) {
                removeEntityByUUID(u);
            }
            spinStands.clear();
            removeEntityByUUID(timerStand);

            Bukkit.broadcastMessage(ChatColor.GREEN + "[Brewing] " + recipe.getName() +
                    " brew has finished at " + locationKey + "!");
            activeSessions.remove(locationKey);
        }


        private void updateFile() {
            dataConfig.set(locationKey + ".potionName", recipe.getName());
            dataConfig.set(locationKey + ".timeLeft", brewTimeRemaining);
            try {
                dataConfig.save(dataFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void removeStandData() {
            dataConfig.set(locationKey, null);
            try {
                dataConfig.save(dataFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void forciblyFinalizeOnShutdown() {
            if (brewTask != null && !brewTask.isCancelled()) {
                brewTask.cancel();
            }
            if (particleTask != null && !particleTask.isCancelled()) {
                particleTask.cancel();
            }
            Location dropLoc = fromLocKey(locationKey).clone().add(0.5, 1, 0.5);
            World w = dropLoc.getWorld();
            if (w == null) return;

            // drop recipe if present
            if (recipePaper != null) {
                w.dropItemNaturally(dropLoc, recipePaper.clone());
            }
            // drop placed ingredients
            for (Map.Entry<String, ItemStack> e : placedIngredientItems.entrySet()) {
                w.dropItemNaturally(dropLoc, e.getValue());
            }
            // remove stands
            if (mainArmorStand != null) removeEntityByUUID(mainArmorStand);
            for (UUID u : labelStands.values()) {
                removeEntityByUUID(u);
            }
            labelStands.clear();
            for (UUID u : spinStands.values()) {
                removeEntityByUUID(u);
            }
            spinStands.clear();
            if (timerStand != null) removeEntityByUUID(timerStand);

            brewTimeRemaining = 0;
            brewing = false;

            dataConfig.set(locationKey, null);
            try {
                dataConfig.save(dataFile);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

        /**
         * Replaces underscores with spaces, e.g. "NETHER_WART" => "nether wart"
         */
        public String matchIngredient(ItemStack hand) {
            if (hand == null || hand.getType() == Material.AIR) return null;
            String displayName;
            if (hand.hasItemMeta() && hand.getItemMeta().hasDisplayName()) {
                displayName = ChatColor.stripColor(hand.getItemMeta().getDisplayName()).toLowerCase();
            } else {
                displayName = hand.getType().toString().replace("_", " ").toLowerCase();
            }
            for (String needed : recipe.getRequiredIngredients()) {
                if (placedIngredients.contains(needed)) continue;
                if (displayName.contains(needed.toLowerCase())) {
                    return needed;
                }
            }
            return null;
        }

        public void placeIngredient(String ingName, ItemStack hand) {
            placedIngredients.add(ingName);
            ItemStack copy = hand.clone();
            copy.setAmount(1);
            placedIngredientItems.put(ingName, copy);

            if (labelStands.containsKey(ingName)) {
                removeEntityByUUID(labelStands.get(ingName));
                labelStands.remove(ingName);
            }
            spawnSpinningIngredient(ingName, hand.clone());
        }

        /**
         * Summons an armor stand 1 block lower than before (y+0.7 instead of 1.7)
         * at random offset. Then spins it quickly.
         */
        private void spawnSpinningIngredient(String ingName, ItemStack item) {
            double offsetX = (Math.random() - 0.5) * 0.6;
            double offsetZ = (Math.random() - 0.5) * 0.6;
            // new, y+0.7 instead of 1.7
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

        /**
         * Spin at 10 degrees per tick for both yaw & arm rotation.
         */
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

        private void removeEntityByUUID(UUID uuid) {
            if (uuid == null) return;
            Entity e = Bukkit.getEntity(uuid);
            if (e != null) {
                e.remove();
            }
        }
    }
}
