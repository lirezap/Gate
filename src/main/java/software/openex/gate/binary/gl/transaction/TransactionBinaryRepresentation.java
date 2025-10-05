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
public final class TransactionBinaryRepresentation extends BinaryRepresentation<Transaction> {
    private final Transaction transaction;

    public TransactionBinaryRepresentation(final Transaction transaction) {
        super(transaction.size());
        this.transaction = transaction;
    }

    public TransactionBinaryRepresentation(final Arena arena, final Transaction transaction) {
        super(arena, transaction.size());
        this.transaction = transaction;
    }

    @Override
    protected int id() {
        return 201;
    }

    @Override
    protected void encodeRecord() {
        try {
            putInt(transaction.getLedger());
            putLong(transaction.getSourceAccount());
            putInt(transaction.getSourceWallet());
            putLong(transaction.getDestinationAccount());
            putInt(transaction.getDestinationWallet());
            putString(transaction.getId());
            putString(transaction.getCurrency());
            putLong(transaction.getAmount());
            putLong(transaction.getMaxOverdraftAmount());
            putString(transaction.getMetadata());
            putLong(transaction.getSourceWalletNewBalance());
            putLong(transaction.getDestinationWalletNewBalance());
            putLong(transaction.getTs());
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public static Transaction decode(final MemorySegment segment) {
        long position = RHS;

        final var ledger = segment.get(INT, position);
        position += INT.byteSize();

        final var sourceAccount = segment.get(LONG, position);
        position += LONG.byteSize();

        final var sourceWallet = segment.get(INT, position);
        position += INT.byteSize();

        final var destinationAccount = segment.get(LONG, position);
        position += LONG.byteSize();

        final var destinationWallet = segment.get(INT, position);
        position += INT.byteSize();

        final var idSize = segment.get(INT, position);
        position += INT.byteSize();

        final var id = segment.getString(position);
        position += idSize;

        final var currencySize = segment.get(INT, position);
        position += INT.byteSize();

        final var currency = segment.getString(position);
        position += currencySize;

        final var amount = segment.get(LONG, position);
        position += LONG.byteSize();

        final var maxOverDraftAmount = segment.get(LONG, position);
        position += LONG.byteSize();

        final var metadataSize = segment.get(INT, position);
        position += INT.byteSize();

        final var metadata = segment.getString(position);
        position += metadataSize;

        final var sourceWalletNewBalance = segment.get(LONG, position);
        position += LONG.byteSize();

        final var destinationWalletNewBalance = segment.get(LONG, position);
        position += LONG.byteSize();

        final var ts = segment.get(LONG, position);

        return new Transaction(
                ledger,
                sourceAccount,
                sourceWallet,
                destinationAccount,
                destinationWallet,
                id,
                currency,
                amount,
                maxOverDraftAmount,
                metadata,
                sourceWalletNewBalance,
                destinationWalletNewBalance,
                ts);
    }
}
