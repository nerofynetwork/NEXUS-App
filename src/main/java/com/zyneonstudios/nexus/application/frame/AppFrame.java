package com.zyneonstudios.nexus.application.frame;

import com.zyneonstudios.nexus.application.listeners.AsyncConnectorListener;
import com.zyneonstudios.nexus.application.main.NexusApplication;
import com.zyneonstudios.nexus.desktop.frame.nexus.NexusWebFrame;
import com.zyneonstudios.nexus.desktop.frame.web.NWebFrame;
import com.zyneonstudios.nexus.desktop.frame.web.NexusWebSetup;
import com.zyneonstudios.nexus.desktop.frame.web.WebFrame;
import com.zyneonstudios.nexus.utilities.strings.StringGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import java.awt.*;
import java.awt.event.*;
import java.net.URI;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * The {@code ApplicationFrame} class represents the main application window for the Nexus application.
 * It extends {@link NWebFrame} and implements {@link ComponentListener} and {@link WebFrame} to provide
 * a web browser-based user interface. This frame handles window events, component events, and
 * communication with the web content.
 */
@SuppressWarnings("unused")
public class AppFrame extends NexusWebFrame implements ComponentListener, WebFrame {

    private static final Logger log = LoggerFactory.getLogger(AppFrame.class);
    // The minimum size of the application window.
    private final Dimension minSize = new Dimension(1024, 640);
    private final String windowId = StringGenerator.generateAlphanumericString(12);
    private SmartBar smartBar;

