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

import software.openex.gate.binary.BinaryRepresentable;
import software.openex.gate.binary.BinaryRepresentation;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;

/**
 * @author Alireza Pourtaghi
 */
public final class FailedTransactionsBinaryRepresentation extends BinaryRepresentation<FailedTransactions> {
    private final FailedTransactions failedTransactions;

    public FailedTransactionsBinaryRepresentation(final FailedTransactions failedTransactions) {
        super(failedTransactions.size());
        this.failedTransactions = failedTransactions;
    }

    public FailedTransactionsBinaryRepresentation(final Arena arena, final FailedTransactions failedTransactions) {
        super(arena, failedTransactions.size());
        this.failedTransactions = failedTransactions;
    }

    @Override
    protected int id() {
        return 205;
    }

    @Override
    protected void encodeRecord() {
        try {
            putBinaryRepresentations(failedTransactions.getItems());
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public static FailedTransaction[] items(final MemorySegment segment) {
        long position = RHS;

        final var itemsSize = segment.get(INT, position);
        position += INT.byteSize();

        final var items = new FailedTransaction[itemsSize];
        for (int i = 0; i < itemsSize; i++) {
            final var size = RHS + BinaryRepresentable.size(segment.asSlice(position));
            items[i] = FailedTransactionBinaryRepresentation.decode(segment.asSlice(position, size));
            position += size;
        }

        return items;
    }
}
