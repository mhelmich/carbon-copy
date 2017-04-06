package org.distbc.parser;

import java.util.List;

public interface ParsingResult {
    List<String> getTableNames();
    List<String> getProjectionColumnNames();
    List<String> getWhereClauses();
    List<String> getJoinClauses();
}
