package goat.minecraft.minecraftnew.subsystems.forestry;

import goat.minecraft.minecraftnew.MinecraftNew;
import goat.minecraft.minecraftnew.other.additionalfunctionality.BlessingUtils;
import goat.minecraft.minecraftnew.other.beacon.Catalyst;
import goat.minecraft.minecraftnew.other.beacon.CatalystManager;
import goat.minecraft.minecraftnew.other.beacon.CatalystType;
import goat.minecraft.minecraftnew.subsystems.pets.PetManager;
import goat.minecraft.minecraftnew.subsystems.pets.PetTrait;
import goat.minecraft.minecraftnew.other.skilltree.Skill;
import goat.minecraft.minecraftnew.other.skilltree.SkillTreeManager;
import goat.minecraft.minecraftnew.other.skilltree.Talent;
import goat.minecraft.minecraftnew.utils.devtools.ItemRegistry;
import goat.minecraft.minecraftnew.utils.devtools.XPManager;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class Forestry implements Listener {

    // Singleton instance
    private static Forestry instance;

    private final MinecraftNew plugin;
    private final XPManager xpManager;
    private final Random random = new Random();

    // Notoriety map tracks each player's current forestry notoriety.
    private Map<UUID, Integer> notorietyMap = new HashMap<>();

    // Set of log materials that can grant forestry XP
    private static final Set<Material> LOG_MATERIALS = new HashSet<>(Arrays.asList(
            Material.OAK_LOG, Material.SPRUCE_LOG, Material.BIRCH_LOG,
            Material.JUNGLE_LOG, Material.ACACIA_LOG, Material.DARK_OAK_LOG,
            Material.MANGROVE_LOG, Material.CHERRY_LOG,
            Material.CRIMSON_STEM, Material.WARPED_STEM
    ));

    // Set of nether log materials that grant more XP
    private static final Set<Material> NETHER_LOG_MATERIALS = new HashSet<>(Arrays.asList(
            Material.CRIMSON_STEM, Material.WARPED_STEM
    ));

    /**
     * Private constructor to prevent instantiation from outside.
     * @param plugin The plugin instance.
     */
    private Forestry(MinecraftNew plugin) {
        this.plugin = plugin;
        this.xpManager = new XPManager(plugin);
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    /**
     * Gets the singleton instance of the Forestry class.
     * @param plugin The plugin instance (only needed on first call).
     * @return The singleton Forestry instance.
     */
    public static Forestry getInstance(MinecraftNew plugin) {
        if (instance == null) {
            instance = new Forestry(plugin);
        }
        return instance;
    }

    /**
     * Gets the singleton instance of the Forestry class.
     * @return The singleton Forestry instance.
     * @throws IllegalStateException if getInstance(plugin) hasn't been called first.
     */
    public static Forestry getInstance() {
        if (instance == null) {
            throw new IllegalStateException("Forestry has not been initialized yet. Call getInstance(plugin) first.");
        }
        return instance;
    }

    /**
     * Initializes Forestry functionality, including starting the notoriety decay task.
     * @param plugin The plugin instance.
     */
    public void init(MinecraftNew plugin) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        startNotorietyDecayTask();
        loadAllNotoriety();
    }

    /**
     * Increments a player's notoriety (capped at 700) and saves the new value.
     * If the notoriety crosses a tier threshold, the player is notified.
     * @param player The player whose notoriety is incremented.
     */
    public void incrementNotoriety(Player player, boolean fromForestry) {
        UUID uuid = player.getUniqueId();
        int currentNotoriety = notorietyMap.getOrDefault(uuid, 0);
        int currentTier = getNotorietyTier(currentNotoriety);
        int newNotoriety = Math.min(currentNotoriety + 1, 700);
        notorietyMap.put(uuid, newNotoriety);
        int newTier = getNotorietyTier(newNotoriety);
        if (newTier > currentTier) {
            notifyNotorietyMilestone(player, newTier, fromForestry);
        }
        saveNotoriety(player);
    }

    public void incrementNotoriety(Player player) {
        incrementNotoriety(player, true);
    }
    /**
     * Adds the specified notoriety amount.
     */
    public void addNotoriety(Player player, int amount) {
        addNotoriety(player, amount, true, true);
    }

    /**
     * Adds the specified notoriety amount.
     * @param player The player to modify notoriety for.
     * @param amount Amount to add.
     * @param notify Whether to notify the player about tier milestones.
     */
    public void addNotoriety(Player player, int amount, boolean notify) {
        addNotoriety(player, amount, notify, true);
    }

    public void addNotoriety(Player player, int amount, boolean notify, boolean fromForestry) {
        UUID uuid = player.getUniqueId();
        int current = notorietyMap.getOrDefault(uuid, 0);
        int currentTier = getNotorietyTier(current);
        int newValue = Math.min(current + amount, 700);
        notorietyMap.put(uuid, newValue);
        int newTier = getNotorietyTier(newValue);
        if (notify && newTier > currentTier) {
            notifyNotorietyMilestone(player, newTier, fromForestry);
        }
        saveNotoriety(player);
    }

    /**
     * Decreases notoriety, clamped to 0.
     */
    public void decreaseNotoriety(Player player, int amount) {
        decreaseNotoriety(player, amount, true);
    }

    /**
     * Decreases notoriety, clamped to 0.
     * @param player The player whose notoriety is changed.
     * @param amount Amount to subtract.
     * @param notify Whether to notify milestone changes.
     */
    public void decreaseNotoriety(Player player, int amount, boolean notify) {
        UUID uuid = player.getUniqueId();
        int current = notorietyMap.getOrDefault(uuid, 0);
        int currentTier = getNotorietyTier(current);
        int newValue = Math.max(current - amount, 0);
        notorietyMap.put(uuid, newValue);
        int newTier = getNotorietyTier(newValue);
        if (notify && newTier < currentTier) {
            // No explicit message for decreasing, but could be added if desired
        }
        saveNotoriety(player);
    }

    /**
     * Sets the player's notoriety to zero.
     */
    public void resetNotoriety(Player player) {
        notorietyMap.put(player.getUniqueId(), 0);
        saveNotoriety(player);
    }

    /**
     * Halves the player's notoriety value.
     */
    public void halveNotoriety(Player player) {
        UUID uuid = player.getUniqueId();
        int current = notorietyMap.getOrDefault(uuid, 0);
        notorietyMap.put(uuid, current / 2);
        saveNotoriety(player);
    }

    /**
     * Determines the notoriety tier for a given notoriety value.
     * Tiers are defined as:
     *  Tier 1: 0 - 64
     *  Tier 2: 65 - 192
     *  Tier 3: 193 - 384
     *  Tier 4: 385 - 640
     *  Tier 5: 641 - 700
     * @param notoriety The notoriety value.
     * @return The tier level (1-5).
     */
    private int getNotorietyTier(int notoriety) {
        if (notoriety <= 64) {
            return 1;
        } else if (notoriety <= 192) {
            return 2;
        } else if (notoriety <= 384) {
            return 3;
        } else if (notoriety <= 640) {
            return 4;
        } else {
            return 5;
        }
    }

    /**
     * Notifies the player with a sound and colored message when they cross a notoriety milestone.
     * The message and sound become more concerning as the tier increases.
     * @param player The player to notify.
     * @param tier The new notoriety tier.
     */
    private void notifyNotorietyMilestone(Player player, int tier, boolean fromForestry) {
        String message;
        ChatColor color;
        Sound sound;
        float pitch;
        switch (tier) {
            case 2:
                message = fromForestry ? "The forest seems uneasy..." : "You feel a bad presence...";
                color = ChatColor.YELLOW;
                sound = Sound.BLOCK_NOTE_BLOCK_PLING;
                pitch = 1.0f;
                break;
            case 3:
                message = fromForestry ? "The forest grows angry!" : "Dread settles over the area.";
                color = ChatColor.GOLD;
                sound = Sound.BLOCK_BELL_USE;
                pitch = 1.0f;
                break;
            case 4:
                message = fromForestry ? "The forest is enraged!" : "A dangerous aura surrounds you!";
                color = ChatColor.RED;
                sound = Sound.ENTITY_WITHER_SPAWN;
                pitch = 1.0f;
                break;
            case 5:
                message = fromForestry ? "The forest is in a murderous fury!" : "Pure malice closes in around you!";
                color = ChatColor.DARK_RED;
                sound = Sound.ENTITY_WITHER_DEATH;
                pitch = 1.0f;
                break;
            default:
                return;
        }
        player.sendMessage(color + message);
        player.playSound(player.getLocation(), sound, 1.0f, pitch);
    }

    /**
     * Starts the notoriety decay task that runs every 5 seconds.
     * For each online player, it loads the player's notoriety file, decreases the value (if above 0),
     * and saves the updated value.
     */
    private void startNotorietyDecayTask() {
        // Decay task disabled; notoriety is now adjusted via events.
    }

    /**
     * Saves the player's notoriety to a file.
     * @param player The player whose notoriety is saved.
     */
    public void saveNotoriety(Player player) {
        UUID uuid = player.getUniqueId();
        File notorietyFile = new File(plugin.getDataFolder(), "notoriety_" + uuid + ".yml");
        YamlConfiguration config = YamlConfiguration.loadConfiguration(notorietyFile);
        config.set("notoriety", notorietyMap.getOrDefault(uuid, 0));
        try {
            config.save(notorietyFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Loads the player's notoriety from file.
     * @param player The player whose notoriety is loaded.
     */
    public void loadNotoriety(Player player) {
        UUID uuid = player.getUniqueId();
        File notorietyFile = new File(plugin.getDataFolder(), "notoriety_" + uuid + ".yml");
        if (notorietyFile.exists()) {
            YamlConfiguration config = YamlConfiguration.loadConfiguration(notorietyFile);
            notorietyMap.put(uuid, config.getInt("notoriety"));
        } else {
            notorietyMap.put(uuid, 0);
        }
    }

    /**
     * Loads notoriety data for all currently online players.
     */
    public void loadAllNotoriety() {
        for (Player p : Bukkit.getOnlinePlayers()) {
            loadNotoriety(p);
        }
    }

    /**
     * Saves notoriety values for all players currently tracked in memory.
     */
    public void saveAllNotoriety() {
        for (UUID id : notorietyMap.keySet()) {
            Player p = Bukkit.getPlayer(id);
            if (p != null) {
                saveNotoriety(p);
            } else {
                File notorietyFile = new File(plugin.getDataFolder(), "notoriety_" + id + ".yml");
                YamlConfiguration config = YamlConfiguration.loadConfiguration(notorietyFile);
                config.set("notoriety", notorietyMap.get(id));
                try {
                    config.save(notorietyFile);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Returns the current notoriety for a given player.
     * @param player The player.
     * @return The notoriety value.
     */
    public int getNotoriety(Player player) {
        return notorietyMap.getOrDefault(player.getUniqueId(), 0);
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Block block = event.getBlock();
        if (LOG_MATERIALS.contains(block.getType())) {
            // Mark placed logs to prevent XP farming.
            block.setMetadata("placed", new FixedMetadataValue(plugin, true));
        }
    }

    private void grantHaste(Player player) {
        SkillTreeManager mgr = SkillTreeManager.getInstance();
        int chanceLevel = 0;
        chanceLevel += mgr.getTalentLevel(player.getUniqueId(), Skill.FORESTRY, Talent.LEVERAGE_I);
        chanceLevel += mgr.getTalentLevel(player.getUniqueId(), Skill.FORESTRY, Talent.LEVERAGE_II);
        chanceLevel += mgr.getTalentLevel(player.getUniqueId(), Skill.FORESTRY, Talent.LEVERAGE_III);
        chanceLevel += mgr.getTalentLevel(player.getUniqueId(), Skill.FORESTRY, Talent.LEVERAGE_IV);
        chanceLevel += mgr.getTalentLevel(player.getUniqueId(), Skill.FORESTRY, Talent.LEVERAGE_V);
        if (chanceLevel <= 0) return;
        int roll = random.nextInt(100) + 1;
        if (roll <= chanceLevel * 2) {
            int potency = mgr.getTalentLevel(player.getUniqueId(), Skill.FORESTRY, Talent.DEFORESTATION);
            potency = Math.min(potency, 4);
            int level = xpManager.getPlayerLevel(player, "Forestry");
            int duration = 100 + (level * 5);
            int frenzy = mgr.getTalentLevel(player.getUniqueId(), Skill.FORESTRY, Talent.FOREST_FRENZY);
            duration += frenzy * 200;
            player.addPotionEffect(new PotionEffect(PotionEffectType.HASTE, duration, potency), true);
            player.playSound(player.getLocation(), Sound.BLOCK_DEEPSLATE_STEP, 1.0f, 1.0f);
        }
    }

    public double calculateSpiritChance(Player player) {
        double spiritChance = 0.01;
        ItemStack axe = player.getInventory().getItemInMainHand();

        int effigyYield = 0;
        spiritChance += effigyYield * 0.001;

        ForestryPetManager forestryPetManager = MinecraftNew.getInstance().getForestryManager();

        PetManager petManager = PetManager.getInstance(plugin);
        PetManager.Pet activePet = petManager.getActivePet(player);
        if (activePet != null && activePet.hasPerk(PetManager.PetPerk.SKEPTICISM)) {
            spiritChance += 0.01;
        }
        if (activePet != null && activePet.hasPerk(PetManager.PetPerk.CHALLENGE)) {
            spiritChance += 0.02;
        }
        if (BlessingUtils.hasFullSetBonus(player, "Nature's Wrath")) {
            spiritChance += 0.04;
        }
        if (petManager.getActivePet(player).getTrait().equals(PetTrait.HAUNTED)) {
            spiritChance += (petManager.getActivePet(player).getTrait().getValueForRarity(
                    petManager.getActivePet(player).getTraitRarity()) / 100);
        }

        CatalystManager catalystManager = CatalystManager.getInstance();
        if (catalystManager != null && catalystManager.isNearCatalyst(player.getLocation(), CatalystType.INSANITY)) {
            final double BASE = 0.01;
            final double PER_TIER = 0.001;
            Catalyst nearest = catalystManager.findNearestCatalyst(player.getLocation(), CatalystType.INSANITY);
            int tier = catalystManager.getCatalystTier(nearest);
            spiritChance += BASE + (tier * PER_TIER);
        }

        SkillTreeManager mgr = SkillTreeManager.getInstance();
        int scTotal = 0;
        scTotal += mgr.getTalentLevel(player.getUniqueId(), Skill.FORESTRY, Talent.SPIRIT_CHANCE_I) * 2;
        scTotal += mgr.getTalentLevel(player.getUniqueId(), Skill.FORESTRY, Talent.SPIRIT_CHANCE_II) * 4;
        scTotal += mgr.getTalentLevel(player.getUniqueId(), Skill.FORESTRY, Talent.SPIRIT_CHANCE_III) * 6;
        scTotal += mgr.getTalentLevel(player.getUniqueId(), Skill.FORESTRY, Talent.SPIRIT_CHANCE_IV) * 8;
        scTotal += mgr.getTalentLevel(player.getUniqueId(), Skill.FORESTRY, Talent.SPIRIT_CHANCE_V) * 10;
        spiritChance += scTotal / 10000.0;

        return spiritChance;
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        Player player = event.getPlayer();
        Material blockType = block.getType();
        ForestryPetManager forestryPetManager = MinecraftNew.getInstance().getForestryManager();
        PetManager petManager = PetManager.getInstance(plugin);
        PetManager.Pet activePet = petManager.getActivePet(player);
        int forestryLevel = xpManager.getPlayerLevel(player, "Forestry");

        // Check if it's a log and not player-placed.
        if (LOG_MATERIALS.contains(blockType) && !block.hasMetadata("placed")) {
            ItemStack axe = player.getInventory().getItemInMainHand();

            int trespasser = 0;
            int fakeNews   = 0;

// cap at level 5
            fakeNews = Math.min(fakeNews, 5);

            int notorietyGain = 1 + (trespasser * 3);
            addNotoriety(player, notorietyGain, true, true);

            if (fakeNews > 0) {
                // roll 0–99; if less than (level * 10), reduce notoriety by 1
                if (random.nextInt(100) < fakeNews * 10) {
                    decreaseNotoriety(player, 1);
                }
            }


            forestryPetManager.incrementForestryCount(player);

            double spiritChance = calculateSpiritChance(player);

            grantHaste(player);

            // Rare drops.
            if (random.nextInt(2600) + 1 == 1) {
                Objects.requireNonNull(player.getLocation().getWorld())
                        .dropItem(player.getLocation(), ItemRegistry.getSecretsOfInfinity());
                player.sendMessage(ChatColor.YELLOW + "You received Secrets of Infinity!");
            }
            if (random.nextInt(2600) + 1 == 1) {
                Objects.requireNonNull(player.getLocation().getWorld())
                        .dropItem(player.getLocation(), ItemRegistry.getSilkWorm());
                player.sendMessage(ChatColor.YELLOW + "You received Silk Worm!");
            }

            // Award XP based on log type.
            int xpAmount = NETHER_LOG_MATERIALS.contains(blockType) ? 10 : 5;
            xpManager.addXP(player, "Forestry", xpAmount);
            int xpBoost = 0;
            if (xpBoost > 0) {
                xpManager.addXP(player, "Forestry", xpBoost * 5);
            }

            handleFeedEffect(player, axe);
            handlePayout(player, axe);

            handleYieldUpgrades(player, block, axe);

            // Process double drop chance.
            processDoubleDropChance(player, block);
            // Process perfect apple drop chance.
            SkillTreeManager mgrTalents = SkillTreeManager.getInstance();
            int orchard = EffigyUpgradeSystem.getUpgradeLevel(axe, EffigyUpgradeSystem.UpgradeType.ORCHARD);
            processPerfectAppleChance(player, block, forestryLevel, orchard);
            // Process honey bottle chance from 100 Acre Woods talent.
            processHoneyBottleChance(player, block);

            int goldenApple = EffigyUpgradeSystem.getUpgradeLevel(axe, EffigyUpgradeSystem.UpgradeType.GOLDEN_APPLE);
            processNotchAppleChance(player, block, goldenApple);

            int regrowth = 0;
            regrowth += mgrTalents.getTalentLevel(player.getUniqueId(), Skill.FORESTRY, Talent.REGROWTH_I);
            regrowth += mgrTalents.getTalentLevel(player.getUniqueId(), Skill.FORESTRY, Talent.REGROWTH_II);
            regrowth += mgrTalents.getTalentLevel(player.getUniqueId(), Skill.FORESTRY, Talent.REGROWTH_III);
            if (regrowth > 0) {
                SaplingManager sm = SaplingManager.getInstance();
                int current = sm.getCooldownSecondsRemaining();
                int reduction = regrowth * 86400;
                sm.setCooldownSecondsRemaining(Math.max(0, current - reduction));
            }

            // (Additional spirit spawning logic could be added here.)
            ForestSpiritManager forestSpiritManager = ForestSpiritManager.getInstance(plugin);
            forestSpiritManager.attemptSpiritSpawn(spiritChance, block.getLocation(), block, player);

        }
    }

    /**
     * Processes the chance for a double log drop.
     * @param player The player who broke the log.
     * @param block The log block that was broken.
     */
    public void processDoubleDropChance(Player player, Block block) {
        SkillTreeManager mgr = SkillTreeManager.getInstance();
        int levelDouble = mgr.getTalentLevel(player.getUniqueId(), Skill.FORESTRY, Talent.TIMBER_I);
        int levelTriple = mgr.getTalentLevel(player.getUniqueId(), Skill.FORESTRY, Talent.TIMBER_II);
        int levelQuad   = mgr.getTalentLevel(player.getUniqueId(), Skill.FORESTRY, Talent.TIMBER_III);

        int extra = 0;
        if (random.nextInt(100) < levelQuad * 20) {
            extra = 3;
        } else if (random.nextInt(100) < levelTriple * 20) {
            extra = 2;
        } else if (random.nextInt(100) < levelDouble * 20) {
            extra = 1;
        }

        CatalystManager catalystManager = CatalystManager.getInstance();
        if (extra < 3 && catalystManager != null && catalystManager.isNearCatalyst(player.getLocation(), CatalystType.PROSPERITY)) {
            Catalyst catalyst = catalystManager.findNearestCatalyst(player.getLocation(), CatalystType.PROSPERITY);
            if (catalyst != null) {
                int tier = catalystManager.getCatalystTier(catalyst);
                double chance = 0.40 + (tier * 0.10);
                chance = Math.min(chance, 1.0);
                if (random.nextDouble() < chance) {
                    extra = Math.max(extra, 2);
                }
            }
        }

        if (extra > 0) {
            final Location dropLocation = block.getLocation().add(0.5, 0.5, 0.5);
            final Material logType = block.getType();
            final int amount = extra;
            new BukkitRunnable() {
                @Override
                public void run() {
                    dropLocation.getWorld().dropItemNaturally(dropLocation, new ItemStack(logType, amount));
                    dropLocation.getWorld().playSound(dropLocation, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.3f, 1.0f);
                    dropLocation.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, dropLocation, 5, 0.3, 0.3, 0.3, 0);
                }
            }.runTaskLater(plugin, 1L);
        }
    }

    /**
     * Processes the chance for a perfect apple drop.
     * @param player The player who broke the log.
     * @param block The log block that was broken.
     * @param forestryLevel The player's forestry level.
     */
    public void processPerfectAppleChance(Player player, Block block, int forestryLevel, int orchardLevel) {
        double chance = forestryLevel * 0.01 + orchardLevel;
        if (random.nextDouble() * 100 < chance) {
            final Location dropLocation = block.getLocation().add(0.5, 0.5, 0.5);
            new BukkitRunnable() {
                @Override
                public void run() {
                    ItemStack perfectApple = ItemRegistry.getBrewingApple();
                    dropLocation.getWorld().dropItemNaturally(dropLocation, perfectApple);
                    dropLocation.getWorld().playSound(dropLocation, Sound.ENTITY_WOLF_AMBIENT, 0.5f, 1.0f);
                    dropLocation.getWorld().spawnParticle(Particle.HEART, dropLocation, 5, 0.3, 0.3, 0.3, 0);
                }
            }.runTaskLater(plugin, 1L);
        }
    }

    public void processNotchAppleChance(Player player, Block block, int goldenLevel) {
        if (goldenLevel <= 0) return;
        double chance = goldenLevel;
        if (random.nextDouble() * 100 < chance) {
            Location dropLocation = block.getLocation().add(0.5, 0.5, 0.5);
            dropLocation.getWorld().dropItemNaturally(dropLocation, new ItemStack(Material.ENCHANTED_GOLDEN_APPLE));
            dropLocation.getWorld().playSound(dropLocation, Sound.ENTITY_ITEM_PICKUP, 0.8f, 1.2f);
        }
    }

    public void processHoneyBottleChance(Player player, Block block) {
        SkillTreeManager mgr = SkillTreeManager.getInstance();
        int level = mgr.getTalentLevel(player.getUniqueId(), Skill.FORESTRY, Talent.ONE_HUNDRED_ACRE_WOODS);
        if (level <= 0) return;

        double chance = level * 1.0; // 1% per level
        if (random.nextDouble() * 100 < chance) {
            final Location dropLocation = block.getLocation().add(0.5, 0.5, 0.5);
            new BukkitRunnable() {
                @Override
                public void run() {
                    dropLocation.getWorld().dropItemNaturally(dropLocation, new ItemStack(Material.HONEY_BOTTLE));
                    dropLocation.getWorld().playSound(dropLocation, Sound.ITEM_BOTTLE_FILL, 0.8f, 1.0f);
                    dropLocation.getWorld().spawnParticle(Particle.FALLING_HONEY, dropLocation, 5, 0.3, 0.3, 0.3, 0.01);
                }
            }.runTaskLater(plugin, 1L);
        }
    }

    private void handleFeedEffect(Player player, ItemStack axe) {
        // perk functionality removed
    }

    private void handlePayout(Player player, ItemStack axe) {
        // perk functionality removed
    }

    private boolean removeLogStack(Player player) {
        ItemStack[] contents = player.getInventory().getContents();
        for (int i = 0; i < contents.length; i++) {
            ItemStack item = contents[i];
            if (item != null && LOG_MATERIALS.contains(item.getType()) && item.getAmount() >= 64) {
                if (item.getAmount() == 64) {
                    player.getInventory().setItem(i, null);
                } else {
                    item.setAmount(item.getAmount() - 64);
                }
                return true;
            }
        }
        return false;
    }

    private void handleYieldUpgrades(Player player, Block block, ItemStack axe) {
        // perk functionality removed
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        resetNotoriety(player);
    }

    @EventHandler
    public void onPlayerBedEnter(PlayerBedEnterEvent event) {
        Player player = event.getPlayer();
        halveNotoriety(player);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        loadNotoriety(event.getPlayer());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        saveNotoriety(event.getPlayer());
    }
}
