package goat.minecraft.minecraftnew.subsystems.culinary;

import goat.minecraft.minecraftnew.utils.XPManager;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.*;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.*;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.EulerAngle;

import java.util.*;
import java.util.logging.Logger;

/**
 * CulinarySubsystem
 *
 * Updated as per request:
 * - No persistence through reloads or restarts.
 * - On server restart or reload, any active sessions have their ingredients dropped as items on the table.
 */
public class CulinarySubsystem implements Listener {
    private JavaPlugin plugin;
    private Logger logger;
    private static CulinarySubsystem instance;
    // Active recipe sessions keyed by the crafting table location
    private Map<Location, RecipeSession> activeRecipeSessions = new HashMap<>();
    public List<ItemStack> getAllRecipeItems() {
        List<ItemStack> recipeItems = new ArrayList<>();

        // Iterate through each recipe in the recipe registry
        for (CulinaryRecipe recipe : recipeRegistry) {
            // Create the recipe item for the current recipe
            ItemStack recipeItem = createRecipeItem(recipe);

            // Add the recipe item to the list
            recipeItems.add(recipeItem);
        }

        return recipeItems;
    }
    public static CulinarySubsystem getInstance(JavaPlugin plugin) {
        if (instance == null) {
            instance = new CulinarySubsystem(plugin);
        }
        return instance;
    }
    // Recipe registry
    public static List<CulinaryRecipe> recipeRegistry = new ArrayList<>();

    static {
        // Example recipes
        recipeRegistry.add(new CulinaryRecipe(
                Material.PAPER,
                Material.COOKED_BEEF,
                "Salted Steak",
                Arrays.asList("Cooked Beef", "Sea Salt"),
                100
        ));
        recipeRegistry.add(new CulinaryRecipe(
                Material.PAPER,
                Material.COOKED_CHICKEN,
                "Chicken Tenders",
                Arrays.asList("Cooked Chicken", "Bread"),
                100
        ));
        recipeRegistry.add(new CulinaryRecipe(
                Material.PAPER,
                Material.YELLOW_DYE,
                "Slice of Cheese",
                Arrays.asList("Milk"),
                100
        ));
        recipeRegistry.add(new CulinaryRecipe(
                Material.PAPER,
                Material.BREAD,
                "Ham and Cheese Sandwich",
                Arrays.asList("Slice of Cheese", "Cooked Porkchop", "Bread"),
                200
        ));
        recipeRegistry.add(new CulinaryRecipe(
                Material.PAPER,
                Material.BREAD,
                "Toast",
                Arrays.asList("Bread", "Butter"),
                200
        ));



        recipeRegistry.add(new CulinaryRecipe(
                Material.PAPER,
                Material.PUMPKIN_PIE,
                "Sweet Feast",
                Arrays.asList("Sugar", "Pumpkin", "Egg", "Wheat"),
                1000
        ));
        recipeRegistry.add(new CulinaryRecipe(
                Material.PAPER,
                Material.CARROT,
                "Vegetarian Feast",
                Arrays.asList("Carrot", "Potato", "Golden Carrot", "Beetroot"),
                1000
        ));
        recipeRegistry.add(new CulinaryRecipe(
                Material.PAPER,
                Material.COOKED_RABBIT,
                "Meatlovers Feast",
                Arrays.asList("Cooked Steak", "Cooked Chicken", "Butter", "Sea Salt", "Cooked Mutton", "Cooked Rabbit", "Cooked Porkchop"),
                1000
        ));
        recipeRegistry.add(new CulinaryRecipe(
                Material.PAPER,
                Material.DRIED_KELP,
                "Seafood Feast",
                Arrays.asList("Dried Kelp Block", "Cod", "Salmon", "Tropical Fish", "Calamari", "Heart of the Sea"),
                1000
        ));
        recipeRegistry.add(new CulinaryRecipe(
                Material.PAPER,
                Material.COOKED_SALMON,
                "Grilled Salmon",
                Arrays.asList("Cooked Salmon", "Sea Salt"),
                150
        ));
        recipeRegistry.add(new CulinaryRecipe(
                Material.PAPER,
                Material.MUSHROOM_STEW,
                "Mushroom Soup",
                Arrays.asList("Red Mushroom", "Brown Mushroom", "Sea Salt"),
                250
        ));
        recipeRegistry.add(new CulinaryRecipe(
                Material.PAPER,
                Material.BAKED_POTATO,
                "Loaded Baked Potato",
                Arrays.asList("Baked Potato", "Butter", "Slice of Cheese", "Cooked Porkchop"),
                300
        ));
    }

