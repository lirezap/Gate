package software.openex.gate.handlers;

import io.vertx.ext.web.RoutingContext;

import static io.netty.handler.codec.http.HttpResponseStatus.NO_CONTENT;
import static software.openex.gate.handlers.Error.SERVER_ERROR;

/**
 * No content responder handler implementation.
 *
 * @author Alireza Pourtaghi
 */
public final class NoContentResponderHandler extends HTTPHandler {

    @Override
    public void handle(final RoutingContext routingContext) {
        try {
            routingContext.response().putHeader(X_FRAME_OPTIONS, "nosniff");
            routingContext.response().putHeader(CACHE_CONTROL, "no-store");

            routingContext.response()
                    .setStatusCode(NO_CONTENT.code())
                    .end();
        } catch (RuntimeException ex) {
            logger.error("{}", ex.getMessage());
            SERVER_ERROR.send(routingContext);
        }
    }
}
