Order book implementation with array based structure and insertion point binary search
Array based - contiguous in memory

* Add at level O(1)
* Add new level N log (N)
* Cancel O(1)

Improvements:

* Make non allocating. Use a Mutable level object and Mutable orders
* this can be zero GC but demonstrates the algorithms required to build an order book in array based fashion


