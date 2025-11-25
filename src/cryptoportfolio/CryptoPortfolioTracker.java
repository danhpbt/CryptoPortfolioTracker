package cryptoportfolio;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.*;
import java.util.*;
import org.json.*;
import javax.swing.Timer;
import javax.imageio.ImageIO;

public class CryptoPortfolioTracker extends JFrame {
    private DefaultTableModel tableModel;
    private JTable portfolioTable;
    private JLabel totalValueLabel;
    private JComboBox<String> cryptoComboBox;
    private JTextField amountField;
    private ArrayList<CryptoHolding> holdings;
    private Timer refreshTimer;
    private Map<String, ImageIcon> imageCache;
    
    // Crypto data map: Symbol -> Full Name
    private final Map<String, String> cryptoMap = new LinkedHashMap<String, String>() {{
        put("bitcoin", "Bitcoin (BTC)");
        put("ethereum", "Ethereum (ETH)");
        put("cardano", "Cardano (ADA)");
        put("solana", "Solana (SOL)");
        put("binancecoin", "Binance Coin (BNB)");
        put("ripple", "Ripple (XRP)");
        put("polkadot", "Polkadot (DOT)");
        put("dogecoin", "Dogecoin (DOGE)");
        put("matic-network", "Polygon (MATIC)");
        put("avalanche-2", "Avalanche (AVAX)");
    }};
    
    // Image file names for each crypto (from resources folder)
    private final Map<String, String> imageFileMap = new HashMap<String, String>() {{
        put("Bitcoin (BTC)", "bitcoin.png");
        put("Ethereum (ETH)", "ethereum.png");
        put("Cardano (ADA)", "cardano.png");
        put("Solana (SOL)", "solana.png");
        put("Binance Coin (BNB)", "binance.png");
        put("Ripple (XRP)", "ripple.png");
        put("Polkadot (DOT)", "polkadot.png");
        put("Dogecoin (DOGE)", "dogecoin.png");
        put("Polygon (MATIC)", "polygon.png");
        put("Avalanche (AVAX)", "avalanche.png");
    }};

    public CryptoPortfolioTracker() {
        imageCache = new HashMap<>();
        holdings = new ArrayList<>();
        loadHoldings();
        loadCryptoImages();
        initializeUI();
        fetchPrices();
        startAutoRefresh();
    }
    
    private void loadCryptoImages() {
        for (Map.Entry<String, String> entry : imageFileMap.entrySet()) {
            try {
                // Try to load from resources folder in src directory
                InputStream imageStream = getClass().getResourceAsStream("/resources/" + entry.getValue());
                
                if (imageStream != null) {
                    BufferedImage image = ImageIO.read(imageStream);
                    if (image != null) {
                        Image scaledImage = image.getScaledInstance(32, 32, Image.SCALE_SMOOTH);
                        imageCache.put(entry.getKey(), new ImageIcon(scaledImage));
                    }
                    imageStream.close();
                } else {
                    // If not found in classpath, try loading from file system
                    File imageFile = new File("src/resources/" + entry.getValue());
                    if (imageFile.exists()) {
                        BufferedImage image = ImageIO.read(imageFile);
                        if (image != null) {
                            Image scaledImage = image.getScaledInstance(32, 32, Image.SCALE_SMOOTH);
                            imageCache.put(entry.getKey(), new ImageIcon(scaledImage));
                        }
                    } else {
                        System.err.println("Image not found: " + entry.getValue());
                        imageCache.put(entry.getKey(), createPlaceholderIcon(entry.getKey()));
                    }
                }
            } catch (Exception ex) {
                System.err.println("Failed to load image for " + entry.getKey() + ": " + ex.getMessage());
                imageCache.put(entry.getKey(), createPlaceholderIcon(entry.getKey()));
            }
        }
    }
    
