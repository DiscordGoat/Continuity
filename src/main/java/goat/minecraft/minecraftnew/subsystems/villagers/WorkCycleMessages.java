package goat.minecraft.minecraftnew.subsystems.villagers;

import org.bukkit.entity.Villager;

import java.util.HashMap;
import java.util.Map;

public class WorkCycleMessages {
    private static final Map<Villager.Profession, String> REQUIREMENTS = new HashMap<>();

    static {
        REQUIREMENTS.put(Villager.Profession.FARMER, "If you place a hay bale nearby, I can harvest your crops for you.");
        REQUIREMENTS.put(Villager.Profession.FISHERMAN, "I can't fish here, but I can if there's some water nearby.");
        REQUIREMENTS.put(Villager.Profession.ARMORER, "If you display armor on an armor stand nearby, I can repair it.");
        REQUIREMENTS.put(Villager.Profession.WEAPONSMITH, "Display your weapons nearby and I'll fix them up.");
        REQUIREMENTS.put(Villager.Profession.TOOLSMITH, "Place your tools on display and I can repair them.");
        REQUIREMENTS.put(Villager.Profession.SHEPHERD, "I couldn't find any sheep nearby to shear.");
        REQUIREMENTS.put(Villager.Profession.LEATHERWORKER, "I couldn't find any cauldrons nearby to tan my leather.");
        REQUIREMENTS.put(Villager.Profession.FLETCHER, "Place a log nearby and I'll gather wood for you!");
        REQUIREMENTS.put(Villager.Profession.MASON, "Place a stone-like block on a smooth stone slab and I'll make more!");
    }

    public static String getRequirement(Villager.Profession profession) {
        return REQUIREMENTS.get(profession);
    }
}
