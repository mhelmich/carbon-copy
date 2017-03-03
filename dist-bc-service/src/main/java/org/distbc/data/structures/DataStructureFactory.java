package org.distbc.data.structures;

public interface DataStructureFactory {
    Table newTable(Table.Builder builder, Txn txn);
    Table loadTable(long id);
    Table loadTableForWrites(long id, Txn txn);

    Index newIndex(Index.Builder builder, Txn txn);
    Index loadIndex(long id);
    Index loadIndexForWrites(long id, Txn txn);
}
