package org.carbon.copy;

import com.google.inject.Inject;
import org.carbon.copy.calcite.AvaticaServer;
import org.carbon.copy.calcite.CalciteModule;
import org.carbon.copy.data.structures.Catalog;
import org.carbon.copy.data.structures.DataStructureFactory;
import org.carbon.copy.data.structures.DataStructureModule;
import org.carbon.copy.data.structures.Table;
import org.carbon.copy.data.structures.Tuple;
import org.carbon.copy.data.structures.Txn;
import org.carbon.copy.data.structures.TxnManager;
import org.carbon.copy.data.structures.TxnManagerModule;
import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.util.Random;
import java.util.UUID;

@RunWith(GuiceJUnit4Runner.class)
@GuiceModules({ DataStructureModule.class, TxnManagerModule.class, CalciteModule.class })
abstract class AbstractEndToEndTest {
    private static Logger logger = LoggerFactory.getLogger(AbstractEndToEndTest.class);

    @Inject
    private DataStructureFactory dsFactory;

    @Inject
    private TxnManager txnManager;

    @Inject
    private Catalog catalog;

    AvaticaServer avaticaServer;

    @Before
    public void setupAvaticaServer() {
        if (avaticaServer != null) {
            logger.warn("Somebody didn't clean up after himself");
            avaticaServer.stop();
            avaticaServer = null;
        }
        avaticaServer = getTestSpecificAvaticaServer();
        avaticaServer.start();
    }

    @After
    public void tearDownAvaticaServer() {
        avaticaServer.stop();
        avaticaServer = null;
    }

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

    private AvaticaServer getTestSpecificAvaticaServer() {
        try {
            int testPort = new Random().nextInt(58000) + 1024;
            // I rather compromise on writing ugly test code than polluting my production interface with test-methods
            // hence I need to jump through these four burning rings on order to get a test-specific server
            Class<?> klass = Class.forName("org.carbon.copy.calcite.AvaticaServerImpl");
            Constructor<?> ctor = klass.getDeclaredConstructor(int.class);
            ctor.setAccessible(true);
            return (AvaticaServer) ctor.newInstance(testPort);
        } catch (Exception xcp) {
            throw new RuntimeException(xcp);
        }
    }
}
