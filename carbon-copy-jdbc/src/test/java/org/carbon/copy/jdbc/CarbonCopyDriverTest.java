package org.carbon.copy.jdbc;

import io.dropwizard.testing.ConfigOverride;
import io.dropwizard.testing.junit.DropwizardAppRule;
import org.carbon.copy.CarbonCopyApplication;
import org.carbon.copy.CarbonCopyConfiguration;
import org.carbon.copy.dtos.ColumnBuilder;
import org.carbon.copy.dtos.Table;
import org.carbon.copy.dtos.TableBuilder;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;

import javax.ws.rs.client.Entity;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;

import static org.junit.Assert.assertTrue;

public class CarbonCopyDriverTest {
    @BeforeClass
    public static void registerJdbcDriver() {
        try {
            Class.forName("org.carbon.copy.jdbc.CarbonCopyDriver");
        } catch (ClassNotFoundException xcp) {
            throw new RuntimeException(xcp);
        }
    }

    @ClassRule
    public static final DropwizardAppRule<CarbonCopyConfiguration> RULE =
            new DropwizardAppRule<>(CarbonCopyApplication.class,
                    "../config/carbon-copy.yml",
                    ConfigOverride.config("defaultPeerXml", "../config/peer.xml"),
                    ConfigOverride.config("defaultPeerProperties", "../config/peer.properties"));

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

    private Table createDummyTable() throws IOException {
        String tableName = "NARF_" + System.currentTimeMillis() + "_" + UUID.randomUUID().toString().replaceAll("-", "");
        return createDummyTable(tableName, 1, 2, 3);
    }

    private Table createDummyTable(String tableName, int... ids) throws IOException {
        ColumnBuilder cb1 = new ColumnBuilder("column1", 0, String.class.getName());
        ColumnBuilder cb2 = new ColumnBuilder("column2", 1, Integer.class.getName());
        TableBuilder dtoTableBuilder = new TableBuilder(tableName, Arrays.asList(cb1, cb2));

        return RULE.client()
                .target(String.format("http://localhost:%d/carbon-copy/createTable", RULE.getLocalPort()))
                .request()
                .post(Entity.json(dtoTableBuilder))
                .readEntity(Table.class);
    }
}
