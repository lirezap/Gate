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
public sealed class BatchBinaryRepresentation extends BinaryRepresentation<Batch>
        permits AtomicBatchBinaryRepresentation {

    private final Batch batch;

    public BatchBinaryRepresentation(final Batch batch) {
        super(batch.size());
        this.batch = batch;
    }

    public BatchBinaryRepresentation(final Arena arena, final Batch batch) {
        super(arena, batch.size());
        this.batch = batch;
    }

    @Override
    protected int id() {
        return 202;
    }

    @Override
    protected void encodeRecord() {
        try {
            putBinaryRepresentations(batch.getTransactions());
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public static Transaction[] transactions(final MemorySegment segment) {
        long position = RHS;

        final var batchSize = segment.get(INT, position);
        position += INT.byteSize();

        final var transactions = new Transaction[batchSize];
        for (int i = 0; i < batchSize; i++) {
            final var size = RHS + BinaryRepresentable.size(segment.asSlice(position));
            transactions[i] = TransactionBinaryRepresentation.decode(segment.asSlice(position, size));
            position += size;
        }

        return transactions;
    }
}
