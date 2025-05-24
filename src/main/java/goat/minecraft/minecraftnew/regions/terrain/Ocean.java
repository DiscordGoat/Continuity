// src/main/java/goat/minecraft/minecraftnew/regions/terrain/Ocean.java
package goat.minecraft.minecraftnew.regions.terrain;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.util.noise.SimplexNoiseGenerator;

public class Ocean {
    // Lowered by one block:
    private static final int SEA_LEVEL = 63;

    private final World world;
    private final SimplexNoiseGenerator boundaryNoise, warpNoise, baseNoise, ravineNoise;

    public Ocean(World world, long seed) {
        this.world       = world;
        this.boundaryNoise = new SimplexNoiseGenerator(seed);
        this.warpNoise     = new SimplexNoiseGenerator(seed+1);
        this.baseNoise     = new SimplexNoiseGenerator(seed+2);
        this.ravineNoise   = new SimplexNoiseGenerator(seed+3);
    }

    public void genOceanFloor(int cx, int cz) {
        int ox = cx << 4, oz = cz << 4;
        for (int dx = 0; dx < 16; dx++) for (int dz = 0; dz < 16; dz++) {
            int wx = ox+dx, wz = oz+dz;
            double dist = Math.hypot(wx, wz);
            double br   = SEA_LEVEL + boundaryNoise.noise(wx*0.03, wz*0.03)*30;
            double t    = Math.min(dist/br,1.0);
            double u = wx + warpNoise.noise(wx*0.005, wz*0.005)*30;
            double v = wz + warpNoise.noise(wx*0.005+100, wz*0.005+100)*30;
            double amp=1, freq=0.01, sum=0;
            for(int o=0;o<6;o++){ sum += baseNoise.noise(u*freq, v*freq)*amp; amp*=0.5; freq*=2; }
            double baseY = lerp(SEA_LEVEL-25, 5, t);
            double height = ((sum+1)/2)*35*Math.pow(t,2.5);
            int floorY = clamp((int)(baseY+height), 5, SEA_LEVEL-4);

            double rv = ravineNoise.noise(wx*0.1, wz*0.1);
            if (rv>0.6) floorY -= (int)((rv-0.6)*20);

            world.getBlockAt(wx, 0, wz).setType(Material.BEDROCK);
            for(int y=1;y<=floorY;y++){
                Material mat;
                if(y>floorY-3) mat=Material.GRAVEL;
                else if(floorY>=45 && y==floorY-3 && Math.random()<0.5){
                    mat=Material.DEEPSLATE_DIAMOND_ORE;
                    for (int dy=-1;dy<=1;dy++)for(int dx2=-1;dx2<=1;dx2++)for(int dz2=-1;dz2<=1;dz2++)
                        if(Math.abs(dx2)+Math.abs(dy)+Math.abs(dz2)<=2)
                            world.getBlockAt(wx+dx2,y+dy,wz+dz2).setType(Material.DEEPSLATE_DIAMOND_ORE);
                    continue;
                } else if(rv>0.6 && y==floorY && Math.random()<0.4) mat=Material.MAGMA_BLOCK;
                else if(y<30 && Math.random()<0.01) mat=Material.MAGMA_BLOCK;
                else mat=(y<30 ? Material.DEEPSLATE : Material.STONE);
                world.getBlockAt(wx, y, wz).setType(mat);
            }
            for(int y=floorY+1; y<=SEA_LEVEL; y++){
                world.getBlockAt(wx, y, wz).setType(Material.WATER);
            }
        }
    }

    private static double lerp(double a, double b, double t) { return a + (b - a) * t; }
    private static int clamp(int v, int lo, int hi)      { return v<lo?lo:(v>hi?hi:v); }
}
