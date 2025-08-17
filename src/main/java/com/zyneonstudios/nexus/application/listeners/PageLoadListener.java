package com.zyneonstudios.nexus.application.listeners;

import com.zyneonstudios.nexus.application.authentication.MicrosoftAuthenticator;
import com.zyneonstudios.nexus.application.events.PageLoadedEvent;
import com.zyneonstudios.nexus.application.main.NexusApplication;

public class PageLoadListener extends PageLoadedEvent {

    public PageLoadListener() {
        super(null);
    }

    @Override
    public boolean onLoad() {
        NexusApplication.getInstance().getApplicationFrame().executeJavaScript("enableDevTools("+NexusApplication.getLogger().isDebugging()+");","app = true;","localStorage.setItem('enabled','true');","version = 'Desktop v"+NexusApplication.getInstance().getVersion()+"';");
        if(getUrl().toLowerCase().contains("page=library")) {
            if(MicrosoftAuthenticator.isLoggedIn()) {
                NexusApplication.getInstance().getApplicationFrame().executeJavaScript("document.querySelector('.menu-panel').querySelector('.card-body').innerHTML = \"<button type='button' class='btn btn-light shadow' onclick=\\\"console.log('[CONNECTOR] logout');\\\"> Logout "+MicrosoftAuthenticator.getUsername()+"</button>\";");
            } else {
                NexusApplication.getInstance().getApplicationFrame().executeJavaScript("loadPage('login.html');");
            }
        }
        if(getUrl().toLowerCase().contains("page=login")) {
            if(MicrosoftAuthenticator.isLoggedIn()) {
                NexusApplication.getInstance().getApplicationFrame().executeJavaScript("loadPage('library.html');");
            }
        }

        return true;
    }
}
