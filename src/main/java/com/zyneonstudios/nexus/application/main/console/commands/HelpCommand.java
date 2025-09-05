package com.zyneonstudios.nexus.application.main.console.commands;

import com.zyneonstudios.nexus.application.main.NexusApplication;
import com.zyneonstudios.nexus.application.main.console.NexusConsoleCommand;
import com.zyneonstudios.nexus.utilities.logger.NexusLogger;

public class HelpCommand extends NexusConsoleCommand {

    public HelpCommand() {
        super("help");
        addAliases("?","nexus","nexushelp","nexus?");
    }

    @Override
    public boolean run(String[] args) {
        NexusLogger logger = NexusApplication.getLogger();
        String separator = "============================================";

        logger.log(" ");
        logger.log("=(NEXUS APP)"+separator);
        logger.deb("NEXUS APP DEBUG MODE ENABLED");
        logger.log("NexusApplication version: "+NexusApplication.getInstance().getVersion());
        logger.log("For more help: https://nexus.zyneon.de/app/");
        logger.log(separator+"(NEXUS APP)=");
        logger.log(" ");

        System.gc();
        return true;
    }
}