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
import io.vertx.core.json.JsonArray;
import io.vertx.ext.web.RoutingContext;
import software.openex.gate.binary.BinaryRepresentation;
import software.openex.gate.binary.base.ErrorMessageBinaryRepresentation;
import software.openex.gate.binary.gl.transaction.*;
import software.openex.gate.binary.gl.wallet.*;
import software.openex.gate.exceptions.ConnectionClosedException;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Optional;
import java.util.concurrent.TimeoutException;

import static io.netty.handler.codec.http.HttpResponseStatus.PRECONDITION_FAILED;
import static io.vertx.core.json.JsonObject.mapFrom;
import static java.lang.Integer.parseInt;
import static java.lang.String.format;
import static java.lang.foreign.Arena.ofConfined;
import static java.util.Optional.empty;
import static java.util.Optional.of;
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
                case 202 -> submitBatch(routingContext);
                case 203 -> submitAtomicBatch(routingContext);
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

            if (context().signatureVerifier() != null) {
                var content = format("%s,%s", model.getLedger(), model.getAccount());
                var signature = body.getString("signature", "");
                if (!context().signatureVerifier().verify(content, signature)) {
                    SIGNATURE_VERIFICATION_FAILED.send(routingContext);
                    return;
                }
            }

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

            if (context().signatureVerifier() != null) {
                var content = format("%s,%s,%s", model.getLedger(), model.getAccount(), model.getWallet());
                var signature = body.getString("signature", "");
                if (!context().signatureVerifier().verify(content, signature)) {
                    SIGNATURE_VERIFICATION_FAILED.send(routingContext);
                    return;
                }
            }

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

    private void submitBatch(final RoutingContext routingContext) {
        context().executors().worker().submit(() -> {
            final var body = routingContext.body().asJsonObject();
            final var transactions = body.getJsonArray("transactions", new JsonArray());

            try (final var arena = ofConfined()) {
                final var batch = new ArrayList<BinaryRepresentation<Transaction>>(transactions.size());
                for (int i = 0; i < transactions.size(); i++) {
                    final var transaction = transactions.getJsonObject(i);
                    final var batchItem = new Transaction(
                            transaction.getInteger("ledger", 0),
                            transaction.getLong("sourceAccount", 0L),
                            transaction.getInteger("sourceWallet", 0),
                            transaction.getLong("destinationAccount", 0L),
                            transaction.getInteger("destinationWallet", 0),
                            transaction.getString("id", ""),
                            transaction.getString("currency", ""),
                            transaction.getLong("amount", 0L),
                            transaction.getLong("maxOverdraftAmount", 0L),
                            transaction.getString("metadata", ""));

                    if (context().signatureVerifier() != null) {
                        var content = format("%s,%s,%s,%s,%s,%s,%s",
                                batchItem.getLedger(),
                                batchItem.getSourceAccount(),
                                batchItem.getSourceWallet(),
                                batchItem.getDestinationAccount(),
                                batchItem.getDestinationWallet(),
                                batchItem.getCurrency(),
                                batchItem.getAmount());

                        var signature = transaction.getString("signature", "");
                        if (!context().signatureVerifier().verify(content, signature)) {
                            SIGNATURE_VERIFICATION_FAILED.send(routingContext);
                            return;
                        }
                    }

                    final var batchItemBinaryRepresentation = new TransactionBinaryRepresentation(arena, batchItem);
                    batchItemBinaryRepresentation.encodeV1();
                    batch.add(batchItemBinaryRepresentation);
                }

                final var model = new Batch(batch);
                final var message = new BatchBinaryRepresentation(arena, model);
                message.encodeV1();

                final var result = submit(routingContext, arena, message.segment());
                if (result.isPresent()) {
                    routingContext.put(RESPONSE_BODY, FailedTransactionsBinaryRepresentation.items(result.get()));
                    routingContext.next();
                }
            }
        });
    }

    private void submitAtomicBatch(final RoutingContext routingContext) {
        context().executors().worker().submit(() -> {
            final var body = routingContext.body().asJsonObject();
            final var transactions = body.getJsonArray("transactions", new JsonArray());

            try (final var arena = ofConfined()) {
                final var batch = new ArrayList<BinaryRepresentation<Transaction>>(transactions.size());
                for (int i = 0; i < transactions.size(); i++) {
                    final var transaction = transactions.getJsonObject(i);
                    final var batchItem = new Transaction(
                            transaction.getInteger("ledger", 0),
                            transaction.getLong("sourceAccount", 0L),
                            transaction.getInteger("sourceWallet", 0),
                            transaction.getLong("destinationAccount", 0L),
                            transaction.getInteger("destinationWallet", 0),
                            transaction.getString("id", ""),
                            transaction.getString("currency", ""),
                            transaction.getLong("amount", 0L),
                            transaction.getLong("maxOverdraftAmount", 0L),
                            transaction.getString("metadata", ""));

                    if (context().signatureVerifier() != null) {
                        var content = format("%s,%s,%s,%s,%s,%s,%s",
                                batchItem.getLedger(),
                                batchItem.getSourceAccount(),
                                batchItem.getSourceWallet(),
                                batchItem.getDestinationAccount(),
                                batchItem.getDestinationWallet(),
                                batchItem.getCurrency(),
                                batchItem.getAmount());

                        var signature = transaction.getString("signature", "");
                        if (!context().signatureVerifier().verify(content, signature)) {
                            SIGNATURE_VERIFICATION_FAILED.send(routingContext);
                            return;
                        }
                    }

                    final var batchItemBinaryRepresentation = new TransactionBinaryRepresentation(arena, batchItem);
                    batchItemBinaryRepresentation.encodeV1();
                    batch.add(batchItemBinaryRepresentation);
                }

                final var model = new Batch(batch);
                final var message = new AtomicBatchBinaryRepresentation(arena, model);
                message.encodeV1();

                final var result = submit(routingContext, arena, message.segment());
                if (result.isPresent()) {
                    routingContext.put(RESPONSE_BODY, FailedTransactionsBinaryRepresentation.items(result.get()));
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

            if (context().signatureVerifier() != null) {
                var content = format("%s,%s", model.getLedger(), model.getId());
                var signature = body.getString("signature", "");
                if (!context().signatureVerifier().verify(content, signature)) {
                    SIGNATURE_VERIFICATION_FAILED.send(routingContext);
                    return;
                }
            }

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

    private Optional<MemorySegment> submit(final RoutingContext routingContext, final Arena arena,
                                           final MemorySegment message) {

        try {
            final var result = context().gl().send(arena, message);
            if (id(result) == -1) {
                error(routingContext, result);
            } else {
                return of(result);
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

        return empty();
    }

    private void error(final RoutingContext routingContext, final MemorySegment result) {
        final var errorMessage = ErrorMessageBinaryRepresentation.decode(result);
        routingContext.response()
                .putHeader(HttpHeaders.CONTENT_TYPE, "application/json")
                .setStatusCode(PRECONDITION_FAILED.code())
                .end(mapFrom(errorMessage).toBuffer());
    }
}