    private CulinarySubsystem(JavaPlugin plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();

        Bukkit.getLogger().info("[CulinarySubsystem] Registering events...");
        Bukkit.getPluginManager().registerEvents(this, plugin);
        Bukkit.getLogger().info("[CulinarySubsystem] Events registered.");
    }

    /**
     * Call this method from your main plugin's onDisable() to drop items
     * and remove all sessions.
     */
    public void finalizeAllSessionsOnShutdown() {
        for (RecipeSession session : activeRecipeSessions.values()) {
            // Drop the recipe paper again
            if (session.tableLocation != null && session.tableLocation.getWorld() != null) {
                ItemStack recipePaper = createRecipeItem(session.recipe);
                session.tableLocation.getWorld().dropItemNaturally(
                        session.tableLocation.clone().add(0.5, 1, 0.5), recipePaper);
            }

            // Drop all placed ingredients as items
            for (Map.Entry<String, UUID> entry : session.placedIngredientsStands.entrySet()) {
                UUID standUUID = entry.getValue();

                if (standUUID == null) {
                    logger.warning("[CulinarySubsystem] Null UUID in placedIngredientsStands. Skipping.");
                    continue;
                }

                Entity e = Bukkit.getEntity(standUUID);
                if (e instanceof ArmorStand) {
                    ArmorStand ingStand = (ArmorStand) e;
                    ItemStack inHand = ingStand.getEquipment().getItemInMainHand();
                    if (inHand != null && inHand.getType() != Material.AIR) {
                        session.tableLocation.getWorld().dropItemNaturally(
                                session.tableLocation.clone().add(0.5, 1, 0.5), inHand.clone());
                    }
                }
                removeEntityByUUID(standUUID);
            }

            // Cancel spin tasks
            for (BukkitTask task : session.ingredientSpinTasks.values()) {
                if (task != null) {
                    task.cancel();
                }
            }

            // Remove main stand
            if (session.mainArmorStandUUID != null) {
                removeEntityByUUID(session.mainArmorStandUUID);
            } else {
                logger.warning("[CulinarySubsystem] Main armor stand UUID is null for a session.");
            }

            // Remove label stands
            for (UUID u : session.ingredientLabelStands.values()) {
                if (u != null) {
                    removeEntityByUUID(u);
                } else {
                    logger.warning("[CulinarySubsystem] Null UUID found in ingredientLabelStands.");
                }
            }
        }

        // Clear all sessions
        activeRecipeSessions.clear();
        logger.info("[CulinarySubsystem] All sessions finalized and cleared on shutdown/reload.");
    }


    public List<ItemStack> getAllRecipePapers() {
        List<ItemStack> recipePapers = new ArrayList<>();

        // Iterate through each recipe in the recipe registry
        for (CulinaryRecipe recipe : recipeRegistry) {
            // Create the recipe item for the current recipe
            ItemStack recipePaper = createRecipeItem(recipe);

            // Add the recipe item to the list
            recipePapers.add(recipePaper);
        }

        return recipePapers;
    }


