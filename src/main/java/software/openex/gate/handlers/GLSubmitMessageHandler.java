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

import io.vertx.core.http.HttpHeaders;
import io.vertx.ext.web.RoutingContext;
import software.openex.gate.binary.base.ErrorMessageBinaryRepresentation;
import software.openex.gate.binary.gl.transaction.InquiryTransaction;
import software.openex.gate.binary.gl.transaction.InquiryTransactionBinaryRepresentation;
import software.openex.gate.binary.gl.transaction.TransactionBinaryRepresentation;
import software.openex.gate.binary.gl.wallet.*;
import software.openex.gate.exceptions.ConnectionClosedException;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.Optional;
import java.util.concurrent.TimeoutException;

import static io.netty.handler.codec.http.HttpResponseStatus.PRECONDITION_FAILED;
import static io.vertx.core.json.JsonObject.mapFrom;
import static java.lang.Integer.parseInt;
import static java.lang.foreign.Arena.ofConfined;
import static software.openex.gate.binary.BinaryRepresentable.id;
import static software.openex.gate.context.AppContext.context;
import static software.openex.gate.handlers.Error.*;

/**
 * @author Alireza Pourtaghi
 */
public final class GLSubmitMessageHandler extends HTTPHandler {

    @Override
    public void handle(final RoutingContext routingContext) {
        try {
            final var id = parseInt(routingContext.request().getParam("id"));

            switch (id) {
                case 103 -> submitFetchAccount(routingContext);
                case 104 -> submitFetchWallet(routingContext);
                case 206 -> submitInquiryTransaction(routingContext);

                default -> HANDLER_NOT_FOUND.send(routingContext);
            }
        } catch (NumberFormatException ex) {
            logger.error("{}", ex.getMessage());
            ID_NOT_VALID.send(routingContext);
        } catch (RuntimeException ex) {
            logger.error("{}", ex.getMessage());
            SERVER_ERROR.send(routingContext);
        }
    }

    private void submitFetchAccount(final RoutingContext routingContext) {
        context().executors().worker().submit(() -> {
            final var body = routingContext.body().asJsonObject();
            final var model = new FetchAccount(
                    body.getInteger("ledger", 0),
                    body.getLong("account", 0L));

            try (final var arena = ofConfined()) {
                final var message = new FetchAccountBinaryRepresentation(arena, model);
                message.encodeV1();

                final var result = submit(routingContext, arena, message.segment());
                if (result.isPresent()) {
                    routingContext.put(RESPONSE_BODY, WalletsBinaryRepresentation.items(result.get()));
                    routingContext.next();
                }
            }
        });
    }

    private void submitFetchWallet(final RoutingContext routingContext) {
        context().executors().worker().submit(() -> {
            final var body = routingContext.body().asJsonObject();
            final var model = new FetchWallet(
                    body.getInteger("ledger", 0),
                    body.getLong("account", 0L),
                    body.getInteger("wallet", 0));

            try (final var arena = ofConfined()) {
                final var message = new FetchWalletBinaryRepresentation(arena, model);
                message.encodeV1();

                final var result = submit(routingContext, arena, message.segment());
                if (result.isPresent()) {
                    routingContext.put(RESPONSE_BODY, WalletBinaryRepresentation.decode(result.get()));
                    routingContext.next();
                }
            }
        });
    }

    private void submitInquiryTransaction(final RoutingContext routingContext) {
        context().executors().worker().submit(() -> {
            final var body = routingContext.body().asJsonObject();
            final var model = new InquiryTransaction(
                    body.getInteger("ledger", 0),
                    body.getString("id", ""));

            try (final var arena = ofConfined()) {
                final var message = new InquiryTransactionBinaryRepresentation(arena, model);
                message.encodeV1();

                final var result = submit(routingContext, arena, message.segment());
                if (result.isPresent()) {
                    routingContext.put(RESPONSE_BODY, TransactionBinaryRepresentation.decode(result.get()));
                    routingContext.next();
                }
            }
        });
    }

    private Optional<MemorySegment> submit(final RoutingContext routingContext, final Arena arena, final MemorySegment message) {
        try {
            final var result = context().gl().send(arena, message);
            if (id(result) == -1) {
                error(routingContext, result);
            } else {
                return Optional.of(result);
            }
        } catch (TimeoutException ex) {
            GL_CONNECT_TIMEOUT.send(routingContext);
        } catch (SocketTimeoutException ex) {
            GL_REQUEST_TIMEOUT.send(routingContext);
        } catch (ConnectionClosedException | SocketException ex) {
            GL_NOT_REACHABLE.send(routingContext);
        } catch (Exception ex) {
            logger.error("{}", ex.getMessage());
            SERVER_ERROR.send(routingContext);
        }

        return Optional.empty();
    }

    private void error(final RoutingContext routingContext, final MemorySegment result) {
        final var errorMessage = ErrorMessageBinaryRepresentation.decode(result);
        routingContext.response()
                .putHeader(HttpHeaders.CONTENT_TYPE, "application/json")
                .setStatusCode(PRECONDITION_FAILED.code())
                .end(mapFrom(errorMessage).toBuffer());
    }
}
