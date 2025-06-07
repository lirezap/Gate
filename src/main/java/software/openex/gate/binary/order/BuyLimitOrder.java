package software.openex.gate.binary.order;

import java.lang.foreign.MemorySegment;

import static java.lang.Long.compare;
import static software.openex.gate.binary.BinaryRepresentable.*;

/**
 * @author Alireza Pourtaghi
 */
public sealed class BuyLimitOrder extends LimitOrder permits IOCBuyLimitOrder {

    public BuyLimitOrder(final long id, final long ts, final String symbol, final String quantity, final String price) {
        this(id, ts, symbol, quantity, quantity, price);
    }

    public BuyLimitOrder(final long id, final long ts, final String symbol, final String quantity,
                         final String remaining, final String price) {

        super(id, ts, symbol, quantity, remaining, price);
    }

    @Override
    public int representationId() {
        return 101;
    }

    @Override
    public int compareTo(final LimitOrder o) {
        final var compare = get_price().compareTo(o.get_price());
        return compare == 0 ? compare(getTs(), o.getTs()) : -compare;
    }

    public static BuyLimitOrder decode(final MemorySegment segment) {
        long position = RHS;

        final var id = segment.get(LONG, position);
        position += LONG.byteSize();

        final var ts = segment.get(LONG, position);
        position += LONG.byteSize();

        final var symbolSize = segment.get(INT, position);
        position += INT.byteSize();

        final var symbol = segment.getString(position);
        position += symbolSize;

        final var quantitySize = segment.get(INT, position);
        position += INT.byteSize();

        final var quantity = segment.getString(position);
        position += quantitySize;

        final var priceSize = segment.get(INT, position);
        position += INT.byteSize();

        final var price = segment.getString(position);

        return new BuyLimitOrder(id, ts, symbol, quantity, price);
    }
}
