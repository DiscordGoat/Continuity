package goat.minecraft.minecraftnew.subsystems.corpses;

import goat.minecraft.minecraftnew.subsystems.fishing.Rarity;
import org.bukkit.Material;
import org.bukkit.event.Listener;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Registry for all Corpse definitions and weighted random selection.
 */
public class CorpseRegistry implements Listener {
    private static final List<Corpse> CORPSES = new ArrayList<>();
    private static final Map<Rarity, Double> RARITY_WEIGHTS = new LinkedHashMap<>();
    private static final Random RANDOM = new Random();
    private static final String DEFAULT_TEXTURE = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMTU4NzNmMDg2NTk1YWQ2OTQyODc4NTM1ZjgxMmFhYjUzNzg1OTQ3MTVmNjk1Yjc3MGMxNzM4YzFmZDNlZTRmZCJ9fX0=";

    static {
        RARITY_WEIGHTS.put(Rarity.COMMON, 0.50);
        RARITY_WEIGHTS.put(Rarity.UNCOMMON, 0.30);
        RARITY_WEIGHTS.put(Rarity.RARE, 0.12);
        RARITY_WEIGHTS.put(Rarity.EPIC, 0.06);
        RARITY_WEIGHTS.put(Rarity.LEGENDARY, 0.02);

        // COMMON
        CORPSES.add(new Corpse("Farmer Corpse", Rarity.COMMON, 30,
                Material.IRON_HOE, false, new ArrayList<>(), DEFAULT_TEXTURE));
        CORPSES.add(new Corpse("Guard Corpse", Rarity.COMMON, 35,
                Material.IRON_SWORD, false, new ArrayList<>(), DEFAULT_TEXTURE));
        CORPSES.add(new Corpse("Archer Corpse", Rarity.COMMON, 40,
                Material.BOW, true, new ArrayList<>(), DEFAULT_TEXTURE));

        // UNCOMMON
        CORPSES.add(new Corpse("Diver Corpse", Rarity.UNCOMMON, 50,
                Material.AIR, false, new ArrayList<>(), DEFAULT_TEXTURE));
        CORPSES.add(new Corpse("Fisherman Corpse", Rarity.UNCOMMON, 55,
                Material.FISHING_ROD, false, new ArrayList<>(), DEFAULT_TEXTURE));
        CORPSES.add(new Corpse("Lumberjack Corpse", Rarity.UNCOMMON, 60,
                Material.IRON_AXE, false, new ArrayList<>(), DEFAULT_TEXTURE));

        // RARE
        CORPSES.add(new Corpse("Adventurer Corpse", Rarity.RARE, 70,
                Material.MAP, false, new ArrayList<>(), DEFAULT_TEXTURE));
        CORPSES.add(new Corpse("Pirates Corpse", Rarity.RARE, 75,
                Material.IRON_SWORD, false, new ArrayList<>(), DEFAULT_TEXTURE));
        CORPSES.add(new Corpse("Sculk Infected", Rarity.RARE, 80,
                Material.AIR, false, new ArrayList<>(), DEFAULT_TEXTURE));

        // EPIC
        CORPSES.add(new Corpse("Necromancer Corpse", Rarity.EPIC, 90,
                Material.SKELETON_SKULL, false, new ArrayList<>(), DEFAULT_TEXTURE));
        CORPSES.add(new Corpse("Gladiator Corpse", Rarity.EPIC, 100,
                Material.DIAMOND_SWORD, false, new ArrayList<>(), DEFAULT_TEXTURE));
        CORPSES.add(new Corpse("Duskblood", Rarity.EPIC, 110,
                Material.AIR, false, new ArrayList<>(), DEFAULT_TEXTURE));

        // LEGENDARY
        CORPSES.add(new Corpse("Trauma", Rarity.LEGENDARY, 120,
                Material.DIAMOND_AXE, false, new ArrayList<>(), DEFAULT_TEXTURE));
        CORPSES.add(new Corpse("Cryptic", Rarity.LEGENDARY, 140,
                Material.BOW, true, new ArrayList<>(), DEFAULT_TEXTURE));
        CORPSES.add(new Corpse("Dreadnaught", Rarity.LEGENDARY, 150,
                Material.NETHERITE_SWORD, false, new ArrayList<>(), DEFAULT_TEXTURE));
    }

    public static Optional<Corpse> getCorpseByName(String name) {
        return CORPSES.stream()
                .filter(c -> c.getDisplayName().equalsIgnoreCase(name))
                .findFirst();
    }

    public static Optional<Corpse> getRandomCorpse() {
        if (CORPSES.isEmpty()) return Optional.empty();

        double rand = RANDOM.nextDouble();
        double cumulative = 0.0;
        Rarity selected = Rarity.COMMON;
        for (Map.Entry<Rarity, Double> entry : RARITY_WEIGHTS.entrySet()) {
            cumulative += entry.getValue();
            if (rand <= cumulative) {
                selected = entry.getKey();
                break;
            }
        }
        Rarity finalSelected = selected;
        List<Corpse> filtered = CORPSES.stream()
                .filter(c -> c.getRarity() == finalSelected)
                .collect(Collectors.toList());
        if (filtered.isEmpty()) return Optional.empty();
        return Optional.of(filtered.get(RANDOM.nextInt(filtered.size())));
    }

    public static List<Corpse> getCorpses() {
        return Collections.unmodifiableList(CORPSES);
    }
}
