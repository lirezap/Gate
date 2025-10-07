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
package software.openex.gate.handlers;

import io.vertx.ext.web.RoutingContext;

/**
 * Real IP detector handler implementation.
 *
 * @author Alireza Pourtaghi
 */
public final class RealIPDetectorHandler extends HTTPHandler {

    @Override
    public void handle(final RoutingContext routingContext) {
        // If ip was not set, suppose it's from localhost.
        var ip = "192.168.1.1";

        final var xRealIp = routingContext.request().getHeader(X_REAL_IP);
        if (xRealIp != null && !xRealIp.isBlank()) {
            ip = xRealIp;
        }

        final var xForwardedFor = routingContext.request().getHeader(X_FORWARDED_FOR);
        if (xForwardedFor != null && !xForwardedFor.isBlank()) {
            ip = xForwardedFor.split(",")[0];
        }

        routingContext.request().headers().remove(X_REAL_IP);
        routingContext.request().headers().add(X_REAL_IP, ip);
        routingContext.next();
    }
}
