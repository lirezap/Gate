package software.openex.gate.context;

import org.slf4j.Logger;

import java.io.Closeable;
import java.io.IOException;
import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeoutException;

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
    private final ArrayBlockingQueue<Socket> connections;

    OMSConnectionPool(final Configuration configuration) {
        this.address = new InetSocketAddress(configuration.loadString("oms.host"), configuration.loadInt("oms.port"));
        this.connectTimeout = (int) configuration.loadDuration("oms.connect_timeout").toMillis();
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
            if (connection.isClosed()) {
                // If current connection closed, we should replace it with a new connection.
                connection = newConnection();
            }

            var outputStream = connection.getOutputStream();
            outputStream.write(message.toArray(JAVA_BYTE));
            outputStream.flush();

            var inputStream = connection.getInputStream();

            var bytes = inputStream.readNBytes(RHS);
            var header = arena.allocate(RHS);
            copy(bytes, 0, header, BYTE, 0, RHS);

            var size = size(header);

            bytes = inputStream.readNBytes(size);
            var body = arena.allocate(size);
            copy(bytes, 0, body, BYTE, 0, size);

            var response = arena.allocate(header.byteSize() + body.byteSize());
            copy(header, 0, response, 0, header.byteSize());
            copy(body, 0, response, header.byteSize(), body.byteSize());

            return response;
        } finally {
            connections.offer(connection);
        }
    }

    private Socket newConnection() {
        try {
            // TODO: Check available options.
            var socket = new Socket();
            socket.connect(address, connectTimeout);

            return socket;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void close() throws IOException {
        logger.info("Closing OMS connections ...");

        for (var connection : connections) {
            connection.close();
        }
    }
}
