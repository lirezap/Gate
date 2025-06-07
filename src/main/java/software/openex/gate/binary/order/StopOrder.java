package software.openex.gate.binary.order;

import java.math.BigDecimal;

import static software.openex.gate.binary.BinaryRepresentable.representationSize;

/**
 * @author Alireza Pourtaghi
 */
public abstract sealed class StopOrder extends MarketOrder permits BuyStopOrder, SellStopOrder {
    private final String stopPrice;
    private final BigDecimal _stopPrice;

    public StopOrder(final long id, final long ts, final String symbol, final String quantity,
                     final String stopPrice) {

        super(id, ts, symbol, quantity);
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
