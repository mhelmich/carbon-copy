package org.carbon.copy.calcite;

import org.apache.calcite.avatica.jdbc.JdbcMeta;
import org.apache.calcite.avatica.remote.LocalService;
import org.apache.calcite.avatica.server.AvaticaJsonHandler;
import org.apache.calcite.avatica.server.HttpServer;

import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicBoolean;

class AvaticaServerImpl implements AvaticaServer {
    private final HttpServer server;
    private final AtomicBoolean isStarted = new AtomicBoolean(false);
    AvaticaServerImpl() {
        try {
            server = new HttpServer.Builder()
                    .withHandler(new AvaticaJsonHandler(new LocalService(new JdbcMeta(EmbeddedCarbonCopyDriver.CONNECTION_PREFIX))))
                    .withPort(8765)
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
    public void join() throws InterruptedException {
        server.join();
    }
}
