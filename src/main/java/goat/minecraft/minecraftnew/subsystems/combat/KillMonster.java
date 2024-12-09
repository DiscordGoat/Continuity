package goat.minecraft.minecraftnew.subsystems.combat;

import goat.minecraft.minecraftnew.MinecraftNew;

import goat.minecraft.minecraftnew.subsystems.utils.XPManager;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.*;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class KillMonster implements Listener {
    private final MinecraftNew plugin = MinecraftNew.getInstance();
    public XPManager xpManager = new XPManager(plugin);

    // Blood Moon Mechanics
    private boolean isBloodMoon = false;
    private long lastCheckedTime = -1;


    @EventHandler
    public void onKill(EntityDeathEvent e) {
        Entity entity = e.getEntity();
        Entity killer = e.getEntity().getKiller();


        if (killer instanceof Player && entity instanceof Monster) {
            Player playerKiller = (Player) killer;

            // Get the monster's level (ensure it's at least 1)
            int monsterLevel = extractIntegerFromEntityName(entity);
            monsterLevel = Math.max(monsterLevel, 1);

            // Calculate XP gain
            int xpGain = 5 + (monsterLevel / 5);

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
