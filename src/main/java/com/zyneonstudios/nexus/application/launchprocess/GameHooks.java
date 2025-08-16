package com.zyneonstudios.nexus.application.launchprocess;

import net.nrfy.nexus.launcher.launcher.LauncherHook;
import net.nrfy.nexus.launcher.launcher.MinecraftLauncher;

public class GameHooks {

    public static LauncherHook getPreLaunchHook(MinecraftLauncher launcher) {
        return new LauncherHook(launcher) {
            @Override
            public void run() {

            }
        };
    }

    public static LauncherHook getPostLaunchHook(MinecraftLauncher launcher) {
        return new LauncherHook(launcher) {
            @Override
            public void run() {

            }
        };
    }

    public static LauncherHook getGameCloseHook(MinecraftLauncher launcher) {
        return new LauncherHook(launcher) {
            @Override
            public void run() {

            }
        };
    }
}