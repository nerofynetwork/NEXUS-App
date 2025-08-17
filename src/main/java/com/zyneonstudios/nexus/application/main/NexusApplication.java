package com.zyneonstudios.nexus.application.main;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.starxg.keytar.Keytar;
import com.zyneonstudios.nexus.application.Main;
import com.zyneonstudios.nexus.application.authentication.MicrosoftAuthenticator;
import com.zyneonstudios.nexus.application.frame.AppFrame;
import com.zyneonstudios.nexus.application.listeners.PageLoadListener;
import com.zyneonstudios.nexus.application.modules.ModuleLoader;
import com.zyneonstudios.nexus.desktop.frame.web.NexusWebSetup;
import com.zyneonstudios.nexus.utilities.file.FileActions;
import com.zyneonstudios.nexus.utilities.file.FileExtractor;
import com.zyneonstudios.nexus.utilities.file.FileGetter;
import com.zyneonstudios.nexus.utilities.json.GsonUtility;
import com.zyneonstudios.nexus.utilities.logger.NexusLogger;
import com.zyneonstudios.nexus.utilities.storage.JsonStorage;
import com.zyneonstudios.nexus.utilities.strings.StringGenerator;
import fr.theshark34.openlauncherlib.minecraft.AuthInfos;
import net.nrfy.nexus.launcher.integrations.curseforge.ZCurseForgeIntegration;
import net.nrfy.nexus.launcher.integrations.modrinth.flowarg.ModrinthIntegration;
import net.nrfy.nexus.launcher.launcher.*;
import org.cef.browser.CefBrowser;
import org.cef.browser.CefFrame;
import org.cef.handler.CefLoadHandlerAdapter;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.Base64;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/**
 * The {@code NexusApplication} class is the main object and core component of the Nexus application.
 * It manages the application's lifecycle, including initialization, module loading, UI setup, and shutdown.
 * It also provides access to various application components and resources.
 */
public class NexusApplication {

    //Authentication
    private static AuthInfos authInfos = null;

    // Static Instance and Directories
    private static NexusApplication instance = null;
    private static String workingDir;
    private static String uiPath = null;

    // Application Components
    private final NexusEventHandler eventHandler = new NexusEventHandler();
    private final NexusRunner runner = new NexusRunner();
    private NexusWebSetup webSetup;
    private ModuleLoader moduleLoader;
    private AppFrame applicationFrame = null;

    // Configuration and State
    private final JsonStorage settings;
    private final boolean onlineUI;
    private boolean launched = false;
    private String version = StringGenerator.generateAlphanumericString(12);

    /**
     * Constructor for the NexusApplication.
     *
     * @param path   The working directory path.
     * @param uiPath The path to the UI or "online" for online UI.
     */
    public NexusApplication(String path, String uiPath) {
        instance = this;

        // Setup working directory
        File workingDirFile = new File(path);
        if (workingDirFile.mkdirs()) {
            getLogger().deb("Creating working directory (first run)...");
        }
        NexusApplication.workingDir = workingDirFile.getAbsolutePath().replace("\\", "/");

        // Setup settings storage
        settings = new JsonStorage(workingDirFile.getAbsolutePath() + "/config/settings.json");

        // Determine UI mode (online or local)
        this.onlineUI = uiPath != null && uiPath.equals("online");
        if (!onlineUI) {
            NexusApplication.uiPath = (uiPath != null) ? uiPath : NexusApplication.getWorkingDir().getAbsolutePath() + "/temp/ui";
        }

        CompletableFuture.runAsync(()->{
            getLogger().log("Checking if user is logged in...");
            try {
                if(Keytar.getInstance().getPassword("ZNA||00||00","0")!=null) {
                    MicrosoftAuthenticator.refresh(new String(Base64.getDecoder().decode(Keytar.getInstance().getPassword("ZNA||01||00",Keytar.getInstance().getPassword("ZNA||00||00","0")+"_0"))),true);
                }
            } catch (Exception e) {
                NexusApplication.getLogger().printErr("NEXUS","AUTHENTICATION","Couldn't initialize key management. Account credentials won't be saved. You'll have to login everytime you restart the application.",e.getMessage(), e.getStackTrace(), "If you're on Linux try to install libsecret.");
            }
        });

        loadVersion();
        setupTempDirectory();
        setupWebEnvironment(workingDirFile);
        getLogger().log("Initializing application...");
    }

