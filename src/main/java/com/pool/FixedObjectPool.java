package com.pool;

import java.lang.reflect.Array;
import java.util.function.Function;
import java.util.function.Supplier;


public class FixedObjectPool<T extends Mutable> implements ObjectPool<T> {
    private T[] objects;
    private int availableIndex;
    private int capacity;

    public FixedObjectPool(int initialCapacity, Supplier<T> factory, Class<T> type) {
        objects = (T[]) Array.newInstance(type, initialCapacity);
        for (int i = 0; i < initialCapacity; i++) {
            objects[i] = factory.get();
        }
        this.capacity = initialCapacity;
        this.availableIndex = capacity - 1;
    }

    @Override
    public T borrow() {
        if(availableIndex < 1) {
            assert false  : "Nothing left";
        }
        return objects[--availableIndex];
    }

    @Override
    public void release(T release) {
        release.reset();
        if(availableIndex == capacity - 1) {
            //capacity to reallocate
            throw new IllegalStateException("capacity full");
        } else {
            objects[availableIndex++] = release;
        }
    }

    public int getCapacity() {
        return objects.length;
    }
    public int getRemaining() {
        return availableIndex + 1;
    }
}
