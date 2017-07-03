package org.carbon.copy.calcite;

import com.google.common.collect.ImmutableMap;
import org.apache.calcite.schema.Table;
import org.apache.calcite.schema.impl.AbstractSchema;
import org.carbon.copy.data.structures.Catalog;

import java.io.IOException;
import java.util.Map;
import java.util.stream.Collectors;

class Schema extends AbstractSchema {

    private final Catalog catalog;

    Schema(Catalog catalog) {
        super();
        this.catalog = catalog;
    }

    @Override
    protected Map<String, Table> getTableMap() {
        try {
            Map<String, Table> m = catalog.listTables().entrySet().stream()
                    .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            entry -> new CarbonCopyTable(catalog, entry.getValue())
                    ));
            return ImmutableMap.copyOf(m);
        } catch (IOException xcp) {
            throw new RuntimeException(xcp);
        }
    }

    @Override
    public boolean isMutable() {
        return false;
    }
}
