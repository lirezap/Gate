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
package software.openex.gate.binary.oms.order;

import software.openex.gate.binary.BinaryRepresentation;

import java.lang.foreign.Arena;

/**
 * @author Alireza Pourtaghi
 */
public final class OrderBinaryRepresentation extends BinaryRepresentation<Order> {
    private final Order order;

    public OrderBinaryRepresentation(final Order order) {
        super(order.size());
        this.order = order;
    }

    public OrderBinaryRepresentation(final Arena arena, final Order order) {
        super(arena, order.size());
        this.order = order;
    }

    @Override
    protected int id() {
        return order.representationId();
    }

    @Override
    protected void encodeRecord() {
        try {
            putLong(order.getId());
            putLong(order.getTs());
            putString(order.getSymbol());
            putString(order.getQuantity());
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}
