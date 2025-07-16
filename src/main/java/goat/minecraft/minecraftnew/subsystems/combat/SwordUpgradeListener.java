package goat.minecraft.minecraftnew.subsystems.combat;

import goat.minecraft.minecraftnew.MinecraftNew;
import goat.minecraft.minecraftnew.utils.devtools.XPManager;
import goat.minecraft.minecraftnew.subsystems.combat.DeteriorationDamageHandler;
import goat.minecraft.minecraftnew.utils.devtools.PlayerMeritManager;
import goat.minecraft.minecraftnew.other.skilltree.Skill;
import goat.minecraft.minecraftnew.other.skilltree.SkillTreeManager;
import goat.minecraft.minecraftnew.other.skilltree.Talent;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.*;

import static goat.minecraft.minecraftnew.subsystems.villagers.VillagerWorkCycleManager.getNearestPlayer;

/**
 * Applies effects from Soul Power sword upgrades during combat.
 */
public class SwordUpgradeListener implements Listener {
    private final Random random = new Random();
    private final XPManager xpManager;

    public SwordUpgradeListener(MinecraftNew plugin) {
        this.xpManager = new XPManager(plugin);
    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player player)) return;
        ItemStack weapon = player.getInventory().getItemInMainHand();
        if (weapon == null) return;
        if (!(event.getEntity() instanceof LivingEntity target)) return;

        int decayLvl = SoulUpgradeSystem.getUpgradeLevel(weapon, SoulUpgradeSystem.SwordUpgrade.ASPECT_OF_DECAY);
        if (decayLvl > 0) {
            int stacks = decayLvl * 2;
            PlayerMeritManager merit = PlayerMeritManager.getInstance(MinecraftNew.getInstance());
            if (merit.hasPerk(player.getUniqueId(), "Decay Mastery")) {
                stacks *= 2;
            }
            DeteriorationDamageHandler.getInstance().addDeterioration(target, stacks);
        }
        if (target instanceof Creeper) {
            int level = 0;
            if (SkillTreeManager.getInstance() != null) {
                level = SkillTreeManager.getInstance()
                        .getTalentLevel(player.getUniqueId(), Skill.COMBAT, Talent.DONT_MINE_AT_NIGHT);
            }
            if (level > 0) {
                event.setDamage(event.getDamage() * (1 + level * 0.10));
            }
        }

        // Regeneration upgrades removed

        // Feed upgrade removed

        // Fury upgrade removed
    }
    @EventHandler
    public void onSpawn(EntitySpawnEvent e) {
        // 1) Only proceed if this is a living Mob:
        if (!(e.getEntity() instanceof Mob mob)) {
            return;
        }

        // 2) Find nearest player—and bail out if there isn't one:
        Player player = getNearestPlayer(mob.getLocation());
        if (player == null) {
            return;
        }

        // 3) Make sure we only handle monsters (and that it's not our own metadata):
        if (!(mob instanceof Monster) || mob.hasMetadata("spawnedBySkill")) {
            return;
        }

        // 4) Grab their weapon, bail if null:
        ItemStack weapon = player.getInventory().getItemInMainHand();
        if (weapon == null || SoulUpgradeSystem.getUpgradeLevel(weapon, SoulUpgradeSystem.SwordUpgrade.CHALLENGE) == 0) {
            return;
        }

        // Now it’s safe to spawn extras:
        int challengeLevel = SoulUpgradeSystem.getUpgradeLevel(weapon, SoulUpgradeSystem.SwordUpgrade.CHALLENGE);
        if (random.nextDouble() < challengeLevel * 0.05) {
            Entity extra = mob.getWorld().spawnEntity(mob.getLocation(), mob.getType());
            extra.setMetadata("spawnedBySkill", new FixedMetadataValue(MinecraftNew.getInstance(), true));
        }

        int bloodMoonLevel = SoulUpgradeSystem.getUpgradeLevel(weapon, SoulUpgradeSystem.SwordUpgrade.BLOOD_MOON);
        if (bloodMoonLevel > 0 && random.nextDouble() < bloodMoonLevel * 0.05) {
            Entity extra = mob.getWorld().spawnEntity(mob.getLocation(), mob.getType());
            extra.setMetadata("spawnedBySkill", new FixedMetadataValue(MinecraftNew.getInstance(), true));
        }
    }
    @EventHandler
    public void onKill(EntityDeathEvent event) {
        if (!(event.getEntity().getKiller() instanceof Player player)) return;
        ItemStack weapon = player.getInventory().getItemInMainHand();
        if (weapon == null) return;
        LivingEntity mob = event.getEntity();

        // Betrayal upgrade removed

        if (SoulUpgradeSystem.getUpgradeLevel(weapon, SoulUpgradeSystem.SwordUpgrade.STARLESS_NIGHT) > 0 && mob instanceof Monster) {
            World world = mob.getWorld();
            long time = world.getTime();
            if (time > 13000 && time < 23000) {
                world.setTime(Math.max(0, time - 60));
            }
        }

        // inside your spawn-handling method, instead of the two separate ifs:
            // 1) get upgrade levels

    }
}
