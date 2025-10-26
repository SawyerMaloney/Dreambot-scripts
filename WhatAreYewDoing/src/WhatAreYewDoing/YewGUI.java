package WhatAreYewDoing;

import javax.swing.*;
import java.awt.*;

public class YewGUI {
    private volatile boolean tree;
    private volatile boolean oak;
    private volatile boolean yew;

    private volatile boolean confirmed = false;

    public YewGUI() {
        SwingUtilities.invokeLater(this::showGUI);
    }

    public void showGUI() {
        JFrame frame = new JFrame("WhatAreYewDoing Settings");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(300, 150);
        frame.setLayout(new GridLayout(3,2));

        JCheckBox treeBox = new JCheckBox("Chop trees", tree);
        JCheckBox oakBox = new JCheckBox("Chop Oak trees", oak);
        JCheckBox yewBox = new JCheckBox("Chop Yew trees", yew);

        JButton startButton = new JButton("Start");
        startButton.addActionListener(e -> {
            tree = treeBox.isSelected();
            oak = oakBox.isSelected();
            yew = yewBox.isSelected();
            confirmed = true;
            frame.dispose();
        });

        frame.add(treeBox);
        frame.add(oakBox);
        frame.add(yewBox);
        frame.add(startButton);

        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    public void sleepUntilConfirmed() {
        while (!confirmed) {
            try {
                Thread.sleep(200);
            } catch (InterruptedException ignored) {}
        }
    }

    public boolean getTree() {
        return tree;
    }

    public boolean getOak() {
        return oak;
    }

    public boolean getYew() {
        return yew;
    }
}
