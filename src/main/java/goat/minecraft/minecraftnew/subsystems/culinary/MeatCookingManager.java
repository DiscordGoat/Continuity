package goat.minecraft.minecraftnew.subsystems.culinary;

import org.bukkit.*;
import org.bukkit.block.data.type.Furnace;
import org.bukkit.entity.*;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.EulerAngle;

import java.util.*;

/**
 * MeatCookingManager
 *
 * Handles the cooking process of various meats in furnaces with unique stages per meat type.
 * Utilizes ArmorStands to display cooking stages and the meat being cooked.
 */
public class MeatCookingManager implements Listener {

    private JavaPlugin plugin;
    // Map furnace location to cooking data
    private Map<Location, MeatCookingData> cookingMap = new HashMap<>();
    // Map to store cooking stages per meat type
    private Map<Material, List<String>> meatStagesMap = new HashMap<>();

    /**
     * MeatCookingManager constructor.
     * Initializes the cooking stages for each meat type and registers event listeners.
     */
    public MeatCookingManager(JavaPlugin plugin) {
        this.plugin = plugin;
        Bukkit.getLogger().info("[MeatCookingManager] Registering events...");
        Bukkit.getPluginManager().registerEvents(this, plugin);
        Bukkit.getLogger().info("[MeatCookingManager] Events registered.");
        initializeMeatStages();
    }

    /**
     * Initializes cooking stages for each raw meat type.
     * Modify this method to adjust stages per meat type as needed.
     */
    private void initializeMeatStages() {
        // Define cooking stages for Beef
        meatStagesMap.put(Material.BEEF, Arrays.asList(
                "Raw Beef", "Rare Steak", "Medium Rare Steak", "Well Done Steak", "Burnt Steak"
        ));

        // Define cooking stages for Porkchop
        meatStagesMap.put(Material.PORKCHOP, Arrays.asList(
                "Raw Porkchop", "Cooked Porkchop", "Bacon", "Ham", "Burnt Porkchop"
        ));

        // Define cooking stages for Chicken
        meatStagesMap.put(Material.CHICKEN, Arrays.asList(
                "Raw Chicken", "Cooked Chicken", "Burnt Chicken"
        ));

        // Define cooking stages for Mutton
        meatStagesMap.put(Material.MUTTON, Arrays.asList(
                "Raw Mutton", "Medium Rare Mutton", "Medium Mutton", "Well Done Mutton", "Burnt Mutton"
        ));

        // Define cooking stages for Rabbit
        meatStagesMap.put(Material.RABBIT, Arrays.asList(
                "Raw Rabbit", "Cooked Rabbit", "Burnt Rabbit"
        ));

        // Add more meat types and their stages as needed
        // Example:
        // meatStagesMap.put(Material.FISH, Arrays.asList("Raw", "Cooked", "Burnt"));
    }

    /**
     * Data class to hold information about a piece of meat currently cooking.
     */
    public static class MeatCookingData {
        Location furnaceLocation;
        UUID standUUID;          // ArmorStand that shows cooking stage text
        UUID itemStandUUID;      // ArmorStand holding the raw/cooking item
        BukkitTask particleTask; // Task for spawning flame particles on item stand
        int stageIndex = 0;
        boolean stopped = false;
        Material rawMeatType;
        UUID ownerPlayerUUID;

        public MeatCookingData(Location furnaceLocation, UUID standUUID, UUID itemStandUUID, Material rawMeatType, UUID owner, BukkitTask particleTask) {
            this.furnaceLocation = furnaceLocation;
            this.standUUID = standUUID;
            this.itemStandUUID = itemStandUUID;
            this.rawMeatType = rawMeatType;
            this.ownerPlayerUUID = owner;
            this.particleTask = particleTask;
        }
    }

