package com.project610.UI;

import javax.swing.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class LazyDataBinding implements KeyListener {
    HornPanel panel;
    String field;

    public LazyDataBinding(HornPanel panel, String field) {
        this.panel = panel;
        this.field = field;
    }

    @Override
    public void keyTyped(KeyEvent e) {
        bindData(e);
    }

    @Override
    public void keyPressed(KeyEvent e) {
        bindData(e);
    }

    @Override
    public void keyReleased(KeyEvent e) {
        bindData(e);
    }

    private void bindData(KeyEvent e) {
        JTextField source = ((JTextField)e.getSource());
        String text = source.getText();

        switch (field) {
            case "name":
                if (text.length() > 6) {
                    text = text.substring(0, Math.min(6, text.length()));
                    source.setText(text);
                }
                panel.name = text;
                break;
            case "info":
                panel.info = text;
                break;
            case "extags":
                panel.extags = text;
                break;
        }
    }
}
