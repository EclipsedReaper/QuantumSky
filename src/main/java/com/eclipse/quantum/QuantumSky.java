package com.eclipse.quantum;

import com.eclipse.quantum.config.Config;
import net.fabricmc.api.ClientModInitializer;

import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class QuantumSky implements ClientModInitializer {
	public static final String MOD_ID = "quantumsky";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	public static final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
	public static KeyBinding modMenuKey;

	public static Vec3d burrowWaypoint = null;

	@Override
	public void onInitializeClient() {
		// Register commands: /diana start to begin tracking and /diana reset to clear.
		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
			dispatcher.register(
					net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal("qs")
							.then(net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal("diana")
									.then(net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal("autoWarp")
											.executes(context -> {
												Config.toggleDianaAutoWarp();
												context.getSource().sendFeedback(Text.literal("[§dQuantum§bSky§r] " + (Config.isDianaAutoWarpActive() ? "§aEnabled" : "§cDisabled") + " §bAuto Warp for Mythological Ritual."));
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
												Config.toggleDianaHelper();
												if (Config.isDianaHelperActive()) {
													MythologicalRitual.init();
												}
												context.getSource().sendFeedback(Text.literal("[§dQuantum§bSky§r] " + (Config.isDianaHelperActive() ? "§aEnabled" : "§cDisabled") + " §bMythological Ritual Helper."));
												return 1;
											})))
			);
		});

		modMenuKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.quantumsky.modmenu", GLFW.GLFW_KEY_M, "category.quantumsky"));
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			while (modMenuKey.wasPressed()) {
				client.setScreen(new QuantumSkyModMenuScreen(client.currentScreen));
			}
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