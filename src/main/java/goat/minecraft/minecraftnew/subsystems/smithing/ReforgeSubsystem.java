package goat.minecraft.minecraftnew.subsystems.smithing;

import goat.minecraft.minecraftnew.MinecraftNew;
import goat.minecraft.minecraftnew.other.skilltree.Skill;
import goat.minecraft.minecraftnew.other.skilltree.SkillTreeManager;
import goat.minecraft.minecraftnew.other.skilltree.Talent;
import goat.minecraft.minecraftnew.subsystems.smithing.tierreforgelisteners.ReforgeManager;
import goat.minecraft.minecraftnew.utils.devtools.XPManager;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Handles timed reforging sessions at anvils.
 */
public class ReforgeSubsystem implements Listener {
    private static ReforgeSubsystem instance;
    private final JavaPlugin plugin;
    private final File dataFile;
    private YamlConfiguration dataConfig;
    private final Map<String, ForgeSession> activeSessions = new HashMap<>();
    private int devSeconds = -1;

    private ReforgeSubsystem(JavaPlugin plugin) {
        this.plugin = plugin;
        dataFile = new File(plugin.getDataFolder(), "reforge_sessions.yml");
        if (!dataFile.exists()) {
            try { dataFile.createNewFile(); } catch (IOException ignored) {}
        }
        dataConfig = YamlConfiguration.loadConfiguration(dataFile);
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public static ReforgeSubsystem getInstance(JavaPlugin plugin) {
        if (instance == null) {
            instance = new ReforgeSubsystem(plugin);
            instance.onEnable();
        }
        return instance;
    }

    public void onEnable() {
        loadAll();
    }

    public void onDisable() {
        saveAll();
    }

    public void setDevSeconds(int secs) {
        this.devSeconds = secs;
    }

    public void startReforge(Location anvilLoc, ItemStack item, ReforgeManager.ReforgeTier tier, Player player) {
        String key = toKey(anvilLoc);
        if (activeSessions.containsKey(key)) {
            player.sendMessage(ChatColor.RED + "That anvil is already reforging!");
            return;
        }
        int duration = getDurationForTier(tier);
        if (devSeconds > 0) duration = devSeconds;
        SkillTreeManager manager = SkillTreeManager.getInstance();
        if (manager != null) {
            int lvl = 0;
            lvl += manager.getTalentLevel(player.getUniqueId(), Skill.SMITHING, Talent.FORGE_LABORATORIES_I);
            lvl += manager.getTalentLevel(player.getUniqueId(), Skill.SMITHING, Talent.FORGE_LABORATORIES_II);
            lvl += manager.getTalentLevel(player.getUniqueId(), Skill.SMITHING, Talent.FORGE_LABORATORIES_III);
            lvl += manager.getTalentLevel(player.getUniqueId(), Skill.SMITHING, Talent.FORGE_LABORATORIES_IV);
            lvl += manager.getTalentLevel(player.getUniqueId(), Skill.SMITHING, Talent.FORGE_LABORATORIES_V);
            if (lvl > 0) {
                duration = (int) Math.ceil(duration * (1 - lvl * 0.02));
            }
        }
        ForgeSession session = new ForgeSession(key, item.clone(), tier, duration);
        activeSessions.put(key, session);
        session.spawnStand();
        session.startTimer();
        saveAll();
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() == Action.LEFT_CLICK_BLOCK && event.getClickedBlock() != null) {
            Block block = event.getClickedBlock();
            if (!AnvilRepair.ANVILS.contains(block.getType())) return;
            String key = toKey(block.getLocation());
            ForgeSession session = activeSessions.get(key);
            if (session != null && session.isComplete()) {
                event.setCancelled(true);
                activeSessions.remove(key);
                session.finish(event.getPlayer(), block.getLocation());
                saveAll();
            }
        }
    }

    private int getDurationForTier(ReforgeManager.ReforgeTier tier) {
        return switch (tier) {
            case TIER_1 -> 60; // common
            case TIER_2 -> 300; // uncommon
            case TIER_3 -> 1200; // rare
            case TIER_4 -> 3600; // epic
            case TIER_5 -> 10800; // legendary
            default -> 60;
        };
    }

    private String toKey(Location loc) {
        return loc.getWorld().getName() + ":" + loc.getBlockX() + ":" + loc.getBlockY() + ":" + loc.getBlockZ();
    }
    private Location fromKey(String key) {
        String[] p = key.split(":");
        World w = Bukkit.getWorld(p[0]);
        int x = Integer.parseInt(p[1]);
        int y = Integer.parseInt(p[2]);
        int z = Integer.parseInt(p[3]);
        return new Location(w, x, y, z);
    }

