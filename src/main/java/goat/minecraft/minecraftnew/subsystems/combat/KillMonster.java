package goat.minecraft.minecraftnew.subsystems.combat;

import goat.minecraft.minecraftnew.MinecraftNew;

import goat.minecraft.minecraftnew.other.additionalfunctionality.Pathfinder;
import goat.minecraft.minecraftnew.utils.devtools.XPManager;
import me.gamercoder215.mobchip.EntityBrain;
import me.gamercoder215.mobchip.ai.EntityAI;
import me.gamercoder215.mobchip.bukkit.BukkitBrain;
import org.bukkit.Location;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;

import java.util.Random;

public class KillMonster implements Listener {
    private final MinecraftNew plugin = MinecraftNew.getInstance();
    public XPManager xpManager = new XPManager(plugin);

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
            int monsterLevel = extractIntegerFromEntityName(entity);
            monsterLevel = Math.max(monsterLevel, 1);

            // Calculate XP gain
            int xpGain = 5 + (monsterLevel / 3);

            // Add XP to the player
            xpManager.addXP(playerKiller, "Combat", xpGain);

            // 20% chance for loot to drop normally, else clear drops
            Random random = new Random();
            if (entity instanceof Monster) {
                if (random.nextInt(100) < 20) {
                    return; // Allow normal drops
                } else {
                    e.getDrops().clear();
                }
            }
        }
    }


    public int extractIntegerFromEntityName(Entity entity) {
        String name = entity.getName();
        String cleanedName = name.replaceAll("(?i)§[0-9a-f]", "");
        String numberString = cleanedName.replaceAll("[^0-9]", "");
        if (numberString.isEmpty()) {
            return 0;
        }
        try {
            return Integer.parseInt(numberString);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return 0;
        }
    }


}
