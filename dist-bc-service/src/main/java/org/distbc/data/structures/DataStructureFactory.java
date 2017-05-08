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

package org.distbc.data.structures;

/**
 * Use me to get (or create) top-level data structures.
 * Don't even think about using them directly!
 */
public interface DataStructureFactory {
    Table newTable(Table.Builder builder, Txn txn);
    Table loadTable(long id);
    Table loadTableForWrites(long id, Txn txn);

    Index newIndex(Index.Builder builder, Txn txn);
    Index loadIndex(long id);
    Index loadIndexForWrites(long id, Txn txn);

    TempTable newTempTable(Txn txn);
    TempTable newTempTableFromTable(Table table, Txn txn);
    TempTable loadTempTableFromId(long id, Txn txn);
}
