package goat.minecraft.minecraftnew.subsystems.auras;

import org.bukkit.Particle;

/**
 * Template definition for an aura effect.
 */
public enum Aura {
    NATURES_WRATH(Particle.SOUL, ParticleStyle.AMBIENT, 1, 5.0),
    DWELLER(Particle.CRIT, ParticleStyle.RING, 8, 100.0),
    PASTURESHADE(Particle.GLOW_SQUID_INK, ParticleStyle.AMBIENT, 1, 5.0),
    SCORCHSTEEL(Particle.FLAME, ParticleStyle.RING, 8, 100.0),
    MONOLITH(Particle.FLAME, ParticleStyle.RING, 8, 100.0),
    LOST_LEGION(Particle.ASH, ParticleStyle.RING, 8, 100.0),
    FATHMIC_IRON(Particle.END_ROD, ParticleStyle.AMBIENT, 1, 5.0);

    private final Particle particle;
    private final ParticleStyle style;
    private final int count;
    private final double frequency;

    Aura(Particle particle, ParticleStyle style, int count, double frequency) {
        this.particle = particle;
        this.style = style;
        this.count = count;
        this.frequency = frequency;
    }

    public Particle getParticle() {
        return particle;
    }

    public ParticleStyle getStyle() {
        return style;
    }

    public int getCount() {
        return count;
    }

    public double getFrequency() {
        return frequency;
    }
}
