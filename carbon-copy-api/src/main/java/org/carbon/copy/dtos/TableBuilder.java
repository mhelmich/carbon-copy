package org.carbon.copy.dtos;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class TableBuilder {
    public final String name;
    public final List<ColumnBuilder> columnMetadata;

    @JsonCreator
    public TableBuilder(@JsonProperty("name") String name, @JsonProperty("columnMetadata") List<ColumnBuilder> columnMetadata) {
        this.name= name;
        this.columnMetadata = columnMetadata;
    }
}
