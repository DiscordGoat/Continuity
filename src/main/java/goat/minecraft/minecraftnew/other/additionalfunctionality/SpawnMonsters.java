package goat.minecraft.minecraftnew.other.additionalfunctionality;

import goat.minecraft.minecraftnew.MinecraftNew;
import goat.minecraft.minecraftnew.subsystems.combat.HostilityManager;
import goat.minecraft.minecraftnew.subsystems.combat.KnightMob;
import goat.minecraft.minecraftnew.utils.devtools.XPManager;
import goat.minecraft.minecraftnew.utils.devtools.ItemRegistry;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Random;

public class SpawnMonsters implements Listener {

    private static SpawnMonsters instance; // <-- Singleton instance

    private XPManager xpManager;
    private final JavaPlugin plugin = MinecraftNew.getInstance();

    private static final int MAX_MONSTER_LEVEL = 300;

    // Private constructor so it can’t be called externally
    private SpawnMonsters(XPManager xpManager) {
        this.xpManager = xpManager;
    }

    // The public method to retrieve the singleton instance.
    // You call this once, passing in your XPManager, and from then on
    // you'll just call SpawnMonsters.getInstance(null) (or the same XPManager).
    public static synchronized SpawnMonsters getInstance(XPManager xpManager) {
        if (instance == null) {
            instance = new SpawnMonsters(xpManager);
        }
        return instance;
    }

    // If desired, you can also have a no-arg getInstance() that simply returns instance:
    // public static SpawnMonsters getInstance() {
    //     return instance;
    // }

    public static int getDayCount(Player player) {
        int playTimeTicks = player.getStatistic(Statistic.PLAY_ONE_MINUTE);
        return playTimeTicks / 24000; // 1 Minecraft day = 24000 ticks
    }

    public boolean shouldMutationOccur(int playerHostility) {
        Random random = new Random();
        int randomValue = random.nextInt(100) + 1; // Generate a random number between 1 and 100
        return randomValue <= playerHostility; // Return true if the random number is <= the hostility percentage
    }

    public void applyRandomArmor(LivingEntity entity) {
        if (entity == null) return;

        Random random = new Random();
        int randomValue = random.nextInt(100);

        // Determine the armor set type
        Material baseChestplate;
        if (randomValue < 80) {
            // 80% chance for leather, chainmail, or gold
            Material[] options = {Material.LEATHER_CHESTPLATE, Material.CHAINMAIL_CHESTPLATE, Material.GOLDEN_CHESTPLATE};
            baseChestplate = options[random.nextInt(options.length)];
        } else if (randomValue < 95) {
            // 15% chance for iron
            baseChestplate = Material.IRON_CHESTPLATE;
        } else {
            // 5% chance for diamond
            baseChestplate = Material.DIAMOND_CHESTPLATE;
        }

        // Determine the corresponding helmet, leggings, and boots based on the chestplate
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
                // Default to leather set if somehow an unknown material is selected
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

        // Set the armor on the entity
        EntityEquipment equipment = entity.getEquipment();
        if (equipment != null) {
            equipment.setHelmet(helmetItem);
            equipment.setChestplate(chestplateItem);
            equipment.setLeggings(leggingsItem);
            equipment.setBoots(bootsItem);
        }
    }

    public void equipRandomWeapon(LivingEntity entity) {
        Random random = new Random();
        Material weaponMaterial = random.nextBoolean() ? Material.IRON_SWORD : Material.IRON_SHOVEL;

        EntityEquipment equipment = entity.getEquipment();
        equipment.setItemInMainHand(new ItemStack(weaponMaterial));
    }

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

        if (entity instanceof Creeper) {
            Random random = new Random();
            int randomValue = random.nextInt(100) + 1; // Generate a random number between 1 and 100
            if (randomValue <= 90) { // Removes 90% of creepers
                entity.remove();
            }
        }

