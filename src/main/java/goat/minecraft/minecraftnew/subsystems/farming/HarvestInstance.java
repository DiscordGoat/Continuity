package goat.minecraft.minecraftnew.subsystems.farming;

import goat.minecraft.minecraftnew.MinecraftNew;
import goat.minecraft.minecraftnew.other.skilltree.Skill;
import goat.minecraft.minecraftnew.other.skilltree.SkillTreeManager;
import goat.minecraft.minecraftnew.other.skilltree.Talent;
import goat.minecraft.minecraftnew.utils.devtools.ItemRegistry;
import goat.minecraft.minecraftnew.utils.devtools.XPManager;
import org.bukkit.*;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;

/**
 * Collects harvest-side effects for up to 2 seconds, then applies them at once.
 */
public class HarvestInstance {
    int farmingXP = 0;
    int expOrbs = 0; // treat as total orb XP to award
    int fertilizerRolls = 0;
    final Map<Material, Integer> cropTallies = new HashMap<>();
    int festivalBeePoints = 0; // interpreted as number of rolls
    boolean playMusic = false;
    ItemStack harvestReward = null; // only 1 per batch

    final long startTime = System.currentTimeMillis();

    void addCrop(Material crop, int xp, boolean rollFertilizer, boolean beeTriggered, boolean musicTriggered, ItemStack reward) {
        farmingXP += Math.max(0, xp);
        // count 1 xp per crop event for orb payout; we'll spawn a single orb combining all
        expOrbs += 1;
        cropTallies.merge(crop, 1, Integer::sum);
        if (rollFertilizer) fertilizerRolls++;
        if (beeTriggered) festivalBeePoints++;
        if (musicTriggered) playMusic = true;
        if (reward != null && harvestReward == null) harvestReward = reward; // first reward only
    }

    boolean isExpired() {
        return System.currentTimeMillis() - startTime >= 2000;
    }

    void grant(Player player) {
        MinecraftNew plugin = MinecraftNew.getInstance();
        XPManager xpManager = new XPManager(plugin);

        // Farming XP
        if (farmingXP > 0) {
            xpManager.addXP(player, "Farming", farmingXP);
        }

        // Orbs: spawn a single orb with combined xp to reduce entities
        if (expOrbs > 0) {
            ExperienceOrb orb = player.getWorld().spawn(player.getLocation(), ExperienceOrb.class);
            orb.setExperience(expOrbs);
        }

        // Fertilizer chance (rolls once per fertilizerRolls)
        if (fertilizerRolls > 0) {
            int un = 0;
            if (SkillTreeManager.getInstance() != null) {
                un = SkillTreeManager.getInstance().getTalentLevel(player.getUniqueId(), Skill.FARMING, Talent.UNRIVALED);
            }
            double chance = un * 0.001; // same as original per-crop roll
            for (int i = 0; i < fertilizerRolls; i++) {
                if (Math.random() < chance) {
                    ItemStack fert = ItemRegistry.getFertilizer().clone();
                    Map<Integer, ItemStack> overflow = player.getInventory().addItem(fert);
                    if (!overflow.isEmpty()) {
                        for (ItemStack left : overflow.values()) {
                            player.getWorld().dropItemNaturally(player.getLocation(), left);
                        }
                    }
                    player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 0.6f, 1.4f);
                    player.sendMessage(ChatColor.GREEN + "You found " + ChatColor.YELLOW + "Fertilizer" + ChatColor.GREEN + " while harvesting.");
                }
            }
        }

        // Crop tally updates and threshold detection
        boolean thresholdCrossed = false;
        Material firstCrossed = null;
        for (Map.Entry<Material, Integer> entry : cropTallies.entrySet()) {
            if (entry.getValue() <= 0) continue;
            boolean crossed = CropCountManager.getInstance(plugin).bulkIncrement(player, entry.getKey(), entry.getValue());
            if (crossed && firstCrossed == null) firstCrossed = entry.getKey();
            thresholdCrossed = thresholdCrossed || crossed;
        }

        // Bees: roll per point using existing talents
        if (festivalBeePoints > 0) {
            SkillTreeManager mgr = SkillTreeManager.getInstance();
            double beeChance = 0.0;
            if (mgr != null) {
                beeChance =
                        mgr.getTalentLevel(player.getUniqueId(), Skill.FARMING, Talent.FESTIVAL_BEES_I) * 0.1 +
                        mgr.getTalentLevel(player.getUniqueId(), Skill.FARMING, Talent.FESTIVAL_BEES_II) * 0.1 +
                        mgr.getTalentLevel(player.getUniqueId(), Skill.FARMING, Talent.FESTIVAL_BEES_III) * 0.1 +
                        mgr.getTalentLevel(player.getUniqueId(), Skill.FARMING, Talent.FESTIVAL_BEES_IV) * 0.1;
                beeChance /= 100.0;
            }
            int successes = 0;
            for (int i = 0; i < festivalBeePoints; i++) {
                if (Math.random() < beeChance) successes++;
            }
            if (successes > 0) {
                int dur = 30;
                if (mgr != null) {
                    dur += mgr.getTalentLevel(player.getUniqueId(), Skill.FARMING, Talent.FESTIVAL_BEE_DURATION_I) * 10;
                    dur += mgr.getTalentLevel(player.getUniqueId(), Skill.FARMING, Talent.FESTIVAL_BEE_DURATION_II) * 10;
                }
                for (int i = 0; i < successes; i++) {
                    FestivalBeeManager.getInstance(plugin).spawnFestivalBee(player.getLocation(), dur);
                }
                int swarm = mgr != null ? mgr.getTalentLevel(player.getUniqueId(), Skill.FARMING, Talent.SWARM) : 0;
                if (swarm > 0 && Math.random() < swarm * 0.10) {
                    FestivalBeeManager.getInstance(plugin).spawnFestivalBee(player.getLocation(), dur);
                }
                player.sendMessage(ChatColor.GOLD + "A Festival Bee has spawned!");
                player.playSound(player.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_LAUNCH, 1.0f, 0.5f);
            }
        }

        // Music
        if (playMusic) {
            player.playSound(player.getLocation(), Sound.MUSIC_DISC_FAR, SoundCategory.AMBIENT, 1.0f, 1.0f);
        }

        // Update sidebar progress (Dinner Bell) using dominant crop of this batch
        Material dominant = null;
        int maxCount = 0;
        for (Map.Entry<Material, Integer> e : cropTallies.entrySet()) {
            if (e.getValue() > maxCount) { maxCount = e.getValue(); dominant = e.getKey(); }
        }
        if (dominant != null) {
            int total = CropCountManager.getInstance(plugin).getCount(player, dominant);
            int req = CropCountManager.getInstance(plugin).getRequirement(player);
            int current = total % Math.max(1, req);
            HarvestProgressTracker.set(player.getUniqueId(), dominant, current, req);
        }

        // Harvest rewards: reinstate legacy behavior and messages
        if (harvestReward != null) {
            player.getWorld().dropItemNaturally(player.getLocation(), harvestReward);
        } else if (thresholdCrossed) {
            // Grant the +100 Farming XP and reward identical to legacy flow
            xpManager.addXP(player, "Farming", 100);
            player.playSound(player.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_LAUNCH, 1.0f, 2.0f);
            if (dominant != null) {
                HarvestRewardLegacy.grant(player, dominant, player.getLocation());
            }
        }
    }
}
