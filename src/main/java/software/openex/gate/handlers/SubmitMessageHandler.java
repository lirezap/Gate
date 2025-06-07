package software.openex.gate.handlers;

import io.vertx.ext.web.RoutingContext;

import static java.lang.Integer.parseInt;
import static software.openex.gate.handlers.Error.*;

/**
 * @author Alireza Pourtaghi
 */
public final class SubmitMessageHandler extends HTTPHandler {

    @Override
    public void handle(final RoutingContext routingContext) {
        try {
            var id = parseInt(routingContext.request().getParam("id"));

            switch (id) {
                case 101 -> HANDLER_NOT_FOUND.send(routingContext);
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
}
