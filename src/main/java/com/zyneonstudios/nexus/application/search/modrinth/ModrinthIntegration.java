package com.zyneonstudios.nexus.application.search.modrinth;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.zyneonstudios.nexus.application.downloads.Download;
import com.zyneonstudios.nexus.application.main.NexusApplication;
import com.zyneonstudios.nexus.application.search.modrinth.resource.ModrinthProject;
import com.zyneonstudios.nexus.utilities.file.FileActions;
import com.zyneonstudios.nexus.utilities.file.FileGetter;
import com.zyneonstudios.nexus.utilities.json.GsonUtility;
import com.zyneonstudios.nexus.utilities.strings.StringGenerator;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ModrinthIntegration {

    public static void installModpack(File installDir, String projectId, String versionId) {
        JsonObject data = GsonUtility.getObject("https://api.modrinth.com/v2/version/"+versionId);
        ModrinthProject project = new ModrinthProject(projectId);
        installDir = getInstallDir(installDir,project.getSlug());

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

        String modrinthPackPath = installDir.getAbsolutePath();
        if(unzip(download.getAbsolutePath(), modrinthPackPath)) {
            File overrides = new File(modrinthPackPath+"/overrides/");
            if(overrides.exists()&&overrides.isDirectory()) {
                if(overrides.listFiles()!=null) {
                    for (File overrideFile : Objects.requireNonNull(overrides.listFiles())) {
                        if (overrideFile.isDirectory()) {
                            try {
                                File destFile = new File(overrides.getParent() + "/" + overrideFile.getName());
                                if (destFile.exists()) {
                                    FileActions.deleteFolder(destFile);
                                }
                                FileUtils.moveDirectory(overrideFile, destFile);
                            } catch (Exception e) {
                                NexusApplication.getLogger().err(e.getMessage());
                            }
                        }
                    }
                }
                FileActions.deleteFolder(overrides);
            }
            File index = new File(modrinthPackPath+"/modrinth.index.json");

            if(index.exists()) {
                JsonObject indexJson = new Gson().fromJson(GsonUtility.getFromFile(index), JsonObject.class);

                String version = indexJson.get("versionId").getAsString();
                String title = indexJson.get("name").getAsString();


                if(indexJson.has("files")) {
                    JsonArray files = indexJson.getAsJsonArray("files");
                    for(JsonElement file_:files) {
                        JsonObject file = file_.getAsJsonObject();
                        for(JsonElement downloads:file.getAsJsonArray("downloads")) {
                            String url = downloads.getAsString();
                            try {
                                File filePath = new File(installDir.getAbsolutePath()+"/"+file.get("path").getAsString());
                                NexusApplication.getLogger().dbg("Created file path "+filePath+": "+filePath.getParentFile().mkdirs());
                                NexusApplication.getInstance().getDownloadManager().addDownload(new Download(project.getTitle()+" "+file.get("path").getAsString(), new URL(url), filePath.toPath()));
                            } catch (Exception e) {
                                NexusApplication.getLogger().err("Cannot download file \""+file.get("path").getAsString()+"\" for modrinth pack \""+project.getTitle()+"\": "+e.getMessage());
                            }
                        }
                    }
                }

                //TODO check if all downloads are finished or failed (not running) and then convert to zyndex instance with modrinth information, also download mrpack via DownloadManager, also wait before continuing
            } else {
                NexusApplication.getLogger().err("Couldn't find Modrinth index json file: "+index.getAbsolutePath());
            }
        }

        System.gc();
        if(!download.delete()) {
            download.deleteOnExit();
        }
    }

    private static boolean unzip(String fileZip, String destDirPath) {
        File destDir = new File(destDirPath);
        if (!destDir.exists()) {
            NexusApplication.getLogger().deb("Created destination path: "+destDir.mkdirs());
        }
        try {
            byte[] buffer = new byte[1024];
            ZipInputStream zis = new ZipInputStream(new FileInputStream(fileZip));
            ZipEntry zipEntry = zis.getNextEntry();
            while (zipEntry != null) {
                String s = (destDir +"/"+ zipEntry.getName()).replace("\\","/").replace("//","/");
                File newFile = new File(s);
                if (zipEntry.isDirectory()) {
                    if (!newFile.isDirectory() && !newFile.mkdirs()) {
                        throw new IOException("Failed to create directory " + newFile);
                    }
                } else {
                    File parent = newFile.getParentFile();
                    if (!parent.isDirectory() && !parent.mkdirs()) {
                        throw new IOException("Failed to create directory " + parent);
                    }

                    FileOutputStream fos = new FileOutputStream(newFile);
                    int len;
                    while ((len = zis.read(buffer)) > 0) {
                        fos.write(buffer, 0, len);
                    }
                    fos.close();
                }
                zipEntry = zis.getNextEntry();
            }

            zis.closeEntry();
            zis.close();
            return true;
        } catch (Exception e) {
            NexusApplication.getLogger().err(e.getMessage());
        }
        return false;
    }

    private static File getInstallDir(File installDir, String id) {
        File bak = installDir;
        if(!installDir.getName().equalsIgnoreCase(id)) {
            installDir = new File(installDir.getAbsolutePath() + "/" + id.replace("/","-")+"/");
        }
        if(!installDir.exists()) {
            if(!installDir.mkdirs()) {
                throw new NullPointerException("Could not find or create instance directory \""+installDir.getAbsolutePath()+"\"");
            }
        } else {
            return getInstallDir(bak, id+"-"+ StringGenerator.generateAlphanumericString(8));
        }
        return installDir;
    }
}