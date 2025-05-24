// src/main/java/goat/minecraft/minecraftnew/regions/RegionMapGenerator.java
package goat.minecraft.minecraftnew.regions;

import org.bukkit.util.noise.SimplexNoiseGenerator;
import java.util.*;

public class RegionMapGenerator {

    /**
     * Picks and validates one seed per region, then paints
     * each chunk as LAND / BEACH / OCEAN based on warped distance.
     *
     * @param worldSize       chunks per side
     * @param regions         land RegionTypes (exclude BEACH & OCEAN)
     * @param regionRadius    land‐core radius
     * @param beachWidth      beach‐ring thickness
     * @param minOceanGap     pure‐ocean gap between beaches
     * @param noiseScale      noise frequency
     * @param noiseAmplitude  noise warp strength (0.0–1.0)
     */
    public static RegionType[][] generateIslands(
            int worldSize,
            List<RegionType> regions,
            int regionRadius,
            int beachWidth,
            int minOceanGap,
            double noiseScale,
            double noiseAmplitude
    ) {
        RegionType[][] map = new RegionType[worldSize][worldSize];
        Random rnd = new Random();
        SimplexNoiseGenerator noise = new SimplexNoiseGenerator(rnd.nextLong());

        // 1) Pick valid seeds with rejection sampling
        int buffer     = regionRadius + beachWidth + minOceanGap;
        int minSpacing = 2*(regionRadius + beachWidth) + minOceanGap;

        record Seed(int x, int z, RegionType type) {}
        List<Seed> seeds = new ArrayList<>();

        outer:
        for (RegionType rt : regions) {
            for (int attempt = 0; attempt < 1000; attempt++) {
                int cx = rnd.nextInt(worldSize - 2*buffer) + buffer;
                int cz = rnd.nextInt(worldSize - 2*buffer) + buffer;

                // ensure not too close to any existing seed
                for (Seed s : seeds) {
                    if (Math.hypot(cx - s.x, cz - s.z) < minSpacing) {
                        continue outer;
                    }
                }

                seeds.add(new Seed(cx, cz, rt));
                continue outer;
            }
            throw new IllegalStateException("Failed to place region " + rt);
        }

        // 2) Paint each cell
        for (int x = 0; x < worldSize; x++) {
            for (int z = 0; z < worldSize; z++) {
                double best1 = Double.MAX_VALUE, best2 = Double.MAX_VALUE;
                RegionType bestType = null;

                // compute two nearest warped distances
                for (Seed s : seeds) {
                    double dx = x - s.x, dz = z - s.z;
                    double dist = Math.hypot(dx, dz);
                    double warp = dist + noise.noise(x*noiseScale, z*noiseScale)
                            * noiseAmplitude * regionRadius;

                    if (warp < best1) {
                        best2 = best1;
                        best1 = warp;
                        bestType = s.type;
                    } else if (warp < best2) {
                        best2 = warp;
                    }
                }

                // default ocean
                RegionType out = RegionType.OCEAN;

                double landThresh  = regionRadius;
                double beachThresh = regionRadius + beachWidth;
                double gapThresh   = beachThresh + minOceanGap;

                // BEACH only if first < beach && second > gap
                if (best1 <= beachThresh && best2 > gapThresh) {
                    out = RegionType.BEACH;
                }
                // LAND core overwrites BEACH
                if (best1 <= landThresh) {
                    out = bestType;
                }

                map[x][z] = out;
            }
        }

        return map;
    }
}
