package goat.minecraft.minecraftnew.subsystems.brewing.custompotions;

import goat.minecraft.minecraftnew.MinecraftNew;
import goat.minecraft.minecraftnew.subsystems.brewing.PotionManager;
import goat.minecraft.minecraftnew.subsystems.brewing.PotionEffectPreferences;
import goat.minecraft.minecraftnew.utils.devtools.XPManager;
import goat.minecraft.minecraftnew.other.skilltree.Skill;
import goat.minecraft.minecraftnew.other.skilltree.SkillTreeManager;
import goat.minecraft.minecraftnew.other.skilltree.Talent;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.UUID;

public class PotionOfSwiftStep implements Listener {


    /**
     * When a player drinks a Potion of Swift Step, apply the custom effect for a duration based on Brewing level.
     */
    @EventHandler
    public void onPotionDrink(PlayerItemConsumeEvent event) {
        ItemStack item = event.getItem();
        if (item != null && item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
            String displayName = ChatColor.stripColor(item.getItemMeta().getDisplayName());
            if (displayName.equals("Potion of Swift Step")) {
                Player player = event.getPlayer();
                XPManager xpManager = new XPManager(MinecraftNew.getInstance());
                int duration = (60 * 3); // Custom scaling
                if (SkillTreeManager.getInstance().hasTalent(player, Talent.SWIFT_STEP_MASTERY)) {
                    int bonus = 50 * SkillTreeManager.getInstance()
                            .getTalentLevel(player.getUniqueId(), Skill.BREWING, Talent.SWIFT_STEP_MASTERY);
                    duration += bonus;
                }
                PotionManager.addCustomPotionEffect("Potion of Swift Step", player, duration);
                player.sendMessage(ChatColor.AQUA + "Potion of Swift Step activated for " + duration + " seconds!");
                xpManager.addXP(player, "Brewing", 100);
            }
        }
    }

    /**
     * Grants the player a Speed II effect while the custom potion is active.
     */
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();

        if (PotionManager.isActive("Potion of Swift Step", player)
                && PotionEffectPreferences.isEnabled(player, "Potion of Swift Step")) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 15 * 20, 1, false, false));
        }
    }
}
