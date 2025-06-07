package software.openex.gate.binary.order.book;

import software.openex.gate.binary.BinaryRepresentation;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;

/**
 * @author Alireza Pourtaghi
 */
public final class FetchOrderBookBinaryRepresentation extends BinaryRepresentation<FetchOrderBook> {
    private final FetchOrderBook fetchOrderBook;

    public FetchOrderBookBinaryRepresentation(final FetchOrderBook fetchOrderBook) {
        super(fetchOrderBook.size());
        this.fetchOrderBook = fetchOrderBook;
    }

    public FetchOrderBookBinaryRepresentation(final Arena arena, final FetchOrderBook fetchOrderBook) {
        super(arena, fetchOrderBook.size());
        this.fetchOrderBook = fetchOrderBook;
    }

    @Override
    protected int id() {
        return 105;
    }

    @Override
    protected void encodeRecord() {
        try {
            putString(fetchOrderBook.getSymbol());
            putInt(fetchOrderBook.getFetchSize());
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public static FetchOrderBook decode(final MemorySegment segment) {
        long position = RHS;

        final var symbolSize = segment.get(INT, position);
        position += INT.byteSize();

        final var symbol = segment.getString(position);
        position += symbolSize;

        final var fetchSize = segment.get(INT, position);

        return new FetchOrderBook(symbol, fetchSize);
    }
}
