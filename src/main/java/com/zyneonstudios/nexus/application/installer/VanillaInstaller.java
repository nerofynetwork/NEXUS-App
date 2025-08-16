package com.zyneonstudios.nexus.application.installer;

import com.zyneonstudios.nexus.application.Main;
import fr.flowarg.flowupdater.FlowUpdater;
import fr.flowarg.flowupdater.versions.VanillaVersion;

import java.nio.file.Path;

public class VanillaInstaller {

    public boolean download(String version, Path instancePath) {
        Main.getLogger().deb("[INSTALLER] Starting download of Minecraft "+version);
        VanillaVersion vanilla = new VanillaVersion.VanillaVersionBuilder()
                .withName(version)
                .build();

        FlowUpdater flowUpdater = new FlowUpdater.FlowUpdaterBuilder()
                .withVanillaVersion(vanilla)
                .build();

        try {
            flowUpdater.update(instancePath);
            return true;
        } catch (Exception e) {
            Main.getLogger().err("[INSTALLER] Couldn't download Minecraft "+version+": "+e.getMessage());
            return false;
        }
    }
}