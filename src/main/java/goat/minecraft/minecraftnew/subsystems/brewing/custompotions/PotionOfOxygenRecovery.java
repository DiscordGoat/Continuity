package goat.minecraft.minecraftnew.subsystems.brewing.custompotions;

import goat.minecraft.minecraftnew.MinecraftNew;
import goat.minecraft.minecraftnew.subsystems.brewing.PotionManager;
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
                if(goat.minecraft.minecraftnew.other.skilltree.SkillTreeManager.getInstance()
                        .hasTalent(player, goat.minecraft.minecraftnew.other.skilltree.Talent.OXYGEN_MASTERY)) {
                    int bonus = 50 * goat.minecraft.minecraftnew.other.skilltree.SkillTreeManager.getInstance()
                            .getTalentLevel(player.getUniqueId(), goat.minecraft.minecraftnew.other.skilltree.Skill.BREWING,
                                    goat.minecraft.minecraftnew.other.skilltree.Talent.OXYGEN_MASTERY);
                    duration += bonus;
                }
                PotionManager.addCustomPotionEffect("Potion of Oxygen Recovery", player, duration);
                player.sendMessage(ChatColor.AQUA + "Potion of Oxygen Recovery activated for " + duration + " seconds!");
                xpManager.addXP(player, "Brewing", 100);
            }
        }
    }
}
