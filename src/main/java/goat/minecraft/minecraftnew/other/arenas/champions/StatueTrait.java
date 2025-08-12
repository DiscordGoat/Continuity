package goat.minecraft.minecraftnew.other.arenas.champions;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.MemoryNPCDataStore;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.npc.NPCRegistry;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.util.DataKey;
import net.citizensnpcs.trait.LookClose;
import net.citizensnpcs.trait.SkinTrait;
import net.citizensnpcs.api.trait.trait.Equipment;
import org.bukkit.*;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Set;

/**
 * Trait for statue entities that spawn champions when hit by players.
 * Plays heartbeat sounds when players are nearby and awakens with stone particles when hit.
 */
public class StatueTrait extends Trait implements Listener {
    private final JavaPlugin plugin;
    private final ChampionType championType;
    private final Set<ChampionBlessing> blessings;
    
    // Task IDs for cleanup
    private int heartbeatTaskId = -1;
    
    // Constants
    private static final String STATUE_SKIN_VALUE = "ewogICJ0aW1lc3RhbXAiIDogMTc1NDk0OTI5MzMzMCwKICAicHJvZmlsZUlkIiA6ICJjOWM5YzkwOWIxNTI0ZDgzODY5NzU2OTE0M2JmNTY4OCIsCiAgInByb2ZpbGVOYW1lIiA6ICJ6dWtlbWF4IiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlL2Y2MGRlMThhMmU2MmZmOGQ3ZDBhMzdkYjIxMThhMjcyZmIwYzQ5MTJkZmVmMjRkNGFhY2FmMjM5NmU4MTQwODMiCiAgICB9CiAgfQp9";
    private static final String STATUE_SKIN_SIGNATURE = "A/zgrDRxWjjkqMy13VjacV9X1NC6E9ZRaw5MeJ/FrVRtDNNAQKX9jJ0T0JoO9Wz4DKdnhFISik26VKOi2w6QSWUZ5IOmEAuWvXQdz4zXJUcV5F72aGMqmoqIWudCnBi88DLL66nbr4ylUtTMhkSUt2eBqUs2QsR43ZJ9ZbeT7oKR2B6P/VhCUsw64seIo8YtdOef9nQSCNrZiYYQ2oE7M3I7xgmIvXDAtPuZgeid/9/tXnNkdS6PABRHphh18NBtqEmuw1nJtoN4Zy4iApwveb637IDhNH6u83YxS+SDIl0qkrJ0kK6MI1B9SvLfkOWsLp8eAJtMi1zJXgi8Atu/U8obQ1g0AQDxjom/EmTaPNtDJ6Uzpw6JZAl8DUlFiQ38038+JPhKZWt5EFH926ER/Ms3bjGWDBfY9bITo6VvBsXaA4T0q/2vUBJykC6qoMaSrJogGhERcX5P0YutOyezlAkJvyKWFlUj50Wlp1Qoa6boJJbi8hcw8KxISIm9J5g1noOkHgG6+rSMEF9sk7uD0JpDUh6XW+aEkqg3Z+trAABDxUhQZ5dhCIyIHVbWD+s3oThUJQsXDot1EBlrlDjciNDqqSjAkZVb095sNdqW7u7iYWK06GbvH55BfWoAJAtFE79s4l5BRVKOtEQ/B9hDc0BZqRx7RnUzDk0aFTIY8rk=";

    public StatueTrait(JavaPlugin plugin, ChampionType championType, Set<ChampionBlessing> blessings) {
        super("statue_trait");
        this.plugin = plugin;
        this.championType = championType;
        this.blessings = blessings;
    }

    @Override
    public void onAttach() {
        npc.setProtected(false);
        // Register events for hit detection
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        initializeStatue();
        startHeartbeatLoop();
    }

