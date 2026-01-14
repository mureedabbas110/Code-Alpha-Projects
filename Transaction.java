class Transaction {
    String type;
    String symbol;
    double price;

    public Transaction(String type, String symbol, double price) {
        this.type = type;
        this.symbol = symbol;
        this.price = price;
    }

    @Override
    public String toString() {
        return type + " - " + symbol + " @ $" + price;
    }
}
