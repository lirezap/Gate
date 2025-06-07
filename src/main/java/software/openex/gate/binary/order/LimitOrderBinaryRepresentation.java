package software.openex.gate.binary.order;

import software.openex.gate.binary.BinaryRepresentation;

import java.lang.foreign.Arena;

/**
 * @author Alireza Pourtaghi
 */
public final class LimitOrderBinaryRepresentation extends BinaryRepresentation<LimitOrder> {
    private final LimitOrder limitOrder;

    public LimitOrderBinaryRepresentation(final LimitOrder limitOrder) {
        super(limitOrder.size());
        this.limitOrder = limitOrder;
    }

    public LimitOrderBinaryRepresentation(final Arena arena, final LimitOrder limitOrder) {
        super(arena, limitOrder.size());
        this.limitOrder = limitOrder;
    }

    @Override
    protected int id() {
        return limitOrder.representationId();
    }

    @Override
    protected void encodeRecord() {
        try {
            putLong(limitOrder.getId());
            putLong(limitOrder.getTs());
            putString(limitOrder.getSymbol());
            putString(limitOrder.getQuantity());
            putString(limitOrder.getPrice());
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}
