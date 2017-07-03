package org.carbon.copy.calcite;

import org.apache.calcite.avatica.remote.Driver;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class EmbeddedCarbonCopyDriver extends Driver {
    static final String CONNECTION_PREFIX = "jdbc:carbon-copy-embedded:";

    static {
        new EmbeddedCarbonCopyDriver().register();
    }

    private EmbeddedCarbonCopyDriver() {
        super();
    }

    @Override
    public Connection connect(String url, Properties info) throws SQLException {
        // all registered drivers are being called
        // if this is not concerning me, return null (as per contract)
        if (!url.startsWith(getConnectStringPrefix())) return null;
        // notice that the URL is thrown away
        // we know better at this point and replace it with a local connection
        // forcing my schema factory and default schema
        info.setProperty("schemaFactory", "org.carbon.copy.calcite.SchemaFactory");
        info.setProperty("schema", "carbon-copy");
        // TODO -- this could be more decomposed
        // not sure the thing that's called by the DriverManager to
        // send it to another server which has the DriverManager called
        // to resolve to another call to the DriverManager
        // but this cascading effect could also be the beauty of it
        return DriverManager.getConnection("jdbc:calcite:", info);
    }

    @Override
    protected String getConnectStringPrefix() {
        return CONNECTION_PREFIX;
    }
}
