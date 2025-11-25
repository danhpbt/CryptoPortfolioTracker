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
import java.util.Map;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;

/**
 *
 * @author Ba Thanh Danh Phan
 */
    // Custom cell renderer with crypto images
class CryptoTableCellRenderer extends DefaultTableCellRenderer {
    
    private Map<String, ImageIcon> imageCache;
    
    public CryptoTableCellRenderer(Map<String, ImageIcon> imageCache){
        this.imageCache = imageCache;
    }
    
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, 
            boolean isSelected, boolean hasFocus, int row, int column) {

        Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

        setBackground(isSelected ? new Color(51, 65, 85) : new Color(30, 41, 59));
        setForeground(Color.WHITE);

        if (column == 0) { // Cryptocurrency column with image
            String cryptoName = (String) value;
            JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
            panel.setBackground(getBackground());

            // Get crypto image
            ImageIcon icon = imageCache.get(cryptoName);
            JLabel iconLabel = new JLabel(icon);

            JLabel nameLabel = new JLabel(cryptoName);
            nameLabel.setForeground(Color.WHITE);
            nameLabel.setFont(new Font("Arial", Font.PLAIN, 13));

            panel.add(iconLabel);
            panel.add(nameLabel);

            return panel;
        } else if (column == 4) { // 24h Change column
            String changeStr = (String) value;
            if (changeStr.startsWith("+")) {
                setForeground(new Color(74, 222, 128)); // Green
            } else if (changeStr.startsWith("-")) {
                setForeground(new Color(248, 113, 113)); // Red
            }
            setHorizontalAlignment(SwingConstants.RIGHT);
        } else if (column >= 1 && column <= 3) {
            setHorizontalAlignment(SwingConstants.RIGHT);
        }

        return c;
    }
}