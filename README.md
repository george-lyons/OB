Order book implementation with array based structure and insertion point binary search
Array based - contiguous in memory

* Add at level O(1)
* Add new level N log (N)
* Cancel O(1)

<<<<<<< Updated upstream
Improvements:

* Make non allocating. Use a Mutable level object and Mutable orders
* this can be zero GC but demonstrates the algorithms required to build an order book in array based fashion
=======
1. Cache Efficiency:

   •	Contiguous Memory Layout: Arrays in Java are stored in contiguous blocks of memory, which means that accessing sequential elements is highly cache-friendly. This leads to fewer cache misses and faster data retrieval, which is crucial for low-latency operations.
2. Predictable Access Times:

   •	O(1) Access: Accessing an element by index in an array is an O(1) operation, providing consistent and predictable performance. This is important in low-latency environments where predictability is as important as speed.
3. Low Overhead:

   •	Minimal Memory Overhead: Arrays have minimal overhead compared to more complex data structures like linked lists or hash maps. This reduced overhead contributes to faster processing times because there’s less work for the garbage collector and less memory management required.
4. Simplicity and Directness:

   •	Straightforward Data Management: Arrays provide a simple and direct way to manage order data. There’s no need to manage pointers or deal with hash collisions, which simplifies the implementation and reduces the potential for bugs or inefficiencies.
5. Efficient Sorting and Searching:

   •	Binary Search: Arrays allow for efficient binary search operations, which are O(log n) and can be easily optimized. This is particularly useful for quickly finding the right insertion or lookup points in an order book.
6. Low Garbage Collection Impact:

   •	Reduced Allocation/Deallocation: Since arrays are pre-allocated and reused, there’s less frequent need for allocation and deallocation of memory, reducing the impact of garbage collection, which can cause latency spikes in real-time systems.

### Access times for order book changes
* Add at a price level O(1) that exists
* Add new level. Best Case: O(1) (price level exists, and no shifts are needed). Worst case: O(N * log (N)) - Binary search for new insertion point, shift array across and save all index points
* Cancel - Best case:  O(1) - lookup the level and remove at existing level. Worst Case:  If delete a level must shift the array O(N)
* Replace O(N) - Best Case: O(1) (existing order replaced with no new level). Worst Case:log(n) * O(n) (order canceled, new order added, involving array shifts)

Alternatives to Improve Time Complexity:

To achieve better time complexities than O(n) for insertion and deletion operations, you would need to consider alternative data structures:

* Skip Lists: Offers O(log n) for both insertion and deletion, while maintaining order.
* Balanced Trees (e.g., AVL Tree, Red-Black Tree): Also provides O(log n) for insertion, deletion, and lookup, maintaining a sorted structure without the need for shifting elements.
* Hash Maps Combined with Linked Lists: Can provide O(1) insertion and deletion but might require additional management to maintain order.

However, these alternatives trade off some of the cache locality and simplicity that arrays offer. For a system where low-latency and predictable access times are paramount, the simplicity and cache-friendliness of an array might still be preferable, despite the O(n) worst-case scenarios.

### Price-Time Priority Matching Algorithm - NOT YET IMPLEMENTED

The price-time priority matching algorithm is a method used in financial markets to efficiently match buy and sell orders in an order book. The process works as follows:

- **Price Priority**:
    - Orders are first prioritized by price.
    - For buy orders (bids), higher prices are given priority.
    - For sell orders (offers), lower prices are given priority.

- **Time Priority**:
    - If multiple orders have the same price, they are prioritized based on the time they were received.
    - The earliest received orders (FIFO: First-In, First-Out) are matched first.

- **Matching Process**:
    - Orders are matched by comparing the best available buy and sell prices.
    - When a match occurs, the orders are executed, and the quantities are adjusted.
    - Partial matches are possible if the quantities do not fully match.

- **Order Book Management**:
    - Unmatched portions of orders remain in the order book until they can be fully matched or canceled.
    - The order book is continually updated to reflect the latest available orders and their priorities.

This algorithm ensures that the most competitive orders are matched first while maintaining fairness by honoring the sequence of order submissions.


### Improvements and TODOS:

Overall this is a basic implementation to demonstrate the algorithms

* Make non allocating. Use a Mutable level object and Mutable orders
* this can be zero GC but demonstrates the algorithms required to build an order book in array based fashion
* Exception handling around edge cases
* More tests to cover order book changes
* Improved benchmarks





>>>>>>> Stashed changes


