package goat.minecraft.minecraftnew.subsystems.dragons;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.EntityType;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

/**
 * Represents a custom dragon definition used during a dragon fight.
 * Implementations provide static attributes such as colouring and
 * combat statistics.  Ability logic will be implemented later.
 */
public abstract class Dragon {

    private final String name;
    private final ChatColor nameColor;
    private final BarColor bossBarColor;
    private final int crystalBias;
    private final int flightSpeed;
    private final int baseRage;

    protected Dragon(String name, ChatColor nameColor, BarColor bossBarColor,
                     int crystalBias, int flightSpeed, int baseRage) {
        this.name = name;
        this.nameColor = nameColor;
        this.bossBarColor = bossBarColor;
        this.crystalBias = crystalBias;
        this.flightSpeed = flightSpeed;
        this.baseRage = baseRage;
    }

    public String getName() {
        return name;
    }

    public ChatColor getNameColor() {
        return nameColor;
    }

    public BarColor getBossBarColor() {
        return bossBarColor;
    }

    public int getCrystalBias() {
        return crystalBias;
    }

    public int getFlightSpeed() {
        return flightSpeed;
    }

    public int getBaseRage() {
        return baseRage;
    }

    /**
     * Spawns an {@link EnderDragon} in the given location and applies the
     * static attributes for this dragon type such as custom name and boss bar
     * configuration.  The dragon's stats are stored in its persistent data
     * container for future use by the combat logic.
     *
     * @param location location to spawn at
     * @param plugin   owning plugin used for namespaced keys
     * @return the spawned dragon entity
     */
    public EnderDragon spawn(Location location, Plugin plugin) {
        EnderDragon dragon = (EnderDragon) location.getWorld().spawnEntity(location, EntityType.ENDER_DRAGON);
        dragon.setCustomName(nameColor + name);
        dragon.setCustomNameVisible(true);
        BossBar bar = dragon.getBossBar();
        bar.setColor(bossBarColor);
        bar.setStyle(BarStyle.SEGMENTED_20);

        PersistentDataContainer data = dragon.getPersistentDataContainer();
        data.set(new NamespacedKey(plugin, "crystalBias"), PersistentDataType.INTEGER, crystalBias);
        data.set(new NamespacedKey(plugin, "flightSpeed"), PersistentDataType.INTEGER, flightSpeed);
        data.set(new NamespacedKey(plugin, "baseRage"), PersistentDataType.INTEGER, baseRage);
        return dragon;
    }
}
