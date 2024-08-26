package com.orderbook;

import com.pool.FixedObjectPool;
import com.pool.Mutable;
import org.agrona.collections.IntArrayList;
import org.agrona.collections.Long2ObjectHashMap;

import java.util.ArrayList;
import java.util.List;


public class OrderBookMutable {
    private static final int MAX_LEVELS = 20000;
    private static final int MISSING_VAL = -1;
    private final int MAX_PRICE_ARRAY = 10_000;

    private final List<MutablePriceLevel> priceToBids = new ArrayList<>(MAX_PRICE_ARRAY);
    private final List<MutablePriceLevel> priceToOffers  = new ArrayList<>(MAX_PRICE_ARRAY);

    private final IntArrayList priceToIndexBids = new IntArrayList (MAX_PRICE_ARRAY, MISSING_VAL);
    private final IntArrayList priceToIndexOffers= new IntArrayList  (MAX_PRICE_ARRAY, MISSING_VAL);

    private final MutablePriceLevel [] bids = new MutablePriceLevel[MAX_LEVELS];
    private final MutablePriceLevel [] offers = new MutablePriceLevel[MAX_LEVELS];

    private int bidsSize = 0;
    private int offersSize = 0;
    private final FixedObjectPool<MutablePriceLevel> fixedObjectPool;

    public OrderBookMutable() {
        for (int i = 0; i < MAX_PRICE_ARRAY; i++) {
            priceToBids.add(null);
            priceToOffers.add(null);
            priceToIndexBids.add(MISSING_VAL);
            priceToIndexOffers.add(MISSING_VAL);
        }
        this.fixedObjectPool = new FixedObjectPool<>(MAX_LEVELS * 2, MutablePriceLevel::new, MutablePriceLevel.class);
    }

    public void newOrder(Order order) {
        assert order.price > 0 && order.price <= MAX_PRICE_ARRAY : "Price out of bounds: " + order.price;
        if(order.isBid) {
            newBid(order);
        } else {
            newOffer(order);
        }
    }

    private void newBid(Order order) {
        if(priceToBids.get((int) order.price) != null) {
            MutablePriceLevel level = priceToBids.get((int) order.price);
            level.addOrder(order);
        } else {
            //otherwise find insertion point log (N) binary search
            //shift rest of array
            int insertionPoint = findInsertionPoint(bids, true, order.price);
            MutablePriceLevel level = fixedObjectPool.borrow();

            level.setPrice(order.price);
            level.addOrder(order);
            addNewLevel(bids, insertionPoint, bidsSize, level, true);
            priceToBids.set((int) order.price, level);
            bidsSize++;
        }
    }

    private void newOffer(Order order) {
        if(priceToOffers.get((int) order.price) != null) {
            MutablePriceLevel level = priceToOffers.get((int) order.price);
            level.addOrder(order);
        } else {
            int insertionPoint = findInsertionPoint(offers, false, order.price);
            MutablePriceLevel level = fixedObjectPool.borrow();
            level.setPrice(order.price);
            level.addOrder(order);
            addNewLevel(offers, insertionPoint, offersSize, level, false);
            priceToOffers.set((int) order.price, level);
            offersSize++;
        }
    }


