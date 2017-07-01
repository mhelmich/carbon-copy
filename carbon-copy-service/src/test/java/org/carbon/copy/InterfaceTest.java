/*
 *
 *  Copyright 2017 Marco Helmich
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.carbon.copy;

import org.carbon.copy.data.structures.Table;
import org.junit.Test;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class InterfaceTest extends AbstractEndToEndTest {
    @Test
    public void testCalciteConnectionListTables() throws SQLException, IOException {
        Table t1 = createDummyTable();
        try (Connection connection = getCalciteConnection()) {
            try (ResultSet tables = connection.getMetaData().getTables(null, "carbon-copy", null, null)) {

                Set<String> tableNames = new HashSet<>();
                while (tables.next()) {
                    tableNames.add(tables.getString("TABLE_NAME"));
                }

                assertTrue(tableNames.remove(t1.getName()));
            }
        }

        Table t2 = createDummyTable();
        try (Connection connection = getCalciteConnection()) {
            try (ResultSet tables = connection.getMetaData().getTables(null, "carbon-copy", null, null)) {

                Set<String> tableNames = new HashSet<>();
                while (tables.next()) {
                    tableNames.add(tables.getString("TABLE_NAME"));
                }

                assertTrue(tableNames.remove(t1.getName()));
                assertTrue(tableNames.remove(t2.getName()));
            }
        }
    }

    @Test
    public void testQueryWithFilters() throws IOException, SQLException {
        Table t = createDummyTable();
        try (Connection connection = getCalciteConnection()) {

            try (ResultSet tables = connection.getMetaData().getTables(null, "carbon-copy", null, null)) {
                Set<String> tableNames = new HashSet<>();
                while (tables.next()) {
                    tableNames.add(tables.getString("TABLE_NAME"));
                }
                assertTrue(tableNames.contains(t.getName()));
            }

            try (Statement statement = connection.createStatement()) {
                String sql = "SELECT * FROM " + t.getName() + " WHERE moep = 'moep' AND 2 = tup_num";
                try (ResultSet resultSet = statement.executeQuery(sql)) {
                    Set<Integer> tupNums = new HashSet<>();

                    while (resultSet.next()) {
                        tupNums.add(resultSet.getInt("tup_num"));
                    }

                    assertEquals(1, tupNums.size());
                    assertTrue(tupNums.remove(2));
                }
            }
        }
    }

    @Test
    public void testQueryWithProjectionsAndFilters() throws Exception {
        Table t = createDummyTable();
        try (Connection connection = getCalciteConnection()) {
            try (Statement statement = connection.createStatement()) {
                String sql = "SELECT tup_num FROM " + t.getName() + " WHERE moep = 'moep' AND 2 = tup_num";
                try (ResultSet resultSet = statement.executeQuery(sql)) {
                    Set<Integer> tupNums = new HashSet<>();

                    while (resultSet.next()) {
                        tupNums.add(resultSet.getInt("tup_num"));
                    }

                    assertEquals(1, tupNums.size());
                    assertTrue(tupNums.remove(2));
                }
            }
        }
    }

    @Test
    public void testQueryWithProjectToSingleValueInTuple() throws Exception {
        Table t = createDummyTable();
        try (Connection connection = getCalciteConnection()) {
            try (Statement statement = connection.createStatement()) {
                String sql = "SELECT tup_num FROM " + t.getName();
                try (ResultSet resultSet = statement.executeQuery(sql)) {
                    Set<Integer> tupNums = new HashSet<>();

                    while (resultSet.next()) {
                        tupNums.add(resultSet.getInt("tup_num"));
                    }

                    assertEquals(3, tupNums.size());
                    assertTrue(tupNums.remove(1));
                    assertTrue(tupNums.remove(2));
                    assertTrue(tupNums.remove(3));
                }
            }
        }
    }

    @Test
    public void testQueryWithProjectOnly() throws Exception {
        Table t = createDummyTable();
        try (Connection connection = getCalciteConnection()) {
            try (Statement statement = connection.createStatement()) {
                String sql = "SELECT TUP_NUM, MOEP FROM " + t.getName();
                try (ResultSet resultSet = statement.executeQuery(sql)) {
                    Set<Integer> tupNums = new HashSet<>();

                    while (resultSet.next()) {
                        tupNums.add(resultSet.getInt("tup_num"));
                    }

                    assertEquals(3, tupNums.size());
                    assertTrue(tupNums.remove(1));
                    assertTrue(tupNums.remove(2));
                    assertTrue(tupNums.remove(3));
                }
            }
        }
    }
}
