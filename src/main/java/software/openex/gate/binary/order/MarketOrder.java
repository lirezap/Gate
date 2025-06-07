package software.openex.gate.binary.order;

/**
 * @author Alireza Pourtaghi
 */
public abstract sealed class MarketOrder extends Order permits BuyMarketOrder, SellMarketOrder, StopOrder {

    public MarketOrder(final long id, final long ts, final String symbol, final String quantity) {
        super(id, ts, symbol, quantity);
    }
}
