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

import java.util.List;

import static java.util.Collections.emptyList;
import static software.openex.gate.binary.BinaryRepresentable.representationSize;

/**
 * @author Alireza Pourtaghi
 */
public final class Batch {
    private final List<BinaryRepresentation<Transaction>> transactions;

    public Batch(final List<BinaryRepresentation<Transaction>> transactions) {
        this.transactions = transactions == null ? emptyList() : transactions;
    }

    public int size() {
        return representationSize(transactions);
    }

    public List<BinaryRepresentation<Transaction>> getTransactions() {
        return transactions;
    }
}
