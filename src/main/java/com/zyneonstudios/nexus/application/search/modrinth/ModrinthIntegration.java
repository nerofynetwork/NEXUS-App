package com.zyneonstudios.nexus.application.search.modrinth;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.zyneonstudios.nexus.application.main.NexusApplication;
import com.zyneonstudios.nexus.utilities.file.FileExtractor;
import com.zyneonstudios.nexus.utilities.file.FileGetter;
import com.zyneonstudios.nexus.utilities.json.GsonUtility;

import java.io.File;

public class ModrinthIntegration {

    public static void installModpack(File installDir, String projectId, String versionId) {
        JsonObject data = GsonUtility.getObject("https://api.modrinth.com/v2/version/"+versionId);
        String fileName = "modrinth-"+projectId+"-"+versionId+".mrpack";
        String downloadName = (NexusApplication.getInstance().getWorkingPath()+"/temp/"+fileName).replace("\\","/").replace("//","/");
        File download = new File(downloadName);
        if(download.exists()) {
            if(!download.delete()) {
                throw new RuntimeException("Failed to delete old download");
            }
        }

        if(data.has("files")) {
            JsonArray files = data.getAsJsonArray("files");
            if(files.size()>0) {
                for(JsonElement file : files) {
                    JsonObject f = file.getAsJsonObject();
                    if(f.has("primary") && f.get("primary").getAsBoolean()) {
                        FileGetter.downloadFile(f.get("url").getAsString(), downloadName);
                        break;
                    }
                }
            }
        }
        if(!download.exists()) {
            throw new NullPointerException("Downloaded file "+downloadName+" not found!");
        }

        if(!installDir.exists()) {
            installDir.mkdirs();
        }
        if(!installDir.isDirectory()) {
            throw new RuntimeException("The install path cannot be a file! It must be a directory.");
        }
        FileExtractor.unzipFile(download.getAbsolutePath(), download.getAbsolutePath().replace(".mrpack","/"));

        System.gc();
        if(!download.delete()) {
            download.deleteOnExit();
        }
    }
}