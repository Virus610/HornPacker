package com.project610;

import com.project610.UI.MainPanel;

import javax.swing.*;
import java.awt.*;

public class HornPacker {
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            JFrame.setDefaultLookAndFeelDecorated(true);
            JDialog.setDefaultLookAndFeelDecorated(true);
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            System.setProperty("awt.useSystemAAFontSettings","on");
            System.setProperty("swing.aatext", "true");
        } catch (Exception ex) {
            System.err.println("Look and feel broke, probably falling back on the garbo L&F");
            ex.printStackTrace();
        }
        JFrame jf = new JFrame("SRB2K Horn Packager");
        //jf.setSize(100, 100);
        jf.setMinimumSize(new Dimension(710, 611));
        MainPanel mainPanel = new MainPanel(args, jf);
        mainPanel.setBackground(new Color(230, 230,230));
        jf.setContentPane(mainPanel);
        jf.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

//        try {
//            jf.setIconImage(ImageIO.read(SpriteSwapper.class.getClassLoader().getResource("icons/appIcon.png")));
//        } catch (Exception ex) {
//            mainPanel.error("Failed to set app icon", ex);
//        }
        jf.setVisible(true);

        mainPanel.init();
    }
}