    @EventHandler
    public void onEat(PlayerItemConsumeEvent event) {
        Player player = event.getPlayer();
        ItemStack consumedItem = event.getItem();
        if (!consumedItem.hasItemMeta()) {
            return;
        }

        ItemMeta meta = consumedItem.getItemMeta();
        if (meta == null) {
            return;
        }

        String displayName = meta.getDisplayName();
        if (displayName == null || displayName.isEmpty()) {
            return;
        }
        displayName = ChatColor.stripColor(displayName).trim();

        List<String> lore = meta.getLore();
        if (lore == null) {
            lore = List.of();
        }

        if (consumedItem.getItemMeta().getDisplayName().contains("Raw")) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, 100, 0));
            player.sendMessage(ChatColor.GREEN + "You ate raw food! What is wrong with you!");
        }
        if (consumedItem.getItemMeta().getDisplayName().contains("Burnt")) {
            player.setFireTicks(60);
            player.sendMessage(ChatColor.GREEN + "You're cooked.");
        }

        boolean hasSeasoning = false;
        for (String line : lore) {
            if (ChatColor.stripColor(line).toLowerCase().contains("sea salt")) {
                hasSeasoning = true;
                break;
            }
        }

        if (hasSeasoning) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 100, 0));
            player.sendMessage(ChatColor.GREEN + "You feel a surge of health!");
        }

        XPManager xpManager = new XPManager(plugin);

        if(displayName.contains("Culinary")){
            xpManager.addXP(player, "Culinary", 125);
        }
        switch (displayName) {
            case "Salted Steak (Culinary)":
                player.addPotionEffect(new PotionEffect(PotionEffectType.SATURATION, 20*2, 0));
                break;
            case "Chicken Tenders (Culinary)":
                player.addPotionEffect(new PotionEffect(PotionEffectType.SATURATION, 20*2, 0));
                break;
            case "Ham and Cheese Sandwich (Culinary)":
                player.addPotionEffect(new PotionEffect(PotionEffectType.SATURATION, 20*3, 0));
                break;
            case "Toast (Culinary)":
                player.addPotionEffect(new PotionEffect(PotionEffectType.SATURATION, 20*1, 0));
                break;
            case "Grilled Salmon (Culinary)":
                player.addPotionEffect(new PotionEffect(PotionEffectType.SATURATION, 20*1, 0));
                break;
            case "Mushroom Soup (Culinary)":
                player.addPotionEffect(new PotionEffect(PotionEffectType.SATURATION, 20*1, 0));
                break;
            case "Loaded Baked Potato (Culinary)":
                player.addPotionEffect(new PotionEffect(PotionEffectType.SATURATION, 20*11, 0));
                break;


            case "Sweet Feast (Culinary)":
                player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 20*60*60*1, 0));
                player.addPotionEffect(new PotionEffect(PotionEffectType.SATURATION, 20*10, 0));
                player.setFoodLevel(20);
                player.setSaturation(player.getSaturation() + 100);
                player.setHealth(player.getMaxHealth());
                break;
            case "Vegetarian Feast (Culinary)":
                player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 20*60*60*1, 0));
                player.addPotionEffect(new PotionEffect(PotionEffectType.SATURATION, 20*10, 0));
                player.setFoodLevel(20);
                player.setSaturation(player.getSaturation() + 100);
                player.setHealth(player.getMaxHealth());
                break;
            case "Meatlovers Feast (Culinary)":
                player.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 20*60*60*1, 0));
                player.addPotionEffect(new PotionEffect(PotionEffectType.SATURATION, 20*10, 0));
                player.setFoodLevel(20);
                player.setSaturation(player.getSaturation() + 100);
                player.setHealth(player.getMaxHealth());
                break;
            case "Seafood Feast (Culinary)":
                player.addPotionEffect(new PotionEffect(PotionEffectType.WATER_BREATHING, 20*60*60*1, 0));
                player.addPotionEffect(new PotionEffect(PotionEffectType.SATURATION, 20*10, 0));
                player.setFoodLevel(20);
                player.setSaturation(player.getSaturation() + 100);
                player.setHealth(player.getMaxHealth());
                break;
            default:
                break;
        }
        clampPlayerStats(player);
    }

    private void clampPlayerStats(Player player) {
        if (player.getFoodLevel() > 20) {
            player.setFoodLevel(20);
        } else if (player.getFoodLevel() < 0) {
            player.setFoodLevel(0);
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getClickedBlock() == null) return;
        if (event.getClickedBlock().getType() != Material.CRAFTING_TABLE) return;

        Player player = event.getPlayer();
        ItemStack hand = player.getInventory().getItemInMainHand().clone();
        Location tableLoc = event.getClickedBlock().getLocation().clone();

        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if(player.getInventory().getItemInMainHand().getType().equals(Material.PAPER)) {
                if (activeRecipeSessions.containsKey(tableLoc)) {
                    event.setCancelled(true);
                    player.sendMessage(ChatColor.RED + "This crafting table is already being used for another recipe!");
                    return;
                }
            }
            if (isRecipeItem(hand)) {
                CulinaryRecipe recipe = parseRecipeFromItem(hand);
                if (recipe == null) {
                    event.setCancelled(true);
                    player.sendMessage(ChatColor.RED + "This recipe is invalid or not recognized.");
                    logger.warning("[CulinarySubsystem] Player " + player.getName() + " attempted to use an unregistered recipe item.");
                    return;
                }

                event.setCancelled(true);
                logger.info("[CulinarySubsystem] Displaying recipe " + recipe.getName() + " at " + tableLoc);

                RecipeSession session = new RecipeSession(recipe, tableLoc);
                activeRecipeSessions.put(tableLoc, session);

                consumeItem(player, hand, 1);

                Location mainLoc = tableLoc.clone().add(0.5, 0.7, 0.5);
                UUID mainStand = spawnInvisibleArmorStand(mainLoc, ChatColor.GOLD + recipe.getName(), Arrays.asList(ChatColor.YELLOW + "Ingredients:"), true);
                session.mainArmorStandUUID = mainStand;

                double offsetY = -0.25;
                for (String ing : recipe.getIngredients()) {
                    offsetY -= 0.3;
                    Location ingLoc = mainLoc.clone().add(0, offsetY, 0);
                    UUID ingStand = spawnInvisibleArmorStand(ingLoc, ChatColor.GRAY + ing, null, false);
                    session.ingredientLabelStands.put(ing, ingStand);
                }

                player.sendMessage(ChatColor.GREEN + "Recipe " + recipe.getName() + " displayed! Right-click with ingredients to place them, left-click to finalize.");
                return;
            }

            if (activeRecipeSessions.containsKey(tableLoc)) {
                RecipeSession session = activeRecipeSessions.get(tableLoc);
                CulinaryRecipe recipe = session.recipe;
                String ingredientName = matchIngredient(hand, recipe.getIngredients(), session.placedIngredientsStands.keySet());
                if (ingredientName != null) {
                    event.setCancelled(true);
                    logger.info("[CulinarySubsystem] Player " + player.getName() + " placing ingredient " + ingredientName);

                    consumeItem(player, hand, 1);

                    UUID standUUID = spawnIngredientAboveTableRandom(tableLoc, hand.getType(), hand);
                    session.placedIngredientsStands.put(ingredientName, standUUID);

                    BukkitTask spinTask = startSpinning(standUUID);
                    session.ingredientSpinTasks.put(ingredientName, spinTask);

                    player.sendMessage(ChatColor.GREEN + ingredientName + " placed.");
                    return;
                } else {
                    player.sendMessage(ChatColor.RED + "This item is not required or already placed.");
                    logger.info("[CulinarySubsystem] Irrelevant item or all ingredients placed.");
                }
            }
        } else if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
            if (activeRecipeSessions.containsKey(tableLoc)) {
                RecipeSession session = activeRecipeSessions.get(tableLoc);
                if (session.placedIngredientsStands.size() == session.recipe.getIngredients().size()) {
                    event.setCancelled(true);
                    logger.info("[CulinarySubsystem] Finalizing recipe " + session.recipe.getName());
                    finalizeRecipe(session, player);
                    player.getWorld().spawnParticle(Particle.SMOKE_LARGE, player.getLocation(), 50, 0.5, 0.5, 0.5, 0.1);
                    player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_WORK_FLETCHER, 1.0f, 1.0f);
                } else {
                    event.setCancelled(true);
                    player.sendMessage(ChatColor.RED + "Not all ingredients are placed yet!");
                    logger.info("[CulinarySubsystem] Not all ingredients placed for " + session.recipe.getName());
                }
            } else {
                logger.info("[CulinarySubsystem] No active recipe session for table at " + tableLoc);
            }
        }
    }

    public static ItemStack createRecipeItem(CulinaryRecipe recipe) {
        ItemStack item = new ItemStack(recipe.getRecipeItem(), 1);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.GOLD + recipe.getName() + " Recipe");
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.YELLOW + "Ingredients:");
        for (String ing : recipe.getIngredients()) {
            lore.add(ChatColor.GRAY + "- " + ing);
        }
        lore.add(ChatColor.DARK_PURPLE + "Culinary Recipe");
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createOutputItem(CulinaryRecipe recipe) {
        ItemStack item = new ItemStack(recipe.getOutputMaterial());
        ItemMeta meta = item.getItemMeta();
        assert meta != null;
        if(recipe.getName().equals("Slice of Cheese")){
            meta.setDisplayName(ChatColor.GOLD + recipe.getName());
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.YELLOW + "Crafted with:");
            for (String ing : recipe.getIngredients()) {
                lore.add(ChatColor.GRAY + "- " + ing);
            }
            lore.add(ChatColor.DARK_PURPLE + "Culinary Ingredient");
            meta.setLore(lore);
            item.setItemMeta(meta);

            return item;
        }
        meta.setDisplayName(ChatColor.GOLD + recipe.getName() + " (Culinary)");
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.YELLOW + "Crafted with:");
        for (String ing : recipe.getIngredients()) {
            lore.add(ChatColor.GRAY + "- " + ing);
        }
        lore.add(ChatColor.DARK_PURPLE + "Culinary Delight");
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    private boolean isRecipeItem(ItemStack item) {
        if (item == null || !item.hasItemMeta() || !item.getItemMeta().hasLore()) return false;
        for (String line : item.getItemMeta().getLore()) {
            if (ChatColor.stripColor(line).equalsIgnoreCase("Ingredients:")) {
                return true;
            }
        }
        return false;
    }

    private CulinaryRecipe parseRecipeFromItem(ItemStack item) {
        logger.info("[CulinarySubsystem] parseRecipeFromItem: Parsing from item.");
        String recipeName = ChatColor.stripColor(item.getItemMeta().getDisplayName()).replace(" Recipe", "");
        CulinaryRecipe found = getRecipeByName(recipeName);
        if (found != null) {
            logger.info("[CulinarySubsystem] Found recipe '" + recipeName + "' in registry.");
            return found;
        }

        logger.warning("[CulinarySubsystem] Recipe '" + recipeName + "' not found in registry.");
        return null;
    }

    private CulinaryRecipe getRecipeByName(String name) {
        for (CulinaryRecipe r : recipeRegistry) {
            if (r.getName().equalsIgnoreCase(name)) return r;
        }
        return null;
    }

    private String matchIngredient(ItemStack item, List<String> neededIngredients, Set<String> alreadyPlaced) {
        if (item == null || item.getType() == Material.AIR) return null;
        String handName = (item.hasItemMeta() && item.getItemMeta().hasDisplayName()) ?
                ChatColor.stripColor(item.getItemMeta().getDisplayName()).toLowerCase() :
                item.getType().toString().toLowerCase().replace("_", " ");

        for (String ing : neededIngredients) {
            if (alreadyPlaced.contains(ing)) continue;
            String ingLower = ing.toLowerCase();
            if (handName.contains(ingLower) || handName.equals(ingLower)) {
                return ing;
            }
        }
        return null;
    }

    private void consumeItem(Player player, ItemStack item, int amount) {
        player.getInventory().getItemInMainHand().setAmount(player.getInventory().getItemInMainHand().getAmount() - 1);
    }

    private void finalizeRecipe(RecipeSession session, Player player) {
        logger.info("[CulinarySubsystem] finalizeRecipe: " + session.recipe.getName() + " at " + session.tableLocation);

        // Remove placed ingredients
        for (Map.Entry<String, UUID> entry : session.placedIngredientsStands.entrySet()) {
            UUID standUUID = entry.getValue();
            removeEntityByUUID(standUUID);
        }

        // Cancel spin tasks
        for (BukkitTask task : session.ingredientSpinTasks.values()) {
            task.cancel();
        }

        // Remove stands
        removeEntityByUUID(session.mainArmorStandUUID);
        for (UUID u : session.ingredientLabelStands.values()) {
            removeEntityByUUID(u);
        }

        // Drop final output
        ItemStack result = createOutputItem(session.recipe);
        session.tableLocation.getWorld().dropItemNaturally(session.tableLocation.clone().add(0.5, 1, 0.5), result);
        XPManager xpManager = new XPManager(plugin);
        xpManager.addXP(player, "Culinary", session.recipe.getXpReward());
        player.sendMessage(ChatColor.GREEN + "You crafted " + session.recipe.getName() + "! You gained culinary XP.");
        logger.info("[CulinarySubsystem] finalizeRecipe: Recipe crafted, XP granted.");

        activeRecipeSessions.remove(session.tableLocation);
    }

    private UUID spawnInvisibleArmorStand(Location loc, String displayName, List<String> lore, boolean marker) {
        logger.info("[CulinarySubsystem] spawnInvisibleArmorStand: Spawning stand at " + loc + ", Name=" + displayName);
        ArmorStand stand = (ArmorStand) loc.getWorld().spawnEntity(loc, EntityType.ARMOR_STAND);
        stand.setInvisible(true);
        stand.setCustomNameVisible(true);
        stand.setCustomName(displayName);
        stand.setGravity(false);
        stand.setMarker(marker);
        stand.setInvulnerable(true);
        return stand.getUniqueId();
    }

    private UUID spawnIngredientAboveTableRandom(Location tableLoc, Material mat, ItemStack ingredient) {
        if (ingredient == null || ingredient.getType() == Material.AIR) {
            logger.warning("[CulinarySubsystem] spawnIngredientAboveTableRandom: Invalid ingredient. Material: " + mat);
            return null;
        }
        double offsetX = (Math.random() - 0.5) * 0.6;
        double offsetZ = (Math.random() - 0.5) * 0.6;
        Location itemLoc = tableLoc.clone().add(0.5 + offsetX, 0.5, 0.5 + offsetZ);
        logger.info("[CulinarySubsystem] spawnIngredientAboveTableRandom: Spawning ingredient stand for " + mat + " at " + itemLoc);
        ArmorStand stand = (ArmorStand) itemLoc.getWorld().spawnEntity(itemLoc, EntityType.ARMOR_STAND);

        stand.setInvisible(true);
        stand.setMarker(true);
        stand.setInvulnerable(true);
        stand.setGravity(false);
        stand.setSmall(true);
        stand.setArms(true);
        ItemStack ingredientCopy = ingredient.clone();
        ingredientCopy.setAmount(1);

        stand.getEquipment().setItemInMainHand(ingredientCopy);
        stand.setCustomNameVisible(false);
        stand.setRightArmPose(new EulerAngle(Math.toRadians(-90), 0, 0));

        UUID uuid = stand.getUniqueId();
        Bukkit.getLogger().info("[CulinarySubsystem] spawnIngredientAboveTableRandom: Spawned item stand UUID=" + uuid);
        return uuid;
    }

    private BukkitTask startSpinning(UUID standUUID) {
        logger.info("[CulinarySubsystem] startSpinning: Starting spin task for stand " + standUUID);
        return new BukkitRunnable() {
            double angle = 0.0;

            @Override
            public void run() {
                Entity e = Bukkit.getEntity(standUUID);
                if (e == null || !(e instanceof ArmorStand) || !e.isValid()) {
                    cancel();
                    logger.warning("[CulinarySubsystem] Spinning task cancelled: Stand " + standUUID + " not found or invalid.");
                    return;
                }
                ArmorStand stand = (ArmorStand) e;
                angle += 5.0;
                if (angle > 360.0) angle -= 360.0;
                Location loc = stand.getLocation();
                loc.setYaw((float) angle);
                stand.teleport(loc);
            }
        }.runTaskTimer(plugin, 1, 1);
    }

    private void removeEntityByUUID(UUID uuid) {
        if (uuid == null) return;
        logger.info("[CulinarySubsystem] removeEntityByUUID: Removing entity " + uuid);
        Entity e = Bukkit.getEntity(uuid);
        if (e != null) {
            e.remove();
            logger.info("[CulinarySubsystem] removeEntityByUUID: Entity " + uuid + " removed.");
        } else {
            logger.warning("[CulinarySubsystem] removeEntityByUUID: Entity " + uuid + " not found.");
        }
    }

    // Placeholder Classes

    public static class CulinaryRecipe {
        private final Material recipeItem;
        private final Material outputMaterial;
        private final String name;
        private final List<String> ingredients;
        private final int xpReward;

        public CulinaryRecipe(Material recipeItem, Material outputMaterial, String name, List<String> ingredients, int xpReward) {
            this.recipeItem = recipeItem;
            this.outputMaterial = outputMaterial;
            this.name = name;
            this.ingredients = ingredients;
            this.xpReward = xpReward;
        }

        public Material getRecipeItem() { return recipeItem; }
        public Material getOutputMaterial() { return outputMaterial; }
        public String getName() { return name; }
        public List<String> getIngredients() { return ingredients; }
        public int getXpReward() { return xpReward; }
    }

    public static class RecipeSession {
        public CulinaryRecipe recipe;
        public Location tableLocation;

        public UUID mainArmorStandUUID;
        public Map<String, UUID> ingredientLabelStands = new HashMap<>();
        public Map<String, UUID> placedIngredientsStands = new HashMap<>();
        public Map<String, BukkitTask> ingredientSpinTasks = new HashMap<>();

        public RecipeSession(CulinaryRecipe recipe, Location tableLocation) {
            this.recipe = recipe;
            this.tableLocation = tableLocation.clone();
        }
    }
}
