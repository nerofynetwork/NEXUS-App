package com.zyneonstudios.nexus.application.listeners;

import com.zyneonstudios.nexus.application.events.PageLoadedEvent;
import com.zyneonstudios.nexus.application.main.NexusApplication;

public class PageLoadListener extends PageLoadedEvent {

    public PageLoadListener() {
        super(null);
    }

    @Override
    public boolean onLoad() {
        NexusApplication.getInstance().getApplicationFrame().executeJavaScript("enableDevTools("+NexusApplication.getLogger().isDebugging()+");","app = true;","localStorage.setItem('enabled','true');","version = 'Desktop v"+NexusApplication.getInstance().getVersion()+"';");
        return true;
    }
}
