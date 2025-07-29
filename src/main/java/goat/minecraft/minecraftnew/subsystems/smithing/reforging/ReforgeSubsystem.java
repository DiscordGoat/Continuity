package goat.minecraft.minecraftnew.subsystems.smithing.reforging;

import goat.minecraft.minecraftnew.MinecraftNew;
import goat.minecraft.minecraftnew.subsystems.smithing.tierreforgelisteners.ReforgeManager;
import goat.minecraft.minecraftnew.other.skilltree.Skill;
import goat.minecraft.minecraftnew.other.skilltree.SkillTreeManager;
import goat.minecraft.minecraftnew.other.skilltree.Talent;
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
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Persistent reforge subsystem that mimics the behaviour of the brewing and culinary subsystems.
 * Each active reforge is tied to an anvil block location and counts down only while players are online.
 */
public class ReforgeSubsystem implements Listener {
    private static ReforgeSubsystem instance;

    private final JavaPlugin plugin;
    private final File dataFile;
    private YamlConfiguration dataConfig;

    // Map of locationKey -> session
    private final Map<String, ReforgeSession> activeSessions = new HashMap<>();

    // Developer override for timer durations (seconds). If <=0, use defaults
    private int devSeconds = -1;

    private ReforgeSubsystem(JavaPlugin plugin) {
        this.plugin = plugin;
        this.dataFile = new File(plugin.getDataFolder(), "reforge_sessions.yml");
        if (!dataFile.exists()) {
            try { dataFile.createNewFile(); } catch (IOException ignored) {}
        }
        this.dataConfig = YamlConfiguration.loadConfiguration(dataFile);
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        loadAll();
    }

    public static ReforgeSubsystem getInstance(JavaPlugin plugin) {
        if (instance == null) {
            instance = new ReforgeSubsystem(plugin);
        }
        return instance;
    }

    public void setDevSeconds(int secs) { this.devSeconds = secs; }

    // =============================================================
    // Session persistence
    // =============================================================
    private void loadAll() {
        for (String key : dataConfig.getKeys(false)) {
            String tierName = dataConfig.getString(key + ".tier");
            int time = dataConfig.getInt(key + ".timeLeft", 0);
            ItemStack item = dataConfig.getItemStack(key + ".item");
            if (tierName == null || item == null) continue;
            ReforgeManager.ReforgeTier tier = ReforgeManager.ReforgeTier.valueOf(tierName);
            ReforgeSession session = new ReforgeSession(key, tier, item);
            session.timeRemaining = time;
            session.spawnStands();
            if (time > 0) session.startTimer();
            activeSessions.put(key, session);
        }
    }

    public void saveAll() {
        for (String k : dataConfig.getKeys(false)) {
            dataConfig.set(k, null);
        }
        for (Map.Entry<String, ReforgeSession> e : activeSessions.entrySet()) {
            ReforgeSession s = e.getValue();
            dataConfig.set(e.getKey() + ".tier", s.targetTier.name());
            dataConfig.set(e.getKey() + ".timeLeft", s.timeRemaining);
            dataConfig.set(e.getKey() + ".item", s.item);
        }
        try { dataConfig.save(dataFile); } catch (IOException ignored) {}
    }

    // =============================================================
    // API
    // =============================================================
    public boolean hasSessionAt(Block anvil) {
        return activeSessions.containsKey(locKey(anvil.getLocation()));
    }

    public void startReforge(Block anvil, ItemStack item, ReforgeManager.ReforgeTier targetTier, Player player) {
        String key = locKey(anvil.getLocation());
        if (activeSessions.containsKey(key)) return; // already in progress
        ReforgeSession s = new ReforgeSession(key, targetTier, item.clone());
        s.timeRemaining = computeDurationSeconds(targetTier, player);
        s.spawnStands();
        s.startTimer();
        activeSessions.put(key, s);
        saveAll();
    }

    private int computeDurationSeconds(ReforgeManager.ReforgeTier tier, Player p) {
        int secs;
        if (devSeconds > 0) secs = devSeconds;
        else {
            switch (tier) {
                case TIER_1 -> secs = 60; // 1 min
                case TIER_2 -> secs = 5 * 60;
                case TIER_3 -> secs = 20 * 60;
                case TIER_4 -> secs = 60 * 60;
                case TIER_5 -> secs = 180 * 60;
                default -> secs = 60;
            }
        }
        if (p != null && SkillTreeManager.getInstance() != null) {
            int total = 0;
            total += SkillTreeManager.getInstance().getTalentLevel(p.getUniqueId(), Skill.SMITHING, Talent.FORGE_LABS_I);
            total += SkillTreeManager.getInstance().getTalentLevel(p.getUniqueId(), Skill.SMITHING, Talent.FORGE_LABS_II);
            total += SkillTreeManager.getInstance().getTalentLevel(p.getUniqueId(), Skill.SMITHING, Talent.FORGE_LABS_III);
            total += SkillTreeManager.getInstance().getTalentLevel(p.getUniqueId(), Skill.SMITHING, Talent.FORGE_LABS_IV);
            total += SkillTreeManager.getInstance().getTalentLevel(p.getUniqueId(), Skill.SMITHING, Talent.FORGE_LABS_V);
            secs = (int)Math.ceil(secs * (1 - total * 0.05));
        }
        return secs;
    }