    /**
     * Loads the application version from the nexus.json file.
     */
    private void loadVersion() {
        try {
            JsonObject data = new Gson().fromJson(GsonUtility.getFromFile(FileGetter.getResourceFile("nexus.json", NexusApplication.class)), JsonObject.class);
            version = data.get("version").getAsString();
        } catch (Exception e) {
            getLogger().deb("Failed to load version from nexus.json: " + e.getMessage());
        }
    }

    /**
     * Sets up the temporary directory for the application.
     */
    private void setupTempDirectory() {
        File temp = new File(NexusApplication.workingDir + "/temp");
        if (temp.exists()) {
            try {
                FileActions.deleteFolder(temp);
            } catch (Exception e) {
                getLogger().err("Couldn't delete old temp folder: " + e.getMessage());
                stop(1);
                return;
            }
        }
        if (temp.mkdirs()) {
            temp.deleteOnExit();
            try {
                FileExtractor.extractResourceFile("html.zip", NexusApplication.workingDir + "/temp/ui.zip", Main.class);
                File uiZip = new File(NexusApplication.workingDir + "/temp/ui.zip");
                FileExtractor.unzipFile(uiZip.getAbsolutePath(), NexusApplication.workingDir + "/temp/ui/");
                if (!uiZip.delete()) {
                    uiZip.deleteOnExit();
                }
            } catch (Exception e) {
                getLogger().err("Error while extracting UI: " + e.getMessage());
                stop(1);
            }
        } else {
            getLogger().err("Couldn't create temp folder.");
            stop(1);
        }
    }

    /**
     * Sets up the web environment for the application.
     *
     * @param workingDirFile The working directory file.
     */
    private void setupWebEnvironment(File workingDirFile) {
        webSetup = new NexusWebSetup(workingDirFile.getAbsolutePath() + "/libs/cef/");
        webSetup.enableCache(true);
        webSetup.enableCookies(true);
        webSetup.setup();
        webSetup.getWebClient().addLoadHandler(new CefLoadHandlerAdapter() {
            @Override
            public void onLoadEnd(CefBrowser browser, CefFrame frame, int httpStatusCode) {
                frame.executeJavaScript("version = \""+version+"\";", browser.getURL() ,0);
            }
        });

        // Setup page load listener
        eventHandler.addPageLoadedEvent(new PageLoadListener());
    }

    /**
     * Loads modules from the modules directory.
     */
    private void loadModules() {
        File modules = new File(workingDir + "/modules");
        if (modules.exists() && modules.isDirectory()) {
            for (File module : Objects.requireNonNull(modules.listFiles())) {
                if (!module.isDirectory()) {
                    try {
                        moduleLoader.loadModule(moduleLoader.readModule(module));
                    } catch (Exception e) {
                        getLogger().err("Couldn't load module " + module.getName() + ": " + e.getMessage());
                    }
                }
            }
        }
    }

    /**
     * Launches the application.
     *
     * @return True if the application was launched successfully, false otherwise.
     */
    public boolean launch() {
        if (!launched) {
            try {
                Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
                String url = onlineUI ? "https://zyneonstudios.github.io/nexus-app/src/main/html?app=true&page=discover.html" : "localhost:" + Main.getPort() + "/index.html?app=true&page=discover.html";
                applicationFrame = new AppFrame(webSetup, url, true);
                applicationFrame.setTitlebar(version, Color.black, Color.white);
                applicationFrame.setSize((int)(screenSize.getWidth()/1.5), (int)(screenSize.getHeight()/1.5));
                applicationFrame.setLocationRelativeTo(null);
                applicationFrame.setVisible(true);
                launched = true;
            } catch (Exception e) {
                getLogger().err("Couldn't launch application: " + e.getMessage());
            }
        }
        runner.start();
        moduleLoader = new ModuleLoader(this);
        loadModules();
        moduleLoader.activateModules();
        return launched;
    }

