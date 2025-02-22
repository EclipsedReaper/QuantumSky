package com.eclipse.quantum.mixin;

import net.minecraft.client.particle.Particle;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Particle.class)
public interface ParticleAccessor {
    @Accessor("x")
    double getX();

    @Accessor("y")
    double getY();

    @Accessor("z")
    double getZ();

    @Accessor("velocityX")
    double getVelocityX();

    @Accessor("velocityY")
    double getVelocityY();

    @Accessor("velocityZ")
    double getVelocityZ();
}
