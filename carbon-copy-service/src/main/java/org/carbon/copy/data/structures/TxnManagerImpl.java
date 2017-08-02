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

package org.carbon.copy.data.structures;

import co.paralleluniverse.galaxy.Store;
import com.google.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

import static org.carbon.copy.data.structures.DataStructure.TIMEOUT_SECS;

/**
 * Galaxy only provides inter-node synchronization. The application (us) needs to implement intra-node synchronization.
 * Today we create a new Txn object for each transaction we want to open. We map from data structures to byte[] ids
 * and the transaction itself only thinks in terms of ids. It pins, releases, and rolls back those ids instead of data structures.
 * I got around to implement a bookkeeping structure that let's us figure out whether a block has been locked locally already.
 * This way we're a) able to run multiple independent transactions concurrently and b) introduce a blocking call only when
 * a transaction wants a block that has been locked already.
 */
class TxnManagerImpl implements TxnManager {
    private static Logger logger = LoggerFactory.getLogger(TxnManagerImpl.class);
    // this contains the node-wide set of blocks that are engaged in a transaction
    // if a transaction tries to pin a block that is already in this set,
    // the transaction steps on somebody else's foot
    private static final ConcurrentHashMap<Long, LocalLock> blocksToLocalLocks = new ConcurrentHashMap<>(16, 0.75f, 8);

    private final Store store;

    @Inject
    TxnManagerImpl(Store store) {
        this.store = store;
    }

    public void doTransactionally(Consumer<Txn> lambda) throws IOException {
        Txn txn = beginTransaction();
        try {
            lambda.accept(txn);
            txn.commit();
        } catch (Exception xcp) {
            txn.rollback();
            throw new IOException(xcp);
        }
    }

    public Txn beginTransaction() {
        return new Txn(store, this);
    }

    // this is a blocking call!
    // getting a lock on a block in carbon-copy is a two-phase affair
    // there is a local lock that coordinates multiple threads inside the same node / JVM
    // and then there's the remote lock that is a galaxy feature ensuring that only one node in the cluster
    // has ownership of the block and is allowed to change it
    void lock(long blockId) {
        LocalLock ll = tryGetLocalLock(blockId);
        Future<byte[]> remoteFuture = tryGetRemoteLock(blockId);

        try {
            if (ll == null || ll.amIHoldingTheLock()) {
                // there was no previous local lock that means we have it
                remoteFuture.get(TIMEOUT_SECS, TimeUnit.SECONDS);
            } else {
                // somebody else is holding a lock on this block
                // that means we play the waiting game
                Future<Void> f = new LocalAndRemoteLockFuture(remoteFuture, ll);
                waitFor(f);
                // recursive call into myself in order to attempt
                lock(blockId);
            }
        } catch (InterruptedException | ExecutionException | TimeoutException xcp) {
            throw new RuntimeException(xcp);
        }
    }

    // visible for testing
    protected void waitFor(Future f) throws InterruptedException, ExecutionException, TimeoutException {
        f.get(TIMEOUT_SECS, TimeUnit.SECONDS);
    }

    private LocalLock tryGetLocalLock(long blockId) {
        return blocksToLocalLocks.putIfAbsent(blockId, new LocalLock(blockId));
    }

    private Future<byte[]> tryGetRemoteLock(long blockId) {
        return store.getxAsync(blockId, null);
    }

    // this method releases the local lock a thread has
    // that will enable other threads to grab the local lock and more changes to the
    // block that was previously locked
    // this implementation gives preference to other threads on this node who want to make updates
    // meaning before releasing the remote lock as well, we check whether other threads want the lock as well
    // if so, we pass on the local lock while retaining the remote lock
    void release(long blockId) {
        // release local lock first
        LocalLock ll = releaseLocalLock(blockId);
        // find out whether there's a waitlist for this block
        // we might piggy back on this remote lock while we have it
        if (!ll.hasThreadsWaiting()) {
            // release remote lock
            releaseRemoteLock(blockId);
        }
    }

    private LocalLock releaseLocalLock(long blockId) {
        LocalLock ll = blocksToLocalLocks.remove(blockId);
        ll.release();
        return ll;
    }

    private void releaseRemoteLock(long blockId) {
        store.release(blockId);
    }

    /**
     * Convenience class around a ReentrantLock. It practically makes the lock feel like a future.
     */
    private static class LocalLock {
        private final ReentrantLock innerLock = new ReentrantLock();
        private final long blockId;

        LocalLock(long blockId) {
            this.blockId = blockId;
            innerLock.lock();
        }

        boolean amIHoldingTheLock() {
            return innerLock.isHeldByCurrentThread();
        }

        boolean hasThreadsWaiting() {
            return innerLock.hasQueuedThreads();
        }

        String getCurrentLockOwner() {
            return blockId + " - " + innerLock.toString() + " - " + innerLock.getHoldCount();
        }

        void lock(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
            boolean haveLock = innerLock.tryLock(timeout, unit);
            // try to behave like a future
            if (!haveLock) {
                throw new TimeoutException("Couldn't acquire lock from " + getCurrentLockOwner());
            }
            if (innerLock.getHoldCount() > 1) {
                logger.warn("LocalLock {} is being acquired more than once", getCurrentLockOwner());
            }
        }

        void lock() throws InterruptedException, ExecutionException {
            innerLock.lockInterruptibly();
        }

        void release() {
            if (amIHoldingTheLock()) {
                innerLock.unlock();
                if (innerLock.getHoldCount() > 0) {
                    logger.warn("LocalLock {} is still being held more than once", getCurrentLockOwner());
                }
            } else {
                throw new IllegalStateException("Can't release a lock you don't own: " + getCurrentLockOwner());
            }
        }
    }

    /**
     * A convenience future that warps around a local lock and a remote lock.
     * It hugs both the remote future and the local lock and tries to provide an easier to use interface.
     * Looks like a future, behaves like a future, is a future.
     */
    private static class LocalAndRemoteLockFuture implements Future<Void> {
        private final Future<byte[]> remoteLockingFuture;
        private final LocalLock localLock;

        LocalAndRemoteLockFuture(Future<byte[]> remoteLockingFuture, LocalLock localLock) {
            this.remoteLockingFuture = remoteLockingFuture;
            this.localLock = localLock;
        }

        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            return remoteLockingFuture.cancel(mayInterruptIfRunning);
        }

        @Override
        public boolean isCancelled() {
            return remoteLockingFuture.isCancelled();
        }

        @Override
        public boolean isDone() {
            return remoteLockingFuture.isDone();
        }

        @Override
        public Void get() throws InterruptedException, ExecutionException {
            localLock.lock();
            remoteLockingFuture.get();
            return null;
        }

        @Override
        public Void get(long timeout, @Nonnull TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
            long before = System.currentTimeMillis();
            localLock.lock(timeout, unit);

            // lots of math in order to find out how much time is still left on the timeout
            // however we want to at least wait five more milliseconds though
            // maybe the remote future finished already and we can proceed and salvage the transaction
            long stillToWait = Math.max(
                    unit.toMillis(timeout) - (System.currentTimeMillis() - before),
                    5
            );

            remoteLockingFuture.get(stillToWait, TimeUnit.MILLISECONDS);
            return null;
        }
    }
}
