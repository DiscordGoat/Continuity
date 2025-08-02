package goat.minecraft.minecraftnew.subsystems.dragons;

import org.bukkit.ChatColor;
import org.bukkit.boss.BarColor;

/**
 * Definition for the Water Dragon.  Only static attributes are
 * implemented at this stage; ability logic will be added later.
 */
public class WaterDragon extends Dragon {

    public WaterDragon() {
        // crystalBias 7, flightSpeed 4, baseRage 2
        super("Water Dragon", ChatColor.AQUA, BarColor.BLUE, 7, 4, 2);
    }
}
