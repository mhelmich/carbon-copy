package org.carbon.copy;

import org.apache.calcite.avatica.Meta;
import org.apache.calcite.avatica.jdbc.JdbcMeta;
import org.apache.calcite.avatica.remote.LocalService;
import org.apache.calcite.avatica.remote.Service;
import org.apache.calcite.avatica.server.AvaticaJsonHandler;
import org.apache.calcite.avatica.server.HttpServer;
import org.carbon.copy.data.structures.Table;
import org.junit.BeforeClass;
import org.junit.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import static org.junit.Assert.assertTrue;

public class CarbonCopyDriverTest extends AbstractEndToEndTest {

    @BeforeClass
    public static void setup() {
        Meta meta;
        try {
            meta = new JdbcMeta("localhost");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        Service service = new LocalService(meta);
        HttpServer server = new HttpServer.Builder()
                .withHandler(new AvaticaJsonHandler(service))
                .withPort(8765)
                .build();
        server.start();

        try {
            Class.forName("org.carbon.copy.jdbc.CarbonCopyDriver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testDriver() throws Exception {
        Table t = createDummyTable();
        Properties props = new Properties();
        props.setProperty("Hello", "World");
        try (Connection conn = DriverManager.getConnection("jdbc:carbon-copy:url=http://localhost:8765", props)) {
            try (ResultSet tables = conn.getMetaData().getTables(null, "carbon-copy", null, null)) {
                Set<String> tablesNames = new HashSet<>();
                while (tables.next()) {
                    tablesNames.add(tables.getString("TABLE_NAME"));
                }
                assertTrue(tablesNames.remove(t.getName()));
            }
        }
    }
}
