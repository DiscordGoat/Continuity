package goat.minecraft.minecraftnew.subsystems.combat;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import goat.minecraft.minecraftnew.MinecraftNew;
import goat.minecraft.minecraftnew.subsystems.mining.PlayerOxygenManager;
import goat.minecraft.minecraftnew.utils.devtools.XPManager;
import goat.minecraft.minecraftnew.utils.devtools.ItemRegistry;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.block.Biome;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.lang.reflect.Field;
import java.util.*;

public class SpawnMonsters implements Listener {

    private static SpawnMonsters instance; // <-- Singleton instance

    private XPManager xpManager;
    private final JavaPlugin plugin = MinecraftNew.getInstance();

    private static final int MAX_MONSTER_LEVEL = 300;

    // Mapping from biomes to leather armor colors
    private static final Map<Biome, org.bukkit.Color> BIOME_COLOR_MAP = new HashMap<>();
    static {
        // Overworld
        BIOME_COLOR_MAP.put(Biome.OCEAN, org.bukkit.Color.fromRGB(28, 107, 160));
        BIOME_COLOR_MAP.put(Biome.DEEP_OCEAN, org.bukkit.Color.fromRGB(24, 90, 140));
        BIOME_COLOR_MAP.put(Biome.FROZEN_OCEAN, org.bukkit.Color.fromRGB(180, 210, 230));
        BIOME_COLOR_MAP.put(Biome.RIVER, org.bukkit.Color.fromRGB(30, 144, 255));
        BIOME_COLOR_MAP.put(Biome.FROZEN_RIVER, org.bukkit.Color.fromRGB(173, 216, 230));
        BIOME_COLOR_MAP.put(Biome.BEACH, org.bukkit.Color.fromRGB(250, 250, 210));
        BIOME_COLOR_MAP.put(Biome.PLAINS, org.bukkit.Color.fromRGB(141, 182, 0));
        BIOME_COLOR_MAP.put(Biome.SUNFLOWER_PLAINS, org.bukkit.Color.fromRGB(150, 190, 0));
        BIOME_COLOR_MAP.put(Biome.SNOWY_PLAINS, org.bukkit.Color.fromRGB(240, 248, 255));
        BIOME_COLOR_MAP.put(Biome.DESERT, org.bukkit.Color.fromRGB(210, 180, 140));
        BIOME_COLOR_MAP.put(Biome.BADLANDS, org.bukkit.Color.fromRGB(189, 154, 122));
        BIOME_COLOR_MAP.put(Biome.WOODED_BADLANDS, org.bukkit.Color.fromRGB(178, 131, 100));
        BIOME_COLOR_MAP.put(Biome.ERODED_BADLANDS, org.bukkit.Color.fromRGB(160, 120, 90));
        BIOME_COLOR_MAP.put(Biome.FOREST, org.bukkit.Color.fromRGB(34, 139, 34));
        BIOME_COLOR_MAP.put(Biome.FLOWER_FOREST, org.bukkit.Color.fromRGB(60, 179, 113));
        BIOME_COLOR_MAP.put(Biome.BIRCH_FOREST, org.bukkit.Color.fromRGB(107, 142, 35));
        BIOME_COLOR_MAP.put(Biome.OLD_GROWTH_BIRCH_FOREST, org.bukkit.Color.fromRGB(85, 107, 47));
        BIOME_COLOR_MAP.put(Biome.DARK_FOREST, org.bukkit.Color.fromRGB(0, 100, 0));
        BIOME_COLOR_MAP.put(Biome.JUNGLE, org.bukkit.Color.fromRGB(34, 139, 34));
        BIOME_COLOR_MAP.put(Biome.SPARSE_JUNGLE, org.bukkit.Color.fromRGB(46, 139, 87));
        BIOME_COLOR_MAP.put(Biome.BAMBOO_JUNGLE, org.bukkit.Color.fromRGB(107, 142, 35));
        BIOME_COLOR_MAP.put(Biome.TAIGA, org.bukkit.Color.fromRGB(107, 142, 35));
        BIOME_COLOR_MAP.put(Biome.OLD_GROWTH_PINE_TAIGA, org.bukkit.Color.fromRGB(69, 139, 116));
        BIOME_COLOR_MAP.put(Biome.SWAMP, org.bukkit.Color.fromRGB(63, 149, 63));
        BIOME_COLOR_MAP.put(Biome.MUSHROOM_FIELDS, org.bukkit.Color.fromRGB(255, 0, 255));
        // Mountains
        BIOME_COLOR_MAP.put(Biome.WINDSWEPT_HILLS, org.bukkit.Color.fromRGB(119, 136, 153));
        BIOME_COLOR_MAP.put(Biome.WINDSWEPT_GRAVELLY_HILLS, org.bukkit.Color.fromRGB(112, 128, 144));
        BIOME_COLOR_MAP.put(Biome.WINDSWEPT_FOREST, org.bukkit.Color.fromRGB(95, 158, 160));
        BIOME_COLOR_MAP.put(Biome.FROZEN_PEAKS, org.bukkit.Color.fromRGB(176, 196, 222));
        BIOME_COLOR_MAP.put(Biome.SNOWY_SLOPES, org.bukkit.Color.fromRGB(240, 248, 255));
        BIOME_COLOR_MAP.put(Biome.JAGGED_PEAKS, org.bukkit.Color.fromRGB(169, 169, 169));
        BIOME_COLOR_MAP.put(Biome.STONY_PEAKS, org.bukkit.Color.fromRGB(112, 128, 144));
        // Caves
        BIOME_COLOR_MAP.put(Biome.DRIPSTONE_CAVES, org.bukkit.Color.fromRGB(102, 102, 102));
        BIOME_COLOR_MAP.put(Biome.LUSH_CAVES, org.bukkit.Color.fromRGB(0, 153, 76));
        BIOME_COLOR_MAP.put(Biome.DEEP_DARK, org.bukkit.Color.fromRGB(20, 20, 20));
        // New biomes (1.20)
        BIOME_COLOR_MAP.put(Biome.CHERRY_GROVE, org.bukkit.Color.fromRGB(255, 192, 203));
        BIOME_COLOR_MAP.put(Biome.MEADOW, org.bukkit.Color.fromRGB(124, 252, 0));
        BIOME_COLOR_MAP.put(Biome.GROVE, org.bukkit.Color.fromRGB(85, 107, 47));
        // Nether
        BIOME_COLOR_MAP.put(Biome.NETHER_WASTES, org.bukkit.Color.fromRGB(85, 0, 0));
        BIOME_COLOR_MAP.put(Biome.SOUL_SAND_VALLEY, org.bukkit.Color.fromRGB(119, 85, 61));
        BIOME_COLOR_MAP.put(Biome.CRIMSON_FOREST, org.bukkit.Color.fromRGB(128, 0, 0));
        BIOME_COLOR_MAP.put(Biome.WARPED_FOREST, org.bukkit.Color.fromRGB(15, 128, 128));
        BIOME_COLOR_MAP.put(Biome.BASALT_DELTAS, org.bukkit.Color.fromRGB(70, 70, 70));
        // The End
        BIOME_COLOR_MAP.put(Biome.THE_END, org.bukkit.Color.fromRGB(160, 160, 160));
        BIOME_COLOR_MAP.put(Biome.END_HIGHLANDS, org.bukkit.Color.fromRGB(200, 200, 200));
        BIOME_COLOR_MAP.put(Biome.END_MIDLANDS, org.bukkit.Color.fromRGB(150, 150, 150));
        BIOME_COLOR_MAP.put(Biome.SMALL_END_ISLANDS, org.bukkit.Color.fromRGB(180, 180, 180));
        BIOME_COLOR_MAP.put(Biome.END_BARRENS, org.bukkit.Color.fromRGB(140, 140, 140));
    }

