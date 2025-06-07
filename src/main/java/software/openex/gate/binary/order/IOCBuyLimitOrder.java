package software.openex.gate.binary.order;

/**
 * @author Alireza Pourtaghi
 */
public sealed class IOCBuyLimitOrder extends BuyLimitOrder permits FOKBuyLimitOrder {

    public IOCBuyLimitOrder(final long id, final long ts, final String symbol, final String quantity,
                            final String price) {

        this(id, ts, symbol, quantity, quantity, price);
    }

    public IOCBuyLimitOrder(final long id, final long ts, final String symbol, final String quantity,
                            final String remaining, final String price) {

        super(id, ts, symbol, quantity, remaining, price);
    }

    @Override
    public int representationId() {
        return 109;
    }
}
