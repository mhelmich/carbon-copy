package org.distbc;

import co.paralleluniverse.galaxy.Grid;
import co.paralleluniverse.galaxy.Store;
import co.paralleluniverse.galaxy.StoreTransaction;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.io.UnsafeMemoryOutput;
import com.google.inject.Inject;
import org.apache.commons.lang3.StringUtils;
import org.distbc.data.structures.experimental.SkipList;
import org.distbc.parser.ParsingResult;
import org.distbc.parser.QueryParser;
import org.distbc.planner.QueryPlanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

class DistBufferCacheResourceImpl implements DistBufferCacheResource {
    private static Logger logger = LoggerFactory.getLogger(DistBufferCacheResourceImpl.class);
    private static final Map<String, Long> namesToId = new ConcurrentHashMap<>();

    private final Grid grid;
    private final QueryParser queryParser;
    private final QueryPlanner queryPlanner;

    @Inject
    DistBufferCacheResourceImpl(Grid grid, QueryParser queryParser, QueryPlanner queryPlanner) {
        this.grid = grid;
        this.queryParser = queryParser;
        this.queryPlanner = queryPlanner;
    }

    public String feedData(String index) {
        final SkipList sl = new SkipList();
        for (int i = 0; i < 10; i++) {
            String key = "key_" + i;
            String value = UUID.randomUUID().toString();
            sl.insert(key, value);
        }

        logger.info("created skip list size [{}]", sl.size());
        final Output output = new UnsafeMemoryOutput(1024);

        long root = -1;
        Store store = grid.store();

        Long id = namesToId.get(index);
        if (id == null) {
            StoreTransaction txn = store.beginTransaction();
            try {
                root = store.getRoot(index, txn);
                if (store.isRootCreated(root, txn)) {
                    store.set(root, output.toBytes(), txn); // initialize root
                }
                store.commit(txn);
            } catch (Exception ex) {
                logger.error("Couldn't create root", ex);
                store.rollback(txn);
                try {
                    store.abort(txn);
                } catch (Exception xcp2) {
                    logger.error("Couldn't abort transaction", xcp2);
                    throw new RuntimeException(xcp2);
                }
            }

            namesToId.put(index, root);
            logger.info("initialized the root node for [{}] with id [{}]", index, root);
        }

        return "ok";
    }

    public Set<Object> query(String query) {
        ParsingResult pr = queryParser.parse(query);
        logger.info("All the tables I want to access: {}", StringUtils.join(", ", pr.getTableNames()));
        logger.info("All the columns I want to access: {}", StringUtils.join(", ", pr.getColumnNames()));
        return queryPlanner.generateQueryPlan(pr).execute();
    }
}
