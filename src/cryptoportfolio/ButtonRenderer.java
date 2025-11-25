/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cryptoportfolio;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

/**
 *
 * @author Ba Thanh Danh Phan
 */
    // Button renderer for Actions column
class ButtonRenderer extends JPanel implements TableCellRenderer {
    private JButton editButton;
    private JButton removeButton;

    public ButtonRenderer() {
        setLayout(new FlowLayout(FlowLayout.CENTER, 5, 14));
        setOpaque(true);

        //editButton = createCellButton("Edit", new Color(37, 99, 235));
        editButton = new JButton("Edit");
        editButton.setFont(new Font("Arial", Font.PLAIN, 11));
        editButton.setBackground(new Color(37, 99, 235));
        editButton.setForeground(Color.WHITE);
        editButton.setFocusPainted(false);
        editButton.setBorderPainted(false);
        editButton.setContentAreaFilled(true);
        editButton.setOpaque(true);
        editButton.setUI(new javax.swing.plaf.basic.BasicButtonUI());

        //removeButton = createCellButton("Removedd", new Color(220, 38, 38));
        removeButton = new JButton("Remove");
        removeButton.setFont(new Font("Arial", Font.PLAIN, 11));
        removeButton.setBackground(new Color(220, 38, 38));
        removeButton.setForeground(Color.WHITE);
        removeButton.setFocusPainted(false);
        removeButton.setBorderPainted(false);
        removeButton.setContentAreaFilled(true);
        removeButton.setOpaque(true);
        removeButton.setUI(new javax.swing.plaf.basic.BasicButtonUI());

        add(editButton);
        add(removeButton);
    }

    public Component getTableCellRendererComponent(JTable table, Object value,
            boolean isSelected, boolean hasFocus, int row, int column) {
        setBackground(isSelected ? new Color(51, 65, 85) : new Color(30, 41, 59));
        return this;
    }

    private JButton createCellButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.PLAIN, 11));
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(true);
        button.setOpaque(true);
        button.setUI(new javax.swing.plaf.basic.BasicButtonUI());
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                button.setBackground(bgColor.darker());
            }
            public void mouseExited(MouseEvent e) {
                button.setBackground(bgColor);
            }
        });

        return button;
    }

}