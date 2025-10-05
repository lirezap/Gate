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

import org.slf4j.Logger;
import software.openex.gate.exceptions.ConnectionClosedException;

import java.io.Closeable;
import java.io.IOException;
import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeoutException;

import static java.lang.Boolean.TRUE;
import static java.lang.foreign.MemorySegment.copy;
import static java.lang.foreign.ValueLayout.JAVA_BYTE;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.slf4j.LoggerFactory.getLogger;
import static software.openex.gate.binary.BinaryRepresentable.*;

/**
 * TCP connections pool.
 *
 * @author Alireza Pourtaghi
 */
public final class ConnectionPool implements Closeable {
    private static final Logger logger = getLogger(ConnectionPool.class);

    private final InetSocketAddress address;
    private final int connectTimeout;
    private final int requestTimeout;
    private final ArrayBlockingQueue<Socket> connections;

    ConnectionPool(final String host, final int port, final int connectTimeout, final int requestTimeout,
                   final int connectionsCount) {

        this.address = new InetSocketAddress(host, port);
        this.connectTimeout = connectTimeout;
        this.requestTimeout = requestTimeout;
        this.connections = new ArrayBlockingQueue<>(connectionsCount);

        for (int i = 1; i <= connectionsCount; i++) {
            try {
                connections.offer(newConnection());
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    public MemorySegment send(final Arena arena, final MemorySegment message) throws Exception {
        var connection = connections.poll(connectTimeout, MILLISECONDS);
        if (connection == null) {
            throw new TimeoutException();
        }

        try {
            if (!connection.isConnected()) {
                connection.close();
                connection = newConnection();
            }

            final var outputStream = connection.getOutputStream();
            final var inputStream = connection.getInputStream();

            outputStream.write(message.toArray(JAVA_BYTE));
            outputStream.flush();

            final var headerBytes = inputStream.readNBytes(RHS);
            if (headerBytes.length == 0) throw new ConnectionClosedException();
            final var header = arena.allocate(RHS);
            copy(headerBytes, 0, header, BYTE, 0, RHS);

            final var bodySize = size(header);
            final var bodyBytes = inputStream.readNBytes(bodySize);
            if (bodyBytes.length == 0) throw new ConnectionClosedException();
            final var body = arena.allocate(bodySize);
            copy(bodyBytes, 0, body, BYTE, 0, bodySize);

            final var response = arena.allocate(header.byteSize() + body.byteSize());
            copy(header, 0, response, 0, header.byteSize());
            copy(body, 0, response, header.byteSize(), body.byteSize());

            return response;
        } catch (SocketException ex) {
            connection.close();
            connection = newConnection();

            throw ex;
        } finally {
            connections.offer(connection);
        }
    }

    private Socket newConnection() throws Exception {
        // TODO: Check available options.
        final var socket = new Socket();
        socket.setReuseAddress(TRUE);
        socket.setKeepAlive(TRUE);
        socket.setSoTimeout(requestTimeout);
        socket.connect(address, connectTimeout);

        return socket;
    }

    @Override
    public void close() throws IOException {
        logger.info("Closing TCP connections pool ...");

        for (final var connection : connections) {
            connection.close();
        }
    }
}
