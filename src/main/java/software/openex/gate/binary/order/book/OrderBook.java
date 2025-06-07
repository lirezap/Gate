package software.openex.gate.binary.order.book;

import software.openex.gate.binary.BinaryRepresentation;
import software.openex.gate.binary.order.LimitOrder;

import java.util.List;

import static java.util.Collections.emptyList;
import static software.openex.gate.binary.BinaryRepresentable.representationSize;

/**
 * @author Alireza Pourtaghi
 */
public final class OrderBook {
    private final List<BinaryRepresentation<LimitOrder>> bids;
    private final List<BinaryRepresentation<LimitOrder>> asks;

    public OrderBook(final List<BinaryRepresentation<LimitOrder>> bids,
                     final List<BinaryRepresentation<LimitOrder>> asks) {

        this.bids = bids == null ? emptyList() : bids;
        this.asks = asks == null ? emptyList() : asks;
    }

    public int size() {
        return representationSize(bids) + representationSize(asks);
    }

    public List<BinaryRepresentation<LimitOrder>> getBids() {
        return bids;
    }

    public List<BinaryRepresentation<LimitOrder>> getAsks() {
        return asks;
    }
}
