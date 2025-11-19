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

import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

/**
 * Different error types list.
 *
 * @author Alireza Pourtaghi
 */
public enum Error {
    // Validation
    ID_NOT_VALID("id.not_valid", "", HttpResponseStatus.BAD_REQUEST.code()),
    SIGNATURE_VERIFICATION_FAILED("signature_verification.failed", "", HttpResponseStatus.BAD_REQUEST.code()),

    // GL
    GL_CONNECT_TIMEOUT("gl.connect_timeout", "GL server connect timeout", HttpResponseStatus.SERVICE_UNAVAILABLE.code()),
    GL_REQUEST_TIMEOUT("gl.request_timeout", "GL server request timeout", HttpResponseStatus.GATEWAY_TIMEOUT.code()),
    GL_NOT_REACHABLE("gl.not_reachable", "GL server not reachable", HttpResponseStatus.SERVICE_UNAVAILABLE.code()),

    // OMS
    OMS_CONNECT_TIMEOUT("oms.connect_timeout", "OMS server connect timeout", HttpResponseStatus.SERVICE_UNAVAILABLE.code()),
    OMS_REQUEST_TIMEOUT("oms.request_timeout", "OMS server request timeout", HttpResponseStatus.GATEWAY_TIMEOUT.code()),
    OMS_NOT_REACHABLE("oms.not_reachable", "OMS server not reachable", HttpResponseStatus.SERVICE_UNAVAILABLE.code()),

    // General
    HANDLER_NOT_FOUND("handler.not_found", "", HttpResponseStatus.NOT_FOUND.code()),
    REQUEST_BODY_NOT_VALID("request_body.not_valid", "", HttpResponseStatus.BAD_REQUEST.code()),
    PARAMETER_NOT_VALID("parameter.not_valid", "", HttpResponseStatus.BAD_REQUEST.code()),
    RESOURCE_NOT_FOUND("resource.not_found", "", HttpResponseStatus.NOT_FOUND.code()),
    TOO_MANY_REQUESTS("too_many_requests", "", HttpResponseStatus.TOO_MANY_REQUESTS.code()),
    FORBIDDEN("forbidden", "", HttpResponseStatus.FORBIDDEN.code()),
    SERVER_ERROR("server.error", "", HttpResponseStatus.INTERNAL_SERVER_ERROR.code());

    private final String code;
    private final String message;
    private final int httpStatusCode;

    Error(final String code, final String message, final int httpStatusCode) {
        this.code = code;
        this.message = message;
        this.httpStatusCode = httpStatusCode;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public int getHttpStatusCode() {
        return httpStatusCode;
    }

    /**
     * Sends current instance of error as an HTTP response.
     *
     * @param routingContext vertx context instance
     */
    public void send(final RoutingContext routingContext) {
        routingContext.response()
                .putHeader(HttpHeaders.CONTENT_TYPE, "application/json")
                .setStatusCode(getHttpStatusCode())
                .end(toJson().toBuffer());
    }

    /**
     * Serializes current instance of error as JSON.
     *
     * @return json representation of current instance
     */
    private JsonObject toJson() {
        final var json = new JsonObject();
        json.put("code", code);
        json.put("message", message);

        return json;
    }

    @Override
    public String toString() {
        return "Error{" +
                "code='" + code + '\'' +
                ", message='" + message + '\'' +
                ", httpStatusCode=" + httpStatusCode +
                '}';
    }
}
