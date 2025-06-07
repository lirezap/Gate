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

            var response = Json.encodeToBuffer(routingContext.get(RESPONSE_BODY));
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
