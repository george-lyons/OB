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
public class ObMutBenchmark {

    private OrderBookMutable orderBookMutable;
    private OrderBookMutable.Order bidOrder;
    private OrderBookMutable.Order offerOrder;
    private OrderBookMutable.Order cancelOrder;

    @Param({ "100", "500"})
    private int numOrders;

    @Setup(Level.Invocation)
    public void setup() {
        orderBookMutable = new OrderBookMutable();
        // Populate the order book with some initial orders
        for (long i = 1; i <= numOrders; i++) {
            boolean isBid = i % 2 == 0;
            long price = isBid ? 100 + ThreadLocalRandom.current().nextLong(-20, 0) : 100 + ThreadLocalRandom.current().nextLong(0, 20);
            orderBookMutable.newOrder(mutableNewOrder(i, price, 100, i % 2 == 0));
        }
        long orderId = numOrders + 1;

        bidOrder = mutableNewOrder(orderId, 90, 200, true);
        offerOrder = mutableNewOrder(orderId, 110, 200, false);

        long cancelOrderId = numOrders / 2; // Cancel an existing order in the middle
        cancelOrder= mutableNewOrder(orderId, cancelOrderId, 100, orderId % 2 == 0);
    }

    private OrderBookMutable.Order mutableNewOrder(long id, long price, long notiona, boolean isBouy) {
        final OrderBookMutable.Order order = new OrderBookMutable.Order();
        order.setOrderId(id);
        order.setPrice(price);
        order.setQuantity(notiona);
        order.setBid(isBouy);
        return order;
    }


    @Benchmark
    public void testNewOrderBid() {
        orderBookMutable.newOrder(bidOrder);
    }

    @Benchmark
    public void testNewOrderOffer() {
        orderBookMutable.newOrder(offerOrder);
    }

    @Benchmark
    public void testCancelOrder() {
        orderBookMutable.cancelOrder(cancelOrder);
    }

    @TearDown(Level.Invocation)
    public void tearDown() {
        // Clean up if necessary
    }

    public static void main(String[] args) throws Exception {
        org.openjdk.jmh.Main.main(args);
    }
}