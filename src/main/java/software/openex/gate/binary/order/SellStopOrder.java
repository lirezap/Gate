package software.openex.gate.binary.order;

import java.lang.foreign.MemorySegment;

import static software.openex.gate.binary.BinaryRepresentable.*;

/**
 * @author Alireza Pourtaghi
 */
public final class SellStopOrder extends StopOrder {

    public SellStopOrder(final long id, final long ts, final String symbol, final String quantity,
                         final String stopPrice) {

        super(id, ts, symbol, quantity, stopPrice);
    }

    @Override
    public int representationId() {
        return 116;
    }

    public static SellStopOrder decode(final MemorySegment segment) {
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

        final var stopPriceSize = segment.get(INT, position);
        position += INT.byteSize();

        final var stopPrice = segment.getString(position);

        return new SellStopOrder(id, ts, symbol, quantity, stopPrice);
    }
}
