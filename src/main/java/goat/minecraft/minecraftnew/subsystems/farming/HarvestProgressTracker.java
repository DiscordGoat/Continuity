package goat.minecraft.minecraftnew.subsystems.farming;

import org.bukkit.Material;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class HarvestProgressTracker {
    public static class Progress {
        public final Material crop;
        public final int current;
        public final int requirement;
        public Progress(Material crop, int current, int requirement) {
            this.crop = crop; this.current = current; this.requirement = requirement;
        }
    }

    private static final Map<UUID, Progress> progressMap = new HashMap<>();

    public static void set(UUID id, Material crop, int current, int requirement) {
        progressMap.put(id, new Progress(crop, current, requirement));
    }

    public static Progress get(UUID id) {
        return progressMap.get(id);
    }
}

