package goat.minecraft.minecraftnew.subsystems.ai;

import goat.minecraft.minecraftnew.MinecraftNew;
import goat.minecraft.minecraftnew.subsystems.combat.KillMonster;
import goat.minecraft.minecraftnew.subsystems.utils.SpawnMonsters;
import goat.minecraft.minecraftnew.subsystems.utils.XPManager;
import me.gamercoder215.mobchip.EntityBrain;
import me.gamercoder215.mobchip.ai.EntityAI;
import me.gamercoder215.mobchip.bukkit.BukkitBrain;
import org.bukkit.Location;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;


public class BlazeAI implements Listener {
    private final MinecraftNew plugin = MinecraftNew.getInstance();
    public XPManager xpManager = new XPManager(plugin);




    @EventHandler
    public void onKill(EntityDeathEvent e) {
        Entity entity = e.getEntity();
        Entity killer = e.getEntity().getKiller();
        int level = extractIntegerFromEntityName(entity);

        if (killer instanceof Player && entity instanceof Blaze) {
            Player playerKiller = (Player) killer;

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
