package goat.minecraft.minecraftnew.subsystems.smithing.reforge;

import goat.minecraft.minecraftnew.MinecraftNew;
import goat.minecraft.minecraftnew.subsystems.smithing.tierreforgelisteners.ReforgeManager;
import goat.minecraft.minecraftnew.utils.devtools.XPManager;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Persistent subsystem that handles timed reforging on anvils.
 */
public class ReforgeSubsystem implements Listener {
    private static ReforgeSubsystem instance;
    private final JavaPlugin plugin;
    private final File dataFile;
    private YamlConfiguration dataConfig;

    // in seconds; dev override when >=0
    private int devSeconds = -1;

    // locationKey -> session
    private final Map<String, ReforgeSession> activeSessions = new HashMap<>();

    public ReforgeSubsystem(JavaPlugin plugin) {
        this.plugin = plugin;
        this.dataFile = new File(plugin.getDataFolder(), "reforge_sessions.yml");
        if (!dataFile.exists()) {
            try { dataFile.createNewFile(); } catch (IOException ignored) {}
        }
        this.dataConfig = YamlConfiguration.loadConfiguration(dataFile);
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public static ReforgeSubsystem getInstance(JavaPlugin plugin) {
        if (instance == null) {
            instance = new ReforgeSubsystem(plugin);
        }
        return instance;
    }

    public void onEnable() {
        loadAll();
    }

    public void onDisable() {
        saveAll();
    }

    public void setDevSeconds(int seconds) {
        this.devSeconds = seconds;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getClickedBlock() == null) return;
        if (event.getHand() != EquipmentSlot.HAND) return;
        Block block = event.getClickedBlock();
        if (!isAnvil(block)) return;
        String locKey = toLocKey(block.getLocation());
        ReforgeSession session = activeSessions.get(locKey);
        Player player = event.getPlayer();

        if (session != null) {
            // dev: reduce timer with iron ingots
            if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                ItemStack hand = player.getInventory().getItemInMainHand();
                if (hand.getType() == Material.IRON_INGOT && hand.getAmount() > 0) {
                    int amount = hand.getAmount();
                    session.reduceTimerPercent(amount);
                    hand.setAmount(0);
                    player.sendMessage(ChatColor.GREEN + "Reduced reforge time by " + amount + "%");
                }
            } else if (event.getAction() == Action.LEFT_CLICK_BLOCK && session.isComplete()) {
                // finalize
                ItemStack result = session.finish();
                player.getWorld().strikeLightningEffect(block.getLocation());
                player.getInventory().addItem(result);
                XPManager xp = new XPManager(plugin);
                xp.addXP(player, "Smithing", 2000.0);
                player.sendMessage(ChatColor.GOLD + "Reforge complete!");
                activeSessions.remove(locKey);
                dataConfig.set(locKey, null);
                try { dataConfig.save(dataFile); } catch (IOException ignored) {}
            }
            event.setCancelled(true);
        }
    }

    public void startSession(Block anvil, ItemStack item, ReforgeManager.ReforgeTier targetTier) {
        String key = toLocKey(anvil.getLocation());
        if (activeSessions.containsKey(key)) return;
        int seconds = switch (targetTier) {
            case TIER_1 -> 60;
            case TIER_2 -> 300;
            case TIER_3 -> 1200;
            case TIER_4 -> 3600;
            case TIER_5 -> 10800;
            default -> 60;
        };
        if (devSeconds >= 0) seconds = devSeconds;
        ReforgeSession session = new ReforgeSession(key, item.clone(), targetTier, seconds);
        activeSessions.put(key, session);
        session.spawn(anvil.getLocation());
        updateDB(key, session);
    }

    private void updateDB(String key, ReforgeSession s) {
        dataConfig.set(key + ".tier", s.tier.getTier());
        dataConfig.set(key + ".time", s.timeRemaining);
        dataConfig.set(key + ".item", s.item);
        try { dataConfig.save(dataFile); } catch (IOException ignored) {}
    }

    private void loadAll() {
        dataConfig = YamlConfiguration.loadConfiguration(dataFile);
        for (String key : dataConfig.getKeys(false)) {
            int tier = dataConfig.getInt(key + ".tier", 0);
            int time = dataConfig.getInt(key + ".time", 0);
            ItemStack item = dataConfig.getItemStack(key + ".item");
            ReforgeManager.ReforgeTier rt = new ReforgeManager().getReforgeTierByTier(tier);
            ReforgeSession session = new ReforgeSession(key, item, rt, time);
            activeSessions.put(key, session);
            Block anvil = fromLocKey(key).getBlock();
            session.spawn(anvil.getLocation());
            if (time > 0) session.resume();
        }
    }

    private void saveAll() {
        for (String k : dataConfig.getKeys(false)) dataConfig.set(k, null);
        for (Map.Entry<String, ReforgeSession> e : activeSessions.entrySet()) {
            updateDB(e.getKey(), e.getValue());
        }
        try { dataConfig.save(dataFile); } catch (IOException ignored) {}
    }

    private boolean isAnvil(Block b) {
        return b.getType() == Material.ANVIL || b.getType() == Material.CHIPPED_ANVIL || b.getType() == Material.DAMAGED_ANVIL;
    }

    private String toLocKey(Location loc) {
        return loc.getWorld().getName()+":"+loc.getBlockX()+":"+loc.getBlockY()+":"+loc.getBlockZ();
    }

    private Location fromLocKey(String key) {
        String[] p = key.split(":");
        World w = Bukkit.getWorld(p[0]);
        int x=Integer.parseInt(p[1]);
        int y=Integer.parseInt(p[2]);
        int z=Integer.parseInt(p[3]);
        return new Location(w,x,y,z);
    }

    class ReforgeSession {
        final String locKey;
        final ItemStack item;
        final ReforgeManager.ReforgeTier tier;
        int timeRemaining;
        boolean complete=false;
        UUID itemStandUUID;
        final List<UUID> matStands = new ArrayList<>();
        BukkitTask timerTask;

        ReforgeSession(String key, ItemStack item, ReforgeManager.ReforgeTier tier, int time) {
            this.locKey = key;
            this.item = item;
            this.tier = tier;
            this.timeRemaining = time;
        }

        void spawn(Location loc) {
            Location standLoc = loc.clone().add(0.5, 1.2, 0.5);
            ArmorStand stand = (ArmorStand) loc.getWorld().spawnEntity(standLoc, EntityType.ARMOR_STAND);
            stand.setInvisible(true);
            stand.setMarker(true);
            stand.setInvulnerable(true);
            stand.setGravity(false);
            stand.setCustomNameVisible(false);
            stand.getEquipment().setItemInMainHand(item.clone());
            itemStandUUID = stand.getUniqueId();

            for (int i=0;i<4;i++) {
                double angle = i*Math.PI/2;
                Location ml = standLoc.clone().add(Math.cos(angle)*0.5, 0, Math.sin(angle)*0.5);
                ArmorStand m = (ArmorStand) loc.getWorld().spawnEntity(ml, EntityType.ARMOR_STAND);
                m.setInvisible(true);
                m.setMarker(true);
                m.setInvulnerable(true);
                m.setGravity(false);
                m.getEquipment().setItemInMainHand(new ItemStack(Material.IRON_INGOT));
                matStands.add(m.getUniqueId());
            }
            startSpinning();
        }

        void startSpinning() {
            timerTask = new BukkitRunnable() {
                double yaw = 0;
                @Override
                public void run() {
                    if (Bukkit.getOnlinePlayers().isEmpty()) return;
                    if (timeRemaining>0) timeRemaining--; else {complete=true; cancel(); updateDB(locKey, ReforgeSession.this); return;}
                    Entity s = Bukkit.getEntity(itemStandUUID);
                    if (s instanceof ArmorStand stand) {
                        yaw+=6; if(yaw>=360) yaw-=360; Location l=stand.getLocation(); l.setYaw((float)yaw); stand.teleport(l);
                    }
                }
            }.runTaskTimer(plugin,20,20);
        }

        void resume() { startSpinning(); }

        void reduceTimerPercent(int percent) {
            int reduce = (int)Math.ceil(timeRemaining*percent/100.0);
            timeRemaining = Math.max(0, timeRemaining - reduce);
            updateDB(locKey, this);
        }

        boolean isComplete(){ return complete || timeRemaining<=0; }

        ItemStack finish() {
            if (timerTask!=null) timerTask.cancel();
            for(UUID u:matStands){ Entity e=Bukkit.getEntity(u); if(e!=null) e.remove(); }
            matStands.clear();
            Entity e = Bukkit.getEntity(itemStandUUID); if (e!=null) e.remove();
            ItemStack out = new ReforgeManager().applyReforge(item.clone(), tier);
            complete=true; timeRemaining=0;
            updateDB(locKey, this);
            return out;
        }
    }
}
