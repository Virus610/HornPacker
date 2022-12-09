package com.project610.UI;

import com.project610.Utils;

import javax.sound.sampled.*;
import javax.swing.*;
import java.applet.AudioClip;
import java.awt.*;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import static com.project610.Utils.hbox;

public class HornPanel extends JPanel {
    public File file;
    public String name, info, extags;
    public MainPanel parent;

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

        JButton deleteButton = new JButton("❌");
        deleteButton.addActionListener(e -> parent.deleteHorn(this));
        fieldsPanel.add(Utils.prefSize(deleteButton, 44, 44));

        fieldsPanel.add(Box.createRigidArea(new Dimension(10,1)));
        JButton previewButton = new JButton("🔊");
        previewButton.addActionListener(e -> previewHorn());
        fieldsPanel.add(Utils.prefSize(previewButton,44, 44));

        fieldsPanel.add(new JLabel("  Name:"));
        JTextField nameField = new JTextField(name, 6);
        nameField.addKeyListener(new LazyDataBinding(this, "name"));
        fieldsPanel.add(nameField);

        fieldsPanel.add(new JLabel("  Info:"));
        JTextField infoField = new JTextField(info, 40);
        infoField.addKeyListener(new LazyDataBinding(this, "info"));
        fieldsPanel.add(infoField);

        fieldsPanel.add(new JLabel("  ExTags:"));
        JTextField extagsField = new JTextField(extags, 20);
        extagsField.addKeyListener(new LazyDataBinding(this, "extags"));
        fieldsPanel.add(extagsField);

        add(Box.createRigidArea(new Dimension(1, 5)));
        add(new JSeparator(JSeparator.HORIZONTAL));
        add(Box.createRigidArea(new Dimension(1, 5)));
    }

    private void previewHorn() {

        try {
            for (AudioFileFormat.Type type : AudioSystem.getAudioFileTypes()) {
                System.out.println("Type: " + type.getExtension());
            }
            BufferedInputStream soundFile;
            Clip clip;
            AudioInputStream stream;

            AudioFormat format;
            DataLine.Info info;

            InputStream is = new FileInputStream(file.getPath());
            soundFile = new BufferedInputStream(is);

            stream = AudioSystem.getAudioInputStream(soundFile);
            format = stream.getFormat();
            info = new DataLine.Info(Clip.class, format);
            clip = (Clip) AudioSystem.getLine(info);
            clip.open(stream);

            // Play sound, then wait to proceed until it's done
            clip.start();
            while (clip.getMicrosecondLength() != clip.getMicrosecondPosition()) {
                Thread.sleep(1);
            }
            clip.close();
            soundFile.close();
            is.close();

        } catch (Exception ex) {
            System.err.println("Audio error");
            ex.printStackTrace();
        }
    }
}
