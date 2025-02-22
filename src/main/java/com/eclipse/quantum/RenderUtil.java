package com.eclipse.quantum;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BeaconBlockEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;

public class RenderUtil {
    /**
     * Renders a beacon beam at the given position using vanilla beacon beam code.
     *
     * @param pos            The world position where the beam should originate.
     * @param matrices       The current MatrixStack.
     * @param vertexConsumers The VertexConsumerProvider.
     * @param partialTicks   The partial tick time.
     */
    public static void renderBeaconBeamAt(Vec3d pos, MatrixStack matrices, VertexConsumerProvider vertexConsumers, float partialTicks) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null) return;

        // Get the camera (render) position.
        Vec3d cameraPos = client.gameRenderer.getCamera().getPos();

        // Translate the matrix so that the beam is rendered at the correct world position relative to the camera.
        matrices.push();
        matrices.translate(pos.x - cameraPos.x, pos.y - cameraPos.y, pos.z - cameraPos.z);

        // Use the vanilla beacon beam texture.
        Identifier texture = BeaconBlockEntityRenderer.BEAM_TEXTURE;
        long worldTime = client.world.getTime();

        // When translated, the beam's origin becomes (0,0,0) in our local space.
        int yOffset = 0;
        int maxY = 256; // Adjust as needed for your beam's length.
        float heightScale = 1.0f;
        int color = 0xFF00FF00; // Green beam.
        float innerRadius = 0.15f;
        float outerRadius = 0.2f;

        BeaconBlockEntityRenderer.renderBeam(matrices, vertexConsumers, texture, partialTicks, heightScale, worldTime, yOffset, maxY, color, innerRadius, outerRadius);

        matrices.pop();
    }
}
