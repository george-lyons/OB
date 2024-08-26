package com.orderbook;

import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


import static org.junit.jupiter.api.Assertions.*;

class OrderBookTest {
    private OrderBook orderBook;

    private OrderBook.Order mutableNewOrder(long id, long price, long notiona, boolean isBouy) {
        final OrderBook.Order order = new OrderBook.Order(id, price, notiona, isBouy);
        return order;
    }

    @BeforeEach
    public void before() {
        orderBook = new OrderBook();
    }

    private void createBook() {
        OrderBook.Order order1 = mutableNewOrder(123, 101L, 10000L, true);
        orderBook.newOrder(order1);
        OrderBook.Order order2 = mutableNewOrder(124, 103L, 10000L,  true);
        orderBook.newOrder(order2);
        OrderBook.Order order3 = mutableNewOrder(125, 106L, 10000L, false);
        orderBook.newOrder(order3);
        OrderBook.Order order4 = mutableNewOrder(126, 106L, 10000L,  false);
        orderBook.newOrder(order4);
        OrderBook.Order order5 = mutableNewOrder(127, 109L, 10000L,  false);
        orderBook.newOrder(order5);
        OrderBook.Order order6 = mutableNewOrder(126, 107L, 10000L,  false);
        orderBook.newOrder(order6);
    }


    @Test
    void addPastFullBookIsManagedCleanly() {
        StringBuilder sb = new StringBuilder();
        orderBook.appendTo(sb);
        System.out.println(sb);
        Assert.assertEquals("Bids: [103 10000,101 10000] Offers: [106 20000,107 10000,109 10000]", sb.toString());
    }


    @Test
    void addBidsAndOffersAggregated() {
        createBook();
        StringBuilder sb = new StringBuilder();
        orderBook.appendTo(sb);
        System.out.println(sb);
        Assert.assertEquals("Bids: [103 10000,101 10000] Offers: [106 20000,107 10000,109 10000]", sb.toString());
    }


    @Test
    void cancelAtVariousLevelsOffers() {

        OrderBook.Order order0 = mutableNewOrder(122, 102L, 10000L, true);
        orderBook.newOrder(order0);
        OrderBook.Order order1 = mutableNewOrder(123, 101L, 10000L, true);
        orderBook.newOrder(order1);
        OrderBook.Order order2 = mutableNewOrder(124, 103L, 10000L,  true);
        orderBook.newOrder(order2);


        OrderBook.Order order3 = mutableNewOrder(125, 106L, 10000L, false);
        orderBook.newOrder(order3);
        OrderBook.Order order4 = mutableNewOrder(126, 106L, 10000L,  false);
        orderBook.newOrder(order4);
        OrderBook.Order order5 = mutableNewOrder(127, 109L, 10000L,  false);
        orderBook.newOrder(order5);
        OrderBook.Order order6 = mutableNewOrder(128, 107L, 10000L,  false);
        orderBook.newOrder(order6);

        StringBuilder sb = new StringBuilder();
        orderBook.appendTo(sb);
        System.out.println(sb);


        assertEquals("Bids: [103 10000,102 10000,101 10000] Offers: [106 20000,107 10000,109 10000]", sb.toString());
        sb.setLength(0);
        orderBook.cancelOrder(order6);
        orderBook.appendTo(sb);
        System.out.println(sb);
        assertEquals("Bids: [103 10000,102 10000,101 10000] Offers: [106 20000,109 10000]", sb.toString());
        sb.setLength(0);

        orderBook.cancelOrder(order3);
        orderBook.appendTo(sb);
        System.out.println(sb);
        assertEquals("Bids: [103 10000,102 10000,101 10000] Offers: [106 10000,109 10000]", sb.toString());
        sb.setLength(0);

        OrderBook.Order order7 = mutableNewOrder(129, 107L, 10000L,  false);
        orderBook.newOrder(order7);
        orderBook.appendTo(sb);

        System.out.println(sb);
        assertEquals("Bids: [103 10000,102 10000,101 10000] Offers: [106 10000,107 10000,109 10000]", sb.toString());
        sb.setLength(0);
    }



    @Test
    void cancelAtVariousLevelsOffersBids() {
        OrderBook.Order order0 = mutableNewOrder(122, 102L, 10000L, true);
        orderBook.newOrder(order0);
        OrderBook.Order order1 = mutableNewOrder(123, 101L, 10000L, true);
        orderBook.newOrder(order1);
        OrderBook.Order order2 = mutableNewOrder(124, 103L, 10000L,  true);
        orderBook.newOrder(order2);


        OrderBook.Order order3 = mutableNewOrder(125, 106L, 10000L, false);
        orderBook.newOrder(order3);
        OrderBook.Order order4 = mutableNewOrder(126, 106L, 10000L,  false);
        orderBook.newOrder(order4);
        OrderBook.Order order5 = mutableNewOrder(127, 109L, 10000L,  false);
        orderBook.newOrder(order5);
        OrderBook.Order order6 = mutableNewOrder(128, 107L, 10000L,  false);
        orderBook.newOrder(order6);

        StringBuilder sb = new StringBuilder();
        orderBook.appendTo(sb);
        System.out.println(sb);

        assertEquals("Bids: [103 10000,102 10000,101 10000] Offers: [106 20000,107 10000,109 10000]", sb.toString());
        sb.setLength(0);

        orderBook.cancelOrder(order0);
        orderBook.appendTo(sb);
        System.out.println(sb);
        assertEquals("Bids: [103 10000,101 10000] Offers: [106 20000,107 10000,109 10000]", sb.toString());
        sb.setLength(0);

        OrderBook.Order order7 = mutableNewOrder(129, 105, 10000L,  true);
        orderBook.newOrder(order7);
        orderBook.appendTo(sb);

        System.out.println(sb);
        assertEquals("Bids: [105 10000,103 10000,101 10000] Offers: [106 20000,107 10000,109 10000]", sb.toString());
        sb.setLength(0);

        OrderBook.Order order8 = mutableNewOrder(130, 104, 10000L,  true);
        orderBook.newOrder(order8);
        orderBook.appendTo(sb);
        assertEquals("Bids: [105 10000,104 10000,103 10000,101 10000] Offers: [106 20000,107 10000,109 10000]", sb.toString());
    }

}