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
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
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

    private EnderDragon activeDragon;
    private BossBar dragonBar;
    private Dragon activeDragonType;
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
        EnderDragon dragon = world.getEntitiesByClass(EnderDragon.class).stream().findFirst().orElse(null);
        if (dragon == null) return;

        String customName = dragon.getCustomName();
        boolean nameValid = false;
        if (customName != null) {
            String stripped = ChatColor.stripColor(customName);
            for (Dragon d : DragonRegistry.getRegistered()) {
                if (ChatColor.stripColor(d.getDisplayName()).equalsIgnoreCase(stripped)) {
                    nameValid = true;
                    break;
                }
            }
        }

        boolean bossBarValid = dragonBar != null && activeDragon != null &&
                dragon.getUniqueId().equals(activeDragon.getUniqueId());

        if (!nameValid || !bossBarValid) {
            Dragon type = DragonRegistry.randomDragon();
            type.applyAttributes(dragon);
            activeDragon = dragon;
            activeDragonType = type;
            activePortalLoc = dragon.getLocation();

            if (dragonBar != null) {
                dragonBar.removeAll();
            }

            dragonBar = Bukkit.createBossBar(type.getDisplayName(), type.getBarColor(), type.getBarStyle());
            dragonBar.setStyle(type.getBarStyle());
            dragonBar.setColor(type.getBarColor());
            dragonBar.setProgress(dragon.getHealth() / dragon.getMaxHealth());
            
            for (Player p : world.getPlayers()) {
                dragonBar.addPlayer(p);
            }
        } else {
            dragonBar.addPlayer(player);
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
                int count = portalEyeCounts.getOrDefault(portalLoc, 0) + 1;
                portalEyeCounts.put(portalLoc, count);
                saveData();
                Bukkit.broadcastMessage(ChatColor.DARK_PURPLE + "An Eye of Ender was placed! Total: " + count);
                blockLoc.getWorld().playSound(blockLoc, Sound.ENTITY_ENDER_DRAGON_GROWL, 1f, 1f);
                blockLoc.getWorld().spawnParticle(Particle.DRAGON_BREATH, blockLoc.clone().add(0.5, 1, 0.5), 100, 0.3, 0.3, 0.3, 0.01);
                spawnFallingEye(blockLoc, player);
                if (count >= 12 && activeDragon == null) {
                    Bukkit.getScheduler().runTaskLater(plugin, () -> startDragonFight(portalLoc), 200L);
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

    private void startDragonFight(Location portalLoc) {
        World world = portalLoc.getWorld();
        if (world == null) return;

        activePortalLoc = portalLoc;
        activeDragonType = DragonRegistry.randomDragon();

        EnderDragon dragon = (EnderDragon) world.spawnEntity(portalLoc.clone().add(0, 5, 0), EntityType.ENDER_DRAGON);
        activeDragonType.applyAttributes(dragon);
        activeDragon = dragon;

        dragonBar = Bukkit.createBossBar(activeDragonType.getDisplayName(),
                activeDragonType.getBarColor(),
                activeDragonType.getBarStyle());
        for (Player p : world.getPlayers()) {
            dragonBar.addPlayer(p);
        }
        dragonBar.setProgress(1.0);

        // Repeating task to keep boss bar in sync with dragon health
        new BukkitRunnable() {
            @Override
            public void run() {
                if (activeDragon == null || dragonBar == null) {
                    cancel();
                    return;
                }
                if (activeDragon.isDead()) {
                    cancel();
                    return;
                }
                dragonBar.setProgress(activeDragon.getHealth() / activeDragon.getMaxHealth());
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }

    private void endDragonFight(Location portalLoc) {
        World world = portalLoc.getWorld();
        if (world == null) return;
        if (dragonBar != null) {
            dragonBar.removeAll();
            dragonBar = null;
        }
        activeDragon = null;
        activeDragonType = null;
        activePortalLoc = null;
        int radius = 20;
        int startX = portalLoc.getBlockX() - radius;
        int endX = portalLoc.getBlockX() + radius;
        int startY = portalLoc.getBlockY() - radius;
        int endY = portalLoc.getBlockY() + radius;
        int startZ = portalLoc.getBlockZ() - radius;
        int endZ = portalLoc.getBlockZ() + radius;
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
                entry.getKey().distanceSquared(portalLoc) <= radius * radius);
        saveData();
    }

    @EventHandler
    public void onDragonDeath(EntityDeathEvent event) {
        if (activeDragon != null && event.getEntity().getUniqueId().equals(activeDragon.getUniqueId())) {
            endDragonFight(activePortalLoc);
        }
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

