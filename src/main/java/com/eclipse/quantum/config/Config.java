package com.eclipse.quantum.config;

public class Config {
    public boolean dianaHelper = false;
    public boolean dianaAutoWarp = false;

    public static void toggleDianaHelper() {
        Config config = ConfigManager.getConfig();
        config.dianaHelper = !config.dianaHelper;
        ConfigManager.saveConfig();
    }

    public static boolean isDianaHelperActive() {
        Config config = ConfigManager.getConfig();
        return config.dianaHelper;
    }

    public static void toggleDianaAutoWarp() {
        Config config = ConfigManager.getConfig();
        config.dianaAutoWarp = !config.dianaAutoWarp;
        ConfigManager.saveConfig();
    }

    public static boolean isDianaAutoWarpActive() {
        Config config = ConfigManager.getConfig();
        return config.dianaAutoWarp;
    }
}
