package org.carbon.copy.jdbc;

import org.apache.calcite.avatica.AvaticaConnection;
import org.apache.calcite.avatica.ConnectStringParser;
import org.apache.calcite.avatica.DriverVersion;
import org.apache.calcite.avatica.remote.Driver;

import java.sql.Connection;
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
        // all registered drivers are being called
        // if this is not concerning me, return null (as per contract)
        if (!url.startsWith(getConnectStringPrefix())) return null;
        Properties propertiesToPassOn = ConnectStringParser.parse(url.substring(getConnectStringPrefix().length()), info);
        // force client to speak protobuf
        propertiesToPassOn.setProperty("serialization", "protobuf");
        // forward this call to the embedded carbon copy driver at the specified location
        String connectionString = "jdbc:carbon-copy-embedded:" + url.substring(getConnectStringPrefix().length());
        AvaticaConnection connection = this.factory.newConnection(this, this.factory, connectionString, propertiesToPassOn);
        // notify the handler
        this.handler.onConnectionInit(connection);
        return connection;
    }
}
