package software.openex.gate.binary.order;

/**
 * @author Alireza Pourtaghi
 */
public final class FOKBuyLimitOrder extends IOCBuyLimitOrder {

    public FOKBuyLimitOrder(final long id, final long ts, final String symbol, final String quantity,
                            final String price) {

        this(id, ts, symbol, quantity, quantity, price);
    }

    public FOKBuyLimitOrder(final long id, final long ts, final String symbol, final String quantity,
                            final String remaining, final String price) {

        super(id, ts, symbol, quantity, remaining, price);
    }

    @Override
    public int representationId() {
        return 111;
    }
}
