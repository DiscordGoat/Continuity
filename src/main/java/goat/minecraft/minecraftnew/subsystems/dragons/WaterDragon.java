package goat.minecraft.minecraftnew.subsystems.dragons;

import org.bukkit.ChatColor;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.entity.EnderDragon;

/**
 * Concrete implementation for the Water Dragon.
 *
 * <p>This dragon focuses on defining base statistics and visual presentation
 * only. Actual combat abilities will be implemented in future iterations.</p>
 */
public class WaterDragon implements Dragon {

    private static final int CRYSTAL_BIAS = 10;
    private static final int FLIGHT_SPEED = 4;
    private static final int BASE_RAGE = 2;
    private static final int MAX_HEALTH = 1_000;
    private static final int DECISION_INTERVAL = 30; // placeholder

    @Override
    public ChatColor getNameColor() {
        return ChatColor.AQUA;
    }

    @Override
    public String getName() {
        return "Water Dragon";
    }

    @Override
    public BarColor getBarColor() {
        return BarColor.BLUE;
    }

    @Override
    public BarStyle getBarStyle() {
        // Using 10 segments for a clear, game-like boss bar.
        return BarStyle.SEGMENTED_10;
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
        // Future AI for the Water Dragon will be implemented here.
    }

    @Override
    public void applyAttributes(EnderDragon dragon) {
        dragon.setCustomName(getDisplayName());
        dragon.setCustomNameVisible(true);
        // Additional attribute application (movement, damage etc.) will be
        // implemented later when abilities are introduced.
    }
}
