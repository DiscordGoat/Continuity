package goat.minecraft.minecraftnew.subsystems.pets.perks;

import goat.minecraft.minecraftnew.subsystems.pets.PetManager;
import goat.minecraft.minecraftnew.other.skilltree.Skill;
import goat.minecraft.minecraftnew.other.skilltree.SkillTreeManager;
import goat.minecraft.minecraftnew.other.skilltree.Talent;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class SecretLegion implements Listener {

    private final PetManager petManager;
    private final JavaPlugin plugin;

    // Player tracking maps
    private final Map<UUID, Set<PigZombie>> playerPiglins = new ConcurrentHashMap<>();
    private final Map<UUID, Integer> playerAggressionLevel = new ConcurrentHashMap<>();
    private final Map<UUID, Integer> playerDamageCount = new ConcurrentHashMap<>();
    private final Map<UUID, Long> playerLastDamageTime = new ConcurrentHashMap<>();
    private final Map<UUID, Integer> playerTaskIds = new ConcurrentHashMap<>();
    private final Map<UUID, Entity> playerLastAttacker = new ConcurrentHashMap<>();

    // Constants
    private static final int STANDBY_COUNT = 2;  // Aggression 1
    private static final int RETALIATORY_COUNT = 4;  // Aggression 2
    private static final int DESPERATE_COUNT = 8;  // Aggression 3
    private static final int PIGLIN_LIFETIME = 20 * 60 * 5;  // 5 minutes in ticks
    private static final int AGGRESSION_RESET_TIME = 20 * 60;  // 1 minute in ticks
    private static final int RETALIATORY_DURATION = 20 * 60 * 2;  // 2 minutes in ticks
    private static final double TELEPORT_DISTANCE = 30.0;  // For aggression 3
    private static final double DETECTION_RANGE = 20.0;

    public SecretLegion(JavaPlugin plugin) {
        this.petManager = PetManager.getInstance(plugin);
        this.plugin = plugin;

        // Schedule task to check player-piglin distances and update formations
        plugin.getServer().getScheduler().runTaskTimer(plugin, this::updatePiglinPositions, 10L, 10L);
    }

    @EventHandler
    public void onPlayerDamaged(EntityDamageByEntityEvent event) {
        // Check if the entity being damaged is a player
        if (!(event.getEntity() instanceof Player player)) return;

        // Check if player has the SECRET_LEGION perk
        PetManager.Pet activePet = petManager.getActivePet(player);
        if (activePet == null || !activePet.hasPerk(PetManager.PetPerk.SECRET_LEGION)) return;

        // Get player UUID
        UUID playerUUID = player.getUniqueId();

        // Get the attacker
        Entity attacker = event.getDamager();
        if (!(attacker instanceof LivingEntity)) return;  // Only living entities trigger aggression increase

        // Store the attacker for targeting
        playerLastAttacker.put(playerUUID, attacker);

        // Record damage time
        long currentTime = System.currentTimeMillis();
        playerLastDamageTime.put(playerUUID, currentTime);

        // Update aggression level based on player state
        int currentAggression = playerAggressionLevel.getOrDefault(playerUUID, 0);

        if (currentAggression == 0) {
            // First damage, initialize to level 1 if needed
            initializeSecretLegion(player);
        }

        if (currentAggression == 2) {
            // Player is already in retaliatory mode, increment damage counter
            int damageCount = playerDamageCount.getOrDefault(playerUUID, 0) + 1;
            playerDamageCount.put(playerUUID, damageCount);

            // Check if we need to escalate to desperate mode
            if (damageCount >= 5) {
                setAggressionLevel(player, 3);
            }
        } else if (currentAggression < 2) {
            // Set to retaliatory mode
            setAggressionLevel(player, 2);

            // Reset damage counter
            playerDamageCount.put(playerUUID, 1);
        }

        int talent = 0;
        if (SkillTreeManager.getInstance() != null) {
            talent = SkillTreeManager.getInstance()
                    .getTalentLevel(player.getUniqueId(), Skill.TAMING, Talent.SECRET_LEGION);
        }
        if (talent == 0) {
            // Consume some saturation as a cost
            player.setSaturation(Math.max(0, player.getSaturation() - 1));
        }
    }

    /**
     * Initialize the Secret Legion for a player if not already done
     */
    private void initializeSecretLegion(Player player) {
        UUID playerUUID = player.getUniqueId();

        // Check if player already has piglins
        if (playerPiglins.containsKey(playerUUID)) {
            return;
        }

        // Create new piglin set
        playerPiglins.put(playerUUID, new HashSet<>());

        // Set initial aggression level
        setAggressionLevel(player, 1);

        // Schedule cleanup task
        int taskId = plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            // Check if it's time to reset aggression to standby
            long lastDamage = playerLastDamageTime.getOrDefault(playerUUID, 0L);
            long currentTime = System.currentTimeMillis();

            if (currentTime - lastDamage > 60000) { // 1 minute without damage
                setAggressionLevel(player, 1);
            }
        }, 20L, 20L).getTaskId();

        playerTaskIds.put(playerUUID, taskId);
    }

    /**
     * Set the player's aggression level and update piglin count
     */
    private void setAggressionLevel(Player player, int level) {
        UUID playerUUID = player.getUniqueId();
        int currentLevel = playerAggressionLevel.getOrDefault(playerUUID, 0);
        Set<PigZombie> piglins = playerPiglins.getOrDefault(playerUUID, new HashSet<>());

        // Don't downgrade from level 3 to 2
        if (currentLevel == 3 && level == 2) {
            return;
        }

        // Update the level
        playerAggressionLevel.put(playerUUID, level);

        // Clear damage counter if moving to level 1
        if (level == 1) {
            playerDamageCount.put(playerUUID, 0);
        }

        // Get target count
        int targetCount = switch (level) {
            case 1 -> STANDBY_COUNT;
            case 2 -> RETALIATORY_COUNT;
            case 3 -> DESPERATE_COUNT;
            default -> 0;
        };

        // Adjust piglin count
        if (piglins.size() < targetCount) {
            // Need to spawn more piglins
            int toSpawn = targetCount - piglins.size();
            for (int i = 0; i < toSpawn; i++) {
                spawnPiglin(player, level);
            }
        } else if (piglins.size() > targetCount) {
            // Need to remove some piglins
            int toRemove = piglins.size() - targetCount;
            Iterator<PigZombie> iterator = piglins.iterator();
            for (int i = 0; i < toRemove && iterator.hasNext(); i++) {
                PigZombie piglin = iterator.next();
                piglin.remove();
                iterator.remove();
            }
        }

        // Play effect based on aggression level
        switch (level) {
            case 1 -> player.playSound(player.getLocation(), Sound.ENTITY_PIGLIN_AMBIENT, 1.0f, 1.0f);
            case 2 -> player.playSound(player.getLocation(), Sound.ENTITY_PIGLIN_ANGRY, 1.0f, 1.0f);
            case 3 -> {
                player.playSound(player.getLocation(), Sound.ENTITY_PIGLIN_CONVERTED_TO_ZOMBIFIED, 1.0f, 0.8f);
                player.playSound(player.getLocation(), Sound.ENTITY_WITHER_AMBIENT, 0.5f, 1.2f);
            }
        }

        // Notify the player
        String message = switch (level) {
            case 1 -> ChatColor.GOLD + "Your Secret Legion flanks you, ready to strike.";
            case 2 -> ChatColor.GOLD + "Your Secret Legion divides to conquer!";
            case 3 -> ChatColor.RED + "Your Secret Legion enters berserker mode!";
            default -> "";
        };

        if (!message.isEmpty()) {
            player.sendMessage(message);
        }

        // Set behavior for all piglins
        updatePiglinBehavior(player, level);
    }

    /**
     * Spawn a new piglin for the player
     */
    private PigZombie spawnPiglin(Player player, int aggressionLevel) {
        UUID playerUUID = player.getUniqueId();
        Set<PigZombie> piglins = playerPiglins.getOrDefault(playerUUID, new HashSet<>());

        // Calculate spawn position
        Location spawnLoc = getSpawnLocation(player, piglins.size());

        // Spawn the piglin
        PigZombie piglin = (PigZombie) player.getWorld().spawnEntity(
                spawnLoc,
                EntityType.ZOMBIFIED_PIGLIN
        );

        // Set basic properties
        piglin.setCustomName(getNameForPiglin(aggressionLevel, piglins.size()));
        piglin.setCustomNameVisible(true);
        piglin.setAngry(aggressionLevel > 1);
        piglin.setRemoveWhenFarAway(false);

        // Equip the piglin
        equipPiglin(piglin, aggressionLevel);

        // Apply effects
        applyPiglinEffects(piglin, aggressionLevel);

        // Add to tracking set
        piglins.add(piglin);
        playerPiglins.put(playerUUID, piglins);

        // Schedule removal
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (piglin.isValid()) {
                piglin.remove();
                Set<PigZombie> currentPiglins = playerPiglins.getOrDefault(playerUUID, new HashSet<>());
                currentPiglins.remove(piglin);

                // If no piglins left, clean up player data
                if (currentPiglins.isEmpty()) {
                    playerPiglins.remove(playerUUID);
                    playerAggressionLevel.remove(playerUUID);
                    playerDamageCount.remove(playerUUID);
                    playerLastDamageTime.remove(playerUUID);
                    playerLastAttacker.remove(playerUUID);

                    // Cancel task
                    Integer taskId = playerTaskIds.remove(playerUUID);
                    if (taskId != null) {
                        plugin.getServer().getScheduler().cancelTask(taskId);
                    }
                }
            }
        }, PIGLIN_LIFETIME);

        return piglin;
    }

    /**
     * Get a spawn location for a new piglin
     */
    private Location getSpawnLocation(Player player, int index) {
        Location playerLoc = player.getLocation();
        double angle = Math.PI * 2 * (index / 8.0); // Evenly distribute around player
        double distance = 2.0;

        double x = Math.sin(angle) * distance;
        double z = Math.cos(angle) * distance;

        return playerLoc.clone().add(x, 0, z);
    }

    /**
     * Get name for piglin based on aggression level and index
     */
    private String getNameForPiglin(int aggressionLevel, int index) {
        String baseColor = switch (aggressionLevel) {
            case 1 -> ChatColor.GOLD.toString();
            case 2 -> ChatColor.YELLOW.toString();
            case 3 -> ChatColor.RED.toString();
            default -> ChatColor.WHITE.toString();
        };

        String role = "";
        if (aggressionLevel == 2) {
            // In retaliatory mode, first 2 are guards, rest are scouts
            role = (index < 2) ? " Guard" : " Scout";
        } else if (aggressionLevel == 3) {
            role = " Berserker";
        }

        return baseColor + "Legion" + role;
    }

    /**
     * Equip a piglin based on aggression level
     */
    private void equipPiglin(PigZombie piglin, int aggressionLevel) {
        // Base equipment
        piglin.getEquipment().setHelmet(new ItemStack(Material.GOLDEN_HELMET));
        piglin.getEquipment().setChestplate(new ItemStack(Material.GOLDEN_CHESTPLATE));
        piglin.getEquipment().setLeggings(new ItemStack(Material.GOLDEN_LEGGINGS));
        piglin.getEquipment().setBoots(new ItemStack(Material.GOLDEN_BOOTS));

        // Weapon based on aggression
        Material weaponMaterial = switch (aggressionLevel) {
            case 1 -> Material.GOLDEN_AXE;
            case 2 -> Material.GOLDEN_SWORD;
            case 3 -> Material.NETHERITE_SWORD;
            default -> Material.GOLDEN_SWORD;
        };

        piglin.getEquipment().setItemInMainHand(new ItemStack(weaponMaterial));

        // Set all drop chances to 0
        piglin.getEquipment().setHelmetDropChance(0.0f);
        piglin.getEquipment().setChestplateDropChance(0.0f);
        piglin.getEquipment().setLeggingsDropChance(0.0f);
        piglin.getEquipment().setBootsDropChance(0.0f);
        piglin.getEquipment().setItemInMainHandDropChance(0.0f);
    }

    /**
     * Apply effects to a piglin based on aggression level
     */
    private void applyPiglinEffects(PigZombie piglin, int aggressionLevel) {
        // Speed effect (increased with aggression)
        int speedLevel = Math.min(aggressionLevel, 2); // Max Speed III
        piglin.addPotionEffect(new PotionEffect(
                PotionEffectType.SPEED,
                PIGLIN_LIFETIME,
                speedLevel,
                false,
                false
        ));

        // Strength effect for higher aggression
        if (aggressionLevel >= 2) {
            piglin.addPotionEffect(new PotionEffect(
                    PotionEffectType.STRENGTH,
                    PIGLIN_LIFETIME,
                    aggressionLevel - 2, // Level 0 for aggr 2, Level 1 for aggr 3
                    false,
                    false
            ));
        }

        // Fire resistance for desperate mode
        if (aggressionLevel == 3) {
            piglin.addPotionEffect(new PotionEffect(
                    PotionEffectType.FIRE_RESISTANCE,
                    PIGLIN_LIFETIME,
                    0,
                    false,
                    false
            ));

            // Add resistance effect for berserkers
            piglin.addPotionEffect(new PotionEffect(
                    PotionEffectType.RESISTANCE,
                    PIGLIN_LIFETIME,
                    1,
                    false,
                    false
            ));
        }
    }

    /**
     * Update behavior for all piglins of a player
     */
    private void updatePiglinBehavior(Player player, int aggressionLevel) {
        UUID playerUUID = player.getUniqueId();
        Set<PigZombie> piglins = playerPiglins.getOrDefault(playerUUID, new HashSet<>());
        Entity lastAttacker = playerLastAttacker.get(playerUUID);

        int index = 0;
        for (PigZombie piglin : piglins) {
            if (aggressionLevel == 1) {
                // Aggression 1: Pigmen flank the player looking cool
                piglin.setAngry(false);
                piglin.setTarget(null);
            } else if (aggressionLevel == 2) {
                // Aggression 2: 2 pigmen attack the entity that attacked the player, 2 attack other nearby monsters
                piglin.setAngry(true);

                if (index < 2 && lastAttacker instanceof LivingEntity) {
                    // First 2 target the attacker
                    piglin.setTarget((LivingEntity) lastAttacker);
                } else {
                    // Other 2 find nearby monsters
                    findAndTargetNearbyMonster(player, piglin);
                }
            } else if (aggressionLevel == 3) {
                // Aggression 3: All pigmen attack nearby enemies but teleport back if too far
                piglin.setAngry(true);
                findAndTargetNearbyMonster(player, piglin);
            }

            index++;
        }
    }

    /**
     * Find a nearby monster and set it as the piglin's target
     */
    private void findAndTargetNearbyMonster(Player player, PigZombie piglin) {
        List<Entity> nearbyEntities = player.getNearbyEntities(DETECTION_RANGE, DETECTION_RANGE, DETECTION_RANGE);

        // Filter to only include monsters
        List<Monster> monsters = new ArrayList<>();
        for (Entity entity : nearbyEntities) {
            if (entity instanceof Monster && !(entity instanceof PigZombie)) {
                monsters.add((Monster) entity);
            }
        }

        if (monsters.isEmpty()) return;

        // Find the closest monster
        Monster closest = null;
        double closestDistance = Double.MAX_VALUE;

        for (Monster monster : monsters) {
            double distance = monster.getLocation().distance(player.getLocation());
            if (distance < closestDistance) {
                closestDistance = distance;
                closest = monster;
            }
        }

        if (closest != null) {
            piglin.setTarget(closest);
        }
    }

    /**
     * Update piglin positions, teleport if necessary, and maintain formations
     */
    private void updatePiglinPositions() {
        for (Map.Entry<UUID, Set<PigZombie>> entry : playerPiglins.entrySet()) {
            UUID playerUUID = entry.getKey();
            Player player = plugin.getServer().getPlayer(playerUUID);

            if (player == null || !player.isOnline()) {
                // Player offline, clean up piglins
                cleanupPlayerPiglins(playerUUID);
                continue;
            }

            int aggressionLevel = playerAggressionLevel.getOrDefault(playerUUID, 1);
            Set<PigZombie> piglins = entry.getValue();

            int index = 0;
            for (PigZombie piglin : piglins) {
                if (!piglin.isValid()) continue;

                if (aggressionLevel == 1) {
                    // Aggression 1: Cool flanking formation
                    updateFlankingPosition(player, piglin, index, piglins.size());
                } else if (aggressionLevel == 3) {
                    // Aggression 3: Teleport back if too far from player
                    double distance = piglin.getLocation().distance(player.getLocation());
                    if (distance > TELEPORT_DISTANCE) {
                        // Teleport piglin near player
                        Location teleportLoc = player.getLocation().clone().add(
                                Math.random() * 4 - 2,
                                0,
                                Math.random() * 4 - 2
                        );

                        piglin.teleport(teleportLoc);

                        // Play teleport sound
                        player.getWorld().playSound(teleportLoc, Sound.ENTITY_ENDERMAN_TELEPORT, 0.3f, 1.5f);

                        // Reset target to find a new nearby monster
                        findAndTargetNearbyMonster(player, piglin);
                    }
                }

                // In standby mode, make piglins look at nearby monsters for the cool factor
                if (aggressionLevel == 1) {
                    List<Entity> nearbyEntities = piglin.getNearbyEntities(10, 10, 10);
                    for (Entity entity : nearbyEntities) {
                        if (entity instanceof Monster && !(entity instanceof PigZombie)) {
                            // Make piglin look at monster without attacking
                            Vector direction = entity.getLocation().toVector().subtract(piglin.getLocation().toVector()).normalize();
                            Location lookLoc = piglin.getLocation().clone();
                            lookLoc.setDirection(direction);
                            piglin.teleport(lookLoc);
                            break;
                        }
                    }
                }

                index++;
            }
        }
    }

    /**
     * Update the position of a piglin in flanking formation
     */
    private void updateFlankingPosition(Player player, PigZombie piglin, int index, int totalPiglins) {
        // Calculate cool flanking position
        Location targetLoc = getFlankingPosition(player, index, totalPiglins);

        // Only teleport if piglin is too far from desired position
        if (piglin.getLocation().distance(targetLoc) > 3.0) {
            piglin.teleport(targetLoc);
        } else {
            // Otherwise just update the direction to look cool
            Vector direction = getFlankingDirection(player, index, totalPiglins);
            Location lookLoc = piglin.getLocation().clone();
            lookLoc.setDirection(direction);
            piglin.teleport(lookLoc);
        }
    }

    /**
     * Calculate flanking position for a piglin
     */
    private Location getFlankingPosition(Player player, int index, int totalPiglins) {
        Location playerLoc = player.getLocation();
        Vector playerDir = playerLoc.getDirection().normalize();

        // Create a perpendicular vector for the flanking formation
        Vector perpendicular = new Vector(-playerDir.getZ(), 0, playerDir.getX()).normalize();

        // Calculate position based on index
        double angle = (index % 2 == 0) ? 1.0 : -1.0;
        double distance = 2.5 + (index / 2) * 0.5; // Staggered distance

        Vector offset = playerDir.clone().multiply(-distance);
        offset.add(perpendicular.clone().multiply(angle * 1.5));

        return playerLoc.clone().add(offset);
    }

    /**
     * Calculate flanking direction for a piglin
     */
    private Vector getFlankingDirection(Player player, int index, int totalPiglins) {
        // Piglins should look slightly outward to appear alert
        Vector playerDir = player.getLocation().getDirection().normalize();
        Vector perpendicular = new Vector(-playerDir.getZ(), 0, playerDir.getX()).normalize();

        // Angle based on which side they're on
        double angle = (index % 2 == 0) ? 0.3 : -0.3;

        Vector direction = playerDir.clone();
        direction.add(perpendicular.clone().multiply(angle)).normalize();

        return direction;
    }

    /**
     * Clean up all piglins for a player
     */
    private void cleanupPlayerPiglins(UUID playerUUID) {
        Set<PigZombie> piglins = playerPiglins.remove(playerUUID);

        if (piglins != null) {
            for (PigZombie piglin : piglins) {
                if (piglin.isValid()) {
                    piglin.remove();
                }
            }
        }

        // Clean up all player data
        playerAggressionLevel.remove(playerUUID);
        playerDamageCount.remove(playerUUID);
        playerLastDamageTime.remove(playerUUID);
        playerLastAttacker.remove(playerUUID);

        // Cancel task
        Integer taskId = playerTaskIds.remove(playerUUID);
        if (taskId != null) {
            plugin.getServer().getScheduler().cancelTask(taskId);
        }
    }

    // Clean up all remaining piglins on plugin disable
    public void cleanupSummonedPiglins() {
        for (Set<PigZombie> piglins : playerPiglins.values()) {
            for (PigZombie piglin : piglins) {
                if (piglin.isValid()) {
                    piglin.remove();
                }
            }
        }

        // Clear all data
        playerPiglins.clear();
        playerAggressionLevel.clear();
        playerDamageCount.clear();
        playerLastDamageTime.clear();
        playerLastAttacker.clear();

        // Cancel all tasks
        for (Integer taskId : playerTaskIds.values()) {
            plugin.getServer().getScheduler().cancelTask(taskId);
        }
        playerTaskIds.clear();
    }

    // Additional event handler to help with targeting
    @EventHandler
    public void onEntityTarget(EntityTargetEvent event) {
        if (!(event.getEntity() instanceof PigZombie piglin)) return;

        // Check if this is one of our piglins
        for (Set<PigZombie> piglins : playerPiglins.values()) {
            if (piglins.contains(piglin)) {
                // Don't let standby piglins target anything automatically
                Player owner = findPiglinOwner(piglin);
                if (owner != null) {
                    int aggressionLevel = playerAggressionLevel.getOrDefault(owner.getUniqueId(), 1);
                    if (aggressionLevel == 1) {
                        event.setCancelled(true);
                    }
                }
                break;
            }
        }
    }

    /**
     * Find the owner of a piglin
     */
    private Player findPiglinOwner(PigZombie piglin) {
        for (Map.Entry<UUID, Set<PigZombie>> entry : playerPiglins.entrySet()) {
            if (entry.getValue().contains(piglin)) {
                return plugin.getServer().getPlayer(entry.getKey());
            }
        }
        return null;
    }
}