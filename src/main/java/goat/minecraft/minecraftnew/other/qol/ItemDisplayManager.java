package goat.minecraft.minecraftnew.other.qol;

import goat.minecraft.minecraftnew.utils.devtools.ItemRegistry;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.Slab;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.EulerAngle;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

public class ItemDisplayManager implements Listener {

    private final JavaPlugin plugin;
    private final Logger logger;
    private File dataFile;
    private YamlConfiguration dataConfig;

    private static final String DISPLAYS_KEY = "itemDisplays";

    // Map of displayID -> ItemDisplay instance
    private final Map<UUID, ItemDisplay> displays = new ConcurrentHashMap<>();

    private static final String PARTICLE_GUI_TITLE = ChatColor.DARK_PURPLE + "Select Particle Effect / Display Mode";
    private static final int MODE_TOGGLE_SLOT = 53;

    // List of all available particle effects for the GUI
    private final List<Particle> availableParticles = Arrays.asList(
            Particle.FLAME,
            Particle.HEART,
            Particle.VILLAGER_HAPPY,
            Particle.SPELL_WITCH,
            Particle.END_ROD,
            Particle.CRIT,
            Particle.CLOUD,
            Particle.NAUTILUS,
            Particle.SPELL_MOB_AMBIENT,
            Particle.TOTEM,
            Particle.CRIT_MAGIC,
            Particle.CAMPFIRE_COSY_SMOKE,
            Particle.CAMPFIRE_SIGNAL_SMOKE,
            Particle.COMPOSTER,
            Particle.CRIT_MAGIC,
            Particle.DRAGON_BREATH,
            Particle.SMOKE_LARGE,
            Particle.DUST_COLOR_TRANSITION,
            Particle.ENCHANTMENT_TABLE,
            Particle.EXPLOSION_HUGE,
            Particle.EXPLOSION_LARGE,
            Particle.EXPLOSION_NORMAL,
            Particle.FIREWORKS_SPARK,
            Particle.FALLING_DUST,
            Particle.SLIME,
            Particle.SNOWBALL,
            Particle.SNOWFLAKE,
            Particle.SQUID_INK,
            Particle.SMOKE_NORMAL,
            Particle.SMOKE_LARGE,
            Particle.LAVA,
            Particle.WARPED_SPORE,
            Particle.SPORE_BLOSSOM_AIR,
            Particle.ASH,
            Particle.WHITE_ASH,
            Particle.SOUL_FIRE_FLAME,
            Particle.SOUL,
            Particle.DRIPPING_HONEY,
            Particle.FALLING_HONEY,
            Particle.LANDING_HONEY,
            Particle.WATER_WAKE,
            Particle.FALLING_LAVA,
            Particle.LANDING_LAVA
    );

    // Map of player UUID -> display UUID they're editing
    private final Map<UUID, UUID> playerDisplayEditing = new HashMap<>();

    public ItemDisplayManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();

        dataFile = new File(plugin.getDataFolder(), "item_displays.yml");
        if (!dataFile.exists()) {
            try {
                dataFile.createNewFile();
            } catch (IOException e) {
                logger.severe("[ItemDisplay] Could not create data file!");
                e.printStackTrace();
            }
        }

        // Register this class as an event listener
        Bukkit.getPluginManager().registerEvents(this, plugin);

        // Load any saved displays
        loadData();
        // Respawn the display stands
        respawnAllDisplays();

