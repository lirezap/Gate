package software.openex.gate.binary.order;

import software.openex.gate.binary.BinaryRepresentation;

import java.lang.foreign.Arena;

/**
 * @author Alireza Pourtaghi
 */
public final class StopOrderBinaryRepresentation extends BinaryRepresentation<StopOrder> {
    private final StopOrder stopOrder;

    public StopOrderBinaryRepresentation(final StopOrder stopOrder) {
        super(stopOrder.size());
        this.stopOrder = stopOrder;
    }

    public StopOrderBinaryRepresentation(final Arena arena, final StopOrder stopOrder) {
        super(arena, stopOrder.size());
        this.stopOrder = stopOrder;
    }

    @Override
    protected int id() {
        return stopOrder.representationId();
    }

    @Override
    protected void encodeRecord() {
        try {
            putLong(stopOrder.getId());
            putLong(stopOrder.getTs());
            putString(stopOrder.getSymbol());
            putString(stopOrder.getQuantity());
            putString(stopOrder.getStopPrice());
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}
