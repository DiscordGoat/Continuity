package goat.minecraft.minecraftnew.subsystems.brewing.custompotions;

import goat.minecraft.minecraftnew.MinecraftNew;
import goat.minecraft.minecraftnew.subsystems.brewing.PotionManager;
import goat.minecraft.minecraftnew.other.skilltree.Skill;
import goat.minecraft.minecraftnew.other.skilltree.SkillTreeManager;
import goat.minecraft.minecraftnew.other.skilltree.Talent;
import goat.minecraft.minecraftnew.utils.devtools.XPManager;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;

/**
 * Potion of Oxygen Recovery - reduces oxygen recovery interval while active.
 */
public class PotionOfOxygenRecovery implements Listener {

    @EventHandler
    public void onPotionDrink(PlayerItemConsumeEvent event) {
        ItemStack item = event.getItem();
        if (item != null && item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
            String displayName = ChatColor.stripColor(item.getItemMeta().getDisplayName());
            if (displayName.equals("Potion of Oxygen Recovery")) {
                Player player = event.getPlayer();
                XPManager xpManager = new XPManager(MinecraftNew.getInstance());
                int brewingLevel = xpManager.getPlayerLevel(player, "Brewing");
                int duration = (60 * 3) + (brewingLevel * 10);
                if (SkillTreeManager.getInstance().hasTalent(player, Talent.OXYGEN_MASTERY)) {
                    int level = SkillTreeManager.getInstance().getTalentLevel(player.getUniqueId(), Skill.BREWING, Talent.OXYGEN_MASTERY);
                    duration += level * 50;
                }
                PotionManager.addCustomPotionEffect("Potion of Oxygen Recovery", player, duration);
                player.sendMessage(ChatColor.AQUA + "Potion of Oxygen Recovery activated for " + duration + " seconds!");
                xpManager.addXP(player, "Brewing", 100);
            }
        }
    }
}
