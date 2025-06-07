package software.openex.gate.binary.order;

import java.lang.foreign.MemorySegment;

import static software.openex.gate.binary.BinaryRepresentable.*;

/**
 * @author Alireza Pourtaghi
 */
public sealed class SellMarketOrder extends MarketOrder permits FOKSellMarketOrder {

    public SellMarketOrder(final long id, final long ts, final String symbol, final String quantity) {
        super(id, ts, symbol, quantity);
    }

    @Override
    public int representationId() {
        return 108;
    }

    public static SellMarketOrder decode(final MemorySegment segment) {
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

        return new SellMarketOrder(id, ts, symbol, quantity);
    }
}
