package goat.minecraft.minecraftnew.subsystems.enchanting;

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
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.inventory.ItemStack;
import org.bukkit.event.block.Action;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.*;

public class UltimateEnchantmentListener implements Listener {

    private final JavaPlugin plugin;

    // Maps: playerUUID -> Map<ultimateEnchantName, nextUsableTimestampInMillis>
    // e.g. If nextUsableTimestampInMillis > System.currentTimeMillis(), the player is still on cooldown
    private final Map<UUID, Map<String, Long>> ultimateCooldowns = new HashMap<>();
    private final Map<UUID, Map<String, Integer>> activeEnchantments = new HashMap<>();
    private final Map<UUID, Boolean> discSeekerActive = new HashMap<>();
    private final Map<UUID, Boolean> hammerActive = new HashMap<>();
    private final Map<UUID, Boolean> treecapitatorActive = new HashMap<>();
    public void activateTreecapitator(Player player) {
        UUID playerUUID = player.getUniqueId();
        treecapitatorActive.put(playerUUID, true);
        player.sendMessage(ChatColor.GREEN + "Treecapitator activated! The next log you break will break connected wood blocks and leaves.");
    }

    public void activateDiscSeeker(Player player) {
        UUID playerUUID = player.getUniqueId();
        discSeekerActive.put(playerUUID, true);
        player.sendMessage(ChatColor.GREEN + "Disc Seeker activated! The next Creeper you kill will drop a random music disc.");
    }
    private void breakSurroundingBlocks(Player player, Block centerBlock, boolean vertical) {
        int range = 1; // 3x3 radius

        for (int x = -range; x <= range; x++) {
            for (int y = -range; y <= range; y++) {
                for (int z = -range; z <= range; z++) {
                    if (x == 0 && y == 0 && z == 0) continue; // Skip the center block

                    Block relativeBlock;

                    if (vertical) {
                        // Break vertically
                        relativeBlock = centerBlock.getRelative(x, y, z);
                    } else {
                        // Break horizontally
                        relativeBlock = centerBlock.getRelative(x, z, y);
                    }

                    // Check if the block is breakable
                    if (relativeBlock.getType().isSolid()) {
                        // Handle the block break
                        breakBlock(player, relativeBlock);
                    }
                }
            }
        }
    }

    private void breakBlock(Player player, Block block) {
        // Get the player's tool
        ItemStack tool = player.getInventory().getItemInMainHand();

        // Drop the block's natural drops
        for (ItemStack itemStack : block.getDrops(tool)) {
            block.getWorld().dropItemNaturally(block.getLocation(), itemStack);
        }

        // Set the block to air (break it)
        block.setType(Material.AIR);
    }

    private boolean isLeafBlock(Material material) {
        return material == Material.OAK_LEAVES || material == Material.BIRCH_LEAVES || material == Material.SPRUCE_LEAVES
                || material == Material.JUNGLE_LEAVES || material == Material.ACACIA_LEAVES || material == Material.DARK_OAK_LEAVES
                || material == Material.NETHER_WART_BLOCK || material == Material.WARPED_WART_BLOCK;
    }
    private boolean isWoodBlock(Material material) {
        return material == Material.OAK_LOG || material == Material.BIRCH_LOG || material == Material.SPRUCE_LOG
                || material == Material.JUNGLE_LOG || material == Material.ACACIA_LOG || material == Material.DARK_OAK_LOG
                || material == Material.CRIMSON_STEM || material == Material.WARPED_STEM;
    }

