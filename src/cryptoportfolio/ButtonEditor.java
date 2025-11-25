/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cryptoportfolio;

import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;

/**
 *
 * @author Ba Thanh Danh Phan
 */
 // Button editor for Actions column
class ButtonEditor extends DefaultCellEditor {
    private JPanel panel;
    private JButton editButton;
    private JButton removeButton;
    private int currentRow;
    private ActionListener actionListener;
    public ButtonEditor(JCheckBox checkBox, ActionListener listener) {
        super(checkBox);
        actionListener = listener;
        
        panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 14));
        panel.setOpaque(true);

        editButton = new JButton("Edit");
        editButton.setFont(new Font("Arial", Font.PLAIN, 11));
        editButton.setBackground(new Color(37, 99, 235));
        editButton.setForeground(Color.WHITE);
        editButton.setFocusPainted(false);
        editButton.setBorderPainted(false);
        editButton.setContentAreaFilled(true);
        editButton.setOpaque(true);
        editButton.setUI(new javax.swing.plaf.basic.BasicButtonUI());
        editButton.addActionListener(e -> {
            fireEditingStopped();
            actionListener.editCrypto();
        });

        removeButton = new JButton("Remove");
        removeButton.setFont(new Font("Arial", Font.PLAIN, 11));
        removeButton.setBackground(new Color(220, 38, 38));
        removeButton.setForeground(Color.WHITE);
        removeButton.setFocusPainted(false);
        removeButton.setBorderPainted(false);
        removeButton.setContentAreaFilled(true);
        removeButton.setOpaque(true);
        removeButton.setUI(new javax.swing.plaf.basic.BasicButtonUI());

        removeButton.addActionListener(e -> {
            fireEditingStopped();
            actionListener.removeCrypto();
        });

        panel.add(editButton);
        panel.add(removeButton);
    }

    public Component getTableCellEditorComponent(JTable table, Object value,
            boolean isSelected, int row, int column) {
        currentRow = row;
        panel.setBackground(new Color(51, 65, 85));
        return panel;
    }

    public Object getCellEditorValue() {
        return "Actions";
    }

    public int getCurrentRow() {
        return currentRow;
    }
    
    interface ActionListener{
        public void editCrypto();
        public void removeCrypto();        
    }
}

