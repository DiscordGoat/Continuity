package goat.minecraft.minecraftnew.other.beacon;

import org.bukkit.ChatColor;
import org.bukkit.Particle;
import org.bukkit.Sound;

public enum CatalystType {
    POWER("Catalyst of Power", Particle.CRIMSON_SPORE, null, ChatColor.RED),
    FLIGHT("Catalyst of Flight", Particle.CLOUD, null, ChatColor.AQUA),
    DEPTH("Catalyst of Depth", Particle.NAUTILUS, null, ChatColor.DARK_AQUA),
    INSANITY("Catalyst of Insanity", Particle.CRIMSON_SPORE, null, ChatColor.DARK_PURPLE),
    REJUVENATION("Catalyst of Rejuvenation", Particle.HEART, null, ChatColor.GOLD),
    PROSPERITY("Catalyst of Prosperity", Particle.CRIMSON_SPORE, null, ChatColor.GREEN),
    DEATH("Catalyst of Death", Particle.SOUL, null, ChatColor.DARK_GRAY);

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