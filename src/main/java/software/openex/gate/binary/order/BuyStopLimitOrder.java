package software.openex.gate.binary.order;

import java.lang.foreign.MemorySegment;

import static java.lang.Long.compare;
import static software.openex.gate.binary.BinaryRepresentable.*;

/**
 * @author Alireza Pourtaghi
 */
public final class BuyStopLimitOrder extends StopLimitOrder {

    public BuyStopLimitOrder(final long id, final long ts, final String symbol, final String quantity,
                             final String price, final String stopPrice) {

        super(id, ts, symbol, quantity, price, stopPrice);
    }

    @Override
    public int representationId() {
        return 117;
    }

    @Override
    public int compareTo(final LimitOrder o) {
        final var compare = get_price().compareTo(o.get_price());
        return compare == 0 ? compare(getTs(), o.getTs()) : -compare;
    }

    public static BuyStopLimitOrder decode(final MemorySegment segment) {
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
        position += priceSize;

        final var stopPriceSize = segment.getString(position);
        position += INT.byteSize();

        final var stopPrice = segment.getString(position);

        return new BuyStopLimitOrder(id, ts, symbol, quantity, price, stopPrice);
    }
}
