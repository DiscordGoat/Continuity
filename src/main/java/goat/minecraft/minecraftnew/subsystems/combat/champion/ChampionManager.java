package goat.minecraft.minecraftnew.subsystems.combat.champion;


import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import goat.minecraft.minecraftnew.MinecraftNew;
import goat.minecraft.minecraftnew.subsystems.combat.HostilityManager;
import goat.minecraft.minecraftnew.subsystems.combat.SpawnMonsters;
import goat.minecraft.minecraftnew.utils.devtools.XPManager;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.profile.PlayerProfile;
import org.bukkit.profile.PlayerTextures;
import org.bukkit.scheduler.BukkitRunnable;

import java.lang.reflect.Field;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ChampionManager implements Listener {
    private static ChampionManager instance;

    private final JavaPlugin plugin;
    private final XPManager xpManager;
    private final Map<UUID, KillInfo> killMap = new HashMap<>();
    private static final long WINDOW_MS = 5 * 60 * 1000L;

    private static class KillInfo {
        int notoriety;
        long start;
    }

    public static synchronized ChampionManager getInstance(JavaPlugin plugin) {
        if (instance == null) {
            instance = new ChampionManager(plugin);
        }
        return instance;
    }

    public static ChampionManager getInstance() {
        return instance;
    }

    private ChampionManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.xpManager = new XPManager(plugin);
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public void recordKill(Player player, Location loc) {
        if (player == null) return;
        long now = System.currentTimeMillis();
        KillInfo info = killMap.get(player.getUniqueId());
        if (info == null || now - info.start > WINDOW_MS) {
            info = new KillInfo();
            info.start = now;
            info.notoriety = 0;
        }
        info.notoriety += 1;
        killMap.put(player.getUniqueId(), info);
        if (info.notoriety >= 100) {
            spawnChampion(player, loc);
            info.notoriety = 0;
            info.start = now;
        }
    }

    private enum Tier {
        WRATH(100, 1,
                "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZjc3OWNmOTdlYzU2ZjgyMDQwNzM5NTU4ODZiMDNhZjJjNTZkOTk5YjEwMDU1N2VkYjVhMGJkYjFiNDdkYmUyNCJ9fX0=",
                ChatColor.WHITE + "Champion of Wrath", Color.WHITE),
        FURY(200, 3,
                "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYmE2OGM2ZDcyYzhkNzA0ZWNmNDczOWMzZWY5MDgyMTY3ZjBhZTQ2MWQ0ZTdmN2I5MDlhYjRmNjE1YTgxM2ExNiJ9fX0=",
                ChatColor.RED + "Champion of Fury", Color.BLACK),
        VENGEANCE(300, 6,
                "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMTY0MzIwNGE5ZDA3MjI4OGY3MmQyZmQwMmQxODljNWEzYjA0MTM4MzU4YzAyMmZiMmNkNTJlMTE1OTM4NDJmNyJ9fX0=",
                ChatColor.GOLD + "Champion of Vengeance", Color.fromRGB(255,215,0));

        final int level;
        final int speed;
        final String texture;
        final String display;
        final Color armorColor;
        Tier(int level,int speed,String texture,String display,Color armorColor){
            this.level=level;this.speed=speed;this.texture=texture;this.display=display;this.armorColor=armorColor;}
    }

    private void spawnChampion(Player player, Location loc) {
        int hostility = HostilityManager.getInstance(plugin).getPlayerHostility(player);
        Tier tier = hostility < 10 ? Tier.WRATH : hostility < 15 ? Tier.FURY : Tier.VENGEANCE;

        Zombie champion = (Zombie) loc.getWorld().spawnEntity(loc, EntityType.ZOMBIE);
        champion.setMetadata("championTier", new FixedMetadataValue(plugin, tier.ordinal()));
        champion.setCustomName(tier.display);
        champion.setCustomNameVisible(true);
        equipChampion(champion, tier);
        champion.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, tier.speed - 1, true));
        SpawnMonsters.getInstance(xpManager).applyMobAttributes(champion, tier.level);

        loc.getWorld().strikeLightningEffect(loc);
        loc.getWorld().spawnParticle(Particle.LARGE_SMOKE, loc, 40, 1,1,1,0.1);
        if (tier == Tier.FURY || tier == Tier.VENGEANCE) {
            loc.getWorld().playSound(loc, Sound.ENTITY_WITHER_SPAWN, 1f, 1f);
        }
        if (tier == Tier.VENGEANCE) {
            loc.getWorld().createExplosion(loc, 0F);
        }
    }

    private void equipChampion(Zombie zombie, Tier tier){
        EntityEquipment eq = zombie.getEquipment();
        if(eq==null) return;

        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta skullMeta = (SkullMeta) head.getItemMeta();
        skullMeta = setCustomSkullTexture(skullMeta, tier.texture);
        head.setItemMeta(skullMeta);

        ItemStack chest = coloredArmor(Material.LEATHER_CHESTPLATE, tier.armorColor);
        ItemStack legs = coloredArmor(Material.LEATHER_LEGGINGS, Color.RED);
        ItemStack boots = coloredArmor(Material.LEATHER_BOOTS, Color.RED);

        ItemStack weapon;
        if (tier == Tier.WRATH) {
            weapon = new ItemStack(Material.IRON_SWORD);
        } else if (tier == Tier.FURY) {
            weapon = new ItemStack(Material.DIAMOND_SWORD);
            weapon.addEnchantment(Enchantment.KNOCKBACK,5);
        } else {
            weapon = new ItemStack(Material.DIAMOND_AXE);
        }

        eq.setHelmet(head);
        eq.setChestplate(chest);
        eq.setLeggings(legs);
        eq.setBoots(boots);
        eq.setItemInMainHand(weapon);
    }

    private ItemStack coloredArmor(Material mat, Color color){
        ItemStack item = new ItemStack(mat);
        if(item.getItemMeta() instanceof LeatherArmorMeta meta){
            meta.setColor(color);
            item.setItemMeta(meta);
        }
        return item;
    }

    private SkullMeta setCustomSkullTexture(SkullMeta skullMeta, String base64Json) {
        if (skullMeta == null || base64Json == null || base64Json.isEmpty()) {
            return skullMeta;
        }

        try {
            // 1) Decode the JSON payload from your Base64 string
            byte[] decoded = Base64.getDecoder().decode(base64Json);
            String json   = new String(decoded, StandardCharsets.UTF_8);

            // 2) Parse out the actual skin URL
            JsonObject root   = JsonParser.parseString(json).getAsJsonObject();
            String    urlText = root
                    .getAsJsonObject("textures")
                    .getAsJsonObject("SKIN")
                    .get("url")
                    .getAsString();

            // 3) Build a fresh profile + textures object
            PlayerProfile profile  = Bukkit.createPlayerProfile(UUID.randomUUID());
            PlayerTextures textures = profile.getTextures();
            textures.setSkin(new URL(urlText), PlayerTextures.SkinModel.CLASSIC);
            profile.setTextures(textures);

            // 4) Apply it to the SkullMeta
            skullMeta.setOwnerProfile(profile);

        } catch (Exception ex) {
            ex.printStackTrace();
            // if anything fails, we just leave the meta untouched
        }

        return skullMeta;
    }

    @EventHandler
    public void onChampionDeath(EntityDeathEvent event){
        if(!(event.getEntity() instanceof Zombie zombie)) return;
        if(!zombie.hasMetadata("championTier")) return;
        int tier = zombie.getMetadata("championTier").get(0).asInt();
        Player killer = zombie.getKiller();
        if(killer!=null){
            int xp = tier==0?1000:tier==1?2000:3000;
            xpManager.addXP(killer, "Combat", xp);
        }
    }

    @EventHandler
    public void onChampionHit(EntityDamageByEntityEvent event){
        if(!(event.getDamager() instanceof LivingEntity entity)) return;
        if(!entity.hasMetadata("championTier")) return;
        int tier = entity.getMetadata("championTier").get(0).asInt();
        if(tier==1 && event.getEntity() instanceof LivingEntity target){
            double dmg = event.getDamage();
            new BukkitRunnable(){
                @Override
                public void run(){ target.damage(dmg, entity); }
            }.runTaskLater(plugin,20L);
            new BukkitRunnable(){
                @Override
                public void run(){ target.damage(dmg, entity); }
            }.runTaskLater(plugin,40L);
        }
    }
}
