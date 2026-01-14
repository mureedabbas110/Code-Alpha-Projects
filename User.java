import java.util.HashMap;
import java.util.Map;

class User {
    double balance = 10000.0;

    Map<String, Integer> portfolio = new HashMap<>();
    Map<String, Double> avgBuyPrice = new HashMap<>();

    void buy(String symbol, double price) {
        balance -= price;

        int qty = portfolio.getOrDefault(symbol, 0);
        double oldAvg = avgBuyPrice.getOrDefault(symbol, 0.0);

        double newAvg = ((oldAvg * qty) + price) / (qty + 1);

        portfolio.put(symbol, qty + 1);
        avgBuyPrice.put(symbol, newAvg);
    }

    void sell(String symbol, double price) {
        balance += price;
        portfolio.put(symbol, portfolio.get(symbol) - 1);
    }
}
