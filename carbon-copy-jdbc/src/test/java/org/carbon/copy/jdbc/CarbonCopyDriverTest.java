package org.carbon.copy.jdbc;

import io.dropwizard.testing.junit.DropwizardAppRule;
import org.carbon.copy.CarbonCopyApplication;
import org.carbon.copy.CarbonCopyConfiguration;
import org.carbon.copy.data.structures.Table;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;

import static org.junit.Assert.assertTrue;

public class CarbonCopyDriverTest {
    private static Logger logger = LoggerFactory.getLogger(CarbonCopyDriverTest.class);

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
            new DropwizardAppRule<>(CarbonCopyApplication.class, "../config/carbon-copy.yml");

    @Test
    @Ignore
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
//        Table.Builder tableBuilder = Table.newBuilder(tableName.toUpperCase())
//                .withColumn("TUP_NUM".toUpperCase(), Integer.class)
//                .withColumn("MOEP".toUpperCase(), String.class)
//                .withColumn("FOO".toUpperCase(), String.class);
//
//        Txn txn = txnManager.beginTransaction();
//        Table table = dsFactory.newTable(tableBuilder, txn);
//
//        for (int id : ids) {
//            Tuple tup = new Tuple(3);
//            tup.put(0, id);
//            tup.put(1, (id % 2 == 0) ? "moep" : "__moep__");
//            tup.put(2, id + "_tup_foo");
//            table.insert(tup, txn);
//        }
//
//        catalog.create(table, txn);
//        txn.commit();
//
//        return table;

        return null;
    }
}
