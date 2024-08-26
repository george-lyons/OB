package com.pool;

public interface ObjectPool<T>{
    T borrow();
    void release(T release);
}
