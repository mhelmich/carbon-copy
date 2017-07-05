package org.carbon.copy.dtos;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Table {
    public final String name;

    @JsonCreator
    public Table(@JsonProperty("name") String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }
}
