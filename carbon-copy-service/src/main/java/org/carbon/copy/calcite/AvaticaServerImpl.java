package org.carbon.copy.calcite;

import org.apache.calcite.avatica.jdbc.JdbcMeta;
import org.apache.calcite.avatica.remote.LocalService;
import org.apache.calcite.avatica.server.AvaticaProtobufHandler;
import org.apache.calcite.avatica.server.HttpServer;

import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicBoolean;

class AvaticaServerImpl implements AvaticaServer {
    private final HttpServer server;
    private final AtomicBoolean isStarted = new AtomicBoolean(false);

    @SuppressWarnings("unused")
    AvaticaServerImpl() {
        this(8765);
    }

    private AvaticaServerImpl(int port) {
        try {
            server = new HttpServer.Builder()
                    // start protobuf server
                    .withHandler(new AvaticaProtobufHandler(new LocalService(new JdbcMeta(EmbeddedCarbonCopyDriver.CONNECTION_PREFIX))))
                    .withPort(port)
                    .build();
        } catch (SQLException xcp) {
            throw new RuntimeException(xcp);
        }
    }

    @Override
    public void start() {
        if (!isStarted.get()) {
            synchronized (server) {
                if (!isStarted.get()) {
                    server.start();
                    isStarted.set(true);
                }
            }
        }
    }

    @Override
    public void stop() {
        if (isStarted.get()) {
            synchronized (server) {
                if (isStarted.get()) {
                    server.stop();
                    isStarted.set(false);
                }
            }
        }
    }

    @Override
    public int getPort() {
        return server.getPort();
    }

    @Override
    public void join() throws InterruptedException {
        server.join();
    }
}
