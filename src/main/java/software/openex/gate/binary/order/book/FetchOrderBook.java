package software.openex.gate.binary.order.book;

import static software.openex.gate.binary.BinaryRepresentable.representationSize;

/**
 * @author Alireza Pourtaghi
 */
public final class FetchOrderBook {
    private final String symbol;
    private final int fetchSize;

    public FetchOrderBook(final String symbol, final int fetchSize) {
        this.symbol = symbol == null ? "" : symbol;
        this.fetchSize = fetchSize;
    }

    public int size() {
        return representationSize(symbol) + 4;
    }

    public String getSymbol() {
        return symbol;
    }

    public int getFetchSize() {
        return fetchSize;
    }
}
