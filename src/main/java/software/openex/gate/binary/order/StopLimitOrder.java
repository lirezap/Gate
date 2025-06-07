package software.openex.gate.binary.order;

import java.math.BigDecimal;

import static software.openex.gate.binary.BinaryRepresentable.representationSize;

/**
 * @author Alireza Pourtaghi
 */
public abstract sealed class StopLimitOrder extends LimitOrder permits BuyStopLimitOrder, SellStopLimitOrder {
    private final String stopPrice;
    private final BigDecimal _stopPrice;

    public StopLimitOrder(final long id, final long ts, final String symbol, final String quantity, final String price,
                          final String stopPrice) {

        super(id, ts, symbol, quantity, price);
        this.stopPrice = stopPrice == null ? "" : stopPrice;
        this._stopPrice = new BigDecimal(this.stopPrice);
    }

    @Override
    public int size() {
        return super.size() + representationSize(stopPrice);
    }

    public String getStopPrice() {
        return stopPrice;
    }

    public BigDecimal get_stopPrice() {
        return _stopPrice;
    }
}
