package software.openex.gate.binary.order;

import java.math.BigDecimal;

import static software.openex.gate.binary.BinaryRepresentable.representationSize;

/**
 * @author Alireza Pourtaghi
 */
public abstract sealed class LimitOrder extends Order implements Comparable<LimitOrder> permits
        BuyLimitOrder, SellLimitOrder, StopLimitOrder {

    private final String price;
    private final BigDecimal _price;

    public LimitOrder(final long id, final long ts, final String symbol, final String quantity, final String price) {
        this(id, ts, symbol, quantity, quantity, price);
    }

    public LimitOrder(final long id, final long ts, final String symbol, final String quantity, final String remaining,
                      final String price) {

        super(id, ts, symbol, quantity, remaining);
        this.price = price == null ? "" : price;
        this._price = new BigDecimal(this.price);
    }

    @Override
    public int size() {
        return super.size() + representationSize(price);
    }

    public final String getPrice() {
        return price;
    }

    public final BigDecimal get_price() {
        return _price;
    }

    @Override
    public String toString() {
        return "LimitOrder{" +
                "price='" + price + '\'' +
                ", _price=" + _price +
                "} " + super.toString();
    }
}
