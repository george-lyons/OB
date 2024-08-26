package com.orderbook;

import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class OrderBookMutableTest {
    private OrderBookMutable orderBookNonMutable;

    private OrderBookMutable.Order mutableNewOrder(long id, long price, long notiona, boolean isBouy) {
        final OrderBookMutable.Order order = new OrderBookMutable.Order();
        order.setOrderId(id);
        order.setPrice(price);
        order.setQuantity(notiona);
        order.setBid(isBouy);
        return order;
    }

    @BeforeEach
    public void before() {
        orderBookNonMutable = new OrderBookMutable();
    }

    private void createBook() {
        OrderBookMutable.Order order1 = mutableNewOrder(123, 101L, 10000L, true);
        orderBookNonMutable.newOrder(order1);
        OrderBookMutable.Order order2 = mutableNewOrder(124, 103L, 10000L,  true);
        orderBookNonMutable.newOrder(order2);
        OrderBookMutable.Order order3 = mutableNewOrder(125, 106L, 10000L, false);
        orderBookNonMutable.newOrder(order3);
        OrderBookMutable.Order order4 = mutableNewOrder(126, 106L, 10000L,  false);
        orderBookNonMutable.newOrder(order4);
        OrderBookMutable.Order order5 = mutableNewOrder(127, 109L, 10000L,  false);
        orderBookNonMutable.newOrder(order5);
        OrderBookMutable.Order order6 = mutableNewOrder(126, 107L, 10000L,  false);
        orderBookNonMutable.newOrder(order6);
    }


    @Test
    void addBidsAndOffersAggregated() {
        createBook();
        StringBuilder sb = new StringBuilder();
        orderBookNonMutable.appendTo(sb);
        System.out.println(sb);
        Assert.assertEquals("Bids: [103 10000,101 10000] Offers: [106 20000,107 10000,109 10000]", sb.toString());
    }


    @Test
    void cancelAtVariousLevelsOffers() {

        OrderBookMutable.Order order0 = mutableNewOrder(122, 102L, 10000L, true);
        orderBookNonMutable.newOrder(order0);
        OrderBookMutable.Order order1 = mutableNewOrder(123, 101L, 10000L, true);
        orderBookNonMutable.newOrder(order1);
        OrderBookMutable.Order order2 = mutableNewOrder(124, 103L, 10000L,  true);
        orderBookNonMutable.newOrder(order2);


        OrderBookMutable.Order order3 = mutableNewOrder(125, 106L, 10000L, false);
        orderBookNonMutable.newOrder(order3);
        OrderBookMutable.Order order4 = mutableNewOrder(126, 106L, 10000L,  false);
        orderBookNonMutable.newOrder(order4);
        OrderBookMutable.Order order5 = mutableNewOrder(127, 109L, 10000L,  false);
        orderBookNonMutable.newOrder(order5);
        OrderBookMutable.Order order6 = mutableNewOrder(128, 107L, 10000L,  false);
        orderBookNonMutable.newOrder(order6);

        StringBuilder sb = new StringBuilder();
        orderBookNonMutable.appendTo(sb);
        System.out.println(sb);


        assertEquals("Bids: [103 10000,102 10000,101 10000] Offers: [106 20000,107 10000,109 10000]", sb.toString());
        sb.setLength(0);
        orderBookNonMutable.cancelOrder(order6);
        orderBookNonMutable.appendTo(sb);
        System.out.println(sb);
        assertEquals("Bids: [103 10000,102 10000,101 10000] Offers: [106 20000,109 10000]", sb.toString());
        sb.setLength(0);

        orderBookNonMutable.cancelOrder(order3);
        orderBookNonMutable.appendTo(sb);
        System.out.println(sb);
        assertEquals("Bids: [103 10000,102 10000,101 10000] Offers: [106 10000,109 10000]", sb.toString());
        sb.setLength(0);

        OrderBookMutable.Order order7 = mutableNewOrder(129, 107L, 10000L,  false);
        orderBookNonMutable.newOrder(order7);
        orderBookNonMutable.appendTo(sb);

        System.out.println(sb);
        assertEquals("Bids: [103 10000,102 10000,101 10000] Offers: [106 10000,107 10000,109 10000]", sb.toString());
        sb.setLength(0);
    }



    @Test
    void cancelAtVariousLevelsOffersBids() {
        OrderBookMutable.Order order0 = mutableNewOrder(122, 102L, 10000L, true);
        orderBookNonMutable.newOrder(order0);
        OrderBookMutable.Order order1 = mutableNewOrder(123, 101L, 10000L, true);
        orderBookNonMutable.newOrder(order1);
        OrderBookMutable.Order order2 = mutableNewOrder(124, 103L, 10000L,  true);
        orderBookNonMutable.newOrder(order2);


        OrderBookMutable.Order order3 = mutableNewOrder(125, 106L, 10000L, false);
        orderBookNonMutable.newOrder(order3);
        OrderBookMutable.Order order4 = mutableNewOrder(126, 106L, 10000L,  false);
        orderBookNonMutable.newOrder(order4);
        OrderBookMutable.Order order5 = mutableNewOrder(127, 109L, 10000L,  false);
        orderBookNonMutable.newOrder(order5);
        OrderBookMutable.Order order6 = mutableNewOrder(128, 107L, 10000L,  false);
        orderBookNonMutable.newOrder(order6);

        StringBuilder sb = new StringBuilder();
        orderBookNonMutable.appendTo(sb);
        System.out.println(sb);

        assertEquals("Bids: [103 10000,102 10000,101 10000] Offers: [106 20000,107 10000,109 10000]", sb.toString());
        sb.setLength(0);

        orderBookNonMutable.cancelOrder(order0);
        orderBookNonMutable.appendTo(sb);
        System.out.println(sb);
        assertEquals("Bids: [103 10000,101 10000] Offers: [106 20000,107 10000,109 10000]", sb.toString());
        sb.setLength(0);

        OrderBookMutable.Order order7 = mutableNewOrder(129, 105, 10000L,  true);
        orderBookNonMutable.newOrder(order7);
        orderBookNonMutable.appendTo(sb);

        System.out.println(sb);
        assertEquals("Bids: [105 10000,103 10000,101 10000] Offers: [106 20000,107 10000,109 10000]", sb.toString());
        sb.setLength(0);

        OrderBookMutable.Order order8 = mutableNewOrder(130, 104, 10000L,  true);
        orderBookNonMutable.newOrder(order8);
        orderBookNonMutable.appendTo(sb);
        assertEquals("Bids: [105 10000,104 10000,103 10000,101 10000] Offers: [106 20000,107 10000,109 10000]", sb.toString());
    }

}