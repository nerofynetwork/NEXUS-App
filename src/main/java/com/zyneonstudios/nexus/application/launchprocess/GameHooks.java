package com.zyneonstudios.nexus.application.launchprocess;

import com.zyneonstudios.nexus.application.main.NexusApplication;
import net.nrfy.nexus.launcher.launcher.LauncherHook;
import net.nrfy.nexus.launcher.launcher.MinecraftLauncher;

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
                NexusApplication.getInstance().getApplicationFrame().setState(Frame.ICONIFIED);
            }
        };
    }

    public static LauncherHook getGameCloseHook(MinecraftLauncher launcher) {
        return new LauncherHook(launcher) {
            @Override
            public void run() {
                NexusApplication.getInstance().getApplicationFrame().setState(Frame.NORMAL);
            }
        };
    }
}