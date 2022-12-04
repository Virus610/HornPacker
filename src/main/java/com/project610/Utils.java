package com.project610;

import javax.swing.*;
import java.awt.*;
import java.io.Closeable;

public class Utils {
    public static void closeThing(Closeable s) {
        try {
            if (null != s) s.close();
        } catch (Exception ex) {
            // Nyeh!
        }
    }

    public static JPanel hbox() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.LINE_AXIS));
        return panel;
    }

    public static JPanel vbox() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
        return panel;
    }

    public static Component prefSize(Component component, int w, int h) {
        component.setPreferredSize(new Dimension(w, h));
        return component;
    }
}
