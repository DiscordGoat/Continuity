package goat.minecraft.minecraftnew.subsystems.forestry;

import com.mojang.authlib.GameProfile;
import goat.minecraft.minecraftnew.MinecraftNew;
import goat.minecraft.minecraftnew.subsystems.combat.SpawnMonsters;
import goat.minecraft.minecraftnew.utils.devtools.ItemRegistry;
import goat.minecraft.minecraftnew.utils.devtools.XPManager;
import goat.minecraft.minecraftnew.utils.devtools.PlayerMeritManager;
import goat.minecraft.minecraftnew.subsystems.forestry.EffigyUpgradeSystem;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;
import org.bukkit.entity.Skeleton;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.lang.reflect.Field;
import java.util.*;

public class ForestSpiritManager implements Listener {

    private static ForestSpiritManager instance;
    private final MinecraftNew plugin;
    private final XPManager xpManager;
    private final Random random = new Random();

    // Mapping for head textures by spirit type (one texture per wood type).
    // Populate these with your Base64 texture strings.
    private final Map<String, String> headTextureMapping = new HashMap<>();
    // Mapping for particle effects by spirit type.
    private final Map<String, Particle> particleMapping = new HashMap<>();
    // Mapping for rare drops by spirit type – set these manually.
    private final Map<String, ItemStack> rareDropMapping = new HashMap<>();

    private ForestSpiritManager(MinecraftNew plugin) {
        this.plugin = plugin;
        this.xpManager = new XPManager(plugin);
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        initMappings();
    }

    public static ForestSpiritManager getInstance(MinecraftNew plugin) {
        if (instance == null) {
            instance = new ForestSpiritManager(plugin);
        }
        return instance;
    }

