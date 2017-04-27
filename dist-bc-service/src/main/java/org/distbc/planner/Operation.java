package org.distbc.planner;

import org.distbc.data.structures.TempTable;
import org.distbc.data.structures.Txn;

interface Operation {
    TempTable apply(TempTable tempTable, Txn txn);
}
