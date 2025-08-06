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

import io.vertx.core.json.Json;
import io.vertx.ext.web.RoutingContext;

import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.vertx.core.http.HttpHeaders.CONTENT_TYPE;
import static software.openex.gate.handlers.Error.SERVER_ERROR;

/**
 * Json body responder handler implementation.
 *
 * @author Alireza Pourtaghi
 */
public final class JsonBodyResponderHandler extends HTTPHandler {

    @Override
    public void handle(final RoutingContext routingContext) {
        try {
            routingContext.response().putHeader(X_FRAME_OPTIONS, "nosniff");
            routingContext.response().putHeader(CACHE_CONTROL, "no-store");

            final var response = Json.encodeToBuffer(routingContext.get(RESPONSE_BODY));
            routingContext.response()
                    .setStatusCode(OK.code())
                    .putHeader(CONTENT_TYPE, "application/json")
                    .end(response);
        } catch (RuntimeException ex) {
            logger.error("{}", ex.getMessage());
            SERVER_ERROR.send(routingContext);
        }
    }
}
