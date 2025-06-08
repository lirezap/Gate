package software.openex.gate.context;

import org.slf4j.Logger;

import java.io.Closeable;
import java.io.IOException;
import java.lang.foreign.MemorySegment;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.ArrayBlockingQueue;

import static org.slf4j.LoggerFactory.getLogger;

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

    public void send(final MemorySegment segment) throws Exception {
        // TODO: Complete implementation.
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
        for (var connection : connections) {
            connection.close();
        }
    }
}
