package software.openex.gate;

import org.slf4j.Logger;

import static java.lang.System.exit;
import static org.slf4j.LoggerFactory.getLogger;
import static software.openex.gate.context.AppContext.context;
import static software.openex.gate.context.AppContext.initialize;

/**
 * Main application class to be executed.
 *
 * @author Alireza Pourtaghi
 */
public final class GateApplication {
    private static final Logger logger = getLogger(GateApplication.class);

    public static void main(final String... args) {
        try {
            initialize();
            context().httpServer().start();
        } catch (Exception ex) {
            logger.error("error on initializing application context: {}", ex.getMessage(), ex);
            exit(-1);
        }
    }
}