    // Handle player interactions with anvils for speeding up or collecting reforges
    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getClickedBlock() == null) return;
        Block block = event.getClickedBlock();
        if (!AnvilRepair.ANVILS.contains(block.getType())) return;
        String key = locKey(block.getLocation());
        if (!activeSessions.containsKey(key)) return;
        ReforgeSession s = activeSessions.get(key);
        Player player = event.getPlayer();

        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            ItemStack hand = player.getInventory().getItemInMainHand();
            if (hand != null && hand.getType() == Material.IRON_INGOT && s.timeRemaining > 0) {
                int amt = hand.getAmount();
                double reduction = s.timeRemaining * 0.01 * amt;
                s.timeRemaining = (int)Math.max(0, s.timeRemaining - reduction);
                hand.setAmount(0);
                player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 1f, 2f);
                s.updateTimerStand();
                saveAll();
            }
            event.setCancelled(true);
        } else if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
            if (s.timeRemaining <= 0) {
                finalizeSession(block.getLocation(), s, player);
                activeSessions.remove(key);
                saveAll();
                event.setCancelled(true);
            }
        }
    }

    private void finalizeSession(Location loc, ReforgeSession session, Player player) {
        ReforgeManager manager = new ReforgeManager();
        ItemStack result = manager.applyReforge(session.item.clone(), session.targetTier);
        loc.getWorld().strikeLightningEffect(loc);
        loc.getWorld().spawnParticle(Particle.LAVA, loc.add(0.5,1,0.5), 20, 0.2,0.2,0.2,0.01);
        loc.getWorld().playSound(loc, Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1f);
        if (player != null) {
            XPManager xp = new XPManager(plugin);
            xp.addXP(player, "Smithing", 2000.0);
            player.getInventory().addItem(result);
            player.sendMessage(ChatColor.GOLD + "Reforge Complete!");
        } else {
            loc.getWorld().dropItemNaturally(loc, result);
        }
        session.destroy();
    }

    private String locKey(Location loc) {
        return loc.getWorld().getName() + ":" + loc.getBlockX() + ":" + loc.getBlockY() + ":" + loc.getBlockZ();
    }

    // =============================================================
    // Inner Session class
    // =============================================================
    private class ReforgeSession {
        final String locKey;
        final ReforgeManager.ReforgeTier targetTier;
        final ItemStack item;
        int timeRemaining;
        BukkitTask timerTask;
        UUID itemStand;
        UUID timerStand;
        final List<UUID> matStands = new ArrayList<>();

        ReforgeSession(String key, ReforgeManager.ReforgeTier tier, ItemStack item) {
            this.locKey = key;
            this.targetTier = tier;
            this.item = item;
        }

        void spawnStands() {
            Location base = fromKey(locKey).add(0.5, 1, 0.5);
            World w = base.getWorld();
            if (w == null) return;
            ArmorStand stand = (ArmorStand) w.spawnEntity(base.clone().add(0,0.7,0), EntityType.ARMOR_STAND);
            stand.setInvisible(true);
            stand.setMarker(true);
            stand.setInvulnerable(true);
            stand.getEquipment().setItemInMainHand(item.clone());
            stand.setCustomNameVisible(false);
            itemStand = stand.getUniqueId();
            // timer stand
            ArmorStand timer = (ArmorStand) w.spawnEntity(base.clone().add(0,1.5,0), EntityType.ARMOR_STAND);
            timer.setInvisible(true);
            timer.setCustomNameVisible(true);
            timer.setGravity(false);
            timer.setInvulnerable(true);
            timer.setMarker(true);
            timer.setCustomName(ChatColor.YELLOW + formatTime(timeRemaining));
            timerStand = timer.getUniqueId();
            // simple circle of materials (cobblestone) purely cosmetic
            for (int i=0;i<4;i++) {
                Location loc = base.clone().add(Math.cos(i*Math.PI/2)*0.5, 0.3, Math.sin(i*Math.PI/2)*0.5);
                ArmorStand m = (ArmorStand) w.spawnEntity(loc, EntityType.ARMOR_STAND);
                m.setInvisible(true);
                m.setMarker(true);
                m.setSmall(true);
                m.setInvulnerable(true);
                m.setGravity(false);
                m.getEquipment().setItemInMainHand(new ItemStack(Material.IRON_INGOT));
                matStands.add(m.getUniqueId());
            }
        }

        void destroy() {
            remove(itemStand); remove(timerStand); for (UUID u:matStands) remove(u);
            if (timerTask!=null) timerTask.cancel();
        }

        void startTimer() {
            if (timerTask != null) timerTask.cancel();
            timerTask = new BukkitRunnable() {
                @Override public void run() {
                    if (Bukkit.getOnlinePlayers().isEmpty()) return; // pause if no players
                    timeRemaining--;
                    updateTimerStand();
                    if (timeRemaining <= 0) {
                        cancel();
                    }
                }
            }.runTaskTimer(plugin, 20L, 20L);
        }

        void updateTimerStand() {
            Entity e = Bukkit.getEntity(timerStand);
            if (e instanceof ArmorStand as) {
                as.setCustomName(ChatColor.YELLOW + formatTime(timeRemaining));
            }
        }
    }

    private void remove(UUID id) {
        if (id == null) return;
        Entity e = Bukkit.getEntity(id);
        if (e != null) e.remove();
    }

    private Location fromKey(String key) {
        String[] parts = key.split(":");
        World w = Bukkit.getWorld(parts[0]);
        int x = Integer.parseInt(parts[1]);
        int y = Integer.parseInt(parts[2]);
        int z = Integer.parseInt(parts[3]);
        return new Location(w,x,y,z);
    }

    private String formatTime(int secs) {
        int m = secs/60; int s = secs%60;
        if (m>0) return m+"m"+s+"s"; else return s+"s";
    }
}
