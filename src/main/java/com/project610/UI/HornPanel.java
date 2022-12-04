package com.project610.UI;

import javax.swing.*;
import java.awt.*;
import java.io.File;

import static com.project610.Utils.hbox;

public class HornPanel extends JPanel {
    public File file;
    public String name, info, extags;

    public HornPanel(File file) {
        this(file, "", "", "");
    }

    public HornPanel(File file, String name, String info, String extags) {
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

        fieldsPanel.add(new JLabel("  Name:"));
        JTextField nameField = new JTextField(name);
        nameField.addKeyListener(new LazyDataBinding(this, "name"));
        fieldsPanel.add(nameField);

        fieldsPanel.add(new JLabel("  Info:"));
        JTextField infoField = new JTextField(info);
        infoField.addKeyListener(new LazyDataBinding(this, "info"));
        fieldsPanel.add(infoField);

        fieldsPanel.add(new JLabel("  ExTags:"));
        JTextField extagsField = new JTextField(extags);
        extagsField.addKeyListener(new LazyDataBinding(this, "extags"));
        fieldsPanel.add(extagsField);

        add(Box.createRigidArea(new Dimension(1, 5)));
        add(new JSeparator(JSeparator.HORIZONTAL));
        add(Box.createRigidArea(new Dimension(1, 5)));
    }
}
