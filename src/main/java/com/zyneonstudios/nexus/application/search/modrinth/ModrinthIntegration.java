package com.zyneonstudios.nexus.application.search.modrinth;

import com.zyneonstudios.nexus.application.main.NexusApplication;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

public class ModrinthIntegration {

    public static void install(File installDir, String projectId, String versionId) {

        Path path = Paths.get(NexusApplication.getInstance().getWorkingDir() +"/temp/"+ UUID.randomUUID() +".zip");

    }

}