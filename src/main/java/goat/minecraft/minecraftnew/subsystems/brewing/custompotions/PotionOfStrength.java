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

public class PotionOfStrength implements Listener {

    /**
     * Handles consumption of the Potion of Strength.
     * Grants the custom effect for a base duration of 3 minutes.
     */
    @EventHandler
    public void onPotionDrink(PlayerItemConsumeEvent event) {
        ItemStack item = event.getItem();
        if (item != null && item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
            String displayName = ChatColor.stripColor(item.getItemMeta().getDisplayName());
            XPManager xpManager = new XPManager(MinecraftNew.getInstance());
            int duration = (60 * 3);
            if (displayName.equals("Potion of Strength")) {
                Player player = event.getPlayer();
                if(goat.minecraft.minecraftnew.other.skilltree.SkillTreeManager.getInstance()
                        .hasTalent(player, goat.minecraft.minecraftnew.other.skilltree.Talent.STRENGTH_MASTERY)) {
                    int bonus = 200 * goat.minecraft.minecraftnew.other.skilltree.SkillTreeManager.getInstance()
                            .getTalentLevel(player.getUniqueId(), goat.minecraft.minecraftnew.other.skilltree.Skill.BREWING,
                                    goat.minecraft.minecraftnew.other.skilltree.Talent.STRENGTH_MASTERY);
                    duration += bonus;
                }
                // Add the custom effect for the calculated duration
                PotionManager.addCustomPotionEffect("Potion of Strength", player, duration);
                player.sendMessage(ChatColor.GREEN + "Potion of Strength effect activated for " + duration + " seconds!");
                xpManager.addXP(player, "Brewing", 100);
            }
        }
    }
}