    /**
     * Player interacts with a furnace with raw meat.
     */
    @EventHandler
    public void onPlayerRightClickFurnace(PlayerInteractEvent event) {
        if (event.getClickedBlock() == null) {
            Bukkit.getLogger().info("[MeatCookingManager] onPlayerRightClickFurnace: No block clicked.");
            return;
        }

        if (event.getClickedBlock().getType() != Material.FURNACE) {
            return; // Not a furnace
        }

        if (event.getAction() != org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK) {
            return; // Not a right-click block action
        }

        Player player = event.getPlayer();
        ItemStack hand = player.getInventory().getItemInMainHand();
        Location furnaceLoc = event.getClickedBlock().getLocation();
        Furnace furnace = (Furnace) event.getClickedBlock().getBlockData();
        furnace.setLit(true);
        Bukkit.getLogger().info("[MeatCookingManager] onPlayerRightClickFurnace: Player=" + player.getName()
                + ", Item=" + (hand != null ? hand.getType() : "null") + ", Furnace=" + furnaceLoc);

        if (cookingMap.containsKey(furnaceLoc)) {
            // Furnace is cooking
            if (!isRawMeat(hand)) {
                // Stop cooking
                Bukkit.getLogger().info("[MeatCookingManager] onPlayerRightClickFurnace: Stopping cooking.");
                event.setCancelled(true);
                MeatCookingData data = cookingMap.get(furnaceLoc);
                stopCooking(data, player, furnace);
                player.damage(1);
                player.getLocation().getWorld().playSound(player.getLocation(), Sound.ENTITY_PLAYER_HURT_ON_FIRE, 1.0f, 1.0f);
            } else {
                // Furnace is already cooking and player is trying to start another cooking session
                player.sendMessage(ChatColor.RED + "This furnace is already cooking.");
                Bukkit.getLogger().info("[MeatCookingManager] onPlayerRightClickFurnace: Furnace already cooking.");
            }
            return;
        }

        // Furnace not cooking yet, need raw meat to start
        if (!isRawMeat(hand)) {
            Bukkit.getLogger().info("[MeatCookingManager] onPlayerRightClickFurnace: No raw meat, no action.");
            return;
        }

        // Start cooking
        event.setCancelled(true);
        startCooking(furnaceLoc, player, hand);
    }