    // Private constructor so it can’t be called externally
    private SpawnMonsters(XPManager xpManager) {
        this.xpManager = xpManager;
    }

    // The public method to retrieve the singleton instance.
    public static synchronized SpawnMonsters getInstance(XPManager xpManager) {
        if (instance == null) {
            instance = new SpawnMonsters(xpManager);
        }
        return instance;
    }

    public static int getDayCount(Player player) {
        int playTimeTicks = player.getStatistic(Statistic.PLAY_ONE_MINUTE);
        return playTimeTicks / 24000; // 1 Minecraft day = 24000 ticks
    }

    public boolean shouldMutationOccur(int playerHostility) {
        Random random = new Random();
        int randomValue = random.nextInt(100) + 1; // 1 to 100
        return randomValue <= playerHostility;
    }

    //armor mutation relic
    @EventHandler
    public void onMobDeathForShatterproofDrop(EntityDeathEvent event) {
        if (!(event.getEntity() instanceof LivingEntity mob)) return;

        EntityEquipment equipment = mob.getEquipment();
        if (equipment == null) return;

        Player killer = mob.getKiller();
        if (killer == null) return;

        ItemStack chestplate = equipment.getChestplate();
        if (chestplate == null) return;

        Material armorType = chestplate.getType();
        Random random = new Random();

        boolean shouldDrop = false;

        switch (armorType) {
            case CHAINMAIL_CHESTPLATE:
            case GOLDEN_CHESTPLATE:
                shouldDrop = random.nextDouble() < 0.10; // 10% chance
                break;
            case DIAMOND_CHESTPLATE:
                shouldDrop = true; // 100% chance
                break;
            default:
                break;
        }

        if (shouldDrop) {
            Location dropLoc = mob.getLocation();
            dropLoc.getWorld().dropItemNaturally(dropLoc, ItemRegistry.getVerdantRelicShatterproof());
            killer.sendMessage(ChatColor.AQUA + "You found a " + ChatColor.GOLD + "Verdant Relic: Shatterproof!");
            killer.playSound(killer.getLocation(), Sound.BLOCK_AMETHYST_BLOCK_CHIME, 1.0f, 1.2f);
        }
    }