    private void initMappings() {
        // Head texture mapping – set your Base64 textures.
        headTextureMapping.put("Oak Spirit", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMTRmNzg2NDQzZDE4NDEyN2MyYjI5ODY4ZWEwMzg1ZGI4MDUyZmY2ZjA4NmVhYTM3YTRlNTE0MDI1ODI5MDc3NSJ9fX0=");
        headTextureMapping.put("Spruce Spirit", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZTMxZGU5MDNmYjc3MGRjZjAyYjQ3ZGNiOTM1NzRiMjg1ODJiNGRkNjBmNDExYWQ0MGYwZWJkYjgwNDc3NWY3MyJ9fX0=");
        headTextureMapping.put("Birch Spirit", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvODZkNGIyN2ZlZWEwMTZjN2ZmY2ZkNjQzN2Y5ZjJhY2Q1MTIyNjQ1NTEzYTRkNTcwNTBlYmMzYjAwNGNlYjk3MSJ9fX0=");
        headTextureMapping.put("Jungle Spirit", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNjY3YjBmMGJmMmNkYjJiMzIwNjM5YmVmNzAxNjRhODVhMzIwZjgxMTZjYjUwOGM5MzA3YTZjODQ3ZmJlNjgyMCJ9fX0=");
        headTextureMapping.put("Acacia Spirit", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMmVlMzAzMGVhYmQ3Yzc5N2U5MGI0M2RjZDM5MDQwMGJkZjhhMDRhZGIxYWYxNmNmZWVkYjk5M2VjODQ2NWZjZSJ9fX0=");
        headTextureMapping.put("Dark Oak Spirit", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZGQ2NmY2ZjEyMDgwYjdiOWYyODdlYjUyNThlNjIzOTMyNzViZWU1MTg1NmUzODA3MmVhZTA2YTEwMmU0ODQ0NSJ9fX0=");
        headTextureMapping.put("Cherry Spirit", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYmNiYzg0ZWYyODg2OTY3NGM0MDFlNWM1MjNjZWFkNGVjMzIyYjA2MmJiOTRjNTIyNTFmZmIwOTY4M2IyOGM2ZCJ9fX0=");
        headTextureMapping.put("Crimson Spirit", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvM2Y5NThlZjZmNmRiNmY0YzQwYmMzNmU0ZWVjOGQyNWQyZGU3Njc4OGM4OGNkOTQxODlhMGE1MTg4NGVmZjFlZCJ9fX0=");
        headTextureMapping.put("Warped Spirit", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZDU3NDczOWNkYjFiN2I4NmI3NmRiYTg1NGYyYmI2MjM3NDFlZTFmNjY1NTE5OThiMjA3MDExMmE5ODY3ZWMwNCJ9fX0=");

        // Rare drop mapping – assign your custom rare drop items here.
        // These items should be manually defined (using your ItemRegistry or custom items).
        rareDropMapping.put("Oak Spirit", ItemRegistry.getPetrifiedLog());
        rareDropMapping.put("Spruce Spirit", ItemRegistry.getPinecone());
        rareDropMapping.put("Birch Spirit", ItemRegistry.getBirchStrip());
        rareDropMapping.put("Jungle Spirit", ItemRegistry.getHumidBark());
        rareDropMapping.put("Acacia Spirit", ItemRegistry.getAcaciaGum());
        rareDropMapping.put("Dark Oak Spirit", ItemRegistry.getAcorn());
        rareDropMapping.put("Cherry Spirit", ItemRegistry.getCherryBlossom());
        rareDropMapping.put("Crimson Spirit", ItemRegistry.getMapleBark());
        rareDropMapping.put("Warped Spirit", ItemRegistry.getBlueNetherWart());

        // Particle mapping per spirit type.
        particleMapping.put("Oak Spirit", Particle.VILLAGER_HAPPY);
        particleMapping.put("Spruce Spirit", Particle.CRIT);
        particleMapping.put("Birch Spirit", Particle.SPELL);
        particleMapping.put("Jungle Spirit", Particle.FLAME);
        particleMapping.put("Acacia Spirit", Particle.CLOUD);
        particleMapping.put("Dark Oak Spirit", Particle.SMOKE_NORMAL);
        particleMapping.put("Cherry Spirit", Particle.HEART);
        particleMapping.put("Crimson Spirit", Particle.REDSTONE);
        particleMapping.put("Warped Spirit", Particle.SPELL_WITCH);
    }

    // Determine the spirit tier based on the player's notoriety from Forestry.
    private int getSpiritTier(Player player) {
        int notoriety = Forestry.getInstance().getNotoriety(player);
        if (notoriety <= 64) return 1;
        else if (notoriety <= 192) return 2;
        else if (notoriety <= 384) return 3;
        else if (notoriety <= 640) return 4;
        else return 5;
    }

    // Return spirit level for a given tier.

    private int getSpiritLevelForTier(int tier) {
        switch (tier) {
            case 1: return 20;
            case 2: return 50;
            case 3: return 100;
            case 4: return 200;
            case 5: return 300;
            default: return 20;
        }
    }

    /**
     * Spawns a forest spirit.
     *
     * @param spiritName The name (type) of the spirit (e.g., "Oak Spirit").
     * @param loc        The spawn location.
     * @param block      The log block that was broken.
     * @param player     The player responsible.
     */
    public void spawnSpirit(String spiritName, Location loc, Block block, Player player) {
        int tier = getSpiritTier(player);
        int level = getSpiritLevelForTier(tier);

        ItemStack axe = player.getInventory().getItemInMainHand();
        int confusion = EffigyUpgradeSystem.getUpgradeLevel(axe, EffigyUpgradeSystem.UpgradeType.ANCIENT_CONFUSION);
        if (confusion > 0) {
            level = Math.max(1, level - confusion * 10);
        }

        SpawnMonsters spawnMonsters = SpawnMonsters.getInstance(xpManager);
        World world = loc.getWorld();
        if (world == null) return;

        // Spawn the spirit as a Skeleton.
        Skeleton spirit = (Skeleton) world.spawnEntity(loc, EntityType.SKELETON);
        spirit.setPersistent(true);
        // Set a temporary name; true level will be applied after 41 ticks.
        spirit.setCustomName(ChatColor.RED + "[Lvl ?] " + spiritName);
        spirit.setCustomNameVisible(true);
        // Mark as forest spirit and store its tier.
        spirit.setMetadata("forestSpirit", new FixedMetadataValue(plugin, true));
        spirit.setMetadata("spiritTier", new FixedMetadataValue(plugin, tier));

        // Make the spirit invulnerable and disable AI for the first 41 ticks.
        spirit.setInvulnerable(true);
        spirit.setAI(false);

        // Schedule delayed level assignment after 41 ticks.
        int finalLevel = level;
        new BukkitRunnable() {
            @Override
            public void run() {
                if (spirit.isDead() || !spirit.isValid()) {
                    cancel();
                    return;
                }
                spawnMonsters.applyMobAttributes(spirit, finalLevel);
                spirit.setCustomName(ChatColor.RED + "[Lvl " + finalLevel + "] " + spiritName);
                spirit.setHealth(spirit.getMaxHealth());
                // Re-enable AI and vulnerability.
                spirit.setAI(true);
                spirit.setInvulnerable(false);
            }
        }.runTaskLater(plugin, 41L);

        // Apply a permanent Speed effect (amplifier = tier - 1).
        spirit.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, tier - 1, false, false, false));

        // Equip a custom head using our texture mapping and full dyed leather armor.
        ItemStack head = createSpiritHead(spiritName);
        EntityEquipment equipment = spirit.getEquipment();
        if (equipment != null) {
            equipment.setHelmet(head);
            equipment.setHelmetDropChance(0.0f);
            Color armorColor = getArmorColorForSpirit(spiritName);
            equipment.setChestplate(createColoredArmor(Material.LEATHER_CHESTPLATE, armorColor));
            equipment.setLeggings(createColoredArmor(Material.LEATHER_LEGGINGS, armorColor));
            equipment.setBoots(createColoredArmor(Material.LEATHER_BOOTS, armorColor));
            equipment.setChestplateDropChance(0.0f);
            equipment.setLeggingsDropChance(0.0f);
            equipment.setBootsDropChance(0.0f);
        }

        // Schedule enhanced particle emission.
        scheduleParticleEmission(spirit, spiritName, tier);

        // If spirit is Tier 4 or 5, schedule a heartbeat sound.
        if (tier >= 4) {
            scheduleHeartbeatSound(spirit, tier);
        }
    }

    /**
     * Attempts to spawn a forest spirit based on chance.
     *
     * @param chance The chance to spawn (max 0.1 = 10%).
     * @param loc    The location.
     * @param block  The log block broken.
     * @param player The player responsible.
     */
    public boolean attemptSpiritSpawn(double chance, Location loc, Block block, Player player) {
        double clampedChance = Math.min(chance, 0.1);
        if (random.nextDouble() < clampedChance) {
            String spiritName = getSpiritNameFromBlock(block.getType());
            if (spiritName != null) {
                spawnSpirit(spiritName, loc, block, player);
                return true;
            }
        }
        return false;
    }

    // Determines the spirit name from the type of log broken.
    private String getSpiritNameFromBlock(Material blockType) {
        switch (blockType) {
            case OAK_LOG:
            case STRIPPED_OAK_LOG:
                return "Oak Spirit";
            case SPRUCE_LOG:
            case STRIPPED_SPRUCE_LOG:
                return "Spruce Spirit";
            case BIRCH_LOG:
            case STRIPPED_BIRCH_LOG:
                return "Birch Spirit";
            case JUNGLE_LOG:
            case STRIPPED_JUNGLE_LOG:
                return "Jungle Spirit";
            case ACACIA_LOG:
            case STRIPPED_ACACIA_LOG:
                return "Acacia Spirit";
            case DARK_OAK_LOG:
            case STRIPPED_DARK_OAK_LOG:
                return "Dark Oak Spirit";
            case CHERRY_LOG:
            case STRIPPED_CHERRY_LOG:
                return "Cherry Spirit";
            case CRIMSON_STEM:
            case STRIPPED_CRIMSON_STEM:
                return "Crimson Spirit";
            case WARPED_STEM:
            case STRIPPED_WARPED_STEM:
                return "Warped Spirit";
            default:
                return null;
        }
    }

    // Creates a custom skull for the spirit using the Base64 texture from our mapping.
    private ItemStack createSpiritHead(String spiritName) {
        String texture = headTextureMapping.getOrDefault(spiritName, "");
        return getCustomSkull(texture);
    }

    // Helper method to create a custom skull from a Base64 texture.
    private ItemStack getCustomSkull(String base64) {
        if (base64 == null || base64.isEmpty()) {
            return new ItemStack(Material.NAME_TAG);
        }
        ItemStack skull = new ItemStack(Material.PLAYER_HEAD, 1);
        SkullMeta skullMeta = (SkullMeta) skull.getItemMeta();
        if (skullMeta == null) {
            return skull;
        }
        GameProfile profile = new GameProfile(UUID.randomUUID(), null);
        profile.getProperties().put("textures", new com.mojang.authlib.properties.Property("textures", base64));
        try {
            Field profileField = skullMeta.getClass().getDeclaredField("profile");
            profileField.setAccessible(true);
            profileField.set(skullMeta, profile);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
            return new ItemStack(Material.NAME_TAG);
        }
        skull.setItemMeta(skullMeta);
        return skull;
    }

    // Creates dyed leather armor with the given material and color.
    private ItemStack createColoredArmor(Material material, Color color) {
        ItemStack armor = new ItemStack(material, 1);
        if (armor.getItemMeta() instanceof LeatherArmorMeta) {
            LeatherArmorMeta meta = (LeatherArmorMeta) armor.getItemMeta();
            meta.setColor(color);
            armor.setItemMeta(meta);
        }
        return armor;
    }

    // Returns the armor color based on the spirit (wood) type.
    private Color getArmorColorForSpirit(String spiritName) {
        switch (spiritName) {
            case "Oak Spirit":
                return Color.fromRGB(102, 51, 0);
            case "Spruce Spirit":
                return Color.fromRGB(51, 25, 0);
            case "Birch Spirit":
                return Color.fromRGB(255, 255, 204);
            case "Jungle Spirit":
                return Color.fromRGB(34, 139, 34);
            case "Acacia Spirit":
                return Color.fromRGB(255, 153, 51);
            case "Dark Oak Spirit":
                return Color.fromRGB(64, 64, 64);
            case "Cherry Spirit":
                return Color.fromRGB(255, 105, 180);
            case "Crimson Spirit":
                return Color.fromRGB(204, 0, 0);
            case "Warped Spirit":
                return Color.fromRGB(0, 128, 128);
            default:
                return Color.WHITE;
        }
    }

    // Schedules a repeating task to emit enhanced particles every half second.
    private void scheduleParticleEmission(Skeleton spirit, String spiritName, int tier) {
        Particle particle = particleMapping.getOrDefault(spiritName, Particle.VILLAGER_HAPPY);
        int count = tier * 100;         // Increased count for high visibility.
        double speed = tier * 0.2;        // Increased speed.
        new BukkitRunnable() {
            @Override
            public void run() {
                if (spirit.isDead() || !spirit.isValid()) {
                    cancel();
                    return;
                }
                Location loc = spirit.getLocation();
                loc.getWorld().spawnParticle(particle, loc, count, 0.5, 0.5, 0.5, speed);
            }
        }.runTaskTimer(plugin, 0L, 10L);
    }

    // Schedules a repeating heartbeat sound for nearby players if the spirit is Tier 4 or 5.
    private void scheduleHeartbeatSound(Skeleton spirit, int tier) {
        long interval = (tier == 4) ? 80L : 40L; // Tier 4: every 4 sec; Tier 5: every 2 sec.
        new BukkitRunnable() {
            @Override
            public void run() {
                if (spirit.isDead() || !spirit.isValid()) {
                    cancel();
                    return;
                }
                for (Player player : spirit.getWorld().getPlayers()) {
                    if (player.getLocation().distanceSquared(spirit.getLocation()) <= 100) {
                        player.playSound(spirit.getLocation(), Sound.ENTITY_WARDEN_HEARTBEAT, 1.0f, 1.0f);
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, interval);
    }

    // Event handler: handle combat interactions with forest spirits.
    @EventHandler
    public void onForestSpiritHit(EntityDamageByEntityEvent event) {
        Entity entity = event.getEntity();
        Entity damager = event.getDamager();

        if (entity.hasMetadata("forestSpirit") && damager instanceof Player) {
            Player player = (Player) damager;
            ItemStack axe = player.getInventory().getItemInMainHand();
            int headhunter = EffigyUpgradeSystem.getUpgradeLevel(axe, EffigyUpgradeSystem.UpgradeType.HEADHUNTER);
            int confusion = EffigyUpgradeSystem.getUpgradeLevel(axe, EffigyUpgradeSystem.UpgradeType.ANCIENT_CONFUSION);

            if (headhunter > 0) {
                event.setDamage(event.getDamage() * (2 + headhunter * 0.10));
            }

            World world = entity.getWorld();
            Location loc = entity.getLocation();
            world.playSound(loc, Sound.BLOCK_BAMBOO_HIT, 100.0f, 1.0f);
        }

        if (damager.hasMetadata("forestSpirit") && entity instanceof Player) {
            Player player = (Player) entity;
            ItemStack axe = player.getInventory().getItemInMainHand();
            int spectral = EffigyUpgradeSystem.getUpgradeLevel(axe, EffigyUpgradeSystem.UpgradeType.SPECTRAL_ARMOR);
            if (spectral > 0) {
                event.setDamage(event.getDamage() * (1 - spectral * 0.10));
            }
        }
    }

    // Event handler for forest spirit death.
    @EventHandler
    public void onForestSpiritDeath(org.bukkit.event.entity.EntityDeathEvent event) {
        if (!(event.getEntity() instanceof Skeleton)) return;
        Skeleton spirit = (Skeleton) event.getEntity();
        if (!spirit.hasMetadata("forestSpirit")) return;

        // Retrieve tier from metadata.
        int tier = spirit.getMetadata("spiritTier").get(0).asInt();

        // Award both generic XP and Forestry XP: 100 * tier each.
        Player killer = spirit.getKiller();
        if (killer != null) {
            event.setDroppedExp(tier * 50);
            xpManager.addXP(killer, "Forestry", 10 * tier);
        }

        // Clear normal drops.
        event.getDrops().clear();

        // Retrieve the spirit type from its custom name.
        String spiritName = getSpiritNameFromEntity(spirit);

        // Drop the rare drop item (100% chance).
        ItemStack rareDrop = rareDropMapping.getOrDefault(spiritName, null);
        if (rareDrop != null) {
            event.getDrops().add(rareDrop);
        }

        // Drop an effigy based on player's notoriety
        if (killer != null) {
            int notoriety = Forestry.getInstance().getNotoriety(killer);
            ItemStack effigy;
            if (notoriety < 300) {
                // Choose common/uncommon/rare
                List<ItemStack> list = List.of(
                        ItemRegistry.getOakEffigy(),
                        ItemRegistry.getBirchEffigy(),
                        ItemRegistry.getSpruceEffigy(),
                        ItemRegistry.getAcaciaEffigy()
                );
                effigy = list.get(new Random().nextInt(list.size()));
            } else {
                // Choose rare/epic/legendary
                List<ItemStack> list = List.of(
                        ItemRegistry.getAcaciaEffigy(),
                        ItemRegistry.getDarkOakEffigy(),
                        ItemRegistry.getCrimsonEffigy(),
                        ItemRegistry.getWarpedEffigy()
                );
                effigy = list.get(new Random().nextInt(list.size()));
            }
            int amount = 1;
            PlayerMeritManager merit = PlayerMeritManager.getInstance(plugin);
            if (merit.hasPerk(killer.getUniqueId(), "Double Effigies") && new Random().nextDouble() < 0.5) {
                amount = 2;
            }
            for (int i = 0; i < amount; i++) {
                event.getDrops().add(effigy.clone());
            }
        }

        // Bonus drop: Jackhammer if the killer's notoriety is low
        if (killer != null) {
            int notoriety = Forestry.getInstance().getNotoriety(killer);
            if (notoriety < 100) {
                event.getDrops().add(ItemRegistry.getJackhammer());
            }
        }

        // Death animation: spawn a falling sapling (using DEAD_BUSH as example) that sinks into the ground.
        Location loc = spirit.getLocation();
        World world = loc.getWorld();
        if (world != null) {
            FallingBlock fallingSapling = world.spawnFallingBlock(loc, Material.DEAD_BUSH.createBlockData());
            fallingSapling.setDropItem(false);
            new BukkitRunnable() {
                int ticks = 0;
                @Override
                public void run() {
                    if (ticks++ >= 20) { // run for 1 second
                        fallingSapling.remove();
                        cancel();
                        return;
                    }
                    fallingSapling.teleport(fallingSapling.getLocation().subtract(0, 0.1, 0));
                }
            }.runTaskTimer(plugin, 0L, 1L);
            world.playSound(loc, Sound.BLOCK_WOOD_BREAK, 1.0f, 1.0f);
            world.spawnParticle(Particle.EXPLOSION_LARGE, loc, 1);
        }
    }

    // Helper method to extract the spirit name from its custom name.
    private String getSpiritNameFromEntity(Skeleton spirit) {
        String name = spirit.getCustomName();
        if (name != null && name.contains("] ")) {
            return name.substring(name.indexOf("] ") + 2);
        }
        return "";
    }
}
