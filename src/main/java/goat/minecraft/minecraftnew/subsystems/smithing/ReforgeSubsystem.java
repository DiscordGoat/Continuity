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
import org.bukkit.util.EulerAngle;
import org.bukkit.Material;

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
        ForgeSession session = new ForgeSession(key, item.clone(), tier, duration, duration);
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
            int total = dataConfig.getInt(key + ".total", time);
            boolean complete = dataConfig.getBoolean(key + ".complete", false);
            ForgeSession session = new ForgeSession(key, item, ReforgeManager.ReforgeTier.values()[tier], time, total);
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
            dataConfig.set(e.getKey() + ".total", s.totalTime);
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
        private final int totalTime;
        private boolean complete = false;
        private UUID standId;
        private UUID itemStandId;
        private final List<UUID> matStandIds = new ArrayList<>();
        private BukkitTask timerTask;
        private BukkitTask spinTask;
        private ItemStack reforgedItem;
        private int soundCounter = 0;

        public ForgeSession(String key, ItemStack item, ReforgeManager.ReforgeTier tier, int timeLeft, int totalTime) {
            this.locKey = key;
            this.item = item;
            this.tier = tier;
            this.timeLeft = timeLeft;
            this.totalTime = totalTime;
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

            spawnVisuals();
        }

        private void spawnVisuals() {
            Location center = fromKey(locKey).add(0.5, 1.4, 0.5);
            itemStandId = spawnItemStand(center, complete ? new ReforgeManager().applyReforge(item.clone(), tier) : item.clone());
            if (!complete) {
                spawnMatStands(center, getMatForTier(tier));
                startSpinTask();
            } else {
                reforgedItem = new ReforgeManager().applyReforge(item.clone(), tier);
                startSlowSpin();
            }
        }

        private UUID spawnItemStand(Location loc, ItemStack stack) {
            ArmorStand stand = (ArmorStand) loc.getWorld().spawnEntity(loc, EntityType.ARMOR_STAND);
            stand.setInvisible(true);
            stand.setMarker(true);
            stand.setInvulnerable(true);
            stand.setGravity(false);
            stand.setSmall(true);
            stand.setArms(true);
            ItemStack copy = stack.clone();
            copy.setAmount(1);
            stand.getEquipment().setItemInMainHand(copy);
            stand.setCustomNameVisible(false);
            stand.setRightArmPose(new EulerAngle(Math.toRadians(-90), 0, 0));
            return stand.getUniqueId();
        }

        private void spawnMatStands(Location center, Material mat) {
            double radius = 0.5;
            for (int i = 0; i < 4; i++) {
                double angle = Math.toRadians(i * 90.0);
                Location loc = center.clone().add(radius * Math.cos(angle), 0, radius * Math.sin(angle));
                ArmorStand stand = (ArmorStand) loc.getWorld().spawnEntity(loc, EntityType.ARMOR_STAND);
                stand.setInvisible(true);
                stand.setMarker(true);
                stand.setInvulnerable(true);
                stand.setGravity(false);
                stand.setSmall(true);
                stand.setArms(true);
                ItemStack it = new ItemStack(mat);
                stand.getEquipment().setItemInMainHand(it);
                stand.setCustomNameVisible(false);
                stand.setRightArmPose(new EulerAngle(Math.toRadians(-90), 0, 0));
                matStandIds.add(stand.getUniqueId());
            }
        }

        private Material getMatForTier(ReforgeManager.ReforgeTier tier) {
            return switch (tier) {
                case TIER_1 -> Material.COBBLESTONE;
                case TIER_2 -> Material.IRON_INGOT;
                case TIER_3 -> Material.GOLD_INGOT;
                case TIER_4 -> Material.AMETHYST_SHARD;
                case TIER_5 -> Material.DIAMOND;
                default -> Material.COBBLESTONE;
            };
        }

        private void startSpinTask() {
            spinTask = new BukkitRunnable() {
                double yaw = 0.0;
                double orbit = 0.0;

                @Override
                public void run() {
                    double pct = (double) timeLeft / totalTime;
                    double speed;
                    if (pct > 0.8) speed = 0.5;
                    else if (pct > 0.6) speed = 1.0;
                    else if (pct > 0.4) speed = 2.0;
                    else if (pct > 0.2) speed = 3.0;
                    else speed = 4.0;

                    yaw += speed * 2.0;
                    if (yaw >= 360.0) yaw -= 360.0;
                    orbit += speed * 0.05;

                    ArmorStand itemStand = (ArmorStand) Bukkit.getEntity(itemStandId);
                    if (itemStand != null && itemStand.isValid()) {
                        Location loc = itemStand.getLocation();
                        loc.setYaw((float) yaw);
                        itemStand.teleport(loc);
                        itemStand.setRightArmPose(new EulerAngle(Math.toRadians(yaw), 0, 0));
                    }

                    Location center = fromKey(locKey).add(0.5, 1.4, 0.5);
                    double radius = 0.5;
                    for (int i = 0; i < matStandIds.size(); i++) {
                        double ang = orbit + i * (Math.PI * 2 / matStandIds.size());
                        Entity ent = Bukkit.getEntity(matStandIds.get(i));
                        if (ent instanceof ArmorStand ms && ms.isValid()) {
                            Location loc = center.clone().add(radius * Math.cos(ang), 0, radius * Math.sin(ang));
                            ms.teleport(loc);
                        }
                    }
                }
            }.runTaskTimer(plugin, 1L, 1L);
        }

        private void startSlowSpin() {
            spinTask = new BukkitRunnable() {
                double yaw = 0.0;
                @Override
                public void run() {
                    ArmorStand itemStand = (ArmorStand) Bukkit.getEntity(itemStandId);
                    if (itemStand == null || !itemStand.isValid()) { cancel(); return; }
                    yaw += 1.0;
                    if (yaw >= 360.0) yaw -= 360.0;
                    Location loc = itemStand.getLocation();
                    loc.setYaw((float) yaw);
                    itemStand.teleport(loc);
                    itemStand.setRightArmPose(new EulerAngle(Math.toRadians(yaw), 0, 0));
                }
            }.runTaskTimer(plugin, 1L, 1L);
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
                    soundCounter++;
                    if (soundCounter % 4 == 0) {
                        Location base = fromKey(locKey).add(0.5, 1.0, 0.5);
                        base.getWorld().playSound(base, Sound.BLOCK_ANVIL_USE, 1f, 2f);
                        base.getWorld().spawnParticle(Particle.FLAME, base, 5, 0.1, 0.1, 0.1, 0.01);
                        base.getWorld().spawnParticle(Particle.SMOKE_NORMAL, base, 5, 0.1, 0.1, 0.1, 0.01);
                    }
                    saveAll();
                    if (timeLeft <= 0) {
                        cancel();
                        completeSession();
                    }
                }
            }.runTaskTimer(plugin, 20L, 20L);
        }

        private void completeSession() {
            complete = true;
            updateStand(ChatColor.GREEN + "Reforge Complete");
            if (spinTask != null) spinTask.cancel();
            for (UUID id : matStandIds) {
                Entity e = Bukkit.getEntity(id);
                if (e != null) e.remove();
            }
            matStandIds.clear();
            reforgedItem = new ReforgeManager().applyReforge(item.clone(), tier);
            ArmorStand itemStand = (ArmorStand) Bukkit.getEntity(itemStandId);
            if (itemStand != null && itemStand.isValid()) {
                ItemStack copy = reforgedItem.clone();
                copy.setAmount(1);
                itemStand.getEquipment().setItemInMainHand(copy);
            }
            startSlowSpin();
        }

        public void finish(Player player, Location anvilLoc) {
            if (timerTask != null) timerTask.cancel();
            updateStand(ChatColor.GREEN + "Reforge Complete");
            if (spinTask != null) spinTask.cancel();
            Entity e = Bukkit.getEntity(standId);
            if (e != null) e.remove();
            Entity itemEnt = Bukkit.getEntity(itemStandId);
            if (itemEnt != null) itemEnt.remove();
            for (UUID id : matStandIds) {
                Entity me = Bukkit.getEntity(id);
                if (me != null) me.remove();
            }
            ItemStack output = reforgedItem != null ? reforgedItem.clone() : new ReforgeManager().applyReforge(item.clone(), tier);
            player.getInventory().addItem(output);
            XPManager xp = new XPManager(MinecraftNew.getInstance());
            xp.addXP(player, "Smithing", 2000.0);
            player.playSound(anvilLoc, Sound.UI_TOAST_CHALLENGE_COMPLETE, 1, 1);
            anvilLoc.getWorld().strikeLightningEffect(anvilLoc);
        }
    }
}
