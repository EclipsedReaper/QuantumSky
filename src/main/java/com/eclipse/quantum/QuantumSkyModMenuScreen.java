package com.eclipse.quantum;

import com.eclipse.quantum.config.Config;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

@Environment(EnvType.CLIENT)
public class QuantumSkyModMenuScreen extends Screen {
    private final Screen parent;
    protected QuantumSkyModMenuScreen(Screen parent) {
        super(Text.literal("Quantum Sky Options"));
        this.parent = parent;
    }


    public ButtonWidget toggleDianaHelper;
    public ButtonWidget toggleAutoWarp;
    public ButtonWidget backButton;

    @Override
    protected void init() {
        toggleDianaHelper = ButtonWidget.builder(Text.literal("Diana Helper [" + (Config.isDianaHelperActive() ? "§aEnabled" : "§cDisabled") + "§r]"), button -> {
                    Config.toggleDianaHelper();
                    button.setMessage(Text.literal("Diana Helper [" + (Config.isDianaHelperActive() ? "§aEnabled" : "§cDisabled") + "§r]"));
                })
                .dimensions(width / 2 - 205, (int) (height * 0.15), 200, 20)
                .tooltip(Tooltip.of(Text.literal("Automatically triangulate burrows and display a beacon at their estimated location.")))
                .build();
        toggleAutoWarp = ButtonWidget.builder(Text.literal("Auto Warp [" + (Config.isDianaAutoWarpActive() ? "§aEnabled" : "§cDisabled") + "§r]"), button -> {
                    Config.toggleDianaAutoWarp();
                    button.setMessage(Text.literal("Auto Warp [" + (Config.isDianaAutoWarpActive() ? "§aEnabled" : "§cDisabled") + "§r]"));
                })
                .dimensions(width / 2 + 5, (int) (height * 0.15), 200, 20)
                .tooltip(Tooltip.of(Text.literal("Automatically warp to the nearest warp location upon successfully triangulating a burrow.")))
                .build();
        backButton = ButtonWidget.builder(Text.literal("Back"), button -> close())
                .dimensions(width / 2 - 100, (int) (height * 0.9), 200, 20)
                .build();

        addDrawableChild(toggleDianaHelper);
        addDrawableChild(toggleAutoWarp);
        addDrawableChild(backButton);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(textRenderer, Text.literal("§dQuantum§bSky§r Options"), width / 2, (int) (height * 0.07), 0xffffff);
    }

    @Override
    public void close() {
        client.setScreen(parent);
    }
}
