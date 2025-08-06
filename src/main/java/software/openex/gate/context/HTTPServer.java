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
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.ext.healthchecks.HealthCheckHandler;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.*;
import org.slf4j.Logger;
import software.openex.gate.handlers.*;

import java.io.Closeable;
import java.io.IOException;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static io.netty.handler.codec.http.HttpResponseStatus.GATEWAY_TIMEOUT;
import static io.vertx.core.http.HttpMethod.*;
import static io.vertx.ext.web.Router.router;
import static io.vertx.ext.web.handler.HSTSHandler.DEFAULT_MAX_AGE;
import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static org.slf4j.LoggerFactory.getLogger;
import static software.openex.gate.context.AppContext.context;

/**
 * HTTP server component.
 *
 * @author Alireza Pourtaghi
 */
public final class HTTPServer implements Closeable {
    private static final Logger logger = getLogger(HTTPServer.class);

    private final Vertx vertx;
    private final HttpServer httpServer;
    private final Router router;

    HTTPServer(final Configuration configuration, final Vertx vertx) {
        this.vertx = vertx;

        final var options = new HttpServerOptions()
                .setHost(configuration.loadString("http.server.host"))
                .setPort(configuration.loadInt("http.server.port"))
                .setIdleTimeout((int) configuration.loadDuration("http.server.idle_timeout").toSeconds()).setIdleTimeoutUnit(TimeUnit.SECONDS)
                .setCompressionSupported(configuration.loadBoolean("http.server.use_compression"))
                .setCompressionLevel(configuration.loadInt("http.server.compression_level"))
                .setReusePort(TRUE)
                .setTcpFastOpen(TRUE)
                .setTcpQuickAck(TRUE)
                .setTcpCork(TRUE);

        this.httpServer = vertx.createHttpServer(options);
        this.router = router(vertx);
    }

    public void start() {
        setupRoutes(context().config().loadInt("http.server.request_body_limit_size"));

        final var cause = httpServer.requestHandler(router).listen().cause();
        if (cause != null) {
            throw new RuntimeException(cause.getMessage());
        }

        logger.info("Successfully started HTTP server and listening on {}:{} with native transport {}",
                context().config().loadString("http.server.host"),
                context().config().loadString("http.server.port"),
                vertx.isNativeTransportEnabled() ? "enabled" : "not enabled");
    }

    private void setupRoutes(final int requestBodyLimitSize) {
        setupBaseHandlers();

        final var bodyHandler = BodyHandler.create(FALSE).setBodyLimit(requestBodyLimitSize);
        final var jsonBodyResponderHandler = new JsonBodyResponderHandler();
        final var noContentResponderHandler = new NoContentResponderHandler();

        router.post("/v1/messages").handler(bodyHandler).handler(new SubmitMessageHandler()).handler(jsonBodyResponderHandler);

        router.get("/v1/ready").handler(new LiveNessHandler());
        router.get("/health*").handler(healthChecksHandler());
    }

    private void setupBaseHandlers() {
        router.route().handler(ResponseTimeHandler.create());
        router.route().handler(TimeoutHandler.create(context().config().loadDuration("http.server.request_timeout").toMillis(), GATEWAY_TIMEOUT.code()));

        if (context().config().loadBoolean("http.server.request_logging_enabled")) {
            router.route().handler(LoggerHandler.create(LoggerFormat.CUSTOM).customFormatter(new RequestLoggingFormatter()));
        }

        if (context().config().loadBoolean("http.server.hsts_enabled")) {
            router.route().handler(HSTSHandler.create(DEFAULT_MAX_AGE, TRUE));
        }

        if (context().config().loadBoolean("http.server.csp_enabled")) {
            router.route().handler(CSPHandler.create());
        }

        if (context().config().loadBoolean("http.server.cors_enabled")) {
            router.route().handler(CorsHandler.create()
                    .addOrigin(context().config().loadString("http.server.cors_origin"))
                    .allowedMethods(Set.of(POST, GET, PUT, PATCH, DELETE, OPTIONS))
                    .allowedHeaders(Set.of("Content-Type"))
                    .allowCredentials(context().config().loadBoolean("http.server.cors_allow_credentials")));
        }

        if (context().config().loadBoolean("http.server.x_frame_enabled")) {
            router.route().handler(XFrameHandler.create(context().config().loadString("http.server.x_frame_action")));
        }
    }

    /**
     * Registers health checks and returns it as a web handler.
     *
     * @return an instance of {@link HealthCheckHandler} to be used by web server
     */
    private HealthCheckHandler healthChecksHandler() {
        return HealthCheckHandler.create(vertx);
    }

    @Override
    public void close() throws IOException {
        logger.info("Closing HTTP server ...");

        final var cause = vertx.close().cause();
        if (cause != null) {
            logger.error("{}", cause.getMessage());
        }
    }
}
