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
import io.vertx.ext.web.handler.LoggerFormatter;

import static io.vertx.ext.web.impl.Utils.formatRFC1123DateTime;

/**
 * Request logging formatter to be able to log incoming HTTP requests.
 *
 * @author Alireza Pourtaghi
 */
public final class RequestLoggingFormatter implements LoggerFormatter {

    @Override
    public String format(final RoutingContext routingContext, final long responseTime) {
        final var ip = routingContext.request().getHeader("x-real-ip");
        final var method = routingContext.request().method().name();
        final var uri = routingContext.request().uri();
        final var version = routingContext.request().version().name();
        final var referrer = routingContext.request().getHeader("referrer");
        final var userAgent = routingContext.request().getHeader("user-agent");
        final var trackId = routingContext.request().getHeader("x-track-id");
        final var responseStatusCode = routingContext.response().getStatusCode();

        return String.format("%s | %s | %s %s %s | referrer: %s | user-agent: %s | x-track-id: %s | response-status-code: %s | response-time: %s",
                ip,
                formatRFC1123DateTime(System.currentTimeMillis()),
                method,
                uri,
                version,
                referrer,
                userAgent,
                trackId,
                responseStatusCode,
                responseTime);
    }
}