    //armor mutation
    public void applyRandomArmor(LivingEntity entity) {
        if (entity == null) return;

        Random random = new Random();
        int randomValue = random.nextInt(100);

        Material baseChestplate;
        if (randomValue < 80) {
            // 80% chance for leather, chainmail, or gold
            Material[] options = {Material.LEATHER_CHESTPLATE, Material.CHAINMAIL_CHESTPLATE, Material.GOLDEN_CHESTPLATE};
            baseChestplate = options[random.nextInt(options.length)];
        } else if (randomValue < 95) {
            baseChestplate = Material.IRON_CHESTPLATE;
        } else {
            baseChestplate = Material.DIAMOND_CHESTPLATE;
        }

        Material helmet, chestplate, leggings, boots;
        switch (baseChestplate) {
            case LEATHER_CHESTPLATE:
                helmet = Material.LEATHER_HELMET;
                chestplate = Material.LEATHER_CHESTPLATE;
                leggings = Material.LEATHER_LEGGINGS;
                boots = Material.LEATHER_BOOTS;
                break;
            case CHAINMAIL_CHESTPLATE:
                helmet = Material.CHAINMAIL_HELMET;
                chestplate = Material.CHAINMAIL_CHESTPLATE;
                leggings = Material.CHAINMAIL_LEGGINGS;
                boots = Material.CHAINMAIL_BOOTS;
                break;
            case GOLDEN_CHESTPLATE:
                helmet = Material.GOLDEN_HELMET;
                chestplate = Material.GOLDEN_CHESTPLATE;
                leggings = Material.GOLDEN_LEGGINGS;
                boots = Material.GOLDEN_BOOTS;
                break;
            case IRON_CHESTPLATE:
                helmet = Material.IRON_HELMET;
                chestplate = Material.IRON_CHESTPLATE;
                leggings = Material.IRON_LEGGINGS;
                boots = Material.IRON_BOOTS;
                break;
            case DIAMOND_CHESTPLATE:
                helmet = Material.DIAMOND_HELMET;
                chestplate = Material.DIAMOND_CHESTPLATE;
                leggings = Material.DIAMOND_LEGGINGS;
                boots = Material.DIAMOND_BOOTS;
                break;
            default:
                helmet = Material.LEATHER_HELMET;
                chestplate = Material.LEATHER_CHESTPLATE;
                leggings = Material.LEATHER_LEGGINGS;
                boots = Material.LEATHER_BOOTS;
                break;
        }

        // Create ItemStacks for each armor piece
        ItemStack helmetItem = new ItemStack(helmet);
        ItemStack chestplateItem = new ItemStack(chestplate);
        ItemStack leggingsItem = new ItemStack(leggings);
        ItemStack bootsItem = new ItemStack(boots);

        // If using leather, camouflage the armor to the biome’s color scheme.
        if (chestplate == Material.LEATHER_CHESTPLATE) {
            // Get the biome at the entity’s location.
            Biome biome = entity.getLocation().getBlock().getBiome();
            org.bukkit.Color camoColor = BIOME_COLOR_MAP.get(biome);
            if (camoColor == null) {
                // Fallback color if the biome isn’t mapped.
                camoColor = org.bukkit.Color.GRAY;
            }

            LeatherArmorMeta meta;

            meta = (LeatherArmorMeta) helmetItem.getItemMeta();
            meta.setColor(camoColor);
            helmetItem.setItemMeta(meta);

            meta = (LeatherArmorMeta) chestplateItem.getItemMeta();
            meta.setColor(camoColor);
            chestplateItem.setItemMeta(meta);

            meta = (LeatherArmorMeta) leggingsItem.getItemMeta();
            meta.setColor(camoColor);
            leggingsItem.setItemMeta(meta);

            meta = (LeatherArmorMeta) bootsItem.getItemMeta();
            meta.setColor(camoColor);
            bootsItem.setItemMeta(meta);
        }

        // Set the armor on the entity
        EntityEquipment equipment = entity.getEquipment();
        if (equipment != null) {
            equipment.setHelmet(helmetItem);
            equipment.setChestplate(chestplateItem);
            equipment.setLeggings(leggingsItem);
            equipment.setBoots(bootsItem);
        }
    }

