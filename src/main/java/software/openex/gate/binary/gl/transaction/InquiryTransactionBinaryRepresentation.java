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
public final class InquiryTransactionBinaryRepresentation extends BinaryRepresentation<InquiryTransaction> {
    private final InquiryTransaction inquiryTransaction;

    public InquiryTransactionBinaryRepresentation(final InquiryTransaction inquiryTransaction) {
        super(inquiryTransaction.size());
        this.inquiryTransaction = inquiryTransaction;
    }

    public InquiryTransactionBinaryRepresentation(final Arena arena, final InquiryTransaction inquiryTransaction) {
        super(arena, inquiryTransaction.size());
        this.inquiryTransaction = inquiryTransaction;
    }

    @Override
    protected int id() {
        return 206;
    }

    @Override
    protected void encodeRecord() {
        try {
            putInt(inquiryTransaction.getLedger());
            putString(inquiryTransaction.getId());
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public static InquiryTransaction decode(final MemorySegment segment) {
        long position = RHS;

        final var ledger = segment.get(INT, position);
        position += INT.byteSize();

        final var idSize = segment.get(INT, position);
        position += INT.byteSize();

        final var id = segment.getString(position);

        return new InquiryTransaction(ledger, id);
    }
}
