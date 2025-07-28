package goat.minecraft.minecraftnew.subsystems.villagers;

import goat.minecraft.minecraftnew.MinecraftNew;
import goat.minecraft.minecraftnew.other.skilltree.Skill;
import goat.minecraft.minecraftnew.other.skilltree.SkillTreeManager;
import goat.minecraft.minecraftnew.other.skilltree.Talent;
import org.bukkit.Bukkit;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityTransformEvent;

public class VillagerTalentListener implements Listener {
    public VillagerTalentListener() {
        Bukkit.getPluginManager().registerEvents(this, MinecraftNew.getInstance());
    }

    @EventHandler
    public void onVillagerDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Villager)) return;
        SkillTreeManager mgr = SkillTreeManager.getInstance();
        if (mgr == null) return;
        int highest = 0;
        for (Player p : Bukkit.getOnlinePlayers()) {
            int lvl = mgr.getTalentLevel(p.getUniqueId(), Skill.BARTERING, Talent.UNIFORM);
            if (lvl > highest) highest = lvl;
        }
        if (highest > 0) {
            double reduction = highest * 0.10;
            event.setDamage(event.getDamage() * (1 - reduction));
        }
    }

    @EventHandler
    public void onVillagerTransform(EntityTransformEvent event) {
        if (event.getEntityType() != EntityType.VILLAGER) return;
        if (event.getTransformedEntityType() != EntityType.ZOMBIE_VILLAGER && event.getTransformedEntityType() != EntityType.WITCH)
            return;
        SkillTreeManager mgr = SkillTreeManager.getInstance();
        if (mgr == null) return;
        int highest = 0;
        for (Player p : Bukkit.getOnlinePlayers()) {
            int lvl = mgr.getTalentLevel(p.getUniqueId(), Skill.BARTERING, Talent.ITS_ALIVE);
            if (lvl > highest) highest = lvl;
        }
        if (highest > 0 && Math.random() < highest * 0.20) {
            event.setCancelled(true);
        }
    }
}
