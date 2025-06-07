package software.openex.gate.binary.order;

import software.openex.gate.binary.BinaryRepresentation;

import java.lang.foreign.Arena;

/**
 * @author Alireza Pourtaghi
 */
public final class StopLimitOrderBinaryRepresentation extends BinaryRepresentation<StopLimitOrder> {
    private final StopLimitOrder stopLimitOrder;

    public StopLimitOrderBinaryRepresentation(final StopLimitOrder stopLimitOrder) {
        super(stopLimitOrder.size());
        this.stopLimitOrder = stopLimitOrder;
    }

    public StopLimitOrderBinaryRepresentation(final Arena arena, final StopLimitOrder stopLimitOrder) {
        super(arena, stopLimitOrder.size());
        this.stopLimitOrder = stopLimitOrder;
    }

    @Override
    protected int id() {
        return stopLimitOrder.representationId();
    }

    @Override
    protected void encodeRecord() {
        try {
            putLong(stopLimitOrder.getId());
            putLong(stopLimitOrder.getTs());
            putString(stopLimitOrder.getSymbol());
            putString(stopLimitOrder.getQuantity());
            putString(stopLimitOrder.getPrice());
            putString(stopLimitOrder.getStopPrice());
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}