    /**
     * Start cooking a piece of meat on the furnace.
     */
    private void startCooking(Location furnaceLoc, Player player, ItemStack rawMeat) {
        Bukkit.getLogger().info("[MeatCookingManager] startCooking: Player=" + player.getName()
                + ", Furnace=" + furnaceLoc + ", Meat=" + rawMeat.getType());

// Retrieve the current amount of meat
        int amt = rawMeat.getAmount();

// Log the intention to consume one meat
        Bukkit.getLogger().info("[MeatCookingManager] startCooking: Attempting to consume one meat. Current amount=" + amt);

// Subtract one from the current amount
        int newAmt = amt - 1;

// Update the ItemStack based on the new amount
        if (newAmt > 0) {
            // There is still meat left after consumption
            rawMeat.setAmount(newAmt);
            player.getInventory().setItemInMainHand(rawMeat);
            Bukkit.getLogger().info("[MeatCookingManager] startCooking: Consumed one meat. Remaining=" + rawMeat.getAmount());
        } else {
            // No meat left; remove the item from the player's hand
            player.getInventory().setItemInMainHand(null);
            Bukkit.getLogger().info("[MeatCookingManager] startCooking: Consumed last meat. Hand is now empty.");
        }



        Material rawType = rawMeat.getType();
        List<String> stages = getMeatStagesForMaterial(rawType);
        if (stages == null || stages.isEmpty()) {
            player.sendMessage(ChatColor.RED + "This meat cannot be cooked.");
            Bukkit.getLogger().warning("[MeatCookingManager] startCooking: No stages found for " + rawType);
            return;
        }

        String initialStage = stages.get(0);
        Bukkit.getLogger().info("[MeatCookingManager] startCooking: Initial stage=" + initialStage);

        // Spawn stage text ArmorStand moved up 1 block
        Location textLoc = furnaceLoc.clone().add(0.5, 1.0, 0.5); // Moved up 1 block
        UUID standUUID = spawnCookingStand(textLoc, initialStage);
        Bukkit.getLogger().info("[MeatCookingManager] startCooking: Spawned stage text stand UUID=" + standUUID);

        // Display the raw item on top of the furnace, moved down 0.5 blocks and centered
        Location itemLoc = furnaceLoc.clone().add(0.9, 0.2, 0.2); // Moved down 0.5 blocks
        UUID itemStandUUID = spawnItemStand(itemLoc, rawType);
        Bukkit.getLogger().info("[MeatCookingManager] startCooking: Spawned item stand UUID=" + itemStandUUID);

        // Spawn fire particles and play sound effects
        playFireEffects(furnaceLoc);

        // Spawn flame particles on the food ArmorStand every 3 ticks
        BukkitTask particleTask = new BukkitRunnable() {
            @Override
            public void run() {
                Entity itemStand = Bukkit.getEntity(itemStandUUID);
                if (itemStand != null && itemStand.isValid()) {
                    Location loc = itemStand.getLocation().add(0, 0.0, 0); // Slightly above the stand
                    playFireEffects(furnaceLoc);
                } else {
                    Bukkit.getLogger().warning("[MeatCookingManager] ParticleTask: Item ArmorStand not found or invalid.");
                    cancel(); // Cancel task if ArmorStand is gone
                }
            }
        }.runTaskTimer(plugin, 0, 10); // Every 3 ticks

        // Create and store cooking data
        MeatCookingData data = new MeatCookingData(furnaceLoc, standUUID, itemStandUUID, rawType, player.getUniqueId(), particleTask);
        cookingMap.put(furnaceLoc, data);
        Bukkit.getLogger().info("[MeatCookingManager] startCooking: Data stored in cookingMap for " + furnaceLoc);

        // Schedule task every 30 seconds (600 ticks) to advance stage
        new BukkitRunnable() {
            @Override
            public void run() {
                MeatCookingData d = cookingMap.get(furnaceLoc);
                if (d == null) {
                    Bukkit.getLogger().info("[MeatCookingManager] CookingTask: Data is null, stopping.");
                    cancel();
                    return;
                }
                if (d.stopped) {
                    Bukkit.getLogger().info("[MeatCookingManager] CookingTask: Cooking stopped, stopping timer.");
                    cancel();
                    return;
                }

                d.stageIndex++;
                Bukkit.getLogger().info("[MeatCookingManager] CookingTask: Advanced stage to " + d.stageIndex + " for " + rawType);
                if (d.stageIndex >= stages.size()) {
                    d.stageIndex = stages.size() - 1;
                    Bukkit.getLogger().info("[MeatCookingManager] CookingTask: Stage index adjusted to last stage.");
                }

                Entity stand = Bukkit.getEntity(d.standUUID);
                if (stand instanceof ArmorStand) {
                    ArmorStand as = (ArmorStand) stand;
                    as.setCustomName(ChatColor.GOLD + stages.get(d.stageIndex)); // Gold color
                    Bukkit.getLogger().info("[MeatCookingManager] CookingTask: Updated stand name to " + stages.get(d.stageIndex));
                } else {
                    Bukkit.getLogger().warning("[MeatCookingManager] CookingTask: Stand not found or not an ArmorStand!");
                }

                // Play fire effects each stage progression
                playFireEffects(furnaceLoc);
            }
        }.runTaskTimer(plugin, 15 * 20, 15 * 20); // Initial delay 10s, then every 30s (600 ticks)

        player.sendMessage(ChatColor.GREEN + "You placed the meat on top of the furnace. It will cook over time.");
        player.sendMessage(ChatColor.YELLOW + "Right-click the furnace again (without raw meat) to stop cooking and retrieve your meat.");
        Bukkit.getLogger().info("[MeatCookingManager] startCooking: Cooking started successfully for " + rawType);
    }
    public void cancelAllCookingsOnShutdown() {
        // Iterate over all currently cooking furnaces
        for (MeatCookingData data : new ArrayList<>(cookingMap.values())) {
            // Mark as stopped
            data.stopped = true;
            cookingMap.remove(data.furnaceLocation);

            // Remove stands
            removeEntityByUUID(data.standUUID);
            removeEntityByUUID(data.itemStandUUID);

            // Cancel particle tasks
            if (data.particleTask != null) {
                data.particleTask.cancel();
            }

            // Drop the raw meat item at the top of the furnace
            // Adjust the offset as desired, here we put it slightly above the furnace
            Location dropLoc = data.furnaceLocation.clone().add(0.5, 1.0, 0.5);
            ItemStack rawItem = new ItemStack(data.rawMeatType, 1);
            dropLoc.getWorld().dropItemNaturally(dropLoc, rawItem);
        }
        Bukkit.getLogger().info("[MeatCookingManager] All cooking sessions canceled and raw meat dropped on shutdown.");
    }

