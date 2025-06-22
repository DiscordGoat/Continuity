package goat.minecraft.minecraftnew.subsystems.beacon;

import org.bukkit.ChatColor;
import org.bukkit.Particle;
import org.bukkit.Sound;

public enum CatalystType {
    POWER("Catalyst of Power", Particle.REDSTONE, Sound.ENTITY_WARDEN_HEARTBEAT, ChatColor.RED),
    FORTITUDE("Catalyst of Fortitude", Particle.SMOKE_NORMAL, Sound.ENTITY_IRON_GOLEM_STEP, ChatColor.GRAY),
    FLIGHT("Catalyst of Flight", Particle.CLOUD, null, ChatColor.AQUA),
    DEPTH("Catalyst of Depth", Particle.NAUTILUS, Sound.BLOCK_CONDUIT_AMBIENT, ChatColor.DARK_AQUA),
    INSANITY("Catalyst of Insanity", Particle.SPELL_WITCH, Sound.BLOCK_WOOD_BREAK, ChatColor.DARK_PURPLE),
    OXYGEN("Catalyst of Oxygen", Particle.SMOKE_LARGE, Sound.BLOCK_STONE_BREAK, ChatColor.BLUE);

    private final String displayName;
    private final Particle particle;
    private final Sound sound;
    private final ChatColor color;

    CatalystType(String displayName, Particle particle, Sound sound, ChatColor color) {
        this.displayName = displayName;
        this.particle = particle;
        this.sound = sound;
        this.color = color;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getColoredDisplayName() {
        return color + displayName;
    }

    public Particle getParticle() {
        return particle;
    }

    public Sound getSound() {
        return sound;
    }

    public ChatColor getColor() {
        return color;
    }

    public static CatalystType fromDisplayName(String displayName) {
        for (CatalystType type : values()) {
            if (type.displayName.equals(displayName)) {
                return type;
            }
        }
        return null;
    }
}