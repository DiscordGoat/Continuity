package goat.minecraft.minecraftnew.subsystems.combat;

import goat.minecraft.minecraftnew.MinecraftNew;
import goat.minecraft.minecraftnew.other.skilltree.Skill;
import goat.minecraft.minecraftnew.other.skilltree.SkillTreeManager;
import goat.minecraft.minecraftnew.other.skilltree.Talent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.ChatColor;
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
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.attribute.Attribute;
import goat.minecraft.minecraftnew.subsystems.combat.BloodlustManager;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.bukkit.profile.PlayerProfile;
import org.bukkit.profile.PlayerTextures;
import java.util.Base64;

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Handles activation of combat talents like Ultimatum and Vampiric Strike.
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

        // Retribution talent: chance to gain stacks on hit
        int retLvl = manager.getTalentLevel(player.getUniqueId(), Skill.COMBAT, Talent.RETRIBUTION);
        if (retLvl > 0 && random.nextDouble() < retLvl * 0.01) {
            BloodlustManager.getInstance(MinecraftNew.getInstance()).addStacks(player, 10);
        }

        // Vengeance talent: chance to gain bloodlust duration on hit
        int venLvl = manager.getTalentLevel(player.getUniqueId(), Skill.COMBAT, Talent.VENGEANCE);
        if (venLvl > 0 && random.nextDouble() < venLvl * 0.01) {
            BloodlustManager.getInstance(MinecraftNew.getInstance()).addDuration(player, 20);
        }

        // Lifesteal while bloodlust stacks are high
        int stacks = BloodlustManager.getInstance(MinecraftNew.getInstance()).getStacks(player);
        double steal = 0.0;
        if (stacks >= 70 && stacks < 90) {
            steal = 0.01;
        } else if (stacks < 100) {
            steal = 0.015;
        } else if (stacks >= 100) {
            steal = 0.02;
        }
        if (steal > 0) {
            double heal = event.getDamage() * steal;
            double max = player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
            player.setHealth(Math.min(max, player.getHealth() + heal));
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

        // Activate Bloodlust on kill if player has the talent
        if (manager.hasTalent(killer, Talent.BLOODLUST)) {
            int duration = 5
                    + manager.getTalentLevel(killer.getUniqueId(), Skill.COMBAT, Talent.BLOODLUST_DURATION_I) * 4
                    + manager.getTalentLevel(killer.getUniqueId(), Skill.COMBAT, Talent.BLOODLUST_DURATION_II) * 4
                    + manager.getTalentLevel(killer.getUniqueId(), Skill.COMBAT, Talent.BLOODLUST_DURATION_III) * 4
                    + manager.getTalentLevel(killer.getUniqueId(), Skill.COMBAT, Talent.BLOODLUST_DURATION_IV) * 4;
            BloodlustManager mgr = BloodlustManager.getInstance(MinecraftNew.getInstance());
            mgr.addStacks(killer, 2);
            mgr.addDuration(killer, duration);
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

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        SkillTreeManager manager = SkillTreeManager.getInstance();
        if (manager == null) return;
        BloodlustManager bl = BloodlustManager.getInstance(MinecraftNew.getInstance());
        int stacks = bl.getStacks(player);
        if (stacks >= 100 && manager.hasTalent(player, Talent.REVENANT)) {
            // Trigger Fury effect placeholder
            player.sendMessage(ChatColor.DARK_RED + "Fury unleashed!");
        }
        bl.clear(player);
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