    /**
     * Stop cooking and drop the current stage meat.
     */
    private void stopCooking(MeatCookingData data, Player player, Furnace furnace) {
        Bukkit.getLogger().info("[MeatCookingManager] stopCooking: Stopping cooking. Furnace=" + data.furnaceLocation + ", StageIndex=" + data.stageIndex);
        data.stopped = true;
        furnace.setLit(false);
        cookingMap.remove(data.furnaceLocation);
        Bukkit.getLogger().info("[MeatCookingManager] stopCooking: Removed from cookingMap.");

        // Remove stage text ArmorStand
        removeEntityByUUID(data.standUUID);
        Bukkit.getLogger().info("[MeatCookingManager] stopCooking: Removed stage text stand.");

        // Remove item ArmorStand
        removeEntityByUUID(data.itemStandUUID);
        Bukkit.getLogger().info("[MeatCookingManager] stopCooking: Removed item stand.");

        // Cancel particle spawning task
        if (data.particleTask != null) {
            data.particleTask.cancel();
            Bukkit.getLogger().info("[MeatCookingManager] stopCooking: Canceled particle spawning task.");
        }

        // Get stages for the material
        List<String> stages = getMeatStagesForMaterial(data.rawMeatType);
        if (stages == null || stages.isEmpty()) {
            player.sendMessage(ChatColor.RED + "Could not retrieve your meat.");
            Bukkit.getLogger().warning("[MeatCookingManager] stopCooking: No stages for " + data.rawMeatType);
            return;
        }

        String stageName = stages.get(data.stageIndex);
        ItemStack result = getStageItem(data.rawMeatType, stageName);
        player.getWorld().dropItemNaturally(player.getLocation(), result);
        player.sendMessage(ChatColor.GREEN + "You retrieved a " + stageName + " " + getMeatName(data.rawMeatType) + ".");
        Bukkit.getLogger().info("[MeatCookingManager] stopCooking: Dropped " + stageName + " " + getMeatName(data.rawMeatType));
    }

    /**
     * Returns whether the provided item is a raw meat recognized by this system.
     */
    private boolean isRawMeat(ItemStack item) {
        if (item == null) {
            Bukkit.getLogger().info("[MeatCookingManager] isRawMeat: item=null");
            return false;
        }
        Material mat = item.getType();
        boolean result = meatStagesMap.containsKey(mat);
        Bukkit.getLogger().info("[MeatCookingManager] isRawMeat: " + mat + " -> " + result);
        return result;
    }

    /**
     * Returns the list of cooking stages for a particular raw meat type.
     */
    private List<String> getMeatStagesForMaterial(Material mat) {
        List<String> stages = meatStagesMap.get(mat);
        if (stages == null) {
            Bukkit.getLogger().warning("[MeatCookingManager] getMeatStagesForMaterial: No stages defined for " + mat);
            return Collections.emptyList();
        }
        return stages;
    }

    /**
     * Returns a cooked item for the given stage.
     */
    private ItemStack getStageItem(Material rawType, String stageName) {
        Material resultMat = getResultMaterialForStage(rawType, stageName);
        ItemStack item = new ItemStack(resultMat, 1);
        if (item.getItemMeta() != null) {
            ItemMeta meta = item.getItemMeta();
            String display = ChatColor.GOLD + stageName;
            meta.setDisplayName(display);
            item.setItemMeta(meta);
            Bukkit.getLogger().info("[MeatCookingManager] getStageItem: Created " + display + " from " + rawType);
        }
        return item;
    }

    /**
     * Determine the result material based on raw meat type and stage.
     * For simplicity, this method returns the cooked version of the meat.
     */
    private Material getResultMaterialForStage(Material rawType, String stageName) {
        switch (rawType) {
            case BEEF:
                // Assuming 'COOKED_BEEF' represents all cooked stages
                // If you want distinct items for each stage, additional logic is needed
                return Material.COOKED_BEEF;
            case PORKCHOP:
                return Material.COOKED_PORKCHOP;
            case CHICKEN:
                return Material.COOKED_CHICKEN;
            case MUTTON:
                return Material.COOKED_MUTTON;
            case RABBIT:
                return Material.COOKED_RABBIT;
            default:
                Bukkit.getLogger().warning("[MeatCookingManager] getResultMaterialForStage: Unknown rawType " + rawType);
                return rawType;
        }
    }

