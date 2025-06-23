package goat.minecraft.minecraftnew.subsystems.combat.bloodmoon;

import java.util.Random;

public class Range {
    private final int min;
    private final int max;

    public Range(int min, int max) {
        this.min = min;
        this.max = max;
    }

    public int getMin() {
        return min;
    }

    public int getMax() {
        return max;
    }

    public int random(Random random) {
        return min + random.nextInt(max - min + 1);
    }
}
