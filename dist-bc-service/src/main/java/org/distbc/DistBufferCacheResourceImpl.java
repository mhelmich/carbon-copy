package org.distbc;

import co.paralleluniverse.galaxy.Grid;
import co.paralleluniverse.galaxy.Store;
import co.paralleluniverse.galaxy.StoreTransaction;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.io.UnsafeMemoryOutput;
import com.esotericsoftware.kryo.pool.KryoCallback;
import com.esotericsoftware.kryo.pool.KryoPool;
import com.google.common.base.Optional;
import org.distbc.data.structures.SkipList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by mhelmich on 9/30/16.
 */
public class DistBufferCacheResourceImpl implements DistBufferCacheResource {

    private static Logger logger = LoggerFactory.getLogger(DistBufferCacheResourceImpl.class);

    private static final Map<String, Long> namesToId = new ConcurrentHashMap<>();

    private final Grid grid;
    private final KryoPool kryoPool;

    DistBufferCacheResourceImpl(Grid grid, KryoPool kryoPool) {
        this.grid = grid;
        this.kryoPool = kryoPool;
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

        kryoPool.run(new KryoCallback<Void>() {
            public Void execute(Kryo kryo) {
                kryo.writeClassAndObject(output, sl);
                return null;
            }
        });

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

    public String query(Optional<String> name) {
        return "all zeros";
    }
}
