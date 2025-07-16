package goat.minecraft.minecraftnew.subsystems.combat;

import goat.minecraft.minecraftnew.MinecraftNew;
import goat.minecraft.minecraftnew.other.skilltree.Skill;
import goat.minecraft.minecraftnew.other.skilltree.SkillTreeManager;
import goat.minecraft.minecraftnew.other.skilltree.Talent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.bukkit.profile.PlayerProfile;
import org.bukkit.profile.PlayerTextures;
import java.util.Base64;

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Handles activation of combat talents like Ultimatum, Armageddon and Vampiric Strike.
 */
public class CombatTalentListener implements Listener {

    private static final String SOUL_ORB_KEY = "SOUL_ORB";
    private static final String SOUL_ORB_TEXTURE = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvM2NkYWE1YmYwYmM1NDIwNTBjNzM0YzU4ZjQyODMyMTVjOWE0ZjBmMThjM2RkYWQ4NzE5MTNkYmY2NjdjZTQzMyJ9fX0=";

    private final Random random = new Random();
    private final Map<UUID, Long> ultimatumCooldown = new HashMap<>();

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player player)) return;
        if (!(event.getEntity() instanceof LivingEntity target) || !(target instanceof Monster)) return;
        SkillTreeManager manager = SkillTreeManager.getInstance();
        if (manager == null) return;

        int ultLevel = manager.getTalentLevel(player.getUniqueId(), Skill.COMBAT, Talent.ULTIMATUM);
        if (ultLevel > 0) {
            long last = ultimatumCooldown.getOrDefault(player.getUniqueId(), 0L);
            if (System.currentTimeMillis() - last >= 20_000L && random.nextDouble() < ultLevel * 0.01) {
                Location loc = player.getLocation();
                for (Entity e : player.getWorld().getNearbyEntities(loc, 25, 25, 25)) {
                    if (e instanceof Monster mob) {
                        mob.getWorld().strikeLightning(mob.getLocation());
                        mob.damage(100, player);
                        FireDamageHandler.addFireStacks(mob, 20);
                    }
                }
                ultimatumCooldown.put(player.getUniqueId(), System.currentTimeMillis());
            }
        }

        int armLevel = manager.getTalentLevel(player.getUniqueId(), Skill.COMBAT, Talent.ARMAGEDDON);
        if (armLevel > 0 && random.nextDouble() < armLevel * 0.01) {
            List<Monster> mobs = new ArrayList<>();
            for (Entity e : player.getWorld().getNearbyEntities(player.getLocation(), 15, 15, 15)) {
                if (e instanceof Monster m) mobs.add(m);
            }
            if (mobs.size() > 8) {
                for (Monster mob : mobs) {
                    if (mob.equals(target)) continue;
                    Vector vec = mob.getLocation().toVector().subtract(player.getLocation().toVector()).normalize().multiply(1.5);
                    vec.setY(0.4);
                    mob.setVelocity(vec);
                }
            }
        }
    }

    @EventHandler
    public void onKill(EntityDeathEvent event) {
        if (!(event.getEntity() instanceof Monster mob)) return;
        Player killer = mob.getKiller();
        if (killer == null) return;
        SkillTreeManager manager = SkillTreeManager.getInstance();
        if (manager == null) return;
        int level = manager.getTalentLevel(killer.getUniqueId(), Skill.COMBAT, Talent.VAMPIRIC_STRIKE);
        if (level > 0 && random.nextDouble() < level * 0.01) {
            spawnSoulOrb(mob.getLocation(), killer.getUniqueId());
        }
    }

    @EventHandler
    public void onOrbDeath(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof ArmorStand stand)) return;
        if (!stand.hasMetadata(SOUL_ORB_KEY)) return;
        stand.remove();
        Entity killer = event.getDamager();
        if(killer instanceof Player player){
            player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 40, 254));
            Bukkit.getLogger().info("Activated Devour Orb Event");
        }

    }

    private void spawnSoulOrb(Location loc, UUID owner) {
        World world = loc.getWorld();
        if (world == null) return;
        ArmorStand stand = world.spawn(loc, ArmorStand.class);
        stand.setInvisible(true);
        stand.setSmall(true);
        stand.setMarker(false);
        stand.setSilent(true);
        stand.setCustomName("Soul Orb");
        stand.setCustomNameVisible(false);
        stand.getEquipment().setHelmet(createSkull());
        stand.setHealth(1);
        stand.setMetadata(SOUL_ORB_KEY, new FixedMetadataValue(MinecraftNew.getInstance(), owner.toString()));
    }

    private ItemStack createSkull() {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) head.getItemMeta();
        setCustomSkullTexture(meta, SOUL_ORB_TEXTURE);
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
