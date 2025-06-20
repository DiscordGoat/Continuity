package goat.minecraft.minecraftnew.subsystems.farming;

import goat.minecraft.minecraftnew.MinecraftNew;
import goat.minecraft.minecraftnew.utils.devtools.ItemRegistry;
import goat.minecraft.minecraftnew.utils.devtools.XPManager;
import goat.minecraft.minecraftnew.utils.devtools.PlayerMeritManager;
import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.Ageable;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class VerdantRelicsSubsystem implements Listener {

    private static VerdantRelicsSubsystem instance;
    private final JavaPlugin plugin;
    private final File dataFile;
    private YamlConfiguration dataConfig;
    private boolean isEnabled = false;

    // In-memory storage: locationKey -> relic session
    private final Map<String, RelicSession> activeSessions = new HashMap<>();

    // ─────────────────────────────────────────────────────────────────────────
    //                           Initialization
    // ─────────────────────────────────────────────────────────────────────────
    public VerdantRelicsSubsystem(JavaPlugin plugin) {
        this.plugin = plugin;
        dataFile = new File(plugin.getDataFolder(), "verdant_relics.yml");
        if (!dataFile.exists()) {
            try { dataFile.createNewFile(); } catch (IOException ex) { ex.printStackTrace(); }
        }
        dataConfig = YamlConfiguration.loadConfiguration(dataFile);

        // Register event listeners
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public static VerdantRelicsSubsystem getInstance(JavaPlugin plugin) {
        if (instance == null) {
            instance = new VerdantRelicsSubsystem(plugin);
            instance.onEnable();
        }
        return instance;
    }

    public void onEnable() {
        if (!isEnabled) {
            loadAllRelics();
            isEnabled = true;
        }
    }

    public void onDisable() {
        saveAllRelics();
    }

    // ─────────────────────────────────────────────────────────────────────────
    //                           Persistence
    // ─────────────────────────────────────────────────────────────────────────
    private void loadAllRelics() {
        dataConfig = YamlConfiguration.loadConfiguration(dataFile);

        for (String key : dataConfig.getKeys(false)) {
            String relicType = dataConfig.getString(key + ".relicType", null);
            int timeLeft = dataConfig.getInt(key + ".growthTimeRemaining", 0);
            int totalGrowth = dataConfig.getInt(key + ".totalGrowthDuration", timeLeft); // <== new
            List<String> comps = dataConfig.getStringList(key + ".complications");
            boolean readyForHarvest = dataConfig.getBoolean(key + ".readyForHarvest", false);

            if (relicType == null) continue;

            RelicSession session = new RelicSession(key, relicType, timeLeft, totalGrowth); // <== new constructor
            session.activeComplications.addAll(comps);
            session.readyForHarvest = readyForHarvest;

            // 1) Check if the block is still a sapling:
            Location relicBlock = fromLocKey(key);
            if (relicBlock.getBlock().getType() != Material.WHEAT) {
                // The sapling was broken or replaced => remove session
                plugin.getLogger().warning("[VerdantRelics] Session for " + key
                        + " removed (sapling missing).");
                relicBlock.getWorld().playSound(relicBlock, Sound.ENTITY_BLAZE_HURT, 1.0f, 1.0f);
                continue; // do not add the session
            }
            //reapply metadata
            relicBlock.getBlock().setMetadata("verdantRelic", new FixedMetadataValue(plugin, "verdant"));

            // 2) Re-spawn displays
            session.spawnDisplayArmorStand();
            session.spawnComplicationStands();
            // 3) If not harvest-ready, and there are no complications, resume the timer
            if (!readyForHarvest) {
                session.startGrowthTask();
            }
            session.updateDisplayName();
            activeSessions.put(key, session);
        }
        plugin.getLogger().info("[VerdantRelics] Loaded " + activeSessions.size()
                + " relic session(s).");

    }

    private void saveAllRelics() {
        // Clear old data
        for (String key : dataConfig.getKeys(false)) {
            dataConfig.set(key, null);
        }
        // Save each active session to file
        for (String locKey : activeSessions.keySet()) {
            RelicSession session = activeSessions.get(locKey);
            dataConfig.set(locKey + ".relicType", session.relicType);
            dataConfig.set(locKey + ".growthTimeRemaining", session.growthTimeRemaining);
            dataConfig.set(locKey + ".totalGrowthDuration", session.totalGrowthDuration); // <== new
            dataConfig.set(locKey + ".complications", new ArrayList<>(session.activeComplications));
            dataConfig.set(locKey + ".readyForHarvest", session.readyForHarvest);
        }
        try {
            dataConfig.save(dataFile);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        plugin.getLogger().info("[VerdantRelics] Saved " + activeSessions.size()
                + " relic session(s) to disk.");
    }

    // ─────────────────────────────────────────────────────────────────────────
    //                         Coordinate Utilities
    // ─────────────────────────────────────────────────────────────────────────
    private String toLocKey(Location loc) {
        return loc.getWorld().getName() + ":"
                + loc.getBlockX() + ":"
                + loc.getBlockY() + ":"
                + loc.getBlockZ();
    }

    private Location fromLocKey(String key) {
        String[] parts = key.split(":");
        World w = Bukkit.getWorld(parts[0]);
        int x = Integer.parseInt(parts[1]);
        int y = Integer.parseInt(parts[2]);
        int z = Integer.parseInt(parts[3]);
        return new Location(w, x, y, z);
    }

    // ─────────────────────────────────────────────────────────────────────────
    //                  Events: Planting, Breaking, Interacting
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Planting a seed with Right-Click on dirt/grass.
     */
    @EventHandler
    public void onPlantRelic(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (event.getClickedBlock() == null) return;

        Block clicked = event.getClickedBlock();
        Material mat = clicked.getType();
        if (mat != Material.DIRT && mat != Material.GRASS_BLOCK && mat != Material.GRASS) {
            return;
        }

        Player p = event.getPlayer();
        ItemStack hand = p.getInventory().getItemInMainHand();
        if (!isVerdantRelicSeed(hand)) return;

        // Extract the seed's display name and remove the "Verdant Relic" prefix (case-insensitive)
        String fullName = ChatColor.stripColor(hand.getItemMeta().getDisplayName());
        String relicName = fullName.replaceFirst("(?i)verdant relic\\s*", "").trim();

        // Use the block above the clicked block for planting the relic.
        Block target = clicked.getRelative(BlockFace.UP);
        if (target.getType() != Material.AIR) {
            p.sendMessage(ChatColor.RED + "There is no space above to plant the relic!");
            return;
        }

        // Plant the relic by setting the block to wheat (instead of OAK_SAPLING)
        target.setType(Material.WHEAT);
        // Mark the wheat block with metadata so it’s recognized as a relic crop
        target.setMetadata("verdantRelic", new FixedMetadataValue(plugin, "verdant"));

        hand.setAmount(hand.getAmount() - 1);
        p.sendMessage(ChatColor.GREEN + "You planted a " + relicName + " relic seed!");

        String locKey = toLocKey(target.getLocation());

        int growthDuration;
        if (relicName.equalsIgnoreCase("Sunflare")) {
            // Sunflare grows quicker than other relics
            growthDuration = 5 * 1200 * 3; // 15 in-game days
        } else {
            // Base duration for relics
            growthDuration = 10 * 1200 * 3; // 30 in-game days
        }

        // Apply Master Botanist perk: reduce growth time by 20%
        PlayerMeritManager meritManager = PlayerMeritManager.getInstance(plugin);
        if (meritManager.hasPerk(p.getUniqueId(), "Master Botanist")) {
            growthDuration = (int) (growthDuration * 0.8);
            Bukkit.getLogger().info("Reduced Verdant Relic Growth Time by 20%. New duration: " + growthDuration);
        }

        RelicSession session = new RelicSession(locKey, relicName, growthDuration, growthDuration);
        activeSessions.put(locKey, session);

        session.spawnDisplayArmorStand();
        session.startGrowthTask();

        event.setCancelled(true);
        saveAllRelics();
    }






    /**
     * Breaking a sapling => lose the relic session.
     */
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Block b = event.getBlock();
        if (b.getType() != Material.WHEAT) return;

        String locKey = toLocKey(b.getLocation());
        if (!activeSessions.containsKey(locKey)) return;

        // Cancel default drops so nothing is dropped when broken
        event.setDropItems(false);

        // This means the user forcibly broke it. Session is destroyed
        RelicSession session = activeSessions.remove(locKey);
        if (session != null) {
            session.removeAllDisplays();
            event.getPlayer().sendMessage(ChatColor.RED + "You destroyed the Verdant Relic before it was harvested...");
            event.getBlock().getWorld().playSound(event.getBlock().getLocation(), Sound.ENTITY_BLAZE_HURT, 1.0f, 1.0f);
        }
        saveAllRelics();
    }



    /**
     * Right-Click logic for all non-harvest interactions:
     * - remedy complications
     * - use watering can
     * - anything else
     */
    @EventHandler
    public void onRightClickRelic(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (event.getClickedBlock() == null) return;

        Block b = event.getClickedBlock();
        if (b.getType() != Material.WHEAT) return;

        String locKey = toLocKey(b.getLocation());
        if (!activeSessions.containsKey(locKey)) return;

        RelicSession session = activeSessions.get(locKey);
        Player player = event.getPlayer();
        ItemStack hand = player.getInventory().getItemInMainHand();

        // Attempt to solve a complication with a remedy
        boolean complicationSolved = session.attemptSolveComplication(hand, player, event.getClickedBlock().getLocation());
        if (complicationSolved) {
            event.setCancelled(true);
            return;
        }
        // Check if item is Watering Can => instant final harvest
        if (isWateringCan(hand)) {
            session.cureAllComplications();
            session.growthTimeRemaining = 0;
            session.readyForHarvest = true;
            session.updateDisplayName();
            hand.setAmount(hand.getAmount() - 1);
            player.sendMessage(ChatColor.AQUA
                    + "You used a Watering Can to instantly finish the relic's growth!");
            event.setCancelled(true);
        }
        saveAllRelics();
    }

    /**
     * Left-click logic for final harvesting.
     */
    @EventHandler
    public void onLeftClickRelic(PlayerInteractEvent event) {
        if (event.getAction() != Action.LEFT_CLICK_BLOCK) return;
        if (event.getClickedBlock() == null) return;

        Block b = event.getClickedBlock();
        if (b.getType() != Material.WHEAT) return;

        String locKey = toLocKey(b.getLocation());
        if (!activeSessions.containsKey(locKey)) return;

        RelicSession session = activeSessions.get(locKey);
        // Only allow harvest if timer is done + no complications
        if (!session.readyForHarvest) {
            return; // ignore the click
        }
        // Harvest
        Player player = event.getPlayer();
        session.finishGrowth(player);
        event.setCancelled(true);
        saveAllRelics();
    }

    // ─────────────────────────────────────────────────────────────────────────
    //                           Helper checks
    // ─────────────────────────────────────────────────────────────────────────
    private boolean isVerdantRelicSeed(ItemStack item) {
        if (item == null) return false;
        if (!item.hasItemMeta() || !item.getItemMeta().hasDisplayName()) return false;
        return ChatColor.stripColor(item.getItemMeta().getDisplayName())
                .contains("Verdant Relic");
    }

    private boolean isWateringCan(ItemStack item) {
        if (item == null || item.getType() != Material.BUCKET) return false;
        if (!item.hasItemMeta() || !item.getItemMeta().hasDisplayName()) return false;
        String name = ChatColor.stripColor(item.getItemMeta().getDisplayName());
        return name.contains("Watering Can");
    }

    // ─────────────────────────────────────────────────────────────────────────
    //                       Inner Class: RelicSession
    // ─────────────────────────────────────────────────────────────────────────
    public class RelicSession {
        private final String locationKey;
        private final String relicType;
        private int growthTimeRemaining; // in seconds
        private final int totalGrowthDuration; // new field for total growth time

        private final XPManager xpManager = new XPManager(plugin);

        private BukkitTask growthTask;
        private ArmorStand mainDisplayStand;
        private ArmorStand growthDisplayStand; // NEW: Separate armor stand for percentage display

        private final Set<String> activeComplications = new HashSet<>();
        private final Map<String, ArmorStand> complicationStands = new HashMap<>();
        public boolean readyForHarvest = false;

        public RelicSession(String locationKey, String relicType, int growthTimeRemaining, int totalGrowthDuration) {
            this.locationKey = locationKey;
            this.relicType = relicType;
            this.growthTimeRemaining = growthTimeRemaining;
            this.totalGrowthDuration = totalGrowthDuration;
        }

        // Summon an invisible ArmorStand for the main floating text
        public void spawnDisplayArmorStand() {
            // Lower the base Y coordinate by 0.7 blocks (1.5 - 0.7 = 0.8)
            Location base = fromLocKey(locationKey).clone().add(0.5, 0.8, 0.5);
            // Spawn main display for relic name
            mainDisplayStand = (ArmorStand) base.getWorld().spawnEntity(base, EntityType.ARMOR_STAND);
            mainDisplayStand.setVisible(false);
            mainDisplayStand.setGravity(false);
            mainDisplayStand.setCustomNameVisible(true);
            mainDisplayStand.setMarker(true);
            mainDisplayStand.setInvulnerable(true);
            mainDisplayStand.setCustomName(ChatColor.GREEN + relicType);

            // Spawn a separate display for the growth percentage below the main display
            Location growthLoc = base.clone().add(0, -0.3, 0);
            growthDisplayStand = (ArmorStand) growthLoc.getWorld().spawnEntity(growthLoc, EntityType.ARMOR_STAND);
            growthDisplayStand.setVisible(false);
            growthDisplayStand.setGravity(false);
            growthDisplayStand.setCustomNameVisible(true);
            growthDisplayStand.setMarker(true);
            growthDisplayStand.setInvulnerable(true);
            growthDisplayStand.setCustomName(ChatColor.YELLOW + "[0% grown]");
        }





        // The main repeating task that counts down the growth timer, if no complications
        public void startGrowthTask() {
            growthTask = new BukkitRunnable() {
                @Override
                public void run() {
                    // If there's any complication, we freeze the timer
                    if (Bukkit.getOnlinePlayers().isEmpty()) {
                        return;
                    }
                    if (!activeComplications.isEmpty()) {
                        return; // do nothing
                    }
                    // Otherwise decrement growth time
                    growthTimeRemaining--;
                    // Possibly spawn a complication every 60s if none exist
                    if (growthTimeRemaining > 0 && growthTimeRemaining % 60 == 0
                            && activeComplications.isEmpty()) {
                        maybeAddComplication();
                    }
                    updateDisplayName();
                    if (growthTimeRemaining <= 0) {
                        readyForHarvest = true;
                        updateDisplayName();
                        cancel(); // stop ticking
                    }
                }
            }.runTaskTimer(plugin, 20L, 20L);
        }

        // Spawns complication stands for any complications that are loaded from disk
        public void spawnComplicationStands() {
            // Clear old if needed
            for (ArmorStand stand : complicationStands.values()) {
                if (stand != null && stand.isValid()) {
                    stand.remove();
                }
            }
            complicationStands.clear();

            if (growthDisplayStand == null || !growthDisplayStand.isValid()) return;

            // Use the growth percentage display's location as the base
            Location base = growthDisplayStand.getLocation();
            double offsetY = -0.3;
            for (String comp : activeComplications) {
                ArmorStand cStand = spawnSingleCompStand(comp, base.clone().add(0, offsetY, 0));
                complicationStands.put(comp, cStand);
                offsetY -= 0.3;
            }
        }


        // Single helper for spawning a complication stand
        private ArmorStand spawnSingleCompStand(String complication, Location loc) {
            ArmorStand stand = (ArmorStand) loc.getWorld().spawnEntity(loc, EntityType.ARMOR_STAND);
            stand.setCustomNameVisible(true);
            stand.setVisible(false);
            stand.setGravity(false);
            stand.setMarker(true);
            stand.setInvulnerable(true);
            stand.setCustomName(getComplicationDisplayName(complication));
            return stand;
        }

        // Display name for a complication
        private String getComplicationDisplayName(String comp) {
            switch (comp.toLowerCase()) {
                case "drought":
                    return ChatColor.AQUA + "Drought";
                case "overgrown":
                    return ChatColor.DARK_GREEN + "Overgrown";
                case "infested":
                    return ChatColor.DARK_PURPLE + "Infested";
                case "malnutrition":
                    return ChatColor.GRAY + "Malnutrition";
                default:
                    return ChatColor.RED + comp;
            }
        }

        // Possibly add a complication with some chance
        private void maybeAddComplication() {
            if (activeComplications.size() >= 1) return; // Changed from 4 to 1 since we only have one complication type now
            if (Math.random() < 0.1) { // 10% chance
                // Only add "Overgrown" as a complication
                if (!activeComplications.contains("Overgrown")) {
                    activeComplications.add("Overgrown");
                    spawnComplicationParticle("Overgrown");
                    spawnComplicationStands(); // show updated stands
                }
            }
        }

        // Particle effect for new complication
        private void spawnComplicationParticle(String comp) {
            Location loc = fromLocKey(locationKey).clone().add(0.5, 1, 0.5);
            Particle particle;
            switch (comp.toLowerCase()) {
                case "drought":
                    particle = Particle.WATER_SPLASH; break;
                case "overgrown":
                    particle = Particle.VILLAGER_HAPPY; break;
                case "infested":
                    particle = Particle.SPELL_WITCH; break;
                case "malnutrition":
                    particle = Particle.CLOUD; break;
                default:
                    particle = Particle.CRIT; break;
            }
            loc.getWorld().spawnParticle(particle, loc, 20, 0.4, 0.4, 0.4, 0.01);
        }

        // Check if the player can remedy a complication
        public boolean attemptSolveComplication(ItemStack hand, Player player, Location loc) {
            if (hand == null) {
                return false;
            }
            String remedyName = ChatColor.stripColor(hand.getItemMeta().getDisplayName()).toLowerCase();
            boolean solved = false;
            Iterator<String> it = activeComplications.iterator();
            while (it.hasNext()) {
                String comp = it.next().toLowerCase();
                // Water bucket cures Drought: replace with an empty bucket.
                if (comp.contains("drought") && hand.getType().equals(Material.WATER_BUCKET)) {
                    it.remove();
                    xpManager.addXP(player, "Farming", 100);
                    player.sendMessage(ChatColor.GREEN + "You solved Drought!");
                    // Replace water bucket with an empty bucket.
                    player.getInventory().setItemInMainHand(new ItemStack(Material.BUCKET));
                    player.playSound(player.getLocation(), Sound.ITEM_BUCKET_EMPTY_FISH, 1000.0f, 1.0f);
                    spawnWater(loc, plugin);
                    solved = true;
                }
                // Shears cure Overgrown: consume durability and break if needed.
                else if (comp.contains("overgrown") && hand.getType().equals(Material.SHEARS)) {
                    it.remove();
                    xpManager.addXP(player, "Farming", 25);
                    player.sendMessage(ChatColor.GREEN + "You solved Overgrown!");

                    if (hand.getItemMeta() instanceof org.bukkit.inventory.meta.Damageable) {
                        org.bukkit.inventory.meta.Damageable dmgMeta =
                                (org.bukkit.inventory.meta.Damageable) hand.getItemMeta();

                        int cost = 10;
                        int unbreakingLevel = hand.getEnchantmentLevel(org.bukkit.enchantments.Enchantment.DURABILITY);
                        if (unbreakingLevel > 5) unbreakingLevel = 5;

                        PlayerMeritManager meritManager = PlayerMeritManager.getInstance(plugin);
                        int perkReduction = 0;
                        if (meritManager.hasPerk(player.getUniqueId(), "Unbreaking")) perkReduction++;

                        cost -= unbreakingLevel;
                        cost -= perkReduction;
                        if (cost < 0) cost = 0;

                        dmgMeta.setDamage(dmgMeta.getDamage() + cost);
                        hand.setItemMeta((org.bukkit.inventory.meta.ItemMeta) dmgMeta);

                        if (dmgMeta.getDamage() >= hand.getType().getMaxDurability()) {
                            hand.setAmount(0);
                            player.playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 1.0f, 1.0f);
                        } else {
                            player.playSound(player.getLocation(), Sound.ENTITY_SHEEP_SHEAR, 1000.0f, 1.0f);
                        }
                    } else {
                        player.playSound(player.getLocation(), Sound.ENTITY_SHEEP_SHEAR, 1000.0f, 1.0f);
                    }

                    spawnLeaves(loc, plugin);
                    solved = true;
                }
                // Pesticide cures Infested, and Organic Soil cures Malnutrition.
                else if (comp.contains("infested") && remedyName.contains("pesticide")) {
                    it.remove();
                    xpManager.addXP(player, "Farming", 100);
                    player.sendMessage(ChatColor.GREEN + "You solved Infestation!");
                    hand.setAmount(hand.getAmount() - 1);
                    solved = true;
                    player.playSound(player.getLocation(), Sound.BLOCK_FURNACE_FIRE_CRACKLE, 1000.0f, 1.0f);
                    spawnFire(loc, plugin);

                } else if (comp.contains("malnutrition") && remedyName.contains("organic soil")) {
                    it.remove();
                    xpManager.addXP(player, "Farming", 100);
                    player.sendMessage(ChatColor.GREEN + "You solved Malnutrition!");
                    hand.setAmount(hand.getAmount() - 1);
                    player.playSound(player.getLocation(), Sound.ENTITY_ZOMBIE_VILLAGER_CURE, 1000.0f, 1.0f);
                    solved = true;
                    spawnAsh(loc, plugin);
                }
                if (solved) break;
            }
            if (solved) {
                spawnComplicationStands();
                updateDisplayName();
                saveAllRelics();
            }
            return solved;
        }

        // Call this method with a valid location and plugin instance
        public void spawnLeaves(Location location, JavaPlugin plugin) {
            World world = location.getWorld();
            new BukkitRunnable() {
                int count = 0;
                @Override
                public void run() {
                    // Run for 20 iterations (~100 ticks total)
                    if (count >= 20) {
                        cancel();
                        return;
                    }
                    // Spawn multiple types of particles in a tight radius
                    world.spawnParticle(Particle.VILLAGER_HAPPY, location, 40, 1.0, 1.0, 1.0, 0.2);
                    count++;
                }
            }.runTaskTimer(plugin, 0L, 1L);
        }
        public void spawnFire(Location location, JavaPlugin plugin) {
            World world = location.getWorld();
            new BukkitRunnable() {
                int count = 0;
                @Override
                public void run() {
                    // Run for 20 iterations (~100 ticks total)
                    if (count >= 20) {
                        cancel();
                        return;
                    }
                    // Spawn multiple types of particles in a tight radius
                    world.spawnParticle(Particle.SOUL_FIRE_FLAME, location, 40, 1.0, 1.0, 1.0, 0.2);
                    count++;
                }
            }.runTaskTimer(plugin, 0L, 1L);
        }
        public void spawnWater(Location location, JavaPlugin plugin) {
            World world = location.getWorld();
            new BukkitRunnable() {
                int count = 0;
                @Override
                public void run() {
                    // Run for 20 iterations (~100 ticks total)
                    if (count >= 20) {
                        cancel();
                        return;
                    }
                    // Spawn multiple types of particles in a tight radius
                    world.spawnParticle(Particle.DRIP_WATER, location, 40, 1.0, 1.0, 1.0, 0.2);
                    count++;
                }
            }.runTaskTimer(plugin, 0L, 1L);
        }
        public void spawnAsh(Location location, JavaPlugin plugin) {
            World world = location.getWorld();
            new BukkitRunnable() {
                int count = 0;
                @Override
                public void run() {
                    // Run for 20 iterations (~100 ticks total)
                    if (count >= 20) {
                        cancel();
                        return;
                    }
                    // Spawn multiple types of particles in a tight radius
                    world.spawnParticle(Particle.WHITE_ASH, location, 40, 1.0, 1.0, 1.0, 0.2);
                    count++;
                }
            }.runTaskTimer(plugin, 0L, 1L);
        }

        // Remove all complications
        public void cureAllComplications() {
            activeComplications.clear();
            // remove stands
            for (ArmorStand cs : complicationStands.values()) {
                if (cs != null && cs.isValid()) {
                    cs.remove();
                }
            }
            complicationStands.clear();
            updateDisplayName();
        }

        public void updateDisplayName() {
            if (mainDisplayStand == null || !mainDisplayStand.isValid() ||
                    growthDisplayStand == null || !growthDisplayStand.isValid()) return;

            // Calculate the percentage grown using the persistent totalGrowthDuration
            int percentGrown = 100 - (growthTimeRemaining * 100 / totalGrowthDuration);
            String percentText = percentGrown + "% grown";

            // Update the underlying wheat crop’s growth stage based on percentage
            Block cropBlock = fromLocKey(locationKey).getBlock();
            if (cropBlock.getType() == Material.WHEAT && cropBlock.getBlockData() instanceof Ageable) {
                Ageable ageable = (Ageable) cropBlock.getBlockData();
                int maxAge = ageable.getMaximumAge();
                int newAge = (int) Math.round((percentGrown / 100.0) * maxAge);
                ageable.setAge(newAge);
                cropBlock.setBlockData(ageable);
            }

            // Update the main display (relic name) without the percentage
            mainDisplayStand.setCustomName(ChatColor.GREEN + relicType);

            // Update the growth display to show percentage and any complications (only the names)
            String growthLine = ChatColor.YELLOW + " [" + percentText + "]";

            if (readyForHarvest) {
                growthLine += ChatColor.GOLD + " [READY TO HARVEST]";
            }
            growthDisplayStand.setCustomName(growthLine);
        }




        private String formatTime(int totalSeconds) {
            int hours = totalSeconds / 3600;
            int minutes = (totalSeconds % 3600) / 60;
            int seconds = totalSeconds % 60;
            return String.format("%02dh:%02dm:%02ds", hours, minutes, seconds);
        }

        // The relic is fully grown, the player left-clicks to harvest
        public void finishGrowth(Player harvester) {
            // Remove the physical wheat block from the world without dropping any crop items
            Location loc = fromLocKey(locationKey);
            loc.getBlock().setType(Material.AIR);

            // Determine the yield item based on the relic's type
            ItemStack yield = getYieldForRelic();

            // Drop the yield at the relic's location
            loc.getWorld().dropItemNaturally(loc.clone().add(0.5, 0.6, 0.5), yield);

            // Award 1000 Farming XP upon harvest
            if (harvester != null) {
                xpManager.addXP(harvester, "Farming", 250);
                harvester.playSound(harvester.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 10, 10.0f);
            }

            // Cleanup: Remove all displays and remove the session
            removeAllDisplays();
            activeSessions.remove(locationKey);
        }


        /**
         * Returns the correct yield based on the relic type.
         * You can add additional relic types and corresponding yield items.
         */
        private ItemStack getYieldForRelic() {

             if (relicType.equalsIgnoreCase("EntionPlast")) {
                return ItemRegistry.getEntionPlastIngredient();
            } else if (relicType.equalsIgnoreCase("Entropy")) {
                 return ItemRegistry.getEntropyIngredient(); // Ensure this method exists in ItemRegistry
             }
             else if (relicType.equalsIgnoreCase("Shatterproof")) {
                 return ItemRegistry.getShatterproof(); // Ensure this method exists in ItemRegistry
             }
             else if (relicType.equalsIgnoreCase("Gravity")) {
                 return ItemRegistry.getGravity(); // Ensure this method exists in ItemRegistry
             }
             else if (relicType.equalsIgnoreCase("Treasury")) {
                 return ItemRegistry.getTreasury(); // Ensure this method exists in ItemRegistry
             }
            else if (relicType.equalsIgnoreCase("Marrow")) {
                return ItemRegistry.getMarrow(); // Ensure this method exists in ItemRegistry
            }
            else if (relicType.equalsIgnoreCase("Sunflare")) {
                return ItemRegistry.getSunflare();
            }
            else if (relicType.equalsIgnoreCase("Starlight")) {
                return ItemRegistry.getStarlight();
            }
            else if (relicType.equalsIgnoreCase("Shiny Emerald")) {
                return ItemRegistry.getShinyEmerald();
            }
            else if (relicType.equalsIgnoreCase("Tide")) {
                return ItemRegistry.getTide();
            }
            // Default fallback yield
            return null;
        }

        // Cancel tasks and remove stands
        public void removeAllDisplays() {
            if (growthTask != null && !growthTask.isCancelled()) {
                growthTask.cancel();
            }
            if (mainDisplayStand != null && mainDisplayStand.isValid()) {
                mainDisplayStand.remove();
            }
            if (growthDisplayStand != null && growthDisplayStand.isValid()) {
                growthDisplayStand.remove();
            }
            for (ArmorStand s : complicationStands.values()) {
                if (s != null && s.isValid()) {
                    s.remove();
                }
            }
            complicationStands.clear();
        }

    }
}
