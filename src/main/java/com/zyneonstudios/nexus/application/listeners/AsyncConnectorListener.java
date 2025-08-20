package com.zyneonstudios.nexus.application.listeners;

import com.starxg.keytar.Keytar;
import com.zyneonstudios.nexus.application.utilities.DiscordRichPresence;
import com.zyneonstudios.nexus.application.utilities.MicrosoftAuthenticator;
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
import java.util.Base64;

public class AsyncConnectorListener extends AsyncWebFrameConnectorEvent {

    private final AppFrame frame;

    public AsyncConnectorListener(WebFrame frame, String message) {
        super(frame, message);
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
        } else if (s.equals("restart")) {
            NexusApplication.restart();
        } else if(s.equals("logout")) {
            MicrosoftAuthenticator.logout();
        } else if(s.equals("login")) {
            MicrosoftAuthenticator.startLogin(true);
        } else if(s.startsWith("logout.")) {
            String uuid = s.replace("logout.","");
            MicrosoftAuthenticator.logout(uuid);
        } else if(s.startsWith("login.")) {
            if(s.replace("login.","").equals("new")) {
                resolveMessage("login");
                return;
            }
            try {
                MicrosoftAuthenticator.refresh(new String(Base64.getDecoder().decode(Keytar.getInstance().getPassword("ZNA||01||00", Base64.getEncoder().encodeToString(s.replace("login.", "").getBytes())+"_0"))), true);
            } catch (Exception e) {
                NexusApplication.getLogger().printErr("NEXUS","AUTHENTICATION","Couldn't refresh the Microsoft token.",e.getMessage(), e.getStackTrace());
            }
        } else if(s.equals("initAccountSettings")) {
            if(MicrosoftAuthenticator.isLoggedIn()) {
                frame.executeJavaScript("document.querySelector('.account-activeSkin').src = 'https://cravatar.eu/helmhead/"+MicrosoftAuthenticator.getUUID()+"/128.png'; document.querySelector('.account-activeName').innerText = '"+MicrosoftAuthenticator.getUsername()+"'; document.querySelector('.account-activeUUID').innerText = '"+MicrosoftAuthenticator.getUUID()+"'; document.querySelector('.account-activeProfileCard').style.display = 'flex';");
                frame.executeJavaScript("document.querySelector('.account-activeRow').style.display = ''; document.querySelector('.account-activeRow').id = '"+MicrosoftAuthenticator.getUUID()+"'; document.querySelector('.account-activeRowName').innerText = '"+MicrosoftAuthenticator.getUsername()+"';");
                for(String uuid : MicrosoftAuthenticator.getDecryptedAuthenticatedUUIDs()) {
                    frame.executeJavaScript("addAccountToAccountList('"+MicrosoftAuthenticator.getDecryptedAuthenticatedUsername(uuid)+"','"+uuid+"');");
                }
            }
        } else if(s.equals("initAppearanceValues")) {
            frame.executeJavaScript("document.querySelector('.appearance-nativeWindow').checked = "+NexusApplication.getInstance().getLocalSettings().useNativeWindow()+";","document.querySelector('.appearance-hideApp').checked = "+NexusApplication.getInstance().getLocalSettings().minimizeApp()+";");
        } else if(s.equals("initDiscordRPC")) {
            boolean rpc = true;
            if(NexusApplication.getInstance().getSettings().has("settings.discord.rpc")) {
                try {
                    rpc = NexusApplication.getInstance().getSettings().getBool("settings.discord.rpc");
                } catch (Exception ignore) {}
            }
            frame.executeJavaScript("document.querySelector('.privacy-enableDiscordRPC').checked = "+rpc+";");
        } else if(s.startsWith("discordrpc.")) {
            if (s.replace("discordrpc.", "").equals("true")) {
                DiscordRichPresence.startRPC();
                NexusApplication.getInstance().getSettings().set("settings.discord.rpc", true);
            } else {
                DiscordRichPresence.stopRPC();
                NexusApplication.getInstance().getSettings().set("settings.discord.rpc", false);
            }
        } else if(s.startsWith("hideApp.")) {
            boolean bool = s.replace("hideApp.", "").equals("true");
            NexusApplication.getInstance().getSettings().set("settings.window.minimizeOnStart", bool);
            NexusApplication.getInstance().getLocalSettings().setMinimizeApp(bool);
        } else if(s.startsWith("nativeWindow.")) {
            boolean bool = s.replace("nativeWindow.", "").equals("true");
            NexusApplication.getInstance().getSettings().set("settings.window.nativeDecorations", bool);
            NexusApplication.getInstance().getLocalSettings().setUseNativeWindow(bool);
            NexusApplication.restart();
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

