package software.openex.gate.binary.base;

import java.lang.foreign.MemorySegment;

import static software.openex.gate.binary.BinaryRepresentable.*;

/**
 * @author Alireza Pourtaghi
 */
public final class ErrorMessage {
    private final String code;
    private final String message;

    public ErrorMessage(final String code, final String message) {
        this.code = code == null ? "" : code;
        this.message = message == null ? "" : message;
    }

    public int size() {
        return representationSize(code) + representationSize(message);
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
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
