package org.carbon.copy;

import org.carbon.copy.data.structures.Table;
import org.junit.BeforeClass;
import org.junit.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import static org.junit.Assert.assertTrue;

public class CarbonCopyDriverTest extends AbstractEndToEndTest {

    @BeforeClass
    public static void registerJdbcDriver() {
        try {
            Class.forName("org.carbon.copy.jdbc.CarbonCopyDriver");
        } catch (ClassNotFoundException xcp) {
            throw new RuntimeException(xcp);
        }
    }

    @Test
    public void testDriver() throws Exception {
        int usedPort = avaticaServer.getPort();
        Table t = createDummyTable();
        Properties props = new Properties();
        props.setProperty("Hello", "World");
        try (Connection conn = DriverManager.getConnection("jdbc:carbon-copy:url=http://localhost:" + usedPort, props)) {
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
