package software.openex.gate.handlers;

import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;
import org.slf4j.Logger;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Base HTTP handler abstraction that can be added into the chain of HTTP handlers.
 *
 * @author Alireza Pourtaghi
 */
public abstract class HTTPHandler implements Handler<RoutingContext> {
    protected final Logger logger = getLogger(this.getClass());

    protected static final String X_FRAME_OPTIONS = "X-FRAME-OPTIONS";
    protected static final String CACHE_CONTROL = "Cache-Control";
    protected static final String RESPONSE_BODY = "RESPONSE_BODY";
}
