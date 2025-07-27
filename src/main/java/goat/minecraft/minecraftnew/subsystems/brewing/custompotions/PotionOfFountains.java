package goat.minecraft.minecraftnew.subsystems.brewing.custompotions;

import goat.minecraft.minecraftnew.MinecraftNew;
import goat.minecraft.minecraftnew.subsystems.brewing.PotionManager;
import goat.minecraft.minecraftnew.utils.devtools.XPManager;
import goat.minecraft.minecraftnew.other.skilltree.Skill;
import goat.minecraft.minecraftnew.other.skilltree.SkillTreeManager;
import goat.minecraft.minecraftnew.other.skilltree.Talent;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;

public class PotionOfFountains implements Listener {
    @EventHandler
    public void onPotionDrink(PlayerItemConsumeEvent event) {
        ItemStack item = event.getItem();
        if (item != null && item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
            String displayName = ChatColor.stripColor(item.getItemMeta().getDisplayName());
            XPManager xpManager = new XPManager(MinecraftNew.getInstance());
            int duration = (60 * 3);
            if (displayName.equals("Potion of Fountains")) {
                Player player = event.getPlayer();
                if (SkillTreeManager.getInstance().hasTalent(player, Talent.FOUNTAIN_MASTERY)) {
                    int bonus = 200 * SkillTreeManager.getInstance()
                            .getTalentLevel(player.getUniqueId(), Skill.BREWING, Talent.FOUNTAIN_MASTERY);
                    duration += bonus;
                }
                // Add the custom effect
                PotionManager.addCustomPotionEffect("Potion of Fountains", player, duration);
                player.sendMessage(ChatColor.GREEN + "Potion of Fountains effect activated for " + duration + " seconds!");
                xpManager.addXP(player, "Brewing", 100);
            }
        }
    }
}