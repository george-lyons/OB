package com.orderbook;

import org.agrona.collections.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Add at level O(1)
 * Add new level N log (N)
 * Cancel O(1)
 */
public final class OrderBook {
    private static final int MAX_LEVELS = 1_000;
    private static final int MISSING_VAL = -1;
    private final int MAX_PRICE_ARRAY = 1_000;

    private final List<PriceLevel> priceToBids = new ArrayList<>(MAX_PRICE_ARRAY);
    private final List<PriceLevel> priceToOffers  = new ArrayList<>(MAX_PRICE_ARRAY);

    private final IntArrayList priceToIndexBids = new IntArrayList (MAX_PRICE_ARRAY, MISSING_VAL);
    private final IntArrayList priceToIndexOffers= new IntArrayList  (MAX_PRICE_ARRAY, MISSING_VAL);

    private final PriceLevel [] bids = new PriceLevel[MAX_LEVELS];
    private final PriceLevel [] offers = new PriceLevel[MAX_LEVELS];

    private int bidsSize = 0;
    private int offersSize = 0;

    public OrderBook() {
        for (int i = 0; i < MAX_PRICE_ARRAY; i++) {
            priceToBids.add(null);
            priceToOffers.add(null);
            priceToIndexBids.add(MISSING_VAL);
            priceToIndexOffers.add(MISSING_VAL);
        }
    }

    public void newOrder(Order order) {
        assert order.price > 0 && order.price <= MAX_PRICE_ARRAY : "Price out of bounds: " + order.price;
        if(order.isBid) {
            newBid(order);
        } else {
            newOffer(order);
        }
    }

    public void cancelOrder(Order order) {
        assert order.price > 0 && order.price <= MAX_PRICE_ARRAY : "Price out of bounds: " + order.price;
        //lookup O(1) and remove
        //lookup level, remove from level
        //if level empty remove, need to lookup index and shift array
        if(order.isBid) {
            cancelBid(order);
        } else {
            cancelOffer(order);
        }
    }

    public void replaceOrder(Order order) {
        cancelOrder(order);
        newOrder(order);
    }

    private void newBid(Order order) {
        if(priceToBids.get((int) order.price) != null) {
            PriceLevel level = priceToBids.get((int) order.price);
            level.addOrder(order);
        } else {
            //otherwise find insertion point log (N) binary search
            //shift rest of array
            int insertionPoint = findInsertionPoint(bids, true, order.price);
            PriceLevel level = new PriceLevel(order.price);
            level.addOrder(order);
            addNewLevel(bids, insertionPoint, bidsSize, level, true);
            priceToBids.set((int) order.price, level);
            bidsSize++;
        }
    }

    private void newOffer(Order order) {
        if(priceToOffers.get((int) order.price) != null) {
            PriceLevel level = priceToOffers.get((int) order.price);
            level.addOrder(order);
        } else {
            int insertionPoint = findInsertionPoint(offers, false, order.price);
            PriceLevel level = new PriceLevel(order.price);
            level.addOrder(order);
            addNewLevel(offers, insertionPoint, offersSize, level, false);
            priceToOffers.set((int) order.price, level);
            offersSize++;
        }
    }


    private int findInsertionPoint(PriceLevel[] levels, boolean isBid, long price) {
        // Binary search
        int left = 0;
        int right = (isBid ? bidsSize : offersSize) - 1;

        while (left <= right) {
            int mid = (left + right) >>> 1;
            long midPrice = levels[mid].price;

            if (midPrice == price) {
                return mid; // Exact match found
            } else if (isBid ? (midPrice < price) : (midPrice > price)) {
                right = mid - 1; // Search left half
            } else {
                left = mid + 1; // Search right half
            }
        }

        // Insertion point
        return left;
    }

