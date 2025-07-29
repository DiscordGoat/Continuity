package goat.minecraft.minecraftnew.subsystems.mining;

import goat.minecraft.minecraftnew.MinecraftNew;
import goat.minecraft.minecraftnew.other.durability.CustomDurabilityManager;
import goat.minecraft.minecraftnew.other.skilltree.Skill;
import goat.minecraft.minecraftnew.other.skilltree.SkillTreeManager;
import goat.minecraft.minecraftnew.other.skilltree.Talent;
import goat.minecraft.minecraftnew.utils.devtools.ItemRegistry;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class MiningTalentFeatures implements Listener {
    private final MinecraftNew plugin;
    private final Map<UUID, Long> bigBubbleCooldown = new HashMap<>();
    private static final String BUBBLE_KEY = "oxygenBubble";
    private static final String HOTM_KEY = "hotm_applied";

    public MiningTalentFeatures(MinecraftNew plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();
        handleBubbles(player, block);
        handleMagnet(player, block);
        handleAncientDebris(player, block);
        handleWakeStatue(player, block);
        handleHeartOfMountain(player);
    }

    private void handleBubbles(Player player, Block block) {
        if (player.getEyeLocation().getBlock().getType() != Material.WATER) return;
        if (SkillTreeManager.getInstance() == null) return;
        int bubbles = SkillTreeManager.getInstance().getTalentLevel(player.getUniqueId(), Skill.MINING, Talent.BUBBLES_I)
                + SkillTreeManager.getInstance().getTalentLevel(player.getUniqueId(), Skill.MINING, Talent.BUBBLES_II);
        if (bubbles <= 0) return;
        double chance = bubbles * 0.01;
        if (Math.random() > chance) return;
        int oxygen = 50;
        int big = SkillTreeManager.getInstance().getTalentLevel(player.getUniqueId(), Skill.MINING, Talent.BIG_BUBBLES_I)
                + SkillTreeManager.getInstance().getTalentLevel(player.getUniqueId(), Skill.MINING, Talent.BIG_BUBBLES_II)
                + SkillTreeManager.getInstance().getTalentLevel(player.getUniqueId(), Skill.MINING, Talent.BIG_BUBBLES_III);
        if (big > 0) {
            long last = bigBubbleCooldown.getOrDefault(player.getUniqueId(), 0L);
            if (System.currentTimeMillis() - last > 10000) {
                oxygen *= 2;
                bigBubbleCooldown.put(player.getUniqueId(), System.currentTimeMillis());
            }
        }
        Location loc = block.getLocation().add(0.5, 0.1, 0.5);
        int finalOxygen = oxygen;
        ArmorStand stand = loc.getWorld().spawn(loc, ArmorStand.class, s -> {
            s.setInvisible(true);
            s.setMarker(true);
            s.setCustomNameVisible(false);
            ItemStack head = new ItemStack(Material.PLAYER_HEAD);
            ItemMeta meta = head.getItemMeta();
            if (meta != null) {
                meta.getPersistentDataContainer().set(new NamespacedKey(plugin, BUBBLE_KEY), PersistentDataType.INTEGER, finalOxygen);
                head.setItemMeta(meta);
            }
            s.getEquipment().setHelmet(head);
        });
    }

    @EventHandler
    public void onHitBubble(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof ArmorStand)) return;
        if (!(event.getDamager() instanceof Player)) return;
        ArmorStand stand = (ArmorStand) event.getEntity();
        ItemStack helmet = stand.getEquipment().getHelmet();
        if (helmet == null) return;
        Integer oxy = helmet.getItemMeta().getPersistentDataContainer().get(new NamespacedKey(plugin, BUBBLE_KEY), PersistentDataType.INTEGER);
        if (oxy == null) return;
        event.setCancelled(true);
        stand.remove();
        Player p = (Player) event.getDamager();
        PlayerOxygenManager.getInstance().addOxygen(p, oxy);
    }


    private void handleMagnet(Player player, Block block) {
        if (SkillTreeManager.getInstance() == null) return;
        int level = SkillTreeManager.getInstance().getTalentLevel(player.getUniqueId(), Skill.MINING, Talent.MAGNET);
        if (level <= 0) return;
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            double radius = Math.min(3, level);
            for (Entity ent : block.getWorld().getNearbyEntities(block.getLocation(), radius, radius, radius)) {
                if (ent instanceof Item) {
                    ent.teleport(player.getLocation());
                }
            }
        }, 1L);
    }

    private void handleAncientDebris(Player player, Block block) {
        if (SkillTreeManager.getInstance() == null) return;
        int level = SkillTreeManager.getInstance().getTalentLevel(player.getUniqueId(), Skill.MINING, Talent.ANCIENT_DEBRIS);
        if (level <= 0) return;
        if (block.getType() == Material.ANCIENT_DEBRIS) {
            double chance = level * 0.05;
            if (Math.random() < chance) {
                block.getWorld().dropItemNaturally(block.getLocation(), ItemRegistry.getMasterworkIngot());
            }
        }
    }

    private void handleWakeStatue(Player player, Block block) {
        if (SkillTreeManager.getInstance() == null) return;
        int level = SkillTreeManager.getInstance().getTalentLevel(player.getUniqueId(), Skill.MINING, Talent.WAKE_UP_THE_STATUES);
        if (level <= 0) return;
        for (Entity e : block.getWorld().getNearbyEntities(block.getLocation(), 5, 5, 5)) {
            if (e instanceof ArmorStand && "Stone Statue".equals(e.getCustomName())) {
                e.remove();
                IronGolem golem = (IronGolem) block.getWorld().spawnEntity(e.getLocation(), EntityType.IRON_GOLEM);
                golem.setCustomName("Awakened Statue");
                golem.setPlayerCreated(false);
                break;
            }
        }
    }

    private void handleHeartOfMountain(Player player) {
        if (SkillTreeManager.getInstance() == null) return;
        int level = SkillTreeManager.getInstance().getTalentLevel(player.getUniqueId(), Skill.MINING, Talent.HEART_OF_THE_MOUNTAIN);
        if (level <= 0) return;
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null || !item.getType().toString().contains("PICKAXE")) return;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;
        NamespacedKey key = new NamespacedKey(plugin, HOTM_KEY);
        PersistentDataContainer data = meta.getPersistentDataContainer();
        if (data.has(key, PersistentDataType.INTEGER)) return;
        data.set(key, PersistentDataType.INTEGER, 1);
        item.setItemMeta(meta);
        CustomDurabilityManager.getInstance().addMaxDurabilityBonus(item, 100);
        new BukkitRunnable() {
            @Override
            public void run() {
                ItemMeta m = item.getItemMeta();
                if (m != null) {
                    m.getPersistentDataContainer().remove(key);
                    item.setItemMeta(m);
                }
                CustomDurabilityManager.getInstance().addMaxDurabilityBonus(item, -100);
            }
        }.runTaskLater(plugin, 100L);
    }
}
