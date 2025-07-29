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
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.profile.PlayerProfile;
import org.bukkit.profile.PlayerTextures;
import org.bukkit.scheduler.BukkitRunnable;
import org.checkerframework.checker.units.qual.C;

import java.net.URL;
import java.util.*;

public class MiningTalentFeatures implements Listener {
    private final MinecraftNew plugin;
    private final Map<UUID, Long> bigBubbleCooldown = new HashMap<>();

    // keys for storing data on skull and pickaxe
    private static final String BUBBLE_KEY = "oxygenBubble";
    private static final String HOTM_KEY   = "hotm_applied";

    public MiningTalentFeatures(MinecraftNew plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block block  = event.getBlock();

        handleBubbles(player, block);
        handleMagnet(player, block);
        handleAncientDebris(player, block);
        handleHeartOfMountain(player, block);
    }
    public static List<Material> onlyOres = Arrays.asList(
            Material.COAL_ORE,
            Material.DEEPSLATE_COAL_ORE,
            Material.IRON_ORE,
            Material.DEEPSLATE_IRON_ORE,
            Material.COPPER_ORE,
            Material.DEEPSLATE_COPPER_ORE,
            Material.GOLD_ORE,
            Material.DEEPSLATE_GOLD_ORE,
            Material.REDSTONE_ORE,
            Material.DEEPSLATE_REDSTONE_ORE,
            Material.EMERALD_ORE,
            Material.DEEPSLATE_EMERALD_ORE,
            Material.LAPIS_ORE,
            Material.DEEPSLATE_LAPIS_ORE,
            Material.DIAMOND_ORE,
            Material.DEEPSLATE_DIAMOND_ORE,
            Material.NETHER_QUARTZ_ORE,
            Material.NETHER_GOLD_ORE,
            Material.ANCIENT_DEBRIS
    );
    private void handleBubbles(Player player, Block block) {
        if (!onlyOres.contains(block.getType())) return;
        if (SkillTreeManager.getInstance() == null) return;

        // total bubble talent points (I + II)
        int bubbles =
                SkillTreeManager.getInstance().getTalentLevel(player.getUniqueId(), Skill.MINING, Talent.BUBBLES_I)
                        + SkillTreeManager.getInstance().getTalentLevel(player.getUniqueId(), Skill.MINING, Talent.BUBBLES_II);
        if (bubbles <= 0) return;

        // spawn chance
        double chance = bubbles * 0.0025;
        if (Math.random() > chance) return;

        int oxygen = 50;

        // big bubbles doubling effect, 10s cooldown
        int bigLvl =
                SkillTreeManager.getInstance().getTalentLevel(player.getUniqueId(), Skill.MINING, Talent.BIG_BUBBLES_I)
                        + SkillTreeManager.getInstance().getTalentLevel(player.getUniqueId(), Skill.MINING, Talent.BIG_BUBBLES_II)
                        + SkillTreeManager.getInstance().getTalentLevel(player.getUniqueId(), Skill.MINING, Talent.BIG_BUBBLES_III);
        if (bigLvl > 0) {
                oxygen += (bigLvl * 10);
        }

        // center of the broken block
        Location center = block.getLocation().add(0.5, 0.5, 0.5);

        // spawn invisible, marker armor stand
        ArmorStand stand = (ArmorStand) block.getWorld().spawnEntity(center, EntityType.ARMOR_STAND);
        stand.setInvisible(true);
        stand.setCustomNameVisible(false);

        // create a player-head with your custom bubble texture
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta  sm   = (SkullMeta) head.getItemMeta();
        if (sm != null) {
            try {
                // 1) build a fresh profile
                PlayerProfile prof = Bukkit.createPlayerProfile(UUID.randomUUID());
                PlayerTextures tex = prof.getTextures();
                // 2) set the skin URL (same as your bubble base64 payload)
                tex.setSkin(
                        new URL("http://textures.minecraft.net/texture/930177b0e294d4e4c1488ad5c43da7ef72e0f031c2c5885e6bda35656f38edb7"),
                        PlayerTextures.SkinModel.CLASSIC
                );
                prof.setTextures(tex);

                // 3) apply to the skull meta
                sm.setOwnerProfile(prof);

                // 4) store oxygen amount
                PersistentDataContainer pdc = sm.getPersistentDataContainer();
                pdc.set(new NamespacedKey(plugin, BUBBLE_KEY), PersistentDataType.INTEGER, oxygen);

                head.setItemMeta(sm);
                player.sendMessage(ChatColor.AQUA + "You spawned an Oxygen Bubble!");

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        stand.getEquipment().setHelmet(head);

        // auto-remove after 30s to avoid world clutter
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!stand.isDead()) {
                    stand.remove();
                }
            }
        }.runTaskLater(plugin, 20 * 30);
    }

    @EventHandler
    public void onHitBubble(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof ArmorStand)) return;
        if (!(event.getDamager() instanceof Player))   return;

        ArmorStand stand = (ArmorStand) event.getEntity();
        ItemStack helmet = stand.getEquipment().getHelmet();
        if (helmet == null) return;

        ItemMeta meta = helmet.getItemMeta();
        if (meta == null) return;

        Integer oxy = meta
                .getPersistentDataContainer()
                .get(new NamespacedKey(plugin, BUBBLE_KEY), PersistentDataType.INTEGER);
        if (oxy == null) return;

        event.setCancelled(true);
        stand.remove();

        Player p = (Player) event.getDamager();
        // add oxygen & re-assess hypoxia
        goat.minecraft.minecraftnew.subsystems.mining.PlayerOxygenManager.getInstance()
                .addOxygen(p, oxy);
        p.playSound(p.getLocation(), Sound.ITEM_TRIDENT_RIPTIDE_1, 1.0f,1.0f);
        p.sendMessage(ChatColor.DARK_AQUA + "You gained +" + oxy + ChatColor.AQUA + " Oxygen!");

    }

    private void handleMagnet(Player player, Block block) {
        if (SkillTreeManager.getInstance() == null) return;
        int lvl = SkillTreeManager.getInstance()
                .getTalentLevel(player.getUniqueId(), Skill.MINING, Talent.MAGNET);
        if (lvl <= 0) return;

        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            double radius = Math.min(3, lvl);
            for (Entity ent : block.getWorld().getNearbyEntities(
                    block.getLocation(), radius, radius, radius)) {
                if (ent instanceof Item itm) {
                    itm.teleport(player.getLocation());
                }
            }
        }, 1L);
    }

    private void handleAncientDebris(Player player, Block block) {
        if (SkillTreeManager.getInstance() == null) return;
        int lvl = SkillTreeManager.getInstance()
                .getTalentLevel(player.getUniqueId(), Skill.MINING, Talent.ANCIENT_DEBRIS);
        if (lvl <= 0) return;

        if (block.getType() == Material.ANCIENT_DEBRIS) {
            double chance = lvl * 0.05;
            if (Math.random() < chance) {
                block.getWorld().dropItemNaturally(
                        block.getLocation(), ItemRegistry.getMasterworkIngot());
            }
        }
    }

    private void handleHeartOfMountain(Player player, Block block) {
        if (SkillTreeManager.getInstance() == null) return;
        int lvl = SkillTreeManager.getInstance()
                .getTalentLevel(player.getUniqueId(), Skill.MINING, Talent.HEART_OF_THE_MOUNTAIN);
        if (lvl <= 0) return;

        ItemStack inHand = player.getInventory().getItemInMainHand();
        if (inHand == null || !inHand.getType().toString().contains("PICKAXE")) return;

        ItemMeta meta = inHand.getItemMeta();
        if (meta == null) return;

        NamespacedKey key = new NamespacedKey(plugin, HOTM_KEY);
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        if (pdc.has(key, PersistentDataType.INTEGER)) return;

        // mark it so we don't reapply on the same pickaxe until next block
        pdc.set(key, PersistentDataType.INTEGER, 1);
        inHand.setItemMeta(meta);

        CustomDurabilityManager.getInstance().addMaxDurabilityBonus(inHand, 100);

        // remove marker & bonus after a short delay so next block will re-trigger
        new BukkitRunnable() {
            @Override
            public void run() {
                ItemMeta m2 = inHand.getItemMeta();
                if (m2 != null) {
                    m2.getPersistentDataContainer().remove(key);
                    inHand.setItemMeta(m2);
                }
                CustomDurabilityManager.getInstance().addMaxDurabilityBonus(inHand, -100);
            }
        }.runTaskLater(plugin, 100L);
    }
}