        logger.info("[ItemDisplay] Manager initialized.");
    }

    /**
     * The inner class representing one individual item display.
     */
    public class ItemDisplay {

        public UUID id;
        public Location blockLocation;
        public UUID standUUID;
        public ItemStack storedItem;
        public Particle particle;
        public boolean blockMode = false;
        public boolean standSpawned = false;

        // The UUID of the player who placed this display
        public UUID placerUUID;

        public BukkitTask rotateTask;
        public BukkitTask particleTask;

        public ItemDisplay(Location blockLocation) {
            this.id = UUID.randomUUID();
            this.blockLocation = blockLocation;
        }

        public void spawn() {
            if (standSpawned) return;
            standSpawned = true;

            double yOffset = blockMode ? 0.4 : 0.1;
            Block block = blockLocation.getBlock();

            // If the block is air, set it to quartz by default
            if (block.getType() == Material.AIR) {
                block.setType(Material.QUARTZ_PILLAR);
            }

            ArmorStand stand = (ArmorStand) blockLocation.getWorld().spawnEntity(
                    blockLocation.clone().add(0.5, yOffset, 0.5),
                    EntityType.ARMOR_STAND
            );
            stand.setInvisible(true);
            stand.setMarker(true);
            stand.setGravity(false);
            stand.setBasePlate(false);
            stand.setArms(false);
            stand.setSmall(true);
            stand.setInvulnerable(true);
            stand.setCustomNameVisible(false);

            this.standUUID = stand.getUniqueId();
            updateStandItem();
            startRotationTask();

            if (particle != null) {
                startParticleTask();
            }

            displays.put(id, this);
        }

        public void remove() {
            if (standUUID != null) {
                Entity e = Bukkit.getEntity(standUUID);
                if (e != null && e.isValid()) {
                    e.remove();
                }
                standUUID = null;
            }

            if (rotateTask != null) {
                rotateTask.cancel();
                rotateTask = null;
            }
            if (particleTask != null) {
                particleTask.cancel();
                particleTask = null;
            }

            displays.remove(id);
            standSpawned = false;
        }

        public void startRotationTask() {
            rotateTask = new BukkitRunnable() {
                double angle = 0;

                @Override
                public void run() {
                    if (standUUID == null) {
                        cancel();
                        return;
                    }
                    Entity e = Bukkit.getEntity(standUUID);
                    if (e == null || !(e instanceof ArmorStand) || !e.isValid()) {
                        cancel();
                        return;
                    }
                    angle += 12.0;
                    if (angle >= 360.0) angle -= 360.0;

                    ArmorStand stand = (ArmorStand) e;
                    Location loc = stand.getLocation();
                    loc.setYaw((float) angle);
                    stand.teleport(loc);
                }
            }.runTaskTimer(plugin, 0, 1); // rotate fast
        }

        public void startParticleTask() {
            if (particle == null) return;
            particleTask = new BukkitRunnable() {
                @Override
                public void run() {
                    if (standUUID == null) {
                        cancel();
                        return;
                    }
                    Entity e = Bukkit.getEntity(standUUID);
                    if (e == null || !e.isValid()) {
                        cancel();
                        return;
                    }
                    Location loc = e.getLocation().add(0, 0.25, 0);
                    loc.getWorld().spawnParticle(particle, loc, 20, 0.2, 1.2, 0.2, 0.01);
                }
            }.runTaskTimer(plugin, 0, 40); // spawn particles every 2 seconds
        }

        public void setParticle(Particle particle) {
            this.particle = particle;
            if (particleTask != null) {
                particleTask.cancel();
            }
            if (standSpawned) {
                startParticleTask();
            }
        }

        public void setItem(ItemStack item) {
            this.storedItem = item;
            if (standSpawned) {
                updateStandItem();
            }
        }

        public void clearItem() {
            this.storedItem = null;
            if (standSpawned) {
                updateStandItem();
            }
        }

        private void updateStandItem() {
            if (!standSpawned) return;
            Entity e = Bukkit.getEntity(standUUID);
            if (e == null || !(e instanceof ArmorStand)) return;

            ArmorStand stand = (ArmorStand) e;
            if (storedItem == null) {
                stand.getEquipment().setHelmet(null);
            } else {
                // Ensure we only show 1 item
                ItemStack copy = storedItem.clone();
                copy.setAmount(1);
                stand.getEquipment().setHelmet(copy);
                // Keep stand's head pose level
                stand.setHeadPose(new EulerAngle(0, 0, 0));
            }
        }

        public void toggleMode() {
            this.blockMode = !this.blockMode;
            if (standSpawned) {
                ItemStack prevItem = (storedItem == null ? null : storedItem.clone());
                Particle prevParticle = particle;
                UUID oldId = this.id;
                UUID oldPlacer = this.placerUUID;

                remove();
                this.id = oldId;
                this.storedItem = prevItem;
                this.particle = prevParticle;
                this.placerUUID = oldPlacer;
                this.standSpawned = false;
                displays.put(this.id, this);

                spawn();
                updateStandItem();
            }
        }
    }

    /**
     * Checks if an ItemStack is our special "Item Display" item
     */
    private boolean isItemDisplayItem(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        ItemMeta meta = item.getItemMeta();
        if (!meta.hasEnchant(Enchantment.DURABILITY)) return false;
        if (!meta.hasDisplayName()) return false;
        return ChatColor.stripColor(meta.getDisplayName()).equals("Item Display");
    }

    /**
     * Player places a new ItemDisplay block if they used the 'Item Display' item.
     */
    @EventHandler
    public void onPlayerPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItemInHand();
        if (!isItemDisplayItem(item)) return;

        Location loc = event.getBlock().getLocation();
        loc.getBlock().setType(Material.QUARTZ_PILLAR);

        ItemDisplay display = new ItemDisplay(loc);
        display.placerUUID = player.getUniqueId();
        display.spawn();
        displays.put(display.id, display);

        player.sendMessage(ChatColor.GREEN + "Item Display placed!");
    }

    /**
     * Handles SHIFT + LEFT CLICK to change block type
     * and RIGHT CLICK for display interactions (retrieving item, placing item, opening particle GUI).
     */
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getClickedBlock() == null) return;
        if (event.getHand() == EquipmentSlot.HAND) {

            Block block = event.getClickedBlock();
            Location loc = block.getLocation();

            ItemDisplay display = getDisplayByLocation(loc);
            if (display == null) {
                return; // Not an ItemDisplay location
            }

            Player player = event.getPlayer();
            ItemStack hand = player.getInventory().getItemInMainHand();

            // SHIFT + LEFT CLICK to change the block type
            if (event.getAction() == Action.LEFT_CLICK_BLOCK && player.isSneaking()) {
                // Does the player have a block in their hand?
                if (hand != null && hand.getType().isBlock() && hand.getAmount() > 0) {
                    Material type = hand.getType();
                    // If it's a slab, place it as a TOP slab
                    if (type.name().endsWith("_SLAB")) {
                        block.setType(type);
                        Slab slab = (Slab) block.getBlockData();
                        slab.setType(Slab.Type.TOP);
                        block.setBlockData(slab);
                    } else {
                        block.setType(type);
                    }
                    // Optionally decrement 1 from the player's hand stack
                    // (only if you want the block to be "used up")
                }
            }

            // RIGHT CLICK interactions
            if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                // If stand is missing for some reason, re-spawn it
                if (!display.standSpawned) {
                    display.spawn();
                    return;
                }

                // SHIFT RIGHT-CLICK with empty hand opens Particle GUI
                if (player.isSneaking() && hand.getType() == Material.AIR) {
                    openParticleGUI(player, display);
                    return;
                }

                // If the player is empty-handed, retrieve the displayed item
                if (hand.getType() == Material.AIR) {
                    if (display.storedItem != null) {
                        HashMap<Integer, ItemStack> leftover = player.getInventory().addItem(display.storedItem.clone());
                        if (!leftover.isEmpty()) {
                            // If inventory is full, drop item at player's feet
                            player.getWorld().dropItemNaturally(player.getLocation(), display.storedItem.clone());
                        }
                        display.clearItem();
                        player.sendMessage(ChatColor.YELLOW + "You retrieved the displayed item.");
                    } else {
                        player.sendMessage(ChatColor.RED + "No item is displayed here.");
                    }
                } else {
                    // If there's no stored item, place the current item in hand
                    if (display.storedItem == null) {
                        ItemStack toPlace = hand.clone();
                        toPlace.setAmount(1);
                        display.setItem(toPlace);

                        hand.setAmount(hand.getAmount() - 1);
                        player.sendMessage(ChatColor.GREEN + "Item displayed!");
                        event.setCancelled(true);
                    } else {
                        player.sendMessage(ChatColor.RED + "An item is already displayed here.");
                    }
                }
            }
        }
    }

    /**
     * If the block is broken and there's an ItemDisplay, drop it properly.
     */
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        ItemDisplay display = getDisplayByLocation(block.getLocation());
        if (display == null) {
            return; // Not an ItemDisplay block
        }

        // If there's an item stored, drop that
        if (display.storedItem != null) {
            block.getWorld().dropItemNaturally(block.getLocation().add(0.5, 1, 0.5), display.storedItem.clone());
        }
        // Always drop the 'Item Display' item
        block.getWorld().dropItemNaturally(block.getLocation().add(0.5, 1, 0.5), ItemRegistry.getItemDisplayItem());

        // Remove the display
        display.remove();
        event.getPlayer().sendMessage(ChatColor.RED + "Item display removed!");
    }

    /**
     * Opens the particle selection GUI for a given player & ItemDisplay.
     */
    public void openParticleGUI(Player player, ItemDisplay display) {
        Inventory inv = Bukkit.createInventory(null, 54, PARTICLE_GUI_TITLE);

        for (int i = 0; i < availableParticles.size(); i++) {
            if (i >= 53) break; // keep slot 53 for the mode toggle
            Particle p = availableParticles.get(i);
            ItemStack item = new ItemStack(Material.FIREWORK_STAR);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(ChatColor.LIGHT_PURPLE + p.name());
            item.setItemMeta(meta);
            inv.setItem(i, item);
        }

        // Mode Toggle lever in slot 53
        ItemStack modeToggle = new ItemStack(Material.LEVER);
        ItemMeta meta = modeToggle.getItemMeta();
        meta.setDisplayName(ChatColor.YELLOW + "Toggle Display Mode");
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + "Current Mode: " + (display.blockMode ? "Block" : "Item"));
        lore.add(ChatColor.GREEN + "Click to toggle mode");
        meta.setLore(lore);
        modeToggle.setItemMeta(meta);
        inv.setItem(MODE_TOGGLE_SLOT, modeToggle);

        playerDisplayEditing.put(player.getUniqueId(), display.id);
        player.openInventory(inv);
    }

    /**
     * Handles clicks in our custom particle GUI.
     */
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        // Check the inventory title
        if (!event.getView().getTitle().equals(PARTICLE_GUI_TITLE)) return;
        event.setCancelled(true);

        Player player = (Player) event.getWhoClicked();
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;

        UUID playerId = player.getUniqueId();
        UUID displayId = playerDisplayEditing.get(playerId);
        if (displayId == null) return;

        ItemDisplay display = displays.get(displayId);
        if (display == null) return;

        // Mode toggle slot
        if (event.getRawSlot() == MODE_TOGGLE_SLOT) {
            display.toggleMode();
            player.sendMessage(ChatColor.GREEN + "Display mode toggled! Now in "
                    + (display.blockMode ? "Block" : "Item") + " mode.");
            updateModeToggleItem(event.getInventory(), display);
            return;
        }

        // Otherwise, they clicked on a particle item
        if (!clicked.hasItemMeta()) return;
        String name = ChatColor.stripColor(clicked.getItemMeta().getDisplayName());
        Particle selected = null;

        for (Particle p : availableParticles) {
            if (p.name().equalsIgnoreCase(name)) {
                selected = p;
                break;
            }
        }

        if (selected != null) {
            display.setParticle(selected);
            player.sendMessage(ChatColor.GREEN + "Particle effect set to " + selected.name() + "!");
        }
    }

    /**
     * Refreshes the lore on the "Toggle Display Mode" item so it shows the correct current mode.
     */
    private void updateModeToggleItem(Inventory inv, ItemDisplay display) {
        ItemStack modeToggle = inv.getItem(MODE_TOGGLE_SLOT);
        if (modeToggle == null) return;

        ItemMeta meta = modeToggle.getItemMeta();
        if (meta == null) return;

        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + "Current Mode: " + (display.blockMode ? "Block" : "Item"));
        lore.add(ChatColor.GREEN + "Click to toggle mode");
        meta.setLore(lore);
        modeToggle.setItemMeta(meta);
        inv.setItem(MODE_TOGGLE_SLOT, modeToggle);
    }

    /**
     * Retrieves the ItemDisplay at a specific block location, if any.
     */
    private ItemDisplay getDisplayByLocation(Location loc) {
        for (ItemDisplay d : displays.values()) {
            if (d.blockLocation.equals(loc)) {
                return d;
            }
        }
        return null;
    }

    /**
     * Returns all displays placed by a specific player.
     */
    public List<ItemDisplay> getDisplaysByPlayer(UUID playerUUID) {
        List<ItemDisplay> result = new ArrayList<>();
        for (ItemDisplay d : displays.values()) {
            if (playerUUID.equals(d.placerUUID)) {
                result.add(d);
            }
        }
        return result;
    }

    /**
     * Save all ItemDisplays to file.
     */
    public void saveData() {
        synchronized (displays) {
            dataConfig = new YamlConfiguration();
            List<Map<String, Object>> displayList = new ArrayList<>();

            for (ItemDisplay d : displays.values()) {
                Map<String, Object> map = new HashMap<>();
                map.put("id", d.id.toString());
                map.put("world", d.blockLocation.getWorld().getName());
                map.put("x", d.blockLocation.getX());
                map.put("y", d.blockLocation.getY());
                map.put("z", d.blockLocation.getZ());
                map.put("particle", d.particle != null ? d.particle.name() : null);
                map.put("standSpawned", d.standSpawned);
                map.put("blockMode", d.blockMode);
                map.put("standUUID", d.standUUID != null ? d.standUUID.toString() : null);
                map.put("placerUUID", d.placerUUID != null ? d.placerUUID.toString() : null);

                // Serialize the stored item
                if (d.storedItem != null) {
                    map.put("item", d.storedItem.serialize());
                } else {
                    map.put("item", null);
                }
                displayList.add(map);
            }

            dataConfig.set(DISPLAYS_KEY, displayList);

            try {
                dataConfig.save(dataFile);
                logger.info("[ItemDisplay] Data saved.");
            } catch (IOException e) {
                logger.severe("[ItemDisplay] Failed to save data!");
                e.printStackTrace();
            }
        }
    }

    /**
     * Load all ItemDisplays from the data file.
     */
    public void loadData() {
        dataConfig = YamlConfiguration.loadConfiguration(dataFile);
        if (dataConfig.contains(DISPLAYS_KEY)) {
            List<Map<String, Object>> displayList = (List<Map<String, Object>>) dataConfig.getList(DISPLAYS_KEY);
            if (displayList != null) {
                for (Map<String, Object> map : displayList) {
                    try {
                        UUID id = UUID.fromString((String) map.get("id"));
                        String worldName = (String) map.get("world");
                        double x = (Double) map.get("x");
                        double y = (Double) map.get("y");
                        double z = (Double) map.get("z");

                        World w = Bukkit.getWorld(worldName);
                        if (w == null) {
                            logger.warning("[ItemDisplay] World '" + worldName + "' not found, skipping display " + id);
                            continue;
                        }

                        Location loc = new Location(w, x, y, z);
                        ItemDisplay d = new ItemDisplay(loc);
                        d.id = id;

                        String particleName = (String) map.get("particle");
                        if (particleName != null) {
                            try {
                                d.particle = Particle.valueOf(particleName);
                            } catch (IllegalArgumentException e) {
                                d.particle = null;
                                logger.warning("[ItemDisplay] Invalid particle '" + particleName + "' for display " + id);
                            }
                        }

                        d.standSpawned = map.containsKey("standSpawned") && (Boolean) map.get("standSpawned");
                        d.blockMode = map.containsKey("blockMode") && (Boolean) map.get("blockMode");

                        String standUUIDStr = (String) map.get("standUUID");
                        if (standUUIDStr != null) {
                            try {
                                d.standUUID = UUID.fromString(standUUIDStr);
                            } catch (IllegalArgumentException ignored) {}
                        }

                        String placerUUIDStr = (String) map.get("placerUUID");
                        if (placerUUIDStr != null) {
                            try {
                                d.placerUUID = UUID.fromString(placerUUIDStr);
                            } catch (IllegalArgumentException ignored) {}
                        }

                        Object itemData = map.get("item");
                        if (itemData instanceof Map) {
                            try {
                                //noinspection unchecked
                                d.storedItem = ItemStack.deserialize((Map<String, Object>) itemData);
                            } catch (Exception ex) {
                                logger.warning("[ItemDisplay] Failed to deserialize item for display "
                                        + id + ": " + ex.getMessage());
                                d.storedItem = null;
                            }
                        }

                        displays.put(id, d);

                    } catch (Exception ex) {
                        logger.warning("[ItemDisplay] Error loading a display entry: " + ex.getMessage());
                    }
                }
            }
        }
        logger.info("[ItemDisplay] Data loaded. Found " + displays.size() + " displays.");
    }

    /**
     * Respawn all displays on server startup.
     */
    public void respawnAllDisplays() {
        for (ItemDisplay d : displays.values()) {
            d.standUUID = null;
            d.standSpawned = false;
            d.spawn();
            d.updateStandItem();
        }
    }

    /**
     * Gracefully shut down and remove all displays, then save data.
     */
    public void shutdown() {
        saveData();
        List<ItemDisplay> toRemove = new ArrayList<>(displays.values());
        for (ItemDisplay display : toRemove) {
            display.remove();
        }
        displays.clear();
        logger.info("[ItemDisplay] Shutdown completed. All displays removed.");
    }
}
