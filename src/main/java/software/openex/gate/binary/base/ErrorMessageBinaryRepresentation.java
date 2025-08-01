package software.openex.gate.binary.base;

import software.openex.gate.binary.BinaryRepresentation;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;

/**
 * @author Alireza Pourtaghi
 */
public final class ErrorMessageBinaryRepresentation extends BinaryRepresentation<ErrorMessage> {
    private final ErrorMessage errorMessage;

    public ErrorMessageBinaryRepresentation(final ErrorMessage errorMessage) {
        super(errorMessage.size());
        this.errorMessage = errorMessage;
    }

    public ErrorMessageBinaryRepresentation(final Arena arena, final ErrorMessage errorMessage) {
        super(arena, errorMessage.size());
        this.errorMessage = errorMessage;
    }

    @Override
    protected int id() {
        return -1;
    }

    @Override
    protected void encodeRecord() {
        try {
            putString(errorMessage.getCode());
            putString(errorMessage.getMessage());
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public static ErrorMessage decode(final MemorySegment segment) {
        long position = RHS;

        final var codeSize = segment.get(INT, position);
        position += INT.byteSize();

        final var code = segment.getString(position);
        position += codeSize;

        final var messageSize = segment.get(INT, position);
        position += INT.byteSize();

        final var message = segment.getString(position);

        return new ErrorMessage(code, message);
    }
}