    /**
     * Returns a nice display name for the meat type.
     */
    private String getMeatName(Material rawType) {
        switch (rawType) {
            case BEEF:
                return "Steak";
            case PORKCHOP:
                return "Pork Chop";
            case CHICKEN:
                return "Chicken";
            case MUTTON:
                return "Mutton";
            case RABBIT:
                return "Rabbit";
            default:
                Bukkit.getLogger().warning("[MeatCookingManager] getMeatName: Unexpected meat type " + rawType);
                return "Meat";
        }
    }

    /**
     * Spawn an ArmorStand to display the cooking stage text, moved up 1 block.
     */
    private UUID spawnCookingStand(Location loc, String stageName) {
        Bukkit.getLogger().info("[MeatCookingManager] spawnCookingStand: Spawning stand at " + loc + " stageName=" + stageName);
        ArmorStand stand = (ArmorStand) loc.getWorld().spawnEntity(loc, EntityType.ARMOR_STAND);
        stand.setInvisible(true);
        stand.setCustomNameVisible(true);
        stand.setCustomName(ChatColor.GOLD + stageName); // Gold color
        stand.setGravity(false);
        stand.setInvulnerable(true);
        stand.setMarker(true); // Marker so no hitbox
        UUID uuid = stand.getUniqueId();
        Bukkit.getLogger().info("[MeatCookingManager] spawnCookingStand: Spawned stand UUID=" + uuid);
        return uuid;
    }

    /**
     * Spawn an ArmorStand to display the food item, moved down 0.5 blocks and centered.
     */
    private UUID spawnItemStand(Location loc, Material meatType) {
        Bukkit.getLogger().info("[MeatCookingManager] spawnItemStand: Spawning item stand at " + loc + " with " + meatType);
        ArmorStand stand = (ArmorStand) loc.getWorld().spawnEntity(loc, EntityType.ARMOR_STAND);
        stand.setInvisible(true);
        stand.setMarker(true); // so item hovers at exact point
        stand.setInvulnerable(true);
        stand.setGravity(false);
        stand.setCustomNameVisible(false);

        // Create the raw item to display
        ItemStack rawItem = new ItemStack(meatType, 1);
        stand.getEquipment().setItemInMainHand(rawItem);
        stand.setRightArmPose(new EulerAngle(0, 0, 0));
        stand.setSmall(false); // smaller stand so item hovers nicely

        UUID uuid = stand.getUniqueId();
        Bukkit.getLogger().info("[MeatCookingManager] spawnItemStand: Spawned item stand UUID=" + uuid);
        return uuid;
    }

    /**
     * Utility to remove an entity by UUID.
     */
    private void removeEntityByUUID(UUID uuid) {
        Bukkit.getLogger().info("[MeatCookingManager] removeEntityByUUID: Removing " + uuid);
        Entity e = Bukkit.getEntity(uuid);
        if (e != null) {
            e.remove();
            Bukkit.getLogger().info("[MeatCookingManager] removeEntityByUUID: Entity removed.");
        } else {
            Bukkit.getLogger().info("[MeatCookingManager] removeEntityByUUID: Entity not found.");
        }
    }

    /**
     * Play fire particles and sound effects at the furnace location.
     */
    public void playFireEffects(Location furnaceLoc) {
        World w = furnaceLoc.getWorld();
        double x = furnaceLoc.getX() + 0.5;
        double y = furnaceLoc.getY() + 1.0;
        double z = furnaceLoc.getZ() + 0.5;
        // Fire particles
        w.spawnParticle(Particle.FLAME, x, y, z, 10, 0.2, 0.2, 0.2, 0.01);
        // Fire sound
        w.playSound(furnaceLoc, Sound.BLOCK_FURNACE_FIRE_CRACKLE, 1.0f, 1.0f);
        w.playSound(furnaceLoc, Sound.BLOCK_BLASTFURNACE_FIRE_CRACKLE, 1.0f, 1.0f);
        w.playSound(furnaceLoc, Sound.BLOCK_FIRE_AMBIENT, 1.0f, 1.0f);
    }
}
