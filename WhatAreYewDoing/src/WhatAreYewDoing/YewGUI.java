package WhatAreYewDoing;

import org.dreambot.api.utilities.Images;
import org.dreambot.api.utilities.Logger;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class YewGUI {
    private volatile boolean tree;
    private volatile boolean oak;
    private volatile boolean yew;
    private volatile boolean useGE;

    private volatile boolean confirmed = false;

    public YewGUI() {
        SwingUtilities.invokeLater(this::showGUI);
    }

    public void showGUI() {
        JFrame frame = new JFrame("WhatAreYewDoing — One Tap Woodcutting");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(450, 450);
        frame.setLocationRelativeTo(null);

        // ---------- Main panel setup ----------
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        panel.setBackground(new Color(80, 99, 85));

        // ---------- Logo ----------
        try {
            BufferedImage icon = Images.loadImage("https://i.imgur.com/bNkG7Z7.png"); // Direct .png URL
            if (icon != null) {
                // Scale image nicely
                Image scaled = icon.getScaledInstance(64, 64, Image.SCALE_SMOOTH);
                JLabel logoLabel = new JLabel(new ImageIcon(scaled));
                logoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
                panel.add(logoLabel);
                panel.add(Box.createRigidArea(new Dimension(0, 10)));

                // Set window icon
                frame.setIconImage(icon);
                Logger.log("✅ Logo loaded successfully");
            } else {
                Logger.log("⚠️ Images.loadImage returned null (check URL)");
            }
        } catch (Exception e) {
            Logger.log("❌ Failed to load logo: " + e.getMessage());
            e.printStackTrace();
        }

        // ---------- Title ----------
        JLabel titleLabel = new JLabel("One Tap Woodcutting");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(titleLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));

        // ---------- Instruction ----------
        JLabel subtitleLabel = new JLabel("Select which trees to chop:");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(subtitleLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));

        titleLabel.setForeground(Color.WHITE);
        subtitleLabel.setForeground(Color.WHITE);

        // ---------- Checkboxes ----------
        JCheckBox treeBox = new JCheckBox("Chop Regular Trees", tree);
        JCheckBox oakBox = new JCheckBox("Chop Oak Trees", oak);
        JCheckBox yewBox = new JCheckBox("Chop Yew Trees", yew);

        for (JCheckBox box : new JCheckBox[]{treeBox, oakBox, yewBox}) {
            box.setAlignmentX(Component.CENTER_ALIGNMENT);
            box.setForeground(Color.WHITE);
            box.setBackground(Color.BLACK);
            panel.add(box);
            panel.add(Box.createRigidArea(new Dimension(0, 5)));
        }
        JLabel useGELabel = new JLabel("Buy new axes automatically from GE:");
        useGELabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        useGELabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(useGELabel);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));

        JCheckBox geBox = new JCheckBox("Buy better axes from the GE:", useGE);
        geBox.setAlignmentX(Component.CENTER_ALIGNMENT);
        geBox.setForeground(Color.WHITE);
        geBox.setBackground(Color.BLACK);
        panel.add(geBox);
        panel.add(Box.createRigidArea(new Dimension(0, 5)));

        // ---------- Start button ----------
        JButton startButton = new JButton("Start Script");
        startButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        startButton.setBackground(new Color(0x4CAF50));
        startButton.setForeground(Color.WHITE);
        startButton.setFocusPainted(false);
        startButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        startButton.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        startButton.addActionListener(e -> {
            tree = treeBox.isSelected();
            oak = oakBox.isSelected();
            yew = yewBox.isSelected();
            useGE = geBox.isSelected();
            confirmed = true;
            frame.dispose();
        });

        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        panel.add(startButton);

        // ---------- Info Note at Bottom ----------
        JLabel noteLabel = new JLabel("If nothing is selected, the script will automatically choose the most efficient log.");
        noteLabel.setForeground(Color.LIGHT_GRAY); // Soft gray text
        noteLabel.setFont(new Font("Arial", Font.ITALIC, 11)); // Small italic font
        noteLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(Box.createRigidArea(new Dimension(0, 10))); // Spacing
        panel.add(noteLabel);

        panel.add(Box.createRigidArea(new Dimension(0, 10))); // spacing before note
        panel.add(noteLabel);

        frame.add(panel);
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

    public boolean getUseGE() { return useGE; }
}
