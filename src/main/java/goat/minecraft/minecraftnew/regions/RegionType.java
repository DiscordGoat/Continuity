// src/main/java/goat/minecraft/minecraftnew/regions/RegionType.java
package goat.minecraft.minecraftnew.regions;

import org.bukkit.Material;

public enum RegionType {
    FIELDS   (Material.WHITE_CONCRETE),
    SAFARI   (Material.ORANGE_CONCRETE),
    FOREST   (Material.GREEN_CONCRETE),
    JUNGLE   (Material.LIME_CONCRETE),
    MOUNTAIN (Material.LIGHT_GRAY_CONCRETE),
    SWAMP    (Material.CYAN_CONCRETE),
    MESA     (Material.RED_CONCRETE),
    DESERT   (Material.YELLOW_CONCRETE),
    ISLAND   (Material.BROWN_CONCRETE),
    // separators:
    BEACH    (Material.LIGHT_BLUE_CONCRETE),
    OCEAN    (Material.BLUE_CONCRETE);

    private final Material marker;
    RegionType(Material marker) { this.marker = marker; }
    public Material getMarker() { return marker; }
}
