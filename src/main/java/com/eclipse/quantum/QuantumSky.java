package com.eclipse.quantum;

import net.fabricmc.api.ClientModInitializer;

import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class QuantumSky implements ClientModInitializer {
	public static final String MOD_ID = "quantumsky";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	public static final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

	public static Vec3d burrowWaypoint = null;

	public static boolean isMythologicalRitualActive = false;

	@Override
	public void onInitializeClient() {
		// Register commands: /diana start to begin tracking and /diana reset to clear.
		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
			dispatcher.register(
					net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal("qs")
							.then(net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal("diana")
									.then(net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal("autoWarp")
											.executes(context -> {
												MythologicalRitual.toggleAutoWarp();
												context.getSource().sendFeedback(Text.literal("[§dQuantum§bSky§r] " + (MythologicalRitual.isAutoWarpActive() ? "§aEnabled" : "§cDisabled") + " §bAuto Warp for Mythological Ritual."));
												return 1;
											}))
									.then(net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal("reset")
											.executes(context -> {
												MythologicalRitual.reset();
												burrowWaypoint = null;
												context.getSource().sendFeedback(Text.literal("[§dQuantum§bSky§r] §bReset values for Mythological Ritual Helper."));
												return 1;
											}))
									.then(net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal("toggle")
											.executes(context -> {
												isMythologicalRitualActive = !isMythologicalRitualActive;
												if (isMythologicalRitualActive) {
													MythologicalRitual.init();
												}
												context.getSource().sendFeedback(Text.literal("[§dQuantum§bSky§r] " + (isMythologicalRitualActive ? "§aEnabled" : "§cDisabled") + " §bMythological Ritual Helper."));
												return 1;
											})))
			);
		});

		// Render our beacon beam overlay (using vanilla beam code) if a burrowWaypoint exists.
		WorldRenderEvents.LAST.register(context -> {
			if (burrowWaypoint != null) {
				// Render using our custom utility. We pass in the partial tick value.
				float tickDelta = ((System.currentTimeMillis() % 50) / 50.0f);
				RenderUtil.renderBeaconBeamAt(burrowWaypoint, context.matrixStack(), context.consumers(), tickDelta);
			}
		});
	}
}