    private void loadAll() {
        dataConfig = YamlConfiguration.loadConfiguration(dataFile);
        for (String key : dataConfig.getKeys(false)) {
            ItemStack item = dataConfig.getItemStack(key + ".item");
            int tier = dataConfig.getInt(key + ".tier", 1);
            int time = dataConfig.getInt(key + ".time", 0);
            boolean complete = dataConfig.getBoolean(key + ".complete", false);
            ForgeSession session = new ForgeSession(key, item, ReforgeManager.ReforgeTier.values()[tier], time);
            session.complete = complete;
            session.spawnStand();
            if (!complete) {
                session.startTimer();
            }
            activeSessions.put(key, session);
        }
        plugin.getLogger().info("[ReforgeSubsystem] Loaded " + activeSessions.size() + " session(s).");
    }

    private void saveAll() {
        for (String k : dataConfig.getKeys(false)) {
            dataConfig.set(k, null);
        }
        for (Map.Entry<String, ForgeSession> e : activeSessions.entrySet()) {
            ForgeSession s = e.getValue();
            dataConfig.set(e.getKey() + ".item", s.item);
            dataConfig.set(e.getKey() + ".tier", s.tier.getTier());
            dataConfig.set(e.getKey() + ".time", s.timeLeft);
            dataConfig.set(e.getKey() + ".complete", s.complete);
        }
        try { dataConfig.save(dataFile); } catch (IOException e) { e.printStackTrace(); }
    }

    // ────────────────────────────────────────────────────────────────
    // ForgeSession class
    // ────────────────────────────────────────────────────────────────
    public class ForgeSession {
        private final String locKey;
        private final ItemStack item;
        private final ReforgeManager.ReforgeTier tier;
        private int timeLeft;
        private boolean complete = false;
        private UUID standId;
        private BukkitTask timerTask;

        public ForgeSession(String key, ItemStack item, ReforgeManager.ReforgeTier tier, int timeLeft) {
            this.locKey = key;
            this.item = item;
            this.tier = tier;
            this.timeLeft = timeLeft;
        }

        public boolean isComplete() { return complete; }

        public void spawnStand() {
            Location loc = fromKey(locKey).add(0.5, 1.7, 0.5);
            ArmorStand stand = (ArmorStand) loc.getWorld().spawnEntity(loc, EntityType.ARMOR_STAND);
            stand.setInvisible(true);
            stand.setCustomNameVisible(true);
            stand.setMarker(true);
            stand.setGravity(false);
            stand.setInvulnerable(true);
            stand.setCustomName(ChatColor.YELLOW + String.valueOf(timeLeft) + "s");
            standId = stand.getUniqueId();
        }

        private void updateStand(String text) {
            Entity e = Bukkit.getEntity(standId);
            if (e instanceof ArmorStand a && a.isValid()) {
                a.setCustomName(text);
            }
        }

        public void startTimer() {
            timerTask = new BukkitRunnable() {
                @Override
                public void run() {
                    if (Bukkit.getOnlinePlayers().isEmpty()) return;
                    if(devSeconds > 0){
                        timeLeft = devSeconds;
                        devSeconds = -1;
                    }
                    timeLeft--;
                    updateStand("" + ChatColor.YELLOW + timeLeft + "s");
                    saveAll();
                    if (timeLeft <= 0) {
                        complete = true;
                        cancel();
                        updateStand(ChatColor.GREEN + "Reforge Complete");
                    }
                }
            }.runTaskTimer(plugin, 20L, 20L);
        }

        public void finish(Player player, Location anvilLoc) {
            if (timerTask != null) timerTask.cancel();
            updateStand(ChatColor.GREEN + "Reforge Complete");
            Entity e = Bukkit.getEntity(standId);
            if (e != null) e.remove();
            ItemStack output = new ReforgeManager().applyReforge(item, tier);
            player.getInventory().addItem(output);
            XPManager xp = new XPManager(MinecraftNew.getInstance());
            xp.addXP(player, "Smithing", 2000.0);
            player.playSound(anvilLoc, Sound.UI_TOAST_CHALLENGE_COMPLETE, 1, 1);
            anvilLoc.getWorld().strikeLightningEffect(anvilLoc);
        }
    }
}
