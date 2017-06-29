package org.carbon.copy.calcite;

import org.apache.calcite.linq4j.Enumerator;

import java.util.Iterator;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

class CarbonCopyEnumerator<T> implements Enumerator<T> {

    private AtomicBoolean cancelFlag;
    private Iterator<T> iterator;
    private T current;

    CarbonCopyEnumerator(Stream<T> stream, AtomicBoolean cancelFlag) {
        this(stream.iterator(), cancelFlag);
    }

    private CarbonCopyEnumerator(Iterator<T> iterator, AtomicBoolean cancelFlag) {
        this.iterator = iterator;
        this.cancelFlag = cancelFlag;
    }

    @Override
    public T current() {
        return current;
    }

    @Override
    public boolean moveNext() {
        if (cancelFlag.get() || !iterator.hasNext()) return false;
        this.current = iterator.next();
        return true;
    }

    @Override
    public void reset() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void close() {
        this.current = null;
        this.iterator = null;
        this.cancelFlag = null;
    }
}