    /**
     * Initializes the statue with appropriate appearance and behavior.
     */
    private void initializeStatue() {
        // Set statue skin with delay to ensure NPC is properly spawned
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (!npc.isSpawned()) return;
            
            // Set statue skin using corpse logic
            SkinTrait skin = npc.getOrAddTrait(SkinTrait.class);
            skin.setFetchDefaultSkin(false);
            skin.setShouldUpdateSkins(false);
            skin.setSkinPersistent("champion_statue", STATUE_SKIN_SIGNATURE, STATUE_SKIN_VALUE);
            
            // Remove all equipment
            Equipment equipment = npc.getOrAddTrait(Equipment.class);
            equipment.set(Equipment.EquipmentSlot.HELMET, null);
            equipment.set(Equipment.EquipmentSlot.CHESTPLATE, null);
            equipment.set(Equipment.EquipmentSlot.LEGGINGS, null);
            equipment.set(Equipment.EquipmentSlot.BOOTS, null);
            equipment.set(Equipment.EquipmentSlot.HAND, null);
            
            // Make statue look down and disable look close
            LookClose lookClose = npc.getOrAddTrait(LookClose.class);
            lookClose.lookClose(false);
            
            // Set statue name to be ambiguous (no champion name revealed)
            npc.setName("Ancient Statue");
            if (npc.getEntity() instanceof LivingEntity living) {
                living.setCustomName("Ancient Statue");
                living.setCustomNameVisible(true);
                
                Location lookDown = living.getLocation();
                lookDown.setPitch(90f); // Look straight down
                living.teleport(lookDown);
            }
        }, 5L);
    }

    /**
     * Starts the heartbeat sound loop when players are nearby.
     */
    private void startHeartbeatLoop() {
        heartbeatTaskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
            if (!npc.isSpawned()) return;
            
            LivingEntity statue = (LivingEntity) npc.getEntity();
            Player nearestPlayer = getNearestPlayer(statue.getLocation());
            
            // Play heartbeat sound if player is within 15 blocks
            if (nearestPlayer != null) {
                double distance = statue.getLocation().distance(nearestPlayer.getLocation());
                if (distance <= 15.0) {
                    // Play heartbeat sound to nearby players
                    statue.getWorld().playSound(statue.getLocation(), Sound.ENTITY_WARDEN_HEARTBEAT, 1.0f, 0.8f);
                }
            }
            
        }, 100L, 100L); // Every 5 seconds
    }

    /**
     * Handles statue being hit - spawns champion with stone particle effects.
     */
    @EventHandler
    public void onStatueHit(EntityDamageByEntityEvent event) {
        if (event.getEntity() != npc.getEntity()) return;
        if (!(event.getDamager() instanceof Player player)) return;
        
        // Cancel damage to statue
        event.setCancelled(true);
        
        // Awaken the champion
        awakenChampion(player);
    }

    /**
     * Spawns the actual champion using corpse logic and removes this statue.
     */
    private void awakenChampion(Player triggeringPlayer) {
        if (!npc.isSpawned()) return;
        
        Location statueLocation = Bukkit.getWorld("world").getHighestBlockAt(npc.getEntity().getLocation()).getLocation();
        
        // Stone particle shower effect
        statueLocation.getWorld().spawnParticle(Particle.BLOCK, statueLocation.add(0, 1, 0), 500,
                1.0, 1.0, 1.0, 0.1, Material.STONE.createBlockData());
        
        // Dramatic awakening sound
        statueLocation.getWorld().playSound(statueLocation, Sound.ENTITY_ENDER_DRAGON_GROWL, 2.0f, 0.6f);
        
        // Create champion NPC using corpse logic
        NPCRegistry registry = CitizensAPI.createAnonymousNPCRegistry(new MemoryNPCDataStore());
        NPC champion = registry.createNPC(EntityType.PLAYER, championType.getName());
        SkinTrait skin = npc.getOrAddTrait(SkinTrait.class);
        skin.setFetchDefaultSkin(false);
        skin.setShouldUpdateSkins(false);
        skin.setSkinPersistent("champion", championType.getSkinSig(), championType.getSkinValue());
        champion.spawn(statueLocation);
        if (champion.getEntity() instanceof Player player) {
            player.setCustomName(championType.getName());
            player.setCustomNameVisible(true);
            // 2: Set skin

            // 3: Set armor contents via Citizens inventory trait
            ChampionEquipmentUtil.setArmorContentsFromFile(plugin, npc, championType.getArmorFile());
            champion.setProtected(false);
            ItemStack sword = ChampionEquipmentUtil.getItemFromFile(plugin, championType.getSwordFile());

            Bukkit.getScheduler().runTask(plugin, () -> {
                Equipment eq = champion.getOrAddTrait(Equipment.class);
                ItemStack[] armor = ChampionEquipmentUtil.getArmorForEquipment(plugin, championType.getArmorFile());
                eq.set(Equipment.EquipmentSlot.HELMET,     armor[0]);
                eq.set(Equipment.EquipmentSlot.CHESTPLATE, armor[1]);
                eq.set(Equipment.EquipmentSlot.LEGGINGS,   armor[2]);
                eq.set(Equipment.EquipmentSlot.BOOTS,      armor[3]);
                eq.set(Equipment.EquipmentSlot.HAND,       sword);
            });

// main hand (if you still want it)
            ChampionEquipmentUtil.setHeldItemFromFile(plugin, (Player) champion.getEntity(), championType.getSwordFile());
        }
        
        // Add simplified ChampionTrait
        champion.addTrait(new ChampionTrait(plugin, championType, blessings));
        npc.destroy();
    }

    private Player getNearestPlayer(Location loc) {
        double best = Double.MAX_VALUE;
        Player closest = null;
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.getGameMode() == GameMode.SPECTATOR) continue;
            double d = p.getLocation().distanceSquared(loc);
            if (d < best) {
                best = d;
                closest = p;
            }
        }
        return closest;
    }

    @Override
    public void onRemove() {
        if (heartbeatTaskId != -1) {
            Bukkit.getScheduler().cancelTask(heartbeatTaskId);
        }
    }

    @Override
    public void load(DataKey key) {}
    
    @Override
    public void save(DataKey key) {}
    
    // Getters for debugging/monitoring
    public ChampionType getChampionType() { return championType; }
    public Set<ChampionBlessing> getBlessings() { return blessings; }
}
