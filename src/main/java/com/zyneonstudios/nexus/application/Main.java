package com.zyneonstudios.nexus.application;

import com.zyneonstudios.nexus.application.frame.ZyneonSplash;
import com.zyneonstudios.nexus.application.main.NexusApplication;
import com.zyneonstudios.nexus.desktop.NexusDesktop;
import com.zyneonstudios.nexus.desktop.frame.web.NWebFrame;
import com.zyneonstudios.nexus.utilities.NexusUtilities;
import com.zyneonstudios.nexus.utilities.logger.NexusLogger;
import net.lenni0451.commons.httpclient.HttpClient;
import net.raphimc.minecraftauth.MinecraftAuth;
import net.raphimc.minecraftauth.step.java.session.StepFullJavaSession;
import net.raphimc.minecraftauth.step.msa.StepMsaDeviceCode;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

/**
 * The {@code Main} class is the primary entry point for the Nexus application.
 * It initializes the application, parses command-line arguments, starts the web server (if necessary),
 * and launches the main application frame. It also handles the application's lifecycle and provides
 * access to the application's logger and port.
 */
@SpringBootApplication
public class Main {

    // Application Logger
    private static final NexusLogger logger = new NexusLogger("NEXUS");

    // Application Configuration
    private static String path = "";
    private static String ui = null;
    private static int port = 8094;

    public static String token = "";

    /**
     * The main method, the entry point of the Nexus application.
     *
     * @param args Command-line arguments passed to the application.
     */
    public static void main(String[] args) {
        // Initialize the Nexus desktop environment.
        NexusDesktop.init();
        NexusUtilities.getLogger().setName("NEXUS",true);

        // Resolve command-line arguments.
        resolveArguments(args);

        // Display the splash screen.
        ZyneonSplash splash = new ZyneonSplash();
        splash.setVisible(true);

        // Create the main application instance.
        NexusApplication application = new NexusApplication(path, ui);

        // Start the web server if the application is not using the online UI.
        if (!application.isOnlineUI()) {
            try {
                startWebServer(args);
            } catch (Exception e) {
                System.exit(-1);
            }
        }

        // Launch the application and dispose of the splash screen if successful.
        if (application.launch()) {
            splash.dispose();
            System.gc();
        } else {
            // Stop the application if launching fails.
            NexusApplication.stop(1);
        }

        try {
            HttpClient httpClient = MinecraftAuth.createHttpClient();
            StepFullJavaSession.FullJavaSession javaSession = MinecraftAuth.JAVA_DEVICE_CODE_LOGIN.getFromInput(httpClient, new StepMsaDeviceCode.MsaDeviceCodeCallback(msaDeviceCode -> {
                // Method to generate a verification URL and a code for the user to enter on that page
                System.out.println("Go to " + msaDeviceCode.getVerificationUri());
                System.out.println("Enter code " + msaDeviceCode.getUserCode());

                // There is also a method to generate a direct URL without needing the user to enter a code
                System.out.println("Go to " + msaDeviceCode.getDirectVerificationUri());

                new NWebFrame(application.getWebSetup().getWebClient(),msaDeviceCode.getDirectVerificationUri(),true).setVisible(true);
            }));
            System.out.println("Username: " + javaSession.getMcProfile().getName());
            System.out.println("Access token: " + javaSession.getMcProfile().getMcToken().getAccessToken());
            System.out.println("Player certificates: " + javaSession.getPlayerCertificates());

            token = javaSession.getMcProfile().getMcToken().getAccessToken();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Starts the embedded web server for the application.
     *
     * @param args Command-line arguments passed to the application.
     */
    private static void startWebServer(String[] args) {
        if(port > 65535) {
            throw new RuntimeException("Port range exceeded, cannot launch application web server. Try to restart your computer or stopping port using applications and try again.");
        }
        try {
            // Configure and start the Spring Boot web server.
            new SpringApplicationBuilder(Main.class)
                    .properties("logging.level.root=WARN", "logging.pattern.console=", "server.port=" + port)
                    .run(args);
        } catch (Exception e) {
            // Increment the port and retry if the initial port is in use.
            port++;
            startWebServer(args);
        }
    }

    /**
     * Resolves and processes command-line arguments.
     *
     * @param args Command-line arguments passed to the application.
     */
    private static void resolveArguments(String[] args) {
        for (int i = 0; i < args.length; i++) {
            try {
                String arg = args[i].toLowerCase();
                switch (arg) {
                    case "-h", "--help" -> {
                        // Display help message and exit.
                        logger.log("NEXUS App help:");
                        logger.log("  -d, --debug: Enables debug console output.");
                        logger.log("  -h, --help: This help message.");
                        logger.log("  -o, --online: Enables the connection to the online UI. Caution: This may cause problems with some modules.");
                        logger.log("  -p <path>, --path <path>: Lets you select the run folder.");
                        logger.log("  -u <path>, --ui <path>: Lets you select the folder where the user interface should be unpacked.");
                        System.exit(0);
                    }
                    case "-u", "--ui" -> ui = args[i + 1];
                    case "-p", "--path" -> path = args[i + 1];
                    case "-o", "--online" -> ui = "online";
                    case "-d", "--debug" -> logger.enableDebug();
                }
            } catch (Exception e) {
                // Handle argument parsing errors.
                logger.err(e.getMessage());
                logger.err("Use -h or --help at startup to view the startup arguments and their syntax.");
                System.exit(1);
            }
        }
    }

    /**
     * Gets the application's logger.
     *
     * @return The NexusLogger instance.
     */
    public static NexusLogger getLogger() {
        return logger;
    }

    /**
     * Gets the port used by the web server.
     *
     * @return The web server port.
     */
    public static int getPort() {
        return port;
    }
}