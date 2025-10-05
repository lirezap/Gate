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

import static java.lang.Math.addExact;
import static software.openex.gate.binary.BinaryRepresentable.representationSize;

/**
 * @author Alireza Pourtaghi
 */
public final class FailedTransaction {
    private final String id;
    private final String reason;

    public FailedTransaction(final String id, final String reason) {
        this.id = id == null ? "" : id;
        this.reason = reason == null ? "" : reason;
    }

    public int size() {
        return addExact(representationSize(id), representationSize(reason));
    }

    public String getId() {
        return id;
    }

    public String getReason() {
        return reason;
    }
}
