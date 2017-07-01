package org.carbon.copy;

import com.google.inject.Inject;
import org.carbon.copy.calcite.CalciteModule;
import org.carbon.copy.data.structures.Catalog;
import org.carbon.copy.data.structures.DataStructureFactory;
import org.carbon.copy.data.structures.DataStructureModule;
import org.carbon.copy.data.structures.Table;
import org.carbon.copy.data.structures.Tuple;
import org.carbon.copy.data.structures.Txn;
import org.carbon.copy.data.structures.TxnManager;
import org.carbon.copy.data.structures.TxnManagerModule;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.util.UUID;

@RunWith(GuiceJUnit4Runner.class)
@GuiceModules({ DataStructureModule.class, TxnManagerModule.class, CalciteModule.class })
abstract class AbstractEndToEndTest {
    @Inject
    private DataStructureFactory dsFactory;

    @Inject
    private TxnManager txnManager;

    @Inject
    private Catalog catalog;

    Connection getCalciteConnection() throws SQLException {
        Properties props = new Properties();
        props.put("model",
                "inline:"
                        + "{\n"
                        + "  version: '1.0',\n"
                        + "  defaultSchema: 'carbon-copy',\n"
                        + "   schemas: [\n"
                        + "     {\n"
                        + "       type: 'custom',\n"
                        + "       name: 'carbon-copy',\n"
                        + "       factory: 'org.carbon.copy.calcite.SchemaFactory',\n"
                        + "       operand: {\n"
                        + "           some: 'text'\n"
                        + "       }\n"
                        + "     }\n"
                        + "   ]\n"
                        + "}");
        return DriverManager.getConnection("jdbc:calcite:", props);
    }

    Table createDummyTable() throws IOException {
        String tableName = "NARF_" + System.currentTimeMillis() + "_" + UUID.randomUUID().toString().replaceAll("-", "");
        return createDummyTable(tableName, 1, 2, 3);
    }

    private Table createDummyTable(String tableName, int... ids) throws IOException {
        Table.Builder tableBuilder = Table.newBuilder(tableName.toUpperCase())
                .withColumn("TUP_NUM".toUpperCase(), Integer.class)
                .withColumn("MOEP".toUpperCase(), String.class)
                .withColumn("FOO".toUpperCase(), String.class);

        Txn txn = txnManager.beginTransaction();
        Table table = dsFactory.newTable(tableBuilder, txn);

        for (int id : ids) {
            Tuple tup = new Tuple(3);
            tup.put(0, id);
            tup.put(1, (id % 2 == 0) ? "moep" : "__moep__");
            tup.put(2, id + "_tup_foo");
            table.insert(tup, txn);
        }

        catalog.create(table, txn);
        txn.commit();

        return table;
    }
}