    //weapon mutation
    public void equipRandomWeapon(LivingEntity entity) {
        Random random = new Random();
        Material weaponMaterial = random.nextBoolean() ? Material.IRON_SWORD : Material.IRON_SHOVEL;
        EntityEquipment equipment = entity.getEquipment();
        equipment.setItemInMainHand(new ItemStack(weaponMaterial));
    }

    //enchantment mutation
    public void enchantArmorWithProtection(LivingEntity entity) {
        EntityEquipment equipment = entity.getEquipment();
        ItemStack[] armor = {
                equipment.getHelmet(),
                equipment.getChestplate(),
                equipment.getLeggings(),
                equipment.getBoots()
        };
        for (ItemStack item : armor) {
            if (item != null && item.getType() != Material.AIR) {
                ItemMeta meta = item.getItemMeta();
                meta.addEnchant(Enchantment.PROTECTION_ENVIRONMENTAL, 3, true);
                item.setItemMeta(meta);
            }
        }
    }

    @EventHandler
    public void alterMonsters(EntitySpawnEvent e) {
        Entity entity = e.getEntity();
        HostilityManager hostilityManager = HostilityManager.getInstance(plugin);
        int playerHostility = hostilityManager.getPlayerDifficultyTier(getNearestPlayer(entity, 1000));

        //creeper rarity
        if (entity instanceof Creeper) {
            Random random = new Random();
            int randomValue = random.nextInt(100) + 1;
            if (randomValue <= 90) { // Remove 90% of creepers.
                entity.remove();
            }
        }

        //speed mutation
        if (entity instanceof Monster monster) {
            if (shouldMutationOccur(playerHostility)) {
                monster.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 2, true));
            }
        }

        if (entity instanceof Zombie zombie) {
            //knight mutation
            if (shouldMutationOccur(playerHostility)) {
                KnightMob knightMob = new KnightMob(plugin);
                knightMob.transformToKnight(zombie);
                zombie.setCustomName(ChatColor.GRAY + "Knight");
            }
            //armor mutation
            if (shouldMutationOccur(playerHostility)) {
                applyRandomArmor(zombie);
            }
            if(shouldMutationOccur(playerHostility)){
                applyEnragedMutation(zombie);
            }
            if (shouldMutationOccur(playerHostility)) {
                applyRandomArmor(zombie);
            }
            if (shouldMutationOccur(playerHostility)) {
                applyRandomArmor(zombie);
            }
            if (shouldMutationOccur(playerHostility)) {
                equipRandomWeapon(zombie);
            }
            if (shouldMutationOccur(playerHostility)) {
                enchantArmorWithProtection(zombie);
            }
        }
        if(entity instanceof Drowned drowned){
            if(shouldMutationOccur(playerHostility)) {
                // Visual indicator that transformation is starting
                drowned.getWorld().spawnParticle(Particle.BUBBLE_COLUMN_UP, drowned.getLocation(),
                        50, 0.5, 1, 0.5, 0.1);
                drowned.getWorld().playSound(drowned.getLocation(),
                        Sound.ENTITY_DROWNED_AMBIENT_WATER, 1.0f, 0.5f);

                // Add glowing effect during transformation
                drowned.setGlowing(true);

                // Schedule the transformation
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        if(drowned.isValid() && !drowned.isDead()) {  // Check if drowned still exists
                            transformDrownedToDeepSeaDiver(drowned);
                            drowned.setGlowing(false);  // Remove glowing effect

                            // Transformation complete effects
                            drowned.getWorld().spawnParticle(Particle.WATER_WAKE,
                                    drowned.getLocation(), 100, 0.5, 1, 0.5);
                            drowned.getWorld().playSound(drowned.getLocation(),
                                    Sound.ENTITY_ZOMBIE_CONVERTED_TO_DROWNED, 1.0f, 1.0f);
                        }
                    }
                }.runTaskLater(plugin, 60L); // 60 ticks = 3 seconds
            }
        }
        if (entity instanceof WitherSkeleton ws) {
            if (shouldMutationOccur(playerHostility)) {
                KnightMob knightMob = new KnightMob(plugin);
                knightMob.transformToKnight(ws);
                ws.setCustomName(ChatColor.GRAY + "Knight");
            }
            if (shouldMutationOccur(playerHostility)) {
                applyRandomArmor(ws);
            }
            if (shouldMutationOccur(playerHostility)) {
                applyRandomArmor(ws);
            }
            if (shouldMutationOccur(playerHostility)) {
                applyRandomArmor(ws);
            }
            if (shouldMutationOccur(playerHostility)) {
                equipRandomWeapon(ws);
            }
            if (shouldMutationOccur(playerHostility)) {
                enchantArmorWithProtection(ws);
            }
        }
        //charged mutation
        if (entity instanceof Creeper creeper) {
            if (shouldMutationOccur(playerHostility)) {
                creeper.setPowered(true);
            }
        }

        if (entity instanceof Blaze monster) {
            //floating mutation
            if (shouldMutationOccur(playerHostility)) {
                monster.setCustomName(ChatColor.RED + "High Flying Blaze");
                monster.addPotionEffect(new PotionEffect(PotionEffectType.LEVITATION, Integer.MAX_VALUE, 1, true));
            }
        }
        //fast swimmer mutation
        if (entity instanceof Drowned monster) {
            if (shouldMutationOccur(playerHostility)) {
                monster.getEquipment().setItemInMainHand(ItemRegistry.getTrident());
                monster.setCustomName(ChatColor.RED + "Olympic Swimmer");
                monster.addPotionEffect(new PotionEffect(PotionEffectType.DOLPHINS_GRACE, Integer.MAX_VALUE, 3, true));
            }
        }
        //giant mutation
        if (entity instanceof MagmaCube monster) {
            if (shouldMutationOccur(playerHostility)) {
                monster.setSize(4);
                monster.getEquipment().setItemInMainHand(ItemRegistry.getTrident());
                monster.setCustomName(ChatColor.RED + "Giant Cube");
                monster.setCustomNameVisible(true);
            }
        }
        //sniper mutation
        if (entity instanceof Skeleton monster) {
            if(shouldMutationOccur(playerHostility)){
                applyEnragedMutation(monster);
            }
            if (shouldMutationOccur(playerHostility)) {
                monster.setCustomName(ChatColor.RED + "Sniper");
                monster.setCustomNameVisible(true);
                ItemStack sniperHelmet = new ItemStack(Material.DIAMOND_HELMET);
                sniperHelmet.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 2);
                sniperHelmet.addEnchantment(Enchantment.OXYGEN, 1);
                monster.getEquipment().setHelmet(sniperHelmet);
            }
        }
        //giant mutation
        if (entity instanceof Slime monster) {
            if (shouldMutationOccur(playerHostility)) {
                monster.setSize(4);
                monster.getEquipment().setItemInMainHand(ItemRegistry.getTrident());
                monster.setCustomName(ChatColor.RED + "Giant Cube");
                monster.setCustomNameVisible(true);
            }
        }


        if (entity instanceof Monster) {
            LivingEntity mob = (LivingEntity) entity;
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (!mob.isValid()) return;
                    if (mob.getEquipment().getItemInMainHand().equals(ItemRegistry.getSpiritBow())) {
                        return;
                    }
                    if (!mob.hasMetadata("SEA_CREATURE")) {
                        double distance = getDistanceFromOrigin(entity);
                        Player nearestPlayer = getNearestPlayer(entity, 1000);
                        int mobLevel;
                        if (nearestPlayer != null) {
                            int level = playerHostility * 10 - new Random().nextInt(11);
                            mobLevel = Math.min(level + getRandomLevelVariation(), MAX_MONSTER_LEVEL);
                        } else {
                            mobLevel = Math.min((int) (distance / 100) + getRandomLevelVariation(), MAX_MONSTER_LEVEL);
                        }

                        applyMobAttributes(mob, mobLevel);
                    } else {
                        plugin.getLogger().info("Skipped attribute application for sea creature: " + mob.getType());
                    }
                }
            }.runTaskLater(plugin, 40L);
        }

    }

    public double getDistanceFromOrigin(Entity mob) {
        Location loc = mob.getLocation();
        double x = loc.getX(), z = loc.getZ();
        return Math.sqrt(x * x + z * z);
    }

    public Player getNearestPlayer(Entity entity, double radius) {
        Location mobLocation = entity.getLocation();
        double nearestDistanceSquared = radius * radius;
        Player nearestPlayer = null;
        for (Player player : entity.getWorld().getPlayers()) {
            double distanceSquared = player.getLocation().distanceSquared(mobLocation);
            if (distanceSquared <= nearestDistanceSquared) {
                nearestDistanceSquared = distanceSquared;
                nearestPlayer = player;
            }
        }
        return nearestPlayer;
    }

    public void applyEnderDragonAttributes(EnderDragon dragon) {
        int level = 300;
        dragon.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, Integer.MAX_VALUE, 1, true));
        dragon.addPotionEffect(new PotionEffect(PotionEffectType.HEALTH_BOOST, Integer.MAX_VALUE, 255, true));
        dragon.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, Integer.MAX_VALUE, 255, true));
        dragon.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, Integer.MAX_VALUE, 3, true));
        double healthMultiplier = 1 + (level * 0.1);
        double originalHealth = dragon.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue();
        double newHealth = Math.min(originalHealth * healthMultiplier, 2000);
        Objects.requireNonNull(dragon.getAttribute(Attribute.GENERIC_MAX_HEALTH)).setBaseValue(2000);
        dragon.setHealth(newHealth);
        dragon.setMetadata("mobLevel", new FixedMetadataValue(MinecraftNew.getInstance(), level));
        String color = getColorForLevel(level);
        dragon.setCustomName(color + "Level: " + level + " " + formatMobType(dragon.getType().toString()));
        dragon.setCustomNameVisible(true);
        dragon.setRemoveWhenFarAway(true);
        dragon.setCustomName(ChatColor.DARK_RED + "[Lv: 1000] Ender Dragon");
        BossBar bossBar = dragon.getBossBar();
        bossBar.setColor(BarColor.RED);
        bossBar.setStyle(BarStyle.SEGMENTED_20);
        dragon.setHealth(dragon.getMaxHealth());
    }
    public void transformDrownedToDeepSeaDiver(Drowned drowned) {
        // Set the custom name to identify our Deep Sea Diver.
        drowned.setCustomName(ChatColor.DARK_AQUA + "Deep Sea Diver");
        drowned.setCustomNameVisible(true);

        // Copper color: you can adjust the RGB as desired.
        Color copperColor = Color.fromRGB(184, 115, 51);

        // Create a custom player head with the given texture.
        ItemStack helmet = new ItemStack(Material.PLAYER_HEAD, 1);
        SkullMeta skullMeta = (SkullMeta) helmet.getItemMeta();
        skullMeta = setCustomSkullTexture(skullMeta, "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMTc4MzBjMWQ4Mjg0NWM5MWI4MzQyOWY5ZGM1OTczMTc4NDE1MzhlMTRkNGZiZWQ2MWFlMWEzYjBlYjdjY2QifX19");
        helmet.setItemMeta(skullMeta);

        // Create the chestplate, leggings, and boots as leather armor dyed copper.
        ItemStack chestplate = new ItemStack(Material.LEATHER_CHESTPLATE);
        LeatherArmorMeta meta = (LeatherArmorMeta) chestplate.getItemMeta();
        meta.setColor(copperColor);
        chestplate.setItemMeta(meta);

        ItemStack leggings = new ItemStack(Material.LEATHER_LEGGINGS);
        meta = (LeatherArmorMeta) leggings.getItemMeta();
        meta.setColor(copperColor);
        leggings.setItemMeta(meta);

        ItemStack boots = new ItemStack(Material.LEATHER_BOOTS);
        meta = (LeatherArmorMeta) boots.getItemMeta();
        meta.setColor(copperColor);
        boots.setItemMeta(meta);

        // Equip the Drowned with the suit.
        EntityEquipment equipment = drowned.getEquipment();
        if (equipment != null) {
            equipment.setHelmet(helmet);
            equipment.setChestplate(chestplate);
            equipment.setLeggings(leggings);
            equipment.setBoots(boots);
        }
    }
    // Call this method when you want to apply the Enraged mutation.
    public void applyEnragedMutation(LivingEntity entity) {
        // Set the custom name to "Enraged" in red.
        entity.setCustomName(ChatColor.RED + "Enraged");
        entity.setCustomNameVisible(true);

        // Create red leather armor pieces.
        ItemStack helmet = new ItemStack(Material.LEATHER_HELMET);
        LeatherArmorMeta helmetMeta = (LeatherArmorMeta) helmet.getItemMeta();
        helmetMeta.setColor(org.bukkit.Color.RED);
        helmet.setItemMeta(helmetMeta);

        ItemStack chestplate = new ItemStack(Material.LEATHER_CHESTPLATE);
        LeatherArmorMeta chestplateMeta = (LeatherArmorMeta) chestplate.getItemMeta();
        chestplateMeta.setColor(org.bukkit.Color.RED);
        chestplate.setItemMeta(chestplateMeta);

        ItemStack leggings = new ItemStack(Material.LEATHER_LEGGINGS);
        LeatherArmorMeta leggingsMeta = (LeatherArmorMeta) leggings.getItemMeta();
        leggingsMeta.setColor(org.bukkit.Color.RED);
        leggings.setItemMeta(leggingsMeta);

        ItemStack boots = new ItemStack(Material.LEATHER_BOOTS);
        LeatherArmorMeta bootsMeta = (LeatherArmorMeta) boots.getItemMeta();
        bootsMeta.setColor(org.bukkit.Color.RED);
        boots.setItemMeta(bootsMeta);

        // Equip the armor and diamond sword.
        EntityEquipment equipment = entity.getEquipment();
        if (equipment != null) {
            equipment.setHelmet(helmet);
            equipment.setChestplate(chestplate);
            equipment.setLeggings(leggings);
            equipment.setBoots(boots);
            // Equip a diamond sword in the main hand.
            equipment.setItemInMainHand(new ItemStack(Material.DIAMOND_SWORD));
        }
    }

    /**
     * Helper method to set a custom texture on a SkullMeta.
     */
    private SkullMeta setCustomSkullTexture(SkullMeta skullMeta, String texture) {
        GameProfile profile = new GameProfile(UUID.randomUUID(), null);
        profile.getProperties().put("textures", new Property("textures", texture));
        try {
            Field profileField = skullMeta.getClass().getDeclaredField("profile");
            profileField.setAccessible(true);
            profileField.set(skullMeta, profile);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return skullMeta;
    }

    // --- Example event handlers ---

    /**
     * When a Deep Sea Diver is killed, increase the player's oxygen.
     */
    @EventHandler
    public void onDeepSeaDiverDeath(EntityDeathEvent event) {
        if (event.getEntity() instanceof Drowned) {
            Drowned drowned = (Drowned) event.getEntity();
            if (drowned.getCustomName() != null && drowned.getCustomName().contains("Deep Sea Diver")) {
                Player killer = drowned.getKiller();
                if (killer != null) {
                    PlayerOxygenManager oxygenManager = PlayerOxygenManager.getInstance();
                    xpManager.addXP(killer, "Fishing", 800);
                    int currentOxygen = oxygenManager.getPlayerOxygen(killer);
                    oxygenManager.setPlayerOxygenLevel(killer, currentOxygen + 100);
                    event.setDroppedExp(100);
                    drowned.getLocation().getWorld().dropItem(drowned.getLocation(), ItemRegistry.getVerdantRelicEntionPlastSeed());
                }
            }
        }
    }
    @EventHandler
    public void onSpeedMutantDeath(EntityDeathEvent event) {
        Random random = new Random();
        if(!(event.getEntity() instanceof Monster)){
            return;
        }
        Monster monster = (Monster) event.getEntity();
        if (monster.hasPotionEffect(PotionEffectType.SPEED)) {
            Player killer = monster.getKiller();
            if (killer != null) {
                xpManager.addXP(killer, "Combat", 100);
                event.setDroppedExp(100);
                if(random.nextBoolean()) {
                    if(random.nextBoolean()) {
                        killer.sendMessage(ChatColor.AQUA + "You found a " + ChatColor.GOLD + "Verdant Relic: Entropy!");
                        Objects.requireNonNull(monster.getLocation().getWorld()).dropItem(monster.getLocation(), ItemRegistry.getVerdantRelicEntropySeed());
                    }
                }
            }
        }
    }
    @EventHandler
    public void onSniperDeath(EntityDeathEvent event) {
        LivingEntity entity = event.getEntity();
        // Check if the monster is mutated as "Enraged" by stripping color codes and comparing.
        if (entity.getCustomName() != null
                && ChatColor.stripColor(entity.getCustomName()).contains("Sniper")) {
            // Optionally, you can add additional checks here such as the mob type.
            Player killer = entity.getKiller();
            if (killer != null) {
                // Notify the player (optional)
                killer.sendMessage(ChatColor.RED + "The Sniper has dropped a rare relic!");

                // Drop the Enraged relic from your ItemRegistry at the location of the death.
                entity.getWorld().dropItemNaturally(entity.getLocation(), ItemRegistry.getVerdantRelicMarrow());
            }
        }
    }
    @EventHandler
    public void onEnragedMonsterDeath(EntityDeathEvent event) {
        LivingEntity entity = event.getEntity();
        // Check if the monster is mutated as "Enraged" by stripping color codes and comparing.
        if (entity.getCustomName() != null
                && ChatColor.stripColor(entity.getCustomName()).contains("Enraged")) {
            // Optionally, you can add additional checks here such as the mob type.
            Player killer = entity.getKiller();
            if (killer != null) {
                // Notify the player (optional)
                killer.sendMessage(ChatColor.RED + "The Enraged monster has dropped a rare relic!");

                // Drop the Enraged relic from your ItemRegistry at the location of the death.
                entity.getWorld().dropItemNaturally(entity.getLocation(), ItemRegistry.getVerdantRelicGravity());
            }
        }
    }

    @EventHandler
    public void onInvisibleSpiderDeath(EntityDeathEvent event) {
        if (!(event.getEntity() instanceof Spider spider)) return;
        if (!spider.isInvisible()) return;

        Player killer = spider.getKiller();
        if (killer != null) {
            spider.getWorld().dropItemNaturally(spider.getLocation(), ItemRegistry.getVerdantRelicStarlightSeed());
            killer.sendMessage(ChatColor.AQUA + "You found a " + ChatColor.GOLD + "Verdant Relic: Starlight!");
        }
    }


    /**
     * When a Deep Sea Diver is hit, play a metal clang sound.
     */
    @EventHandler
    public void onDeepSeaDiverHit(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Drowned) {
            Drowned drowned = (Drowned) event.getEntity();
            if (drowned.getCustomName() != null && drowned.getCustomName().contains("Deep Sea Diver")) {
                drowned.getWorld().playSound(drowned.getLocation(), Sound.BLOCK_ANVIL_LAND, 1.0f, 1.0f);
            }
        }
    }
    public void applyMobAttributes(LivingEntity mob, int level) {
        level = Math.max(1, Math.min(level, MAX_MONSTER_LEVEL));
        AttributeInstance healthAttribute = mob.getAttribute(Attribute.GENERIC_MAX_HEALTH);
        if (healthAttribute != null) {
            for (AttributeModifier modifier : new ArrayList<>(healthAttribute.getModifiers())) {
                healthAttribute.removeModifier(modifier);
            }
            double defaultHealth = healthAttribute.getDefaultValue();
            healthAttribute.setBaseValue(defaultHealth);
        }

        double healthMultiplier;

        if (level <= 10) {
            // Scale linearly from 0.1x to 1.0x from level 1 to 10
            healthMultiplier = 0.1 + 0.1 * (level - 1);
        } else {
            // From level 11 upward, scale normally
            healthMultiplier = 1 + ((level - 10) * 0.1);
        }

        double originalHealth = mob.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue();
        double newHealth = Math.min(originalHealth * healthMultiplier, 2000);
        mob.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(newHealth);
        mob.setHealth(newHealth);
        mob.setMetadata("mobLevel", new FixedMetadataValue(MinecraftNew.getInstance(), level));

        // Skip renaming if it's a Knight
        String baseName;
        if (mob.getCustomName() != null && !mob.getCustomName().isEmpty()) {
            baseName = ChatColor.stripColor(mob.getCustomName());
        } else {
            baseName = formatMobType(mob.getType().toString());
        }

        String color = getColorForLevel(level);
        // Build the final custom name combining the level with the mutation (or default mob type) name.
        mob.setCustomName(ChatColor.GRAY + "[" + color + "Lv: " + level + ChatColor.GRAY + "] " + color + baseName);
        mob.setCustomNameVisible(true);
        mob.setRemoveWhenFarAway(true);
    }


    private String getColorForLevel(int level) {
        if (level <= 20) return ChatColor.GRAY.toString();
        else if (level <= 40) return ChatColor.GREEN.toString();
        else if (level <= 60) return ChatColor.AQUA.toString();
        else if (level <= 80) return ChatColor.LIGHT_PURPLE.toString();
        else if (level <= 100) return ChatColor.GOLD.toString();
        else if (level <= 120) return ChatColor.BLUE.toString();
        else if (level <= 150) return ChatColor.RED.toString();
        else if (level <= 200) return ChatColor.DARK_RED.toString();
        else if (level <= 250) return ChatColor.DARK_PURPLE.toString();
        else if (level <= 280) return ChatColor.BLACK.toString();
        else return ChatColor.WHITE.toString();
    }

    private String formatMobType(String mobType) {
        String formattedType = mobType.replace('_', ' ').toLowerCase();
        return formattedType.substring(0, 1).toUpperCase() + formattedType.substring(1);
    }

    private int getRandomLevelVariation() {
        Random rand = new Random();
        return rand.nextDouble() < 0.1 ? rand.nextInt(100) + 1 : 0;
    }
}
