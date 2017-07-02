package org.carbon.copy.calcite;

import com.google.inject.AbstractModule;
import org.apache.calcite.avatica.jdbc.JdbcMeta;
import org.apache.calcite.avatica.remote.LocalService;
import org.apache.calcite.avatica.server.AvaticaJsonHandler;
import org.apache.calcite.avatica.server.HttpServer;

import java.sql.SQLException;

public class CalciteModule extends AbstractModule {
    @Override
    protected void configure() {
        // yupp, not the fine British way
        // read the comment in Injector for more context
        requestStaticInjection(Injector.class);

        try {
            HttpServer server = new HttpServer.Builder()
                    .withHandler(new AvaticaJsonHandler(new LocalService(new JdbcMeta(EmbeddedCarbonCopyDriver.CONNECTION_PREFIX))))
                    .withPort(8765)
                    .build();
            server.start();
        } catch (SQLException xcp) {
            throw new RuntimeException(xcp);
        }

        try {
            Class.forName(EmbeddedCarbonCopyDriver.class.getName());
        } catch (ClassNotFoundException xcp) {
            throw new RuntimeException(xcp);
        }
    }
}
