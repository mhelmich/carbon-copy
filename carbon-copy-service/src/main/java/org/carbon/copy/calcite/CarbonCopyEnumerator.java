package org.carbon.copy.calcite;

import org.apache.calcite.linq4j.Enumerator;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

class CarbonCopyEnumerator<T> implements Enumerator<T> {

    private AtomicBoolean cancelFlag;
    private Iterator<Object[]> iterator;
    private final Class<T> klass;
    private T current;

    CarbonCopyEnumerator(Stream<Object[]> stream, AtomicBoolean cancelFlag) {
        this(stream.iterator(), cancelFlag);
    }

    private CarbonCopyEnumerator(Iterator<Object[]> iterator, AtomicBoolean cancelFlag) {
        this.iterator = iterator;
        this.cancelFlag = cancelFlag;
        this.klass = getParamterKlass();
    }

    @SuppressWarnings("unchecked")
    private Class<T> getParamterKlass() {
        Type firstSooper = getClass().getGenericInterfaces()[0];
        sun.reflect.generics.reflectiveObjects.TypeVariableImpl parameterType = (sun.reflect.generics.reflectiveObjects.TypeVariableImpl) ((ParameterizedType)firstSooper).getActualTypeArguments()[0];
        return (Class<T>)parameterType.getBounds()[0];
    }

    @Override
    public T current() {
        return current;
    }

    @Override
    public boolean moveNext() {
        if (cancelFlag.get() || !iterator.hasNext()) return false;
        this.current = convert(iterator.next());
        return true;
    }

    // for a single-value tuple the (unwritten) contract says
    // that the value needs to be the object itself
    // for multi-value tuples the returned object
    // is supposed to be a array
    private T convert(Object[] o) {
        if (o.length == 1) {
            return klass.cast(o[0]);
        } else {
            return klass.cast(o);
        }
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
