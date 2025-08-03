package goat.minecraft.minecraftnew.subsystems.dragons;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import goat.minecraft.minecraftnew.MinecraftNew;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.EndPortalFrame;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.ArmorStand;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.profile.PlayerProfile;
import org.bukkit.profile.PlayerTextures;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import goat.minecraft.minecraftnew.utils.dimensions.end.BetterEnd;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class DragonFightManager implements Listener {

    private static final String EYE_TEXTURE = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMTk4YTQ5Y2E1NGMzZWE2N2E4NmVjOGI5ZjE2YmRmNDZhYTVlZmM1YWVlZmI3YTE5Y2NjYzc5NjJlODIxYTU5OSJ9fX0=";
    private final MinecraftNew plugin;
    private final File gatewaysFile;
    private final YamlConfiguration gatewaysConfig;
    private final Map<Location, Integer> portalEyeCounts = new HashMap<>();

    private DragonFight activeFight;
    private Location activePortalLoc;

    public DragonFightManager(MinecraftNew plugin) {
        this.plugin = plugin;
        gatewaysFile = new File(plugin.getDataFolder(), "gateways.yml");
        if (!gatewaysFile.exists()) {
            try {
                gatewaysFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        gatewaysConfig = YamlConfiguration.loadConfiguration(gatewaysFile);
        loadData();
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    private void loadData() {
        var section = gatewaysConfig.getConfigurationSection("gateways");
        if (section == null) return;
        for (String key : section.getKeys(false)) {
            Location loc = parseLocationKey(key);
            if (loc != null) {
                portalEyeCounts.put(loc, section.getInt(key));
            }
        }
    }

    private Location parseLocationKey(String key) {
        String[] parts = key.split("_");
        if (parts.length != 4) return null;
        World world = Bukkit.getWorld(parts[0]);
        if (world == null) return null;
        int x = Integer.parseInt(parts[1]);
        int y = Integer.parseInt(parts[2]);
        int z = Integer.parseInt(parts[3]);
        return new Location(world, x, y, z);
    }

    private String locationKey(Location loc) {
        return loc.getWorld().getName() + "_" + loc.getBlockX() + "_" + loc.getBlockY() + "_" + loc.getBlockZ();
    }

    public void onDisable() {
        saveData();
    }

    private void saveData() {
        gatewaysConfig.set("gateways", null);
        for (Map.Entry<Location, Integer> entry : portalEyeCounts.entrySet()) {
            gatewaysConfig.set("gateways." + locationKey(entry.getKey()), entry.getValue());
        }
        try {
            gatewaysConfig.save(gatewaysFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        handlePlayerEnterWorld(event.getPlayer(), event.getPlayer().getWorld());
    }

    @EventHandler
    public void onPlayerChangedWorld(PlayerChangedWorldEvent event) {
        handlePlayerEnterWorld(event.getPlayer(), event.getPlayer().getWorld());
    }

    private void handlePlayerEnterWorld(Player player, World world) {
        if (!world.getName().equals("custom_end")) {
            if (activeFight != null && activeFight.getBossBar() != null) {
                activeFight.getBossBar().removePlayer(player);
            }
            return;
        }

        if (activeFight == null && activePortalLoc != null) {
            startDragonFight(world);
        }

        if (activeFight != null && activeFight.getBossBar() != null) {
            activeFight.getBossBar().addPlayer(player);
        }
    }

    @EventHandler
    public void onPlayerPlaceEye(PlayerInteractEvent e) {
        if (e.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (e.getClickedBlock() == null) return;
        if (e.getClickedBlock().getType() != Material.END_PORTAL_FRAME) return;
        ItemStack item = e.getItem();
        if (item == null || item.getType() != Material.ENDER_EYE) return;
        Player player = e.getPlayer();
        int before = item.getAmount();
        EquipmentSlot slot = e.getHand();
        Location blockLoc = e.getClickedBlock().getLocation();
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            ItemStack afterStack = slot == EquipmentSlot.HAND ? player.getInventory().getItemInMainHand() : player.getInventory().getItemInOffHand();
            int after = afterStack != null && afterStack.getType() == Material.ENDER_EYE ? afterStack.getAmount() : 0;
            EndPortalFrame frame = (EndPortalFrame) blockLoc.getBlock().getBlockData();
            boolean placed = before - 1 == after && frame.hasEye();
            if (placed) {
                Location portalLoc = findPortal(blockLoc);
                int count = countPortalEyes(portalLoc);
                portalEyeCounts.put(portalLoc, count);
                saveData();
                Bukkit.broadcastMessage(ChatColor.DARK_PURPLE + "An Eye of Ender was placed! Total: " + count);
                blockLoc.getWorld().playSound(blockLoc, Sound.ENTITY_ENDER_DRAGON_GROWL, 1f, 1f);
                blockLoc.getWorld().spawnParticle(Particle.DRAGON_BREATH, blockLoc.clone().add(0.5, 1, 0.5), 100, 0.3, 0.3, 0.3, 0.01);
                spawnFallingEye(blockLoc, player);
                if (count >= 12 && activeFight == null) {
                    activePortalLoc = portalLoc;
                    Bukkit.broadcastMessage(ChatColor.DARK_PURPLE + "The portal hums with energy...");
                }
            }
        }, 1L);
    }

    private Location findPortal(Location loc) {
        for (Location stored : portalEyeCounts.keySet()) {
            if (stored.getWorld().equals(loc.getWorld()) && stored.distanceSquared(loc) <= 400) {
                return stored;
            }
        }
        return new Location(loc.getWorld(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
    }

    private int countPortalEyes(Location portalLoc) {
        World world = portalLoc.getWorld();
        if (world == null) return 0;
        int radius = 5;
        int startX = portalLoc.getBlockX() - radius;
        int endX = portalLoc.getBlockX() + radius;
        int startY = portalLoc.getBlockY() - 1;
        int endY = portalLoc.getBlockY() + 1;
        int startZ = portalLoc.getBlockZ() - radius;
        int endZ = portalLoc.getBlockZ() + radius;
        int count = 0;
        for (int x = startX; x <= endX; x++) {
            for (int y = startY; y <= endY; y++) {
                for (int z = startZ; z <= endZ; z++) {
                    Block block = world.getBlockAt(x, y, z);
                    if (block.getType() == Material.END_PORTAL_FRAME) {
                        EndPortalFrame frame = (EndPortalFrame) block.getBlockData();
                        if (frame.hasEye()) {
                            count++;
                        }
                    }
                }
            }
        }
        return count;
    }

    private void startDragonFight(World world) {
        Dragon type = DragonRegistry.randomDragon();
        EnderDragon dragon = (EnderDragon) world.spawnEntity(world.getSpawnLocation().clone().add(0, 5, 0), EntityType.ENDER_DRAGON);
        type.applyAttributes(dragon);
        if (dragon.getAttribute(Attribute.GENERIC_MAX_HEALTH) != null) {
            dragon.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(type.getMaxHealth());
        }
        dragon.setHealth(type.getMaxHealth());
        if (dragon.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED) != null) {
            dragon.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED)
                    .setBaseValue(dragon.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).getBaseValue() * (type.getFlightSpeed() / 5.0));
        }
        activeFight = new DragonFight(type, dragon);

        BossBar bar = Bukkit.createBossBar(type.getDisplayName(), type.getBarColor(), type.getBarStyle());
        activeFight.setBossBar(bar);
        bar.setProgress(1.0);
        for (Player p : world.getPlayers()) {
            bar.addPlayer(p);
        }

        int cooldown = Math.max(15, 65 - type.getBaseRage() * 5);
        BukkitTask task = new BukkitRunnable() {
            @Override
            public void run() {
                double chance = (type.getBaseRage() * 10) / 100.0;
                if (Math.random() <= chance) {
                    plugin.getLogger().info("dragon has made a decision");
                }
            }
        }.runTaskTimer(plugin, cooldown * 20L, cooldown * 20L);
        activeFight.setDecisionTask(task);
    }

    private void endDragonFight() {
        if (activeFight != null && activeFight.getBossBar() != null) {
            activeFight.getBossBar().removeAll();
        }
        if (activeFight != null && activeFight.getDecisionTask() != null) {
            activeFight.getDecisionTask().cancel();
        }
        if (activePortalLoc != null) {
            World world = activePortalLoc.getWorld();
            int radius = 20;
            int startX = activePortalLoc.getBlockX() - radius;
            int endX = activePortalLoc.getBlockX() + radius;
            int startY = activePortalLoc.getBlockY() - radius;
            int endY = activePortalLoc.getBlockY() + radius;
            int startZ = activePortalLoc.getBlockZ() - radius;
            int endZ = activePortalLoc.getBlockZ() + radius;
            for (int x = startX; x <= endX; x++) {
                for (int y = startY; y <= endY; y++) {
                    for (int z = startZ; z <= endZ; z++) {
                        Block block = world.getBlockAt(x, y, z);
                        if (block.getType() == Material.END_PORTAL_FRAME) {
                            EndPortalFrame frame = (EndPortalFrame) block.getBlockData();
                            frame.setEye(false);
                            block.setBlockData(frame);
                        } else if (block.getType() == Material.END_PORTAL) {
                            block.setType(Material.AIR);
                        }
                    }
                }
            }
            portalEyeCounts.entrySet().removeIf(entry ->
                    entry.getKey().getWorld().equals(world) &&
                    entry.getKey().distanceSquared(activePortalLoc) <= radius * radius);
            saveData();
        }
        activeFight = null;
        activePortalLoc = null;
    }

    @EventHandler
    public void onDragonDamage(EntityDamageEvent event) {
        if (activeFight == null) return;
        if (!event.getEntity().getUniqueId().equals(activeFight.getDragon().getUniqueId())) return;
        event.setCancelled(true);
        double health = activeFight.getCurrentHealth() - event.getFinalDamage();
        activeFight.setCurrentHealth(Math.max(0, health));
        if (activeFight.getBossBar() != null) {
            activeFight.getBossBar().setProgress(activeFight.getCurrentHealth() / activeFight.getMaxHealth());
        }
        if (activeFight.getCurrentHealth() <= 0) {
            activeFight.getDragon().setHealth(0);
        }
    }

    @EventHandler
    public void onDragonDeath(EntityDeathEvent event) {
        if (activeFight != null && event.getEntity().getUniqueId().equals(activeFight.getDragon().getUniqueId())) {
            handleDragonVictory();
        }
    }

    private void handleDragonVictory() {
        if (activeFight == null) return;
        World world = activeFight.getDragon().getWorld();
        for (Player p : world.getPlayers()) {
            p.sendTitle(ChatColor.GOLD + "You Defeated the " + activeFight.getType().getName() + " Dragon!", "", 10, 70, 20);
        }
        if (activeFight.getDecisionTask() != null) {
            activeFight.getDecisionTask().cancel();
        }
        new BukkitRunnable() {
            int seconds = 60;

            @Override
            public void run() {
                if (seconds <= 0) {
                    for (Player p : world.getPlayers()) {
                        Location spawn = p.getBedSpawnLocation();
                        if (spawn == null) spawn = Bukkit.getWorlds().get(0).getSpawnLocation();
                        p.teleport(spawn);
                    }
                    endDragonFight();
                    BetterEnd.init(plugin);
                    cancel();
                    return;
                }
                for (Player p : world.getPlayers()) {
                    p.sendMessage(ChatColor.YELLOW + "Returning in " + seconds + "s");
                }
                seconds--;
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }

    private void spawnFallingEye(Location frameLoc, Player player) {
        Location start = frameLoc.clone().add(0.5, 1.5, 0.5);
        float yaw = player.getLocation().getYaw() + 180F;
        start.setYaw(yaw);
        ArmorStand stand = (ArmorStand) frameLoc.getWorld().spawnEntity(start, EntityType.ARMOR_STAND);
        stand.setSmall(true);
        stand.setBasePlate(false);
        stand.setGravity(false);
        stand.setVisible(false);
        stand.setRotation(yaw, 0F);
        stand.getEquipment().setHelmet(createEyeSkull());
        new BukkitRunnable() {
            int ticks = 0;
            @Override
            public void run() {
                if (!stand.isValid()) { cancel(); return; }
                stand.getWorld().spawnParticle(Particle.DRAGON_BREATH, stand.getLocation(), 5, 0.1, 0.1, 0.1, 0.01);
                stand.teleport(stand.getLocation().subtract(0, 0.1, 0));
                ticks++;
                if (ticks >= 10) {
                    stand.remove();
                    cancel();
                }
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    private ItemStack createEyeSkull() {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) head.getItemMeta();
        setCustomSkullTexture(meta, EYE_TEXTURE);
        head.setItemMeta(meta);
        return head;
    }

    private SkullMeta setCustomSkullTexture(SkullMeta skullMeta, String base64Json) {
        if (skullMeta == null || base64Json == null || base64Json.isEmpty()) {
            return skullMeta;
        }
        try {
            byte[] decoded = Base64.getDecoder().decode(base64Json);
            String json = new String(decoded, StandardCharsets.UTF_8);
            JsonObject root = JsonParser.parseString(json).getAsJsonObject();
            String urlText = root.getAsJsonObject("textures").getAsJsonObject("SKIN").get("url").getAsString();
            PlayerProfile profile = Bukkit.createPlayerProfile(UUID.randomUUID());
            PlayerTextures textures = profile.getTextures();
            textures.setSkin(new URL(urlText), PlayerTextures.SkinModel.CLASSIC);
            profile.setTextures(textures);
            skullMeta.setOwnerProfile(profile);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return skullMeta;
    }
}