    private int findInsertionPoint(MutablePriceLevel[] levels, boolean isBid, long price) {
        // Binary search
        int left = 0;
        int right = (isBid ? bidsSize : offersSize) - 1;

        while (left <= right) {
            int mid = (left + right) >>> 1;
            long midPrice = levels[mid].getPrice();

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

    private void addNewLevel(MutablePriceLevel[] levels, int index, int length, MutablePriceLevel newLevel, boolean isBid) {
        // Shift array elements to the right and update index mappings in a single loop
        for (int i = length; i > index; i--) {
            levels[i] = levels[i - 1]; // Shift element to the right
            long priceMove = levels[i].getPrice(); // Get the price of the shifted element
            if (isBid) {
                priceToIndexBids.set((int) priceMove, i); // Update the index in bids map
            } else {
                priceToIndexOffers.set((int) priceMove, i); // Update the index in offers map
            }
        }
        // Insert the new price level and update the index
        levels[index] = newLevel;
        if (isBid) {
            priceToIndexBids.set((int) newLevel.getPrice(), index);
        } else {
            priceToIndexOffers.set((int) newLevel.getPrice(), index);
        }
    }

    private void removeLevel(MutablePriceLevel[] levels, int index, int length, boolean isBid) {
        // Shift elements to the left to fill the gap created by the removed level
        MutablePriceLevel levelToRemove = levels[index];

        if (index < length - 1) {
            for (int i = index + 1; i < length; i++) {
                long priceMove = levels[i].getPrice();
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
        // Nullify the last element, which has been moved left, release level to remove
        fixedObjectPool.release(levelToRemove);
        levels[length - 1] = null;
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

    private void cancelOffer(Order order) {
        MutablePriceLevel level = priceToOffers.get((int)order.price);
        if(level != null) {
            level.removeOrder(order.orderId);
            if(level.getTotalNotional() <= 0) {
                priceToOffers.set((int) order.price, null);
                int index = priceToIndexOffers.get((int)order.price);
                removeLevel(offers, index, offersSize, order.isBid);
                priceToIndexOffers.set((int)order.price, MISSING_VAL);
                offersSize--;
            }
        }
    }
    private void cancelBid(Order order) {
        MutablePriceLevel level = priceToBids.get((int)order.price);
        if(level != null) {
            level.removeOrder(order.orderId);
            if(level.getTotalNotional() <= 0) {
                priceToBids.set((int) order.price, null);
                int index = priceToIndexBids.get((int)order.price);
                removeLevel(bids, index, bidsSize, order.isBid);
                priceToIndexBids.set((int)order.price, MISSING_VAL);
                bidsSize--;
            }
        }
    }

    public void replaceOrder(Order order) {
        cancelOrder(order);
        newOrder(order);
    }


    private static class MutablePriceLevel  implements Mutable {
        private long price;
        private long totalNotional;

        private final Long2ObjectHashMap<OrderBookMutable.Order> idToOrder = new Long2ObjectHashMap();

        //todo consider the complexity to remove, should jnot be deep
//        private final LongArrayList idOrdering = new LongArrayList();

        public void addOrder(OrderBookMutable.Order order) {
            assert order.price == price : "Price Must match";
//            idOrdering.add(order.orderId);
            idToOrder.put(order.orderId, order);
            totalNotional += order.quantity;
        }

        public boolean removeOrder(long orderId) {
            final OrderBookMutable.Order order = idToOrder.remove(orderId);
            if(order == null) {
                return false;
            }
//            idOrdering.removeIf( (i) -> i == orderId);
            totalNotional -= order.quantity;
            return true;
        }

        boolean isEmpty() {
            return idToOrder.isEmpty();
        }

        public void setPrice(long price) {
            this.price = price;
        }

        public long getPrice() {
            return price;
        }

        public long getTotalNotional() {
            return totalNotional;
        }

        public void reset() {
            price = 0;
            totalNotional = 0;
            idToOrder.clear();
        }
    }
    public static class Order {
        public long orderId;
        public long price;
        public long quantity;
        public boolean isBid;

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
                sb.append(bids[i].getPrice()).append(" ").append(bids[i].getTotalNotional()).append(",");
            }
        }
        sb.deleteCharAt(sb.length() - 1);
        sb.append("]");
        sb.append(" Offers: [");
        for(int i =0; i < offersSize; i++) {
            if (offers[i] != null) {
                sb.append(offers[i] .getPrice()).append(" ").append(offers[i].getTotalNotional()).append(",");
            }
        }
        sb.deleteCharAt(sb.length() - 1);
        sb.append("]");
    }

}