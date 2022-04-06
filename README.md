# Peculiar DBMS Pattern

## FIFO pattern for conservative locking protocol
The implementation of [Elasql's conservative lock table](https://github.com/elasql/elasql/tree/master/src/main/java/org/elasql/storage/tx/concurrency) has room for improvement.

Elasql forces each worker thread that wants to access the same record to wait on the same object.
Once a thread commits and releases the locks,
the committed thread has the responsibility to NOTIFY ALL other threads that intend to get the record.
However, NOTIFY ALL is not guaranteed which threads will be notified
and the behavior contradict the conservative locking protocol.
The workaround in Elasql is to use a queue to maintain the request order,
and the waken thread will check the queue to see
whether it has the right to get the resource.
These wake-then-check behavior causes quite a few overhead.

Fifo pattern can be used to amortize the overhead because NOTIFY ALL is
eliminated. The key point is to let each thread wait on different objects
(proxy object). With the proper use of an atmoic queue
(concurrentLinkedQueue), a worker thread now can put a proxy object into the
queue, and wait on this proxy object until the head of the queue is itself.
After the previous worker thread commits, it will notify directly to the head object of the queue.