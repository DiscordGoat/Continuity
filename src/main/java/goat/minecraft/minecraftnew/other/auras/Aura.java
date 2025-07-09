package goat.minecraft.minecraftnew.other.auras;

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
    SLAYER(Particle.FLAME, ParticleStyle.RING, 8, 100.0),
    COUNTERSHOT(Particle.SWEEP_ATTACK, ParticleStyle.RING, 4, 1.0),
    SHADOWSTEP(Particle.PORTAL, ParticleStyle.AMBIENT, 4, 10.0),
    STRIDER(Particle.CLOUD, ParticleStyle.TRAIL, 5, 20.0),
    DUSKBLOOD(Particle.PORTAL, ParticleStyle.AMBIENT, 4, 10.0),
    THUNDERFORGE(Particle.ASH, ParticleStyle.AMBIENT, 4, 10.0),
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
