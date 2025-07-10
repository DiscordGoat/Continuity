package goat.minecraft.minecraftnew.subsystems.corpses;

import goat.minecraft.minecraftnew.subsystems.fishing.Rarity;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;

import java.util.*;

public class CorpseRegistry {
    private static final List<Corpse> CORPSES = new ArrayList<>();
    private static final Map<Rarity, Double> RARITY_WEIGHTS = new HashMap<>();
    private static final Random RANDOM = new Random();

    static {
        RARITY_WEIGHTS.put(Rarity.COMMON, 0.50);
        RARITY_WEIGHTS.put(Rarity.UNCOMMON, 0.30);
        RARITY_WEIGHTS.put(Rarity.RARE, 0.12);
        RARITY_WEIGHTS.put(Rarity.EPIC, 0.06);
        RARITY_WEIGHTS.put(Rarity.LEGENDARY, 0.02);

        // COMMON
        CORPSES.add(new Corpse("Farmer Corpse", Rarity.COMMON, 30, Material.IRON_HOE, false, "", null));
        CORPSES.add(new Corpse("Guard Corpse", Rarity.COMMON, 35, Material.IRON_SWORD, false, "", null));
        CORPSES.add(new Corpse("Archer Corpse", Rarity.COMMON, 40, Material.BOW, true, "", null));

        // UNCOMMON
        CORPSES.add(new Corpse("Diver Corpse", Rarity.UNCOMMON, 50, Material.TRIDENT, false, "", null));
        CORPSES.add(new Corpse("Fisherman Corpse", Rarity.UNCOMMON, 55, Material.FISHING_ROD, false, "", null));
        CORPSES.add(new Corpse("Lumberjack Corpse", Rarity.UNCOMMON, 60, Material.IRON_AXE, false, "", null));

        // RARE
        CORPSES.add(new Corpse("Adventurer Corpse", Rarity.RARE, 70, Material.MAP, false, "", null));
        CORPSES.add(new Corpse("Pirate Corpse", Rarity.RARE, 75, Material.IRON_SWORD, false, "", null));
        CORPSES.add(new Corpse("Sculk Infected", Rarity.RARE, 80, Material.STONE_SWORD, false, "", null));

        // EPIC
        CORPSES.add(new Corpse("Necromancer Corpse", Rarity.EPIC, 90, Material.SKELETON_SKULL, false, "", null));
        CORPSES.add(new Corpse("Gladiator Corpse", Rarity.EPIC, 100, Material.DIAMOND_SWORD, false, "", null));
        CORPSES.add(new Corpse("Duskblood", Rarity.EPIC, 110, Material.AIR, false, "", null));

        // LEGENDARY
        CORPSES.add(new Corpse("Trauma", Rarity.LEGENDARY, 120, Material.DIAMOND_AXE, false, "", null));
        CORPSES.add(new Corpse("Cryptic", Rarity.LEGENDARY, 140, Material.BOW, true, "", null));
        CORPSES.add(new Corpse("Dreadnaught", Rarity.LEGENDARY, 150, Material.NETHERITE_SWORD, false, "", null));
    }

    public static Optional<Corpse> getCorpseByName(String name) {
        return CORPSES.stream().filter(c -> c.getName().equalsIgnoreCase(name)).findFirst();
    }

    public static Optional<Corpse> getRandomCorpse() {
        double rand = RANDOM.nextDouble();
        double cumulative = 0.0;
        Rarity selected = Rarity.COMMON;
        for (Map.Entry<Rarity, Double> e : RARITY_WEIGHTS.entrySet()) {
            cumulative += e.getValue();
            if (rand <= cumulative) {
                selected = e.getKey();
                break;
            }
        }
        List<Corpse> filtered = new ArrayList<>();
        for (Corpse c : CORPSES) {
            if (c.getRarity() == selected) {
                filtered.add(c);
            }
        }
        if (filtered.isEmpty()) return Optional.empty();
        return Optional.of(filtered.get(RANDOM.nextInt(filtered.size())));
    }
}
