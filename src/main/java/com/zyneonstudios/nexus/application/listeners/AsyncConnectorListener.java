package com.zyneonstudios.nexus.application.listeners;

import com.zyneonstudios.nexus.application.authentication.MicrosoftAuthenticator;
import com.zyneonstudios.nexus.application.events.PageLoadedEvent;
import com.zyneonstudios.nexus.application.frame.AppFrame;
import com.zyneonstudios.nexus.application.launchprocess.GameHooks;
import com.zyneonstudios.nexus.application.main.NexusApplication;
import com.zyneonstudios.nexus.desktop.events.AsyncWebFrameConnectorEvent;
import com.zyneonstudios.nexus.desktop.frame.web.WebFrame;
import com.zyneonstudios.verget.fabric.FabricVerget;
import net.nrfy.nexus.launcher.launcher.FabricLauncher;
import java.awt.*;
import java.nio.file.Path;

public class AsyncConnectorListener extends AsyncWebFrameConnectorEvent {

    private final NexusApplication application;
    private final AppFrame frame;

    public AsyncConnectorListener(WebFrame frame, String message) {
        super(frame, message);

        application = NexusApplication.getInstance();
        this.frame = (AppFrame)frame;
    }

    @Override
    protected void resolveMessage(String s) {
        // Handle theme change events.
        if (s.startsWith("event.theme.changed.")) {
            if (s.endsWith("dark")) {
                frame.setTitleBackground(Color.black);
                frame.setTitleForeground(Color.white);
            } else {
                frame.setTitleBackground(Color.white);
                frame.setTitleForeground(Color.black);
            }
            // Handle page loaded events.
        } else if (s.startsWith("event.page.loaded")) {
            for (PageLoadedEvent event : NexusApplication.getInstance().getEventHandler().getPageLoadedEvents()) {
                event.setUrl(frame.getBrowser().getURL());
                event.execute();
            }
        } else if (s.equals("exit")) {
            NexusApplication.stop(0);
        } else if(s.equals("logout")) {
            MicrosoftAuthenticator.logout();
        } else if(s.equals("login")) {
            MicrosoftAuthenticator.startLogin(true);
        } else if(s.equals("run.test")) {
            FabricLauncher launcher = NexusApplication.getFabricLauncher();
            launcher.setPreLaunchHook(GameHooks.getPreLaunchHook(launcher));
            launcher.setPostLaunchHook(GameHooks.getPostLaunchHook(launcher));
            launcher.setGameCloseHook(GameHooks.getGameCloseHook(launcher));
            String version = FabricVerget.getSupportedMinecraftVersions(false).getFirst();
            launcher.launch(version, FabricVerget.getVersions(true).getFirst(),4096, Path.of("target/run/game/"+version+"/"),"test");
        }
    }
}

