package com.zyneonstudios.nexus.application.main.console.commands;

import com.zyneonstudios.nexus.application.main.NexusApplication;
import com.zyneonstudios.nexus.application.main.console.NexusConsoleCommand;

import java.util.Arrays;

public class InstallCommand extends NexusConsoleCommand {

    public InstallCommand() {
        super("install");
        addAliases("download","search","get");
    }

    @Override
    public boolean run(String[] args) {
        if(args.length>0) {
            String query = Arrays.toString(args).replace("[", "").replace("]", "").replace(", ", " ");
            NexusApplication.getInstance().getApplicationFrame().executeJavaScript("loadPage('discover.html',false,\"&dt=search&q=" + query + "\");");
            return true;
        }
        return false;
    }
}
