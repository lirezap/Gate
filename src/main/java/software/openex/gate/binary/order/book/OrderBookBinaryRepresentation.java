package software.openex.gate.binary.order.book;

import software.openex.gate.binary.BinaryRepresentable;
import software.openex.gate.binary.BinaryRepresentation;
import software.openex.gate.binary.order.BuyLimitOrder;
import software.openex.gate.binary.order.SellLimitOrder;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Alireza Pourtaghi
 */
public final class OrderBookBinaryRepresentation extends BinaryRepresentation<OrderBook> {
    private final OrderBook orderBook;

    public OrderBookBinaryRepresentation(final OrderBook orderBook) {
        super(orderBook.size());
        this.orderBook = orderBook;
    }

    public OrderBookBinaryRepresentation(final Arena arena, final OrderBook orderBook) {
        super(arena, orderBook.size());
        this.orderBook = orderBook;
    }

    @Override
    protected int id() {
        return 106;
    }

    @Override
    protected void encodeRecord() {
        try {
            putBinaryRepresentations(orderBook.getBids());
            putBinaryRepresentations(orderBook.getAsks());
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public static List<BuyLimitOrder> bids(final MemorySegment segment) {
        long position = RHS;

        final var bidsSize = segment.get(INT, position);
        position += INT.byteSize();

        final var bids = new ArrayList<BuyLimitOrder>(bidsSize);
        for (int i = 1; i <= bidsSize; i++) {
            final var size = RHS + BinaryRepresentable.size(segment.asSlice(position));
            bids.add(BuyLimitOrder.decode(segment.asSlice(position, size)));
            position += size;
        }

        return bids;
    }

    public static List<SellLimitOrder> asks(final MemorySegment segment) {
        long position = RHS;

        final var bidsSize = segment.get(INT, position);
        position += INT.byteSize();
        for (int i = 1; i <= bidsSize; i++) {
            final var size = RHS + BinaryRepresentable.size(segment.asSlice(position));
            position += size;
        }

        final var asksSize = segment.get(INT, position);
        position += INT.byteSize();

        final var asks = new ArrayList<SellLimitOrder>(asksSize);
        for (int i = 1; i <= asksSize; i++) {
            final var size = RHS + BinaryRepresentable.size(segment.asSlice(position));
            asks.add(SellLimitOrder.decode(segment.asSlice(position, size)));
            position += size;
        }

        return asks;
    }
}