    private void addNewLevel(PriceLevel[] levels, int index, int length, PriceLevel newLevel, boolean isBid) {
        // Shift array elements to the right and update index mappings in a single loop
        for (int i = length; i > index; i--) {
            levels[i] = levels[i - 1]; // Shift element to the right
            long priceMove = levels[i].price; // Get the price of the shifted element
            if (isBid) {
                priceToIndexBids.set((int) priceMove, i); // Update the index in bids map
            } else {
                priceToIndexOffers.set((int) priceMove, i); // Update the index in offers map
            }
        }
        // Insert the new price level and update the index
        levels[index] = newLevel;
        if (isBid) {
            priceToIndexBids.set((int) newLevel.price, index);
        } else {
            priceToIndexOffers.set((int) newLevel.price, index);
        }
    }

    private void removeLevel(PriceLevel[] levels, int index, int length, boolean isBid) {
        // Shift elements to the left to fill the gap created by the removed level
        if (index < length - 1) {
            for (int i = index + 1; i < length; i++) {
                long priceMove = levels[i].price;
                // Update the index mappings to reflect the new positions
                if (isBid) {
                    priceToIndexBids.set((int) priceMove, i - 1);
                } else {
                    priceToIndexOffers.set((int) priceMove, i - 1);
                }
                // Shift the element to the left
                levels[i - 1] = levels[i];
            }
        }
        // Nullify the last element, which has been moved left
        levels[length - 1] = null;
    }

    private void cancelOffer(Order order) {
        PriceLevel level = priceToOffers.get((int)order.price);
        if(level != null) {
            level.removeOrder(order.orderId);
            if(level.totalNotional <= 0) {
                priceToOffers.set((int) order.price, null);
                int index = priceToIndexOffers.get((int)order.price);
                removeLevel(offers, index, offersSize, order.isBid);
                priceToIndexOffers.set((int)order.price, MISSING_VAL);
                offersSize--;
            }
        }
    }
    private void cancelBid(Order order) {
        PriceLevel level = priceToBids.get((int)order.price);
        if(level != null) {
            level.removeOrder(order.orderId);
            if(level.totalNotional <= 0) {
                priceToBids.set((int) order.price, null);
                int index = priceToIndexBids.get((int)order.price);
                removeLevel(bids, index, bidsSize, order.isBid);
                priceToIndexBids.set((int)order.price, MISSING_VAL);
                bidsSize--;
            }
        }
    }


    static class PriceLevel {
        long price;
        long totalNotional;

        private final Long2ObjectHashMap<Order> idToOrder = new Long2ObjectHashMap();
        private final LongArrayList idOrdering = new LongArrayList();

        private PriceLevel(long price) {
            this.price = price;
        }

        void addOrder(Order order) {
            assert order.price == price : "Price Must match";
            idOrdering.add(order.orderId);
            idToOrder.put(order.orderId, order);
            totalNotional += order.quantity;
        }

        boolean removeOrder(long orderId) {
            final Order order = idToOrder.remove(orderId);
            if(order == null) {
                return false;
            }
            //TODO this loops through, but only for orders at level, need to maintain order here
            idOrdering.removeIf((i) -> i == orderId);
            totalNotional -= order.quantity;
            return true;
        }

        boolean isEmpty() {
            return idToOrder.isEmpty();
        }
    }

    static class Order {
        long orderId;
        long price;
        long quantity;
        boolean isBid;

        Order(long orderId, long price, long quantity, boolean idBid) {
            this.orderId = orderId;
            this.price = price;
            this.quantity = quantity;
            this.isBid = idBid;
        }
    }

    public void appendTo(StringBuilder sb) {
        sb.append("Bids: [");
        for(int i =0; i < bidsSize; i++) {
            if (bids[i] != null) {
                sb.append(bids[i].price).append(" ").append(bids[i].totalNotional).append(",");
            }
        }
        sb.deleteCharAt(sb.length() - 1);
        sb.append("]");
        sb.append(" Offers: [");
        for(int i =0; i < offersSize; i++) {
            if (offers[i] != null) {
                sb.append(offers[i] .price).append(" ").append(offers[i].totalNotional).append(",");
            }
        }
        sb.deleteCharAt(sb.length() - 1);
        sb.append("]");
    }

}