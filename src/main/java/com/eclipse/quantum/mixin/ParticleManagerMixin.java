package com.eclipse.quantum.mixin;

import com.eclipse.quantum.MythologicalRitual;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.ParticleS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public abstract class ParticleManagerMixin {
	@Inject(method = "onParticle", at = @At("RETURN"))
	private void onParticle(ParticleS2CPacket packet, CallbackInfo ci) {
		MythologicalRitual.onParticle(packet);
	}
}
