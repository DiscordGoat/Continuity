package goat.minecraft.minecraftnew.subsystems.enchanting;

import goat.minecraft.minecraftnew.MinecraftNew;
import goat.minecraft.minecraftnew.subsystems.forestry.ForestSpiritManager;
import goat.minecraft.minecraftnew.utils.XPManager;
import net.minecraft.world.entity.monster.IMonster;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.inventory.ItemStack;
import org.bukkit.event.block.Action;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.*;

import static goat.minecraft.minecraftnew.subsystems.enchanting.UltimateEnchantmentListener.OreUtils.isOreBlock;


public class UltimateEnchantmentListener implements Listener {

    private final JavaPlugin plugin;

    // Maps: playerUUID -> Map<ultimateEnchantName, nextUsableTimestampInMillis>
    private final Map<UUID, Map<String, Long>> ultimateCooldowns = new HashMap<>();
    private final Map<UUID, Map<String, Integer>> activeEnchantments = new HashMap<>();
    private final Map<UUID, Boolean> discSeekerActive = new HashMap<>();
    // Removed hammerActive and treecapitatorActive

    // Removed activateTreecapitator(...) and activateHammer(...)

    public UltimateEnchantmentListener(JavaPlugin plugin) {
        this.plugin = plugin;
        loadCooldowns();
    }

    /**
     * Checks whether a block is considered an ore.
     * This example just checks if the Material name ends with "_ORE".
     * Customize as needed for deepslate ores, ancient debris, etc.
     */

    /**
     * Example check for whether the player's item is "Hammer" enchanted.
     * If you store your custom enchant differently, adjust the logic accordingly.
     */
    private boolean hasHammerEnchant(ItemStack item) {
        // Simplest check: item’s display name or lore contains “Hammer”
        // Or you could check for a custom enchant in your system.
        if (item == null) return false;
        // Example check:
        return item.getItemMeta() != null && item.getItemMeta().hasLore()
                && item.getItemMeta().getLore().stream()
                .anyMatch(l -> l.toLowerCase().contains("hammer"));
    }

    /**
     * Example check for whether the player's item is “Treecapitator” enchanted.
     */
    private boolean hasTreecapEnchant(ItemStack item) {
        if (item == null) return false;
        return item.getItemMeta() != null && item.getItemMeta().hasLore()
                && item.getItemMeta().getLore().stream()
                .anyMatch(l -> l.toLowerCase().contains("treecapitator"));
    }

