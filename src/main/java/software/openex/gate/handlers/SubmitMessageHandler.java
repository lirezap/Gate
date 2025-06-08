package software.openex.gate.handlers;

import io.vertx.core.http.HttpHeaders;
import io.vertx.ext.web.RoutingContext;
import software.openex.gate.binary.base.ErrorMessage;
import software.openex.gate.binary.order.BuyLimitOrder;
import software.openex.gate.binary.order.LimitOrderBinaryRepresentation;
import software.openex.gate.exceptions.ConnectionClosedException;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.net.ConnectException;
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
public final class SubmitMessageHandler extends HTTPHandler {

    @Override
    public void handle(final RoutingContext routingContext) {
        try {
            final var id = parseInt(routingContext.request().getParam("id"));

            switch (id) {
                case 101 -> submitBuyLimitOrder(routingContext);
                case 102 -> HANDLER_NOT_FOUND.send(routingContext);
                case 104 -> HANDLER_NOT_FOUND.send(routingContext);
                case 105 -> HANDLER_NOT_FOUND.send(routingContext);
                case 107 -> HANDLER_NOT_FOUND.send(routingContext);
                case 108 -> HANDLER_NOT_FOUND.send(routingContext);
                case 109 -> HANDLER_NOT_FOUND.send(routingContext);
                case 110 -> HANDLER_NOT_FOUND.send(routingContext);
                case 111 -> HANDLER_NOT_FOUND.send(routingContext);
                case 112 -> HANDLER_NOT_FOUND.send(routingContext);
                case 113 -> HANDLER_NOT_FOUND.send(routingContext);
                case 114 -> HANDLER_NOT_FOUND.send(routingContext);

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

    private void submitBuyLimitOrder(final RoutingContext routingContext) {
        context().executors().worker().submit(() -> {
            final var body = routingContext.body().asJsonObject();
            final var buyLimitOrder = new BuyLimitOrder(
                    body.getLong("id"),
                    body.getLong("ts"),
                    body.getString("symbol"),
                    body.getString("quantity"),
                    body.getString("price"));

            try (final var arena = ofConfined()) {
                final var message = new LimitOrderBinaryRepresentation(arena, buyLimitOrder);
                message.encodeV1();

                final var result = submit(routingContext, arena, message.segment());
                if (result.isPresent()) {
                    routingContext.put(RESPONSE_BODY, BuyLimitOrder.decode(result.get()));
                    routingContext.next();
                }
            }
        });
    }

    private Optional<MemorySegment> submit(final RoutingContext routingContext, final Arena arena, final MemorySegment message) {
        try {
            final var result = context().oms().send(arena, message);
            if (id(result) == -1) {
                error(routingContext, result);
            } else {
                return Optional.of(result);
            }
        } catch (TimeoutException ex) {
            OMS_CONNECT_TIMEOUT.send(routingContext);
        } catch (SocketTimeoutException ex) {
            OMS_REQUEST_TIMEOUT.send(routingContext);
        } catch (ConnectException | ConnectionClosedException ex) {
            OMS_NOT_REACHABLE.send(routingContext);
        } catch (Exception ex) {
            logger.error("{}", ex.getMessage());
            SERVER_ERROR.send(routingContext);
        }

        return Optional.empty();
    }

    private void error(final RoutingContext routingContext, final MemorySegment result) {
        final var errorMessage = ErrorMessage.decode(result);
        routingContext.response()
                .putHeader(HttpHeaders.CONTENT_TYPE, "application/json")
                .setStatusCode(PRECONDITION_FAILED.code())
                .end(mapFrom(errorMessage).toBuffer());
    }
}