    private void breakConnectedWoodAndLeaves(Player player, Block startBlock) {
        Queue<Block> queue = new ArrayDeque<>();
        Set<Block> visited = new HashSet<>();
        int maxBlocks = 400;
        int leavesRange = 5;

        queue.add(startBlock);
        visited.add(startBlock);

        while (!queue.isEmpty() && visited.size() <= maxBlocks) {
            Block currentBlock = queue.poll();

            // Break the current block
            breakBlock(player, currentBlock);

            // Check adjacent blocks
            for (int x = -1; x <= 1; x++) {
                for (int y = -1; y <= 1; y++) {
                    for (int z = -1; z <= 1; z++) {
                        if (x == 0 && y == 0 && z == 0) continue; // Skip the current block

                        Block adjacentBlock = currentBlock.getRelative(x, y, z);

                        // Check if the adjacent block is a wood block and hasn't been visited
                        if (isWoodBlock(adjacentBlock.getType()) && !visited.contains(adjacentBlock)) {
                            queue.add(adjacentBlock);
                            visited.add(adjacentBlock);
                        }
                    }
                }
            }
        }

        // Break leaves within 5 blocks of any broken wood block
        for (Block woodBlock : visited) {
            for (int x = -leavesRange; x <= leavesRange; x++) {
                for (int y = -leavesRange; y <= leavesRange; y++) {
                    for (int z = -leavesRange; z <= leavesRange; z++) {
                        Block leafBlock = woodBlock.getRelative(x, y, z);

                        // Check if the block is a leaf and hasn't been visited
                        if (isLeafBlock(leafBlock.getType()) && !visited.contains(leafBlock)) {
                            breakBlock(player, leafBlock);
                            visited.add(leafBlock);
                        }
                    }
                }
            }
        }
    }
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        UUID playerUUID = player.getUniqueId();
        if (treecapitatorActive.containsKey(playerUUID) && treecapitatorActive.get(playerUUID)) {
            Block brokenBlock = event.getBlock();

            // Check if the broken block is a log
            if (isWoodBlock(brokenBlock.getType())) {
                // Break connected wood blocks and related leaves
                breakConnectedWoodAndLeaves(player, brokenBlock);

                // Deactivate the Treecapitator effect
                treecapitatorActive.remove(playerUUID);
            }
        }
        // Check if the Hammer effect is active for the player
        if (hammerActive.containsKey(playerUUID) && hammerActive.get(playerUUID)) {
            Block brokenBlock = event.getBlock();
            BlockFace blockFace = event.getBlock().getFace(brokenBlock);

            // Determine the direction of the block break
            boolean isVerticalBreak = blockFace == BlockFace.UP || blockFace == BlockFace.DOWN;

            // Break surrounding blocks
            breakSurroundingBlocks(player, brokenBlock, isVerticalBreak);

            // Deactivate the Hammer effect
            hammerActive.remove(playerUUID);
        }
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
        Material randomDisc = musicDiscs[random.nextInt(musicDiscs.length)];
        return new ItemStack(randomDisc);
    }
    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        if (event.getEntity() instanceof Creeper) {
            Creeper creeper = (Creeper) event.getEntity();
            Player killer = creeper.getKiller();

            if (killer != null) {
                UUID playerUUID = killer.getUniqueId();

                // Check if the Disc Seeker effect is active for the player
                if (discSeekerActive.containsKey(playerUUID) && discSeekerActive.get(playerUUID)) {
                    // Drop a random music disc
                    ItemStack randomMusicDisc = getRandomMusicDisc();
                    creeper.getWorld().dropItemNaturally(creeper.getLocation(), randomMusicDisc);

                    // Deactivate the Disc Seeker effect
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
        }.runTaskLater(plugin, 10L); // 10 ticks = 0.5 seconds
    }
    public UltimateEnchantmentListener(JavaPlugin plugin) {
        this.plugin = plugin;
        // Attempt to load any previously saved cooldown data
        loadCooldowns();
    }

    private void applyInfernoEffect(Player player, LivingEntity target, int level) {
        // Define the total duration and damage
        int totalDurationTicks = 40 * level; // e.g., 2 seconds per level
        double damagePerTick = 5.0 * level; // Damage scales with enchantment level

        // Schedule a repeating task to apply damage over time
        new BukkitRunnable() {
            int elapsedTicks = 0;

            @Override
            public void run() {
                if (target.isDead()) {
                    this.cancel();
                    return;
                }

                // Apply damage
                target.damage(damagePerTick, player);
                player.sendMessage(ChatColor.GOLD + "Inferno Blade burns " + target.getName() + " for " + damagePerTick + " damage!");

                // Spawn custom colored particles (yellow/gold/red)
                target.getWorld().spawnParticle(Particle.FLAME, target.getLocation().add(0, 1, 0), 20, 0.5, 0.5, 0.5, 0.02);
                target.getWorld().spawnParticle(Particle.LAVA, target.getLocation().add(0, 1, 0), 10, 0.3, 0.3, 0.3, 0.01);
                target.getWorld().spawnParticle(Particle.SMOKE_NORMAL, target.getLocation().add(0, 1, 0), 10, 0.3, 0.3, 0.3, 0.01);

                elapsedTicks++;
                if (elapsedTicks >= totalDurationTicks) {
                    player.sendMessage(ChatColor.RED + "Inferno Blade's burn effect has worn off on " + target.getName() + ".");
                    this.cancel();
                }
            }
        }.runTaskTimer(plugin, 0L, 1L); // Run every tick (1L)
    }
    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Player && event.getDamager() instanceof Monster || event.getDamager() instanceof Projectile) {
            if (event.getEntity() instanceof Player) {
                Player player = (Player) event.getEntity();
                UUID playerUUID = player.getUniqueId();

                if (defenseActive.containsKey(playerUUID) && defenseActive.get(playerUUID)) {
                    event.setCancelled(true);
                    defenseActive.remove(playerUUID);

                    // Knockback the attacker
                    Monster attacker = (Monster) event.getDamager();
                    attacker.setHealth(attacker.getHealth() / 2);
                    Vector knockbackDirection = attacker.getLocation().toVector().subtract(player.getLocation().toVector()).normalize();
                    knockbackDirection.multiply(0.5); // Adjust knockback strength
                    knockbackDirection.setY(0.3); // Add vertical knockback to make it look more dramatic
                    attacker.setVelocity(knockbackDirection);

                    // Apply slowness to the attacker
                    attacker.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 120, 1)); // Slowness 6 for 6 seconds

                    player.sendMessage(ChatColor.GREEN + "You parried!");
                }
            }
        }
        // Check if the damager is a player
        if (!(event.getDamager() instanceof Player)) return;
        Player player = (Player) event.getDamager();

        UUID playerUUID = player.getUniqueId();

        // Check if the player has "inferno blade" active with charges
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

        // Apply the Inferno Blade effect
        LivingEntity target = (LivingEntity) event.getEntity();

        // Apply Wither-like effect: rapid fire tick damage
        applyInfernoEffect(player, target, playerEnchants.get("Ultimate: Inferno"));

        // Decrement charges
        playerEnchants.put("Ultimate: Inferno", charges - 1);
        if (playerEnchants.get("Ultimate: Inferno") <= 0) {
            playerEnchants.remove("Ultimate: Inferno");
            player.sendMessage(ChatColor.RED + "Ultimate: Inferno effect has ended.");
            if (playerEnchants.isEmpty()) {
                activeEnchantments.remove(playerUUID);
            }
        }
    }
    public void launchPlayerForward(Player player) {
        // Get the player's current location and direction
        Vector direction = player.getLocation().getDirection().normalize();

        // Multiply the direction vector by 8 to cover 8 blocks
        Vector launchVector = direction.multiply(3);

        // Apply the launch vector to the player's velocity
        player.setVelocity(launchVector);

        // Send a message to the player
        player.sendMessage(ChatColor.GREEN + "You have been launched forward!");
    }
    /**
     * Detect right-click with an item in hand that has an Ultimate Enchantment
     */
    public void activateHammer(Player player) {
        UUID playerUUID = player.getUniqueId();
        hammerActive.put(playerUUID, true);
        player.sendMessage(ChatColor.GREEN + "Hammer effect activated! The next block you break will break surrounding blocks.");
    }
    public void fireHomingArrows(Player player) {
        // Define the spread angle for the shotgun pattern
        double spreadAngle = 5.0; // Degrees

        // Use a BukkitRunnable to fire the arrows with a delay
        new BukkitRunnable() {
            int arrowCount = 0;

            @Override
            public void run() {
                if (arrowCount >= 5) {
                    this.cancel(); // Stop the task after firing 5 arrows
                    return;
                }

                // Calculate the offset angle for each arrow
                double angleOffset = (arrowCount - 2) * spreadAngle; // Center arrow has an offset of 0

                // Create a new direction vector with the offset angle
                Vector direction = player.getLocation().getDirection().clone();
                Vector rotatedDirection = rotateVectorAroundYAxis(direction, angleOffset);

                // Launch the arrow with the adjusted direction
                Arrow arrow = player.launchProjectile(Arrow.class);
                arrow.setVelocity(rotatedDirection.multiply(1.5)); // Adjust velocity as needed

                // Schedule a task to activate the homing effect after 0.2 seconds (4 ticks)
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (arrow.isDead() || arrow.isOnGround()) {
                            this.cancel(); // Cancel the task if the arrow is no longer valid
                            return;
                        }

                        // Find the nearest living entity to home in on
                        List<Entity> nearbyEntities = arrow.getNearbyEntities(20, 20, 20);
                        LivingEntity target = null;
                        double closestDistance = Double.MAX_VALUE;

                        for (Entity entity : nearbyEntities) {
                            if (entity instanceof LivingEntity && entity != player) {
                                double distance = entity.getLocation().distance(arrow.getLocation());
                                if (distance < closestDistance) {
                                    closestDistance = distance;
                                    target = (LivingEntity) entity;
                                }
                            }
                        }

                        // If a target is found, adjust the arrow's velocity towards the target
                        if (target != null) {
                            // Add a vertical offset to the target's location to make the arrow launch downward
                            Vector targetLocationWithOffset = target.getLocation().toVector().add(new Vector(0, 1, 0));
                            Vector direction = targetLocationWithOffset.subtract(arrow.getLocation().toVector()).normalize();
                            arrow.setVelocity(direction.multiply(3)); // Adjust velocity as needed
                        }
                    }
                }.runTaskLater(plugin, 4); // 4 ticks = 0.2 seconds

                arrowCount++;
            }
        }.runTaskTimer(plugin, 0L, 10L); // 10 ticks = 0.5 seconds
    }

    /**
     * Rotates a vector around the Y-axis by the specified angle in degrees.
     *
     * @param vector The vector to rotate.
     * @param angle  The angle in degrees.
     * @return The rotated vector.
     */
    private Vector rotateVectorAroundYAxis(Vector vector, double angle) {
        double radians = Math.toRadians(angle);
        double x = vector.getX() * Math.cos(radians) - vector.getZ() * Math.sin(radians);
        double z = vector.getX() * Math.sin(radians) + vector.getZ() * Math.cos(radians);
        return new Vector(x, vector.getY(), z);
    }
    private void fireLeapingArrowWithSlowness(Player player) {
        // Create a new arrow projectile
        Arrow arrow = player.launchProjectile(Arrow.class);

        // Set the arrow's velocity to make it leap forward
        Vector direction = player.getLocation().getDirection().normalize();
        arrow.setVelocity(direction.multiply(2)); // Adjust the multiplier for desired speed

        // Apply Slowness 100 effect to the arrow
        PotionEffect slownessEffect = new PotionEffect(PotionEffectType.SLOW, 20 * 60 * 30, 99); // 30 minutes, level 100
        arrow.addCustomEffect(slownessEffect, true);

        // Apply Jump Boost effect to the arrow
        PotionEffect jumpBoostEffect = new PotionEffect(PotionEffectType.JUMP, 20 * 60 * 30, 254); // 30 minutes, level 255
        arrow.addCustomEffect(jumpBoostEffect, true);

        // Send a message to the player
        player.sendMessage(ChatColor.GREEN + "You fired a legshot!");
    }
    private void fireDamageArrow(Player player) {
        // Create a new arrow projectile
        Arrow arrow = player.launchProjectile(Arrow.class);

        // Set the arrow to deal 100 damage
        arrow.setDamage(100);

        // Optionally, you can add effects to the arrow, such as slowness
        arrow.addCustomEffect(new PotionEffect(PotionEffectType.SLOW, 20 * 60 * 30, 100), true); // Slowness for 30 minutes

        // Send a message to the player
        player.sendMessage(ChatColor.GREEN + "You fired a powerful arrow that deals 100 damage!");
    }

    @EventHandler
    public void onPlayerRightClick(PlayerInteractEvent event) {
        // Check if it's a right-click on air or block


        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null) return;

        // Attempt to read the Ultimate Enchantment from this item
        CustomEnchantmentManager.UltimateEnchantmentData ueData = CustomEnchantmentManager.getUltimateEnchantment(item);
        if (ueData == null) {
            // Not an item with an Ultimate Enchantment
            return;
        }
        String enchantName = ueData.getName().toLowerCase();
        long cooldownMs = 0;
        if (event.getAction() == Action.LEFT_CLICK_AIR) {
            if (isOnCooldown(player.getUniqueId(), ueData.getName())) {
                long timeLeft = getCooldownTimeLeft(player.getUniqueId(), ueData.getName());
                return;
            }
            switch (enchantName) {
                case "homing arrows":
                    fireHomingArrows(player);
                    cooldownMs = 5_000L;
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

        if(event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {// Check cooldown
            if (isOnCooldown(player.getUniqueId(), ueData.getName())) {
                long timeLeft = getCooldownTimeLeft(player.getUniqueId(), ueData.getName());
                player.sendMessage(ChatColor.RED + "This Ultimate Enchantment is on cooldown for "
                        + (timeLeft / 1000) + "s!");
                return;
            }




            switch (enchantName) {
                case "hammer":
                    activateHammer(player);
                    cooldownMs = 0_100L;
                    break;
                case "treecapitator":
                    activateTreecapitator(player);
                    cooldownMs = 1_000L;
                    break;
                case "excavate":
                    player.playSound(player.getLocation(), Sound.BLOCK_BEACON_POWER_SELECT, 1.0f, 1.0f);
                    player.addPotionEffect(new PotionEffect(PotionEffectType.FAST_DIGGING, 20 * 40, 4)); // Speed II
                    cooldownMs = 120_000L;
                    break;
                case "leap":
                    player.playSound(player.getLocation(), Sound.ENTITY_GOAT_LONG_JUMP, 1.0f, 1.0f);
                    launchPlayerForward(player);
                    player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 20 * 4, 255)); // Speed II
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
                    // Apply potion effects for 30 seconds
                    player.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 600, 1)); // Strength II
                    player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 600, 1)); // Speed II
                    player.addPotionEffect(new PotionEffect(PotionEffectType.FAST_DIGGING, 600, 1)); // Speed II
                    // 45 seconds cooldown for Inferno Blade
                    cooldownMs = 120_000L;
                    break;
                case "warp":
                    Vector direction = player.getLocation().getDirection().normalize();
                    Vector offset = direction.multiply(10);
                    player.teleport(player.getLocation().add(offset));
                    player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0F, 1.0f);
                    cooldownMs = 0_100L;
                    break;
                case "disc seeker":
                    activateDiscSeeker(player);
                    cooldownMs = 120_000L;
                    // Deduct 2 saturation if possible, otherwise take 2 hunger

                    // Send a message to the player confirming the teleport
                    player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 10, 10);
                    break;
                case "inferno":
                    activateInfernoBlade(player, ueData.getLevel());
                    player.sendMessage(ChatColor.GREEN + "Your Inferno Blade has been activated! Your next 3 attacks will set enemies ablaze!");
                    cooldownMs = 30_000L; // Cooldown: 60 seconds
                    setPlayerCooldown(player.getUniqueId(), ueData.getName(), cooldownMs);
                    saveCooldowns();
                    break;
                case "snowstorm":
                    activateSnowstorm(player, 1);
                    cooldownMs = 35_000L; // Cooldown: 60 seconds
                    setPlayerCooldown(player.getUniqueId(), ueData.getName(), cooldownMs);
                    saveCooldowns();
                    break;

                default:
                    cooldownMs = 1L;
                    break;
            }
        }

        // Put the player on cooldown for this ultimate enchant
        setPlayerCooldown(player.getUniqueId(), ueData.getName(), cooldownMs);

        // Save cooldowns to file so server reloads won't wipe them
        saveCooldowns();
    }
    private void activateSnowstorm(Player player, int level) {
        UUID playerUUID = player.getUniqueId();

        // Define the radius of the snowstorm effect
        int radius = 16 + level; // Radius scales with enchantment level

        // Get all nearby entities within the radius
        for (Entity entity : player.getNearbyEntities(radius, radius, radius)) {
            if (entity instanceof LivingEntity && entity instanceof Monster && !(entity instanceof Boss)) {
                LivingEntity monster = (LivingEntity) entity;

                // Apply Slowness V effect for 10 seconds
                monster.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 200, 100));
                monster.setHealth(monster.getHealth() / 2);

                // Set freezing ticks to a high value (e.g., 200 ticks)
                monster.setFreezeTicks(2000);

                // Spawn snow particles around the monster
                monster.getWorld().spawnParticle(Particle.SNOWBALL, monster.getLocation().add(0, 1, 0), 50, 0.5, 0.5, 0.5, 0.1);
            }
        }

        // Spawn snow particles around the player
        player.getWorld().spawnParticle(Particle.SNOWBALL, player.getLocation(), 100, 0, 1, 0, 0.1);

        // Play a sound effect to indicate the snowstorm activation
        player.playSound(player.getLocation(), Sound.BLOCK_GLASS_BREAK, 1.0f, 0.5f);

        // Send a message to the player
        player.sendMessage(ChatColor.AQUA + "Snowstorm activated! Nearby monsters are frozen!");
    }

    private void activateInfernoBlade(Player player, int level) {
        UUID playerUUID = player.getUniqueId();
        activeEnchantments.putIfAbsent(playerUUID, new HashMap<>());
        activeEnchantments.get(playerUUID).put("Ultimate: Inferno", 3); // 3 charges
    }
    /**
     * Checks if the given player's ultimate enchant is currently on cooldown.
     */
    private boolean isOnCooldown(UUID playerUUID, String ultimateName) {
        if (!ultimateCooldowns.containsKey(playerUUID)) {
            return false;
        }
        Long nextUseTime = ultimateCooldowns.get(playerUUID).get(ultimateName.toLowerCase());
        return nextUseTime != null && System.currentTimeMillis() < nextUseTime;
    }

    /**
     * Returns how many milliseconds are left until the cooldown finishes.
     */
    private long getCooldownTimeLeft(UUID playerUUID, String ultimateName) {
        if (!ultimateCooldowns.containsKey(playerUUID)) {
            return 0;
        }
        Long nextUseTime = ultimateCooldowns.get(playerUUID).get(ultimateName.toLowerCase());
        if (nextUseTime == null) return 0;
        return nextUseTime - System.currentTimeMillis();
    }

    /**
     * Sets the player's cooldown for the given ultimateName for [cooldownMs] time from now.
     */
    private void setPlayerCooldown(UUID playerUUID, String ultimateName, long cooldownMs) {
        ultimateCooldowns.putIfAbsent(playerUUID, new HashMap<>());
        long nextUseTime = System.currentTimeMillis() + cooldownMs;
        ultimateCooldowns.get(playerUUID).put(ultimateName.toLowerCase(), nextUseTime);
    }

    /**
     * Loads cooldown data from the plugin's config (or a dedicated file).
     */
    private void loadCooldowns() {
        // For simplicity, we store cooldowns in the plugin config; you could also use a dedicated file.
        FileConfiguration config = plugin.getConfig();
        if (!config.contains("ultimate-cooldowns")) {
            return;
        }

        // Structure example:
        // ultimate-cooldowns:
        //   <playerUUID>:
        //     <ultimateName>: <timestampInMillis>

        // We iterate over keys in "ultimate-cooldowns"
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

    /**
     * Saves current cooldowns to the plugin's config, so they persist across server restarts.
     */
    public void saveCooldowns() {
        FileConfiguration config = plugin.getConfig();

        // Wipe old data
        if (config.contains("ultimate-cooldowns")) {
            config.set("ultimate-cooldowns", null);
        }

        // Rebuild it from memory
        for (Map.Entry<UUID, Map<String, Long>> entry : ultimateCooldowns.entrySet()) {
            String uuidStr = entry.getKey().toString();
            Map<String, Long> enchantMap = entry.getValue();
            for (Map.Entry<String, Long> enchantEntry : enchantMap.entrySet()) {
                String enchantName = enchantEntry.getKey();
                long nextUseTime = enchantEntry.getValue();
                config.set("ultimate-cooldowns." + uuidStr + "." + enchantName, nextUseTime);
            }
        }

        // Save to file
        plugin.saveConfig();
    }
}
