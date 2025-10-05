/*
 * ISC License
 *
 * Copyright (c) 2025, Alireza Pourtaghi <lirezap@protonmail.com>
 *
 * Permission to use, copy, modify, and/or distribute this software for any
 * purpose with or without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 *
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
 * ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
 * OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 */
package software.openex.gate.binary.gl.transaction;

import software.openex.gate.binary.BinaryRepresentation;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;

/**
 * @author Alireza Pourtaghi
 */
public final class FailedTransactionBinaryRepresentation extends BinaryRepresentation<FailedTransaction> {
    private final FailedTransaction failedTransaction;

    public FailedTransactionBinaryRepresentation(final FailedTransaction failedTransaction) {
        super(failedTransaction.size());
        this.failedTransaction = failedTransaction;
    }

    public FailedTransactionBinaryRepresentation(final Arena arena, final FailedTransaction failedTransaction) {
        super(arena, failedTransaction.size());
        this.failedTransaction = failedTransaction;
    }

    @Override
    protected int id() {
        return 204;
    }

    @Override
    protected void encodeRecord() {
        try {
            putString(failedTransaction.getId());
            putString(failedTransaction.getReason());
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public static FailedTransaction decode(final MemorySegment segment) {
        long position = RHS;

        final var idSize = segment.get(INT, position);
        position += INT.byteSize();

        final var id = segment.getString(position);
        position += idSize;

        final var reasonSize = segment.get(INT, position);
        position += INT.byteSize();

        final var reason = segment.getString(position);

        return new FailedTransaction(id, reason);
    }
}
