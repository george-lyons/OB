package com.orderbook;

import org.openjdk.jmh.annotations.*;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 5, time = 1)
@State(Scope.Thread)
@Fork(1)
public class ObBenchmark {

    private OrderBook orderBook;
    private OrderBook.Order bidOrder;
    private OrderBook.Order offerOrder;

    private OrderBook.Order cancelOrder;

    @Param({ "100", "500"})
    private int numOrders;

    @Setup(Level.Invocation)
    public void setup() {
        orderBook = new OrderBook();
        // Populate the order book with some initial orders
        for (long i = 1; i <= numOrders; i++) {
            boolean isBid = i % 2 == 0;
            long price = isBid ? 100 + ThreadLocalRandom.current().nextLong(-20, 0) : 100 + ThreadLocalRandom.current().nextLong(0, 20);
            orderBook.newOrder(new OrderBook.Order(i, price, 100, i % 2 == 0));
        }
        long orderId = numOrders + 1;
        bidOrder = new OrderBook.Order(orderId, 90, 200, true);
        offerOrder = new OrderBook.Order(orderId, 110, 200, false);

        long cancelOrderId = numOrders / 2; // Cancel an existing order in the middle
        cancelOrder= new OrderBook.Order(orderId, cancelOrderId, 100, orderId % 2 == 0);
    }

    @Benchmark
    public void testNewOrderBid() {
        orderBook.newOrder(bidOrder);
    }

    @Benchmark
    public void testNewOrderOffer() {
        orderBook.newOrder(offerOrder);
    }

    @Benchmark
    public void testCancelOrder() {
        orderBook.cancelOrder(cancelOrder);
    }

    @TearDown(Level.Invocation)
    public void tearDown() {
        // Clean up if necessary
    }

    public static void main(String[] args) throws Exception {
        org.openjdk.jmh.Main.main(args);
    }
}