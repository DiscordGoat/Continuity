package goat.minecraft.minecraftnew.subsystems.brewing.custompotions;

import goat.minecraft.minecraftnew.MinecraftNew;
import goat.minecraft.minecraftnew.subsystems.brewing.PotionManager;
import goat.minecraft.minecraftnew.subsystems.brewing.PotionEffectPreferences;
import goat.minecraft.minecraftnew.utils.devtools.XPManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;

public class PotionOfStrength implements Listener {

    /**
     * Listens for a player drinking a potion.
     * If the potion’s display name (after stripping colors) equals "Potion of Strength",
     * the custom effect is added for 15 seconds.
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
                // Add the custom effect for 15 seconds
                PotionManager.addCustomPotionEffect("Potion of Strength", player, duration);
                player.sendMessage(ChatColor.GREEN + "Potion of Strength effect activated for " + duration + " seconds!");
                xpManager.addXP(player, "Brewing", 100);
            }
        }
    }

    /**
     * Listens for when a player deals damage.
     * If the damager is a player with an active "Potion of Strength" effect,
     * increases the damage by 15%.
     */
    @EventHandler
    public void onPlayerDamage(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player) {
            Player player = (Player) event.getDamager();
            if (PotionManager.isActive("Potion of Strength", player)
                    && PotionEffectPreferences.isEnabled(player, "Potion of Strength")) {
                double extraDamage = event.getDamage() * 0.25;
                event.setDamage(event.getDamage() + extraDamage);
            }
        }
    }
}
