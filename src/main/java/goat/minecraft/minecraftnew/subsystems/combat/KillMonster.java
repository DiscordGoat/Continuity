package goat.minecraft.minecraftnew.subsystems.combat;

import goat.minecraft.minecraftnew.MinecraftNew;

import goat.minecraft.minecraftnew.other.additionalfunctionality.Pathfinder;
import goat.minecraft.minecraftnew.utils.devtools.PlayerMeritManager;
import goat.minecraft.minecraftnew.utils.devtools.XPManager;
import goat.minecraft.minecraftnew.subsystems.combat.champion.ChampionManager;
import goat.minecraft.minecraftnew.subsystems.combat.utils.EntityLevelExtractor;
import goat.minecraft.minecraftnew.utils.devtools.ItemRegistry;
import goat.minecraft.minecraftnew.subsystems.forestry.Forestry;
import me.gamercoder215.mobchip.EntityBrain;
import me.gamercoder215.mobchip.ai.EntityAI;
import me.gamercoder215.mobchip.bukkit.BukkitBrain;
import org.bukkit.Location;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Material;

import java.util.Random;

public class KillMonster implements Listener {
    private final MinecraftNew plugin = MinecraftNew.getInstance();
    public XPManager xpManager = new XPManager(plugin);
    private final EntityLevelExtractor levelExtractor = new EntityLevelExtractor();

    // Blood Moon Mechanics
    private boolean isBloodMoon = false;
    private long lastCheckedTime = -1;

    public void reinforceComrade(Player player) {
        // Get the player's location
        Location playerLocation = player.getLocation();

        // Iterate through all entities in the world within 100 blocks

        for (Entity entity : player.getWorld().getNearbyEntities(playerLocation, 1000, 1000, 1000)) {
            // Check if the entity is a Mob (Monster)
            if (entity instanceof Mob mob) {
                // Check if the entity has a BukkitBrain (MobChip-compatible entity)
                EntityBrain brain = BukkitBrain.getBrain(mob);
                if (brain != null) {
                    // Additional check to ensure it's a Monster
                    if (mob instanceof Monster) {
                        Creature creature = (Creature) mob;
                        EntityAI goal = brain.getGoalAI();
                        moveTo(creature, player);
                    }
                }
            }
        }
    }
    public static void moveTo(Mob mob, Player player) {
        // Gets the entity's global brain
        EntityBrain brain = BukkitBrain.getBrain(mob);

        brain.getController().moveTo(player);
    }
    @EventHandler
    public void onKill(EntityDeathEvent e) {
        Entity entity = e.getEntity();
        Entity killer = e.getEntity().getKiller();
    
        if (killer instanceof Player && entity instanceof Monster) {
            Player playerKiller = (Player) killer;
    
            if (entity instanceof Zombie) {
                Pathfinder pathfinder = new Pathfinder();
                pathfinder.reinforceZombies(entity, 100);
            }
    
            // Get the monster's level (ensure it's at least 1)
            int monsterLevel = levelExtractor.extractLevelFromName(entity);
            monsterLevel = Math.max(monsterLevel, 1);
    
            // Calculate base XP gain
            int xpGain = 5 + (monsterLevel / 4);
    
            // Get player's combat level
            int playerCombatLevel = xpManager.getPlayerLevel(playerKiller, "Combat");
    
            // Calculate bonus XP if monster level is higher than player's combat level
            if (monsterLevel > playerCombatLevel) {
                int levelDifference = monsterLevel - playerCombatLevel;
                int bonusXP = levelDifference * 2;
                xpGain += bonusXP;
            }

            // Skip combat XP for sea creatures and forest spirits
            if (!entity.hasMetadata("SEA_CREATURE") && !entity.hasMetadata("forestSpirit")) {
                // Cap combat XP and award it
                xpGain = Math.min(xpGain, 125);
                xpManager.addXP(playerKiller, "Combat", xpGain);
                // Increase forestry notoriety from combat
                Forestry.getInstance().addNotoriety(playerKiller, 3, false, false);
                ChampionManager.getInstance(MinecraftNew.getInstance())
                        .recordKill(playerKiller, entity.getLocation());
            }

            // 20% chance for loot to drop normally, else clear drops
            Random random = new Random();
            if (entity instanceof Monster) {
                Monster monsterEntity = (Monster) entity;
                if (!monsterEntity.hasMetadata("SEA_CREATURE") && !monsterEntity.hasMetadata("forestSpirit")) {
                    if (random.nextInt(100) < 2) {
                        int amount = 1;
                        PlayerMeritManager merit = PlayerMeritManager.getInstance(plugin);
                        if (merit.hasPerk(playerKiller.getUniqueId(), "Librarian")) {
                            amount = 2;
                        }
                        for (int i = 0; i < amount; i++) {
                            e.getDrops().add(ItemRegistry.getForbiddenBook());
                        }
                    }
                }
                boolean allowNormalDrops;
                if (entity instanceof Wither) {
                    // Always allow normal drops for Withers
                    allowNormalDrops = true;
                } else {
                    allowNormalDrops = random.nextInt(100) < 20;
                }
                if (!allowNormalDrops && entity instanceof Zombie zombie) {
                    ItemStack main = zombie.getEquipment().getItemInMainHand();
                    if (main != null && main.getType() != Material.AIR && !main.getType().name().endsWith("_SWORD")) {
                        zombie.getWorld().dropItemNaturally(zombie.getLocation(), main.clone());
                    }
                    ItemStack off = zombie.getEquipment().getItemInOffHand();
                    if (off != null && off.getType() != Material.AIR && !off.getType().name().endsWith("_SWORD")) {
                        zombie.getWorld().dropItemNaturally(zombie.getLocation(), off.clone());
                    }
                }
                if (allowNormalDrops) {
                    return; // Allow normal drops
                } else {
                    e.getDrops().clear();
                }
            }
        }
    }


    public int extractIntegerFromEntityName(Entity entity) {
        return levelExtractor.extractLevelFromName(entity);
    }


}
