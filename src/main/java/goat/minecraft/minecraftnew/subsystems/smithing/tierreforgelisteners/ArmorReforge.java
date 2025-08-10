package goat.minecraft.minecraftnew.subsystems.smithing.tierreforgelisteners;

import org.bukkit.event.Listener;

/**
 * Legacy listener for armor reforges. Reforges now grant flat Defense
 * through {@link goat.minecraft.minecraftnew.utils.stats.DefenseManager},
 * so this listener no longer applies additional damage reduction.
 */
public class ArmorReforge implements Listener {
    private final ReforgeManager reforgeManager;

    public ArmorReforge(ReforgeManager reforgeManager) {
        this.reforgeManager = reforgeManager;
    }
}
