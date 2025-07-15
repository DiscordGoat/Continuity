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

/**
 * Potion of Charismatic Bartering - grants extra villager trade discounts.
 */
public class PotionOfCharismaticBartering implements Listener {
    @EventHandler
    public void onPotionDrink(PlayerItemConsumeEvent event) {
        ItemStack item = event.getItem();
        if (item != null && item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
            String displayName = ChatColor.stripColor(item.getItemMeta().getDisplayName());
            if (displayName.equals("Potion of Charismatic Bartering")) {
                Player player = event.getPlayer();
                XPManager xpManager = new XPManager(MinecraftNew.getInstance());
                int duration = (60 * 3);
                if (SkillTreeManager.getInstance().hasTalent(player, Talent.CHARISMA_MASTERY)) {
                    int bonus = 50 * SkillTreeManager.getInstance()
                            .getTalentLevel(player.getUniqueId(), Skill.BREWING, Talent.CHARISMA_MASTERY);
                    duration += bonus;
                }
                PotionManager.addCustomPotionEffect("Potion of Charismatic Bartering", player, duration);
                player.sendMessage(ChatColor.GREEN + "Potion of Charismatic Bartering activated for " + duration + " seconds!");
                xpManager.addXP(player, "Brewing", 100);
            }
        }
    }
}
