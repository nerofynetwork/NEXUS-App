package com.zyneonstudios.nexus.application.utilities;

import com.zyneonstudios.nexus.application.main.NexusApplication;
import com.zyneonstudios.nexus.utilities.logger.NexusLogger;

import javax.swing.*;

public class ApplicationLogger extends NexusLogger {

    public ApplicationLogger(String name) {
        super(name);
    }

    @Override
    public void err(String errorMessage) {
        super.err(errorMessage);
        JOptionPane.showMessageDialog(NexusApplication.getInstance().getApplicationFrame(), errorMessage,
                "NEXUS App (Error)", JOptionPane.ERROR_MESSAGE);
    }

    @Override
    public void printErr(String prefix, String type, String message, String reason, StackTraceElement[] cause, String... possibleFixes) {
        super.printErr(prefix, type, message, reason, cause, possibleFixes);
        JOptionPane.showMessageDialog(NexusApplication.getInstance().getApplicationFrame(), reason,
                "NEXUS App (Error)", JOptionPane.ERROR_MESSAGE);
    }
}