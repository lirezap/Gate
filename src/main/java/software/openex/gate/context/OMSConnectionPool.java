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
 * OMS TCP connections pool.
 *
 * @author Alireza Pourtaghi
 */
public final class OMSConnectionPool implements Closeable {
    private static final Logger logger = getLogger(OMSConnectionPool.class);

    private final InetSocketAddress address;
    private final int connectTimeout;
    private final int requestTimeout;
    private final ArrayBlockingQueue<Socket> connections;

    OMSConnectionPool(final Configuration configuration) {
        this.address = new InetSocketAddress(configuration.loadString("oms.host"), configuration.loadInt("oms.port"));
        this.connectTimeout = (int) configuration.loadDuration("oms.connect_timeout").toMillis();
        this.requestTimeout = (int) configuration.loadDuration("oms.request_timeout").toMillis();
        this.connections = new ArrayBlockingQueue<>(configuration.loadInt("oms.connections_count"));

        for (int i = 1; i <= configuration.loadInt("oms.connections_count"); i++) {
            connections.offer(newConnection());
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

    private Socket newConnection() {
        try {
            // TODO: Check available options.
            final var socket = new Socket();
            socket.setReuseAddress(TRUE);
            socket.setKeepAlive(TRUE);
            socket.setSoTimeout(requestTimeout);
            socket.connect(address, connectTimeout);

            return socket;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void close() throws IOException {
        logger.info("Closing OMS connections ...");

        for (final var connection : connections) {
            connection.close();
        }
    }
}
