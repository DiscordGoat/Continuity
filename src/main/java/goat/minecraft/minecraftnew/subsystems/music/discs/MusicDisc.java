package goat.minecraft.minecraftnew.subsystems.music.discs;

import org.bukkit.Material;
import org.bukkit.entity.Player;

public interface MusicDisc {
    Material getDiscMaterial();
    void onUse(Player player);
}
