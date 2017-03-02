package org.distbc.parser;

import java.util.List;

public interface ParsingResult {
    List<String> getTableNames();
    List<String> getColumnNames();
}
