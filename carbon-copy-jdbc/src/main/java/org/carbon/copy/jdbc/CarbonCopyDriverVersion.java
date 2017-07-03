package org.carbon.copy.jdbc;

import org.apache.calcite.avatica.DriverVersion;

class CarbonCopyDriverVersion extends DriverVersion {
    CarbonCopyDriverVersion() {
        super(
                "Calcite JDBC Driver for CarbonCopy",
                "0.1",
                "Calcite-CarbonCopy",
                "0.1",
                true,
                0,
                1,
                0,
                1
        );
    }
}
