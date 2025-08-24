package com.zyneonstudios.nexus.application.launchprocess;

import com.zyneonstudios.nexus.application.main.NexusApplication;
import live.nerotv.aminecraftlauncher.launcher.LauncherHook;
import live.nerotv.aminecraftlauncher.launcher.MinecraftLauncher;

import java.awt.*;

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
                if(NexusApplication.getInstance().getLocalSettings().minimizeApp()) {
                    NexusApplication.getInstance().getApplicationFrame().setState(Frame.ICONIFIED);
                }
            }
        };
    }

    public static LauncherHook getGameCloseHook(MinecraftLauncher launcher) {
        return new LauncherHook(launcher) {
            @Override
            public void run() {
                if(NexusApplication.getInstance().getLocalSettings().minimizeApp()) {
                    NexusApplication.getInstance().getApplicationFrame().setState(Frame.NORMAL);
                }
            }
        };
    }
}