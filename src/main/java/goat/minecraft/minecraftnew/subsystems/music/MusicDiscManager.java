package goat.minecraft.minecraftnew.subsystems.music;

import goat.minecraft.minecraftnew.MinecraftNew;
import goat.minecraft.minecraftnew.subsystems.mining.Mining;
import goat.minecraft.minecraftnew.subsystems.mining.PlayerOxygenManager;
import goat.minecraft.minecraftnew.subsystems.pets.PetManager;
import goat.minecraft.minecraftnew.subsystems.utils.CustomItemManager;
import goat.minecraft.minecraftnew.subsystems.utils.XPManager;
import goat.minecraft.minecraftnew.subsystems.villagers.VillagerTradeManager;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.block.Jukebox;
import org.bukkit.block.data.BlockData;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.EulerAngle;

import java.util.*;

import static goat.minecraft.minecraftnew.subsystems.fishing.SeaCreatureRegistry.createAlchemyItem;
import static goat.minecraft.minecraftnew.subsystems.utils.CustomItemManager.createCustomItem;


public class MusicDiscManager implements Listener {

    private final JavaPlugin plugin;

    // Constructor to pass the main plugin instance
    public MusicDiscManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Event handler for player interactions.
     * Detects when a player right-clicks a jukebox.
     */
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        // Check if the action is a right-click on a block
        if (event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
            Block clickedBlock = event.getClickedBlock();

            if (clickedBlock != null && clickedBlock.getType() == Material.JUKEBOX) {
                Jukebox jukebox = (Jukebox) clickedBlock.getState();
                event.setCancelled(true);

                Player player = event.getPlayer();
                ItemStack heldItem = player.getInventory().getItemInMainHand();

                // Check if the player is holding a music disc
                if (isMusicDisc(heldItem)) {
                    Material discType = heldItem.getType();
                    identifyAndHandleDisc(player, discType);
                    player.getInventory().getItemInMainHand().setAmount(player.getInventory().getItemInMainHand().getAmount()-1);
                }
            }
        }
    }

    /**
     * Checks if the given ItemStack is a music disc.
     *
     * @param item The ItemStack to check.
     * @return True if it's a music disc, false otherwise.
     */
    private boolean isMusicDisc(ItemStack item) {
        if (item == null) return false;

        Material type = item.getType();
        return type.toString().startsWith("MUSIC_DISC_");
    }

    /**
     * Identifies the type of music disc and calls the corresponding method.
     *
     * @param player    The player who is holding the disc.
     * @param discType  The type of the music disc.
     */
    private void identifyAndHandleDisc(Player player, Material discType) {
        switch (discType) {
            case MUSIC_DISC_11:
                handleMusicDisc11(player);
                break;
            case MUSIC_DISC_13:
                handleMusicDisc13(player);
                break;
            case MUSIC_DISC_BLOCKS:
                handleMusicDiscBlocks(player);
                break;
            case MUSIC_DISC_CAT:
                handleMusicDiscCat(player);
                break;
            case MUSIC_DISC_CHIRP:
                handleMusicDiscChirp(player);
                break;
            case MUSIC_DISC_FAR:
                handleMusicDiscFar(player);
                break;
            case MUSIC_DISC_MALL:
                handleMusicDiscMall(player);
                break;
            case MUSIC_DISC_MELLOHI:
                handleMusicDiscMellohi(player);
                break;
            case MUSIC_DISC_STAL:
                handleMusicDiscStal(player);
                break;
            case MUSIC_DISC_STRAD:
                handleMusicDiscStrad(player);
                break;
            case MUSIC_DISC_WAIT:
                handleMusicDiscWait(player);
                break;
            case MUSIC_DISC_WARD:
                handleMusicDiscWard(player);
                break;
            case MUSIC_DISC_PIGSTEP:
                handleMusicDiscPigstep(player);
                break;
            case MUSIC_DISC_5:
                handleMusicDisc5(player);
                break;
            case MUSIC_DISC_RELIC:
                handleMusicDiscRelic(player, player.getLocation());
                break;
            case MUSIC_DISC_OTHERSIDE:
                handleMusicDiscOtherside(player);
                break;
            // Add more cases if there are additional discs in newer Minecraft versions
            default:
                handleUnknownMusicDisc(player, discType);
                break;
        }
    }

    // Empty methods for each music disc variant


    private static final List<ItemStack> LOOT_ITEMS = new ArrayList<>();

    static {
        LOOT_ITEMS.add(new ItemStack(Material.MUSIC_DISC_CAT));
        LOOT_ITEMS.add(new ItemStack(Material.MUSIC_DISC_BLOCKS));
        LOOT_ITEMS.add(new ItemStack(Material.MUSIC_DISC_CHIRP));
        LOOT_ITEMS.add(new ItemStack(Material.MUSIC_DISC_FAR));
        LOOT_ITEMS.add(new ItemStack(Material.MUSIC_DISC_MALL));
        LOOT_ITEMS.add(new ItemStack(Material.MUSIC_DISC_MELLOHI));
        LOOT_ITEMS.add(new ItemStack(Material.MUSIC_DISC_STAL));
        LOOT_ITEMS.add(new ItemStack(Material.MUSIC_DISC_STRAD));
        LOOT_ITEMS.add(new ItemStack(Material.MUSIC_DISC_WARD));
        LOOT_ITEMS.add(new ItemStack(Material.MUSIC_DISC_WAIT));
        LOOT_ITEMS.add(new ItemStack(Material.MUSIC_DISC_PIGSTEP)); // Rare item
        LOOT_ITEMS.add(new ItemStack(Material.MUSIC_DISC_OTHERSIDE)); // Rare item
        LOOT_ITEMS.add(new ItemStack(Material.MUSIC_DISC_RELIC)); // Rare item
    }

    /**
     * Returns a random ItemStack from the list.
     *
     * @return A randomly selected ItemStack.
     */
    public static ItemStack getRandomLootItem() {
        Random random = new Random();
        return LOOT_ITEMS.get(random.nextInt(LOOT_ITEMS.size()));
    }
    private void handleMusicDisc13(Player player) {
        // Set time to midnight
        Bukkit.getWorld("world").setTime(18000); // Midnight in Minecraft time (18,000 ticks)

        // Broadcast the activation message to all players
        Bukkit.broadcastMessage(ChatColor.AQUA + "The BaroTrauma Virus (BT) has been activated for 2 minutes 58 seconds!");
        Bukkit.broadcastMessage(ChatColor.AQUA + "Beware! BT-infected monsters are slower but carry rare loot. Don't get infected!");

        // Play the MUSIC_DISC_13 sound to the activating player
        player.playSound(player.getLocation(), Sound.MUSIC_DISC_13, 1000.0f, 1.0f);

        // Create the listener
        Listener btListener = new Listener() {
            @EventHandler
            public void onEntitySpawn(CreatureSpawnEvent event) {
                if (Math.random() < 0.3) { // 30% chance for the monster to be infected with BT
                    LivingEntity entity = event.getEntity();
                    entity.setCustomName(ChatColor.AQUA + "BT-Infected " + entity.getType().name());
                    entity.setCustomNameVisible(true);
                    entity.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, (2 * 60 + 58) * 20, 0)); // Aqua glowing effect
                    entity.getWorld().spawnParticle(Particle.WATER_SPLASH, entity.getLocation(), 50, 0.5, 0.5, 0.5, 0.1); // Aqua particles

                    // Apply BT behavior: Slower movement, special loot chance
                    entity.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, (2 * 60 + 58) * 20, 2)); // Slower movement
                    entity.getPersistentDataContainer().set(new NamespacedKey(MinecraftNew.getInstance(), "bt_monster"), PersistentDataType.BYTE, (byte) 1);
                }
            }

            @EventHandler
            public void onEntityDeath(EntityDeathEvent event) {
                if (event.getEntity().getPersistentDataContainer().has(new NamespacedKey(MinecraftNew.getInstance(), "bt_monster"), PersistentDataType.BYTE)) {
                    // 10% chance to drop a random music disc
                    if (Math.random() < 0.2) {
                        event.getDrops().add(getRandomLootItem()); // Replace with a random disc if needed
                    }
                }
            }

            @EventHandler
            public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
                if (event.getDamager() instanceof LivingEntity && event.getEntity() instanceof Player) {
                    LivingEntity damager = (LivingEntity) event.getDamager();
                    Player damagedPlayer = (Player) event.getEntity();

                    // Check if the damager is a BT monster
                    if (damager.getPersistentDataContainer().has(new NamespacedKey(MinecraftNew.getInstance(), "bt_monster"), PersistentDataType.BYTE)) {
                        if (Math.random() < 0.5) { // 10% chance to infect the player with BT
                            Bukkit.broadcastMessage(ChatColor.RED + damagedPlayer.getName() + " has been infected with the BaroTrauma Virus!");
                            PlayerOxygenManager playerOxygenManager = new PlayerOxygenManager(MinecraftNew.getInstance());
                            player.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 200, 3));
                            damagedPlayer.sendMessage(ChatColor.DARK_AQUA + "You are suffocating! Run!");
                        }
                    }
                }
            }
        };

        // Register the listener
        Bukkit.getPluginManager().registerEvents(btListener, MinecraftNew.getInstance());

        // Schedule the deactivation task
        Bukkit.getScheduler().runTaskLater(MinecraftNew.getInstance(), () -> {
            Bukkit.broadcastMessage(ChatColor.RED + "The BaroTrauma Virus has subsided. Infected monsters are no longer spawning.");

            // Unregister the listener to stop spawning infected monsters
            HandlerList.unregisterAll(btListener);
        }, ((2 * 60) + 58) * 20L); // Runs after 2 minutes and 58 seconds
    }



    private void handleMusicDiscBlocks(Player player) {
        // Broadcast the activation message to all players
        Bukkit.broadcastMessage(ChatColor.GREEN + "Builder Mode Activated! You can fly for 5 minutes and 45 seconds!");

        // Play the MUSIC_DISC_BLOCKS sound to the activating player
        player.playSound(player.getLocation(), Sound.MUSIC_DISC_BLOCKS, 3.0f, 1.0f);

        // Allow the player to fly
        player.setAllowFlight(true);
        player.setFlying(true);

        // Schedule a task to disable flight after 5 minutes and 45 seconds (345 seconds)
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            // Check if the player is still online before modifying their state
            if (player.isOnline()) {
                player.setFlying(false);
                player.setAllowFlight(false);
                player.sendMessage(ChatColor.RED + "Your flight ability has expired.");
                Bukkit.broadcastMessage(ChatColor.RED + "Flight ability has been disabled for all players.");
            }
        }, 345 * 20L); // 345 seconds converted to ticks (20 ticks = 1 second)
    }

    private void handleMusicDiscRelic(Player player, Location jukeboxLocation) {
        Bukkit.broadcastMessage(ChatColor.LIGHT_PURPLE + "Festivity Activated for 3 minutes 38 seconds!");
        player.playSound(player.getLocation(), Sound.MUSIC_DISC_RELIC, 3.0f, 1.0f);

        // Duration of the effect in ticks (3 minutes 38 seconds = 218 seconds = 4360 ticks)
        long durationTicks = 218 * 20L;

        // Schedule Fireworks Task: Every 5 seconds
        BukkitTask fireworksTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            spawnFireworks(jukeboxLocation);
        }, 0L, 5 * 20L); // 0 tick initial delay, 5*20 ticks = 5 seconds

        // Schedule Glowing Task: Every 30 seconds

        // Schedule Particles Task: Every 3 seconds

        // Schedule a task to cancel all repeating tasks after the duration
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            fireworksTask.cancel();
            Bukkit.broadcastMessage(ChatColor.LIGHT_PURPLE + "Festivity has ended.");
        }, durationTicks);
    }

    /**
     * Spawns a cluster of 20 fireworks at random locations up to 100 blocks away from the jukebox.
     *
     * @param jukeboxLocation The location of the jukebox.
     */
    private void spawnFireworks(Location jukeboxLocation) {
        Random random = new Random();
        for (int i = 0; i < 50; i++) {
            // Generate random offset within 100 blocks
            double offsetX = (random.nextDouble() * 200) - 100; // Range: -100 to +100
            double offsetZ = (random.nextDouble() * 200) - 100; // Range: -100 to +100

            Location fireworkLocation = jukeboxLocation.clone().add(offsetX, 0, offsetZ);

            // Find the highest Y at this X,Z
            int highestY = fireworkLocation.getWorld().getHighestBlockYAt(fireworkLocation);
            fireworkLocation.setY(highestY + 1); // Spawn above the highest block

            // Spawn the Firework entity
            Firework firework = fireworkLocation.getWorld().spawn(fireworkLocation, Firework.class);
            FireworkMeta meta = firework.getFireworkMeta();

            // Random Firework Effect
            FireworkEffect effect = FireworkEffect.builder()
                    .flicker(random.nextBoolean())
                    .trail(random.nextBoolean())
                    .with(FireworkEffect.Type.values()[random.nextInt(FireworkEffect.Type.values().length)])
                    .withColor(getRandomColor())
                    .withFade(getRandomColor())
                    .build();

            meta.addEffect(effect);
            meta.setPower(random.nextInt(2) + 1); // Power between 1-2
            firework.setFireworkMeta(meta);
        }
    }


    /**
     * Spawns colored particles around an entity to simulate a colored glow effect.
     *
     * @param entity The entity around which to spawn particles.
     */
    private void spawnColoredParticlesAroundEntity(Entity entity) {
        Location loc = entity.getLocation();

        // Choose a random color
        Color color = getRandomColor();

        // Spawn a colored Particle.REDSTONE around the entity
        Particle.DustOptions dustOptions = new Particle.DustOptions(color, 1.0f);
        entity.getWorld().spawnParticle(Particle.REDSTONE, loc, 100, 1, 1, 1, dustOptions);
    }



    /**
     * Generates a random color.
     *
     * @return A randomly generated Color.
     */
    private Color getRandomColor() {
        Random random = new Random();
        float r = random.nextFloat();
        float g = random.nextFloat();
        float b = random.nextFloat();
        return Color.fromRGB((int) (r * 255), (int) (g * 255), (int) (b * 255));
    }

    /**
     * Handles the MUSIC_DISC_RELIC functionality.
     * This method was previously defined and implemented above.
     */

    private void handleMusicDiscCat(Player player) {
        // Broadcast the activation message to all players
        Bukkit.broadcastMessage(ChatColor.GREEN + "Harvest Frenzy Activated for 3 minutes 5 seconds!");

        // Play the MUSIC_DISC_CAT sound to the activating player
        player.playSound(player.getLocation(), Sound.MUSIC_DISC_CAT, 3.0f, 1.0f);

        // Set the randomTickSpeed gamerule to 1000
        Bukkit.getWorlds().forEach(world -> {
            world.setGameRule(GameRule.RANDOM_TICK_SPEED, 1000);
        });

        // Schedule a task to reset the randomTickSpeed after 3 minutes (3 * 60 * 20 ticks)
        Bukkit.getScheduler().runTaskLater(MinecraftNew.getInstance(), () -> {
            Bukkit.getWorlds().forEach(world -> {
                world.setGameRule(GameRule.RANDOM_TICK_SPEED, 3); // Reset to default value
            });
            Bukkit.broadcastMessage(ChatColor.RED + "Harvest Frenzy has ended. Tick speed has been reset.");
        }, ((3 * 60)+(5)) * 20L); // 3 minutes in ticks (20 ticks per second)
    }


    private void handleMusicDiscChirp(Player player) {
        XPManager xpManager = new XPManager(plugin);
        // Notify the player and play the music disc sound
        Bukkit.broadcastMessage(ChatColor.YELLOW + "Timber Boost! Bonus log-chopping event activated for 3 minutes and 5 seconds!");
        player.playSound(player.getLocation(), Sound.MUSIC_DISC_CHIRP, 1000.0f, 1.0f);

        // Listener for block-breaking events during the Timber Boost
        Listener logBreakListener = new Listener() {
            @EventHandler
            public void onLogChop(BlockBreakEvent event) {
                // Ensure the event is triggered by the same player who activated the disc
                if (!event.getPlayer().equals(player)) {
                    return;
                }

                // Check if the broken block is a log
                Material brokenMaterial = event.getBlock().getType();
                if (brokenMaterial == Material.OAK_LOG || brokenMaterial == Material.BIRCH_LOG ||
                        brokenMaterial == Material.SPRUCE_LOG || brokenMaterial == Material.JUNGLE_LOG ||
                        brokenMaterial == Material.ACACIA_LOG || brokenMaterial == Material.DARK_OAK_LOG) {

                    // Determine the bonus logs based on roll chance
                    double roll = Math.random() * 100; // Generate a random number between 0 and 100
                    int bonusLogs = 0;
                    Sound jingle = Sound.BLOCK_NOTE_BLOCK_BASS; // Default sound for common rarity
                    float pitch = 1.0f;

                    if (roll <= 0.2) { // Legendary (0.5% chance)
                        bonusLogs = 64;
                        jingle = Sound.ENTITY_ENDER_DRAGON_GROWL;
                        pitch = 2.0f; // High-intensity pitch for legendary
                        xpManager.addXP(player, "Forestry", 500);
                    } else if (roll <= 1) { // Epic (1% chance)
                        bonusLogs = 32;
                        jingle = Sound.UI_TOAST_CHALLENGE_COMPLETE;
                        pitch = 1.8f;
                        xpManager.addXP(player, "Forestry", 250);

                    } else if (roll <= 2) { // Rare (2% chance)
                        bonusLogs = 16;
                        jingle = Sound.ENTITY_PLAYER_LEVELUP;
                        pitch = 1.5f;
                        xpManager.addXP(player, "Forestry", 175);

                    } else if (roll <= 3.5) { // Uncommon (3% chance)
                        bonusLogs = 8;
                        jingle = Sound.BLOCK_NOTE_BLOCK_PLING;
                        pitch = 1.2f;
                        xpManager.addXP(player, "Forestry", 100);

                    } else if (roll <= 5.5) { // Common (4% chance)
                        bonusLogs = 4;
                        jingle = Sound.BLOCK_NOTE_BLOCK_BASS;
                        pitch = 1.0f;
                        xpManager.addXP(player, "Forestry", 50);

                    }

                    // If bonus logs were rolled, drop them and notify the player
                    if (bonusLogs > 0) {
                        ItemStack bonusLogStack = new ItemStack(brokenMaterial, bonusLogs);
                        event.getBlock().getWorld().dropItem(event.getBlock().getLocation(), bonusLogStack);

                        // Play the jingle based on rarity
                        player.playSound(player.getLocation(), jingle, 3.0f, pitch);

                        // Notify the player of their bonus
                        event.getPlayer().sendMessage(ChatColor.GOLD + "Bonus Logs! You received " + bonusLogs + " extra " +
                                brokenMaterial.name().toLowerCase().replace("_", " ") + "!");
                    }
                }
            }
        };

        // Register the listener
        Bukkit.getPluginManager().registerEvents(logBreakListener, plugin);

        // Schedule a task to unregister the listener after 3 minutes and 5 seconds
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            HandlerList.unregisterAll(logBreakListener);
            player.sendMessage(ChatColor.RED + "The Timber Boost event has ended!");
        }, 185 * 20L); // 185 seconds converted to ticks (20 ticks = 1 second)
    }




    private void handleMusicDiscFar(Player player) {
        // Stop the original sound and functionality for MUSIC_DISC_FAR
        player.stopSound(Sound.MUSIC_DISC_FAR);

        // Generate a random teleport location within a 100,000 block radius
        int maxDistance = 100000;
        Random random = new Random();
        int randomX = random.nextInt(maxDistance * 2) - maxDistance;
        int randomZ = random.nextInt(maxDistance * 2) - maxDistance;

        // Find the highest surface block at the location
        World world = player.getWorld();
        Location randomLocation = new Location(world, randomX, world.getHighestBlockYAt(randomX, randomZ), randomZ);

        // Save the player's original location
        Location originalLocation = player.getLocation();

        // Teleport the player to the random location
        player.teleport(randomLocation);
        Bukkit.broadcastMessage(ChatColor.YELLOW + "Teleported to Random Location! Happy looting!");

        // Make the player invincible for 2:54

        // Apply Speed II effect for 2:54
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 2 * 60 * 20 + 54 * 20, 1)); // 20 ticks = 1 second
        player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 2 * 60 * 20 + 54 * 20, 5)); // 20 ticks = 1 second

        // Play the song at the new location
        player.playSound(player.getLocation(), Sound.MUSIC_DISC_FAR, 100000, 1.0f);

        // Schedule a task to return the player after 2:54 (20 ticks per second)
        Bukkit.getScheduler().runTaskLater(MinecraftNew.getInstance(), () -> {
            // Return the player to the original location
            player.teleport(originalLocation);

            // Remove invulnerability


        }, (2 * 60 * 20) + 54 * 20); // Delay of 2:54 in ticks
    }


    private void handleMusicDiscMall(Player player) {
        // Play the Mall music disc sound
        player.playSound(player.getLocation(), Sound.MUSIC_DISC_MALL, 100000, 1.0f);
        Bukkit.broadcastMessage(ChatColor.YELLOW + "Monster spawns have been disabled for the duration of the song!");

        // Create a temporary flag to disable monster spawns
        Listener monsterSpawnListener = new Listener() {
            @EventHandler
            public void onCreatureSpawn(org.bukkit.event.entity.CreatureSpawnEvent event) {
                // Cancel monster spawns
                if (event.getEntity().getType().isAlive() && event.getEntity() instanceof org.bukkit.entity.Monster) {
                    event.setCancelled(true);
                }
            }
        };

        // Register the monster spawn listener
        Bukkit.getPluginManager().registerEvents(monsterSpawnListener, MinecraftNew.getInstance());

        // Schedule a task to re-enable monster spawns after 3:17 (197 seconds)
        Bukkit.getScheduler().runTaskLater(MinecraftNew.getInstance(), () -> {
            // Unregister the listener
            HandlerList.unregisterAll(monsterSpawnListener);

            // Notify players that monster spawns are re-enabled
            Bukkit.broadcastMessage(ChatColor.RED + "Monster spawns are now enabled again.");
        }, 197 * 20L); // 197 seconds in ticks (20 ticks per second)
    }



    private void handleMusicDiscMellohi(Player player) {
        // Play the Mellohi music disc sound
        player.playSound(player.getLocation(), Sound.MUSIC_DISC_MELLOHI, 100000, 1.0f);
        Bukkit.getWorld("world").setTime(18000);
        player.sendMessage(ChatColor.RED + "The Zombie Apocalypse has begun!");

        // Set the duration of the effect (1:36 = 96 seconds)
        int durationTicks = 96 * 20;

        // Listener to transform all spawned monsters into zombies
        Listener entitySpawnListener = new Listener() {
            @EventHandler
            public void onEntitySpawn(CreatureSpawnEvent event) {
                if (event.getEntity() instanceof Monster && !(event.getEntity() instanceof Zombie)) {
                    Location spawnLocation = event.getLocation();
                    event.setCancelled(true); // Cancel original monster spawn
                    Zombie zombie = (Zombie) spawnLocation.getWorld().spawnEntity(spawnLocation, EntityType.ZOMBIE);
                    zombie.getWorld().playSound(spawnLocation, Sound.ENTITY_ZOMBIE_AMBIENT, 1.0f, 1.0f);
                }
            }
        };

        // Listener to handle zombie interactions
        Listener zombieDamageListener = new Listener() {
            @EventHandler
            public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
                if (event.getEntity() instanceof Player && event.getDamager() instanceof Zombie) {
                    Player victim = (Player) event.getEntity();

                    // 10% chance to infect player with lethal Wither
                    if (Math.random() < 0.10) {
                        victim.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 200, 2)); // Wither effect for 10 seconds, level 2
                        victim.sendMessage(ChatColor.DARK_RED + "You've been infected!");
                    }
                }
            }
        };

        // Listener to modify zombie drops
        Listener zombieDropListener = new Listener() {
            @EventHandler
            public void onEntityDeath(EntityDeathEvent event) {
                if (event.getEntity() instanceof Zombie) {
                    // Drop 1-2 emeralds
                    int emeralds = 2 + (Math.random() < 0.5 ? 1 : 0);
                    event.getDrops().add(new ItemStack(Material.EMERALD, emeralds));
                }
            }
        };

        // Register listeners
        Bukkit.getPluginManager().registerEvents(entitySpawnListener, MinecraftNew.getInstance());
        Bukkit.getPluginManager().registerEvents(zombieDamageListener, MinecraftNew.getInstance());
        Bukkit.getPluginManager().registerEvents(zombieDropListener, MinecraftNew.getInstance());

        // Schedule a task to end the effect after 1:36 (96 seconds)
        Bukkit.getScheduler().runTaskLater(MinecraftNew.getInstance(), () -> {
            // Unregister listeners to stop the effect
            HandlerList.unregisterAll(entitySpawnListener);
            HandlerList.unregisterAll(zombieDamageListener);
            HandlerList.unregisterAll(zombieDropListener);

            // Notify the player that the effect has ended
            player.sendMessage(ChatColor.RED + "The Zombie Apocalypse has ended.");
        }, durationTicks);
    }


    private void handleMusicDiscStal(Player player) {
        // Find the nearest jukebox to the player
        Block nearestJukeboxBlock = findNearestJukebox(player.getLocation(), 20); // Search radius of 20 blocks

        if (nearestJukeboxBlock == null) {
            player.sendMessage(ChatColor.RED + "No jukebox found nearby to start the auction event.");
            return; // Exit if no jukebox is found
        }

        // Position of the jukebox
        Location jukeboxLocation = nearestJukeboxBlock.getLocation();

        // Play the music disc at the jukebox location
        player.getWorld().playSound(jukeboxLocation, Sound.MUSIC_DISC_STAL, SoundCategory.RECORDS, 100000, 1.0f);
        player.sendMessage(ChatColor.DARK_PURPLE + "The Grand Auction Event has begun!");

        // List of auction items (populate this manually)
        List<AuctionItem> auctionItems = new ArrayList<>();

        ItemStack enderDrop = CustomItemManager.createCustomItem(
                Material.ENDER_PEARL,
                ChatColor.YELLOW + "End Pearl",
                Arrays.asList(
                        ChatColor.GRAY + "Something doesn't look normal here...",
                        ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Reusable ender pearl.",
                        ChatColor.DARK_PURPLE + "Artifact"
                ),
                1,
                false,
                true
        );
        ItemStack undeadDrop = CustomItemManager.createCustomItem(Material.ROTTEN_FLESH, ChatColor.YELLOW +
                "Beating Heart", Arrays.asList(
                ChatColor.GRAY + "An undead heart still beating with undead life.",
                ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Apply it to equipment to unlock the secrets of Smite.",
                ChatColor.DARK_PURPLE + "Smithing Item"
        ), 1,false, true);
        ItemStack creeperDrop = CustomItemManager.createCustomItem(Material.TNT, ChatColor.YELLOW +
                "Hydrogen Bomb", Arrays.asList(
                ChatColor.GRAY + "500 KG of TNT.",
                ChatColor.BLUE + "Use: " + ChatColor.GRAY + "to summon a large quantity of live-fuse TNT.",
                ChatColor.DARK_PURPLE + "Artifact"
        ), 1,false, true);
        ItemStack spiderDrop = CustomItemManager.createCustomItem(Material.SPIDER_EYE, ChatColor.YELLOW +
                "SpiderBane", Arrays.asList(
                ChatColor.GRAY + "A strange substance lethal against spiders.",
                ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Apply it to equipment to unlock the secrets of Bane of Anthropods.",
                ChatColor.DARK_PURPLE + "Smithing Item"
        ), 1,false, true);
        ItemStack blazeDrop = CustomItemManager.createCustomItem(Material.FIRE_CHARGE, ChatColor.YELLOW +
                "Fire Ball", Arrays.asList(
                ChatColor.GRAY + "A projectile ball of fire.",
                ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Apply it to equipment to unlock the secrets of Flame.",
                ChatColor.DARK_PURPLE + "Smithing Item"
        ), 1,false, true);
        ItemStack witchDrop = CustomItemManager.createCustomItem(Material.ENCHANTED_BOOK, ChatColor.YELLOW +
                "Mending", Arrays.asList(
                ChatColor.GRAY + "An extremely rare enchantment.",
                ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Apply it to equipment to unlock the secrets of Mending.",
                ChatColor.DARK_PURPLE + "Smithing Item"
        ), 1,false, false);
        ItemStack witherSkeletonDrop = CustomItemManager.createCustomItem(Material.WITHER_SKELETON_SKULL, ChatColor.YELLOW +
                "Wither Skeleton Skull", Arrays.asList(
                ChatColor.GRAY + "A cursed skull.",
                ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Used in spawning the Wither.",
                ChatColor.DARK_PURPLE + "Summoning Item"
        ), 1,false, true);
        ItemStack guardianDrop = CustomItemManager.createCustomItem(Material.PRISMARINE_SHARD, ChatColor.YELLOW +
                "Rain", Arrays.asList(
                ChatColor.GRAY + "A strange object.",
                ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Used in summoning Rain.",
                ChatColor.DARK_PURPLE + "Artifact"
        ), 1,false, true);
        ItemStack elderGuardianDrop = CustomItemManager.createCustomItem(Material.ICE, ChatColor.YELLOW +
                "Frost Heart", Arrays.asList(
                ChatColor.GRAY + "A rare object.",
                ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Apply it to equipment to unlock the secrets of Frost Walker.",
                ChatColor.DARK_PURPLE + "Smithing Item"
        ), 1,false, true);
        ItemStack pillagerDrop = CustomItemManager.createCustomItem(Material.IRON_BLOCK, ChatColor.YELLOW +
                "Iron Golem", Arrays.asList(
                ChatColor.GRAY + "Ancient Summoning Artifact.",
                ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Summon an Iron Golem.",
                ChatColor.DARK_PURPLE + "Summoning Artifact"
        ), 1,false, true);
        ItemStack vindicatorDrop = CustomItemManager.createCustomItem(Material.SLIME_BALL, ChatColor.YELLOW +
                "KB Ball", Arrays.asList(
                ChatColor.GRAY + "An extremely bouncy ball.",
                ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Apply it to equipment to unlock the secrets of Knockback.",
                ChatColor.DARK_PURPLE + "Smithing Item"
        ), 1,false, true);
        ItemStack piglinDrop = CustomItemManager.createCustomItem(Material.ARROW, ChatColor.YELLOW +
                "High Caliber Arrow", Arrays.asList(
                ChatColor.GRAY + "A heavy arrow.",
                ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Apply it to equipment to unlock the secrets of Piercing.",
                ChatColor.DARK_PURPLE + "Smithing Item"
        ), 1,false, true);
        ItemStack piglinBruteDrop = CustomItemManager.createCustomItem(Material.SOUL_SOIL, ChatColor.YELLOW +
                "Grains of Soul", Arrays.asList(
                ChatColor.GRAY + "Soul soil with spirits of speed.",
                ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Apply it to equipment to unlock the secrets of Soul Speed.",
                ChatColor.DARK_PURPLE + "Smithing Item"
        ), 1,false, true);
        ItemStack zombifiedPiglinDrop = CustomItemManager.createCustomItem(Material.GOLD_INGOT, ChatColor.YELLOW +
                "Gold Bar", Arrays.asList(
                ChatColor.GRAY + "High value magnet.",
                ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Apply it to equipment to unlock the secrets of Looting.",
                ChatColor.DARK_PURPLE + "Smithing Item"
        ), 1,false, true);
        ItemStack drownedDrop = CustomItemManager.createCustomItem(Material.LEATHER_BOOTS, ChatColor.YELLOW +
                "Fins", Arrays.asList(
                ChatColor.GRAY + "Water Technology.",
                ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Apply it to equipment to unlock the secrets of Depth Strider.",
                ChatColor.DARK_PURPLE + "Smithing Item"
        ), 1,false, true);
        ItemStack skeletonDrop = CustomItemManager.createCustomItem(Material.BOW, ChatColor.YELLOW +
                "Bowstring", Arrays.asList(
                ChatColor.GRAY + "Air Technology.",
                ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Apply it to equipment to unlock the secrets of Power.",
                ChatColor.DARK_PURPLE + "Smithing Item"
        ), 1,false, true);
         ItemStack singularity = createCustomItem(
                    Material.IRON_NUGGET,
                    ChatColor.BLUE + "Singularity",
                    List.of(ChatColor.GRAY + "A rare blueprint entrusted to the Knights",
                            ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Reforges Items to the first Tier.",
                            ChatColor.DARK_PURPLE + "Smithing Item"),
                    1,
                    false // Set to true if you want it to be unbreakable
                    , true
         );
        ItemStack createHireVillager =
             createCustomItem(
                    Material.WOODEN_HOE,
                    ChatColor.YELLOW + "Hire Villager",
                    List.of(ChatColor.GRAY + "A proverbially useful companion.",
                            ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Summons a Villager.",
                            ChatColor.DARK_PURPLE + "Summoning Item"),
                    1,
                    false // Set to true if you want it to be unbreakable
                    , true
            );
        ItemStack leviathanHeart = createAlchemyItem("Leviathan Heart", Material.RED_DYE, List.of(
                ChatColor.GRAY + "The beating heart of a mighty creature.",
                ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Artifact for health.",
                ChatColor.GRAY + "Adds Regeneration 8 for 180 seconds.",
                ChatColor.DARK_PURPLE + "Artifact"
        ));
        ItemStack abyssalInk = createAlchemyItem("Abyssal Ink", Material.BLACK_DYE, List.of(
                ChatColor.GRAY + "Ink from the deepest abyss.",
                ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Exceptionally powerful Potency Modifier.",
                ChatColor.GRAY + "",
                ChatColor.DARK_PURPLE + "Mastery Brewing Modifier"
        ));


        ItemStack abyssalShell = createAlchemyItem("Abyssal Shell", Material.YELLOW_DYE, List.of(
                ChatColor.GRAY + "A shell from the deepest abyss.",
                ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Repairs equipment massively.",
                ChatColor.GRAY + "Restores 10000 durability.",
                ChatColor.DARK_PURPLE + "Smithing Ingredient"
        ));

        ItemStack abyssalVenom = createAlchemyItem("Abyssal Venom", Material.POTION, List.of(
                ChatColor.GRAY + "A vial of fatal venom.",
                ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Exceptionally powerful Duration Modifier.",
                ChatColor.GRAY + "",
                ChatColor.DARK_PURPLE + "Mastery Brewing Modifier"
        ));
        ItemStack trident = createAlchemyItem("Trident", Material.TRIDENT, List.of(
                ChatColor.GRAY + "A Trident.",
                ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Trident-ing.",
                ChatColor.GRAY + "",
                ChatColor.DARK_PURPLE + "Trident"
        ));
        ItemStack forbiddenBook = CustomItemManager.createCustomItem(
                Material.WRITTEN_BOOK,
                ChatColor.YELLOW + "Forbidden Book",
                Arrays.asList(
                        ChatColor.GRAY + "A dangerous book full of experimental magic.",
                        ChatColor.GRAY + "Apply it to equipment to push the limits of Enchantments.",
                        ChatColor.DARK_PURPLE + "Enchanting Item"
                ),
                1,
                false, // Not unbreakable
                true   // Add enchantment shimmer
        );
         ItemStack mithrilChunk
            = createCustomItem(
                    Material.LIGHT_BLUE_DYE,
                    ChatColor.BLUE + "Mithril Chunk",
                    List.of(ChatColor.GRAY + "A rare mineral.",
                            "Apply it to equipment to unlock the secrets of Unbreaking.",
                            "Smithing Item"),
                    1,
                    false // Set to true if you want it to be unbreakable
                    , true
            );

         ItemStack perfectDiamond =
             createCustomItem(
                    Material.DIAMOND,
                    ChatColor.BLUE + "Perfect Diamond",
                    List.of(ChatColor.GRAY + "A rare mineral.",
                            "Apply it to a pickaxe to unlock the secrets of Fortune.",
                            "Smithing Item"),
                    1,
                    false // Set to true if you want it to be unbreakable
                    , true
            );






        auctionItems.add(new AuctionItem(new ItemStack(Material.DIAMOND), 12));
        auctionItems.add(new AuctionItem(new ItemStack(Material.ENCHANTED_GOLDEN_APPLE), 64));
        auctionItems.add(new AuctionItem(trident, 64));
        auctionItems.add(new AuctionItem(enderDrop, 64));
        auctionItems.add(new AuctionItem(undeadDrop, 32));
        auctionItems.add(new AuctionItem(skeletonDrop, 32));
        auctionItems.add(new AuctionItem(creeperDrop, 64));
        auctionItems.add(new AuctionItem(spiderDrop, 32));
        auctionItems.add(new AuctionItem(blazeDrop, 32));
        auctionItems.add(new AuctionItem(witchDrop, 128));
        auctionItems.add(new AuctionItem(witherSkeletonDrop, 32));
        auctionItems.add(new AuctionItem(guardianDrop, 32));
        auctionItems.add(new AuctionItem(elderGuardianDrop, 32));
        auctionItems.add(new AuctionItem(pillagerDrop, 32));
        auctionItems.add(new AuctionItem(vindicatorDrop, 32));
        auctionItems.add(new AuctionItem(piglinDrop, 32));
        auctionItems.add(new AuctionItem(piglinBruteDrop, 32));
        auctionItems.add(new AuctionItem(zombifiedPiglinDrop, 32));
        auctionItems.add(new AuctionItem(drownedDrop, 32));
        auctionItems.add(new AuctionItem(skeletonDrop, 32));
        auctionItems.add(new AuctionItem(singularity, 16));
        auctionItems.add(new AuctionItem(createHireVillager, 64));
        auctionItems.add(new AuctionItem(leviathanHeart, 64));
        auctionItems.add(new AuctionItem(abyssalInk, 64));
        auctionItems.add(new AuctionItem(abyssalVenom, 64));
        auctionItems.add(new AuctionItem(abyssalShell, 64));
        auctionItems.add(new AuctionItem(forbiddenBook, 16));
        auctionItems.add(new AuctionItem(mithrilChunk, 16));
        auctionItems.add(new AuctionItem(perfectDiamond, 64));

        // Randomly select 5 unique items from the auctionItems list
        List<AuctionItem> selectedItems = new ArrayList<>();
        List<AuctionItem> itemsCopy = new ArrayList<>(auctionItems);

        Random random = new Random();
        int itemsToSelect = Math.min(5, itemsCopy.size()); // Ensure we don't try to select more items than are available

        for (int i = 0; i < itemsToSelect; i++) {
            int randomIndex = random.nextInt(itemsCopy.size());
            AuctionItem selectedItem = itemsCopy.remove(randomIndex);
            selectedItems.add(selectedItem);
        }

        // Map to keep track of which items each player has purchased
        Map<UUID, Set<Integer>> purchasedItems = new HashMap<>();

        // List to store the ArmorStands
        List<ArmorStand> armorStands = new ArrayList<>();

        // Schedule the tasks to display items
        for (int i = 0; i < selectedItems.size(); i++) {
            final int index = i;
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                // Remove previous item if any
                if (index > 0 && index - 1 < armorStands.size()) {
                    ArmorStand previousArmorStand = armorStands.get(index - 1);
                    if (previousArmorStand != null && !previousArmorStand.isDead()) {
                        previousArmorStand.remove();
                    }
                }

                // Get the selected item at this index
                AuctionItem auctionItem = selectedItems.get(index);

                // Display the item over the jukebox
                ArmorStand armorStand = (ArmorStand) jukeboxLocation.getWorld().spawnEntity(
                        jukeboxLocation.clone().add(0.5, 1, 0.5), EntityType.ARMOR_STAND);
                armorStand.setVisible(false);
                armorStand.setGravity(false);
                armorStand.setSmall(true);
                armorStand.setCustomNameVisible(true);

                // Set the custom name to show the item name and cost
                String itemName = auctionItem.getItemStack().getItemMeta().hasDisplayName()
                        ? auctionItem.getItemStack().getItemMeta().getDisplayName()
                        : auctionItem.getItemStack().getType().name();
                armorStand.setCustomName(ChatColor.GREEN + itemName + ChatColor.YELLOW +
                        " - " + auctionItem.getEmeraldCost() + " Emeralds");
                armorStand.setHelmet(auctionItem.getItemStack());
                armorStand.setInvulnerable(true);
                // armorStand.setMarker(true); // Commented out to enable interaction
                armorStands.add(armorStand);

                // Make the ArmorStand spin
                new BukkitRunnable() {
                    double angle = 0;

                    @Override
                    public void run() {
                        if (armorStand.isDead()) {
                            this.cancel();
                        } else {
                            angle += Math.PI / 16;
                            if (angle >= 2 * Math.PI) {
                                angle = 0;
                            }
                            armorStand.setHeadPose(new EulerAngle(0, angle, 0));
                        }
                    }
                }.runTaskTimer(plugin, 1L, 1L); // Corrected scheduling

                // Add a large particle effect when a new item is displayed
                armorStand.getWorld().spawnParticle(
                        Particle.FIREWORKS_SPARK,
                        armorStand.getLocation().add(0, 1, 0),
                        100, // Number of particles
                        0.5, 0.5, 0.5, // Offset
                        0.1 // Speed
                );

                // Play a sound when a new item is displayed
                armorStand.getWorld().playSound(
                        armorStand.getLocation(),
                        Sound.ENTITY_FIREWORK_ROCKET_LAUNCH,
                        SoundCategory.MASTER,
                        1.0f, // Volume
                        1.0f  // Pitch
                );
            }, i * 30 * 20L); // Delay in ticks (i * 30 seconds * 20 ticks per second)
        }

        // Schedule a task to remove the last item after the song ends
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            // Remove last ArmorStand
            if (!armorStands.isEmpty()) {
                ArmorStand lastArmorStand = armorStands.get(armorStands.size() - 1);
                if (lastArmorStand != null && !lastArmorStand.isDead()) {
                    lastArmorStand.remove();
                }
            }
            player.sendMessage(ChatColor.DARK_PURPLE + "The Grand Auction Event has ended!");
        }, 150 * 20L); // 150 seconds

        // Add particle effects around the jukebox
        new BukkitRunnable() {
            int ticksRun = 0;

            @Override
            public void run() {
                if (ticksRun >= 150 * 20L) {
                    this.cancel();
                } else {
                    // Generate particle effects around the jukebox
                    jukeboxLocation.getWorld().spawnParticle(Particle.ENCHANTMENT_TABLE,
                            jukeboxLocation.clone().add(0.5, 1, 0.5), 20, 0.5, 1, 0.5, 0);
                    ticksRun += 20;
                }
            }
        }.runTaskTimer(plugin, 0L, 20L); // Corrected scheduling

        // Handle player interactions
        Listener interactionListener = new Listener() {
            @EventHandler
            public void onPlayerInteractAtEntity(PlayerInteractAtEntityEvent event) {
                Player interactingPlayer = event.getPlayer();
                Entity entity = event.getRightClicked();

                // Debug message
                // Check if the entity is one of our ArmorStands
                if (entity instanceof ArmorStand) {
                    if (armorStands.contains(entity)) {
                        event.setCancelled(true); // Prevent default interaction
                        int itemIndex = armorStands.indexOf(entity);

                        if (itemIndex >= 0 && itemIndex < selectedItems.size()) {
                            AuctionItem auctionItem = selectedItems.get(itemIndex);
                            interactingPlayer.sendMessage(ChatColor.GRAY + "Auction item found: " + auctionItem.getItemStack().getType());

                            // Check if the player has already purchased this item
                            Set<Integer> playerPurchasedItems = purchasedItems.getOrDefault(
                                    interactingPlayer.getUniqueId(), new HashSet<>());
                            if (playerPurchasedItems.contains(itemIndex)) {
                                interactingPlayer.sendMessage(ChatColor.RED + "You have already purchased this item.");
                            } else {
                                int emeraldCost = auctionItem.getEmeraldCost();

                                // Process the purchase
                                boolean success = processPurchase(interactingPlayer, emeraldCost, auctionItem.getItemStack());

                                if (success) {
                                    // Mark the item as purchased
                                    playerPurchasedItems.add(itemIndex);
                                    purchasedItems.put(interactingPlayer.getUniqueId(), playerPurchasedItems);

                                    // Place particle effect and sound when a player purchases an item
                                    interactingPlayer.playSound(interactingPlayer.getLocation(),
                                            Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
                                    interactingPlayer.spawnParticle(Particle.VILLAGER_HAPPY,
                                            interactingPlayer.getLocation(), 20, 0.5, 0.5, 0.5);
                                }
                            }
                        } else {
                            interactingPlayer.sendMessage(ChatColor.RED + "No auction item found at this index.");
                        }
                    } else {
                        interactingPlayer.sendMessage(ChatColor.GRAY + "ArmorStand is not part of the auction.");
                    }
                }
            }
        };
        Bukkit.getPluginManager().registerEvents(interactionListener, plugin);

        // Unregister the interaction listener after the event ends
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            HandlerList.unregisterAll(interactionListener);
        }, 150 * 20L); // 150 seconds
    }


    // Method to find the nearest jukebox within a certain radius
    private Block findNearestJukebox(Location location, int radius) {
        World world = location.getWorld();
        Block nearestJukebox = null;
        double nearestDistanceSquared = Double.MAX_VALUE;
        int cx = location.getBlockX();
        int cy = location.getBlockY();
        int cz = location.getBlockZ();

        for (int x = cx - radius; x <= cx + radius; x++) {
            for (int y = cy - radius; y <= cy + radius; y++) {
                for (int z = cz - radius; z <= cz + radius; z++) {
                    Block block = world.getBlockAt(x, y, z);
                    if (block.getType() == Material.JUKEBOX) {
                        double distanceSquared = block.getLocation().distanceSquared(location);
                        if (distanceSquared < nearestDistanceSquared) {
                            nearestDistanceSquared = distanceSquared;
                            nearestJukebox = block;
                        }
                    }
                }
            }
        }
        return nearestJukebox;
    }

    // Method to check if the player has enough items
    private boolean hasEnoughItems(Inventory inventory, Material material, int amount) {
        int total = 0;
        for (ItemStack item : inventory.getContents()) {
            if (item != null && item.getType() == material) {
                total += item.getAmount();
                if (total >= amount) return true; // Sufficient items found
            }
        }
        return false; // Not enough items
    }

    // Method to remove items from the player's inventory
    private void removeItems(Inventory inventory, Material material, int amount) {
        int remaining = amount;
        for (ItemStack item : inventory.getContents()) {
            if (item != null && item.getType() == material) {
                int itemAmount = item.getAmount();
                if (itemAmount <= remaining) {
                    // Remove the whole stack
                    inventory.remove(item);
                    remaining -= itemAmount;
                } else {
                    // Partially reduce the stack
                    item.setAmount(itemAmount - remaining);
                    remaining = 0;
                    break; // No need to process further
                }
            }
        }
        if (remaining > 0) {
            throw new IllegalStateException("Inventory state mismatch: not enough items removed.");
        }
    }


    // Method to process the purchase
    public boolean processPurchase(Player player, int cost, ItemStack item) {
        Inventory inventory = player.getInventory();
        Material emeraldMaterial = Material.EMERALD;

        // Validate enough emeralds are available
        if (!hasEnoughItems(inventory, emeraldMaterial, cost)) {
            player.sendMessage(ChatColor.RED + "You don't have enough emeralds.");
            return false;
        }

        // Remove emeralds and give the item
        removeItems(inventory, emeraldMaterial, cost);
        ItemStack itemToGive = item.clone();
        itemToGive.setAmount(1); // Ensure only one item is given
        inventory.addItem(itemToGive);

        // Feedback and sound
        player.sendMessage(ChatColor.GREEN + "You purchased " + itemToGive.getType() + " for " + cost + " emerald(s).");
        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.2f);
        return true;
    }


    // AuctionItem class to hold the item and its emerald cost
    class AuctionItem {
        private ItemStack itemStack;
        private int emeraldCost;

        public AuctionItem(ItemStack itemStack, int emeraldCost) {
            this.itemStack = itemStack;
            this.emeraldCost = emeraldCost;
        }

        public ItemStack getItemStack() {
            return itemStack;
        }

        public int getEmeraldCost() {
            return emeraldCost;
        }
    }




    private void handleMusicDiscStrad(Player player) {
        // Play the music disc sound
        player.playSound(player.getLocation(), Sound.MUSIC_DISC_STRAD, 3.0f, 1.0f);
        player.sendMessage(ChatColor.GREEN + "Your items are being repaired!");
        plugin.getLogger().info("MusicDiscStrad event started for player: " + player.getName());

        // Duration of the song in ticks (188 seconds * 20 ticks per second)
        int durationTicks = 188 * 20;

        // Interval in ticks (20 ticks = 1 second)
        int intervalTicks = 10;

        // Total number of cycles (188 cycles)
        int totalCycles = durationTicks / intervalTicks;

        // Total durability to restore per item: 308
        // Durability per cycle per item: 308 / 188 ≈ 1.638
        double durabilityPerCycle = 608.0 / totalCycles;

        // Schedule a repeating task to repair items
        new BukkitRunnable() {
            int cyclesRun = 0;
            double accumulatedDurability = 0.0;

            @Override
            public void run() {
                plugin.getLogger().info("Repair Cycle " + cyclesRun + " started for player: " + player.getName());

                if (cyclesRun >= totalCycles) {
                    this.cancel();
                    player.sendMessage(ChatColor.RED + "The repair effect has ended!");
                    plugin.getLogger().info("MusicDiscStrad event ended for player: " + player.getName());
                    return;
                }

                // Accumulate the durability to restore
                accumulatedDurability += durabilityPerCycle;
                int repairAmount = (int) Math.floor(accumulatedDurability);
                accumulatedDurability -= repairAmount;

                if (repairAmount > 0) {
                    plugin.getLogger().info("Cycle " + cyclesRun + ": Repairing items by " + repairAmount + " durability points.");
                    // Repair items in inventory and armor
                    repairPlayerItems(player, repairAmount);
                } else {
                    plugin.getLogger().info("Cycle " + cyclesRun + ": No repair this cycle.");
                }

                cyclesRun++;
            }
        }.runTaskTimer(plugin, 0L, intervalTicks); // Start immediately, repeat every second
    }

    private void repairPlayerItems(Player player, int repairAmount) {
        plugin.getLogger().info("Starting to repair items for player: " + player.getName());

        // Repair items in main inventory and off-hand
        ItemStack[] inventoryContents = player.getInventory().getContents();
        for (int i = 0; i < inventoryContents.length; i++) {
            ItemStack item = inventoryContents[i];
            if (item != null) {
                repairItem(item, repairAmount, "Inventory Slot " + i);
            }
        }

        // Repair items in armor slots
        ItemStack[] armorContents = player.getInventory().getArmorContents();
        String[] armorSlots = {"Helmet", "Chestplate", "Leggings", "Boots"};
        for (int i = 0; i < armorContents.length; i++) {
            ItemStack armorItem = armorContents[i];
            if (armorItem != null) {
                repairItem(armorItem, repairAmount, "Armor Slot " + armorSlots[i]);
            }
        }

        // Update player's inventory to reflect changes
        player.updateInventory();
        plugin.getLogger().info("Finished repairing items for player: " + player.getName());
    }

    private void repairItem(ItemStack item, int repairAmount, String slotInfo) {
        if (item == null) return;

        Material material = item.getType();
        short maxDurability = material.getMaxDurability();

        if (maxDurability > 0) {
            // Item is damageable
            short currentDurability = item.getDurability(); // Deprecated but works
            short newDurability = (short) Math.max(currentDurability - repairAmount, 0);

            // Debug messages
            plugin.getLogger().info("Item: " + material);
            plugin.getLogger().info("Current Durability (Damage): " + currentDurability);
            plugin.getLogger().info("Max Durability: " + maxDurability);
            plugin.getLogger().info("Repairing " + material + " in " + slotInfo + " from damage " + currentDurability + " to " + newDurability);

            item.setDurability(newDurability); // Deprecated but necessary here
        } else {
            plugin.getLogger().info(material + " in " + slotInfo + " is not damageable.");
        }
    }





    public void handleMusicDiscWait(Player player) {
        XPManager xpManager = new XPManager(plugin);

        // Find the nearest jukebox to the player
        Block nearestJukeboxBlock = findNearestJukebox(player.getLocation(), 20); // Search radius of 20 blocks

        if (nearestJukeboxBlock == null) {
            player.sendMessage(ChatColor.RED + "No jukebox found nearby to start the experience surge.");
            return; // Exit if no jukebox is found
        }

        // Position of the jukebox
        Location jukeboxLocation = nearestJukeboxBlock.getLocation();

        // Play the music disc sound at the jukebox location
        player.getWorld().playSound(jukeboxLocation, Sound.MUSIC_DISC_WAIT, SoundCategory.RECORDS, 3.0f, 1.0f);
        player.sendMessage(ChatColor.GREEN + "You feel a surge of experience flowing through you!");

        // Duration of the song in ticks (231 seconds * 20 ticks per second)
        int durationTicks = 231 * 20;

        // Interval in ticks (20 ticks = 1 second)
        int intervalTicks = 20;

        // Total number of cycles (231 cycles)
        int totalCycles = durationTicks / intervalTicks;

        // List of skills
        String[] skills = {"Combat", "Fishing", "Forestry", "Mining", "Farming"};

        // Colors for each skill
        Map<String, ChatColor> skillColors = new HashMap<>();
        skillColors.put("Combat", ChatColor.RED);
        skillColors.put("Fishing", ChatColor.AQUA);
        skillColors.put("Forestry", ChatColor.DARK_GREEN);
        skillColors.put("Mining", ChatColor.GRAY);
        skillColors.put("Farming", ChatColor.YELLOW);

        Random random = new Random();

        // Schedule a repeating task to give XP and display messages
        new BukkitRunnable() {
            int cyclesRun = 0;

            @Override
            public void run() {
                if (cyclesRun >= totalCycles) {
                    this.cancel();
                    player.sendMessage(ChatColor.GREEN + "The experience surge has ended.");
                    return;
                }

                // Select one random skill
                String skill = skills[random.nextInt(skills.length)];

                int xp = random.nextInt(50) + 1; // Random XP between 1 and 50

                // Call addXP method
                xpManager.addXP(player, skill, xp);

                // Display the XP given using an ArmorStand
                // Spawn an invisible ArmorStand over the jukebox
                Location loc = jukeboxLocation.clone().add(0.5, 1.0, 0.5);
                ArmorStand armorStand = (ArmorStand) player.getWorld().spawnEntity(loc, EntityType.ARMOR_STAND);
                armorStand.setVisible(false);
                armorStand.setGravity(false);
                armorStand.setCustomNameVisible(true);
                armorStand.setMarker(true); // Makes the ArmorStand have no hitbox and be small

                ChatColor color = skillColors.getOrDefault(skill, ChatColor.WHITE);
                armorStand.setCustomName(color + "+" + xp + " " + skill + " XP");

                // Schedule the ArmorStand to be removed after 2 seconds (40 ticks)
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        armorStand.remove();
                    }
                }.runTaskLater(plugin, 20L);

                cyclesRun++;
            }
        }.runTaskTimer(plugin, 0L, intervalTicks);
    }

    // Method to find the nearest jukebox within a certain radius



    public void handleMusicDiscWard(Player player) {
        XPManager xpManager = new XPManager(plugin);

        // Find the nearest jukebox to the player
        Block nearestJukeboxBlock = findNearestJukebox(player.getLocation(), 20); // Search radius of 20 blocks

        if (nearestJukeboxBlock == null) {
            player.sendMessage(ChatColor.RED + "No jukebox found nearby to start the XP rain.");
            return; // Exit if no jukebox is found
        }

        // Position of the jukebox
        Location jukeboxLocation = nearestJukeboxBlock.getLocation();

        // Play the music disc sound at the jukebox location
        player.getWorld().playSound(jukeboxLocation, Sound.MUSIC_DISC_WARD, SoundCategory.RECORDS, 3.0f, 1.0f);
        player.sendMessage(ChatColor.GREEN + "The mystical music fills the air, raining experience!");

        // Duration of the song in ticks (251 seconds * 20 ticks per second)
        int durationTicks = 251 * 20;

        // Interval in ticks (3 ticks per XP spawn)
        int intervalTicks = 3;

        // Total number of cycles
        int totalCycles = durationTicks / intervalTicks;

        Random random = new Random();

        // Schedule a repeating task to spawn XP orbs
        new BukkitRunnable() {
            int cyclesRun = 0;

            @Override
            public void run() {
                if (cyclesRun >= totalCycles) {
                    this.cancel();
                    player.sendMessage(ChatColor.GREEN + "The XP rain has ended.");
                    return;
                }

                // Random XP amount (1-2)
                int xpAmount = random.nextInt(1) + 1;

                // Spawn the XP orb at the jukebox location
                player.getWorld().spawn(jukeboxLocation.clone().add(0.5, 1.0, 0.5), ExperienceOrb.class, orb -> orb.setExperience(xpAmount));

                // Notify the player with a message above the jukebox
                Location loc = jukeboxLocation.clone().add(0.5, 1.0, 0.5);
                ArmorStand armorStand = (ArmorStand) player.getWorld().spawnEntity(loc, EntityType.ARMOR_STAND);
                armorStand.setVisible(false);
                armorStand.setGravity(false);
                armorStand.setCustomNameVisible(true);
                armorStand.setMarker(true); // No hitbox

                armorStand.setCustomName(ChatColor.AQUA + "+" + xpAmount + " XP");

                // Schedule the ArmorStand to be removed after 2 seconds (40 ticks)
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        armorStand.remove();
                    }
                }.runTaskLater(plugin, 40L);

                cyclesRun++;
            }
        }.runTaskTimer(plugin, 0L, intervalTicks);
    }

    private void handleMusicDiscPigstep(Player player) {
        player.playSound(player.getLocation(), Sound.MUSIC_DISC_PIGSTEP, 300.0f, 1.0f);

        World world = player.getWorld();
        Location spawnLocation = findNearestJukebox(player.getLocation(), 10).getLocation();

        // Spawn an angry Zombie Pigman (Zombified Piglin)
        PigZombie pigman = (PigZombie) world.spawnEntity(spawnLocation, EntityType.ZOMBIFIED_PIGLIN);

        // Set Pigman attributes
        pigman.setCustomName(ChatColor.RED + "Angry Pigman");

        pigman.setCustomNameVisible(true);
        pigman.setAnger(Integer.MAX_VALUE); // Stay angry forever
        pigman.setTarget(player); // Target the player who triggered the event

        // Equip Pigman with enchanted golden armor and sword
        pigman.getEquipment().setHelmet(createEnchantedItem(Material.GOLDEN_HELMET));
        pigman.getEquipment().setChestplate(createEnchantedItem(Material.GOLDEN_CHESTPLATE));
        pigman.getEquipment().setLeggings(createEnchantedItem(Material.GOLDEN_LEGGINGS));
        pigman.getEquipment().setBoots(createEnchantedItem(Material.GOLDEN_BOOTS));
        pigman.getEquipment().setItemInMainHand(createEnchantedItem(Material.GOLDEN_SWORD));
        pigman.getEquipment().setHelmetDropChance(0.0f);
        pigman.getEquipment().setChestplateDropChance(0.0f);
        pigman.getEquipment().setLeggingsDropChance(0.0f);
        pigman.getEquipment().setBootsDropChance(0.0f);
        pigman.getEquipment().setItemInMainHandDropChance(0.0f);

        // Handle the Pigman's death event
        Bukkit.getPluginManager().registerEvents(new Listener() {
            @EventHandler
            public void onPigmanDeath(EntityDeathEvent event) {
                if (event.getEntity().equals(pigman)) {
                    // Placeholder for actions to perform when the Pigman dies
                    // Example: player.sendMessage(ChatColor.GOLD + "You defeated the Angry Pigman!");

                    PetManager petManager = PetManager.getInstance(plugin);
                    Random random = new Random();
                    if(random.nextDouble() < 0.1) {
                        petManager.createPet(player, "Piglin Brute", PetManager.Rarity.LEGENDARY, 100, Particle.FIREWORKS_SPARK, PetManager.PetPerk.SPEED_BOOST, PetManager.PetPerk.CHALLENGE, PetManager.PetPerk.SECOND_WIND);
                    }
                    if (random.nextDouble() < 1) { // Always true
                        int goldIngots = random.nextInt(49) + 16; // Random between 16 and 64
                        ItemStack goldStack = new ItemStack(Material.GOLD_INGOT, goldIngots);
                        player.getInventory().addItem(goldStack); // Add to player's inventory
                        player.sendMessage(ChatColor.GOLD + "You received " + goldIngots + " gold ingots!");
                    }

// Second condition: 20% chance to give a Notch Apple
                    if (random.nextDouble() < 0.2) { // 20% chance
                        ItemStack notchApple = new ItemStack(Material.ENCHANTED_GOLDEN_APPLE, 1);
                        player.getInventory().addItem(notchApple); // Add to player's inventory
                        player.sendMessage(ChatColor.GOLD + "You received an Enchanted Golden Apple!");
                    }
                    // Unregister the listener after handling the event
                    EntityDeathEvent.getHandlerList().unregister(this);
                }
            }
        }, plugin);
    }

    // Utility method to create enchanted items
    private ItemStack createEnchantedItem(Material material) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.addEnchant(Enchantment.PROTECTION_ENVIRONMENTAL, 3, true); // Add example enchantment
            meta.addEnchant(Enchantment.DURABILITY, 2, true); // Add another example enchantment
            item.setItemMeta(meta);
        }
        return item;
    }

    @EventHandler
    public void onWardenDeath(EntityDeathEvent event) {
        if (event.getEntity() instanceof Warden) {
            Warden warden = (Warden) event.getEntity();

            // Check if the Warden has metadata to track the spawner
            if (warden.hasMetadata("spawner")) {
                UUID spawnerUUID = (UUID) warden.getMetadata("spawner").get(0).value();
                Player spawner = Bukkit.getPlayer(spawnerUUID);

                if (spawner != null && spawner.isOnline()) {
                    Random random = new Random();
                    // Reward the player with the Warden pet
                    if (random.nextDouble() < 0.1) { // 20% chance
                        giveWardenPet(spawner);
                        spawner.sendMessage(ChatColor.GOLD + "Congratulations! You have been rewarded with a Warden pet!");
                    }
                }
            }
        }
    }

    private void giveWardenPet(Player player) {
        PetManager petManager = PetManager.getInstance(plugin);
        petManager.createPet(player, "Warden", PetManager.Rarity.LEGENDARY, 100, Particle.WARPED_SPORE, PetManager.PetPerk.DIGGING_CLAWS, PetManager.PetPerk.ECHOLOCATION, PetManager.PetPerk.LASER_BEAM, PetManager.PetPerk.BONE_PLATING);

    }


    private void handleMusicDisc5(Player player) {
        PlayerOxygenManager playerOxygenManager = PlayerOxygenManager.getInstance();
        UUID playerUUID = player.getUniqueId();

        // Reset the player's oxygen to the initial value
        int initialOxygen = playerOxygenManager.calculateInitialOxygen(player);
        playerOxygenManager.setPlayerOxygenLevel(player, initialOxygen);

        // Apply night vision for 20 minutes (20 ticks/second * 60 seconds/minute * 20 minutes)
        player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 20 * 60 * 20, 0, true, false, false));

        // Notify the player

    }



    private void handleMusicDisc11(Player player) {
        // Play the creepy music
        player.getWorld().playSound(player.getLocation(), Sound.MUSIC_DISC_11, SoundCategory.RECORDS, 3.0f, 1.0f);

        // Spawn the Warden miniboss
        Warden warden = (Warden) player.getWorld().spawnEntity(player.getLocation(), EntityType.WARDEN);
        warden.setCustomName(ChatColor.DARK_RED + "Miniboss: The Schizophrenic Warden");
        warden.setCustomNameVisible(true);

        // Apply special effects to the Warden (e.g., glowing)
        warden.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, Integer.MAX_VALUE, 0));

        // Track the player who spawned the Warden
        warden.setMetadata("spawner", new FixedMetadataValue(MinecraftNew.getInstance(), player.getUniqueId()));

        // Scheduler to make the Warden lose sight of the player every 4 seconds
        Bukkit.getScheduler().runTaskTimer(MinecraftNew.getInstance(), () -> {
            if (!warden.isValid()) {
                return; // Stop if the Warden is no longer in the world
            }
            // Remove all active targets from the Warden
            warden.setAnger(player, 0);
            warden.setTarget(null);

            // Play a sound effect to indicate blindness
            warden.getWorld().playSound(warden.getLocation(), Sound.ENTITY_BAT_AMBIENT, 1.0f, 0.5f);

            // Show a particle effect to signal disorientation
            warden.getWorld().spawnParticle(Particle.SMOKE_LARGE, warden.getLocation(), 10, 1, 1, 1);
            Bukkit.broadcastMessage(ChatColor.BLACK + "The Schizophrenic Warden has lost your scent!");

        }, 0L, 200L); // Runs every 4 seconds (80 ticks)
    }


    public void handleMusicDiscOtherside(Player player) {
        Random random = new Random();
        World world = player.getWorld();
        PetManager petManager = PetManager.getInstance(plugin);
        petManager.createPet(player, "Parrot", PetManager.Rarity.LEGENDARY, 100, Particle.TOTEM, PetManager.PetPerk.FLIGHT, PetManager.PetPerk.LULLABY);

        // Play the music disc sound
        world.playSound(player.getLocation(), Sound.MUSIC_DISC_OTHERSIDE, SoundCategory.RECORDS, 1000.0f, 1.0f);
        player.sendMessage(ChatColor.GREEN + "Time accelerates around you!");

        // Duration of the disc in ticks (195 seconds * 20 ticks per second)
        int durationTicks = 195 * 20;

        // Time acceleration factor: 10 seconds per tick (200 ticks per tick)
        long timeAcceleration = 180L;

        // List to keep track of spawned parrots
        List<Parrot> spawnedParrots = new ArrayList<>();

        // Generate a random color for fireworks


        // Variables to keep track of day/night
        final boolean[] isDay = {isDayTime(world)};

        // Schedule repeating task to speed up time and handle events
        new BukkitRunnable() {
            int ticksRun = 0;

            @Override
            public void run() {
                if (ticksRun >= durationTicks) {
                    // Time is up, reset time progression
                    this.cancel();

                    // Despawn all parrots
                    for (Parrot parrot : spawnedParrots) {
                        if (!parrot.isDead()) {
                            parrot.remove();
                        }
                    }

                    player.sendMessage(ChatColor.GREEN + "Time returns to normal.");
                    return;
                }

                // Accelerate time
                long currentTime = world.getTime();
                world.setTime(currentTime + timeAcceleration);

                // Check if day/night has changed
                boolean newIsDay = isDayTime(world);
                if (newIsDay != isDay[0]) {
                    // Day/night transition occurred
                    if (newIsDay) {
                        // It's now day
                        // Spawn parrots up to 100 blocks away
                        for (int i = 0; i < 10; i++) {
                            Location spawnLocation = player.getLocation().clone().add(
                                    (random.nextDouble() - 0.5) * 200,
                                    0,
                                    (random.nextDouble() - 0.5) * 200
                            );
                            // Find highest block at location to spawn parrot on ground
                            spawnLocation.setY(world.getHighestBlockYAt(spawnLocation) + 1);

                            Parrot parrot = (Parrot) world.spawnEntity(spawnLocation, EntityType.PARROT);
                            parrot.setVariant(Parrot.Variant.values()[random.nextInt(Parrot.Variant.values().length)]);
                            spawnedParrots.add(parrot);
                        }
                    } else {
                        // It's now night
                        // Launch fireworks up to 100 blocks away
                        for (int i = 0; i < 45; i++) {
                            Location fireworkLocation = player.getLocation().clone().add(
                                    (random.nextDouble() - 0.5) * 200,
                                    0,
                                    (random.nextDouble() - 0.5) * 200
                            );
                            fireworkLocation.setY(world.getHighestBlockYAt(fireworkLocation) + 1);
                            Color fireworkColor = Color.fromRGB(random.nextInt(256), random.nextInt(256), random.nextInt(256));
                            launchFirework(world, fireworkLocation, fireworkColor);
                        }
                    }
                    isDay[0] = newIsDay;
                }

                ticksRun++;
            }
        }.runTaskTimer(plugin, 0L, 1L); // Run every tick
    }

    // Helper method to check if it's day
    private boolean isDayTime(World world) {
        long time = world.getTime() % 24000;
        return time < 12300 || time > 23850;
    }

    // Helper method to launch a firework at a location with a given color
    private void launchFirework(World world, Location location, Color color) {
        Random random = new Random();
        Firework firework = (Firework) world.spawnEntity(location, EntityType.FIREWORK);
        FireworkMeta meta = firework.getFireworkMeta();

        // Create firework effect with random type and effects
        FireworkEffect effect = FireworkEffect.builder()
                .withColor(color)
                .withFade(color)
                .with(FireworkEffect.Type.values()[random.nextInt(FireworkEffect.Type.values().length)])
                .flicker(random.nextBoolean())
                .trail(random.nextBoolean())
                .build();

        meta.addEffect(effect);
        meta.setPower(1); // Flight duration

        firework.setFireworkMeta(meta);
    }


    private void handleUnknownMusicDisc(Player player, Material discType) {
        // Handle any unknown or new music discs
        player.sendMessage("You used an unrecognized music disc: " + discType.name());
    }
}
