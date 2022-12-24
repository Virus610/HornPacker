package com.project610.UI;

import com.project610.Utils.Utils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import static com.project610.Utils.Utils.hbox;


import java.io.File;

import static javax.sound.sampled.AudioSystem.getAudioInputStream;


public class HornPanel extends JPanel {
    public File file;
    public String name, info, extags;
    public MainPanel parent;

    public JButton deleteButton, previewButton;
    public JTextField nameField, infoField, extagsField;

    public HornPanel(MainPanel parent, File file) {
        this(parent, file, "", "", "");
    }

    public HornPanel(MainPanel parent, File file, String name, String info, String extags) {
        this.parent = parent;
        this.file = file;
        this.name = name;
        this.info = info;
        this.extags = extags;
        initUI();
    }

    public void initUI() {
        setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
        setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));

        JPanel pathPanel = hbox();
        add(pathPanel);

        pathPanel.add(Box.createRigidArea(new Dimension(25,10)));
        pathPanel.add(new JLabel(file.getAbsolutePath()));
        pathPanel.add(Box.createHorizontalGlue());

        JPanel fieldsPanel = hbox();
        add(fieldsPanel);

        deleteButton = new JButton("❌");
        deleteButton.addActionListener(e -> parent.deleteHorn(this));
        deleteButton.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {

            }

            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_UP) {
                    changeRecord("deleteButton", -1);
                } else if (e.getKeyCode() == KeyEvent.VK_DOWN) {
                    changeRecord("deleteButton", 1);
                } else if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
                    changeRecord("previewButton", 0);
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {

            }
        });
        fieldsPanel.add(Utils.prefSize(deleteButton, 44, 44));

        fieldsPanel.add(Box.createRigidArea(new Dimension(10,1)));
        previewButton = new JButton("🔊");
        previewButton.addActionListener(e -> Player.play(file.getPath()));
        previewButton.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {

            }

            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_UP) {
                    changeRecord("previewButton", -1);
                } else if (e.getKeyCode() == KeyEvent.VK_DOWN) {
                    changeRecord("previewButton", 1);
                } else if (e.getKeyCode() == KeyEvent.VK_LEFT) {
                    changeRecord("deleteButton", 0);
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {

            }
        });
        fieldsPanel.add(Utils.prefSize(previewButton,44, 44));

        fieldsPanel.add(new JLabel("  Name:"));
        nameField = new JTextField(name, 6);
        nameField.addKeyListener(new LazyDataBinding(this, "name"));
        fieldsPanel.add(nameField);

        fieldsPanel.add(new JLabel("  Info:"));
        infoField = new JTextField(info, 40);
        infoField.addKeyListener(new LazyDataBinding(this, "info"));
        fieldsPanel.add(infoField);

        fieldsPanel.add(new JLabel("  ExTags:"));
        extagsField = new JTextField(extags, 20);
        extagsField.addKeyListener(new LazyDataBinding(this, "extags"));
        fieldsPanel.add(extagsField);

        add(Box.createRigidArea(new Dimension(1, 5)));
        add(new JSeparator(JSeparator.HORIZONTAL));
        add(Box.createRigidArea(new Dimension(1, 5)));
    }

    public void changeRecord(String componentName, int direction) {
        try {
            HornPanel newPanel = parent.hornList.get(parent.hornList.indexOf(this) + direction);
            switch (componentName) {
                case "deleteButton":
                    newPanel.deleteButton.grabFocus();
                    break;
                case "previewButton":
                    newPanel.previewButton.grabFocus();
                    break;
                default:
            }
        } catch (IndexOutOfBoundsException ex) {}
    }
}
