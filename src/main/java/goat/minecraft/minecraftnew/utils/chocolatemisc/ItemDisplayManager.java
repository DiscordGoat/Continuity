package goat.minecraft.minecraftnew.utils.chocolatemisc;

import goat.minecraft.minecraftnew.utils.ItemRegistry;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.*;
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

    // Particle GUI constants
    private static final String PARTICLE_GUI_TITLE = ChatColor.DARK_PURPLE + "Select Particle Effect / Display Mode";
    private static final int MODE_TOGGLE_SLOT = 53;

    // Expanded particle list
    // Add more particles to the menu as requested
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

        loadData();
        respawnAllDisplays();

        Bukkit.getPluginManager().registerEvents(this, plugin);
        logger.info("[ItemDisplay] Manager initialized.");
    }

    public class ItemDisplay {
        public UUID id;
        public Location blockLocation;
        public UUID standUUID;
        public ItemStack storedItem;
        public Particle particle;
        public boolean blockMode = false;
        public boolean standSpawned = false;

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
            block.setType(Material.QUARTZ_PILLAR);

            ArmorStand stand = (ArmorStand) blockLocation.getWorld().spawnEntity(
                    blockLocation.clone().add(0.5, yOffset, 0.5), EntityType.ARMOR_STAND);
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
            }

            if (rotateTask != null) rotateTask.cancel();
            if (particleTask != null) particleTask.cancel();

            displays.remove(id);
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
                    angle += 12.0; // Keep spinning speed the same
                    if (angle >= 360.0) angle -= 360.0;
                    ArmorStand stand = (ArmorStand) e;
                    Location loc = stand.getLocation();
                    loc.setYaw((float) angle);
                    stand.teleport(loc);
                }
            }.runTaskTimer(plugin, 0, 1);
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
                    Location loc = e.getLocation().add(0,0.25,0);
                    loc.getWorld().spawnParticle(particle, loc, 20, 0.2, 1.2, 0.2, 0.01);
                }
            }.runTaskTimer(plugin, 0, 40);
        }

        public void setParticle(Particle particle) {
            this.particle = particle;
            if (particleTask != null) particleTask.cancel();
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
                ItemStack copy = storedItem.clone();
                copy.setAmount(1);
                stand.getEquipment().setHelmet(copy);
                stand.setHeadPose(new EulerAngle(0,0,0));
            }
        }

        public void toggleMode() {
            this.blockMode = !this.blockMode;
            if (standSpawned) {
                ItemStack prevItem = storedItem == null ? null : storedItem.clone();
                Particle prevParticle = particle;
                UUID oldId = this.id;
                boolean oldSpawned = this.standSpawned;

                remove();
                // Re-initialize and re-spawn
                this.id = oldId;
                this.storedItem = prevItem;
                this.particle = prevParticle;
                this.standSpawned = false;
                displays.put(this.id, this);
                spawn();
                updateStandItem();
            }
        }
    }



    private boolean isItemDisplayItem(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        ItemMeta meta = item.getItemMeta();
        if (!meta.hasEnchant(Enchantment.DURABILITY)) return false;
        if (!meta.hasDisplayName()) return false;
        return ChatColor.stripColor(meta.getDisplayName()).equals("Item Display");
    }

    @EventHandler
    public void onPlayerPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItemInHand();
        if (!isItemDisplayItem(item)) return;
        Location loc = event.getBlock().getLocation();
        loc.getBlock().setType(Material.QUARTZ_PILLAR);

        ItemDisplay display = new ItemDisplay(loc);
        displays.put(display.id, display);
        player.sendMessage(ChatColor.GREEN + "Item Display placed! Shift-Right-click the pillar to set display mode.");
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (event.getHand() != EquipmentSlot.HAND) return;
        if (event.getClickedBlock() == null) return;
        if (event.getClickedBlock().getType() != Material.QUARTZ_PILLAR) return;
        Player player = event.getPlayer();
        Location loc = event.getClickedBlock().getLocation();
        ItemDisplay display = getDisplayByLocation(loc);
        if (display == null) return;

        if (!display.standSpawned) {
            display.spawn();

        } else {
            if (player.isSneaking() && player.getInventory().getItemInMainHand().getType() == Material.AIR) {
                openParticleGUI(player, display);
                return;
            }

            ItemStack hand = player.getInventory().getItemInMainHand();
            if (hand.getType() == Material.AIR) {
                if (display.storedItem != null) {
                    HashMap<Integer, ItemStack> result = player.getInventory().addItem(display.storedItem.clone());
                    if (!result.isEmpty()) {
                        player.getWorld().dropItemNaturally(player.getLocation(), display.storedItem.clone());
                    }
                    display.clearItem();
                    player.sendMessage(ChatColor.YELLOW + "You retrieved the displayed item.");
                } else {
                    player.sendMessage(ChatColor.RED + "No item is displayed here.");
                }
            } else {
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

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        if (block.getType() != Material.QUARTZ_PILLAR) return;

        ItemDisplay display = getDisplayByLocation(block.getLocation());
        if (display == null) return;

        if (display.storedItem != null) {
            block.getWorld().dropItemNaturally(block.getLocation().add(0.5,1,0.5), display.storedItem.clone());
            block.getWorld().dropItemNaturally(block.getLocation().add(0.5,1,0.5), ItemRegistry.getItemDisplayItem());
        }
        else{
            block.getWorld().dropItemNaturally(block.getLocation().add(0.5,1,0.5), ItemRegistry.getItemDisplayItem());
        }
        display.remove();
        event.getPlayer().sendMessage(ChatColor.RED + "Item display removed!");
    }

    public void openParticleGUI(Player player, ItemDisplay display) {
        Inventory inv = Bukkit.createInventory(null, 54, PARTICLE_GUI_TITLE);

        for (int i = 0; i < availableParticles.size(); i++) {
            Particle p = availableParticles.get(i);
            ItemStack item = new ItemStack(Material.FIREWORK_STAR);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(ChatColor.LIGHT_PURPLE + p.name());
            item.setItemMeta(meta);
            inv.setItem(i, item);
        }

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

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
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

        if (event.getRawSlot() == MODE_TOGGLE_SLOT) {
            display.toggleMode();
            player.sendMessage(ChatColor.GREEN + "Display mode toggled! Now in " + (display.blockMode ? "Block" : "Item") + " mode.");
            updateModeToggleItem(event.getInventory(), display);
            return;
        }

        if (!clicked.hasItemMeta()) return;
        String name = ChatColor.stripColor(clicked.getItemMeta().getDisplayName());
        Particle selectedParticle = null;
        for (Particle p : availableParticles) {
            if (p.name().equalsIgnoreCase(name)) {
                selectedParticle = p;
                break;
            }
        }

        if (selectedParticle != null) {
            display.setParticle(selectedParticle);
            player.sendMessage(ChatColor.GREEN + "Particle effect set to " + selectedParticle.name() + "!");
        }
    }

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

    private ItemDisplay getDisplayByLocation(Location loc) {
        for (ItemDisplay d : displays.values()) {
            if (d.blockLocation.equals(loc)) return d;
        }
        return null;
    }

    public void saveData() {
        // Since 'displays' is a ConcurrentHashMap, a synchronized block isn't strictly required,
        // but using one for consistent practice is fine:
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

                // Serialize the item if present
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

    @SuppressWarnings("unchecked")
    private void loadData() {
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

                        Object itemData = map.get("item");
                        if (itemData != null && itemData instanceof Map) {
                            try {
                                d.storedItem = ItemStack.deserialize((Map<String, Object>) itemData);
                            } catch (Exception ex) {
                                logger.warning("[ItemDisplay] Failed to deserialize item for display " + id + ": " + ex.getMessage());
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


    public void respawnAllDisplays() {
        for (ItemDisplay d : displays.values()) {
            if (d.standSpawned) {
                // Old stand no longer exists. Clear the old UUID.
                d.standUUID = null;
                // Set standSpawned to false so that spawn() actually runs again.
                d.standSpawned = false;
                // Now spawn the stand fresh.
                d.spawn();
                // Update the displayed item now that a new stand is in place.
                d.updateStandItem();
            }
        }
    }




    public void shutdown() {
        // Save data first
        saveData();

        // Collect displays to remove in a separate list to avoid concurrent modification
        List<ItemDisplay> toRemove = new ArrayList<>(displays.values());

        for (ItemDisplay display : toRemove) {
            display.remove();
        }

        // Clear the map to ensure no lingering references
        displays.clear();

        logger.info("[ItemDisplay] Shutdown completed. All displays removed.");
    }

}
