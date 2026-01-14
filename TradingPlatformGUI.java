import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class TradingPlatformGUI extends JFrame {

    Map<String, Stock> market = new HashMap<>();
    User user = new User();
    ArrayList<Transaction> transactions = new ArrayList<>();

    JTable marketTable, portfolioTable;
    DefaultTableModel marketModel, portfolioModel;
    JLabel balanceLabel, profitLossLabel;

    public TradingPlatformGUI() {
        setTitle("Stock Trading Simulator");
        setSize(900, 500);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // ===== Market Data =====
        market.put("AAPL", new Stock("AAPL", 150));
        market.put("GOOGL", new Stock("GOOGL", 2800));
        market.put("TSLA", new Stock("TSLA", 700));

        // ===== Top Panel =====
        JPanel topPanel = new JPanel(new GridLayout(1, 2));
        balanceLabel = new JLabel("Balance: $10000");
        profitLossLabel = new JLabel("Total P/L: $0.00");

        topPanel.add(balanceLabel);
        topPanel.add(profitLossLabel);
        add(topPanel, BorderLayout.NORTH);

        // ===== Tables =====
        marketModel = new DefaultTableModel(new String[]{"Symbol", "Price"}, 0);
        portfolioModel = new DefaultTableModel(
                new String[]{"Symbol", "Shares", "Avg Buy", "Current Price", "P/L"}, 0
        );

        marketTable = new JTable(marketModel);
        portfolioTable = new JTable(portfolioModel);

        refreshMarket();

        // Color profit/loss
        portfolioTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            public Component getTableCellRendererComponent(
                    JTable table, Object value, boolean isSelected,
                    boolean hasFocus, int row, int column) {

                Component c = super.getTableCellRendererComponent(
                        table, value, isSelected, hasFocus, row, column);

                if (column == 4) {
                    double pl = Double.parseDouble(table.getValueAt(row, 4).toString());
                    c.setForeground(pl >= 0 ? Color.GREEN : Color.RED);
                } else {
                    c.setForeground(Color.BLACK);
                }
                return c;
            }
        });

        JSplitPane splitPane = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT,
                new JScrollPane(marketTable),
                new JScrollPane(portfolioTable)
        );
        
        // Set the divider position to show the market table on the left
        // This value (300) will make the left pane (market table) 300 pixels wide
        // You can adjust this value based on your preference
        splitPane.setDividerLocation(300);
        
        // Optional: Set one-touch expandable for better user experience
        splitPane.setOneTouchExpandable(true);
        
        add(splitPane, BorderLayout.CENTER);

        // ===== Buttons =====
        JPanel buttonPanel = new JPanel();
        JButton buyBtn = new JButton("Buy");
        JButton sellBtn = new JButton("Sell");
        JButton savePLBtn = new JButton("Save P/L");

        buttonPanel.add(buyBtn);
        buttonPanel.add(sellBtn);
        buttonPanel.add(savePLBtn);
        add(buttonPanel, BorderLayout.SOUTH);

        buyBtn.addActionListener(e -> buyStock());
        sellBtn.addActionListener(e -> sellStock());
        savePLBtn.addActionListener(e -> saveProfitLossHistory());

        // ===== Price Fluctuation =====
        new javax.swing.Timer(3000, e -> fluctuatePrices()).start();
    }

    private void refreshMarket() {
        // Store current selection
        int selectedRow = marketTable.getSelectedRow();
        String selectedSymbol = null;
        if (selectedRow >= 0) {
            selectedSymbol = marketTable.getValueAt(selectedRow, 0).toString();
        }
        
        marketModel.setRowCount(0);
        for (Stock s : market.values()) {
            marketModel.addRow(new Object[]{s.symbol, String.format("%.2f", s.price)});
        }
        
        // Restore selection if possible
        if (selectedSymbol != null) {
            for (int i = 0; i < marketModel.getRowCount(); i++) {
                if (marketModel.getValueAt(i, 0).equals(selectedSymbol)) {
                    marketTable.setRowSelectionInterval(i, i);
                    break;
                }
            }
        }
    }

    private void refreshPortfolio() {
        // Store current selection
        int selectedRow = portfolioTable.getSelectedRow();
        String selectedSymbol = null;
        if (selectedRow >= 0) {
            selectedSymbol = portfolioTable.getValueAt(selectedRow, 0).toString();
        }
        
        portfolioModel.setRowCount(0);

        user.portfolio.forEach((symbol, qty) -> {
            if (qty > 0) {
                double avgBuy = user.avgBuyPrice.get(symbol);
                double current = market.get(symbol).price;
                double pl = (current - avgBuy) * qty;

                portfolioModel.addRow(new Object[]{
                        symbol,
                        qty,
                        String.format("%.2f", avgBuy),
                        String.format("%.2f", current),
                        String.format("%.2f", pl)
                });
            }
        });

        balanceLabel.setText("Balance: $" + String.format("%.2f", user.balance));
        profitLossLabel.setText("Total P/L: $" + String.format("%.2f", calculateProfitLoss()));
        
        // Restore selection if possible
        if (selectedSymbol != null) {
            for (int i = 0; i < portfolioModel.getRowCount(); i++) {
                if (portfolioModel.getValueAt(i, 0).equals(selectedSymbol)) {
                    portfolioTable.setRowSelectionInterval(i, i);
                    break;
                }
            }
        }
    }

    private double calculateProfitLoss() {
        double total = 0;
        for (String symbol : user.portfolio.keySet()) {
            int qty = user.portfolio.get(symbol);
            if (qty > 0) {
                double current = market.get(symbol).price;
                double buy = user.avgBuyPrice.get(symbol);
                total += (current - buy) * qty;
            }
        }
        return total;
    }

    private void buyStock() {
        int row = marketTable.getSelectedRow();
        if (row == -1) return;

        String symbol = marketTable.getValueAt(row, 0).toString();
        double price = market.get(symbol).price;

        if (user.balance >= price) {
            user.buy(symbol, price);
            transactions.add(new Transaction("BUY", symbol, price));
            refreshPortfolio();
        } else {
            JOptionPane.showMessageDialog(this, "Insufficient balance.");
        }
    }

    private void sellStock() {
        int row = portfolioTable.getSelectedRow();
        if (row == -1) return;

        String symbol = portfolioTable.getValueAt(row, 0).toString();
        double price = market.get(symbol).price;

        if (user.portfolio.get(symbol) > 0) {
            user.sell(symbol, price);
            transactions.add(new Transaction("SELL", symbol, price));
            refreshPortfolio();
        }
    }

    private void fluctuatePrices() {
        Random rand = new Random();
        for (Stock s : market.values()) {
            s.price = Math.max(1, s.price + (rand.nextDouble() * 10 - 5));
        }
        refreshMarket();
        refreshPortfolio();
    }

    private void saveProfitLossHistory() {
        try (FileWriter writer = new FileWriter("profit_loss_history.txt", true)) {
            writer.write("Balance: $" + user.balance + "\n");
            user.portfolio.forEach((symbol, qty) -> {
                if (qty > 0) {
                    double pl = (market.get(symbol).price -
                            user.avgBuyPrice.get(symbol)) * qty;
                    try {
                        writer.write(symbol + " | Qty: " + qty +
                                " | P/L: $" + String.format("%.2f", pl) + "\n");
                    } catch (IOException ignored) {}
                }
            });
            writer.write("------------\n");
            JOptionPane.showMessageDialog(this, "P/L history saved.");
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "File error.");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            TradingPlatformGUI gui = new TradingPlatformGUI();
            gui.setVisible(true);
        });
    }
}