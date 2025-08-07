package goat.minecraft.minecraftnew.other.skilltree;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.EnumSet;
import java.util.Set;

public class FastFarmerBonus implements Listener {
    private final JavaPlugin plugin;
    private static final Set<Material> CROPS = EnumSet.of(
            Material.WHEAT,
            Material.NETHER_WART,
            Material.POTATOES,
            Material.CARROTS,
            Material.CARROT,
            Material.BEETROOTS,
            Material.MELON,
            Material.PUMPKIN
    );

    public FastFarmerBonus(JavaPlugin plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onCropBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();
        Material type = block.getType();

        if (!CROPS.contains(type)) return;

        if (block.getBlockData() instanceof Ageable age && age.getAge() != age.getMaximumAge()) {
            return;
        }

        int level = SkillTreeManager.getInstance().getTalentLevel(player.getUniqueId(), Skill.FARMING, Talent.FAST_FARMER);
        if (level > 0) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 10*level,  1));
        }
    }
}
