package com.pool;

import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;

class FixedArrayObjectPoolTest {
    private FixedObjectPool<TestMutableInt> fixedObjectPool;
    private int counterObject = 1;



    private static class TestMutableInt implements Mutable {
        public void setVal(int val) {
            this.val = val;
        }

        private int val;

        public TestMutableInt(int val) {
            this.val = val;
        }

        @Override
        public void reset() {
            val = 0;
        }

        public int getVal() {
            return val;
        }
    }

    @BeforeEach
    void before() {

    }

    @Test
    void borrow() {
        Supplier<TestMutableInt> supplier = () -> new TestMutableInt(++counterObject);
        fixedObjectPool = new FixedObjectPool<>(3, supplier, TestMutableInt.class);
        assertEquals(3, fixedObjectPool.getRemaining());
        verifyBorrow(3);
    }


    @Test
    void borrowAndRelease() {
        Supplier<TestMutableInt> supplier = () -> new TestMutableInt(++counterObject);
        fixedObjectPool = new FixedObjectPool<>(3, supplier, TestMutableInt.class);
        assertEquals(3, fixedObjectPool.getRemaining());
        TestMutableInt borrowed = fixedObjectPool.borrow();
        assertEquals(2, fixedObjectPool.getRemaining());
        fixedObjectPool.release(borrowed);
        assertEquals(3, fixedObjectPool.getRemaining());
    }

    @Test
    void borrowAndReleasePastCapacity() {
        Supplier<TestMutableInt> supplier = () -> new TestMutableInt(++counterObject);
        fixedObjectPool = new FixedObjectPool<>(3, supplier, TestMutableInt.class);
        assertEquals(3, fixedObjectPool.getRemaining());
        TestMutableInt borrowed = fixedObjectPool.borrow();
        Assert.assertEquals(3, borrowed.val);
        assertEquals(2, fixedObjectPool.getRemaining());
        fixedObjectPool.release(borrowed);
        assertEquals(3, fixedObjectPool.getRemaining());

        assertThrows(IllegalStateException.class, () -> {
            fixedObjectPool.release(borrowed);
        });
    }

    @Test
    void borrowCheckReset() {
        Supplier<TestMutableInt> supplier = () -> new TestMutableInt(0);
        fixedObjectPool = new FixedObjectPool<>(3, supplier, TestMutableInt.class);
        TestMutableInt testMutableInt = fixedObjectPool.borrow();
        assertEquals(0, testMutableInt.val);
        testMutableInt.setVal(5);
        assertEquals(5, testMutableInt.val);
        fixedObjectPool.release(testMutableInt);
        assertEquals(0, testMutableInt.val);
    }

    private void verifyBorrow(int i) {
        TestMutableInt borrowed = fixedObjectPool.borrow();
        assertEquals((i), borrowed.val);
    }

    @Test
    void borrowPastCapacity() {
        Supplier<TestMutableInt> supplier = () -> new TestMutableInt(0);
        fixedObjectPool = new FixedObjectPool<>(3, supplier, TestMutableInt.class);
        verifyBorrow(0);
        verifyBorrow(0);
        assertThrows(AssertionError.class, () -> {
            assertNull(fixedObjectPool.borrow());
        });
    }

}