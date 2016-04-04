package ua.com.nmonokov;

import org.apache.commons.collections4.queue.CircularFifoQueue;

import java.awt.AWTException;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.FlavorEvent;
import java.awt.datatransfer.FlavorListener;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.net.URL;
import java.util.Queue;

public class Main implements FlavorListener {

    private static final Clipboard CLIPBOARD = Toolkit.getDefaultToolkit().getSystemClipboard();
    private static final int ITEMS_LIMIT = 5;
    private static final Queue<String> ITEMS = new CircularFifoQueue<>(ITEMS_LIMIT);

    public Main() {
        initUI();
    }

    public static void main(String[] args) throws InterruptedException, IOException, UnsupportedFlavorException {
        new Main();
    }

    private void initUI() {
        if (!SystemTray.isSupported()) {
            System.out.println("Tray is not supported");
            return;
        }
        // Empty contents to invoke FlavorListener for sure.
        CLIPBOARD.setContents(CLIPBOARD.getContents(null), null);
        CLIPBOARD.addFlavorListener(this);
        createTray();
    }

    // Creates system tray on the startup.
    private void createTray() {
        final PopupMenu popup = new PopupMenu();
        URL resource = ClassLoader.getSystemResource("images/icon.png");
        Image image = Toolkit.getDefaultToolkit().getImage(resource);
        final TrayIcon icon = new TrayIcon(image, "Buffer plus");
        final SystemTray systemTray = SystemTray.getSystemTray();
        MenuItem exit = new MenuItem("Exit");
        exit.addActionListener(e -> System.exit(0));

        popup.add(new MenuItem("- empty -"));
        popup.addSeparator();
        popup.add(exit);

        icon.setImageAutoSize(true);
        icon.setPopupMenu(popup);

        try {
            systemTray.add(icon);
        } catch (AWTException e) {
            System.out.println("TrayIcon could not be added.");
        }
    }

    // Recreates tray using elements from ITEMS queue.
    private void recreateTray() {
        SystemTray systemTray = SystemTray.getSystemTray();
        TrayIcon icon = systemTray.getTrayIcons()[0];
        final PopupMenu popup = new PopupMenu();
        for (String item : ITEMS) {
            MenuItem menuItem = new MenuItem(getDisplayedName(item));
            menuItem.addActionListener(e -> {
                StringSelection selection = new StringSelection(item);
                CLIPBOARD.removeFlavorListener(this);
                CLIPBOARD.setContents(selection, null);
                CLIPBOARD.addFlavorListener(this);
            });
            popup.add(menuItem);
        }
        popup.addSeparator();
        MenuItem exit = new MenuItem("Exit");
        popup.add(exit);
        exit.addActionListener(event -> System.exit(0));
        icon.setImageAutoSize(true);
        icon.setPopupMenu(popup);
    }

    @Override
    public void flavorsChanged(FlavorEvent e) {
        String data = null;
        try {
            data = (String) CLIPBOARD.getData(DataFlavor.stringFlavor);
        } catch (UnsupportedFlavorException | IOException e1) {
            e1.printStackTrace();
        }

        if (!ITEMS.contains(data)) {
            ITEMS.add(data);
            recreateTray();
        }

        // Make sure flavoreChanged will invoke only once.
        CLIPBOARD.removeFlavorListener(this);
        CLIPBOARD.setContents(CLIPBOARD.getContents(null), null);
        CLIPBOARD.addFlavorListener(this);
    }

    private String getDisplayedName(String data) {
        if (data.length() > 15) {
            return data.substring(0, 15) + " ...";
        } else {
            return data;
        }
    }
}
