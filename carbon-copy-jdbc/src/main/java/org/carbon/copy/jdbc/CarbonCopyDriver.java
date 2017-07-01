package org.carbon.copy.jdbc;

import org.apache.calcite.avatica.DriverVersion;
import org.apache.calcite.avatica.remote.Driver;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class CarbonCopyDriver extends Driver {
    static {
        new CarbonCopyDriver().register();
    }

    private CarbonCopyDriver() {
        super();
    }

    @Override
    protected String getConnectStringPrefix() {
        return "jdbc:carbon-copy:";
    }

    @Override
    protected DriverVersion createDriverVersion() {
        return new CarbonCopyDriverVersion();
    }

    @Override
    public Connection connect(String url, Properties info) throws SQLException {
        // notice that the URL is thrown away
        // we know better at this point and replace it with a local connection
        // forcing my schema factory and default schema
        info.setProperty("schemaFactory", "org.carbon.copy.calcite.SchemaFactory");
        info.setProperty("schema", "carbon-copy");
        return getCalciteConnection(info);
    }

    private Connection getCalciteConnection(Properties props) throws SQLException {
        return DriverManager.getConnection("jdbc:calcite:", props);
    }
}
