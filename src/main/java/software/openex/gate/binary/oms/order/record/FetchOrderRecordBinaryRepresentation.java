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
package software.openex.gate.binary.oms.order.record;

import software.openex.gate.binary.BinaryRepresentation;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;

/**
 * @author Alireza Pourtaghi
 */
public final class FetchOrderRecordBinaryRepresentation extends BinaryRepresentation<FetchOrderRecord> {
    private final FetchOrderRecord fetchOrderRecord;

    public FetchOrderRecordBinaryRepresentation(final FetchOrderRecord fetchOrderRecord) {
        super(fetchOrderRecord.size());
        this.fetchOrderRecord = fetchOrderRecord;
    }

    public FetchOrderRecordBinaryRepresentation(final Arena arena, final FetchOrderRecord fetchOrderRecord) {
        super(arena, fetchOrderRecord.size());
        this.fetchOrderRecord = fetchOrderRecord;
    }

    @Override
    protected int id() {
        return 119;
    }

    @Override
    protected void encodeRecord() {
        try {
            putString(fetchOrderRecord.getSymbol());
            putLong(fetchOrderRecord.getId());
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public static FetchOrderRecord decode(final MemorySegment segment) {
        long position = RHS;

        final var symbolSize = segment.get(INT, position);
        position += INT.byteSize();

        final var symbol = segment.getString(position);
        position += symbolSize;

        final var id = segment.get(LONG, position);

        return new FetchOrderRecord(symbol, id);
    }
}
