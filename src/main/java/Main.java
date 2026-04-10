
import com.formdev.flatlaf.FlatIntelliJLaf;
import ui.MainFrame;

import javax.swing.*;
import java.awt.*;

public class Main {

    public static void main(String[] args) {
        // Install FlatLaf before any Swing components are created
        FlatIntelliJLaf.setup();

        // Global UI tweaks — applied once here so every component picks them up
        UIManager.put("Button.arc", 8);
        UIManager.put("Component.arc", 6);
        UIManager.put("TextComponent.arc", 6);
        UIManager.put("Component.focusWidth", 1);
        UIManager.put("ScrollBar.trackArc", 999);
        UIManager.put("ScrollBar.thumbArc", 999);
        UIManager.put("ScrollBar.width", 10);
        UIManager.put("TabbedPane.tabType", "card");
        UIManager.put("Table.showHorizontalLines", false);
        UIManager.put("Table.showVerticalLines", false);
        UIManager.put("Table.intercellSpacing", new Dimension(0, 1));

        // So we use EDT so that all the UI creation happens on the thread so we dont get random glitches/freezes
        SwingUtilities.invokeLater(() -> {
            MainFrame f = new MainFrame();
            f.start();
        });
    }
}
