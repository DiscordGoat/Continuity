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

import java.util.List;

public class PotionOfLiquidLuck implements Listener {

    @EventHandler
    public void onPotionDrink(PlayerItemConsumeEvent event) {
        ItemStack item = event.getItem();
        if (item != null && item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
            String displayName = ChatColor.stripColor(item.getItemMeta().getDisplayName());
            XPManager xpManager = new XPManager(MinecraftNew.getInstance());
            int duration = (60 * 3);
            if (displayName.equals("Potion of Liquid Luck")) {
                Player player = event.getPlayer();
                if(goat.minecraft.minecraftnew.other.skilltree.SkillTreeManager.getInstance()
                        .hasTalent(player, goat.minecraft.minecraftnew.other.skilltree.Talent.LIQUID_LUCK_MASTERY)) {
                    int bonus = 50 * goat.minecraft.minecraftnew.other.skilltree.SkillTreeManager.getInstance()
                            .getTalentLevel(player.getUniqueId(), goat.minecraft.minecraftnew.other.skilltree.Skill.BREWING,
                                    goat.minecraft.minecraftnew.other.skilltree.Talent.LIQUID_LUCK_MASTERY);
                    duration += bonus;
                }
                // Add the custom effect for 15 seconds
                PotionManager.addCustomPotionEffect("Potion of Liquid Luck", player, duration);
                player.sendMessage(ChatColor.GREEN + "Potion of Liquid Luck effect activated for " + duration + " seconds!");
                xpManager.addXP(player, "Brewing", 100);
            }
        }
    }
}
