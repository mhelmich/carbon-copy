package org.carbon.copy.dtos;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ColumnBuilder {
    public final String name;
    public final int index;
    public final String typeName;

    @JsonCreator
    public ColumnBuilder(@JsonProperty("name") String name, @JsonProperty("index") int index, @JsonProperty("typeName") String typeName) {
        this.name = name;
        this.index = index;
        this.typeName = typeName;
    }
}
