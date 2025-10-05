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
package software.openex.gate.binary.gl.wallet;

import software.openex.gate.binary.BinaryRepresentation;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;

/**
 * @author Alireza Pourtaghi
 */
public final class FetchAccountBinaryRepresentation extends BinaryRepresentation<FetchAccount> {
    private final FetchAccount fetchAccount;

    public FetchAccountBinaryRepresentation(final FetchAccount fetchAccount) {
        super(fetchAccount.size());
        this.fetchAccount = fetchAccount;
    }

    public FetchAccountBinaryRepresentation(final Arena arena, final FetchAccount fetchAccount) {
        super(arena, fetchAccount.size());
        this.fetchAccount = fetchAccount;
    }

    @Override
    protected int id() {
        return 103;
    }

    @Override
    protected void encodeRecord() {
        try {
            putInt(fetchAccount.getLedger());
            putLong(fetchAccount.getAccount());
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public static FetchAccount decode(final MemorySegment segment) {
        long position = RHS;

        final var ledger = segment.get(INT, position);
        position += INT.byteSize();

        final var account = segment.get(LONG, position);

        return new FetchAccount(ledger, account);
    }
}