    /**
     * Constructor for the ApplicationFrame.
     *
     * @param setup     The NexusWebSetup instance for configuring the web client.
     * @param url       The initial URL to load in the web browser.
     * @param decorated Whether the window should have a title bar and borders.
     */
    public AppFrame(NexusWebSetup setup, String url, boolean decorated) {
        super(setup.getWebClient(), url, decorated, NexusApplication.getInstance().getLocalSettings().useNativeWindow());
        try {
            // Set the application icon.
            setIconImage(ImageIO.read(Objects.requireNonNull(getClass().getResource("/icon.png"))).getScaledInstance(32, 32, Image.SCALE_SMOOTH));
        } catch (Exception ignored) {
            // Ignore any exceptions that occur during icon loading.
        }

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                NexusApplication.stop(0);
            }
        });

        JMenuBar devBar = new JMenuBar();
        JPanel spacer = new JPanel();
        spacer.setBackground(null);
        devBar.add(spacer);

        JMenu browser = new JMenu("Browser");
        browser.getPopupMenu().setBackground(Color.black);
        JMenu actions = new JMenu("Actions");
        actions.getPopupMenu().setBackground(Color.black);

        JMenuItem refresh = new JMenuItem("Open start page");
        refresh.addActionListener(e -> getBrowser().loadURL(url));
        actions.add(refresh);

        JMenuItem goForward = new JMenuItem("Go forward");
        goForward.addActionListener(e -> getBrowser().goForward());
        browser.add(goForward);

        JMenuItem goBack = new JMenuItem("Go back");
        goBack.addActionListener(e -> getBrowser().goBack());
        browser.add(goBack);

        browser.addMenuListener(new MenuListener() {
            @Override
            public void menuSelected(MenuEvent e) {
                goBack.setEnabled(getBrowser().canGoBack());
                goForward.setEnabled(getBrowser().canGoForward());
            }

            @Override
            public void menuDeselected(MenuEvent e) {

            }

            @Override
            public void menuCanceled(MenuEvent e) {

            }
        });

        if(NexusApplication.getLogger().isDebugging()) {
            JMenuItem inputUrl = new JMenuItem("Input URL");
            inputUrl.addActionListener(e -> {
                JDialog inputWindow = new JDialog(AppFrame.this, "Input url ("+windowId+", "+getTitle()+")", true);
                inputWindow.setLocationRelativeTo(this);
                inputWindow.setResizable(false);
                inputWindow.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
                inputWindow.setSize(500,75);
                JTextField urlField = new JTextField(getBrowser().getURL());
                urlField.addActionListener(e1 -> {
                    getBrowser().loadURL(urlField.getText());
                    inputWindow.dispose();
                });
                inputWindow.add(urlField);
                inputWindow.setVisible(true);
            });
            browser.add(inputUrl);

            JMenuItem openInBrowser = new JMenuItem("Open in default browser");
            openInBrowser.addActionListener(e -> {
                try {
                    if (Desktop.isDesktopSupported()) {
                        Desktop.getDesktop().browse(new URI(getBrowser().getURL()));
                    }
                } catch (Exception ignored) {}
            });
            actions.add(openInBrowser);

            JMenuItem devTools = new JMenuItem("Show DevTools");
            devTools.addActionListener(e -> getBrowser().openDevTools());
            actions.add(devTools);

            AtomicInteger clones = new AtomicInteger(1);
            JMenuItem cloneWindow = new JMenuItem("Clone window");
            cloneWindow.addActionListener(e -> {
                AppFrame clone = new AppFrame(setup, getBrowser().getURL(), decorated);
                clone.setTitleColors(Color.decode("#333399"),Color.decode("#ffffff"));
                clone.setVisible(true);

                clone.setTitlebar(windowId+"-clone "+clones, Color.decode("#333399"), Color.white);
                clone.setSize(getSize());
                clone.setLocationRelativeTo(null);
                clone.setVisible(true);
                clone.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
                clones.getAndIncrement();
            });
            actions.add(cloneWindow);

            JMenuItem disableDevtools = new JMenuItem("Disable dev mode");
            disableDevtools.addActionListener(e -> {
                NexusApplication.getLogger().disableDebug();
                setVisible(false);
                browser.remove(inputUrl);
                actions.remove(disableDevtools);
                actions.remove(cloneWindow);
                actions.remove(devTools);
                actions.remove(openInBrowser);
                setVisible(true);
                executeJavaScript("enableDevTools(false);");
            });
            actions.add(disableDevtools);
        }
        JMenuItem reload = new JMenuItem("Reload");
        reload.addActionListener(e -> getBrowser().reload());
        browser.add(reload);

        JMenuItem exit = new JMenuItem("Exit");
        exit.addActionListener(e -> NexusApplication.stop(0));
        actions.add(exit);

        devBar.add(browser);
        devBar.add(actions);
        devBar.setBorder(null);
        smartBar = new SmartBar();
        smartBar.setMargin(0,3,0,6);
        smartBar.setMaximumSize(new Dimension(250,getSize().height));

        devBar.add(smartBar);
        devBar.setBackground(Color.black);
        devBar.setOpaque(true);

        if(NexusApplication.getInstance().getLocalSettings().useNativeWindow()) {
            add(devBar, BorderLayout.NORTH);
        } else {
            getTitlebar().add(devBar, BorderLayout.SOUTH);
        }

        getBrowser().getUIComponent().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                getBrowser().getUIComponent().requestFocusInWindow();
            }
        });

        // Add a component listener to handle window resize and move events.
        addComponentListener(this);

        // Add a window listener to handle the window closing event.
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                // Stop the application when the window is closed.
                if(!getTitle().contains("-clone ")) {
                    NexusApplication.stop(0);
                }
            }

            @Override
            public void windowClosed(WindowEvent e) {
                // Stop the application when the window is closed.
                if(!getTitle().contains("-clone ")) {
                    NexusApplication.stop(0);
                }
            }
        });

        setMinimumSize(minSize);

        // Set the AsyncWebFrameConnectorEvent for the frame.
        setAsyncWebFrameConnectorEvent(new AsyncConnectorListener(this,null));
    }

    /**
     * Gets the minimum size of the application window.
     *
     * @return The minimum size of the window.
     */
    public Dimension getMinSize() {
        return minSize;
    }

    /**
     * Sets the title bar text and colors.
     *
     * @param title      The title text.
     * @param background The background color of the title bar.
     * @param foreground The foreground color of the title bar text.
     */
    public void setTitlebar(String title, Color background, Color foreground) {
        setTitle("NEXUS App (" + title + ")");
        setTitleBackground(background);
        setTitleForeground(foreground);
    }

    /**
     * Sets the title bar colors.
     *
     * @param background The background color of the title bar.
     * @param foreground The foreground color of the title bar text.
     */
    //@Override
    public void setTitleColors(Color background, Color foreground) {
        setTitleBackground(background);
        setTitleForeground(foreground);
    }

    /**
     * Sets the background color of the title bar.
     *
     * @param color The background color.
     */
    public void setTitleBackground(Color color) {
        setBackground(color);
        getRootPane().putClientProperty("JRootPane.titleBarBackground", color);
        smartBar.setSpaceColor(color);
    }

    /**
     * Sets the foreground color of the title bar text.
     *
     * @param color The foreground color.
     */
    public void setTitleForeground(Color color) {
        getRootPane().putClientProperty("JRootPane.titleBarForeground", color);
    }

    /**
     * Executes JavaScript code in the web browser.
     *
     * @param script The JavaScript code to execute.
     */
    public void executeJavaScript(String script) {
        super.executeJavaScript(script);
    }

    /**
     * Gets the frame as a JFrame.
     *
     * @return The frame as a JFrame.
     */
    @Override
    public JFrame getAsJFrame() {
        return this;
    }

    /**
     * Called when the component's size changes.
     *
     * @param e The ComponentEvent.
     */
    @Override
    public void componentResized(ComponentEvent e) {
        // Not implemented.
    }

    /**
     * Called when the component's position changes.
     *
     * @param e The ComponentEvent.
     */
    @Override
    public void componentMoved(ComponentEvent e) {
        // Not implemented.
    }

    /**
     * Called when the component becomes visible.
     *
     * @param e The ComponentEvent.
     */
    @Override
    public void componentShown(ComponentEvent e) {
        // Not implemented.
    }

    /**
     * Called when the component becomes hidden.
     *
     * @param e The ComponentEvent.
     */
    @Override
    public void componentHidden(ComponentEvent e) {
        // Not implemented.
    }

    public SmartBar getSmartBar() {
        return smartBar;
    }

    public void setSmartBar(SmartBar smartBar) {
        this.smartBar = smartBar;
    }
}