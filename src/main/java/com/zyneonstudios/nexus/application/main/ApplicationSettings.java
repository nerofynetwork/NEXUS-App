package com.zyneonstudios.nexus.application.main;

import java.util.HashMap;

public class ApplicationSettings {

    private final HashMap<String, Object> temporarySettings = new HashMap<>();
    private int memory = 1024;
    private boolean minimizeApp = true;
    private boolean nativeWindow = true;

    public HashMap<String, Object> getTemporarySettings() {
        return temporarySettings;
    }

    public void setTemporarySettings(String path, Object value) {
        temporarySettings.put(path, value);
    }

    public Object getTemporarySettings(String path) {
        return temporarySettings.get(path);
    }

    public String getTemporaryString(String path) {
        return (String) temporarySettings.get(path);
    }

    public int getTemporaryInt(String path) {
        return (int) temporarySettings.get(path);
    }

    public boolean getTemporaryBoolean(String path) {
        return (boolean) temporarySettings.get(path);
    }

    public double getTemporaryDouble(String path) {
        return (double) temporarySettings.get(path);
    }

    public void clearTemporarySettings() {
        temporarySettings.clear();
    }

    public void removeTemporarySetting(String path) {
        temporarySettings.remove(path);
    }

    public int getDefaultMemory() {
        return memory;
    }

    public void setDefaultMemory(int memory) {
        this.memory = memory;
    }

    public boolean minimizeApp() {
        return minimizeApp;
    }

    public void setMinimizeApp(boolean minimizeApp) {
        this.minimizeApp = minimizeApp;
    }

    public boolean useNativeWindow() {
        return nativeWindow;
    }

    public void setUseNativeWindow(boolean nativeWindow) {
        this.nativeWindow = nativeWindow;
    }
}