        if(entity instanceof LivingEntity monster){
            if(shouldMutationOccur(playerHostility)) {
                monster.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 2, true));
            }
        }

        if(entity instanceof Zombie zombie){
            if(shouldMutationOccur(playerHostility)){
                KnightMob knightMob = new KnightMob(plugin);
                knightMob.transformToKnight(zombie);
                zombie.setCustomName(ChatColor.GRAY + "Knight");
            }
            if(shouldMutationOccur(playerHostility)){
                applyRandomArmor(zombie);
            }
            if(shouldMutationOccur(playerHostility)){
                applyRandomArmor(zombie);
            }
            if(shouldMutationOccur(playerHostility)){
                applyRandomArmor(zombie);
            }
            if(shouldMutationOccur(playerHostility)){
                equipRandomWeapon(zombie);
            }
            if(shouldMutationOccur(playerHostility)){
                enchantArmorWithProtection(zombie);
            }
        }

        if(entity instanceof WitherSkeleton ws){
            if(shouldMutationOccur(playerHostility)){
                KnightMob knightMob = new KnightMob(plugin);
                knightMob.transformToKnight(ws);
                ws.setCustomName(ChatColor.GRAY + "Knight");
            }
            if(shouldMutationOccur(playerHostility)){
                applyRandomArmor(ws);
            }
            if(shouldMutationOccur(playerHostility)){
                applyRandomArmor(ws);
            }
            if(shouldMutationOccur(playerHostility)){
                applyRandomArmor(ws);
            }
            if(shouldMutationOccur(playerHostility)){
                equipRandomWeapon(ws);
            }
            if(shouldMutationOccur(playerHostility)){
                enchantArmorWithProtection(ws);
            }
        }

        if(entity instanceof Creeper creeper){
            if(shouldMutationOccur(playerHostility)){
                creeper.setPowered(true);
            }
        }

        if(entity instanceof Blaze monster){
            if(shouldMutationOccur(playerHostility)){
                monster.setCustomName(ChatColor.RED + "High Flying Blaze");
                monster.addPotionEffect(new PotionEffect(PotionEffectType.LEVITATION, Integer.MAX_VALUE, 1, true));
            }
        }

        if(entity instanceof Drowned monster){
            if(shouldMutationOccur(playerHostility)){
                monster.getEquipment().setItemInMainHand(ItemRegistry.getTrident());
                monster.setCustomName(ChatColor.RED + "Olympic Swimmer");
                monster.addPotionEffect(new PotionEffect(PotionEffectType.DOLPHINS_GRACE, Integer.MAX_VALUE, 3, true));
            }
        }

        if(entity instanceof MagmaCube monster){
            if (shouldMutationOccur(playerHostility)) {
                // Set the size to something bigger
                monster.setSize(4);

                monster.getEquipment().setItemInMainHand(ItemRegistry.getTrident());
                monster.setCustomName(ChatColor.RED + "Giant Cube");
                monster.setCustomNameVisible(true);
            }
        }

        if (entity instanceof Skeleton monster) {
            if (shouldMutationOccur(playerHostility)) {
                monster.setCustomName(ChatColor.RED + "Sniper");
                monster.setCustomNameVisible(true);

                ItemStack sniperHelmet = new ItemStack(Material.DIAMOND_HELMET);
                sniperHelmet.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 2);
                sniperHelmet.addEnchantment(Enchantment.OXYGEN, 1);

                monster.getEquipment().setHelmet(sniperHelmet);
            }
        }

        if(entity instanceof Slime monster){
            if (shouldMutationOccur(playerHostility)) {
                monster.setSize(4);

                monster.getEquipment().setItemInMainHand(ItemRegistry.getTrident());
                monster.setCustomName(ChatColor.RED + "Giant Cube");
                monster.setCustomNameVisible(true);
            }
        }

        Random random = new Random();
        if (entity instanceof Monster) {
            LivingEntity mob = (LivingEntity) entity;

            // Schedule a delayed task to apply attributes
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (!mob.isValid()) {
                        return; // Skip if entity is no longer valid
                    }

                    // Check for SEA_CREATURE metadata after the delay
                    if(mob.getEquipment().getItemInMainHand().equals(ItemRegistry.getSpiritBow())){
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
            }.runTaskLater(plugin, 40L); // Delay 40 ticks (2 seconds)
        }
    }

    public double getDistanceFromOrigin(Entity mob) {
        Location mobLocation = mob.getLocation();
        double x = mobLocation.getX();
        double z = mobLocation.getZ();
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
        int level = 300; // Force EnderDragon level to 300
        dragon.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, Integer.MAX_VALUE, 1, true));
        dragon.addPotionEffect(new PotionEffect(PotionEffectType.HEALTH_BOOST, Integer.MAX_VALUE, 255, true));

        double healthMultiplier = 1 + (level * 0.1);
        double originalHealth = dragon.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue();
        double newHealth = Math.min(originalHealth * healthMultiplier, 2000);
        dragon.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(newHealth);
        dragon.setHealth(newHealth);

        dragon.setMetadata("mobLevel", new FixedMetadataValue(MinecraftNew.getInstance(), level));
        String color = getColorForLevel(level);
        dragon.setCustomName(color + "Level: " + level + " " + formatMobType(dragon.getType().toString()));
        dragon.setCustomNameVisible(true);
        dragon.setRemoveWhenFarAway(true);

        // Override custom name with a more “bossy” name
        dragon.setCustomName(ChatColor.DARK_RED + "Ender Dragon");

        BossBar bossBar = dragon.getBossBar();
        bossBar.setColor(BarColor.RED);
        bossBar.setStyle(BarStyle.SEGMENTED_20);

        dragon.setHealth(dragon.getMaxHealth());
    }

    public void applyMobAttributes(LivingEntity mob, int level) {
        // Clamp the level between 1 and MAX_MONSTER_LEVEL.
        level = Math.max(1, Math.min(level, MAX_MONSTER_LEVEL));

        // Get the health attribute instance.
        AttributeInstance healthAttribute = mob.getAttribute(Attribute.GENERIC_MAX_HEALTH);
        if (healthAttribute != null) {
            // Remove all existing modifiers to prevent stacking.
            for (AttributeModifier modifier : new ArrayList<>(healthAttribute.getModifiers())) {
                healthAttribute.removeModifier(modifier);
            }
            // Reset base health to its default value.
            double defaultHealth = healthAttribute.getDefaultValue();
            healthAttribute.setBaseValue(defaultHealth);
        }

        // Calculate the new health multiplier.
        double healthMultiplier = 1 + (level * 0.1);
        double originalHealth = mob.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue();
        double newHealth = Math.min(originalHealth * healthMultiplier, 2000);

        // Apply the new health value.
        mob.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(newHealth);
        mob.setHealth(newHealth);

        // Set metadata to store the mob's level.
        mob.setMetadata("mobLevel", new FixedMetadataValue(MinecraftNew.getInstance(), level));

        // If the mob is a "Knight" (custom-named in gray), skip renaming.
        String color = getColorForLevel(level);
        if (mob.getCustomName() != null && mob.getCustomName().equals(ChatColor.GRAY + "Knight")) {
            return;
        }

        // Set the custom name with level and mob type.
        mob.setCustomName(ChatColor.GRAY + "[" + color + "Lv: " + level + ChatColor.GRAY + "] " + formatMobType(mob.getType().toString()));
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
