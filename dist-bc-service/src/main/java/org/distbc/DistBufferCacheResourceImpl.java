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

package org.distbc;

import co.paralleluniverse.galaxy.Grid;
import co.paralleluniverse.galaxy.Store;
import co.paralleluniverse.galaxy.StoreTransaction;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.io.UnsafeMemoryOutput;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.inject.Inject;
import org.apache.commons.lang3.StringUtils;
import org.distbc.data.structures.TempTable;
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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.stream.Collectors;

class DistBufferCacheResourceImpl implements DistBufferCacheResource {
    private static Logger logger = LoggerFactory.getLogger(DistBufferCacheResourceImpl.class);
    private static final Map<String, Long> namesToId = new ConcurrentHashMap<>();

    private final Grid grid;
    private final QueryParser queryParser;
    private final QueryPlanner queryPlanner;

    private static final ThreadFactory tf = new ThreadFactoryBuilder().setNameFormat("query-worker-%d").build();
    private static final ExecutorService es = Executors.newFixedThreadPool(3, tf);

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

    public Set<Object> query(String query) throws Exception {
        ParsingResult pr = queryParser.parse(query);
        logger.info("All the tables I want to access: {}", StringUtils.join(pr.getTableNames(), ", "));
        logger.info("All the columns I want to access: {}", StringUtils.join(pr.getProjectionColumnNames(), ", "));
        TempTable tuples = queryPlanner.generateQueryPlan(pr).execute(es);
        logger.info("#tuples {}", tuples.size());
        return tuples.keys().collect(Collectors.toSet());
    }
}
