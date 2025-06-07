package software.openex.gate.binary.order;

/**
 * @author Alireza Pourtaghi
 */
public final class FOKBuyMarketOrder extends BuyMarketOrder {

    public FOKBuyMarketOrder(final long id, final long ts, final String symbol, final String quantity) {
        super(id, ts, symbol, quantity);
    }

    @Override
    public int representationId() {
        return 113;
    }
}