    /**
     * Break a single block and drop its loot.
     * If it's not an ore, we’ll also apply tool durability usage (so Hammer costs durability).
     */
    private void breakBlock(Player player, Block block, boolean consumeDurabilityIfNotOre) {
        ItemStack tool = player.getInventory().getItemInMainHand();
        // Drop the block's natural drops
        for (ItemStack drop : block.getDrops(tool)) {
            if (drop != null && drop.getType() != Material.AIR) { // Check for null or air
                block.getWorld().dropItemNaturally(block.getLocation(), drop);
            }
        }
        // Set the block to air
        if(isLeafBlock(block.getType())){
            block.setType(Material.AIR);
            return;
        }
        block.setType(Material.AIR);

        // If it's not an ore, consume 1 durability (Hammer usage).
        // (Check if the tool can lose durability)

        if (consumeDurabilityIfNotOre && tool != null && tool.getType().getMaxDurability() > 0) {
            short currentDamage = tool.getDurability();
            int unbreakingLevel = tool.getEnchantmentLevel(Enchantment.DURABILITY); // Get Unbreaking level
            double chanceToNotUseDurability = unbreakingLevel * 0.15; // 15% chance per level
            if (Math.random() > chanceToNotUseDurability) { // Spend durability only if random value exceeds the chance
                tool.setDurability((short) (currentDamage + 1));

                // Optional: If the tool breaks, remove it
                if (tool.getDurability() >= tool.getType().getMaxDurability()) {
                    tool.setAmount(0); // Break the tool
                    player.playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 1f, 1f);
                }
            }
        }
    }


    /**
     * Break surrounding 3×3 blocks ignoring ores, applying durability for each broken block.
     * @param centerBlock  The block the player actually broke.
     * @param vertical     If true, break in x-y-z with y as vertical axis. Otherwise horizontally.
     */
    private void breakSurroundingBlocks(Player player, Block centerBlock, boolean vertical) {
        int range = 1; // 1 in each direction -> 3×3 area
        for (int x = -range; x <= range; x++) {
            for (int y = -range; y <= range; y++) {
                for (int z = -range; z <= range; z++) {
                    // Skip the center block itself
                    if (x == 0 && y == 0 && z == 0) continue;

                    Block relativeBlock;
                    if (vertical) {
                        relativeBlock = centerBlock.getRelative(x, y, z);
                    } else {
                        relativeBlock = centerBlock.getRelative(x, z, y);
                    }

                    if (!relativeBlock.getType().isAir() && relativeBlock.getType().isSolid()) {
                        // Check if the block is an ore
                        boolean isOre = isOreBlock(relativeBlock);

                        // Debug messages
                        Bukkit.getLogger().info("[DEBUG] Checking block: " + relativeBlock.getType() + " at " + relativeBlock.getLocation());
                        Bukkit.getLogger().info("[DEBUG] Is ore: " + isOre);

                        // Skip ores
                        if (isOre) {
                            Bukkit.getLogger().info("[DEBUG] Skipping block: " + relativeBlock.getType() + " (ore detected).");
                            continue;
                        }

                        // Break the block if it's not an ore
                        Bukkit.getLogger().info("[DEBUG] Breaking block: " + relativeBlock.getType() + " at " + relativeBlock.getLocation());
                        breakBlock(player, relativeBlock, true);
                        XPManager xpManager = new XPManager(plugin);
                        xpManager.addXP(player, "Mining", 1);
                    }
                }
            }
        }
    }


    /**
     * Determine if a material is a log or wood block (for Treecapitator).
     */
    private boolean isWoodBlock(Material material) {
        return material == Material.OAK_LOG || material == Material.BIRCH_LOG ||
                material == Material.SPRUCE_LOG || material == Material.JUNGLE_LOG ||
                material == Material.ACACIA_LOG || material == Material.DARK_OAK_LOG ||
                material == Material.CRIMSON_STEM || material == Material.WARPED_STEM;
    }

    private boolean isLeafBlock(Material material) {
        return material == Material.OAK_LEAVES || material == Material.BIRCH_LEAVES ||
                material == Material.SPRUCE_LEAVES || material == Material.JUNGLE_LEAVES ||
                material == Material.ACACIA_LEAVES || material == Material.DARK_OAK_LEAVES ||
                material == Material.NETHER_WART_BLOCK || material == Material.WARPED_WART_BLOCK;
    }

    /**
     * Treecapitator logic: BFS to break connected logs and nearby leaves.
     */
    private void breakConnectedWoodAndLeaves(Player player, Block startBlock) {
        Queue<Block> queue = new ArrayDeque<>();
        Set<Block> visited = new HashSet<>();
        Set<Block> tempVisited = new HashSet<>(); // Temporary set to store blocks during iteration
        int maxBlocks = 400;
        int leavesRange = 3;

        queue.add(startBlock);
        visited.add(startBlock);

        while (!queue.isEmpty() && visited.size() <= maxBlocks) {
            Block currentBlock = queue.poll();
            // Break the current log block (consume durability for each) and give XP
            breakBlock(player, currentBlock, true);
            givePlayerXp(player);  // Gives 1 XP to the player for each wood block broken
            ForestSpiritManager forestSpiritManager = ForestSpiritManager.getInstance(MinecraftNew.getInstance());
            Random random = new Random();
            if (random.nextInt(100) < 4) { // 1% chance
                if (isWoodBlock(currentBlock.getType())) {
                    forestSpiritManager.spawnSpirit(currentBlock.getType(), player.getLocation(), player);
                    player.sendMessage(ChatColor.LIGHT_PURPLE + "A Forest Spirit has been summoned!");
                }
            }

            // Check adjacent blocks
            for (int x = -1; x <= 1; x++) {
                for (int y = -1; y <= 1; y++) {
                    for (int z = -1; z <= 1; z++) {
                        if (x == 0 && y == 0 && z == 0) continue;
                        Block adjacentBlock = currentBlock.getRelative(x, y, z);

                        if (isWoodBlock(adjacentBlock.getType()) && !visited.contains(adjacentBlock)) {
                            queue.add(adjacentBlock);
                            tempVisited.add(adjacentBlock);  // Add to temporary set instead of directly to visited
                        }
                    }
                }
            }
        }

        visited.addAll(tempVisited); // Add all at once after the iteration of the queue is complete

        // Break leaves within 5 blocks range but do not give XP
        for (Block woodBlock : visited) {
            for (int x = -leavesRange; x <= leavesRange; x++) {
                for (int y = -leavesRange; y <= leavesRange; y++) {
                    for (int z = -leavesRange; z <= leavesRange; z++) {
                        Block leafBlock = woodBlock.getRelative(x, y, z);
                        if (isLeafBlock(leafBlock.getType()) && !visited.contains(leafBlock)) {
                            breakBlock(player, leafBlock, true);
                            // No XP given for breaking leaf blocks
                            // Do not modify 'visited' during this nested iteration
                        }
                    }
                }
            }
        }
    }



    private void givePlayerXp(Player player) {
        XPManager xpManager = new XPManager(plugin);
        xpManager.addXP(player, "Forestry", 8);
    }


    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block brokenBlock = event.getBlock();
        ItemStack tool = player.getInventory().getItemInMainHand();

        // If the player is sneaking (holding Shift) and the tool has "Treecapitator" ...
        if (player.isSneaking() && hasTreecapEnchant(tool)) {
            // If the broken block is wood
            if (isWoodBlock(brokenBlock.getType())) {
                // Cancel the normal block break drop because we’ll handle it
                event.setCancelled(true);
                // Chop all connected wood
                breakConnectedWoodAndLeaves(player, brokenBlock);
            }
        }

        // If the player is sneaking (holding Shift) and the tool has "Hammer"
        if (player.isSneaking() && hasHammerEnchant(tool)) {
            // Cancel the normal block break drop for the center block
            // We’ll break it ourselves in code below so we can handle ore-skip + durability

            // Break the center block the player actually mined
            boolean isOre = false;

            breakBlock(player, brokenBlock, !isOre);

            // Determine if we’re breaking vertically or horizontally
            BlockFace blockFace = event.getBlock().getFace(brokenBlock);
            boolean isVerticalBreak = (blockFace == BlockFace.UP || blockFace == BlockFace.DOWN);

            // Break surrounding 3×3 ignoring ores and applying durability
            breakSurroundingBlocks(player, brokenBlock, isVerticalBreak);
        }
    }

    // -------------------------------------------------------------------
    // Disc Seeker, Inferno Blade, etc. remain unchanged below...
    // -------------------------------------------------------------------
    public void activateDiscSeeker(Player player) {
        UUID playerUUID = player.getUniqueId();
        discSeekerActive.put(playerUUID, true);
        player.sendMessage(ChatColor.GREEN + "Disc Seeker activated! The next Creeper you kill will drop a random music disc.");
    }
    private ItemStack getRandomMusicDisc() {
        Material[] musicDiscs = {
                Material.MUSIC_DISC_13,
                Material.MUSIC_DISC_CAT,
                Material.MUSIC_DISC_BLOCKS,
                Material.MUSIC_DISC_CHIRP,
                Material.MUSIC_DISC_FAR,
                Material.MUSIC_DISC_MALL,
                Material.MUSIC_DISC_MELLOHI,
                Material.MUSIC_DISC_STAL,
                Material.MUSIC_DISC_STRAD,
                Material.MUSIC_DISC_WARD,
                Material.MUSIC_DISC_11,
                Material.MUSIC_DISC_WAIT
        };

        Random random = new Random();
        return new ItemStack(musicDiscs[random.nextInt(musicDiscs.length)]);
    }
    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        if (event.getEntity() instanceof Creeper) {
            Creeper creeper = (Creeper) event.getEntity();
            Player killer = creeper.getKiller();

            if (killer != null) {
                UUID playerUUID = killer.getUniqueId();
                if (discSeekerActive.containsKey(playerUUID) && discSeekerActive.get(playerUUID)) {
                    ItemStack randomMusicDisc = getRandomMusicDisc();
                    creeper.getWorld().dropItemNaturally(creeper.getLocation(), randomMusicDisc);
                    discSeekerActive.remove(playerUUID);
                    killer.sendMessage(ChatColor.GOLD + "You got a music disc from the Creeper!");
                }
            }
        }
    }

    private final Map<UUID, Boolean> defenseActive = new HashMap<>();
    public void activateDefenseMechanism(Player player) {
        UUID playerUUID = player.getUniqueId();
        defenseActive.put(playerUUID, true);
        new BukkitRunnable() {
            @Override
            public void run() {
                defenseActive.remove(playerUUID);
            }
        }.runTaskLater(plugin, 10L);
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        // Defense parry logic, Inferno Blade, etc. remain as-is
        if (event.getEntity() instanceof Player &&
                (event.getDamager() instanceof Monster || event.getDamager() instanceof Projectile)) {
            Player player = (Player) event.getEntity();
            UUID playerUUID = player.getUniqueId();

            if (defenseActive.containsKey(playerUUID) && defenseActive.get(playerUUID)) {
                event.setCancelled(true);
                defenseActive.remove(playerUUID);

                if (event.getDamager() instanceof Monster) {
                    Monster attacker = (Monster) event.getDamager();
                    attacker.setHealth(attacker.getHealth() / 2);
                    Vector knockbackDirection = attacker.getLocation().toVector().subtract(player.getLocation().toVector()).normalize();
                    knockbackDirection.multiply(0.5);
                    knockbackDirection.setY(0.3);
                    attacker.setVelocity(knockbackDirection);
                    attacker.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 120, 1));
                }
                player.sendMessage(ChatColor.GREEN + "You parried!");
            }
        }

        // Check if the damager is a player
        if (!(event.getDamager() instanceof Player)) return;
        Player player = (Player) event.getDamager();
        UUID playerUUID = player.getUniqueId();

        if (!activeEnchantments.containsKey(playerUUID)) return;
        Map<String, Integer> playerEnchants = activeEnchantments.get(playerUUID);
        if (!playerEnchants.containsKey("Ultimate: Inferno")) return;

        int charges = playerEnchants.get("Ultimate: Inferno");
        if (charges <= 0) {
            playerEnchants.remove("Ultimate: Inferno");
            if (playerEnchants.isEmpty()) {
                activeEnchantments.remove(playerUUID);
            }
            return;
        }

        LivingEntity target = (LivingEntity) event.getEntity();
        applyInfernoEffect(player, target, playerEnchants.get("Ultimate: Inferno"));
        playerEnchants.put("Ultimate: Inferno", charges - 1);
        if (playerEnchants.get("Ultimate: Inferno") <= 0) {
            playerEnchants.remove("Ultimate: Inferno");
            player.sendMessage(ChatColor.RED + "Ultimate: Inferno effect has ended.");
            if (playerEnchants.isEmpty()) {
                activeEnchantments.remove(playerUUID);
            }
        }
    }

    private void applyInfernoEffect(Player player, LivingEntity target, int level) {
        int totalDurationTicks = 40 * level;
        double damagePerTick = 5.0 * level;

        new BukkitRunnable() {
            int elapsedTicks = 0;
            @Override
            public void run() {
                if (target.isDead()) {
                    this.cancel();
                    return;
                }
                target.damage(damagePerTick, player);
                //player.sendMessage(ChatColor.GOLD + "Inferno Blade burns " + target.getName() + " for " + damagePerTick + " damage!");
                target.getWorld().spawnParticle(Particle.FLAME, target.getLocation().add(0, 1, 0), 20, 0.5, 0.5, 0.5, 0.02);
                target.getWorld().spawnParticle(Particle.LAVA, target.getLocation().add(0, 1, 0), 10, 0.3, 0.3, 0.3, 0.01);
                target.getWorld().spawnParticle(Particle.SMOKE_NORMAL, target.getLocation().add(0, 1, 0), 10, 0.3, 0.3, 0.3, 0.01);
                elapsedTicks++;
                if (elapsedTicks >= totalDurationTicks) {
                    player.sendMessage(ChatColor.RED + "Inferno Blade's burn effect has worn off on " + target.getName() + ".");
                    this.cancel();
                }
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    // The rest of your ultimate enchant code for arrows, parry, snowstorm, etc. remain as-is...
    // Note that we removed the "hammer" and "treecapitator" cases from onPlayerRightClick below:

    @EventHandler
    public void onPlayerRightClick(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null) return;

        CustomEnchantmentManager.UltimateEnchantmentData ueData = CustomEnchantmentManager.getUltimateEnchantment(item);
        if (ueData == null) return;

        String enchantName = ueData.getName().toLowerCase();
        long cooldownMs = 0;

        // Example: left-click logic, etc. (Hammer/Treecap removed)
        if (event.getAction() == Action.LEFT_CLICK_AIR) {
            if (isOnCooldown(player.getUniqueId(), ueData.getName())) {
                long timeLeft = getCooldownTimeLeft(player.getUniqueId(), ueData.getName());
                return;
            }

            switch (enchantName) {
                case "homing arrows":
                    fireHomingArrows(player);
                    cooldownMs = 15_000L;
                    break;
                case "leg shot":
                    fireLeapingArrowWithSlowness(player);
                    cooldownMs = 5_000L;
                    break;
                case "headshot":
                    fireDamageArrow(player);
                    cooldownMs = 15_000L;
                    break;
                default:
                    cooldownMs = 1L;
                    break;
            }
        }

        // Right-click logic, with Hammer/Treecap removed
        if(event.getAction() == Action.RIGHT_CLICK_AIR) {
            if (!event.getHand().equals(EquipmentSlot.HAND) && event.getHand()!=null) {
                return; // Ignore offhand interactions
            }
            if (isOnCooldown(player.getUniqueId(), ueData.getName())) {
                long timeLeft = getCooldownTimeLeft(player.getUniqueId(), ueData.getName());
                player.sendMessage(ChatColor.RED + "This Ultimate Enchantment is on cooldown for "
                        + (timeLeft / 1000) + "s!");
                return;
            }
            switch (enchantName) {
                // Hammer/Treecapitator removed. No cooldown for them.
                case "excavate":
                    player.playSound(player.getLocation(), Sound.BLOCK_BEACON_POWER_SELECT, 1.0f, 1.0f);
                    player.addPotionEffect(new PotionEffect(PotionEffectType.FAST_DIGGING, 20 * 40, 4));
                    cooldownMs = 120_000L;
                    break;
                case "leap":
                    player.playSound(player.getLocation(), Sound.ENTITY_GOAT_LONG_JUMP, 1.0f, 1.0f);
                    launchPlayerForward(player);
                    player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 20 * 4, 255));
                    cooldownMs = 5_000L;
                    break;
                case "parry":
                    player.playSound(player.getLocation(), Sound.BLOCK_POWDER_SNOW_STEP, 1.0f, 1.0f);
                    activateDefenseMechanism(player);
                    cooldownMs = 1_000L;
                    break;
                case "rage mode":
                    player.sendMessage(ChatColor.GREEN + "You become enraged!");
                    player.playSound(player.getLocation(), Sound.ENTITY_WARDEN_HEARTBEAT, 1.0f, 1.0f);
                    player.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 600, 1)); // 30s
                    player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 600, 1));
                    player.addPotionEffect(new PotionEffect(PotionEffectType.FAST_DIGGING, 600, 1));
                    cooldownMs = 120_000L;
                    break;
                case "warp":
                    Vector direction = player.getLocation().getDirection().normalize();
                    Vector offset = direction.multiply(8);
                    player.teleport(player.getLocation().add(offset));
                    player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0F, 1.0f);
                    cooldownMs = 1_000L;
                    break;
                case "disc seeker":
                    activateDiscSeeker(player);
                    cooldownMs = 120_000L;
                    player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 10, 10);
                    break;
                case "inferno":
                    activateInfernoBlade(player, ueData.getLevel());
                    player.sendMessage(ChatColor.GREEN + "Your Inferno Blade has been activated! Your next 3 attacks will set enemies ablaze!");
                    cooldownMs = 30_000L;
                    setPlayerCooldown(player.getUniqueId(), ueData.getName(), cooldownMs);
                    saveCooldowns();
                    break;
                case "snowstorm":
                    activateSnowstorm(player, 1);
                    cooldownMs = 35_000L;
                    setPlayerCooldown(player.getUniqueId(), ueData.getName(), cooldownMs);
                    saveCooldowns();
                    break;
                default:
                    cooldownMs = 1L;
                    break;
            }
        }

        // Put the player on cooldown for everything else except hammer/treecap
        setPlayerCooldown(player.getUniqueId(), ueData.getName(), cooldownMs);
        saveCooldowns();
    }

    private void activateSnowstorm(Player player, int level) {
        int radius = 16 + level;
        for (Entity entity : player.getNearbyEntities(radius, radius, radius)) {
            if (entity instanceof LivingEntity && entity instanceof Monster && !(entity instanceof Boss)) {
                LivingEntity monster = (LivingEntity) entity;
                monster.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 200, 100));
                monster.setHealth(monster.getHealth() / 2);
                monster.setFreezeTicks(2000);
                monster.getWorld().spawnParticle(Particle.SNOWBALL, monster.getLocation().add(0, 1, 0),
                        50, 0.5, 0.5, 0.5, 0.1);
            }
        }
        player.getWorld().spawnParticle(Particle.SNOWBALL, player.getLocation(),
                100, 0, 1, 0, 0.1);
        player.playSound(player.getLocation(), Sound.BLOCK_GLASS_BREAK, 1.0f, 0.5f);
        player.sendMessage(ChatColor.AQUA + "Snowstorm activated! Nearby monsters are frozen!");
    }

    private void activateInfernoBlade(Player player, int level) {
        UUID playerUUID = player.getUniqueId();
        activeEnchantments.putIfAbsent(playerUUID, new HashMap<>());
        activeEnchantments.get(playerUUID).put("Ultimate: Inferno", 3); // 3 charges
    }

    // Homing arrows etc. remain unchanged
    public void fireHomingArrows(Player player) {
        double spreadAngle = 5.0;
        new BukkitRunnable() {
            int arrowCount = 0;
            @Override
            public void run() {
                if (arrowCount >= 5) {
                    this.cancel();
                    return;
                }
                double angleOffset = (arrowCount - 2) * spreadAngle;
                Vector direction = player.getLocation().getDirection().clone();
                Vector rotatedDirection = rotateVectorAroundYAxis(direction, angleOffset);
                Arrow arrow = player.launchProjectile(Arrow.class);
                arrow.setVelocity(rotatedDirection.multiply(1.5));
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (arrow.isDead() || arrow.isOnGround()) {
                            this.cancel();
                            return;
                        }
                        List<Entity> nearbyEntities = arrow.getNearbyEntities(20, 20, 20);
                        LivingEntity target = null;
                        double closestDistance = Double.MAX_VALUE;
                        for (Entity e : nearbyEntities) {
                            if (e instanceof LivingEntity && e != player) {
                                double distance = e.getLocation().distance(arrow.getLocation());
                                if (distance < closestDistance) {
                                    closestDistance = distance;
                                    target = (LivingEntity) e;
                                }
                            }
                        }
                        if (target != null) {
                            Vector targetVec = target.getLocation().toVector().add(new Vector(0, 1, 0));
                            Vector dir = targetVec.subtract(arrow.getLocation().toVector()).normalize();
                            arrow.setVelocity(dir.multiply(3));
                        }
                    }
                }.runTaskLater(plugin, 4);
                arrowCount++;
            }
        }.runTaskTimer(plugin, 0L, 10L);
    }
    private Vector rotateVectorAroundYAxis(Vector vector, double angle) {
        double radians = Math.toRadians(angle);
        double x = vector.getX() * Math.cos(radians) - vector.getZ() * Math.sin(radians);
        double z = vector.getX() * Math.sin(radians) + vector.getZ() * Math.cos(radians);
        return new Vector(x, vector.getY(), z);
    }
    private void fireLeapingArrowWithSlowness(Player player) {
        Arrow arrow = player.launchProjectile(Arrow.class);
        Vector direction = player.getLocation().getDirection().normalize();
        arrow.setVelocity(direction.multiply(2));
        PotionEffect slownessEffect = new PotionEffect(PotionEffectType.SLOW, 20 * 60 * 30, 99);
        arrow.addCustomEffect(slownessEffect, true);
        PotionEffect jumpBoostEffect = new PotionEffect(PotionEffectType.JUMP, 20 * 60 * 30, 254);
        arrow.addCustomEffect(jumpBoostEffect, true);
        player.sendMessage(ChatColor.GREEN + "You fired a legshot!");
    }
    private void fireDamageArrow(Player player) {
        Arrow arrow = player.launchProjectile(Arrow.class);
        arrow.setDamage(100);
        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_WORK_FLETCHER, 10.f, 1.0f);
    }
    public void launchPlayerForward(Player player) {
        Vector direction = player.getLocation().getDirection().normalize();
        Vector launchVector = direction.multiply(3);
        player.setVelocity(launchVector);
        player.sendMessage(ChatColor.GREEN + "You have been launched forward!");
    }

    // Cooldown / config saving logic (unchanged)
    private boolean isOnCooldown(UUID playerUUID, String ultimateName) {
        if (!ultimateCooldowns.containsKey(playerUUID)) {
            return false;
        }
        Long nextUseTime = ultimateCooldowns.get(playerUUID).get(ultimateName.toLowerCase());
        return nextUseTime != null && System.currentTimeMillis() < nextUseTime;
    }
    private long getCooldownTimeLeft(UUID playerUUID, String ultimateName) {
        if (!ultimateCooldowns.containsKey(playerUUID)) {
            return 0;
        }
        Long nextUseTime = ultimateCooldowns.get(playerUUID).get(ultimateName.toLowerCase());
        if (nextUseTime == null) return 0;
        return nextUseTime - System.currentTimeMillis();
    }
    private void setPlayerCooldown(UUID playerUUID, String ultimateName, long cooldownMs) {
        ultimateCooldowns.putIfAbsent(playerUUID, new HashMap<>());
        long nextUseTime = System.currentTimeMillis() + cooldownMs;
        ultimateCooldowns.get(playerUUID).put(ultimateName.toLowerCase(), nextUseTime);
    }
    private void loadCooldowns() {
        FileConfiguration config = plugin.getConfig();
        if (!config.contains("ultimate-cooldowns")) {
            return;
        }
        for (String uuidStr : config.getConfigurationSection("ultimate-cooldowns").getKeys(false)) {
            UUID uuid;
            try {
                uuid = UUID.fromString(uuidStr);
            } catch (IllegalArgumentException e) {
                continue;
            }
            Map<String, Long> map = new HashMap<>();
            for (String enchantName : config.getConfigurationSection("ultimate-cooldowns." + uuidStr).getKeys(false)) {
                long nextUseTime = config.getLong("ultimate-cooldowns." + uuidStr + "." + enchantName);
                map.put(enchantName, nextUseTime);
            }
            ultimateCooldowns.put(uuid, map);
        }
    }
    public void saveCooldowns() {
        FileConfiguration config = plugin.getConfig();
        if (config.contains("ultimate-cooldowns")) {
            config.set("ultimate-cooldowns", null);
        }
        for (Map.Entry<UUID, Map<String, Long>> entry : ultimateCooldowns.entrySet()) {
            String uuidStr = entry.getKey().toString();
            Map<String, Long> enchantMap = entry.getValue();
            for (Map.Entry<String, Long> enchantEntry : enchantMap.entrySet()) {
                String enchantName = enchantEntry.getKey();
                long nextUseTime = enchantEntry.getValue();
                config.set("ultimate-cooldowns." + uuidStr + "." + enchantName, nextUseTime);
            }
        }
        plugin.saveConfig();
    }
    public class OreUtils {

        // A predefined set of all known ores for reliable and fast lookups
        private static final Set<Material> ORES;

        static {
            ORES = new HashSet<>();
            // Overworld Ores
            ORES.add(Material.COAL_ORE);
            ORES.add(Material.IRON_ORE);
            ORES.add(Material.COPPER_ORE);
            ORES.add(Material.GOLD_ORE);
            ORES.add(Material.REDSTONE_ORE);
            ORES.add(Material.LAPIS_ORE);
            ORES.add(Material.DIAMOND_ORE);
            ORES.add(Material.EMERALD_ORE);

            // Deepslate Ores
            ORES.add(Material.DEEPSLATE_COAL_ORE);
            ORES.add(Material.DEEPSLATE_IRON_ORE);
            ORES.add(Material.DEEPSLATE_COPPER_ORE);
            ORES.add(Material.DEEPSLATE_GOLD_ORE);
            ORES.add(Material.DEEPSLATE_REDSTONE_ORE);
            ORES.add(Material.DEEPSLATE_LAPIS_ORE);
            ORES.add(Material.DEEPSLATE_DIAMOND_ORE);
            ORES.add(Material.DEEPSLATE_EMERALD_ORE);

            // Nether Ores
            ORES.add(Material.NETHER_QUARTZ_ORE);
            ORES.add(Material.NETHER_GOLD_ORE);

            // Ancient Debris
            ORES.add(Material.ANCIENT_DEBRIS);
        }

        /**
         * Checks if the given block is an ore.
         * This method uses both a predefined set of ores and a fallback check based on material names.
         *
         * @param block The block to check.
         * @return True if the block is an ore, false otherwise.
         */
        public static boolean isOreBlock(Block block) {
            if (block == null) {
                Bukkit.getLogger().info("[DEBUG] Block is null, returning false.");
                return false;
            }

            Material material = block.getType();

            // Check if the block is in the predefined set
            boolean inSet = ORES.contains(material);
            if (inSet) {
                Bukkit.getLogger().info("[DEBUG] Block " + material + " detected as ore from predefined set.");
                return true;
            }

            // Fallback: Check material name
            String materialName = material.name();
            boolean isOre = materialName.contains("ORE") || materialName.equals("ANCIENT_DEBRIS");

            if (isOre) {
                Bukkit.getLogger().info("[DEBUG] Block " + material + " detected as ore from fallback logic.");
            } else {
                Bukkit.getLogger().info("[DEBUG] Block " + material + " is not an ore.");
            }

            return isOre;
        }

    }

}
