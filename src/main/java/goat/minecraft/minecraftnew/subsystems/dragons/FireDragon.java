package goat.minecraft.minecraftnew.subsystems.dragons;

import org.bukkit.ChatColor;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.entity.EnderDragon;

/**
 * Concrete implementation for the Fire Dragon.
 *
 * <p>The Fire Dragon represents an aggressive counterpart to the healing
 * focused {@link WaterDragon}. It boasts higher rage, reduced decision
 * intervals and a larger health pool.</p>
 */
public class FireDragon implements Dragon {

    private static final int CRYSTAL_BIAS = 5;
    private static final int FLIGHT_SPEED = 8;
    private static final int BASE_RAGE = 7;
    private static final int MAX_HEALTH = 3_000;
    private static final int DECISION_INTERVAL = 10 * 20; // 10 seconds

    @Override
    public ChatColor getNameColor() {
        return ChatColor.RED;
    }

    @Override
    public String getName() {
        return "Fire Dragon";
    }

    @Override
    public BarColor getBarColor() {
        return BarColor.RED;
    }

    @Override
    public BarStyle getBarStyle() {
        return BarStyle.SEGMENTED_12;
    }

    @Override
    public int getCrystalBias() {
        return CRYSTAL_BIAS;
    }

    @Override
    public int getFlightSpeed() {
        return FLIGHT_SPEED;
    }

    @Override
    public int getBaseRage() {
        return BASE_RAGE;
    }

    @Override
    public int getMaxHealth() {
        return MAX_HEALTH;
    }

    @Override
    public int getDecisionInterval() {
        return DECISION_INTERVAL;
    }

    @Override
    public void decide(EnderDragon dragon) {
        // Future AI for the Fire Dragon will be implemented here.
    }

    @Override
    public void applyAttributes(EnderDragon dragon) {
        dragon.setCustomName(getDisplayName());
        dragon.setCustomNameVisible(true);
    }
}