    private ImageIcon createPlaceholderIcon(String cryptoName) {
        BufferedImage placeholder = new BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = placeholder.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Draw a circle with first letter
        g2d.setColor(new Color(100, 100, 100));
        g2d.fillOval(0, 0, 32, 32);
        
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 16));
        String symbol = cryptoName.substring(cryptoName.indexOf("(") + 1, cryptoName.indexOf(")"));
        FontMetrics fm = g2d.getFontMetrics();
        int x = (32 - fm.stringWidth(symbol.substring(0, 1))) / 2;
        int y = ((32 - fm.getHeight()) / 2) + fm.getAscent();
        g2d.drawString(symbol.substring(0, 1), x, y);
        g2d.dispose();
        
        return new ImageIcon(placeholder);
    }

    private void initializeUI() {
        setTitle("Crypto Portfolio Tracker");
        setSize(1200, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        // Main panel with dark background
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBackground(new Color(15, 23, 42));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Header Panel
        JPanel headerPanel = createHeaderPanel();
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        
        // Table Panel
        JPanel tablePanel = createTablePanel();
        mainPanel.add(tablePanel, BorderLayout.CENTER);
        
        // Add Portfolio Panel
        JPanel addPanel = createAddPanel();
        mainPanel.add(addPanel, BorderLayout.SOUTH);
        
        add(mainPanel);
        setVisible(true);
    }
    
    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout(10, 10));
        headerPanel.setBackground(new Color(30, 41, 59));
        headerPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(51, 65, 85)),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));
        
        // Title section
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        titlePanel.setBackground(new Color(30, 41, 59));
                
        JPanel textPanel = new JPanel(new GridLayout(2, 1));
        textPanel.setBackground(new Color(30, 41, 59));
        
        JLabel titleLabel = new JLabel("Crypto Portfolio Tracker");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);
        
        JLabel subtitleLabel = new JLabel("Real-time USD conversion");
        subtitleLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        subtitleLabel.setForeground(new Color(148, 163, 184));
        
        textPanel.add(titleLabel);
        textPanel.add(subtitleLabel);
        
        titlePanel.add(textPanel);
        
        // Refresh button
        JButton refreshButton = createStyledButton("Refresh Prices", new Color(73, 170, 77));
        refreshButton.addActionListener(e -> fetchPrices());
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(new Color(30, 41, 59));
        buttonPanel.add(refreshButton);
        
        headerPanel.add(titlePanel, BorderLayout.WEST);
        headerPanel.add(buttonPanel, BorderLayout.EAST);
        
        // Total value panel
        JPanel totalPanel = new JPanel(new GridLayout(2, 1));
        totalPanel.setBackground(new Color(37, 99, 235));
        totalPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        
        JLabel totalLabel = new JLabel("Total Portfolio Value");
        totalLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        totalLabel.setForeground(new Color(224, 231, 255));
        
        totalValueLabel = new JLabel("$0.00");
        totalValueLabel.setFont(new Font("Arial", Font.BOLD, 32));
        totalValueLabel.setForeground(Color.WHITE);
        
        totalPanel.add(totalLabel);
        totalPanel.add(totalValueLabel);
        
        JPanel outerPanel = new JPanel(new BorderLayout());
        outerPanel.setBackground(new Color(30, 41, 59));
        outerPanel.add(headerPanel, BorderLayout.NORTH);
        outerPanel.add(totalPanel, BorderLayout.CENTER);
        
        return outerPanel;
    }
    
    private JPanel createTablePanel() {
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBackground(new Color(30, 41, 59));
        tablePanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(51, 65, 85)),
            BorderFactory.createEmptyBorder(0, 0, 0, 0)
        ));
        
        // Table header
        JPanel tableHeaderPanel = new JPanel(new BorderLayout());
        tableHeaderPanel.setBackground(new Color(51, 65, 85));
        tableHeaderPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        
        JLabel tableTitle = new JLabel("Your Holdings");
        tableTitle.setFont(new Font("Arial", Font.BOLD, 18));
        tableTitle.setForeground(Color.WHITE);
        tableHeaderPanel.add(tableTitle, BorderLayout.WEST);
        
        // Create table
        String[] columnNames = {"Cryptocurrency", "Amount", "Price (USD)", "Value (USD)", "24h Change", "Actions"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 5; // Only Actions column is editable
            }
        };
        
        portfolioTable = new JTable(tableModel);
        portfolioTable.setRowHeight(50);
        portfolioTable.setBackground(new Color(30, 41, 59));
        portfolioTable.setForeground(Color.WHITE);
        portfolioTable.setSelectionBackground(new Color(51, 65, 85));
        portfolioTable.setSelectionForeground(Color.WHITE);
        portfolioTable.setGridColor(new Color(51, 65, 85));
        portfolioTable.setFont(new Font("Arial", Font.PLAIN, 13));
        
        // Custom header renderer
        JTableHeader header = portfolioTable.getTableHeader();
        header.setDefaultRenderer(new DefaultTableCellRenderer() {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel label = (JLabel) super.getTableCellRendererComponent(
                        table, value, isSelected, hasFocus, row, column);
                label.setBackground(new Color(37, 99, 235));
                label.setForeground(Color.WHITE);
                label.setFont(new Font("Arial", Font.BOLD, 12));
                label.setHorizontalAlignment(SwingConstants.CENTER);
                label.setBorder(BorderFactory.createEmptyBorder(10, 5, 10, 5));
                label.setOpaque(true);
                return label;
            }
        });

        // Custom cell renderer with icons
        portfolioTable.setDefaultRenderer(Object.class, new CryptoTableCellRenderer());
        
        // Button column
        portfolioTable.getColumn("Actions").setCellRenderer(new ButtonRenderer());
        portfolioTable.getColumn("Actions").setCellEditor(new ButtonEditor(new JCheckBox()));
        
        JScrollPane scrollPane = new JScrollPane(portfolioTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(new Color(30, 41, 59));
        
        tablePanel.add(tableHeaderPanel, BorderLayout.NORTH);
        tablePanel.add(scrollPane, BorderLayout.CENTER);
        
        return tablePanel;
    }
    
    private JPanel createAddPanel() {
        JPanel addPanel = new JPanel(new BorderLayout());
        addPanel.setBackground(new Color(30, 41, 59));
        addPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(51, 65, 85)),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));
        
        JLabel addTitle = new JLabel("Add to Portfolio");
        addTitle.setFont(new Font("Arial", Font.BOLD, 16));
        addTitle.setForeground(Color.WHITE);
        addTitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        
        JPanel formPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        formPanel.setBackground(new Color(30, 41, 59));
        
        // Crypto ComboBox
        JPanel cryptoPanel = new JPanel(new BorderLayout(5, 5));
        cryptoPanel.setBackground(new Color(30, 41, 59));
        JLabel cryptoLabel = new JLabel("Select Cryptocurrency");
        cryptoLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        cryptoLabel.setForeground(new Color(203, 213, 225));
        
        String[] cryptoNames = cryptoMap.values().toArray(new String[0]);
        cryptoComboBox = new JComboBox<>(cryptoNames);
        cryptoComboBox.setPreferredSize(new Dimension(250, 35));
        cryptoComboBox.setBackground(new Color(51, 65, 85));
        cryptoComboBox.setBackground(Color.RED);
        cryptoComboBox.setForeground(Color.WHITE);
        
        cryptoPanel.add(cryptoLabel, BorderLayout.NORTH);
        cryptoPanel.add(cryptoComboBox, BorderLayout.CENTER);
        
        // Amount Field
        JPanel amountPanel = new JPanel(new BorderLayout(5, 5));
        amountPanel.setBackground(new Color(30, 41, 59));
        JLabel amountLabel = new JLabel("Amount");
        amountLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        amountLabel.setForeground(new Color(203, 213, 225));
        
        amountField = new JTextField();
        amountField.setPreferredSize(new Dimension(250, 35));
        amountField.setBackground(new Color(51, 65, 85));
        amountField.setForeground(Color.WHITE);
        amountField.setCaretColor(Color.WHITE);
        amountField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(71, 85, 105)),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        
        amountPanel.add(amountLabel, BorderLayout.NORTH);
        amountPanel.add(amountField, BorderLayout.CENTER);
        
        // Add Button
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonPanel.setBackground(new Color(30, 41, 59));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(17, 0, 0, 0));
        
        JButton addButton = createStyledButton("Add", new Color(37, 99, 235));
        addButton.addActionListener(e -> addCrypto());
        
        buttonPanel.add(addButton);
        
        formPanel.add(cryptoPanel);
        formPanel.add(amountPanel);
        formPanel.add(buttonPanel);
        
        JPanel container = new JPanel(new BorderLayout());
        container.setBackground(new Color(30, 41, 59));
        container.add(addTitle, BorderLayout.NORTH);
        container.add(formPanel, BorderLayout.CENTER);
        
        addPanel.add(container);
        
        return addPanel;
    }
    
    private JButton createStyledButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 13));
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
    
    private void addCrypto() {
        String selectedCrypto = (String) cryptoComboBox.getSelectedItem();
        String amountText = amountField.getText().trim();
        
        if (selectedCrypto == null || amountText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select a cryptocurrency and enter an amount.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        try {
            double amount = Double.parseDouble(amountText);
            if (amount <= 0) {
                JOptionPane.showMessageDialog(this, "Amount must be greater than zero.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            CryptoHolding holding = new CryptoHolding(selectedCrypto, amount);
            holdings.add(holding);
            saveHoldings();
            fetchPrices();
            
            amountField.setText("");
            cryptoComboBox.setSelectedIndex(0);
            
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Please enter a valid number for amount.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void fetchPrices() {
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() {
                try {
                    // Build API URL with all crypto IDs
                    StringBuilder ids = new StringBuilder();
                    for (String id : cryptoMap.keySet()) {
                        if (ids.length() > 0) ids.append(",");
                        ids.append(id);
                    }
                    
                    String apiUrl = "https://api.coingecko.com/api/v3/simple/price?ids=" + ids + 
                                    "&vs_currencies=usd&include_24hr_change=true";
                    
                    URL url = new URL(apiUrl);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("GET");
                    
                    BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();
                    
                    JSONObject jsonResponse = new JSONObject(response.toString());
                    
                    // Update holdings with prices
                    for (CryptoHolding holding : holdings) {
                        String id = getIdFromName(holding.name);
                        if (jsonResponse.has(id)) {
                            JSONObject cryptoData = jsonResponse.getJSONObject(id);
                            holding.priceUSD = cryptoData.getDouble("usd");
                            holding.change24h = cryptoData.optDouble("usd_24h_change", 0.0);
                        }
                    }
                    
                } catch (Exception ex) {
                    ex.printStackTrace();
                    SwingUtilities.invokeLater(() -> {
                        JOptionPane.showMessageDialog(CryptoPortfolioTracker.this, 
                            "Failed to fetch prices. Please check your internet connection.", 
                            "Error", JOptionPane.ERROR_MESSAGE);
                    });
                }
                return null;
            }
            
            @Override
            protected void done() {
                updateTable();
            }
        };
        worker.execute();
    }
    
    private String getIdFromName(String name) {
        for (Map.Entry<String, String> entry : cryptoMap.entrySet()) {
            if (entry.getValue().equals(name)) {
                return entry.getKey();
            }
        }
        return "";
    }
    
    private void updateTable() {
        tableModel.setRowCount(0);
        double totalValue = 0.0;
        
        for (CryptoHolding holding : holdings) {
            double value = holding.amount * holding.priceUSD;
            totalValue += value;
            
            String changeStr = String.format("%+.2f%%", holding.change24h);
            
            tableModel.addRow(new Object[]{
                holding.name,
                String.format("%.8f", holding.amount),
                String.format("$%.2f", holding.priceUSD),
                String.format("$%.2f", value),
                changeStr,
                "Actions"
            });
        }
        
        totalValueLabel.setText(String.format("$%.2f", totalValue));
    }
    
    private void removeHolding(int index) {
        if (index >= 0 && index < holdings.size()) {
            holdings.remove(index);
            saveHoldings();
            updateTable();
        }
    }
    
    private void editHolding(int index) {
        if (index >= 0 && index < holdings.size()) {
            CryptoHolding holding = holdings.get(index);
            String newAmount = JOptionPane.showInputDialog(this, 
                "Enter new amount for " + holding.name + ":", 
                holding.amount);
            
            if (newAmount != null && !newAmount.trim().isEmpty()) {
                try {
                    double amount = Double.parseDouble(newAmount);
                    if (amount > 0) {
                        holding.amount = amount;
                        saveHoldings();
                        updateTable();
                    }
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this, "Invalid amount entered.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }
    
    private void saveHoldings() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("portfolio.dat"))) {
            oos.writeObject(holdings);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
    
    @SuppressWarnings("unchecked")
    private void loadHoldings() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream("portfolio.dat"))) {
            holdings = (ArrayList<CryptoHolding>) ois.readObject();
        } catch (FileNotFoundException ex) {
            holdings = new ArrayList<>();
        } catch (IOException | ClassNotFoundException ex) {
            ex.printStackTrace();
            holdings = new ArrayList<>();
        }
    }
    
    private void startAutoRefresh() {
        refreshTimer = new Timer(60000, e -> fetchPrices()); // Refresh every 60 seconds
        refreshTimer.start();
    }
    
    // Custom cell renderer with crypto images
    class CryptoTableCellRenderer extends DefaultTableCellRenderer {
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
    
    // Button editor for Actions column
    class ButtonEditor extends DefaultCellEditor {
        private JPanel panel;
        private JButton editButton;
        private JButton removeButton;
        private int currentRow;
        
        public ButtonEditor(JCheckBox checkBox) {
            super(checkBox);
            
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
                editHolding(currentRow);
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
                int confirm = JOptionPane.showConfirmDialog(
                    CryptoPortfolioTracker.this,
                    "Are you sure you want to remove this cryptocurrency?",
                    "Confirm Removal",
                    JOptionPane.YES_NO_OPTION
                );
                if (confirm == JOptionPane.YES_OPTION) {
                    removeHolding(currentRow);
                }
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
    }
    
    // CryptoHolding class
    static class CryptoHolding implements Serializable {
        private static final long serialVersionUID = 1L;
        String name;
        double amount;
        double priceUSD;
        double change24h;
        
        public CryptoHolding(String name, double amount) {
            this.name = name;
            this.amount = amount;
            this.priceUSD = 0.0;
            this.change24h = 0.0;
        }
    }
    
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        SwingUtilities.invokeLater(() -> new CryptoPortfolioTracker());
    }
}