    /**
     * Stops the application.
     *
     * @param exitCode The exit code to use.
     */
    public static void stop(int exitCode) {
        SwingUtilities.invokeLater(() -> {
            try {
                if (getInstance().getWebSetup() != null && getInstance().getWebSetup().getWebApp() != null) {
                    getInstance().getWebSetup().getWebApp().dispose();
                }
            } catch (Exception ignore) {
            }
            try {
                if (getInstance().getModuleLoader() != null) {
                    getInstance().getModuleLoader().deactivateModules();
                }
            } catch (Exception ignore) {
            }
            System.exit(exitCode);
        });
    }

    // --- Getter Methods ---

    /**
     * Gets the application's logger.
     *
     * @return The NexusLogger instance.
     */
    public static NexusLogger getLogger() {
        return Main.getLogger();
    }

    /**
     * Gets the working directory as a File object.
     *
     * @return The working directory.
     */
    public static File getWorkingDir() {
        return new File(workingDir);
    }

    /**
     * Gets the path to the UI.
     *
     * @return The UI path.
     */
    public static String getUiPath() {
        return uiPath;
    }

    /**
     * Gets the NexusApplication instance.
     *
     * @return The NexusApplication instance.
     */
    public static NexusApplication getInstance() {
        return instance;
    }

    /**
     * Gets the application's runner.
     *
     * @return The NexusRunner instance.
     */
    public NexusRunner getRunner() {
        return runner;
    }

    /**
     * Gets the working path as a String.
     *
     * @return The working path.
     */
    public String getWorkingPath() {
        return workingDir;
    }

    /**
     * Gets the application's main frame.
     *
     * @return The ApplicationFrame instance.
     */
    public AppFrame getApplicationFrame() {
        return applicationFrame;
    }

    /**
     * Gets the web setup.
     *
     * @return The NexusWebSetup instance.
     */
    public NexusWebSetup getWebSetup() {
        return webSetup;
    }

    /**
     * Checks if the application is launched.
     *
     * @return True if launched, false otherwise.
     */
    public boolean isLaunched() {
        return launched;
    }

    /**
     * Checks if the application is using the online UI.
     *
     * @return True if using online UI, false otherwise.
     */
    public boolean isOnlineUI() {
        return onlineUI;
    }

    /**
     * Gets the application's version.
     *
     * @return The version string.
     */
    public String getVersion() {
        return version;
    }

    /**
     * Gets the application's settings storage.
     *
     * @return The JsonStorage instance.
     */
    public JsonStorage getSettings() {
        return settings;
    }

    /**
     * Gets the module loader.
     *
     * @return The ModuleLoader instance.
     */
    public ModuleLoader getModuleLoader() {
        return moduleLoader;
    }

    /**
     * Gets the event handler.
     *
     * @return The NexusEventHandler instance.
     */
    public NexusEventHandler getEventHandler() {
        return eventHandler;
    }

    /**
     * Initializes and returns a Minecraft launcher with application auth infos
     *
     * @return A pre-initialized Vanilla launcher
     */
    public static VanillaLauncher getVanillaLauncher() {
        return new VanillaLauncher(authInfos);
    }

    /**
     * Initializes and returns a Minecraft launcher with application auth infos
     *
     * @return A pre-initialized Fabric launcher
     */
    public static FabricLauncher getFabricLauncher() {
        return new FabricLauncher(authInfos);
    }

    /**
     * Initializes and returns a Minecraft launcher with application auth infos
     *
     * @return A pre-initialized Forge launcher
     */
    public static ForgeLauncher getForgeLauncher() {
        return new ForgeLauncher(authInfos);
    }

    /**
     * Initializes and returns a Minecraft launcher with application auth infos
     *
     * @return A pre-initialized NeoForge launcher
     */
    public static NeoForgeLauncher getNeoForgeLauncher() {
        return new NeoForgeLauncher(authInfos);
    }

    /**
     * Initializes and returns a Minecraft launcher with application auth infos
     *
     * @return A pre-initialized Quilt launcher
     */
    public static QuiltLauncher getQuiltLauncher() {
        return new QuiltLauncher(authInfos);
    }

    /**
     * Sets the authentication information for the used Microsoft account
     *
     * @param authInfos Map of authentication information
     */
    public static void setAuthInfos(AuthInfos authInfos) {
        NexusApplication.authInfos = authInfos;
    }
}