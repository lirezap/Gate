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
package software.openex.gate.context;

import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.micrometer.MicrometerMetricsOptions;
import io.vertx.micrometer.VertxPrometheusOptions;
import org.slf4j.Logger;

import java.io.Closeable;
import java.util.concurrent.atomic.AtomicBoolean;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static java.lang.Runtime.getRuntime;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * Singleton application context that contains main components.
 *
 * @author Alireza Pourtaghi
 */
public final class AppContext implements Closeable {
    private static final Logger logger = getLogger(AppContext.class);

    private static final AtomicBoolean initialized = new AtomicBoolean(FALSE);
    private static AppContext context;

    private final Configuration configuration;
    private final SignatureVerifier signatureVerifier;
    private final ConnectionPool glConnectionPool;
    private final ConnectionPool omsConnectionPool;
    private final Executors executors;
    private final Vertx vertx;
    private final HTTPServer httpServer;

    private AppContext() {
        addShutdownHook();

        this.configuration = new Configuration();
        this.signatureVerifier = signatureVerifier(this.configuration);
        this.glConnectionPool = glConnectionPool(this.configuration);
        this.omsConnectionPool = omsConnectionPool(this.configuration);
        this.executors = new Executors(this.configuration);
        this.vertx = Vertx.vertx(vertxOptions(this.configuration));
        this.httpServer = new HTTPServer(this.configuration, this.vertx);
    }

    /**
     * Thread safe application context initializer.
     */
    public static void initialize() {
        if (initialized.getAcquire()) {
            // Do nothing; already initialized application context.
        } else {
            // Initializing application context.
            initialized.set(TRUE);
            context = new AppContext();
        }
    }

    /**
     * Returns current instance of application context.
     * This method is not safe if initialized method is not called and may return null.
     *
     * @return current initialized application context
     */
    public static AppContext context() {
        return context;
    }

    /**
     * Returns current instance of application context.
     * This method is safe if initialized method is not called, thereby always has a return value.
     *
     * @return current initialized application context
     */
    public static AppContext contextSafe() {
        initialize();
        return context;
    }

    /**
     * Returns new instance of application context.
     * This method must only be used for testing purposes.
     *
     * @return new application context
     */
    public static AppContext contextTest() {
        context = new AppContext();
        return context;
    }

    public Configuration config() {
        return configuration;
    }

    public SignatureVerifier signatureVerifier() {
        return signatureVerifier;
    }

    public ConnectionPool gl() {
        return glConnectionPool;
    }

    public ConnectionPool oms() {
        return omsConnectionPool;
    }

    public Executors executors() {
        return executors;
    }

    public Vertx vertx() {
        return vertx;
    }

    public HTTPServer httpServer() {
        return httpServer;
    }

    private static SignatureVerifier signatureVerifier(final Configuration configuration) {
        if (configuration.loadBoolean("signature.verification_enabled")) {
            return new SignatureVerifier(configuration);
        }

        logger.warn("\uD83D\uDEA8 signature.verification_enabled option has not been set");
        return null;
    }

    private static ConnectionPool glConnectionPool(final Configuration configuration) {
        if (configuration.loadBoolean("gl.connect")) {
            return new ConnectionPool(
                    configuration.loadString("gl.host"),
                    configuration.loadInt("gl.port"),
                    (int) configuration.loadDuration("gl.connect_timeout").toMillis(),
                    (int) configuration.loadDuration("gl.request_timeout").toMillis(),
                    configuration.loadInt("gl.connections_count"));
        }

        logger.warn("⚠\uFE0F gl.connect option has not been set");
        return null;
    }

    private static ConnectionPool omsConnectionPool(final Configuration configuration) {
        if (configuration.loadBoolean("oms.connect")) {
            return new ConnectionPool(
                    configuration.loadString("oms.host"),
                    configuration.loadInt("oms.port"),
                    (int) configuration.loadDuration("oms.connect_timeout").toMillis(),
                    (int) configuration.loadDuration("oms.request_timeout").toMillis(),
                    configuration.loadInt("oms.connections_count"));
        }

        logger.warn("⚠\uFE0F oms.connect option has not been set");
        return null;
    }

    private static VertxOptions vertxOptions(final Configuration configuration) {
        var metricsServerOptions = new HttpServerOptions()
                .setHost(configuration.loadString("metrics.server.host"))
                .setPort(configuration.loadInt("metrics.server.port"));

        var prometheusOptions = new VertxPrometheusOptions()
                .setEnabled(configuration.loadBoolean("metrics.server.enabled"))
                .setStartEmbeddedServer(TRUE)
                .setEmbeddedServerOptions(metricsServerOptions)
                .setEmbeddedServerEndpoint("/v1/metrics");

        var micrometerOptions = new MicrometerMetricsOptions()
                .setEnabled(configuration.loadBoolean("metrics.server.enabled"))
                .setPrometheusOptions(prometheusOptions)
                .setJvmMetricsEnabled(TRUE);

        return new VertxOptions()
                .setPreferNativeTransport(TRUE)
                .setMetricsOptions(micrometerOptions);
    }

    /**
     * Adds a shutdown hook for context.
     */
    private void addShutdownHook() {
        getRuntime().addShutdownHook(new Thread(this::close));
    }

    @Override
    public void close() {
        try {
            if (httpServer != null) httpServer.close();
            if (executors != null) executors.close();
            if (omsConnectionPool != null) omsConnectionPool.close();
            if (glConnectionPool != null) glConnectionPool.close();
        } catch (Exception ex) {
            logger.error("{}", ex.getMessage());
        }
    }
}
